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
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.Utilities;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphUtilities;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBuildTask;
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
    
    public static void addToScene(CasaModelGraphScene scene, JbiBuildTask task) {
        if (getBuildMessageWidget(scene) == null) {
            WaitMessageWidget messageWidget = new WaitMessageWidget(
                    scene,
                    NbBundle.getMessage(WaitMessageHandler.class, "LBL_WaitMessage3"));
            messageWidget.setTask(task);
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
                BorderFactory.createLineBorder(2, new Color(220, 220, 200)),
                // round-rect line
                BorderFactory.createRoundedBorder(8, 8, null, Color.LIGHT_GRAY),
                // inner border
                BorderFactory.createLineBorder(4, 8, 4, 8, new Color(255, 255, 255, 200)));
        
        private DependenciesRegistry mDependenciesRegistry;
        private String[] mAnimationText;
        private boolean mIsLockPosition;
        
        private Runnable mCurrentAnimator;
        private JbiBuildTask mBuildTask;
        
        
        /**
         * Creates a new wait message widget with the given initial label.
         * If animation text is set, for best results, the label passed into
         * the constructor should be as long or longer than the longest
         * animation text - otherwise the widget will change its size
         * to accomodate longer text, which is not as visibly pleasing.
         */
        public WaitMessageWidget(Scene scene, String label) {
            super(scene);
            setLabel(label);
            setBorder(BORDER);
            setFont(FONT_MESSAGE);
            setForeground(Color.DARK_GRAY);
            setBackground(new Color(255, 255, 255, 200));
            setOpaque(true);
        }
        
        /**
         * Sets the animation text.
         * The animation must be set before the widget is added to a parent.
         */
        public void setAnimationText(String... text) {
            mAnimationText = text;
            if (getParentWidget() != null) {
                // This method must be called before the widget is
                // added to a parent.
                throw new IllegalStateException();
            }
        }
        
        public void setTask(JbiBuildTask task) {
            mBuildTask = task;
        }
        
        protected void notifyAdded() {
            super.notifyAdded();
            
            mDependenciesRegistry = new DependenciesRegistry(getParentWidget());
            
            setMinimumSize(getPreferredBounds().getSize());
            
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
                    private boolean isForcingCleanup = false;
                    private int mForcedCleanupDelay = 0;
                    public void run() {
                        do {
                            checkTaskFinished();
                            try {
                                mForcedCleanupDelay += 800;
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
                    private void checkTaskFinished() {
                        if (
                                !isForcingCleanup &&
                                mBuildTask != null &&
                                mBuildTask.isFinished()) {
                            // The standard task listener should fire a completion
                            // notification, but sometimes this does not happen.
                            // We give the task listener a few seconds but if it
                            // does not fire then we force the cleanup.
                            isForcingCleanup = true;
                            mForcedCleanupDelay = 0;
                        } else if (isForcingCleanup && mForcedCleanupDelay >= 5000) {
                            if (mCurrentAnimator == this) {
                                CasaModelGraphUtilities.setSceneEnabled(
                                        (CasaModelGraphScene) getScene(),
                                        true);
                            }
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
                Point location = Utilities.center(bounds, layerBounds);
                setPreferredLocation(location);
            }
        }
        
        private DependenciesRegistry getRegistry() {
            return mDependenciesRegistry;
        }
    }
}
