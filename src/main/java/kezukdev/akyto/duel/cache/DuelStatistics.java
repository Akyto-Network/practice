package kezukdev.akyto.duel.cache;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DuelStatistics {
	
	private int hits = 0;
	private int combo = 0;
	private int longestHit = 0;
	private long enderpearlCooldown = 0L;
	
	public boolean isEnderPearlCooldownActive() {
		return this.enderpearlCooldown > System.currentTimeMillis();
	}
	
	public long getEnderPearlCooldown() {
		return Math.max(0L, this.enderpearlCooldown - System.currentTimeMillis());
	}

	public void applyEnderPearlCooldown() {
		this.enderpearlCooldown = Long.valueOf(System.currentTimeMillis() + 16 * 1000);
	}

	public void removeEnderPearlCooldown() {
		this.enderpearlCooldown = 0L;
	}
}
