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

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Defines how to work with lists allowing items reordering.
 */
public interface OrderedListDriver extends MultiSelListDriver {

    /**
     * Changes item index.
     * @param oper List operator.
     * @param itemIndex Current item index.
     * @param newIndex Ne witem index.
     */
    public void moveItem(ComponentOperator oper, int itemIndex, int newIndex);
}
