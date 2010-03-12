/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * Customizer.java
 *
 * Created on 23.Mar 2004, 11:31
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Kaspar
 */
public class CustomizerJad extends JPanel implements CustomizerPanel, VisualPropertyGroup, ListSelectionListener, ActionListener {
    
    static final String[] PROPERTY_GROUP = new String[] { DefaultPropertiesDescriptor.MANIFEST_OTHERS, DefaultPropertiesDescriptor.MANIFEST_JAD, DefaultPropertiesDescriptor.MANIFEST_MANIFEST, DefaultPropertiesDescriptor.DEPLOYMENT_OVERRIDE_JARURL, DefaultPropertiesDescriptor.DEPLOYMENT_JARURL };
    
    final protected JTable table;
    final protected StorableTableModel tableModel;
    private VisualPropertySupport vps;
    
    private String configuration;
    private String configurationProfileValue;
    private String defaultProfileValue;
    
    /** Creates new form CustomizerConfigs */
    public CustomizerJad() {
        initComponents();
        initAccessibility();
        table = new JTable(tableModel = new StorableTableModel());
        scrollPane.setViewportView(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("synthetic-access")
			public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2  &&  e.getButton() == MouseEvent.BUTTON1)
                    bEditActionPerformed(null);
            }
        });
        jCheckBoxOverride.addActionListener(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupModel = new javax.swing.ButtonGroup();
        cDefault = new javax.swing.JCheckBox();
        lTable = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        bAdd = new javax.swing.JButton();
        bEdit = new javax.swing.JButton();
        bRemove = new javax.swing.JButton();
        jCheckBoxOverride = new javax.swing.JCheckBox();
        jTextFieldURL = new javax.swing.JTextField();
        jLabelExpl = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabelModel = new javax.swing.JLabel();
        jRadioButtonSuite = new javax.swing.JRadioButton();
        jRadioButtonLIBlet = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(cDefault, org.openide.util.NbBundle.getMessage(CustomizerJad.class, "LBL_Use_Default")); // NOI18N
        cDefault.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(cDefault, gridBagConstraints);

        lTable.setLabelFor(lTable);
        org.openide.awt.Mnemonics.setLocalizedText(lTable, org.openide.util.NbBundle.getMessage(CustomizerJad.class, "LBL_Jad_Table")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 4);
        add(lTable, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(scrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bAdd, org.openide.util.NbBundle.getMessage(CustomizerJad.class, "LBL_Jad_Add")); // NOI18N
        bAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 11, 0);
        add(bAdd, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bEdit, org.openide.util.NbBundle.getMessage(CustomizerJad.class, "LBL_Jad_Edit")); // NOI18N
        bEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(bEdit, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bRemove, org.openide.util.NbBundle.getMessage(CustomizerJad.class, "LBL_Jad_Remove")); // NOI18N
        bRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(bRemove, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxOverride, NbBundle.getMessage(CustomizerJad.class, "LBL_CustDeploy_OverrideURL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(jCheckBoxOverride, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(jTextFieldURL, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelExpl, NbBundle.getMessage(CustomizerJad.class, "LBL_CustDeploy_URLExpl")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jLabelExpl, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabelModel, org.openide.util.NbBundle.getMessage(CustomizerJad.class, "LBL_CustomizerJad_PackagignModel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabelModel, gridBagConstraints);

        buttonGroupModel.add(jRadioButtonSuite);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonSuite, org.openide.util.NbBundle.getMessage(CustomizerJad.class, "LBL_CustomizerJad_MIDletSuite")); // NOI18N
        jRadioButtonSuite.setActionCommand("false");
        jRadioButtonSuite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonSuiteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(jRadioButtonSuite, gridBagConstraints);

        buttonGroupModel.add(jRadioButtonLIBlet);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonLIBlet, org.openide.util.NbBundle.getMessage(CustomizerJad.class, "LBL_CustomizerJad_LIBlet")); // NOI18N
        jRadioButtonLIBlet.setActionCommand("true");
        jRadioButtonLIBlet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLIBletActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(jRadioButtonLIBlet, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerJad.class, "ACSN_Jad"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJad.class, "ACSD_Jad"));
    }
    
    private void bAddActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddActionPerformed
        final AddAttributePanel add = new AddAttributePanel();
        add.init(false, tableModel, tableModel.getKeys(), null, null);
        final DialogDescriptor dd = new DialogDescriptor(
                add, NbBundle.getMessage(CustomizerJad.class, "TITLE_AddAttribute"), //NOI18N
                true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(AddAttributePanel.class),
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                    int row = tableModel.addRow(add.getKey(), add.getValue(), add.getPlacement());
                    table.getSelectionModel().setSelectionInterval(row, row);
                }
            }
        }
        );
        add.setDialogDescriptor(dd);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }//GEN-LAST:event_bAddActionPerformed
    
    private void bEditActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEditActionPerformed
        final int row = table.getSelectedRow();
        if (row < 0)
            return;
        final String key = (String) tableModel.getValueAt(row, 1);
        final String value = (String) tableModel.getValueAt(row, 2);
        final AddAttributePanel add = new AddAttributePanel();
        add.init(true, tableModel, tableModel.getKeys(), key, value);
        final DialogDescriptor dd = new DialogDescriptor(
                add, NbBundle.getMessage(CustomizerJad.class, "TITLE_EditAttribute"), //NOI18N
                true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(AddAttributePanel.class),
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                    if (key == null  ||  ! key.equals(add.getKey())) {
                        if (key != null)
                            tableModel.removeKey(key);
                        int newrow = tableModel.addRow(add.getKey(), add.getValue(), add.getPlacement());
                        table.getSelectionModel().setSelectionInterval(newrow, newrow);
                    } else {
                        tableModel.editRow(add.getKey(), add.getValue(), add.getPlacement());
                        table.getSelectionModel().setSelectionInterval(row, row);
                    }
                }
            }
        }
        );
        add.setDialogDescriptor(dd);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }//GEN-LAST:event_bEditActionPerformed
    
    private void bRemoveActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRemoveActionPerformed
        final int i = table.getSelectedRow();
        if (i < 0)
            return;
        tableModel.removeRow(i);
        final int max = tableModel.getRowCount();
        if (max <= 0)
            table.getSelectionModel().clearSelection();
        else if (i < max)
            table.getSelectionModel().setSelectionInterval(i, i);
        else
            table.getSelectionModel().setSelectionInterval(max - 1, max - 1);
    }//GEN-LAST:event_bRemoveActionPerformed

    private void jRadioButtonSuiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonSuiteActionPerformed
        tableModel.switchManifestModel(jRadioButtonLIBlet.isSelected());
    }//GEN-LAST:event_jRadioButtonSuiteActionPerformed

    private void jRadioButtonLIBletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLIBletActionPerformed
        tableModel.switchManifestModel(jRadioButtonLIBlet.isSelected());
    }//GEN-LAST:event_jRadioButtonLIBletActionPerformed
    
    public void initValues(ProjectProperties props, String configuration) {
        this.configuration = configuration;
        configurationProfileValue = (String) props.get(VisualPropertySupport.translatePropertyName(configuration, "platform.profile", false));
        defaultProfileValue = (String) props.get("platform.profile"); //NOI18N
        
        vps = VisualPropertySupport.getDefault(props);
        vps.register(cDefault, configuration, this);
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_GROUP;
    }
    
    public void initGroupValues(final boolean useDefault) {
        String value = null;
        if (configuration != null)
            value = configurationProfileValue;
        if (value == null)
            value = defaultProfileValue;
        tableModel.setMIDP(value);
        vps.register(jRadioButtonSuite, DefaultPropertiesDescriptor.MANIFEST_IS_LIBLET, useDefault);
        vps.register(jRadioButtonLIBlet, DefaultPropertiesDescriptor.MANIFEST_IS_LIBLET, useDefault);
        tableModel.initManifestModel(jRadioButtonLIBlet.isSelected());
        vps.register(tableModel, new String[] {DefaultPropertiesDescriptor.MANIFEST_OTHERS, DefaultPropertiesDescriptor.MANIFEST_JAD, DefaultPropertiesDescriptor.MANIFEST_MANIFEST}, useDefault);
        vps.register(jTextFieldURL, DefaultPropertiesDescriptor.DEPLOYMENT_JARURL, useDefault);
        vps.register(jCheckBoxOverride, DefaultPropertiesDescriptor.DEPLOYMENT_OVERRIDE_JARURL, useDefault);
        jLabelModel.setEnabled(!useDefault);
        jRadioButtonLIBlet.setEnabled(!useDefault && tableModel.getMIDPVersion() == 3);
        bAdd.setEnabled(!useDefault);
        bEdit.setEnabled(!useDefault);
        bRemove.setEnabled(!useDefault);
        table.setEnabled(!useDefault);
        lTable.setEnabled(!useDefault);
        jLabelExpl.setEnabled(!useDefault);
        table.setBackground(javax.swing.UIManager.getDefaults().getColor(useDefault ? "Panel.background" : "Table.background")); //NOI18N
        tableModel.switchManifestModel(jRadioButtonLIBlet.isSelected());
        valueChanged(null);
        actionPerformed(null);
    }

    public void actionPerformed(final ActionEvent e) {
        jTextFieldURL.setEditable(jCheckBoxOverride.isEnabled() && jCheckBoxOverride.isSelected());
    }
    
    public void valueChanged(@SuppressWarnings("unused")
	final javax.swing.event.ListSelectionEvent e) {
        final int row = table.getSelectedRow();
        final boolean enabled = table.isEnabled()  &&  row >= 0;
        bEdit.setEnabled(enabled);
        bRemove.setEnabled(enabled  &&  ! tableModel.containsInMandatory((String) tableModel.getValueAt(row, 1)));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bEdit;
    private javax.swing.JButton bRemove;
    private javax.swing.ButtonGroup buttonGroupModel;
    private javax.swing.JCheckBox cDefault;
    private javax.swing.JCheckBox jCheckBoxOverride;
    private javax.swing.JLabel jLabelExpl;
    private javax.swing.JLabel jLabelModel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButtonLIBlet;
    private javax.swing.JRadioButton jRadioButtonSuite;
    private javax.swing.JTextField jTextFieldURL;
    private javax.swing.JLabel lTable;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
    public static class StorableTableModel extends AbstractTableModel implements VisualPropertySupport.StorableTableModel {
        
        
        private HashMap<String,String> map1 = new HashMap<String,String>(), 
        	map2 = new HashMap<String,String>(), 
        	map3 = new HashMap<String,String>();
        final private ArrayList<String> items = new ArrayList<String>();
        private int midpVersion = 1;
        private boolean isLIBlet;

        private String[] additionalAttributes = null;

        final private static String NAME="MIDlet-Name"; //NOI18N
        final private static String VENDOR="MIDlet-Vendor"; //NOI18N
        final private static String VERSION="MIDlet-Version"; //NOI18N
        final private static String ICON="MIDlet-Icon"; //NOI18N
        final private static String DESCRIPTION="MIDlet-Description"; //NOI18N
        final private static String INFOURL="MIDlet-Info-URL"; //NOI18N
        final private static String DATASIZE="MIDlet-Data-Size"; //NOI18N
        final private static String MECONFIG="MicroEdition-Configuration"; //NOI18N
        final private static String MEPROFILE="MicroEdition-Profile"; //NOI18N
        final private static String INSTALL="MIDlet-Install-Notify"; //NOI18N
        final private static String DELETE="MIDlet-Delete-Notify"; //NOI18N
        final private static String CONFIRM="MIDlet-Delete-Confirm"; //NOI18N

        final private static String FONT="MIDlet-Font"; //NOI18N
        final private static String MINCANVAS="MIDlet-Minimum-Canvas-Size"; //NOI18N
        final private static String MAXCANVAS="MIDlet-Maximum-Canvas-Size"; //NOI18N
        final private static String SPLASH="MIDlet-Splash-Screen-Image"; //NOI18N
        final private static String USERDENIED="MIDlet-UserDenied"; //NOI18N
        final private static String UPDATEURL="MIDlet-Update-URL"; //NOI18N
        final private static String IPVERSION="MIDlet-Required-IP-Version"; //NOI18N
        final private static String SCICON="MIDlet-Scalable-Icon"; //NOI18N

        final private static String LNAME="LIBlet-Name"; //NOI18N
        final private static String LVENDOR="LIBlet-Vendor"; //NOI18N
        final private static String LVERSION="LIBlet-Version"; //NOI18N

        final private static String LDESCRIPTION="LIBlet-Description"; //NOI18N
        final private static String LNONSHAREDDATA="LIBlet-NonShared-Data-Size"; //NOI18N
        final private static String LSHAREDDATA="LIBlet-Shared-Data-Size"; //NOI18N
        final private static String LINSTALL="LIBlet-Install-Notify"; //NOI18N
        final private static String LICON="LIBlet-Icon"; //NOI18N
        final private static String LDELETE="LIBlet-Delete-Notify"; //NOI18N
        final private static String LFONT="LIBlet-Font"; //NOI18N

        private static final String[][] mandatoryProperties = {
            new String[] {
                LNAME, LVENDOR, LVERSION
            },
            new String[] {
                NAME, VENDOR, VERSION
            },
            new String[] {
                NAME, VENDOR, VERSION
            },
            new String[] {
                NAME, VENDOR, VERSION
            },
        };
        
        private static final String[][] nonmandatoryProperties = {
            new String [] {
                LDESCRIPTION, LNONSHAREDDATA, LSHAREDDATA,
                LINSTALL, LICON, LDELETE, LFONT,
            },
            new String[] {
                ICON, DESCRIPTION, INFOURL, 
                DATASIZE, 
                INSTALL, DELETE, CONFIRM, 
                MECONFIG, MEPROFILE, 
            },
            new String[] {
                ICON, DESCRIPTION, INFOURL, 
                DATASIZE, 
                INSTALL, DELETE, CONFIRM, 
                MECONFIG, MEPROFILE, 
            },
            new String[] {
                ICON, DESCRIPTION, INFOURL,
                DATASIZE,
                INSTALL, DELETE, CONFIRM,
                FONT, MINCANVAS, MAXCANVAS, SPLASH,
                USERDENIED, UPDATEURL, IPVERSION, SCICON,
                MECONFIG, MEPROFILE,
            },
        };
        
//        private static final String[][] allMIDletProperties = {
//            new String[] {
//                NAME, VENDOR, VERSION, ICON, DESCRIPTION, INFOURL,
//                DATASIZE,
//                INSTALL, DELETE, CONFIRM,
//                MECONFIG, MEPROFILE,
//            },
//            new String[] {
//                NAME, VENDOR, VERSION, ICON, DESCRIPTION, INFOURL,
//                DATASIZE,
//                INSTALL, DELETE, CONFIRM,
//                MECONFIG, MEPROFILE,
//            },
//        };
        
        private static final String[] riskyProperties = {
            MECONFIG, MEPROFILE, 
        };
        
        private static final long serialVersionUID = -2195421895353167160L;

        private static final Pattern midpPattern = Pattern.compile("MIDP-([1-3])");

        public void initManifestModel(boolean isLIBlet) {
            this.isLIBlet = isLIBlet;
        }

        public void setMIDP(final String midp) {
            if (midp != null) {
                Matcher m = midpPattern.matcher(midp);
                midpVersion = m.find() ? Integer.parseInt(m.group(1)) : 2;
            }
        }
        
        public boolean isMIDP2() {
            return midpVersion == 2;
        }
        
        public int getMIDPVersion() {
            return midpVersion;
        }

        public HashSet<String> getKeys() {
            return new HashSet<String>(items);
        }
        
        public int getRowCount() {
            return items.size();
        }
        
        public int getColumnCount() {
            return 3;
        }
        
        public boolean isCellEditable(@SuppressWarnings("unused")
		final int rowIndex, @SuppressWarnings("unused")
		final int columnIndex) {
            return false;
        }
        
        public String getColumnName(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return NbBundle.getMessage(CustomizerJad.class, "LBL_Jad_Column_Type"); //NOI18N
                case 1:
                    return NbBundle.getMessage(CustomizerJad.class, "LBL_Jad_Column_Key"); //NOI18N
                case 2:
                    return NbBundle.getMessage(CustomizerJad.class, "LBL_Jad_Column_Value"); //NOI18N
                default:
                    return null;
            }
        }

        public void switchManifestModel(boolean toLIBlet) {
            isLIBlet = toLIBlet;
            final String FROM = toLIBlet ? "MIDlet-" : "LIBlet-"; //NOI18N
            final String TO = toLIBlet ? "LIBlet-" : "MIDlet-"; //NOI18N
            for (int i = 0; i< items.size(); i++) {
                String key = items.get(i);
                if (key.startsWith(FROM)) {
                    String newKey = TO + key.substring(FROM.length());
                    if (contains(getMandatory(), newKey) || contains(getNonMandatory(), newKey)) {
                        items.set(i, newKey);
                        String v = map1.remove(key);
                        if (v != null) map1.put(newKey, v);
                        v = map2.remove(key);
                        if (v != null) map2.put(newKey, v);
                        v = map3.remove(key);
                        if (v != null) map3.put(newKey, v);
                        fireTableRowsUpdated(i, i);
                    }
                }
            }
        }

        public Class<?> getColumnClass(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
                default:
                    return null;
            }
        }
        
        public synchronized Object[] getDataDelegates() {
            updateMapsFromItems();
            return new Object[] {map1, map2, map3};
        }
        
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            assert rowIndex < items.size();
            switch (columnIndex) {
                case 0: {
                    final String item = items.get(rowIndex);
                    String value;
                    if (containsInMandatory(item))
                        value = NbBundle.getMessage(CustomizerJad.class, "LBL_CustomJad_Required"); //NOI18N
                    else if (containsInRisky(item))
                        value = NbBundle.getMessage(CustomizerJad.class, "LBL_CustomJad_Risky"); //NOI18N
                    else if (containsInNonMandatory(item))
                        value = NbBundle.getMessage(CustomizerJad.class, "LBL_CustomJad_Optional"); //NOI18N
                    else {
                        final Boolean b = getPlacement(item);
                        value = NbBundle.getMessage(CustomizerJad.class, "LBL_CustomJad_Custom" + (b == null ? "" : (b.booleanValue() ? "_J" : "_M"))); //NOI18N
                    }
                    return value;
                }
                case 1:
                    return items.get(rowIndex);
                case 2: {
                    String value = map1.get(items.get(rowIndex));
                    if (value == null)
                        value = map2.get(items.get(rowIndex));
                    if (value == null)
                        value = map3.get(items.get(rowIndex));
                    if (value == null)
                        value = ""; //NOI18N
                    return value;
                }
                default:
                    return null;
            }
        }
        
        public synchronized void setDataDelegates(final Object data[]) {
            assert data != null;
            map1 = data[0] == null ? new HashMap<String,String>() : (HashMap<String,String>) data[0];
            map2 = data[1] == null ? new HashMap<String,String>() : (HashMap<String,String>) data[1];
            map3 = data[2] == null ? new HashMap<String,String>() : (HashMap<String,String>) data[2];
            updateItemsFromMaps();
            fireTableDataChanged();
        }
        
        public synchronized void updateItemsFromMaps() {
            items.clear();
            final ArrayList<String> keys = new ArrayList<String>(map1.keySet());
            keys.addAll(map2.keySet());
            keys.addAll(map3.keySet());
            for (int a = 0; a < getMandatory().length; a ++) {
                items.add(getMandatory()[a]);
                keys.remove(getMandatory()[a]);
            }
            final String[] strKeys = keys.toArray(new String[keys.size()]);
            Arrays.sort(strKeys);
            for (int a = 0; a < strKeys.length; a ++)
                items.add(strKeys[a]);
        }
        
        public synchronized void updateMapsFromItems() {
            final HashMap<String,String> res1 = new HashMap<String,String>(), 
            		res2 = new HashMap<String,String>(),
            		res3 = new HashMap<String,String>();
            for (int a = 0; a < items.size(); a ++) {
                final String key = items.get(a);
                String value = map3.get(key);
                if (value != null) {
                    res3.put(key, value);
                } else {
                    value = map2.get(key);
                    if (value != null) {
                        res2.put(key, value);
                    } else {
                        value = map1.get(key);
                        res1.put(key, value == null ? "" : value); //NOI18N
                    }
                }
            }
            map1 = res1;
            map2 = res2;
            map3 = res3;
        }
        
        private static boolean contains(final String[] array, final String item) {
            for (int a = 0; a < array.length; a ++)
                if (array[a].equals(item))
                    return true;
            return false;
        }
        
        public String[] getMandatory() {
            return mandatoryProperties[isLIBlet ? 0 : getMIDPVersion()];
        }
        
        public String[] getNonMandatory() {
            if (additionalAttributes == null){
                additionalAttributes = loadAdditionalAttributes();
            }
            return mergeAttributes(nonmandatoryProperties[isLIBlet ? 0 : getMIDPVersion()], additionalAttributes);
        }
        
        public String[] getAllAttrs() {
            return mergeAttributes(getMandatory(), getNonMandatory());//allMIDletProperties[isMIDP20 ? 1 : 0];
        }
        
        public boolean containsInMandatory(final String item) {
            return contains(getMandatory(), item);
        }
        
        public boolean containsInRisky(final String item) {
            return contains(riskyProperties, item);
        }
        
        public boolean containsInNonMandatory(final String item) {
            return contains(getNonMandatory(), item);
        }
        
        public boolean containsInAllAttrs(final String item) {
            return contains(getAllAttrs(), item);
        }
        
        public boolean isAcceptable(final String key) {
            if (key == null)
                return false;
            if (! key.startsWith("MIDlet-") && ! key.startsWith("LIBlet-")) //NOI18N
                return true;
            if (containsInAllAttrs(key))
                return true;
            return false;
        }
        
        public int addRow(final String key, final String value, final Boolean placement) {
            if (key == null || items.contains(key))
                return -1;
            final int row = items.size();
            map1.remove(key);
            map2.remove(key);
            map3.remove(key);
            if (placement == null) map1.put(key, value);
            else if (placement.booleanValue()) map2.put(key, value);
            else map3.put(key, value);
            items.add(key);
            fireTableRowsInserted(row, row);
            return row;
        }
        
        public void editRow(final String key, final String value, final Boolean placement) {
            final int row = items.indexOf(key);
            if (row < 0)
                return;
            map1.remove(key);
            map2.remove(key);
            map3.remove(key);
            if (placement == null) map1.put(key, value);
            else if (placement.booleanValue()) map2.put(key, value);
            else map3.put(key, value);
            fireTableRowsUpdated(row, row);
        }
        
        public Boolean getPlacement(final String key) {
            if (map2.containsKey(key)) return Boolean.TRUE;
            if (map3.containsKey(key)) return Boolean.FALSE;
            return null;
        }
        
        public void removeRow(final int row) {
            assert row < items.size();
            if (containsInMandatory(items.get(row)))
                return;
            items.remove(row);
            fireTableRowsDeleted(row, items.size() + 1);
        }
        
        public void removeKey(final String key) {
            final int row = items.indexOf(key);
            if (row < 0)
                return;
            removeRow(row);
        }

        private String[] loadAdditionalAttributes(){
            final List<String> attrs = new ArrayList<String>();

            final FileObject xml = FileUtil.getConfigFile("Buildsystem/ApplicationDescriptor/Attributes"); // NOI18N
            if (xml == null){
                return new String[0];
            }

            FileObject[] entries = xml.getChildren();            
            for (FileObject fileObject : Arrays.asList(entries)) {
                InputStream is = null;
                try {
                    is = fileObject.getInputStream();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    try {
                        String s;
                        while ((s = reader.readLine()) != null){
                            s = s.trim();
                            if (!attrs.contains(s) && s.length() != 0){
                                attrs.add(s);
                            }
                        }
                    } finally {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return attrs.toArray(new String[attrs.size()]);
        }

        private String[] mergeAttributes(String[] a, String[] b){
            String[] s = new String[a.length + b.length];
            System.arraycopy(a, 0, s, 0, a.length);
            System.arraycopy(b, 0, s, a.length, b.length);
            return s;
        }
    }
    
}
