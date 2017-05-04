#ChitChat
ChitChat is a powerful chat plugin that allows you to make the chat look just like you want it to. ChitChat allows you to make custom chat channels, and control who can enter those channels. ChitChat also provides basic commands like pm, me, broadcast, which you can also change the format of.

ChitChat needs KatLib 2.1.0.

##Features
- Divide chat into channels.
- Create, modify and remove channels on the fly.
- Makes ugly commands look good.
- Completely customizable how every command and normal chat looks.
- Play a sound every time a player is mentioned in chat
- Uses TextTemplateAppliers, which means that it will play nice with other chat plugins that also uses them

##Formatting
Every message in ChitChat is customizable. You can change these messages in the config. You will for the most part encounter two types of config values. `Text` is used for normal text when it's just printed without any changes. `TextTemplate` is a template `Text` where some values will be replaced before it is used. Note that if another plugin replaces the `TextTemplate` specified by ChitChat, you should still be able to insert the arguments into the new `TextTemplate` as ChitChat applies it's argument to all the `TextTemplate`s it can find.

The second type of formatting you would want to do is to format how a player is represented in a chat message. ChitChat uses permission options to add prefix and suffix to the chat message. `prefix` and `suffix` for formatting with `&`. `jsonprefix` and `jsonsuffix` for formatting with json. An example with the normal variant would be `&9[&2Admin&9]`. Do note that the color of the player name is not supplied in the prefix, but instead in a separate permission option called `namestyle`. In here you specify the color and style you want for the player name formatting codes, for example `&5`. The reason they are separate is to allow moving the prefix and the player name around in the config independently of each other.

##Commands
- `/announce -c <message>` - Send a message to all players. If the flag -c is specified, it will do the announcement as console.
- `/inChannel|chmembers <channel>` - List the people in a channel.
- `/joinChannel|chjoin <channel>` - Join a channel.
- `/listChannels|channels|chlist` - Lists all channels, with a join button.
- `/chitchat` - Shows you information about ChitChat.
- `/chitchat help [<command>]` - If used with no parameters shows the help for all the commands registered by ChitChat. You can also show the help for individual commands by specifying the parameters to be that command. This also works with subcommands.
- `/me <message>` - Act out an action.
- `/channelmod|chmod` - Interface for editing different channels.
- `/channelmod|chmod edit|modify [--name <name>] [--prefix <prefix>] [--description <description>] [--extra <extra...>]` - Change a channel's settings. Note that you can use quotes to include spaces. So if you for example wanted to change the name and description, you would do `/chmod edit MyChannel --name MyNewChannel --description "This is a description"`. Don't worry about the type and extra field, they are currently unused.
- `/channelmod|chmod create|new <name> [prefix] [description] [type] [extra...]` - Create a new channel. Note that you can use quotes to include spaces. Don't worry about the type and extra field, they are currently unused.
- `/channelmod|chmod delete|remove <channel>` - Delete a channel.
- `/pm|msg|whisper [player] <message>` - Send a message to another player. The player is optional if you they were the previous player you sent a message to.
- `/r|reply <message>` - Sends a PM to the person you most recently had a conversation with. Same as `/pm <message>`
- `/shout <channel name> <message>` - Sends a message to another channel.

##Permissions
- `chitchat.chat.color` - Allows the player to have their chat color formatted.


- `chitchat.user.joinchannel.[channelName]` - Allows players to use `/joinChannel`. A channel name can be specified to only allow entrance into those channels.
- `chitchat.user.listchannels` - Allows players to use `/listChannels`.
- `chitchat.user.inchannel` - Allows players to use `/inChannel`.
- `chitchat.user.me` - Allows players to use `/me`.
- `chitchat.user.pm` - Allows players to use `/pm` and `/r`.
- `chitchat.user.shout.[channelName]` - Allows players to use `/shout`. A channel name can be specified to only allow shouting to those channels.


- `chitchat.mod.interface.[channelName]` - Allows players to use `/channelmod`. A channel name can be specified to only allow seeing those channels.
- `chitchat.mod.createchannel.[channelName]` - Allows players to use `/channelmod create`. A channel name can be specified to only allow creating channels with that name.
- `chitchat.mod.editchannel.[channelName]` - Allows players to use `/channelmod edit`. A channel name can be specified to only allow editing channels with that name.
- `chitchat.mod.deletechannel.[channelName]` - Allows players to use `/channelmod edit`. A channel name can be specified to only allow deleting channels with that name.
- `chitchat.mod.announce` - Allows players to use `/announce`.
- `chitchat.mod.announceconsole` - Allows players to use `/announce -c`.

## Channel types

Chit chat can have multiple channel types, however at the moment it only has one, so no need to think about that too much.

### Simple

Has a prefix, a description and a name. The most barebone type of channel.

## Future plans
For now ChitChat only has one channel type, the simple one. Somewhat soon (hopefully) ChitChat will gain a more advanced channel type that is super extensible, especially if you have some other ways of giving users specific permission nodes through commands. So, what are examples of what it should be able to do once it's in?
- Spy on channels
- Listen to multiple channels
- Filter messages
- World channels
- Distance based channels.