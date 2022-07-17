package me.Coderforlife.SimpleDrugs.GUI;

import me.Coderforlife.SimpleDrugs.Druging.Drug;
import me.Coderforlife.SimpleDrugs.Druging.Util.DrugEffect;
import me.Coderforlife.SimpleDrugs.Main;
import me.Coderforlife.SimpleDrugs.Settings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class BagOfDrugsGUI implements Listener {
    private Main plugin = Main.plugin;
    Settings s = plugin.getSettings();

    public BagOfDrugsGUI() {
    }

    private ItemStack bag = BagOfDrugsStack();
    private final int maxdrugs = 45;
    public final String bagName = "§6§l§oBag of Drugs";
    public final String invName = ChatColor.translateAlternateColorCodes('&',
            "          &6&l&oBag Of Drugs");;

    @EventHandler
    public void BagOpen(PlayerInteractEvent ev) {
        if (ev.getHand() == null) {
            return;
        }
        if (ev.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }
        Player p = ev.getPlayer();
        Action pa = ev.getAction();
        if (p.getInventory().getItemInMainHand().getItemMeta() == null) {
            return;
        }

        if (pa.equals(Action.RIGHT_CLICK_AIR) || pa.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(bagName)
                    && p.getInventory().getItemInMainHand()
                            .hasItemMeta()) {
                if (p.hasPermission("drugs.use.bagofdrugs")) {
                    Location loc = p.getLocation();
                    for (int degree = 0; degree < 360; degree++) {
                        double radians = Math.toRadians(degree);
                        double x = Math.cos(radians);
                        double z = Math.sin(radians);
                        loc.add(x, 0, z);
                        loc.getWorld().playEffect(loc, Effect.SMOKE, degree);
                        loc.subtract(x, 0, z);
                    }
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, (float) 0.4);
                    p.openInventory(create());
                } else {
                    p.sendMessage("§cYou don't have permission to use this!");
                }
            }
        }
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent ev) {
        Player p = (Player) ev.getWhoClicked();
        ItemStack clickedItem = ev.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        if (!Main.plugin.getSettings().isBagOfDrugs_CanMove()) {
            if (!clickedItem.hasItemMeta()) {
                return;
            }
            if (clickedItem.getItemMeta().getDisplayName().equals(bagName)) {
                p.setItemOnCursor(new ItemStack(Material.AIR));
                ev.setCancelled(true);
            }
        }

        if (!ev.getView().getTitle().equals(invName)) {
            return;
        }
        ev.setCancelled(true);

        String itemname = clickedItem.getItemMeta().getDisplayName();
        String[] pagenumber = itemname.split(" ");

        if (ev.getCurrentItem().getType().equals(Material.ARROW) && itemname.startsWith("§6Page")) {
            int page = Integer.parseInt(pagenumber[1]);
            if (page == 1) {
                p.openInventory(this.create());
            } else {
                p.openInventory(this.openPage(page));
            }
            return;
        }
        if (plugin.getDrugManager().isDrugItem(clickedItem)) {
            Drug d = plugin.getDrugManager().matchDrug(clickedItem);
            if (ev.getClick() == ClickType.LEFT) {
                ItemStack drug = d.getItem();
                ItemMeta im = drug.getItemMeta();
                List<String> lores = new ArrayList<>();
                lores.clear();
                im.setLore(lores);
                for (DrugEffect de : d.getEffects()) {
                    lores.add(ChatColor.translateAlternateColorCodes('&',
                            "&7- &6&o" + de.getEffect().getName()));
                }
                drug.setAmount(1);
                im.setLore(lores);
                drug.setItemMeta(im);
                p.getInventory().addItem(drug);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, (float) 1.0, (float) 1.0);
                p.sendMessage(
                        plugin.getMessages().getPrefix() + ChatColor.translateAlternateColorCodes('&',
                                "&oYou've been given " + d.getDisplayName()));

            } else if (ev.getClick() == ClickType.SHIFT_LEFT) {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, (float) 1.0, (float) 1.0);
                ItemStack d64 = d.getItem();
                ItemMeta im = d64.getItemMeta();
                im.getLore().clear();
                List<String> lores = new ArrayList<>();
                im.setLore(lores);
                for (DrugEffect de : d.getEffects()) {
                    lores.add(ChatColor.translateAlternateColorCodes('&',
                            "&7- &6&o" + de.getEffect().getName()));

                }
                d64.setAmount(64);
                im.setLore(lores);
                d64.setItemMeta(im);
                p.getInventory().addItem(d64);

            } else if (ev.getClick() == ClickType.RIGHT) {
                d.influencePlayer(p);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, (float) 1.0, (float) 1.0);
                p.closeInventory();
            } else if (ev.getClick() == ClickType.SHIFT_RIGHT) {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, (float) 1.0, (float) 1.0);
                p.closeInventory();
                new SDRecipeInventory(d).createSDRecipeInventory(p);
            }
        }
    }

    public Inventory create() {
        ArrayList<Drug> drugs = new ArrayList<>(Main.plugin.getDrugManager().getItems().values());
        int amountofdrugs = drugs.size();

        ArrayList<ItemStack> stack = new ArrayList<>();
        amountofdrugs = Math.min(amountofdrugs, maxdrugs);

        for (int i = 0; i < amountofdrugs; i++) {
            Drug d = drugs.get(i);
            ItemStack drug = d.getItem();
            stack.add(drug);
        }
        while (stack.size() % 9 != 0) {
            stack.add(new ItemStack(Material.AIR));
        }

        stack.add(BackwardButton(false, 1));
        for (int i = 0; i < 7; i++) {
            stack.add(GreyGlassPane());
        }
        stack.add(ForwardButton(drugs.size() > maxdrugs, 2));

        Inventory inv = Bukkit.createInventory(null, stack.size(), invName);

        inv.setContents(stack.toArray(new ItemStack[0]));
        // BUG TODO: Lore is changed when shop is opened.
        for (ItemStack s : inv.getStorageContents()) {
            s.setAmount(1);
            if (plugin.getDrugManager().isDrugItem(s)) {
                Drug d = plugin.getDrugManager().matchDrug(s);
                ItemMeta im = s.getItemMeta();
                im.getLore().clear();
                s.setItemMeta(d.getItem().getItemMeta());
            }
        }
        return inv;
    }

    public Inventory openPage(int page) {
        ArrayList<Drug> drugs = new ArrayList<>(Main.plugin.getDrugManager().getItems().values());
        int amountofdrugs = drugs.size();

        int drugsleft = amountofdrugs - ((page - 1) * maxdrugs);
        int startfrom = (maxdrugs * (page - 1));

        ArrayList<ItemStack> stack = new ArrayList<>();
        amountofdrugs = Math.min(drugsleft, maxdrugs);

        for (int i = startfrom; i < amountofdrugs + (startfrom - 1); i++) {
            stack.add(drugs.get(i).getItem());
        }
        while (stack.size() % 9 != 0) {
            stack.add(new ItemStack(Material.AIR));
        }

        stack.add(BackwardButton(page > 1, page - 1));
        for (int i = 0; i < 7; i++) {
            stack.add(GreyGlassPane());
        }
        stack.add(ForwardButton(drugs.size() > maxdrugs * page, page + 1));

        Inventory inv = Bukkit.createInventory(null, stack.size(), invName);
        inv.setContents(stack.toArray(new ItemStack[0]));
        return inv;
    }

    private ItemStack GreyGlassPane() {
        ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(" ");
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack ForwardButton(boolean active, int page) {
        if (!active)
            return GreyGlassPane();

        ItemStack stack = new ItemStack(Material.ARROW);
        ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§6Page " + page);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack BackwardButton(boolean active, int page) {
        if (!active)
            return GreyGlassPane();

        ItemStack stack = new ItemStack(Material.ARROW);
        ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§6Page " + page);
        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler
    public void onDragEvent(InventoryDragEvent ev) {
        if (ev.getView().getTitle().equals(invName)) {
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent ev) {
        if (!Main.plugin.getSettings().isBagOfDrugs_CanDrop()) {
            if (ev.getItemDrop().getItemStack().getItemMeta().getDisplayName().equals(bagName)) {
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BagSpawn(ItemSpawnEvent ev) {
        Item drop = ev.getEntity();
        ItemStack item = drop.getItemStack();
        if (item.hasItemMeta()) {
            if (item.getItemMeta().getDisplayName().equals(bagName)) {
                drop.setCustomName(bagName);
                drop.setCustomNameVisible(true);
            }
            if (Main.plugin.getDrugManager().isDrugItem(item)) {
                drop.setCustomName(item.getItemMeta().getDisplayName());
                drop.setCustomNameVisible(true);
            }

        }
    }

    @EventHandler
    public void DisableBeaconDup(CraftItemEvent e) {
        Player p = (Player) e.getView().getPlayer();
        CraftingInventory inv = e.getInventory();
        ItemStack[] mat = inv.getMatrix();

        if (inv.getResult().getType() == Material.BEACON) {
            if (mat[4].getItemMeta().getDisplayName().contentEquals(bagName)) {
                p.sendMessage(
                        plugin.getMessages().getPrefix() + "§c§oCan " + "not use " + bagName + " §c§oto craft a " + "§b"
                                + inv.getResult().getType());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBagClose(InventoryCloseEvent ev) {
        if (ev.getView().getTitle().equals(invName)) {
            Player p = (Player) ev.getPlayer();

        }

    }

    @EventHandler
    public void onBagOpen(InventoryOpenEvent ev) {
        if (ev.getView().getTitle().equals(invName)) {
            Player p = (Player) ev.getPlayer();
        }
    }

    public String getBagName() {
        return bagName;
    }

    private ItemStack BagOfDrugsStack() {
        ItemStack stack = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(this.bagName);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "---------------------");
        lore.add(ChatColor.RED + "A Bag Full Of Drugs :)");
        lore.add("Enjoy.");
        lore.add(ChatColor.ITALIC + "Simple-Drugs®");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.BINDING_CURSE, 100, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack getBagOfDrugs() {
        return bag;
    }
}
