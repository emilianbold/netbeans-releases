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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.includes;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.loaders.ExtensionList;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmIncludeCompletionQuery {
    private Collection<CsmIncludeCompletionItem> results;
    private final CsmFile file;
    public CsmIncludeCompletionQuery(CsmFile file) {
        this.file = file;
    }
    
    public Collection<CsmIncludeCompletionItem> query( BaseDocument doc, String childSubDir, int substitutionOffset, Boolean usrInclude, boolean showAll) {
        results = new ArrayList<CsmIncludeCompletionItem>(100);
        CsmFile docFile = this.file;
        if (docFile == null) {
            docFile = CsmUtilities.getCsmFile(doc, false);
        }
        String usrFilePath;
        Collection<String> usrPaths = Collections.<String>emptyList();
        Collection<String> sysPaths = Collections.<String>emptyList();
        if (childSubDir.startsWith(CsmIncludeCompletionItem.SLASH)) {
            // special handling for absolute paths...
            addFolderItems(CsmIncludeCompletionItem.SLASH, 
                    CsmIncludeCompletionItem.SLASH, 
                    childSubDir, true, (usrInclude != null ? usrInclude : false), 
                    true, substitutionOffset);
            return results;
        }
        if (docFile != null) {
            usrFilePath = docFile.getAbsolutePath();
            usrPaths = getFileIncludes(docFile, false);
            sysPaths = getFileIncludes(docFile, true);
        } else {
            File baseFile = CsmUtilities.getFile(doc);
            usrFilePath = baseFile.getAbsolutePath();
        }
        File usrDir = new File(usrFilePath).getParentFile();
        if (usrInclude == null || usrInclude == Boolean.TRUE) {
            addFolderItems(usrDir.getAbsolutePath(), ".", childSubDir, false, false, true, substitutionOffset);
            if (showAll) {
                for (String usrPath : usrPaths) {
                    addFolderItems(usrPath, usrPath, childSubDir, false, false, false, substitutionOffset);
                }
                for (String sysPath : sysPaths) {
                    addFolderItems(sysPath, sysPath, childSubDir, false, true, false, substitutionOffset);
                }
            }
            addParentFolder(substitutionOffset, childSubDir, true);
        } else {
            for (String sysPath : sysPaths) {
                addFolderItems(sysPath, sysPath, childSubDir, false, true, false, substitutionOffset);
            }
            if (showAll) {
                for (String usrPath : usrPaths) {
                    addFolderItems(usrPath, usrPath, childSubDir, false, false, false, substitutionOffset);
                }
                addFolderItems(usrDir.getAbsolutePath(), ".", childSubDir, false, false, true, substitutionOffset);
                addParentFolder(substitutionOffset, childSubDir, false);
            }
        }
        return results;
    }
    
    private void addFolderItems(String parentFolder, String parentFolderPresentation,
            String childSubDir, boolean highPriority, boolean system, boolean filtered, int substitutionOffset) {
        File dir = new File (parentFolder, childSubDir);
        if (dir != null && dir.exists()) {
            File[] list = filtered ?  dir.listFiles(new MyFileFilter(HDataLoader.getInstance().getExtensions())) :
                                    dir.listFiles();
            String relFileName;
            for (File curFile : list) {
                relFileName = curFile.getName();
                CsmIncludeCompletionItem item = CsmIncludeCompletionItem.createItem(
                        substitutionOffset, relFileName, parentFolderPresentation, childSubDir,
                        system, highPriority, curFile.isDirectory(), true);
                results.add(item);
            }
        }        
    }

    private void addParentFolder(int substitutionOffset, String childSubDir, boolean system) {
        CsmIncludeCompletionItem item = CsmIncludeCompletionItem.createItem(
                substitutionOffset, "..", ".", childSubDir, system, false, true, false); // NOI18N
        results.add(item);
    }
    
    private static final class MyFileFilter implements FileFilter {
        private final ExtensionList exts;

        protected MyFileFilter(ExtensionList exts) {
            this.exts = exts;
        }
        
        public boolean accept(File dir, String name) {
            return exts.isRegistered(name);
        }

        public boolean accept(File pathname) {
            return exts.isRegistered(pathname.getName()) || pathname.isDirectory();
        }
        
    }

    private Collection<String> getFileIncludes(CsmFile file, boolean system) {
        if (system) {
            return CsmFileInfoQuery.getDefault().getSystemIncludePaths(file);
        } else {
            return CsmFileInfoQuery.getDefault().getUserIncludePaths(file);
        }
    }
}
