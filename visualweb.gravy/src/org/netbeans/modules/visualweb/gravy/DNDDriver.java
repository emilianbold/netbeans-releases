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
* Contributor(s):
*
* The Original Software is NetBeans. The Initial Developer of the Original
* Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
* Microsystems, Inc. All Rights Reserved.
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
*/

package org.netbeans.modules.visualweb.gravy;

import java.awt.Point;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * This class allows tester to imitate "drag-and-drop" action (the appropriate 
 * sequence of events), as if a mouse is being used in an automated test by hand.
 */
public class DNDDriver {
    MouseRobotDriver mDriver;
    Timeout beforeDragSleep, afterDragSleep;
    
    /** 
     * Creates a new instance of DNDDriver 
     */
    public DNDDriver() {
        mDriver = new MouseRobotDriver(new Timeout("", 10));
        beforeDragSleep = new Timeout("", 100);
        afterDragSleep = new Timeout("", 10);
    }
    
    /**
     * Performs "drag-and-drop" action.
     * @param source an component, which should be moved
     * @param from a point inside the source component, where mouse cursor should be placed 
     * @param target an component, on which the source component should be dropped
     * @param to a point inside the target component, where mouse cursor should be placed finally
     * @param button defines, which mouse button should be used (left, right,...) 
     * @param modifiers defines, which additional keyboard keys (Ctrl, Alt,...) should be used 
     */    
    public void dnd(ComponentOperator source, Point from, ComponentOperator target, Point to, int button, int modifiers) {
        mDriver.moveMouse(source, from.x, from.y);
        mDriver.pressMouse(source, from.x, from.y, button, modifiers);
        beforeDragSleep.sleep();
        mDriver.moveMouse(source, from.x, from.y);
        mDriver.moveMouse(source, from.x + 1, from.y + 1);
        mDriver.moveMouse(target, to.x + 1, to.y + 1);
        mDriver.moveMouse(target, to.x, to.y);
        afterDragSleep.sleep();
        mDriver.releaseMouse(target, to.x, to.y, button, modifiers);
    }
    
    /**
     * Performs "drag-and-drop" action.
     * @param source an component, which should be moved
     * @param from a point inside the source component, where mouse cursor should be placed 
     * @param target an component, on which the source component should be dropped
     * @param to a point inside the target component, where mouse cursor should be placed finally
     */    
    public void dnd(ComponentOperator source, Point from, ComponentOperator target, Point to) {
        dnd(source, from, target, to, Operator.getDefaultMouseButton(), 0);
    }

    /**
     * Performs "drag-and-drop" action.
     * @param source an component, which should be moved
     * @param target an component, on which the source component should be dropped
     */    
    public void dnd(ComponentOperator source, ComponentOperator target) {
        dnd(source, new Point(source.getCenterXForClick(),
        source.getCenterYForClick()), 
        target, new Point(target.getCenterXForClick(),
        target.getCenterYForClick()));
    }
}
