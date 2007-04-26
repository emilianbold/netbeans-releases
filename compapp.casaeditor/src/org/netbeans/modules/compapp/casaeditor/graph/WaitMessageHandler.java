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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;
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
            WaitMessageWidget messageWidget = new WaitMessageWidget(
                    scene,
                    NbBundle.getMessage(WaitMessageHandler.class, "LBL_WaitMessage0"));
            messageWidget.setAnimationText(
                    NbBundle.getMessage(WaitMessageHandler.class, "LBL_WaitMessage1"),
                    NbBundle.getMessage(WaitMessageHandler.class, "LBL_WaitMessage2"),
                    NbBundle.getMessage(WaitMessageHandler.class, "LBL_WaitMessage3"));
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
                new Font("SansSerif", Font.BOLD, 16);
        
        private static final Border BORDER = 
                BorderFactory.createCompositeBorder(
                // outer border
                BorderFactory.createLineBorder(2, Color.LIGHT_GRAY),
                // round-rect line
                BorderFactory.createRoundedBorder(8, 8, null, Color.LIGHT_GRAY),
                // inner border
                BorderFactory.createLineBorder(4, 8, 4, 8, new Color(255, 255, 255, 200)));
        
        private DependenciesRegistry mDependenciesRegistry;
        private String[] mAnimationText;
        private boolean mIsLockPosition;
        
        private Runnable mCurrentAnimator;
        
        
        public WaitMessageWidget(Scene scene, String label) {
            super(scene);
            setLabel(label);
            setBorder(BORDER);
            setFont(FONT_MESSAGE);
            setForeground(Color.DARK_GRAY);
            setBackground(new Color(255, 255, 255, 200));
            setOpaque(true);
        }
        
        public void setAnimationText(String... text) {
            mAnimationText = text;
        }
        
        protected void notifyAdded() {
            super.notifyAdded();
            
            mDependenciesRegistry = new DependenciesRegistry(getParentWidget());
            
            Rectangle preferredBounds = getPreferredBounds();
            setMinimumSize(new Dimension(preferredBounds.width + 20, preferredBounds.height));
            
            center();
            Widget.Dependency centerer = new Widget.Dependency() {
                public void revalidateDependency() {
                    if (!mIsLockPosition) {
                        center();
                    }
                }
            };
            getRegistry().registerDependency(centerer);
            
            startAnimation();
        }
        
        private void startAnimation() {
            if (mAnimationText != null) {
                Runnable animator = new Runnable() {
                    private int mAnimationIndex = 0;
                    public void run() {
                        do {
                            try {
                                Thread.sleep(800);
                                if (mCurrentAnimator == this) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            updateLabel();
                                        }
                                    });
                                }
                            } catch (Throwable t) {
                                t.printStackTrace(System.err);
                            }
                        }
                        while (mCurrentAnimator == this);
                    }
                    private void updateLabel() {
                        try {
                            mIsLockPosition = true;
                            setLabel(mAnimationText[mAnimationIndex]);
                            getScene().revalidate();
                            getScene().validate();
                            mAnimationIndex = (mAnimationIndex + 1) % mAnimationText.length;
                        } finally {
                            mIsLockPosition = false;
                        }
                    }
                };
                mCurrentAnimator = animator;
                new Thread(animator).start();
            }
        }
        
        protected void notifyRemoved() {
            super.notifyRemoved();
            
            mCurrentAnimator = null;
            
            if (getRegistry() != null) {
                getRegistry().removeAllDependencies();
            }
            mDependenciesRegistry = null;
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
        
        private DependenciesRegistry getRegistry() {
            return mDependenciesRegistry;
        }
    }
}
