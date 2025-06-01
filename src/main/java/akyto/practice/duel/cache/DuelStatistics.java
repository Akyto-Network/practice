package akyto.practice.duel.cache;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DuelStatistics {
	
	private int hits = 0;
	private int combo = 0;
	private int longestHit = 0;
	private Long nextHitTick = 0L;
	private boolean bedBroke = false;

	private int potMissed = 0;
	private int potThrown = 0;
	private double potHeal = 0;

	private int blockHit = 0;

	public void updateNextHitTick() {
		this.nextHitTick = System.currentTimeMillis() + 500L;
	}

}
