package me.obbyaddons.client.features.dungeon.tracker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AthenPriceProvider {

    private static final URI PRICES_URI =
            URI.create(
                    "https://athen.aerii.xyz/prices"
            );

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(10);

    private static final long REFRESH_MINUTES =
            10L;

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(
                            Duration.ofSeconds(5)
                    )
                    .build();

    private static final ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(
                    runnable -> {

                        Thread thread =
                                new Thread(
                                        runnable,
                                        "ObbyAddons-AthenPrices"
                                );

                        thread.setDaemon(true);

                        return thread;
                    }
            );

    /*
     * Current live prices.
     *
     * The map is replaced after every successful
     * Athen refresh.
     */
    private static volatile Map<String, Double> prices =
            Map.of();

    private static volatile Map<String, String> sources =
            Map.of();

    private static volatile long lastSuccessfulRefresh =
            0L;

    private static boolean started =
            false;

    private AthenPriceProvider() {
    }

    // =========================
    // START / REFRESH
    // =========================

    public static synchronized void start() {

        if (started) {
            return;
        }

        started = true;

        /*
         * Fetch immediately when Minecraft starts.
         */
        EXECUTOR.execute(
                AthenPriceProvider::refreshSafely
        );

        /*
         * Then refresh every 10 minutes.
         */
        EXECUTOR.scheduleAtFixedRate(
                AthenPriceProvider::refreshSafely,
                REFRESH_MINUTES,
                REFRESH_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public static void refreshNow() {

        EXECUTOR.execute(
                AthenPriceProvider::refreshSafely
        );
    }

    private static void refreshSafely() {

        try {

            refresh();

        } catch (Exception exception) {

            System.out.println(
                    "[ObbyAddons] Failed to refresh Athen prices. "
                            + "Keeping previous prices."
            );

            exception.printStackTrace();
        }
    }

    private static void refresh()
            throws IOException, InterruptedException {

        HttpRequest request =
                HttpRequest.newBuilder(
                                PRICES_URI
                        )
                        .timeout(
                                REQUEST_TIMEOUT
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                HTTP_CLIENT.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        int status =
                response.statusCode();

        if (
                status < 200
                        || status >= 300
        ) {
            throw new IOException(
                    "Unexpected Athen response status: "
                            + status
            );
        }

        JsonObject root =
                JsonParser.parseString(
                                response.body()
                        )
                        .getAsJsonObject();

        Map<String, Double> nextPrices =
                new ConcurrentHashMap<>();

        Map<String, String> nextSources =
                new ConcurrentHashMap<>();

        loadAuctionHouse(
                root.getAsJsonObject(
                        "auction_house"
                ),
                nextPrices,
                nextSources
        );

        loadBazaar(
                root.getAsJsonObject(
                        "bazaar"
                ),
                nextPrices,
                nextSources
        );

        /*
         * Never wipe a working cache because Athen
         * returned an empty/invalid response.
         */
        if (nextPrices.isEmpty()) {

            throw new IOException(
                    "Athen returned zero usable prices."
            );
        }

        prices =
                Map.copyOf(
                        nextPrices
                );

        sources =
                Map.copyOf(
                        nextSources
                );

        lastSuccessfulRefresh =
                System.currentTimeMillis();

        System.out.println(
                "[ObbyAddons] Loaded "
                        + prices.size()
                        + " Athen prices."
        );
    }

    // =========================
    // AUCTION HOUSE
    // =========================

    private static void loadAuctionHouse(
            JsonObject section,
            Map<String, Double> nextPrices,
            Map<String, String> nextSources
    ) {
        if (section == null) {
            return;
        }

        for (
                Map.Entry<String, JsonElement> entry
                : section.entrySet()
        ) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }

            JsonObject priceObject =
                    entry.getValue()
                            .getAsJsonObject();

            Double lbin =
                    positiveNumber(
                            priceObject,
                            "lbin"
                    );

            Double p3d =
                    positiveNumber(
                            priceObject,
                            "p3d"
                    );

            Double p7d =
                    positiveNumber(
                            priceObject,
                            "p7d"
                    );

            Double price =
                    firstPositive(
                            lbin,
                            p3d,
                            p7d
                    );

            if (
                    price == null
                            || price <= 0.0D
            ) {
                continue;
            }

            nextPrices.put(
                    entry.getKey(),
                    price
            );

            nextSources.put(
                    entry.getKey(),
                    "auction_house"
            );
        }
    }

    // =========================
    // BAZAAR
    // =========================

    private static void loadBazaar(
            JsonObject section,
            Map<String, Double> nextPrices,
            Map<String, String> nextSources
    ) {
        if (section == null) {
            return;
        }

        for (
                Map.Entry<String, JsonElement> entry
                : section.entrySet()
        ) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }

            JsonObject priceObject =
                    entry.getValue()
                            .getAsJsonObject();

            Double instantSell =
                    positiveNumber(
                            priceObject,
                            "is"
                    );

            Double sellOffer =
                    positiveNumber(
                            priceObject,
                            "ts"
                    );

            Double instantBuy =
                    positiveNumber(
                            priceObject,
                            "ib"
                    );

            Double buyOrder =
                    positiveNumber(
                            priceObject,
                            "tb"
                    );

            Double price =
                    firstPositive(
                            instantSell,
                            sellOffer,
                            instantBuy,
                            buyOrder
                    );

            if (
                    price == null
                            || price <= 0.0D
            ) {
                continue;
            }

            /*
             * If an auction price already exists,
             * keep it.
             */
            if (
                    !nextPrices.containsKey(
                            entry.getKey()
                    )
            ) {
                nextPrices.put(
                        entry.getKey(),
                        price
                );

                nextSources.put(
                        entry.getKey(),
                        "bazaar"
                );
            }
        }
    }

    // =========================
    // PRICE LOOKUP
    // =========================

    public static long getPriceCoins(
            String itemId
    ) {
        if (
                itemId == null
                        || itemId.isBlank()
        ) {
            return 0L;
        }

        Double price =
                prices.get(
                        itemId
                );

        if (price == null) {
            return 0L;
        }

        return Math.max(
                0L,
                Math.round(
                        price
                )
        );
    }

    public static double getPrice(
            String itemId
    ) {
        if (
                itemId == null
                        || itemId.isBlank()
        ) {
            return 0.0D;
        }

        return prices.getOrDefault(
                itemId,
                0.0D
        );
    }

    public static String getSource(
            String itemId
    ) {
        if (
                itemId == null
                        || itemId.isBlank()
        ) {
            return "";
        }

        return sources.getOrDefault(
                itemId,
                ""
        );
    }

    public static boolean hasPrice(
            String itemId
    ) {
        return itemId != null
                && prices.containsKey(
                        itemId
                );
    }

    public static int getPriceCount() {

        return prices.size();
    }

    public static long getLastSuccessfulRefresh() {

        return lastSuccessfulRefresh;
    }

    // =========================
    // JSON HELPERS
    // =========================

    private static Double positiveNumber(
            JsonObject object,
            String key
    ) {
        if (object == null) {
            return null;
        }

        JsonElement element =
                object.get(
                        key
                );

        if (
                element == null
                        || !element.isJsonPrimitive()
                        || !element
                        .getAsJsonPrimitive()
                        .isNumber()
        ) {
            return null;
        }

        double value =
                element.getAsDouble();

        if (value <= 0.0D) {
            return null;
        }

        return value;
    }

    private static Double firstPositive(
            Double... values
    ) {
        for (Double value : values) {

            if (
                    value != null
                            && value > 0.0D
            ) {
                return value;
            }
        }

        return null;
    }
}