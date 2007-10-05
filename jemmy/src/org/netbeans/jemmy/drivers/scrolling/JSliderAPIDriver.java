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

package org.netbeans.jemmy.drivers.scrolling;

import java.awt.Point;

import javax.swing.JSlider;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.MouseDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JSliderOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * A scroll driver serving JSlider component.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JSliderAPIDriver  extends AbstractScrollDriver {
    private final static int SMALL_INCREMENT = 1;
    private QueueTool queueTool;

    /**
     * Constructs a JSliderDriver object.
     */
    public JSliderAPIDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JSliderOperator"});
        queueTool = new QueueTool();
    }

    public void scrollToMinimum(final ComponentOperator oper, int orientation) {
	((JSliderOperator)oper).setValue(((JSliderOperator)oper).getMinimum());
    }

    public void scrollToMaximum(final ComponentOperator oper, int orientation) {
        ((JSliderOperator)oper).setValue(((JSliderOperator)oper).getMaximum());
    }

    protected void step(ComponentOperator oper, ScrollAdjuster adj) {
        JSliderOperator scroll = (JSliderOperator)oper;
        int newValue = -1;
        if(adj.getScrollDirection() == adj.DECREASE_SCROLL_DIRECTION) {
	    newValue = (scroll.getValue() > scroll.getMinimum() +
			getUnitIncrement(scroll)) ?
		scroll.getValue() - getUnitIncrement(scroll) :
		scroll.getMinimum();
        } else if(adj.getScrollDirection() == adj.INCREASE_SCROLL_DIRECTION) {
	    newValue = (scroll.getValue() < scroll.getMaximum() -
			getUnitIncrement(scroll)) ?
		scroll.getValue() + getUnitIncrement(scroll) :
		scroll.getMaximum();
        }
        setValue(oper, newValue);
    }

    private void setValue(ComponentOperator oper, int value) {
        if(value != -1) {
	    ((JSliderOperator)oper).setValue(value);
        }
    }

    protected Timeout getScrollDeltaTimeout(ComponentOperator oper) {
        return(oper.getTimeouts().
               create("JSliderOperator.ScrollingDelta"));
    }

    protected void jump(final ComponentOperator oper, final ScrollAdjuster adj) {
        JSliderOperator scroll = (JSliderOperator)oper;
        int newValue = -1;
        if(adj.getScrollDirection() == adj.DECREASE_SCROLL_DIRECTION) {
	    newValue = (scroll.getValue() > scroll.getMinimum() +
			getBlockIncrement(scroll)) ?
		scroll.getValue() - getBlockIncrement(scroll) :
		scroll.getMinimum();
        } else if(adj.getScrollDirection() == adj.INCREASE_SCROLL_DIRECTION) {
	    newValue = (scroll.getValue() < scroll.getMaximum() -
			getBlockIncrement(scroll)) ?
		scroll.getValue() + getBlockIncrement(scroll) :
		scroll.getMaximum();
        }
        setValue(oper, newValue);
    }

    protected void startPushAndWait(ComponentOperator oper, int direction, int orientation) {
    }

    protected void stopPushAndWait(ComponentOperator oper, int direction, int orientation) {
    }

    protected Point startDragging(ComponentOperator oper) {
        return(null);
    }

    protected void drop(ComponentOperator oper, Point pnt) {
    }

    protected void drag(ComponentOperator oper, Point pnt) {
    }

    protected boolean canDragAndDrop(ComponentOperator oper) {
        return(false);
    }

    protected boolean canJump(ComponentOperator oper) {
        return(isSmallIncrement((JSliderOperator)oper));
    }

    protected boolean canPushAndWait(ComponentOperator oper) {
        return(false);
    }

    protected int getDragAndDropStepLength(ComponentOperator oper) {
        return(1);
    }

    private int getUnitIncrement(JSliderOperator oper) {
	return((oper.getMinorTickSpacing() == 0) ?
	       1 :
	       oper.getMinorTickSpacing());
    }

    private int getBlockIncrement(JSliderOperator oper) {
        return((oper.getMajorTickSpacing() == 0) ?
               1 :
               oper.getMajorTickSpacing());
    }

    private boolean isSmallIncrement(JSliderOperator oper) {
        return(oper.getMajorTickSpacing() <= SMALL_INCREMENT &&
               oper.getMajorTickSpacing() <= SMALL_INCREMENT);
    }

}
