/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.common.api;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author marekfukala
 */
public abstract class FileReferenceCompletion<T> implements ValueCompletion<T> {

    private static final ImageIcon PACKAGE_ICON =
            ImageUtilities.loadImageIcon("org/openide/loaders/defaultFolder.gif", false); // NOI18N

    public abstract T createFileItem(int anchor, String name, Color color, ImageIcon icon);

    public abstract T createGoUpItem(int anchor, Color color, ImageIcon icon);

    @Override
    public List<T> getItems(FileObject orig, int offset, String valuePart) {
        List<T> result = new ArrayList<T>();

        String path = "";   // NOI18N
        String fileNamePart = valuePart;
        int lastSlash = valuePart.lastIndexOf('/');
        if (lastSlash == 0) {
            path = "/"; // NOI18N
            fileNamePart = valuePart.substring(1);
        } else if (lastSlash > 0) { // not a leading slash?
            path = valuePart.substring(0, lastSlash);
            fileNamePart = (lastSlash == valuePart.length()) ? "" : valuePart.substring(lastSlash + 1);    // NOI18N
        }

        int anchor = offset - valuePart.length() + lastSlash + 1;  // works even with -1

        try {
            Project project = FileOwnerQuery.getOwner(orig);
            FileObject documentBase = project != null ? project.getProjectDirectory() : orig;

//                FileObject documentBase = JspUtils.guessWebModuleRoot(orig);
            // need to normalize fileNamePart with respect to orig
            String ctxPath = resolveRelativeURL("/" + orig.getPath(), path);  // NOI18N
            //is this absolute path?
            if (path.startsWith("/")) {
                ctxPath = documentBase.getPath() + path;
            } else {
                ctxPath = ctxPath.substring(1);
            }

            FileSystem fs = orig.getFileSystem();

            FileObject folder = fs.findResource(ctxPath);
            if (folder != null) {
                //add all accessible files from current context
                result.addAll(files(anchor, folder, fileNamePart));

                //add go up in the directories structure item
                if (!folder.equals(documentBase) && !path.startsWith("/") // NOI18N
                        && (path.length() == 0 || (path.lastIndexOf("../") + 3 == path.length()))) { // NOI18N
//                        result.add(HtmlCompletionItem.createGoUpFileCompletionItem(anchor, java.awt.Color.BLUE, PACKAGE_ICON)); // NOI18N
                    result.add(createGoUpItem(anchor, Color.BLUE, PACKAGE_ICON)); // NOI18N
                }
            }
        } catch (FileStateInvalidException ex) {
            // unreachable FS - disable completion
        } catch (IllegalArgumentException ex) {
            // resolving failed
        }

        return result;
    }

    private List<T> files(int offset, FileObject folder, String prefix) {
        List<T> res = new ArrayList<T>();
        TreeMap<String, T> resFolders = new TreeMap<String, T>();
        TreeMap<String, T> resFiles = new TreeMap<String, T>();

        Enumeration<? extends FileObject> files = folder.getChildren(false);
        while (files.hasMoreElements()) {
            FileObject file = files.nextElement();
            String fname = file.getNameExt();
            if (fname.startsWith(prefix) && !"cvs".equalsIgnoreCase(fname)) {

                if (file.isFolder()) {
                    resFolders.put(file.getNameExt(), createFileItem(offset, file.getNameExt() + "/", java.awt.Color.BLUE, PACKAGE_ICON));
                } else {
                    java.awt.Image icon = getIcon(file);
                    if (icon != null) {
                        resFiles.put(file.getNameExt(), createFileItem(offset, file.getNameExt(), java.awt.Color.BLACK, new javax.swing.ImageIcon(icon)));
                    } else {
                        resFiles.put(file.getNameExt(), createFileItem(offset, file.getNameExt(), java.awt.Color.BLACK, null));
                    }
                }
            }
        }

        res.addAll(resFolders.values());
        res.addAll(resFiles.values());

        return res;
    }

    /** Returns an absolute context URL (starting with '/') for a relative URL and base URL.
     *  @param relativeTo url to which the relative URL is related. Treated as directory iff
     *    ends with '/'
     *  @param url the relative URL by RFC 2396
     *  @exception IllegalArgumentException if url is not absolute and relativeTo
     * can not be related to, or if url is intended to be a directory
     */
    private static String resolveRelativeURL(String relativeTo, String url) {
        //System.out.println("- resolving " + url + " relative to " + relativeTo);
        String result;
        if (url.startsWith("/")) { // NOI18N
            result = "/"; // NOI18N
            url = url.substring(1);
        } else {
            // canonize relativeTo
            if ((relativeTo == null) || (!relativeTo.startsWith("/"))) // NOI18N
            {
                throw new IllegalArgumentException();
            }
            relativeTo = resolveRelativeURL(null, relativeTo);
            int lastSlash = relativeTo.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new IllegalArgumentException();
            }
            result = relativeTo.substring(0, lastSlash + 1);
        }

        // now url does not start with '/' and result starts with '/' and ends with '/'
        StringTokenizer st = new StringTokenizer(url, "/", true); // NOI18N
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            //System.out.println("token : \"" + tok + "\""); // NOI18N
            if (tok.equals("/")) { // NOI18N
                if (!result.endsWith("/")) // NOI18N
                {
                    result = result + "/"; // NOI18N
                }
            } else if (tok.equals("")) // NOI18N
            ; // do nohing
            else if (tok.equals(".")) // NOI18N
            ; // do nohing
            else if (tok.equals("..")) { // NOI18N
                String withoutSlash = result.substring(0, result.length() - 1);
                int ls = withoutSlash.lastIndexOf("/"); // NOI18N
                if (ls != -1) {
                    result = withoutSlash.substring(0, ls + 1);
                }
            } else {
                // some file
                result = result + tok;
            }
            //System.out.println("result : " + result); // NOI18N
        }
        //System.out.println("- resolved to " + result);
        return result;
    }

    /** This method returns an image, which is displayed for the FileObject in the explorer.
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file.
     */
    private static java.awt.Image getIcon(FileObject fo) {
        try {
            return DataObject.find(fo).getNodeDelegate().getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        } catch (DataObjectNotFoundException e) {
            Logger.getLogger(FileReferenceCompletion.class.getName()).log(Level.INFO, "Cannot find icon for " + fo.getNameExt(), e);
        }
        return null;
    }
}
