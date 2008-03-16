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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.upgrade;
import java.beans.PropertyVetoException;
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
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/** pending
 *
 * @author  Jiri Rechtacek
 */
public final class AutoUpgrade {

    public static void main (String[] args) throws Exception {
        String[] version = new String[1];
        File sourceFolder = checkPrevious (version, VERSION_TO_CHECK);
        if (sourceFolder != null) {
            if (!showUpgradeDialog (sourceFolder)) {
                throw new org.openide.util.UserCancelException ();
            }
            doUpgrade (sourceFolder, version[0]);
            //support for non standard configuration files
            doNonStandardUpgrade(sourceFolder, version[0]);
            //#75324 NBplatform settings are not imported
            upgradeBuildProperties(sourceFolder, version);
            //migrates SystemOptions, converts them as a Preferences
            Importer.doImport();
        }
    }

    //#75324 NBplatform settings are not imported
    private static void upgradeBuildProperties(final File sourceFolder, final String[] version) throws  IOException {
        try {   
            //TODO: review and implement less version specific
            if (version[0].startsWith("2_")) {//CREATOR
                File userdir = new File(System.getProperty("netbeans.user", ""));//NOI18N
                Copy.appendSelectedLines(new File(sourceFolder,"build.properties"), //NOI18N
                        userdir,new String[] {".*"});                
            } else if (Float.parseFloat(version[0]) >= 5.0 ) {//NOI18N
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

    private static final String CREATOR = ".Creator/2_1"; //NOI18N
    private static final String VISUALWEB_REPRESENTATION = "modules/org-netbeans-modules-visualweb-insync.jar";//NOI18N
    
    // the order of VERSION_TO_CHECK here defines the precedence of imports
    // the first one will be choosen for import
    final static private List VERSION_TO_CHECK = 
            Arrays.asList (new String[] { ".netbeans/6.0",".netbeans/5.5.1",".netbeans/5.5",".netbeans/5.0",CREATOR });//NOI18N

            
    static private File checkPrevious (String[] version, final List versionsToCheck) {        
        String userHome = System.getProperty ("user.home"); // NOI18N
        File sourceFolder = null;
        
        if (userHome != null) {
            File userHomeFile = new File (userHome);
            Iterator it = versionsToCheck.iterator ();
            String ver;
            while (it.hasNext () && sourceFolder == null) {
                ver = (String) it.next ();
                if (ver.equals(CREATOR)) {//NOI18N
                    final boolean visualWebPresent = InstalledFileLocator.getDefault().locate(VISUALWEB_REPRESENTATION, null, false) != null;
                    if (!visualWebPresent) {
                        continue;
                    }
                }                
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
                xmlfs = (url != null) ? new XMLFileSystem(url) : null;
            } catch (SAXException ex) {
                IOException e = new IOException ("Cannot import from version: " + oldVersion); // NOI18N
                e.initCause (ex);
                throw e;
            }
            
            old = (xmlfs != null) ? createLayeredSystem(lfs, xmlfs) : lfs;
        }
        org.openide.filesystems.FileSystem mine = Repository.getDefault ().
            getDefaultFileSystem ();
        
        Copy.copyDeep (old.getRoot (), mine.getRoot (), includeExclude, PathTransformation.getInstance(oldVersion));
        
    }
    
    /* copy-pasted method doUpgrade and slightly modified to copy files relative
     * to userdir.
     */
    private static void doNonStandardUpgrade (File source,String oldVersion) 
            throws IOException, PropertyVetoException {
        File userdir = new File(System.getProperty("netbeans.user", "")); // NOI18N        
        java.util.Set includeExclude;
        try {
            InputStream is = AutoUpgrade.class.getResourceAsStream("nonstandard" + oldVersion);
            if (is == null) return;
            Reader r = new InputStreamReader(is, "utf-8"); // NOI18N
            includeExclude = IncludeExclude.create(r);
            r.close();
        } catch (IOException ex) {
            IOException e = new IOException("Cannot import from version: " +  oldVersion + "nonstandard");
            e.initCause(ex);
            throw e;
        }        
        ErrorManager.getDefault ().log (ErrorManager.USER, "Import: Old version: " // NOI18N
            + oldVersion + "nonstandard"  + ". Importing from " + source + " to " + userdir // NOI18N
        );        
        
        LocalFileSystem  old = new LocalFileSystem();
        old.setRootDirectory(source);
        
        LocalFileSystem nfs = new LocalFileSystem();
        nfs.setRootDirectory(userdir);                
        Copy.copyDeep(old.getRoot(), nfs.getRoot(), includeExclude, PathTransformation.getInstance(oldVersion));
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