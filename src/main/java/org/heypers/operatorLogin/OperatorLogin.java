package org.heypers.operatorLogin;


import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.heypers.operatorLogin.auth.AuthService;
import org.heypers.operatorLogin.manager.LoginManager;
import org.heypers.operatorLogin.events.LoginListener;
import org.heypers.operatorLogin.commands.LoginCommand;
import org.heypers.operatorLogin.commands.RegisterCommand;


public final class OperatorLogin extends JavaPlugin {
    private AuthService authService;
    private LoginManager loginManager;
    private boolean authOnlyOp;
    private int kickTimeout;


    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.authOnlyOp = getConfig().getBoolean("authOnlyOp", true);
        this.kickTimeout = getConfig().getInt("kickTimeout", 60);

        this.authService = new AuthService(getConfig());
        this.loginManager = new LoginManager(authService);

        getCommand("login").setExecutor(new LoginCommand(loginManager));
        getCommand("register").setExecutor(new RegisterCommand(loginManager));

        Bukkit.getPluginManager().registerEvents(
                new LoginListener(this, loginManager, authService, authOnlyOp, kickTimeout),
                this
        );
    }


    @Override
    public void onDisable() {
        saveConfig();
    }
}
