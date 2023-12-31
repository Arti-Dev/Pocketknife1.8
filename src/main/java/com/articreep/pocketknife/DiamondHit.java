package com.articreep.pocketknife;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.util.*;

public class DiamondHit extends PocketknifeSubcommand implements Listener, PocketknifeConfigurable {

    // WeakHashMaps automatically remove garbage-collected members
    private static final Set<Item> droppedXPSet = Collections.newSetFromMap(new WeakHashMap<>());
    private boolean enabled = false;

    @EventHandler
    public void onDiamondHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!enabled) return;

        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        ItemStack[] items = victim.getEquipment().getArmorContents();

        boolean hasDiamondArmor = false;
        for (ItemStack item : items) {
            if (Utils.isDiamondArmor(item)) {
                hasDiamondArmor = true;
                break;
            }
        }

        // todo add a cooldown
        if (hasDiamondArmor) dropXP(damager, victim);
    }
    private void dropXP(Player damager, Player victim) {
        Vector v = Utils.entitiesToNormalizedVector(damager, victim, 0.5);
        v.setY(0.5);
        Item bottle = victim.getWorld().dropItem(victim.getLocation().add(0, 1, 0), new ItemStack(Material.EXP_BOTTLE));
        damager.getWorld().playSound(damager.getLocation(), Sound.SHOOT_ARROW, 1, 1);
        bottle.setVelocity(v);
        droppedXPSet.add(bottle);
    }

    @EventHandler
    public void onXPPickup(PlayerPickupItemEvent event) {
        // todo - this isn't working on 1.8
        if (event.isCancelled()) return;
        if (!enabled) return;

        Player player = event.getPlayer();
        if (droppedXPSet.contains(event.getItem())) {
            event.setCancelled(true);
            droppedXPSet.remove(event.getItem());
            event.getItem().remove();
            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "XP!" + ChatColor.RESET + "" + ChatColor.AQUA + " +30 XP " + ChatColor.GRAY + "from opponent armor piece");
            player.playSound(player.getLocation(), Sound.ARROW_HIT, 1, 1);
        }
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        enabled = config.getBoolean("diamondhit");
        config.set("diamondhit", enabled);
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
                    sender.sendMessage(ChatColor.RED + "DiamondHit toggled " +
                            Utils.booleanStatus(enabled));
                } else {
                    sendSyntaxMessage(sender);
                }
                Pocketknife.getInstance().getConfig().set("diamondhit", enabled);
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
        return "Usage: /pocketknife DiamondHit toggle";
    }

    @Override
    public String getDescription() {
        return ChatColor.AQUA + "Players drop XP when struck if wearing diamond armor.";
    }
}
