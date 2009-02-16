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
 */

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.core.windows.Constants;
import org.netbeans.swing.tabcontrol.*;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/*
 * Helper class to manage slide operations of asociated slide bar.
 * Handles sliding logic that assures
 * just one component is slided at the time.
 * Uses TabbedContainer to represent and display slide component.
 *
 * @author Dafe Simonek
 */
final class CommandManager implements ActionListener {
   
    /** Asociated slide bar */
    private final SlideBar slideBar;
    /** Local tabbed container used to display slided component */
    private TabbedContainer slidedTabContainer;

    /** Data of slide operation in progress */
    private Component curSlidedComp;
    private SlidingButton curSlideButton;
    private int curSlideOrientation;
    private int curSlidedIndex;
    private ResizeGestureRecognizer recog;
    
    
    public CommandManager(SlideBar slideBar) {
        this.slideBar = slideBar;
        recog = new ResizeGestureRecognizer(this);
    }
   
    ResizeGestureRecognizer getResizer() {
        return recog;
    }
    
    public void slideResize(int delta) {
        if (!isCompSlided()) {
            return;
        }
        SlideOperation op = SlideOperationFactory.createSlideResize(getSlidedTabContainer(), curSlideOrientation);
        Rectangle finish = getSlidedTabContainer().getBounds(null);
        String side = orientation2Side(curSlideOrientation);
        if (Constants.BOTTOM.equals(side)) {
            finish.height = finish.height - delta;
            finish.y = finish.y + delta;
        }
        if (Constants.RIGHT.equals(side)) {
            finish.width = finish.width - delta;
            finish.x = finish.x + delta;
        }
        if (Constants.LEFT.equals(side)) {
            finish.width = finish.width + delta;
        }
        op.setFinishBounds(finish);
        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_SLIDE_RESIZE, op));
        
    }
    
    public void slideIn(int tabIndex) {
        SlideBarDataModel model = slideBar.getModel();
        if (isCompSlided()) {
            if (curSlidedComp != model.getTab(tabIndex).getComponent()) {
                // another component requests slide in, so slide out current first
                slideOut(false, false);
            }
        }
        
        curSlidedIndex = tabIndex;
        curSlidedComp = model.getTab(tabIndex).getComponent();
        curSlideOrientation = model.getOrientation();
        curSlideButton = slideBar.getButton(tabIndex);
        TabbedContainer cont = updateSlidedTabContainer(tabIndex);
        SlideOperation operation = SlideOperationFactory.createSlideIn(
            cont, curSlideOrientation, true, true);
        
        curSlideButton.setSelected(true);

        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_SLIDE_IN, operation));
        recog.attachResizeRecognizer(orientation2Side(curSlideOrientation), cont);
    }
    
    /** Fires slide out operation. 
     * @param requestsActivation true means restore focus to some other view after
     * slide out, false means no additional reactivation
     */
    public void slideOut(boolean requestsActivation, boolean useEffect) {
        if (!isCompSlided()) {
            return;
        }
        
        SlideOperation operation = SlideOperationFactory.createSlideOut(
            getSlidedTabContainer(), curSlideOrientation, useEffect, requestsActivation);
        
        curSlideButton.setSelected(false);
        
        recog.detachResizeRecognizer(orientation2Side(curSlideOrientation), getSlidedTabContainer());
        
        curSlidedComp = null;
        curSlideButton = null;
        curSlideOrientation = -1;
        curSlidedIndex = -1;

        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_SLIDE_OUT, operation));
    }
    
    
    public void slideIntoDesktop(int tabIndex, boolean useEffect) {
        SlideOperation operation = null;
        if (isCompSlided()) {
            operation = SlideOperationFactory.createSlideIntoDesktop(
                getSlidedTabContainer(), curSlideOrientation, useEffect);
        }
        recog.detachResizeRecognizer(orientation2Side(curSlideOrientation), getSlidedTabContainer());
        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_DISABLE_AUTO_HIDE, operation, null, tabIndex));
    }
    
    public void toggleTransparency( int tabIndex ) {
        if( isCompSlided() ) {
            TabbedContainer container = (TabbedContainer)getSlidedComp();
            container.setTransparent( !container.isTransparent() );
        }
    }
    
    public void showPopup(MouseEvent mouseEvent, int tabIndex) {
        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_POPUP_REQUEST, mouseEvent, tabIndex));
    }
    
    protected static String orientation2Side (int orientation) {
        String side = Constants.LEFT; 
        if (orientation == SlideBarDataModel.WEST) {
            side = Constants.LEFT;
        } else if (orientation == SlideBarDataModel.EAST) {
            side = Constants.RIGHT;
        } else if (orientation == SlideBarDataModel.SOUTH) {
            side = Constants.BOTTOM;
        }
        return side;
    }
    
    
    /** Activates or deactivates asociated tabbed container used as
     * sliding component.
     */
    public void setActive(boolean active) {
        getSlidedTabContainer().setActive(active);
    }
    
    /********* implementation of ActionListener **************/
    
    /** Reacts to actions from currently slided tabbed container, forwards
     * received events to tabbed instance, which ensures that 
     * actions are handled in the same way as usual.
     */
    public void actionPerformed(ActionEvent e) {
        if (TabbedContainer.COMMAND_POPUP_REQUEST.equals(e.getActionCommand())) {
            TabActionEvent tae = (TabActionEvent) e;
            if (curSlidedComp != null && curSlidedComp instanceof TopComponent) {
                TopComponent tc = (TopComponent)curSlidedComp;
                Action[] actions = slideBar.getTabbed().getPopupActions(tc.getActions(), curSlidedIndex);
                if (actions == null) {
                    actions = tc.getActions();
                }
                if (actions == null || actions.length == 0 )
                    return;
                
                showPopupMenu(
                    Utilities.actionsToPopup(actions, tc.getLookup()), tae.getMouseEvent().getPoint(), tae.getMouseEvent().getComponent());
                
            }
        } else if (TabbedContainer.COMMAND_DISABLE_AUTO_HIDE.equals(e.getActionCommand())) {
            slideIntoDesktop(curSlidedIndex, true);
        } else if (TabbedContainer.COMMAND_TOGGLE_TRANSPARENCY.equals(e.getActionCommand())) {
            TabActionEvent tae = (TabActionEvent) e;
            toggleTransparency( tae.getTabIndex() );
        } else if (TabbedContainer.COMMAND_MAXIMIZE.equals(e.getActionCommand())) {
            //inform the window system that the slided window changes its maximized status
            postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_MAXIMIZE, null, null, curSlidedIndex));
        } else {
            // convert event - fix index, local tabbed container index isn't right in slide bar context
            TabActionEvent tae = (TabActionEvent)e;
            TabActionEvent newEvt = new TabActionEvent(
                tae.getSource(), tae.getActionCommand(), curSlidedIndex, tae.getMouseEvent());
            
            postEvent(newEvt);
        }
    }
    
    /************************** non-public stuff **********************/

    private Rectangle getScreenCompRect(Component comp) { 
        Rectangle result = new Rectangle(comp.getLocationOnScreen(), comp.getSize());
        
        return result;
    }
    
     private static final boolean NO_POPUP_PLACEMENT_HACK = Boolean.getBoolean("netbeans.popup.no_hack"); // NOI18N
// ##########################     
// copied from TabbedHandler, maybe reuse..
//     

    /** Shows given popup on given coordinations and takes care about the
     * situation when menu can exceed screen limits */
    private static void showPopupMenu (JPopupMenu popup, Point p, Component comp) {
        if (NO_POPUP_PLACEMENT_HACK) {
            popup.show(comp, p.x, p.y);
            return;
        }

        SwingUtilities.convertPointToScreen (p, comp);
        Dimension popupSize = popup.getPreferredSize ();
        Rectangle screenBounds = Utilities.getUsableScreenBounds(comp.getGraphicsConfiguration());

        if (p.x + popupSize.width > screenBounds.x + screenBounds.width) {
            p.x = screenBounds.x + screenBounds.width - popupSize.width;
        }
        if (p.y + popupSize.height > screenBounds.y + screenBounds.height) {
            p.y = screenBounds.y + screenBounds.height - popupSize.height;
        }

        SwingUtilities.convertPointFromScreen (p, comp);
        popup.show(comp, p.x, p.y);
    }    

    
    private TabbedContainer getSlidedTabContainer () {
        if (slidedTabContainer == null) {
            TabDataModel slidedCompModel = new DefaultTabDataModel();
            slidedTabContainer = new TabbedContainer(slidedCompModel, TabbedContainer.TYPE_VIEW, slideBar.createWinsysInfo());
            slidedTabContainer.addActionListener(this);
            Border b = null;
            String side = orientation2Side( slideBar.getModel().getOrientation() );
            b = UIManager.getBorder("floatingBorder-"+side); //NOI18N
            if( b == null )
                b = UIManager.getBorder("floatingBorder"); //NOI18N
            if (b != null) {
                slidedTabContainer.setBorder (b);
            }
            
            registerEscHandler(slidedTabContainer);
        }
        return slidedTabContainer;
    }
    
    private TabbedContainer updateSlidedTabContainer(int tabIndex) {
        TabbedContainer container = getSlidedTabContainer();
        TabDataModel containerModel = container.getModel();
        SlideBarDataModel dataModel = slideBar.getModel();
        // creating new TabData instead of just referencing
        // to be able to compare and track changes between models of slide bar and 
        // slided tabbed container
        TabData origTab = dataModel.getTab(tabIndex);
        TabData newTab = new TabData(origTab.getUserObject(), origTab.getIcon(), 
                            origTab.getText(), origTab.getTooltip());
        if (containerModel.size() == 0) {
            containerModel.addTab(0, newTab);
        } else {
            containerModel.setTab(0, newTab);
        }
        container.getSelectionModel().setSelectedIndex(0);
        return container;
    }
    
    private void registerEscHandler (JComponent comp) {
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "slideOut");
        comp.getActionMap().put("slideOut", escapeAction);
    }
    

/***** dumping info about all registered Esc handlers, could be usable for
 * debugging
    
    private void dumpEscHandlers (JComponent comp) {
        InputMap map = null;
        JComponent curChild = null;
        Component[] children = comp.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof JComponent) {
                curChild =(JComponent)children[i]; 
                dumpItem(curChild);
                dumpEscHandlers(curChild);
            }
        }
    }
    
    private void dumpItem(JComponent comp) {
        dumpInnerItem(comp.getInputMap(JComponent.WHEN_FOCUSED), comp.getActionMap(), comp);
        dumpInnerItem(comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), comp.getActionMap(), comp);
        dumpInnerItem(comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), comp.getActionMap(), comp);
    }
    
    private void dumpInnerItem(InputMap map, ActionMap actionMap, JComponent comp) {
        Object cmdKey = map.get(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        if (cmdKey != null) {
            Action action = actionMap.get(cmdKey);
            if (action.isEnabled()) {
                System.out.println("Enabled command found:");
                System.out.println("component: " + comp);
                System.out.println("command key: " + cmdKey);
                System.out.println("action: " + action);
            } else {
                System.out.println("disabled command " + cmdKey);
            }
        }
    }
 
 **********/
    
    /** @return true if some component is currently slided, it means visible
     * over another components in desktop, false otherwise
     */
    boolean isCompSlided() {
        return curSlidedComp != null;
    }
    
    /* #return Component that is slided into desktop or null if no component is
     * slided currently.
     */
    Component getSlidedComp() {
        if (!isCompSlided()) {
            return null;
        }
        return slidedTabContainer;
    }

    /** Synchronizes its state with current state of data model. 
     * Removes currently slided component if it is no longer present in the model,
     * also keeps text up to date.
     */
    void syncWithModel() {
        if (curSlidedComp == null) {
            return; 
        }
        
        if (!slideBar.containsComp(curSlidedComp)) {
            // TBD - here should be closeSlide operation, which means
            // just remove from desktop
            slideOut(false, false);
        } else {
            // keep title text up to date
            SlideBarDataModel model = slideBar.getModel();
            // #46319 - during close, curSlidedIndex may become out of sync,
            // in which case do nothing
            if (curSlidedIndex < model.size()) {
                String freshText = model.getTab(curSlidedIndex).getText();
                TabDataModel slidedModel = getSlidedTabContainer().getModel();
                String slidedText = slidedModel.getTab(0).getText();
                if (slidedText == null || !slidedText.equals(freshText)) {
                    slidedModel.setText(0, freshText);
                    slideBar.repaint();
                }
            }
        }
    }

    /** Actually performs sliding related event by sending it to the 
     * winsys through Tabbed instance
     */
    private void postEvent(ActionEvent evt) {
        ((TabbedSlideAdapter)slideBar.getTabbed()).postActionEvent(evt);
    }
    
    private final Action escapeAction = new EscapeAction();
    
    private final class EscapeAction extends javax.swing.AbstractAction {
        public void actionPerformed(ActionEvent e) {
            slideOut(true, true);
        }
    } // end of EscapeAction
   
    
}
