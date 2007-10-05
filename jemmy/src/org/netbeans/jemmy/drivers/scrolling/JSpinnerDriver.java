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
