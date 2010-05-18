/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.configextension.handlers.properties;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.netbeans.modules.compapp.configextension.handlers.model.Handler;

/**
 *
 * @author jqian
 */
public class HandlerPanel extends javax.swing.JPanel {

    private boolean editable;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup2;

    public HandlerPanel() {
        initComponents();
    }

    public void setEditable(boolean editable) {
        this.editable = editable;

        txtName.setEditable(editable);
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        if (bindingGroup2 != null) {
            bindingGroup2.unbind();
        }

        this.handler = handler;

        boolean noHandler = handler == null;
        lblName.setVisible(!noHandler);
        txtName.setVisible(!noHandler);
        lblClass.setVisible(!noHandler);
        txtClass.setVisible(!noHandler);
        lblProject.setVisible(!noHandler);
        txtProject.setVisible(!noHandler);
        lblJars.setVisible(!noHandler);
        spListJars.setVisible(!noHandler);
        lblParams.setVisible(!noHandler);
        spParamTable.setVisible(!noHandler);

        setupBinding();
    }

    private void setupBinding() {

        bindingGroup2 = new BindingGroup();
        org.jdesktop.beansbinding.Binding binding =
                Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                handler, ELProperty.create("${name}"), // NOI18N
                txtName, BeanProperty.create("text")); // NOI18N
        bindingGroup2.addBinding(binding);

        binding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                handler, ELProperty.create("${className}"), // NOI18N
                txtClass, BeanProperty.create("text")); // NOI18N
        bindingGroup2.addBinding(binding);

        binding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                handler, ELProperty.create("${projectPath}"), // NOI18N
                txtProject, BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST")); // NOI18N
        bindingGroup2.addBinding(binding);

        ELProperty eLProperty = ELProperty.create("${jarPaths}"); // NOI18N
        JListBinding jListBinding =
                SwingBindings.createJListBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                handler, eLProperty, listJars);
        bindingGroup2.addBinding(jListBinding);

        eLProperty = ELProperty.create("${parameters}"); // NOI18N
        JTableBinding jTableBinding =
                SwingBindings.createJTableBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                handler, eLProperty, paramTable);
        JTableBinding.ColumnBinding columnBinding =
                jTableBinding.addColumnBinding(
                ELProperty.create("${name}")); // NOI18N
        columnBinding.setColumnName("Name"); // NOI18N
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(ELProperty.create("${value}")); // NOI18N
        columnBinding.setColumnName("Value"); // NOI18N
        columnBinding.setEditable(editable);
        columnBinding.setColumnClass(String.class);
        bindingGroup2.addBinding(jTableBinding);

        bindingGroup2.bind();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        handler = new org.netbeans.modules.compapp.configextension.handlers.model.Handler();
        lblName = new javax.swing.JLabel();
        lblProject = new javax.swing.JLabel();
        lblClass = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtClass = new javax.swing.JTextField();
        txtProject = new javax.swing.JTextField();
        lblParams = new javax.swing.JLabel();
        spParamTable = new javax.swing.JScrollPane();
        paramTable = new javax.swing.JTable();
        lblJars = new javax.swing.JLabel();
        spListJars = new javax.swing.JScrollPane();
        listJars = new javax.swing.JList();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.border.title"))); // NOI18N

        lblName.setLabelFor(txtName);
        lblName.setText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.lblName.text")); // NOI18N

        lblProject.setLabelFor(txtProject);
        lblProject.setText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.lblProject.text")); // NOI18N

        lblClass.setLabelFor(txtClass);
        lblClass.setText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.lblClass.text")); // NOI18N

        txtName.setToolTipText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.txtName.tooltipText")); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, handler, org.jdesktop.beansbinding.ELProperty.create("${name}"), txtName, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        txtClass.setEditable(false);
        txtClass.setToolTipText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.txtClass.tooltipText")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, handler, org.jdesktop.beansbinding.ELProperty.create("${className}"), txtClass, org.jdesktop.beansbinding.BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);

        txtProject.setEditable(false);
        txtProject.setToolTipText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.txtProject.tooltipText")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, handler, org.jdesktop.beansbinding.ELProperty.create("${projectPath}"), txtProject, org.jdesktop.beansbinding.BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);

        lblParams.setLabelFor(paramTable);
        lblParams.setText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.lblParams.text")); // NOI18N

        paramTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        paramTable.setToolTipText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.paramTable.tooltipText")); // NOI18N

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${parameters}");
        org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, handler, eLProperty, paramTable);
        org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${name}"));
        columnBinding.setColumnName("Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${value}"));
        columnBinding.setColumnName("Value");
        columnBinding.setColumnClass(String.class);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        spParamTable.setViewportView(paramTable);

        lblJars.setLabelFor(listJars);
        lblJars.setText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.lblJars.text")); // NOI18N

        listJars.setToolTipText(org.openide.util.NbBundle.getMessage(HandlerPanel.class, "HandlerPanel.listJars.tooltipText")); // NOI18N
        listJars.setEnabled(false);

        eLProperty = org.jdesktop.beansbinding.ELProperty.create("${jarPaths}");
        org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, handler, eLProperty, listJars);
        bindingGroup.addBinding(jListBinding);

        spListJars.setViewportView(listJars);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblName)
                    .add(lblProject)
                    .add(lblClass)
                    .add(lblJars)
                    .add(lblParams))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, txtProject, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, txtClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, spParamTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                    .add(txtName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                    .add(spListJars, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtProject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblProject))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spListJars, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblJars))
                .add(7, 7, 7)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblClass)
                    .add(txtClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spParamTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .add(lblParams))
                .addContainerGap())
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.compapp.configextension.handlers.model.Handler handler;
    private javax.swing.JLabel lblClass;
    private javax.swing.JLabel lblJars;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblParams;
    private javax.swing.JLabel lblProject;
    private javax.swing.JList listJars;
    private javax.swing.JTable paramTable;
    private javax.swing.JScrollPane spListJars;
    private javax.swing.JScrollPane spParamTable;
    private javax.swing.JTextField txtClass;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtProject;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
