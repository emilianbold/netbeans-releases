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
/*
 * MethodPanel.java
 *
 * Created on March 1, 2005, 5:39 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import org.netbeans.modules.visualweb.ejb.load.EjbLoaderHelper;
import org.netbeans.modules.visualweb.ejb.util.InvalidParameterNameException;
import org.netbeans.modules.visualweb.ejb.util.MethodParamValidator;
import java.net.URLClassLoader;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * This panel displays the information for the given method.
 *
 * @author  cao
 */
public class MethodDetailPanel extends javax.swing.JPanel {
    
    private EjbGroup ejbGroup;
    private MethodInfo methodInfo;
    
    private URLClassLoader classloader;
    
    public MethodDetailPanel( EjbGroup ejbGrp, MethodInfo methodInfo ) {
        initComponents();
        paramTable.setPreferredScrollableViewportSize(paramTable.getPreferredSize());
        
        this.ejbGroup = ejbGrp;
        this.methodInfo = methodInfo;
        
        displayValue();
    }
    
    private void displayValue() {
        if (methodInfo != null) {
            signatureTextArea.setText(methodInfo.toString());

            // the method return type is a collection, then the combo box will be
            // enabled to ask for the element class type.
            if (methodInfo.getReturnType().isCollection()) {
                returnTypeTextField.setText(methodInfo.getReturnType().getClassName());
                classNameTextField.setEditable(true);
                classNameTextField.setText(methodInfo.getReturnType().getElemClassName());
            } else {
                returnTypeTextField.setText(methodInfo.getReturnType().getClassName());
                classNameTextField.setText(null);
                classNameTextField.setEditable(false);
            }
        
            // Init the table with the parameter information
            MethodParamTableModel tableModel = new MethodParamTableModel(methodInfo);
            paramTable.setModel(tableModel);

            paramTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));
        }else {
            paramTable.setModel(new DefaultTableModel());
            paramTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));
            classNameTextField.setText(null);
            returnTypeTextField.setText(null);
            classNameTextField.setEditable(false);
        }
    }
    
    public void updateColElemClassName()
    {
        if (methodInfo == null) return;
        
        String className = classNameTextField.getText();
        if( className != null && className.trim().length() != 0 )
        {
            // Validating
            if( classloader == null )
                classloader = EjbLoaderHelper.getEjbGroupClassLoader( ejbGroup );
            
            // Make sure that the element class specified by the user is a valid one
            try {
                Class c = Class.forName( className, true,  classloader );
            }catch ( java.lang.ClassNotFoundException ce ) {
                NotifyDescriptor d = new NotifyDescriptor.Message( "Class " + className + " not found", /*NbBundle.getMessage(MethodNode.class, "PARAMETER_NAME_NOT_UNIQUE", name ),*/ NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify( d );
                return;
            }
            
            methodInfo.getReturnType().setElemClassName( className.trim() );
        }
        else
            methodInfo.getReturnType().setElemClassName( null );
    }
    
    public void stopLastCellEditing() 
    {    
        // If the table is editing mode, programmatically stop the CellEditor
        if( paramTable.isEditing() )
            paramTable.getCellEditor().stopCellEditing();
    }
    
    public void setMethod( MethodInfo method ) {
        methodInfo = method;
        
        displayValue();
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        returnTypeLabel = new javax.swing.JLabel();
        paramLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        signatureTextArea = new javax.swing.JTextArea();
        methodSigLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        paramTable = new javax.swing.JTable();
        returnTypeTextField = new javax.swing.JTextField();
        elemClassTypeLabel = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        returnTypeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("RETURN_TYPE_LABEL_MNEMONIC").charAt(0));
        returnTypeLabel.setLabelFor(returnTypeTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle"); // NOI18N
        returnTypeLabel.setText(bundle.getString("RETURN_TYPE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(returnTypeLabel, gridBagConstraints);
        returnTypeLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("RETURN_TYPE_DESC")); // NOI18N

        paramLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("METHOD_PARAMETERS_LABEL_MNEMONIC").charAt(0));
        paramLabel.setLabelFor(paramTable);
        paramLabel.setText(bundle.getString("PARAMETER")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(paramLabel, gridBagConstraints);
        paramLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("PARAMETER_DESC")); // NOI18N

        jScrollPane2.setAutoscrolls(true);

        signatureTextArea.setEditable(false);
        signatureTextArea.setLineWrap(true);
        signatureTextArea.setRows(3);
        signatureTextArea.setAutoscrolls(false);
        jScrollPane2.setViewportView(signatureTextArea);
        signatureTextArea.getAccessibleContext().setAccessibleName(bundle.getString("METHOD_SIGNATURE")); // NOI18N
        signatureTextArea.getAccessibleContext().setAccessibleDescription(bundle.getString("METHOD_SIGNATURE")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jScrollPane2, gridBagConstraints);

        methodSigLabel.setLabelFor(signatureTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(methodSigLabel, org.openide.util.NbBundle.getMessage(MethodDetailPanel.class, "METHOD_SIGNATURE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(methodSigLabel, gridBagConstraints);
        methodSigLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodDetailPanel.class, "METHOD_SIGNATURE")); // NOI18N

        paramTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(paramTable);
        paramTable.getAccessibleContext().setAccessibleName(bundle.getString("PARAMETER_DESC")); // NOI18N
        paramTable.getAccessibleContext().setAccessibleDescription(bundle.getString("PARAMETER_DESC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.7;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 6, 0);
        add(jScrollPane1, gridBagConstraints);
        jScrollPane1.getAccessibleContext().setAccessibleName(bundle.getString("PARAMETER")); // NOI18N
        jScrollPane1.getAccessibleContext().setAccessibleDescription(bundle.getString("PARAMETER_DESC")); // NOI18N

        returnTypeTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 2);
        add(returnTypeTextField, gridBagConstraints);
        returnTypeTextField.getAccessibleContext().setAccessibleName(bundle.getString("RETURN_TYPE_DESC")); // NOI18N
        returnTypeTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("RETURN_TYPE_DESC")); // NOI18N

        elemClassTypeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("ELEMENT_CLASS_LABEL_MNEMONIC").charAt(0));
        elemClassTypeLabel.setLabelFor(classNameTextField);
        elemClassTypeLabel.setText(bundle.getString("ELEMENT_CLASS_TYPE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(elemClassTypeLabel, gridBagConstraints);
        elemClassTypeLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ELEMENT_CLASS_TYPE_DESC")); // NOI18N

        classNameTextField.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 2);
        add(classNameTextField, gridBagConstraints);
        classNameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ELEMENT_CLASS_TYPE")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JLabel elemClassTypeLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel methodSigLabel;
    private javax.swing.JLabel paramLabel;
    private javax.swing.JTable paramTable;
    private javax.swing.JLabel returnTypeLabel;
    private javax.swing.JTextField returnTypeTextField;
    private javax.swing.JTextArea signatureTextArea;
    // End of variables declaration//GEN-END:variables
    
    public class MethodParamTableModel extends AbstractTableModel {
        
        private MethodInfo method;
        private final String[] columnNames = { "Name", "Type" };
        
        
        public MethodParamTableModel( MethodInfo method ) {
            this.method = method;
        }
        
        public String getColumnName(int column) {
            
            return columnNames[column];
        }
        
        /**
         * Returns the column class for column <code>column</code>. This
         * is set in the constructor.
         */
        public Class getColumnClass(int column) {
            return String.class;
        }
        
        public boolean isCellEditable(int row, int column) {
            if( column == 0 )
                return true;
            else
                return false;
            
        }
        
        /**
         * Sets the value to <code>aValue</code> for the object
         * <code>node</code> in column <code>column</code>. This is done
         * by using the setter method name, and coercing the passed in
         * value to the specified type.
         */
        public void setValueAt(Object aValue, int row, int column) {
            
            // Ignore null or empty string
            if( aValue == null || ((String)aValue).trim().length() == 0 )
                return;
            else
            {
                String argName = ((String)aValue).trim();
                
                // Make sure it is a legal parameter name
                try {
                    MethodParamValidator.validate( argName, method, row );
                }
                catch( InvalidParameterNameException e ) {
                    NotifyDescriptor d = new NotifyDescriptor.Message( e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify( d );

                    return;
                }

                // Update the methodParam with the new name
                MethodParam param = (MethodParam)method.getParameters().get( row );
                param.setName( argName );

                // Temporary here. Will be in the a listener
                signatureTextArea.setText( methodInfo.toString() );
            }
            
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            if( method.getParameters() == null )
                return 0;
            else
                return method.getParameters().size();
            
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            MethodParam param = (MethodParam)method.getParameters().get( rowIndex );
            
            if( columnIndex == 0 )
                return param.getName();
            else //if( columnIndex == 1 )
                return param.getType();
            
        }
        
    }
}
