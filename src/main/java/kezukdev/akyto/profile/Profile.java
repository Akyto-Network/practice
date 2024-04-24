package kezukdev.akyto.profile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import kezukdev.akyto.Practice;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Profile {
	
	private ProfileState profileState;
	private List<int[]> stats;
	private List<Boolean> settings;
	private List<Boolean> spectateSettings;
	
	public Profile(final Practice main, final UUID uuid) {
		this.setProfileState(ProfileState.FREE);
		this.stats = Arrays.asList(new int[7], new int[7], new int[7]);
		for (int i = 0; i <= this.stats.get(2).length - 1; i++) this.stats.get(2)[i] = 1000;
		this.settings = Arrays.asList(true, true, true);
		this.spectateSettings = Arrays.asList(true, true);
		main.getManagerHandler().getProfileManager().getProfiles().put(uuid, this);
	}

}
