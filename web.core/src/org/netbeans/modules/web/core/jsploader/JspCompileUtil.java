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

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** JSP compilation utilities
*
* @author Petr Jiricka
*/
public class JspCompileUtil {
    
    /** Finds a relative context path between rootFolder and relativeObject.
     * Similar to <code>FileUtil.getRelativePath(FileObject, FileObject)</code>, only
     * different slash '/' conventions.
     * @return relative context path between rootFolder and relativeObject. The returned path
     * always starts with a '/'. It ends with a '/' if the relative object is a directory.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */
    public static String findRelativeContextPath(FileObject rootFolder, FileObject relativeObject) {
        String result = "/" + FileUtil.getRelativePath(rootFolder, relativeObject); // NOI18N
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
    
}

