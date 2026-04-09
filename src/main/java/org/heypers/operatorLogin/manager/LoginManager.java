package org.heypers.operatorLogin.manager;

import org.bukkit.entity.Player;
import org.heypers.operatorLogin.auth.AuthService;
import java.util.*;


public class LoginManager {
    private final AuthService authService;
    private final Set<UUID> notLogged = new HashSet<>();


    public LoginManager(AuthService authService) {
        this.authService = authService;
    }


    public boolean needsLogin(Player p, boolean authOnlyOp) {
        return (!p.isOp() && authOnlyOp) ? false : true;
    }


    public void handleJoin(Player p, boolean authOnlyOp) {
        if (!needsLogin(p, authOnlyOp)) return;
        notLogged.add(p.getUniqueId());
        freeze(p);
        authService.migrateIfNeeded(p.getUniqueId());
        if (!authService.hasPassword(p.getUniqueId()))
            p.sendMessage("§eУстановите пароль: /register <пароль>");
        else
            p.sendMessage("§eВведите пароль: /login <пароль>");
    }


    public boolean isLogged(Player p) {
        return !notLogged.contains(p.getUniqueId());
    }


    public void logout(Player p) {
        notLogged.remove(p.getUniqueId());
    }


    public void tryRegister(Player p, String pass) {
        if (authService.hasPassword(p.getUniqueId())) {
            p.sendMessage("§cВы уже зарегистрированы.");
            return;
        }
        authService.setPassword(p.getUniqueId(), pass);
        notLogged.remove(p.getUniqueId());
        unfreeze(p);
        p.sendMessage("§aПароль установлен! Вы вошли.");
    }


    public void tryLogin(Player p, String pass) {
        if (!authService.hasPassword(p.getUniqueId())) {
            p.sendMessage("§cВы не зарегистрированы. /register <пароль>");
            return;
        }
        if (authService.checkPassword(p.getUniqueId(), pass)) {
            notLogged.remove(p.getUniqueId());
            unfreeze(p);
            p.sendMessage("§aУспешный вход!");
        } else {
            p.sendMessage("§cНеверный пароль!");
        }
    }


    public void freeze(Player p) {
        p.setWalkSpeed(0f);
        p.setFlySpeed(0f);
    }


    public void unfreeze(Player p) {
        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.1f);
    }
}