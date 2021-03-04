package com.github.kumitatepazuru.nanikaplugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class NanikaPlugin extends JavaPlugin implements Listener {
    List<String> youkoso_msg = new ArrayList<>(Arrays.asList("kon(*^__^*)tya", "（へ。へ）y", "こ(´∀｀*）ん", "(/*^^)/ﾊｯﾛ-!!", "a(*^。^*)hello////"));
    List<Inventory> die_inventory = new ArrayList<>();
    List<BukkitTask> die_task = new ArrayList<>();
    List<BukkitTask> die_task_after = new ArrayList<>();
    /* TODO:configデータからアイテム消滅時間を取得 */
    int count;
    File die_f = new File(getDataFolder().getAbsolutePath(), "die.yml");
    FileConfiguration die;

    private int die_msg_task(Player player, Location pos, String id) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (player != null) {
                    player.getWorld().spawnParticle(
                            Particle.END_ROD,
                            pos,
                            100,
                            0.1,
                            255,
                            0.1,
                            0
                    );
                    count++;
                    int t = 5 * 60 * 20 - count;
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§f§l死亡場所 [ X:" + pos.getBlockX() + " Y:" + pos.getBlockY() + " Z:" + pos.getBlockZ() +
                            " ] §r§2§o残り " + (int) Math.ceil(t / 60.0 / 20.0 - 1) + "分" + (int) Math.ceil(t / 20.0 % 60.0 - 1) + "秒"));
                }
            }
        }.runTaskTimer(this, 1, 1L);
        die_task.add(task);
        int task_size = die_task.size() - 1;
        die_task_after.add(Bukkit.getServer().getScheduler().runTaskLater(this, () -> {
            die_task.get(task_size).cancel();
            die_task.set(task_size, null);
            die_inventory.get(task_size).clear();
            die_task_after.set(task_size, null);
            assert player != null;
            player.sendMessage("§c§l死亡場所 [ X:" + pos.getBlockX() + " Y:" + pos.getBlockY() + " Z:" + pos.getBlockZ() + " ]の死亡アイテムが消滅しました!!");
        }, 20 * 60 * 5));
        return task_size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        getLogger().info("なにかプラグインが有効になりました。");
        Bukkit.dispatchCommand(getServer().getConsoleSender(), "gamerule keepInventory true");
        getServer().broadcastMessage("[SERVER/DEBUG] set keepInventory gamerule");
        getServer().getPluginManager().registerEvents(this, this);
        die = YamlConfiguration.loadConfiguration(die_f);
        ConfigurationSection die_conf = die.getConfigurationSection("die");
        if (die_conf != null) {
            for (String i : die_conf.getKeys(false)) {
                Inventory inventory = Bukkit.createInventory(null, 36, die.getString("die." + i + ".name") + "の死亡時のアイテム");
                inventory.setContents(((List<ItemStack>) Objects.requireNonNull(die.get("die." + i + ".content"))).toArray(new ItemStack[0]));
                die_inventory.add(inventory);
                Location pos = die.getLocation("die." + i + ".pos");
                assert pos != null;
                die_msg_task(null, pos, die.getString("die." + i + ".id"));
            }
        }
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
        getServer().broadcastMessage("§2§o" + result + "§r§l " + player.getDisplayName());
        player.sendTitle("§9なにかサーバー！", "§oPowered by Riku Ueda", 20, 40, 20);
        Bukkit.dispatchCommand(player, "cp menu item " + player.getDisplayName());
    }

    @EventHandler
    public void OnDeath(PlayerDeathEvent event) throws IOException {
        Player player = event.getEntity().getPlayer();
        assert player != null;
        Location pos = player.getLocation();
        getServer().broadcastMessage("§c§o" + player.getDisplayName() + "が死にました。§r\n§f§l死亡場所:[ X:" + pos.getBlockX() + " Y:" + pos.getBlockY() + " Z:" + pos.getBlockZ() + " ]");
        count = 0;
        int task_size = die_msg_task(player, pos, player.getUniqueId().toString());
        Block block = pos.getBlock();
        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();

        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName("DEATH:" + player.getUniqueId());
        item.setItemMeta(itemMeta);
        chest.getBlockInventory().setItem(0, item);

        item = new ItemStack(Material.STONE);
        itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(String.valueOf(die_inventory.size()));
        item.setItemMeta(itemMeta);
        chest.getBlockInventory().setItem(1, item);

        Inventory inventory = Bukkit.createInventory(null, 36, player.getDisplayName() + "の死亡時のアイテム");
        int size = inventory.getSize();
        die.set("die." + task_size + ".id", player.getUniqueId().toString());
        die.set("die." + task_size + ".name", player.getDisplayName());
        die.set("die." + task_size + ".pos", pos);
        /* TODO: アーマー保存も実現　*/
        for (int i = 9; i < size; i++) {
            ItemStack tmp = player.getInventory().getItem(i);
            if (tmp != null) {
                inventory.setItem(i - 9, tmp);
            }
        }
        for (int i = 0; i < 9; i++) {
            ItemStack tmp = player.getInventory().getItem(i);
            if (tmp != null) {
                inventory.setItem(i + size - 9, tmp);
            }
        }
        die_inventory.add(inventory);
        player.getInventory().clear();
        die.set("die." + task_size + ".content", inventory.getContents());
        die.set("die." + task_size + ".count", 5 * 60 * 20);
        die.set("die." + task_size + ".counting", true);
        die.save(die_f);
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        getServer().getOnlinePlayers();
        if (inventory.getHolder() != null) {
            HumanEntity player = event.getPlayer();
            Bukkit.getServer().getScheduler().runTaskLater(this, () -> {
                ItemStack item = inventory.getItem(0);
                if (item != null) {
                    if (item.getType() == Material.STONE && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals("DEATH:" + player.getUniqueId())) {
                        item = inventory.getItem(1);
                        if (item != null) {
                            if (item.getType() == Material.STONE) {
                                int index = Integer.parseInt(Objects.requireNonNull(item.getItemMeta()).getDisplayName());
                                player.closeInventory();
                                player.openInventory(die_inventory.get(index));
                                if (die_task.get(index) != null) {
                                    die_task.get(index).cancel();
                                    die_task_after.get(index).cancel();
                                }
                            }
                        }
                    } else if (item.getType() == Material.STONE && Objects.requireNonNull(item.getItemMeta()).getDisplayName().contains("DEATH:")){
                        player.closeInventory();
                        player.sendMessage("このチェストは他の人のチェストです！");
                    }
                }
            }, 1L);
        } else {
            inventory.getHolder();
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            Inventory inventory = chest.getBlockInventory();
            ItemStack item = inventory.getItem(0);
            if (item != null) {
                if (item.getType() == Material.STONE && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals("DEATH:" + player.getUniqueId())) {
                    item = inventory.getItem(1);
                    if (item != null) {
                        if (item.getType() == Material.STONE) {
                            inventory.clear();
                            block.setType(Material.AIR);
                            int index = Integer.parseInt(Objects.requireNonNull(item.getItemMeta()).getDisplayName());
                            if (die_task.get(index) != null) {
                                die_task.get(index).cancel();
                                die_task_after.get(index).cancel();
                            }
                        }
                    }
                }
            }
        }
    }
}
