package com.jets.chat.common.util;

public class Functions {
    public static String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "?";

        String[] parts = name.split("\\s+");
        StringBuilder initials = new StringBuilder();

        initials.append(parts[0].charAt(0));

        if (parts.length > 1) {
            initials.append(parts[parts.length - 1].charAt(0));
        }

        return initials.toString().toUpperCase();
    }
}
