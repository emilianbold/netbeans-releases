/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.text.MessageFormat;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFolder;
import org.openide.execution.NbClassPath;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.core.WebExecUtil;
import org.openide.filesystems.FileUtil;

/** JSP compilation utilities
*
* @author Petr Jiricka
*/
public class JspCompileUtil {
    
    private static final Object repositoryJobLock = new Object();

    /** Finds a fileobject for an absolute file name or null if not found */
    public static FileObject findFileObjectForFile(String fileName) {
        Repository rep = Repository.getDefault();
        for (Enumeration en = rep.getFileSystems(); en.hasMoreElements(); ) {
            FileSystem fs = (FileSystem)en.nextElement();
            FileObject fo = findFileObjectForFile(fs, fileName);
            if (fo != null)
                return fo;
        }
        return null;
    }
    
    /** Finds a FileObject for an absolute file name and ensures that the resource
     * path of the FileObject matches the specified resource name. 
     * @return null if such a FileObject was not found
     */
    public static FileObject findFileObjectForFile(String fileName, String resourceName) {
        Repository rep = Repository.getDefault();
        for (Enumeration en = rep.getFileSystems(); en.hasMoreElements(); ) {
            FileSystem fs = (FileSystem)en.nextElement();
            FileObject fo = findFileObjectForFile(fs, fileName);
            if ((fo != null) && resourceName.equals(fo.getPackageNameExt('/','.')))
                return fo;
        }
        return null;
    }

    /** Finds a fileobject for an absolute file name or null if not found */
    public static FileObject findFileObjectForFile(FileSystem fs, String fileName) {
        String canName = ""; // NOI18N
        String canName2 = ""; // NOI18N
        try {
            canName = new File(fileName).getCanonicalPath();
        }
        catch (IOException e) {
            return null;
        }
        String resFriendly = canName.replace(File.separatorChar, '/');
        int sepFound = -1;
        while (true) {
            sepFound = resFriendly.indexOf('/', sepFound + 1);
            if (sepFound == -1)
                return null;
            String resName = resFriendly.substring(sepFound);
            FileObject fo = fs.findResource(resName);
            if (fo == null) continue;
            File ff = NbClassPath.toFile(fo);
            if (ff == null) continue;
            // verify that they're the same
            try {
                canName2 = ff.getCanonicalPath();
            }
            catch (IOException e) {
                continue;
            }
            if (!(canName2.equals(canName)))
                continue;
            return fo;
        }
    }

    public static final String getFileObjectFileName(FileObject fo) throws FileStateInvalidException {
        File ff = NbClassPath.toFile(fo);
        if (ff == null)
            throw new FileStateInvalidException(NbBundle.getBundle(JspCompileUtil.class).getString("CTL_NotLocalFile"));
        return ff.getAbsolutePath();
    }

    /** Decides whether a given file is in the subtree defined by the given folder.
     * Similar to <code>org.openide.filesystems.FileUtil.isParentOf (FileObject folder, FileObject fo)</code>, 
     * but also accepts the case that <code>fo == folder</code>
     */
    public static boolean isInSubTree(FileObject folder, FileObject fo) {
        if (fo == folder) {
            return true;
        }
        else return FileUtil.isParentOf(folder, fo);
    }

    /** Finds a relative resource path between rootFolder and relativeObject.
     * @return relative path between rootFolder and relativeObject. The returned path
     * never starts with a '/'. It never ends with a '/'.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */
    public static String findRelativePath(FileObject rootFolder, FileObject relativeObject) {
        String rfp = rootFolder.getPath();
        String rop = relativeObject.getPath();
        // check that they share the start of the path
        if (!isInSubTree(rootFolder, relativeObject)) {
            throw new IllegalArgumentException("" + rootFolder + " / " + relativeObject); // NOI18N
        }
        // now really return the result
        String result = rop.substring(rfp.length());
        if (result.startsWith("/")) { // NOI18N
            result = result.substring(1);
        }
        return result;
    }
    
    /** Finds a relative context path between rootFolder and relativeObject.
     * Similar to <code>findRelativePath(FileObject, FileObject)</code>, only
     * different slash '/' conventions.
     * @return relative context path between rootFolder and relativeObject. The returned path
     * always starts with a '/'. It ends with a '/' if the relative object is a directory.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */
    public static String findRelativeContextPath(FileObject rootFolder, FileObject relativeObject) {
        String result = "/" + findRelativePath(rootFolder, relativeObject); // NOI18N
        return relativeObject.isFolder() ? (result + "/") : result; // NOI18N
    }
    
    /** Returns whether a given file is a JSP file, or possibly a JSP segment.
     * The recognition happens based on file extension (not on actual inclusion in other files).
     * @param fo the file to examine
     * @param acceptSegment whether segments should be accepted
     */
    public static boolean isJspFile(FileObject fo, boolean acceptSegment) {
        String ext = fo.getExt().toLowerCase();
        if ("jsp".equals(ext) || "jspx".equals(ext)) { // NOI18N
            return true;
        }
        if ("jspf".equals(ext) && acceptSegment) { // NOI18N
            return true;
        }
        return false;
    }
    
    /** Returns whether a given file is a tag file, or possibly a tag segment.
     * The recognition happens based on file extension (not on actual inclusion in other files).
     * @param fo the file to examine
     * @param acceptSegment whether segments should be accepted
     */
    public static boolean isTagFile(FileObject fo, boolean acceptSegment) {
        String ext = fo.getExt().toLowerCase();
        if ("tag".equals(ext) || "tagx".equals(ext)) { // NOI18N
            return true;
        }
        if ("tagf".equals(ext) && acceptSegment) { // NOI18N
            return true;
        }
        return false;
    }
    
    public static final String getContextPath(FileObject fo) {
        return "/" + fo.getPackageNameExt('/','.'); // NOI18N
    }

    /** Returns an absolute context URL (starting with '/') for a relative URL and base URL.
    *  @param relativeTo url to which the relative URL is related. Treated as directory iff
    *    ends with '/'
    *  @param url the relative URL by RFC 2396
    *  @exception IllegalArgumentException if url is not absolute and relativeTo 
    * can not be related to, or if url is intended to be a directory
    */
    public static String resolveRelativeURL(String relativeTo, String url) {
        //System.out.println("- resolving " + url + " relative to " + relativeTo);
        String result;
        if (url.startsWith("/")) { // NOI18N
            result = "/"; // NOI18N
            url = url.substring(1);
        }
        else {
            // canonize relativeTo
            if ((relativeTo == null) || (!relativeTo.startsWith("/"))) // NOI18N
                throw new IllegalArgumentException();
            relativeTo = resolveRelativeURL(null, relativeTo);
            int lastSlash = relativeTo.lastIndexOf('/');
            if (lastSlash == -1)
                throw new IllegalArgumentException();
            result = relativeTo.substring(0, lastSlash + 1);
        }

        // now url does not start with '/' and result starts with '/' and ends with '/'
        StringTokenizer st = new StringTokenizer(url, "/", true); // NOI18N
        while(st.hasMoreTokens()) {
            String tok = st.nextToken();
            //System.out.println("token : \"" + tok + "\""); // NOI18N
            if (tok.equals("/")) { // NOI18N
                if (!result.endsWith("/")) // NOI18N
                    result = result + "/"; // NOI18N
            }
            else
                if (tok.equals("")) // NOI18N
                    ;  // do nohing
                else
                    if (tok.equals(".")) // NOI18N
                        ;  // do nohing
                    else
                        if (tok.equals("..")) { // NOI18N
                            String withoutSlash = result.substring(0, result.length() - 1);
                            int ls = withoutSlash.lastIndexOf("/"); // NOI18N
                            if (ls != -1)
                                result = withoutSlash.substring(0, ls + 1);
                        }
                        else {
                            // some file
                            result = result + tok;
                        }
            //System.out.println("result : " + result); // NOI18N
        }
        //System.out.println("- resolved to " + result);
        return result;
    }

    /** Returns a FileObject whose resource path is relativePath relatively to folder rootFolder. 
     *  Null if does not exist.
     */
    public static FileObject findRelativeResource(FileObject rootFolder, String relativePath) throws FileStateInvalidException {
        if (!rootFolder.isFolder())
            throw new IllegalArgumentException();
        String rootPath = rootFolder.getPackageName('/');
        if (!(rootPath.endsWith("/"))) // NOI18N
            rootPath = rootPath + "/"; // NOI18N
        if (relativePath.startsWith("/")) // NOI18N
            relativePath = relativePath.substring(1);
        return rootFolder.getFileSystem().findResource(rootPath + relativePath);
    }
    

    public static FileSystem mountJarIfNotMounted(String jarFileName) throws IOException {
        //if(libs.containsKey(jarFileName)) return (FileSystem) libs.get(jarFileName);
        org.openide.filesystems.JarFileSystem jfs = new org.openide.filesystems.JarFileSystem();
        File f = new File(jarFileName);
        try {
            jfs.setJarFile(f);
        } catch (java.beans.PropertyVetoException jbp) {}
        FileSystem fs = mountFS(jfs);
        //libs.put(jarFileName, fs);
        return fs;
    }

    private static FileSystem mountFS(FileSystem fs) throws IOException {
        // compare with the other filesystems
        Enumeration en = Repository.getDefault().getFileSystems();
        for (;en.hasMoreElements(); ) {
            FileSystem f = (FileSystem)en.nextElement();
            if (f.getSystemName().equals(fs.getSystemName()))
                return f;
        }
        
        // add to the repository
        fs.setHidden(true);
        Repository.getDefault().addFileSystem(fs);
        return fs;
    }

}

