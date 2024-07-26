package akyto.practice.handler;

import akyto.practice.Practice;
import akyto.practice.handler.manager.ArenaManager;
import akyto.practice.handler.manager.DuelManager;
import akyto.practice.handler.manager.InventoryManager;
import akyto.practice.handler.manager.ItemManager;
import akyto.practice.handler.manager.LeaderboardManager;
import akyto.practice.handler.manager.PartyManager;
import akyto.practice.handler.manager.ProfileManager;
import akyto.practice.handler.manager.QueueManager;
import akyto.practice.handler.manager.RequestManager;
import lombok.Getter;

@Getter
public class ManagerHandler {

	private final ProfileManager profileManager;
	private final ItemManager itemManager;
	private final LeaderboardManager leaderboardManager;
	private final InventoryManager inventoryManager;
	private final QueueManager queueManager;
	private final DuelManager duelManager;
	private final ArenaManager arenaManager;
	private final PartyManager partyManager;
	private final RequestManager requestManager;
	
	public ManagerHandler(final Practice main) {
		this.profileManager = new ProfileManager();
		this.itemManager = new ItemManager(main);
		this.queueManager = new QueueManager(main);
		this.duelManager = new DuelManager(main);
		this.arenaManager = new ArenaManager(main);
		this.leaderboardManager = new LeaderboardManager(main);
		this.inventoryManager = new InventoryManager(main);
		this.requestManager = new RequestManager(main);
		this.partyManager = new PartyManager(main);
	}
}
