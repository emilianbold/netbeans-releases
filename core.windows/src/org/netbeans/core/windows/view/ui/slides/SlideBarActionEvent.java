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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author Dafe Simonek
 */
public final class SlideBarActionEvent extends ActionEvent {
    
    private final MouseEvent mouseEvent;
    
    private final int tabIndex;
    
    private final SlideOperation slideOperation;

    public SlideBarActionEvent(Object source, String command, MouseEvent mouseEvent, int tabIndex) {
        this(source, command, null, mouseEvent, tabIndex);
    }
    
    public SlideBarActionEvent(Object source, String command, SlideOperation slideOperation) {
        this(source, command, slideOperation, null, -1);
    }
    
    public SlideBarActionEvent(Object source, String command, SlideOperation operation,
                                MouseEvent mouseEvent, int tabIndex) {
        super(source, ActionEvent.ACTION_PERFORMED, command);
        this.tabIndex = tabIndex;
        this.mouseEvent = mouseEvent;
        this.slideOperation = operation;
    }
    
    
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    public int getTabIndex() {
        return tabIndex;
    }
    
    public SlideOperation getSlideOperation() {
        return slideOperation;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SlideBarActionEvent:"); //NOI18N
        sb.append ("Tab " + tabIndex + " " + getActionCommand()); //NOI18N
        return sb.toString();
    }

}
