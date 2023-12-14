package com.articreep.pocketknife;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.util.*;

public class ExplosiveGold extends PocketknifeSubcommand implements Listener, PocketknifeConfigurable {


    private boolean enabled = false;

    @EventHandler
    public void onKill(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!enabled) return;

        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        // if victim is dead
        if (victim.getHealth() - event.getFinalDamage() < 0) {
            for (int i = 0; i < 3; i++) {
                dropGold(victim);
            }
        }
    }
    private void dropGold(Player victim) {
        Vector v = Utils.randomVector(0.2);
        v.setY(0.2);
        Item gold = victim.getWorld().dropItem(victim.getLocation().add(0, 1, 0), createGold());
        gold.setVelocity(v);
    }

    @EventHandler
    public void onXPPickup(PlayerPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (!enabled) return;

        Player player = event.getPlayer();
        if (event.getItem().getItemStack().getItemMeta().getDisplayName().equals("Dropped Golden Ingot")) {
            event.setCancelled(true);
            event.getItem().remove();
            player.playSound(player.getLocation(), Sound.ARROW_HIT, 1, 1);
            player.sendMessage("Pretend you got absorption here lol");
        }
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        enabled = config.getBoolean("explosivegold");
        config.set("explosivegold", enabled);
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) {
                sendDescriptionMessage(sender);
                sendSyntaxMessage(sender);
            } else {
                if (args[0].equalsIgnoreCase("toggle")) {
                    enabled = !enabled;
                    sender.sendMessage(ChatColor.RED + "ExplosiveGold toggled " +
                            Utils.booleanStatus(enabled));
                } else {
                    sendSyntaxMessage(sender);
                }
                Pocketknife.getInstance().getConfig().set("explosivegold", enabled);
                Pocketknife.getInstance().saveConfig();
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add("toggle");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        return completions;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife explosivegold toggle";
    }

    @Override
    public String getDescription() {
        return ChatColor.AQUA + "Players drop gold on kill.";
    }

    private ItemStack createGold() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        // this is in no way a good ID it's just a jank way of doing it
        meta.setDisplayName("Dropped Golden Ingot");
        item.setItemMeta(meta);
        return item;
    }
}

