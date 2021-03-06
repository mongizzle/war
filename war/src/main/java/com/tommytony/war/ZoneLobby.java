package com.tommytony.war;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import bukkit.tommytony.war.War;

import com.tommytony.war.mappers.VolumeMapper;
import com.tommytony.war.volumes.VerticalVolume;
import com.tommytony.war.volumes.Volume;

/**
 * 
 * @author tommytony
 *
 */
public class ZoneLobby {
	private final War war;
	private final Warzone warzone;
	private BlockFace wall;
	private Volume volume;
	Block lobbyMiddleWallBlock = null;	// on the zone wall, one above the zone lobby floor
	
	Block warHubLinkGate = null;
	
	Block diamondGate = null;
	Block ironGate = null;
	Block goldGate = null;
	Block autoAssignGate = null;
	
	Block zoneTeleportBlock = null;
	
	private final int lobbyHeight = 3;
	private final int lobbyHalfSide = 7;
	private final int lobbyDepth = 10;
	
	public ZoneLobby(War war, Warzone warzone, BlockFace wall) {
		this.war = war;
		this.warzone = warzone;
		this.changeWall(wall);
	}
	
	/**
	 * Convenience ctor when loading form disk.
	 * This figures out the middle wall block of the lobby from the volume instead 
	 * of the other way around.
	 */
	public ZoneLobby(War war, Warzone warzone, BlockFace wall, Volume volume) {
		this.war = war;
		this.warzone = warzone;
		Volume zoneVolume = warzone.getVolume();
		this.wall = wall;
		this.setVolume(volume);
		// we're setting the zoneVolume directly, so we need to figure out the lobbyMiddleWallBlock on our own
		if(wall == BlockFace.NORTH) {
			lobbyMiddleWallBlock = volume.getCornerOne().getFace(BlockFace.UP).getFace(BlockFace.EAST, lobbyHalfSide); 
		} else if (wall == BlockFace.EAST){
			lobbyMiddleWallBlock = volume.getCornerOne().getFace(BlockFace.UP).getFace(BlockFace.SOUTH, lobbyHalfSide);
 		} else if (wall == BlockFace.SOUTH){
 			lobbyMiddleWallBlock = volume.getCornerOne().getFace(BlockFace.UP).getFace(BlockFace.WEST, lobbyHalfSide);
		} else if (wall == BlockFace.WEST){
			lobbyMiddleWallBlock = volume.getCornerOne().getFace(BlockFace.UP).getFace(BlockFace.NORTH, lobbyHalfSide);
		}
	}
	
	public void changeWall(BlockFace newWall) {
		if(this.wall != newWall) {
			if(volume == null) {
				// no previous wall
				this.volume = new Volume("lobby", war, warzone.getWorld());
			} else {
				// move the lobby
				this.volume.resetBlocks();
			}
			
			this.wall = newWall;
			// find center of the wall and set the new volume corners
			VerticalVolume zoneVolume = warzone.getVolume();
			
			Block corner1 = null;
			Block corner2 = null;
			
			if(wall == BlockFace.NORTH) {
				int wallStart = zoneVolume.getMinZ();
				int wallEnd = zoneVolume.getMaxZ();
				int x = zoneVolume.getMinX();
				int wallLength = wallEnd - wallStart + 1;
				int wallCenterPos = wallStart + wallLength / 2;
				int highestNonAirBlockAtCenter = warzone.getWorld().getHighestBlockYAt(x, wallCenterPos);
				lobbyMiddleWallBlock = warzone.getWorld().getBlockAt(x, highestNonAirBlockAtCenter+1, wallCenterPos);
				corner1 = warzone.getWorld().getBlockAt(x, highestNonAirBlockAtCenter, wallCenterPos + lobbyHalfSide);
				corner2 = warzone.getWorld().getBlockAt(x - lobbyDepth, 
						highestNonAirBlockAtCenter + 1 + lobbyHeight, wallCenterPos - lobbyHalfSide);
			} else if (wall == BlockFace.EAST){
				int wallStart = zoneVolume.getMinX();
				int wallEnd = zoneVolume.getMaxX();
				int z = zoneVolume.getMinZ();
				int wallLength = wallEnd - wallStart + 1;
				int wallCenterPos = wallStart + wallLength / 2;
				int highestNonAirBlockAtCenter = warzone.getWorld().getHighestBlockYAt(wallCenterPos, z);
				lobbyMiddleWallBlock = warzone.getWorld().getBlockAt(wallCenterPos, highestNonAirBlockAtCenter + 1, z);
				corner1 = warzone.getWorld().getBlockAt(wallCenterPos - lobbyHalfSide, highestNonAirBlockAtCenter, z);
				corner2 = warzone.getWorld().getBlockAt(wallCenterPos + lobbyHalfSide, 
						highestNonAirBlockAtCenter + 1 + lobbyHeight, z - lobbyDepth);
	 		} else if (wall == BlockFace.SOUTH){
	 			int wallStart = zoneVolume.getMinZ();
				int wallEnd = zoneVolume.getMaxZ();
				int x = zoneVolume.getMaxX();
				int wallLength = wallEnd - wallStart + 1;
				int wallCenterPos = wallStart + wallLength / 2;
				int highestNonAirBlockAtCenter = warzone.getWorld().getHighestBlockYAt(x, wallCenterPos);
				lobbyMiddleWallBlock = warzone.getWorld().getBlockAt(x, highestNonAirBlockAtCenter + 1, wallCenterPos);
				corner1 = warzone.getWorld().getBlockAt(x, highestNonAirBlockAtCenter, wallCenterPos - lobbyHalfSide);
				corner2 = warzone.getWorld().getBlockAt(x + lobbyDepth, 
						highestNonAirBlockAtCenter + 1 + lobbyHeight, wallCenterPos + lobbyHalfSide);
			} else if (wall == BlockFace.WEST){
				int wallStart = zoneVolume.getMinX();
				int wallEnd = zoneVolume.getMaxX();
				int z = zoneVolume.getMaxZ();
				int wallLength = wallEnd - wallStart + 1;
				int wallCenterPos = wallStart + wallLength / 2;
				int highestNonAirBlockAtCenter = warzone.getWorld().getHighestBlockYAt(wallCenterPos, z);
				lobbyMiddleWallBlock = warzone.getWorld().getBlockAt(wallCenterPos, highestNonAirBlockAtCenter + 1, z);
				corner1 = warzone.getWorld().getBlockAt(wallCenterPos + lobbyHalfSide, highestNonAirBlockAtCenter, z);
				corner2 = warzone.getWorld().getBlockAt(wallCenterPos - lobbyHalfSide, highestNonAirBlockAtCenter + 1 + lobbyHeight, z + lobbyDepth);
			}
			
			if(corner1 != null && corner2 != null) {
				// save the blocks, wide enough for three team gates, 3+1 high and 10 deep, extruding out from the zone wall.
				this.volume.setCornerOne(corner1);
				this.volume.setCornerTwo(corner2);
				this.volume.saveBlocks();
				VolumeMapper.save(volume, warzone.getName(), war);
			}
		}
	}
	
	public void initialize() {
		// maybe the number of teams change, now reset the gate positions
		setGatePositions(lobbyMiddleWallBlock);

		if(lobbyMiddleWallBlock != null && volume != null && volume.isSaved()) {
			// flatten the area (set all but floor to air, then replace any floor air blocks with glass)
			this.volume.clearBlocksThatDontFloat();
			this.volume.setToMaterial(Material.AIR);
			this.volume.setFaceMaterial(BlockFace.DOWN, Material.GLASS);	// beautiful
			
			// add war hub link gate
			if(war.getWarHub() != null) {
				placeGate(warHubLinkGate, Material.OBSIDIAN);
				// add warhub sign
				String[] lines = new String[4];
				lines[0] = "";
				lines[1] = "To War hub";
				lines[2] = "";
				lines[3] = "";
				resetGateSign(warHubLinkGate, lines, false);
			}
			
			// add team gates or single auto assign gate
			placeAutoAssignGate();
			placeGate(diamondGate, TeamMaterials.TEAMDIAMOND);
			placeGate(ironGate, TeamMaterials.TEAMIRON);
			placeGate(goldGate, TeamMaterials.TEAMGOLD);
			for(Team t : warzone.getTeams()) {
				resetTeamGateSign(t);
			}
			
			// set zone tp
			zoneTeleportBlock = lobbyMiddleWallBlock.getFace(wall, 6);
			int yaw = 0;
			if(wall == BlockFace.WEST) {
				yaw = 180;
			} else if (wall == BlockFace.SOUTH) {
				yaw = 90;
			} else if (wall == BlockFace.EAST) {
				yaw = 0;
			} else if (wall == BlockFace.NORTH) {
				yaw = 270;
			}
			warzone.setTeleport(new Location(warzone.getWorld(), zoneTeleportBlock.getX(), zoneTeleportBlock.getY(), zoneTeleportBlock.getZ(), yaw, 0));
			
			// set zone sign
			Block zoneSignBlock = lobbyMiddleWallBlock.getFace(wall, 4);
			zoneSignBlock.setType(Material.SIGN_POST);
			if(wall == BlockFace.NORTH) {
				zoneSignBlock.setData((byte)4);
			} else if(wall == BlockFace.EAST) {
				zoneSignBlock.setData((byte)8);
			} else if(wall == BlockFace.SOUTH) {
				zoneSignBlock.setData((byte)12);
			} else if(wall == BlockFace.WEST) {
				zoneSignBlock.setData((byte)0);
			}
			BlockState state = zoneSignBlock.getState();
			if(state instanceof Sign) {
				Sign sign = (Sign) state;
				sign.setLine(0, "Warzone");
				sign.setLine(1, warzone.getName());
				if(autoAssignGate != null) {
					sign.setLine(2, "Walk in the");
					sign.setLine(3, "auto-assign gate.");
				} else {
					sign.setLine(2, "");
					sign.setLine(3, "Pick your team.");
				}
				state.update(true);
			}
			
			// lets get some light in here
			if(wall == BlockFace.NORTH || wall == BlockFace.SOUTH) {
				lobbyMiddleWallBlock.getFace(BlockFace.DOWN).getFace(BlockFace.WEST, lobbyHalfSide - 1).getFace(wall, 9).setType(Material.GLOWSTONE);
				lobbyMiddleWallBlock.getFace(BlockFace.DOWN).getFace(BlockFace.EAST, lobbyHalfSide - 1).getFace(wall, 9).setType(Material.GLOWSTONE);
			} else {
				lobbyMiddleWallBlock.getFace(BlockFace.DOWN).getFace(BlockFace.NORTH, lobbyHalfSide - 1).getFace(wall, 9).setType(Material.GLOWSTONE);
				lobbyMiddleWallBlock.getFace(BlockFace.DOWN).getFace(BlockFace.SOUTH, lobbyHalfSide - 1).getFace(wall, 9).setType(Material.GLOWSTONE);
			}
		} else {
			war.warn("Failed to initalize zone lobby for zone " + warzone.getName());
		}
	}

	private void setGatePositions(Block lobbyMiddleWallBlock) {
		BlockFace leftSide = null;	// look at the zone
		BlockFace rightSide = null;
		if(wall == BlockFace.NORTH) {
			leftSide = BlockFace.EAST;
			rightSide = BlockFace.WEST;
		} else if(wall == BlockFace.EAST) {
			leftSide = BlockFace.SOUTH;
			rightSide = BlockFace.NORTH;
		} else if(wall == BlockFace.SOUTH) {
			leftSide = BlockFace.WEST;
			rightSide = BlockFace.EAST;
		} else if(wall == BlockFace.WEST) {
			leftSide = BlockFace.NORTH;
			rightSide = BlockFace.SOUTH;
		}  
		if(warzone.getAutoAssignOnly()){
			autoAssignGate = lobbyMiddleWallBlock;
			diamondGate = null;
			ironGate = null;
			goldGate = null;
		} else if(warzone.getTeams().size() == 1) { 
			autoAssignGate = null;
			if(warzone.getTeamByMaterial(TeamMaterials.TEAMDIAMOND) != null) {
				diamondGate = lobbyMiddleWallBlock;
				ironGate = null;
				goldGate = null;
			} else if (warzone.getTeamByMaterial(TeamMaterials.TEAMIRON) != null) {
				ironGate = lobbyMiddleWallBlock;
				diamondGate = null;
				goldGate = null;
			} else if (warzone.getTeamByMaterial(TeamMaterials.TEAMGOLD) != null) {
				goldGate = lobbyMiddleWallBlock;
				diamondGate = null;
				ironGate = null;
			}
		} else if(warzone.getTeams().size() == 2) {
			autoAssignGate = null;
			if(warzone.getTeamByMaterial(TeamMaterials.TEAMDIAMOND) != null 
					&& warzone.getTeamByMaterial(TeamMaterials.TEAMIRON) != null) {
				diamondGate = lobbyMiddleWallBlock.getFace(leftSide, 2);
				ironGate = lobbyMiddleWallBlock.getFace(rightSide, 2);
				goldGate = null;
			} else if (warzone.getTeamByMaterial(TeamMaterials.TEAMIRON) != null
					&& warzone.getTeamByMaterial(TeamMaterials.TEAMGOLD) != null) {
				ironGate = lobbyMiddleWallBlock.getFace(leftSide, 2);
				goldGate = lobbyMiddleWallBlock.getFace(rightSide, 2);
				diamondGate = null;
			}
			if (warzone.getTeamByMaterial(TeamMaterials.TEAMDIAMOND) != null 
					&& warzone.getTeamByMaterial(TeamMaterials.TEAMGOLD) != null) {
				diamondGate = lobbyMiddleWallBlock.getFace(leftSide, 2);
				goldGate = lobbyMiddleWallBlock.getFace(rightSide, 2);
				ironGate = null;
			}
		} else if(warzone.getTeams().size() == 3) {
			autoAssignGate = null;
			diamondGate = lobbyMiddleWallBlock.getFace(leftSide, 4);
			ironGate = lobbyMiddleWallBlock;
			goldGate = lobbyMiddleWallBlock.getFace(rightSide, 4);
		}
		warHubLinkGate = lobbyMiddleWallBlock.getFace(wall, 9);
	}

	private void placeGate(Block block,
			Material teamMaterial) {
		if(block != null) {
			BlockFace leftSide = null;	// look at the zone
			BlockFace rightSide = null;
			if(wall == BlockFace.NORTH) {
				leftSide = BlockFace.EAST;
				rightSide = BlockFace.WEST;
			} else if(wall == BlockFace.EAST) {
				leftSide = BlockFace.SOUTH;
				rightSide = BlockFace.NORTH;
			} else if(wall == BlockFace.SOUTH) {
				leftSide = BlockFace.WEST;
				rightSide = BlockFace.EAST;
			} else if(wall == BlockFace.WEST) {
				leftSide = BlockFace.NORTH;
				rightSide = BlockFace.SOUTH;
			}  
			block.getFace(BlockFace.DOWN).setType(Material.GLOWSTONE);
			block.setType(Material.PORTAL);
			block.getFace(BlockFace.UP).setType(Material.PORTAL);
			block.getFace(leftSide).setType(teamMaterial);
			block.getFace(rightSide).getFace(BlockFace.UP).setType(teamMaterial);
			block.getFace(leftSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(teamMaterial);
			block.getFace(rightSide).setType(teamMaterial);
			block.getFace(leftSide).getFace(BlockFace.UP).setType(teamMaterial);
			block.getFace(rightSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(teamMaterial);
			block.getFace(BlockFace.UP).getFace(BlockFace.UP).setType(teamMaterial);
		}
	}
	
	private void placeAutoAssignGate() {
		if(autoAssignGate != null) {
			BlockFace leftSide = null;	// look at the zone
			BlockFace rightSide = null;
			if(wall == BlockFace.NORTH) {
				leftSide = BlockFace.EAST;
				rightSide = BlockFace.WEST;
			} else if(wall == BlockFace.EAST) {
				leftSide = BlockFace.SOUTH;
				rightSide = BlockFace.NORTH;
			} else if(wall == BlockFace.SOUTH) {
				leftSide = BlockFace.WEST;
				rightSide = BlockFace.EAST;
			} else if(wall == BlockFace.WEST) {
				leftSide = BlockFace.NORTH;
				rightSide = BlockFace.SOUTH;
			}  
			
			Team diamondTeam = warzone.getTeamByMaterial(TeamMaterials.TEAMDIAMOND);
			Team ironTeam = warzone.getTeamByMaterial(TeamMaterials.TEAMIRON);
			Team goldTeam = warzone.getTeamByMaterial(TeamMaterials.TEAMGOLD);
			autoAssignGate.getFace(BlockFace.DOWN).setType(Material.GLOWSTONE);
			autoAssignGate.setType(Material.PORTAL);
			autoAssignGate.getFace(BlockFace.UP).setType(Material.PORTAL);
			if(diamondTeam != null && ironTeam != null && goldTeam != null) {
				autoAssignGate.getFace(leftSide).setType(TeamMaterials.TEAMDIAMOND);
				autoAssignGate.getFace(leftSide).getFace(BlockFace.UP).setType(TeamMaterials.TEAMIRON);
				autoAssignGate.getFace(leftSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMGOLD);
				autoAssignGate.getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMDIAMOND);
				autoAssignGate.getFace(rightSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMIRON);
				autoAssignGate.getFace(rightSide).getFace(BlockFace.UP).setType(TeamMaterials.TEAMGOLD);
				autoAssignGate.getFace(rightSide).setType(TeamMaterials.TEAMDIAMOND);			
			} else if (diamondTeam != null && ironTeam != null) {
				autoAssignGate.getFace(leftSide).setType(TeamMaterials.TEAMDIAMOND);
				autoAssignGate.getFace(leftSide).getFace(BlockFace.UP).setType(TeamMaterials.TEAMIRON);
				autoAssignGate.getFace(leftSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMDIAMOND);
				autoAssignGate.getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMIRON);
				autoAssignGate.getFace(rightSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMDIAMOND);
				autoAssignGate.getFace(rightSide).getFace(BlockFace.UP).setType(TeamMaterials.TEAMIRON);
				autoAssignGate.getFace(rightSide).setType(TeamMaterials.TEAMDIAMOND);	
			} else if (ironTeam != null && goldTeam != null) {
				autoAssignGate.getFace(leftSide).setType(TeamMaterials.TEAMIRON);
				autoAssignGate.getFace(leftSide).getFace(BlockFace.UP).setType(TeamMaterials.TEAMGOLD);
				autoAssignGate.getFace(leftSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMIRON);
				autoAssignGate.getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMGOLD);
				autoAssignGate.getFace(rightSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMIRON);
				autoAssignGate.getFace(rightSide).getFace(BlockFace.UP).setType(TeamMaterials.TEAMGOLD);
				autoAssignGate.getFace(rightSide).setType(TeamMaterials.TEAMIRON);	
			} else if (diamondTeam != null && goldTeam != null) {
				autoAssignGate.getFace(leftSide).setType(TeamMaterials.TEAMDIAMOND);
				autoAssignGate.getFace(leftSide).getFace(BlockFace.UP).setType(TeamMaterials.TEAMGOLD);
				autoAssignGate.getFace(leftSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMDIAMOND);
				autoAssignGate.getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMGOLD);
				autoAssignGate.getFace(rightSide).getFace(BlockFace.UP).getFace(BlockFace.UP).setType(TeamMaterials.TEAMDIAMOND);
				autoAssignGate.getFace(rightSide).getFace(BlockFace.UP).setType(TeamMaterials.TEAMGOLD);
				autoAssignGate.getFace(rightSide).setType(TeamMaterials.TEAMDIAMOND);	
			}
		}
	}

	public boolean isInTeamGate(Material team, Location location) {
		if(team == TeamMaterials.TEAMDIAMOND && diamondGate != null
				&& location.getBlockX() == diamondGate.getX()
				&& location.getBlockY() == diamondGate.getY()
				&& location.getBlockZ() == diamondGate.getZ()) {
			return true;
		} else if(team == TeamMaterials.TEAMIRON && ironGate != null
				&& location.getBlockX() == ironGate.getX()
				&& location.getBlockY() == ironGate.getY()
				&& location.getBlockZ() == ironGate.getZ()) {
			return true;
		} else if(team == TeamMaterials.TEAMGOLD && goldGate != null
				&& location.getBlockX() == goldGate.getX()
				&& location.getBlockY() == goldGate.getY()
				&& location.getBlockZ() == goldGate.getZ()) {
			return true;
		} 
		return false;
	}
	
	public boolean isAutoAssignGate(Location location) {
		if(autoAssignGate != null
				&& location.getBlockX() == autoAssignGate.getX()
				&& location.getBlockY() == autoAssignGate.getY()
				&& location.getBlockZ() == autoAssignGate.getZ()) {
			return true;
		} 
		return false;
	}
	
	public Volume getVolume() {
		return this.volume;
	}
	
	public void setVolume(Volume volume) {
		this.volume = volume;
	}
	

	public BlockFace getWall() {
		return wall;
	}

	public boolean isInWarHubLinkGate(Location location) {
		if(warHubLinkGate != null
				&& location.getBlockX() == warHubLinkGate.getX()
				&& location.getBlockY() == warHubLinkGate.getY()
				&& location.getBlockZ() == warHubLinkGate.getZ()) {
			return true;
		} 
		return false;
	}

	public boolean blockIsAGateBlock(Block block, BlockFace blockWall) {
		if(blockWall == wall) {
			return isPartOfGate(diamondGate, block)
					|| isPartOfGate(ironGate, block)
					|| isPartOfGate(goldGate, block)
					|| isPartOfGate(autoAssignGate, block);
		}
		return false;
	}

	private boolean isPartOfGate(Block gateBlock, Block block) {
		if(gateBlock != null) {
			BlockFace leftSide = null;	// look at the zone
			BlockFace rightSide = null;
			if(wall == BlockFace.NORTH) {
				leftSide = BlockFace.EAST;
				rightSide = BlockFace.WEST;
			} else if(wall == BlockFace.EAST) {
				leftSide = BlockFace.SOUTH;
				rightSide = BlockFace.NORTH;
			} else if(wall == BlockFace.SOUTH) {
				leftSide = BlockFace.WEST;
				rightSide = BlockFace.EAST;
			} else if(wall == BlockFace.WEST) {
				leftSide = BlockFace.NORTH;
				rightSide = BlockFace.SOUTH;
			}
			return (block.getX() == gateBlock.getX()
						&& block.getY() == gateBlock.getY()
						&& block.getZ() == gateBlock.getZ())
					||
					(block.getX() == gateBlock.getFace(BlockFace.UP).getX()
						&& block.getY() == gateBlock.getFace(BlockFace.UP).getY()
						&& block.getZ() == gateBlock.getFace(BlockFace.UP).getZ())
					||
					(block.getX() == gateBlock.getFace(leftSide).getX()
						&& block.getY() == gateBlock.getFace(leftSide).getY()
						&& block.getZ() == gateBlock.getFace(leftSide).getZ())
					||
					(block.getX() == gateBlock.getFace(leftSide).getFace(BlockFace.UP).getX()
						&& block.getY() == gateBlock.getFace(leftSide).getFace(BlockFace.UP).getY()
						&& block.getZ() == gateBlock.getFace(leftSide).getFace(BlockFace.UP).getZ())
					||
					(block.getX() == gateBlock.getFace(leftSide).getFace(BlockFace.UP).getFace(BlockFace.UP).getX()
						&& block.getY() == gateBlock.getFace(leftSide).getFace(BlockFace.UP).getFace(BlockFace.UP).getY()
						&& block.getZ() == gateBlock.getFace(leftSide).getFace(BlockFace.UP).getFace(BlockFace.UP).getZ())
					||
					(block.getX() == gateBlock.getFace(BlockFace.UP).getFace(BlockFace.UP).getX()
						&& block.getY() == gateBlock.getFace(BlockFace.UP).getFace(BlockFace.UP).getY()
						&& block.getZ() == gateBlock.getFace(BlockFace.UP).getFace(BlockFace.UP).getZ())
					||
					(block.getX() == gateBlock.getFace(rightSide).getFace(BlockFace.UP).getX()
						&& block.getY() == gateBlock.getFace(rightSide).getFace(BlockFace.UP).getY()
						&& block.getZ() == gateBlock.getFace(rightSide).getFace(BlockFace.UP).getZ())
					||
					(block.getX() == gateBlock.getFace(rightSide).getFace(BlockFace.UP).getFace(BlockFace.UP).getX()
						&& block.getY() == gateBlock.getFace(rightSide).getFace(BlockFace.UP).getFace(BlockFace.UP).getY()
						&& block.getZ() == gateBlock.getFace(rightSide).getFace(BlockFace.UP).getFace(BlockFace.UP).getZ())
					||
					(block.getX() == gateBlock.getFace(rightSide).getX()
						&& block.getY() == gateBlock.getFace(rightSide).getY()
						&& block.getZ() == gateBlock.getFace(rightSide).getZ())
					||
					(block.getX() == gateBlock.getX()
							&& block.getY() == gateBlock.getY() - 1
							&& block.getZ() == gateBlock.getZ())
					;
		}
		return false;
	}

	public Warzone getZone() {
		return this.warzone;
	}

	public void resetTeamGateSign(Team team) {
		if(team.getMaterial() == TeamMaterials.TEAMDIAMOND) {
			resetTeamGateSign(team, diamondGate);
		} else if(team.getMaterial() == TeamMaterials.TEAMIRON) {
			resetTeamGateSign(team, ironGate);
		} else if(team.getMaterial() == TeamMaterials.TEAMGOLD) {
			resetTeamGateSign(team, goldGate);
		}
//		
//		if(war.getWarHub() != null) {
//			war.getWarHub().resetZoneSign(warzone);
//		}
	}

	private void resetTeamGateSign(Team team, Block gate) {
		if(gate != null) {
			String[] lines = new String[4];
			lines[0] =  "Team " + team.getName();
			lines[1] = team.getPlayers().size() + "/" + warzone.getTeamCap() + " players";
			lines[2] = team.getPoints() + "/" + warzone.getScoreCap() + " pts";
			lines[3] = team.getRemainingTickets() + "/" + warzone.getLifePool() + " lives left";
			resetGateSign(gate, lines, true);
		}
	}
	
	private void resetGateSign(Block gate, String[] lines, boolean awayFromWall) {
		Block block = null;
		BlockFace direction = null;
		if(awayFromWall) {
			direction = wall;
		} else if (wall == BlockFace.NORTH) {
			direction = BlockFace.SOUTH;
		} else if (wall == BlockFace.EAST) {
			direction = BlockFace.WEST;
		} else if (wall == BlockFace.SOUTH) {
			direction = BlockFace.NORTH;
		} else if (wall == BlockFace.WEST) {
			direction = BlockFace.EAST;
		}
		if(wall == BlockFace.NORTH) {
			block = gate.getFace(direction).getFace(BlockFace.EAST);
			if(block.getType() != Material.SIGN_POST) block.setType(Material.SIGN_POST);
			if(awayFromWall) block.setData((byte)4);
			else block.setData((byte)12);
		} else if(wall == BlockFace.EAST) {
			block = gate.getFace(direction).getFace(BlockFace.SOUTH);
			if(block.getType() != Material.SIGN_POST) block.setType(Material.SIGN_POST);
			if(awayFromWall) block.setData((byte)8);
			else block.setData((byte)0);
		} else if(wall == BlockFace.SOUTH) {
			block = gate.getFace(direction).getFace(BlockFace.WEST);
			if(block.getType() != Material.SIGN_POST) block.setType(Material.SIGN_POST);
			if(awayFromWall) block.setData((byte)12);
			else block.setData((byte)4);
		} else if(wall == BlockFace.WEST) {
			block = gate.getFace(direction).getFace(BlockFace.NORTH);
			if(block.getType() != Material.SIGN_POST) block.setType(Material.SIGN_POST);
			if(awayFromWall) block.setData((byte)0);
			else block.setData((byte)8);
		}
		
		BlockState state = block.getState();
		if(state instanceof Sign) {
			Sign sign = (Sign) state;
			sign.setLine(0, lines[0]);
			sign.setLine(1, lines[1]);
			sign.setLine(2, lines[2]);
			sign.setLine(3, lines[3]);
			state.update(true);
		}
	}
	
	public boolean isLeavingZone(Location location) {
		
		BlockFace inside = null;
		BlockFace left = null;
		BlockFace right = null;
		if (wall == BlockFace.NORTH) {
			inside = BlockFace.SOUTH;
			left = BlockFace.WEST;
			right = BlockFace.EAST;
		} else if (wall == BlockFace.EAST) {
			inside = BlockFace.WEST;
			left = BlockFace.NORTH;
			right = BlockFace.SOUTH;
		} else if (wall == BlockFace.SOUTH) {
			inside = BlockFace.NORTH;
			left = BlockFace.EAST;
			right = BlockFace.WEST;
		} else if (wall == BlockFace.WEST) {
			inside = BlockFace.EAST;
			left = BlockFace.SOUTH;
			right = BlockFace.NORTH;
		}		
		if(autoAssignGate != null){
			if(leaving(location, autoAssignGate, inside, left, right)) return true;
		} else if(diamondGate != null){
			if(leaving(location, diamondGate, inside, left, right)) return true;
		} else if(ironGate != null){
			if(leaving(location, ironGate, inside, left, right)) return true;
		} else if(goldGate != null){
			if(leaving(location, goldGate, inside, left, right)) return true;
		}		
		return false;
	}

	private boolean leaving(Location location, Block gate, BlockFace inside,
			BlockFace left, BlockFace right) {
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		Block out = gate.getFace(inside);
		Block outL = out.getFace(left);
		Block outR = out.getFace(right);
		Block out2 = gate.getFace(inside, 2);
		Block out2L = out2.getFace(left);
		Block out2R = out2.getFace(right);
		if(out.getX() == x && out.getY() == y && out.getZ() == z) {
			return true;
		} else if(outL.getX() == x && outL.getY() == y && outL.getZ() == z) {
			return true;
		} else if(outR.getX() == x && outR.getY() == y && outR.getZ() == z) {
			return true;
		} else if(out2.getX() == x && out2.getY() == y && out2.getZ() == z) {
			return true;
		} else if(out2L.getX() == x && out2L.getY() == y && out2L.getZ() == z) {
			return true;
		} else if(out2R.getX() == x && out2R.getY() == y && out2R.getZ() == z) {
			return true;
		}
		return false;
	}
}
