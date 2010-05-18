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
package org.netbeans.modules.vmd.game.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;

import org.netbeans.modules.vmd.game.model.ImageUtils;


/**
 *
 * @author kherink
 */
public class PartialImageGridPreview extends AbstractImagePreviewComponent {

    public static final boolean DEBUG = false;
    
	private static final int TILE_GAP = 4;
	
	private URL imageURL;
	private BufferedImage originalImage;
	private Image preview;
	private int tileWidth;
	private int tileHeight;
	
	public PartialImageGridPreview () {
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		Dimension d = new Dimension(50, 20);
		this.setMinimumSize(d);
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (DEBUG) System.out.println("resized - updating preview"); // NOI18N
				PartialImageGridPreview.this.createPartialPreview();
				PartialImageGridPreview.this.repaint();
			}
		});
	}
	
	public void setImageURL(URL imageURL) throws MalformedURLException, IllegalArgumentException {
		this.imageURL = imageURL;
		if (imageURL == null){
                    return;
                }
		Image image = ImageUtils.loadImage(imageURL);
		if (image == null) {
                    throw new IllegalArgumentException();
		}
		BufferedImage bufImg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) bufImg.getGraphics();
		graphics.drawImage(image, 0, 0, null);
		this.originalImage = bufImg;
		this.tileWidth = originalImage.getWidth(this);
		this.tileHeight = originalImage.getHeight(this);
		
		this.createPartialPreview();
		this.repaint();
	}

	public URL getImageURL() {
		return this.imageURL;
	}
	
	public Image getImage() {
		return this.originalImage;
	}
	
	private void createPartialPreview() {
		if (this.originalImage == null)
			return;
		
		BufferedImage tmpImg = null;
		
		int rows = originalImage.getHeight(this) / this.tileHeight;
		int cols = originalImage.getWidth(this) / this.tileWidth;
		
		if (rows == 1 && cols == 1) {
			this.preview = ImageUtils.getScaledImage(this.originalImage, this.getWidth(), this.getHeight());
			return;
		}
		
		else if (rows == 1 && cols != 1) {
			int maxWidth = (this.getWidth() - TILE_GAP) / 2;
			int maxHeight = this.getHeight();
			Image left = ImageUtils.getScaledImage(this.originalImage.getSubimage(0, 0, this.tileWidth, this.tileHeight), 
					maxWidth, maxHeight);
			Image right = ImageUtils.getScaledImage(this.originalImage.getSubimage(this.tileWidth, 0, this.tileWidth, this.tileHeight),
					maxWidth, maxHeight);
			
			tmpImg = new BufferedImage(left.getWidth(this) * 2 + TILE_GAP, left.getHeight(this), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = tmpImg.createGraphics();
			g.drawImage(left, 0, 0, this);
			g.setColor(Color.WHITE);
			g.fillRect(left.getWidth(this), 0, TILE_GAP, left.getHeight(this) * 2 + TILE_GAP);
			g.setColor(Color.BLACK);
			g.fillRect(left.getWidth(this) + TILE_GAP/4, 0, TILE_GAP/2, left.getHeight(this) * 2 + TILE_GAP);
			g.drawImage(right, left.getWidth(this) + TILE_GAP, 0, this);
			
		}
		else if (rows != 1 && cols == 1) {
			int maxWidth = this.getWidth();
			int maxHeight = (this.getHeight() - TILE_GAP) / 2;
			Image top = ImageUtils.getScaledImage(this.originalImage.getSubimage(0, 0, this.tileWidth, this.tileHeight), 
					maxWidth, maxHeight);
			Image bottom = ImageUtils.getScaledImage(this.originalImage.getSubimage(0, this.tileHeight, this.tileWidth, this.tileHeight),
					maxWidth, maxHeight);
			
			tmpImg = new BufferedImage(top.getWidth(this) , top.getHeight(this) * 2 + TILE_GAP, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = tmpImg.createGraphics();
			g.drawImage(top, 0, 0, this);
			g.setColor(Color.WHITE);
			g.fillRect(0, top.getHeight(this), top.getWidth(this) * 2 + TILE_GAP, TILE_GAP);
			g.setColor(Color.BLACK);
			g.fillRect(0, top.getHeight(this) + TILE_GAP/4, top.getWidth(this) * 2 + TILE_GAP, TILE_GAP/2);
			g.drawImage(bottom, 0, top.getHeight(this) + TILE_GAP, this);
		}
		else {
			int maxWidth = (this.getWidth() - TILE_GAP) / 2;
			int maxHeight = (this.getHeight() - TILE_GAP) / 2;
			Image topLeft = ImageUtils.getScaledImage(this.originalImage.getSubimage(0, 0, this.tileWidth, this.tileHeight), 
					maxWidth, maxHeight);
			Image topRight = ImageUtils.getScaledImage(this.originalImage.getSubimage(this.tileWidth, 0, this.tileWidth, this.tileHeight),
					maxWidth, maxHeight);
			Image bottomLeft = ImageUtils.getScaledImage(this.originalImage.getSubimage(0, this.tileHeight, this.tileWidth, this.tileHeight),
					maxWidth, maxHeight);
			Image bottomRight = ImageUtils.getScaledImage(this.originalImage.getSubimage(this.tileWidth, this.tileHeight, this.tileWidth, this.tileHeight),
					maxWidth, maxHeight);
			
			tmpImg = new BufferedImage(topLeft.getWidth(this) * 2 + TILE_GAP , topLeft.getHeight(this) * 2 + TILE_GAP, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = tmpImg.createGraphics();
			g.drawImage(topLeft, 0, 0, this);
			g.drawImage(topRight, topLeft.getWidth(this) + TILE_GAP, 0, this);
			g.setColor(Color.WHITE);
			g.fillRect(topLeft.getWidth(this), 0, TILE_GAP, topLeft.getHeight(this) * 2 + TILE_GAP);
			g.fillRect(0, topLeft.getHeight(this), topLeft.getWidth(this) * 2 + TILE_GAP, TILE_GAP);
			g.setColor(Color.BLACK);
			g.fillRect(topLeft.getWidth(this) + TILE_GAP/4, 0, TILE_GAP/2, topLeft.getHeight(this) * 2 + TILE_GAP);
			g.fillRect(0, topLeft.getHeight(this) + TILE_GAP/4, topLeft.getWidth(this) * 2 + TILE_GAP, TILE_GAP/2);
			g.drawImage(bottomLeft, 0, topLeft.getHeight(this) + TILE_GAP, this);
			g.drawImage(bottomRight, topLeft.getWidth(this) + TILE_GAP, topLeft.getHeight(this) + TILE_GAP, this);
		}
		this.preview = tmpImg;
	}
	
	public void setTileWidth(int width) {
		if (DEBUG) System.out.println("setting tile width to: " + width); // NOI18N
		this.tileWidth = width;
		this.createPartialPreview();
		this.repaint();
	}
	
	public void setTileHeight(int height) {
		if (DEBUG) System.out.println("setting tile height to: " + height); // NOI18N
		this.tileHeight = height;
		this.createPartialPreview();
		this.repaint();
	}
	
	
        @Override
	public void paintComponent(Graphics g) {
		//int rounding = 20;
		//g.setColor(Color.BLACK);
		//g.drawRoundRect(0, 0, getWidth(), getHeight(), rounding, rounding);
		if (this.originalImage != null) {
			if (DEBUG) System.out.println("about to draw image preview: " + this.preview); // NOI18N
			//this.createPreview();
			int offX = (this.getWidth() - this.preview.getWidth(this)) / 2;
			int offY = (this.getHeight() - this.preview.getHeight(this)) / 2;
                        g.clearRect(0, 0, this.getWidth(), this.getWidth());
			g.drawImage(this.preview, offX, offY, this);
		}
	}

    public int getTileWidth() {
        return this.tileWidth;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }
	
}
