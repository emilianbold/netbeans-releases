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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.vmd.game.model.ImageUtils;
import org.netbeans.modules.vmd.game.model.Position;

/**
 *
 * 
 */
public class FullImageGridPreview extends AbstractImagePreviewComponent {

	private static final boolean DEBUG = false;
	
	private URL imageURL;
	private BufferedImage originalImage;
	
	private int cellHeight;
	private int cellWidth;
	
	private int gridWidth = 3;
	
    public FullImageGridPreview() {
    }
	
	public void setTileWidth(int width) {
		if (DEBUG) System.out.println("setting tile width to: " + width); // NOI18N
		this.cellWidth = width;
		this.repaint();
	}
	
	public void setTileHeight(int height) {
		if (DEBUG) System.out.println("setting tile height to: " + height); // NOI18N
		this.cellHeight = height;
		this.repaint();
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
		this.cellWidth = originalImage.getWidth(this);
		this.cellHeight = originalImage.getHeight(this);
		
		this.repaint();
	}
	
	public URL getImageURL() {
		return this.imageURL;
	}
	
	public Image getImage() {
		return this.originalImage;
	}
	
        @Override
	public Dimension getPreferredSize() {
		if (imageURL == null) {
			return super.getPreferredSize();
		}
		int cols = this.originalImage.getWidth() / this.cellWidth;
		int rows = this.originalImage.getHeight() / this.cellHeight;
		int width = this.gridWidth + (this.cellWidth + this.gridWidth) * cols;
		int height = this.gridWidth + (this.cellHeight + this.gridWidth) * rows;
		return new Dimension(width, height);
	}
	
    @Override
	public void paintComponent(Graphics g) {
		//tiled layer editor component - paintCells() etc.
		Graphics2D g2d = (Graphics2D) g;
		if (this.originalImage == null) {
			return;
		}
                g2d.clearRect(0, 0, this.getWidth(), this.getWidth());
		this.paintGridLines(g2d);
		this.paintCells(g2d);
	}
	
	void paintCells(Graphics2D g) {
		Rectangle rect = g.getClipBounds();
		if (DEBUG) System.out.println("Paint cell: " + rect); // NOI18N
		Position topLeft = this.getCellAtPoint(rect.getLocation());
		Position bottomRight = this.getCellAtCoordinates(rect.getLocation().x + rect.width, rect.getLocation().y + rect.height);
		if (DEBUG) System.out.println("topLeft: " + topLeft + ", bottomRight: " + bottomRight); // NOI18N
		//rows
		for (int row = topLeft.getRow(); row <= bottomRight.getRow(); row++) {
			//cols
			for (int col = topLeft.getCol(); col <= bottomRight.getCol(); col++) {
				Position cell = new Position(row, col);
				//if (DEBUG) System.out.println("Looking at: " + cell + " compared to " + this.cellHiLited);
				this.paintCellContents(g, cell);				
			}
		}
	}
	
	private void paintCellContents(Graphics2D g, Position cell) {
		Rectangle rect = this.getCellArea(cell);
		int dx = rect.x;
		int dy = rect.y;
		int sx = cell.getCol() * cellWidth;
		int sy = cell.getRow() * cellHeight;
		g.drawImage(originalImage, dx, dy, dx+cellWidth, dy+cellHeight, sx, sy, sx+cellWidth, sy+cellHeight, null);
	}
	
	private Position getCellAtPoint(Point p) {
		return this.getCellAtCoordinates(p.x, p.y);
	}
	private Position getCellAtCoordinates(int x, int y) {
		int row = (y - this.gridWidth) / (this.cellHeight + this.gridWidth);
		int col = (x - this.gridWidth) / (this.cellWidth + this.gridWidth);
		if (x < 0) {
			col--;
		}
		if (y < 0) {
			row--;
		}
		//if (DEBUG) System.out.println("row = " + row + " col = " + col);
		return new Position(row, col);
	}
	
	private Rectangle getCellArea(Position cell) {
		return this.getCellArea(cell.getRow(), cell.getCol());
	}
	private Rectangle getCellArea(int row, int col) {
		Rectangle cellArea = new Rectangle( ( (this.cellWidth + this.gridWidth) * col) + this.gridWidth, ((this.cellHeight + this.gridWidth) * row) + this.gridWidth, this.cellWidth, this.cellHeight);
		return cellArea;
	}
	
	private void paintGridLines(Graphics2D g) {
		for (int horizontal = 0; horizontal < this.getPreferredSize().height; horizontal += (this.cellHeight + this.gridWidth)) {
			g.setColor(Color.WHITE);
			g.fillRect(0, horizontal, this.getPreferredSize().width, this.gridWidth);
		}
		for (int vertical = 0; vertical < this.getPreferredSize().width; vertical += (cellWidth + this.gridWidth)) {
			g.setColor(Color.WHITE);
			g.fillRect(vertical, 0, this.gridWidth, this.getPreferredSize().height);
		}
		for (int horizontal = 0; horizontal < this.getPreferredSize().height; horizontal += (this.cellHeight + this.gridWidth)) {
			g.setColor(Color.BLACK);
			g.drawLine(1, horizontal+1, this.getPreferredSize().width-2, horizontal+1);
		}
		for (int vertical = 0; vertical < this.getPreferredSize().width; vertical += (cellWidth + this.gridWidth)) {
			g.setColor(Color.BLACK);
			g.drawLine(vertical+1, 1, vertical+1, this.getPreferredSize().height-2);
		}
	}

    public int getTileWidth() {
        return this.cellWidth;
    }

    public int getTileHeight() {
        return this.cellHeight;
    }
	
}
