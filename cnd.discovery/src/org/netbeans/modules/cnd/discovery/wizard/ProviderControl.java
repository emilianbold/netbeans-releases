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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty.PropertyKind;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ProviderControl {
    private ProviderProperty property;
    private String propertyKey;
    private String description;
    private JLabel label;
    private JComboBox field;
    private JButton button;
    private int chooserMode = 0;
    private JPanel panel;
    private ChangeListener listener;
    
    public ProviderControl(String key, ProviderProperty property, DiscoveryDescriptor wizardDescriptor,
            JPanel panel, ChangeListener listener){
        this.propertyKey = key;
        this.property = property;
        this.panel = panel;
        this.listener = listener;
        description = property.getDescription();
        label = new JLabel();
        Mnemonics.setLocalizedText(label, property.getName());
        switch(property.getKind()) {
            case MakeLogFile:
                field = new JComboBox();
                field.setEditable(true);
                chooserMode = JFileChooser.FILES_ONLY;
                initBuildOrRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt, ProviderControl.this.property.getKind()==PropertyKind.BinaryFile,
                                                        getString("LOG_FILE_CHOOSER_TITLE_TXT"));
                    }
                });
                addListeners();
                break;
            case BinaryFile:
                field = new JComboBox();
                field.setEditable(true);
                chooserMode = JFileChooser.FILES_ONLY;
                initBuildOrRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt, ProviderControl.this.property.getKind()==PropertyKind.BinaryFile,
                                                        getString("BINARY_FILE_CHOOSER_TITLE_TXT"));
                    }
                });
                addListeners();
                break;
            case Folder:
                field = new JComboBox();
                field.setEditable(true);
                chooserMode = JFileChooser.DIRECTORIES_ONLY;
                initRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt, true, getString("ROOT_DIR_CHOOSER_TITLE_TXT"));
                    }
                });
                addListeners();
                break;
            case BinaryFiles:
                field = new JComboBox();
                field.setEditable(true);
                chooserMode = JFileChooser.FILES_ONLY;
                initArray();
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_EDIT_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    @Override
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
            initFields(output);
            return;
        }
        output = wizardDescriptor.getBuildResult();
        if (output != null && output.length() > 0){
            initFields(output);
            return;
        }
        initFields(wizardDescriptor.getRootFolder());
    }
    
    private void initRoot(DiscoveryDescriptor wizardDescriptor){
        Object val = property.getValue();
        String output = null;
        if (val instanceof String){
            output = (String)val;
        }
        if (output != null && output.length() > 0){
            initFields(output);
            return;
        }
        initFields(wizardDescriptor.getRootFolder());
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
            initFields(buf.toString());
        } else {
            initFields(""); // NOI18N
        }
    }

    private void initComboBox(String root){
        List<String> vector = new ArrayList<String>();
        vector.add(root);
        Preferences prefs = NbPreferences.forModule(ProviderControl.class);
        String old = prefs.get(propertyKey, ""); // NOI18N
        StringTokenizer st = new StringTokenizer(old, "\u0000"); // NOI18N
        int history = 5;
        while(st.hasMoreTokens()) {
            String s = st.nextToken();
            if (!vector.contains(s)) {
                vector.add(s);
                history--;
                if (history == 0) {
                    break;
                }
            }
        }
        DefaultComboBoxModel rootModel = new DefaultComboBoxModel(vector.toArray());
        field.setModel(rootModel);
    }
    
    private void addListeners(){
        field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        ComboBoxEditor editor = field.getEditor();
        Component component = editor.getEditorComponent();
        if (component instanceof JTextField) {
            ((JTextField)component).getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    update();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    update();
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });
        }
    }
    
    private void update() {
        listener.stateChanged(null);
    }
    
    public void store(){
        switch(property.getKind()) {
            case MakeLogFile:
            case Folder:
            case BinaryFile:
                property.setValue(getComboBoxText());
                storeHistory();
                break;
            case BinaryFiles:
                String text = getComboBoxText();
                StringTokenizer st = new StringTokenizer(text,";"); // NOI18N
                List<String> list = new ArrayList<String>();
                while(st.hasMoreTokens()){
                    list.add(st.nextToken());
                }
                property.setValue(list.toArray(new String[list.size()]));
                storeHistory();
                break;
            default:
                break;
        }

    }
    
    private void storeHistory() {
        List<String> vector = new ArrayList<String>();
        vector.add(getComboBoxText());
        for(int i = 0; i < field.getModel().getSize(); i++){
            String s = field.getModel().getElementAt(i).toString();
            if (!vector.contains(s)) {
                vector.add(s);
            }
        }
        StringBuilder buf = new StringBuilder();
        for(String s : vector) {
            if (buf.length()>0) {
                buf.append((char)0);
            }
            buf.append(s);
        }
        Preferences prefs = NbPreferences.forModule(ProviderControl.class);
        prefs.put(propertyKey, buf.toString()); // NOI18N
    }

    public boolean valid() {
        String path = getComboBoxText();
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
            case MakeLogFile:
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
                String text = getComboBoxText();
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
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < 35; i++) {
            buf.append("w"); // NOI18N
        }
        field.setPrototypeDisplayValue(buf.toString());
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = GridBagConstraints.SOUTH;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        panel.add(button, gridBagConstraints);
    }
    
    private void additionalLibrariesButtonActionPerformed(ActionEvent evt) {
        StringTokenizer tokenizer = new StringTokenizer(getComboBoxText(), ";"); // NOI18N
        List<String> list = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        AdditionalLibrariesListPanel libPanel = new AdditionalLibrariesListPanel(list);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(AdditionalLibrariesListPanel.wrapPanel(libPanel),
                getString("ADDITIONAL_LIBRARIES_TXT"));
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue()  == DialogDescriptor.OK_OPTION) {
            List<String> newList = libPanel.getListData();
            StringBuilder includes = new StringBuilder();
            for (int i = 0; i < newList.size(); i++) {
                if (i > 0) {
                    includes.append(';'); // NOI18N
                }
                includes.append(newList.get(i));
            }
            field.setSelectedItem(includes.toString());
        }
    }
    
    private void rootFolderButtonActionPerformed(ActionEvent evt, boolean isBinary, String title) {
        FileFilter[] filters = null;
        if (chooserMode == JFileChooser.FILES_ONLY){
            if (isBinary) {
                if (Utilities.isWindows()) {
                    filters = new FileFilter[]{FileFilterFactory.getPeExecutableFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getPeDynamicLibraryFileFilter()
                    };
                } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                    filters = new FileFilter[]{FileFilterFactory.getMacOSXExecutableFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getMacOSXDynamicLibraryFileFilter()
                    };
                } else {
                    filters = new FileFilter[]{FileFilterFactory.getElfExecutableFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getElfDynamicLibraryFileFilter()
                    };
                }
            } else {
                filters = new FileFilter[]{new LogFileFilter()};
            }
        }
        
        JFileChooser fileChooser = new FileChooser(
                title,
                getString("ROOT_DIR_BUTTON_TXT"), // NOI18N
                chooserMode,
                filters,
                getComboBoxText(),
                false
                );
        int ret = fileChooser.showOpenDialog(panel);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        field.setSelectedItem(path);
    }

    private String getComboBoxText() {
        ComboBoxEditor editor = field.getEditor();
        if (editor != null) {
            Component component = editor.getEditorComponent();
            if (component instanceof JTextField) {
                return ((JTextField)component).getText();
            }
        }
        if (field.getSelectedItem() != null) {
            return field.getSelectedItem().toString();
        }
        return null;
    }
    
    private void initFields(String path) {
        // Set default values
        if (path == null) {
            initComboBox(""); // NOI18N
        } else {
            if (Utilities.isWindows()) {
                path = path.replace('/', File.separatorChar);
            }
            initComboBox(path);
        }
    }
    
    private String getString(String key) {
        return NbBundle.getBundle(ProviderControl.class).getString(key);
    }
    
    private class LogFileFilter extends javax.swing.filechooser.FileFilter {
        public LogFileFilter() {
        }
        @Override
        public String getDescription() {
            return(getString("FILECHOOSER_MAK_LOG_FILEFILTER")); // NOI18N
        }
        @Override
        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().endsWith(".log"); // NOI18N
            }
            return false;
        }
    }
}
