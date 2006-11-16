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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.Tabbed;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbed;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.windows.TopComponent;

/*
 * Swing component of slide bar. 
 * Holds and shows set of toggle slide buttons and synchronizes them with 
 * data model.
 *
 * All data manipulation are done indirectly through ascoiated models,
 * Swing AWT hierarchy is just synchronized.
 *
 * @author Dafe Simonek
 */
public final class SlideBar extends Box implements ComplexListDataListener,
    SlideBarController, Tabbed.Accessor, WinsysInfoForTabbed, ChangeListener {
    
    /** Command indicating request for slide in (appear) of sliding component */
    public static final String COMMAND_SLIDE_IN = "slideIn"; //NOI18N
    
    /** Command indicating request for slide out (hide) of sliding component */
    public static final String COMMAND_SLIDE_OUT = "slideOut"; //NOI18N

    public static final String COMMAND_SLIDE_RESIZE = "slideResize"; //NOI18N

    /** Action command indicating that a popup menu should be shown */
    public static final String COMMAND_POPUP_REQUEST = "popup"; //NOI18N

    /** Action command indicating that component is going from auto-hide state to regular */
    public static final String COMMAND_DISABLE_AUTO_HIDE = "disableAutoHide"; //NOI18N

    /** Action command indicating that component is going from regular to maximized size and vice versa */
    public static final String COMMAND_MAXIMIZE = "slideMaximize"; //NOI18N
    
    /** Asociation with Tabbed implementation */
    private final TabbedSlideAdapter tabbed;
    /** Holds all data of slide bar */
    private final SlideBarDataModel dataModel;
    /** Selection info */
    private final SingleSelectionModel selModel;
    /** listener for mouse actions and moves, which trigger slide operations */
    private SlideGestureRecognizer gestureRecognizer;
    /** list of sliding buttons */
    private List<SlidingButton> buttons;
    /** operation handler */
    private CommandManager commandMgr;
    /** true when this slide bar is active in winsys, false otherwise */
    private boolean active = false;
    
    /** Creates a new instance of SlideBarContainer with specified orientation.
     * See SlideBarDataModel for possible orientation values.
     */
    public SlideBar(TabbedSlideAdapter tabbed, SlideBarDataModel dataModel, SingleSelectionModel selModel) {
        super(dataModel.getOrientation() == SlideBarDataModel.SOUTH
                ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS);
        this.tabbed = tabbed;                
        this.dataModel = dataModel;
        this.selModel = selModel;
        commandMgr = new CommandManager(this);
        gestureRecognizer = new SlideGestureRecognizer(this, commandMgr.getResizer());
        buttons = new ArrayList<SlidingButton>(5);
        
        syncWithModel();
        
        dataModel.addComplexListDataListener(this);
        selModel.addChangeListener(this);
    }
    
    public SlideBarDataModel getModel() {
        return dataModel;
    }
    
    public SingleSelectionModel getSelectionModel () {
        return selModel;
    }
    
    /***** reactions to changes in data model, synchronizes AWT hierarchy and display ***/
    
    public void intervalAdded(ListDataEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        int first = e.getIndex0();
        int last = e.getIndex1();
        SlideBarDataModel data = (SlideBarDataModel)e.getSource();
        SlidingButton curButton;
        for (int i = first; i <= last; i++) {
            curButton = new SlidingButton(data.getTab(i), data.getOrientation());
            gestureRecognizer.attachButton(curButton);
            buttons.add(i, curButton);
            add(curButton, i * 2);
            add(createStrut(), i * 2 + 1);
            revalidate();
        }
    }
    
    public void intervalRemoved(ListDataEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        int first = e.getIndex0();
        int last = e.getIndex1();
        SlideBarDataModel data = (SlideBarDataModel)e.getSource();
        SlidingButton curButton = null;
        for (int i = last; i >= first; i--) {
            gestureRecognizer.detachButton((SlidingButton)buttons.get(i));
            buttons.remove(i);
            // have to remove also strut (space) component
            remove(i * 2 + 1);
            remove(i * 2);
        }
    }
    
    public void contentsChanged(ListDataEvent e) {
        syncWithModel();
    }
    
    public void indicesAdded(ComplexListDataEvent e) {
        syncWithModel();
    }
    
    public void indicesChanged(ComplexListDataEvent e) {
        syncWithModel();
    }
    
    public void indicesRemoved(ComplexListDataEvent e) {
        syncWithModel();
    }

    /** Finds button which contains given point and returns button's index
     * valid in asociated dataModel. Or returns -1 if no button contains
     * given point
     */  
    public int tabForCoordinate(int x, int y) {
        Rectangle curBounds = new Rectangle();
        int index = 0;
        for (Iterator iter = buttons.iterator(); iter.hasNext(); index++) {
            ((Component)iter.next()).getBounds(curBounds);
            if (curBounds.contains(x, y)) {
                return index;
            }
        }
        return -1;
    }
    
    int nextTabForCoordinate(int x, int y) {
        Rectangle curBounds = new Rectangle();
        int index = 0;
        Iterator iter = buttons.iterator();
        while (iter.hasNext()) {
            Component comp = (Component)iter.next();
            comp.getBounds(curBounds);
            if (dataModel.getOrientation() == SlideBarDataModel.SOUTH) {
                if (curBounds.x  + (curBounds.width/2) < x) {
                    index = index + 1;
                    continue;
                }
            } else {
                if (curBounds.y  + (curBounds.height/2) < y) {
                    index = index + 1;
                    continue;
                }
            }
            return index;
        }
        return index;
    }
    
    
    /** Implementation of ChangeListener, reacts to selection changes
     * and assures that currently selected component is slided in
     */
    public void stateChanged(ChangeEvent e) {
        int selIndex = selModel.getSelectedIndex();
        
        // notify winsys about selection change
        tabbed.postSelectionEvent();
        // a check to prevent NPE as described in #43605, dafe - is this correct or rather a hack? mkleint
        if (isDisplayable() && isVisible()) {
            // slide in or out
            if (selIndex != -1) {
                commandMgr.slideIn(selIndex);
            } else {
                commandMgr.slideOut(true, true);
            }
        }
    }
    
    
    /********** implementation of SlideBarController *****************/
    
    public void userToggledAutoHide(int tabIndex, boolean enabled) {
        commandMgr.slideIntoDesktop(tabIndex, true);
    }
    
    public void userTriggeredPopup(MouseEvent mouseEvent, Component clickedButton) {
        int index = getButtonIndex(clickedButton);
        commandMgr.showPopup(mouseEvent, index);
    }
    
    private SlidingButton buttonFor (TopComponent tc) {
        int idx = 0;
        for (Iterator i=dataModel.getTabs().iterator(); i.hasNext();) {
            TabData td = (TabData) i.next();
            if (td.getComponent() == tc) {
                break;
            }
            if (!i.hasNext()) {
                idx = -1;
            } else {
                idx++;
            }
        }
        if (idx >= 0 && idx < dataModel.size()) {
            return getButton(idx);
        } else {
            return null;
        }
    }
    
    public void setBlinking (TopComponent tc, boolean val) {
        SlidingButton button = buttonFor (tc);
        if (button != null) {
            button.setBlinking(val);
        }
    }

    /** Triggers slide operation by changing selected index */
    public void userClickedSlidingButton(Component clickedButton) {
        int index = getButtonIndex(clickedButton);
        SlidingButton button = (SlidingButton) buttons.get(index);
        button.setBlinking(false);
        
        if (index != selModel.getSelectedIndex() || !isActive()) {
            TopComponent tc = (TopComponent)dataModel.getTab(index).getComponent();
            if (tc != null) {
                tc.requestActive();
            }
        } else {
            selModel.setSelectedIndex(-1);
        }
    }

    /** Request for automatic slide in from gesture recognizer */
    public boolean userTriggeredAutoSlideIn(Component sourceButton) {
        int index = getButtonIndex(sourceButton);
        if (index < 0) {
            return false;
        }
        SlidingButton button = (SlidingButton) buttons.get(index);
        button.setBlinking(false);
        TopComponent tc = (TopComponent)dataModel.getTab(index).getComponent();
        if (tc == null) {
            return false;
        }
        tc.requestVisible();
        return true;
    }    
    
    /** Request for automatic slide out from gesture recognizer */
    public void userTriggeredAutoSlideOut() {
        selModel.setSelectedIndex(-1);
    }
    
    public Rectangle getTabBounds(int tabIndex) {
        Component button = getButton(tabIndex);
        if (button == null) {
            return null;
        }
        Insets insets = getInsets();
        Point leftTop = new Point(insets.left, insets.top);
        
        Dimension strutPrefSize = createStrut().getPreferredSize();
        if (dataModel.getOrientation() == SlideBarDataModel.SOUTH) {
            // horizontal layout
            for (int i = 0; i < tabIndex; i++) {
                leftTop.x += getButton(i).getPreferredSize().width;
                leftTop.x += strutPrefSize.width;
            }
        } else {
            // vertical layout
            for (int i = 0; i < tabIndex; i++) {
                leftTop.y += getButton(i).getPreferredSize().height;
                leftTop.y += strutPrefSize.height;
            }
        }
        return new Rectangle(leftTop, button.getPreferredSize());
    }
    
    /********* implementation of Tabbed.Accessor **************/
    
    public Tabbed getTabbed () {
        return tabbed;
    }
    
    /********* implementation of WinsysInfoForTabbed **************/
    
    public Object getOrientation(Component comp) {
        if (WindowManagerImpl.getInstance().getEditorAreaState() != Constants.EDITOR_AREA_JOINED) {
            return TabDisplayer.ORIENTATION_INVISIBLE;
        }
        return TabDisplayer.ORIENTATION_CENTER;
    }
    
    public boolean inMaximizedMode(Component comp) {
        return TabbedAdapter.isInMaximizedMode(comp);
    }
    
    
    /*************** non public stuff **************************/
    
    /* #return Component that is slided into desktop or null if no component is
     * slided currently.
     */
    Component getSlidedComp() {
        return commandMgr.getSlidedComp();
    }
    
    void setActive(boolean active) {
        this.active = active;
        commandMgr.setActive(active);
    }
    
    boolean isActive() {
        return active;
    }
    
    boolean isHoveringAllowed() {
        return !isActive() || !commandMgr.isCompSlided();
    }
    
    int getButtonIndex(Component button) {
        return buttons.indexOf(button);
    }
    
    SlidingButton getButton(int index) {
        return (SlidingButton)buttons.get(index);
    }
    
    /** @return true if slide bar contains given component, false otherwise */
    boolean containsComp(Component comp) {
        List tabs = getModel().getTabs();
        TabData curTab = null;
        for (Iterator iter = tabs.iterator(); iter.hasNext(); ) {
            curTab = (TabData)iter.next();
            if (comp.equals(curTab.getComponent())) {
                return true;
            }
        }
        return false;
    }
    
    private Component createStrut () {
        return dataModel.getOrientation() == SlideBarDataModel.SOUTH
            ? createHorizontalStrut(5) : createVerticalStrut(5);
    }
    
    private void syncWithModel () {
        assert SwingUtilities.isEventDispatchThread();
        Set<TabData> blinks = null;
        for (SlidingButton curr: buttons) {
            if (curr.isBlinking()) {
                if (blinks == null) {
                    blinks = new HashSet<TabData>();
                }
                blinks.add (curr.getData());
            }
            gestureRecognizer.detachButton(curr);
        }
        removeAll();
        buttons.clear();
        
        List<TabData> dataList = dataModel.getTabs();
        SlidingButton curButton;
        for (Iterator iter = dataList.iterator(); iter.hasNext(); ) {
            TabData td = (TabData) iter.next();
            curButton = new SlidingButton(td, dataModel.getOrientation());
            if (blinks != null && blinks.contains(td)) {
                curButton.setBlinking(true);
            }
            gestureRecognizer.attachButton(curButton);
            buttons.add(curButton);
            add(curButton);
            add(createStrut());
        }

        commandMgr.syncWithModel();
        // #46488 - add(...) is sometimes not enough for proper repaint, god knows why
        revalidate();
        //#47227 - repaint the bar when removing component from bar.
        //#48318 - repaint when changing name -> can change the width of buttons.
        repaint();
    }
    
}


    /********* Swing standard handling mechanism for asociated UI class - will
      see if we need our own UI class or not */
    
    /** String ID of UI class for slide bar used in UIManager */ 
    //private static final String uiClassID = "SlideBarUI";

    /**
     * Returns the tool bar's current UI.
     * @see #setUI
     */
    /*public SlideBarUI getUI() {
        return (SlideBarUI)ui;
    }*
    
    /**
     * Sets the L&F object that renders this component.
     */
    /*public void setUI(SlideBarUI ui) {
        super.setUI(ui);
    }*/
    
    /**
     * Notification from the <code>UIFactory</code> that the L&F has changed. 
     * Called to replace the UI with the latest version from the 
     * <code>UIFactory</code>.
     *
     * @see JComponent#updateUI
     */
    /*public void updateUI() {
        setUI((SlideBarUI)UIManager.getUI(this));
        invalidate();
    }*/

    /**
     * Returns the name of the L&F class that renders this component.
     */
    /*public String getUIClassID() {
        return uiClassID;
    }*/
