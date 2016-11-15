package discordbot.modules.profile;

import discordbot.db.controllers.CUser;
import discordbot.db.model.OUser;
import discordbot.main.Launcher;
import discordbot.util.GfxUtil;
import net.dv8tion.jda.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ProfileImageV3 extends ProfileImage {
	private Random rng;

	public ProfileImageV3(User user) {
		super(user);
		rng = new Random();
	}

	public File getProfileImage() throws IOException {
		int fontsize = 28;
		if (getUser().getUsername().length() <= 4) {
			fontsize = 32;
		}
		if (getUser().getUsername().length() > 8) {
			fontsize = 22;
		}
		OUser dbuser = CUser.findBy(getUser().getId());
		double level = Math.log(dbuser.commandsUsed + 1);//+1 for this command
		int xpPercent = (int) ((level % 1D) * 100D);
		Font defaultFont = new Font("Forte", Font.BOLD + Font.ITALIC, fontsize);
		Font score = new Font("Forte", Font.BOLD, 24);
		Font creditFont = new Font("Forte", Font.ITALIC, 12);
		BufferedImage result = new BufferedImage(
				320, 265,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.getGraphics();
		g.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		BufferedImage profileImg = getUserAvatar();
		BufferedImage backgroundImage = ImageIO.read(Launcher.class.getClassLoader().getResource("profile_bg_6.png"));
		BufferedImage xpProgressBar = ImageIO.read(Launcher.class.getClassLoader().getResource("progressbar.png"));

		g.drawImage(profileImg, 18, 33, 141, 159, 0, 0, profileImg.getWidth(), profileImg.getHeight(), null);
		g.drawImage(backgroundImage, 0, 0, 320, 265, 0, 0, 320, 265, null);
		g.drawImage(xpProgressBar, 137, 133, 317 - (int) ((181D / 100D) * (100D - xpPercent)), 148, 0, 0, 175, 15, null);

		GfxUtil.addCenterShadow(getUser().getUsername(), defaultFont, 222, 71 + (fontsize / 2), g, Color.black);
		GfxUtil.addCenterText(getUser().getUsername(), defaultFont, 222, 71 + (fontsize / 2), g, Color.white);
		GfxUtil.addRightText("made by Emily", creditFont, 318, 199, g, new Color(0x3A3A38));
		GfxUtil.addCenterShadow("" + xpPercent + "%", creditFont, 218, 145, g, Color.black);
		GfxUtil.addCenterText("" + xpPercent + "%", creditFont, 218, 145, g, new Color(0xf37000));//% xp

		GfxUtil.addText("" + (int) level, score, 173, 118, g, new Color(0xffff00));//rewards
//		GfxUtil.addRightText("" + rng.nextInt(1000), score, 290, 118, g, new Color(0x36cbe9));//currency

		GfxUtil.addCenterText("" + rng.nextInt(100), score, 31, 246, g, new Color(0x5c7e32));//health
		GfxUtil.addCenterText("" + rng.nextInt(100), score, 134, 246, g, new Color(0x5c7e32));//attack
		GfxUtil.addCenterText("" + rng.nextInt(100), score, 237, 246, g, new Color(0x5c7e32));//defense
		File file = new File("profile_v2_" + getUser().getId() + ".png");
		ImageIO.write(result, "png", file);
		return file;
	}
}