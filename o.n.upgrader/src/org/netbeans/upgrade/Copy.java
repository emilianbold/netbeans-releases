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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.*;

import org.openide.filesystems.*;

/** Does copy of objects on filesystems.
 *
 * @author Jaroslav Tulach
 */
final class Copy extends Object {


    /** Does a selective copy of one source tree to another.
     * @param source file object to copy from
     * @param target file object to copy to
     * @param thoseToCopy set on which contains (relativeNameOfAFileToCopy)
     *   is being called to find out whether to copy or not
     * @throws IOException if coping fails
     */
    public static void copyDeep (FileObject source, FileObject target, Set thoseToCopy) 
    throws IOException {
        copyDeep (source, target, thoseToCopy, null);
    }
    
    private static void copyDeep ( 
        FileObject source, FileObject target, Set thoseToCopy, String prefix
    ) throws IOException {
        FileObject src = prefix == null ? source : FileUtil.createFolder (source, prefix);
        
        FileObject[] arr = src.getChildren();
        for (int i = 0; i < arr.length; i++) {
            String fullname;
            if (prefix == null) {
                fullname = arr[i].getNameExt ();
            } else {
                fullname = prefix + "/" + arr[i].getNameExt ();
            }
            if (arr[i].isData ()) {
                if (!thoseToCopy.contains (fullname)) {
                    continue;
                }
            }

            
            if (arr[i].isFolder()) {
                copyDeep (source, target, thoseToCopy, fullname);
                if (thoseToCopy.contains (fullname) && arr[i].getAttributes ().hasMoreElements ()) {
                    FileObject tg = FileUtil.createFolder (target, fullname);
                    FileUtil.copyAttributes (arr[i], tg);
                }
            } else {
                FileObject folder = prefix == null ? target : FileUtil.createFolder (target, prefix);
                FileObject tg = folder.getFileObject (arr[i].getNameExt ());
                try {
                    if (tg == null) {
                        // copy the file otherwise keep old content
                        tg = FileUtil.copyFile (arr[i], folder, arr[i].getName(), arr[i].getExt ());
                    }
                } catch (IOException ex) {
                    if (arr[i].getNameExt().endsWith("_hidden")) {
                        continue;
                    }
                    throw ex;
                }
                FileUtil.copyAttributes (arr[i], tg);
            }
        }
        
        
    }
    
    public static void appendSelectedLines(File sourceFile, File targetFolder, String[] regexForSelection)
    throws IOException {        
        if (!sourceFile.exists()) {
            return;
        }
        Pattern[] linePattern = new Pattern[regexForSelection.length];
        for (int i = 0; i < linePattern.length; i++) {
            linePattern[i] = Pattern.compile(regexForSelection[i]);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();        
        File targetFile = new File(targetFolder,sourceFile.getName());
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        assert targetFolder.exists();
        
        if (!targetFile.exists()) {
            targetFile.createNewFile();
        } else {
            //read original content into  ByteArrayOutputStream
            FileInputStream targetIS = new FileInputStream(targetFile);
            try {
                FileUtil.copy(targetIS, bos);
            } finally {
                targetIS.close();
            }            
        }
        assert targetFile.exists();

        
        //append lines into ByteArrayOutputStream
        String line = null;        
        BufferedReader sourceReader = new BufferedReader(new FileReader(sourceFile));
        try {
            while ((line = sourceReader.readLine()) != null) {
                if (linePattern != null) {
                    for (int i = 0; i < linePattern.length; i++) {
                        Matcher m = linePattern[i].matcher(line);
                        if (m.matches()) {
                            bos.write(line.getBytes());
                            bos.write('\n');
                            break;
                        }                        
                    }                    
                } else {
                    bos.write(line.getBytes());
                    bos.write('\n');
                }
            }
        } finally {
            sourceReader.close();
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
        FileOutputStream targetOS = new FileOutputStream(targetFile);
        try {
            FileUtil.copy(bin, targetOS);        
        } finally {
            bin.close();
            targetOS.close();
        }
    }
    
    
    

    /** Updates the IDE.
     * @param sourceDir original instalation of the IDE
     * @param targetSystem target system to copy files to
     * @param backupSystem filesystem to do backupSystemFo to (or null)
     * @exception IOException if the copying fails
     *
    protected final void upgradeIde (String ver, File src, File trg) throws Exception {
        
        
        int version = getIdeVersion (ver);
        if (version < 0 || version >= versions.length) {
            message (getString ("MSG_BAD_IDE"));
            for (int i = 0 ; i < versions.length ; i++ ) {
                message (versions[i]);
            }
            throw new Exception ("Invalid IDE version"); //NOI18N
        }

        message (getString ("MSG_UPDATE_FROM", versions[version]));

        FileSystem srcFS = null;
        FileSystem trgFS = null;
        FileSystem tmpFS = null;
        Object filter [] = null;

        if (-1 != ver.indexOf (DIRTYPE_INST)) {
            File srcFile = new File (src, "system"); //NOI18N
            File trgFile = new File (trg, "system"); //NOI18N
            srcFS = createFileSystem (srcFile);
            trgFS = createFileSystem (trgFile);

            if (srcFS == null) {
                message (getString ("MSG_directory_not_exist", srcFile.getAbsolutePath ())); //NOI18N
                throw new Exception ("Directory doesn't exist - " + srcFile.getAbsolutePath ()); //NOI18N
            }

            if (trgFS == null) {
                message (getString ("MSG_directory_not_exist", trgFile.getAbsolutePath ()));
                throw new Exception ("Directory doesn't exist - " + trgFile.getAbsolutePath ()); //NOI18N
            }

            File tmpRoot = new File (trg, "system_backup"); //NOI18N
            if (!tmpRoot.exists ()) {
//                message (getString ("MSG_BackupDir_exists", tmpRoot.getAbsolutePath ())); //NOI18N
//                throw new Exception ("Backup directory already exists - " + tmpRoot.getAbsolutePath ()); //NOI18N
//            } else {
                tmpRoot.mkdirs ();
            }
            tmpFS = createFileSystem (tmpRoot);

            filter = originalFiles (nonCpFiles[version]);
        } else {
            srcFS = createFileSystem (src); //NOI18N
            trgFS = createFileSystem (trg); //NOI18N

            if (srcFS == null) {
                message (getString ("MSG_directory_not_exist", src.getAbsolutePath ())); //NOI18N
                throw new Exception ("Directory doesn't exist - " + src.getAbsolutePath ()); //NOI18N
            }

            if (trgFS == null) {
                message (getString ("MSG_directory_not_exist", trg.getAbsolutePath ())); //NOI18N
                throw new Exception ("Directory doesn't exist - " + trg.getAbsolutePath ()); //NOI18N
            }

            File tmpRoot = new File (trg.getParentFile (), "userdir_backup"); //NOI18N
            if (!tmpRoot.exists ()) {
//                message (getString ("MSG_BackupDir_exists", tmpRoot.getAbsolutePath ())); //NOI18N
//                throw new Exception ("Backup directory already exists - " + tmpRoot.getAbsolutePath ()); //NOI18N
//            } else {
                tmpRoot.mkdirs ();
            }
            tmpFS = createFileSystem (tmpRoot);

            filter = originalFiles (userdirNonCpFiles);
        }

        if (tmpFS != null) {
            // clean up temporary filesystem
            FileObject ch [] = tmpFS.getRoot ().getChildren ();
            for (int i = 0; i < ch.length; i++) {
                deleteAll (ch[i]);
            }
            // make a backup copy
            copyAttributes(trgFS.getRoot (), tmpFS.getRoot ());
            recursiveCopy(trgFS.getRoot (), tmpFS.getRoot ());
        }
        
        try {
            update (srcFS, trgFS, getLastModified (src), filter);
        }
        catch (Exception e) {
            if (tmpFS != null) {
                message (getString ("MSG_recovery_started")); //NOI18N
                deleteAll (trgFS.getRoot ());
                copyAttributes (tmpFS.getRoot (), trgFS.getRoot ());
                recursiveCopy (tmpFS.getRoot (), trgFS.getRoot ());
                message (getString ("MSG_recovery_finished")); //NOI18N
            }
            throw e;
        }
    }
    
    private FileSystem createFileSystem (File root) {
        LocalFileSystem lfs = null;

        if (root.exists () && root.isDirectory ()) {
            try {
                lfs = new LocalFileSystem ();
                lfs.setRootDirectory (root);
            }
            catch (Exception e) {
                lfs = null;
            }
        }
  
        return lfs == null ? null : new AttrslessLocalFileSystem (lfs);
    }

    private void update(
        FileSystem src, FileSystem trg, long sourceBaseTime, Object[] filter
    ) throws IOException {

        items = 0;
        maxItems = 0;

        copyAttributes (src.getRoot (),trg.getRoot ());
        recursiveCopyWithFilter (
            src.getRoot (),
            trg.getRoot (),
            filter,
            sourceBaseTime
        );
    }
    
    /** copies recursively directory, skips files existing in target location
     *  @param source source directory
     *  @param dest destination directory
     */
    private void recursiveCopy (FileObject sourceFolder, FileObject destFolder) throws IOException {
        FileObject childrens []  = sourceFolder.getChildren();
        for (int i = 0 ; i < childrens.length ; i++ ) {
            final FileObject subSourceFo = childrens[i];
            FileObject subTargetFo = null;
            
            if (subSourceFo.isFolder()) {
                subTargetFo =  destFolder.getFileObject(subSourceFo.getName());
                if (subTargetFo == null) {
                    subTargetFo = destFolder.createFolder(subSourceFo.getName());
                    
                }
                copyAttributes(subSourceFo,subTargetFo);
                recursiveCopy(subSourceFo,subTargetFo);
            } else {
                subTargetFo =  destFolder.getFileObject(subSourceFo.getNameExt());
                if (subTargetFo == null) {
                     if ( Utilities.getOperatingSystem () == Utilities.OS_VMS 
                        && subSourceFo.getNameExt ().equalsIgnoreCase ( "_nbattrs.") ) 
                        subTargetFo = FileUtil.copyFile(subSourceFo, destFolder, subSourceFo.getNameExt(), subSourceFo.getExt());
                     else
                        subTargetFo = FileUtil.copyFile(subSourceFo, destFolder, subSourceFo.getName(), subSourceFo.getExt());
                }
                copyAttributes(subSourceFo,subTargetFo);
            }
        }
    }

    private void message (String s) {
        
    }
    private void progress (int x, int y) {
        
    }
    private int maxItems;
    private int items;
    private int timeDev;
    
    /** Copies recursively dircectory. Files are copied when when basicTime + timeDev < time of file.
     *  @param source source directory
     *  @param #dest destination dirctory
     */
    private void recursiveCopyWithFilter (
        FileObject source, FileObject dest, Object[] filter, long basicTime
    ) throws IOException {
        FileObject childrens []  = source.getChildren();
        if (source.isFolder() == false ) {
            message (getString("MSG_IS_NOT_FOLDER", source.getName()));
        }

        // adjust max number of items
        maxItems += childrens.length;

        for (int i = 0 ; i < childrens.length ; i++ ) {
            FileObject subSourceFo = childrens[i];

            // report progress
            items++;
            progress(items, maxItems);

            if (!canCopy (subSourceFo, filter, basicTime))
                continue;
            
            FileObject subTargetFo = null;
            if (subSourceFo.isFolder ()) {
                subTargetFo =  dest.getFileObject (subSourceFo.getNameExt ());
                if (subTargetFo == null) {
                    subTargetFo = dest.createFolder (subSourceFo.getNameExt ());
                    
                }
                copyAttributes (subSourceFo, subTargetFo);
                recursiveCopyWithFilter (subSourceFo, subTargetFo, filter, basicTime);
            } else {
                subTargetFo = dest.getFileObject (subSourceFo.getName (), subSourceFo.getExt ());
               
                if (subTargetFo != null) {
                    FileLock lock = subTargetFo.lock ();
                    subTargetFo.delete (lock);
                    lock.releaseLock ();
                } 
                
                if ( Utilities.getOperatingSystem () == Utilities.OS_VMS 
                    && subSourceFo.getNameExt ().equalsIgnoreCase ( "_nbattrs.") ) 
                    subTargetFo = copyFile (subSourceFo, dest, subSourceFo.getNameExt ());
                else
                    subTargetFo = copyFile (subSourceFo, dest, subSourceFo.getName ());
                copyAttributes (subSourceFo, subTargetFo);
            }
        }
    }

    private FileObject copyFile (FileObject src, FileObject trg, String newName) throws IOException {
        return FileUtil.copyFile (src, trg, newName);
    }
        
    private static void copyAttributes (FileObject source, FileObject dest) throws IOException {
        Enumeration attrKeys = source.getAttributes();
        while (attrKeys.hasMoreElements()) {
            String key = (String) attrKeys.nextElement();
            Object value = source.getAttribute(key);
            if (value != null) {
                dest.setAttribute(key, value);
            }
        }
    }
    /** test if file can be copied
     */
    private boolean canCopy (FileObject fo, Object[] filter, long basicTime) throws IOException {
        String nonCopiedFiles [] = (String []) filter [0];
        String wildcards [] = (String []) filter [1];
        String name =  fo.getPath();
        
        if (fo.isFolder ()) {
            return Arrays.binarySearch (nonCopiedFiles, name + "/*") < 0; //NOI18N
        }

        for (int i = 0; i < wildcards.length; i++) {
            if (name.endsWith (wildcards [i])) {
                return false;
            }
        }

        long time =  fo.lastModified().getTime();
        
        boolean canCopy = Arrays.binarySearch (nonCopiedFiles, name) < 0 &&
               basicTime + timeDev <= time;
        if (!canCopy) {
            return false;
        }
        
        // #31623 - the fastjavac settings should not be imported.
        // In NB3.5 the fastjavac was separated into its own module.
        // Its old settings (bounded to java module) must not be imported.
        // For fastjavac settings created by NB3.5 this will work, because they
        // will be bound to new "org.netbeans.modules.java.fastjavac" module.
        if (fo.getExt().equals("settings")) { //NOI18N
            boolean tag1 = false;
            boolean tag2 = false;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(fo.getInputStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    if (line.indexOf("<module name=") != -1) { //NOI18N
                        if (line.indexOf("<module name=\"org.netbeans.modules.java/1\"") != -1) { //NOI18N
                            tag1 = true; // it is java module setting
                        } else {
                            break; // some other setting, ignore this file
                        }
                    }
                    if (line.indexOf("<serialdata class=") != -1) { //NOI18N
                        if (line.indexOf("<serialdata class=\"org.netbeans.modules.java.FastJavacCompilerType\">") != -1) { //NOI18N
                            tag2 = true; // it is fastjavac setting
                            if (tag1) {
                                break;
                            }
                        } else {
                            break; // some other setting, ignore this file
                        }
                    }
                }
            } catch (IOException ex) {
                // ignore this problem. 
                // in worst case the fastjavac settings will be copied.
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
            if (tag1 && tag2) {
                return false; // ignore this file. it is fastjavac settings
            }
        }
        
        return true;
    }
    // ************************* version retrieving code ********************

    
    /** We support import just from release 3.6
     * @param dir user dir to check for version
     * @return either null or name of the version
     */
    public static String getIdeVersion (File dir) {
        String version = null;
        String dirType = null;
        String branding = null;
        
        if (new File (dir, "system").exists ()) {
            return "3.6";
        }
        return null;
    }

    // ************** strings from bundle ***************

    protected static String getString (String key) {
        return NbBundle.getMessage (Copy.class, key);
    }

    protected static String getString (String key,String param) {
        return NbBundle.getMessage(Copy.class,key,param);
    }
    
    private static class AttrslessLocalFileSystem extends AbstractFileSystem implements AbstractFileSystem.Attr {
        public AttrslessLocalFileSystem (LocalFileSystem fs) {
            super ();
            this.change = new LocalFileSystem.Impl (fs);
            this.info = (AbstractFileSystem.Info) this.change;
            this.list = (AbstractFileSystem.List) this.change;
            this.attr = this;
        }
        public boolean isReadOnly () {
            return false;
        }
        public String getDisplayName () {
            return getClass ().toString (); // this will never be shown to user
        }

        // ***** no-op implementation of AbstractFileSystem.Attr *****

        public void deleteAttributes (String name) {
        }
        public Enumeration<String> attributes (String name) {
            return org.openide.util.Enumerations.empty ();
        }
        public void renameAttributes (String oldName, String newName) {
        }
        public void writeAttribute (String name, String attrName, Object value) throws IOException {
        }
        public Object readAttribute (String name, String attrName) {
            return null;
        }
    }
}
