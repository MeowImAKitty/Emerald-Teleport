package com.github.meowimakitty.emeraldteleport;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventListener implements Listener
{
	// To trigger teleportation.
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		// Get the player.
		Player player = event.getPlayer();
		
		// If the player right clicked an emerald block...
		if((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getType() == Material.EMERALD_BLOCK))
		{
			Location blockLocation = event.getClickedBlock().getLocation();
			// If the block is on the list...
			if(Main.isLocationInList(blockLocation))
			{
				// If the player doesn't have permission to use teleportation
				// blocks then send them an error message.
				if(! player.hasPermission("emeraldteleport.use"))
				{
					player.sendMessage(ChatColor.RED + "You don't have permission to use this block!");
					return;
				}

				Main.teleportPlayer(player);
				return;
			}
			/* Debug text
			else
			{
				player.sendMessage("Block not in location list.");
				player.sendMessage(blockLocation.toString() + "\n");
				for(int i = 0; i < Main.emeraldBlockLocations.size(); i++)
				{
					player.sendMessage(Main.emeraldBlockLocations.get(i).toString());
				}
			}
			*/
		}
	}
	
	// To prevent emerald block destruction and remove blocks from the list as they are destroyed.
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		// Get the player.
		Player player = event.getPlayer();
		
		Location blockLocation = event.getBlock().getLocation();
		if(Main.isLocationInList(blockLocation))
		{
			// If the player has permission to delete emerald teleportation blocks
			// then don't cancel the event when they attempt to destroy the block.
			if(player.hasPermission("emeraldteleport.delete"))
			{
				// Remove the block's location from the list.
				Main.removeLocationFromList(blockLocation);
				player.sendMessage(ChatColor.GREEN + "Emerald teleportation block destroyed!");
				return;
			} // Won't get past here if the player is privileged.
			
			// Cancel the block break event.
			event.setCancelled(true);
			
			// Tell the player why the event was cancelled with red error text.
			event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to destroy that block!");
		}
	}
}
