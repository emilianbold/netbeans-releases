/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.rest.projects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  nam
 */
public class RestWebCustomizerPanel extends javax.swing.JPanel implements ActionListener {

    private static final String RESTAPT_PREFIX = "rest.apt.";
    private static final int COLUMN_KEY = 0;
    private static final int COLUMN_VALUE = 1;
    private static final int COLUMN_DEFAULT = 2;
    public static final String[] COLUMN_NAMES = {
        NbBundle.getMessage(RestWebCustomizerPanel.class, "LBL_Key"),
        NbBundle.getMessage(RestWebCustomizerPanel.class, "LBL_Value"),
        NbBundle.getMessage(RestWebCustomizerPanel.class, "LBL_Default")};

    private final RestSupport support;

    /** Creates new form RestWebCustomizerPanel */
    public RestWebCustomizerPanel(RestSupport support) {
        assert support != null : "Null restSupport";
        this.support = support;
        initComponents();
        optionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int row = optionsTable.getSelectedRow();
                String desc = ""; //NOI18N
                if (row > -1 && row < getOptions().length) {
                    desc = getOptions()[row].getDescription();
                }
                optionDescription.setText("<html>"+desc+"</html>");
            }
        });
    }

    private class OptionsModel extends DefaultTableModel {

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public int getRowCount() {
            return getOptions().length;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 1 ? Boolean.class : super.getColumnClass(column);
        }

        @Override
        public Object getValueAt(int row, int column) {
            CustomizerOption co = getOptions()[row];
            if (column == COLUMN_KEY) {
                return co.getName();
            } else if (column == COLUMN_VALUE) {
                Object value = co.getValue();
                if (value == null) {
                    return co.getDefaultValue();
                }
                return value;
            } else if (column == COLUMN_DEFAULT) {
                return co.getDefaultValue();
            }
            return super.getValueAt(row, column);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public void setValueAt(Object v, int row, int column) {
            CustomizerOption co = getOptions()[row];
            if (column == 1) {
                if (co.getType().isAssignableFrom(v.getClass())) {
                    co.setValue(v);
                }
            }
            super.setValueAt(v, row, column);
        }
    }

    CustomizerOption[] options;
    private CustomizerOption[] getOptions() {
        if (options == null) {
            options = CustomizerOption.createOptions();
            for (CustomizerOption co : options) {
                String name = RESTAPT_PREFIX + co.getName();
                String s = support.getProjectProperty(name);
                if (s != null) {
                    if (Boolean.class.isAssignableFrom(co.getType())) {
                        co.setValue(Boolean.valueOf(s));
                    } else {
                        co.setValue(s);
                    }
                }
            }
        }
        return options;
    }
    
    public void actionPerformed(ActionEvent e) {
        for (CustomizerOption co : getOptions()) {
            String name = RESTAPT_PREFIX + co.getName();
            if (co.getValue() != null) {
                support.setProjectProperty(name, co.getValue().toString());
            }
        }
        
        try {
            new AntFilesHelper(support).refreshRestBuildXml();
            ProjectManager.getDefault().saveProject(support.getProject());
        } catch(IOException ex) {
            Logger.getLogger(getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        optionsTable = new javax.swing.JTable();
        aptOptionsLabel = new javax.swing.JLabel();
        optionDescription = new javax.swing.JLabel();

        jScrollPane1.setOpaque(false);

        optionsTable.setModel(new OptionsModel());
        optionsTable.setGridColor(new java.awt.Color(255, 255, 255));
        optionsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(optionsTable);

        aptOptionsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/rest/wizard/Bundle").getString("MNE_RestAptOptions").charAt(0));
        aptOptionsLabel.setLabelFor(optionsTable);
        aptOptionsLabel.setText(org.openide.util.NbBundle.getMessage(RestWebCustomizerPanel.class, "RestWebCustomizerPanel.aptOptionsLabel.text")); // NOI18N

        optionDescription.setText(org.openide.util.NbBundle.getMessage(RestWebCustomizerPanel.class, "RestWebCustomizerPanel.optionDescription.text")); // NOI18N
        optionDescription.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, optionDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, aptOptionsLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(aptOptionsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aptOptionsLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel optionDescription;
    private javax.swing.JTable optionsTable;
    // End of variables declaration//GEN-END:variables

}
