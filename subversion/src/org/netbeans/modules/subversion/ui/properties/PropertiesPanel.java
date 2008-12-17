/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.subversion.ui.properties;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static org.jdesktop.layout.GroupLayout.BASELINE;
import static org.jdesktop.layout.GroupLayout.DEFAULT_SIZE;
import static org.jdesktop.layout.GroupLayout.HORIZONTAL;
import static org.jdesktop.layout.GroupLayout.LEADING;
import static org.jdesktop.layout.GroupLayout.PREFERRED_SIZE;
import static org.jdesktop.layout.LayoutStyle.RELATED;

/**
 *
 * @author  Peter Pis
 * @author  Marian Petras
 */
public class PropertiesPanel extends JPanel implements PreferenceChangeListener, TableModelListener {

    final JButton btnAdd = new JButton();
    final JButton btnBrowse = new JButton();
    final JButton btnRefresh = new JButton();
    final JButton btnRemove = new JButton();
    final JCheckBox cbxRecursively = new JCheckBox();
    final JComboBox comboName = new JComboBox();
    final JTextArea txtAreaValue = new JTextArea();
    final JPanel propsPanel = new DerivedHeightPanel(txtAreaValue, 2.0f);
    final JLabel labelForTable = new JLabel();

    private static final Object EVENT_SETTINGS_CHANGED = new Object();
    private PropertiesTable propertiesTable;
    private ListenersSupport listenerSupport = new ListenersSupport(this);
    private final JLabel lblErrMessage = new JLabel();

    /** Creates new form PropertiesPanel */
    public PropertiesPanel() {
        initComponents();
    }
    
    void setPropertiesTable(PropertiesTable propertiesTable){
        this.propertiesTable = propertiesTable;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);        
        propertiesTable.getTableModel().addTableModelListener(this);
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        txtAreaValue.selectAll();
    }

    @Override
    public void removeNotify() {
        propertiesTable.getTableModel().removeTableModelListener(this);
        SvnModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        super.removeNotify();
    }
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(SvnModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            propertiesTable.dataChanged();
            listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        }
    }

    public void tableChanged(TableModelEvent e) {
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
    }
    
    // <editor-fold defaultstate="collapsed" desc="UI Definition Code">
    private void initComponents() {
        JLabel lblPropertyName = new JLabel();
        lblPropertyName.setLabelFor(comboName);
        Mnemonics.setLocalizedText(lblPropertyName, getString("PropertiesPanel.jLabel2.text")); // NOI18N

        JLabel lblPropertyValue = new JLabel();
        lblPropertyValue.setLabelFor(txtAreaValue);
        Mnemonics.setLocalizedText(lblPropertyValue, getString("PropertiesPanel.jLabel1.text")); // NOI18N

        txtAreaValue.setColumns(20);
        txtAreaValue.setRows(5);
        JScrollPane jScrollPane1 = new JScrollPane();
        jScrollPane1.setViewportView(txtAreaValue);

        lblErrMessage.setForeground(Color.RED);
        lblErrMessage.setVisible(false);
        lblErrMessage.setText(" ");  //to get non-zero preferred height //NOI18N

        Mnemonics.setLocalizedText(btnBrowse, getString("PropertiesPanel.btnBrowse.text")); // NOI18N
        btnBrowse.setActionCommand(getString("btnBrowse.actionCommand")); // NOI18N

        JSeparator jSeparator1 = new JSeparator();

        Mnemonics.setLocalizedText(btnAdd, getString("PropertiesPanel.btnAdd.text")); // NOI18N

        Mnemonics.setLocalizedText(cbxRecursively, getString("PropertiesPanel.cbxRecursively.text")); // NOI18N
        cbxRecursively.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        Mnemonics.setLocalizedText(btnRemove, getString("PropertiesPanel.btnRemove.text")); // NOI18N
        btnRemove.setActionCommand(getString("btnRemove.actionCommand")); // NOI18N

        Mnemonics.setLocalizedText(btnRefresh, getString("PropertiesPanel.btnRefresh.text")); // NOI18N
        btnRefresh.setActionCommand(getString("btnRefresh.actionCommand")); // NOI18N

        Mnemonics.setLocalizedText(labelForTable, getString("jLabel3.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(LEADING)
                                .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup()
                                                .add(lblPropertyName)
                                                .add(lblPropertyValue))
                                        .addPreferredGap(RELATED)
                                        .add(layout.createParallelGroup()
                                                .add(comboName, 0, DEFAULT_SIZE, DEFAULT_SIZE)
                                                .add(jScrollPane1)))
                                .add(layout.createSequentialGroup()
                                        .add(lblErrMessage, 0, DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(RELATED)
                                        .add(btnBrowse))
                                .add(jSeparator1)
                                .add(layout.createSequentialGroup()
                                        .add(btnAdd)
                                        .addPreferredGap(RELATED)
                                        .add(cbxRecursively)
                                        .addPreferredGap(cbxRecursively, btnRemove, RELATED, true)
                                        .add(btnRemove)
                                        .addPreferredGap(RELATED)
                                        .add(btnRefresh))
                                .add(labelForTable)
                                .add(propsPanel))
                        .addContainerGap()
        );
        layout.linkSize(new Component[] {btnAdd, btnBrowse, btnRefresh, btnRemove}, HORIZONTAL);
        layout.setHonorsVisibility(false);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(BASELINE)
                                .add(lblPropertyName)
                                .add(comboName))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(LEADING)
                                .add(lblPropertyValue)
                                .add(jScrollPane1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(lblErrMessage)
                                .add(btnBrowse))
                        .addPreferredGap(RELATED)
                        .add(jSeparator1, PREFERRED_SIZE, 10, PREFERRED_SIZE)
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(btnAdd)
                                .add(cbxRecursively)
                                .add(btnRemove)
                                .add(btnRefresh))
                        .add(18)
                        .add(labelForTable)
                        .addPreferredGap(RELATED)
                        .add(propsPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap()
        );

        lblPropertyName.getAccessibleContext().setAccessibleDescription(getString("jLabel2.AccessibleContext.accessibleDescription")); // NOI18N
        lblPropertyValue.getAccessibleContext().setAccessibleDescription(getString("jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        comboName.getAccessibleContext().setAccessibleName(getString("comboName.AccessibleContext.accessibleName")); // NOI18N
        comboName.getAccessibleContext().setAccessibleDescription(getString("comboName.AccessibleContext.accessibleDescription")); // NOI18N

        txtAreaValue.getAccessibleContext().setAccessibleName(getString("txtAreaValue.AccessibleContext.accessibleName")); // NOI18N
        txtAreaValue.getAccessibleContext().setAccessibleDescription(getString("txtAreaValue.AccessibleContext.accessibleDescription")); // NOI18N

        btnBrowse.getAccessibleContext().setAccessibleDescription(getString("btnBrowse.AccessibleContext.accessibleDescription")); // NOI18N

        btnAdd.getAccessibleContext().setAccessibleDescription(getString("btnAdd.AccessibleContext.accessibleDescription")); // NOI18N
        cbxRecursively.getAccessibleContext().setAccessibleDescription(getString("cbxRecursively.AccessibleContext.accessibleDescription")); // NOI18N
        btnRemove.getAccessibleContext().setAccessibleDescription(getString("btnRemove.AccessibleContext.accessibleDescription")); // NOI18N
        btnRefresh.getAccessibleContext().setAccessibleDescription(getString("btnRefresh.AccessibleContext.accessibleDescription")); // NOI18N

        labelForTable.getAccessibleContext().setAccessibleDescription(getString("labelForTable.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>

    void setErrMessage(String message) {
        if (message == null) {
            lblErrMessage.setText(" ");                                 //NOI18N
            lblErrMessage.setVisible(false);
        } else {
            lblErrMessage.setText(message);
            lblErrMessage.setVisible(true);
            int widthReserve = lblErrMessage.getSize().width - lblErrMessage.getPreferredSize().width;
            if (widthReserve < 0) {
                makeDialogWider(-widthReserve);
            }
        }
    }

    private void makeDialogWider(int delta) {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            Dimension size = w.getSize();
            size.width += delta;
            w.setSize(size);
        }
    }

    private static String getString(String msgKey) {
        return NbBundle.getMessage(PropertiesPanel.class, msgKey);
    }

    /**
     * Panel whose preferred height is derived from preferred height
     * of another component.
     */
    private final class DerivedHeightPanel extends JPanel {
        private final Component deriveFrom;
        private final float ratio;
        private Dimension prefSize = null;
        private DerivedHeightPanel(Component deriveFrom, float ratio) {
            this.deriveFrom = deriveFrom;
            this.ratio = ratio;
        }
        @Override
        public Dimension getPreferredSize() {
            if (prefSize == null) {
                prefSize = computePrefSize();
            }
            return prefSize;
        }
        private Dimension computePrefSize() {
            int prefHeight = Math.round(
                                 ratio * deriveFrom.getPreferredSize().height);
            return new Dimension(0, prefHeight);
        }
    }

}
