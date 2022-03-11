package org.riskfirst.tweetprint.image;

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

@FunctionalInterface
public interface CompositeFunction {

	public RenderedImage performCompositing(OrderDetails od, ImageBuilder ib);
	
	
}
