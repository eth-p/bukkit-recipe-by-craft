package dev.ethp.recipebycraft;

import java.util.logging.Logger;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;

public class Plugin extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		PluginDescriptionFile manifest = this.getDescription();
		Logger logger = this.getLogger();
		logger.info("Authors: " + String.join(", ", manifest.getAuthors()));
		logger.info("Support: " + manifest.getWebsite());

		this.getServer().getPluginManager().registerEvents(this, this);

		logger.info("Enabled " + manifest.getName() + " version " + manifest.getVersion() + ".");
	}

	@Override
	public void onDisable() {
		PluginDescriptionFile manifest = this.getDescription();
		Logger logger = this.getLogger();
		logger.info("Disabled " + manifest.getName() + ".");
	}

	@EventHandler
	public void onRecipeDiscover(PlayerRecipeDiscoverEvent event) {
		Player player = event.getPlayer();

		// Allow the discovery if the player has the bypass permission.
		if (player.hasPermission("recipebycraft.bypass")) return;

		// Allow the discovery if they recently crafted the item.
		for (MetadataValue metadata : event.getPlayer().getMetadata("discover-recipe")) {
			if (metadata.asString().equals(event.getRecipe().toString())) {
				return;
			}
		}

		// Cancel the recipe discover.
		event.setCancelled(true);
	}

	@EventHandler
	public void onRecipeCraft(CraftItemEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if (!(holder instanceof Player)) return;

		Player player = (Player) holder;
		ItemMeta meta = event.getRecipe().getResult().getItemMeta();
		if (meta != null) {
			// Don't do anything if it's likely to be a custom recipe.
			if (meta.hasDisplayName() || meta.hasLore()) {
				return;
			}
		}

		// Set the player metadata to allow discovering the recipe, and then discover it.
		NamespacedKey key = event.getRecipe().getResult().getType().getKey();
		player.setMetadata("discover-recipe", new FixedMetadataValue(this, key.toString()));
		player.discoverRecipe(key);
	}

}
