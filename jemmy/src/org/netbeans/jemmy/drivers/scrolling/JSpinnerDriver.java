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

package org.netbeans.jemmy.drivers.scrolling;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.ScrollDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JSpinnerOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * A scroll driver serving JSpinner component.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JSpinnerDriver extends LightSupportiveDriver implements ScrollDriver {

    /**
     * Constructs a JSpinnerDriver object.
     */
    public JSpinnerDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JSpinnerOperator"});
    }

    public void scrollToMinimum(final ComponentOperator oper, int orientation) {
        Object minimum = ((JSpinnerOperator)oper).getMinimum();
        if(minimum == null) {
            throw(new JSpinnerOperator.SpinnerModelException("Impossible to get a minimum of JSpinner model.", oper.getSource()));
        }
        scroll(oper, new ScrollAdjuster() {
                public int getScrollOrientation() {
                    return(SwingConstants.VERTICAL);
                }
                public String getDescription() {
                    return("Spin to minimum");
                }
                public int getScrollDirection() {
                    if(((JSpinnerOperator)oper).getModel().getPreviousValue() != null) {
                        return(ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
                    } else {
                        return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
                    }
                }
            });
    }

    public void scrollToMaximum(final ComponentOperator oper, int orientation) {
        Object maximum = ((JSpinnerOperator)oper).getMaximum();
        if(maximum == null) {
            throw(new JSpinnerOperator.SpinnerModelException("Impossible to get a maximum of JSpinner model.", oper.getSource()));
        }
        scroll(oper, new ScrollAdjuster() {
                public int getScrollOrientation() {
                    return(SwingConstants.VERTICAL);
                }
                public String getDescription() {
                    return("Spin to maximum");
                }
                public int getScrollDirection() {
                    if(((JSpinnerOperator)oper).getModel().getNextValue() != null) {
                        return(ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
                    } else {
                        return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
                    }
                }
            });
    }

    public void scroll(ComponentOperator oper, ScrollAdjuster adj) {
        JButtonOperator increaseButton = ((JSpinnerOperator)oper).getIncreaseOperator();
        JButtonOperator decreaseButton = ((JSpinnerOperator)oper).getDecreaseOperator();
        if(adj.getScrollDirection() == ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION) {
            return;
        }
        int originalDirection = adj.getScrollDirection();
        while(adj.getScrollDirection() == originalDirection) {
            if(originalDirection == ScrollAdjuster.INCREASE_SCROLL_DIRECTION) {
                increaseButton.push();
            } else {
                decreaseButton.push();
            }
        }
    }
}
