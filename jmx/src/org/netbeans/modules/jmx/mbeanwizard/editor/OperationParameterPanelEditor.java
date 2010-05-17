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
package org.netbeans.modules.jmx.mbeanwizard.editor;
import javax.swing.table.TableCellEditor;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeEvent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;

import java.awt.Component;
import java.util.List;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanOperationTableModel;


/**
 * Class which handels the behaviour of the panel which popups the parameter
 * window
 *
 */
public class OperationParameterPanelEditor implements TableCellEditor {
    
    /*******************************************************************/
    // here, the model is not typed because more than one table uses it
    // i.e we have to call explicitely the model's internal structure
    // via getValueAt and setValueAt
    /********************************************************************/
    
    private JPanel thisPanel;
    private JTextField text;
    private int editingRow = 0;
    private MBeanOperationTableModel model = null;
    
    protected EventListenerList listenerList = new EventListenerList();
    protected ChangeEvent changeEvent = new ChangeEvent(this);
    
    /**
     * Constructor
     * @param panel the panel containing the textfield and the popup button
     * @param jTextField the textfield
     * @param model the table model of the operation table
     * @param editingRow the current edited row in the table
     */
    public OperationParameterPanelEditor(MBeanOperationTableModel model, 
            JPanel panel, JTextField jTextField,
            int editingRow) {
        this.thisPanel = panel;
        this.text = jTextField;
        this.editingRow = editingRow;
        this.model = model;
    }
    
    /**
     * Overriden method; called eached time the component gets in the editor
     * mode
     * @param table the JTable in which the component is in
     * @param value the object with the current value
     * @param isSelected boolean indicating whether the component is selected 
     * or not
     * @param row the selected row in the table
     * @param column the selected column in the table
     * @return Component the modified component
     */
    public Component getTableCellEditorComponent(JTable table, Object value, 
            boolean isSelected,
            int row, int column) {
        List<MBeanOperationParameter> oText = 
                (List<MBeanOperationParameter>) table.getModel().getValueAt(row, column);
        String paramString = "";// NOI18N
        for (int i = 0; i < oText.size(); i++) {
            paramString += oText.get(i).getParamType() + " " + // NOI18N
                    oText.get(i).getParamName();
            
            if (i < oText.size()-1)
                paramString += ",";// NOI18N
        }
        text.setText(paramString);
        
        return thisPanel;
    }
    
    /**
     * Adds a listener to the listener list
     * @param listener a CellEditorListener
     */
    public void addCellEditorListener(CellEditorListener listener) {
        listenerList.add(CellEditorListener.class,listener);
    }
    
    /**
     * Removes a listener from the listener list
     * @param listener a CellEditorListener
     */
    public void removeCellEditorListener(CellEditorListener listener) {
        listenerList.remove(CellEditorListener.class, listener);
    }
    
    protected void fireEditingStopped() {
        CellEditorListener listener;
        Object[] listeners = listenerList.getListenerList();
        for (int i=0;i< listeners.length;i++) {
            if (listeners[i] == CellEditorListener.class) {
                listener = (CellEditorListener) listeners[i+1];
                listener.editingStopped(changeEvent);
            }
        }
    }
    
    protected void fireEditingCanceled() {
        CellEditorListener listener;
        Object[] listeners = listenerList.getListenerList();
        for (int i=0;i< listeners.length;i++) {
            if (listeners[i] == CellEditorListener.class) {
                listener = (CellEditorListener) listeners[i+1];
                listener.editingCanceled(changeEvent);
            }
        }
    }
    
    public void cancelCellEditing() {
        fireEditingCanceled();
    }
    
    public boolean stopCellEditing() {
        cancelCellEditing();
        return true;
    }
    
    public boolean isCellEditable(java.util.EventObject event) {
        return true;
    }
    
    public boolean shouldSelectCell(java.util.EventObject event) {
        return true;
    }
    
    public Object getCellEditorValue() {
        return model.getOperation(editingRow).getParametersList();
    }
    
}
