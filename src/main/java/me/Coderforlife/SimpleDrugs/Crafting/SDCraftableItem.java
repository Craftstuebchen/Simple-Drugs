package me.Coderforlife.SimpleDrugs.Crafting;

import org.bukkit.inventory.ItemStack;

public interface SDCraftableItem {

	public String getNamespaceName();
	public ItemStack getItem();
	public String getFile();
	public void setFile(String s);
	
}