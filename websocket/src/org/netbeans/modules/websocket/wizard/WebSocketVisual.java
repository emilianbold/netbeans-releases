/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websocket.wizard;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author den
 */
class WebSocketVisual extends javax.swing.JPanel {

    WebSocketVisual(WizardDescriptor myDescriptor) {
        initComponents();
        myUri.setText("/endpoint");         // NOI18N
        myListeners = new CopyOnWriteArrayList<ChangeListener>();
    }
    
    void addChangeListener( ChangeListener listener ) {
        myListeners.add(listener);
    }

    String getError() {
        if ( myUri.getText().trim().length() == 0 ){
            return NbBundle.getMessage(WebSocketVisual.class, "ERR_EmptyUri");  // NOI18N
        }
        return null;
    }

    void readSettings( WizardDescriptor descriptor ) {
        // TODO Auto-generated method stub
        
    }

    void removeChangeListener( ChangeListener listener ) {
        myListeners.remove(listener);
    }

    void storeSettings( WizardDescriptor descriptor ) {
        descriptor.putProperty(WebSocketPanel.URI, myUri.getText());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myPathLbl = new javax.swing.JLabel();
        myUri = new javax.swing.JTextField();

        myPathLbl.setLabelFor(myUri);
        org.openide.awt.Mnemonics.setLocalizedText(myPathLbl, org.openide.util.NbBundle.getMessage(WebSocketVisual.class, "LBL_URI")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(myPathLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(myUri, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(myPathLbl)
                    .addComponent(myUri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        myPathLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebSocketVisual.class, "ACSN_URI")); // NOI18N
        myPathLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebSocketVisual.class, "ACSD_URI")); // NOI18N
        myUri.getAccessibleContext().setAccessibleName(myPathLbl.getAccessibleContext().getAccessibleName());
        myUri.getAccessibleContext().setAccessibleDescription(myPathLbl.getAccessibleContext().getAccessibleDescription());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel myPathLbl;
    private javax.swing.JTextField myUri;
    // End of variables declaration//GEN-END:variables

    private List<ChangeListener> myListeners;
}
