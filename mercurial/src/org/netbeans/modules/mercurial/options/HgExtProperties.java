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
 * "Portions Copyrighted [year] [name of copyright owner]"  // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial.options;

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
import org.netbeans.modules.mercurial.ui.properties.PropertiesPanel;
import org.netbeans.modules.mercurial.ui.properties.PropertiesTable;
import org.netbeans.modules.mercurial.ui.properties.HgPropertiesNode;
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
 * @author Padraig O'Briain
 */
public class HgExtProperties implements ActionListener, DocumentListener {
    
    private PropertiesPanel panel;
    private File root;
    private PropertiesTable propTable;
    private HgProgressSupport support;
    private File loadedValueFile;
    private Font fontTextArea;
    
    /** Creates a new instance of HgExtProperties */
    public HgExtProperties(PropertiesPanel panel, PropertiesTable propTable, File root) {
        this.panel = panel;
        this.propTable = propTable;
        this.root = root;
        panel.getTxtAreaValue().getDocument().addDocumentListener(this);
        ((JTextField) panel.getComboName().getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        propTable.getTable().addMouseListener(new TableMouseListener());
        panel.getBtnAdd().addActionListener(this);
        panel.getBtnRemove().addActionListener(this);
        panel.getComboName().setEditable(true);
        panel.getBtnAdd().setEnabled(false);
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
        
        if (source.equals(panel.getBtnAdd())) {
            addProperty();
        }
        
        if (source.equals(panel.getBtnRemove())) {
            removeProperties();
        }
        
    }
    
    protected void initPropertyNameCbx() {
        List<String> lstName = new ArrayList<String>(8);

        ComboBoxModel comboModel = new DefaultComboBoxModel(new Vector(lstName));
        panel.getComboName().setModel(comboModel);
        panel.getComboName().getEditor().setItem(""); // NOI18N
    }
    
    protected String getPropertyValue() {
        return panel.getTxtAreaValue().getText();
    }
    
    protected String getPropertyName() {
        Object selectedItem = panel.getComboName().getSelectedObjects()[0];
        if (selectedItem != null) {
            return panel.getComboName().getEditor().getItem().toString().trim();
        } else {
            return selectedItem.toString().trim();
        }
    }
    
    protected void refreshProperties() {        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    Properties props = HgModuleConfig.getDefault().getProperties(root, "extensions"); // NOI18N
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
            support.start(rp, null, org.openide.util.NbBundle.getMessage(HgExtProperties.class, "LBL_Properties_Progress")); // NOI18N
        } finally {
            support = null;
        }
    }
    
    private boolean addProperty(String name, String value) {
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
            panel.getComboName().getEditor().setItem(""); // NOI18N
            panel.getTxtAreaValue().setText(""); // NOI18N
        }
    }
    
    public void setProperties() {
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    HgModuleConfig.getDefault().clearProperties(root, "extensions"); // NOI18N
                    HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
                    for (int i = 0; i < hgPropertiesNodes.length; i++) {
                        String hgPropertyName = hgPropertiesNodes[propTable.getModelIndex(i)].getName();
                        String hgPropertyValue = hgPropertiesNodes[propTable.getModelIndex(i)].getValue();
                        HgModuleConfig.getDefault().setProperty(root, "extensions", hgPropertyName, hgPropertyValue); // NOI18N
                    }
                }
            };
            support.start(rp, null, org.openide.util.NbBundle.getMessage(HgExtProperties.class, "LBL_Properties_Progress")); // NOI18N
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
                hgProps[k++] = hgPropertiesNodes[i];
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
        String name = panel.getComboName().getEditor().getItem().toString().trim();
        String value = panel.getTxtAreaValue().getText().trim();
        
        if (name.length() == 0 || name.indexOf(" ") > 0) { // NOI18N
            panel.getBtnAdd().setEnabled(false);
        } else {
            panel.getBtnAdd().setEnabled(true);
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
                        panel.getComboName().getEditor().setItem(hgPropertyName);
                        panel.getTxtAreaValue().setText(hgPropertyValue);
                    }
                });
            }
        }
}
}
