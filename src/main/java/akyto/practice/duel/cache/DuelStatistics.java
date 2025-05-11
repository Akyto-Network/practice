package akyto.practice.duel.cache;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;

import java.util.List;

@Getter @Setter
public class DuelStatistics {
	
	private int hits = 0;
	private int combo = 0;
	private int longestHit = 0;
}
