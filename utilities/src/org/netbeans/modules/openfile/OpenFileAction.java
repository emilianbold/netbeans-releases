/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.openfile;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.utilities.Manager;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

/** 
 * Action which allows user open file from disk. It is installed
 * in Menu | File | Open file... .
 *
 * @author Jesse Glick
 * @author Marian Petras
 */
public class OpenFileAction extends CallableSystemAction {

    /** Generated serial version UID. */
    static final long serialVersionUID = -3424129228987962529L;
    
    /** stores the last current directory of the file chooser */
    private static File currDir;

    
    public String getName() {
        return NbBundle.getMessage(OpenFileAction.class, "LBL_openFile");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenFileAction.class);
    }

    protected String iconResource() {
        return null; //"org/netbeans/modules/openfile/openFile.gif"; // NOI18N
    }

    /**
     * Creates and initializes a file chooser.
     *
     * @return  the initialized file chooser
     */
    protected JFileChooser prepareFileChooser() {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, currDir);
        HelpCtx.setHelpIDString(chooser, getHelpCtx().getHelpID());
        
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        
        /* initialize file filters */
        FileFilter currentFilter = chooser.getFileFilter();
        chooser.addChoosableFileFilter(new Filter(
            new String[] {DefaultOpenFileImpl.JAVA_EXT},
            NbBundle.getBundle(getClass()).getString("TXT_JavaFilter")));
        chooser.addChoosableFileFilter(new Filter(
            new String[] {DefaultOpenFileImpl.TXT_EXT}, 
            NbBundle.getBundle(getClass()).getString("TXT_TxtFilter")));
        chooser.setFileFilter(currentFilter);
        
        return chooser;
    }
    
    /**
     * Displays the specified file chooser and returns a list of selected files.
     *
     * @param  chooser  file chooser to display
     * @return  array of selected files,
     * @exception  org.openide.util.UserCancelException
     *                     if the user cancelled the operation
     */
    public static File[] chooseFilesToOpen(JFileChooser chooser)
            throws UserCancelException {
        File[] files;
        do {
            int selectedOption = chooser.showOpenDialog(
                WindowManager.getDefault().getMainWindow());
            
            if (selectedOption != JFileChooser.APPROVE_OPTION) {
                throw new UserCancelException();
            }
            files = chooser.getSelectedFiles();
        } while (files.length == 0);
        return files;
    }
    
    /**
     * {@inheritDoc} Displays a file chooser dialog
     * and opens the selected files.
     */
    public void performAction() {
        if (!Manager.actionActivated(this)) {
            return;
        }
        try {
            JFileChooser chooser = prepareFileChooser();
            File[] files;
            try {
                files = chooseFilesToOpen(chooser);
            } catch (UserCancelException ex) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                OpenFile.openFile(files[i], -1, null);
            }
            currDir = chooser.getCurrentDirectory();
        } finally {
            Manager.actionFinished(this);
        }
    }
    
    /**
     */
    protected boolean asynchronous() {
        return false;
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
