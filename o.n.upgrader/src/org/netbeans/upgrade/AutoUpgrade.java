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
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
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
        
/*
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
*/

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
        
/*
        // convert shortcuts ...................................................
        
        // copy Shortcuts to Keymaps/NetBeans
        FileObject shortcuts = old.getRoot ().getFileObject ("Shortcuts"); //NOI18N
        if (shortcuts != null) {
            FileObject root = mine.getRoot ();
            FileObject keymaps = root.getFileObject ("Keymaps"); //NOI18N
            if (keymaps == null)
                keymaps = root.createFolder ("Keymaps"); //NOI18N
            FileObject nb = keymaps.getFileObject ("NetBeans");
            if (nb == null)
                nb = keymaps.createFolder ("NetBeans"); //NOI18N
            copy (shortcuts, nb);
        }
        
        // convert editor font & colors ........................................
        
        // load default NetBeans coloring scheme
        // mimeTypeToColoring: Map (
        //     String (resourceName starting in Editors) > 
        //     Map (
        //         String (attribute name like "java-comment") > 
        //         SimpleAttributeSet))
        Map mimeTypeToColoring = new HashMap ();
        addColoring (mimeTypeToColoring, "NetBeans41/defaultColoring.xml");
        addColoring (mimeTypeToColoring, "NetBeans41/editorColoring.xml");
        addColoring (mimeTypeToColoring, "text/plain/NetBeans41/coloring.xml");
        addColoring (mimeTypeToColoring, "text/css/NetBeans41/coloring.xml");
        addColoring (mimeTypeToColoring, "text/html/NetBeans41/coloring.xml");
        addColoring (mimeTypeToColoring, "text/x-java/NetBeans41/coloring.xml");
        addColoring (mimeTypeToColoring, "text/x-jsp/NetBeans41/coloring.xml");
        addColoring (mimeTypeToColoring, "text/xml/NetBeans41/coloring.xml");
        addColoring (mimeTypeToColoring, "text/x-properties/NetBeans41/coloring.xml");
        addColoring (mimeTypeToColoring, "application/xml-dtd/NetBeans41/coloring.xml");
        
        // get all old coloring files
        FileObject editors = old.getRoot ().getFileObject ("Editors"); //NOI18N
        if (editors == null) return;
        List coloringFiles = getFiles (editors, 2, "fontsColors", "xml");
        
        // List => Map (String (mimeType) > List (AttributeSet))
        Iterator it = coloringFiles.iterator ();
        while (it.hasNext ()) {
            FileObject fo = (FileObject) it.next ();
            String name = fo.getPath ();
            name = name.substring (
                "Editors/".length (), // beginIndex
                name.length () - 
                    "/fontColors.xml".length () - 1 // endIndex
            );
            Map coloring = ColoringStorage.loadColorings (
                fo.getInputStream (),
                fo.getPath ()
            );
            mergeColorings (
                mimeTypeToColoring,
                name + "/NetBeans41/coloring.xml",
                coloring
            );
            if (name.equals ("text/x-java")) {
                initializeAllLanguages (mimeTypeToColoring, coloring);
                initializeHighlightings (mimeTypeToColoring, coloring);
            }
        }

        // save colorings
        it = mimeTypeToColoring.keySet ().iterator ();
        while (it.hasNext ()) {
            String name = (String) it.next ();
            FileObject fo = FileUtil.createData 
                (mine.getRoot (), "Editors/" + name);
            Map colorings = (Map) mimeTypeToColoring.get (name);
            ColoringStorage.saveColorings (fo, colorings.values ());
        }
        
        // set current coloring to NetBeans41
        FileObject newEditors = mine.getRoot ().getFileObject ("Editors");
        newEditors.setAttribute ("currentFontColorProfile", "NetBeans41");
*/
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

    /**
     * Loads coloring defaults for given name and adds it to given map.
     */
    private static void addColoring (Map m, String name) {
        InputStream is = AutoUpgrade.class.getResourceAsStream ( 
            "/org/netbeans/upgrade/resources/" + name);
        m.put (name, ColoringStorage.loadColorings (is, name));
    }
    
    private static void mergeColorings (Map m, String name, Map coloring) {
        Map defaults = (Map) m.get (name);
        if (defaults == null) return;
        Iterator it = coloring.keySet ().iterator ();
        while (it.hasNext ()) {
            String attributeName = (String) it.next ();
            mergeAttributes (defaults, attributeName, coloring, attributeName);
        }
    }
    
    private static void initializeAllLanguages (Map m, Map javaColoring) {
        Map a = (Map) m.get ("NetBeans41/defaultColoring.xml");
        mergeAttributes (a, "default", javaColoring, "default");
        mergeAttributes (a, "char", javaColoring, "java-char-literal");
        mergeAttributes (a, "comment", javaColoring, "java-block-comment");
        mergeAttributes (a, "error", javaColoring, "java-errors");
        mergeAttributes (a, "identifier", javaColoring, "java-identifier");
        mergeAttributes (a, "keyword", javaColoring, "java-keywords");
        mergeAttributes (a, "number", javaColoring, "java-numeric-literals");
        mergeAttributes (a, "operator", javaColoring, "java-operators");
        mergeAttributes (a, "string", javaColoring, "java-string-literal");
        mergeAttributes (a, "whitespace", javaColoring, "java-whitespace");
    }
    
    private static void initializeHighlightings (Map m, Map javaColoring) {
        Map a = (Map) m.get ("NetBeans41/editorColoring.xml");
        Iterator it = a.keySet ().iterator ();
        while (it.hasNext ()) {
            String attributeName = (String) it.next ();
            mergeAttributes (a, attributeName, javaColoring, attributeName);
        }
    }
    
    private static void mergeAttributes (Map m1, String n1, Map m2, String n2) {
        SimpleAttributeSet a1 = (SimpleAttributeSet) m1.get (n1);
        SimpleAttributeSet a2 = (SimpleAttributeSet) m2.get (n2);
        if (a2 == null || a1 == null) return;
        a1.addAttributes (a2);
        a1.addAttribute (StyleConstants.NameAttribute, n1);
    }
    
    private static List getFiles (
        FileObject      folder,
        int             depth,
        String          fileName,
        String          extension
    ) {
        if (depth == 0) {
            FileObject result = folder.getFileObject (fileName, extension);
            if (result == null) return Collections.EMPTY_LIST;
            return Collections.singletonList (result);
        }
        Enumeration en = folder.getChildren (false);
        List result = new ArrayList ();
        while (en.hasMoreElements ()) {
            FileObject fo = (FileObject) en.nextElement ();
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



