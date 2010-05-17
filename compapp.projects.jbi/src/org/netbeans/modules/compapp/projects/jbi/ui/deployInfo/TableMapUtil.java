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

package org.netbeans.modules.compapp.projects.jbi.ui.deployInfo;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;


/**
 * In a chain of data manipulators some behaviour is common. TableMap provides most of this
 * behavour and can be subclassed by filters that only need to override a handful of specific
 * methods. TableMap implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting a TableMap which has not
 * been subclassed into a chain of table filters should have no effect.
 *
 * @author Philip Milne
 * @version 1.4 12/17/97
 */
public class TableMapUtil extends AbstractTableModel implements TableModelListener {
    /** table model to use */
    protected TableModel model;

    /**
     * DOCUMENT ME!
     *
     * @return TableModel model
     */
    public TableModel getModel() {
        return model;
    }

    /**
     * DOCUMENT ME!
     *
     * @param model TableModel to use
     */
    public void setModel(TableModel model) {
        this.model = model;
        model.addTableModelListener(this);
    }

    // By default, implement TableModel by forwarding all messages
    // to the model.

    /**
     * DOCUMENT ME!
     *
     * @param aRow row
     * @param aColumn column
     *
     * @return object value at row, column position
     */
    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn);
    }

    /**
     * DOCUMENT ME!
     *
     * @param aValue value to set
     * @param aRow row
     * @param aColumn column
     */
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn);
    }

    /**
     * DOCUMENT ME!
     *
     * @return int row count
     */
    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount();
    }

    /**
     * DOCUMENT ME!
     *
     * @return int column count
     */
    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount();
    }

    /**
     * DOCUMENT ME!
     *
     * @param aColumn aColumn
     *
     * @return String name of this column
     */
    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn);
    }

    /**
     * DOCUMENT ME!
     *
     * @param aColumn aColumn
     *
     * @return Class  class for this column
     */
    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn);
    }

    /**
     * DOCUMENT ME!
     *
     * @param row row
     * @param column column
     *
     * @return boolean true if this cell is editable; otherwise false
     */
    public boolean isCellEditable(int row, int column) {
        return model.isCellEditable(row, column);
    }

    //
    // Implementation of the TableModelListener interface,
    //
    // By default forward all events to all the listeners.

    /**
     * DOCUMENT ME!
     *
     * @param e TableModelEvent
     */
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }
}
