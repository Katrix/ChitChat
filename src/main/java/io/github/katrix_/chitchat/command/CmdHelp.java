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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList.Builder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.github.katrix_.chitchat.helper.LogHelper;
import io.github.katrix_.chitchat.lib.LibCommandKey;
import io.github.katrix_.chitchat.lib.LibPerm;

public class CmdHelp extends CommandBase {

	public static final CmdHelp INSTANCE = new CmdHelp(CmdChitChat.INSTANCE);
	private BiMap<CommandBase, List<String>> commandMap = HashBiMap.create();

	private String baseCommand(CommandBase command) {
		StringBuilder completeCommand = new StringBuilder(command.getAliases()[0]);
		CommandBase commandParent = command;

		while(commandParent.getParent() != null) {
			commandParent = commandParent.getParent();
			completeCommand.insert(0, commandParent.getAliases()[0] + " ");
		}
		completeCommand.insert(0, "/");

		return completeCommand.toString();
	}

	private BiFunction<CommandBase, CommandSource, Text> getHelp = (command, src) -> {
		String stringCommand = baseCommand(command);
		CommandSpec commandSpec = command.getCommand();

		Text.Builder commandText = Text.builder().append(Text.of(TextColors.GREEN, TextStyles.UNDERLINE, stringCommand));
		commandText.onHover(TextActions.showText(commandSpec.getHelp(src).orElse(commandSpec.getUsage(src))));
		commandText.onClick(TextActions.suggestCommand(stringCommand));
		return Text.of(commandText, Text.of(" "), commandSpec.getShortDescription(src).orElse(commandSpec.getUsage(src)));
	};

	private CmdHelp(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<String> optCommandName = args.<String>getOne(LibCommandKey.COMMAND);
		if(!optCommandName.isPresent()) {
			Builder pages = Sponge.getGame().getServiceManager().provideUnchecked(PaginationService.class).builder();
			pages.title(Text.of(TextColors.RED, "ChitChat Help"));

			List<Text> text = commandMap.keySet().stream().map(commandBase -> getHelp.apply(commandBase, src)).collect(Collectors.toList());
			text.sort(null);
			pages.contents(text);
			pages.sendTo(src);
		}
		else {
			String commandName = optCommandName.get();
			Optional<CommandBase> optCommand = getCommandFromString(commandName);
			if(!optCommand.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, "Command not found"));
				return CommandResult.empty();
			}

			CommandSpec commandSpec = optCommand.get().getCommand();
			Text.Builder commandText = Text.builder().append(Text.of(TextColors.GREEN, TextStyles.UNDERLINE, "/" + commandName));
			commandText.onHover(TextActions.showText(commandSpec.getHelp(src).orElse(commandSpec.getUsage(src))));
			src.sendMessage(commandText.build());
		}
		return CommandResult.success();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().description(Text.of("This command right here."))
				.extendedDescription(Text.of("Use /chitchat help <command> <subcommand> \nto get help for a specific command"))
				.permission(LibPerm.HELP).arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(LibCommandKey.COMMAND)))
				.executor(this).build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"help"};
	}

	public static void registerCommandHelp(String parent, String[] aliases, CommandBase command) {
		if(parent == null) {
			List<String> list = Arrays.asList(aliases);
			LogHelper.debug("Registering help command: " + list);
			INSTANCE.commandMap.put(command, list);
		}
		else {
			List<String> existingList = INSTANCE.commandMap.get(command);
			List<String> list = existingList != null ? new ArrayList<>(INSTANCE.commandMap.get(command)) : new ArrayList<>();

			for(String string : aliases) {
				list.add(parent + " " + string);
			}
			LogHelper.debug("Registering help command: " + list);
			INSTANCE.commandMap.put(command, list);
		}
	}

	private Optional<CommandBase> getCommandFromString(String commandName) {
		for(List<String> list : commandMap.inverse().keySet()) {
			if(list.contains(commandName)) {
				for(String string : list) {
					if(string.equals(commandName)) return Optional.of(commandMap.inverse().get(list));
				}
			}
		}
		return Optional.empty();
	}
}
