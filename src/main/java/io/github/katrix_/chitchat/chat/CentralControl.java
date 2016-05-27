package io.github.katrix_.chitchat.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.data.DataQuery;

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

}
