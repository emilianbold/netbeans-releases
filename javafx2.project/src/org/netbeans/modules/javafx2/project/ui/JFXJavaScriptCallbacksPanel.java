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
 * JFXJavaScriptCallbacksPanel.java
 *
 * Created on 26.8.2011, 12:02:29
 */
package org.netbeans.modules.javafx2.project.ui;

import java.util.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.modules.javafx2.project.JFXProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Somol
 */
public class JFXJavaScriptCallbacksPanel extends javax.swing.JPanel {

    private final JFXProjectProperties props;
    
    /** Creates new form JFXJavaScriptCallbacksPanel */
    public JFXJavaScriptCallbacksPanel(JFXProjectProperties props) {
        this.props = props;
        initComponents();
        this.tableCallbacks.setModel(createModel(createResources()));
        this.tableCallbacks.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    Map<String,String> getResources() {
        final TableModel model = this.tableCallbacks.getModel();
        final Map<String,String> result = new TreeMap<String,String>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0) != null && !((String)model.getValueAt(i, 0)).isEmpty()) {
                result.put((String)model.getValueAt(i, 0), (String)model.getValueAt(i, 1));
            }
        }
        return result;
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

        labelCallbacks = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableCallbacks = new javax.swing.JTable();

        setPreferredSize(new java.awt.Dimension(410, 300));
        setLayout(new java.awt.GridBagLayout());

        labelCallbacks.setLabelFor(tableCallbacks);
        org.openide.awt.Mnemonics.setLocalizedText(labelCallbacks, org.openide.util.NbBundle.getMessage(JFXJavaScriptCallbacksPanel.class, "LBL_JFXJavaScriptCallbacksPanel.labelCallbacks.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 0);
        add(labelCallbacks, gridBagConstraints);
        labelCallbacks.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXJavaScriptCallbacksPanel.class, "AN_JFXJavaScriptCallbacksPanel.labelCallbacks.text")); // NOI18N
        labelCallbacks.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXJavaScriptCallbacksPanel.class, "AD_JFXJavaScriptCallbacksPanel.labelCallbacks.text")); // NOI18N

        tableCallbacks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Callback", "Definition"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tableCallbacks);
        tableCallbacks.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(JFXJavaScriptCallbacksPanel.class, "JFXJavaScriptCallbacksPanel.tableCallbacks.columnModel.title0")); // NOI18N
        tableCallbacks.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(JFXJavaScriptCallbacksPanel.class, "JFXJavaScriptCallbacksPanel.tableCallbacks.columnModel.title1")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelCallbacks;
    private javax.swing.JTable tableCallbacks;
    // End of variables declaration//GEN-END:variables

    private static TableModel createModel(final Map<String,String> callbacks) {
        final Object[][] data = new Object[callbacks.size()][];
        final Iterator<Map.Entry<String,String>> it = callbacks.entrySet().iterator();
        for (int i=0; it.hasNext(); i++) {
            final Map.Entry<String,String> entry = it.next();
            data[i] = new Object[] {entry.getKey(),entry.getValue()};
        }
        return new DefaultTableModel(
            data,
            new String[] {
                NbBundle.getMessage(JFXDownloadModePanel.class, "JFXJavaScriptCallbacksPanel.tableCallbacks.columnModel.title0"), //NOI18N
                NbBundle.getMessage(JFXDownloadModePanel.class, "JFXJavaScriptCallbacksPanel.tableCallbacks.columnModel.title1") //NOI18N
            }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    default:
                        throw new IllegalStateException();
                }
            }
            
        };
    }
    
    private Map<String,String/*|null*/> createResources() {
        PropertyEvaluator eval = props.getEvaluator();
        String platformName = eval.getProperty(JFXProjectProperties.PLATFORM_ACTIVE);
        Map<String,List<String>/*|null*/> callbacks = JFXProjectUtils.getJSCallbacks(platformName);
        Map<String,String/*|null*/> jsCallbacks = props.getJSCallbacks();
        Map<String,String/*|null*/> result = new LinkedHashMap<String,String/*|null*/>();
        for(Map.Entry<String,List<String>/*|null*/> entry : callbacks.entrySet()) {
            String v = jsCallbacks.get(entry.getKey());
            //String v = eval.getProperty(JFXProjectProperties.JAVASCRIPT_CALLBACK_PREFIX + entry.getKey());
            result.put(entry.getKey(), v);
        }
        return result;
    }    
}
