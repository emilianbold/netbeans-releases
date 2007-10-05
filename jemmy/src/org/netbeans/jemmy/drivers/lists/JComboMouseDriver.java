/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
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
