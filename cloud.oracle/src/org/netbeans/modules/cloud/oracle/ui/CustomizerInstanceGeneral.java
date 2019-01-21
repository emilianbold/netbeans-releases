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

/*
 * CustomizerInstanceGeneral.java
 *
 * Created on Sep 1, 2011, 12:24:44 PM
 */
package org.netbeans.modules.cloud.oracle.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.cloud.oracle.OracleInstanceManager;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleJ2EEInstance;
import org.netbeans.modules.j2ee.weblogic9.DomainSupport;
import org.netbeans.modules.j2ee.weblogic9.DomainSupport.WLDomain;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class CustomizerInstanceGeneral extends javax.swing.JPanel implements HelpCtx.Provider {

    public static final String FAKE_EE5_INSTANCE_ID = "javaee-api-5.0";

    /** Creates new form CustomizerInstanceGeneral */
    public CustomizerInstanceGeneral(final OracleJ2EEInstance aij) {
        initComponents();
        
        Collection<DomainSupport.WLDomain> domains = DomainSupport.getUsableDomainInstances(null);

        List<ComboItem> items = new ArrayList<ComboItem>(domains.size() + 1);
        ComboItem selected = null;
        for (WLDomain d : domains) {
            ComboItem item = new ComboItem(null, d);
            items.add(item);
            if (d.getUrl().equals(aij.getOracleInstance().getOnPremiseServerInstanceId())) {
                selected = item;
            }
        }

        ComboItem fallback = null;
        Library l = LibraryManager.getDefault().getLibrary("javaee-api-5.0"); // NOI18N
        if (l != null) {
            fallback = new ComboItem(NbBundle.getMessage(CustomizerInstanceGeneral.class,
                    "CustomizerInstanceGeneral.javaee5"), null);
            items.add(fallback);
        }

        if (selected == null && FAKE_EE5_INSTANCE_ID.equals(
                aij.getOracleInstance().getOnPremiseServerInstanceId())) {
            selected = fallback;
        }

        boolean noServers = domains.isEmpty();
        if (selected == null) {
            if (aij.getOracleInstance().getOnPremiseServerInstanceId() != null) {
                aij.getOracleInstance().setOnPremiseServerInstanceId(FAKE_EE5_INSTANCE_ID);
                OracleInstanceManager.getDefault().update(aij.getOracleInstance());
            }
            if (fallback == null) {
                items.add(0, null);
            }
        }
        
        classpathComboBox.setModel(new DefaultComboBoxModel(items.toArray()));
        if (selected != null) {
            classpathComboBox.setSelectedItem(selected);
        }
        
        classpathComboBox.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                ComboItem item = (ComboItem) classpathComboBox.getSelectedItem();
                String url = null;
                if (item != null) {
                    if (item.getDomain() != null) {
                        url = item.getDomain().getUrl();
                    } else {
                        url = FAKE_EE5_INSTANCE_ID;
                    }
                }
                aij.getOracleInstance().setOnPremiseServerInstanceId(url);
                OracleInstanceManager.getDefault().update(aij.getOracleInstance());
                return;
            }
        });
        
        warningLabel.setVisible(noServers);
    }

    private static class ComboItem {
        
        private final String name;
        
        private final DomainSupport.WLDomain domain;

        public ComboItem(String name, WLDomain domain) {
            this.name = name;
            this.domain = domain;
        }

        public WLDomain getDomain() {
            return domain;
        }

        public String toString() {
            if (name == null) {
                return domain.toString();
            }
            return name;
        }

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        classpathLabel = new javax.swing.JLabel();
        classpathComboBox = new javax.swing.JComboBox();
        warningLabel = new javax.swing.JLabel();

        classpathLabel.setLabelFor(classpathComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(classpathLabel, org.openide.util.NbBundle.getMessage(CustomizerInstanceGeneral.class, "CustomizerInstanceGeneral.classpathLabel.text")); // NOI18N

        warningLabel.setFont(warningLabel.getFont().deriveFont(warningLabel.getFont().getSize()-2f));
        warningLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerInstanceGeneral.class, "CustomizerInstanceGeneral.warningLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(classpathLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(warningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(classpathComboBox, 0, 245, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classpathLabel)
                    .addComponent(classpathComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(55, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox classpathComboBox;
    private javax.swing.JLabel classpathLabel;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerInstanceGeneral.class.getName());
    }
}
