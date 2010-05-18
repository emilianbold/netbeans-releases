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

/*
 * EnumerationCustomizer.java
 *
 * Created on January 17, 2006, 10:26 PM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.openide.util.HelpCtx;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SimpleRestriction;

/**
 * Attribute customizer
 *
 * @author  Ajit Bhate
 */
public class EnumerationCustomizer<T extends SimpleRestriction>
        extends AbstractSchemaComponentCustomizer<T>
{
    
    static final long serialVersionUID = 1L;
    
    /**
     * Creates new form EnumerationCustomizer
     */
    public EnumerationCustomizer(SchemaComponentReference<T> reference) {
        super(reference);
        initComponents();
        initializeModel();
        initializeUISelection();
        addListeners();
    }
    
    public void applyChanges() throws IOException {
        if(valueTable.getCellEditor()!=null)
            valueTable.getCellEditor().stopCellEditing();
        if(enumData!=null&& enumData.isChanged()) {
            enumData.save();
        }
    }
    
    public void reset() {
        removeListeners();
        initializeModel();
        initializeUISelection();
        addListeners();
        setSaveEnabled(false);
        setResetEnabled(false);
    }
    
    /**
     * initializes non ui elements from model
     */
    private void initializeModel() {
        enumData = new EnumData(getReference().get(),
                (DefaultTableModel)valueTable.getModel());
    }
    
    /**
     * Initializes UI from model values
     */
    private void initializeUISelection() {
        Collection<Enumeration> enums = getReference().get().getEnumerations();
        DefaultTableModel tableModel = (DefaultTableModel) valueTable.getModel();
        int rowCount = tableModel.getRowCount();
        if(rowCount>0) {
            for (int i=rowCount-1;i>=0;) {
                tableModel.removeRow(i--);
            }
        }
        for(Enumeration e:enums) {
            String value = e.getValue();
            String description = null;
            if(e.getAnnotation()!=null &&
                    !e.getAnnotation().getDocumentationElements().isEmpty()) {
                description = e.getAnnotation().
                        getDocumentationElements().iterator().next().
                        getContentFragment();
            }
            tableModel.addRow(new String[]{value,description});
        }
    }
    
    private void addListeners() {
        if(tableListener == null) {
            tableListener = new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    if(enumData==null) return;
                    if(e.getType()==TableModelEvent.UPDATE) {
                        for(int i=e.getFirstRow();i<=e.getLastRow();i++)
                            enumData.modify(e.getFirstRow());
                        determineValidity();
                    } else if(e.getType()==TableModelEvent.INSERT) {
                        for(int i=e.getFirstRow();i<=e.getLastRow();i++)
                            enumData.add();
                        determineValidity();
                    } else if(e.getType()==TableModelEvent.DELETE) {
                        for(int i=e.getLastRow();i>=e.getFirstRow();i--)
                            enumData.remove(i);
                        determineValidity();
                    }
                }
            };
        }
        valueTable.getModel().addTableModelListener(tableListener);
    }
    
    private void removeListeners() {
        valueTable.getModel().removeTableModelListener(tableListener);
    }
    
    /**
     * Based on the current radio button status and node selections, decide
     * if we are in a valid state for accepting the user's input.
     */
    private void determineValidity() {
        boolean valueChange = enumData!=null&&enumData.isChanged();
        if(!valueChange) {
            setSaveEnabled(false);
            setResetEnabled(false);
            return;
        } else {
            setResetEnabled(true);
            setSaveEnabled(true);
        }
    }
    
    /**
     * This method is called from within the constructor to
     * initializeTypeView the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        valuePane = new javax.swing.JScrollPane();
        valueTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        valuePane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        valueTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 128, 128)));
        valueTable.setModel(new DefaultTableModel(
            new Object [][]{},
            new String []{org.openide.util.NbBundle.getMessage(EnumerationCustomizer.class, "LBL_Enumeration_Value"),
                org.openide.util.NbBundle.getMessage(EnumerationCustomizer.class, "LBL_Enumeration_Description"),
            }
        ));
        valueTable.setToolTipText(org.openide.util.NbBundle.getBundle(EnumerationCustomizer.class).getString("HINT_Enumeration_Table"));
        valueTable.getSelectionModel().addListSelectionListener(
            new	ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if(valueTable.getSelectedRowCount()<=0) {
                        removeButton.setEnabled(false);
                    } else {
                        removeButton.setEnabled(true);
                    }
                }
            });
            valuePane.setViewportView(valueTable);
            valueTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(EnumerationCustomizer.class).getString("HINT_Enumeration_Table"));
            valueTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(EnumerationCustomizer.class).getString("HINT_Enumeration_Table"));

            org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(EnumerationCustomizer.class, "LBL_Enumeration_AddValue"));
            addButton.setToolTipText(org.openide.util.NbBundle.getBundle(EnumerationCustomizer.class).getString("HINT_Enumeration_AddValue"));
            addButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    addButtonActionPerformed(evt);
                }
            });

            org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(EnumerationCustomizer.class, "LBL_Enumeration_RemoveValue"));
            removeButton.setToolTipText(org.openide.util.NbBundle.getBundle(EnumerationCustomizer.class).getString("HINT_Enumeration_RemoveValue"));
            removeButton.setEnabled(false);
            removeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    removeButtonActionPerformed(evt);
                }
            });

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(valuePane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .add(addButton)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(removeButton)))
                    .addContainerGap())
            );

            layout.linkSize(new java.awt.Component[] {addButton, removeButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

            layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .add(valuePane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(removeButton)
                        .add(addButton))
                    .addContainerGap())
            );

            layout.linkSize(new java.awt.Component[] {addButton, removeButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        }// </editor-fold>//GEN-END:initComponents
    
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int [] rows = valueTable.getSelectedRows();
        if (rows == null || rows.length<1) return;
        DefaultTableModel tableModel =
                (DefaultTableModel)valueTable.getModel();
        for (int i =rows.length;i>0;i--) {
            tableModel.removeRow(rows[i-1]);
        }
    }//GEN-LAST:event_removeButtonActionPerformed
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        DefaultTableModel tableModel =
                (DefaultTableModel)valueTable.getModel();
        tableModel.addRow(new String[] {"",""});
        if(valueTable.getCellEditor()!=null)
            valueTable.getCellEditor().stopCellEditing();
        valueTable.requestFocusInWindow();
        valueTable.changeSelection(valueTable.getRowCount()-1,0,false,false);
    }//GEN-LAST:event_addButtonActionPerformed
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(EnumerationCustomizer.class);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton addButton;
    public javax.swing.JButton removeButton;
    public javax.swing.JScrollPane valuePane;
    public javax.swing.JTable valueTable;
    // End of variables declaration//GEN-END:variables
    
    private TableModelListener tableListener;
    private transient EnumData enumData;
    
    private static class EnumData {
        enum State {ADDED,REMOVED,MODIFIED,UNMODIFIED};
        private ArrayList<Enumeration> enums;
        private ArrayList<State> states;
        private SimpleRestriction str;
        private DefaultTableModel tModel;
        
        EnumData(SimpleRestriction str, DefaultTableModel tModel) {
            enums = new ArrayList<Enumeration>(0);
            states = new ArrayList<State>(0);
            for(Enumeration e:str.getEnumerations()) {
                enums.add(e);
                states.add(State.UNMODIFIED);
            }
            this.str = str;
            this.tModel = tModel;
        }
        
        int getRealIndex(int i) {
            int idx = -1;
            for(int ctr = 0; ctr<states.size();ctr++) {
                if(states.get(ctr)!=State.REMOVED)
                    idx++;
                if(idx==i) return ctr;
            }
            return i;
        }
        
        void add() {
            states.add(State.ADDED);
            enums.add(null);
        }
        
        void remove(int idx) {
            int i = getRealIndex(idx);
            if(enums.get(i)==null) {
                enums.remove(i);
                states.remove(i);
            } else {
                states.set(i,State.REMOVED);
            }
        }
        
        void modify(int idx) {
            String newValue = (String) tModel.getValueAt(idx,0);
            String newDesc = (String) tModel.getValueAt(idx,1);
            int i = getRealIndex(idx);
            Enumeration e = enums.get(i);
            if(e!=null) {
                String value = e.getValue();
                if(!newValue.equals(e.getValue())) {
                    states.set(i,State.MODIFIED);
                    return;
                }
                String description = null;
                if(e.getAnnotation()!=null &&
                        !e.getAnnotation().getDocumentationElements().isEmpty()) {
                    description = e.getAnnotation().
                            getDocumentationElements().iterator().next().
                            getContentFragment();
                }
                if(description != null && !description.equals(newDesc) ||
                        newDesc != null && !newDesc.equals(description)) {
                    states.set(i,State.MODIFIED);
                    return;
                }
                states.set(i,State.UNMODIFIED);
            }
        }
        
        boolean isChanged() {
            if(states.contains(State.ADDED) ||
                    states.contains(State.REMOVED) ||
                    states.contains(State.MODIFIED))
                return true;
            return false;
        }
        
        void save() {
            SchemaComponentFactory factory = str.getModel().getFactory();
            for(int i=0;i<states.size();i++) {
                State state = states.get(i);
                switch (state) {
                    case ADDED:
                        Enumeration e = factory.createEnumeration();
                        enums.set(i,e);
                        str.addEnumeration(e);
                        e.setValue((String)tModel.getValueAt(i,0));
                        String newDesc = (String) tModel.getValueAt(i,1);
                        if(newDesc!=null && !"".equals(newDesc)) {
                            try {
                                Documentation d = factory.createDocumentation();
                                d.setContentFragment(newDesc);
                                Annotation a = factory.createAnnotation();
                                a.addDocumentation(d);
                                e.setAnnotation(a);
                            } catch (IOException ex) {
                            }
                        }
                        states.set(i,State.UNMODIFIED);
                        break;
                    case REMOVED:
                        str.removeEnumeration(enums.get(i));
                        enums.remove(i);
                        states.remove(i--); //decrement i
                        break;
                    case MODIFIED:
                        e = enums.get(i);
                        assert e != null;
                        e.setValue((String)tModel.getValueAt(i,0));
                        newDesc = (String) tModel.getValueAt(i,1);
                        if(newDesc!=null && !"".equals(newDesc)) {
                            try {
                                Documentation d = factory.createDocumentation();
                                d.setContentFragment(newDesc);
                                Annotation a = factory.createAnnotation();
                                a.addDocumentation(d);
                                e.setAnnotation(a);
                            } catch (IOException ex) {
                            }
                        }
                        states.set(i,State.UNMODIFIED);
                        break;
                }
            }
        }
    }
    
}
