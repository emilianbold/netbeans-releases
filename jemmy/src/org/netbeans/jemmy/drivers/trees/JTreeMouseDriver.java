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

package org.netbeans.jemmy.drivers.trees;

import java.awt.Point;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.text.JTextComponent;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.drivers.PathChooser;
import org.netbeans.jemmy.drivers.TextDriver;
import org.netbeans.jemmy.drivers.TreeDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * TreeDriver for javax.swing.JTree component type.
 * Uses mouse operations.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JTreeMouseDriver extends LightSupportiveDriver implements TreeDriver {
    QueueTool queueTool;
    /**
     * Constructs a JTreeMouseDriver.
     */
    public JTreeMouseDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JTreeOperator"});
	queueTool = new QueueTool();
    }

    public void selectItem(ComponentOperator oper, int index) {
	selectItems(oper, new int[] {index});
    }

    public void selectItems(final ComponentOperator oper, int[] indices) {
        ((JTreeOperator)oper).clearSelection();
        checkSupported(oper);
        final MouseDriver mdriver = DriverManager.getMouseDriver(oper);
        final JTreeOperator toper = (JTreeOperator)oper;
        final Timeout clickTime = oper.getTimeouts().create("ComponentOperator.MouseClickTimeout");
        QueueTool qt = new QueueTool();
        for(int i = 0; i < indices.length; i++) {
            final int index = i;
            if(!queueTool.isDispatchThread()) {
                toper.scrollToRow(indices[i]);
            }
            final Point p = toper.getPointToClick(indices[index]);
            queueTool.invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                    public Object launch() {
                        mdriver.clickMouse(oper, p.x, p.y, 1, Operator.getDefaultMouseButton(),
                                           (index == 0) ? 0 : InputEvent.CTRL_MASK, clickTime);
                        return(null);
                    }
                });
        }
        //1.5 workaround
        if(System.getProperty("java.specification.version").compareTo("1.4") > 0) {
            if(!QueueTool.isDispatchThread()) {
                queueTool.setOutput(oper.getOutput().createErrorOutput());
                queueTool.waitEmpty(10);
                queueTool.waitEmpty(10);
                queueTool.waitEmpty(10);
            }
        }
        //end of 1.5 workaround
    }

    public void expandItem(ComponentOperator oper, final int index) {
	checkSupported(oper);
	final JTreeOperator toper = (JTreeOperator)oper;
	final MouseDriver mdriver = DriverManager.getMouseDriver(oper);
	if(!toper.isExpanded(index)) {
            queueTool.invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                    public Object launch() {
                        Point p = toper.getPointToClick(index);
                        mdriver.clickMouse(toper, p.x, p.y, 2, Operator.getDefaultMouseButton(),
                                           0, toper.getTimeouts().
                                           create("ComponentOperator.MouseClickTimeout"));
                        return(null);
                    }
                });
	}
    }

    public void collapseItem(ComponentOperator oper, final int index) {
	checkSupported(oper);
	final JTreeOperator toper = (JTreeOperator)oper;
	final MouseDriver mdriver = DriverManager.getMouseDriver(oper);
	if(toper.isExpanded(index)) {
            queueTool.invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                    public Object launch() {
                        Point p = toper.getPointToClick(index);
                        mdriver.clickMouse(toper, p.x, p.y, 2, Operator.getDefaultMouseButton(),
                                           0, toper.getTimeouts().
                                           create("ComponentOperator.MouseClickTimeout"));
                        return(null);
                    }
                });
	}
    }

    public void editItem(ComponentOperator oper, int index, Object newValue, Timeout waitEditorTime) {
	JTextComponentOperator textoper = startEditingAndReturnEditor(oper, index, waitEditorTime);
	final TextDriver text = DriverManager.getTextDriver(JTextComponentOperator.class);
	text.clearText(textoper);
	text.typeText(textoper, newValue.toString(), 0);
	DriverManager.getKeyDriver(oper).
	    pushKey(textoper, KeyEvent.VK_ENTER, 0,
		    oper.getTimeouts().
		    create("ComponentOperator.PushKeyTimeout"));
    }

    public void startEditing(ComponentOperator oper, int index, Timeout waitEditorTime) {
	startEditingAndReturnEditor(oper, index, waitEditorTime);
    }

    private JTextComponentOperator startEditingAndReturnEditor(ComponentOperator oper, final int index, Timeout waitEditorTime) {
	checkSupported(oper);
	final JTreeOperator toper = (JTreeOperator)oper;
	final MouseDriver mdriver = DriverManager.getMouseDriver(oper);
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                public Object launch() {
                    Point p = toper.getPointToClick(index);
                    mdriver.clickMouse(toper, p.x, p.y, 1, Operator.getDefaultMouseButton(),
			   0, toper.getTimeouts().
			   create("ComponentOperator.MouseClickTimeout"));
                    return(null);
                }
            });
	oper.getTimeouts().sleep("JTreeOperator.BeforeEditTimeout");
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                public Object launch() {
                    Point p = toper.getPointToClick(index);
                    mdriver.clickMouse(toper, p.x, p.y, 1, Operator.getDefaultMouseButton(),
                                       0, toper.getTimeouts().
                                       create("ComponentOperator.MouseClickTimeout"));
                    return(null);
                }
            });
	toper.getTimeouts().
	    setTimeout("ComponentOperator.WaitComponentTimeout", waitEditorTime.getValue());
	return(new JTextComponentOperator((JTextComponent)toper.
					  waitSubComponent(new JTextComponentOperator.
							   JTextComponentFinder())));
    }
}
