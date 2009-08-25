/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.completion;

import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.html.editor.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.ImageUtilities;

/**
 *
 * @author marekfukala
 */
public abstract class AttrValuesCompletion {

    private static final Map<String, Map<String, AttrValuesCompletion>> SUPPORTS = new HashMap<String, Map<String, AttrValuesCompletion>>();
    private static final AttrValuesCompletion FILE_NAME_SUPPORT = new FilenameSupport();

    static {
        //TODO uff, such long list ... redo it so it resolves according to the DTD attribute automatically
        putSupport("a", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("area", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("link", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("base", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("script", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("img", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("img", "longdesc", FILE_NAME_SUPPORT); //NOI18N
        putSupport("img", "usemap", FILE_NAME_SUPPORT); //NOI18N
        putSupport("input", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("frame", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("iframe", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("body", "background", FILE_NAME_SUPPORT); //NOI18N
        putSupport("input", "usemap", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "classid", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "codebase", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "data", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "usemap", FILE_NAME_SUPPORT); //NOI18N
        putSupport("applet", "codebase", FILE_NAME_SUPPORT); //NOI18N
        putSupport("q", "cite", FILE_NAME_SUPPORT); //NOI18N
        putSupport("blackquote", "cite", FILE_NAME_SUPPORT); //NOI18N
        putSupport("ins", "cite", FILE_NAME_SUPPORT); //NOI18N
        putSupport("del", "cite", FILE_NAME_SUPPORT); //NOI18N
        putSupport("form", "action", FILE_NAME_SUPPORT); //NOI18N
    }

    private static void putSupport(String tag, String attr, AttrValuesCompletion support) {
        Map<String, AttrValuesCompletion> map = SUPPORTS.get(tag);
        if (map == null) {
            map = new HashMap<String, AttrValuesCompletion>();
            SUPPORTS.put(tag, map);
        }
        map.put(attr, support);
    }

    public static AttrValuesCompletion getSupport(String tag, String attr) {
        Map<String, AttrValuesCompletion> map = SUPPORTS.get(tag.toLowerCase(Locale.ENGLISH));
        if(map == null) {
            return null;
        } else {
            return map.get(attr.toLowerCase(Locale.ENGLISH));
        }
    }

    public abstract List<HtmlCompletionItem> getValueCompletionItems(Document doc, int offset, String valuePart);

    public static class FilenameSupport extends AttrValuesCompletion {

        static final ImageIcon PACKAGE_ICON =
                ImageUtilities.loadImageIcon("org/openide/loaders/defaultFolder.gif", false); // NOI18N

        public List<HtmlCompletionItem> getValueCompletionItems(Document doc, int offset, String valuePart) {
            List<HtmlCompletionItem> result = new ArrayList<HtmlCompletionItem>();

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
                FileObject orig = DataLoadersBridge.getDefault().getFileObject(doc);
                Project project = FileOwnerQuery.getOwner(orig);
                FileObject documentBase = project != null ? project.getProjectDirectory() : orig;

//                FileObject documentBase = JspUtils.guessWebModuleRoot(orig);
                // need to normalize fileNamePart with respect to orig
                String ctxPath = Utils.resolveRelativeURL("/" + orig.getPath(), path);  // NOI18N
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
                        result.add(HtmlCompletionItem.createGoUpFileCompletionItem(anchor, java.awt.Color.BLUE, PACKAGE_ICON)); // NOI18N
                    }
                }
            } catch (FileStateInvalidException ex) {
                // unreachable FS - disable completion
            } catch (IllegalArgumentException ex) {
                // resolving failed
            }

            return result;
        }

        private List<HtmlCompletionItem> files(int offset, FileObject folder, String prefix) {
            List<HtmlCompletionItem> res = new ArrayList<HtmlCompletionItem>();
            TreeMap<String, HtmlCompletionItem> resFolders = new TreeMap<String, HtmlCompletionItem>();
            TreeMap<String, HtmlCompletionItem> resFiles = new TreeMap<String, HtmlCompletionItem>();

            Enumeration<? extends FileObject> files = folder.getChildren(false);
            while (files.hasMoreElements()) {
                FileObject file = files.nextElement();
                String fname = file.getNameExt();
                if (fname.startsWith(prefix) && !"cvs".equalsIgnoreCase(fname)) {

                    if (file.isFolder()) {
                        resFolders.put(file.getNameExt(), HtmlCompletionItem.createFileCompletionItem(file.getNameExt() + "/", offset, java.awt.Color.BLUE, PACKAGE_ICON));
                    } else {
                        java.awt.Image icon = Utils.getIcon(file);
                        if (icon != null) {
                            resFiles.put(file.getNameExt(), HtmlCompletionItem.createFileCompletionItem(file.getNameExt(), offset, java.awt.Color.BLACK, new javax.swing.ImageIcon(icon)));
                        } else {
                            resFiles.put(file.getNameExt(), HtmlCompletionItem.createFileCompletionItem(file.getNameExt(), offset, java.awt.Color.BLACK, null));
                        }
                    }
                }
            }
            
            res.addAll(resFolders.values());
            res.addAll(resFiles.values());

            return res;
        }
    }
}
