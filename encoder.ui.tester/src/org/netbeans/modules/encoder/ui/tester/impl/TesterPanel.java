/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.encoder.ui.tester.impl;

import java.io.File;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.namespace.QName;
import org.netbeans.modules.encoder.ui.basic.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;

/**
 * Implementation of the Encoder tester form
 *
 * @author  Cannis Meng
 */
public class TesterPanel extends javax.swing.JPanel implements DocumentListener {

    private static final String[] CHARSET_NAMES_EXTRA = Utils.getCharsetNames(true);
    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/ui/tester/impl/Bundle");

    private static final String PREF_XSD_FILE = "xsd-file";  //NOI18N
    private static final String PREF_TOP_ELEM = "top-elem";  //NOI18N
    private static final String PREF_ACTION = "action";  //NOI18N
    private static final String PREF_INPUT = "in-file";  //NOI18N
    private static final String PREF_OUTPUT = "out-file";  //NOI18N
    private static final String PREF_OVERWRITE = "overwrite";  //NOI18N
    private static final String PREF_CHAR_BASED = "char_based"; //NOI18N
    private static final String PREF_DOC_CODING = "doc_coding"; //NOI18N
    private static final String ACTION_ENCODE = "encode";  //NOI18N
    private static final String ACTION_DECODE = "decode";  //NOI18N
    private static final String PREF_DEBUG_LEVEL = "debug_level";  //NOI18N
    private static final String[] DEBUG_LEVELS = new String[] {
        _bundle.getString("test_panel.verbose.none"),
        _bundle.getString("test_panel.verbose.info"),
        _bundle.getString("test_panel.verbose.fine"),
        _bundle.getString("test_panel.verbose.finer"),
        _bundle.getString("test_panel.verbose.finest")};

    private String xsdFilePath;
    private Preferences mPrefs = Preferences.userNodeForPackage(this.getClass());

    /** Creates new form TesterPanel */
    public TesterPanel(String xsdFile) {
        xsdFilePath = xsdFile;
        initComponents();
        buttonGroup1.add(this.jRadioButtonEncode);
        buttonGroup1.add(this.jRadioButtonDecode);
        jTextFieldXSDFile.setText(xsdFile);
        jTextFieldXSDFile.setEditable(false);
        File f = new File(xsdFile);
        jTextFieldFolder.setText(f.getParent());
        this.jTextFieldFileName.getDocument().addDocumentListener(this);
        this.jTextFieldFolder.getDocument().addDocumentListener(this);
        this.jTextFieldCreatedFile.setEditable(false);
        jRadioButtonDecode.setSelected(true);
        if (xsdFilePath != null) {
            File file = new File(xsdFilePath);
            if (file.getParent() != null) {
                jTextFieldFolder.setText(file.getParent());
            }
        }
        setComboBoxList(jComboBoxResultCoding, CHARSET_NAMES_EXTRA);
        jComboBoxResultCoding.setSelectedIndex(-1);
        setComboBoxList(jComboBoxSourceCoding, CHARSET_NAMES_EXTRA);
        jComboBoxSourceCoding.setSelectedIndex(-1);
        setComboBoxList(jComboBoxVerboseLevel, DEBUG_LEVELS);
        jComboBoxVerboseLevel.setSelectedIndex(0);
        applyPreferences();
        updateComponents();
    }

    private void setComboBoxList(JComboBox comboBox, String[] list) {
        comboBox.removeAllItems();
        for (int i = 0; i < list.length; i++) {
            comboBox.addItem(list[i]);
        }
        comboBox.setSelectedIndex(-1);
    }

    private void applyPreferences() {
        if (sameAsPreviousLaunch()) {
            String value = mPrefs.get(PREF_ACTION, null);
            if (value == null) {
                return;
            }
            if (ACTION_DECODE.equals(value)) {
                jRadioButtonDecode.setSelected(true);
            } else {
                jRadioButtonEncode.setSelected(true);
            }
            value = mPrefs.get(PREF_INPUT, null);
            if (value == null) {
                return;
            }
            if (jRadioButtonEncode.isSelected()) {
                jTextFieldXMLSourceFile.setText(value);
                value = mPrefs.get(PREF_CHAR_BASED, null);
                if (value != null) {
                    jCheckBoxToString.setSelected(Boolean.valueOf(value));
                }
                value = mPrefs.get(PREF_DOC_CODING, null);
                if (value != null) {
                    jComboBoxResultCoding.getEditor().setItem(value);
                }
            } else {
                jTextFieldDataFile.setText(value);
                value = mPrefs.get(PREF_CHAR_BASED, null);
                if (value != null) {
                    jCheckBoxFromString.setSelected(Boolean.valueOf(value));
                }
                value = mPrefs.get(PREF_DOC_CODING, null);
                if (value != null) {
                    jComboBoxSourceCoding.getEditor().setItem(value);
                }
            }
            value = mPrefs.get(PREF_OUTPUT, null);
            if (value == null) {
                return;
            }
            File outputFile = new File(value);
            if (outputFile.getName() != null) {
                int pos = outputFile.getName().lastIndexOf('.');
                if (pos >= 0) {
                    jTextFieldFileName.setText(outputFile.getName().substring(0, pos));
                } else {
                    jTextFieldFileName.setText(outputFile.getName());
                }
            }
            jTextFieldFolder.setText(outputFile.getParent());
            jCheckBoxOverwriteOutput.setSelected(mPrefs.getBoolean(PREF_OVERWRITE, true));
            jComboBoxVerboseLevel.setSelectedIndex(mPrefs.getInt(PREF_DEBUG_LEVEL, 0));
        }
    }

    public void savePreferences() throws BackingStoreException {
        mPrefs.put(PREF_XSD_FILE, xsdFilePath);
        if (this.getActionType().equals(EncoderTestPerformerImpl.DECODE)) {
            mPrefs.put(PREF_ACTION, ACTION_DECODE);
            mPrefs.putBoolean(PREF_CHAR_BASED, isFromString());
            mPrefs.put(PREF_DOC_CODING, getPredecodeCoding());
        } else {
            mPrefs.put(PREF_ACTION, ACTION_ENCODE);
            mPrefs.putBoolean(PREF_CHAR_BASED, isToString());
            mPrefs.put(PREF_DOC_CODING, getPostencodeCoding());
        }
        if (getSelectedTopElementDecl() != null) {
            mPrefs.put(PREF_TOP_ELEM, getSelectedTopElementDecl().toString());
        }
        if (getProcessFile() != null) {
            mPrefs.put(PREF_INPUT, getProcessFile());
        }
        if (getOutputFile() != null) {
            mPrefs.put(PREF_OUTPUT, getOutputFile());
        }
        mPrefs.putBoolean(PREF_OVERWRITE, isOverwrite());
        mPrefs.putInt(PREF_DEBUG_LEVEL, getDebugLevelIndex());
        mPrefs.flush();
    }

    private void updateComponents() {
        if (!jRadioButtonEncode.isSelected()) {
            jLabelXMLSourceFile.setEnabled(false);
            jTextFieldXMLSourceFile.setEnabled(false);
            jButtonBrowseXMLSourceFile.setEnabled(false);
            jLabelResultCoding.setEnabled(false);
            jComboBoxResultCoding.setEnabled(false);
            jCheckBoxToString.setEnabled(false);
        } else {
            jLabelXMLSourceFile.setEnabled(true);
            jTextFieldXMLSourceFile.setEnabled(true);
            jButtonBrowseXMLSourceFile.setEnabled(true);
            jLabelResultCoding.setEnabled(true);
            jComboBoxResultCoding.setEnabled(true);
            jCheckBoxToString.setEnabled(true);
        }

        if (!jRadioButtonDecode.isSelected()) {
            jLabelDataFile.setEnabled(false);
            jTextFieldDataFile.setEnabled(false);
            jButtonBrowseDataFile.setEnabled(false);
            jLabelSourceCoding.setEnabled(false);
            jComboBoxSourceCoding.setEnabled(false);
            jCheckBoxFromString.setEnabled(false);
        } else {
            jLabelDataFile.setEnabled(true);
            jTextFieldDataFile.setEnabled(true);
            jButtonBrowseDataFile.setEnabled(true);
            jLabelSourceCoding.setEnabled(true);
            jComboBoxSourceCoding.setEnabled(true);
            jCheckBoxFromString.setEnabled(true);
        }

        updateCreatedFolder();
    }

    private boolean sameAsPreviousLaunch() {
        String prefXsdFile = mPrefs.get(PREF_XSD_FILE, null);
        if (prefXsdFile == null) {
            return false;
        }
        return new File(xsdFilePath).equals(new File(prefXsdFile));
    }

    public String getOutputFileName() {
        return jTextFieldFileName.getText();
    }

    /**
     * Gets the generated output file in full path.
     *
     * @return output file path
     */
    public String getOutputFile() {
        return jTextFieldCreatedFile.getText();
    }

    /**
     * Gets the action type. ie. either encode or decode.
     *
     * @return action string
     */
    public String getActionType() {
        return jRadioButtonEncode.isSelected() ? EncoderTestPerformerImpl.ENCODE : EncoderTestPerformerImpl.DECODE;
    }


    /**
     * Gets the process file. For encode, it will be an xml file;
     * for decode, it will be any data file with the decoded string.
     *
     * @return process file full path
     */
    public String getProcessFile() {
        if (this.getActionType().equals(EncoderTestPerformerImpl.ENCODE)) {
            return jTextFieldXMLSourceFile.getText();
        } else {
            return jTextFieldDataFile.getText();
        }
    }

    /**
     * Overwrites the output file?
     */
    public boolean isOverwrite() {
        return jCheckBoxOverwriteOutput.isSelected();
    }

    /**
     * Is encoding to string?
     */
    public boolean isToString() {
        return jCheckBoxToString.isSelected();
    }

    /**
     * Is decoding from string?
     */
    public boolean isFromString() {
        return jCheckBoxFromString.isSelected();
    }

    /**
     * Gets the pre-decoding coding.
     */
    public String getPredecodeCoding() {
        Object obj = jComboBoxSourceCoding.getEditor().getItem();
        return obj == null ? "" : obj.toString(); //NOI18N
    }

    /**
     * Gets the post-encoding coding.
     */
    public String getPostencodeCoding() {
        Object obj = jComboBoxResultCoding.getEditor().getItem();
        return obj == null ? "" : obj.toString(); //NOI18N
    }

    /**
     * Returns the debug level selection index starting from 0.
     * @return the debug level selection index starting from 0.
     */
    public int getDebugLevelIndex() {
        return jComboBoxVerboseLevel.getSelectedIndex();
    }

    /**
     * Returns the debug level string.
     * @return the debug level string.
     */
    public String getDebugLevel() {
        return jComboBoxVerboseLevel.getSelectedItem().toString();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabelXSDFile = new javax.swing.JLabel();
        jTextFieldXSDFile = new javax.swing.JTextField();
        jRadioButtonEncode = new javax.swing.JRadioButton();
        jRadioButtonDecode = new javax.swing.JRadioButton();
        jTextFieldXMLSourceFile = new javax.swing.JTextField();
        jTextFieldDataFile = new javax.swing.JTextField();
        jLabelFileName = new javax.swing.JLabel();
        jTextFieldFileName = new javax.swing.JTextField();
        jCheckBoxOverwriteOutput = new javax.swing.JCheckBox();
        jButtonBrowseXMLSourceFile = new javax.swing.JButton();
        jButtonBrowseDataFile = new javax.swing.JButton();
        jLabelDataFile = new javax.swing.JLabel();
        jLabelXMLSourceFile = new javax.swing.JLabel();
        jLabelFolder = new javax.swing.JLabel();
        jTextFieldFolder = new javax.swing.JTextField();
        jButtonBrowseFolder = new javax.swing.JButton();
        jLabelCreatedFile = new javax.swing.JLabel();
        jTextFieldCreatedFile = new javax.swing.JTextField();
        jComboBoxSelectElement = new javax.swing.JComboBox();
        jLabelSelectAnElement = new javax.swing.JLabel();
        jCheckBoxToString = new javax.swing.JCheckBox();
        jLabelResultCoding = new javax.swing.JLabel();
        jComboBoxResultCoding = new javax.swing.JComboBox();
        jCheckBoxFromString = new javax.swing.JCheckBox();
        jLabelSourceCoding = new javax.swing.JLabel();
        jComboBoxSourceCoding = new javax.swing.JComboBox();
        jSeparatorMeta = new javax.swing.JSeparator();
        jSeparatorDecodeEncode = new javax.swing.JSeparator();
        jSeparatorInputOutput = new javax.swing.JSeparator();
        jSeparatorDebug = new javax.swing.JSeparator();
        jLabelVerboseLevel = new javax.swing.JLabel();
        jComboBoxVerboseLevel = new javax.swing.JComboBox();
        jLabelMeta = new javax.swing.JLabel();
        jLabelInput = new javax.swing.JLabel();
        jLabelOutput = new javax.swing.JLabel();
        jLabelDebug = new javax.swing.JLabel();

        setFocusTraversalPolicy(null);

        jLabelXSDFile.setDisplayedMnemonic('i');
        jLabelXSDFile.setLabelFor(jTextFieldXSDFile);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/encoder/ui/tester/impl/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabelXSDFile, bundle.getString("test_panel.lbl.xsd_file")); // NOI18N

        jTextFieldXSDFile.setBackground(new java.awt.Color(240, 240, 240));
        jTextFieldXSDFile.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.xsd_file.description")); // NOI18N
        jTextFieldXSDFile.setPreferredSize(new java.awt.Dimension(94, 19));

        jRadioButtonEncode.setMnemonic('E');
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonEncode, bundle.getString("test_panel.lbl.encode")); // NOI18N
        jRadioButtonEncode.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.encode.description")); // NOI18N
        jRadioButtonEncode.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonEncode.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButtonEncode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonEncodeActionPerformed(evt);
            }
        });

        jRadioButtonDecode.setMnemonic('D');
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonDecode, org.openide.util.NbBundle.getBundle(TesterPanel.class).getString("test_panel.lbl.decode")); // NOI18N
        jRadioButtonDecode.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.decode.description")); // NOI18N
        jRadioButtonDecode.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonDecode.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButtonDecode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDecodeActionPerformed(evt);
            }
        });

        jTextFieldXMLSourceFile.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.xml_source_file.description")); // NOI18N

        jTextFieldDataFile.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.data_file.description")); // NOI18N

        jLabelFileName.setDisplayedMnemonic('N');
        jLabelFileName.setLabelFor(jTextFieldFileName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelFileName, bundle.getString("test_panel.lbl.output_file_name")); // NOI18N

        jTextFieldFileName.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_file_name.description")); // NOI18N

        jCheckBoxOverwriteOutput.setMnemonic('O');
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxOverwriteOutput, bundle.getString("test_panel.lbl.overwrite_output")); // NOI18N
        jCheckBoxOverwriteOutput.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.overwrite_output.description")); // NOI18N
        jCheckBoxOverwriteOutput.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxOverwriteOutput.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jButtonBrowseXMLSourceFile.setMnemonic('B');
        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowseXMLSourceFile, bundle.getString("test_panel.lbl.browse2")); // NOI18N
        jButtonBrowseXMLSourceFile.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.input_xml_source_file.browse.description")); // NOI18N
        jButtonBrowseXMLSourceFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseXMLSourceFileActionPerformed(evt);
            }
        });

        jButtonBrowseDataFile.setMnemonic('B');
        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowseDataFile, bundle.getString("test_panel.lbl.browse1")); // NOI18N
        jButtonBrowseDataFile.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.input_data_file.browse.description")); // NOI18N
        jButtonBrowseDataFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseDataFileActionPerformed(evt);
            }
        });

        jLabelDataFile.setDisplayedMnemonic('a');
        jLabelDataFile.setLabelFor(jTextFieldDataFile);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDataFile, bundle.getString("test_panel.lbl.input_data_file")); // NOI18N

        jLabelXMLSourceFile.setDisplayedMnemonic('X');
        jLabelXMLSourceFile.setLabelFor(jTextFieldXMLSourceFile);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelXMLSourceFile, bundle.getString("test_panel.lbl.xml_source")); // NOI18N

        jLabelFolder.setDisplayedMnemonic('F');
        jLabelFolder.setLabelFor(jTextFieldFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelFolder, bundle.getString("test_panel.lbl.output_folder")); // NOI18N

        jTextFieldFolder.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_folder.description")); // NOI18N

        jButtonBrowseFolder.setMnemonic('w');
        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowseFolder, bundle.getString("test_panel.lbl.browse3")); // NOI18N
        jButtonBrowseFolder.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_folder.browse.description")); // NOI18N
        jButtonBrowseFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseFolderActionPerformed(evt);
            }
        });

        jLabelCreatedFile.setDisplayedMnemonic('l');
        jLabelCreatedFile.setLabelFor(jTextFieldCreatedFile);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelCreatedFile, bundle.getString("test_panel.lbl.created_file")); // NOI18N

        jTextFieldCreatedFile.setBackground(new java.awt.Color(240, 240, 240));
        jTextFieldCreatedFile.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.created_file.description")); // NOI18N

        jComboBoxSelectElement.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxSelectElement.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.select_an_element.description")); // NOI18N

        jLabelSelectAnElement.setDisplayedMnemonic('S');
        jLabelSelectAnElement.setLabelFor(jComboBoxSelectElement);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSelectAnElement, bundle.getString("test_panel.lbl.select_an_element")); // NOI18N

        jCheckBoxToString.setMnemonic('T');
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxToString, org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.to_string")); // NOI18N
        jCheckBoxToString.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.to_string.description")); // NOI18N
        jCheckBoxToString.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxToString.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabelResultCoding.setDisplayedMnemonic('R');
        jLabelResultCoding.setLabelFor(jComboBoxResultCoding);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelResultCoding, org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.result_coding")); // NOI18N

        jComboBoxResultCoding.setEditable(true);
        jComboBoxResultCoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxResultCoding.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.result_coding.description")); // NOI18N

        jCheckBoxFromString.setMnemonic('m');
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxFromString, org.openide.util.NbBundle.getBundle(TesterPanel.class).getString("test_panel.lbl.from_string")); // NOI18N
        jCheckBoxFromString.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.from_string.description")); // NOI18N
        jCheckBoxFromString.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxFromString.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabelSourceCoding.setDisplayedMnemonic('u');
        jLabelSourceCoding.setLabelFor(jComboBoxSourceCoding);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSourceCoding, org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.source_coding")); // NOI18N

        jComboBoxSourceCoding.setEditable(true);
        jComboBoxSourceCoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxSourceCoding.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.source_coding.description")); // NOI18N

        jLabelVerboseLevel.setDisplayedMnemonic('V');
        jLabelVerboseLevel.setLabelFor(jComboBoxVerboseLevel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVerboseLevel, org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.verbose_level")); // NOI18N

        jComboBoxVerboseLevel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxVerboseLevel.setToolTipText(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.verbose_level.description")); // NOI18N

        jLabelMeta.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelMeta.setLabelFor(jLabelMeta);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMeta, org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.meta_section")); // NOI18N

        jLabelInput.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelInput.setLabelFor(jLabelInput);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelInput, org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.input_section")); // NOI18N

        jLabelOutput.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabelOutput.setLabelFor(jLabelOutput);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelOutput, org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.output_section")); // NOI18N

        jLabelDebug.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelDebug.setLabelFor(jLabelDebug);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDebug, org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.debug_section")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelDebug)
                    .add(jLabelOutput, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .add(jLabelInput)
                    .add(jLabelMeta))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 6, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jRadioButtonEncode)
                    .add(jLabelXMLSourceFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .add(jLabelResultCoding, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .add(jLabelFileName)
                    .add(jLabelFolder)
                    .add(jLabelVerboseLevel)
                    .add(jLabelSourceCoding)
                    .add(jLabelDataFile)
                    .add(jRadioButtonDecode)
                    .add(jLabelXSDFile)
                    .add(jLabelSelectAnElement)
                    .add(jLabelCreatedFile))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBoxResultCoding, 0, 339, Short.MAX_VALUE)
                    .add(jTextFieldXMLSourceFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .add(jCheckBoxToString)
                    .add(jTextFieldFileName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .add(jTextFieldFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .add(jCheckBoxOverwriteOutput)
                    .add(jTextFieldCreatedFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .add(jComboBoxSourceCoding, 0, 339, Short.MAX_VALUE)
                    .add(jTextFieldDataFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .add(jCheckBoxFromString)
                    .add(jTextFieldXSDFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .add(jComboBoxSelectElement, 0, 339, Short.MAX_VALUE)
                    .add(jComboBoxVerboseLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 337, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jButtonBrowseFolder)
                    .add(jButtonBrowseXMLSourceFile)
                    .add(jButtonBrowseDataFile))
                .addContainerGap(17, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jSeparatorDecodeEncode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 553, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jSeparatorMeta, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparatorDebug, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparatorInputOutput, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(82, 82, 82)
                        .add(jLabelInput))
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(9, 9, 9)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabelSelectAnElement)
                                    .add(jComboBoxSelectElement, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(8, 8, 8)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabelXSDFile)
                                    .add(jTextFieldXSDFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(jLabelMeta))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparatorMeta, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(30, 30, 30)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jRadioButtonDecode)
                            .add(jCheckBoxFromString))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                            .add(jButtonBrowseDataFile)
                            .add(jTextFieldDataFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabelDataFile))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                                    .add(jComboBoxSourceCoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabelSourceCoding))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jSeparatorDecodeEncode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jRadioButtonEncode)
                                    .add(jCheckBoxToString))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                                    .add(jLabelXMLSourceFile)
                                    .add(jTextFieldXMLSourceFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jButtonBrowseXMLSourceFile))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabelResultCoding)
                                .add(11, 11, 11)
                                .add(jSeparatorInputOutput, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabelOutput)
                                .add(11, 11, 11)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabelFileName)
                                    .add(jTextFieldFileName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                                    .add(jButtonBrowseFolder)
                                    .add(jTextFieldFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabelFolder))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jCheckBoxOverwriteOutput)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jTextFieldCreatedFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabelCreatedFile))
                                .add(16, 16, 16)
                                .add(jSeparatorDebug, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(5, 5, 5)
                                        .add(jLabelDebug))
                                    .add(layout.createSequentialGroup()
                                        .add(18, 18, 18)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                            .add(jLabelVerboseLevel)
                                            .add(jComboBoxVerboseLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                            .add(layout.createSequentialGroup()
                                .add(84, 84, 84)
                                .add(jComboBoxResultCoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabelXSDFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.xsd_file")); // NOI18N
        jLabelXSDFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.xsd_file")); // NOI18N
        jTextFieldXSDFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.xsd_file.name")); // NOI18N
        jTextFieldXSDFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.xsd_file.description")); // NOI18N
        jRadioButtonEncode.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.encode.name")); // NOI18N
        jRadioButtonEncode.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.encode.description")); // NOI18N
        jRadioButtonDecode.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.decode.name")); // NOI18N
        jRadioButtonDecode.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.decode.description")); // NOI18N
        jTextFieldXMLSourceFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.xml_source_file.name")); // NOI18N
        jTextFieldXMLSourceFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.xml_source_file.description")); // NOI18N
        jTextFieldDataFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.input_data_file.name")); // NOI18N
        jTextFieldDataFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.data_file.description")); // NOI18N
        jLabelFileName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.output_file_name")); // NOI18N
        jLabelFileName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.output_file_name")); // NOI18N
        jTextFieldFileName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panell.output_file_name.name")); // NOI18N
        jTextFieldFileName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_file_name.description")); // NOI18N
        jCheckBoxOverwriteOutput.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.overwrite_output.name")); // NOI18N
        jCheckBoxOverwriteOutput.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.overwrite_output.description")); // NOI18N
        jButtonBrowseXMLSourceFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.input_xml_source_file.browse.name")); // NOI18N
        jButtonBrowseXMLSourceFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.input_xml_source_file.browse.description")); // NOI18N
        jButtonBrowseDataFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.input_data_file.browse.name")); // NOI18N
        jButtonBrowseDataFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.input_data_file.browse.description")); // NOI18N
        jLabelDataFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.input_data_file")); // NOI18N
        jLabelDataFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.input_data_file")); // NOI18N
        jLabelXMLSourceFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.xml_source")); // NOI18N
        jLabelXMLSourceFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.xml_source")); // NOI18N
        jLabelFolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.output_folder")); // NOI18N
        jLabelFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.output_folder")); // NOI18N
        jTextFieldFolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_folder.name")); // NOI18N
        jTextFieldFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_folder.description")); // NOI18N
        jButtonBrowseFolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_folder.browse.name")); // NOI18N
        jButtonBrowseFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_folder.browse.description")); // NOI18N
        jLabelCreatedFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.created_file")); // NOI18N
        jLabelCreatedFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.created_file")); // NOI18N
        jTextFieldCreatedFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.created_file.name")); // NOI18N
        jTextFieldCreatedFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.created_file.description")); // NOI18N
        jComboBoxSelectElement.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.select_an_element.name")); // NOI18N
        jComboBoxSelectElement.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.select_an_element.description")); // NOI18N
        jLabelSelectAnElement.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.select_an_element")); // NOI18N
        jLabelSelectAnElement.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.select_an_element")); // NOI18N
        jCheckBoxToString.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.to_string.name")); // NOI18N
        jCheckBoxToString.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.to_string.description")); // NOI18N
        jLabelResultCoding.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.result_coding")); // NOI18N
        jLabelResultCoding.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.result_coding")); // NOI18N
        jComboBoxResultCoding.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.result_coding.name")); // NOI18N
        jComboBoxResultCoding.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.result_coding.description")); // NOI18N
        jCheckBoxFromString.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.from_string.name")); // NOI18N
        jCheckBoxFromString.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.from_string.description")); // NOI18N
        jLabelSourceCoding.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.source_coding")); // NOI18N
        jLabelSourceCoding.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.source_coding")); // NOI18N
        jComboBoxSourceCoding.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.source_coding.name")); // NOI18N
        jComboBoxSourceCoding.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.source_coding.description")); // NOI18N
        jSeparatorMeta.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.separator.meta")); // NOI18N
        jSeparatorMeta.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.separator.meta")); // NOI18N
        jSeparatorDecodeEncode.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.separator.decode_encode")); // NOI18N
        jSeparatorDecodeEncode.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.separator.decode_encode")); // NOI18N
        jSeparatorInputOutput.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.separator.input_output")); // NOI18N
        jSeparatorInputOutput.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.separator.input_output")); // NOI18N
        jSeparatorDebug.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.separator.debug")); // NOI18N
        jSeparatorDebug.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.separator.debug")); // NOI18N
        jLabelVerboseLevel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.verbose_level")); // NOI18N
        jLabelVerboseLevel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.verbose_level")); // NOI18N
        jComboBoxVerboseLevel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.verbose_level.name")); // NOI18N
        jComboBoxVerboseLevel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.verbose_level.description")); // NOI18N
        jLabelMeta.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.lbl.meta_section")); // NOI18N
        jLabelMeta.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.meta_section.description")); // NOI18N
        jLabelInput.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.input_section.description")); // NOI18N
        jLabelInput.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.input_section.description")); // NOI18N
        jLabelOutput.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_section.description")); // NOI18N
        jLabelOutput.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.output_section.description")); // NOI18N
        jLabelDebug.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.debug_section.description")); // NOI18N
        jLabelDebug.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.debug_section.description")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.name")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TesterPanel.class, "test_panel.description")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonBrowseFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseFolderActionPerformed
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }

            public String getDescription() {
                return _bundle.getString("test_panel.lbl.all_directories"); //NOI18N
            }
        };
        chooser.setFileFilter(fileFilter);
        String whereToLook;
        if (jTextFieldFolder.getText() != null && jTextFieldFolder.getText().length() != 0) {
            whereToLook = jTextFieldFolder.getText();
        } else {
            whereToLook = xsdFilePath;
        }
        chooser.setCurrentDirectory(new File(whereToLook));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        File selectedFile = null;
        if ( chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ) {
            selectedFile = chooser.getSelectedFile();
        }
        if (selectedFile != null) {
            this.jTextFieldFolder.setText(selectedFile.getAbsolutePath());
            updateCreatedFolder();
        }
}//GEN-LAST:event_jButtonBrowseFolderActionPerformed

    private void jButtonBrowseDataFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseDataFileActionPerformed
        String whereToLook;
        if (jTextFieldDataFile.getText() != null && jTextFieldDataFile.getText().length() != 0) {
            whereToLook = jTextFieldDataFile.getText();
        } else {
            whereToLook = mPrefs.get(PREF_INPUT, xsdFilePath);
        }
        File selectedFile = this.getFileFromChooser(whereToLook, null);
        if (selectedFile == null) {
            return;
        }
        if (!selectedFile.exists()) {
            NotifyDescriptor desc = new NotifyDescriptor.Message(
                    _bundle.getString("test_panel.lbl.file_does_not_exist"), //NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return;
        }
        jTextFieldDataFile.setText(selectedFile.getAbsolutePath());
        if (jTextFieldFileName.getText() == null || jTextFieldFileName.getText().length() == 0) {
            String name = selectedFile.getName();
            name = name.indexOf('.') >= 0 ?
                name.substring(0, name.indexOf('.')) : name;
            jTextFieldFileName.setText(name);
        }
}//GEN-LAST:event_jButtonBrowseDataFileActionPerformed

    private void jButtonBrowseXMLSourceFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseXMLSourceFileActionPerformed

        String whereToLook;
        if (jTextFieldXMLSourceFile.getText() != null && jTextFieldXMLSourceFile.getText().length() != 0) {
            whereToLook = jTextFieldXMLSourceFile.getText();
        } else {
            whereToLook = mPrefs.get(PREF_INPUT, xsdFilePath);
        }
        File selectedFile = this.getFileFromChooser(whereToLook, new String[][] {
            {"xml", _bundle.getString("test_panel.lbl.xml_files") + " (*.xml)"} //NOI18N
        });
        if (selectedFile == null) {
            return;
        }
        if (!selectedFile.exists()) {
            NotifyDescriptor desc = new NotifyDescriptor.Message(
                    _bundle.getString("test_panel.lbl.xml_file_does_not_exist"), //NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return;
        }
        jTextFieldXMLSourceFile.setText(selectedFile.getAbsolutePath());
        if (jTextFieldFileName.getText() == null || jTextFieldFileName.getText().length() == 0) {
            String name = selectedFile.getName();
            name = name.indexOf('.') >= 0 ?
                name.substring(0, name.indexOf('.')) : name;
            jTextFieldFileName.setText(name);
        }
}//GEN-LAST:event_jButtonBrowseXMLSourceFileActionPerformed

    private void jRadioButtonDecodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDecodeActionPerformed
        updateComponents();
}//GEN-LAST:event_jRadioButtonDecodeActionPerformed

    private void jRadioButtonEncodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonEncodeActionPerformed
        updateComponents();
}//GEN-LAST:event_jRadioButtonEncodeActionPerformed

    /** Open the file chooser and return the file.
     *@param oldUrl url where to start browsing
     */
    private File getFileFromChooser(String oldUrl, final String[][] extensions) {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();

        if (extensions != null) {
            FileFilter filter = new FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }

                    String extension = FileUtil.getExtension(f.getAbsolutePath());
                    //String extension = getExtension(f);
                    if (extension != null) {
                        int size = extensions.length;
                        for (int i = 0; i < size; i++) {
                            if (extensions[i][0].equals(extension)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }

                public String getDescription() {
                    if (extensions == null) {
                        return ""; //NOI18N
                    }
                    StringBuffer desc = new StringBuffer();
                    for (int i = 0; i < extensions.length; i++) {
                        if (i > 0) {
                            desc.append(", ");  //NOI18N
                        }
                        desc.append(extensions[i][1]);
                    }
                    return desc.toString();
                }
            };
            chooser.setFileFilter(filter);
        }

        if (oldUrl!=null) {
            try {
                File file = new File(oldUrl);
                File parentDir = file.getParentFile();
                if (parentDir!=null && parentDir.exists()) {
                    chooser.setCurrentDirectory(parentDir);
                }
            } catch (java.lang.IllegalArgumentException x) {
                //Ignore
            }
        }
        File selectedFile=null;
        if ( chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ) {
            selectedFile = chooser.getSelectedFile();
        }
        return selectedFile;
    }

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    private void updateCreatedFolder() {
        String expectedExtension = "";  //NOI18N
        if (getActionType().equals(EncoderTestPerformerImpl.DECODE)) {
            expectedExtension = ".xml";  //NOI18N
        } else if (getActionType().equals(EncoderTestPerformerImpl.ENCODE)) {
            expectedExtension = ".out";  //NOI18N
        }
        String folderName = jTextFieldFolder.getText().trim();
        String outputName = jTextFieldFileName.getText().trim();

        File f = new File(xsdFilePath);
        String createdFileName = folderName +
            ( folderName.endsWith("/") || folderName.endsWith( File.separator ) || folderName.length() == 0 ? "" : "/" ) + // NOI18N
            outputName + expectedExtension;

        jTextFieldCreatedFile.setText( createdFileName.replace( '/', File.separatorChar ) ); // NOI18N
    }

    public void insertUpdate(DocumentEvent e) {
        updateCreatedFolder();
    }

    public void removeUpdate(DocumentEvent e) {
        updateCreatedFolder();
    }

    public void changedUpdate(DocumentEvent e) {
        updateCreatedFolder();
    }

    public void setTopElementDecls(QName[] elements, QName select) {
        if (select == null && sameAsPreviousLaunch()) {
            String topElem = mPrefs.get(PREF_TOP_ELEM, null);
            if (topElem != null) {
                select = QName.valueOf(topElem);
            }
        }
        jComboBoxSelectElement.removeAllItems();
        DisplayQName selectDispQName = null;
        DisplayQName dispQName;
        for (int i = 0; i < elements.length; i++) {
            dispQName = new DisplayQName(elements[i]);
            if (elements[i].equals(select)) {
                selectDispQName = dispQName;
            }
            jComboBoxSelectElement.addItem(dispQName);
        }
        if (selectDispQName != null) {
            jComboBoxSelectElement.setSelectedItem(selectDispQName);
        }
    }

    public QName getSelectedTopElementDecl() {
        DisplayQName dispQName = (DisplayQName) jComboBoxSelectElement.getSelectedItem();
        if (dispQName == null) {
            return null;
        }
        return dispQName.getQName();
    }

    @Override
    public boolean contains(int x, int y) {
        return true;
    }

    /**
     * Used for displaying qualified name using local part.
     */
    private static class DisplayQName {

        private final QName mQName;

        public DisplayQName(QName qName) {
            mQName = qName;
        }

        public QName getQName() {
            return mQName;
        }

        @Override
        public String toString() {
            return mQName.getLocalPart();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonBrowseDataFile;
    private javax.swing.JButton jButtonBrowseFolder;
    private javax.swing.JButton jButtonBrowseXMLSourceFile;
    private javax.swing.JCheckBox jCheckBoxFromString;
    private javax.swing.JCheckBox jCheckBoxOverwriteOutput;
    private javax.swing.JCheckBox jCheckBoxToString;
    private javax.swing.JComboBox jComboBoxResultCoding;
    private javax.swing.JComboBox jComboBoxSelectElement;
    private javax.swing.JComboBox jComboBoxSourceCoding;
    private javax.swing.JComboBox jComboBoxVerboseLevel;
    private javax.swing.JLabel jLabelCreatedFile;
    private javax.swing.JLabel jLabelDataFile;
    private javax.swing.JLabel jLabelDebug;
    private javax.swing.JLabel jLabelFileName;
    private javax.swing.JLabel jLabelFolder;
    private javax.swing.JLabel jLabelInput;
    private javax.swing.JLabel jLabelMeta;
    private javax.swing.JLabel jLabelOutput;
    private javax.swing.JLabel jLabelResultCoding;
    private javax.swing.JLabel jLabelSelectAnElement;
    private javax.swing.JLabel jLabelSourceCoding;
    private javax.swing.JLabel jLabelVerboseLevel;
    private javax.swing.JLabel jLabelXMLSourceFile;
    private javax.swing.JLabel jLabelXSDFile;
    private javax.swing.JRadioButton jRadioButtonDecode;
    private javax.swing.JRadioButton jRadioButtonEncode;
    private javax.swing.JSeparator jSeparatorDebug;
    private javax.swing.JSeparator jSeparatorDecodeEncode;
    private javax.swing.JSeparator jSeparatorInputOutput;
    private javax.swing.JSeparator jSeparatorMeta;
    private javax.swing.JTextField jTextFieldCreatedFile;
    private javax.swing.JTextField jTextFieldDataFile;
    private javax.swing.JTextField jTextFieldFileName;
    private javax.swing.JTextField jTextFieldFolder;
    private javax.swing.JTextField jTextFieldXMLSourceFile;
    private javax.swing.JTextField jTextFieldXSDFile;
    // End of variables declaration//GEN-END:variables

}
