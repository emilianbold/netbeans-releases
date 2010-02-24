/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.includes;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ExtensionList;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmIncludeCompletionQuery {

    private static final Collection<String> EXCLUDED_DIR_NAMES = Arrays.asList(new String[]{
                "CVS", ".hg", "nbproject", "SCCS", "SunWS_cache"}); // NOI18N
    private Map<String, CsmIncludeCompletionItem> results;
    private final CsmFile file;

    public CsmIncludeCompletionQuery(CsmFile file) {
        this.file = file;
    }

    public Collection<CsmIncludeCompletionItem> query(BaseDocument doc, String childSubDir, int substitutionOffset, Boolean usrInclude, boolean showAll) {
        results = new HashMap<String, CsmIncludeCompletionItem>(100);
        CsmFile docFile = this.file;
        if (docFile == null) {
            docFile = CsmUtilities.getCsmFile(doc, false, false);
        }
        String usrFilePath;
        Collection<String> usrPaths = Collections.<String>emptyList();
        Collection<String> sysPaths = Collections.<String>emptyList();
        File fileChildSubDir = new File(childSubDir);
        if (fileChildSubDir.isAbsolute()) {
            // special handling for absolute paths...
            addFolderItems("",
                    "",
                    childSubDir, true, (usrInclude != null ? usrInclude : false),
                    true, substitutionOffset);
            return results.values();
        }
        if (docFile != null) {
            usrFilePath = docFile.getAbsolutePath().toString();
            usrPaths = getFileIncludes(docFile, false);
            sysPaths = getFileIncludes(docFile, true);
        } else {
            File baseFile = CsmUtilities.getFile(doc);
            if (baseFile == null) {
                // IZ#123039: NPE
                return Collections.<CsmIncludeCompletionItem>emptyList();
            }
            usrFilePath = baseFile.getAbsolutePath();
        }
        File usrDir = new File(usrFilePath).getParentFile();
        if (usrInclude == null || usrInclude == Boolean.TRUE) {
            addFolderItems(usrDir.getAbsolutePath(), ".", childSubDir, false, false, true, substitutionOffset); // NOI18N
            if (showAll) {
                for (String usrPath : usrPaths) {
                    addFolderItems(usrPath, usrPath, childSubDir, false, false, true, substitutionOffset);
                }
                for (String sysPath : sysPaths) {
                    addFolderItems(sysPath, sysPath, childSubDir, false, true, false, substitutionOffset);
                }
            }
            if (usrDir.getParentFile() != null) {
                addParentFolder(substitutionOffset, childSubDir, false);
            }
        } else {
            for (String sysPath : sysPaths) {
                addFolderItems(sysPath, sysPath, childSubDir, false, true, false, substitutionOffset);
            }
            if (showAll) {
                for (String usrPath : usrPaths) {
                    addFolderItems(usrPath, usrPath, childSubDir, false, false, true, substitutionOffset);
                }
                addFolderItems(usrDir.getAbsolutePath(), ".", childSubDir, false, false, true, substitutionOffset); // NOI18N
                if (usrDir.getParentFile() != null) {
                    addParentFolder(substitutionOffset, childSubDir, true);
                }
            }
        }
        return results.values();
    }

    private void addFolderItems(String parentFolder, String parentFolderPresentation,
            String childSubDir, boolean highPriority, boolean system, boolean filtered, int substitutionOffset) {
        File dir = new File(parentFolder, childSubDir);
        if (dir != null && dir.exists()) {
            File[] list = filtered ? dir.listFiles(new HeadersFileFilter()) : dir.listFiles(new DefFileFilter());
            if (list != null) {
                String relFileName;
                for (File curFile : list) {
                    relFileName = curFile.getName();
                    CsmIncludeCompletionItem item = CsmIncludeCompletionItem.createItem(
                            substitutionOffset, relFileName, parentFolderPresentation, childSubDir,
                            system, highPriority, curFile.isDirectory(), true);
                    if (!results.containsKey(relFileName)) {
                        results.put(relFileName, item);
                    }
                }
            }
        }
    }

    private void addParentFolder(int substitutionOffset, String childSubDir, boolean system) {
        // IZ#128044: Completion in #include should switch to 2-nd mode if there are no files in the list
        // doesn't append ".." item for empty lists
        if (!results.isEmpty()) {
            CsmIncludeCompletionItem item = CsmIncludeCompletionItem.createItem(
                    substitutionOffset, "..", ".", childSubDir, system, false, true, false); // NOI18N
            results.put("..", item);//NOI18N
        }
    }

    private Collection<String> getFileIncludes(CsmFile file, boolean system) {
        if (system) {
            return CsmFileInfoQuery.getDefault().getSystemIncludePaths(file);
        } else {
            return CsmFileInfoQuery.getDefault().getUserIncludePaths(file);
        }
    }

    private static final class DefFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            return !specialFile(pathname);
        }
    }

    private static final class HeadersFileFilter implements FileFilter {

        private final ExtensionList exts;

        protected HeadersFileFilter() {
            exts = new ExtensionList();
            for (String ext : MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues()) {
                exts.addExtension(ext);
            }
        }

        public boolean accept(File pathname) {
            return !specialFile(pathname) &&
                    (exts.isRegistered(pathname.getName()) || pathname.isDirectory() || isHeaderFileWoExt(pathname));
        }
    }

    private static boolean isHeaderFileWoExt(File pathname) {
        if (FileUtil.getExtension(pathname.getName()).length() == 0) {
            return MIMENames.HEADER_MIME_TYPE.equals(MIMESupport.getFileMIMEType(pathname));
        }
        return false;
    }

    private static boolean specialFile(File file) {
        String name = file.getName();
        if (name.startsWith(".")) { // NOI18N
            return true;
        } else if (name.endsWith("~")) { // NOI18N
            return true;
        } else if (file.isDirectory()) {
            if (EXCLUDED_DIR_NAMES.contains(name)) {
                return true;
            }
        }
        return false;
    }
}
