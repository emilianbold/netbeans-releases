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
package org.netbeans.modules.coherence.editor.cache.acceptors;

import org.netbeans.modules.coherence.xml.cache.AddressProvider;
import org.netbeans.modules.coherence.xml.cache.LocalAddress;
import org.netbeans.modules.coherence.xml.cache.TcpAcceptor;
import java.util.List;
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
public class TcpAcceptorPanel extends javax.swing.JPanel {

    /** Creates new form TcpAcceptorPanel */
    public TcpAcceptorPanel() {
        initComponents();
    }

    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    public TcpAcceptorPanel(TcpAcceptor acceptor) {
        this();
        this.acceptor = acceptor;
        setupBindings();
    }
    /*
     * Properties
     */
    private TcpAcceptor acceptor = null;
    private BindingGroup bindingGroup = new BindingGroup();

    /*
     * Methods
     */
    public TcpAcceptor getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(TcpAcceptor acceptor) {
        this.acceptor = acceptor;
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
        Property propertyLocalAddress = BeanProperty.create("localAddress");
        Binding bindingLocalAddress = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyLocalAddress, tfLocalAddress, propertyTextValue);
        bindingGroup.addBinding(bindingLocalAddress);
        // Bind All
//        bindingGroup.addBindingListener(this);
        bindingGroup.bind();
    }
    /*
     * Binding Methods
     */
    // Getters & Setters Used for none simple binding

    public void setLocalAddress(String s) {
        if (acceptor == null) {
            acceptor = new TcpAcceptor();
        }
        List<Object> addressList = acceptor.getLocalAddressOrAddressProvider();
        LocalAddress localAddress = new LocalAddress();
    }

    public String getLocalAddress() {
        String s = null;
        if (acceptor != null) {
            List<Object> addressList = acceptor.getLocalAddressOrAddressProvider();
            LocalAddress localAddress = null;
            AddressProvider addressProvider = null;
            List<Object> nameList = null;
            for (Object o : addressList) {
                if (o instanceof LocalAddress) {
                    localAddress = ((LocalAddress) o);
                    if (localAddress.getAddress() != null) {
                        s = localAddress.getAddress().getvalue();
                    }
                } else if (o instanceof AddressProvider) {
                    addressProvider = ((AddressProvider) o);
                    nameList = addressProvider.getClassNameOrClassFactoryNameOrMethodName();
                    for (Object nameObj : nameList) {
                        
                    }
                }
            }
        }
        return s;
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfLocalAddress = new javax.swing.JTextField();
        tfLocalPort = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TcpAcceptorPanel.class, "TcpAcceptorPanel.border.title"))); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TcpAcceptorPanel.class, "TcpAcceptorPanel.jPanel1.border.title"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TcpAcceptorPanel.class, "TcpAcceptorPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(TcpAcceptorPanel.class, "TcpAcceptorPanel.jLabel2.text")); // NOI18N

        tfLocalAddress.setText(org.openide.util.NbBundle.getMessage(TcpAcceptorPanel.class, "TcpAcceptorPanel.tfLocalAddress.text")); // NOI18N

        tfLocalPort.setText(org.openide.util.NbBundle.getMessage(TcpAcceptorPanel.class, "TcpAcceptorPanel.tfLocalPort.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfLocalPort, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                    .addComponent(tfLocalAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfLocalAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfLocalPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField tfLocalAddress;
    private javax.swing.JTextField tfLocalPort;
    // End of variables declaration//GEN-END:variables
}
