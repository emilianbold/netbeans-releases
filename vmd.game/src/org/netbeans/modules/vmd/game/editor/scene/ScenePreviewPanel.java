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
package org.netbeans.modules.vmd.game.editor.scene;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Scene.LayerInfo;
import org.netbeans.modules.vmd.game.model.SceneListener;

/**
 *
 * @author kaja
 */
public class ScenePreviewPanel extends JComponent implements SceneListener {

    private Scene scene;
	private ScenePanel scenePanel;

	public ScenePreviewPanel(Scene scene) {
		this.scene = scene;
		this.scenePanel = new ScenePanel(scene);
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				//System.out.println("ScenePreviewPanel resized - updating preview");
				ScenePreviewPanel.this.repaint();
			}
		});
		this.scene.addSceneListener(this);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		Rectangle bounds = scene.getAllLayersBounds();
		double ratioW = (double) this.getWidth() / bounds.getWidth();
		double ratioH = (double) this.getHeight() / bounds.getHeight();
		double ratio = Math.min(ratioW, ratioH);
		
		//center
		double x = 0;
		double y = 0;
		if (ratio == ratioW) {
			double newHeight = bounds.getHeight() * ratio;
			y = (this.getHeight() - newHeight) / 2;
		}
		else {
			double newWidth = bounds.getWidth() * ratio;
			x = (this.getWidth() - newWidth) / 2;
		}
		g.translate(x, y);
		g.scale(ratio, ratio);
        scenePanel.drawLayers(g);
    }
	
    public void layerAdded(Scene sourceScene, Layer layer, int index) {
        this.sceneChangedVisualy();
    }
	
    public void layerRemoved(Scene sourceScene, Layer layer, LayerInfo info, int index) {
        this.sceneChangedVisualy();
    }
	
    public void layerMoved(Scene sourceScene, Layer layer, int indexOld, int indexNew) {
        this.sceneChangedVisualy();
    }
	
    public void layerPositionChanged(Scene sourceScene, Layer layer, Point oldPosition, Point newPosition, boolean inTransition) {
        if (inTransition) {
			return;
		}
		this.sceneChangedVisualy();
    }
	
    public void layerLockChanged(Scene sourceScene, Layer layer, boolean locked) {
        this.sceneChangedVisualy();
    }
	
    public void layerVisibilityChanged(Scene sourceScene, Layer layer, boolean visible) {
        this.sceneChangedVisualy();
    }
	
	private void sceneChangedVisualy() {
		this.repaint();
	}
	
}
