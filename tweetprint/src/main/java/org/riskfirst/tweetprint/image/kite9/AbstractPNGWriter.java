package org.riskfirst.tweetprint.image.kite9;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class AbstractPNGWriter {

	public void writeImageAsPng(HttpServletResponse response, BufferedImage image) throws Exception {

		ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, "png", jpegOutputStream);
		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

		byte[] imgByte = jpegOutputStream.toByteArray();

		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/png");
		ServletOutputStream responseOutputStream = response.getOutputStream();
		responseOutputStream.write(imgByte);
		responseOutputStream.flush();
		responseOutputStream.close();
	}
//	
//	public void writePortionAsPng(HttpServletResponse response, Graphics2D image, Rectangle2D portion, Dimension2D size, boolean landscape) throws Exception {
//		BufferedImage bImage = new BufferedImage((int) size.getWidth(), (int) size.getHeight(), BufferedImage.TYPE_INT_RGB);
//	
//		image.drawim
//	
//	}
	
	
}
