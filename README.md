# Speak
A simple [Fabric](https://fabricmc.net/) based Minecraft mod adding an in-game Voice Chat system.  
Requires [Fabric API](https://modrinth.com/mod/fabric-api).  
You need to install the mod both on the client and the server to work properly.

## For players
The Voice Chat is push to talk. The default keybinding is the `Alt` key. You can change it in the Vanilla's "Controls" menu.

## For server administrators
If you are a server administrator, you can make the mod optional for clients and allow joining without it if you want to. By default, it is optional, and you can change it using `/gamerule requireSpeakMod <true|false>`.  
You can customize the range the player can be heard from using `/gamerule voiceChatRange <value>`.