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
import org.netbeans.modules.coherence.xml.cache.InitialSize;
import org.netbeans.modules.coherence.xml.cache.MaximumSize;
import org.netbeans.modules.coherence.xml.cache.NioMemoryManager;
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
public class NIOMemoryManagerPanel extends javax.swing.JPanel {

    /** Creates new form NIOMemoryManagerPanel */
    public NIOMemoryManagerPanel() {
        initComponents();
    }

    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    public NIOMemoryManagerPanel(NioMemoryManager manager) {
        this();
        this.manager = manager;
        setupBindings();
    }
    /*
     * Properties
     */
    private NioMemoryManager manager = null;
    private BindingGroup bindingGroup = new BindingGroup();
    /*
     * Methods
     */

    public NioMemoryManager getManager() {
        return manager;
    }

    public void setManager(NioMemoryManager manager) {
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
        // Classname
        Property propertyClassname = BeanProperty.create("classname");
        Binding bindingClassname = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyClassname, tfClassname, propertyTextValue);
        bindingGroup.addBinding(bindingClassname);
        // InitialSize
        Property propertyInitialSize = BeanProperty.create("nioInitialSize");
        Binding bindingInitialSize = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyInitialSize, tfInitialSize, propertyTextValue);
        bindingGroup.addBinding(bindingInitialSize);
        // MaximumSize
        Property propertyMaximumSize = BeanProperty.create("nioMaximumSize");
        Binding bindingMaximumSize = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyMaximumSize, tfMaximumSize, propertyTextValue);
        bindingGroup.addBinding(bindingMaximumSize);
        // Bind All
//        bindingGroup.addBindingListener(this);
        bindingGroup.bind();
    }
    /*
     * Binding Methods
     */
    // Getters & Setters Used for none simple binding

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

    public void setNioMaximumSize(String s) {
        if (manager.getMaximumSize() == null) {
            manager.setMaximumSize(new MaximumSize());
        }
        manager.getMaximumSize().setvalue(s);
    }

    public String getNioMaximumSize() {
        if (manager.getMaximumSize() == null) return null;
        else return manager.getMaximumSize().getvalue();
    }

    public void setNioInitialSize(String s) {
        if (manager.getInitialSize() == null) {
            manager.setInitialSize(new InitialSize());
        }
        manager.getInitialSize().setvalue(s);
    }

    public String getNioInitialSize() {
        if (manager.getInitialSize() == null) return null;
        else return manager.getInitialSize().getvalue();
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
        tfClassname = new javax.swing.JTextField();
        tfInitialSize = new javax.swing.JTextField();
        tfMaximumSize = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NIOMemoryManagerPanel.class, "NIOMemoryManagerPanel.border.title"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NIOMemoryManagerPanel.class, "NIOMemoryManagerPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(NIOMemoryManagerPanel.class, "NIOMemoryManagerPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(NIOMemoryManagerPanel.class, "NIOMemoryManagerPanel.jLabel3.text")); // NOI18N

        tfClassname.setText(org.openide.util.NbBundle.getMessage(NIOMemoryManagerPanel.class, "NIOMemoryManagerPanel.tfClassname.text")); // NOI18N

        tfInitialSize.setText(org.openide.util.NbBundle.getMessage(NIOMemoryManagerPanel.class, "NIOMemoryManagerPanel.tfInitialSize.text")); // NOI18N

        tfMaximumSize.setText(org.openide.util.NbBundle.getMessage(NIOMemoryManagerPanel.class, "NIOMemoryManagerPanel.tfMaximumSize.text")); // NOI18N

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
                    .addComponent(tfInitialSize, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                    .addComponent(tfClassname, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                    .addComponent(tfMaximumSize, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfClassname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfInitialSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfMaximumSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField tfClassname;
    private javax.swing.JTextField tfInitialSize;
    private javax.swing.JTextField tfMaximumSize;
    // End of variables declaration//GEN-END:variables

}
