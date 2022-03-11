package org.riskfirst.tweetprint.image.compositing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.riskfirst.tweetprint.builder.Arrangement;
import org.riskfirst.tweetprint.builder.OrderDetails;

public class GreetingsCardCompositeFunction extends AbstractCompositeFunction {
	
	public static final GreetingsCardCompositeFunction INSTANCE = new GreetingsCardCompositeFunction();
	
	protected BufferedImage compositeAllImages(OrderDetails od, float panelWidth, float panelHeight, BufferedImage tweetImage, BufferedImage messageImage) {
		BufferedImage out = new BufferedImage((int) (panelHeight * 4), (int) (panelWidth), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) out.getGraphics();
		g2.setBackground(Color.WHITE);  
		g2.fillRect(0, 0, out.getWidth(), out.getHeight());
		g2.drawImage(messageImage, getRotateOp(true, messageImage), (int) (3*panelHeight), 0);
		g2.drawImage(tweetImage, getRotateOp(od.arrangement == Arrangement.LANDSCAPE, out), (int) panelHeight, 0);
		return out;
	};

}
