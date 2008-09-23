/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.gsfret.source.usages;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.napi.gsfret.source.ParserTaskImpl;
import org.netbeans.modules.gsf.Language;
import org.openide.filesystems.FileObject;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Tomas Zezula
 */
public class SourceAnalyser implements IndexDocumentFactory {    
    
    private final Index index;
    private final Map<String, List<String>> references;
    //private final Set<String> toDelete;
    
    /** Creates a new instance of SourceAnalyser */
    public SourceAnalyser (final Index index) {
        assert index != null;
        this.index = index;
        this.references = new HashMap<String, List<String>> ();
        //this.toDelete = new HashSet<String> ();
    }
    
    public final boolean isUpToDate(String resourceName, long resourceMTime) throws IOException {
        return this.index.isUpToDate(resourceName, resourceMTime);
    }
    
    public void store () throws IOException {
    }

    public boolean isValid () throws IOException {
        return this.index.isValid(true);
    }

    public void analyse(Language language, final Iterable<ParserResult> data) throws IOException {
        // I should stash some shit into this.references here such that I can try storing them later, for example
        // all the class names I can find. This would be a good place to look for the desired language...
        // Of course, I can have multiple, so I have to index these by the language type, right? Otherwise
        // how do I choose which one to ask?
        // Actually, do it once per filetype - it's cheap compared to all the other crap I do per file anyway
        Indexer indexer = language.getIndexer();
        if (indexer != null) {
            for (ParserResult result : data) {
                String fileUrl = indexer.getPersistentUrl(result.getFile().getFile());
                List<IndexDocument> documents = indexer.index(result, this);
                // Null means delete this document from the index which is different than
                // a document with no indexable information
                if (documents == null) {
                    documents = Collections.emptyList();
                }
                index.store(fileUrl, documents);
            }
        }
    }
    
    void analyseUnitAndStore (Indexer indexer, ParserResult result) throws IOException {
        String fileUrl = indexer.getPersistentUrl(result.getFile().getFile());
        FileObject fo = result.getFile().getFileObject();
        List<IndexDocument> documents = null;
        if (fo != null && fo.isValid()) {
            // http://www.netbeans.org/issues/show_bug.cgi?id=145386
            documents = indexer.index(result, this);
            if (documents == null) {
                // Null means delete this document from the index which is different than
                // a document with no indexable information
                documents = Collections.emptyList();
            }
        }
        index.store(fileUrl, documents);
    }
    
    public void delete (final ParserFile parserFile, Language language) throws IOException {
        if (!this.index.isValid(false)) {
            return;
        }
        //this.toDelete.add(className);

        Indexer indexer = language.getIndexer();
        if (indexer != null && indexer.isIndexable(parserFile)) {
            String fileUrl = indexer.getPersistentUrl(parserFile.getFile());
            index.store(fileUrl, null);
        }
    }

    Map<String,String> getTimeStamps() throws IOException {
        return index.getTimeStamps();
    }
    
    public IndexDocument createDocument(int initialPairs) {
        return new IndexDocumentImpl(initialPairs);
    }

    public IndexDocument createDocument(int initialPairs, String overrideUrl) {
        return new IndexDocumentImpl(initialPairs, overrideUrl);
    }
    
    @Override
    public String toString() {
        return "SourceAnalyzer(" + this.index.toString().substring(this.index.toString().indexOf("@")+1) + ")";
    }
    
    boolean hasData() {
        try {
            return !index.isValid(false);
        } catch (IOException ioe) {
            return true;
        }
    }

    void batchStore(List<IndexBatchEntry> list, boolean create) throws IOException {
        index.batchStore(list, create);
    }
}
