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

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import io.github.katrix.chitchat.lib.LibKeys;

public class UserChitChatManipulatorBuilder implements DataManipulatorBuilder<UserChitChatData, ImmutableUserChitChatData> {

	@Override
	public UserChitChatData create() {
		return new UserChitChatData();
	}

	@Override
	public Optional<UserChitChatData> createFrom(DataHolder dataHolder) {
		return create().fill(dataHolder);
	}

	@Override
	public Optional<UserChitChatData> build(DataView container) throws InvalidDataException {
		if(container.contains(LibKeys.USER_CHANNEL)) {
			@SuppressWarnings("OptionalGetWithoutIsPresent")
			DataQuery query = container.getObject(LibKeys.USER_CHANNEL.getQuery(), DataQuery.class).get();
			return Optional.of(new UserChitChatData(query));
		}

		return Optional.empty();
	}
}
