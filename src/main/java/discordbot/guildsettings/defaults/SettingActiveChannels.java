package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingActiveChannels extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "bot_listen";
	}

	@Override
	public String getDefault() {
		return "all";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"What channels to listen to? (all;mine)",
				"all -> responds to all channels",
				"mine -> only responds to messages in configured channel"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("all") || input.equals("mine"));
	}
}
