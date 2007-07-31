/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.modules.xsl.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.*;
import javax.swing.*;
import java.util.Vector;

import javax.xml.transform.*;

import org.openide.loaders.DataObject;
import org.openide.loaders.RepositoryNodeFactory;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.api.xml.cookies.TransformableCookie;

import org.netbeans.modules.xsl.settings.TransformHistory;
import org.netbeans.modules.xsl.utils.TransformUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TransformPanel extends javax.swing.JPanel {
    /** Serial Version UID */
    private static final long serialVersionUID = -3449709794133206327L;
    
    public static final String DATA_XML_MODIFIED="DATA_XML_MODIFIED";
    public static final String DATA_XSL_MODIFIED="DATA_XSL_MODIFIED";
    public static final String DATA_OUTPUT_MODIFIED="DATA_OUTPUT_MODIFIED";
    public static final String DATA_PROCESS_MODIFIED="DATA_PROCESS_MODIFIED";
    public static final String DATA_OVERWRITE_MODIFIED="DATA_OVERWRITE_MODIFIED";
    
    private URL baseURL;
    private Data data;
    private boolean initialized = false;
    
    private DataObject    xmlDataObject;
    private String xml_stylesheet; // <?xsl-stylesheet ...
    private DataObject    xslDataObject;
    private TransformHistory xmlHistory;
    private TransformHistory xslHistory;
    
    private boolean userSetOutput  = false;
    private boolean userSetProcess = false;
    private String lastOutputFileExt = TransformUtil.DEFAULT_OUTPUT_EXT;
    private Object lastXSLObject     = new Object();
    
    /** Hide transformation components if true. */
    private boolean suppressXSL;
    
    /** Names of output actions. */
    private static final String[] SHOW_NAMES = new String[] {
        Util.THIS.getString("NAME_process_output_do_nothing"),           // TransformHistory.DO_NOTHING
        Util.THIS.getString("NAME_process_output_apply_default_action"), // TransformHistory.APPLY_DEFAULT_ACTION
        Util.THIS.getString("NAME_process_output_open_in_browser"),      // TransformHistory.OPEN_IN_BROWSER
    };
    
    private static final Object JUST_PREVIEW = new Preview();
    
    /** Creates new form TransformPanel */
    public TransformPanel (DataObject xml, String xml_ss, DataObject xsl, boolean suppressXSL) throws MalformedURLException, FileStateInvalidException {
        initComponents();
        
        init(xml, xml_ss, xsl, suppressXSL);
        initAccessibility();
    }
    
    /** Creates new form TransformPanel */
    public TransformPanel(DataObject xml, String xml_ss, DataObject xsl) throws MalformedURLException, FileStateInvalidException {
        this(xml, xml_ss, xsl, false);
    }
    
    private void init(DataObject xml, String xml_ss, DataObject xsl, boolean supXSL) throws MalformedURLException, FileStateInvalidException {
        data = new Data();
        xmlDataObject  = xml;
        xml_stylesheet = xml_ss;
        xslDataObject  = xsl;
        suppressXSL    = supXSL;
        
        if ( xmlDataObject != null ) {
            setInput(TransformUtil.getURLName(xmlDataObject.getPrimaryFile()));
            FileObject xmlFileObject = xmlDataObject.getPrimaryFile();
            xmlHistory = (TransformHistory)xmlFileObject.getAttribute(TransformHistory.TRANSFORM_HISTORY_ATTRIBUTE);
            if ( xmlHistory != null ) {
                setXSL(xmlHistory.getLastXSL());
            }
            if ( ( data.xsl == null ) &&
            ( xml_stylesheet != null ) ) {
                setXSL(xml_stylesheet);
            }
            try {
                baseURL = xmlFileObject.getParent().getURL();
            } catch (FileStateInvalidException exc) {
                // ignore it
            }
        }
        if ( xslDataObject != null ) {
            setXSL(TransformUtil.getURLName(xslDataObject.getPrimaryFile()));
            FileObject xslFileObject = xslDataObject.getPrimaryFile();
            xslHistory = (TransformHistory)xslFileObject.getAttribute(TransformHistory.TRANSFORM_HISTORY_ATTRIBUTE);
            if ( ( data.xml == null ) && ( xslHistory != null ) ) {
                setInput(xslHistory.getLastXML());
            }
            if ( baseURL == null ) {
                try {
                    baseURL = xslFileObject.getParent().getURL();
                } catch (FileStateInvalidException exc) {
                    // ignore it
                }
            }
        }
        
        if ( ( xmlHistory != null ) || ( xslHistory != null ) ) {
            if ( xmlHistory != null ) {
                setOutput(xmlHistory.getLastXSLOutput());
            }
            if ( ( data.output == null || (data.output instanceof String && "".equals(data.output)) ) &&
            ( xslHistory != null ) ) {
                setOutput(xslHistory.getLastXMLOutput());
            }
            if ( data.output == null ) {
                setOutput(JUST_PREVIEW);
            }
        }
        
        if ( xmlHistory != null ) {
            setOverwriteOutput( xmlHistory.isOverwriteOutput());
            setProcessOutput(new Integer(xmlHistory.getProcessOutput()));
        } else if ( xslHistory != null ) {
            setOverwriteOutput( xslHistory.isOverwriteOutput());
            setProcessOutput(new Integer(xslHistory.getProcessOutput()));
        }
        
        ownInitComponents();
    }
    
    
    private void ownInitComponents() {
        // XML Input
        updateXMLComboBoxModel(null);
        // XSL Transformation
        updateXSLComboBoxModel(null);
        
        updateComponents();
        
        setCaretPosition(inputComboBox);
        setCaretPosition(transformComboBox);
        setCaretPosition(outputComboBox);
    }
    
    
    private void setCaretPosition(JComboBox comboBox) {
        ComboBoxEditor cbEditor = comboBox.getEditor();
        final Component editorComponent = cbEditor.getEditorComponent();
        
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug("TransformPanel.setCaretPosition: " + comboBox);
            Util.THIS.debug("    editorComponent = " + editorComponent);
        }
        
        if ( editorComponent instanceof JTextField ) {
            SwingUtilities.invokeLater
            (new Runnable() {
                public void run() {
                    JTextField textField = (JTextField) editorComponent;
                    int length = textField.getText().length();
                    textField.setCaretPosition(length);
                    
                    if ( Util.THIS.isLoggable() ) /* then */ {
                        Util.THIS.debug("    text[" + length + "]='" + textField.getText() + "' - " + textField.getCaretPosition());
                    }
                }
            }
            );
        }
    }
    
    private void updateXMLComboBoxModel(Object prefItem) {
        Object[] history = null;
        if ( ( xmlDataObject == null ) &&
        ( xslHistory != null ) ) {
            history = xslHistory.getXMLs();
        }
        
        Vector cbModel = new Vector();
        
        // Preferred Item
        if ( prefItem != null ) {
            cbModel.add(prefItem);
        }
        // History
        if ( history != null ) {
            for ( int i = 0; i < history.length; i++ ) {
                cbModel.add(history[i]);
            }
        }
        
        inputComboBox.setModel(new DefaultComboBoxModel(cbModel));
    }
    
    private void updateXSLComboBoxModel(Object prefItem) {
        Object[] history = null;
        if ( ( xslDataObject == null ) &&
        ( xmlHistory != null ) ) {
            history = xmlHistory.getXSLs();
        }
        
        Vector cbModel = new Vector();
        
        // Preferred Item
        if ( prefItem != null ) {
            cbModel.add(prefItem);
        }
        // <?xsl-stylesheet ...
        if ( xml_stylesheet != null ) {
            cbModel.add(xml_stylesheet);
        }
        // History
        if ( history != null ) {
            for ( int i = 0; i < history.length; i++ ) {
                if ( ( history[i] != null ) &&
                ( history[i].equals(xml_stylesheet) == false ) ) { // do not duplicate xml_stylesheet item
                    cbModel.add(history[i]);
                }
            }
        }
        
        transformComboBox.setModel(new DefaultComboBoxModel(cbModel));
    }
    
    private boolean isInitialized() {
        synchronized ( data ) {
            return initialized;
        }
    }
    
    private void setInitialized(boolean init) {
        synchronized ( data ) {
            initialized = init;
        }
    }
    
    private static String guessFileName(String xml) {
        String fileName = null;
        
        int slashIndex = xml.lastIndexOf('/');
        if ( slashIndex != -1 ) {
            fileName = xml.substring(slashIndex + 1);
        } else {
            fileName = xml;
        }
        
        return fileName;
    }
    
    private String guessOutputFileExt() {
        String ext = lastOutputFileExt;
        String xslObject = getXSL();
        
        if ( xslObject != lastXSLObject ) {
            try {
                Source xslSource;
                xslSource = TransformUtil.createSource(baseURL, xslObject);
                ext = TransformUtil.guessOutputExt(xslSource);
                
                // cache last values
                lastXSLObject = xslObject;
                lastOutputFileExt = ext;
            } catch (Exception exc) {
                // ignore it
                
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("[TransformPanel] Cannot guess extension!", exc);
            }
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("[TransformPanel] I guess output has '" + ext + "' extension.");
        
        return ext;
    }
    
    private Object guessOutputFile() {
        Object output = data.output;
        
        if ( output == null || "".equals(output) || JUST_PREVIEW.equals(output)) {
            String origName = guessFileName(data.xml);
            String origExt = "";
            int dotIndex = origName.lastIndexOf('.');
            if ( dotIndex != -1 ) {
                origExt = origName.substring(dotIndex + 1);
                origName = origName.substring(0, dotIndex);
            }
            
            String ext = guessOutputFileExt();
            String plusName = "";
            if ( ext.equals(origExt) ) {
                plusName = Util.THIS.getString("NAME_plusNameIfSameName");
            }
            
            output = origName + plusName  + "." + ext; // NOI18N
        }
        
        return output;
    }
    
    private void initOutputComboBox(Object defaultOutput) {
        outputComboBox.setModel(new DefaultComboBoxModel(new Object[] { defaultOutput, JUST_PREVIEW }));
    }
    
    
    private void updateComponents() {
        setInitialized(false);
        
        // XML Input
        boolean notXML = ( xmlDataObject == null );
        if ( data.xml != null ) {
            inputComboBox.setSelectedItem(data.xml);
            inputComboBox.setEditable(data.xml instanceof String);
        }
        inputComboBox.setEnabled(notXML);
        browseInputButton.setEnabled(notXML);
        
        // XSL Transformation
        if ( suppressXSL ) {
            transformLabel.setVisible(false);
            transformComboBox.setVisible(false);
            browseXSLTButton.setVisible(false);
        } else {
            transformLabel.setVisible(true);
            transformComboBox.setVisible(true);
            browseXSLTButton.setVisible(true);
            
            boolean notXSL = ( xslDataObject == null );
            transformComboBox.setEnabled(notXSL);
            browseXSLTButton.setEnabled(notXSL);
            if ( data.xsl != null ) {
                transformComboBox.setSelectedItem(data.xsl);
                transformComboBox.setEditable(data.xsl instanceof String);
            }
        }
        
        // test if XML and also XSL
        boolean canOutput = true;
        if ( ( data.xml == null ) ||
        ( data.xsl == null ) ||
        ( data.xml.length() == 0 ) ||
        ( data.xsl.length() == 0 ) ) {
            canOutput = false;
        }
        
        // Output
        outputComboBox.setEnabled(canOutput);
        if ( canOutput ) {
            Object output = guessOutputFile();
            initOutputComboBox(output);
            
            outputComboBox.setSelectedItem(data.output != null ? output : JUST_PREVIEW);
            outputComboBox.setEditable(data.output != null);
        }
        
        // Overwrite Output
        if ( data.overwrite != null ) {
            overwriteCheckBox.setSelected(data.overwrite.booleanValue());
        }
        overwriteCheckBox.setEnabled(canOutput && data.output != null);
        
        // Process Output
        if ( data.process != null ) {
            showComboBox.setSelectedIndex(data.process.intValue());
        } else {
            String ext = guessOutputFileExt().toLowerCase();
            if ( ext.equals("html") || ext.equals("htm") ) { // NOI18N
                showComboBox.setSelectedIndex(TransformHistory.OPEN_IN_BROWSER);
            } else {
                showComboBox.setSelectedIndex(TransformHistory.APPLY_DEFAULT_ACTION);
            }
        }
        showComboBox.setEnabled(canOutput && data.output != null);
        
        setInitialized(true);
    }
    
    
    public Data getData() {
        return new Data (getInput(), getXSL(), getOutput(), isOverwriteOutput(), getProcessOutput());
    }
    
    public void setData(Data data) {
        this.data = data;
        updateComponents();
    }
    
    /**
     * @return selected XML input.
     */
    private String getInput() {
        return (String) inputComboBox.getSelectedItem();
    }
    
    /**
     * @return selected XSLT script.
     */
    private String getXSL() {
        return (String) transformComboBox.getSelectedItem();
    }
    
    /**
     * @return selected output.
     */
    private String getOutput() {
        Object output = outputComboBox.getSelectedItem();
        
        if ( JUST_PREVIEW.equals(output) ) {
            return null;
        }
        return (String) output;
    }
    
    /**
     * @return selected overwrite output option.
     */
    private boolean isOverwriteOutput() {
        return overwriteCheckBox.isSelected();
    }
    
    /**
     * @return selected process output option.
     */
    private int getProcessOutput() {
        return showComboBox.getSelectedIndex();
    }
    
    /**
     * Compute preffered dimension for combo with
     * particulal number of columns
     */
    private Dimension comboSize(int columns) {
        JTextField template = new JTextField();
        template.setColumns(columns);
        return template.getPreferredSize();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        inputLabel = new javax.swing.JLabel();
        inputLabel.setDisplayedMnemonic (Util.THIS.getChar ("LBL_XML_input_mnemonic"));
        inputComboBox = new javax.swing.JComboBox();
        browseInputButton = new javax.swing.JButton();
        transformLabel = new javax.swing.JLabel();
        transformLabel.setDisplayedMnemonic (Util.THIS.getChar ("LBL_XSL_transform_mnemonic"));
        transformComboBox = new javax.swing.JComboBox();
        browseXSLTButton = new javax.swing.JButton();
        outputLabel = new javax.swing.JLabel();
        outputLabel.setDisplayedMnemonic (Util.THIS.getChar ("LBL_trans_output_mnemonic"));
        outputComboBox = new javax.swing.JComboBox();
        overwriteCheckBox = new javax.swing.JCheckBox();
        overwriteCheckBox.setMnemonic (Util.THIS.getChar ("LBL_over_write_mnemonic"));
        showLabel = new javax.swing.JLabel();
        showLabel.setDisplayedMnemonic (Util.THIS.getChar ("LBL_show_output_mnemonic"));
        showComboBox = new javax.swing.JComboBox();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        inputLabel.setLabelFor(inputComboBox);
        inputLabel.setText(Util.THIS.getString ("LBL_XML_input"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(inputLabel, gridBagConstraints);

        inputComboBox.setEditable(true);
        inputComboBox.setPreferredSize(comboSize(40));
        inputComboBox.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints.weightx = 1.0;
        add(inputComboBox, gridBagConstraints);

        browseInputButton.setMnemonic(Util.THIS.getChar ("LBL_browse_file_mnemonic"));
        browseInputButton.setText(Util.THIS.getString ("LBL_browse_file"));
        browseInputButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 11);
        add(browseInputButton, gridBagConstraints);

        transformLabel.setLabelFor(transformComboBox);
        transformLabel.setText(Util.THIS.getString ("LBL_XSL_transform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(transformLabel, gridBagConstraints);

        transformComboBox.setEditable(true);
        transformComboBox.setPreferredSize(comboSize(40));
        transformComboBox.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        gridBagConstraints.weightx = 1.0;
        add(transformComboBox, gridBagConstraints);

        browseXSLTButton.setMnemonic(Util.THIS.getChar ("LBL_browse_xslt_mnemonic"));
        browseXSLTButton.setText(Util.THIS.getString ("LBL_browse_xslt"));
        browseXSLTButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(browseXSLTButton, gridBagConstraints);

        outputLabel.setLabelFor(outputComboBox);
        outputLabel.setText(Util.THIS.getString ("LBL_trans_output"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(outputLabel, gridBagConstraints);

        outputComboBox.setEditable(true);
        outputComboBox.setPreferredSize(comboSize(40));
        outputComboBox.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        gridBagConstraints.weightx = 1.0;
        add(outputComboBox, gridBagConstraints);

        overwriteCheckBox.setSelected(true);
        overwriteCheckBox.setText(Util.THIS.getString ("LBL_over_write"));
        overwriteCheckBox.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(overwriteCheckBox, gridBagConstraints);

        showLabel.setText(Util.THIS.getString ("LBL_show_output"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(showLabel, gridBagConstraints);

        showComboBox.setModel(new javax.swing.DefaultComboBoxModel (SHOW_NAMES));
        showComboBox.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 6, 0);
        gridBagConstraints.weightx = 1.0;
        add(showComboBox, gridBagConstraints);

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == inputComboBox) {
                TransformPanel.this.inputComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == browseInputButton) {
                TransformPanel.this.browseInputButtonActionPerformed(evt);
            }
            else if (evt.getSource() == transformComboBox) {
                TransformPanel.this.transformComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == browseXSLTButton) {
                TransformPanel.this.browseXSLTButtonActionPerformed(evt);
            }
            else if (evt.getSource() == outputComboBox) {
                TransformPanel.this.outputComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == overwriteCheckBox) {
                TransformPanel.this.overwriteCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == showComboBox) {
                TransformPanel.this.showComboBoxActionPerformed(evt);
            }
        }
    }//GEN-END:initComponents
    
    
    /** Initialize accesibility
     */
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_TransformPanel"));
        
        overwriteCheckBox.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_overwriteCheckBox"));
        outputComboBox.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_outputComboBox"));
        inputComboBox.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_inputComboBox"));
        browseXSLTButton.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_browseXSLTButton"));
        showComboBox.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_showComboBox"));
        browseInputButton.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_browseInputButton"));
        transformComboBox.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_transformComboBox"));
    }
    
    
    private void browseXSLTButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseXSLTButtonActionPerformed

        try {
            File selectedFile=getFileFromChooser(getXSL());
            if (selectedFile==null) return;
            FileObject fo = FileUtil.toFileObject(selectedFile);
            DataObject dObj = fo == null ? null : DataObject.find(fo);
            if (dObj==null || !TransformUtil.isXSLTransformation(dObj)) {
                NotifyDescriptor desc =  new NotifyDescriptor.Message(
                    Util.THIS.getString("MSG_notXslFile", //NOI18N
                    selectedFile.getName()),NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return;
            }
            setXSL(TransformUtil.getURLName(fo));

            if ( ( userSetOutput == false ) && ( xmlHistory != null ) ) {
                setOutput(xmlHistory.getXSLOutput(data.xsl));
            }
            if ( userSetProcess == false ) {
                setProcessOutput(null);
            }
            updateXSLComboBoxModel(data.xsl);

            updateComponents();

            setCaretPosition(transformComboBox);
            
        } catch (IOException exc) { // TransformUtil.getURLName (...)
            // ignore it
            Util.THIS.debug(exc);
        } finally {
            Util.icons = null;
        }
    }//GEN-LAST:event_browseXSLTButtonActionPerformed
    
    private void browseInputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseInputButtonActionPerformed

        try {
            File selectedFile=getFileFromChooser(getInput());
            if (selectedFile==null) return;
            FileObject fo = FileUtil.toFileObject(selectedFile);
            DataObject dObj = fo == null ? null : DataObject.find(fo);
            if (dObj==null || dObj.getCookie(TransformableCookie.class)==null) {
                NotifyDescriptor desc =  new NotifyDescriptor.Message(
                    Util.THIS.getString("MSG_notXmlFile", //NOI18N
                    selectedFile.getName()),NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return;
            }
            setInput(TransformUtil.getURLName(fo));
            
            if ( ( userSetOutput == false ) && ( xslHistory != null ) ) {
                setOutput(xslHistory.getXMLOutput(data.xml));
            }
            if ( userSetProcess == false ) {
                setProcessOutput(null);
            }
            updateXMLComboBoxModel(data.xml);
            
            updateComponents();
            
            setCaretPosition(inputComboBox);
            
        } catch (IOException exc) { // TransformUtil.getURLName (...)
            // ignore it
            Util.THIS.debug(exc);
        } finally {
            Util.icons = null;
        }
    }//GEN-LAST:event_browseInputButtonActionPerformed
    
    public void setInput(String input) {
        if(data==null) {
            return;
        }
        String oldInput=data.getInput();
        data.setInput(input);
        firePropertyChange(DATA_XML_MODIFIED,oldInput,input);
    }
    
    public void setXSL(String xslValue) {
        if(data==null) {
            return;
        }
        String oldXSL=data.getXSL();
        data.setXSL(xslValue);
        firePropertyChange(DATA_XSL_MODIFIED,oldXSL,xslValue);
    }
    
    public void setOutput(Object outputValue) {
        if(data==null) {
            return;
        }
        Object oldOutput=data.getOutput();
        data.setOutput(outputValue);
        firePropertyChange(DATA_OUTPUT_MODIFIED,oldOutput,outputValue);
    }
    
    public void setOverwriteOutput(Boolean overwriteObject) {
        if(data==null || overwriteObject==null) {
            return;
        }
        setOverwriteOutput(overwriteObject.booleanValue());
    }
    
    public void setOverwriteOutput(boolean overwriteValue) {
        if(data==null) {
            return;
        }
        boolean oldOverwrite=data.isOverwriteOutput();
        data.setOverwriteOutput(overwriteValue);
        firePropertyChange(DATA_OVERWRITE_MODIFIED,oldOverwrite,overwriteValue);
    }
    
    public void setProcessOutput(Integer processObject) {
        if(data==null || processObject==null) {
            return;
        }
        setProcessOutput(processObject.intValue());
    }
    
    public void setProcessOutput(int processValue) {
        if(data==null) {
            return;
        }
        int oldProcess=data.getProcessOutput();
        data.setProcessOutput(processValue);
        firePropertyChange(DATA_PROCESS_MODIFIED,oldProcess,processValue);
    }
    
    private void showComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showComboBoxActionPerformed
        // Add your handling code here:
        if ( isInitialized() ) {
            setProcessOutput(new Integer(showComboBox.getSelectedIndex()));
            userSetProcess = true;
            updateComponents();
        }
    }//GEN-LAST:event_showComboBoxActionPerformed
    
    private void overwriteCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overwriteCheckBoxActionPerformed
        // Add your handling code here:
        if ( isInitialized() ) {
            setOverwriteOutput( overwriteCheckBox.isSelected() );
            updateComponents();
        }
    }//GEN-LAST:event_overwriteCheckBoxActionPerformed
    
    private void transformComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transformComboBoxActionPerformed
        // Add your handling code here:
        if ( isInitialized() ) {
            String item = (String) transformComboBox.getSelectedItem();
            
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug("TransformPanel.transformComboBoxActionPerformed: getSelectedItem = " + item);
            }
            
            if ( item == null ) {
                return;
            }
            
            setXSL(item.trim());
            
            if ( ( userSetOutput == false ) && ( xmlHistory != null ) ) {
                setOutput(xmlHistory.getXSLOutput(data.xsl));
            }
            if ( userSetProcess == false ) {
                setProcessOutput(null);
            }
            
            updateComponents();
            
            //             setCaretPosition (transformComboBox);
        }
    }//GEN-LAST:event_transformComboBoxActionPerformed
    
    private void inputComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputComboBoxActionPerformed
        // Add your handling code here:
        if ( isInitialized() ) {
            String item = (String) inputComboBox.getSelectedItem();
            
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug("TransformPanel.inputComboBoxActionPerformed: getSelectedItem = " + item);
            }
            
            if ( item == null ) {
                return;
            }
            
            setInput(item.trim());
            
            if ( ( userSetOutput == false ) && ( xslHistory != null ) ) {
                setOutput(xslHistory.getXMLOutput(data.xml));
            }
            if ( userSetProcess == false ) {
                setProcessOutput(null);
            }
            
            updateComponents();
            
            //             setCaretPosition (inputComboBox);
        }
    }//GEN-LAST:event_inputComboBoxActionPerformed
    
    private void outputComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputComboBoxActionPerformed
        // Add your handling code here:
        if ( isInitialized() ) {
            Object item = outputComboBox.getSelectedItem();
            if ( item instanceof String ) {
                String str = ((String) item).trim();
                if ( str.length() == 0 ) {
                    str = null;
                }
                item = str;
            }
            setOutput(item);
            
            
            userSetOutput = true;
            updateComponents();
            
            
            //             setCaretPosition (outputComboBox);
        }
    }//GEN-LAST:event_outputComboBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseInputButton;
    private javax.swing.JButton browseXSLTButton;
    private javax.swing.JComboBox inputComboBox;
    private javax.swing.JLabel inputLabel;
    private javax.swing.JComboBox outputComboBox;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JCheckBox overwriteCheckBox;
    private javax.swing.JComboBox showComboBox;
    private javax.swing.JLabel showLabel;
    private javax.swing.JComboBox transformComboBox;
    private javax.swing.JLabel transformLabel;
    // End of variables declaration//GEN-END:variables
    
    
    //
    // class Data
    //
    
    public static final class Data {
        private String  xml;
        private String  xsl;
        private Object  output;
        private Boolean overwrite;
        private Integer process;
        
        public Data() {
            this.xml       = null;
            this.xsl       = null;
            this.output    = "";
            this.overwrite = null;
            this.process   = null;
        }
        
        public Data(String xml, String xsl, Object output, boolean overwrite, int process) {
            this.xml       = xml;
            this.xsl       = xsl;
            this.output    = output;
            this.overwrite = overwrite ? Boolean.TRUE : Boolean.FALSE;
            this.process   = process == -1 ? null : new Integer(process);
        }
        
        /**
         * @return selected XML input.
         */
        public String getInput() {
            return xml;
        }
        
        /**
         * @return selected XSLT script.
         */
        public String getXSL() {
            return xsl;
        }
        
        /**
         * @return selected output.
         */
        public Object getOutput() {
            return output;
        }
        
        /**
         * @return selected overwrite output option.
         */
        public boolean isOverwriteOutput() {
            if(overwrite==null) {
                return false;
            }
            return overwrite.booleanValue();
        }
        
        /**
         * @return selected process output option.
         */
        public int getProcessOutput() {
            if(process==null) {
                return 0;
            }
            return process.intValue();
        }
        
        public void setInput(String input) {
            xml=input;
        }
        
        public void setXSL(String xslValue) {
            xsl=xslValue;
        }
        
        public void setOutput(Object outputValue) {
            if (JUST_PREVIEW.equals(outputValue)) {
                output = null;
            } else {
                output=outputValue;
            }
        }
        
        public void setOverwriteOutput(boolean overwriteValue) {
            overwrite = overwriteValue ? Boolean.TRUE : Boolean.FALSE;
        }
        
        public void setProcessOutput(Integer processObject) {
            process=processObject;
        }
        
        public void setProcessOutput(int processValue) {
            setProcessOutput(new Integer(processValue));
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer(super.toString());
            
            sb.append("[input='").append(xml).append("'; ");
            sb.append("xsl='").append(xsl).append("'; ");
            sb.append("output='").append(output).append("'; ");
            sb.append("overwrite='").append(overwrite).append("'; ");
            sb.append("process='").append(process).append("]");
            
            return sb.toString();
        }
        
    } // class Data
    
    
    //
    // class Preview
    //
    
    private static class Preview {
        public String toString() {
            return Util.THIS.getString("NAME_output_just_preview");
        }
    } // class Preview
    
    
    /** Open the file chooser and return the file.
     *@param oldUrl url where to start browsing
     */
    private File getFileFromChooser(String oldUrl) {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        if (oldUrl!=null) {
            try {
                File file=null;
                java.net.URI url = new java.net.URI(oldUrl);
                if (url!=null) {
                    file = new File(url);
                }
                if (file!=null) {
                    File parentDir = file.getParentFile();
                    if (parentDir!=null && parentDir.exists() ) 
                        chooser.setCurrentDirectory(parentDir);
                }
            } catch (java.net.URISyntaxException ex) {}
            catch (java.lang.IllegalArgumentException x) {}
        }
        File selectedFile=null;
        if ( chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ) {
            selectedFile = chooser.getSelectedFile();
        }
        return selectedFile;
    }

}
