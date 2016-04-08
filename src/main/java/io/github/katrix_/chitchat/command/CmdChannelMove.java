package io.github.katrix_.chitchat.command;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatPlayers;
import io.github.katrix_.chitchat.lib.LibCommandKey;
import io.github.katrix_.chitchat.lib.LibPerm;

public class CmdChannelMove extends CommandBase {

	public static final CmdChannelMove INSTANCE = new CmdChannelMove(CmdChannel.INSTACE);

	protected CmdChannelMove(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<User> optUser = args.getOne(LibCommandKey.USER);
		Optional<ChannelChitChat> optChannel = args.getOne(LibCommandKey.CHANNEL_NAME);
		if(!channelExists(src, optChannel)) return CommandResult.empty();
		if(!optUser.isPresent()) {
			src.sendMessage(Text.of(TextColors.RED, "No user by that name found"));
			return CommandResult.empty();
		}
		ChannelChitChat channel = optChannel.get();
		User user = optUser.get();
		if(!permissionChannel(channel.getName(), src, LibPerm.CHANNEL_MOVE)) return CommandResult.empty();
		Optional<Player> optPlayer = user.getPlayer();
		if(optPlayer.isPresent()) {
			Player player = optPlayer.get();
			ChitChatPlayers.getOrCreatePlayer(player).setChannel(channel);
			src.sendMessage(Text.of(TextColors.GREEN, player.getName() + " was move into channel " + channel.getName()));
			player.sendMessage(Text.of(TextColors.YELLOW, "You were moved into " + channel.getName() + " by " + src.getName()));
		}
		else {
			if(!src.hasPermission(LibPerm.CHANNEL_MOVE_OFFLINE)) {
				src.sendMessage(Text.of(TextColors.RED, "You do not have permission to move offline players"));
				return CommandResult.empty();
			}
			ChitChatPlayers.getUserFromUuid(user.getUniqueId()).setChannel(channel);
			src.sendMessage(Text.of(TextColors.GREEN, "Next time " + user.getName() + " joins, they will be in " + channel.getName()));
		}

		return CommandResult.success();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().description(Text.of("Move a player into a channel")).extendedDescription(Text.of("You can also use this command on offline players")).permission(
				LibPerm.CHANNEL_MOVE).arguments(GenericArguments.user(LibCommandKey.USER), new CommandElementChannel(LibCommandKey.CHANNEL_NAME)).executor(this).build();
	}


	@Override
	public String[] getAliases() {
		return new String[] {"move", "moveplayer"};
	}
}
