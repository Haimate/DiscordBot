package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * introduction of music_log; keep track of whats being played
 */
public class db_11_to_12 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 11;
	}

	@Override
	public int getToVersion() {
		return 12;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE music_log(\n" +
						" id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,\n" +
						" music_id INT(11) DEFAULT '0' NOT NULL,\n" +
						" guild_id INT(11) DEFAULT '0' NOT NULL,\n" +
						" user_id INT(11),\n" +
						" play_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL )\n",
				"CREATE INDEX music_log_guild_id_index ON music_log (guild_id)",
				"CREATE INDEX music_log_guild_id_music_id_index ON music_log (guild_id, music_id)",
				"CREATE INDEX music_log_music_id_index ON music_log (music_id)",
				"CREATE INDEX music_filename_unique_index ON music (filename)",
				"CREATE INDEX music_youtubecode_unique_index ON music (youtubecode)",
		};
	}
}