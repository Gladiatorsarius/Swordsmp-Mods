package combat.log.discord.util;

import java.util.Map;

/**
 * Simple template formatter for {placeholders}.
 */
public final class MessageFormatter {
    private MessageFormatter() {
    }

    public static String format(String template, Map<String, String> values) {
        if (template == null || values == null || values.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = "{" + entry.getKey() + "}";
            String value = entry.getValue() == null ? "" : entry.getValue();
            result = result.replace(key, value);
        }

        return result;
    }
}
