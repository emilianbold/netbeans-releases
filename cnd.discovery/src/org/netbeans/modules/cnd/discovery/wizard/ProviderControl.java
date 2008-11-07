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
import org.netbeans.modules.cnd.discovery.api.ProviderProperty.PropertyKind;
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
            case MakeLogFile:
                field = new JTextField();
                chooserMode = JFileChooser.FILES_ONLY;
                initBuildOrRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt, ProviderControl.this.property.getKind()==PropertyKind.BinaryFile,
                                                        getString("LOG_FILE_CHOOSER_TITLE_TXT"));
                    }
                });
                addListeners();
                break;
            case BinaryFile:
                field = new JTextField();
                chooserMode = JFileChooser.FILES_ONLY;
                initBuildOrRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt, ProviderControl.this.property.getKind()==PropertyKind.BinaryFile,
                                                        getString("BINARY_FILE_CHOOSER_TITLE_TXT"));
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
                        rootFolderButtonActionPerformed(evt, true, getString("ROOT_DIR_CHOOSER_TITLE_TXT"));
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
            case MakeLogFile:
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
                if (i > 0) {
                    includes += ";"; // NOI18N
                }
                includes += newList.elementAt(i);
            }
            field.setText(includes);
        }
    }
    
    private void rootFolderButtonActionPerformed(ActionEvent evt, boolean isBinary, String title) {
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
            if (isBinary) {
                if (Utilities.isWindows()) {
                    filters = new FileFilter[]{PeExecutableFileFilter.getInstance(),
                        ElfStaticLibraryFileFilter.getInstance(),
                        PeDynamicLibraryFileFilter.getInstance()
                    };
                } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                    filters = new FileFilter[]{MacOSXExecutableFileFilter.getInstance(),
                        ElfStaticLibraryFileFilter.getInstance(),
                        MacOSXDynamicLibraryFileFilter.getInstance()
                    };
                } else {
                    filters = new FileFilter[]{ElfExecutableFileFilter.getInstance(),
                        ElfStaticLibraryFileFilter.getInstance(),
                        ElfDynamicLibraryFileFilter.getInstance()
                    };
                }
            } else {
                filters = new FileFilter[]{new LogFileFilter()};
            }
        }
        
        JFileChooser fileChooser = new FileChooser(
                title,
                getString("ROOT_DIR_BUTTON_TXT"), // NOI18N
                chooserMode, false,
                filters,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(panel);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
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
    
    private class LogFileFilter extends javax.swing.filechooser.FileFilter {
        public LogFileFilter() {
        }
        public String getDescription() {
            return(getString("FILECHOOSER_MAK_LOG_FILEFILTER")); // NOI18N
        }
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