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

import java.awt.Rectangle;

import javax.swing.JTabbedPane;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.ListDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

/**
 * List driver for javax.swing.JTabbedPane component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JTabAPIDriver extends LightSupportiveDriver implements ListDriver {
    private QueueTool queueTool;
    /**
     * Constructs a JTabMouseDriver.
     */
    public JTabAPIDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JTabbedPaneOperator"});
        queueTool = new QueueTool();
    }

    public void selectItem(final ComponentOperator oper, final int index) {
	if(index != -1) {
            queueTool.invokeSmoothly(new QueueTool.QueueAction("Choise expanding") {
                    public Object launch() {
                        ((JTabbedPaneOperator)oper).setSelectedIndex(index);
                        return(null);
                    }
                });
	}
    }
}
