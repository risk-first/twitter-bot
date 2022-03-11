package org.riskfirst.tweetprint.image.compositing;

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
import org.riskfirst.tweetprint.image.CompositeFunction;
import org.riskfirst.tweetprint.image.ImageBuilder;

public abstract class AbstractCompositeFunction implements CompositeFunction {

	public static BufferedImageOp getRotateOp(boolean rotate, BufferedImage originalImage) {
		if (rotate) {
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
	
	@Override
	public RenderedImage performCompositing(OrderDetails od, ImageBuilder ib) {
		try {
			float panelWidth = CardType.POSTCARD.width;
			float panelHeight = CardType.POSTCARD.height;
					
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
			
			BufferedImage out = compositeAllImages(od, panelWidth, panelHeight, tweetImage, messageImage);
			
			return out;
			
		} catch (Exception e) {
			throw new RuntimeException("Couldn't produce composite image", e);
		}
	}

	protected abstract BufferedImage compositeAllImages(OrderDetails od, float panelWidth, float panelHeight, BufferedImage tweetImage, BufferedImage messageImage);

}
