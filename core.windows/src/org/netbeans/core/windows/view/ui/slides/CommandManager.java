/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
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
    
    public CommandManager(SlideBar slideBar) {
        this.slideBar = slideBar;
    }
    
    public void slideIn(int tabIndex) {
        SlideBarDataModel model = slideBar.getModel();
        if (isCompSlided()) {
            if (curSlidedComp == model.getTab(tabIndex).getComponent()) {
                // same button clicked again, treat as slide out only
                slideOut(true);
                return;
            } else {
                // another component requests slide in, so slide out current first
                slideOut(false);
            }
        }
        
        curSlidedIndex = tabIndex;
        curSlidedComp = model.getTab(tabIndex).getComponent();
        curSlideOrientation = model.getOrientation();
        curSlideButton = slideBar.getButton(tabIndex);
        
        SlideOperation operation = new SlideInOperation(
            updateSlidedTabContainer(tabIndex), curSlideButton, new DefaultSlidingFx(), curSlideOrientation, true);
        
        curSlideButton.setSelected(true);

        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_SLIDE_IN, operation));
        
    }

    /** Fires slide out operation. 
     * @param requestsActivation true means restore focus to some other view after
     * slide out, false means no additional reactivation
     */
    public void slideOut(boolean requestsActivation) {
        if (!isCompSlided()) {
            return;
        }
        
        SlideOperation operation = new SlideOutOperation(
            getSlidedTabContainer(), curSlideButton, new DefaultSlidingFx(),
            requestsActivation, curSlideOrientation);
        
        curSlideButton.setSelected(false);
        
        curSlidedComp = null;
        curSlideButton = null;
        curSlideOrientation = -1;
        curSlidedIndex = -1;

        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_SLIDE_OUT, operation));
    }
    
    
    public void slideIntoBar(int tabIndex) {
        // XXX - TBD
        throw new UnsupportedOperationException();
    }
    
    public void slideIntoDesktop(int tabIndex) {
        if (isCompSlided()) {
            // XXX - we should use special SLIDE_INTO_DESKTOP event and effect,
            // not just slide out
            slideOut(false);
        }
        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_DISABLE_AUTO_HIDE, null, tabIndex));
    }
    
    public void showPopup(MouseEvent mouseEvent, int tabIndex) {
        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_POPUP_REQUEST, mouseEvent, tabIndex));
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
        if (TabbedContainer.COMMAND_DISABLE_AUTO_HIDE.equals(e.getActionCommand())) {
            slideIntoDesktop(curSlidedIndex);
        } else {
            // convert event - fix index, local tabbed container index isn't right in slide bar context
            TabActionEvent tae = (TabActionEvent)e;
            TabActionEvent newEvt = new TabActionEvent(
                tae.getSource(), tae.getActionCommand(), curSlidedIndex, tae.getMouseEvent());
            
            postEvent(newEvt);
        }
    }
    
    /************************** non-public stuff **********************/
    
    private TabbedContainer getSlidedTabContainer () {
        if (slidedTabContainer == null) {
            TabDataModel slidedCompModel = new DefaultTabDataModel();
            slidedTabContainer = new TabbedContainer(slidedCompModel, TabbedContainer.TYPE_VIEW, slideBar);
            slidedTabContainer.addActionListener(this);
            registerEscHandler(slidedTabContainer);
        }
        return slidedTabContainer;
    }
    
    private TabbedContainer updateSlidedTabContainer(int tabIndex) {
        TabbedContainer container = getSlidedTabContainer();
        TabDataModel containerModel = container.getModel();
        SlideBarDataModel dataModel = slideBar.getModel();
        if (containerModel.size() == 0) {
            containerModel.addTab(0, dataModel.getTab(tabIndex));
        } else {
            containerModel.setTab(0, dataModel.getTab(tabIndex));
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

    /** Synchronizes its state with current state of data model. 
     * Removes currently slided component if it is no longer present in the model.
     */
    void syncWithModel() {
        if ((curSlidedComp != null) && !slideBar.containsComp(curSlidedComp)) {
            // TBD - here should be closeSlide operation, which means
            // just remove from desktop
            slideOut(false);
        }
    }

    /** Actually performs sliding related event by sending it to the 
     * winsys through Tabbed instance
     */
    private void postEvent(ActionEvent evt) {
        ((TabbedSlideAdapter)slideBar.getTabbed()).postActionEvent(evt);
    }
    
    private final Action escapeAction = new EscapeAction();
    
    private final class EscapeAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            slideOut(true);
        }
    } // end of EscapeAction
    
}
