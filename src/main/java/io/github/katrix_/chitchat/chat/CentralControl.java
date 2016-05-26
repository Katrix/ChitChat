package io.github.katrix_.chitchat.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import io.github.katrix_.chitchat.lib.LibDataQuaries;

public class CentralControl {

	public static final CentralControl INSTANCE = new CentralControl();

	private final Map<DataQuery, ChannelChitChat> channelMap = new HashMap<>();

	private CentralControl() {}

	public Optional<ChannelChitChat> getChannel(DataQuery fullName) {
		return Optional.ofNullable(channelMap.get(fullName));
	}

	public void registerChannel(ChannelChitChat channel) {
		channelMap.put(channel.getQueryName(), channel);
	}

	/*============================== Utility stuff ==============================*/

	public static Optional<ChannelChitChat> getChannelUser(User user) {
		Optional<DataQuery> query = user.toContainer().getObject(LibDataQuaries.PLAYER_CHANNEL, DataQuery.class);
		return query.flatMap(CentralControl.INSTANCE::getChannel);
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public static void setChannelUser(User user, ChannelChitChat channel) {
		boolean online = user.isOnline();

		if(online) {
			getChannelUser(user).ifPresent(c -> c.removeMember(user.getPlayer().get()));
		}
		user.toContainer().set(LibDataQuaries.PLAYER_CHANNEL, channel.getQueryName());

		if(online) {
			Player player = user.getPlayer().get();
			player.setMessageChannel(channel);
			channel.addMember(player);
		}
	}
}
