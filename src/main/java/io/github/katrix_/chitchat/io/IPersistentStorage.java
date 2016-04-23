/*
 * This file is part of PermissionBlock, licensed under the MIT License (MIT).
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
package io.github.katrix_.chitchat.io;

import java.util.UUID;

import javax.annotation.Nullable;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.UserChitChat;

public interface IPersistentStorage {

	/**
	 * Deletes all the channels and adds them again
	 * @return if successful.
	 */
	boolean reloadChannels();

	/**
	 * Saves a channel to disk.
	 * @param channel The channel to save.
	 * @return if successful.
	 */
	boolean saveChannel(ChannelChitChat channel);

	/**
	 * Update a channel with new information
	 * @param channel The Channel to modify.
	 * @param prefix Set a new prefix. If null it will not change the prefix.
	 * @param description Set a new description. If null it will not change the description.
	 * @return if successful, or if anything changed.
	 */
	default boolean updateChannel(ChannelChitChat channel, @Nullable Text prefix, @Nullable Text description) {
		return updateChannel(channel.getName(), prefix, description);
	}

	/**
	 * Update a channel with new information
	 * @param channel The Channel to modify.
	 * @param prefix Set a new prefix. If null it will not change the prefix.
	 * @param description Set a new description. If null it will not change the description.
	 * @return if successful, or if anything changed.
	 */
	boolean updateChannel(String channel, @Nullable Text prefix, @Nullable Text description);

	/**
	 * Deletes a channel from storage.
	 * @param channel The channel to delete.
	 * @return if successful.
	 */
	default boolean deleteChannel(ChannelChitChat channel) {
		return deleteChannel(channel.getName());
	}

	/**
	 * Deletes a channel from storage.
	 * @param channel The channel to delete.
	 * @return if successful.
	 */
	boolean deleteChannel(String channel);

	/**
	 * Gets the channel a user is currently in.
	 * @param user The User to get the channel for.
	 * @return The channel the user is in. Returns global if no channel is found.
	 */
	default ChannelChitChat getChannelForUser(User user) {
		return getChannelForUser(user.getUniqueId());
	}

	/**
	 * Gets the channel a user is currently in.
	 * @param uuid The uuid for the user to get the channel for.
	 * @return The channel the user is in. Returns global if no channel is found.
	 */
	ChannelChitChat getChannelForUser(UUID uuid);

	/**
	 * Updates what's stored in the storage with new information.
	 * @param user The User to update for.
	 * @return if successful.
	 */
	boolean updateUserChannel(UserChitChat user);
}
