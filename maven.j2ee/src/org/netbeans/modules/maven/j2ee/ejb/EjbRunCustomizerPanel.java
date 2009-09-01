/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.j2ee.POHImpl;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.SessionContent;
import org.netbeans.modules.maven.j2ee.Wrapper;


/**
 *
 * @author  mkleint
 */
public class EjbRunCustomizerPanel extends javax.swing.JPanel {

    private Project project;
    private ModelHandle handle;
    private EjbJar module;
    private ComboBoxUpdater<Wrapper> listener;

    /**
     * Creates new form EjbRunCustomizerPanel
     */
    public EjbRunCustomizerPanel(ModelHandle handle, Project project) {
        initComponents();
        this.handle = handle;
        this.project = project;
        module = EjbJar.getEjbJar(project.getProjectDirectory());
        loadComboModel();
        if (module != null) {
            txtJ2EEVersion.setText(module.getJ2eePlatformVersion());
        }
        initValues();
    }

    private void initValues() {
        listener = Wrapper.createComboBoxUpdater(handle, comServer, lblServer);
    }

    private void loadComboModel() {
        String[] ids = Deployment.getDefault().getServerInstanceIDs(Collections.singleton(J2eeModule.Type.EJB));
        Collection<Wrapper> col = new ArrayList<Wrapper>();
//        Wrapper selected = null;
        SessionContent sc = project.getLookup().lookup(SessionContent.class);
        if (sc != null && sc.getServerInstanceId() != null) {
            col.add(new Wrapper(ExecutionChecker.DEV_NULL, sc.getServerInstanceId()));
        } else {
            col.add(new Wrapper(ExecutionChecker.DEV_NULL));
        }
        for (int i = 0; i < ids.length; i++) {
            Wrapper wr = new Wrapper(ids[i]);
            col.add(wr);
//            if (selectedId.equals(ids[i])) {
//                selected = wr;
//            }
        }
        comServer.setModel(new DefaultComboBoxModel(col.toArray()));
//        if (selected != null) {
//            comServer.setSelectedItem(selected);
//        }
    }

    void applyChangesInAWT() {
        // USG logging
        Object obj = comServer.getSelectedItem();
        if (obj != null) {
            LogRecord record = new LogRecord(Level.INFO, "USG_PROJECT_CONFIG_MAVEN_SERVER");  //NOI18N
            record.setLoggerName(POHImpl.USG_LOGGER_NAME);
            record.setParameters(new Object[] { obj.toString() });
            POHImpl.USG_LOGGER.log(record);
        }
    }


    void applyChanges() {
        //#109507 workaround -
        SessionContent sc = project.getLookup().lookup(SessionContent.class);
        sc.setServerInstanceId(null);
        //TODO - not sure this is necessary since the PoHImpl listens on project changes.
        //any save of teh project shall effectively caus ethe module server change..
        POHImpl poh = project.getLookup().lookup(POHImpl.class);
        poh.hackModuleServerChange();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblServer = new javax.swing.JLabel();
        comServer = new javax.swing.JComboBox();
        lblJ2EEVersion = new javax.swing.JLabel();
        txtJ2EEVersion = new javax.swing.JTextField();

        lblServer.setText(org.openide.util.NbBundle.getMessage(EjbRunCustomizerPanel.class, "LBL_Server")); // NOI18N

        lblJ2EEVersion.setText(org.openide.util.NbBundle.getMessage(EjbRunCustomizerPanel.class, "LBL_J2EE_Version")); // NOI18N

        txtJ2EEVersion.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblJ2EEVersion)
                    .add(lblServer))
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comServer, 0, 277, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, txtJ2EEVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblServer)
                    .add(comServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblJ2EEVersion)
                    .add(txtJ2EEVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(239, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comServer;
    private javax.swing.JLabel lblJ2EEVersion;
    private javax.swing.JLabel lblServer;
    private javax.swing.JTextField txtJ2EEVersion;
    // End of variables declaration//GEN-END:variables

}