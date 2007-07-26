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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Dimension;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.utils.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.api.utils.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.ElfStaticLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXExecutableFileFilter;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.netbeans.modules.cnd.api.utils.PeDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.PeExecutableFileFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 *
 * @author Alexander Simon
 */
public class AdditionalLibrariesListPanel extends ListEditorPanel {
    
    public static JPanel wrapPanel(ListEditorPanel innerPanel) {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(innerPanel, gridBagConstraints);
        outerPanel.setPreferredSize(new Dimension(500, 250));
        return outerPanel;
    }
    
    public AdditionalLibrariesListPanel(Object[] objects) {
        super(objects);
        getDefaultButton().setVisible(false);
        getUpButton().setVisible(false);
        getDownButton().setVisible(false);
        getCopyButton().setVisible(false);
    }
    
    public Object addAction() {
        String seed = null;
        if (FileChooser.getCurrectChooserFile()  != null)
            seed = FileChooser.getCurrectChooserFile().getPath();
        if (seed == null)
            seed = System.getProperty("user.home"); // NOI18N
        FileFilter[] filters;
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
        FileChooser fileChooser = new FileChooser(
                getString("LIBRARY_CHOOSER_TITLE_TXT"),
                getString("LIBRARY_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                false,
                filters,
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION)
            return null;
        String itemPath = fileChooser.getSelectedFile().getPath();
        itemPath = FilePathAdaptor.normalize(itemPath);
        return itemPath;
    }
    
    public String getListLabelText() {
        return getString("LIBRARY_LIST_TXT");
    }
    public char getListLabelMnemonic() {
        return getString("LIBRARY_LIST_MN").charAt(0);
    }
    
    public String getAddButtonText() {
        return getString("ADD_BUTTON_TXT");
    }
    public char getAddButtonMnemonics() {
        return getString("ADD_BUTTON_MN").charAt(0);
    }
    
    public String getRenameButtonText() {
        return getString("EDIT_BUTTON_TXT");
    }
    public char getRenameButtonMnemonics() {
        return getString("EDIT_BUTTON_MN").charAt(0);
    }
    
    public Object copyAction(Object o) {
        return new String((String) o);
    }
    
    @SuppressWarnings("unchecked") // NOI18N
    public void editAction(Object o) {
        String s = (String)o;
        
        InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
        notifyDescriptor.setInputText(s);
        DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (notifyDescriptor.getValue()  != NotifyDescriptor.OK_OPTION)
            return;
        String newS = notifyDescriptor.getInputText();
        Vector vector = getListData();
        Object[] arr = getListData().toArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == o) {
                vector.remove(i);
                vector.add(i, newS);
                break;
            }
        }
    }
    
    private String getString(String key) {
        return NbBundle.getBundle(AdditionalLibrariesListPanel.class).getString(key);
    }
}