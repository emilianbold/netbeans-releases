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



package org.netbeans.jemmy.drivers.input;



import java.awt.Component;



import java.awt.event.MouseEvent;



import org.netbeans.jemmy.Timeout;



import org.netbeans.jemmy.drivers.MouseDriver;



import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.jemmy.operators.Operator;



/**

 * MouseDriver using event dispatching.

 *

 * @author Alexandre Iline(alexandre.iline@sun.com)

 */

public class MouseEventDriver extends EventDriver implements MouseDriver {



    /**

     * Constructs a MouseEventDriver object.

     * @param supported an array of supported class names

     */

    public MouseEventDriver(String[] supported) {

	super(supported);

    }



    /**

     * Constructs a MouseEventDriver object.

     */

    public MouseEventDriver() {

	super();

    }



    public void pressMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_PRESSED, 

		      modifiers, x, y, 1,

		      mouseButton);

    }



    public void releaseMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_RELEASED, 

		      modifiers, x, y, 1,

		      mouseButton);

    }



    public void moveMouse(ComponentOperator oper, int x, int y) {

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_MOVED, 

		      0, x, y, 0,

		      Operator.getDefaultMouseButton());

    }



    public void clickMouse(ComponentOperator oper, int x, int y, int clickCount, int mouseButton, 

			   int modifiers, Timeout mouseClick) {

	moveMouse(oper, x, y);

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_ENTERED, 

		      0, x, y, 0,

		      Operator.getDefaultMouseButton());

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_PRESSED, 

		      modifiers, x, y, 1,

		      mouseButton);

	for(int i = 1; i < clickCount; i++) {

	    dispatchEvent(oper.getSource(),

			  MouseEvent.MOUSE_RELEASED, 

			  modifiers, x, y, i,

			  mouseButton);

	    dispatchEvent(oper.getSource(),

			  MouseEvent.MOUSE_CLICKED, 

			  modifiers, x, y, i,

			  mouseButton);

	    dispatchEvent(oper.getSource(),

			  MouseEvent.MOUSE_PRESSED, 

			  modifiers, x, y, i + 1,

			  mouseButton);

	}

	mouseClick.sleep();

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_RELEASED, 

		      modifiers, x, y, clickCount,

		      mouseButton);

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_CLICKED, 

		      modifiers, x, y, clickCount,

		      mouseButton);

	exitMouse(oper);

    }



    public void dragMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_DRAGGED, 

		      modifiers, x, y, 1,

		      mouseButton);

    }



    public void dragNDrop(ComponentOperator oper, int start_x, int start_y, int end_x, int end_y, 

			  int mouseButton, int modifiers, Timeout before, Timeout after) {

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_ENTERED, 

		      0, start_x, start_y, 0,

		      Operator.getDefaultMouseButton());

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_PRESSED, 

		      modifiers, start_x, start_y, 1,

		      mouseButton);

	before.sleep();

	dragMouse(oper, end_x,  end_y, mouseButton, modifiers);

	after.sleep();

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_RELEASED, 

		      modifiers, end_x, end_y, 1,

		      mouseButton);

	exitMouse(oper);

    }



    public void enterMouse(ComponentOperator oper) {

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_ENTERED, 

		      0, oper.getCenterX(), oper.getCenterY(), 0,

		      Operator.getDefaultMouseButton());

    }



    public void exitMouse(ComponentOperator oper) {

	dispatchEvent(oper.getSource(),

		      MouseEvent.MOUSE_EXITED, 

		      0, oper.getCenterX(), oper.getCenterY(), 0,

		      Operator.getDefaultMouseButton());

    }



    /**

     * Dispatches a mouse event to the component.

     * @param comp Component to dispatch events to.

     * @param id an event id.

     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.

     * @param x relative x coordinate of event point

     * @param y relative y coordinate of event point

     * @param clickCount click count

     * @param mouseButton  mouse button.

     */

    protected void dispatchEvent(Component comp, int id, int modifiers, int x, int y, int clickCount, int mouseButton) {

	dispatchEvent(comp,

		      new MouseEvent(comp, 

				     id, 

				     System.currentTimeMillis(), 

				     modifiers | mouseButton, x, y, clickCount,

				     mouseButton == Operator.getPopupMouseButton() &&

                                     id == MouseEvent.MOUSE_PRESSED));

    }

}

