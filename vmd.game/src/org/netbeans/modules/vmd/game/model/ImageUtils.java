/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
			System.err.println("Load Image error for " + imageURL + ":\n" + e); // NOI18N
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
