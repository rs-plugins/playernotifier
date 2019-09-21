import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Hero Notifier",
        description = "Plays a sound when a player comes too close.",
        enabledByDefault = false,
        type = PluginType.EXTERNAL
)
public class PlayerNotifierPlugin extends Plugin {
    @Inject
    private EventBus eventBus;

    @Inject
    private Client client;

    private static final Logger logger = LoggerFactory.getLogger(PlayerNotifierPlugin.class);

    @Override
    protected void startUp() throws Exception {
        logger.info("PlayerNotifierPlugin started!");

        eventBus.subscribe(PlayerSpawned.class, this, this::onPlayerSpawned);
        eventBus.subscribe(PlayerDespawned.class, this, this::onPlayerDespawned);
    }

    @Override
    protected void shutDown() throws Exception {
        logger.info("PlayerNotifierPlugin shutDown!");
        eventBus.unregister(this);
    }

    private void onPlayerSpawned(PlayerSpawned event) {
        final Player local = client.getLocalPlayer();
        final Player player = event.getPlayer();

        // If the player is not us, is not in our clan and is not a friend,
        // sound the alarm
        if (isEnemy(local, player)) {
            logger.info("Player {} spotted", player.getName());
        }
    }

    private void onPlayerDespawned(PlayerDespawned event) {
        final Player player = event.getPlayer();

        logger.info("Player {} despawned", player.getName());
    }

    private boolean isEnemy(Player local, Player player) {
        return player != local && !player.isClanMember() && !player.isFriend();
    }
}
