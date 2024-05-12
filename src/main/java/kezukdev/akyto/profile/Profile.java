package kezukdev.akyto.profile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.scoreboard.Team;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Profile {
	
	private ProfileState profileState;
	private List<int[]> stats;
	private List<Boolean> settings;
	private List<Boolean> spectateSettings;
	private Team team;
	
	public Profile(final UUID uuid) {
		this.profileState = ProfileState.FREE;
		this.stats = Arrays.asList(new int[7], new int[7], new int[7]);
		for (int i = 0; i <= this.stats.get(2).length - 1; i++) this.stats.get(2)[i] = 1000;
		this.settings = Arrays.asList(true, true, true);
		this.spectateSettings = Arrays.asList(true, true);
	}
	
	// Return whether the profile is in any of the given states
	public boolean isInState(ProfileState... states) {
		for (ProfileState state : states) {
			if (this.profileState.equals(state))
				return true;
		}
		return false;
	}
}
