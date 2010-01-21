/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

/*
 * GdbServerAttachPanel.java
 *
 * Created on Aug 14, 2009, 7:39:50 PM
 */

package org.netbeans.modules.cnd.debugger.gdb.attach;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.debugger.gdb.DebuggerStartException;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.attach.GdbAttachPanel.ProjectCBItem;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Egor Ushakov
 */
public class GdbServerAttachPanel extends javax.swing.JPanel {
    private final Controller controller;

    private static final String HOST_KEY = "last-gdbserver-host"; //NOI18N
    private static final String PORT_KEY = "last-gdbserver-port"; //NOI18N

    /** Creates new form GdbServerAttachPanel */
    public GdbServerAttachPanel() {
        controller = new GdbServerAttachController();
        initComponents();
        // Fill the Projects combo box
        GdbAttachPanel.fillProjectsCombo(projectCB);
        hostTF.setText(NbPreferences.forModule(GdbServerAttachPanel.class).get(HOST_KEY, "")); //NOI18N
        portTF.setText(NbPreferences.forModule(GdbServerAttachPanel.class).get(PORT_KEY, "")); //NOI18N
    }

    Controller getController() {
        return controller;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectLabel = new javax.swing.JLabel();
        projectCB = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        hostTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        portTF = new javax.swing.JTextField();

        projectLabel.setText(org.openide.util.NbBundle.getMessage(GdbServerAttachPanel.class, "GdbServerAttachPanel.projectLabel.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(GdbServerAttachPanel.class, "GdbServerAttachPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(GdbServerAttachPanel.class, "GdbServerAttachPanel.jLabel2.text")); // NOI18N

        portTF.setText(org.openide.util.NbBundle.getMessage(GdbServerAttachPanel.class, "GdbServerAttachPanel.portTF.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectLabel)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(portTF, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                    .addComponent(hostTF, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                    .addComponent(projectCB, 0, 335, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(hostTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(portTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField hostTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField portTF;
    private javax.swing.JComboBox projectCB;
    private javax.swing.JLabel projectLabel;
    // End of variables declaration//GEN-END:variables

    private class GdbServerAttachController implements Controller {

        public boolean cancel() {
            return true;
        }

        public boolean ok() {
            String hostValue = hostTF.getText();
            if (hostValue.length() == 0) {
                return false;
            }
            String portValue = portTF.getText();
            if (portValue.length() == 0) {
                return false;
            }
            
            //store last values
            NbPreferences.forModule(GdbServerAttachPanel.class).put(HOST_KEY, hostValue);
            NbPreferences.forModule(GdbServerAttachPanel.class).put(PORT_KEY, portValue);

            ProjectCBItem pi = (ProjectCBItem) projectCB.getSelectedItem();
            if (pi != null) {
                String target = hostValue + ':' + portValue;
                try {
                    GdbDebugger.attachGdbServer(target, pi.getProjectInformation());
                } catch (DebuggerStartException dse) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(GdbServerAttachPanel.class,
                           "ERR_UnexpecedAttachGdbServerFailure", target))); // NOI18N
                }
            }
            return true;
        }
        
        @Override
        public boolean isValid() {
            return projectCB.getItemCount() > 0;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

    }
}
