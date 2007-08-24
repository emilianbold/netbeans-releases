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
package org.netbeans.modules.mercurial.ui.properties;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Peter Pis
 */
public class HgProperties implements ActionListener, DocumentListener {
    
    private static final String HGPROPNAME_USERNAME = "username"; // NOI18N
    private static final String HGPROPNAME_DEFAULT_PULL = "default-pull"; // NOI18N
    private static final String HGPROPNAME_DEFAULT_PUSH = "default-push"; // NOI18N

    private PropertiesPanel panel;
    private File root;
    private PropertiesTable propTable;
    private HgProgressSupport support;
    private File loadedValueFile;
    private Font fontTextArea;
    
    /** Creates a new instance of HgProperties */
    public HgProperties(PropertiesPanel panel, PropertiesTable propTable, File root) {
        this.panel = panel;
        this.propTable = propTable;
        this.root = root;
        panel.txtAreaValue.getDocument().addDocumentListener(this);
        ((JTextField) panel.comboName.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        propTable.getTable().addMouseListener(new TableMouseListener());
        panel.btnAdd.addActionListener(this);
        panel.btnRemove.addActionListener(this);
        panel.comboName.setEditable(true);
        initPropertyNameCbx();
        refreshProperties();
        
    }
    
    public PropertiesPanel getPropertiesPanel() {
        return panel;
    }
    
    public void setPropertiesPanel(PropertiesPanel panel) {
        this.panel = panel;
    }
    
    public File getRoot() {
        return root;
    }
    
    public void setRoot(File root) {
        this.root = root;
    }
    
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        
        if (source.equals(panel.btnAdd)) {
            addProperty();
        }
        
        if (source.equals(panel.btnRemove)) {
            removeProperties();
        }
        
    }
    
    protected void initPropertyNameCbx() {
        List<String> lstName = new ArrayList<String>(8);

        lstName.add(HGPROPNAME_DEFAULT_PULL);
        lstName.add(HGPROPNAME_DEFAULT_PUSH);
        lstName.add(HGPROPNAME_USERNAME);

        ComboBoxModel comboModel = new DefaultComboBoxModel(new Vector(lstName));
        panel.comboName.setModel(comboModel);
        panel.comboName.setSelectedIndex(0);
    }
    
    protected String getPropertyValue() {
        return panel.txtAreaValue.getText();
    }
    
    protected String getPropertyName() {
        Object selectedItem = panel.comboName.getSelectedObjects()[0];
        if (selectedItem != null) {
            return panel.comboName.getEditor().getItem().toString().trim();
        } else {
            return selectedItem.toString().trim();
        }
    }
    
    protected void refreshProperties() {        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root.getAbsolutePath());
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    Properties props = HgModuleConfig.getDefault().getProperties(root);
                    HgPropertiesNode[] hgProps = new HgPropertiesNode[props.size()];
                    int i = 0;

                    for (Enumeration e = props.propertyNames(); e.hasMoreElements() ; ) {
                        String name = (String) e.nextElement();
                        String tmp = props.getProperty(name);
                        String value = tmp != null ? tmp : ""; // NOI18N
                        hgProps[i] = new HgPropertiesNode(name, value);
                        i++;
                     }
                     propTable.setNodes(hgProps);
                }
            };
            support.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(HgProperties.class, "LBL_Properties_Progress")); // NOI18N
        } finally {
            support = null;
        }
    }
    
    private boolean addProperty(String name, String value) {
        if (name.equals(HGPROPNAME_USERNAME)) {
            if (!HgModuleConfig.getDefault().isUserNameValid(value)) {
                try {
                    JOptionPane.showMessageDialog(null, 
                        NbBundle.getMessage(HgProperties.class, "MSG_PROPERTIES_INVALID_MESSAGE", value, name),  // NOI18N
                        NbBundle.getMessage(HgProperties.class, "MSG_PROPERTIES_INVALID_TITLE"), JOptionPane.ERROR_MESSAGE); // NOI18N
                } catch (Exception ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
                return false;
            }
        }
        
        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
        for (int i = 0; i < hgPropertiesNodes.length; i++) {
            String hgPropertyName = hgPropertiesNodes[propTable.getModelIndex(i)].getName(); 
            if (hgPropertyName.equals(name)) {
                hgPropertiesNodes[propTable.getModelIndex(i)].setValue(value); 
                propTable.setNodes(hgPropertiesNodes);
                return true;
            } 
        }
        HgPropertiesNode[] hgProps = new HgPropertiesNode[hgPropertiesNodes.length + 1];
        for (int i = 0; i < hgPropertiesNodes.length; i++) {
            hgProps[i] = hgPropertiesNodes[i];
        }
        hgProps[hgPropertiesNodes.length] = new HgPropertiesNode(name, value);
        propTable.setNodes(hgProps); 
        return true;
    }

    public void addProperty() {
        if (addProperty(getPropertyName(), getPropertyValue())) {
            panel.comboName.getEditor().setItem(""); // NOI18N
            panel.txtAreaValue.setText(""); // NOI18N
        }
    }
    
    public void setProperties() {
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root.getAbsolutePath());
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    HgModuleConfig.getDefault().clearProperties(root, "paths"); // NOI18N
                    HgModuleConfig.getDefault().removeProperty(root, "ui", HGPROPNAME_USERNAME); // NOI18N
                    HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
                    for (int i = 0; i < hgPropertiesNodes.length; i++) {
                        String hgPropertyName = hgPropertiesNodes[propTable.getModelIndex(i)].getName();
                        String hgPropertyValue = hgPropertiesNodes[propTable.getModelIndex(i)].getValue();
                        HgModuleConfig.getDefault().setProperty(root, hgPropertyName, hgPropertyValue);
                    }
                    HgRepositoryContextCache.resetPullDefault();
                    HgRepositoryContextCache.resetPushDefault();
                }
            };
            support.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(HgProperties.class, "LBL_Properties_Progress")); // NOI18N
        } finally {
            support = null;
        }
    }

    public void removeProperties() {
        final int[] rows = propTable.getSelectedItems();
        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
        HgPropertiesNode[] hgProps = new HgPropertiesNode[hgPropertiesNodes.length - rows.length];
        int j = 0;
        int k = 0;
        for (int i = 0; i < hgPropertiesNodes.length; i++) {
            if (i != rows[j]) {
                hgProps[k++] = hgPropertiesNodes[propTable.getModelIndex(i)];
            } else {
                if (j < rows.length - 1) j++;
            }
        }
        propTable.setNodes(hgProps);
    }
    
    public void insertUpdate(DocumentEvent event) {
        validateUserInput(event);
    }

    public void removeUpdate(DocumentEvent event) {
        validateUserInput(event);
    }

    public void changedUpdate(DocumentEvent event) {
        validateUserInput(event);
    }
    
    private void validateUserInput(DocumentEvent event) {
        
        Document doc = event.getDocument();
        String name = panel.comboName.getEditor().getItem().toString().trim();
        String value = panel.txtAreaValue.getText().trim();
        
        if (name.length() == 0 || value.length() == 0 || name.indexOf(" ") > 0) { // NOI18N
            panel.btnAdd.setEnabled(false);
        } else {
            panel.btnAdd.setEnabled(true);
        }
    }    
    
    public class TableMouseListener extends MouseAdapter {
        
        @Override
        public void mouseClicked(MouseEvent event) {
            //super.mouseClicked(arg0);
            if (event.getClickCount() == 2) {
                int[] rows = propTable.getSelectedItems();
                HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
                if (hgPropertiesNodes == null)
                    return;
                final String hgPropertyName = hgPropertiesNodes[propTable.getModelIndex(rows[0])].getName(); 
                final String hgPropertyValue = hgPropertiesNodes[propTable.getModelIndex(rows[0])].getValue(); 
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        panel.comboName.getEditor().setItem(hgPropertyName);
                        panel.txtAreaValue.setText(hgPropertyValue);
                    }
                });
            }
        }
    }
}
