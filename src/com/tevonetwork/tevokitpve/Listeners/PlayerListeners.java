package com.tevonetwork.tevokitpve.Listeners;

import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.tevonetwork.tevoapi.API.Events.CombatLogEvent;
import com.tevonetwork.tevoapi.API.Events.KillEvent;
import com.tevonetwork.tevoapi.API.Events.PVPEvent;
import com.tevonetwork.tevoapi.API.Events.PlayerDamageEvent;
import com.tevonetwork.tevoapi.API.Scoreboards.ScoreboardManager;
import com.tevonetwork.tevoapi.API.Stats.StatManager;
import com.tevonetwork.tevoapi.API.Titles.Title;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilFirework;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer.playerSounds;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Gamemodes;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevoapi.Core.Messages.AnnounceMSG;
import com.tevonetwork.tevoapi.Core.Punish.Punish;
import com.tevonetwork.tevoapi.Core.Travel.SendtoLocation;
import com.tevonetwork.tevoapi.Economy.EconomyManager;
import com.tevonetwork.tevokitpve.TevoKitPVE;
import com.tevonetwork.tevokitpve.Kit.KitManager;
import com.tevonetwork.tevokitpve.PVE.KillStreakCountdown;
import com.tevonetwork.tevokitpve.PVE.KillStreakType;
import com.tevonetwork.tevokitpve.PVE.PVEManager;

public class PlayerListeners implements Listener {

	private TevoKitPVE main = TevoKitPVE.getInstance();
	private HashMap<String, BukkitTask> recall_Tasks = new HashMap<String, BukkitTask>();
	public static HashMap<String, Integer> killstreakcount = new HashMap<String, Integer>();
	public static HashMap<String, KillStreakType> killstreak = new HashMap<String, KillStreakType>();
	public static HashMap<String, KillStreakCountdown> killstreakcountdown = new HashMap<String, KillStreakCountdown>();

	private void cancelRecall(Player p) {
		if (recall_Tasks.containsKey(p.getName())) {
			p.setWalkSpeed(0.2F);
			recall_Tasks.get(p.getName()).cancel();
			recall_Tasks.remove(p.getName());
			Title title = new Title("", CC.tnError + "Teleport Cancelled!");
			title.setfadeIn(0);
			title.setfadeOut(0);
			title.setStay(15);
			title.send(p);
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof LivingEntity) {
			KitManager.handleNPCClick(e.getRightClicked(), e.getPlayer());
		}
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent e) {
		final Player p = e.getPlayer();
		if ((p.getGameMode() == GameMode.CREATIVE) || (p.getGameMode() == GameMode.SPECTATOR)) {
			return;
		}
		if (!e.isSneaking()) {
			cancelRecall(p);
		}
		else {
			if ((PVEManager.getSpawnRegion() != null) && (!PVEManager.getSpawnRegion().containsBlock(p.getLocation()))) {
				p.setWalkSpeed(0.0F);
				recall_Tasks.put(p.getName(), new BukkitRunnable() {
					int counter = 1;

					@Override
					public void run() {
						int recall = 5;
						StringBuilder builder = new StringBuilder();
						builder.append(CC.cGreen);
						int greenblocks = 0;
						for (greenblocks = 0; greenblocks < counter; greenblocks++) {
							builder.append("█");
						}
						builder.append(CC.cRED);
						for (int redblocks = 0; redblocks < (recall - greenblocks); redblocks++) {
							builder.append("░");
						}
						Title title = new Title();
						title.setTitle(builder.toString());
						title.setSubtitle(CC.tnInfo + "Teleporting to Spawn...");
						title.setfadeIn(0);
						title.setfadeOut(0);
						title.setStay(25);
						title.send(p);
						float pitch = 0.5F + (0.1F * counter);
						if (pitch > 2.0F) {
							pitch = 2.0F;
						}
						p.playSound(p.getLocation(), Sound.CLICK, 0.5F, pitch);
						if (counter >= recall) {
							recall_Tasks.remove(p.getName());
							new SendtoLocation(p, PVEManager.getSpawnpoint());
							p.setHealth(20.0);
							p.setWalkSpeed(0.2F);
							UtilPlayer.message(Category.PLAYER, p, CC.tnInfo + "You now have full health.");
							UtilPlayer.message(Category.PLAYER, p, CC.tnInfo + "Your killstreak has been reset!");
							PVEManager.setPVPEnabled(p, false);
							UtilPlayer.setoutCombat(p, false);
							killstreakcount.put(p.getName(), 0);
							cancel();
						}
						counter++;
					}
				}.runTaskTimer(main, 0L, 20L));
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (!e.getPlayer().getWorld().getName().startsWith("hub")) {
			return;
		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block clicked = e.getClickedBlock();
			if (clicked.getType() == Material.JUKEBOX) {
				e.setCancelled(true);
			}
			if (!UtilPlayer.hasRank(e.getPlayer(), Rank.ADMIN)) {
				if ((clicked.getType() == Material.WOODEN_DOOR) || (clicked.getType() == Material.TRAP_DOOR) || (clicked.getType() == Material.WOOD_BUTTON) || (clicked.getType() == Material.LEVER)
						|| (clicked.getType() == Material.STONE_BUTTON) || (clicked.getType() == Material.CHEST) || (clicked.getType() == Material.ENDER_CHEST)) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		p.setWalkSpeed(0.2F);
		KitManager.addPlayerCache(p);
		e.setJoinMessage(null);
		p.teleport(PVEManager.getSpawnpoint());
		UtilPlayer.clearInv(p);
		PVEManager.setPVPEnabled(p, false);
		p.setGameMode(GameMode.SURVIVAL);
		p.setLevel(0);
		p.setExp(0.0F);
		p.setHealth(20.0);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		e.setQuitMessage(null);
		KitManager.unequipKit(p, true);
		KitManager.removeCache(p);
		PVEManager.handleLogout(p);
		killstreakcount.remove(p);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (p.getGameMode() != GameMode.CREATIVE) {
			e.setCancelled(true);
			p.updateInventory();
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		cancelRecall(e.getPlayer());
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		if (PVEManager.stopping) {
			e.setResult(Result.KICK_OTHER);
			e.setKickMessage(CC.tnInfo + "Server is restarting, try again later!");
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if ((e.getFrom().getBlockX() != e.getTo().getBlockX()) || (e.getFrom().getBlockY() != e.getTo().getBlockY()) || (e.getFrom().getBlockZ() != e.getTo().getBlockZ())) {
			Player p = e.getPlayer();
			if ((p.getGameMode() == GameMode.CREATIVE) || (p.getGameMode() == GameMode.SPECTATOR)) {
				PVEManager.setPVPEnabled(p, false);
				return;
			}
			if (PVEManager.getSpawnRegion() == null) {
				return;
			}
			if (PVEManager.stopping) {
				return;
			}
			if (p.getGameMode() != GameMode.CREATIVE) {
				if ((!PVEManager.getSpawnRegion().containsBlock(p.getLocation())) && (!KitManager.hasKit(p))) {
					new SendtoLocation(p, PVEManager.getSpawnpoint());
					Title msg = new Title("", CC.cRED + CC.fBold + "Please select a kit using an NPC!");
					msg.setfadeIn(20);
					msg.setStay(20);
					msg.setfadeOut(20);
					msg.send(p);
				}
			}
			if (PVEManager.getSpawnRegion().containsBlock(p.getLocation())) {
				PVEManager.setPVPEnabled(p, false);
			}
			else {
				if (!PVEManager.isPVPEnabled(p)) {
					Title bye = new Title();
					bye.setTitle("");
					bye.setSubtitle(CC.tnUse + "Hold Shift " + CC.tnInfo + "to return to spawn.");
					bye.setfadeIn(0);
					bye.setStay(20);
					bye.setfadeOut(20);
					bye.send(p);
				}
				PVEManager.setPVPEnabled(p, true);
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		e.getDrops().clear();
		killstreak.remove(e.getEntity().getName());
		if (killstreakcountdown.containsKey(e.getEntity().getName())) {
			killstreakcountdown.get(e.getEntity().getName()).cancel();
			killstreakcountdown.remove(e.getEntity().getName());
		}
		KitManager.unequipKit(e.getEntity(), false);
		PVEManager.setPVPEnabled(e.getEntity(), false);
		UtilPlayer.setoutCombat(e.getEntity(), false);
		killstreakcount.put(e.getEntity().getName(), 0);
	}

	@EventHandler
	public void onKill(KillEvent e) {
		if ((e.getKiller() != null) && (e.getVictim() != null)) {
			Player killer = e.getKiller();
			Player victim = e.getVictim();
			KillStreakCountdown c = new KillStreakCountdown(killer.getName(), 8);
			if (killstreak.containsKey(killer.getName())) {
				KillStreakType type = killstreak.get(killer.getName());
				switch (type) {
					case DOUBLE:
						killstreak.put(killer.getName(), KillStreakType.TRIPLE);
						c.setTime(10);
						c.runTaskTimerAsynchronously(main, 0L, 20L);
						killstreakcountdown.put(killer.getName(), c);
						AnnounceMSG.toWorld(killer.getWorld(), CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " killed " + CC.tnPlayer + CC.fBold + victim.getName() + CC.tnInfo
								+ CC.fBold + " for a " + KillStreakType.toString(type) + CC.tnInfo + CC.fBold + "!");
						FireworkEffect effect = FireworkEffect.builder().with(Type.BALL).withColor(Color.RED).withTrail().build();
						UtilFirework.launchFirework(victim.getLocation(), 2, effect);
						Title doublet = new Title("", KillStreakType.toString(type), 10, 30, 10);
						doublet.send(killer);
						break;
					case TRIPLE:
						killstreak.put(killer.getName(), KillStreakType.QUADRA);
						c.setTime(12);
						c.runTaskTimerAsynchronously(main, 0L, 20L);
						killstreakcountdown.put(killer.getName(), c);
						AnnounceMSG.toWorld(killer.getWorld(), CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " killed " + CC.tnPlayer + CC.fBold + victim.getName() + CC.tnInfo
								+ CC.fBold + " for a " + KillStreakType.toString(type) + CC.tnInfo + CC.fBold + "!");
						FireworkEffect effect2 = FireworkEffect.builder().with(Type.BALL).withColor(Color.PURPLE).withTrail().build();
						UtilFirework.launchFirework(victim.getLocation(), 2, effect2);
						Title triplet = new Title("", KillStreakType.toString(type), 10, 30, 10);
						triplet.send(killer);
						break;
					case QUADRA:
						killstreak.put(killer.getName(), KillStreakType.PENTA);
						c.setTime(14);
						c.runTaskTimerAsynchronously(main, 0L, 20L);
						killstreakcountdown.put(killer.getName(), c);
						AnnounceMSG.toWorld(killer.getWorld(), CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " killed " + CC.tnPlayer + CC.fBold + victim.getName() + CC.tnInfo
								+ CC.fBold + " for a " + KillStreakType.toString(type) + CC.tnInfo + CC.fBold + "!");
						FireworkEffect effect3 = FireworkEffect.builder().with(Type.BALL).withColor(Color.BLUE).withTrail().build();
						UtilFirework.launchFirework(victim.getLocation(), 2, effect3);
						Title quadt = new Title("", KillStreakType.toString(type), 10, 30, 10);
						quadt.send(killer);
						break;
					case PENTA:
						AnnounceMSG.toWorld(killer.getWorld(), CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " killed " + CC.tnPlayer + CC.fBold + victim.getName() + CC.tnInfo
								+ CC.fBold + " for a " + KillStreakType.toString(type) + CC.tnInfo + CC.fBold + "!");
						killstreak.remove(killer.getName());
						killer.getWorld().playSound(killer.getLocation(), Sound.WITHER_SPAWN, 100F, 1F);
						FireworkEffect effect4 = FireworkEffect.builder().with(Type.BALL_LARGE).withColor(Color.YELLOW).withTrail().build();
						UtilFirework.launchFirework(victim.getLocation(), 2, effect4);
						Title pentat = new Title("", KillStreakType.toString(type), 10, 30, 10);
						pentat.send(killer);
						break;
				}
			}
			else {
				killstreak.put(killer.getName(), KillStreakType.DOUBLE);
				c.setTime(8);
				c.runTaskTimerAsynchronously(main, 0L, 20L);
				killstreakcountdown.put(killer.getName(), c);
			}
			if (killstreakcount.containsKey(killer.getName())) {
				int killstreak = killstreakcount.get(killer.getName());
				killstreak++;
				killstreakcount.put(killer.getName(), killstreak);
				if (killstreak == 15) {
					AnnounceMSG.toServer(CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " is on a rampage! Killstreak: " + CC.tnValue + CC.fBold + killstreak);
					killer.getWorld().playSound(killer.getLocation(), Sound.WITHER_SPAWN, 100F, 2F);
				}
				if (killstreak == 20) {
					AnnounceMSG.toServer(CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " is unstoppable! Killstreak: " + CC.tnValue + CC.fBold + killstreak);
					killer.getWorld().playSound(killer.getLocation(), Sound.WITHER_SPAWN, 100F, 1.8F);
				}
				if (killstreak == 25) {
					AnnounceMSG.toServer(CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " is godlike! Killstreak: " + CC.tnValue + CC.fBold + killstreak);
					killer.getWorld().playSound(killer.getLocation(), Sound.WITHER_SPAWN, 100F, 1.6F);
				}
				if (killstreak == 30) {
					AnnounceMSG.toServer(CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " is godlike! Killstreak: " + CC.tnValue + CC.fBold + killstreak);
					killer.getWorld().playSound(killer.getLocation(), Sound.WITHER_SPAWN, 100F, 1.4F);
				}
				if (killstreak == 40) {
					AnnounceMSG.toServer(CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " is godlike! Killstreak: " + CC.tnValue + CC.fBold + killstreak);
					killer.getWorld().playSound(killer.getLocation(), Sound.WITHER_SPAWN, 100F, 1.4F);
				}
				if (killstreak == 50) {
					AnnounceMSG.toServer(CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " is godlike! Killstreak: " + CC.tnValue + CC.fBold + killstreak);
					killer.getWorld().playSound(killer.getLocation(), Sound.WITHER_SPAWN, 100F, 1.4F);
				}
				if (killstreak == 60) {
					AnnounceMSG.toServer(CC.tnPlayer + CC.fBold + killer.getName() + CC.tnInfo + CC.fBold + " is godlike! Killstreak: " + CC.tnValue + CC.fBold + killstreak);
					killer.getWorld().playSound(killer.getLocation(), Sound.WITHER_SPAWN, 100F, 1.4F);
				}
			}
			else {
				killstreakcount.put(killer.getName(), 1);
			}
		}
		if (e.getKiller() != null) {
			Player killer = e.getKiller();
			UtilPlayer.sound(killer, playerSounds.KILL);
			if (UtilPlayer.multiplier(killer) > 0) {
				UtilPlayer.transaction(killer, 10 * UtilPlayer.multiplier(killer), "kill", new String[] { Rank.getRankPrefix(UtilPlayer.getRank(killer)) + CC.tnValue + " Rank" });
				EconomyManager.addTokens(killer, 10 * UtilPlayer.multiplier(killer));
			}
			else {
				UtilPlayer.transaction(killer, 10, "kill", null);
				EconomyManager.addTokens(killer, 10);
			}
			StatManager.addKills(killer, Gamemodes.KITPVE, 1);
			ScoreboardManager.updateKills(killer);
			ScoreboardManager.updateKD(killer);
		}
		for (Player assists : e.getAssists()) {
			UtilPlayer.sound(assists, playerSounds.KILL);
			if (UtilPlayer.multiplier(assists) > 0) {
				UtilPlayer.transaction(assists, 1 * UtilPlayer.multiplier(assists), "assisted kill", new String[] { Rank.getRankPrefix(UtilPlayer.getRank(assists)) + CC.tnValue + " Rank" });
				EconomyManager.addTokens(assists, 1 * UtilPlayer.multiplier(assists));
			}
			else {
				UtilPlayer.transaction(assists, 1, "assisted kill", null);
				EconomyManager.addTokens(assists, 1);
			}
		}
		if (e.getVictim() != null) {
			StatManager.addDeaths(e.getVictim(), Gamemodes.KITPVE, 1);
			ScoreboardManager.updateDeaths(e.getVictim());
			ScoreboardManager.updateKD(e.getVictim());
		}
	}

	@EventHandler
	public void onPVPEvent(PVPEvent e) {
		if ((e.getAttacker() != null) && (e.getPlayer() != null)) {
			if (PVEManager.stopping) {
				e.setCancelled(true);
				return;
			}
			if ((recall_Tasks.containsKey(e.getAttacker().getName())) || (recall_Tasks.containsKey(e.getPlayer().getName()))) {
				cancelRecall(e.getAttacker());
				cancelRecall(e.getPlayer());
			}
			if (PVEManager.getSpawnRegion() != null) {
				if ((PVEManager.getSpawnRegion().containsBlock(e.getPlayer().getLocation())) || (PVEManager.getSpawnRegion().containsBlock(e.getAttacker().getLocation()))) {
					e.setCancelled(true);
				}
				else {
					Player attacker = e.getAttacker();
					Player p = e.getPlayer();
					UtilPlayer.setinCombat(p, CC.cRED + CC.fBold + "You are now in combat mode! Do not logout or you will be banned for 2 hours!");
					UtilPlayer.setinCombat(attacker, CC.cRED + CC.fBold + "You are now in combat mode! Do not logout or you will be banned for 2 hours!");
				}
			}
			if ((!PVEManager.isPVPEnabled(e.getAttacker())) || (!PVEManager.isPVPEnabled(e.getPlayer()))) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onCombatLog(CombatLogEvent e) {
		Player logger = e.getPlayer();
		Punish.tempBanPlayer(logger.getName(), "SERVER", "CombatLog", "h:2");
	}

	@EventHandler
	public void onPlayerDamage(PlayerDamageEvent e) {
		Player p = e.getPlayer();
		cancelRecall(p);
		if (e.getCause() == DamageCause.SUICIDE) {
			return;
		}
		if ((PVEManager.getSpawnRegion() != null && PVEManager.getSpawnRegion().containsBlock(p.getLocation())) || (!PVEManager.isPVPEnabled(p))) {
			e.setCancelled(true);
		}
	}

}
