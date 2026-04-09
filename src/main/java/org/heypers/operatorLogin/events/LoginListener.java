package org.heypers.operatorLogin.events;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.*;
import org.heypers.operatorLogin.auth.AuthService;
import org.heypers.operatorLogin.manager.LoginManager;
import org.heypers.operatorLogin.OperatorLogin;

public class LoginListener implements Listener {
    private final OperatorLogin plugin;
    private final LoginManager loginManager;
    private final AuthService authService;
    private final boolean authOnlyOp;
    private final int kickTimeout;


    public LoginListener(OperatorLogin pl, LoginManager lm, AuthService as, boolean onlyOp, int timeout) {
        this.plugin = pl;
        this.loginManager = lm;
        this.authService = as;
        this.authOnlyOp = onlyOp;
        this.kickTimeout = timeout;
    }


    private boolean block(Player p) {
        return !loginManager.isLogged(p);
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        loginManager.handleJoin(p, authOnlyOp);


        if (!loginManager.isLogged(p) && loginManager.needsLogin(p, authOnlyOp)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!loginManager.isLogged(p)) p.kick(Component.text("Время авторизации вышло!"));
            }, kickTimeout * 20L);
        }
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        loginManager.logout(e.getPlayer());
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (block(e.getPlayer())) e.setCancelled(true);
    }


    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (block(e.getPlayer())) e.setCancelled(true);
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (block(e.getPlayer())) e.setCancelled(true);
    }


    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (block(e.getPlayer())) e.setCancelled(true);
    }


    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (block(e.getPlayer())) e.setCancelled(true);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p && block(p)) e.setCancelled(true);
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player p && block(p)) e.setCancelled(true);
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (block(e.getPlayer())) e.setCancelled(true);
    }


    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (loginManager.isLogged(p)) return;
        String msg = e.getMessage().toLowerCase();
    }
}