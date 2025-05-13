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

	public void updateNextHitTick() {
		this.nextHitTick = System.currentTimeMillis() + 500L;
	}

}
