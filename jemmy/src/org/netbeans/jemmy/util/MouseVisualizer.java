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

package org.netbeans.jemmy.util;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.drivers.DriverManager;

import org.netbeans.jemmy.drivers.input.MouseRobotDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;
import org.netbeans.jemmy.operators.JScrollPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.Operator.ComponentVisualizer;
import org.netbeans.jemmy.operators.WindowOperator;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Point;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 *
 * Does <code>super.activate(org.netbeans.jemmy.operators.WindowOperator)</code>.
 * Then, if java version is appropriate (1.3 or later) activates windows by robot mouse click on border.
 * 
 * @see org.netbeans.jemmy.operators.Operator#setVisualizer(Operator.ComponentVisualizer)
 * @see org.netbeans.jemmy.operators.Operator.ComponentVisualizer
 *
 * <BR><BR>Timeouts used: <BR>
 * MouseVisualiser.BeforeClickTimeout - time to let a window manager to move a window as it wants<BR>
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 */
public class MouseVisualizer extends DefaultVisualizer {

    private static final long BEFORE_CLICK = 100;

    /**
     * A constant used to inform that window activating click 
     * needs to performed on the <b>top</b> side of frame.
     * @see #MouseVisualizer()
     */
    public static int TOP = 0;

    /**
     * A constant used to inform that window activating click 
     * needs to performed on the <b>botton</b> side of frame.
     * @see #MouseVisualizer()
     */
    public static int BOTTOM = 1;

    /**
     * A constant used to inform that window activating click 
     * needs to performed on the <b>left</b> side of frame.
     * @see #MouseVisualizer()
     */
    public static int LEFT = 2;

    /**
     * A constant used to inform that window activating click 
     * needs to performed on the <b>right</b> side of frame.
     * @see #MouseVisualizer()
     */
    public static int RIGHT = 3;

    private int place = 0;
    private double pointLocation = 0;
    private int depth = 0;
    private boolean checkMouse = false;

    /**
     * Creates a visualizer which clicks on (0, 0) window coords.
     */
    public MouseVisualizer() {
    }

    /**
     * Creates a visualizer which clicks on window boder.
     * In case if <code>place == BOTTOM</code>, for example 
     * clicks on (width * pointLocation, height - depth) coordinates.
     * @param place One of the predefined value: TOP, BOTTOM, LEFT, RIGHT
     * @param pointLocation Proportial coordinates to click.
     * @param depth Distance from the border.
     * @param checkMouse Check if there is any java component under mouse
     */
    public MouseVisualizer(int place, double pointLocation, int depth, boolean checkMouse) {
	this.place = place;
	this.pointLocation = pointLocation;
	this.depth = depth;
	this.checkMouse = checkMouse;
    }

    static {
	Timeouts.initDefault("MouseVisualiser.BeforeClickTimeout", BEFORE_CLICK);
    }

    protected boolean isWindowActive(WindowOperator winOper) {
        return(super.isWindowActive(winOper) &&
               (winOper.getSource() instanceof Frame ||
                winOper.getSource() instanceof Dialog));
    }

    protected void makeWindowActive(WindowOperator winOper) {
        JemmyProperties.getCurrentTimeouts().
            create("MouseVisualiser.BeforeClickTimeout").sleep();
        super.makeWindowActive(winOper);
        if(!System.getProperty("java.version").startsWith("1.2")) {
            Point p = getClickPoint(winOper);
            new MouseRobotDriver(winOper.getTimeouts().create("EventDispatcher.RobotAutoDelay")).
                clickMouse(winOper, p.x, p.y,
                           1, winOper.getDefaultMouseButton(),
                           0, 
                           winOper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
        }
    }

    private Point getClickPoint(WindowOperator win) {
	int x, y;
	if(place == LEFT ||
	   place == RIGHT) {
	    y = ((int)(win.getHeight() * pointLocation - 1));
	    if(place == RIGHT) {
		x = win.getWidth() - 1 - depth;
	    } else {
		x = depth;
	    }
	} else {
	    x = ((int)(win.getWidth() * pointLocation - 1));
	    if(place == BOTTOM) {
		y = win.getHeight() - 1 - depth;
	    } else {
		y = depth;
	    }
	}
	if(x < 0) {
	    x = 0;
	}
	if(x >= win.getWidth()) {
	    x = win.getWidth() - 1;
	}
	if(y < 0) {
	    y = 0;
	}
	if(y >= win.getHeight()) {
	    y = win.getHeight() - 1;
	}
	return(new Point(x, y));
    }
}
