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

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.ListDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;

import org.netbeans.jemmy.util.EmptyVisualizer;

/**
 * List driver for javax.swing.JCompoBox component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JComboMouseDriver extends LightSupportiveDriver implements ListDriver {

    /**
     * Constructs a JComboMouseDriver.
     */
    public JComboMouseDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JComboBoxOperator"});
    }

    public void selectItem(ComponentOperator oper, int index) {
	JComboBoxOperator coper = (JComboBoxOperator)oper;
	if(!coper.isPopupVisible()) {
	    DriverManager.getButtonDriver(coper.getButton()).
		push(coper.getButton());
	}
	JListOperator list = new JListOperator(coper.waitList());
        list.copyEnvironment(coper);
        list.setVisualizer(new EmptyVisualizer());
	coper.getTimeouts().sleep("JComboBoxOperator.BeforeSelectingTimeout");
	DriverManager.getListDriver(list).
	    selectItem(list, index);
    }
}
