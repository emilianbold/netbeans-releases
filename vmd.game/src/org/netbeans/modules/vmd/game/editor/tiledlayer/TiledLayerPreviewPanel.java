/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When d1istributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 */package org.netbeans.modules.vmd.game.editor.tiledlayer;

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
    private boolean autoUpdate;

    public TiledLayerPreviewPanel(TiledLayer tl, boolean autoUpdate) {
        this.tiledLayer = tl;
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                TiledLayerPreviewPanel.this.repaint();
            }
        });
        this.setAutoUpdate(autoUpdate);
    }
	
    public void setAutoUpdate(boolean autoUpdate) {
        if (autoUpdate == this.autoUpdate) {
            return;
        }
        if (autoUpdate) {
            this.tiledLayer.addTiledLayerListener(this);
        } else {
            this.tiledLayer.removeTiledLayerListener(this);
        }
        this.autoUpdate = autoUpdate;
    }
	
    public boolean isAutoUpdate() {
        return this.autoUpdate;
    }

    public void refresh() {
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        double ratioW = (double) this.getWidth() / (double) this.tiledLayer.getWidth();
        double ratioH = (double) this.getHeight() / (double) this.tiledLayer.getHeight();
        double ratio = Math.min(ratioW, ratioH);
		
        //center
        double x = 0;
        double y = 0;
        if (ratio == ratioW) {
            double newHeight = this.tiledLayer.getHeight() * ratio;
            y = (this.getHeight() - newHeight) / 2;
            //System.out.println("new height: " + newHeight + ", offY: " + y);
        }
	else {
            double newWidth = this.tiledLayer.getWidth() * ratio;
            x = (this.getWidth() - newWidth) / 2;
            //System.out.println("new width: " + newWidth + ", offX: " + x);
	}
        g.translate(x, y);
        g.scale(ratio, ratio);
        this.paintCellContents(g);
    }
	
    private void paintCellContents(Graphics2D g) {
        for (int r = 0; r < this.tiledLayer.getRowCount(); r++) {
            for (int c = 0; c < this.tiledLayer.getColumnCount(); c++) {
                Tile tile = this.tiledLayer.getTileAt(r, c);
                // Fix for #144948 - [65cat] NullPointerException at org.netbeans.modules.vmd.game.editor.tiledlayer.TiledLayerPreviewPanel.paintCellContents
                if ( tile != null ){
                    tile.paint(g, c * tile.getWidth(), r * tile.getHeight());
                }
            }
        }
    }

        
    // TiledLayerListener implementation
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
	
    public void tilesStructureChanged(TiledLayer source) {
        this.tiledLayerChangedVisualy();
    }

    private void tiledLayerChangedVisualy() {
        this.refresh();
    }

}
