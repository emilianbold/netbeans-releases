/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.util;

import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator.ComponentVisualizer;

import java.awt.Component;

/**
 *
 * Being used bas visualizer does nothing.
 * 
 * @see org.netbeans.jemmy.operators.Operator#setVisualizer(Operator.ComponentVisualizer)
 * @see org.netbeans.jemmy.operators.Operator.ComponentVisualizer
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 */
public class EmptyVisualizer implements ComponentVisualizer {

    public void makeVisible(ComponentOperator compOper) {
    }
}
