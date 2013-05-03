package com.github.spy1134.emeraldteleport;

import java.io.Serializable;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializableLocation implements Serializable
{
	// Required for classes that can be serialized.
	private static final long serialVersionUID = -4291087571122145099L;

	// Location info fields.
	private double x, y, z;
	private String world;
	
	// To make a location serializable...
	public SerializableLocation(Location loc)
	{
		// Get x, y and z.
		x = loc.getX();
		y = loc.getY();
		z = loc.getZ();
		
		// Change the world to a string.
		world = loc.getWorld().getName();
	}
	
	// To get the location from a serializable location.
	public Location toLocation()
	{
		// Get the world from it's name.
		World w = Bukkit.getServer().getWorld(world);
		
		// Make a location out of the world and the coordinates.
		Location loc = new Location(w,x,y,z);
		
		// Return the location.
		return loc;
	}
	
	// Turns a serializablelocation list into a location list.
	public static ArrayList<Location> listToLocations(ArrayList<SerializableLocation> list)
	{
		ArrayList<Location> returnList = new ArrayList<Location>();
		for(int i = 0; i < list.size(); i++)
		{
			returnList.add(list.get(i).toLocation());
		}
		return returnList;
	}
	
	// Turns a location list into a serializablelocation list.
	public static ArrayList<SerializableLocation> listToSerializable(ArrayList<Location> list)
	{
		ArrayList<SerializableLocation> returnList = new ArrayList<SerializableLocation>();
		for(int i = 0; i < list.size(); i++)
		{
			returnList.add(new SerializableLocation(list.get(i)));
		}
		return returnList;
	}
}
