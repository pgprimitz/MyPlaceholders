package dev.nutellim.MyPlaceholders.utilities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JavaUtil {

    public String join(String[] args, int start) {
        StringBuilder builder = new StringBuilder();

        for (int i = start; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }

        return builder.toString().trim();
    }
}
