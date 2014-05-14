/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.requirejs.editor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.javascript2.requirejs.editor.index.RequireJsIndex;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Pisl
 */
public class FSCompletionUtils {
    final static String GO_UP = "../"; //NOI18N
    
    public static List<? extends CompletionProposal> computeRelativeItems(
            Collection<? extends FileObject> relativeTo,
            final String prefix,
            int anchor,
            FileObjectFilter filter) throws IOException {
        
        assert relativeTo != null;

        List<CompletionProposal> result = new LinkedList();

        int lastSlash = prefix.lastIndexOf('/');
        String pathPrefix;
        String filePrefix;

        if (lastSlash != (-1)) {
            pathPrefix = prefix.substring(0, lastSlash);
            filePrefix = prefix.substring(lastSlash + 1);
        } else {
            pathPrefix = null;
            filePrefix = prefix;
        }

        Set<FileObject> directories = new HashSet();
        File prefixFile = null;
        if (pathPrefix != null && !pathPrefix.startsWith(".")) { //NOI18N
            if (pathPrefix.length() == 0 && prefix.startsWith("/")) {
                prefixFile = new File("/"); //NOI18N
            } else {
                prefixFile = new File(pathPrefix);
            }
        }
        if (prefixFile != null && prefixFile.exists()) {
            //absolute path
            File normalizeFile = FileUtil.normalizeFile(prefixFile);
            FileObject fo = FileUtil.toFileObject(normalizeFile);
            if (fo != null) {
                directories.add(fo);
            }
        } else {
            //relative path
            for (FileObject f : relativeTo) {
                if (pathPrefix != null) {
                    File toFile = FileUtil.toFile(f);
                    if (toFile != null) {
                        URI resolve = Utilities.toURI(toFile).resolve(pathPrefix).normalize();
                        File normalizedFile = FileUtil.normalizeFile(Utilities.toFile(resolve));
                        f = FileUtil.toFileObject(normalizedFile);
                    } else {
                        f = f.getFileObject(pathPrefix);
                    }
                }

                if (f != null) {
                    directories.add(f);
                }
            }
        }

        for (FileObject dir : directories) {
            FileObject[] children = dir.getChildren();

            for (int cntr = 0; cntr < children.length; cntr++) {
                FileObject current = children[cntr];

                if (VisibilityQuery.getDefault().isVisible(current) && current.getNameExt().toLowerCase().startsWith(filePrefix.toLowerCase()) && filter.accept(current)) {
                    int newAnchor = pathPrefix == null ? 
                            anchor - prefix.length() : anchor - (prefix.length() - pathPrefix.length() - 2);
                    result.add(new FSCompletionItem(current, pathPrefix != null ? pathPrefix + "/" : "./", newAnchor)); //NOI18N
                }
            }
        }
//        if (GO_UP.startsWith(filePrefix) && directories.size() == 1) {
//            FileObject parent = directories.iterator().next();
//            if (parent.getParent() != null && VisibilityQuery.getDefault().isVisible(parent.getParent()) && filter.accept(parent.getParent())) {
//                if (!parent.isFolder()) {
//                    parent = parent.getParent();
//                }
//                result.add(new FSCompletionItem(parent, "", anchor) {
//                    
//                    @Override
//                    protected String getText() {
//                        return (!prefix.equals("..") && !prefix.equals(".") ? prefix : "") + GO_UP; //NOI18N
//                    }
//                });
//            }
//        }

        return result;
    }
    
    public static class JSIncludesFilter implements FileObjectFilter {
        private FileObject currentFile;

        public JSIncludesFilter(FileObject currentFile) {
            this.currentFile = currentFile;
        }

        @Override
        public boolean accept(FileObject file) {
            if (file.equals(currentFile) || isNbProjectMetadata(file)) {
                return false; //do not include self in the cc result
            }

            if (file.isFolder()) {
                return true;
            }

            String mimeType = FileUtil.getMIMEType(file);

            return mimeType != null && mimeType.startsWith("text/");
        }

        private static boolean isNbProjectMetadata(FileObject fo) {
            final String metadataName = "nbproject"; //NOI18N
            if (fo.getPath().indexOf(metadataName) != -1) {
                while (fo != null) {
                    if (fo.isFolder()) {
                        if (metadataName.equals(fo.getNameExt())) {
                            return true;
                        }
                    }
                    fo = fo.getParent();
                }
            }
            return false;
        }
    }
    interface FileObjectFilter {

        boolean accept(FileObject file);

    }
    
    /**
     * Returns corresponding file, if it's found for the specific path
     * @param path
     * @param info
     * @return 
     */
    public static FileObject findFileObject(final String pathToFile, FileObject parent) {
        String path = pathToFile;
        String[] pathParts = path.split("/");
        if (parent != null) {
            Project project = FileOwnerQuery.getOwner(parent);
            RequireJsIndex rIndex = null;
            try {
                rIndex = RequireJsIndex.get(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            if (rIndex != null) {
                Map<String, String> pathMappings = rIndex.getPathMappings(pathParts[0]);
                String alias = "";
                for (String possibleAlias : pathMappings.keySet()) {
                    if (possibleAlias.equals(path)) {
                        alias = possibleAlias;
                        break;
                    }
                    if (path.startsWith(possibleAlias) && (alias.length() < possibleAlias.length())) {
                        alias = possibleAlias;
                    }
                }
                if (!alias.isEmpty()) {
                    path = pathMappings.get(alias) + path.substring(alias.length());
                    pathParts = path.split("/");                        //NOI18N
                }
            }
        }
        if (parent != null && pathParts.length > 0) {
            parent = parent.getParent();
            FileObject targetFO;
            while (parent != null) {
                targetFO = parent.getFileObject(path);
                if (targetFO != null) {
                    return targetFO;
                }
                targetFO = parent.getFileObject(path + ".js");
                if (targetFO != null) {
                    return targetFO;
                }
                parent = parent.getParent();
            }
        }
        return null;
    }
}
