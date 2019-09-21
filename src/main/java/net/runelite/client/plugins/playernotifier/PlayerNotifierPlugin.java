package net.runelite.client.plugins.playernotifier;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.Sound;
import net.runelite.client.game.SoundManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


@PluginDescriptor(
        name = "Player Notifier",
        description = "Plays a sound when a player comes too close.",
        tags = {"players"},
        enabledByDefault = false,
        type = PluginType.EXTERNAL
)
public class PlayerNotifierPlugin extends Plugin {
    private List<Player> currentPlayers = new ArrayList<>();
    private Boolean alarmPlaying;

    @Inject
    private EventBus eventBus;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private Client client;

    @Inject
    private SoundManager soundManager;

    private static final Logger logger = LoggerFactory.getLogger(PlayerNotifierPlugin.class);

    @Override
    protected void startUp() throws Exception {
        eventBus.subscribe(PlayerSpawned.class, this, this::onPlayerSpawned);
        eventBus.subscribe(PlayerDespawned.class, this, this::onPlayerDespawned);
    }

    @Override
    protected void shutDown() throws Exception {
        eventBus.unregister(this);
    }

    private void onPlayerSpawned(PlayerSpawned event) {
        final Player local = client.getLocalPlayer();
        final Player player = event.getPlayer();
        logger.info("Player {} spotted, not in wilderness", player.getName());

        if (!isInWlderness()) {
            return;
        }

        // If the player is not us, is not in our clan and is not a friend,
        // sound the alarm
        if (isEnemy(local, player)) {
            logger.info("Player {} shown, playing RUNAWAY", player.getName());

            // Add to list
            if (!currentPlayers.contains(player)) {
                currentPlayers.add(player);
                soundManager.playSound(Sound.RUNAWAY);
            }
        }
    }

    private boolean isInWlderness() {
        return client.getVar(Varbits.IN_WILDERNESS) == 1 || WorldType.isPvpWorld(client.getWorldType());
    }

    private void onPlayerDespawned(PlayerDespawned event) {
        final Player player = event.getPlayer();

        // If the player is not us, is not in our clan and is not a friend,
        // sound the alarm
        if (currentPlayers.contains(player)) {
            logger.info("Player {} despawned", player.getName());

            // Remove from list
            currentPlayers.remove(player);
        }
    }

    private boolean isEnemy(Player local, Player player) {
        return player != local && !player.isClanMember() && !player.isFriend();
    }
}
