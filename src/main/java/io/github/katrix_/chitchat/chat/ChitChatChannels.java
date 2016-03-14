/**
 * This file is part of ChitChat, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.katrix_.chitchat.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;
import io.github.katrix_.chitchat.io.ConfigSettings;

public class ChitChatChannels {

	private static final String GLOBAL_NAME = "Global";
	private static final ChannelChitChat GLOBAL = new ChannelChitChat(GLOBAL_NAME, Text.of("The global channel"),
			ConfigSettings.getGlobalChannelPrefix());
	private static final Map<Player, ChannelChitChat> CHANNEL_PLAYER_MAP = new WeakHashMap<>();
	private static final Map<String, ChannelChitChat> CHANNELS = new HashMap<>();

	public static void initChannels() {
		Sponge.getEventManager().registerListeners(ChitChat.getPlugin(), new ChatListener());
		addChannel(GLOBAL);
	}

	public static void setChannelForPlayer(Player player, String channelName) {
		setChannelForPlayer(player, getChannel(channelName));
	}

	public static void setChannelForPlayer(Player player, ChannelChitChat channel) {
		if(CHANNEL_PLAYER_MAP.containsKey(player)) {
			getChannelForPlayer(player).removeMember(player);
		}
		CHANNEL_PLAYER_MAP.put(player, channel);
		channel.addMember(player);
	}

	public static ChannelChitChat getChannelForPlayer(Player player) {
		return CHANNEL_PLAYER_MAP.get(player);
	}

	public static void addChannel(ChannelChitChat channel) {
		LogHelper.info("Creating channel " + channel.getName());
		CHANNELS.put(channel.getName(), channel);
	}

	public static void removeChannel(String name) {
		removeChannel(getChannel(name));
	}

	public static void removeChannel(ChannelChitChat channel) {
		if(channel.equals(GLOBAL)) return;

		LogHelper.info("Removing channel " + channel.getName());
		Map<Player, ChannelChitChat> map = getChannelPlayerMap();
		map.keySet().stream().filter(player -> map.get(player).equals(channel)).forEach(player -> {
			player.sendMessage(Text.of(TextColors.RED, "You are being moved to the global channel as the channel you are in is being removed."));
			setChannelForPlayer(player, GLOBAL);
		});
		channel.clearMembers();

		CHANNELS.remove(channel.getName());
	}

	public static boolean doesChannelExist(String name) {
		return CHANNELS.containsKey(name);
	}

	public static ChannelChitChat getChannel(String name) {
		return CHANNELS.get(name);
	}

	public static void remapChannelName(ChannelChitChat channel, String oldName) {
		LogHelper.info("Remmaping channel " + oldName + " to " + channel.getName());
		CHANNELS.remove(oldName);
		CHANNELS.put(channel.getName(), channel);
	}

	public static ImmutableMap<String, ChannelChitChat> getChannelMap() {
		return ImmutableMap.copyOf(CHANNELS);
	}

	public static ImmutableMap<Player, ChannelChitChat> getChannelPlayerMap() {
		return ImmutableMap.copyOf(CHANNEL_PLAYER_MAP);
	}

	public static void clearChannelMap() {
		for(ChannelChitChat channel : CHANNELS.values()) {
			removeChannel(channel);
		}
	}

	public static void moveAllPlayersToGlobal() {
		for(Player player : CHANNEL_PLAYER_MAP.keySet()) {
			setChannelForPlayer(player, GLOBAL);
		}
	}

	public static ChannelChitChat getGlobalChannel() {
		return GLOBAL;
	}
}
