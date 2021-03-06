package com.tommytony.war.mappers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import bukkit.tommytony.war.War;

import com.tommytony.war.Monument;
import com.tommytony.war.Team;
import com.tommytony.war.TeamMaterials;
import com.tommytony.war.Warzone;
import com.tommytony.war.ZoneLobby;
import com.tommytony.war.volumes.VerticalVolume;
import com.tommytony.war.volumes.Volume;

/**
 * 
 * @author tommytony
 *
 */
public class WarzoneMapper {

	public static Warzone load(War war, String name, boolean loadBlocks) {
		//war.getLogger().info("Loading warzone " + name + " config and blocks...");
		PropertiesFile warzoneConfig = new PropertiesFile(war.getName() + "/warzone-" + name + ".txt");
		try {
			warzoneConfig.load();
		} catch (IOException e) {
			war.getLogger().info("Failed to load warzone-" + name + ".txt file.");
			e.printStackTrace();
		}
		
		World[] worlds = war.getServer().getWorlds();
		World world = worlds[0];
		Warzone warzone = new Warzone(war, world, name);
		
		// Create file if needed 
		if(!warzoneConfig.containsKey("name")) {
			WarzoneMapper.save(war, warzone, false);
			war.getLogger().info("Warzone " + name + " config file created.");
			try {
				warzoneConfig.load();
			} catch (IOException e) {
				//war.getLogger().info("Failed to reload warzone-" + name + ".txt file after creating it.");
				e.printStackTrace();
			}
		}
		
		// world
		//String worldStr = warzoneConfig.getProperty("world");
		warzone.setWorld(world);	// default world for now
				
		// northwest
		String nwStr = warzoneConfig.getString("northWest");
		if(nwStr != null && !nwStr.equals("")) {
			String[] nwStrSplit = nwStr.split(",");
			
			int nwX = Integer.parseInt(nwStrSplit[0]);
			int nwY = Integer.parseInt(nwStrSplit[1]);
			int nwZ = Integer.parseInt(nwStrSplit[2]);
			Location nw = new Location(world, nwX, nwY, nwZ);
			warzone.setNorthwest(nw);
		}
		
		// southeast
		String seStr = warzoneConfig.getString("southEast");
		if(nwStr != null && !nwStr.equals("")) {
			String[] seStrSplit = seStr.split(",");
			int seX = Integer.parseInt(seStrSplit[0]);
			int seY = Integer.parseInt(seStrSplit[1]);
			int seZ = Integer.parseInt(seStrSplit[2]);
			Location se = new Location(world, seX, seY, seZ);
			warzone.setSoutheast(se);
		}
		
		// teleport
		String teleportStr = warzoneConfig.getString("teleport");
		if(teleportStr != null && !teleportStr.equals("")) {
			String[] teleportSplit = teleportStr.split(",");
			int teleX = Integer.parseInt(teleportSplit[0]);
			int teleY = Integer.parseInt(teleportSplit[1]);
			int teleZ = Integer.parseInt(teleportSplit[2]);
			int yaw = Integer.parseInt(teleportSplit[3]);
			warzone.setTeleport(new Location(world, teleX, teleY, teleZ, yaw, 0));
		}
		
		// teams
		String teamsStr = warzoneConfig.getString("teams");
		if(teamsStr != null && !teamsStr.equals("")) {
			String[] teamsSplit = teamsStr.split(";");
			warzone.getTeams().clear();
			for(String teamStr : teamsSplit) {
				if(teamStr != null && !teamStr.equals("")){
					String[] teamStrSplit = teamStr.split(",");
					int teamX = Integer.parseInt(teamStrSplit[1]);
					int teamY = Integer.parseInt(teamStrSplit[2]);
					int teamZ = Integer.parseInt(teamStrSplit[3]);
					Team team = new Team(teamStrSplit[0], 
										TeamMaterials.teamMaterialFromString(teamStrSplit[0]),
										new Location(world, teamX, teamY, teamZ),
										war, warzone );
					team.setRemainingTickets(warzone.getLifePool());
					warzone.getTeams().add(team);
				}
			}
		}
		
		// ff
		warzone.setFriendlyFire(warzoneConfig.getBoolean("friendlyFire"));
		
		// loadout
		String loadoutStr = warzoneConfig.getString("loadout");
		if(loadoutStr != null && !loadoutStr.equals("")) {
			String[] loadoutStrSplit = loadoutStr.split(";");
			warzone.getLoadout().clear();
			for(String itemStr : loadoutStrSplit) {
				if(itemStr != null && !itemStr.equals("")) {
					String[] itemStrSplit = itemStr.split(",");
					ItemStack item = new ItemStack(Integer.parseInt(itemStrSplit[0]),
							Integer.parseInt(itemStrSplit[1]));
					warzone.getLoadout().put(Integer.parseInt(itemStrSplit[2]), item);
				}
			}
		}
		
		// life pool
		warzone.setLifePool(warzoneConfig.getInt("lifePool"));
		
		// drawZoneOutline
		warzone.setFriendlyFire(warzoneConfig.getBoolean("drawZoneOutline"));
		
		// autoAssignOnly
		warzone.setAutoAssignOnly(warzoneConfig.getBoolean("autoAssignOnly"));
		
		// team cap
		warzone.setTeamCap(warzoneConfig.getInt("teamCap"));
		
		// score cap
		warzone.setScoreCap(warzoneConfig.getInt("scoreCap"));

				
		// monuments
		String monumentsStr = warzoneConfig.getString("monuments");
		if(monumentsStr != null && !monumentsStr.equals("")) {
			String[] monumentsSplit = monumentsStr.split(";");
			warzone.getMonuments().clear();
			for(String monumentStr  : monumentsSplit) {
				if(monumentStr != null && !monumentStr.equals("")){
					String[] monumentStrSplit = monumentStr.split(",");
					int monumentX = Integer.parseInt(monumentStrSplit[1]);
					int monumentY = Integer.parseInt(monumentStrSplit[2]);
					int monumentZ = Integer.parseInt(monumentStrSplit[3]);
					Monument monument = new Monument(monumentStrSplit[0], war, warzone, 
											new Location(world, monumentX, monumentY, monumentZ));
					warzone.getMonuments().add(monument);
				}
			}
		}
		
		// lobby
		String lobbyStr = warzoneConfig.getString("lobby");
		
		warzoneConfig.close();
		
		if(loadBlocks && warzone.getNorthwest() != null && warzone.getSoutheast() != null) {
			
			// zone blocks 
			VerticalVolume zoneVolume = VolumeMapper.loadVerticalVolume(warzone.getName(), warzone.getName(), war, warzone.getWorld());
			warzone.setVolume(zoneVolume);
		}
	
		// monument blocks
		for(Monument monument: warzone.getMonuments()) {
			monument.setVolume(VolumeMapper.loadVolume(monument.getName(),warzone.getName(), war, world));
		}
		
		// team spawn blocks
		for(Team team : warzone.getTeams()) {
			team.setVolume(VolumeMapper.loadVolume(team.getName(), warzone.getName(), war, world));
		}
		
		// lobby
		BlockFace lobbyFace = null;
		if(lobbyStr != null && !lobbyStr.equals("")){
			if(lobbyStr.equals("south")) {
				lobbyFace = BlockFace.SOUTH;
			} else if(lobbyStr.equals("east")) {
				lobbyFace = BlockFace.EAST;
			} else if(lobbyStr.equals("north")) {
				lobbyFace = BlockFace.NORTH;
			} else if(lobbyStr.equals("west")) {
				lobbyFace = BlockFace.WEST;
			}
			Volume lobbyVolume = VolumeMapper.loadVolume("lobby", warzone.getName(), war, world);
			ZoneLobby lobby = new ZoneLobby(war, warzone, lobbyFace, lobbyVolume);
			warzone.setLobby(lobby);
		}
				
		return warzone;
		
	}
	
	public static void save(War war, Warzone warzone, boolean saveAllBlocks) {
		(new File(war.getName()+"/dat/warzone-"+warzone.getName())).mkdir();
		PropertiesFile warzoneConfig = new PropertiesFile(war.getName() + "/warzone-" + warzone.getName() + ".txt");
		//war.getLogger().info("Saving warzone " + warzone.getName() + "...");
		
		// name
		warzoneConfig.setString("name", warzone.getName());
		
		// world
		warzoneConfig.setString("world", "world");	// default for now
		
		// northwest
		String nwStr = "";
		Location nw = warzone.getNorthwest();
		if(nw != null) {
			nwStr = nw.getBlockX() + "," + nw.getBlockY() + "," + nw.getBlockZ();
		}
		warzoneConfig.setString("northWest", nwStr);
		
		// southeast
		String seStr = "";
		Location se = warzone.getSoutheast();
		if(se != null) {
			seStr = se.getBlockX() + "," + se.getBlockY() + "," + se.getBlockZ();
		}
		warzoneConfig.setString("southEast", seStr);
		
		// teleport
		String teleportStr = "";
		Location tele = warzone.getTeleport();
		if(tele != null) {
			teleportStr = tele.getBlockX() + "," + tele.getBlockY() + "," + tele.getBlockZ() + "," + (int)tele.getYaw();
		}
		warzoneConfig.setString("teleport", teleportStr);
		
		// teams
		String teamsStr = "";
		List<Team> teams = warzone.getTeams();
		for(Team team : teams) {
			Location spawn = team.getTeamSpawn();
			teamsStr += team.getName() + "," + spawn.getBlockX() + "," + spawn.getBlockY() + "," + spawn.getBlockZ() + ";";
		}
		warzoneConfig.setString("teams", teamsStr);
		
		// ff
		warzoneConfig.setBoolean("firendlyFire", warzone.getFriendlyFire());
		
		// loadout
		String loadoutStr = "";
		HashMap<Integer, ItemStack> items = warzone.getLoadout();
		for(Integer slot : items.keySet()) {
			ItemStack item = items.get(slot);
			loadoutStr += item.getTypeId() + "," + item.getAmount() + "," + slot + ";";
		}
		warzoneConfig.setString("loadout", loadoutStr);
		
		// life pool
		warzoneConfig.setInt("lifePool", warzone.getLifePool());
		
		// drawZoneOutline
		warzoneConfig.setBoolean("drawZoneOutline", warzone.isDrawZoneOutline());
		
		// autoAssignOnly
		warzoneConfig.setBoolean("autoAssignOnly", warzone.isAutoAssignOnly());
		
		// team cap
		warzoneConfig.setInt("teamCap", warzone.getTeamCap());
		
		// score cap
		warzoneConfig.setInt("scoreCap", warzone.getScoreCap());
		
		// monuments
		String monumentsStr = "";
		List<Monument> monuments = warzone.getMonuments();
		for(Monument monument : monuments) {
			Location monumentLoc = monument.getLocation();
			monumentsStr += monument.getName() + "," + monumentLoc.getBlockX() + "," + monumentLoc.getBlockY() + "," + monumentLoc.getBlockZ() + ";";
		}
		warzoneConfig.setString("monuments", monumentsStr);
		
		// lobby
		String lobbyStr = "";
		if(warzone.getLobby() != null) {
			if(BlockFace.SOUTH == warzone.getLobby().getWall()) {
				lobbyStr = "south";
			} else if(BlockFace.EAST == warzone.getLobby().getWall()) {
				lobbyStr = "east";
			} else if(BlockFace.NORTH == warzone.getLobby().getWall()) {
				lobbyStr = "north";
			} else if(BlockFace.WEST == warzone.getLobby().getWall()) {
				lobbyStr = "west";
			} 
		}
		warzoneConfig.setString("lobby", lobbyStr);
		
		warzoneConfig.save();
		warzoneConfig.close();
		
		if(saveAllBlocks) {
			// zone blocks
			VolumeMapper.save(warzone.getVolume(), warzone.getName(), war);
		}
			
		// monument blocks
		for(Monument monument: monuments) {
			VolumeMapper.save(monument.getVolume(), warzone.getName(), war);
		}
		
		// team spawn blocks
		for(Team team : teams) {
			VolumeMapper.save(team.getVolume(), warzone.getName(), war);
		}
		
		if(warzone.getLobby() != null) {
			VolumeMapper.save(warzone.getLobby().getVolume(), warzone.getName(), war);
		}
		
//		if(saveBlocks) {
//			war.getLogger().info("Saved warzone " + warzone.getName() + " config and blocks.");
//		} else {
//			war.getLogger().info("Saved warzone " + warzone.getName() + " config.");
//		}
	}
	
	public static void delete(War war, String name) {
		File zoneFolder = new File(war.getName() + "/dat/warzone-" + name);
		File[] files = zoneFolder.listFiles();
		for(File file : files) {
			boolean deletedData = file.delete();
			if(!deletedData) {
				war.warn("Failed to delete file " + file.getName());
			}
		}
		boolean deletedData = zoneFolder.delete();
		if(!deletedData) {
			war.warn("Failed to delete folder " + zoneFolder.getName());
		}
		File zoneFile = new File(war.getName() + "/warzone-" + name + ".txt");
		deletedData = zoneFile.delete();
		if(!deletedData) {
			war.warn("Failed to delete file " + zoneFile.getName());
		}
	}

}
