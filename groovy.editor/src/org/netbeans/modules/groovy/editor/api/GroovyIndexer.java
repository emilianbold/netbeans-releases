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
package org.netbeans.modules.groovy.editor.api;

import groovyjarjarasm.asm.Opcodes;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.StructureAnalyzer.AnalysisResult;
import org.netbeans.modules.groovy.editor.api.elements.AstClassElement;
import org.netbeans.modules.groovy.editor.api.elements.AstElement;
import org.netbeans.modules.groovy.editor.api.elements.IndexedElement;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.codehaus.groovy.ast.FieldNode;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.elements.AstFieldElement;
import org.netbeans.modules.groovy.editor.api.elements.AstMethodElement;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;

/**
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
public class GroovyIndexer extends EmbeddingIndexer {

    // class
    static final String FQN_NAME = "fqn"; //NOI18N
    static final String CLASS_NAME = "class"; //NOI18N
    static final String CASE_INSENSITIVE_CLASS_NAME = "class-ig"; //NOI18N
    // not indexed
    static final String IN = "in"; //NOI18N
    /** Attributes: hh;nnnn where hh is a hex representing flags in IndexedClass, and nnnn is the documentation length */
    static final String CLASS_ATTRS = "attrs"; //NOI18N

    // method
    static final String METHOD_NAME = "method"; //NOI18N

    // field
    static final String FIELD_NAME = "field"; //NOI18N

    /** Attributes: "i" -> private, "o" -> protected, ", "s" - static/notinstance, "d" - documented */
    //static final String ATTRIBUTE_NAME = "attribute"; //NOI18N

    private static FileObject preindexedDb;

    // some statistics about the indexer

    private static long indexerRunTime = 0;
    private static long indexerFirstRun = 0;
    private static long filesIndexed = 0;

    private static final Logger LOG = Logger.getLogger(GroovyIndexer.class.getName());

    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        long indexerThisStartTime = System.currentTimeMillis();

        if (indexerFirstRun == 0) {
            indexerFirstRun = indexerThisStartTime;
        }

        GroovyParserResult r = (GroovyParserResult) AstUtilities.getParseResult(parserResult);
        ASTNode root = AstUtilities.getRoot(r);

        if (root == null) {
            return;
        }

        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return;
        }

        TreeAnalyzer analyzer = new TreeAnalyzer(r, support, indexable);
        analyzer.analyze();

        for(IndexDocument doc : analyzer.getDocuments()) {
            support.addDocument(doc);
        }

        filesIndexed++;
        long indexerThisStopTime = System.currentTimeMillis();
        long indexerThisRunTime = indexerThisStopTime - indexerThisStartTime;
        indexerRunTime += indexerThisRunTime;

        LOG.log(Level.FINEST, "Indexed File                : {0}", r.getSnapshot().getSource().getFileObject());
        LOG.log(Level.FINEST, "Indexing time (ms)          : {0}", indexerThisRunTime);

        LOG.log(Level.FINEST, "Number of files indexed     : {0}", filesIndexed);
        LOG.log(Level.FINEST, "Time spend indexing (ms)    : {0}", indexerRunTime);
        LOG.log(Level.FINEST, "Avg indexing time/file (ms) : {0}", indexerRunTime/filesIndexed);
        LOG.log(Level.FINEST, "Time betw. 1st and Last idx : {0}", indexerThisStopTime - indexerFirstRun);
        LOG.log(Level.FINEST, "---------------------------------------------------------------------------------");
    }
    
// FIXME parsing API
//    public String getPersistentUrl(File file) {
//        String url;
//        try {
//            url = file.toURI().toURL().toExternalForm();
//            // Make relative URLs for urls in the libraries
//            return GroovyIndex.getPreindexUrl(url);
//        } catch (MalformedURLException ex) {
//            Exceptions.printStackTrace(ex);
//            return file.getPath();
//        }
//    }

    public FileObject getPreindexedDb() {
        // no preindexed libraries for now
//        if (preindexedDb == null) {
//            File preindexed = InstalledFileLocator.getDefault().locate(
//                    "preindexed-groovy", "org.netbeans.modules.groovy.editor", false); // NOI18N
//            if (preindexed == null || !preindexed.isDirectory()) {
//                throw new RuntimeException("Can't locate preindexed directory. Installation might be damaged"); // NOI18N
//            }
//            preindexedDb = FileUtil.toFileObject(preindexed);
//        }
        return preindexedDb;
    }

// FIXME parsing API
//    public boolean acceptQueryPath(String url) {
//        // LOG.log(Level.FINEST, "acceptQueryPath : {0}", url);
//        return url.indexOf("/target/") == -1 && url.indexOf("/art/") == -1; // NOI18N
//    }

    public static final class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "groovy"; // NOI18N
        public static final int VERSION = 8;

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(indexable, snapshot)) {
                return new GroovyIndexer();
            } else {
                return null;
            }
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        private boolean isIndexable(Indexable indexable, Snapshot snapshot) {
            String extension = snapshot.getSource().getFileObject().getExt();

            if (extension.equals("groovy")) { // NOI18N
                return true;
            }
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport support = IndexingSupport.getInstance(context);
                for (Indexable indexable : deleted) {
                    support.removeDocuments(indexable);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> removedRoots) {

        }
        
        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : dirty) {
                    is.markDirtyDocuments(i);
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }
    }
    
    private static class TreeAnalyzer {

        private final FileObject file;
        private final IndexingSupport support;
        private final Indexable indexable;
        private final GroovyParserResult result;
        private final List<IndexDocument> documents = new ArrayList<IndexDocument>();

        private String url;
        private BaseDocument doc;
        
        private TreeAnalyzer(GroovyParserResult result, IndexingSupport support, Indexable indexable) {
            this.result = result;
            this.file = result.getSnapshot().getSource().getFileObject();
            this.support = support;
            this.indexable = indexable;
        }

        List<IndexDocument> getDocuments() {
            return documents;
        }

        public void analyze() {
            this.doc = LexUtilities.getDocument(result, true);

            try {
                url = file.getURL().toExternalForm();

                // Make relative URLs for urls in the libraries
                url = GroovyIndex.getPreindexUrl(url);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            AnalysisResult ar = result.getStructure();
            List<?extends AstElement> children = ar.getElements();

            if ((children == null) || (children.size() == 0)) {
                return;
            }

            for (AstElement child : children) {
                switch (child.getKind()) {
                    case CLASS:
                        analyzeClass((AstClassElement) child);
                        break;
                }
            }

        }

        private void analyzeClass(AstClassElement element) {
            IndexDocument document = support.createDocument(indexable);
            documents.add(document);
            indexClass(element, document);

            for (AstElement child : element.getChildren()) {
                switch (child.getKind()) {
                    case METHOD:
                        indexMethod((AstMethodElement) child, document);
                        break;
                    case FIELD:
                        indexField((AstFieldElement) child, document);
                        break;
                }
            }
        }

        private void indexClass(AstClassElement element, IndexDocument document) {
            final String name = element.getName();
            document.addPair(FQN_NAME, element.getFqn(), true, true);
            document.addPair(CLASS_NAME, name, true, true);
            document.addPair(CASE_INSENSITIVE_CLASS_NAME, name.toLowerCase(), true, true);
        }

        private void indexField(AstFieldElement child, IndexDocument document) {

            StringBuilder sb = new StringBuilder(child.getName());
            FieldNode node = (FieldNode) child.getNode();

            sb.append(';').append(org.netbeans.modules.groovy.editor.java.Utilities.translateClassLoaderTypeName(
                    node.getType().getName()));

            int flags = getFieldModifiersFlag(child.getModifiers());
            if (flags != 0 || child.isProperty()) {
                sb.append(';');
                sb.append(IndexedElement.flagToFirstChar(flags));
                sb.append(IndexedElement.flagToSecondChar(flags));
            }

            if (child.isProperty()) {
                sb.append(';');
                sb.append(child.isProperty());
            }

            // TODO - gather documentation on fields? naeh
            document.addPair(FIELD_NAME, sb.toString(), true, true);
        }

        private void indexMethod(AstMethodElement child, IndexDocument document) {

            MethodNode childNode = (MethodNode) child.getNode();
            StringBuilder sb = new StringBuilder(AstUtilities.getDefSignature(childNode));

            sb.append(';').append(org.netbeans.modules.groovy.editor.java.Utilities.translateClassLoaderTypeName(
                    childNode.getReturnType().getName()));

            Set<Modifier> modifiers = child.getModifiers();

            int flags = getMethodModifiersFlag(modifiers);

            if (flags != 0) {
                sb.append(';');
                sb.append(IndexedElement.flagToFirstChar(flags));
                sb.append(IndexedElement.flagToSecondChar(flags));
            }

            document.addPair(METHOD_NAME, sb.toString(), true, true);
        }

    }

    // note that default field modifier is private
    private static int getFieldModifiersFlag(Set<Modifier> modifiers) {
        int flags = modifiers.contains(Modifier.STATIC) ? Opcodes.ACC_STATIC : 0;
        if (modifiers.contains(Modifier.PUBLIC)) {
            flags |= Opcodes.ACC_PUBLIC;
        } else if (modifiers.contains(Modifier.PROTECTED)) {
            flags |= Opcodes.ACC_PROTECTED;
        }

        return flags;
    }

    // note that default method (and class) modifier is public
    private static int getMethodModifiersFlag(Set<Modifier> modifiers) {
        int flags = modifiers.contains(Modifier.STATIC) ? Opcodes.ACC_STATIC : 0;
        if (modifiers.contains(Modifier.PRIVATE)) {
            flags |= Opcodes.ACC_PRIVATE;
        } else if (modifiers.contains(Modifier.PROTECTED)) {
            flags |= Opcodes.ACC_PROTECTED;
        }

        return flags;
    }

}
