package me.mattstudios.mfgui.gui.guis;

import me.mattstudios.mfgui.gui.components.GuiAction;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import static me.mattstudios.mfgui.gui.components.ItemNBT.getNBTTag;

public final class GuiListener implements Listener {

    private final Plugin plugin;

    public GuiListener(final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles what happens when a player clicks on the GUI
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onGuiCLick(final InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        if (!(event.getInventory().getHolder() instanceof BaseGui)) return;

        // Gui
        final BaseGui gui = (BaseGui) event.getInventory().getHolder();

        // Default click action and checks weather or not there is a default action and executes it
        final GuiAction<InventoryClickEvent> defaultTopClick = gui.getDefaultTopClickAction();
        if (defaultTopClick != null && event.getClickedInventory().getType() != InventoryType.PLAYER)
            defaultTopClick.execute(event);

        // Default click action and checks weather or not there is a default action and executes it
        final GuiAction<InventoryClickEvent> defaultClick = gui.getDefaultClickAction();
        if (defaultClick != null) defaultClick.execute(event);

        // Slot action and checks weather or not there is a slot action and executes it
        final GuiAction<InventoryClickEvent> slotAction = gui.getSlotAction(event.getSlot());
        if (slotAction != null && event.getClickedInventory().getType() != InventoryType.PLAYER)
            slotAction.execute(event);

        // The clicked GUI Item
        final GuiItem guiItem = gui.getGuiItem(event.getSlot());

        // Returns if there is no gui item
        if (guiItem == null) return;

        // Checks whether or not the Item is truly a GUI Item
        if (!getNBTTag(event.getCurrentItem(), "mf-gui").equalsIgnoreCase(guiItem.getUuid().toString())) return;

        // Executes the action of the item
        guiItem.getAction().execute(event);

    }

    /**
     * Handles what happens when a player clicks on the GUI
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onGuiDrag(final InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseGui)) return;

        // Gui
        final BaseGui gui = (BaseGui) event.getInventory().getHolder();

        // Default click action and checks weather or not there is a default action and executes it
        final GuiAction<InventoryDragEvent> dragAction = gui.getDragAction();
        if (dragAction != null) dragAction.execute(event);
    }

    /**
     * Handles what happens when the GUI is closed
     *
     * @param event The InventoryCloseEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onGuiClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseGui)) return;

        // GUI
        final BaseGui gui = (BaseGui) event.getInventory().getHolder();

        // The GUI action for closing
        final GuiAction<InventoryCloseEvent> closeAction = gui.getCloseGuiAction();

        // Checks if there is or not an action set and executes it
        if (closeAction != null && !gui.isUpdating()) closeAction.execute(event);

        // Checks for stolen items
        checkItemSteal(event.getPlayer().getInventory());
    }

    /**
     * Handles what happens when the GUI is opened
     *
     * @param event The InventoryOpenEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onGuiOpen(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseGui)) return;

        // GUI
        final BaseGui gui = (BaseGui) event.getInventory().getHolder();

        // The GUI action for opening
        final GuiAction<InventoryOpenEvent> openAction = gui.getOpenGuiAction();

        // Checks if there is or not an action set and executes it
        if (openAction != null && !gui.isUpdating()) openAction.execute(event);
    }

    /**
     * Checks for potential item dropping
     *
     * @param event The player drop item event
     */
    @EventHandler
    public void itemDrop(final PlayerDropItemEvent event) {
        if (!isGuiItem(event.getItemDrop().getItemStack())) return;

        event.setCancelled(true);
        event.getPlayer().getInventory().remove(event.getItemDrop().getItemStack());
    }

    /**
     * Checks for stealing items by throwing them out
     *
     * @param item The item thrown
     */
    private boolean isGuiItem(final ItemStack item) {
        return getNBTTag(item, "mf-gui") != null;
    }

    /**
     * Checks for stealing items from inventory on player's inventory
     *
     * @param inventory The player's inventory
     */
    private void checkItemSteal(final PlayerInventory inventory) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            for (final ItemStack item : inventory.getContents()) {
                if (getNBTTag(item, "mf-gui") == null) continue;
                inventory.remove(item);
            }
        }, 2L);
    }

}