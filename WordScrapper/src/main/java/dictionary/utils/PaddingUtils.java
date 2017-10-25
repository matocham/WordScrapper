package dictionary.utils;

/**
 * Created by Mateusz on 02.04.2017.
 */
public class PaddingUtils {
    public static String pad(String value, int length) {
        return pad(value, length, " ");
    }

    private static String pad(String value, int length, String with) {
        StringBuilder result = new StringBuilder(length);
        result.append(fill(Math.max(0, length - value.length()), with));
        result.append(value);

        return result.toString();
    }

    public static String padLeft(String value, int length) {
        return padLeft(value, length, " ");
    }

    private static String padLeft(String value, int length, String with) {
        StringBuilder result = new StringBuilder(length);
        result.append(value);
        result.append(fill(Math.max(0, length - value.length()), with));
        return result.toString();
    }

    private static String fill(int length, String with) {
        StringBuilder sb = new StringBuilder(length);
        while (sb.length() < length) {
            sb.append(with);
        }
        return sb.toString();
    }
}
