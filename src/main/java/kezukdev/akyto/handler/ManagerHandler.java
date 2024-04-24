package kezukdev.akyto.handler;

import kezukdev.akyto.Practice;
import kezukdev.akyto.handler.manager.ArenaManager;
import kezukdev.akyto.handler.manager.DuelManager;
import kezukdev.akyto.handler.manager.InventoryManager;
import kezukdev.akyto.handler.manager.ItemManager;
import kezukdev.akyto.handler.manager.LeaderboardManager;
import kezukdev.akyto.handler.manager.PartyManager;
import kezukdev.akyto.handler.manager.ProfileManager;
import kezukdev.akyto.handler.manager.QueueManager;
import kezukdev.akyto.handler.manager.RequestManager;
import lombok.Getter;

@Getter
public class ManagerHandler {

	private ProfileManager profileManager;
	private ItemManager itemManager;
	private LeaderboardManager leaderboardManager;
	private InventoryManager inventoryManager;
	private QueueManager queueManager;
	private DuelManager duelManager;
	private ArenaManager arenaManager;
	private PartyManager partyManager;
	private RequestManager requestManager;
	
	public ManagerHandler(final Practice main) {
		this.profileManager = new ProfileManager(main);
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
