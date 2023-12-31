package com.articreep.pocketknife;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    /**
     * Returns whether the provided ItemStack is a piece of diamond armor.
     * @param item ItemStack to check
     * @return true if diamond armor - false otherwise
     */
    public static boolean isDiamondArmor(ItemStack item) {
        if (item == null) return false;
        Material material = item.getType();
        return material == Material.DIAMOND_BOOTS ||
                material == Material.DIAMOND_LEGGINGS ||
                material == Material.DIAMOND_CHESTPLATE ||
                material == Material.DIAMOND_HELMET;
    }

    /**
     * Takes two entities and converts their locations to vectors and creates a resultant.
     * The vector is then normalized to length 1 and multiplied by the provided factor.
     * @param start Starting entity, vector points away from this entity
     * @param end Ending entity, vector points towards this entity
     * @param factor Magnitude of the final vector
     * @return The final vector
     */
    public static Vector entitiesToNormalizedVector(Entity start, Entity end, double factor) {
        if (start.getWorld() != end.getWorld()) throw new IllegalArgumentException("Entities' worlds do not match!");

        Location startLoc = start.getLocation();
        Location endLoc = end.getLocation();

        // Generate vector, player - tnt = vector
        Vector vector = endLoc.toVector().subtract(startLoc.toVector());
        if (vector.lengthSquared() == 0) return vector;
        vector.normalize();
        vector.multiply(factor);

        return vector;
    }

    /**
     * Takes two entities and converts their locations to vectors and creates a resultant.
     * The resultant is then stripped of its y-value.
     * The vector is then normalized to length 1 and multiplied by the provided factor.
     * @param start Starting entity, vector points away from this entity
     * @param end Ending entity, vector points towards this entity
     * @param factor Magnitude of the final vector
     * @return The final vector
     */
    public static Vector entitiesToHorizontalNormalizedVector(Entity start, Entity end, double factor) {
        if (start.getWorld() != end.getWorld()) throw new IllegalArgumentException("Entities' worlds do not match!");

        Location startLoc = start.getLocation();
        Location endLoc = end.getLocation();

        // Generate vector, player - tnt = vector
        Vector vector = endLoc.toVector().subtract(startLoc.toVector());
        // IMPORTANT: make sure you do NOT normalize the zero vector.
        if (vector.lengthSquared() == 0) return vector;
        vector.normalize();
        vector.multiply(factor);
        vector.setY(0);

        return vector;
    }

    /**
     * Gets a runCommand method object from the specified class name. Make sure hasCommandMethod is checked!
     * @param className class name
     * @param classLoader class loader from Bukkit
     * @return The method, null if not found
     */
    public static Method getCommandMethod(String className, ClassLoader classLoader) {
        Class<?> targetClass;
        try {
            targetClass = Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Method m;
        try {
            m = targetClass.getMethod("runCommand", CommandSender.class, Command.class, String.class, String[].class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }

        return m;

    }

    /**
     * Gets a runCommand method object from the specified class. Make sure hasCommandMethod is checked!
     * @param targetClass class to check
     * @return method object, null if not found
     */
    public static Method getCommandMethod(Class<?> targetClass) {
        Method m;
        try {
            m = targetClass.getMethod("runCommand", CommandSender.class, Command.class, String.class, String[].class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        return m;
    }

    /**
     * Note to self: This does not work if the method is protected!!
     * Takes a class object and checks to see if there is a static runCommand(CommandSender sender, Command command, String label, String[] args) method.
     * @param targetClass Any class
     * @return Whether it contains the method
     */
    public static boolean hasCommandMethod(Class<?> targetClass) {
        try {
            targetClass.getMethod("runCommand", CommandSender.class, Command.class, String.class, String[].class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Removes the first argument from an argument array.
     * @param args arguments
     * @return Arguments array with the first argument removed
     */
    public static String[] removeFirstArg(String[] args) {
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        // TODO I don't know what I'm doing here
        String[] strings = new String[0];
        return argsList.toArray(strings);
    }

    /**
     * Takes a list of type String and a string.
     * Checks if the string is in the list, regardless of case.
     * @param list List of strings
     * @param check Element to check against List
     * @return Whether the element is in the list, regardless of case
     */
    public static boolean containsIgnoreCase(List<String> list, String check) {
        for (String str : list) {
            if (str.equalsIgnoreCase(check)) return true;
        }
        return false;
    }

    /**
     * Removes all matches from the first argument list that are in the second list.
     * @param list First list (items will be removed from this list)
     * @param remove Second list (items will be unchanged)
     */
    public static void removeAllIgnoreCase(List<String> list, List<String> remove) {
        for (String str : remove) {
            if (containsIgnoreCase(list, str)) list.remove(str);
        }
    }

    public static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Is this a good way of doing this? No clue.
    public static void trueDamage(Player victim, Player damager, double amount) {
        if (victim.getHealth() - amount <= 0) {
            victim.damage(100000, damager);
        } else {
            victim.setHealth(victim.getHealth() - amount);
            victim.damage(0.0000000000000000000000000000000000001);
        }
    }

    public static void trueDamage(Player victim, double amount) {
        if (victim.getHealth() - amount <= 0) {
            victim.damage(100000);
        } else {
            // TODO this line was throwing an IllegalArgumentException (must be between 0 and 20.0). Not sure why.
            victim.setHealth(victim.getHealth() - amount);
            victim.damage(0.0000000000000000000000000000000000001);
        }
    }

    public static Vector randomKB(double magnitude) {
        double x = (Math.random() * 2) - 1;
        double z = (Math.random() * 2) - 1;
        return new Vector(x, 0, z).normalize().multiply(magnitude).setY(0.4);
    }

    public static Vector randomVector(double magnitude) {
        double x = (Math.random() * 2) - 1;
        double y = (Math.random() * 2) - 1;
        double z = (Math.random() * 2) - 1;
        return new Vector(x, y, z).normalize().multiply(magnitude);
    }
/* TODO Make this use NBT
//    private static final NamespacedKey key = new NamespacedKey(Pocketknife.getInstance(), "ITEM_ID");
//    /**
//     * Adds an item ID to the PersistentDataContainer of the item.
//     * @param item Item to add an ID to
//     * @throws NullPointerException if item is null
//     */
//    public static void setItemID(ItemStack item, String id) {
//
//        ItemMeta meta = item.getItemMeta();
//        if (meta == null) throw new NullPointerException("Item has no ItemMeta");
//
//        PersistentDataContainer container = meta.getPersistentDataContainer();
//        container.set(key, PersistentDataType.STRING, id);
//        item.setItemMeta(meta);
//    }
//
//    /**
//     * Adds an item ID to the PersistentDataContainer of the meta. Remember to apply the ItemMeta back to the ItemStack!
//     * @param meta Meta to add an ID to
//     * @throws NullPointerException if meta is null
//     */
//    public static void setItemID(ItemMeta meta, String id) {
//
//        if (meta == null) throw new NullPointerException("Meta cannot be null");
//
//        PersistentDataContainer container = meta.getPersistentDataContainer();
//        container.set(key, PersistentDataType.STRING, id);
//    }
//
//    public static String getItemID(ItemStack item) {
//        if (item == null) return null;
//        ItemMeta meta = item.getItemMeta();
//        if (meta == null) return null;
//
//        PersistentDataContainer container = meta.getPersistentDataContainer();
//        return container.get(key, PersistentDataType.STRING);
//    }

    public static String booleanStatus(boolean boo) {
        if (boo) return "ON";
        else return "OFF";
    }

    public static float invertYaw(float yaw) {
        yaw += 180;
        if (yaw >= 360) {
            yaw -= 360;
        }
        return yaw;
    }

    public static float invertPitch(float pitch) {
        return -pitch;
    }

    public static void sendActionBar(Player player, String string) {
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + string + "\"}"), (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendbeegExplosion(Location loc) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_HUGE,
                true, (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 0, 0, 0, 0, 1);
        for(Player online : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void sendExplosion(Location loc) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_LARGE, true,
                (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 0, 0, 0, 0, 1);
        for(Player online : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void sendRedstoneParticle(Pocketknife plugin, Location loc) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
                (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 0, 0, 0, 0, 3);
        for(Player online : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
        }
    }
}
