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

package org.netbeans.jemmy.drivers;

import java.awt.Component;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Defines the way to get a title pane.
 */
public interface InternalFrameDriver {
    /**
     * Returns the title pane component.
     * @param oper operator for an internal frame.
     * @return a component - title pane.
     */
    public Component getTitlePane(ComponentOperator oper);
}
