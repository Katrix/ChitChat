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
package io.github.katrix.chitchat.chat.data;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import io.github.katrix.chitchat.chat.channels.ChannelRoot;
import io.github.katrix.chitchat.lib.LibKeys;

public class UserChitChatData extends AbstractData<UserChitChatData, ImmutableUserChitChatData> {

	//How to deal with this one?
	private DataQuery channel = ChannelRoot.getRoot().getQueryName();

	public UserChitChatData() {
		registerGettersAndSetters();
	}

	public UserChitChatData(DataQuery channel) {
		this();
		this.channel = channel;
	}

	@Override
	protected void registerGettersAndSetters() {
		registerKeyValue(LibKeys.USER_CHANNEL, this::channel);
		registerFieldGetter(LibKeys.USER_CHANNEL, this::getChannel);
		registerFieldSetter(LibKeys.USER_CHANNEL, this::setChannel);
	}

	@SuppressWarnings("WeakerAccess")
	public Value<DataQuery> channel() {
		return Sponge.getRegistry().getValueFactory().createValue(LibKeys.USER_CHANNEL, channel, ChannelRoot.getRoot().getQueryName());
	}

	@SuppressWarnings("WeakerAccess")
	public DataQuery getChannel() {
		return channel;
	}

	@SuppressWarnings("WeakerAccess")
	public UserChitChatData setChannel(DataQuery channel) {
		this.channel = channel;
		return this;
	}

	@Override
	public Optional<UserChitChatData> fill(DataHolder dataHolder, MergeFunction overlap) {
		UserChitChatData newObj = new UserChitChatData().setChannel(dataHolder.get(LibKeys.USER_CHANNEL).orElse(channel));
		UserChitChatData merged = overlap.merge(copy(), newObj);
		return Optional.of(setChannel(merged.channel));
	}

	@Override
	public Optional<UserChitChatData> from(DataContainer container) {
		if(container.contains(LibKeys.USER_CHANNEL)) {
			//noinspection OptionalGetWithoutIsPresent
			channel = container.getObject(LibKeys.USER_CHANNEL.getQuery(), DataQuery.class).get();
			return Optional.of(this);
		}

		return Optional.empty();
	}

	@Override
	public UserChitChatData copy() {
		return new UserChitChatData(channel);
	}

	@Override
	public ImmutableUserChitChatData asImmutable() {
		return new ImmutableUserChitChatData(channel);
	}

	@Override
	public int compareTo(UserChitChatData o) {
		return channel.toString().compareTo(o.toString());
	}

	@Override
	public int getContentVersion() {
		return 0;
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer()
				.set(LibKeys.USER_CHANNEL, channel);
	}
}
