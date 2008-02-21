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
import javax.swing.text.BadLocationException;
import org.mozilla.javascript.Node;
import org.netbeans.fpi.gsf.ElementKind;
import org.netbeans.fpi.gsf.Indexer;
import org.netbeans.fpi.gsf.OffsetRange;
import org.netbeans.fpi.gsf.ParserFile;
import org.netbeans.fpi.gsf.ParserResult;
import org.netbeans.fpi.gsf.TranslatedSource;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.fpi.gsf.IndexDocument;
import org.netbeans.fpi.gsf.IndexDocumentFactory;
import org.netbeans.modules.javascript.editing.JsAnalyzer.AnalysisResult;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
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
    
    static final String FIELD_FQN_NAME = "fqn"; //NOI18N
//    static final String FIELD_FUNCTION = "func"; //NOI18N
    static final String FIELD_JS_FUNCTION = "func"; //NOI18N
    static final String FIELD_JS_GLOBAL = "global"; //NOI18N
    static final String FIELD_CLASS_NAME = "class"; //NOI18N
    static final String FIELD_CASE_INSENSITIVE_CLASS_NAME = "class-ig"; //NOI18N
    static final String FIELD_IN = "in"; //NOI18N
    
    public boolean isIndexable(ParserFile file) {
        return JsMimeResolver.isJavaScriptExt(file.getExtension()) ||
                file.getExtension().equals("rhtml") ||  file.getExtension().equals("html");
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
        return "6.102"; // NOI18N
    }

    public String getIndexerName() {
        return "javascript"; // NOI18N
    }
    
    private static class TreeAnalyzer {
        private final ParserFile file;
        private String url;
        //private String requires;
        private final JsParseResult result;
        private final BaseDocument doc;
        private int docMode;
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
            String fileName = file.getNameExt();
//            // DB migration?
//            if (Character.isDigit(fileName.charAt(0)) && fileName.matches("^\\d\\d\\d_.*")) { // NOI18N
//                FileObject fo = file.getFileObject();
//                if (fo != null && fo.getParent() != null && fo.getParent().getName().equals("migrate")) { // NOI18N
//                    handleMigration();
//                    // Don't exit here - proceed to also index the class as Ruby code
//                }
//            } else if ("schema.rb".equals(fileName)) { //NOI18N
//                FileObject fo = file.getFileObject();
//                if (fo != null && fo.getParent() != null && fo.getParent().getName().equals("db")) { // NOI18N
//                    handleMigration();
//                    // Don't exit here - proceed to also index the class as Ruby code
//                }
//            }
            
            //Node root = result.getRootNode();

            // Compute the requires for this file first such that
            // each class or module recorded in the index for this
            // file can reference their includes
            AnalysisResult ar = result.getStructure();
          //  requires = getRequireString(ar.getRequires());
            List<?extends AstElement> structure = ar.getElements();

            if ((structure == null) || (structure.size() == 0)) {
                return;
            }

            analyze(structure);
        }

        private void analyze(List<?extends AstElement> children) {
            IndexDocument document = factory.createDocument(40); // TODO - measure!
            documents.add(document);

//            String name = "Object";
//            String in = null;
//            String fqn = "Object";

            int flags = 0;
//            notIndexed.put(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags));
//            indexed.put(FIELD_FQN_NAME, fqn);
//            indexed.put(FIELD_CASE_INSENSITIVE_CLASS_NAME, name.toLowerCase());
//            indexed.put(FIELD_CLASS_NAME, name);
//            addRequire(indexed);
//            if (requires != null) {
//                notIndexed.put(FIELD_REQUIRES, requires);
//            }

            // TODO - find a way to combine all these methods (from this file) into a single item
            
            // Add the fields, etc.. Recursively add the children classes or modules if any
            for (AstElement child : children) {
                if (child.getKind() == ElementKind.CONSTRUCTOR || child.getKind() == ElementKind.METHOD) {
                    indexMethod(child, document, true, false);
                } else if (child.getKind() == ElementKind.GLOBAL) {
                    String global = child.getName();
                    indexGlobal(global, document);
                } else {
                    assert false : child.getKind();
                }
                // XXX what about fields, constants, attributes?
                
                assert child.getChildren().size() == 0;
            }
        }
        
        private void indexMethod(AstElement child, IndexDocument document, boolean topLevel, boolean nodoc) {
//            MethodDefNode childNode = (MethodDefNode)child.getNode();
//            String signature = AstUtilities.getDefSignature(childNode);
//            FunctionNode childNode = (FunctionNode)child.getNode();
            FunctionAstElement func = (FunctionAstElement)child;            
//            String signature = func.getSignature();
//            Set<Modifier> modifiers = child.getModifiers();
            
//            int flags = getModifiersFlag(modifiers);
            int flags = 0;
//
//            if (nodoc) {
//                flags |= IndexedElement.NODOC;
//            }
//
//            if (topLevel) {
//                flags |= IndexedElement.TOPLEVEL;
//            }
//
//            boolean methodIsDocumented = isDocumented(childNode);
//            if (methodIsDocumented) {
//                flags |= IndexedElement.DOCUMENTED;
//            }
//
//            if (flags != 0) {
//                StringBuilder sb = new StringBuilder(signature);
//                sb.append(';');
//                sb.append(IndexedElement.flagToFirstChar(flags));
//                sb.append(IndexedElement.flagToSecondChar(flags));
//                signature = sb.toString();
//            }
            
//            if (file.isPlatform() || PREINDEXING) {
//                Node root = AstUtilities.getRoot(result);
//                signature = RubyIndexerHelper.getMethodSignature(child, root, 
//                       flags, signature, file.getFileObject(), doc);
//                if (signature == null) {
//                    return;
//                }
//            }

            int docOffset = getDocumentationOffset(func);
//            if (docOffset != -1) {
//                signature = signature + ":" + Integer.toString(docOffset);
//            }
//            
//            ru.put(FIELD_FUNCTION, signature);
            

            // Look up compatibility
            String compatibility = "";
            if (file.getNameExt().startsWith("stub_")) {
                int astOffset = func.getNode().getSourceStart();
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

            StringBuilder sb = new StringBuilder();
            sb.append(func.getName());
            sb.append(';');
            if (func.getIn() != null) {
                sb.append(func.getIn());
            }
            sb.append(';');
            int argIndex = 0;
            for (String param : func.getParameters()) {
                if (argIndex > 0) {
                    sb.append(",");
                }
                sb.append(param);
                argIndex++;
            }
            sb.append(";");
            if (docOffset != -1) {
                sb.append(Integer.toString(docOffset));
            }
            sb.append(";");
            sb.append(compatibility);
            sb.append(";");
            
            document.addPair(FIELD_JS_FUNCTION, sb.toString(), true);
            
            // Storing a lowercase method name is kinda pointless in
            // Ruby because the convention is to use all lowercase characters
            // (using _ to separate words rather than camel case) so we're
            // bloating the database for very little practical use here...
            //ru.put(FIELD_CASE_INSENSITIVE_METHOD_NAME, name.toLowerCase());

//            if (child.getName().equals("initialize")) {
//                // Create static method alias "new"; rdoc also seems to do this
//                Map<String, String> ru2;
//                ru2 = new HashMap<String, String>();
//                indexedList.add(ru2);
//
//                // Change signature
//                // TODO - don't do this for methods annotated :notnew: 
//                signature = signature.replaceFirst("initialize", "new"); // NOI18N
//                                                                         // Make it static
//
//                if ((flags & IndexedElement.STATIC) == 0) {
//                    // Add in static flag
//                    flags |= IndexedElement.STATIC;
//                    char first = IndexedElement.flagToFirstChar(flags);
//                    char second = IndexedElement.flagToSecondChar(flags);
//                    int attributeIndex = signature.indexOf(';');
//                    if (attributeIndex == -1) {
//                        signature = ((signature+ ";") + first) + second;
//                    } else {
//                        signature = (signature.substring(0, attributeIndex+1) + first) + second + signature.substring(attributeIndex+3);
//                    }
//                }
//                ru2.put(FIELD_METHOD_NAME, signature);
//            }
        }

        private void indexGlobal(String name, IndexDocument document) {
            document.addPair(FIELD_JS_GLOBAL, name, true);
        }
        
        private int getDocumentationOffset(FunctionAstElement func) {
            int offset = func.getNode().getSourceStart();
            try {
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

//        private void indexAttribute(AstElement child, Set<Map<String, String>> indexedList,
//            Set<Map<String, String>> notIndexedList, boolean nodoc) {
//            Map<String, String> ru;
//            ru = new HashMap<String, String>();
//            indexedList.add(ru);
//
//            
//            String attribute = child.getName();
//
//            boolean isDocumented = isDocumented(child.getNode());
//
//            int flags = isDocumented ? IndexedMethod.DOCUMENTED : 0;
//            if (nodoc) {
//                flags |= IndexedElement.NODOC;
//            }
//
//            char first = IndexedElement.flagToFirstChar(flags);
//            char second = IndexedElement.flagToSecondChar(flags);
//            
//            if (isDocumented) {
//                attribute = attribute + (";" + first) + second;
//            }
//            
//            ru.put(FIELD_ATTRIBUTE_NAME, attribute);
//        }
//
//        private void indexConstant(AstElement child, Set<Map<String, String>> indexedList,
//            Set<Map<String, String>> notIndexedList, boolean nodoc) {
//            Map<String, String> ru;
//            ru = new HashMap<String, String>();
//            indexedList.add(ru);
//
//            int flags = 0; // TODO
//            if (nodoc) {
//                flags |= IndexedElement.NODOC;
//            }
//
//            // TODO - add the RHS on the right
//            ru.put(FIELD_CONSTANT_NAME, child.getName());
//        }
//
//        private void indexField(AstElement child, Set<Map<String, String>> indexedList,
//            Set<Map<String, String>> notIndexedList, boolean nodoc) {
//            Map<String, String> ru;
//            ru = new HashMap<String, String>();
//            indexedList.add(ru);
//
//            String signature = child.getName();
//            int flags = getModifiersFlag(child.getModifiers());
//            if (nodoc) {
//                flags |= IndexedElement.NODOC;
//            }
//
//            if (flags != 0) {
//                StringBuilder sb = new StringBuilder(signature);
//                sb.append(';');
//                sb.append(IndexedElement.flagToFirstChar(flags));
//                sb.append(IndexedElement.flagToSecondChar(flags));
//                signature = sb.toString();
//            }
//
//            // TODO - gather documentation on fields? naeh
//            ru.put(FIELD_FIELD_NAME, signature);
//        }

//        private int getDocumentSize(Node node) {
//            if (doc != null) {
//                List<String> comments = AstUtilities.gatherDocumentation(null, doc, node);
//
//                if ((comments != null) && (comments.size() > 0)) {
//                    int size = 0;
//
//                    for (String line : comments) {
//                        size += line.length();
//                    }
//
//                    return size;
//                }
//            }
//
//            return 0;
//        }
//
//        private boolean isDocumented(Node node) {
//            if (doc != null) {
//                List<String> comments = AstUtilities.gatherDocumentation(null, doc, node);
//
//                if ((comments != null) && (comments.size() > 0)) {
//                    return true;
//                }
//            }
//
//            return false;
//        }
//
//        private void addRequire(Map<String, String> ru) {
//            // Don't generate "require" clauses for anything in generated ruby;
//            // these classes are all built in and do not require any includes
//            // (besides, the file names are bogus - they are just derived from
//            // the class name by the stub generator)
//            FileObject fo = file.getFileObject();
//            String folder = (fo.getParent() != null) && fo.getParent().getParent() != null ?
//                fo.getParent().getParent().getNameExt() : "";
//
//            if (folder.equals("rubystubs") && fo.getName().startsWith("stub_")) {
//                return;
//            }
//
//            // Index for require-completion
//            String relative = file.getRelativePath();
//
//            if (relative != null) {
//                if (relative.endsWith(".rb")) { // NOI18N
//                    relative = relative.substring(0, relative.length() - 3);
//                    ru.put(FIELD_REQUIRE, relative);
//                }
//            }
//        }
    }
    
    private void applyBrowserCompatibility() {
        // See DOM: http://www.quirksmode.org/dom/w3c_core.html
        // Events:  http://www.quirksmode.org/js/events_compinfo.html
        // HTML: http://www.quirksmode.org/dom/w3c_html.html
        // Range: http://www.quirksmode.org/dom/w3c_range.html
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
