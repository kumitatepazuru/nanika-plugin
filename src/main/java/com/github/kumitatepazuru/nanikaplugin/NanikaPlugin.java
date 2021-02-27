package com.github.kumitatepazuru.nanikaplugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class NanikaPlugin extends JavaPlugin implements Listener {
    List<String> youkoso_msg = new ArrayList<>(Arrays.asList("kon(*^__^*)tya", "（へ。へ）y", "こ(´∀｀*）ん", "(/*^^)/ﾊｯﾛ-!!", "a(*^。^*)hello////"));
    List<Inventory> die_inventory = new ArrayList<>();

    @Override
    public void onEnable() {
        getLogger().info("なにかプラグインが有効になりました。");

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("なにかプラグインが無効になりました。");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int index = new Random().nextInt(youkoso_msg.size());
        String result = youkoso_msg.get(index);
        getServer().broadcastMessage("§2§o" + result + "§r§l " + player.getName());
        player.sendTitle("§9なにかサーバー！", "§oPowered by Riku Ueda", 20, 40, 20);
        Bukkit.dispatchCommand(player, "cp menu item " + player.getName());
    }

    @EventHandler
    public void OnDeath(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();
        assert player != null;
        Location pos = player.getLocation();
        getServer().broadcastMessage("§c§o" + player.getName() + "が死にました。§r\n§f§l死亡場所:[ X:" + pos.getBlockX() + " Y:" + pos.getBlockY() + " Z:" + pos.getBlockZ() + " ]");
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                player.getWorld().spawnParticle(
                        Particle.END_ROD,
                        pos,
                        100,
                        0.1,
                        255,
                        0.1,
                        0
                );
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent("§f§l死亡場所 [ X:" + pos.getBlockX() + " Y:" + pos.getBlockY() + " Z:" + pos.getBlockZ() + " ]"));
            }
        }.runTaskTimer(this, 0, 1L);
        Bukkit.getServer().getScheduler().runTaskLater(this, task::cancel, 6000L);
        Block block = pos.getBlock();
        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(String.valueOf(die_inventory.size()));
        item.setItemMeta(itemMeta);
        chest.getBlockInventory().setItem(0,item);
        Inventory inventory = Bukkit.createInventory(null,36);
        for (int i=0;i<player.getInventory().getSize();i++){
            ItemStack tmp = player.getInventory().getItem(i);
            if (tmp != null) {
                inventory.setItem(i, tmp);
            }
        }
        die_inventory.add(inventory);
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e){
        if (e.getInventory().getHolder() != null || e.getInventory().getHolder() != null){
            HumanEntity player = e.getPlayer();
            ItemStack item = e.getInventory().getItem(0);
            assert item != null;
            player.openInventory(die_inventory.get(Integer.parseInt(Objects.requireNonNull(item.getItemMeta()).getDisplayName())));
        }
    }
}
