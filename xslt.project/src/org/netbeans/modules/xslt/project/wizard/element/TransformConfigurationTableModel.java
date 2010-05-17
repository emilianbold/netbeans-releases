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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xslt.project.wizard.element;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.filesystems.FileObject;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformConfigurationTableModel extends AbstractTableModel {

    private String myFileNamePrefix;
    private String myTransformNamePrefix;
    private FileObject myCurrentFolder;
    
    private List<TransformationItem> myDataModel = new ArrayList<TransformationItem>();
    
    
    private static final String[] TRANSFORM_TBL_HEADERS = new String[] {
        i18n(TransformConfigurationTableModel.class, "LBL_InputPartsHeader"), // NOI18N
        i18n(TransformConfigurationTableModel.class, "LBL_XslFilePathHeader"), // NOI18N
        i18n(TransformConfigurationTableModel.class, "LBL_OutputPartsHeader"), // NOI18N
        i18n(TransformConfigurationTableModel.class, "LBL_NameHeader") // NOI18N
    };
    private static final int NUM_COLUMNS = TRANSFORM_TBL_HEADERS.length;
    
    
    public TransformConfigurationTableModel(List<TransformationItem> dataModel) 
    {
        myDataModel = dataModel;
    }
    
    
    public int getRowCount() {
        return myDataModel.size();
    }

    public int getColumnCount() {
        return NUM_COLUMNS;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (myDataModel == null) {
            return null;
        }
        if (rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
            return i18n(TransformConfigurationTableModel.class, "LBL_MissingData"); // NOI18N
        }

        TransformationItem item = myDataModel.get(rowIndex);
        if (item == null) {
            return i18n(TransformConfigurationTableModel.class, "LBL_MissingData"); // NOI18N
        }
        
        if (columnIndex == 0) {
            return item.getInputPartName();
        } else if (columnIndex == 1) {
            return item.getXslFilePath();
        } else if (columnIndex == 2) {
            return item.getOutputPartName();
        } else if (columnIndex == 3) {
            return item.getName();
        }
        
        return i18n(TransformConfigurationTableModel.class, "LBL_MissingData"); // NOI18N
    }

    @Override
    public String getColumnName(int column) {
        return column < NUM_COLUMNS ? TRANSFORM_TBL_HEADERS[column] : super.getColumnName(column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) { 
        if (myDataModel == null || !(aValue instanceof String)) {
            return;
        }
        if (rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
            return; 
        }

        TransformationItem item = myDataModel.get(rowIndex);
        if (item == null) {
            return; 
        }
        
        if (columnIndex == 0) {
            item.setInputPartName((String)aValue);
        } else if (columnIndex == 1) {
            item.setXslFilePath((String)aValue);
        } else if (columnIndex == 2) {
            item.setOutputPartName((String)aValue);
        } else if (columnIndex == 3) {
            item.setName((String)aValue);
        }
        
        fireTableCellUpdated(rowIndex, columnIndex);        
    }
    
}
