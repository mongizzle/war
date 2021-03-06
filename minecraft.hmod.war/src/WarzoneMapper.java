import java.io.File;
import java.io.IOException;
import java.util.List;


public class WarzoneMapper {

	public static Warzone load(War war, String name) {
		PropertiesFile warzoneConfig = new PropertiesFile("warzone-" + name + ".txt");
		try {
			warzoneConfig.load();
		} catch (IOException e) {
			war.getLogger().info("Failed to load warzone-" + name + ".txt file.");
			e.printStackTrace();
		}
		Warzone warzone = new Warzone(war, name);
		
		// Create file if needed 
		if(!warzoneConfig.containsKey("name")) {
			WarzoneMapper.save(warzone, false);
			war.getLogger().info("Warzone " + name + " config file created.");
			try {
				warzoneConfig.load();
			} catch (IOException e) {
				war.getLogger().info("Failed to reload warzone-" + name + ".txt file after creating it.");
				e.printStackTrace();
			}
		}
				
		// northwest
		String nwStr = warzoneConfig.getString("northWest");
		String[] nwStrSplit = nwStr.split(",");
		int nwX = Integer.parseInt(nwStrSplit[0]);
		int nwY = Integer.parseInt(nwStrSplit[1]);
		int nwZ = Integer.parseInt(nwStrSplit[2]);
		Location nw = new Location(nwX, nwY, nwZ);
		warzone.setNorthwest(nw);
		
		// southeast
		String seStr = warzoneConfig.getString("southEast");
		String[] seStrSplit = seStr.split(",");
		int seX = Integer.parseInt(seStrSplit[0]);
		int seY = Integer.parseInt(seStrSplit[1]);
		int seZ = Integer.parseInt(seStrSplit[2]);
		Location se = new Location(seX, seY, seZ);
		warzone.setSoutheast(se);
		
		// teleport
		String teleportStr = warzoneConfig.getString("teleport");
		if(teleportStr != null && !teleportStr.equals("")) {
			String[] teleportSplit = teleportStr.split(",");
			int teleX = Integer.parseInt(teleportSplit[0]);
			int teleY = Integer.parseInt(teleportSplit[1]);
			int teleZ = Integer.parseInt(teleportSplit[2]);
			warzone.setTeleport(new Location(teleX, teleY, teleZ));
		}
		
		// teams
		String teamsStr = warzoneConfig.getString("teams");
		String[] teamsSplit = teamsStr.split(";");
		warzone.getTeams().clear();
		for(String teamStr : teamsSplit) {
			if(teamStr != null && !teamStr.equals("")){
				String[] teamStrSplit = teamStr.split(",");
				int teamX = Integer.parseInt(teamStrSplit[1]);
				int teamY = Integer.parseInt(teamStrSplit[2]);
				int teamZ = Integer.parseInt(teamStrSplit[3]);
				Team team = new Team(teamStrSplit[0], 
									new Location(teamX, teamY, teamZ));
				team.setRemainingTickets(warzone.getLifePool());
				warzone.getTeams().add(team);
			}
		}
		
		// ff
		warzone.setFriendlyFire(warzoneConfig.getBoolean("friendlyFire"));
		
		// loadout
		String loadoutStr = warzoneConfig.getString("loadout");
		String[] loadoutStrSplit = loadoutStr.split(";");
		warzone.getLoadout().clear();
		for(String itemStr : loadoutStrSplit) {
			if(itemStr != null && !itemStr.equals("")) {
				String[] itemStrSplit = itemStr.split(",");
				Item item = new Item(Integer.parseInt(itemStrSplit[0]),
						Integer.parseInt(itemStrSplit[1]), Integer.parseInt(itemStrSplit[2]));
				warzone.getLoadout().add(item);
			}
		}
		
		// life pool
		warzone.setLifePool(warzoneConfig.getInt("lifePool"));
				
		// monuments
		String monumentsStr = warzoneConfig.getString("monuments");
		String[] monumentsSplit = monumentsStr.split(";");
		warzone.getMonuments().clear();
		for(String monumentStr  : monumentsSplit) {
			if(monumentStr != null && !monumentStr.equals("")){
				String[] monumentStrSplit = monumentStr.split(",");
				int monumentX = Integer.parseInt(monumentStrSplit[1]);
				int monumentY = Integer.parseInt(monumentStrSplit[2]);
				int monumentZ = Integer.parseInt(monumentStrSplit[3]);
				Monument monument = new Monument(monumentStrSplit[0], war, 
										new Location(monumentX, monumentY, monumentZ));
				warzone.getMonuments().add(monument);
			}
		}
		
		// zone blocks 
		PropertiesFile warzoneBlocksFile = new PropertiesFile("warzone-" + warzone.getName() + ".dat");
		int northSouth = ((int)(warzone.getSoutheast().x)) - ((int)(warzone.getNorthwest().x));
		int eastWest = ((int)(warzone.getNorthwest().z)) - ((int)(warzone.getSoutheast().z));
		int minY = 0;
		int maxY = 128;
		int[][][] state = new int[northSouth][128][eastWest];
		String stateStr = warzoneBlocksFile.getString("zoneBlocks");
		String[] stateStrSplit = stateStr.split(",");
		int splitIndex = 0;
		if(stateStrSplit.length > 1) {
			for(int i = 0; i < northSouth; i++){
				for(int j = 0; j < maxY - minY; j++) {
					for(int k = 0; k < eastWest; k++) {
						String currentBlockType = stateStrSplit[splitIndex];
						if(currentBlockType != null && !currentBlockType.equals("")) {
							state[i][j][k] = Integer.parseInt(currentBlockType);
						}
						splitIndex++;
					}
				}
			}
		}
		warzone.setInitialState(state);
		
		// monument blocks
		for(Monument monument: warzone.getMonuments()) {
			String monumentBlocksStr = warzoneBlocksFile.getString("monument"+monument.getName()+"Blocks");
			String[] monumentBlocksSplit = monumentBlocksStr.split(",");
			int[] monumentState = new int[10];
			for(int i = 0; i < monumentBlocksSplit.length; i++) {
				String split = monumentBlocksSplit[i];
				if(split != null && !split.equals("")) {
					monumentState[i] = Integer.parseInt(split);
				}
			}
			monument.setInitialState(monumentState);
		}
		
		// team spawn blocks
		for(Team team : warzone.getTeams()) {
			String teamBlocksStr = warzoneBlocksFile.getString("team"+team.getName()+"Blocks");
			String[] teamBlocksSplit = teamBlocksStr.split(",");
			int[] teamState = new int[10];
			for(int i = 0; i < teamBlocksSplit.length; i++) {
				String split = teamBlocksSplit[i];
				if(split != null && !split.equals("")) {
					teamState[i] = Integer.parseInt(split);
				}
			}
			team.setOldSpawnState(teamState);
		}
		
		return warzone;
		
	}
	
	public static void save(Warzone warzone, boolean saveBlocks) {
		PropertiesFile warzoneConfig = new PropertiesFile("warzone-" + warzone.getName() + ".txt");
		
		// name
		warzoneConfig.setString("name", warzone.getName());
		
		// northwest
		String nwStr = "";
		Location nw = warzone.getNorthwest();
		if(nw != null) {
			nwStr = (int)nw.x + "," + (int)nw.y + "," + (int)nw.z;
		}
		warzoneConfig.setString("northWest", nwStr);
		
		// southeast
		String seStr = "";
		Location se = warzone.getSoutheast();
		if(se != null) {
			seStr = (int)se.x + "," + (int)se.y + "," + (int)se.z;
		}
		warzoneConfig.setString("southEast", seStr);
		
		// teleport
		String teleportStr = "";
		Location tele = warzone.getTeleport();
		if(tele != null) {
			teleportStr = (int)tele.x + "," + (int)tele.y + "," + (int)tele.z;
		}
		warzoneConfig.setString("teleport", teleportStr);
		
		// teams
		String teamsStr = "";
		List<Team> teams = warzone.getTeams();
		for(Team team : teams) {
			Location spawn = team.getTeamSpawn();
			teamsStr += team.getName() + "," + (int)spawn.x + "," + (int)spawn.y + "," + (int)spawn.z + ";";
		}
		warzoneConfig.setString("teams", teamsStr);
		
		// ff
		warzoneConfig.setBoolean("firendlyFire", warzone.getFriendlyFire());
		
		// loadout
		String loadoutStr = "";
		List<Item> items = warzone.getLoadout();
		for(Item item : items) {
			loadoutStr += item.getItemId() + "," + item.getAmount() + "," + item.getSlot() + ";";
		}
		warzoneConfig.setString("loadout", loadoutStr);
		
		// life pool
		warzoneConfig.setInt("lifePool", warzone.getLifePool());
		
		// monuments
		String monumentsStr = "";
		List<Monument> monuments = warzone.getMonuments();
		for(Monument monument : monuments) {
			Location monumentLoc = monument.getLocation();
			monumentsStr += monument.getName() + "," + (int)monumentLoc.x + "," + (int)monumentLoc.y + "," + (int)monumentLoc.z + ";";
		}
		warzoneConfig.setString("monuments", monumentsStr);
		warzoneConfig.save();
		
		if(saveBlocks) {
			// zone blocks
			PropertiesFile warzoneBlocksFile = new PropertiesFile("warzone-" + warzone.getName() + ".dat");
			int northSouth = ((int)(warzone.getSoutheast().x)) - ((int)(warzone.getNorthwest().x));
			int eastWest = ((int)(warzone.getNorthwest().z)) - ((int)(warzone.getSoutheast().z));
			int x = (int)warzone.getNorthwest().x;
			int minY = 0;
			int maxY = 128;
			int[][][] state = warzone.getInitialState();
			StringBuilder stateBuilder = new StringBuilder();
			if(state.length > 1) {
				for(int i = 0; i < northSouth; i++){
					for(int j = 0; j < maxY - minY; j++) {
						for(int k = 0; k < eastWest; k++) {
							stateBuilder.append(state[i][j][k] + ",");
						}
					}
				}
			}
			warzoneBlocksFile.setString("zoneBlocks", stateBuilder.toString());
			
			
			// monument blocks
			for(Monument monument: monuments) {
				String monumentBlocksStr = "";
				for(int type : monument.getInitialState()) {
					monumentBlocksStr += type + ",";
				}
				warzoneBlocksFile.setString("monument"+monument.getName()+"Blocks", monumentBlocksStr);
			}
			
			// team spawn blocks
			for(Team team : teams) {
				String teamBlocksStr = "";
				for(int type : team.getOldSpawnState()) {
					teamBlocksStr += type + ",";
				}
				warzoneBlocksFile.setString("team"+team.getName()+"Blocks", teamBlocksStr);
			}
			
			warzoneBlocksFile.save();
		}
		
	}
	
	public static void delete(String name) {
		File warzoneConfig = new File("warzone-" + name + ".txt");
		warzoneConfig.delete();
		File warzoneBlocksFile = new File("warzone-" + name + ".dat");
		warzoneBlocksFile.delete();
	}

}
