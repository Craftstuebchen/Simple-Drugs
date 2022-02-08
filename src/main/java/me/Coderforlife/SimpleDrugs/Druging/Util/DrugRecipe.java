package me.Coderforlife.SimpleDrugs.Druging.Util;

import org.bukkit.inventory.ItemStack;

import me.Coderforlife.SimpleDrugs.Crafting.SDCraftableItem;
import me.Coderforlife.SimpleDrugs.Crafting.Recipes.SDRecipe;
import me.Coderforlife.SimpleDrugs.Druging.Drug;

public class DrugRecipe implements SDCraftableItem {

	private Drug drug;
	private SDRecipe recipe;
	private String file;
	
	public DrugRecipe(Drug d, SDRecipe rec) {
		drug = d;
		recipe = rec;
	}
	
	public Drug getDrug() {
		return drug;
	}
	
	public SDRecipe getRecipe() {
		return recipe;
	}
	
	public String getNamespaceName() {
		return null;
	}

	public ItemStack getItem() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String s) {
		file = s;
	}
	
}