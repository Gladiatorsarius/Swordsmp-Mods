package armour.invis.swordsmp;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArmourInvisClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("armour-invis");
	@Override
	public void onInitializeClient() {
		// Client-side initialization: set up rendering hooks.
		LOGGER.info("Initializing Armourinvis");
	}
}