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
package io.github.katrix_.chitchat.command;

import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.io.ConfigSettings;
import io.github.katrix_.chitchat.lib.LibCommandKey;
import io.github.katrix_.chitchat.lib.LibPerm;

public class CmdReply extends CommandBase {

	public static final CmdReply INSTANCE = new CmdReply(null);

	private CmdReply(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String message = args.<String>getOne(LibCommandKey.MESSAGE).orElse("");
		Optional<CommandSource> receiverOpt = CmdPM.INSTANCE.getConversationPartner(src);

		if(receiverOpt.isPresent()) {
			CommandSource receiver = receiverOpt.get();
			Map<String, TextElement> templateMap = ImmutableMap.of(ConfigSettings.TEMPLATE_PLAYER, Text.of(receiver.getName()), ConfigSettings.TEMPLATE_MESSAGE, Text.of(message));

			receiver.sendMessage(ConfigSettings.getPmReceiverTemplate(), templateMap);
			src.sendMessage(ConfigSettings.getPmSenderTemplate(), templateMap);

			if(ConfigSettings.getChatPling() && receiver instanceof Player) {
				Player player = (Player)receiver;
				player.playSound(SoundTypes.ORB_PICKUP, player.getLocation().getPosition(), 0.5D);
			}

			return CommandResult.success();
		}

		src.sendMessage(Text.of(TextColors.RED, "You don't have anyone to reply to"));
		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().description(Text.of("Send a message to the person you most recently had a conversation with"))
				.permission(LibPerm.REPLY).arguments(GenericArguments.remainingJoinedStrings(LibCommandKey.MESSAGE)).executor(this).build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"r", "reply"};
	}
}
