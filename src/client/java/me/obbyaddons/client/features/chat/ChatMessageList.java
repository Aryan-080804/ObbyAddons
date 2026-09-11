package me.obbyaddons.client.features.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ChatMessageList {

    private final List<String> patterns;

    private ChatMessageList(List<String> patterns) {
        this.patterns = patterns;
    }

    public static ChatMessageList load(String resourcePath) {
        List<String> patterns = new ArrayList<>();

        try (InputStream stream =
                     ChatMessageList.class.getResourceAsStream(resourcePath)) {

            if (stream == null) {
                System.err.println(
                        "[ObbyAddons] COULD NOT FIND: " + resourcePath
                );

                return new ChatMessageList(Collections.emptyList());
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            stream,
                            StandardCharsets.UTF_8
                    ))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    patterns.add(line);
                }
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        System.out.println(
                "[ObbyAddons] Loaded "
                        + patterns.size()
                        + " chat patterns from "
                        + resourcePath
        );

        return new ChatMessageList(patterns);
    }

    public boolean matches(String message) {
        for (String pattern : patterns) {
            if (message.contains(pattern)) {

                System.out.println(
                        "[ObbyAddons] Chat rule matched: "
                                + pattern
                );

                return true;
            }
        }

        return false;
    }

    public List<String> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }
}