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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.mbeanwizard.table;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import java.awt.Dimension;
import javax.swing.ListSelectionModel;

/**
 * Class responsible for the notification type table in the notification popup
 *
 */
public class NotificationTypePopupTable extends JTable {

    /*******************************************************************/
    // here we use raw model calls (i.e getValueAt and setValueAt) to
    // access the model data because the inheritance pattern
    // makes it hard to type these calls and to use the object model
    /********************************************************************/
    
    /**
     * Constructor
     * @param model the table model of this table
     */
    public NotificationTypePopupTable(AbstractTableModel model) {
        super(model);
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(250, 70));
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    /**
     * Returns the cell editor for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellEditor the cell editor
     */
    public TableCellEditor getCellEditor(int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        final JTextField typeField = new JTextField();
        String o = ((String)getModel().getValueAt(row,column));
        typeField.setText(o);
        return new JTextFieldCellEditor(typeField, this);
    }
     
}
