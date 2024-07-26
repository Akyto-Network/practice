package akyto.practice.duel.cache;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DuelStatistics {
	
	private int hits = 0;
	private int combo = 0;
	private int longestHit = 0;
	private long pearlCooldown = 0L;
	
	public boolean hasPearlCooldown() {
		return this.pearlCooldown > System.currentTimeMillis();
	}
	
	public long getEnderPearlCooldown() {
		return Math.max(0L, this.pearlCooldown - System.currentTimeMillis());
	}

	public void applyEnderPearlCooldown() {
		this.pearlCooldown = System.currentTimeMillis() + 16L * 1000L;
	}

	public void removeEnderPearlCooldown() {
		this.pearlCooldown = 0L;
	}
}
