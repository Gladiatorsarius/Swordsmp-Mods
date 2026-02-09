package armour.invis.swordsmp;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArmourInvisClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Client-side initialization: set up rendering hooks.
		Logger logger = LoggerFactory.getLogger("armour-invis");
		logger.info("Initializing Armourinvis");
	}
}