package saki4.skaddtrap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SKaddTrap extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.LOWEST) // Срабатываем максимально быстро
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();
        Material type = item.getType();

        // Проверяем, трапка это или пласт
        if ((type == Material.NETHERITE_SCRAP && name.contains("трапка")) ||
                (type == Material.DRIED_KELP && name.contains("пласт"))) {

            Player player = event.getPlayer();
            int radius = getConfig().getInt("radius", 5);
            List<String> restrictedBlocks = getConfig().getStringList("restricted-blocks");

            if (isNearRestrictedBlock(player.getLocation(), radius, restrictedBlocks)) {

                // 1. ОТМЕНЯЕМ СОБЫТИЕ
                event.setCancelled(true);

                // 2. ХИТРОСТЬ: Временно убираем предмет из руки, чтобы VioTrap его не "увидел"
                ItemStack handItem = item.clone();
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                // 3. Возвращаем предмет через 1 тик (0.05 сек)
                getServer().getScheduler().runTaskLater(this, () -> {
                    player.getInventory().setItemInMainHand(handItem);
                }, 1L);

                // 4. Пишем сообщение
                String msg = getConfig().getString("message", "&c[!] Здесь нельзя использовать это!");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
        }
    }

    private boolean isNearRestrictedBlock(Location center, int radius, List<String> blockList) {
        if (blockList == null || blockList.isEmpty()) return false;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = center.clone().add(x, y, z).getBlock();
                    if (blockList.contains(b.getType().name())) return true;
                }
            }
        }
        return false;
    }
}