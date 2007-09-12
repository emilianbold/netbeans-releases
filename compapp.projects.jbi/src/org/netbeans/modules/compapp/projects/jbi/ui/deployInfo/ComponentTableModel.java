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

import java.util.List;
import org.openide.ErrorManager;
import javax.swing.table.AbstractTableModel;


/**
 * JBI deployInfo component Table Model
 *
 * @author Tientien Li
 */
public class ComponentTableModel extends AbstractTableModel {
    /** Column labels used */
    private List<String> mColumnNames = null;

    /** Data for this model */
    private List<ComponentObject> mComponentObjects = null;

    /**
     * Constructor for the DependencyModel object
     *
     * @param data row data to populate table model with
     * @param columnNames column titles to populate table model with
     */
    public ComponentTableModel(List<ComponentObject> componentObjects, List<String> columnNames) {
        mComponentObjects = componentObjects;
        mColumnNames = columnNames;
    }

    /**
     * get column count
     *
     * @return int
     */
    public int getColumnCount() {
        return mColumnNames.size();
    }

    /**
     * get row count
     *
     * @return int
     */
    public int getRowCount() {
        return mComponentObjects.size();
    }

    /**
     * return name of column given column index
     *
     * @param col int
     *
     * @return String
     */
    public String getColumnName(int col) {
        return mColumnNames.get(col);
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /**
     * Get the valueAt attribute of the model object
     *
     * @param row row position
     * @param col column position
     *
     * @return The valueAt value
     */
    public Object getValueAt(int row, int col) {
        Object obj = null;

        try {
            if ((getRowCount() > 0) && ((row != -1) && (col != -1))) {
                ComponentObject tableEntry = mComponentObjects.get(row);

                if (tableEntry != null) {
                    // set the right data for each column
                    if (col == 0) {
                        obj = tableEntry.getType();
                    } else if (col == 1) {
                        obj = tableEntry.getName();
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return obj;
    }

    /**
     * reset the data 
     *
     * @param componentObjects row data to set
     */
    public void setData(List<ComponentObject> componentObjects) {
        mComponentObjects = componentObjects;        
        fireTableDataChanged();
    }
}
