package net.mcreator.swordssmp.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of other entities' Invisibility amplifiers, filled by
 * {@link InvisibilityAmplifierSyncMessage}. Cleared when leaving a world.
 */
public final class InvisibilityAmplifierClientCache {
	private static final Map<Integer, Integer> AMPLIFIERS = new ConcurrentHashMap<>();

	private InvisibilityAmplifierClientCache() {
	}

	public static void set(int entityId, int amplifier) {
		if (amplifier < 0) {
			AMPLIFIERS.remove(entityId);
		} else {
			AMPLIFIERS.put(entityId, amplifier);
		}
	}

	public static int get(int entityId) {
		return AMPLIFIERS.getOrDefault(entityId, -1);
	}

	public static void clear() {
		AMPLIFIERS.clear();
	}
}
