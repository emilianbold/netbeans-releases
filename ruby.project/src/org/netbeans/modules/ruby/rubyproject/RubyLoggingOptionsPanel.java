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
package org.netbeans.modules.ruby.rubyproject;

import javax.swing.JPanel;

/**
 * Panel for Ruby IDE logging options.
 *
 * @author Erno Mononen
 */
public final class RubyLoggingOptionsPanel extends JPanel {

    /** Creates new form RubyLoggingOptionsPanel */
    public RubyLoggingOptionsPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loggingLabel = new javax.swing.JLabel();
        standardLogging = new javax.swing.JCheckBox();
        debuggerLogging = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();

        loggingLabel.setText(org.openide.util.NbBundle.getMessage(RubyLoggingOptionsPanel.class, "RubyLoggingOptionsPanel.loggingLabel.text")); // NOI18N

        standardLogging.setText(org.openide.util.NbBundle.getMessage(RubyLoggingOptionsPanel.class, "RubyLoggingOptionsPanel.standardLogging.text")); // NOI18N
        standardLogging.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                standardLoggingItemStateChanged(evt);
            }
        });

        debuggerLogging.setText(org.openide.util.NbBundle.getMessage(RubyLoggingOptionsPanel.class, "RubyLoggingOptionsPanel.debuggerLogging.text")); // NOI18N
        debuggerLogging.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                debuggerLoggingItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(loggingLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                    .add(standardLogging)
                    .add(debuggerLogging))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(loggingLabel))
                    .add(layout.createSequentialGroup()
                        .add(19, 19, 19)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(standardLogging)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(debuggerLogging)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        standardLogging.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyLoggingOptionsPanel.class, "AD_Options_StandardLogging")); // NOI18N
        debuggerLogging.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyLoggingOptionsPanel.class, "AD_Options_DebuggerLogging")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void standardLoggingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_standardLoggingItemStateChanged
    }//GEN-LAST:event_standardLoggingItemStateChanged

    private void debuggerLoggingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_debuggerLoggingItemStateChanged
    }//GEN-LAST:event_debuggerLoggingItemStateChanged

    void setStandardLogging(boolean enabled) {
        standardLogging.setSelected(enabled);
    }

    void enableStandardLoggingCheckBox(boolean enabled) {
        standardLogging.setEnabled(enabled);
    }

    void setDebuggerLogging(boolean enabled) {
        debuggerLogging.setSelected(enabled);
    }

    void enableDebuggerLoggingCheckBox(boolean enabled) {
        debuggerLogging.setEnabled(enabled);
    }

    boolean isStandardLoggingEnabled() {
        return standardLogging.isSelected();
    }

    boolean isDebuggerLoggingEnabled() {
        return debuggerLogging.isSelected();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox debuggerLogging;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel loggingLabel;
    private javax.swing.JCheckBox standardLogging;
    // End of variables declaration//GEN-END:variables

}
