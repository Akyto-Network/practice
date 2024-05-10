package kezukdev.akyto.request;

import java.util.UUID;

import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.kit.Kit;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Request {
	
	private UUID requester;
	private UUID receiver;
	private Kit kit;
	private Arena arena;
	
	public Request(final UUID requester, final UUID receiver, final Kit kit, final Arena arena, final RequestType type) {
		this.requester = requester;
		this.receiver = receiver;
	}

	public enum RequestType {
		DUEL,
		PARTY
	}
}
