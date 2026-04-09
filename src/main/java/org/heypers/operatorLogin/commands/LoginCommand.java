package org.heypers.operatorLogin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.heypers.operatorLogin.manager.LoginManager;


public class LoginCommand implements CommandExecutor {
    private final LoginManager loginManager;


    public LoginCommand(LoginManager m) { this.loginManager = m; }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (args.length != 1) {
            p.sendMessage("§cИспользование: /login <пароль>");
            return true;
        }
        loginManager.tryLogin(p, args[0]);
        return true;
    }
}