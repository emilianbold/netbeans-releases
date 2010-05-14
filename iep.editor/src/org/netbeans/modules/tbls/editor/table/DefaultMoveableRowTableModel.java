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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.tbls.editor.table;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.netbeans.modules.iep.editor.ps.GUIUtil;

/**
 * Concrete implementation of interface MoveableRowTable. This is the default
 * TableModel for use with MoveableRowTable that allow users to drag selected
 * rows to new location within the table
 *
 * @author Bing Lu
 *
 * @since July 8, 2002
 */
public class DefaultMoveableRowTableModel
    extends DefaultTableModel
    implements MoveableRowTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = -6467642233798433503L;

    /**
     * Constructs a default <code>DefaultMoveableRowTableModel</code> which is
     * a table of zero columns and zero rows.
     */
    public DefaultMoveableRowTableModel() {
        super();
    }

    /**
     * Constructs a <code>DefaultMoveableRowTableModel</code> and initializes
     * the table by passing <code>data</code> and <code>columnNames</code> to
     * the <code>setDataVector</code> method.
     *
     * @param data the data of the table
     * @param columnNames <code>vector</code> containing the names of the new
     *        columns
     *
     * @see #getDataVector
     * @see #setDataVector
     */
    public DefaultMoveableRowTableModel(Vector data, Vector columnNames) {
        super(data, columnNames);
    }

    /**
     * Constructs a <code>DefaultMoveableRowTableModel</code> and initializes
     * the table by passing <code>data</code> and <code>columnNames</code> to
     * the <code>setDataVector</code> method. The first index in the
     * <code>Object[][]</code> array is the row index and the second is the
     * column index.
     *
     * @param data the data of the table
     * @param columnNames the names of the columns
     *
     * @see #getDataVector
     * @see #setDataVector
     */
    public DefaultMoveableRowTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    /**
     * Implements interface MoveableRowTableModel. Simply invoke the moveRow
     * method already implemented by super class DefaultTableModel
     *
     * @param start the starting row index to be moved
     * @param end the ending row index to be moved
     * @param to the destination of the rows to be moved
     */
    @Override
    public void moveRow(int start, int end, int to) {
        super.moveRow(start, end, to);
    }
    
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
