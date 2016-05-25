package io.github.katrix_.chitchat.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;

public class CentralControl {

	public static final CentralControl INSTANCE = new CentralControl();

	private final Map<DataQuery, ChannelChitChat> channelMap = new HashMap<>();
	private final Map<Player, UserChitChat> userMap = new WeakHashMap<>();

	private CentralControl() {}

	public Optional<ChannelChitChat> getChannel(DataQuery fullName) {
		return Optional.ofNullable(channelMap.get(fullName));
	}

	public void registerChannel(ChannelChitChat channel) {
		channelMap.put(channel.getQueryName(), channel);
	}

	public UserChitChat getOrCreateUser(Player player) {
		UserChitChat user = userMap.get(player);

		if(user == null) {
			user = new UserChitChat(player);
			userMap.put(player, user);
		}

		return user;
	}

	public UserChitChat getUserUuid(UUID uuid) {

		return null; //TODO
	}
}
