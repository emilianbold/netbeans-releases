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
/*
 * TabDisplayerUI.java
 *
 * Created on March 16, 2004, 5:55 PM
 */

package org.netbeans.swing.tabcontrol;

import org.netbeans.swing.tabcontrol.event.TabActionEvent;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * The basic UI of a tab displayer component.  Defines the API of the UI for
 * TabDisplayers, which may be called by TabDisplayer.
 *
 * @author Tim Boudreau
 * @see org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI
 * @see org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI
 */
public abstract class TabDisplayerUI extends ComponentUI {
    protected SingleSelectionModel selectionModel = null;
    protected final TabDisplayer displayer;

    /**
     * Creates a new instance of TabDisplayerUI
     */
    protected TabDisplayerUI(TabDisplayer displayer) {
        this.displayer = displayer;
    }


    public void installUI(JComponent c) {
        assert c == displayer;
        selectionModel = displayer.getSelectionModel();
        
        //Will only be non-null if we are in the middle of an L&F change - don't
        //replace it so listeners are not clobbered
        if (selectionModel == null) {
            selectionModel = createSelectionModel();
        }
        
        installSelectionModel();
    }

    public void uninstallUI(JComponent c) {
        assert c == displayer;
    }

    /**
     * Get a shape representing the exact outline of the numbered tab. The
     * implementations in the package will return instances of
     * <code>EqualPolygon</code> from this method; other implementations may
     * return what they want, but for performance reasons, it is highly
     * desirable that the shape object returned honor <code>equals()</code> and
     * <code>hashCode()</code>, as there are significant optimizations in
     * NetBeans' drag and drop support that depend on this.
     */
    public abstract Polygon getExactTabIndication(int index);

    /**
     * Get a shape representing the area of visual feedback during a drag and
     * drop operation, which represents where a tab will be inserted if a drop
     * operation is performed over the indicated tab. <p>The implementations in
     * the package will return instances of <code>EqualPolygon</code> from this
     * method; other implementations may return what they want, but for
     * performance reasons, it is highly desirable that the shape object
     * returned honor <code>equals()</code> and <code>hashCode()</code>, as
     * there are significant optimizations in NetBeans' drag and drop support
     * that depened on this.
     *
     * @return Shape representing feedback shape
     */
    public abstract Polygon getInsertTabIndication(int index);

    /**
     * Returns the index of the tab at the passed point, or -1 if no tab is at
     * that location. Note that this method may return -1 for coordinates which
     * are within a tab as returned by getTabRect(), but are not within the
     * visible shape of the tab as the UI paints it.
     */
    public abstract int tabForCoordinate(Point p);

    /**
     * Configure the passed rectangle with the shape of the tab at the given
     * index.
     */
    public abstract Rectangle getTabRect(int index,
                                         final Rectangle destination);

    /**
     * Returns an image suitable for use in drag and drop operations,
     * representing the tab at this index.  The default implementation returns null.
     *
     * @param index A tab index
     * @throws IllegalArgumentException if no tab is at the passed index
     */
    public Image createImageOfTab(int index) {
        return null;
    }

    /**
     * Create the selection model which will handle selection for the
     * TabDisplayer.  SPI method located here because TabDisplayer.setSelectionModel
     * is package private.
     */
    protected abstract SingleSelectionModel createSelectionModel();

    /**
     * Allows ActionListeners attached to the container to determine if the
     * event should be acted on. Delegates to <code>displayer.postActionEvent()</code>.
     * This method will create a TabActionEvent with the passed string as an 
     * action command, and cause the displayer to fire this event.  It will
     * return true if no listener on the displayer consumed the TabActionEvent;
     * consuming the event is the way a listener can veto a change, or provide
     * special handling for it.
     *
     * @param command The action command - this should be TabDisplayer.COMMAND_SELECT
     *                or TabDisplayer.COMMAND_CLOSE, but private contracts
     *                between custom UIs and components are also an option.
     * @param tab     The index of the tab upon which the action should act, or
     *                -1 if non-applicable
     * @param event   A mouse event which initiated the action, or null
     * @return true if the event posted was not consumed by any listener
     */
    protected final boolean shouldPerformAction(String command, int tab,
                                                MouseEvent event) {
        TabActionEvent evt = new TabActionEvent(displayer, command, tab, event);
        displayer.postActionEvent(evt);
        return !evt.isConsumed();
    }

    /**
     * Instruct the UI to ensure that the tab at the given index is visible.
     * Some UIs allow scrolling or otherwise hiding tabs.  The default
     * implementation is a no-op.
     *
     * @param index The index of the tab that should be made visible, which
     *              should be within the range of 0 to the count of tabs in the
     *              model
     */
    public void makeTabVisible(int index) {
        //do nothing
    }

    /**
     * Installs the selection model into the tab control via a package private
     * method.
     */
    private void installSelectionModel() {
        displayer.setSelectionModel(selectionModel);
    }

    /** Get the command associated with a given point if the default mouse button
     * is used, such as TabDisplayer.COMMAND_SELECT or TabDisplayer.COMMAND_CLOSE.
     * @param p A point
     * @return An action command
     */
    public abstract String getCommandAtPoint(Point p);

    /**
     *  The index a tab would acquire if dropped at a given point
     *
     * @param p A point
     * @return An index which may be equal to the size of the data model
     */
    public abstract int dropIndexOfPoint (Point p);
    
    public abstract void registerShortcuts (JComponent comp);
        
    public abstract void unregisterShortcuts (JComponent comp);
    
    
    protected abstract void requestAttention (int tab);
    
    protected abstract void cancelRequestAttention (int tab);   
    
    
    public String getTooltipForButtons(Point point) {
        return null;
    }
}
