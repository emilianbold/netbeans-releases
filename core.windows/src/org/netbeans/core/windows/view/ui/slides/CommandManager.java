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
import java.awt.event.MouseEvent;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;

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
                slideOut();
                return;
            } else {
                // another component requests slide in, so slide out current first
                slideOut();
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
    
    public void slideOut() {
        if (!isCompSlided()) {
            return;
        }
        
        SlideOperation operation = new SlideOutOperation(
            getSlidedTabContainer(), curSlideButton, new DefaultSlidingFx(), curSlideOrientation);
        
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
            slideOut();
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
    
    /** @return true if some component is currently slided, it means visible
     * over another components in desktop, false otherwise
     */
    boolean isCompSlided() {
        return curSlidedComp != null;
    }

    /** Actually performs sliding related event by sending it to the 
     * winsys through Tabbed instance
     */
    private void postEvent(ActionEvent evt) {
        ((TabbedSlideAdapter)slideBar.getTabbed()).postActionEvent(evt);
    }
    
    
}
