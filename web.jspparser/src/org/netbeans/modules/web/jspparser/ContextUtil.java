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

package org.netbeans.modules.web.jspparser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/** Static utilities related to web context stuff - relative paths, relative objects etc.
 *
 * @author Petr Jiricka
 */
public class ContextUtil {
    
    /** Returns a message for a given throwable. Optionally includes the throwable
     * stack trace in the message.
     * @param throwable throwable for which to construct the message
     * @param includeStackTrace whether to include a stack trace of the throwable in the message
     * @return an appropriate message for the throwable
     */
    public static String getThrowableMessage(Throwable throwable,
            boolean includeStackTrace) {
        if (includeStackTrace) {
            StringWriter swriter = new StringWriter();
            PrintWriter pw = new PrintWriter(swriter);
            throwable.printStackTrace(pw);
            pw.close();
            return swriter.toString();
        }
        else {
            return throwable.getMessage();
        }
    }
    
    /**********************************
     * Copied over from WebModuleUtils.
     **********************************
     */
    
    /** Finds a relative resource path between rootFolder and relativeObject. 
     * @return relative path between rootFolder and relativeObject. The returned path
     * never starts with a '/'. It never ends with a '/'.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */ 
    public static String findRelativePath(FileObject rootFolder, FileObject relativeObject) {
        String rfp = rootFolder.getPath();
        String rop = relativeObject.getPath();
        // check that they share the start of the path 
        if (!FileUtil.isParentOf (rootFolder, relativeObject)) {
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
    
    /** Finds a FileObject relative to a given root folder, with a given relative path. 
     * @param rootFolder the root folder
     * @relativePath the relative path (not starting with a '/', delimited by '/')
     * @return fileobject relative to the given root folder or null if not found.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */ 
    public static FileObject findRelativeFileObject(FileObject rootFolder, String relativePath) {
        if (relativePath.startsWith("/")) {  // NOI18N
            relativePath = relativePath.substring(1);
        }
        FileObject myObj = rootFolder;
        StringTokenizer st = new StringTokenizer(relativePath, "/"); // NOI18N
        while (myObj != null && st.hasMoreTokens()) {
            myObj = myObj.getFileObject(st.nextToken());
        }
        return myObj;
    }
}
