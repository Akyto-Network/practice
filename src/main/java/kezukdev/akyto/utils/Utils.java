package kezukdev.akyto.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.handler.manager.PartyManager;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import kezukdev.akyto.utils.chat.ComponentJoiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Utils {
	
	private final Practice main;
	
	public Utils(final Practice main) { this.main = main; }
	
	public ItemStack createItem(final Material material, final int amount, final byte id, final String displayName) {
		final ItemStack item = new ItemStack(material, amount, id);
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		item.setItemMeta(meta);
		return item;
	}
	
	public ItemStack createItem(final Material material, final String displayName, final List<String> lore) {
		final ItemStack item = new ItemStack(material);
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		meta.spigot().setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}
	
	public void resetPlayer(final UUID uuid) {
		if (Bukkit.getPlayer(uuid) == null) return;
		final Player player = Bukkit.getPlayer(uuid);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setFireTicks(0);
		player.setExhaustion(0.3f);
		player.setFoodLevel(20);
		player.setHealth(player.getMaxHealth());
		player.setLevel(0);
		player.setExp(0);
		player.setWalkSpeed(0.2f);
		player.setSneaking(false);
		player.setSaturation(20.0f);
		player.resetPlayerWeather();
		player.closeInventory();
		player.setGameMode(GameMode.SURVIVAL);
		player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
	}
	
	public void multiArena(final UUID uuid, final boolean display) {
	    if (!display) {
	    	Bukkit.getOnlinePlayers().forEach(player -> {
	    		Bukkit.getPlayer(uuid).hidePlayer(player);
	    		player.hidePlayer(Bukkit.getPlayer(uuid));
	    	});
	    	final Duel duel = this.getDuelByUUID(uuid);
	    	duel.getFirst().forEach(first -> {
	    		if (Bukkit.getPlayer(first) == null) return;
	    		Bukkit.getPlayer(first).showPlayer(Bukkit.getPlayer(uuid));
	    		Bukkit.getPlayer(uuid).showPlayer(Bukkit.getPlayer(first));
	    	});
	    	duel.getSecond().forEach(second -> {
	    		if (Bukkit.getPlayer(second) == null) return;
	    		Bukkit.getPlayer(second).showPlayer(Bukkit.getPlayer(uuid));
	    		Bukkit.getPlayer(uuid).showPlayer(Bukkit.getPlayer(second));
	    	});
	    }
	}



	public List<UUID> getOpponents(UUID uuid) {
        Duel duel = getDuelByUUID(uuid);
        return duel.getFirst().contains(uuid) ? new ArrayList<>(duel.getSecond()) : new ArrayList<>(duel.getFirst());
	}

	public Duel getDuelByUUID(UUID uuid) {
	    return main.getDuels().stream().filter(duel -> duel.getFirst().contains(uuid) || duel.getSecond().contains(uuid)).findFirst().orElse(null);
	}
	
	public Duel getDuelBySpectator(UUID uuid) {
	    return main.getDuels().stream().filter(duel -> duel.getSpectator().contains(uuid)).findFirst().orElse(null);
	}
	
	public PartyManager.PartyEntry getPartyByUUID(UUID uuid) {
	    return main.getManagerHandler().getPartyManager().getPartys().stream().filter(party -> party.getMembers().contains(uuid)).findFirst().orElse(null);
	}
	
	public Profile getProfiles(final UUID uuid) {
		return this.main.getManagerHandler().getProfileManager().getProfiles().get(uuid);
	}
	
	public void addPlayedToData(final UUID uuid, final Kit kit) {
		this.getProfiles(uuid).getStats().get(0)[kit.id()] = this.getProfiles(uuid).getStats().get(0)[kit.id()]+1;
	}
	
	public void addWinToData(final UUID uuid, final Kit kit) { 
		this.getProfiles(uuid).getStats().get(1)[kit.id()] = this.getProfiles(uuid).getStats().get(1)[kit.id()]+1; 
	}
	
	public void resetData(final UUID uuid) {
		// RESET DATA HERE //
	}
	
	public void sendToSpawn(final UUID uuid, final boolean teleport) {
		if (Bukkit.getPlayer(uuid) == null) return;
		Bukkit.getOnlinePlayers().forEach(player -> {
			player.showPlayer(Bukkit.getPlayer(uuid));
			Bukkit.getPlayer(uuid).showPlayer(player);
		});
		this.resetPlayer(uuid);
		this.getProfiles(uuid).setProfileState(ProfileState.FREE);
		this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
		if (teleport) {
			Bukkit.getPlayer(uuid).teleport(this.main.getSpawn().getLocation());
		}
	}
	
	public void sendToEditor(final UUID uuid, final Kit kit) {
		if (Bukkit.getPlayer(uuid) == null) return;
		Bukkit.getOnlinePlayers().forEach(player -> {
			player.hidePlayer(Bukkit.getPlayer(uuid));
			Bukkit.getPlayer(uuid).hidePlayer(player);
		});
		this.resetPlayer(uuid);
		this.getProfiles(uuid).setProfileState(ProfileState.EDITOR);
		this.main.getManagerHandler().getProfileManager().getEditing().put(uuid, kit.name());
		this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
		Bukkit.getPlayer(uuid).teleport(this.main.getEditor().getLocation());
	}
	
    public void startDuration(final Duel matchEntry) {
        matchEntry.getTimer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDuration(matchEntry);
            }
        }, 1000, 1000);
    }
    
    private void updateDuration(final Duel matchEntry) {
        long currentTime = System.currentTimeMillis();
        matchEntry.duration = currentTime - matchEntry.getStartTime();
    }
	
	public void addDrops(Item item, final UUID uuid) {
		this.getDuelByUUID(uuid).getDropped().add(item.getUniqueId());	
	}
	
	public void removeDrops(Item item, final UUID uuid) {
		this.getDuelByUUID(uuid).getDropped().remove(item.getUniqueId());	
	}
	
	public boolean containDrops(Item item, final UUID uuid) {
		return this.getDuelByUUID(uuid).getDropped().contains(item.getUniqueId());
	}
	
	public void clearDrops(final UUID uuid) {
		if ((this.getDuelByUUID(uuid) != null && this.getDuelByUUID(uuid).getDropped().isEmpty())) return;
		final World world = Bukkit.getWorld("world");
		for (Entity entities : world.getEntities()) {
			if (entities == null || !(entities instanceof Item) && ((this.getDuelByUUID(uuid) != null && !this.getDuelByUUID(uuid).getDropped().contains(entities.getUniqueId())))) continue;
			entities.remove();
		}
	}
	
	public int[] getSplitValue(final String string, final String spliter) {
        final String[] split = string.split(spliter);
        final int[] board = new int[split.length];
        for (int i = 0; i <= split.length - 1; ++i) {
            board[i] = Integer.parseInt(split[i]);
        }
        return board;
    }
	
	public String getStringValue(final int[] board, final String spliter) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i <= board.length - 1; ++i) {
            stringBuilder.append(board[i]);
            if (i != board.length - 1) {
                stringBuilder.append(spliter);
            }
        }
        return stringBuilder.toString();
    }
	
	public String formatTime(final long cooldown, final double dividend) {
		final double time = cooldown / dividend;
		final DecimalFormat df = new DecimalFormat("#.#");
		return df.format(time);
	}
	
	public TextComponent endMessage(final UUID winner, final UUID looser) {
		TextComponent winnerComponent = new TextComponent("Winner: ");
		winnerComponent.setColor(ChatColor.GREEN);
		TextComponent winnerNameComponent = new TextComponent(Bukkit.getOfflinePlayer(winner).getName());
		winnerNameComponent.setColor(ChatColor.GRAY);
		winnerNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + (Bukkit.getPlayer(winner) != null ? Bukkit.getPlayer(winner).getName() : Bukkit.getOfflinePlayer(winner).getName())));
		TextComponent loserComponent = new TextComponent("Loser: ");
		loserComponent.setColor(ChatColor.RED);
		TextComponent loserNameComponent = new TextComponent(Bukkit.getOfflinePlayer(looser).getName());
		loserNameComponent.setColor(ChatColor.GRAY);
		loserNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + (Bukkit.getPlayer(looser) != null ? Bukkit.getPlayer(looser).getName() : Bukkit.getOfflinePlayer(looser).getName())));
		TextComponent separatorComponent = new TextComponent(ChatColor.GRAY + " - ");
		winnerComponent.addExtra(winnerNameComponent);
		loserComponent.addExtra(loserNameComponent);
		winnerComponent.addExtra(separatorComponent);
		winnerComponent.addExtra(loserComponent);
		return winnerComponent;
	}
	
	public void sendSplitMessage(final List<UUID> first, final List<UUID> second, final Kit ladder) {
	    final ComponentJoiner joinerOne = new ComponentJoiner(ChatColor.GRAY + ", ");
	    final ComponentJoiner joinerTwo = new ComponentJoiner(ChatColor.GRAY + ", ");
        final TextComponent firsttxt = new TextComponent(ChatColor.DARK_GRAY + Bukkit.getPlayer(first.get(0)).getName() + "'s teams" + ChatColor.GRAY + ": " + ChatColor.RED);
        final TextComponent secondtxt = new TextComponent(ChatColor.DARK_GRAY + Bukkit.getPlayer(second.get(0)).getName() + "'s teams" + ChatColor.GRAY + ": " + ChatColor.RED);
	    first.forEach(uuid -> {
	    	firsttxt.addExtra(Bukkit.getPlayer(uuid).getName());
	        firsttxt.setColor(ChatColor.RED);
	        firsttxt.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD.toString() + this.main.getManagerHandler().getProfileManager().getProfiles().get(uuid).getStats().get(2)[ladder.id()] + " elos").create()));
	        joinerOne.add(firsttxt);
	    });
	    second.forEach(uuid -> {
	    	secondtxt.addExtra(Bukkit.getPlayer(uuid).getName());
	        secondtxt.setColor(ChatColor.RED);
	        secondtxt.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD.toString() + this.main.getManagerHandler().getProfileManager().getProfiles().get(uuid).getStats().get(2)[ladder.id()] + " elos").create()));
	        joinerTwo.add(secondtxt);
	    });
	    final List<List<UUID>> list = Arrays.asList(first, second);
	    list.forEach(uuids -> {
	    	for (UUID uuid : uuids) {
	    		Bukkit.getPlayer(uuid).spigot().sendMessage(firsttxt);
	    		Bukkit.getPlayer(uuid).spigot().sendMessage(secondtxt);
	    	}
	    });
	}
	
	public void sendPartyComponent(final List<List<UUID>> players) {
		final Duel duel = this.getDuelByUUID(players.get(0).get(0));
		if (duel.getDuelType().equals(DuelType.FFA)) {
			TextComponent invComponent = new TextComponent(ChatColor.YELLOW + "Inventorie(s)" + ChatColor.GRAY + ": ");
            final ComponentJoiner joiner = new ComponentJoiner(ChatColor.GRAY + ", ");
            players.forEach(uuid -> {
                for (UUID uuids : uuid) {
                    final TextComponent itxt = new TextComponent(Bukkit.getPlayer(uuids) == null ? Bukkit.getOfflinePlayer(uuids).getName() : Bukkit.getPlayer(uuids).getName());
                    itxt.setColor(ChatColor.WHITE);
                    itxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to view " + (Bukkit.getPlayer(uuids) == null ? Bukkit.getOfflinePlayer(uuids).getName() : Bukkit.getPlayer(uuids).getName()) + "'s inventory").create()));
                    itxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + (Bukkit.getPlayer(uuids) == null ? Bukkit.getOfflinePlayer(uuids).getName() : Bukkit.getPlayer(uuids).getName())));
                    joiner.add(itxt);

                }
            });
            invComponent.addExtra(joiner.toTextComponent());
            players.forEach(uuid -> {
                for (UUID uuids : uuid) {
                    if (Bukkit.getPlayer(uuids) != null) {
                    	Bukkit.getPlayer(uuids).sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
        				Bukkit.getPlayer(uuids).sendMessage(ChatColor.YELLOW + "Match Information");
        				Bukkit.getPlayer(uuids).sendMessage(ChatColor.GRAY + "Winner: " + ChatColor.GOLD + (Bukkit.getPlayer(this.main.getUtils().getDuelByUUID(uuids).getWinner().get(0)) != null ? Bukkit.getPlayer(this.main.getUtils().getDuelByUUID(uuids).getWinner().get(0)).getName() : Bukkit.getOfflinePlayer(this.main.getUtils().getDuelByUUID(uuids).getWinner().get(0)).getName()));
        				Bukkit.getPlayer(uuids).sendMessage(" ");
        				Bukkit.getPlayer(uuids).spigot().sendMessage(invComponent);
        				Bukkit.getPlayer(uuids).sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
                    }
                }
            });
            return;
		}
		if (duel.getDuelType().equals(DuelType.SPLIT)) {
            TextComponent winnerComponent = new TextComponent(ChatColor.GRAY + "Winner(s)" + ChatColor.GRAY + ": ");
            TextComponent loserComponent = new TextComponent(ChatColor.GRAY + "Looser(s)" + ChatColor.GRAY + ": ");
            final ComponentJoiner joinerWin = new ComponentJoiner(ChatColor.GRAY + ", ");
            final ComponentJoiner joinerLose = new ComponentJoiner(ChatColor.GRAY + ", ");
            duel.getWinner().forEach(uuid -> {
                final TextComponent wtxt = new TextComponent(Bukkit.getPlayer(uuid) == null ? Bukkit.getOfflinePlayer(uuid).getName() : Bukkit.getPlayer(uuid).getName());
                wtxt.setColor(ChatColor.GREEN);
                wtxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to view " + (Bukkit.getPlayer(uuid) == null ? Bukkit.getOfflinePlayer(uuid).getName() : Bukkit.getPlayer(uuid).getName()) + "'s inventory").create()));
                wtxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + (Bukkit.getPlayer(uuid) == null ? Bukkit.getOfflinePlayer(uuid).getName() : Bukkit.getPlayer(uuid).getName())));
                joinerWin.add(wtxt);
            });

            List<UUID> losers = duel.getFirst().containsAll(duel.getWinner()) ? new ArrayList<>(duel.getSecond()) : new ArrayList<>(duel.getFirst());

            losers.forEach(uuid -> {
                final TextComponent ltxt = new TextComponent(Bukkit.getPlayer(uuid) == null ? Bukkit.getOfflinePlayer(uuid).getName() : Bukkit.getPlayer(uuid).getName());
                ltxt.setColor(ChatColor.RED);
                ltxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to view " + (Bukkit.getPlayer(uuid) == null ? Bukkit.getOfflinePlayer(uuid).getName() : Bukkit.getPlayer(uuid).getName()) + "'s inventory").create()));
                ltxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + (Bukkit.getPlayer(uuid) == null ? Bukkit.getOfflinePlayer(uuid).getName() : Bukkit.getPlayer(uuid).getName())));
				joinerLose.add(ltxt);
            });

            winnerComponent.addExtra(joinerWin.toTextComponent());
            loserComponent.addExtra(joinerLose.toTextComponent());

            players.forEach(uuidList -> uuidList.forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
                    player.spigot().sendMessage(winnerComponent);
                    player.spigot().sendMessage(loserComponent);
                    player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
                }
            }));
		}
	}
	
    public void addKill(final UUID uuid, final UUID killer) {
        final Duel match = this.getDuelByUUID(uuid);
        if (match == null) return;
        this.main.getManagerHandler().getInventoryManager().generatePreviewInventory(uuid, killer);
        if (match.getDuelType().equals(DuelType.FFA)) {
        	match.getAlives().remove(uuid);
            if (match.getAlives().size() == 1) {
            	if (killer != null) {
            		this.main.getManagerHandler().getInventoryManager().generatePreviewInventory(killer, uuid);
            	}
                this.main.getManagerHandler().getDuelManager().endMultiple(killer);
                return;
            }

            List<UUID> allPlayers = Stream.of(match.getFirst(), match.getSecond())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            allPlayers.forEach(uuids -> {
                Player player = Bukkit.getPlayer(uuids);
                if (player != null) {
                    player.sendMessage(ChatColor.WHITE + Bukkit.getPlayer(uuid).getName() +  ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + Bukkit.getPlayer(killer).getName()));
                }
            });
            
            match.getSpectator().forEach(uuids -> {
                Player spectator = Bukkit.getPlayer(uuids);
                if (spectator != null) {
                    spectator.sendMessage(ChatColor.WHITE + Bukkit.getPlayer(uuid).getName() +  ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + Bukkit.getPlayer(killer).getName()));
                }
            });
            if (Bukkit.getPlayer(uuid) != null) {
                this.addSpectateParty(uuid);
            }
        }

        if (match.getDuelType().equals(DuelType.SPLIT)) {
            match.getFirstAlives().remove(uuid);
            match.getSecondAlives().remove(uuid);
            if (match.getFirstAlives().isEmpty() || match.getSecondAlives().isEmpty()) {
                this.main.getManagerHandler().getDuelManager().endMultiple(killer);
                return;
            }

			List<UUID> allPlayers = Stream.of(match.getFirst(), match.getSecond())
					.flatMap(Collection::stream)
					.collect(Collectors.toList());

			allPlayers.forEach(uuids -> {
				Player player = Bukkit.getPlayer(uuids);
				if (player != null) {
					int aliveSize = match.getFirstAlives().contains(uuid) ? match.getFirstAlives().size() : match.getSecondAlives().size();
					int totalSize = match.getFirst().contains(uuid) ? match.getSecond().size() : match.getFirst().size();
					player.sendMessage(ChatColor.WHITE + Bukkit.getPlayer(uuid).getName() + ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + Bukkit.getPlayer(killer).getName() +ChatColor.GRAY + " (" + ChatColor.GREEN + aliveSize + ChatColor.GRAY + "/" + ChatColor.RED + totalSize + ChatColor.GRAY + ")"));
				}
			});

			match.getSpectator().forEach(uuids -> {
				Player spectator = Bukkit.getPlayer(uuids);
				if (spectator != null) {
					int aliveSize = match.getFirstAlives().contains(uuid) ? match.getFirstAlives().size() : match.getSecondAlives().size();
					int totalSize = match.getFirst().contains(uuid) ? match.getSecond().size() : match.getFirst().size();
					spectator.sendMessage(ChatColor.WHITE + Bukkit.getPlayer(uuid).getName() + ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + Bukkit.getPlayer(killer).getName() +ChatColor.GRAY + " (" + ChatColor.GREEN + aliveSize + ChatColor.GRAY + "/" + ChatColor.RED + totalSize + ChatColor.GRAY + ")"));
				}
			});
			if (Bukkit.getPlayer(uuid) != null)
				this.addSpectateParty(uuid);
        }
    }
    
    private void addSpectateParty(final UUID uuid) {
    	final Duel duel = this.getDuelByUUID(uuid);
    	if (duel.getDuelType().equals(DuelType.FFA)) {
    		duel.getAlives().forEach(alives -> {
    			Bukkit.getPlayer(alives).hidePlayer(Bukkit.getPlayer(uuid));
    			this.getProfiles(uuid).setProfileState(ProfileState.SPECTATE);
    			this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
    		});
    	}
    	if (duel.getDuelType().equals(DuelType.SPLIT)) {
    		Arrays.asList(duel.getFirstAlives(), duel.getSecondAlives()).forEach(alivesArray -> alivesArray.forEach(alives -> {
                Bukkit.getPlayer(alives).hidePlayer(Bukkit.getPlayer(uuid));
                this.getProfiles(uuid).setProfileState(ProfileState.SPECTATE);
                this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
            }));
    	}
		final Profile profile = this.getProfiles(uuid);
		if (!duel.getSpectator().isEmpty()) {
			duel.getSpectator().forEach(spectator -> {
				if (profile.getSpectateSettings().get(0)) Bukkit.getPlayer(uuid).showPlayer(Bukkit.getPlayer(spectator));
				if (!profile.getSpectateSettings().get(0)) Bukkit.getPlayer(uuid).hidePlayer(Bukkit.getPlayer(spectator));
			});
		}
		if (profile.getSpectateSettings().get(1)) Bukkit.getPlayer(uuid).setFlySpeed(0.1f);
		if (!profile.getSpectateSettings().get(1)) Bukkit.getPlayer(uuid).setFlySpeed(0.25f);
    }
	
    public String formatTime(int time) {
        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        int hours = ((time / 60) / 60) % 24;
        int days = (((time / 60) / 60) / 24);
        String format;
        if(days > 0) {
            format = days + ":" + (hours >= 10 ? hours : "0" + hours) + ":" + (minutes >= 10 ? minutes : "0" + minutes) + ":" + (seconds >= 10 ? seconds : "0" + seconds);
            return format;
        }
        if(hours > 0) {
            format = hours + ":" + (minutes >= 10 ? minutes : "0" + minutes) + ":" + (seconds >= 10 ? seconds : "0" + seconds);
            return format;
        }
        if(minutes > 0) {
            format = minutes + ":" + (seconds >= 10 ? seconds : "0" + seconds);
            return format;
        }
        format = seconds + "s";
        return format;
    }

    public int getSeconds(int seconds) {
        return seconds;
    }

    public int getSeconds(int minutes, int seconds) {
        return (minutes * 60) + seconds;
    }

    public int getSeconds(int hours, int minutes, int seconds) {
        return ((hours * 60) * 60) + (minutes * 60) + seconds;
    }

    public int getSeconds(int days, int hours, int minutes, int seconds) {
        return (((days * 24) * 60) * 60) + ((hours * 60) * 60) + (minutes * 60) + seconds;
    }
}
