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

package org.netbeans.upgrade;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.netbeans.upgrade.systemoptions.Importer;

import org.netbeans.util.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

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
            //#75324 NBplatform settings are not imported
            upgradeBuildProperties(sourceFolder, version);
            //migrates SystemOptions, converts them as a Preferences
            Importer.doImport();
        }
    }

    //#75324 NBplatform settings are not imported
    private static void upgradeBuildProperties(final File sourceFolder, final String[] version) throws  IOException {
        try {
            float f = Float.parseFloat(version[0]);
            if (f >= 5.0) {
                File userdir = new File(System.getProperty("netbeans.user", ""));//NOI18N
                String[] regexForSelection = new String[] {
                    "^nbplatform[.](?!default[.]netbeans[.]dest[.]dir).+[.].+=.+$"//NOI18N
                };
                Copy.appendSelectedLines(new File(sourceFolder,"build.properties"), //NOI18N
                        userdir,regexForSelection);
            }            
        } catch(NumberFormatException nex) {
            return;
        }
    }
    
    // the order of VERSION_TO_CHECK here defines the precedence of imports
    // the first one will be choosen for import
    final static private List VERSION_TO_CHECK = Arrays.asList (new String[] { ".netbeans/5.5",".netbeans/5.0" });
    
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
        Util.setDefaultLookAndFeel();
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
    
    static void doUpgrade (File source, String oldVersion) 
    throws java.io.IOException, java.beans.PropertyVetoException {        
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
            
            XMLFileSystem xmlfs = null;
            try {
                URL url = AutoUpgrade.class.getResource("layer" + oldVersion + ".xml"); // NOI18N
                xmlfs = new XMLFileSystem(url);
            } catch (SAXException ex) {
                IOException e = new IOException ("Cannot import from version: " + oldVersion); // NOI18N
                e.initCause (ex);
                throw e;
            }
            
            old = createLayeredSystem(lfs, xmlfs);
        }
        org.openide.filesystems.FileSystem mine = Repository.getDefault ().
            getDefaultFileSystem ();
        
        Copy.copyDeep (old.getRoot (), mine.getRoot (), includeExclude);
        
    }

    static MultiFileSystem createLayeredSystem(final LocalFileSystem lfs, final XMLFileSystem xmlfs) {
        MultiFileSystem old;
        
        old = new MultiFileSystem (
            new org.openide.filesystems.FileSystem[] { lfs, xmlfs }
        ) {
            {
                setPropagateMasks(true);
            }
        };
        return old;
    }
    
    private static List<FileObject> getFiles (
        FileObject      folder,
        int             depth,
        String          fileName,
        String          extension
    ) {
        if (depth == 0) {
            FileObject result = folder.getFileObject (fileName, extension);
            if (result == null) return Collections.emptyList();
            return Collections.singletonList (result);
        }
        Enumeration<? extends FileObject> en = folder.getChildren (false);
        List<FileObject> result = new ArrayList<FileObject> ();
        while (en.hasMoreElements ()) {
            FileObject fo = en.nextElement ();
            if (!fo.isFolder ()) continue;
            result.addAll (getFiles (fo, depth - 1, fileName, extension));
        }
        return result;
    }
    
    private static void copy (FileObject sourceDir, FileObject destDir) 
    throws IOException {
        Enumeration en = sourceDir.getData (false);
        while (en.hasMoreElements ()) {
            FileObject fo = (FileObject) en.nextElement ();
            if (fo.isFolder ()) {
                FileObject newDestDir = destDir.createFolder (fo.getName ());
                copy (fo, newDestDir);
            } else {
                try {
                    FileObject destFile = FileUtil.copyFile 
                            (fo, destDir, fo.getName (), fo.getExt ());
                    FileUtil.copyAttributes (fo, destFile);
                } catch (IOException ex) {    
                    if (!fo.getNameExt ().endsWith ("_hidden"))
                        throw ex;    
                }
            }
        }
    }
}