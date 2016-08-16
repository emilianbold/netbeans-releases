/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.editor;

import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.lang.model.SourceVersion;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sdedic
 */
class ClassNamePanel extends javax.swing.JPanel implements DocumentListener {
    private RequestProcessor RP = new RequestProcessor(ClassNamePanel.class);
    
    private final Project       project;
    private final FileObject    anchor;
    private final RequestProcessor.Task checkTask = RP.create(this::delayedCheck, true);
    private ChangeListener listener;
    private NotificationLineSupport notifier;
    
    /**
     * Creates new form ClassNamePanel
     */
    public ClassNamePanel(Project project, FileObject anchor, String initialName) {
        this.project = project;
        this.anchor = anchor;
        initComponents();
        
        locationSelect.setRenderer(new GroupCellRenderer());
        packageSelect.setRenderer(PackageView.listRenderer());
        
        updateRoots();
        updatePackages();
        
        selectInitialPackage();
        
        ActionListener al = this::actionPerformed;
        locationSelect.addActionListener(al);
        packageSelect.addActionListener(al);
        packageSelect.getEditor().addActionListener(al);
        className.getDocument().addDocumentListener(this);
        
        if (initialName != null) {
            className.setText(initialName);
        }
    }
    
    public void setNotifier(NotificationLineSupport support) {
        this.notifier = support;
    }
    
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::initDisplay);
    }
    
    private void initDisplay() {
        checkErrors();
        className.requestFocus();
    }
    
    public void addChangeListener(ChangeListener l) {
        this.listener = l;
    }
    
    public void removeChangeListener(ChangeListener l) {
        this.listener = null;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        className = new javax.swing.JTextField();
        packageSelect = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        locationSelect = new javax.swing.JComboBox();
        message = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ClassNamePanel.class, "ClassNamePanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ClassNamePanel.class, "ClassNamePanel.jLabel2.text")); // NOI18N

        className.setText(org.openide.util.NbBundle.getMessage(ClassNamePanel.class, "ClassNamePanel.className.text")); // NOI18N
        className.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classNameActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ClassNamePanel.class, "ClassNamePanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(message, org.openide.util.NbBundle.getMessage(ClassNamePanel.class, "ClassNamePanel.message.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(message, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locationSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel3)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(className)
                                .addComponent(packageSelect, 0, 328, Short.MAX_VALUE)))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packageSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(className, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(message)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void classNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_classNameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField className;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox locationSelect;
    private javax.swing.JLabel message;
    private javax.swing.JComboBox packageSelect;
    // End of variables declaration//GEN-END:variables


    private SourceGroup[] groups;
    

    @Override
    public void insertUpdate(DocumentEvent e) {
        checkTask.schedule(200);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        checkTask.schedule(200);
    }
    
    private void delayedCheck() {
        if (isVisible()) {
            checkErrors();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) { }
    
    
    private void actionPerformed(ActionEvent e) {
        if (e.getSource() == locationSelect) {
            updatePackages();
            checkErrors();
        } else if (e.getSource() == packageSelect) {
            checkErrors();
        } else {
            // combo box was edited; schedule a delay
        }
    }
    
    private void updateRoots() {
        Sources sources = ProjectUtils.getSources(project);
        groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        // XXX why?? This is probably wrong. If the project has no Java groups,
        // you cannot move anything into it.
        if (groups.length == 0) {
            groups = sources.getSourceGroups( Sources.TYPE_GENERIC ); 
        }
        
        int preselectedItem = 0;
        if (anchor != null) {
            for( int i = 0; i < groups.length; i++ ) {
                if (groups[i].contains(anchor)) {
                    preselectedItem = i;
                    break;
                }
            }
        }
                
        // Setup comboboxes 
        locationSelect.setModel(new DefaultComboBoxModel(groups));
        if(groups.length > 0) {
            locationSelect.setSelectedIndex(preselectedItem);
        }
    }

    private void updatePackages() {
        SourceGroup g = (SourceGroup) locationSelect.getSelectedItem();
        packageSelect.setModel(g != null
                ? PackageView.createListView(g)
                : new DefaultComboBoxModel());
    }
    
    private void reportError(String err) {
        notifier.setErrorMessage(err);
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    @NbBundle.Messages({
       "ERR_ClassnameNotSpecified=Error: class name has not been specified",
       "# {0} - class name",
       "ERR_ClassnameInvalid=Error: {0} is not a valid Java class name",
       "ERR_PackagDoesNotExist=Error: existing package must be selected",
       "ERR_NoSourceGroups=Error: no place to generate class, set up a Source folder",
       "INFO_ClassAlreadyExists=Note: The class already exists. It will be overwritten."
    })
    private void checkErrors() {
        if (packageSelect.getItemCount() == 0) {
            reportError(Bundle.ERR_NoSourceGroups());
            return;
        }
        if (packageSelect.getSelectedItem() == null) {
            reportError(Bundle.ERR_PackagDoesNotExist());
            return;
        }
        String n = className.getText().trim();
        if (n.isEmpty()) {
            reportError(Bundle.ERR_ClassnameNotSpecified());
            return;
        }
        
        if (!SourceVersion.isName(n)) {
            reportError(Bundle.ERR_ClassnameInvalid(n));
            return;
        }
        
        notifier.clearMessages();

        FileObject folder = getTarget();
        if (folder != null) {
            n = getClassName();
            FileObject existing = folder.getFileObject(n, "java"); // NOI18N
            if (existing != null) {
                notifier.setInformationMessage(Bundle.INFO_ClassAlreadyExists());
            }
        }
        
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
    
    public String getClassName() {
        String n = className.getText().trim();
        if (n.isEmpty() || !SourceVersion.isName(n)) {
            return null;
        }
        return n;
    }
    
    public FileObject getRootFolder() {
        Object item = locationSelect.getSelectedItem();
        if (item == null) {
            return null;
        }
        return ((SourceGroup) item).getRootFolder();
    }
    
    public String getPackageName() {
        String packageName = packageSelect.getSelectedItem().toString();
        return packageName; // NOI18N
    }
    
    public FileObject getTarget() {
        FileObject root = getRootFolder();
        if (root == null) {
            return null;
        }
        String pkg = getPackageName().replace(".", "/");
        return root.getFileObject(pkg);
    }

    private void selectInitialPackage() {
        FileObject root = getRootFolder();
        if (root == null) {
            return;
        }
        if (anchor == null || !FileUtil.isParentOf(root, anchor)) {
            return;
        }
        String rp = FileUtil.getRelativePath(root, anchor.getParent()).replace("/", ".");
        for (int i = 0; i < packageSelect.getItemCount(); i++) {
            Object o = packageSelect.getItemAt(i);
            if (rp.equals(o.toString())) {
                packageSelect.setSelectedIndex(i);
                break;
            }
        }
    }
    
    public boolean hasErrors() {
        return getTarget() == null || 
               getClassName() == null;
    }


    private abstract static class BaseCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public BaseCellRenderer () {
            setOpaque(true);
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }
    
    /** Groups combo renderer, used also in MoveMembersPanel */
    static class GroupCellRenderer extends BaseCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if (value instanceof SourceGroup) {
                SourceGroup g = (SourceGroup) value;
                setText(g.getDisplayName());
                setIcon(g.getIcon(false));
            } else {
                setText(""); // NOI18N
                setIcon(null);
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    
}
