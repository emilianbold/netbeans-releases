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

