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
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
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
    
    public Collection<CsmIncludeCompletionItem> query( BaseDocument doc,int substitutionOffset, Boolean usrInclude, boolean showAll) {
        results = new ArrayList<CsmIncludeCompletionItem>(100);
        CsmFile docFile = this.file;
        if (docFile == null) {
            docFile = CsmUtilities.getCsmFile(doc, false);
        }
        if (docFile != null) {
            File dir = new File (docFile.getAbsolutePath()).getParentFile();
            if (dir != null && dir.exists()) {
                File[] list = dir.listFiles(new MyFileFilter(HDataLoader.getInstance().getExtensions()));
                String relFileName;
                boolean quoted = usrInclude == null ? true : usrInclude;
                for (File curFile : list) {
                    relFileName = curFile.getName();
                    CsmIncludeCompletionItem item = CsmIncludeCompletionItem.createItem(substitutionOffset, relFileName, dir, quoted, true, curFile.isDirectory());
                    results.add(item);
                }
            }
        }
        return results;
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
}
