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

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Defines how to work with tables.
 */
public interface TableDriver {

    /**
     * Selects a cell.
     * @param oper Table operator.
     * @param row Cell row index.
     * @param column Cell column index.
     */
    public void selectCell(ComponentOperator oper, int row, int column);

    /**
     * Edits a cell.
     * @param oper Table operator.
     * @param row Cell row index.
     * @param column Cell column index.
     * @param value New value.
     */
    public void editCell(ComponentOperator oper, int row, int column, Object value);
}

