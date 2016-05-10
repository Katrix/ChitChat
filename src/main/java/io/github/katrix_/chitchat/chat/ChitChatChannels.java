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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;

public class ChitChatChannels {

	private static final String GLOBAL_NAME = "Global";
	private static final ChannelChitChat GLOBAL = new ChannelChitChat(GLOBAL_NAME, Text.of("The global channel"),
			ChitChat.getConfig().getGlobalChannelPrefix());
	private static final Map<String, ChannelChitChat> CHANNELS = new HashMap<>();

	public static void init() {
		Sponge.getEventManager().registerListeners(ChitChat.getPlugin(), new ChatListener());
		add(GLOBAL);
		ChitChat.getStorage().reloadChannels(); //Hacky yeah, but it works
	}

	public static void add(ChannelChitChat channel) {
		LogHelper.info("Creating channel " + channel.getName());
		CHANNELS.put(channel.getName(), channel);
	}

	public static void remove(String name) {
		if(existName(name)) {
			remove(get(name));
		}
	}

	public static void remove(ChannelChitChat channel) {
		if(!channel.equals(GLOBAL)) {
			LogHelper.info("Removing channel " + channel.getName());
			ChitChatPlayers.moveChannelToGlobal(channel,
					Text.of(TextColors.RED, "You are being moved to the global channel as the channel you are in is being removed."));
			channel.clearMembers(); //As an extra precaution

			CHANNELS.remove(channel.getName());
		}
	}

	public static boolean existName(String name) {
		return CHANNELS.containsKey(name);
	}

	public static ChannelChitChat get(String name) {
		return CHANNELS.get(name);
	}

	public static void remap(ChannelChitChat channel, String oldName) {
		LogHelper.info("Remapping channel " + oldName + " to " + channel.getName());
		CHANNELS.remove(oldName);
		CHANNELS.put(channel.getName(), channel);
		ChitChat.getStorage().deleteChannel(oldName);
		ChitChat.getStorage().saveChannel(channel);
	}

	public static ImmutableMap<String, ChannelChitChat> getMap() {
		return ImmutableMap.copyOf(CHANNELS);
	}

	public static void clearMap() {
		new ArrayList<>(CHANNELS.values()).forEach(ChitChatChannels::remove); //New List so we don't get any ConcurrentModificationException
	}

	public static ChannelChitChat getGlobal() {
		return GLOBAL;
	}
}
