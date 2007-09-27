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


package org.netbeans.modules.iep.editor.tcg.table;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

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
    public void moveRow(int start, int end, int to) {
        super.moveRow(start, end, to);
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
