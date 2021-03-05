package com.github.kumitatepazuru.nanikaplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NanikaPlugin extends JavaPlugin implements Listener {
    List<String> hello_msg = new ArrayList<>(Arrays.asList("kon(*^__^*)tya", "（へ。へ）y", "こ(´∀｀*）ん", "(/*^^)/ﾊｯﾛ-!!", "a(*^。^*)hello////"));

    @Override
    public void onEnable() {
        getLogger().info("なにかプラグインが有効になりました。");
        Bukkit.dispatchCommand(getServer().getConsoleSender(), "gamerule keepInventory true");
        getServer().broadcastMessage("[SERVER/DEBUG] set keepInventory gamerule");
        getServer().getPluginManager().registerEvents(this, this);
        die o_die = new die(this);
        o_die.onEnable();
        getServer().getPluginManager().registerEvents(o_die, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("なにかプラグインが無効になりました。");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int index = new Random().nextInt(hello_msg.size());
        String result = hello_msg.get(index);
        getServer().broadcastMessage("§2§o" + result + "§r§l " + player.getDisplayName());
        player.sendTitle("§9なにかサーバー！", "§oPowered by Riku Ueda", 20, 40, 20);
        Bukkit.dispatchCommand(player, "cp menu item " + player.getDisplayName());
    }
}

