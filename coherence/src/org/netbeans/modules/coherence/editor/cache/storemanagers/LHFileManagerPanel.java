/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.editor.cache.storemanagers;

import org.netbeans.modules.coherence.xml.cache.ClassName;
import org.netbeans.modules.coherence.xml.cache.Directory;
import org.netbeans.modules.coherence.xml.cache.LhFileManager;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Property;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class LHFileManagerPanel extends javax.swing.JPanel {

    /** Creates new form LHFileManagerPanel */
    public LHFileManagerPanel() {
        initComponents();
    }

    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    public LHFileManagerPanel(LhFileManager manager) {
        this();
        this.manager = manager;
        setupBindings();
    }
    /*
     * Properties
     */
    private LhFileManager manager = null;
    private BindingGroup bindingGroup = new BindingGroup();
    /*
     * Methods
     */

    public LhFileManager getManager() {
        return manager;
    }

    public void setManager(LhFileManager manager) {
        this.manager = manager;
        refreshBindings();
    }

    private void refreshBindings() {
        for (Binding b : bindingGroup.getBindings()) {
            bindingGroup.removeBinding(b);
        }
        setupBindings();
    }

    private void setupBindings() {
        // Set Bindings
        Property propertyTextValue = BeanProperty.create("text");
        // Directory
        Property propertyDirectory = BeanProperty.create("directory");
        Binding bindingDirectory = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyDirectory, tfDirectory, propertyTextValue);
        bindingGroup.addBinding(bindingDirectory);
        // Classname
        Property propertyClassname = BeanProperty.create("classname");
        Binding bindingClassname = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyClassname, tfClassname, propertyTextValue);
        bindingGroup.addBinding(bindingClassname);
        // MaximumSize
        Property propertyMaximumSize = BeanProperty.create("fileName");
        Binding bindingMaximumSize = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, manager, propertyMaximumSize, tfFilename, propertyTextValue);
        bindingGroup.addBinding(bindingMaximumSize);
        // Bind All
//        bindingGroup.addBindingListener(this);
        bindingGroup.bind();
    }
    /*
     * Binding Methods
     */
    // Getters & Setters Used for none simple binding

    public void setDirectory(String s) {
        if (manager.getDirectory() == null) {
            manager.setDirectory(new Directory());
        }
        manager.getDirectory().setvalue(s);
    }

    public String getDirectory() {
        if (manager.getDirectory() == null) {
            return null;
        } else {
            return manager.getDirectory().getvalue();
        }
    }

    public void setClassname(String s) {
        if (manager.getClassName() == null) {
            manager.setClassName(new ClassName());
        }
        manager.getClassName().setvalue(s);
    }

    public String getClassname() {
        if (manager.getClassName() == null) {
            return null;
        } else {
            return manager.getClassName().getvalue();
        }
    }

    /*
     * =========================================================================
     * END: Custom Code
     * =========================================================================
     */

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        tfDirectory = new javax.swing.JTextField();
        tfFilename = new javax.swing.JTextField();
        tfClassname = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(LHFileManagerPanel.class, "LHFileManagerPanel.border.title"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(LHFileManagerPanel.class, "LHFileManagerPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(LHFileManagerPanel.class, "LHFileManagerPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(LHFileManagerPanel.class, "LHFileManagerPanel.jLabel3.text")); // NOI18N

        tfDirectory.setText(org.openide.util.NbBundle.getMessage(LHFileManagerPanel.class, "LHFileManagerPanel.tfDirectory.text")); // NOI18N

        tfFilename.setText(org.openide.util.NbBundle.getMessage(LHFileManagerPanel.class, "LHFileManagerPanel.tfFilename.text")); // NOI18N

        tfClassname.setText(org.openide.util.NbBundle.getMessage(LHFileManagerPanel.class, "LHFileManagerPanel.tfClassname.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfFilename, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addComponent(tfDirectory, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addComponent(tfClassname, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfClassname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField tfClassname;
    private javax.swing.JTextField tfDirectory;
    private javax.swing.JTextField tfFilename;
    // End of variables declaration//GEN-END:variables

}
