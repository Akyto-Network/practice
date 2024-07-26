package akyto.practice.request;

import java.util.UUID;

import akyto.practice.arena.Arena;
import akyto.practice.kit.Kit;
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
