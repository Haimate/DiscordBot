package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingWelcomeNewUsers extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "welcome_new_users";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Show a welcome message to new users?",
				"Valid options:",
				"true  -> shows a welcome when a user joins or leaves the guild",
				"false -> Disabled, doesn't say anything",
				"",
				"The welcome message can be set with the template: ",
				"welcome_new_user",
				"",
				"The welcome back message can be set with the template (if the user had joined before): ",
				"welcome_back_user",
				"",
				"The leave message can be set with the template: ",
				"message_user_leaves",
				"",
				"If multiple templates are set a random one will be chosen",
				"See the template command for more details"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
