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
 * TabActionEvent.java
 *
 * Created on March 17, 2004, 3:48 PM
 */

package org.netbeans.swing.tabcontrol.event;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * An action event which may be consumed by a listener.  These are fired by
 * TabControl and TabbedContainer to determine if outside code wants to handle
 * an event, such as clicking the close button (which might be vetoed), or if
 * the control should handle it itself.
 *
 * @author Tim Boudreau
 */
public final class TabActionEvent extends ActionEvent {
    private MouseEvent mouseEvent = null;
    private int tabIndex;

    /**
     * Creates a new instance of TabActionEvent
     */
    public TabActionEvent(Object source, String command, int tabIndex) {
        super(source, ActionEvent.ACTION_PERFORMED, command);
        this.tabIndex = tabIndex;
        consumed = false;
    }

    public TabActionEvent(Object source, String command, int tabIndex,
                          MouseEvent mouseEvent) {
        this(source, command, tabIndex);
        this.mouseEvent = mouseEvent;
        consumed = false;
    }

    /**
     * Consume this event - any changes that should be performed as a result
     * will be done by external code by manipulating the models or other means
     */
    public void consume() {
        consumed = true;
    }

    /**
     * Determine if the event has been consumed
     */
    public boolean isConsumed() {
        return super.isConsumed();
    }

    /**
     * If the action event was triggered by a mouse event, get the mouse event
     * in question
     *
     * @return The mouse event, or null
     */
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public void setSource(Object source) {
        //Skip some native peer silliness in AWTEvent
        this.source = source;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("TabActionEvent:"); //NOI18N
        sb.append ("Tab " + tabIndex + " " + getActionCommand()); //NOI18N
        return sb.toString();
    }

}
