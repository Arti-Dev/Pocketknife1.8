package com.articreep.pocketknife;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class Reach extends PocketknifeSubcommand implements Listener {

    @Override
    public String getDescription() {
        return "I love reach! Let's talk about it more.";
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        Player damager;

        // Melee hit?
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else {
            return;
        }

        // Calculate reach
        Location damagerLoc = damager.getLocation();
        Location victimLoc = victim.getLocation();
        double reach = damagerLoc.distance(victimLoc);

        // Read their pants.

        ItemStack pants = victim.getInventory().getLeggings();
//        String id = Utils.getItemID(pants);
//        if (id == null) return;
//
//        if (id.startsWith("REGULARITY_")) {
//            int option = Utils.parseInt(id.substring(id.length() - 1));

        // todo this is temporary
        if (pants == null) return;
        if (!pants.hasItemMeta()) return;
        if (!pants.getItemMeta().hasDisplayName()) return;
        String name = pants.getItemMeta().getDisplayName();
        if (name.startsWith(ChatColor.DARK_RED + "Spatial Testing Pants - Damage Reduction -")) {
            // hardcoded lol
            int reduction = Utils.parseInt(name.substring(44)) * (int) reach;
            // todo what am i doing
            event.setDamage(event.getDamage() * (1 - (double) reduction/100));
            if (PVPDebug.isEnabled()) {
                victim.sendMessage("You took " + ChatColor.RED + "-" + reduction + "%" + ChatColor.WHITE + " damage");
            }
        }
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendDescriptionMessage(sender);
            sendSyntaxMessage(sender);
        } else {
            int option;
            try {
                option = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "That's not an integer.");
                return true;
            }
            if (option < 1 || option > 100) {
                sender.sendMessage(ChatColor.RED + "Reduction must be between 1 and 100");
                return true;
            }
            Player player = (Player) sender;
            player.getInventory().addItem(createPants(option));
            sender.sendMessage(ChatColor.BLUE + "Spatial Testing Pants - Damage Reduction -" + option + " added to your inventory!");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife Reach <reduction>";
    }

    private static ItemStack createPants(int option) {
        final ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(ChatColor.DARK_RED + "Spatial Testing Pants - Damage Reduction -" + option);
//        Utils.setItemID(meta, "REGULARITY_" + option);


        meta.setLore(Arrays.asList(
                ChatColor.RED + "Receive -x% damage per block",
                ChatColor.RED + "between you and your opponent."));

        item.setItemMeta(meta);
        return item;
    }
}
