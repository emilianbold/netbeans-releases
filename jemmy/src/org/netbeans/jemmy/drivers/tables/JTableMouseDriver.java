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

package org.netbeans.jemmy.drivers.tables;

import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.KeyEvent;

import javax.swing.text.JTextComponent;

import org.netbeans.jemmy.QueueTool;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.TableDriver;
import org.netbeans.jemmy.drivers.TextDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * TableDriver for javax.swing.JTableDriver component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JTableMouseDriver extends LightSupportiveDriver implements TableDriver {
    QueueTool queueTool;

    /**
     * Constructs a JTableMouseDriver.
     */
    public JTableMouseDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JTableOperator"});;
	queueTool = new QueueTool();
    }

    public void selectCell(ComponentOperator oper, int row, int column) {
	clickOnCell((JTableOperator)oper, row, column, 1);
    }

    public void editCell(ComponentOperator oper, int row, int column, Object value) {
	JTableOperator toper = (JTableOperator)oper;
	toper.scrollToCell(row, column);
	if(!toper.isEditing() ||
	   toper.getEditingRow() != row ||
	   toper.getEditingColumn() != column) {
	    clickOnCell((JTableOperator)oper, row, column, 2);
	}
	JTextComponentOperator textoper = 
	    new JTextComponentOperator((JTextComponent)toper.
				       waitSubComponent(new JTextComponentOperator.
							JTextComponentFinder()));
	TextDriver text = DriverManager.getTextDriver(JTextComponentOperator.class);
	text.clearText(textoper);
	text.typeText(textoper, value.toString(), 0);
	DriverManager.getKeyDriver(oper).
	    pushKey(textoper, KeyEvent.VK_ENTER, 0,
		    oper.getTimeouts().
		    create("ComponentOperator.PushKeyTimeout"));
    }

    /**
     * Clicks on JTable cell.
     * @param oper Table operator.
     * @param row Cell row index.
     * @param column Cell column index.
     * @param clickCount Count to click.
     */
    protected void clickOnCell(final JTableOperator oper, final int row, final int column, final int clickCount) {
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                public Object launch() {
                    Point point = oper.getPointToClick(row, column);
                    DriverManager.getMouseDriver(oper).
                        clickMouse(oper, point.x, point.y, clickCount, 
                                   oper.getDefaultMouseButton(), 
                                   0, 
                                   oper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
                    return(null);
                }
            });
    }
}

