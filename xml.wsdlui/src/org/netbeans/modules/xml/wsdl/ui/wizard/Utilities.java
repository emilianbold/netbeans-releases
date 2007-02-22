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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.awt.Component;
import java.io.*;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.WizardDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
/**
 *
 * @author  mkuchtiak
 */
public class Utilities {
    
    public static String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
    
    public static void replaceInDocument(javax.swing.text.Document document, String replaceFrom, String replaceTo) {
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument)document;
        int len = replaceFrom.length();
        try {
            String content = doc.getText(0,doc.getLength());
            int index = content.lastIndexOf(replaceFrom);
            while (index>=0) {
                doc.replace(index,len,replaceTo,null);
                content=content.substring(0,index);
                index = content.lastIndexOf(replaceFrom);
            }
        } catch (javax.swing.text.BadLocationException ex){}
    }
    
    // last selected directory
    private static File lastDirectory;
    
    /**
     * Allows the user to select multiple files
     * @param extensions takes a list of file extensions
     * @param dialogTitle dialog title
     * @param maskTitle title for filter mask
     * @return array of selected files
     */
    public static File[] selectFiles(final String extensions, String dialogTitle, final String maskTitle,
            Project project){
        ArrayList<File> fileList = new ArrayList<File>();
        SelectSchemaPanel explorerPanel = new SelectSchemaPanel(project);
        DialogDescriptor descriptor = new DialogDescriptor(explorerPanel,
                NbBundle.getMessage(Utilities.class,"TTL_SelectSchemas")); //NOI18N
        /*
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
         
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                java.util.StringTokenizer token = new java.util.StringTokenizer(extensions, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return true;
                }
                return false;
            }
            public String getDescription() {
                return maskTitle; // NOI18N
            }
        });
         
        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }
         
        chooser.setDialogTitle(dialogTitle);
        if (chooser.showDialog(org.openide.windows.WindowManager.getDefault().getMainWindow(),
                NbBundle.getMessage(Utilities.class,"PROP_select_button"))
                == JFileChooser.APPROVE_OPTION) {
         
            File[] files = chooser.getSelectedFiles();
            lastDirectory = chooser.getCurrentDirectory();
            for(int i = 0; i < files.length; i++){
                File f = files[i];
                if (f != null && f.isFile()) {
                    java.util.StringTokenizer token = new java.util.StringTokenizer(extensions, " ");  // NOI18N
                    boolean validFile = false;
                    while (token.hasMoreElements()) {
                        if (f.getName().endsWith(token.nextToken())){
                            fileList.add(f);
                            validFile = true;
                        }
                    }
                    if(!validFile){
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(Utilities.class,"MSG_inValidFile", f.getName()), NotifyDescriptor.WARNING_MESSAGE));
                    }
                }
            }
        }
         */
        if(DialogDisplayer.getDefault().notify(descriptor).equals(NotifyDescriptor.OK_OPTION)) {
            boolean accepted = true;
            String errMsg = null;
            Node[] selectedNodes = explorerPanel.getSelectedNodes();
            for(int i = 0; i < selectedNodes.length; i++){
                Node node = selectedNodes[i];
                DataObject dobj = (DataObject)node.getCookie(DataObject.class);
                if(dobj != null){
                    FileObject fo = dobj.getPrimaryFile();
                    if(fo != null){
                        File file = FileUtil.toFile(fo);
                        if (file != null && file.isFile()) {
                            java.util.StringTokenizer token = new java.util.StringTokenizer(extensions, " ");  // NOI18N
                            boolean validFile = false;
                            while (token.hasMoreElements()) {
                                if (file.getName().endsWith(token.nextToken())){
                                    fileList.add(file);
                                    validFile = true;
                                }
                            }
                            if(!validFile){
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                        NbBundle.getMessage(Utilities.class,"MSG_inValidFile", file.getName()), NotifyDescriptor.WARNING_MESSAGE));
                            }
                        }
                    }
                }
            }
            if (!accepted) {
                NotifyDescriptor.Message notifyDescr =
                        new NotifyDescriptor.Message(errMsg,
                        NotifyDescriptor.ERROR_MESSAGE );
                DialogDisplayer.getDefault().notify(notifyDescr);
                descriptor.setClosingOptions(closingOptionsWithoutOK);
            } else {
                // Everything was fine so allow OK
                descriptor.setClosingOptions(closingOptionsWithOK);
            }
        }
        File[] selectedFiles = new File[fileList.size()];
        return fileList.toArray(selectedFiles);
    }
    
    private static Object[] closingOptionsWithoutOK = {DialogDescriptor.CANCEL_OPTION,
    DialogDescriptor.CLOSED_OPTION};
    private static Object[] closingOptionsWithOK = {DialogDescriptor.CANCEL_OPTION,
    DialogDescriptor.CLOSED_OPTION, DialogDescriptor.OK_OPTION};
    
    /**
     * Prompts user for a file.
     * @param extensions takes a list of file extensions
     * @param dialogTitle dialog title
     * @param maskTitle title for filter mask
     * @return filename or null if operation was cancelled
     */
    public static File selectFile(final String extensions, String dialogTitle, final String maskTitle) {
        JFileChooser chooser = new JFileChooser();
        
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                java.util.StringTokenizer token = new java.util.StringTokenizer(extensions, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return true;
                }
                return false;
            }
            public String getDescription() {
                return maskTitle; // NOI18N
            }
        });
        
        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }
        
        chooser.setDialogTitle(dialogTitle);
        while (chooser.showDialog(org.openide.windows.WindowManager.getDefault().getMainWindow(),
                NbBundle.getMessage(Utilities.class,"PROP_select_button"))
                == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            lastDirectory = chooser.getCurrentDirectory();
            if (f != null && f.isFile()) {
                java.util.StringTokenizer token = new java.util.StringTokenizer(extensions, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return f;
                }
            }
            
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(Utilities.class,"MSG_inValidFile"), NotifyDescriptor.WARNING_MESSAGE));
        }
        return null;
    }
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static JTextComponent findTextFieldForLabel(JComponent component, String text) {
        JLabel label = findLabel(component, text);
        if(label != null) {
            Component comp = label.getLabelFor();
            if (comp!=null && (comp instanceof JTextComponent)) return (JTextComponent)comp;
        }
        return null;
    }

    private static JLabel findLabel(JComponent component, String text) {
        Component[] components = component.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                if (((JLabel) comp).getText().equals(text)) {
                    return (JLabel) comp;
                }
            } else if (comp instanceof JComponent){
                return findLabel((JComponent) comp, text);
            }
        }
        return null;
    }
}
