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


package org.netbeans.modules.iep.editor.tcg.table;

import javax.swing.table.TableModel;

/**
 * Interface specifying the method needed to move rows in a JTable to a new
 * location
 *
 * @author Bing Lu
 *
 * @since July 8, 2002
 */
public interface MoveableRowTableModel
    extends TableModel {

    /**
     * Moves one or more rows from the inclusive range <code>start</code> to
     * <code>end</code> to the <code>to</code> position in the model. After
     * the move, the row that was at index <code>start</code> will be at index
     * <code>to</code>. This method will send a <code>tableChanged</code>
     * notification message to all the listeners.
     *
     * <p>
     * <pre>
     *  Examples of moves:
     *  <p>
     *
     * 1. moveRow(1,3,5); a|B|C|D|e|f|g|h|i|j|k - before a|e|f|g|h|B|C|D|i|j|k -
     * after <p>
     *
     * 2. moveRow(6,7,1); a|b|c|d|e|f|G|H|i|j|k - before a|G|H|b|c|d|e|f|i|j|k -
     * after <p>
     *
     * </pre>
     * </p>
     *
     * @param start the starting row index to be moved
     * @param end the ending row index to be moved
     * @param to the destination of the rows to be moved
     */
    public void moveRow(int start, int end, int to);
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
