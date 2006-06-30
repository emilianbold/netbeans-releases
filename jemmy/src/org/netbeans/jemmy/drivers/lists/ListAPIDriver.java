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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
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
