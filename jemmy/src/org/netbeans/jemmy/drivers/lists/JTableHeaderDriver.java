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

import java.awt.Point;

import java.awt.event.InputEvent;

import org.netbeans.jemmy.QueueTool;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.OrderedListDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTableHeaderOperator;

/**
 * List driver for javax.swing.table.JTableHeader component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JTableHeaderDriver extends LightSupportiveDriver implements OrderedListDriver {
    private QueueTool queueTool;

    /**
     * Constructs a JTableHeaderDriver.
     */
    public JTableHeaderDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JTableHeaderOperator"});
        queueTool = new QueueTool();
    }

    public void selectItem(ComponentOperator oper, int index) {
	clickOnHeader((JTableHeaderOperator)oper, index);
    }

    public void selectItems(ComponentOperator oper, int[] indices) {
	clickOnHeader((JTableHeaderOperator)oper, indices[0]);
	for(int i = 1; i < indices.length; i++) {
	    clickOnHeader((JTableHeaderOperator)oper, indices[i], InputEvent.CTRL_MASK);
	}
    }

    public void moveItem(ComponentOperator oper, int moveColumn, int moveTo) {
        Point start = ((JTableHeaderOperator)oper).getPointToClick(moveColumn);
        Point   end = ((JTableHeaderOperator)oper).getPointToClick(moveTo);
        oper.dragNDrop(start.x, start.y, end.x, end.y);
    }

    /**
     * Clicks on a column header.
     * @param oper an operator to click on.
     * @param index column index.
     */
    protected void clickOnHeader(JTableHeaderOperator oper, int index) {
	clickOnHeader(oper, index, 0);
    }

    /**
     * Clicks on a column header.
     * @param oper an operator to click on.
     * @param index column index.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    protected void clickOnHeader(final JTableHeaderOperator oper, final int index, final int modifiers) {
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Column selecting") {
                public Object launch() {
                    Point toClick = ((JTableHeaderOperator)oper).getPointToClick(index);
                    DriverManager.getMouseDriver(oper).
                        clickMouse(oper, 
                                   toClick.x,
                                   toClick.y,
                                   1, oper.getDefaultMouseButton(), modifiers,
                                   oper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
                    return(null);
                }
            });
    }
}
