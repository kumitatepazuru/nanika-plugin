package com.github.kumitatepazuru.nanikaplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class NanikaPlugin extends JavaPlugin implements Listener {
    List<String> youkoso_msg = new ArrayList<>(Arrays.asList("kon(*^__^*)tya","（へ。へ）y","こ(´∀｀*）ん","(/*^^)/ﾊｯﾛ-!!","a(*^。^*)hello////"));

    @Override
    public void onEnable() {
        getLogger().info("なにかプラグインが有効になりました。");

        getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        getLogger().info("なにかプラグインが無効になりました。");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        int index = new Random().nextInt(youkoso_msg.size());
        String result = youkoso_msg.get(index);
        getServer().broadcastMessage("§2§o"+result+"§r§l "+player.getName());
        player.sendTitle("§9なにかサーバー！","§oPowered by Riku Ueda",20,40,20);
        Bukkit.dispatchCommand(player, "cp menu item "+player.getName());
    }

    @EventHandler
    public void OnDeath(PlayerDeathEvent event){
        Player player = event.getEntity().getPlayer();
        assert player != null;
        Location pos = player.getLocation();
        player.sendMessage("§c§oあなたは死にました。§r\n§f§l死亡場所:[ X:"+pos.getX()+" Y:"+pos.getY()+" Z:"+pos.getZ()+" ]");
    }
}
