package org.heypers.operatorLogin.auth;

import org.bukkit.configuration.file.FileConfiguration;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;


public class AuthService {
    private final FileConfiguration config;


    public AuthService(FileConfiguration config) {
        this.config = config;
    }


    public boolean hasPassword(UUID uuid) {
        return config.contains("passwords." + uuid);
    }


    public boolean isHashed(String stored) {
        return stored.startsWith("HASH:");
    }


    public void migrateIfNeeded(UUID uuid) {
        String stored = config.getString("passwords." + uuid);
        if (stored == null) return;
        if (!isHashed(stored)) setPassword(uuid, stored);
    }


    public void setPassword(UUID uuid, String raw) {
        String hash = hash(raw);
        config.set("passwords." + uuid, "HASH:" + hash);
    }


    public boolean checkPassword(UUID uuid, String input) {
        String stored = config.getString("passwords." + uuid);
        if (stored == null) return false;
        if (!isHashed(stored)) {
            boolean ok = stored.equals(input);
            if (ok) setPassword(uuid, input);
            return ok;
        }
        return stored.equals("HASH:" + hash(input));
    }


    private String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(raw.getBytes()));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}