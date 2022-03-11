package org.riskfirst.tweetprint.builder.compositing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.riskfirst.tweetprint.builder.Arrangement;
import org.riskfirst.tweetprint.builder.CardType;
import org.riskfirst.tweetprint.builder.OrderDetails;
import org.riskfirst.tweetprint.image.ImageBuilder;

@FunctionalInterface
public interface CompositeFunction {

	public RenderedImage performCompositing(OrderDetails od, ImageBuilder ib);
	
	public static CompositeFunction POSTCARD_CF = (od, ib) -> {
		try {
			float panelWidth = CardType.POSTCARD.width;
			float panelHeight = CardType.POSTCARD.height;
			
			BufferedImage out = new BufferedImage((int) (panelWidth * 2), (int) (panelHeight), BufferedImage.TYPE_INT_RGB);
			
			float width , height;
			if (od.arrangement == Arrangement.PORTRAIT) {
				width = panelHeight;
				height = panelWidth;
			} else {
				width = panelWidth;
				height = panelHeight;
			}
			
			byte[] tweetImageBytes = ib.produceTweetImage(od, width, height);
			byte[] messageImageBytes = ib.produceMessageImage(od, panelWidth, panelHeight);
			
			BufferedImage tweetImage = ImageIO.read(new ByteArrayInputStream(tweetImageBytes));
			BufferedImage messageImage = ImageIO.read(new ByteArrayInputStream(messageImageBytes));
			
			Graphics2D g2 = (Graphics2D) out.getGraphics();
			g2.setBackground(Color.WHITE);  
			g2.fillRect(0, 0, out.getWidth(), out.getHeight());
			g2.drawImage(messageImage, 0, 0, null);
			BufferedImageOp op = getOp(od.arrangement, tweetImage);
			g2.drawImage(tweetImage, op, (int) panelWidth, 0);
			
			return out;
			
		} catch (Exception e) {
			throw new RuntimeException("Couldn't produce composite image", e);
		}
	};

	public static BufferedImageOp getOp(Arrangement arrangement, BufferedImage originalImage) {
		if (arrangement == Arrangement.PORTRAIT) {
			AffineTransform tx = new AffineTransform();
	
			// last, width = height and height = width :)
			tx.translate(originalImage.getHeight() / 2,originalImage.getWidth() / 2);
			tx.rotate(Math.PI / 2);
			// first - center image at the origin so rotate works OK
			tx.translate(-originalImage.getWidth() / 2,-originalImage.getHeight() / 2);
	
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
			return op;
		} else {
			return null;
		}
	}
	
}
