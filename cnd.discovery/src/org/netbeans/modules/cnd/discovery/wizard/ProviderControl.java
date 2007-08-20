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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.utils.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.api.utils.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.ElfStaticLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXExecutableFileFilter;
import org.netbeans.modules.cnd.api.utils.PeDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.PeExecutableFileFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ProviderControl {
    private ProviderProperty property;
    private String description;
    private JLabel label;
    private JTextField field;
    private JButton button;
    private int chooserMode = 0;
    private JPanel panel;
    private ChangeListener listener;
    
    public ProviderControl(ProviderProperty property, DiscoveryDescriptor wizardDescriptor,
            JPanel panel, ChangeListener listener){
        this.property = property;
        this.panel = panel;
        this.listener = listener;
        description = property.getDescription();
        label = new JLabel();
        Mnemonics.setLocalizedText(label, property.getName());
        switch(property.getKind()) {
            case BinaryFile:
                field = new JTextField();
                chooserMode = JFileChooser.FILES_ONLY;
                initBuildOrRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt);
                    }
                });
                addListeners();
                break;
            case Folder:
                field = new JTextField();
                chooserMode = JFileChooser.DIRECTORIES_ONLY;
                initRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt);
                    }
                });
                addListeners();
                break;
            case BinaryFiles:
                field = new JTextField();
                chooserMode = JFileChooser.FILES_ONLY;
                initArray();
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_EDIT_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        additionalLibrariesButtonActionPerformed(evt);
                    }
                });
                addListeners();
                break;
            default:
                // unsuported UI
                break;
        }
    }
    
    private void initBuildOrRoot(DiscoveryDescriptor wizardDescriptor){
        Object val = property.getValue();
        String output = null;
        if (val instanceof String){
            output = (String)val;
        }
        if (output != null && output.length() > 0){
            initFields(output,field);
            return;
        }
        output = wizardDescriptor.getBuildResult();
        if (output != null && output.length() > 0){
            initFields(output,field);
            return;
        }
        initFields(wizardDescriptor.getRootFolder(),field);
    }
    
    private void initRoot(DiscoveryDescriptor wizardDescriptor){
        Object val = property.getValue();
        String output = null;
        if (val instanceof String){
            output = (String)val;
        }
        if (output != null && output.length() > 0){
            initFields(output,field);
            return;
        }
        initFields(wizardDescriptor.getRootFolder(),field);
    }
    
    private void initArray(){
        Object val = property.getValue();
        if (val instanceof String[]){
            StringBuilder buf = new StringBuilder();
            for(String s : (String[])val){
                if (buf.length()>0){
                    buf.append(';');
                }
                buf.append(s);
            }
            field.setText(buf.toString());
        }
    }
    
    private void addListeners(){
        DocumentListener documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }
            
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }
            
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };
        field.getDocument().addDocumentListener(documentListener);
    }
    
    private void update(DocumentEvent e) {
        listener.stateChanged(null);
    }
    
    public void store(){
        switch(property.getKind()) {
            case Folder:
            case BinaryFile:
                property.setValue(field.getText());
                break;
            case BinaryFiles:
                String text = field.getText();
                StringTokenizer st = new StringTokenizer(text,";"); // NOI18N
                List<String> list = new ArrayList<String>();
                while(st.hasMoreTokens()){
                    list.add(st.nextToken());
                }
                property.setValue(list.toArray(new String[list.size()]));
                break;
            default:
                break;
        }
    }
    public boolean valid() {
        String path = field.getText();
        File file;
        switch(property.getKind()) {
            case Folder:
                if (path.length() == 0) {
                    return false;
                }
                file = new File(path);
                if (file.exists() && file.isDirectory()) {
                    return true;
                }
                break;
            case BinaryFile:
                if (path.length() == 0) {
                    return false;
                }
                file = new File(path);
                if (file.exists() && file.isFile()) {
                    return true;
                }
                break;
            case BinaryFiles:
                String text = field.getText();
                StringTokenizer st = new StringTokenizer(text,";"); // NOI18N
                while(st.hasMoreTokens()){
                    path = st.nextToken();
                    if (path.length() == 0) {
                        return false;
                    }
                    file = new File(path);
                    if (!(file.exists() && file.isFile())) {
                        return false;
                    }
                }
                return true;
            default:
                break;
        }
        return false;
    }
    
    private void layout(JPanel panel){
        GridBagConstraints gridBagConstraints = null;
        label.setLabelFor(field);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        panel.add(label, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 0, 0);
        panel.add(field, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        panel.add(button, gridBagConstraints);
    }
    
    private void additionalLibrariesButtonActionPerformed(ActionEvent evt) {
        StringTokenizer tokenizer = new StringTokenizer(field.getText(), ";"); // NOI18N
        List<String> list = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        AdditionalLibrariesListPanel libPanel = new AdditionalLibrariesListPanel(list.toArray());
        DialogDescriptor dialogDescriptor = new DialogDescriptor(AdditionalLibrariesListPanel.wrapPanel(libPanel),
                getString("ADDITIONAL_LIBRARIES_TXT"));
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue()  == DialogDescriptor.OK_OPTION) {
            Vector newList = libPanel.getListData();
            String includes = ""; // NOI18N
            for (int i = 0; i < newList.size(); i++) {
                if (i > 0)
                    includes += ";"; // NOI18N
                includes += newList.elementAt(i);
            }
            field.setText(includes);
        }
    }
    
    private void rootFolderButtonActionPerformed(ActionEvent evt) {
        String seed = null;
        if (field.getText().length() > 0) {
            seed = field.getText();
        } else            if (FileChooser.getCurrectChooserFile()  != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        }  else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        FileFilter[] filters = null;
        if (chooserMode == JFileChooser.FILES_ONLY){
            if (Utilities.isWindows()){
                filters = new FileFilter[] {PeExecutableFileFilter.getInstance(),
                ElfStaticLibraryFileFilter.getInstance(),
                PeDynamicLibraryFileFilter.getInstance()};
            } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                filters = new FileFilter[] {MacOSXExecutableFileFilter.getInstance(),
                ElfStaticLibraryFileFilter.getInstance(),
                MacOSXDynamicLibraryFileFilter.getInstance()};
            }  else {
                filters = new FileFilter[] {ElfExecutableFileFilter.getInstance(),
                ElfStaticLibraryFileFilter.getInstance(),
                ElfDynamicLibraryFileFilter.getInstance()};
            }
        }
        
        JFileChooser fileChooser = new FileChooser(
                getString("ROOT_DIR_CHOOSER_TITLE_TXT"), // NOI18N
                getString("ROOT_DIR_BUTTON_TXT"), // NOI18N
                chooserMode, false,
                filters,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(panel);
        if (ret == JFileChooser.CANCEL_OPTION)
            return;
        String path = fileChooser.getSelectedFile().getPath();
        //path = FilePathAdaptor.normalize(path);
        field.setText(path);
    }
    
    private void initFields(String path, JTextField field) {
        // Set default values
        if (path == null) {
            field.setText(""); // NOI18N
        } else {
            if (Utilities.isWindows()) {
                path = path.replace('/', File.separatorChar);
            }
            field.setText(path);
        }
    }
    
    private String getString(String key) {
        return NbBundle.getBundle(ProviderControl.class).getString(key);
    }
}