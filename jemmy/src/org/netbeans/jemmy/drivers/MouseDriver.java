/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.operators.ComponentOperator;

public interface MouseDriver extends Driver {
    public void pressMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers);
    public void releaseMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers);
    public void clickMouse(ComponentOperator oper, int x, int y, int clickCount, int mouseButton, 
			   int modifiers, Timeout mouseClick);
    public void moveMouse(ComponentOperator oper, int x, int y);
    public void dragMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers);
    public void dragNDrop(ComponentOperator oper, int start_x, int start_y, int end_x, int end_y, 
			  int mouseButton, int modifiers, Timeout before, Timeout after);
    public void enterMouse(ComponentOperator oper);
    public void exitMouse(ComponentOperator oper);
}
