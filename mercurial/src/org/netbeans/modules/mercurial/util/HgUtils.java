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

package org.netbeans.modules.mercurial.util;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.status.SyncFileNode;
import org.openide.util.NbBundle;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.api.project.ProjectManager;

/**
 *
 * @author jrice
 */
public class HgUtils {    
    private static final Pattern metadataPattern = Pattern.compile(".*\\" + File.separatorChar + "(\\.)hg(\\" + File.separatorChar + ".*|$)"); // NOI18N
    
    // IGNORE SUPPORT HG: following file patterns are added to {Hg repos}/.hgignore and Hg will ignore any files
    // that match these patterns, reporting "I"status for them // NOI18N
    private static final String [] HG_IGNORE_FILES = { ".hgignore", "*.orig"}; // NOI18N
    
    private static final String HG_IGNORE_FILE = ".hgignore"; // NOI18N
    private static final String HG_IGNORE_PATTERN = "syntax: glob\n"; // NOI18N


    /**
     * confirmDialog - display a confirmation dialog
     *
     * @param bundleLocation location of string resources to display
     * @param title of dialog to display    
     * @param query ask user
     * @return boolean true - answered Yes, false - answered No
     */
    public static boolean confirmDialog(Class bundleLocation, String title, String query) {
        int response = JOptionPane.showOptionDialog(null, NbBundle.getMessage(bundleLocation, query), NbBundle.getMessage(bundleLocation, title), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (response == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * warningDialog - display a warning dialog
     *
     * @param bundleLocation location of string resources to display
     * @param title of dialog to display    
     * @param warning to display to the user
     */
     public static void warningDialog(Class bundleLocation, String title, String warning) {
        JOptionPane.showMessageDialog(null,
                NbBundle.getMessage(bundleLocation,warning),
                NbBundle.getMessage(bundleLocation,title),
                JOptionPane.WARNING_MESSAGE);
    }
    

    /**
     * isLocallyAdded - checks to see if this file has been Locally Added to Hg
     *
     * @param file to check
     * @return boolean true - ignore, false - not ignored
     */
    public static boolean isLocallyAdded(File file){
        if (file == null) return false;
        Mercurial hg = Mercurial.getInstance();        

        if ((hg.getFileStatusCache().getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) !=0)
            return true;
        else
            return false;
    }
    
    /**
     * isIgnored - checks to see if this is a file Hg should ignore
     *
     * @param File file to check
     * @return boolean true - ignore, false - not ignored
     */
    public static boolean isIgnored(File file){
        if (file == null) return true;
        
        for(String name: HG_IGNORE_FILES){
            if(file.getName().equals(name))
                return true;
        }
        return false;
    }

    /**
     * createIgnored - creates .hgignore file in the repository in which 
     * the given file belongs. This .hgignore file ensures Hg will ignore 
     * the files specified in HG_IGNORE_FILES list
     *
     * @param path to repository to place .hgignore file
     */
    public static void createIgnored(File path){
        if( path == null) return;
        BufferedWriter fileWriter = null;
        Mercurial hg = Mercurial.getInstance();
        File root = hg.getTopmostManagedParent(path);
        if( root == null) return;
        File ignore = new File(root, HG_IGNORE_FILE);
           
        try     {
            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(ignore)));
            fileWriter.write(HG_IGNORE_PATTERN);
            for (String name : HG_IGNORE_FILES) {
                fileWriter.write(name + "\n"); // NOI18N
            }
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.FINE, "createIgnored(): File {0} - {1}",  // NOI18N
                    new Object[] {ignore.getAbsolutePath(), ex.toString()});
        }finally {
            try {
                fileWriter.close();
            } catch (IOException ex) {
                Mercurial.LOG.log(Level.FINE, "createIgnored(): File {0} - {1}",  // NOI18N
                        new Object[] {ignore.getAbsolutePath(), ex.toString()});
            }
        }
    }

    private static LinkedList<String> readIgnoreEntries(File directory) throws IOException {
        File hgIgnore = new File(directory, HG_IGNORE_FILE);

        LinkedList<String> entries = new LinkedList<String>();
        if (!hgIgnore.canRead()) return entries;

        String s;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(hgIgnore));
            while ((s = r.readLine()) != null) {
                System.err.println("readIgnoreEntries: " + s); // NOI18N
                entries.add(s.trim());
            }
        } finally {
            if (r != null) try { r.close(); } catch (IOException e) {}
        }
        return entries;
    }

    private static String computePatternToIgnore(File directory, File file) {
        String name = file.getAbsolutePath().substring(directory.getAbsolutePath().length()+1);
        return name.replace(' ', '?');
    }

    private static void writeIgnoreEntries(File directory, LinkedList entries) throws IOException {
        File hgIgnore = new File(directory, HG_IGNORE_FILE);
        FileObject fo = FileUtil.toFileObject(hgIgnore);

        if (entries.size() == 0) {
            if (fo != null) fo.delete();
            return;
        }

        if (fo == null || !fo.isValid()) {
            fo = FileUtil.toFileObject(directory);
            fo = fo.createData(HG_IGNORE_FILE);
        }
        FileLock lock = fo.lock();
        PrintWriter w = null;
        try {
            w = new PrintWriter(fo.getOutputStream(lock));
            for (Iterator i = entries.iterator(); i.hasNext();) {
                w.println(i.next());
            }
        } finally {
            lock.releaseLock();
            if (w != null) w.close();
        }
    }

    /**
     * addIgnored - Add the specified files to the .hgignore file in the 
     * specified repository.
     *
     * @param directory for repository for .hgignore file
     * @param files an array of Files to be added
     */
    public static void addIgnored(File directory, File[] files) throws IOException {
        LinkedList entries = readIgnoreEntries(directory);
        for (File file: files) {
            String patterntoIgnore = computePatternToIgnore(directory, file);
            entries.add(0, patterntoIgnore);
        }
        writeIgnoreEntries(directory, entries);
    }

    /**
     * removeIgnored - Remove the specified files from the .hgignore file in 
     * the specified repository.
     *
     * @param directory for repository for .hgignore file
     * @param files an array of Files to be removed
     */
    public static void removeIgnored(File directory, File[] files) throws IOException {
        LinkedList entries = readIgnoreEntries(directory);
        for (File file: files) {
            String patterntoIgnore = computePatternToIgnore(directory, file);
            entries.remove(patterntoIgnore);
        }
        writeIgnoreEntries(directory, entries);
    }


    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead of Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @param nodes null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     * @param includingFileStatus if any activated file does not have this CVS status, an empty array is returned
     * @param includingFolderStatus if any activated folder does not have this CVS status, an empty array is returned
     * @return File [] array of activated files, or an empty array if any of examined files/folders does not have given status
     */
    public static VCSContext getCurrentContext(Node[] nodes, int includingFileStatus, int includingFolderStatus) {
        VCSContext context = getCurrentContext(nodes);
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (File file : context.getRootFiles()) {
            FileInformation fi = cache.getStatus(file);
            if (file.isDirectory()) {
                if ((fi.getStatus() & includingFolderStatus) == 0) return VCSContext.EMPTY;
            } else {
                if ((fi.getStatus() & includingFileStatus) == 0) return VCSContext.EMPTY;
            }
        }
        return context;
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActiva
tedNodes()} except that this
     * method returns File objects instead of Nodes. Every node is examined for
Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are repr
esented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @return File [] array of activated files
     * @param nodes or null (then taken from windowsystem, it may be wrong on ed
itor tabs #66700).
     */

    public static VCSContext getCurrentContext(Node[] nodes) {
        if (nodes == null) {
            nodes = TopComponent.getRegistry().getActivatedNodes();
        }
        return VCSContext.forNodes(nodes);
    }

   /**
     * Returns path to repository root or null if not managed
     *
     * @param VCSContext
     * @return String of repository root path
     */
    public static String getRootPath(VCSContext context){
        File root = getRootFile(context);
        return (root == null) ? null: root.getAbsolutePath();
    }
    
   /**
     * Returns path to repository root or null if not managed
     *
     * @param VCSContext
     * @return String of repository root path
     */
    public static File getRootFile(VCSContext context){
        if (context == null) return null;
        Mercurial hg = Mercurial.getInstance();
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) return null;
        
        File root = hg.getTopmostManagedParent(files[0]);
        return root;
    }
    
   /**
     * Returns path to Project Directory root
     *
     * @param VCSContext
     * @return String of Project Directory path
     */
    public static File getProjectFile(VCSContext context){
        if (context == null) return null;
        Mercurial hg = Mercurial.getInstance();
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) return null;
        
        File root = files[0];
        final ProjectManager projectManager = ProjectManager.getDefault();
        for (;root != null; root = root.getParentFile()) {
            FileObject rootFileObj = FileUtil.toFileObject(FileUtil.normalizeFile(root));
            if (projectManager.isProject(rootFileObj)){
                break;
             }
        }
        return root;
    }
    
    /**
     * Checks file location to see if it is part of mercurial metdata
     *
     * @param file file to check
     * @return true if the file or folder is a part of mercurial metadata, false otherwise
     */
    public static boolean isPartOfMercurialMetadata(File file) {
        return metadataPattern.matcher(file.getAbsolutePath()).matches();
    }
    

    /**
     * Forces refresh of Status for the given directory and files below recursively
     *
     * @param start file or dir to begin refresh from
     * @return void
     */
    public static void forceStatusRefresh(File file) {
        if (Mercurial.getInstance().isAdministrative(file)) return;
        
        Mercurial.getInstance().getFileStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        if(!file.isFile()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                forceStatusRefresh(files[i]);
            }
        }                
    }

    /**
     * Tests parent/child relationship of files.
     *
     * @param parent file to be parent of the second parameter
     * @param file file to be a child of the first parameter
     * @return true if the second parameter represents the same file as the first parameter OR is its descendant (child)
     */
    public static boolean isParentOrEqual(File parent, File file) {
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(parent)) return true;
        }
        return false;
    }
    
    /**
     * Returns path of file relative to root repository or a warning message
     * if the file is not under the repository root.
     *
     * @param File to get relative path from the repository root
     * @return String of relative path of the file from teh repository root
     */
    public static String getRelativePath(File file) {
            if (file == null){
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
            String shortPath = file.getAbsolutePath();
            if (shortPath == null){
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
            
            Mercurial mercurial = Mercurial.getInstance();
            File rootManagedFolder = mercurial.getTopmostManagedParent(file);
            if ( rootManagedFolder == null){
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
            
            String root = rootManagedFolder.getAbsolutePath();
            if(shortPath.startsWith(root)) {
                return shortPath.substring(root.length()+1);
            }else{
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
     }

    /**
     * Normalize flat files, Mercurial treats folder as normal file
     * so it's necessary explicitly list direct descendants to
     * get classical flat behaviour.
     *
     * <p> E.g. revert on package node means:
     * <ul>
     *   <li>revert package folder properties AND
     *   <li>revert all modified (including deleted) files in the folder
     * </ul>
     *
     * @return files with given status and direct descendants with given status.
     */

    public static File[] flatten(File[] files, int status) {
        LinkedList<File> ret = new LinkedList<File>();

        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (int i = 0; i<files.length; i++) {
            File dir = files[i];
            FileInformation info = cache.getStatus(dir);
            if ((status & info.getStatus()) != 0) {
                ret.add(dir);
            }
            File[] entries = cache.listFiles(dir);  // comparing to dir.listFiles() lists already deleted too
            for (int e = 0; e<entries.length; e++) {
                File entry = entries[e];
                info = cache.getStatus(entry);
                if ((status & info.getStatus()) != 0) {
                    ret.add(entry);
                }
            }
        }

        return ret.toArray(new File[ret.size()]);
    }

    /**
     * Utility method that returns all non-excluded modified files that are
     * under given roots (folders) and have one of specified statuses.
     *
     * @param context context to search
     * @param includeStatus bit mask of file statuses to include in result
     * @return File [] array of Files having specified status
     */
    public static File [] getModifiedFiles(VCSContext context, int includeStatus) {
        File[] all = Mercurial.getInstance().getFileStatusCache().listFiles(context, includeStatus);
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < all.length; i++) {
            File file = all[i];
            String path = file.getAbsolutePath();
            if (HgModuleConfig.getDefault().isExcludedFromCommit(path) == false) {
                files.add(file);
            }
        }

        // ensure that command roots (files that were explicitly selected by user) are included in Diff
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (File file : context.getRootFiles()) {
            if (file.isFile() && (cache.getStatus(file).getStatus() & includeStatus) != 0 && !files.contains(file)) {
                files.add(file);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    /**
     * Checks if the file is binary.
     *
     * @param file file to check
     * @return true if the file cannot be edited in NetBeans text editor, false otherwise
     */
    public static boolean isFileContentBinary(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return false;
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getCookie(EditorCookie.class) == null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }

    /**
     * @return true if the buffer is almost certainly binary.
     * Note: Non-ASCII based encoding encoded text is binary,
     * newlines cannot be reliably detected.
     */
    public static boolean isBinary(byte[] buffer) {
        for (int i = 0; i<buffer.length; i++) {
            int ch = buffer[i];
            if (ch < 32 && ch != '\t' && ch != '\n' && ch != '\r') {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares two {@link FileInformation} objects by importance of statuses they represent.
     */
    public static class ByImportanceComparator<T> implements Comparator<FileInformation> {
        public int compare(FileInformation i1, FileInformation i2) {
            return getComparableStatus(i1.getStatus()) - getComparableStatus(i2.getStatus());
        }
    }

    /**
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100.
     *
     * @return status constant suitable for 'by importance' comparators
     */
    public static int getComparableStatus(int status) {
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return 0;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return 1;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return 10;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return 11;
       } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return 12;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return 13;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return 14;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return 30;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return 31;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return 32;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return 50;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)){
            return 100;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return 101;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return 102;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }

    protected static int getFileEnabledStatus() {
        return ~0;
    }

    protected static int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    /**
     * Rips an eventual username off - e.g. user@svn.host.org
     *
     * @param host - hostname with a userneame
     * @return host - hostname without the username
     */
    public static String ripUserFromHost(String host) {
        int idx = host.indexOf('@');
        if(idx < 0) {
            return host;
        } else {
            return host.substring(idx + 1);
        }
    }


    /**
     * Print contents of list to Mercurial Output Tab
     *
     * @param list to print out
     * 
     */
     public static void outputMercurialTab(List<String> list){
        if( list.isEmpty()) return;

        InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
        io.select();
        OutputWriter out = io.getOut();
        
        for (String s : list){
            out.println(s);
        }
        out.close();
    }

     /**
     * Print msg to Mercurial Output Tab
     *
     * @param list to print out
     * 
     */
     public static void outputMercurialTab(String msg){
        if( msg == null) return;

        InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
        io.select();
        OutputWriter out = io.getOut();
        
        out.println(msg);
        out.close();
    }

    /**
     * Print msg to Mercurial Output Tab in Red
     *
     * @param list to print out
     * 
     */
     public static void outputMercurialTabInRed(String msg){
        if( msg == null) return;

        InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
        io.select();
        OutputWriter out = io.getErr();
        
        out.println(msg);
        out.close();
    }

    /**
     * Select and Clear Mercurial Output Tab
     *
     * @param list to print out
     * 
     */
     public static void clearOutputMercurialTab(){
         InputOutput io = IOProvider.getDefault().getIO(
                 Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
         
         io.select();
         OutputWriter out = io.getOut();
         
         try {
             out.reset();
         } catch (IOException ex) {
             // Ignore Exception
         }
         out.close();
    }
     
    /**
     * This utility class should not be instantiated anywhere.
     */
    private HgUtils() {
    }
    
}
