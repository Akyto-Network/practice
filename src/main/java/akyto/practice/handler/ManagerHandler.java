package akyto.practice.handler;

import akyto.practice.Practice;
import akyto.practice.handler.manager.*;
import akyto.practice.handler.manager.TimerManager;
import akyto.practice.utils.timer.impl.EnderpearlTimer;
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
	private final EventManager eventManager;
	private final TimerManager timerManager;
	
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
		this.eventManager = new EventManager(main);
		this.timerManager = new TimerManager(main);
		if (this.timerManager.getTimer(EnderpearlTimer.class) == null) {
			this.timerManager.registerTimer(new EnderpearlTimer());
		}
	}
}
