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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaPinWidget;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;

/**
 * Modified version of javax.swing.ToolTipManager
 *
 * @author (ConnectionHintManager) Josh Sandusky
 * @author (ToolTipManager) Dave Moore
 * @author (ToolTipManager) Rich Schiavi
 */
public class ConnectionHintManager {
    
    private final static ConnectionHintManager sharedInstance = new ConnectionHintManager();
    
    private Timer enterTimer, exitTimer, insideTimer;
    private String toolTipText;
    private boolean showImmediately;
    
    private transient Popup tipWindow;
    private JToolTip tip;
    
    private Rectangle popupRect = null;
    private boolean enabled = true;
    private boolean tipShowing = false;
    
    private CasaPinWidget mSourceWidget;
    private CasaPinWidget mTargetWidget;
    
    
    ConnectionHintManager() {
        enterTimer = new Timer(750, new insideTimerAction());
        enterTimer.setRepeats(false);
        exitTimer = new Timer(500, new outsideTimerAction());
        exitTimer.setRepeats(false);
        insideTimer = new Timer(4000, new stillInsideTimerAction());
        insideTimer.setRepeats(false);
    }
    
    
    /**
     * Enables or disables the tooltip.
     *
     * @param flag  true to enable the tip, false otherwise
     */
    public void setEnabled(boolean flag) {
        enabled = flag;
        if (!flag) {
            hideTipWindow();
        }
    }
    
    /**
     * Returns true if this object is enabled.
     *
     * @return true if this object is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    
    /**
     * Specifies the initial delay value.
     *
     * @param milliseconds  the number of milliseconds to delay
     *        (after the cursor has paused) before displaying the
     *        tooltip
     * @see #getInitialDelay
     */
    public void setInitialDelay(int milliseconds) {
        enterTimer.setInitialDelay(milliseconds);
    }
    
    /**
     * Returns the initial delay value.
     *
     * @return an integer representing the initial delay value,
     *		in milliseconds
     * @see #setInitialDelay
     */
    public int getInitialDelay() {
        return enterTimer.getInitialDelay();
    }
    
    /**
     * Specifies the dismissal delay value.
     *
     * @param milliseconds  the number of milliseconds to delay
     *        before taking away the tooltip
     * @see #getDismissDelay
     */
    public void setDismissDelay(int milliseconds) {
        insideTimer.setInitialDelay(milliseconds);
    }
    
    /**
     * Returns the dismissal delay value.
     *
     * @return an integer representing the dismissal delay value,
     *		in milliseconds
     * @see #setDismissDelay
     */
    public int getDismissDelay() {
        return insideTimer.getInitialDelay();
    }
    
    /**
     * Used to specify the amount of time before the user has to wait
     * <code>initialDelay</code> milliseconds before a tooltip will be
     * shown. That is, if the tooltip is hidden, and the user moves into
     * a region of the same Component that has a valid tooltip within
     * <code>milliseconds</code> milliseconds the tooltip will immediately
     * be shown. Otherwise, if the user moves into a region with a valid
     * tooltip after <code>milliseconds</code> milliseconds, the user
     * will have to wait an additional <code>initialDelay</code>
     * milliseconds before the tooltip is shown again.
     *
     * @param milliseconds time in milliseconds
     * @see #getReshowDelay
     */
    public void setReshowDelay(int milliseconds) {
        exitTimer.setInitialDelay(milliseconds);
    }
    
    /**
     * Returns the reshow delay property.
     *
     * @return reshown delay property
     * @see #setReshowDelay
     */
    public int getReshowDelay() {
        return exitTimer.getInitialDelay();
    }
    
    void showTipWindow() {
        if (mTargetWidget == null || !mTargetWidget.isVisible())
            return;
        
        if (enabled) {
            JComponent sceneComponent = mTargetWidget.getScene().getView();
            Point sceneComponentLocation = sceneComponent.getLocationOnScreen();
            
            Dimension size;
            Point screenLocation = mTargetWidget.getParentWidget().convertLocalToScene(mTargetWidget.getLocation());
            screenLocation.x += sceneComponentLocation.x;
            screenLocation.y += sceneComponentLocation.y;
            Point location = new Point();
            Rectangle sBounds = sceneComponent.getGraphicsConfiguration().getBounds();
            
            // Just to be paranoid
            hideTipWindow();
            
            tip = new JToolTip();
            tip.setComponent(mTargetWidget.getScene().getView());
            tip.setTipText(toolTipText);
            size = tip.getPreferredSize();
            
            location.x = screenLocation.x;
            location.y = screenLocation.y;
            
            // we do not adjust x/y when using awt.Window tips
            if (popupRect == null){
                popupRect = new Rectangle();
            }
            popupRect.setBounds(
                    location.x,
                    location.y,
                    size.width,
                    size.height);
            
            // Fit as much of the tooltip on screen as possible
            if (location.x < sBounds.x) {
                location.x = sBounds.x;
            } else if (location.x - sBounds.x + size.width > sBounds.width) {
                location.x = sBounds.x + Math.max(0, sBounds.width - size.width);
            }
            if (location.y < sBounds.y) {
                location.y = sBounds.y;
            } else if (location.y - sBounds.y + size.height > sBounds.height) {
                location.y = sBounds.y + Math.max(0, sBounds.height - size.height);
            }
            
            PopupFactory popupFactory = PopupFactory.getSharedInstance();
            
            
            tipWindow = popupFactory.getPopup(
                    mTargetWidget.getScene().getView(),
                    tip,
                    location.x,
                    location.y);
            
            tipWindow.show();
            
            insideTimer.start();
            tipShowing = true;
        }
    }
    
    void hideTipWindow() {
        if (tipWindow != null) {
            tipWindow.hide();
            tipWindow = null;
            tipShowing = false;
            (tip.getUI()).uninstallUI(tip);
            tip = null;
            insideTimer.stop();
        }
    }
    
    /**
     * Returns a shared <code>ToolTipManager</code> instance.
     *
     * @return a shared <code>ToolTipManager</code> object
     */
    public static ConnectionHintManager sharedInstance() {
        return sharedInstance;
    }
    
    /**
     *  This determines whether the tool tip should be shown.
     *
     *  @param event  the event in question
     */
    public void widgetEntered(CasaPinWidget sourceWidget, CasaPinWidget targetWidget) {
        initiateToolTip(sourceWidget, targetWidget);
    }
    
    private void initiateToolTip(CasaPinWidget sourceWidget, CasaPinWidget targetWidget) {
        exitTimer.stop();
        
        if (mTargetWidget != null) {
            enterTimer.stop();
        }
        
        boolean sameComponent = (mTargetWidget == targetWidget);
        
        mSourceWidget = sourceWidget;
        mTargetWidget = targetWidget;
        if (tipWindow != null){
            if (showImmediately) {
                String newToolTipText = getHint();
                if (
                        !sameComponent ||
                        !toolTipText.equals(newToolTipText)) {
                    toolTipText = newToolTipText;
                    showTipWindow();
                }
            } else {
                enterTimer.start();
            }
        }
    }
    
    /**
     *  Any tool tip showing should be hidden.
     *
     *  @param event  the event in question
     */
    public void widgetExited() {
        cleanup();
        exitTimer.restart();
    }
    
    public void cleanup() {
        enterTimer.stop();
        mSourceWidget = null;
        mTargetWidget = null;
        toolTipText = null;
        hideTipWindow();
        exitTimer.stop();
        showImmediately = false;
    }
    
    /**
     *  Determines whether the tool tip should be displayed.
     *
     *  @param event  the event in question
     */
    public void widgetMovedOver(CasaPinWidget sourceWidget, CasaPinWidget targetWidget) {
        if (tipShowing) {
            checkForTipChange(targetWidget);
        } else if (showImmediately) {
            toolTipText = getHint();
            if (toolTipText != null) {
                mSourceWidget = sourceWidget;
                mTargetWidget = targetWidget;
                exitTimer.stop();
                showTipWindow();
            }
        } else {
            // Lazily lookup the values from within insideTimerAction
            mSourceWidget = sourceWidget;
            mTargetWidget = targetWidget;
            toolTipText = null;
            enterTimer.restart();
        }
    }
    
    /**
     * Checks to see if the tooltip needs to be changed in response to
     * the MouseMoved event <code>event</code>.
     */
    private void checkForTipChange(Widget widget) {
        String newText = getHint();
        
        if (newText != null) {
            if (newText.equals(toolTipText)) {
                if (tipWindow != null) {
                    insideTimer.restart();
                } else {
                    enterTimer.restart();
                }
            } else {
                toolTipText = newText;
                if (showImmediately) {
                    hideTipWindow();
                    showTipWindow();
                    exitTimer.stop();
                } else {
                    enterTimer.restart();
                }
            }
        } else {
            toolTipText = null;
            mSourceWidget = null;
            mTargetWidget = null;
            hideTipWindow();
            enterTimer.stop();
            exitTimer.restart();
        }
    }
    
    private String getHint() {
        if (mSourceWidget != null && mTargetWidget != null) {
            CasaModelGraphScene scene = (CasaModelGraphScene) mSourceWidget.getScene();
            CasaEndpointRef sourceRef = (CasaEndpointRef) scene.findObject(mSourceWidget);
            CasaEndpointRef targetRef = (CasaEndpointRef) scene.findObject(mTargetWidget);
            return scene.getModel().getUnConnectableReason(sourceRef, targetRef);
        }
        return null;
    }
    
    
    
    protected class insideTimerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (mTargetWidget != null && mTargetWidget.isVisible()) {
                // Lazy lookup
                if (toolTipText == null) {
                    toolTipText = getHint();
                }
                if (toolTipText != null) {
                    showImmediately = true;
                    showTipWindow();
                } else {
                    mSourceWidget = null;
                    mTargetWidget = null;
                    toolTipText = null;
                    hideTipWindow();
                }
            }
        }
    }
    
    
    protected class outsideTimerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            showImmediately = false;
        }
    }
    
    
    protected class stillInsideTimerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            hideTipWindow();
            enterTimer.stop();
            showImmediately = false;
            mSourceWidget = null;
            mTargetWidget = null;
        }
    }
}
