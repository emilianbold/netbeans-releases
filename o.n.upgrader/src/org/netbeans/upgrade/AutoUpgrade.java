/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.upgrade;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

/** pending
 *
 * @author  Jiri Rechtacek
 */
public final class AutoUpgrade {

    public static void main (String[] args) throws Exception {
        String[] version = new String[1];
        File sourceFolder = checkPrevious (version);
        if (sourceFolder != null) {
            if (!showUpgradeDialog (sourceFolder)) {
                throw new org.openide.util.UserCancelException ();
            }
            doUpgrade (sourceFolder, version[0]);
        }
    }
    
    // the order of VERSION_TO_CHECK here defines the precedence of imports
    // the first one will be choosen for import
    final static private List VERSION_TO_CHECK = Arrays.asList (new String[] { ".netbeans/4.1", ".netbeans/4.0", ".netbeans/3.6", "jstudio_6me_user" });
    
    static private File checkPrevious (String[] version) {
        boolean exists;
        
        String userHome = System.getProperty ("user.home"); // NOI18N
        File sourceFolder = null;
        
        if (userHome != null) {
            File userHomeFile = new File (userHome);
            exists = userHomeFile.isDirectory ();

            Iterator it = VERSION_TO_CHECK.iterator ();
            String ver;
            while (it.hasNext () && sourceFolder == null) {
                ver = (String) it.next ();
                sourceFolder = new File (userHomeFile.getAbsolutePath (), ver);
                
                if (sourceFolder.isDirectory ()) {
                    version[0] = sourceFolder.getName();
                    break;
                }
                sourceFolder = null;
            }
            return sourceFolder;
        } else {
            return null;
        }
    }
    
    private static boolean showUpgradeDialog (final File source) {
        JOptionPane p = new JOptionPane (
            new AutoUpgradePanel (source.getAbsolutePath ()),
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.YES_NO_OPTION
        );
        javax.swing.JDialog d = p.createDialog (
            null,
            NbBundle.getMessage (AutoUpgrade.class, "MSG_Confirmation_Title") // NOI18N
        );
        d.setModal (true);
        d.setVisible (true);

        return new Integer (JOptionPane.YES_OPTION).equals (p.getValue ());
    }
    
    private static void doUpgrade (File source, String oldVersion) 
    throws java.io.IOException, java.beans.PropertyVetoException {
        
        
        if ("3.6".equals (oldVersion) || "jstudio_6me_user".equals (oldVersion)) { // NOI18N
            File userdir = new File(System.getProperty ("netbeans.user", "")); // NOI18N

            Reader r = new InputStreamReader (
                AutoUpgrade.class.getResourceAsStream ("copy"+oldVersion), // NOI18N
                "utf-8"
            );
            java.util.Set includeExclude = IncludeExclude.create (r);
            r.close ();


            ErrorManager.getDefault ().log (
                ErrorManager.USER, "Import: Old version: " // NOI18N
                + oldVersion + ". Importing from " + source + " to " + userdir // NOI18N
            );

            File oldConfig = new File (source, "system"); // NOI18N
            org.openide.filesystems.FileSystem old;
            {
                LocalFileSystem lfs = new LocalFileSystem ();
                lfs.setRootDirectory (oldConfig);
                old = new org.openide.filesystems.MultiFileSystem (
                    new org.openide.filesystems.FileSystem[] { lfs }
                );
            }
            org.openide.filesystems.FileSystem mine = Repository.getDefault ().getDefaultFileSystem ();
            
            FileObject defaultProject = old.findResource ("Projects/Default/system"); // NOI18N
            if (defaultProject != null) {
                // first copy content from default project
                Copy.copyDeep (defaultProject, mine.getRoot (), includeExclude);
            }
            
            FileObject projects = old.findResource ("Projects"); // NOI18N
            if (projects != null) {
                FileObject[] allProjects = projects.getChildren ();
                for (int i = 0; i < allProjects.length; i++) {
                    // content from projects is prefered
                    FileObject otherProject = allProjects[i].getFileObject ("system"); // NOI18N
                    if (otherProject != null) {
                        Copy.copyDeep (otherProject, mine.getRoot (), includeExclude);
                    }
                }
            }
            

            Copy.copyDeep (old.getRoot (), mine.getRoot (), includeExclude);
            return;
        }
        

        File userdir = new File(System.getProperty ("netbeans.user", "")); // NOI18N
        
        java.util.Set includeExclude;
        try {
            Reader r = new InputStreamReader (
                    AutoUpgrade.class.getResourceAsStream ("copy" + oldVersion), // NOI18N
                    "utf-8"); // NOI18N
            includeExclude = IncludeExclude.create (r);
            r.close ();
        } catch (IOException ex) {
            IOException e = new IOException ("Cannot import from version: " + oldVersion);
            e.initCause (ex);
            throw e;
        }

        ErrorManager.getDefault ().log (
            ErrorManager.USER, "Import: Old version: " // NOI18N
            + oldVersion + ". Importing from " + source + " to " + userdir // NOI18N
        );

        File oldConfig = new File (source, "config"); // NOI18N
        org.openide.filesystems.FileSystem old;
        {
            LocalFileSystem lfs = new LocalFileSystem ();
            lfs.setRootDirectory (oldConfig);
            old = new org.openide.filesystems.MultiFileSystem (
                new org.openide.filesystems.FileSystem[] { lfs }
            );
        }
        org.openide.filesystems.FileSystem mine = Repository.getDefault ().getDefaultFileSystem ();
        
        Copy.copyDeep (old.getRoot (), mine.getRoot (), includeExclude);
    }
}
