package org.heypers.operatorLogin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class OperatorLogin extends JavaPlugin {
    private AuthService authService;
    private LoginManager loginManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.authService = new AuthService(getConfig());
        this.loginManager = new LoginManager(authService);

        Bukkit.getPluginManager().registerEvents(new LoginListener(loginManager), this);

        getCommand("login").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player p)) return true;
            if (args.length != 1) {
                p.sendMessage("§cИспользование: /login <пароль>");
                return true;
            }
            loginManager.tryLogin(p, args[0]);
            saveConfig();
            return true;
        });

        getCommand("register").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player p)) return true;
            if (args.length != 1) {
                p.sendMessage("§cИспользование: /register <пароль>");
                return true;
            }
            loginManager.tryRegister(p, args[0]);
            saveConfig();
            return true;
        });
    }

    @Override
    public void onDisable() {
        saveConfig();
    }
}

class AuthService {
    private final FileConfiguration config;

    public AuthService(FileConfiguration config) {
        this.config = config;
    }

    public boolean hasPassword(UUID uuid) {
        return config.contains("passwords." + uuid);
    }

    public void setPassword(UUID uuid, String pass) {
        config.set("passwords." + uuid, pass);
    }

    public boolean checkPassword(UUID uuid, String pass) {
        return pass.equals(config.getString("passwords." + uuid));
    }
}

class LoginManager {
    private final AuthService authService;
    private final Set<UUID> notLogged = new HashSet<>();

    public LoginManager(AuthService authService) {
        this.authService = authService;
    }

    public void handleJoin(Player p) {
        notLogged.add(p.getUniqueId());

        p.setWalkSpeed(0f);
        p.setFlySpeed(0f);

        if (!authService.hasPassword(p.getUniqueId())) {
            p.sendMessage("§eУстановите пароль: /register <пароль>");
        } else {
            p.sendMessage("§eВведите пароль: /login <пароль>");
        }
    }

    public void logout(Player p) {
        notLogged.remove(p.getUniqueId());
    }

    public boolean isLogged(Player p) {
        return !notLogged.contains(p.getUniqueId());
    }

    public void tryRegister(Player p, String pass) {
        if (authService.hasPassword(p.getUniqueId())) {
            p.sendMessage("§cВы уже зарегистрированы.");
            return;
        }
        authService.setPassword(p.getUniqueId(), pass);
        notLogged.remove(p.getUniqueId());

        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.1f);

        p.sendMessage("§aПароль установлен! Вы вошли.");
    }

    public void tryLogin(Player p, String pass) {
        if (!authService.hasPassword(p.getUniqueId())) {
            p.sendMessage("§cВы не зарегистрированы. /register <пароль>");
            return;
        }
        if (authService.checkPassword(p.getUniqueId(), pass)) {
            notLogged.remove(p.getUniqueId());

            p.setWalkSpeed(0.2f);
            p.setFlySpeed(0.1f);

            p.sendMessage("§aУспешный вход!");
        } else {
            p.sendMessage("§cНеверный пароль!");
        }
    }
}

class LoginListener implements Listener {
    private final LoginManager loginManager;

    public LoginListener(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    private boolean blockIfNotLogged(Player p) {
        if (!loginManager.isLogged(p)) {
            return true;
        }
        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        loginManager.handleJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        loginManager.logout(e.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!loginManager.isLogged(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (blockIfNotLogged(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (blockIfNotLogged(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (blockIfNotLogged(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (blockIfNotLogged(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p && blockIfNotLogged(p)) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player p && blockIfNotLogged(p)) e.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (blockIfNotLogged(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (loginManager.isLogged(p)) return;

        String m = e.getMessage().toLowerCase();
        if (m.startsWith("/login") || m.startsWith("/register")) return;

        e.setCancelled(true);
        p.sendMessage("§cДоступно только: /login, /register");
    }
}
