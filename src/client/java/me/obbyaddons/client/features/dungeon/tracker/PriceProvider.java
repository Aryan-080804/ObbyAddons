package me.obbyaddons.client.features.dungeon.tracker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public final class PriceProvider {

    private static final URI PRICES_URI =
            URI.create(
                    "https://api.starred.foo/prices"
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
                                        "ObbyAddons-StarredPrices"
                                );

                        thread.setDaemon(true);

                        return thread;
                    }
            );

    private static volatile Map<String, Double> prices =
            Map.of();

    private static volatile Map<String, String> sources =
            Map.of();

    private static volatile long lastSuccessfulRefresh =
            0L;

    private static boolean started =
            false;

    private PriceProvider() {
    }

    public static synchronized void start() {

        if (started) {
            return;
        }

        started = true;

        EXECUTOR.execute(
                PriceProvider::refreshSafely
        );

        EXECUTOR.scheduleAtFixedRate(
                PriceProvider::refreshSafely,
                REFRESH_MINUTES,
                REFRESH_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public static void refreshNow() {

        EXECUTOR.execute(
                PriceProvider::refreshSafely
        );
    }

    private static void refreshSafely() {

        try {

            refresh();

        } catch (Exception exception) {

            System.out.println(
                    "[ObbyAddons] Failed to refresh Starred prices. "
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

        HttpResponse<byte[]> response =
                HTTP_CLIENT.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        int status =
                response.statusCode();

        System.out.println(
                "[ObbyAddons] Starred status: "
                        + status
        );

        System.out.println(
                "[ObbyAddons] Starred content type: "
                        + response.headers()
                        .firstValue("Content-Type")
                        .orElse("unknown")
        );

        System.out.println(
                "[ObbyAddons] Starred content encoding: "
                        + response.headers()
                        .firstValue("Content-Encoding")
                        .orElse("none")
        );

        if (
                status < 200
                        || status >= 300
        ) {
            throw new IOException(
                    "Unexpected Starred response status: "
                            + status
            );
        }

        String contentEncoding =
                response.headers()
                        .firstValue("Content-Encoding")
                        .orElse("");

        String responseBody;

        if ("gzip".equalsIgnoreCase(contentEncoding)) {

            try (
                    GZIPInputStream gzipInputStream =
                            new GZIPInputStream(
                                    new ByteArrayInputStream(
                                            response.body()
                                    )
                            )
            ) {

                responseBody =
                        new String(
                                gzipInputStream.readAllBytes(),
                                StandardCharsets.UTF_8
                        );
            }

        } else {

            responseBody =
                    new String(
                            response.body(),
                            StandardCharsets.UTF_8
                    );
        }

        JsonReader reader =
                new JsonReader(
                        new StringReader(
                                responseBody
                        )
                );

        reader.setStrictness(
                Strictness.LENIENT
        );

        JsonElement parsed =
                JsonParser.parseReader(
                        reader
                );

        if (!parsed.isJsonObject()) {
            throw new IOException(
                    "Starred returned invalid JSON."
            );
        }

        JsonObject root =
                parsed.getAsJsonObject();

        Map<String, Double> nextPrices =
                new ConcurrentHashMap<>();

        Map<String, String> nextSources =
                new ConcurrentHashMap<>();

        JsonObject auctionHouse =
                root.has("auction_house")
                        && root.get("auction_house").isJsonObject()
                        ? root.getAsJsonObject("auction_house")
                        : null;

        JsonObject bazaar =
                root.has("bazaar")
                        && root.get("bazaar").isJsonObject()
                        ? root.getAsJsonObject("bazaar")
                        : null;

        int rawAuctionHouseCount =
                auctionHouse == null
                        ? 0
                        : auctionHouse.size();

        int rawBazaarCount =
                bazaar == null
                        ? 0
                        : bazaar.size();

        int auctionHouseCount = 0;
        int bazaarCount = 0;

        if (auctionHouse != null) {

            int beforeAuctionHouse =
                    nextPrices.size();

            loadAuctionHouse(
                    auctionHouse,
                    nextPrices,
                    nextSources
            );

            auctionHouseCount =
                    nextPrices.size()
                            - beforeAuctionHouse;
        }

        if (bazaar != null) {

            int beforeBazaar =
                    nextPrices.size();

            loadBazaar(
                    bazaar,
                    nextPrices,
                    nextSources
            );

            bazaarCount =
                    nextPrices.size()
                            - beforeBazaar;
        }

        if (nextPrices.isEmpty()) {

            throw new IOException(
                    "Starred returned zero usable prices."
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
                "[ObbyAddons] Starred auction house: "
                        + auctionHouseCount
                        + "/"
                        + rawAuctionHouseCount
                        + " usable."
        );

        System.out.println(
                "[ObbyAddons] Starred bazaar: "
                        + bazaarCount
                        + "/"
                        + rawBazaarCount
                        + " usable."
        );

        System.out.println(
                "[ObbyAddons] Loaded "
                        + prices.size()
                        + " unique Starred prices."
        );
    }

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

            String itemId =
                    entry.getKey();

            if (
                    itemId == null
                            || itemId.isBlank()
            ) {
                continue;
            }

            nextPrices.put(
                    itemId,
                    price
            );

            nextSources.put(
                    itemId,
                    "auction_house"
            );
        }
    }

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

            String itemId =
                    entry.getKey();

            if (
                    itemId == null
                            || itemId.isBlank()
            ) {
                continue;
            }

            /*
             * Preserve an auction-house price if the same
             * item is present in both sections.
             */
            if (
                    nextPrices.containsKey(
                            itemId
                    )
            ) {
                continue;
            }

            nextPrices.put(
                    itemId,
                    price
            );

            nextSources.put(
                    itemId,
                    "bazaar"
            );
        }
    }

    private static String resolvePriceId(
            String itemId
    ) {

        if (
                itemId == null
                        || itemId.isBlank()
        ) {
            return "";
        }

        String normalized =
                itemId.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return switch (normalized) {

            case "APEX_DRAGON_SHARD" ->
                    "SHARD_APEX_DRAGON";

            case "POWER_DRAGON_SHARD" ->
                    "SHARD_POWER_DRAGON";

            case "BONZO_SHARD" ->
                    "SHARD_BONZO";

            case "SCARF_SHARD" ->
                    "SHARD_SCARF";

            case "THORN_SHARD" ->
                    "SHARD_THORN";

            default ->
                    normalized;
        };
    }

    public static long getPriceCoins(
            String itemId
    ) {

        String resolvedId =
                resolvePriceId(
                        itemId
                );

        if (resolvedId.isBlank()) {
            return 0L;
        }

        Double price =
                prices.get(
                        resolvedId
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

        String resolvedId =
                resolvePriceId(
                        itemId
                );

        if (resolvedId.isBlank()) {
            return 0.0D;
        }

        return prices.getOrDefault(
                resolvedId,
                0.0D
        );
    }

    public static String getSource(
            String itemId
    ) {

        String resolvedId =
                resolvePriceId(
                        itemId
                );

        if (resolvedId.isBlank()) {
            return "";
        }

        return sources.getOrDefault(
                resolvedId,
                ""
        );
    }

    public static boolean hasPrice(
            String itemId
    ) {

        String resolvedId =
                resolvePriceId(
                        itemId
                );

        return !resolvedId.isBlank()
                && prices.containsKey(
                        resolvedId
                );
    }

    public static int getPriceCount() {

        return prices.size();
    }

    public static long getLastSuccessfulRefresh() {

        return lastSuccessfulRefresh;
    }

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
