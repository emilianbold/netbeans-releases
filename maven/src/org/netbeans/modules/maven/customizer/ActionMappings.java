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

package org.netbeans.modules.maven.customizer;

import hidden.org.codehaus.plexus.util.StringUtils;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import java.awt.Component;
import java.awt.Cursor;
import javax.swing.JList;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.maven.profiles.Profile;
import org.netbeans.modules.maven.spi.grammar.GoalsProvider;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.maven.MavenProjectPropsImpl;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author  mkleint
 */
public class ActionMappings extends javax.swing.JPanel {
    private static final String CUSTOM_ACTION_PREFIX = "CUSTOM-"; //NOI18N
    private NbMavenProjectImpl project;
    private ModelHandle handle;
    private HashMap<String, String> titles = new HashMap<String, String>();
    
    private GoalsListener goalsListener;
    private TextValueCompleter goalcompleter;
    private TextValueCompleter profilecompleter;
    private ProfilesListener profilesListener;
    private PropertiesListener propertiesListener;
    private RecursiveListener recursiveListener;
    private CheckBoxUpdater commandLineUpdater;
    public static final String PROP_SKIP_TEST="maven.test.skip"; //NOI18N
    private ActionToGoalMapping actionmappings;
    private ActionListener comboListener;
    
    private ActionMappings() {
        initComponents();
        lstMappings.setCellRenderer(new Renderer());
        lstMappings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        goalsListener = new GoalsListener();
        profilesListener = new ProfilesListener();
        propertiesListener = new PropertiesListener();
        recursiveListener = new RecursiveListener();
        FocusListener focus = new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (e.getComponent() == txtGoals) {
                    lblHint.setText(NbBundle.getMessage(ActionMappings.class, "ActionMappings.txtGoals.hint"));
                }
                if (e.getComponent() == txtProfiles) {
                    lblHint.setText(NbBundle.getMessage(ActionMappings.class, "ActinMappings.txtProfiles.hint"));
                }
                if (e.getComponent() == taProperties) {
                    lblHint.setText(NbBundle.getMessage(ActionMappings.class, "ActinMappings.txtProperties.hint"));
                }
            }
            public void focusLost(FocusEvent e) {
                lblHint.setText(""); //NOI18N
            }
        };
        txtGoals.addFocusListener(focus);
        txtProfiles.addFocusListener(focus);
        taProperties.addFocusListener(focus);
        goalcompleter = new TextValueCompleter(Collections.<String>emptyList(), txtGoals, " "); //NOI18N
        profilecompleter = new TextValueCompleter(Collections.<String>emptyList(), txtProfiles, " "); //NOI18N
    }
    
    public ActionMappings(ActionToGoalMapping mapp) {
        this();
        actionmappings = mapp;        
        loadMappings();
        btnSetup.setVisible(false);
        cbCommandLine.setVisible(false);
        cbRecursively.setVisible(false);
        comConfiguration.setVisible(false);
        lblConfiguration.setVisible(false);
        clearFields();
        Mnemonics.setLocalizedText(btnAdd, NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnAdd.text2"));
        Mnemonics.setLocalizedText(btnRemove, NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnRemove.text2"));
    }
    
    /** Creates new form ActionMappings */
    public ActionMappings(ModelHandle hand, NbMavenProjectImpl proj) {
        this();
        project = proj;
        handle = hand;
        titles.put(ActionProvider.COMMAND_BUILD, NbBundle.getMessage(ActionMappings.class, "COM_Build_project"));
        titles.put(ActionProvider.COMMAND_CLEAN, NbBundle.getMessage(ActionMappings.class, "COM_Clean_project"));
        titles.put(ActionProvider.COMMAND_COMPILE_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Compile_file"));
        titles.put(ActionProvider.COMMAND_DEBUG, NbBundle.getMessage(ActionMappings.class, "COM_Debug_project"));
        titles.put(ActionProvider.COMMAND_DEBUG_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Debug_file"));
        titles.put(ActionProvider.COMMAND_DEBUG_STEP_INTO, null);
        titles.put(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Debug_test"));
        titles.put(ActionProvider.COMMAND_REBUILD, NbBundle.getMessage(ActionMappings.class, "COM_ReBuild_project"));
        titles.put(ActionProvider.COMMAND_RUN, NbBundle.getMessage(ActionMappings.class, "COM_Run_project"));
        titles.put(ActionProvider.COMMAND_RUN_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Run_file"));
        titles.put(ActionProvider.COMMAND_TEST, NbBundle.getMessage(ActionMappings.class, "COM_Test_project"));
        titles.put(ActionProvider.COMMAND_TEST_SINGLE, NbBundle.getMessage(ActionMappings.class, "COM_Test_file"));
        titles.put("profile", NbBundle.getMessage(ActionMappings.class, "COM_Profile_project"));
        comConfiguration.setEditable(false);
        comConfiguration.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component com = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (com instanceof JLabel) {
                    if (value == ActionMappings.this.handle.getActiveConfiguration()) {
                        com.setFont(com.getFont().deriveFont(Font.BOLD));
                    }
                }
                return com;
            }
        });
        setupConfigurations();
        
        loadMappings();
        btnSetup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSetup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OptionsDisplayer.getDefault().open(OptionsDisplayer.ADVANCED + "/Maven"); //NOI18N - the id is the name of instance in layers.
            }
            
        });
        commandLineUpdater = new CheckBoxUpdater(cbCommandLine) {
            public Boolean getValue() {
                Profile prof = handle.getNetbeansPrivateProfile(false);
                if (prof != null && prof.getProperties().getProperty(Constants.HINT_USE_EXTERNAL) != null) {
                    return Boolean.valueOf(prof.getProperties().getProperty(Constants.HINT_USE_EXTERNAL));
                }
                String val = handle.getPOMModel().getProperties().getProperty(Constants.HINT_USE_EXTERNAL);
                if (val != null) {
                    return Boolean.valueOf(val);
                }
                MavenProjectPropsImpl props = project.getLookup().lookup(MavenProjectPropsImpl.class);
                val = props.get(Constants.HINT_USE_EXTERNAL, true, false);
                if (val != null) {
                    return Boolean.valueOf(val);
                }
                return null;
            }

            public void setValue(Boolean value) {
                MavenProjectPropsImpl props = project.getLookup().lookup(MavenProjectPropsImpl.class);
                boolean hasConfig = props.get(Constants.HINT_USE_EXTERNAL, true, false) != null;
                //TODO also try to take the value in pom vs inherited pom value into account.

                Profile prof = handle.getNetbeansPrivateProfile(false);
                if (prof != null && prof.getProperties().getProperty(Constants.HINT_USE_EXTERNAL) != null) {
                    prof.getProperties().setProperty(Constants.HINT_USE_EXTERNAL, value == null ? "true" : value.toString());
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        props.put(Constants.HINT_USE_EXTERNAL, null, true);
                    }
                    handle.markAsModified(handle.getProfileModel());
                    return;
                }

                if (handle.getProject().getProperties().containsKey(Constants.HINT_USE_EXTERNAL)) {
                    handle.getPOMModel().addProperty(Constants.HINT_USE_EXTERNAL, value == null ? "true" : value.toString()); //NOI18N
                    handle.markAsModified(handle.getPOMModel());
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        props.put(Constants.HINT_USE_EXTERNAL, null, true);
                    }
                    return;
                }
                props.put(Constants.HINT_USE_EXTERNAL, value == null ? null : value.toString(), true);
            }

            public boolean getDefaultValue() {
                return true;
            }
        };
        clearFields();
        comboListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearFields();
                loadMappings();
                addListeners();
            }
        };
    }
    
    private void addListeners() {
        comConfiguration.addActionListener(comboListener);
    }
    
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        clearFields();
    }
    
    
    @Override
    public void addNotify() {
        super.addNotify();
        setupConfigurations();
        loadMappings();
        addListeners();
        //TODO move the list population out of AWT.
        GoalsProvider provider = Lookup.getDefault().lookup(GoalsProvider.class);
        if (provider != null) {
            Set<String> strs = provider.getAvailableGoals();
            try {
                @SuppressWarnings("unchecked")
                List<String> phases = EmbedderFactory.getProjectEmbedder().getLifecyclePhases();
                strs.addAll(phases);
            } catch (Exception e) {
                // oh wel just ignore..
                e.printStackTrace();
            }
            goalcompleter.setValueList(strs);
        }
        if (project != null) {
            ProjectProfileHandler profileHandler = project.getLookup().lookup(ProjectProfileHandler.class);
            profilecompleter.setValueList(profileHandler.getAllProfiles());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbCommandLine = new javax.swing.JCheckBox();
        btnSetup = new javax.swing.JButton();
        lblConfiguration = new javax.swing.JLabel();
        comConfiguration = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstMappings = new javax.swing.JList();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        lblGoals = new javax.swing.JLabel();
        txtGoals = new javax.swing.JTextField();
        lblProfiles = new javax.swing.JLabel();
        txtProfiles = new javax.swing.JTextField();
        lblProperties = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        taProperties = new javax.swing.JTextArea();
        cbRecursively = new javax.swing.JCheckBox();
        lblMappings = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lblHint = new javax.swing.JLabel();
        btnAddProps = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(cbCommandLine, org.openide.util.NbBundle.getMessage(ActionMappings.class, "LBL_UseExternal")); // NOI18N
        cbCommandLine.setToolTipText(org.openide.util.NbBundle.getMessage(ActionMappings.class, "TLT_UseExternal")); // NOI18N
        cbCommandLine.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbCommandLine.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(btnSetup, org.openide.util.NbBundle.getMessage(ActionMappings.class, "LBL_SetupExternal")); // NOI18N
        btnSetup.setBorder(null);
        btnSetup.setBorderPainted(false);
        btnSetup.setContentAreaFilled(false);
        btnSetup.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        lblConfiguration.setLabelFor(comConfiguration);
        org.openide.awt.Mnemonics.setLocalizedText(lblConfiguration, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lblConfiguration.text")); // NOI18N

        lstMappings.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstMappingsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstMappings);

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnRemove.text")); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblGoals, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lblGoals.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblProfiles, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lblProfiles.text")); // NOI18N

        lblProperties.setLabelFor(taProperties);
        org.openide.awt.Mnemonics.setLocalizedText(lblProperties, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.lblProperties.text")); // NOI18N

        taProperties.setColumns(20);
        taProperties.setRows(5);
        jScrollPane3.setViewportView(taProperties);

        org.openide.awt.Mnemonics.setLocalizedText(cbRecursively, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.cbRecursively.text")); // NOI18N
        cbRecursively.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lblMappings.setLabelFor(lstMappings);
        org.openide.awt.Mnemonics.setLocalizedText(lblMappings, org.openide.util.NbBundle.getMessage(ActionMappings.class, "LBL_Actions")); // NOI18N
        lblMappings.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lblHint.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jScrollPane2.setViewportView(lblHint);

        org.openide.awt.Mnemonics.setLocalizedText(btnAddProps, org.openide.util.NbBundle.getMessage(ActionMappings.class, "ActionMappings.btnAddProps.text")); // NOI18N
        btnAddProps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPropsActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(cbCommandLine)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 112, Short.MAX_VALUE)
                        .add(btnSetup, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 210, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblGoals)
                            .add(lblProfiles)
                            .add(lblProperties)
                            .add(btnAddProps))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtProfiles, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                            .add(txtGoals, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                            .add(cbRecursively)
                            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(lblConfiguration)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comConfiguration, 0, 350, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, lblMappings)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(btnRemove)
                            .add(btnAdd)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {btnAdd, btnRemove}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbCommandLine)
                    .add(btnSetup))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(comConfiguration, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblConfiguration))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblMappings)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(btnAdd)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnRemove)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblGoals)
                    .add(txtGoals, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblProfiles)
                    .add(txtProfiles, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lblProperties)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnAddProps))
                    .add(jScrollPane3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbRecursively)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
    NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(org.openide.util.NbBundle.getMessage(ActionMappings.class, "TIT_Add_action"), org.openide.util.NbBundle.getMessage(ActionMappings.class, "LBL_AddAction"));//GEN-HEADEREND:event_btnAddActionPerformed
    Object ret = DialogDisplayer.getDefault().notify(nd);
    if (ret == NotifyDescriptor.OK_OPTION) {
        NetbeansActionMapping nam = new NetbeansActionMapping();
        nam.setDisplayName(nd.getInputText());
        nam.setActionName(CUSTOM_ACTION_PREFIX + nd.getInputText()); 
        getActionMappings().addAction(nam);
        if (handle != null) {
            handle.markAsModified(getActionMappings());
        }
        MappingWrapper wr = new MappingWrapper(nam);
        wr.setUserDefined(true);
        ((DefaultListModel)lstMappings.getModel()).addElement(wr);
        lstMappings.setSelectedIndex(lstMappings.getModel().getSize() - 1);
        lstMappings.ensureIndexIsVisible(lstMappings.getModel().getSize() - 1);
    }
}//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        Object obj = lstMappings.getSelectedValue();//GEN-HEADEREND:event_btnRemoveActionPerformed
        if (obj == null) {
            return;
        }
        MappingWrapper wr = (MappingWrapper)obj;
        NetbeansActionMapping mapp = wr.getMapping();
        if (mapp != null) {
            if (mapp.getActionName().startsWith(CUSTOM_ACTION_PREFIX)) { 
                ((DefaultListModel)lstMappings.getModel()).removeElement(wr);
            }
            // try removing from model, if exists..
            List lst = getActionMappings().getActions();
            if (lst != null) {
                Iterator it = lst.iterator();
                while (it.hasNext()) {
                    NetbeansActionMapping elem = (NetbeansActionMapping) it.next();
                    if (mapp.getActionName().equals(elem.getActionName())) {
                        it.remove();
                        if (handle != null) {
                            mapp = ActionToGoalUtils.getDefaultMapping(mapp.getActionName(), project);
                        } else {
                            mapp = null;
                        }
                        wr.setMapping(mapp);
                        wr.setUserDefined(false);
                        lstMappingsValueChanged(null);
                        if (handle != null) {
                            handle.markAsModified(getActionMappings());
                        }
                        break;
                    }
                }
            }
        }
    }//GEN-LAST:event_btnRemoveActionPerformed
    
    private void lstMappingsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstMappingsValueChanged
        Object obj = lstMappings.getSelectedValue();//GEN-HEADEREND:event_lstMappingsValueChanged
        if (obj == null) {
            clearFields();
        } else {
            MappingWrapper wr = (MappingWrapper)obj;
            NetbeansActionMapping mapp = wr.getMapping();
            txtGoals.setEditable(true);
            taProperties.setEditable(true);
            txtProfiles.setEditable(true);
            
            txtGoals.getDocument().removeDocumentListener(goalsListener);
            txtProfiles.getDocument().removeDocumentListener(profilesListener);
            taProperties.getDocument().removeDocumentListener(propertiesListener);
            cbRecursively.removeActionListener(recursiveListener);
            
            txtGoals.setText(createSpaceSeparatedList(mapp != null ? mapp.getGoals() : Collections.EMPTY_LIST));
            txtProfiles.setText(createSpaceSeparatedList(mapp != null ? mapp.getActivatedProfiles() : Collections.EMPTY_LIST));
            taProperties.setText(createPropertiesList(mapp != null ? mapp.getProperties() : new Properties()));
            if (handle != null && "pom".equals(handle.getProject().getPackaging())) { //NOI18N
                cbRecursively.setEnabled(true);
                cbRecursively.setSelected(mapp != null ? mapp.isRecursive() : true);
            }
            txtGoals.getDocument().addDocumentListener(goalsListener);
            txtProfiles.getDocument().addDocumentListener(profilesListener);
            taProperties.getDocument().addDocumentListener(propertiesListener);
            cbRecursively.addActionListener(recursiveListener);
            btnAddProps.setEnabled(true);
            updateColor(wr);
        }
    }//GEN-LAST:event_lstMappingsValueChanged

    private void btnAddPropsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPropsActionPerformed
        // TODO add your handling code here:
        JPopupMenu menu = new JPopupMenu();
        menu.add(new SkipTestsAction(taProperties));
        menu.add(new DebugMavenAction(taProperties));
        menu.show(btnAddProps, btnAddProps.getSize().width, 0);

    }//GEN-LAST:event_btnAddPropsActionPerformed
    
    private void loadMappings() {
        DefaultListModel model = new DefaultListModel();
        if (handle != null) {
            addSingleAction(ActionProvider.COMMAND_BUILD, model);
            addSingleAction(ActionProvider.COMMAND_CLEAN, model);
            addSingleAction(ActionProvider.COMMAND_REBUILD, model);
            addSingleAction(ActionProvider.COMMAND_TEST, model);
            addSingleAction(ActionProvider.COMMAND_TEST_SINGLE, model);
            addSingleAction(ActionProvider.COMMAND_RUN, model);
            addSingleAction(ActionProvider.COMMAND_RUN_SINGLE, model);
            addSingleAction(ActionProvider.COMMAND_DEBUG, model);
            addSingleAction(ActionProvider.COMMAND_DEBUG_SINGLE, model);
            addSingleAction(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, model);
            addSingleAction("profile", model);
        }
        List customs = getActionMappings().getActions();
        if (customs != null) {
            Iterator it = customs.iterator();
            while (it.hasNext()) {
                NetbeansActionMapping elem = (NetbeansActionMapping) it.next();
                if (elem.getActionName().startsWith(CUSTOM_ACTION_PREFIX)) {
                    MappingWrapper wr = new MappingWrapper(elem);
                    model.addElement(wr);
                    wr.setUserDefined(true);
                }
            }
        }
        lstMappings.setModel(model);
    }
    
    private void addSingleAction(String action, DefaultListModel model) {
        NetbeansActionMapping mapp = null;
        List lst = getActionMappings().getActions();
        if (lst != null) {
            Iterator it = lst.iterator();
            while (it.hasNext()) {
                NetbeansActionMapping elem = (NetbeansActionMapping) it.next();
                if (action.equals(elem.getActionName())) {
                    mapp = elem;
                    break;
                }
            }
        }
        boolean userDefined = true;
        if (mapp == null) {
            mapp = ActionToGoalUtils.getDefaultMapping(action, project);
            userDefined = false;
        }
        MappingWrapper wr;
        if (mapp == null) {
            wr = new MappingWrapper(action);
        } else {
            wr = new MappingWrapper(mapp);
        }
        wr.setUserDefined(userDefined);
        model.addElement(wr);
    }
    
    private String createSpaceSeparatedList(List list) {
        String str = ""; //NOI18N
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                String elem = (String) it.next();
                str = str + elem + " "; //NOI18N
            }
        }
        return str;
    }
    
    private void clearFields() {
        comConfiguration.removeActionListener(comboListener);
        txtGoals.getDocument().removeDocumentListener(goalsListener);
        txtProfiles.getDocument().removeDocumentListener(profilesListener);
        taProperties.getDocument().removeDocumentListener(propertiesListener);
        
        txtGoals.setText(""); //NOI18N
        txtProfiles.setText(""); //NOI18N
        taProperties.setText(""); //NOI18N
        
        txtGoals.getDocument().addDocumentListener(goalsListener);
        txtProfiles.getDocument().addDocumentListener(profilesListener);
        taProperties.getDocument().addDocumentListener(propertiesListener);
        
        txtGoals.setEditable(false);
        taProperties.setEditable(false);
        txtProfiles.setEditable(false);
        updateColor(null);
        cbRecursively.setEnabled(false);
        btnAddProps.setEnabled(false);
    }
    
    private void updateColor(MappingWrapper wr) {
        Font fnt = lblGoals.getFont();
        fnt = fnt.deriveFont(wr != null && wr.isUserDefined() ? Font.BOLD : Font.PLAIN);
        lblGoals.setFont(fnt);
        lblProperties.setFont(fnt);
        lblProfiles.setFont(fnt);
    }
    
    private String createPropertiesList(Properties properties) {
        String str = ""; //NOI18N
        if (properties != null) {
            Iterator it = properties.keySet().iterator();
            while (it.hasNext()) {
                String elem = (String) it.next();
                String val = properties.getProperty(elem);
                if (val.indexOf(" ") > -1) { //NOI18N
                    val = "\"" + val + "\""; //NOI18N
                }
                str = str + elem + "=" + val + "\n"; //NOI18N
            }
        }
        return str;
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAddProps;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSetup;
    private javax.swing.JCheckBox cbCommandLine;
    private javax.swing.JCheckBox cbRecursively;
    private javax.swing.JComboBox comConfiguration;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblConfiguration;
    private javax.swing.JLabel lblGoals;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblMappings;
    private javax.swing.JLabel lblProfiles;
    private javax.swing.JLabel lblProperties;
    private javax.swing.JList lstMappings;
    private javax.swing.JTextArea taProperties;
    private javax.swing.JTextField txtGoals;
    private javax.swing.JTextField txtProfiles;
    // End of variables declaration//GEN-END:variables
    
    private void writeProperties(final NetbeansActionMapping mapp) {
        String text = taProperties.getText();
        PropertySplitter split = new PropertySplitter(text);
        String tok = split.nextPair();
        Properties props = new Properties();
        while (tok != null) {
            String[] prp = StringUtils.split(tok, "=", 2); //NOI18N
            if (prp.length == 2) {
                String key = prp[0];
                //in case the user adds -D by mistake, remove it to get a parsable xml file.
                if (key.startsWith("-D")) { //NOI18N
                    key = key.substring("-D".length()); //NOI18N
                }
                props.setProperty(key, prp[1]);
            }
            tok = split.nextPair();
        }
        mapp.setProperties(props);
        if (handle != null) {
            handle.markAsModified(handle.getActionMappings());
        }
    }
    
    private ActionToGoalMapping getActionMappings() {
        assert handle != null || actionmappings != null;
        if (handle != null) {
            if (handle.isConfigurationsEnabled()) {
                return handle.getActionMappings((ModelHandle.Configuration) comConfiguration.getSelectedItem());
            } else {
                return handle.getActionMappings();
            }
        }
        return actionmappings;
    }
    
    private static class Renderer extends DefaultListCellRenderer {
        
    
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int arg2, boolean arg3,
                                                      boolean arg4) {
            Component supers = super.getListCellRendererComponent(list, value, arg2, arg3, arg4);
            if (supers instanceof JLabel && value instanceof MappingWrapper) {
                MappingWrapper wr = (MappingWrapper)value;
                JLabel lbl = (JLabel)supers;
                if (wr.isUserDefined()) {
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else {
                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                }
            }
            return supers;
        }
    }
    
    
    private class MappingWrapper {
        private NetbeansActionMapping mapping;
        private String action;
        private boolean userDefined = false;
        
        public MappingWrapper(String action) {
            this.action = action;
        }
        
        public MappingWrapper(NetbeansActionMapping mapp) {
            action = mapp.getActionName();
            mapping = mapp;
        }
        
        public void setMapping(NetbeansActionMapping mapp) {
            mapping = mapp;
        }
        
        public String getActionName() {
            return action;
        }
        
        public NetbeansActionMapping getMapping() {
            return mapping;
        }
        
        @Override
        public String toString() {
            if (titles.get(action) != null) {
                return titles.get(action);
            }
            if (mapping != null) {
                if (mapping.getDisplayName() != null) {
                    return mapping.getDisplayName();
                }
                return mapping.getActionName();
            }
            return action;
        }
        
        public boolean isUserDefined() {
            return userDefined;
        }
        
        public void setUserDefined(boolean userDefined) {
            this.userDefined = userDefined;
        }
    }
    
    private abstract class TextFieldListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            doUpdate();
        }
        
        public void removeUpdate(DocumentEvent e) {
            doUpdate();
        }
        
        public void changedUpdate(DocumentEvent e) {
            doUpdate();
        }
        
        protected MappingWrapper doUpdate() {
            MappingWrapper map = (MappingWrapper)lstMappings.getSelectedValue();
            if (map != null) {
                if (!map.isUserDefined()) {
                    NetbeansActionMapping mapping = map.getMapping();
                    if (mapping == null) {
                        mapping = new NetbeansActionMapping();
                        mapping.setActionName(map.getActionName());
                        map.setMapping(mapping);
                    }
                    getActionMappings().addAction(mapping);
                    if (handle != null) {
                        handle.markAsModified(getActionMappings());
                    }
                    map.setUserDefined(true);
                    updateColor(map);
                }
            }
            return map;
        }
    }
    
    private class GoalsListener extends TextFieldListener {
        @Override
        protected MappingWrapper doUpdate() {
            MappingWrapper wr = super.doUpdate();
            if (wr != null) {
                String text = txtGoals.getText();
                StringTokenizer tok = new StringTokenizer(text, " "); //NOI18N
                NetbeansActionMapping mapp = wr.getMapping();
                List<String> goals = new ArrayList<String>();
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    goals.add(token);
                }
                mapp.setGoals(goals);
                if (handle != null) {
                    handle.markAsModified(getActionMappings());
                }
            }
            return wr;
        }
    }
    
    private class ProfilesListener extends TextFieldListener {
        @Override
        protected MappingWrapper doUpdate() {
            MappingWrapper wr = super.doUpdate();
            if (wr != null) {
                String text = txtProfiles.getText();
                StringTokenizer tok = new StringTokenizer(text, " ,"); //NOI18N
                NetbeansActionMapping mapp = wr.getMapping();
                List<String> profs = new ArrayList<String>();
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    profs.add(token);
                }
                mapp.setActivatedProfiles(profs);
                if (handle != null) {
                    handle.markAsModified(getActionMappings());
                }
            }
            return wr;
        }
    }
    
    private class PropertiesListener extends TextFieldListener {
        @Override
        protected MappingWrapper doUpdate() {
            MappingWrapper wr = super.doUpdate();
            if (wr != null) {
                NetbeansActionMapping mapp = wr.getMapping();
                writeProperties(mapp);
            }
            return wr;
        }
        
    }
    
    private class RecursiveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            MappingWrapper map = (MappingWrapper)lstMappings.getSelectedValue();
            if (map != null) {
                if (!map.isUserDefined()) {
                    NetbeansActionMapping mapping = map.getMapping();
                    if (mapping == null) {
                        mapping = new NetbeansActionMapping();
                        mapping.setActionName(map.getActionName());
                    }
                    
                    getActionMappings().addAction(mapping);
                    map.setUserDefined(true);
                    updateColor(map);
                }
                map.getMapping().setRecursive(cbRecursively.isSelected());
                if (handle != null) {
                    handle.markAsModified(getActionMappings());
                }
            }
        }
        
    }
    
    private void setupConfigurations() {
        if (handle != null && handle.isConfigurationsEnabled()) {
            lblConfiguration.setVisible(true);
            comConfiguration.setVisible(true);
            DefaultComboBoxModel comModel = new DefaultComboBoxModel();
            for (ModelHandle.Configuration conf : handle.getConfigurations()) {
                comModel.addElement(conf);
            }
            comConfiguration.setModel(comModel);
            comConfiguration.setSelectedItem(handle.getActiveConfiguration());
        } else {
            lblConfiguration.setVisible(false);
            comConfiguration.setVisible(false);
            DefaultComboBoxModel comModel = new DefaultComboBoxModel();
            comConfiguration.setModel(comModel);
        }
    }

    static class SkipTestsAction extends AbstractAction {
        private JTextArea area;
        SkipTestsAction(JTextArea area) {
            putValue(Action.NAME, NbBundle.getMessage(ActionMappings.class, "ActionMappings.skipTests"));
            this.area = area;
        }

        public void actionPerformed(ActionEvent e) {
            String replace = PROP_SKIP_TEST + "=true"; //NOI18N
            String pattern = ".*" + PROP_SKIP_TEST + "([\\s]*=[\\s]*[\\S]+).*"; //NOI18N
            replacePattern(pattern, area, replace, true);
        }
    }
    
    static class DebugMavenAction extends AbstractAction {
        private JTextArea area;
        
        DebugMavenAction(JTextArea area) {
            putValue(Action.NAME, NbBundle.getMessage(ActionMappings.class, "ActionMappings.debugMaven"));
            this.area = area;
        }

        public void actionPerformed(ActionEvent e) {
            String replace = Constants.ACTION_PROPERTY_JPDALISTEN + "=maven"; //NOI18N
            String pattern = ".*" + Constants.ACTION_PROPERTY_JPDALISTEN + "([\\s]*=[\\s]*[\\S]+).*"; //NOI18N
            replacePattern(pattern, area, replace, true);
        }
    }

    private static void replacePattern(String pattern, JTextArea area, String replace, boolean select) {
        String props = area.getText();
        Matcher match = Pattern.compile(pattern, Pattern.DOTALL).matcher(props);
        if (match.matches()) {
            int begin = props.indexOf(PROP_SKIP_TEST);
            props = props.replace(PROP_SKIP_TEST + match.group(1), replace); //NOI18N
            area.setText(props);
            if (select) {
                area.setSelectionStart(begin);
                area.setSelectionEnd(begin + replace.length());
            }
        } else {
            String sep = "\n";//NOI18N
            if (props.endsWith("\n") || props.trim().length() == 0) {//NOI18N
                sep = "";//NOI18N
            }
            props = props + sep + replace; //NOI18N
            area.setText(props);
            if (select) {
                area.setSelectionStart(props.length() - replace.length());
                area.setSelectionEnd(props.length());
            }
        }

    }
}
