/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.game.model;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;


public class ImageUtils {
	
	private final static Component component = new Component() {};
	private final static GraphicsConfiguration gc;
	
	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
	}
	
	public static BufferedImage loadImage(URL imageURL) throws IllegalArgumentException {
		try {
			BufferedImage im = ImageIO.read(imageURL);
			if (im == null) {
				return null;
			}
			int transparency = im.getColorModel().getTransparency();
			BufferedImage copy = gc.createCompatibleImage(im.getWidth(), im.getHeight(), transparency);
			Graphics2D g2d = copy.createGraphics();
			g2d.drawImage(im, 0, 0, null);
			g2d.dispose();
			return copy;
		} catch (IOException e) {
			System.out.println("Load Image error for " + imageURL + ":\n" + e); // NOI18N
			e.printStackTrace();
			return null;
		}
	}
	
	//XXX - this method must be removed!!!
	public static Image getScaledImage(Image image, int maxWidth, int maxHeight) {
		Image scaledImage = image;
		//if we want to scale the image (passed into the constructor) then do it
		if (maxWidth != 0 && maxHeight != 0) {
			float imgRatio = (float) image.getHeight(null) / image.getWidth(null);
			float previewRatio = (float) maxHeight / maxWidth;
			if (previewRatio > imgRatio) {
				scaledImage = image.getScaledInstance(maxWidth, -1, Image.SCALE_SMOOTH | Image.SCALE_AREA_AVERAGING);
			} else {
				scaledImage = image.getScaledInstance(-1, maxHeight, Image.SCALE_SMOOTH | Image.SCALE_AREA_AVERAGING);
			}
		}
		MediaTracker mediaTracker = new MediaTracker(component);
		mediaTracker.addImage(scaledImage, 0);
		try {
			mediaTracker.waitForID(0);
		} catch (InterruptedException e) {
			mediaTracker.removeImage(scaledImage, 0);
			e.printStackTrace();
		}
		mediaTracker.removeImage(scaledImage, 0);
		return scaledImage;
	}
	
}
