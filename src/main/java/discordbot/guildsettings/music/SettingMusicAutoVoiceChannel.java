package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicAutoVoiceChannel extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "music_channel_auto";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"The channel where I automatically connect to if a user joins",
				"",
				"false:",
				"Not using this setting, wont auto-connect to anything.",
				"",
				"setting this to match a voice channel name:",
				"The moment a user connects to the specified channel I connect too and start to play music.",
				"",
				"Important to note: ",
				"* If the configured channel does not exist, this setting will be turned off",
				"* If I'm already connected to a different voice-channel I won't use this setting"
		};
	}

	@Override
	public boolean isValidValue(String input) {
		return true;
	}
}