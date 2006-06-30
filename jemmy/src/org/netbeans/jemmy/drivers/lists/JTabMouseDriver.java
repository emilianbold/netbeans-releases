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
public class JTabMouseDriver extends LightSupportiveDriver implements ListDriver {
    private QueueTool queueTool;
    /**
     * Constructs a JTabMouseDriver.
     */
    public JTabMouseDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JTabbedPaneOperator"});
        queueTool = new QueueTool();
    }

    public void selectItem(final ComponentOperator oper, final int index) {
	if(index != -1) {
            queueTool.invokeSmoothly(new QueueTool.QueueAction("Choise expanding") {
                    public Object launch() {
                        Rectangle rect = ((JTabbedPaneOperator)oper).
                            getUI().
                            getTabBounds((JTabbedPane)oper.getSource(),
                                         index);
                        DriverManager.getMouseDriver(oper).
                            clickMouse(oper, 
                                       (int)(rect.getX() + rect.getWidth() / 2),
                                       (int)(rect.getY() + rect.getHeight() / 2),
                                       1, oper.getDefaultMouseButton(), 0,
                                       oper.getTimeouts().create("ComponentOperator.MouseClickTimeout")); 
                        return(null);
                    }
                });
	}
    }
}
