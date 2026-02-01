package com.jets.chat.client.util;

import java.io.*;
import java.util.Properties;

public class SessionManager {
    private static final String FILE_NAME = System.getProperty("user.home") + File.separator
            + ".chatapp.session";

    public static void saveSession(long userId, String sessionId) {
        Properties props = new Properties();
        props.setProperty("userId", String.valueOf(userId));
        props.setProperty("sessionId", sessionId);

        try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
            props.store(out, "ChatApp User Session");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties loadSession() {
        Properties props = new Properties();
        File file = new File(FILE_NAME);
        if (!file.exists())
            return null;

        try (FileInputStream in = new FileInputStream(FILE_NAME)) {
            props.load(in);
            return props;
        } catch (IOException e) {
            return null;
        }
    }

    public static void clearSession() {
        new File(FILE_NAME).delete();
    }
}