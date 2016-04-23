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

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import io.github.katrix_.chitchat.ChitChat;

public class UserChitChat {

	private final UUID uuid;
	private ChannelChitChat channel = ChitChatChannels.getGlobalChannel();

	public UserChitChat(UUID uuid) {
		this.uuid = uuid;
	}

	public UserChitChat(Player player) {
		uuid = player.getUniqueId();
		channel.addMember(player);
	}

	public UserChitChat(Player player, ChannelChitChat channel) {
		uuid = player.getUniqueId();
		this.channel = channel;
		channel.addMember(player);
	}

	public UserChitChat(UUID uuid, ChannelChitChat channel) {
		this.uuid = uuid;
		this.channel = channel;
	}

	public UUID getUUID() {
		return uuid;
	}

	public Optional<Player> getPlayer() {
		return Sponge.getServer().getPlayer(uuid);
	}

	public ChannelChitChat getChannel() {
		return channel;
	}

	public void setChannel(ChannelChitChat channel) {
		Optional<Player> optPlayer = getPlayer();
		if(optPlayer.isPresent()) {
			Player player = optPlayer.get();
			this.channel.removeMember(player);
			this.channel = channel;
			this.channel.addMember(player);
		}
		else {
			this.channel = channel;
		}
		ChitChat.getStorage().updateUserChannel(this);
	}
}
