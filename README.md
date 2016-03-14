#ChitChat
ChitChat is a powerful chat plugin that allows you to make the chat look just like you want it to. ChitChat allows you to make custom chat channels, and control who can enter those channels. ChitChat also provides basic commands like pm, me, broadcase, which you can also change the format of.

##Features
- Divide chat into channels.
- Create, modify and remove channels on the fly.
- Makes ugly commands look good.
- Completely customizable how every command and normal chat looks.

**NOTE:** Currently channels do not persist when closing the server. This will be implemented soon.

##Commands
- `/announce -c <message>` - Send a message to all players. If the flag -c is specified, it will do the announcement as console.
- `/channel` - Do stuff with channels.
- `/channel create <name> [<prefix>] [<description>]` - Create a new channel. Prefix and description is optional. Prefix and description can also be formated using &. If no prefix is specified, the name of the channel will be used instead.
- `/channel join <channel name>` - Join an already existing channel.
- `/channel list` - Will show you a list of all the channels you can join.
- `/channel properties <channel name>` - Shows you the properties(name, prefix description, members) of a channel.
- `/channel properties description <channel name> [<new description>]` - Specify a new description for a channel. If no description is provided it will be removed.
- `/channel properties name <channel name> <new name>` - Specify a new name for a channel.
- `/channel properties prefix <channel name> [<new prefix>]` - Specify a new prefix for a channel. If no description is provided it will use the channel name instead.
- `/channel remove <channel name>` - Removes an already existing channel. If there is currently players in the channel, they will be moved to the global channel.
- `/chitchat` - Shows you information about ChitChat.
- `/chitchat help [<command>]` - If used with no parameters shows the help for all the commands registered by ChitChat. You can also show the help for individual commands by specifying the parameters to be that command. This also works with subcommands. For example do `/chitchat help channel create` to see the help for the create channel command.
- `/chitchat reload` - Reloads the configs for ChitChat.
- `/me <message>` - Act out something.
- `/pm <player> <message>` - Sends a PM to another player. If someone sends a PM to you, you will also hear a pling sound if not disabled in the config.
- `/r <message>` - Sends a PM to the person you most recently had a conversation with.
- `/shout <channel name> <message>` - Sends a message to a specific channel.

##Permissions
- `chitchat.command.chitchat` - Allows players to use `/chitchat`.
- `chitchat.command.chitchat.help` - Allows players to use `/chitchat help`.
- `chitchat.chat.shout` - Allows players to use `/shout`.
- `chitchat.chat.shout.<channel name>` - Allows players to shout to the given channel.
- `chitchat.chat.pm` - Allows players to use `/pm`.
- `chitchat.chat.reply` - Allows players to use `/r`.
- `chitchat.chat.me` - Allows players to use `/me`.
- `chitchat.chat.color` - Will format players chat using formating codes(&).
- `chitchat.chat.mod.announce` - Allows players to use `/announce`.
- `chitchat.chat.mod.announce.console` - Allows players to use `/announce -c`.
- `chitchat.admin.reload` - Allows players to use `/chitchat reload`.
- `chitchat.channel` - Allows players to use `/channel`.
- `chitchat.channel.join` - Allows players to use `/channel join`.
- `chitchat.channel.join.<channelName>` - Allows players to join players with the given channel.
- `chitchat.channel.list` - Allows players to use `/channel list`.
- `chitchat.channel.info` - Allows players to use `/channel properties`.
- `chitchat.channel.info.<channel name>` - Allows players to see the info of the given channel.
- `chitchat.channel.mod.modify.name` - Allows players to use `/channel properties name`.
- `chitchat.channel.mod.modify.name.<channel name>` - Allows players change the name of the given channel. The player also needs to have permission for the new name.
- `chitchat.channel.mod.modify.prefix` - Allows players to use `/channel properties prefix`.
- `chitchat.channel.mod.modify.prefix.<channel name>` - Allows players to change the prefix of the given channel.
- `chitchat.channel.mod.modify.description` - Allows players to use `/channel properties description`.
- `chitchat.channel.mod.modify.description.<channel name>` - Allows players to change the description of the given channel.
- `chitchat.channel.mod.create` - Allows players to use `/channel create`.
- `chitchat.channel.mod.create.<channel name>` - Allows players to use create channels with the given channel name.
- `chitchat.channel.mod.remove` - Allows players to use `/channel remove`.
- `chitchat.channel.mod.remove.<channel name>` - Allows players to remove channels with the given channel name.