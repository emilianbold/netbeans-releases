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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.mozilla.javascript.Node;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;
import org.netbeans.modules.javascript.editing.JsAnalyzer.AnalysisResult;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * Index Ruby structure into the persistent store for retrieval by
 * {@link JsIndex}.
 * 
 * @todo Index methods as func.in and then distinguish between exact completion and multi-completion.
 * @todo Ensure that all the stub files are compileable!
 * @todo Should I perhaps store globals and functions using the same query prefix (since I typically
 *    have to search for both anyway) ? Or perhaps not - not when doing inherited checks...
 * @todo Index file inclusion dependencies! (Uh oh - that means I -do- have to do models for HTML, etc. right?
 *     Or can I perhaps only compute that stuff live?
 * @todo Use the JsCommentLexer to pull out relevant attributes -- @private and such -- and set these
 *     as function attributes.
 * @todo There are duplicate elements -- why???
 * 
 * @author Tor Norbye
 */
public class JsIndexer implements Indexer {
    static final boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing");
    
    // I need to be able to search several things:
    // (1) by function root name, e.g. quickly all functions that start
    //    with "f" should find unknown.foo.
    // (2) by namespace, e.g. I should be able to quickly find all
    //    "foo.bar.b*" functions
    // (3) constructors
    // (4) global variables, preferably in the same way
    // (5) extends so I can do inheritance inclusion!

    // Solution: Store the following:
    // class:name for each class
    // extend:old:new for each inheritance? Or perhaps do this in the class entry
    // fqn: f.q.n.function/global;sig; for each function
    // base: function;fqn;sig
    // The signature should look like this:
    // ;flags;;args;offset;docoffset;browsercompat;types;
    // (between flags and args you have the case sensitive name for flags)

    static final String FIELD_FQN = "fqn"; //NOI18N
    static final String FIELD_BASE = "base"; //NOI18N
    static final String FIELD_EXTEND = "extend"; //NOI18N
    static final String FIELD_CLASS = "clz"; //NOI18N
    
    public boolean isIndexable(ParserFile file) {
        if (JsMimeResolver.isJavaScriptExt(file.getExtension()) ||
                file.getExtension().equals("rhtml") ||  file.getExtension().equals("html")) { // NOI18N

            // Skip Gem versions; Rails copies these files into the project anyway! Don't want
            // duplicate entries.
            if (PREINDEXING) {
                try {
                    //if (file.getRelativePath().startsWith("action_view")) {
                    if (file.getFileObject().getURL().toExternalForm().indexOf("/gems/") != -1) {
                        return false;
                    }
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return true;
        }
        
        return false;
    }

    public String getPersistentUrl(File file) {
        String url;
        try {
            url = file.toURI().toURL().toExternalForm();
            // Make relative URLs for urls in the libraries
            return JsIndex.getPreindexUrl(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return file.getPath();
        }

    }

    public List<IndexDocument> index(ParserResult result, IndexDocumentFactory factory) throws IOException {
        JsParseResult r = (JsParseResult)result;
        Node root = r.getRootNode();

        if (root == null) {
            return null;
        }

        TreeAnalyzer analyzer = new TreeAnalyzer(r, factory);
        analyzer.analyze();
        
        return analyzer.getDocuments();
    }
    
    public String getIndexVersion() {
        return "6.111"; // NOI18N
    }

    public String getIndexerName() {
        return "javascript"; // NOI18N
    }
    
    private static class TreeAnalyzer {
        private final ParserFile file;
        private String url;
        private final JsParseResult result;
        private final BaseDocument doc;
        private IndexDocumentFactory factory;
        private List<IndexDocument> documents = new ArrayList<IndexDocument>();
        
        private TreeAnalyzer(JsParseResult result, IndexDocumentFactory factory) {
            this.result = result;
            this.file = result.getFile();
            this.factory = factory;

            FileObject fo = file.getFileObject();

            if (fo != null) {
                this.doc = NbUtilities.getBaseDocument(fo, true);
            } else {
                this.doc = null;
            }

            try {
                url = file.getFileObject().getURL().toExternalForm();

                // Make relative URLs for urls in the libraries
                url = JsIndex.getPreindexUrl(url);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        List<IndexDocument> getDocuments() {
            return documents;
        }

        public void analyze() throws IOException {
            AnalysisResult ar = result.getStructure();
            List<?extends AstElement> children = ar.getElements();

            if ((children == null) || (children.size() == 0)) {
                return;
            }

            IndexDocument document = factory.createDocument(40); // TODO - measure!
            documents.add(document);

            // Add the fields, etc.. Recursively add the children classes or modules if any
            for (AstElement child : children) {
                ElementKind childKind = child.getKind();
                if (childKind == ElementKind.CONSTRUCTOR || childKind == ElementKind.METHOD) {
                    String signature = computeSignature(child);
                    indexFuncOrProperty(child, document, signature);
                    String name = child.getName();
                    if (Character.isUpperCase(name.charAt(0))) {
                        indexClass(child, document, signature);
                    }
                } else if (childKind == ElementKind.GLOBAL || childKind == ElementKind.PROPERTY) {
                    indexFuncOrProperty(child, document, computeSignature(child));
                } else {
                    assert false : childKind;
                }
                // XXX what about fields, constants, attributes?
                
                assert child.getChildren().size() == 0;
            }

            Map<String,String> classExtends = ar.getExtendsMap();
            if (classExtends != null) {
                for (Map.Entry<String,String> entry : classExtends.entrySet()) {
                    String clz = entry.getKey();
                    String superClz = entry.getValue();
                    document.addPair(FIELD_EXTEND, clz.toLowerCase() + ";" + clz + ";" + superClz, true); // NOI18N
                }
            }
        }

        private void indexClass(AstElement element, IndexDocument document, String signature) {
            final String name = element.getName();
            document.addPair(FIELD_CLASS, name+ ";" + signature, true);
        }

        private String computeSignature(AstElement element) {
            // Look up compatibility
            int index = IndexedElement.FLAG_INDEX;
            
            int docOffset = getDocumentationOffset(element);
            
            String compatibility = "";
            if (file.getNameExt().startsWith("stub_")) {
                int astOffset = element.getNode().getSourceStart();
                int lexOffset = astOffset;
                TranslatedSource source = result.getTranslatedSource();
                if (source != null) {
                    lexOffset = source.getLexicalOffset(astOffset);
                }
                try {
                    String line = doc.getText(lexOffset,
                            Utilities.getRowEnd(doc, lexOffset)-lexOffset);
                    int compatIdx = line.indexOf("COMPAT=");
                    if (compatIdx != -1) {
                        compatIdx += "COMPAT=".length();
                        EnumSet<BrowserVersion> es = BrowserVersion.fromFlags(line.substring(compatIdx));
                        compatibility = BrowserVersion.toCompactFlags(es);
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            assert index == IndexedElement.FLAG_INDEX;
            StringBuilder sb = new StringBuilder();
            int flags = IndexedElement.getFlags(element);
            if (docOffset != -1) {
                flags = flags | IndexedElement.DOCUMENTED;
            }
            sb.append(IndexedElement.encode(flags));
            
            // Parameters
            sb.append(";");
            index++;
            assert index == IndexedElement.ARG_INDEX;
            if (element instanceof FunctionAstElement) {
                FunctionAstElement func = (FunctionAstElement)element;            

                int argIndex = 0;
                for (String param : func.getParameters()) {
                    if (argIndex == 0 && "$super".equals(param)) { // NOI18N
                        // Prototype inserts these as the first param to handle inheritance/super
                        argIndex++;
                        continue;
                    } 
                    if (argIndex > 0) {
                        sb.append(",");
                    }
                    sb.append(param);
                    argIndex++;
                }
            }

            // Node offset
            sb.append(';');
            index++;
            assert index == IndexedElement.NODE_INDEX;
            sb.append("0");
            //sb.append(IndexedElement.encode(element.getNode().getSourceStart()));
            
            // Documentation offset
            sb.append(';');
            index++;
            assert index == IndexedElement.DOC_INDEX;
            if (docOffset != -1) {
                sb.append(IndexedElement.encode(docOffset));
            }

            // Browser compatibility
            sb.append(";");
            index++;
            assert index == IndexedElement.BROWSER_INDEX;
            sb.append(compatibility);
            
            // Types
            sb.append(";");
            index++;
            assert index == IndexedElement.TYPE_INDEX;
            if (element.getKind() == ElementKind.GLOBAL) {
                String type = ((GlobalAstElement)element).getType();
                if (type != null) {
                    sb.append(type);
                }
            }
            // TBD

            sb.append(';');
            String signature = sb.toString();
            return signature;
        }

        private void indexFuncOrProperty(AstElement element, IndexDocument document, String signature) {
            String in = element.getIn();
            String name = element.getName();
            StringBuilder base = new StringBuilder();
            base.append(name.toLowerCase());
            base.append(';');                
            if (in != null) {
                base.append(in);
            }
            base.append(';');
            base.append(name);
            base.append(';');
            base.append(signature);
            document.addPair(FIELD_BASE, base.toString(), true);
            
            StringBuilder fqn = new StringBuilder();
            if (in != null && in.length() > 0) {
                fqn.append(in.toLowerCase());
                fqn.append('.');
            }
            fqn.append(name.toLowerCase());
            fqn.append(';');
            fqn.append(';');
            if (in != null && in.length() > 0) {
                fqn.append(in);
                fqn.append('.');
            }
            fqn.append(name);
            fqn.append(';');
            fqn.append(signature);
            document.addPair(FIELD_FQN, fqn.toString(), true);
        }
        
        private int getDocumentationOffset(AstElement element) {
            int offset = element.getNode().getSourceStart();
            try {
                if (offset > doc.getLength()) {
                    return -1;
                }
                offset = Utilities.getRowStart(doc, offset);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            OffsetRange range = LexUtilities.getCommentBlock(doc, offset, true);
            if (range != OffsetRange.NONE) {
                return range.getStart();
            } else {
                return -1;
            }
        }
    }
    
    public File getPreindexedData() {
        return null;
    }

    private static FileObject preindexedDb;

    /** For testing only */
    public static void setPreindexedDb(FileObject preindexedDb) {
        JsIndexer.preindexedDb = preindexedDb;
    }
    
    public FileObject getPreindexedDb() {
        if (preindexedDb == null) {
            File preindexed = InstalledFileLocator.getDefault().locate(
                    "preindexed-javascript", "org.netbeans.modules.javascript.editing", false); // NOI18N
            if (preindexed == null || !preindexed.isDirectory()) {
                throw new RuntimeException("Can't locate preindexed directory. Installation might be damaged"); // NOI18N
            }
            preindexedDb = FileUtil.toFileObject(preindexed);
        }
        return preindexedDb;
    }
}
