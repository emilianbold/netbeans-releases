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

import javax.swing.tree.TreePath;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.operators.ComponentOperator;

public interface TreeDriver extends MultiSelListDriver {
    public void expandItem(ComponentOperator oper, int index);
    public void collapseItem(ComponentOperator oper, int index);
    public void editItem(ComponentOperator oper, int index, Object newValue, Timeout waitEditorTime);
    public void startEditing(ComponentOperator oper, int index, Timeout waitEditorTime);
}
