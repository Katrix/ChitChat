package net.katsstuff.chitchat.lib

object LibPerm {

  final val ChitChat = "chitchat"

  final val Chat      = s"$ChitChat.chat"
  final val ChatColor = s"$Chat.color"

  final val UserCmd = s"$ChitChat.userCmd"
  final val JoinChannel = s"$UserCmd.joinChannel"
  final val ListChannels = s"$UserCmd.listChannels"
  final val InChannel = s"$UserCmd.inChannel"

  final val AdminCmd = s"$ChitChat.adminCmd"
  final val AdminInterfaceCmd = s"$AdminCmd.interface"
  final val CreateChannelCmd = s"$AdminCmd.createChannel"
  final val ChangeChannelCmd = s"$AdminCmd.editChannel"
  final val DeleteChannelCmd = s"$AdminCmd.deleteChannel"

}
