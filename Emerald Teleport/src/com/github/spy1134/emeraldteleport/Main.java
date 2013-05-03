package com.github.spy1134.emeraldteleport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
	static String filePath = "./plugins/Emerald Teleport/";
	static String fileName = "emeraldLocations.dat";
	static String fullPath = filePath + fileName;
	
	// This stores all emerald block locations.
	static ArrayList<Location> emeraldBlockLocations;
	
	public void onEnable()
	{
		// Load locations from disk.
		emeraldBlockLocations = loadLocationsFromFile();
		
		// Register the event listener.
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		
		getLogger().info(this.getName() + " enabled!");
	}
	
	public void onDisable()
	{
		// Save locations to disk.
		saveLocationsToFile();
		getLogger().info(this.getName() + " disabled!");
	}
	
	// Load locations (if any) from disk.
	// Returns locations or an empty ArrayList.
	@SuppressWarnings("unchecked")
	public ArrayList<Location> loadLocationsFromFile()
	{
		// Make all folders if they don't already exist.
		File folder = new File(filePath);
		if(! folder.isDirectory())
		{
			folder.mkdirs();
		}
		
		getLogger().info(this.getName() + " Loading data...");
		
		// Try to open an input stream.
		try
		{
			// Create a new input stream.
			ObjectInputStream emeraldDataInput = new ObjectInputStream(new FileInputStream(fullPath));
			
			// Try to read the stream.
			try
			{
				// Read the input and close it.
				Object deserialized = emeraldDataInput.readObject();
				emeraldDataInput.close();
				
				// Typecast the data and return it.
				if(deserialized instanceof ArrayList)
				{
					// Change the serializable location list into a location list.
					ArrayList<Location> returnList = SerializableLocation.listToLocations(((ArrayList<SerializableLocation>) deserialized));
					return returnList;
				}
				else
				{
					// Output a warning.
					getLogger().warning("Unable to load emerald teleportation block locations!\nStarting with empty emerald block list.");
					
					// Return an empty ArrayList.
					return new ArrayList<Location>();
				}
			}
			// Something went wrong, print details.
			catch (Exception e)
			{
				// Output a warning.
				getLogger().warning("Unable to load emerald teleportation block locations!\nStarting with empty emerald block list.");
				
				// Return an empty ArrayList.
				return new ArrayList<Location>();
			}
		}
		// The file we are trying to open doesn't exist. Let's create it.
		catch (FileNotFoundException ex)
		{
			getLogger().info("Making a new file.");
			getLogger().info(fullPath);
			
			// Make the file.
			File emeraldDataFile = new File(fullPath);
			try
			{
				emeraldDataFile.createNewFile();
			}
			// Something went wrong, print details.
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// Return an empty ArrayList.
			return new ArrayList<Location>();
		}
		// Something went wrong, print details.
		catch (Exception ex)
		{
			ex.printStackTrace();
			
			// Return an empty ArrayList.
			return new ArrayList<Location>();
		}
		
	}
	
	// Save locations to disk.
	// Returns true on success, false otherwise.
	public boolean saveLocationsToFile()
	{
		ObjectOutputStream emeraldDataOutput;
		
		getLogger().info("Saving data...");
		
		// Try to open emeraldlocations.dat
		try
		{
			emeraldDataOutput = new ObjectOutputStream(new FileOutputStream(fullPath));
		}
		// The file doesn't exist. Let's create it.
		catch(FileNotFoundException ex)
		{
			getLogger().info("Making new file...");
			getLogger().info(fullPath);
			
			// Try to make the file.
			File emeraldDataFile = new File(fullPath);
			try
			{
				// Create the file.
				emeraldDataFile.createNewFile();
				
				// Make another attempt to open the output stream.
				emeraldDataOutput = new ObjectOutputStream(new FileOutputStream(fullPath));
			}
			// Something went wrong, print details.
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}			
		}
		// Something went wrong, print details.
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		
		// Try to write the ArrayList to the file.
		try
		{
			// Change the block locations into serializable locations.
			ArrayList<SerializableLocation> toSave = SerializableLocation.listToSerializable(emeraldBlockLocations);
			emeraldDataOutput.writeObject(toSave);
			emeraldDataOutput.close();
			return true;
		}
		// Something went wrong, print details.
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	// Check if the given location is in the list.
	// Piggybacks off of findLocationInList
	protected static boolean isLocationInList(Location location)
	{
		if(findLocationInList(location) == -1)
			return false;
		else
			return true;
	}
	
	// Returns the index of the given location in this list, -1 if location was not found.
	protected static int findLocationInList(Location location)
	{
		for(int i = 0; i < emeraldBlockLocations.size(); i++)
		{
			// If the current location matches the one given...
			if(location.equals(emeraldBlockLocations.get(i)))
			{
				// Return the current index.
				return i;
			}
		}
		// If the loop has completed without finding anything...
		return -1;
	}
	
	// Removes the given location from the list.
	// Returns true on success, false if location is not in the list.
	protected static boolean removeLocationFromList(Location location)
	{
		// Get the index of the location in the list.
		int index = findLocationInList(location);
		
		// If the location was not found then return false.
		if(index == -1)
			return false;
		
		// Remove the location from the list.
		emeraldBlockLocations.remove(index);
		return true;
	}
	
	// Teleport the player to a random emerald block.
	static public void teleportPlayer(Player player)
	{
		Location teleportLocation = getRandomEmeraldLocation();
		
		// To prevent modifying locations in the list we
		// have to clone the location returned by getRandomEmeraldLocation()
		Location adjustedLocation = teleportLocation.clone();
		adjustedLocation.add(0, 1, 0);
		
		player.sendMessage(ChatColor.GREEN + "Teleporting...");
		player.teleport(adjustedLocation);
	}
	
	// Get a random emerald block.
	// Returns a location or null if no emerald blocks have been set.
	public static Location getRandomEmeraldLocation()
	{
		// Check if the list is empty.
		if(emeraldBlockLocations.size() == 0)
			return null;
		
		// Math.random returns a double between 0 (inclusive) and 1 (exclusive).
		// Multiply it by the highest index to scale it and typecast
		// it to an int to round it to a whole number.
		int randomIndex = (int) (Math.random() * (emeraldBlockLocations.size()));
		
		// Return the corresponding location.
		return emeraldBlockLocations.get(randomIndex);
	}
	
	// When a player types a command, this method is run.
	// The server should block players from using commands
	// they shouldn't since we have specified permissions for
	// the commands in our plugin.yml configuration file.
	// Because of this, it isn't really necessary to check
	// for permissions unless a special situation calls for it
	// (one command has multiple subcommands with their own permissions, etc.)
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		// Declare a variable to store the player.
		Player player;
		
		// If the sender is a player...
		if(sender instanceof Player)
			player = (Player) sender;
		// If the sender is the console...
		else
		{
			player = null;
		}
		
		// ~~~~~~~~~~~~~~~~~~~ SET EMERALD COMMAND ~~~~~~~~~~~~~~~~~~~~~~~~~
		// Permission: emeraldteleport.set
		if(cmd.getName().equalsIgnoreCase("setemerald"))
		{
			// If console...
			if(player == null)
			{
				// Output an error message and return true so command usage is not displayed.
				sender.sendMessage(ChatColor.RED + "You must be a player to use that command!");
				return true;
			}
			
			// Copy player location and move it down by one block.
			Location blockLocation = player.getLocation();
			blockLocation.subtract(0, 1, 0);
			
			// To round coordinates.
			blockLocation = blockLocation.getBlock().getLocation();
			
			// Check to see if the block under the player is an emerald block.
			if(blockLocation.getBlock().getType() == Material.EMERALD_BLOCK)
			{
				// Check if the block is already registered.
				if(Main.isLocationInList(blockLocation))
				{
					// Output an error and return true since we handled the error message.
					player.sendMessage(ChatColor.RED + "This block is already registered!");
					return true;
				}
				
				// Add the block to the list and output a confirmation message.
				emeraldBlockLocations.add(blockLocation);
				player.sendMessage(ChatColor.GREEN + "Block has been registered!");
				// Debug text
				// player.sendMessage(blockLocation.toString());
				return true;
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You must be standing on an emerald block!");
			}
		}
		// Clear emerald block locations. Console and OP only for security.
		// No permission. OP and console only.
		else if(cmd.getName().equalsIgnoreCase("clearemerald"))
		{
			// If source is console or OP...
			if(player == null || player.isOp())
			{
				// Clear the list.
				emeraldBlockLocations.clear();
				sender.sendMessage(ChatColor.GREEN + "List cleared!");
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "You don't have permission to do this! Go die in a hole!");
			}
		}
		// Save/load block locations to/from disk.
		// Permission: emeraldteleport.save/emeraldteleport.load
		else if(cmd.getName().equalsIgnoreCase("saveemerald") || cmd.getName().equalsIgnoreCase("loademerald"))
		{
			// If save was requested...
			if(cmd.getName().equalsIgnoreCase("saveemerald"))
			{
				saveLocationsToFile();
				sender.sendMessage(ChatColor.GREEN + "Locations saved!");
				/* Debug text.
				for(int i = 0; i < emeraldBlockLocations.size(); i++)
				{
					sender.sendMessage(emeraldBlockLocations.get(i).toString());
				}
				*/
			}
			// The only other case is that a load was requested.
			else
			{
				loadLocationsFromFile();
				sender.sendMessage(ChatColor.GREEN + "Locations loaded!");
			}
		}
		return true;
	}
}
