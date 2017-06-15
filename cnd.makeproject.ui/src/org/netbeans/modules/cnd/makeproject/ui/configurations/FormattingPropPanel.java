/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.makeproject.api.CodeStyleWrapper;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author alsimon
 */
public class FormattingPropPanel extends javax.swing.JPanel implements MakeContext.Savable {
    private static final String C_CODE_STYLES = "C_CodeStyles"; // NOI18N
    private static final String CPP_CODE_STYLES = "CPP_CodeStyles"; // NOI18N
    private static final String H_CODE_STYLES = "H_CodeStyles"; // NOI18N
    private static final String LIST_OF_STYLES = "List_Of_Styles"; // NOI18N
    private static final String CODE_STYLE = "CodeStyle"; // NOI18N
    private static final String CUSTOM_STYLE_NAME_SUFFIX = "_Style_Name"; // NOI18N
    private static final String PREDEFINED_STYLE_NAME_SUFFIX = "_Name"; // NOI18N
    private static final String PREFERENCES_PROVIDER_CLASS = "org.netbeans.modules.cnd.editor.options.CodeStylePreferencesProvider"; // NOI18N
    private static final String SEPARATOR = ","; // NOI18N
    private final Project project;
    private final MakeConfigurationDescriptor makeConfigurationDescriptor;

    // copy-paste from org.netbeans.modules.cnd.editor.options.EditorOptions
    private static final String APACHE_PROFILE = "Apache"; // NOI18N
    private static final String DEFAULT_PROFILE = "Default"; // NOI18N
    private static final String GNU_PROFILE = "GNU"; // NOI18N
    private static final String LUNIX_PROFILE = "Linux"; // NOI18N
    private static final String ANSI_PROFILE = "ANSI"; // NOI18N
    private static final String OPEN_SOLARIS_PROFILE = "OpenSolaris"; // NOI18N
    private static final String K_AND_R_PROFILE = "KandR"; // NOI18N
    private static final String MYSQL_PROFILE = "MySQL"; // NOI18N
    private static final String WHITESMITHS_PROFILE = "Whitesmiths"; // NOI18N

    private static final String[] PREDEFINED_STYLES = new String[] {
                        DEFAULT_PROFILE, APACHE_PROFILE, GNU_PROFILE,
                        LUNIX_PROFILE, ANSI_PROFILE, OPEN_SOLARIS_PROFILE,
                        K_AND_R_PROFILE, MYSQL_PROFILE, WHITESMITHS_PROFILE
    };

    public FormattingPropPanel(Project project, ConfigurationDescriptor configurationDescriptor) {
        this.project = project;
        makeConfigurationDescriptor = (MakeConfigurationDescriptor) configurationDescriptor;
        initComponents();
        CodeStyleWrapper style;
        style = ((MakeProject)project).getProjectFormattingStyle(MIMENames.C_MIME_TYPE);
        StylePresentation def = null;
        for (Map.Entry<String,CodeStyleWrapper> s : getAllStyles(MIMENames.C_MIME_TYPE).entrySet()) {
            StylePresentation stylePresentation = new StylePresentation(s);
            if (style != null) {
                if (stylePresentation.key.getStyleId().equals(style.getStyleId())) {
                    def = stylePresentation;
                }
            }
            cComboBox.addItem(stylePresentation);
        }
        if (def != null) {
            cComboBox.setSelectedItem(def);
        }

        style = ((MakeProject)project).getProjectFormattingStyle(MIMENames.CPLUSPLUS_MIME_TYPE);
        def = null;
        for (Map.Entry<String,CodeStyleWrapper> s : getAllStyles(MIMENames.CPLUSPLUS_MIME_TYPE).entrySet()) {
            StylePresentation stylePresentation = new StylePresentation(s);
            if (style != null) {
                if (stylePresentation.key.getStyleId().equals(style.getStyleId())) {
                    def = stylePresentation;
                }
            }
            cppComboBox.addItem(stylePresentation);
        }
        if (def != null) {
            cppComboBox.setSelectedItem(def);
        }

        style = ((MakeProject)project).getProjectFormattingStyle(MIMENames.HEADER_MIME_TYPE);
        def = null;
        for (Map.Entry<String,CodeStyleWrapper> s : getAllStyles(MIMENames.HEADER_MIME_TYPE).entrySet()) {
            StylePresentation stylePresentation = new StylePresentation(s);
            if (style != null) {
                if (stylePresentation.key.getStyleId().equals(style.getStyleId())) {
                    def = stylePresentation;
                }
            }
            headerComboBox.addItem(stylePresentation);
        }
        if (def != null) {
            headerComboBox.setSelectedItem(def);
        }
        style = ((MakeProject)project).getProjectFormattingStyle(null);
        if (style != null) {
            ((MyComboBox)styleField).setText(style.getStyleId());
        }
        switch (((MakeProject)project).isProjectFormattingStyle()) {
            case Global:
                globalRadioButton.setSelected(true);
                globalRadioButtonActionPerformed(null);
                break;
            case Project:
                projectRadioButton.setSelected(true);
                projectRadioButtonActionPerformed(null);
                break;
            case ClangFormat:
                useClangFormatRadioButton.setSelected(true);
                useClangFormatRadioButtonActionPerformed(null);
                break;
        }
    }

    public static Map<String,CodeStyleWrapper> getAllStyles(String mimeType) {
        Preferences pref = null;
        CodeStylePreferences.Provider myProvider = null;
        for(CodeStylePreferences.Provider p : Lookup.getDefault().lookupAll(CodeStylePreferences.Provider.class)) {
            if (p.getClass().getName().equals(PREFERENCES_PROVIDER_CLASS)) {
                myProvider = p;
                pref = p.forDocument(null, mimeType);
            }
        }
        String styles = null;
        StringBuilder def = new StringBuilder();
        //the problem here if provider is not found (f.E. module is not loaded, see bz#247485)
        //the list of predefined styles still used, which is incorrect
        //just return empty TreeMap here
        if (myProvider == null) {
            return new TreeMap<>();
        }
        for(String s: PREDEFINED_STYLES){
            if (def.length() > 0){
                def.append(SEPARATOR);
            }
            def.append(s);
        }
        if (pref != null) {
            if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
                styles = pref.node(C_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
                styles = pref.node(CPP_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            } else  if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
                styles = pref.node(H_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            } else {
                styles = def.toString();
            }
        } else {
            styles = def.toString();
        }
        Map<String,CodeStyleWrapper> res = new TreeMap<>();
        StringTokenizer st = new StringTokenizer(styles, SEPARATOR);
        while(st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            String styleDisplayName = getStyleDisplayName(pref, myProvider, nextToken);
            res.put(styleDisplayName, CodeStyleWrapper.createProjectStyle(nextToken, styleDisplayName));
        }
        return res;
    }

    public static String getStyleDisplayName(String styleId, String mimeType) {
        Preferences pref = null;
        CodeStylePreferences.Provider myProvider = null;
        for(CodeStylePreferences.Provider p : Lookup.getDefault().lookupAll(CodeStylePreferences.Provider.class)) {
            if (p.getClass().getName().equals(PREFERENCES_PROVIDER_CLASS)) {
                myProvider = p;
                pref = p.forDocument(null, mimeType);
            }
        }
        if (myProvider == null) {
            return styleId;
        }
        return getStyleDisplayName(pref, myProvider, styleId);
    }

    private static String getStyleDisplayName(Preferences pref, CodeStylePreferences.Provider myProvider, String styleId) {
        for (String name : PREDEFINED_STYLES) {
            if (styleId.equals(name)) {
                return NbBundle.getMessage(myProvider.getClass(), styleId+PREDEFINED_STYLE_NAME_SUFFIX);
            }
        }
        return pref.node(CODE_STYLE).get(styleId+CUSTOM_STYLE_NAME_SUFFIX, styleId);
    }

    public static boolean createStyle(CodeStyleWrapper styleId, String mimeType) {
        Preferences pref = null;
        CodeStylePreferences.Provider myProvider = null;
        for(CodeStylePreferences.Provider p : Lookup.getDefault().lookupAll(CodeStylePreferences.Provider.class)) {
            if (p.getClass().getName().equals(PREFERENCES_PROVIDER_CLASS)) {
                myProvider = p;
                pref = p.forDocument(null, mimeType);
            }
        }
        if (pref == null || myProvider == null) {
            return false;
        }
        StringBuilder def = new StringBuilder();
        for(String s: PREDEFINED_STYLES){
            if (def.length() > 0){
                def.append(SEPARATOR);
            }
            def.append(s);
        }
        if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            String styles = pref.node(C_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            pref.node(C_CODE_STYLES).put(LIST_OF_STYLES, styles+SEPARATOR+styleId.getStyleId());
            pref.node(CODE_STYLE).put(styleId+CUSTOM_STYLE_NAME_SUFFIX, styleId.getDisplayName());
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            String styles = pref.node(CPP_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            pref.node(CPP_CODE_STYLES).put(LIST_OF_STYLES, styles+SEPARATOR+styleId.getStyleId());
            pref.node(CODE_STYLE).put(styleId+CUSTOM_STYLE_NAME_SUFFIX, styleId.getDisplayName());
        } else  if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            String styles = pref.node(H_CODE_STYLES).get(LIST_OF_STYLES, def.toString());
            pref.node(H_CODE_STYLES).put(LIST_OF_STYLES, styles+SEPARATOR+styleId.getStyleId());
            pref.node(CODE_STYLE).put(styleId+CUSTOM_STYLE_NAME_SUFFIX, styleId.getDisplayName());
        }
        return true;
    }

    @Override
    public void save() {
        MakeProject.FormattingStyle style = MakeProject.FormattingStyle.Global;
        if (projectRadioButton.isSelected()) {
            style = MakeProject.FormattingStyle.Project;
        } else if (globalRadioButton.isSelected()) {
            style = MakeProject.FormattingStyle.Global;
        } else if (useClangFormatRadioButton.isSelected()) {
            style = MakeProject.FormattingStyle.ClangFormat;
        }
        ((MakeProject)project).setProjectFormattingStyle(style);
        switch (style) {
            case Global:
                break;
            case Project:
                ((MakeProject)project).setProjectFormattingStyle(MIMENames.C_MIME_TYPE, ((StylePresentation) cComboBox.getSelectedItem()).key);
                ((MakeProject)project).setProjectFormattingStyle(MIMENames.CPLUSPLUS_MIME_TYPE, ((StylePresentation) cppComboBox.getSelectedItem()).key);
                ((MakeProject)project).setProjectFormattingStyle(MIMENames.HEADER_MIME_TYPE, ((StylePresentation) headerComboBox.getSelectedItem()).key);
                break;
            case ClangFormat:
                ((MakeProject)project).setProjectFormattingStyle(null, CodeStyleWrapper.createClangFormatStyle(((MyComboBox)styleField).getText().trim()));
                break;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        globalRadioButton = new javax.swing.JRadioButton();
        projectRadioButton = new javax.swing.JRadioButton();
        cLabel = new javax.swing.JLabel();
        cppLabel = new javax.swing.JLabel();
        headerLabel = new javax.swing.JLabel();
        cComboBox = new javax.swing.JComboBox();
        cppComboBox = new javax.swing.JComboBox();
        headerComboBox = new javax.swing.JComboBox();
        useClangFormatRadioButton = new javax.swing.JRadioButton();
        clangFormatLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        clangStyleButton = new javax.swing.JButton();
        styleField = new MyComboBox();

        setLayout(new java.awt.GridBagLayout());

        buttonGroup1.add(globalRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(globalRadioButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.globalRadioButton.text")); // NOI18N
        globalRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                globalRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(globalRadioButton, gridBagConstraints);

        buttonGroup1.add(projectRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(projectRadioButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.projectRadioButton.text")); // NOI18N
        projectRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(projectRadioButton, gridBagConstraints);

        cLabel.setLabelFor(cComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(cLabel, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.cLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 6);
        add(cLabel, gridBagConstraints);

        cppLabel.setLabelFor(cppComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(cppLabel, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.cppLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 6);
        add(cppLabel, gridBagConstraints);

        headerLabel.setLabelFor(headerComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(headerLabel, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.headerLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 6);
        add(headerLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
        add(cComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
        add(cppComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
        add(headerComboBox, gridBagConstraints);

        buttonGroup1.add(useClangFormatRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(useClangFormatRadioButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.useClangFormatRadioButton.text")); // NOI18N
        useClangFormatRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useClangFormatRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(useClangFormatRadioButton, gridBagConstraints);

        clangFormatLabel.setLabelFor(styleField);
        org.openide.awt.Mnemonics.setLocalizedText(clangFormatLabel, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.clangFormatLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 6);
        add(clangFormatLabel, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 451, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 66, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(clangStyleButton, org.openide.util.NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.clangStyleButton.text")); // NOI18N
        clangStyleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clangStyleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(clangStyleButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(styleField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void globalRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalRadioButtonActionPerformed
        cComboBox.setEnabled(false);
        cppComboBox.setEnabled(false);
        headerComboBox.setEnabled(false);
        styleField.setEnabled(false);
        clangStyleButton.setEnabled(false);
    }//GEN-LAST:event_globalRadioButtonActionPerformed

    private void projectRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectRadioButtonActionPerformed
        cComboBox.setEnabled(true);
        cppComboBox.setEnabled(true);
        headerComboBox.setEnabled(true);
        styleField.setEnabled(false);
        clangStyleButton.setEnabled(false);
    }//GEN-LAST:event_projectRadioButtonActionPerformed

    private void useClangFormatRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useClangFormatRadioButtonActionPerformed
        cComboBox.setEnabled(false);
        cppComboBox.setEnabled(false);
        headerComboBox.setEnabled(false);
        styleField.setEnabled(true);
        clangStyleButton.setEnabled(true);
    }//GEN-LAST:event_useClangFormatRadioButtonActionPerformed

    private void clangStyleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clangStyleButtonActionPerformed
        FileObject baseDirFileObject = makeConfigurationDescriptor.getBaseDirFileObject();
        final ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(makeConfigurationDescriptor.getBaseDirFileSystem());
        String seed = baseDirFileObject.getPath();
        String title = NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.SelectClangFormat"); //NOI18N
        String approve = NbBundle.getMessage(FormattingPropPanel.class, "FormattingPropPanel.Select"); //NOI18N
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(env, title, approve,
                JFileChooser.FILES_ONLY, null, seed, true);
        int ret = fileChooser.showOpenDialog(null);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        final File selectedFile = fileChooser.getSelectedFile();
        String path = CndPathUtilities.normalizeSlashes(selectedFile.getPath());
        String toRelativePath = CndPathUtilities.toRelativePath(baseDirFileObject, path);
        ((MyComboBox)styleField).setText("@"+toRelativePath); //NOI18N
    }//GEN-LAST:event_clangStyleButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cComboBox;
    private javax.swing.JLabel cLabel;
    private javax.swing.JLabel clangFormatLabel;
    private javax.swing.JButton clangStyleButton;
    private javax.swing.JComboBox cppComboBox;
    private javax.swing.JLabel cppLabel;
    private javax.swing.JRadioButton globalRadioButton;
    private javax.swing.JComboBox headerComboBox;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton projectRadioButton;
    private javax.swing.JComboBox<String> styleField;
    private javax.swing.JRadioButton useClangFormatRadioButton;
    // End of variables declaration//GEN-END:variables

    private static final class StylePresentation {
        private CodeStyleWrapper key;
        private final String name;
        private StylePresentation(Map.Entry<String, CodeStyleWrapper> entry) {
            name = entry.getKey();
            key = entry.getValue();
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    public static class MyComboBox extends JComboBox {

        public MyComboBox() {
            setEditable(true);
            init();
        }

        /**
         * Read combo box state from storage
         *
         * @param path is initial combo box value
         */
        private void init() {
            List<String> list = new ArrayList<>();
            list.add("@.clang-format"); //NOI18N
            list.add("BasedOnStyle: Chromium"); //NOI18N
            list.add("BasedOnStyle: GNU"); //NOI18N
            list.add("BasedOnStyle: Google"); //NOI18N
            list.add("BasedOnStyle: LLVM"); //NOI18N
            list.add("BasedOnStyle: Mozilla"); //NOI18N
            list.add("BasedOnStyle: WebKit"); //NOI18N
            DefaultComboBoxModel rootModel = new DefaultComboBoxModel(list.toArray());
            setModel(rootModel);
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < 30; i++) {
                buf.append("w"); // NOI18N
            }
            setPrototypeDisplayValue(buf.toString());
        }

        /**
         * Get field text. Override this method to add additional expansion possibilities.
         *
         * @return field text
         */
        public String getText() {
            return getTextImpl();
        }

        /**
         * Get original field text.
         *
         * @return field text
         */
        protected final String getTextImpl() {
            if (editor != null) {
                Component component = editor.getEditorComponent();
                if (component instanceof JTextField) {
                    return ((JTextField) component).getText();
                }
            }
            if (getSelectedItem() != null) {
                return getSelectedItem().toString();
            }
            return null;
        }

        /**
         * Set current text
         *
         * @param path update field
         */
        public void setText(String path) {
            setSelectedItem(path);
        }

        /**
         * Add action and document listeners
         *
         * @param listener
         */
        public void addChangeListener(final ActionListener listener) {
            //addActionListener(listener);
            Component component = editor.getEditorComponent();
            if (component instanceof JTextField) {
                ((JTextField) component).getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        listener.actionPerformed(null);
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        listener.actionPerformed(null);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        listener.actionPerformed(null);
                    }
                });
            }
        }
    }

}
