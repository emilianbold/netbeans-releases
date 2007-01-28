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
package org.netbeans.modules.vmd.game.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.netbeans.modules.vmd.game.model.ImageUtils;


public class PartialImageGridPreview extends JComponent {

    public static final boolean DEBUG = false;
    
	private static final int MAX_PREVIEW_ROWS = 2;
	private static final int MAX_PREVIEW_COLS = 2;
	
	private static final int TILE_GAP = 4;
	
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
				if (DEBUG) System.out.println("resized - updating preview");
				PartialImageGridPreview.this.createPreview();
				PartialImageGridPreview.this.repaint();
			}
		});
	}
	
	public void setImage(String imageFileName) throws MalformedURLException {
		URL imgUrl = (new File(imageFileName)).toURL();
		this.setImage(imgUrl);
	}

	public void setImage(URL imgUrl) throws MalformedURLException {
		Image image = ImageUtils.loadImage(imgUrl);
		BufferedImage bufImg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) bufImg.getGraphics();
		graphics.drawImage(image, 0, 0, null);
		this.originalImage = bufImg;
		this.tileWidth = originalImage.getWidth(this);
		this.tileHeight = originalImage.getHeight(this);
		this.createPreview();
		this.repaint();
	}


	private void createPreview() {
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
			//ImageUtils.getScaledImage(tmpImg, this.getWidth(), this.getHeight());
	}
	
	public void setTileWidth(int width) {
		if (DEBUG) System.out.println("setting tile width to: " + width);
		this.tileWidth = width;
		this.createPreview();
		this.repaint();
	}
	
	public void setTileHeight(int height) {
		if (DEBUG) System.out.println("setting tile height to: " + height);
		this.tileHeight = height;
		this.createPreview();
		this.repaint();
	}
	
	
	public void paintComponent(Graphics g) {
		//int rounding = 20;
		//g.setColor(Color.BLACK);
		//g.drawRoundRect(0, 0, getWidth(), getHeight(), rounding, rounding);
		if (this.originalImage != null) {
			if (DEBUG) System.out.println("about to draw image preview: " + this.preview);
			//this.createPreview();
			int offX = (this.getWidth() - this.preview.getWidth(this)) / 2;
			int offY = (this.getHeight() - this.preview.getHeight(this)) / 2;
			g.drawImage(this.preview, offX, offY, this);
		}
	}
	
	
	public List<Integer> getValidTileWidths() {
		int imgWidth = this.originalImage.getWidth(null);
		return this.getEvenDivisors(imgWidth);
	}
	
	public List<Integer> getValidTileHeights() {
		int imgHeight = this.originalImage.getHeight(null);
		return this.getEvenDivisors(imgHeight);
	}
	
	private List getEvenDivisors(int number) {
		ArrayList divisors = new ArrayList();
		
		for (int i = 1; i <= number; i++) {
			if (number % i == 0) {
				divisors.add(new Integer(i));
			}
		}
		return divisors;
	}

}
