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
 * RemoveInstrumentationPanel.java
 *
 * Created on Sep 14, 2009, 3:33:07 PM
 */

package org.netbeans.modules.cnd.tha.support;

import java.awt.Color;
import java.awt.Dialog;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class RemoveInstrumentationPanel extends javax.swing.JPanel {

    /** Creates new form RemoveInstrumentationPanel */
    private RemoveInstrumentationPanel(String projectName) {
        initComponents();
        message.setBackground(getBackground());
        message.setContentType("text/html"); // NOI18N
        message.setText(NbBundle.getMessage(RemoveInstrumentationPanel.class, "RemoveInstrumentationPanel.info.text", projectName)); // NOI18N
    }

    public static boolean showDialog(Project project){
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        String projectName = nativeProject.getProjectDisplayName();
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();
        int decision = mc.getProfile().getRemoveInstrumentation().getValue();
        boolean isRemoveInstrumentation;
        if (decision == RunProfile.REMOVE_INSTRUMENTATION_ASK) {
            RemoveInstrumentationPanel panel = new RemoveInstrumentationPanel(projectName);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(RemoveInstrumentationPanel.class, "TITLE_RemoveInstrumentation"), true,
                        DialogDescriptor.YES_NO_OPTION, DialogDescriptor.YES_OPTION, null);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setVisible(true);
            if (dd.getValue() == DialogDescriptor.YES_OPTION) {
                isRemoveInstrumentation = true;
            } else {
                isRemoveInstrumentation = false;
            }
            if (panel.getDontAskMeStatus()) {
                if (isRemoveInstrumentation) {
                    mc.getProfile().getRemoveInstrumentation().setValue(RunProfile.REMOVE_INSTRUMENTATION_YES);
                } else {
                    mc.getProfile().getRemoveInstrumentation().setValue(RunProfile.REMOVE_INSTRUMENTATION_NO);
                }
                mcd.setModified();
            }
        } else {
            isRemoveInstrumentation = decision == RunProfile.REMOVE_INSTRUMENTATION_YES;
        }
        return isRemoveInstrumentation;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        message = new javax.swing.JTextPane();
        dontAsk = new javax.swing.JCheckBox();

        setBackground(getBackground());
        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 100));

        message.setBorder(null);
        jScrollPane1.setViewportView(message);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 6);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(dontAsk, org.openide.util.NbBundle.getMessage(RemoveInstrumentationPanel.class, "RemoveInstrumentationPanel.dontAsk.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(dontAsk, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    public boolean getDontAskMeStatus(){
        return dontAsk.isSelected();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox dontAsk;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane message;
    // End of variables declaration//GEN-END:variables

}
