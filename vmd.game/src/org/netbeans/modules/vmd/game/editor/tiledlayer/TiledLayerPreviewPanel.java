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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.game.editor.tiledlayer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.TiledLayerListener;

/**
 *
 * @author kaja
 */
public class TiledLayerPreviewPanel extends JComponent implements TiledLayerListener {

	private TiledLayer tiledLayer;
	
    public TiledLayerPreviewPanel(TiledLayer tl) {
		this.tiledLayer = tl;
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				TiledLayerPreviewPanel.this.repaint();
			}
		});
		this.tiledLayer.addTiledLayerListener(this);
    }
	
    protected void paintComponent(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		//System.out.println("panel width: " + this.getWidth() + ", height: " + this.getHeight());
		//System.out.println("layer width: " + this.tiledLayer.getWidth() + ", height: " + this.tiledLayer.getHeight());
		
		
		double ratioW = (double) this.getWidth() / (double) this.tiledLayer.getWidth();
		double ratioH = (double) this.getHeight() / (double) this.tiledLayer.getHeight();
		double ratio = Math.min(ratioW, ratioH);
		
		//System.out.println("ratioW: " + ratioW + ", ratioH: " + ratioH);
		
		//center
		double x = 0;
		double y = 0;
		if (ratio == ratioW) {
			double newHeight = this.tiledLayer.getHeight() * ratio;
			y = (this.getHeight() - newHeight) / 2;
			System.out.println("new height: " + newHeight + ", offY: " + y);
		}
		else {
			double newWidth = this.tiledLayer.getWidth() * ratio;
			x = (this.getWidth() - newWidth) / 2;
			System.out.println("new width: " + newWidth + ", offX: " + x);
		}
		g.translate(x, y);
		g.scale(ratio, ratio);
        this.paintCellContents(g);
	}
	
	private void paintCellContents(Graphics2D g) {
		for (int r = 0; r < this.tiledLayer.getRowCount(); r++) {
			for (int c = 0; c < this.tiledLayer.getColumnCount(); c++) {
				Tile tile = this.tiledLayer.getTileAt(r, c);
				tile.paint(g, c * tile.getWidth(), r * tile.getHeight());
			}
		}
	}

    public void tileChanged(TiledLayer source, int row, int col) {
        this.tiledLayerChangedVisualy();
    }

    public void tilesChanged(TiledLayer source, Set positions) {
        this.tiledLayerChangedVisualy();
    }

    public void columnsInserted(TiledLayer source, int index, int count) {
        this.tiledLayerChangedVisualy();
    }

    public void columnsRemoved(TiledLayer source, int index, int count) {
        this.tiledLayerChangedVisualy();
    }

    public void rowsInserted(TiledLayer source, int index, int count) {
        this.tiledLayerChangedVisualy();
    }

    public void rowsRemoved(TiledLayer source, int index, int count) {
        this.tiledLayerChangedVisualy();
    }
	
	private void tiledLayerChangedVisualy() {
		this.repaint();
	}
	
}
