/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.visualweb.propertyeditors;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A model that defines methods for manipulating a tabular property value. A
 * tabular property is typically a matrix of primitive types (e.g. a list of
 * lists of strings, or a list of objects with any number of properties).
 *
 * @author gjmurphy
 */
public interface TabularPropertyModel extends TableModel {

    /**
     * Return the value of the table contents, as an object of type suitable
     * for the property being edited.
     */
    public Object getValue();

    /**
     * Set the initial value of the table contents.
     */
    public void setValue(Object value);

    /**
     * Adds a listener to the model, which is notified each time a change to the
     * model occurs.
     */
    public void addTableModelListener(TableModelListener listener);

    /**
     * Removes the listener specified.
     */
    public void removeTableModelListener(TableModelListener listener);

    /**
     * Returns the number of columns in the model. This number must not
     * change over the lifetime of the editor.
     *
     * @return The number of columns in the model.
     */
    public int getColumnCount();

    /**
     * Returns the display name of the column with the index specified.
     *
     * @param columnIndex
     * @return The display name of the column with the index specified.
     */
    public String getColumnName(int columnIndex);

    /**
     * Returns the type of the column with the index specified.
     *
     * @param columnIndex
     * @return The type of the column with the index specified.
     */
    public Class getColumnClass(int columnIndex);

    /**
     * Returns the current number of rows. The number may change over the
     * lifetime of the editor.
     *
     * @return The current number of rows.
     */
    public int getRowCount();

    /**
     * Returns the value of the cell at the row and column specified.
     *
     * @param rowIndex
     * @param columnIndex
     * @return The value of the cell at the row and column specified.
     */
    public Object getValueAt(int rowIndex, int columnIndex);

    /**
     * Returns <code>true</code> if the cell specified is editable.
     *
     * @param rowIndex
     * @param columnIndex
     * @return <code>true</code> if the cell specified is editable.
     */
    public boolean isCellEditable(int rowIndex, int columnIndex);

    /**
     * Change the value of the cell specified.
     *
     * @param newValue
     * @param rowIndex
     * @param columnIndex
     * @return <code>true</code> if the cell specified was modified.
     */
    public void setValueAt(Object newValue, int rowIndex, int columnIndex);

    /**
     * Returns true if rows may be added to this table model.
     *
     * @return <code>true</code> if rows may be added to this table model.
     */
    public boolean canAddRow();

    /**
     * Add a row to this model.
     *
     * @return <code>true</code> if a row was added to this table model.
     */
    public boolean addRow();

    /**
     * Returns <code>true</code> if the row specified may be moved to the
     * new location specified. Rows should be shift down one to make room
     * for the new row.
     */
    public boolean canMoveRow(int indexFrom, int indexTo);

    public boolean moveRow(int indexFrom, int indexTo);

    /**
     * Returns <code>true</code> if the row specified may be removed. Remaining
     * rows should be shifted up one.
     */
    public boolean canRemoveRow(int index);

    public boolean removeRow(int index);

    public boolean removeAllRows();
}
