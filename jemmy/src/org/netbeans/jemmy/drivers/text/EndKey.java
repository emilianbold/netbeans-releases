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

package org.netbeans.jemmy.drivers.text;

import org.netbeans.jemmy.operators.ComponentOperator;

class EndKey extends OffsetKey {
    TextKeyboardDriver cont;
    ComponentOperator oper;
    public EndKey(int keyCode, int mods, TextKeyboardDriver cont, ComponentOperator oper) {
	super(keyCode, mods);
	this.cont = cont;
	this.oper = oper;
    }
    public int getDirection() {
	return(1);
    }
    public int getExpectedPosition() {
	return(cont.getText(oper).length());
    }
}
