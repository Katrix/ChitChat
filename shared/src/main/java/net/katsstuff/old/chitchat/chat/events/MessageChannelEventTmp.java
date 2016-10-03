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
package net.katsstuff.old.chitchat.chat.events;

import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

/**
 * Tmp(for now) to use for events until I figure out the best way to solve it.
 */
public class MessageChannelEventTmp implements MessageChannelEvent {

	private final MessageChannel originalMessageChannel;
	@Nullable
	private MessageChannel messageChannel;
	private final Text originalMessage;
	private boolean isCancelled;
	private final MessageFormatter formatter;
	private final Cause cause;

	public MessageChannelEventTmp(Cause cause, MessageChannel messageChannel, MessageFormatter formatter, boolean isCancelled) {
		this.originalMessageChannel = messageChannel;
		this.messageChannel = messageChannel;
		this.originalMessage = formatter.format();
		this.isCancelled = isCancelled;
		this.formatter = formatter;
		this.cause = cause;
	}

	@Override
	public MessageChannel getOriginalChannel() {
		return originalMessageChannel;
	}

	@Override
	public Optional<MessageChannel> getChannel() {
		return Optional.ofNullable(messageChannel);
	}

	@Override
	public void setChannel(@Nullable MessageChannel channel) {
		messageChannel = channel;
	}

	@Override
	public Text getOriginalMessage() {
		return originalMessage;
	}

	@Override
	public boolean isMessageCancelled() {
		return isCancelled;
	}

	@Override
	public void setMessageCancelled(boolean cancelled) {
		isCancelled = cancelled;
	}

	@Override
	public MessageFormatter getFormatter() {
		return formatter;
	}

	@Override
	public Cause getCause() {
		return cause;
	}
}
