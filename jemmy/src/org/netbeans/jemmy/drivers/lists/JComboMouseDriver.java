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

import javax.swing.UIManager;

import org.netbeans.jemmy.QueueTool;

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
    QueueTool queueTool;
    public JComboMouseDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JComboBoxOperator"});
        queueTool = new QueueTool();
    }

    public void selectItem(ComponentOperator oper, int index) {
	JComboBoxOperator coper = (JComboBoxOperator)oper;
        //1.5 workaround
        if(System.getProperty("java.specification.version").compareTo("1.4") > 0) {
            queueTool.setOutput(oper.getOutput().createErrorOutput());
            queueTool.waitEmpty(10);
            queueTool.waitEmpty(10);
            queueTool.waitEmpty(10);
        }
        //end of 1.5 workaround
	if(!coper.isPopupVisible()) {
            if(UIManager.getLookAndFeel().getClass().getName().equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel")) {
                oper.clickMouse(oper.getWidth() - 2, oper.getHeight()/2, 1);
            } else {
                DriverManager.getButtonDriver(coper.getButton()).
                    push(coper.getButton());
            }
	}
	JListOperator list = new JListOperator(coper.waitList());
        list.copyEnvironment(coper);
        list.setVisualizer(new EmptyVisualizer());
	coper.getTimeouts().sleep("JComboBoxOperator.BeforeSelectingTimeout");
	DriverManager.getListDriver(list).
	    selectItem(list, index);
    }
}
