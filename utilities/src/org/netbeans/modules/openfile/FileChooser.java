/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openfile;

import java.awt.GridLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Jesse Glick
 * @author  Marian Petras
 */
class FileChooser extends JFileChooser {
    
    /** Creates a new instance of FileChooser */
    FileChooser() {
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        setMultiSelectionEnabled(true);
        
        /* initialize file filters */
        FileFilter currentFilter = getFileFilter();
        addChoosableFileFilter(new Filter(
            new String[] {DefaultOpenFileImpl.JAVA_EXT},
            NbBundle.getMessage(FileChooser.class, "TXT_JavaFilter"))); //NOI18N
        addChoosableFileFilter(new Filter(
            new String[] {DefaultOpenFileImpl.TXT_EXT}, 
            NbBundle.getMessage(FileChooser.class, "TXT_TxtFilter")));  //NOI18N
        setFileFilter(currentFilter);
    }

    public int showDialog(Component parent, String approveButtonText) {
        // don't offer C:\Document and Settings\joeuser as the default starting
        // point in the file chooser on Windows
        
        if (Utilities.isWindows() && getCurrentDirectory() != null) {
            String dir = System.getProperty("user.home");   //NOI18N
            if (dir != null && dir.startsWith(":\\", 1)) { // NOI18N
                dir = dir.substring(0, 3);
                setCurrentDirectory(new File(dir));
            }
            else if (new File("C:\\").exists()) { // NOI18N
                setCurrentDirectory(new File("C:\\")); // NOI18N
            }
        }

        return super.showDialog(parent, approveButtonText);
    }
    
    public void approveSelection() {
        final File[] selectedFiles = getSelectedFiles();

        /* check the files: */
        List/*<String>*/ errorMsgs = null;
        for (int i = 0; i < selectedFiles.length; i++) {
            String msgPatternRef = null;
            File file = selectedFiles[i];

            if (!file.exists()) {
                msgPatternRef = "MSG_FileDoesNotExist";                 //NOI18N
            } else if (file.isDirectory()) {
                msgPatternRef = "MSG_FileIsADirectory";                 //NOI18N
            } else if (!file.isFile()) {
                msgPatternRef = "MSG_FileIsNotPlainFile";               //NOI18N
            }
            if (msgPatternRef == null) {
                continue;
            }

            if (errorMsgs == null) {
                errorMsgs = new ArrayList(selectedFiles.length - i);
            }
            errorMsgs.add(NbBundle.getMessage(FileChooser.class,
                                              msgPatternRef,
                                              file.getName()));
        }
        if (errorMsgs == null) {
            super.approveSelection();
        } else {
            JPanel panel = new JPanel(new GridLayout(errorMsgs.size(), 0,
                                                     0, 2));        //gaps
            for (java.util.Iterator i = errorMsgs.iterator(); i.hasNext(); ) {
                panel.add(new JLabel((String) i.next()));
            }
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            panel, NotifyDescriptor.WARNING_MESSAGE));
        }
    }
        
    /** File chooser filter that filters files by their names' suffixes. */
    private static class Filter extends FileFilter {
        
        /** suffixes accepted by this filter */
        private String[] extensions;
        
        /** localized description of this filter */
        private String description;
        
        
        /**
         * Creates a new filter that accepts files having specified suffixes.
         * The filter is case-insensitive.
         * <p>
         * The filter does not use file <em>extensions</em> but it just
         * tests whether the file name ends with the specified string.
         * So it is recommended to pass a file name extension including the
         * preceding dot rather than just the extension.
         *
         * @param  extensions  list of accepted suffixes
         * @param  description  name of the filter
         */
        public Filter(String[] extensions, String description) {
            
            this.extensions = new String[extensions.length];
            for (int i = 0; i < extensions.length; i++) {
                this.extensions[i] = extensions[i].toUpperCase();
            }
            this.description = description;
        }
        
        
        /**
         * @return  <code>true</code> if the file's name ends with one of the
         *          strings specified by the constructor or if the file
         *          is a directory, <code>false</code> otherwise
         */
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            for (int i = 0; i < extensions.length; i++) {
                if (file.getName().toUpperCase().endsWith(extensions[i])) {
                    return true;
                }
            }
            
            return false;
        }
        
        /** */
        public String getDescription() {
            return description;
        }
    } // End of Filter class.

}
