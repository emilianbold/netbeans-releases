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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.openide.util.NbBundle;

/**
 *
 * @author Josh Sandusky
 */
public class WaitMessageHandler {

    private static Widget getBuildMessageWidget(CasaModelGraphScene scene) {
        for (Widget child : scene.getDragLayer().getChildren()) {
            if (child instanceof WaitMessageWidget) {
                return child;
            }
        }
        return null;
    }
    
    public static void addToScene(CasaModelGraphScene scene) {
        if (getBuildMessageWidget(scene) == null) {
            WaitMessageWidget messageWidget = new WaitMessageWidget(scene);
            scene.getDragLayer().addChild(messageWidget);
            scene.validate();
        } 
    }
    
    public static void removeFromScene(CasaModelGraphScene scene) {
        Widget messageWidget = getBuildMessageWidget(scene);
        if (messageWidget != null) {
            scene.getDragLayer().removeChild(messageWidget);
            scene.validate();
        }
    }
    
    private static class WaitMessageWidget extends LabelWidget {
        
        private static final Font FONT_MESSAGE = 
                new Font("SansSerif", Font.BOLD, 18);
        
        private static final Border BORDER = 
                BorderFactory.createCompositeBorder(
                BorderFactory.createLineBorder(2, Color.WHITE),
                BorderFactory.createRoundedBorder(8, 8, null, Color.LIGHT_GRAY),
                BorderFactory.createLineBorder(4, 8, 4, 8, Color.WHITE));
        
        private DependenciesRegistry mDependenciesRegistry;
        
        public WaitMessageWidget(Scene scene) {
            super(scene);
            setBorder(BORDER);
            setFont(FONT_MESSAGE);
            setLabel(NbBundle.getMessage(WaitMessageHandler.class, "LBL_WaitMessage"));
            setForeground(Color.GRAY);
            setBackground(Color.WHITE);
            setOpaque(true);
        }
        
        protected void notifyAdded() {
            super.notifyAdded();
            
            mDependenciesRegistry = new DependenciesRegistry(getParentWidget());
            
            center();
            Widget.Dependency centerer = new Widget.Dependency() {
                public void revalidateDependency() {
                    center();
                }
            };
            getRegistry().registerDependency(centerer);
        }
        
        private void center() {
            Rectangle bounds = getPreferredBounds();
            CasaModelGraphScene scene = (CasaModelGraphScene) getScene();
            Rectangle layerBounds = scene.getView().getVisibleRect();
            if (
                    getParentWidget() == scene.getDragLayer() &&
                    bounds != null &&
                    layerBounds != null) {
                Point location = new Point(
                        layerBounds.x + (layerBounds.width - bounds.width) / 2,
                        layerBounds.y + (layerBounds.height - bounds.height) / 2);
                setPreferredLocation(location);
            }
        }
        
        protected void notifyRemoved() {
            super.notifyRemoved();
            
            if (getRegistry() != null) {
                getRegistry().removeAllDependencies();
            }
            mDependenciesRegistry = null;
        }
        
        private DependenciesRegistry getRegistry() {
            return mDependenciesRegistry;
        }
    }
}
