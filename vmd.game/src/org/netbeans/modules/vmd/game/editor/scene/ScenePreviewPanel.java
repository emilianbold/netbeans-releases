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
 * License.  When distributing the software, include this License Header
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
 */package org.netbeans.modules.vmd.game.editor.scene;

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
        } else {
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
