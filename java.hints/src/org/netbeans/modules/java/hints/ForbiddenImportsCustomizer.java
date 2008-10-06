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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  phrebejk
 */
public class ForbiddenImportsCustomizer extends javax.swing.JPanel {
    
    private static String FORBIDDEN_IMPORTS_KEY = "ForbiddenImports"; // NOI18N
    private static String FORBIDDEN_IMPORTS_DEFAULT = "sun.**"; // NOI18N
    private static String IMPORTS_DELIMITER = ";"; // NOI18N
    
    private Preferences prefs;
    
    /** Creates new form ForbiddenImports */
    public ForbiddenImportsCustomizer(Preferences node) {
        this.prefs = node;
        initComponents();
        DefaultListModel model = new DefaultListModel();
        for( String item : getForbiddenImports(node) ) {
            model.addElement( item );
        }
        listItems.setModel( model );
        listItems.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                enableButtons();
            }
        });
        enableButtons();
    }
    
    private void enableButtons() {
        int selIndex = listItems.getSelectedIndex();
        btnEdit.setEnabled( selIndex >= 0 );
        btnRemove.setEnabled( selIndex >= 0 );
    }
    
    private void updatePreferences() {
        String[] items = new String[listItems.getModel().getSize()];
        for( int i=0; i<((DefaultListModel)listItems.getModel()).size(); i++ ) {
            items[i] = (String)listItems.getModel().getElementAt(i);
        }
        prefs.put(FORBIDDEN_IMPORTS_KEY, encodeForbiddenImports( items ));
    }
    
    private String showInputDialog( String initialValue ) {
        String title = null == initialValue ? NbBundle.getMessage( ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.titleAdd" ) //NOI18N
                : NbBundle.getMessage( ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.titleEdit" ); //NOI18N
        final JButton btnOk = new JButton( null == initialValue ? NbBundle.getMessage( ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.btnAdd" ) //NOI18N
                : NbBundle.getMessage( ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.btnEdit" ) ); //NOI18N
        btnOk.setEnabled( null != initialValue );
        JButton btnCancel = new JButton( NbBundle.getMessage( ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.btnCancel" ) ); //NOI18N
        
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setBorder( BorderFactory.createEmptyBorder(10, 10, 0, 10) );
        
        JTextField input = new JTextField();
        input.setText( null == initialValue ? "" : initialValue );
        input.getDocument().addDocumentListener( new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                btnOk.setEnabled( e.getDocument().getLength() > 0 );
            }

            public void removeUpdate(DocumentEvent e) {
                btnOk.setEnabled( e.getDocument().getLength() > 0 );
            }

            public void changedUpdate(DocumentEvent e) {
                btnOk.setEnabled( e.getDocument().getLength() > 0 );
            }
        });
        
        panel.add( new JLabel(NbBundle.getMessage( ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.label" ) ),  //NOI18N
                new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(0,0,0,5),0,0) );
        panel.add( input, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );
        panel.add( new JLabel(NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.hint") ),  //NOI18N 
                new GridBagConstraints(1,1,1,1,1.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(5,0,0,0),0,0) );
        
        DialogDescriptor dd = new DialogDescriptor(panel, title, true, 
                new Object[] { btnOk, btnCancel }, btnOk, 
                DialogDescriptor.DEFAULT_ALIGN, null, null );
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible( true );
        if( btnOk == dd.getValue() ) {
            return input.getText();
        }
        return null;
    } 
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        listItems = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        listItems.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(listItems);
        listItems.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.listItems.AccessibleContext.accessibleName")); // NOI18N
        listItems.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.listItems.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
        jScrollPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.jScrollPane1.AccessibleContext.accessibleName")); // NOI18N
        jScrollPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.jScrollPane1.AccessibleContext.accessibleDescription")); // NOI18N

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItem(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel1.add(btnAdd, gridBagConstraints);
        btnAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.btnAdd.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnEdit, org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.btnEdit.text")); // NOI18N
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editItem(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel1.add(btnEdit, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.btnRemove.text")); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeItem(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(btnRemove, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(jPanel1, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForbiddenImportsCustomizer.class, "ForbiddenImportsCustomizer.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void removeItem(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeItem
    int selIndex = listItems.getSelectedIndex();//GEN-LAST:event_removeItem
    if( selIndex < 0 )
        return;
    ((DefaultListModel)listItems.getModel()).remove( selIndex );
    selIndex++;
    if( selIndex > listItems.getModel().getSize()-1 )
        selIndex = listItems.getModel().getSize()-1;
    listItems.getSelectionModel().setSelectionInterval( selIndex, selIndex );
    updatePreferences();
}                           

private void editItem(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editItem
    int selIndex = listItems.getSelectedIndex();//GEN-LAST:event_editItem
    if( selIndex < 0 )
        return;
    String initialValue = (String)((DefaultListModel)listItems.getModel()).get( selIndex );
    String newValue = showInputDialog( initialValue );
    if( null != newValue ) {
        ((DefaultListModel)listItems.getModel()).set( selIndex, newValue );
        listItems.getSelectionModel().setSelectionInterval( selIndex, selIndex );
        updatePreferences();
    }
}                         

private void addItem(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItem
    String newValue = showInputDialog( null );//GEN-LAST:event_addItem
    if( null != newValue ) {
        ((DefaultListModel)listItems.getModel()).addElement( newValue );
        int count = listItems.getModel().getSize();
        if( count > 0 )
            listItems.getSelectionModel().setSelectionInterval( count-1, count-1 );
        updatePreferences();
    }

}                        
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRemove;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList listItems;
    // End of variables declaration//GEN-END:variables
    
    public static String encodeForbiddenImports( String[] forbiddenImports ) {
        
        StringBuffer sb = new StringBuffer();
        
        for( String fi : forbiddenImports ) {
            sb.append(fi);
            sb.append(IMPORTS_DELIMITER); 
        }
        
        return sb.toString();
    }
    
    public static String[] decodeForbiddenImports( String forbiddenImports ) {
        
        StringTokenizer st = new StringTokenizer(forbiddenImports, IMPORTS_DELIMITER, false );
        
        List<String> imList = new ArrayList<String>();
        
        while( st.hasMoreTokens() ) {
            String im = st.nextToken();
            imList.add(im);
        }
        
        return imList.toArray(new String[imList.size()]);
        
    }
    
    public static String[] getForbiddenImports(Preferences node ) {
        String text = node.get(FORBIDDEN_IMPORTS_KEY, FORBIDDEN_IMPORTS_DEFAULT);
        return decodeForbiddenImports(text);
    }
    
}
