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
package net.katsstuff.old.chitchat.chat.data;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import net.katsstuff.old.chitchat.lib.LibKeys;

public class ImmutableUserChitChatData extends AbstractImmutableData<ImmutableUserChitChatData, UserChitChatData> {

	private final DataQuery channel;
	private final ImmutableValue<DataQuery> channelValue;

	public ImmutableUserChitChatData(DataQuery channel) {
		this.channel = channel;
		channelValue = Sponge.getRegistry().getValueFactory().createValue(LibKeys.USER_CHANNEL, channel).asImmutable();
		registerGetters();
	}

	@Override
	protected void registerGetters() {
		registerKeyValue(LibKeys.USER_CHANNEL, this::channel);
		registerFieldGetter(LibKeys.USER_CHANNEL, this::getChannel);
	}

	@SuppressWarnings("WeakerAccess")
	public DataQuery getChannel() {
		return channel;
	}

	@SuppressWarnings("WeakerAccess")
	public ImmutableValue<DataQuery> channel() {
		return channelValue;
	}

	@Override
	public UserChitChatData asMutable() {
		return new UserChitChatData(channel);
	}

	@Override
	public int compareTo(ImmutableUserChitChatData o) {
		return 0;
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
