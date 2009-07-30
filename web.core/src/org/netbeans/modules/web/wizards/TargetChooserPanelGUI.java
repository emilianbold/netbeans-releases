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

package org.netbeans.modules.web.wizards;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.web.core.Util;

// XXX I18N

/**
 *
 * @author  phrebejk, mkuchtiak, alexeybutenko
 */
public class TargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener, DocumentListener  {
    private static final Logger LOG = Logger.getLogger(TargetChooserPanelGUI.class.getName());
    private static final String TAG_FILE_FOLDER="WEB-INF/tags"; //NOI18N
    private static final String TAG_FILE_IN_JAVALIB_FOLDER="META-INF/tags"; //NOI18N
    private static final String TLD_FOLDER="WEB-INF/tlds"; //NOI18N
    private static final String TLD_IN_JAVALIB_FOLDER="META-INF"; //NOI18N
    private static final String NEW_FILE_PREFIX =
        NbBundle.getMessage( TargetChooserPanelGUI.class, "LBL_TargetChooserPanelGUI_NewFilePrefix" ); // NOI18N

    
    private TargetChooserPanel wizardPanel;
    private Project project;
    private String expectedExtension;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private SourceGroup[] folders;
    private FileType fileType;
    private WebModule wm;

    private TargetChooserPanel.PreferredLanguage preferredLanguage;
    // GUI components for JSP/TAG
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextArea descriptionArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton jspSyntaxButton, xmlSyntaxButton, faceletsSyntaxButton;
    private javax.swing.JCheckBox segmentBox, tldCheckBox;
    private javax.swing.JButton browseButton1;
    private javax.swing.JLabel descriptionLabel, optionLabel;
    private javax.swing.JPanel optionsPanel;
 
    private java.util.Set tagValues;
    private String tagName;

    // GUI components for TLD
    private javax.swing.JTextField uriTextField,prefixTextField, tldTextField, tagNameTextField;
    private javax.swing.JLabel uriLabel, prefixLabel;
    private boolean uriWasTyped,prefixWasTyped;
    boolean tagFileValid=true;
    private FileObject tldFo;
    
    /** Creates new form TargetChooserGUI */
    public TargetChooserPanelGUI(final TargetChooserPanel wizardPanel, Project project, SourceGroup[] folders, FileType fileType) {
        this.wizardPanel = wizardPanel;
        this.project = project;
        this.folders=folders;
        this.fileType=fileType;
        initComponents();
        getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_TargetPanel",fileType.toString()));
        
        if (FileType.JSP.equals(fileType) || FileType.TAG.equals(fileType)) {
            buttonGroup1 = new javax.swing.ButtonGroup();
            jScrollPane1 = new javax.swing.JScrollPane();
            descriptionArea = new javax.swing.JTextArea();
            segmentBox = new javax.swing.JCheckBox();
            descriptionLabel = new javax.swing.JLabel();
            optionLabel = new javax.swing.JLabel();
            jspSyntaxButton = new javax.swing.JRadioButton();
            xmlSyntaxButton = new javax.swing.JRadioButton();
            if (isFaceletsAvailable())
                faceletsSyntaxButton = new javax.swing.JRadioButton();
            
            optionsPanel = new javax.swing.JPanel();

            optionsPanel.setLayout(new java.awt.GridBagLayout());

            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 4;
            gridBagConstraints.weightx = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            //gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            customPanel.add(optionsPanel, gridBagConstraints);
            
            segmentBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    checkBoxChanged(evt);
                }
            });

            optionLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Options"));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            optionsPanel.add(optionLabel, gridBagConstraints);

            jspSyntaxButton.setSelected(true);
            if (FileType.JSP.equals(fileType))
                jspSyntaxButton.setMnemonic(NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_JspStandard_mnem").charAt(0));
            else
                jspSyntaxButton.setMnemonic(NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_TagStandard_mnem").charAt(0));
            buttonGroup1.add(jspSyntaxButton);
            jspSyntaxButton.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    checkBoxChanged(evt);
                }
            });

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            optionsPanel.add(jspSyntaxButton, gridBagConstraints);
            
            xmlSyntaxButton.setMnemonic(NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_JspXml_mnem").charAt(0));
            buttonGroup1.add(xmlSyntaxButton);
            xmlSyntaxButton.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    checkBoxChanged(evt);
                }
            });

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            optionsPanel.add(xmlSyntaxButton, gridBagConstraints);

            if (isFaceletsAvailable()) {
                faceletsSyntaxButton.setMnemonic(NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_Facelets_mnem").charAt(0));
                buttonGroup1.add(faceletsSyntaxButton);
                faceletsSyntaxButton.addItemListener(new java.awt.event.ItemListener() {
                    public void itemStateChanged(ItemEvent evt) {
                        checkBoxChanged(evt);
                    }
                });

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                optionsPanel.add(faceletsSyntaxButton,gridBagConstraints);
            }

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
            segmentBox.setMnemonic(NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_JspSegment_mnem").charAt(0));
            optionsPanel.add(segmentBox, gridBagConstraints);
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridheight = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 2.0;
            optionsPanel.add(new javax.swing.JPanel(), gridBagConstraints);
            
            descriptionLabel.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_description"));
            descriptionLabel.setDisplayedMnemonic(NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_Description_mnem").charAt(0));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
            descriptionLabel.setLabelFor(descriptionArea);
            customPanel.add(descriptionLabel, gridBagConstraints);

            descriptionArea.setEditable(false);
            descriptionArea.setLineWrap(true);
            descriptionArea.setRows(2);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setOpaque(false);
            descriptionArea.getAccessibleContext().setAccessibleDescription(descriptionLabel.getText());
            jScrollPane1.setViewportView(descriptionArea);
            jScrollPane1.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 4;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 2.0;
            customPanel.add(jScrollPane1, gridBagConstraints);
            if (FileType.TAG.equals(fileType)) {
                
                //remove(fillerPanel);
                tldCheckBox = new javax.swing.JCheckBox();
                tldCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_AddToTLD_mnem").charAt(0));
                tldCheckBox.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "OPT_addTagFileToTLD"));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridwidth = 4;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(6, 0, 2, 0);
                customPanel.add(tldCheckBox, gridBagConstraints);
                tldCheckBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("OPT_addToTLD"));
                /*
                javax.swing.JLabel tldDescriptionLabel = new javax.swing.JLabel();
                tldDescriptionLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "HINT_tldFile"));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 4;
                gridBagConstraints.gridwidth = 3;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
                customPanel.add(tldDescriptionLabel, gridBagConstraints);
                */
                javax.swing.JLabel tldFileLabel = new javax.swing.JLabel();
                tldFileLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_TLDName_mnem").charAt(0));
                tldFileLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_tldFile"));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 5;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
                customPanel.add(tldFileLabel, gridBagConstraints);
                
                tldTextField = new javax.swing.JTextField();
                tldTextField.setEditable(false);
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 5;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
                customPanel.add(tldTextField, gridBagConstraints);
                tldTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_TLDFile"));
                tldFileLabel.setLabelFor(tldTextField);

                browseButton1 = new javax.swing.JButton();
                browseButton1.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_Browse1_Mnemonic").charAt(0));
                browseButton1.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Browse"));
                browseButton1.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        browseButton1ActionPerformed(evt);
                    }
                });
                browseButton1.setEnabled(false);
                
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridy = 5;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
                customPanel.add(browseButton1, gridBagConstraints);
                
                browseButton1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_Browse"));
                tldCheckBox.addItemListener(new java.awt.event.ItemListener() {
                    public void itemStateChanged(java.awt.event.ItemEvent evt) {
                        tagNameTextField.setEditable(tldCheckBox.isSelected());
                        browseButton1.setEnabled(tldCheckBox.isSelected());
                        if (tldCheckBox.isSelected()) {
                            if (tagName==null) {
                                String name = documentNameTextField.getText().trim();
                                if (name.length()>0) {
                                    tagNameTextField.setText(name);
                                    tagName = name;
                                }
                            }
                        }
                        wizardPanel.fireChange();
                    }
                });
                
                javax.swing.JLabel tagNameLabel = new javax.swing.JLabel();
                tagNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_TagName_mnem").charAt(0));          
                tagNameLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_tagName"));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 6;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
                customPanel.add(tagNameLabel, gridBagConstraints);
                
                tagNameTextField = new javax.swing.JTextField();
                //tagNameTextField.setColumns(10);
                tagNameTextField.setEditable(false);
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 6;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
                customPanel.add(tagNameTextField, gridBagConstraints);
                tagNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_TagName"));
                tagNameLabel.setLabelFor(tagNameTextField);
                tagNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        tagName = tagNameTextField.getText().trim();
                        wizardPanel.fireChange();
                    }
                });
                
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 6;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                customPanel.add(new javax.swing.JPanel(), gridBagConstraints);
            }
        } else if (FileType.TAGLIBRARY.equals(fileType)) {
            java.awt.GridBagConstraints gridBagConstraints;
            uriTextField = new javax.swing.JTextField();
            uriTextField.setColumns(20);
            prefixTextField = new javax.swing.JTextField();
            prefixTextField.setColumns(5);
            
            uriLabel = new javax.swing.JLabel(NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_URI"));
            uriLabel.setToolTipText(NbBundle.getMessage(TargetChooserPanelGUI.class, "TTT_URI"));
            uriLabel.setLabelFor(uriTextField);
            uriLabel.setDisplayedMnemonic(NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_URI_mnem").charAt(0));
            uriTextField.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_URI"));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            customPanel.add(uriLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            customPanel.add(uriTextField, gridBagConstraints);

            prefixLabel = new javax.swing.JLabel(NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Prefix"));
            prefixLabel.setLabelFor(prefixTextField);
            prefixLabel.setToolTipText(NbBundle.getMessage(TargetChooserPanelGUI.class, "TTT_prefix"));
            prefixLabel.setDisplayedMnemonic(NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_Prefix_mnem").charAt(0));
            prefixTextField.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_Prefix"));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            customPanel.add(prefixLabel, gridBagConstraints);
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
            customPanel.add(prefixTextField, gridBagConstraints);
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridheight = 2;
            gridBagConstraints.weightx = 2.0;
            customPanel.add(new javax.swing.JPanel(), gridBagConstraints);
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            customPanel.add(new javax.swing.JPanel(), gridBagConstraints);

            uriTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    uriWasTyped=true;
                    wizardPanel.fireChange();
                }
            });

            prefixTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    prefixWasTyped=true;
                    wizardPanel.fireChange();
                }
            });
        }
          
        browseButton.addActionListener( this );
        documentNameTextField.getDocument().addDocumentListener( this );
        folderTextField.getDocument().addDocumentListener( this );
        
        setName( NbBundle.getMessage(TargetChooserPanelGUI.class,"TITLE_name_location"));
        
        if (fileType.equals(FileType.JSP)) {
            nameLabel.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_JspName"));
            jspSyntaxButton.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "OPT_JspSyntax"));
            jspSyntaxButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(TargetChooserPanelGUI.class, "DESC_JSP"));
            xmlSyntaxButton.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "OPT_XmlSyntax"));
            xmlSyntaxButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(TargetChooserPanelGUI.class, "DESC_JSP_XML"));
            if (isFaceletsAvailable()) {
                faceletsSyntaxButton.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "OPT_Facelets"));
                faceletsSyntaxButton.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(TargetChooserPanelGUI.class, "DESC_FACELETS"));
            }
            segmentBox.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "OPT_JspSegment"));
            segmentBox.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_JSP_segment"));
            descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_JSP"));
        } else if (fileType.equals(FileType.TAG)) {
            nameLabel.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_TagFileName"));
            jspSyntaxButton.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "OPT_TagFileJsp"));
            jspSyntaxButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(TargetChooserPanelGUI.class, "DESC_TagFile"));
            xmlSyntaxButton.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "OPT_TagFileXml"));
            xmlSyntaxButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(TargetChooserPanelGUI.class, "DESC_TagFileXml"));
            segmentBox.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "OPT_TagFileSegment"));
            segmentBox.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_TagFile_segment"));
            descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_TagFile"));
        } else if (fileType.equals(FileType.TAGLIBRARY)) {
            nameLabel.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_TldName"));
        } else if (fileType.equals(FileType.HTML)) {
            nameLabel.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_HtmlName"));
            //listener to update fileTextField
            locationCB.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    changedUpdate(null);
                }
            });
        } else if (fileType.equals(FileType.CSS)) {
            nameLabel.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_CssName"));
            //listener to update fileTextField
            locationCB.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    changedUpdate(null);
                }
            });
        } else if (fileType.equals(FileType.XHTML)) {
            nameLabel.setText(NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_XHtmlName"));
            //listener to update fileTextField
            locationCB.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    changedUpdate(null);
                }
            });
        }
    }

    WebModule getWebModule() {
        return wm;
    }
    
    public void initValues( Project p, FileObject template, FileObject preselectedFolder ) {
        projectTextField.setText(ProjectUtils.getInformation(p).getDisplayName());
        // set the location field and find web module
        if (folders!=null && folders.length>0) {
            locationCB.setModel(new javax.swing.DefaultComboBoxModel(getLocations(folders)));
            wm = WebModule.getWebModule(folders[0].getRootFolder());
        } else
            locationCB.setModel(new javax.swing.DefaultComboBoxModel(
                new Object[]{new LocationItem(p.getProjectDirectory())}));
        
        // filling the folder field
        String target=null;
        if (preselectedFolder != null) {
            for(int item = 0; target == null && item < locationCB.getModel().getSize(); item++) {
                FileObject docBase = ((LocationItem)locationCB.getModel().getElementAt(item)).getFileObject();
                if (preselectedFolder.equals(docBase) || FileUtil.isParentOf(docBase, preselectedFolder)) {
                    target = FileUtil.getRelativePath(docBase, preselectedFolder);
                    locationCB.getModel().setSelectedItem(locationCB.getModel().getElementAt(item));
                    break;
                }
            }
        }
        
        // leave target null for tag files and TLDs outside the web project
        if (wm==null) {
            if (FileType.TAG.equals(fileType) && target!=null && !target.startsWith(TAG_FILE_IN_JAVALIB_FOLDER)) {
                target=null;
            }
            if (FileType.TAGLIBRARY.equals(fileType) && target!=null && !target.startsWith(TLD_IN_JAVALIB_FOLDER)) {
                target=null;
            }
        }

        // setting target folders for tag files and tlds 
        if (FileType.TAG.equals(fileType)) {
            if (wm!=null) {
                if (target==null || !target.startsWith(TAG_FILE_FOLDER))
                    folderTextField.setText(TAG_FILE_FOLDER+"/"); // NOI18N
                else folderTextField.setText( target == null ? "" : target ); // NOI18N
            } else {
                 if (target==null || !target.startsWith(TAG_FILE_IN_JAVALIB_FOLDER))
                     folderTextField.setText(TAG_FILE_IN_JAVALIB_FOLDER+"/"); // NOI18N
                 else folderTextField.setText( target == null ? "" : target ); // NOI18N
            }
        } else if (FileType.TAGLIBRARY.equals(fileType) && target==null) {
            if (wm==null) folderTextField.setText(TLD_IN_JAVALIB_FOLDER+"/"); // NOI18N
            else folderTextField.setText(TLD_FOLDER+"/"); // NOI18N
        } else 
            folderTextField.setText( target == null ? "" : target ); // NOI18N      
        String ext = template == null ? "" : template.getExt(); // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N

        //set default new file name
        String documentName = NEW_FILE_PREFIX + fileType.toString();
        String newDocumentName = documentName;
        File targetFolder = getFileCreationRoot();
        if (targetFolder != null) {
            FileObject folder = FileUtil.toFileObject(targetFolder);
            if (folder != null) {
                int index = 0;
                while (true) {
                    FileObject _tmp = folder.getFileObject(documentName, ext);
                    if (_tmp == null) {
                        break;
                    }
                    documentName = newDocumentName + (++index);
                }
            }
        }
        documentNameTextField.setText(documentName);
    }
    
    private Object[] getLocations(SourceGroup[] folders) {
        Object[] loc = new Object[folders.length];
        for (int i=0;i<folders.length;i++) loc[i] = new LocationItem(folders[i]);
        return loc;
    }
    
    private String getRelativeSourcesFolder() {
        String sourceDir="";
        if (wm!=null) {
            FileObject docBase = wm.getDocumentBase();
            FileObject sourcesBase = ((LocationItem)locationCB.getModel().getSelectedItem()).getFileObject();
            sourceDir = FileUtil.getRelativePath( docBase, sourcesBase );
            
            //just for source roots
            if (sourceDir == null)
                sourceDir = "";
        }
        return sourceDir.length()==0?"":sourceDir+"/";        
    }
    
    public String getRelativeTargetFolder() {
        return getRelativeSourcesFolder()+getNormalizedFolder();
    }
    
    public String getNormalizedFolder() {
        String norm = folderTextField.getText().trim();
        if (norm.length()==0) return "";       
        norm = norm.replace('\\','/');
        // removing leading slashes
        int i=0;
        while (i<norm.length() && norm.charAt(i)=='/') i++;
        if (i==norm.length()) return ""; //only slashes  
        norm = norm.substring(i);

        // removing multiple slashes
        java.util.StringTokenizer tokens = new java.util.StringTokenizer(norm,"/");
        java.util.List list = new java.util.ArrayList();
        StringBuffer buf = new StringBuffer(tokens.nextToken());
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (token.length()>0) buf.append("/"+token);
        }     
        return buf.toString();
    }
    
    public String getTargetFolder() {
        return getTargetFile().getPath();
    }
    
    public File getTargetFile() {
        String text = getRelativeTargetFolder();
        
        if ( text.length() == 0 ) {
            if (wm==null)
                return FileUtil.toFile(getLocationRoot());
            else
                return FileUtil.toFile( wm.getDocumentBase());
        }
        else {
            // XXX have to account for FU.tF returning null
            if (wm==null) {
                return new File( FileUtil.toFile(getLocationRoot()), text );
            } else {
                return new File( FileUtil.toFile( wm.getDocumentBase() ), text );
            }
        }
    }
    public String getTargetName() {
        
        String text = documentNameTextField.getText().trim();
        
        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationCB = new javax.swing.JComboBox();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        pathLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        customPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_FileName_mnem").charAt(0));
        nameLabel.setLabelFor(documentNameTextField);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_JspName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(nameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(documentNameTextField, gridBagConstraints);
        documentNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_FileName"));

        projectLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_Project_mnem").charAt(0));
        projectLabel.setLabelFor(projectTextField);
        projectLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Project"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(projectLabel, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_Project"));

        locationLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_Location_mnem").charAt(0));
        locationLabel.setLabelFor(locationCB);
        locationLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Location"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(locationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(locationCB, gridBagConstraints);
        locationCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_Location"));

        folderLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_Folder_mnem").charAt(0));
        folderLabel.setLabelFor(folderTextField);
        folderLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Folder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(folderLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(folderTextField, gridBagConstraints);
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_Folder"));

        browseButton.setMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Browse_Mnemonic").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Browse"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "ACSD_Browse"));

        pathLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_CreatedFile_mnem").charAt(0));
        pathLabel.setLabelFor(fileTextField);
        pathLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_CreatedFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(pathLabel, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_CreatedFile"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(targetSeparator, gridBagConstraints);

        customPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(customPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(fillerPanel, gridBagConstraints);

    }//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JPanel customPanel;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JComboBox locationCB;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            FileObject fo=null;
            // Show the browse dialog 
            if (folders!=null) fo = BrowseFolders.showDialog(folders,
                    org.openide.loaders.DataFolder.class,
                    folderTextField.getText().replace( File.separatorChar, '/' ) );
            else {		           
                Sources sources = ProjectUtils.getSources(project);
                fo = BrowseFolders.showDialog( sources.getSourceGroups( Sources.TYPE_GENERIC ),
                        org.openide.loaders.DataFolder.class,
                        folderTextField.getText().replace( File.separatorChar, '/' ) );
            }
            
            if ( fo != null && fo.isFolder() ) {
                FileObject root = ((LocationItem)locationCB.getSelectedItem()).getFileObject();
                folderTextField.setText( FileUtil.getRelativePath( root, fo ) );
            }
                        
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(javax.swing.event.DocumentEvent e) {

        File rootDirFile = FileUtil.toFile(((LocationItem)locationCB.getSelectedItem()).getFileObject());
        if (rootDirFile != null) {
            String documentName = documentNameTextField.getText().trim();
            if (documentName.length() == 0) {
                fileTextField.setText(""); // NOI18N
            } else {
                String extension = isFacelets() ? ".xhtml" : expectedExtension+(isSegment()?"f":(isXml()?"x":""));
                File newFile = new File(new File(rootDirFile, folderTextField.getText().replace('/', File.separatorChar)),
                                        documentName + extension); //NOI18N
                fileTextField.setText(newFile.getAbsolutePath());
            }
        } else {
            // Not on disk.
            fileTextField.setText(""); // NOI18N
        }
        if (FileType.TAGLIBRARY.equals(fileType)) {
            if (!uriWasTyped) {
                String norm=getNormalizedFolder();
                //Default value for uri
                if (wm==null) {
                    String pack = getPackageNameInMetaInf();
                    uriTextField.setText((pack.length()>0?pack+".":"")+documentNameTextField.getText());
                }
                else uriTextField.setText((norm.length()==0?"":"/"+getNormalizedFolder())+ //NOI18N
                                          "/"+documentNameTextField.getText().trim()); //NOI18N
            }
            //Default value for prefix
            if (!prefixWasTyped)
                 prefixTextField.setText(documentNameTextField.getText().trim().toLowerCase());
        }
        wizardPanel.fireChange();
    }
    
    private String getPackageNameInMetaInf() {
        String pack = getRelativeTargetFolder();
        if (pack.startsWith("META-INF")) {//NOI18N
            pack = pack.substring(8);
            if (pack.length()==0) return "";
            if (pack.startsWith("/")) pack=pack.substring(1);//NOI18N
        }
        if (pack.length()==0) return "";//NOI18N
        pack = pack.replace('/', '.');
        return pack;
    }
    
    
    /** specific for JSP/TAG wizards
     */
    private void checkBoxChanged(java.awt.event.ItemEvent evt) {
        if (fileType.equals(FileType.JSP)) {
            if (isSegment()) {
                if (isXml()) {
                    descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_segment_XML"));
                } else {
                    descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_segment"));
                }
                String createdFile = fileTextField.getText();
                if (createdFile.endsWith("jspx")) //NOI18N
                    fileTextField.setText(createdFile.substring(0,createdFile.length()-1)+"f"); //NOI18N
                else if (createdFile.endsWith("jsp")) //NOI18N
                    fileTextField.setText(createdFile+"f"); //NOI18N
            } else {
                String createdFile = fileTextField.getText();
                if (isXml()) {
                    descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_JSP_XML"));
                    if (createdFile.endsWith("jspf")) { //NOI18N
                        fileTextField.setText(createdFile.substring(0,createdFile.length()-1)+"x"); //NOI18N
                    } else if (createdFile.endsWith("jsp")) { //NOI18N
                        fileTextField.setText(createdFile+"x"); //NOI18N
                    } else {
                        fileTextField.setText(createdFile.substring(0,createdFile.lastIndexOf(".")+1)+"jspx"); //NOI18N
                    }
                } else if(isFacelets()) {
                    descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_FACELETS"));
                    if (createdFile.endsWith("jspf") || createdFile.endsWith("jspx")) { //NOI18N
                        fileTextField.setText(createdFile.substring(0,createdFile.length()-4)+"xhtml"); //NOI18N
                    } else if(createdFile.endsWith("jsp")) { //NOI18N
                        fileTextField.setText(createdFile.substring(0,createdFile.length()-3)+"xhtml"); //NOI18N
                    }
                    segmentBox.setEnabled(false);
                } else {
                    segmentBox.setEnabled(true);
                    descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_JSP"));
                    if (createdFile.endsWith("jspf") || createdFile.endsWith("jspx")) { //NOI18N
                        fileTextField.setText(createdFile.substring(0,createdFile.length()-1)); //NOI18N
                    } else {
                        fileTextField.setText(createdFile.substring(0,createdFile.lastIndexOf(".")+1)+"jsp"); //NOI18N
                    }
                }
            }
        } else if (fileType.equals(FileType.TAG)){
            if (isSegment()) {
                if (isXml()) {
                    descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_TagFileSegmentXml"));
                } else {
                    descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_TagFileSegment"));
                }
                String createdFile = fileTextField.getText();
                if (createdFile.endsWith("tagx")) //NOI18N
                    fileTextField.setText(createdFile.substring(0,createdFile.length()-1)+"f"); //NOI18N
                else if (createdFile.endsWith("tag")) //NOI18N 
                    fileTextField.setText(createdFile+"f"); //NOI18N
            } else {
                String createdFile = fileTextField.getText();
                if (isXml()) {
                    descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_TagFileXml"));
                    if (createdFile.endsWith("tagf")) { //NOI18N
                        fileTextField.setText(createdFile.substring(0,createdFile.length()-1)+"x"); //NOI18N
                    } else if (createdFile.endsWith("tag")) { //NOI18N
                        fileTextField.setText(createdFile+"x"); //NOI18N
                    }
                } else {
                    descriptionArea.setText(NbBundle.getMessage(TargetChooserPanelGUI.class,"DESC_TagFile"));
                    if (createdFile.endsWith("tagf") || createdFile.endsWith("tagx")) { //NOI18N
                        fileTextField.setText(createdFile.substring(0,createdFile.length()-1)); //NOI18N
                    }
                }
            }
        }
        wizardPanel.fireChange();
    }
    
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    boolean isXml() {
        if (xmlSyntaxButton==null) return false;
        else return xmlSyntaxButton.isSelected();
    }
    
    boolean isSegment() {
        if (segmentBox==null) return false;
        else return segmentBox.isSelected();
    }

    boolean isFacelets() {
        if (faceletsSyntaxButton == null) return false;
        else return faceletsSyntaxButton.isSelected();
    }

    String getUri() {
        if (uriTextField==null) return "";
        else return uriTextField.getText();
    }
    String getPrefix() {
        if (prefixTextField==null) return "";
        else return prefixTextField.getText();
    }
    
    String getErrorMessage() {
        if (FileType.JSP.equals(fileType)) {
            if (isSegment() && !getNormalizedFolder().startsWith("WEB-INF/jspf")) //NOI18N
                return NbBundle.getMessage(TargetChooserPanelGUI.class,"NOTE_segment");
        } else if (FileType.TAG.equals(fileType)) {
            tagFileValid=true;
            if (wm!=null) {
                if (!getNormalizedFolder().startsWith(TAG_FILE_FOLDER)) {
                    tagFileValid=false;
                    return NbBundle.getMessage(TargetChooserPanelGUI.class,"MSG_TagFile");
                }
            } else {
                if (!getNormalizedFolder().startsWith(TAG_FILE_IN_JAVALIB_FOLDER)) {
                    tagFileValid=false;
                    return NbBundle.getMessage(TargetChooserPanelGUI.class,"MSG_TagFileInJavalib");
                }
            }
        } else if (FileType.TAGLIBRARY.equals(fileType)) {
            if (wm==null) {
                if (!getNormalizedFolder().startsWith(TLD_IN_JAVALIB_FOLDER))
                    return NbBundle.getMessage(TargetChooserPanelGUI.class,"NOTE_TLDInJavalib");
            } else
                if (!getNormalizedFolder().startsWith("WEB-INF")) //NOI18N
                    return NbBundle.getMessage(TargetChooserPanelGUI.class,"NOTE_TLDInWeb");              
            if (getUri().length()==0) //NOI18N
                return NbBundle.getMessage(TargetChooserPanelGUI.class,"MSG_missingUri");
            if (getPrefix().length()==0) //NOI18N
                return NbBundle.getMessage(TargetChooserPanelGUI.class,"MSG_missingPrefix");
        }
        return null;
    }
    
    boolean isPanelValid() {
        if (FileType.TAGLIBRARY.equals(fileType) && 
            (getUri().length()==0 || getPrefix().length()==0))
            return false;
        if (FileType.TAG.equals(fileType) && !tagFileValid)
            return false;
        return true;
    }
    
    FileObject getLocationRoot() {
        return ((LocationItem)locationCB.getModel().getSelectedItem()).getFileObject();
    }
    
    public static class LocationItem {
        FileObject fo;
        SourceGroup group;
        public LocationItem(FileObject fo) {
            this.fo=fo;
        }
        public LocationItem(SourceGroup group) {
            this.fo=group.getRootFolder();
            this.group=group;
        }        
        public FileObject getFileObject() {
            return fo;
        }
        
        public String toString() {
            return (group==null?fo.getName():group.getDisplayName());
        }
    }
    
    private void browseButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                             
        org.openide.filesystems.FileObject fo=null;
        // Show the browse dialog 
        if (folders!=null) fo = BrowseFolders.showDialog(folders, TLDDataObject.class,
                folderTextField.getText().replace( File.separatorChar, '/' ) );
        else {       
            Sources sources = ProjectUtils.getSources(project);
            fo = BrowseFolders.showDialog( sources.getSourceGroups( Sources.TYPE_GENERIC ),
                                           org.openide.loaders.DataFolder.class,
                    folderTextField.getText().replace( File.separatorChar, '/' ) );
        }

        if ( fo != null) {
            tldFo=fo;
            FileObject targetFolder=Templates.getTargetFolder(wizardPanel.getTemplateWizard());
            WebModule wm = (targetFolder==null?null:WebModule.getWebModule(targetFolder));
            tldTextField.setText( FileUtil.getRelativePath( (wm==null?project.getProjectDirectory():wm.getDocumentBase()), fo ) );
            try {
                java.io.InputStream is = tldFo.getInputStream();
                // get existing tag names for testing duplicity
                tagValues = Util.getTagValues(is, new String[]{"tag","tag-file"},"name"); //NOI18N
                is.close();
            }
            catch (java.io.IOException ex) {
                LOG.log(Level.FINE, "error", ex);
            }
            catch (org.xml.sax.SAXException ex){
                LOG.log(Level.FINE, "error", ex);
            }
            wizardPanel.fireChange();
        }
    }

    boolean isFaceletsAvailable() {
        if (preferredLanguage == null) {
            Preferences preferences = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
            String lang = preferences.get("jsf.language", "NOLANG");   //NOI18N
            if ("JSP".equals(lang))   //NOI18N
                preferredLanguage = TargetChooserPanel.PreferredLanguage.JSP;
            else if("Facelets".equals(lang))   //NOI18N
                preferredLanguage = TargetChooserPanel.PreferredLanguage.Facelets;
            else
                return false;
        }
        if (preferredLanguage == TargetChooserPanel.PreferredLanguage.Facelets)
            return true;
        return false;
    }

    boolean isTldCheckBoxSelected() {
        return tldCheckBox.isSelected();
    }
    
    String getTagName() {
        return tagName;
    }
    
    FileObject getTldFileObject() {
        return tldFo;
    }
    
    static boolean isTagNameEmpty(String name) {
        if (name == null) {
            return true;
        }
        return "".equals(name); // NOI18N
    }

    static boolean isValidTagName(String name) {
        if (name==null) return false;
        return org.apache.xerces.util.XMLChar.isValidNCName(name);
    }
    
    boolean tagNameExists(String name) {
        if (tagValues!=null && tagValues.contains(name)) return true; 
        else return false;
    }
    
    public String getCreatedFilePath() {
        return fileTextField.getText();
    }

    private File getFileCreationRoot() {
        File rootDirFile = FileUtil.toFile(((LocationItem) locationCB.getSelectedItem()).getFileObject());
        if (rootDirFile != null) {
            return new File(rootDirFile, folderTextField.getText().replace('/', File.separatorChar));
        } else {
            return null;
        }
    }

}
