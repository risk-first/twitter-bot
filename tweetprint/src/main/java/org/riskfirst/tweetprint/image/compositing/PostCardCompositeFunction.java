package org.riskfirst.tweetprint.image.compositing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import org.riskfirst.tweetprint.builder.Arrangement;
import org.riskfirst.tweetprint.builder.OrderDetails;

public class PostCardCompositeFunction extends AbstractCompositeFunction {
	
	public static final PostCardCompositeFunction INSTANCE = new PostCardCompositeFunction();

	protected BufferedImage compositeAllImages(OrderDetails od, float panelWidth, float panelHeight, BufferedImage tweetImage, BufferedImage messageImage) {
			BufferedImage out = new BufferedImage((int) (panelWidth * 2), (int) (panelHeight), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D) out.getGraphics();
			g2.setBackground(Color.WHITE);  
			g2.fillRect(0, 0, out.getWidth(), out.getHeight());
			g2.drawImage(messageImage, 0, 0, null);
			BufferedImageOp op = getRotateOp(od.arrangement == Arrangement.PORTRAIT, tweetImage);
			g2.drawImage(tweetImage, op, (int) panelWidth, 0);
			return out;
		};;

}
