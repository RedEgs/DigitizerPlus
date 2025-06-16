package net.redegs.digitizerplus.python;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonErrorResolver {
    private static String RUNTIME_ERROR = "<class 'RuntimeError'>";
    private static String NAME_ERROR = "<class 'NameError'>";
    private static String SYNTAX_ERROR = "<class 'SyntaxError'>";

    public static HashMap<Integer, String> resolveError(String stacktrace, String errorMsg) {
        HashMap<Integer, String> lineError = new HashMap<>();
        Pattern pattern;

        if (errorMsg.startsWith(RUNTIME_ERROR) || errorMsg.startsWith(NAME_ERROR)) {
            pattern = Pattern.compile("\\(<string>:(\\d+)\\)");
        } else {
            return lineError;
        }

        Matcher matcher = pattern.matcher(stacktrace);

        if (matcher.find()) {
            String lineStr = matcher.group(1);  // This is just the digits
            int line = Integer.parseInt(lineStr);  // Safe now


            lineError.put(line-1, errorMsg);
            return lineError;
        } else {
            System.out.println("No line number found. Full error:");
            System.out.println(stacktrace);
        }

        return lineError;
    }
}
