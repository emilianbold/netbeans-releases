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

package org.netbeans.jemmy.drivers.lists;

import java.awt.event.KeyEvent;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.MultiSelListDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ListOperator;

/**
 * List driver for java.awt.List component type.
 * Uses API calls.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class ListAPIDriver extends LightSupportiveDriver implements MultiSelListDriver {

    /**
     * Constructs a ListAPIDriver.
     */
    public ListAPIDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.ListOperator"});
    }

    public void selectItem(ComponentOperator oper, int index) {
	ListOperator loper = (ListOperator)oper;
	clearSelection(loper);
	loper.select(index);
    }

    public void selectItems(ComponentOperator oper, int[] indices) {
	ListOperator loper = (ListOperator)oper;
	clearSelection(loper);
	for(int i = 0; i < indices.length; i++) {
	    loper.select(indices[i]);
	}
    }

    private void clearSelection(ListOperator loper) {
	for(int i = 0; i < loper.getItemCount(); i++) {
	    loper.deselect(i);
	}
    }
}
