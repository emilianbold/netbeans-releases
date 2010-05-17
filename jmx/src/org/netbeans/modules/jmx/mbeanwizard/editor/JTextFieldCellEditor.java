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
import org.netbeans.modules.jmx.mbeanwizard.listener.TableRemoveListener;
import org.netbeans.modules.jmx.mbeanwizard.table.AttributeTable;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationTable;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.DefaultCellEditor;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.Component;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
        
/**
 * Class implementing the behaviour for the editor of a Text field
 * 
 */
public class JTextFieldCellEditor extends DefaultCellEditor 
        implements FocusListener, TableRemoveListener {
    
    /*******************************************************************/
    // here, the model is not typed because more than one table uses it
    // i.e we have to call explicitely the model's internal structure
    // via getValueAt and setValueAt
    /********************************************************************/
    
        private JTextField tf;
        private JTable table;
        private TableModel model;
        
        private int editedRow = 0;
        private int editedColumn = 0;
        
        /**
         * Constructor
         * @param tf the textfield
         * @param table the JTable containing the textfield
         */
        public JTextFieldCellEditor(JTextField tf, JTable table) {
            super(tf);
            this.tf = tf;
            this.table = table;
            this.model = table.getModel();
            ((AbstractJMXTableModel)this.model).addTableRemoveListener(this);
            tf.addFocusListener(this);
            super.setClickCountToStart(1);
        }

        /**
         * Overriden method; called eached time the component gets in the 
         * editor mode
         * @param table the JTable in which the component is in
         * @param value the object with the current value
         * @param isSelected boolean indicating whether the component is 
         * selected or not
         * @param row the selected row in the table
         * @param column the selected column in the table
         * @return Component the modified component
         */
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            Object data = model.getValueAt(row,column);
            if(data != null)
                tf.setText(data.toString());
  
            return tf;
        }
        
        /**
         * Method defining the behaviour of the component when he gets 
         * the focus
         * @param e a focusEvent
         */
        public void focusGained(FocusEvent e) {
            tf.selectAll();
            
            editedRow = table.getEditingRow();
            editedColumn = table.getEditingColumn();
        }
        
        /**
         * Method defining the behaviour of the component when he looses 
         * the focus
         * @param e a focusEvent
         */
        public void focusLost(FocusEvent e) {
            if (editedRow < ((AbstractJMXTableModel)table.getModel()).size()) 
                model.setValueAt(tf.getText(), editedRow, editedColumn);
            
            if (table instanceof AttributeTable) {
                ((AttributeTable)table).getWiz().event();
            } else {
                if (table instanceof OperationTable) {
                    ((OperationTable)table).getWiz().event();
                }
            }
            
        }
        
        /**
         * Method which sets the model right after a Table Model Event has 
         * been thrown
         * @param e the table model event
         */
        public void tableStateChanged(TableModelEvent e) {
            if (e.getFirstRow() < ((AbstractJMXTableModel)
                        table.getModel()).size()) {
                if (e.getColumn() != -1)
                    tf.setText(model.getValueAt(e.getFirstRow(),
                            e.getColumn()).toString());
            }
        }
}
