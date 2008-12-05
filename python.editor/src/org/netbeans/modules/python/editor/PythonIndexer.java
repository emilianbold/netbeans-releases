/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.python.editor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.Parser.Job;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.gsf.spi.DefaultParseListener;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.editor.elements.IndexedElement;
import org.netbeans.modules.python.editor.hints.Deprecations;
import org.netbeans.modules.python.editor.scopes.ScopeConstants;
import org.netbeans.modules.python.editor.scopes.ScopeInfo;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.netbeans.modules.python.editor.scopes.SymInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.exprType;

/**
 *
 * @todo Store information about all symbols exported by a module.
 *  I can use that to provide "unused import" help.
 * @todo Clean this stuff up: store data, functions, etc.
 * @todo Improve detection of builtins. Perhaps run from within Python,
 *   something like this:
>>> dir(__builtins__)
['ArithmeticError', 'AssertionError', 'AttributeError', 'BaseException', 'DeprecationWarning', 'EOFError', 'Ellipsis', 'EnvironmentError', 'Exception', 'False', 'FloatingPointError', 'FutureWarning', 'GeneratorExit', 'IOError', 'ImportError', 'ImportWarning', 'IndentationError', 'IndexError', 'KeyError', 'KeyboardInterrupt', 'LookupError', 'MemoryError', 'NameError', 'None', 'NotImplemented', 'NotImplementedError', 'OSError', 'OverflowError', 'PendingDeprecationWarning', 'ReferenceError', 'RuntimeError', 'RuntimeWarning', 'StandardError', 'StopIteration', 'SyntaxError', 'SyntaxWarning', 'SystemError', 'SystemExit', 'TabError', 'True', 'TypeError', 'UnboundLocalError', 'UnicodeDecodeError', 'UnicodeEncodeError', 'UnicodeError', 'UnicodeTranslateError', 'UnicodeWarning', 'UserWarning', 'ValueError', 'Warning', 'ZeroDivisionError', '_', '__debug__', '__doc__', '__import__', '__name__', 'abs', 'all', 'any', 'apply', 'basestring', 'bool', 'buffer', 'callable', 'chr', 'classmethod', 'cmp', 'coerce', 'compile', 'complex', 'copyright', 'credits', 'delattr', 'dict', 'dir', 'divmod', 'enumerate', 'eval', 'execfile', 'exit', 'file', 'filter', 'float', 'frozenset', 'getattr', 'globals', 'hasattr', 'hash', 'help', 'hex', 'id', 'input', 'int', 'intern', 'isinstance', 'issubclass', 'iter', 'len', 'license', 'list', 'locals', 'long', 'map', 'max', 'min', 'object', 'oct', 'open', 'ord', 'pow', 'property', 'quit', 'range', 'raw_input', 'reduce', 'reload', 'repr', 'reversed', 'round', 'set', 'setattr', 'slice', 'sorted', 'staticmethod', 'str', 'sum', 'super', 'tuple', 'type', 'unichr', 'unicode', 'vars', 'xrange', 'zip']
 *
 * My code for scanning for functions has to be smarter:
.. function:: ljust(s, width)
rjust(s, width)
center(s, width)
 * Here I need to pick up all 3 signatures!
 * @author Tor Norbye
 */
public class PythonIndexer implements Indexer {
    public static boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing"); // NOI18N
    public static final String FIELD_MEMBER = "member"; //NOI18N
    public static final String FIELD_MODULE_NAME = "module"; //NOI18N
    public static final String FIELD_MODULE_ATTR_NAME = "modattrs"; //NOI18N
    public static final String FIELD_CLASS_ATTR_NAME = "clzattrs"; //NOI18N
    public static final String FIELD_EXTENDS_NAME = "extends"; //NOI18N
    public static final String FIELD_ITEM = "item"; //NOI18N
    public static final String FIELD_IN = "in"; //NOI18N
    public static final String FIELD_CLASS_NAME = "class"; //NOI18N
    public static final String FIELD_CASE_INSENSITIVE_CLASS_NAME = "class-ig"; //NOI18N
    private FileObject prevParent;
    private boolean prevResult;

    public boolean isIndexable(ParserFile file) {
        String extension = file.getExtension();
        if ("py".equals(extension)) { // NOI18N

            // Skip "test" folders under lib... Lots of weird files there
            // and we don't want to pollute the index with them
            File parent = file.getFile().getParentFile();

            if (parent != null && parent.getName().equals("test")) { // NOI18N
                // Make sure it's really a lib folder, we want to include the
                // user's files

                // Avoid double-indexing files that have multiple versions - e.g. foo.js and foo-min.js
                // or foo.uncompressed
                FileObject fo = file.getFileObject();
                if (fo == null) {
                    return true;
                }
                FileObject parentFo = fo.getParent();
                if (prevParent == parentFo) {
                    return prevResult;
                }
                prevResult = true;
                prevParent = parentFo;
                PythonPlatformManager manager = PythonPlatformManager.getInstance();
                Platforms:
                for (String name : manager.getPlatformList()) {
                    PythonPlatform platform = manager.getPlatform(name);
                    if (platform != null) {
                        for (FileObject root : platform.getLibraryRoots()) {
                            if (FileUtil.isParentOf(root, parentFo)) {
                                prevResult = false;
                                break Platforms;
                            }
                        }
                    }
                }
            }

            return true;
        }

        if ("rst".equals(extension)) { // NOI18N
            // Index restructured text if it looks like it contains Python library
            // definitions
            return true;
        }

        if ("egg".equals(extension)) { // NOI18N
            return true;
        }

        return false;
    }

    public List<IndexDocument> index(ParserResult result, IndexDocumentFactory factory) throws IOException {
        PythonParserResult parseResult = (PythonParserResult)result;
        if (parseResult == null) {
            return Collections.emptyList();
        }
        String extension = result.getFile().getNameExt();

        if (extension.endsWith(".rst")) { // NOI18N
            return scanRst(result.getFile().getFileObject(), factory, null);
        } else if (extension.endsWith(".egg")) { // NOI18N
            return scanEgg(result, factory);
        } else {
            // Normal python file
            return new IndexTask(parseResult, factory).scan();
        }
    }

    public boolean acceptQueryPath(String url) {
        return true;
    }

    public String getPersistentUrl(File file) {
        String url;
        try {
            url = file.toURI().toURL().toExternalForm();

            // Make relative URLs for urls in the libraries
            return PythonIndex.getPreindexUrl(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return file.getPath();
        }
    }

    public String getIndexVersion() {
        return "0.115"; // NOI18N
    }

    public String getIndexerName() {
        return "python"; // NOI18N
    }

    public FileObject getPreindexedDb() {
        return null;
    }

    private static void appendFlags(StringBuilder sb, char c, SymInfo sym, int flags) {
        sb.append(';');
        sb.append(c);
        sb.append(';');

        if (sym.isPrivate()) {
            flags |= IndexedElement.PRIVATE;
        }
        if (c == 'c') {
            flags |= IndexedElement.CONSTRUCTOR;
        }

        sb.append(IndexedElement.encode(flags));
        sb.append(';');
    }
    private static final int DEFAULT_DOC_SIZE = 40; // TODO Measure

    private static class IndexTask {
        private PythonParserResult result;
        private ParserFile file;
        private IndexDocumentFactory factory;
        private List<IndexDocument> documents = new ArrayList<IndexDocument>();
        private String url;
        private String module;
        private SymbolTable symbolTable;
        private String overrideUrl;

        private IndexTask(PythonParserResult result, IndexDocumentFactory factory) {
            this.result = result;
            this.file = result.getFile();
            this.factory = factory;

            module = PythonUtils.getModuleName(null, file);
        //PythonTree root = PythonAstUtils.getRoot(result);
        //if (root instanceof Module) {
        //    Str moduleDoc = PythonAstUtils.getDocumentationNode(root);
        //    if (moduleDoc != null) {
        //        moduleAttributes = "d(" + moduleDoc.getCharStartIndex() + ")";
        //    }
        //}
        }

        private IndexTask(PythonParserResult result, IndexDocumentFactory factory, String overrideUrl) {
            this(result, factory);
            this.overrideUrl = overrideUrl;
        }

        public List<IndexDocument> scan() {
            FileObject fileObject = file.getFileObject();
            try {
                url = fileObject.getURL().toExternalForm();

                // Make relative URLs for urls in the libraries
                url = PythonIndex.getPreindexUrl(url);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            IndexDocument doc = createDocument();
            doc.addPair(FIELD_MODULE_NAME, module, true);

            String moduleAttrs = null;
            if (url.startsWith(PythonIndex.CLUSTER_URL) || url.startsWith(PythonIndex.PYTHONHOME_URL)) {
                moduleAttrs = "S"; // NOI18N
            } else if (PREINDEXING) {
                String prj = System.getProperty("gsf.preindexing.projectpath");
                if (prj != null && url.indexOf(prj) == -1) {
                    System.err.println("WARNING -- not marking url " + url + " from " + file + " as a system library!");
                }
            }
            if (Deprecations.isDeprecatedModule(module)) {
                if (moduleAttrs == null) {
                    moduleAttrs = "D"; // NOI18N
                } else {
                    moduleAttrs += "D"; // NOI18N
                }
            }
            if (moduleAttrs != null) {
                doc.addPair(FIELD_MODULE_ATTR_NAME, moduleAttrs, false); // NOI18N
            }

            PythonTree root = PythonAstUtils.getRoot(result);
            if (root == null) {
                return documents;
            }
            assert root instanceof Module;
            symbolTable = result.getSymbolTable();
            ScopeInfo scopeInfo = symbolTable.getScopeInfo(root);
            for (Map.Entry<String, SymInfo> entry : scopeInfo.tbl.entrySet()) {
                String name = entry.getKey();
                SymInfo sym = entry.getValue();

                if (sym.isClass()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'C', sym, 0);
                    doc.addPair(FIELD_ITEM, sig.toString(), true);

                    if (sym.node instanceof ClassDef) {
                        assert sym.node instanceof ClassDef : sym.node;
                        indexClass(name, sym, (ClassDef)sym.node);
                    } else {
                        // Could be a symbol defined both as a class and a function
                        // (conditionally) such as _Environ in minicompat.py,
                        // and another trigger in socket.py.
                    }
                } else if (sym.isFunction()) {
                    if (sym.node instanceof Name) {
                        assert false : "Unexpected non-function node, " + ((Name)sym.node).id + " - from symbol " + name + " in " + file + " with sym=" + sym;
                    }
                    assert sym.node instanceof FunctionDef : sym.node;
                    FunctionDef def = (FunctionDef)sym.node;
                    String sig = computeFunctionSig(name, def, sym);
                    doc.addPair(FIELD_ITEM, sig, true);
                } else if (sym.isImported()) {
                    if (!"*".equals(name)) { // NOI18N
                        StringBuilder sig = new StringBuilder();
                        sig.append(name);
                        appendFlags(sig, 'I', sym, 0);
                        doc.addPair(FIELD_ITEM, sig.toString(), true);
                    }
                } else if (sym.isGeneratorExp()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'G', sym, 0);
                    doc.addPair(FIELD_ITEM, sig.toString(), true);
                } else if (sym.isData()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'D', sym, 0);
                    doc.addPair(FIELD_ITEM, sig.toString(), true);
                } else {
                    // XXX what the heck is this??
                }
            }

            return documents;
        }

        private void indexClass(String className, SymInfo classSym, ClassDef clz) {
            IndexDocument classDocument = createDocument();
            classDocument.addPair(FIELD_IN, module, true);

            // Superclass
            if (clz.bases != null) {
                for (exprType base : clz.bases) {
                    String extendsName = PythonAstUtils.getExprName(base);
                    if (extendsName != null) {
                        classDocument.addPair(FIELD_EXTENDS_NAME, extendsName, true);
                    }
                }
            }

            classDocument.addPair(FIELD_CLASS_NAME, className, true);

            if (classSym.isPrivate()) {
                // TODO - store Documented, Deprecated, DocOnly, etc.
                classDocument.addPair(FIELD_CLASS_ATTR_NAME, IndexedElement.encode(IndexedElement.PRIVATE), false);
            }
            classDocument.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, className.toLowerCase(), true);

            //Str doc = PythonAstUtils.getDocumentationNode(clz);
            //if (doc != null) {
            //    StringBuilder sb = new StringBuilder();
            //    sb.append("d("); // NOI18N
            //    sb.append(doc.getCharStartIndex());
            //    sb.append(")"); // NOI18N
            //    classDocument.addPair(FIELD_CLASS_ATTRS, sb.toString(), false);
            //}

            ScopeInfo scopeInfo = symbolTable.getScopeInfo(clz);
            for (Map.Entry<String, SymInfo> entry : scopeInfo.tbl.entrySet()) {
                String name = entry.getKey();
                SymInfo sym = entry.getValue();

//                int flags = sym.flags;
//                assert !sym.isClass() : "found a class " + name + " of type " + sym.dumpFlags(scopeInfo) + " within class " + className + " in module " + module;
//                if (!(sym.isFunction() || sym.isMember() || sym.isData())) {
//                }
//                assert sym.isFunction() || sym.isMember() || sym.isData() : name + ";" + sym.toString();

                if (sym.isClass()) {
                    // Triggers in httplib _socket_close inside FakeSocket
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'C', sym, 0);
                    classDocument.addPair(FIELD_ITEM, sig.toString(), true);

                } else if (sym.isFunction() && sym.node instanceof FunctionDef) {
                    if (sym.node instanceof Name) {
                        assert false : "Unexpected non-function node, " + ((Name)sym.node).id + " - from symbol " + name + " in " + file + " with sym=" + sym;
                    }
                    FunctionDef def = (FunctionDef)sym.node;
                    String sig = computeFunctionSig(name, def, sym);
                    classDocument.addPair(FIELD_MEMBER, sig, true);
                } else if (sym.isData()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'D', sym, 0);
                    classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                } else if (sym.isMember()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'A', sym, 0);
                    classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                } else if (!sym.isBound()) {
                    continue;
                } else {
                    // XXX what the heck is this??
                    assert false : className + "::" + name + " : " + sym.dumpFlags(scopeInfo);
                }
            }

            if (scopeInfo.attributes.size() > 0) {
                for (Map.Entry<String, SymInfo> entry : scopeInfo.attributes.entrySet()) {
                    String name = entry.getKey();
                    SymInfo sym = entry.getValue();

                    if (sym.isClass()) {
                        // Triggers in httplib _socket_close inside FakeSocket
                        StringBuilder sig = new StringBuilder();
                        sig.append(name);
                        appendFlags(sig, 'C', sym, 0);
                        classDocument.addPair(FIELD_ITEM, sig.toString(), true);

                    } else if (sym.isFunction() && sym.node instanceof FunctionDef) {
                        if (sym.node instanceof Name) {
                            assert false : "Unexpected non-function node, " + ((Name)sym.node).id + " - from symbol " + name + " in " + file + " with sym=" + sym;
                        }
                        FunctionDef def = (FunctionDef)sym.node;
                        String sig = computeFunctionSig(name, def, sym);
                        classDocument.addPair(FIELD_MEMBER, sig, true);
                    } else if (sym.isData()) {
                        StringBuilder sig = new StringBuilder();
                        sig.append(name);
                        appendFlags(sig, 'D', sym, 0);
                        classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                    } else if (sym.isMember()) {
                        StringBuilder sig = new StringBuilder();
                        sig.append(name);
                        appendFlags(sig, 'A', sym, 0);
                        classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                    } else if (!sym.isBound()) {
                        continue;
                    } else {
                        // XXX what the heck is this??
                        assert false : className + "::" + name + " : " + sym.dumpFlags(scopeInfo);
                    }
                }
            }
        }


// TODO - what about nested functions?
        private IndexDocument createDocument() {
            IndexDocument doc = factory.createDocument(DEFAULT_DOC_SIZE, overrideUrl);
            documents.add(doc);

            return doc;
        }
    }

    public static String computeClassSig(ClassDef def, SymInfo sym) {
        StringBuilder sig = new StringBuilder();
        sig.append(def.name);
        appendFlags(sig, 'C', sym, 0);

        return sig.toString();
    }

    public static String computeFunctionSig(String name, FunctionDef def, SymInfo sym) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        char type;
        if ("__init__".equals(name)) { // NOI18N
            type = 'c';
        } else {
            type = 'F';
        }
        appendFlags(sb, type, sym, 0);

        List<String> params = PythonAstUtils.getParameters(def);
        boolean first = true;
        for (String param : params) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(param);
        }
        sb.append(';');
        String sig = sb.toString();
        return sig;
    }

    private String cleanupSignature(String signature) {
        // Clean up signatures - remove [optional] areas, deal
        //   with arg=Default.Value parameters,
        //   or "literal" or (lit,er,al) default values.
        // See unit tests for details.
        boolean lastWasComma = false;
        StringBuilder sb = new StringBuilder();
        Loop:
        for (int i = 0, n = signature.length(); i < n; i++) {
            char c = signature.charAt(i);
            switch (c) {
            case ' ':
            case '[':
            case ']':
            case '\'':
            case '"':
            case '.':
                continue Loop;
            case '=': {
                int level = 0;
                for (i++; i < n; i++) {
                    c = signature.charAt(i);
                    if (c == '(') {
                        level++;
                    } else if (c == ')') {
                        if (level == 0) {
                            break;
                        }
                        level--;
                    }
                    if (c == ',' && level == 0) {
                        break;
                    }
                }
                i--; // compensate for loop-increment
                continue Loop;
            }
            case ')':
                if (lastWasComma) {
                    sb.setLength(sb.length() - 1);
                    lastWasComma = false;
                }
                break;
            case ',':
                if (lastWasComma) {
                    continue Loop;
                }
                lastWasComma = true;
                break;
            default:
                lastWasComma = false;
            }
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Determine if the definition beginning on lines[lineno] is deprecated.
     */
    private boolean isDeprecated(String[] lines, int lineno) {
        int firstIndent = RstFormatter.getIndentation(lines[lineno], 0);
        for (int i = lineno + 1; i < lines.length; i++) {
            String line = lines[i];
            int indent = RstFormatter.getIndentation(line, 0);
            if (indent == -1) { // empty line
                continue;
            }
            if (line.contains(":deprecated:") || line.contains(".. deprecated::")) { // NOI18N
                return true;
            }
            // Note - we checked for ::deprecated BEFORE bailing on the next
            // same-indent line, because in some cases, these appear on the same
            // level as the deprecated element (for exampe, modules)
            if (indent <= firstIndent) {
                return false;
            }

            // For classes we can have embedded definitions of functions/data/methods --
            // a deprecated note for these should not be considered a deprecation of
            // the whole class! See the unit test for bz2.zip for example.
            if (line.startsWith(".. attribute::", indent) || // NOI18N
                    line.startsWith(".. data::", indent) || // NOI18N
                    line.startsWith(".. function::", indent) || // NOI18N
                    line.startsWith(".. method::", indent)) { // NOI18N
                return false;
            }
        }

        return false;
    }

    private List<IndexDocument> scanRst(FileObject fo, IndexDocumentFactory factory, String overrideUrl) {
        List<IndexDocument> documents = new ArrayList<IndexDocument>();

        if (fo != null) {
            String module = fo.getNameExt();
            assert module.endsWith(".rst"); // NOI18N
            module = module.substring(0, module.length() - 4);

            // Skip files that are already in the standard Python libraries (as .py files).
            // For these, normal scanning applies
            // (I should consider checking that they are consistent with the official
            // documentation, at least during preindexing)
            if (PREINDEXING) {
                // XXX This doesn't work right for anything but the builtin Jython interpreter....
                // OTOH that's the only thing we're preindexing at this point
                FileObject lib = getLibDir();
                if (lib != null) {
                    String path = module.replace('.', '/');
                    FileObject py = lib.getFileObject(path); // Look for package dir
                    if (py == null) {
                        py = lib.getFileObject(path + ".py"); // NOI18N
                    }
                    if (py != null) {
                        System.err.println("DELETE " + FileUtil.getFileDisplayName(fo) + " because there is a corresponding " + FileUtil.getFileDisplayName(py)); // NOI18N
                        // No - it's in a zip archive now
                        //try {
                        //    // Delete it!
                        //    fo.delete();
                        //} catch (IOException ex) {
                        //    Exceptions.printStackTrace(ex);
                        //}
                        return documents;
                    }
                }
            }

            String name = fo.getName();

            // Skip some really obsolete libraries -- IRIX only etc
            if (name.equals("gl") || name.equals("cd") || // NOI18N
                    name.equals("al") || name.equals("fm") ||
                    name.equals("fl") || name.equals("imgfile") || // NOI18N
                    name.equals("jpeg") || // NOI18N
                    name.equals("sunau") || name.equals("sunaudio")) { // NOI!8N
                return documents;
            }

            Pattern PATTERN = Pattern.compile("\\s*\\.\\.\\s+(.*)::\\s*(.+)\\s*"); // NOI18N

            BaseDocument doc = GsfUtilities.getDocument(fo, true);
            if (doc != null) {
                try {
                    String text = doc.getText(0, doc.getLength());
                    String[] lines = text.split("\n");
                    String currentClass = null;
                    Map<String, IndexDocument> classDocs = new HashMap<String, IndexDocument>();
                    IndexDocument document = null;

                    for (int lineno = 0, maxLines = lines.length; lineno < maxLines; lineno++) {
                        String line = lines[lineno];
                        if (!line.startsWith(".. ") && line.indexOf(" .. ") == -1) { // NOI18N
                            continue;
                        }

                        Matcher m = PATTERN.matcher(line);
                        if (m.matches()) {
                            String key = m.group(1);

                            if (key.equals("attribute") || // NOI18N
                                    key.equals("currentmodule") || // NOI18N
                                    key.equals("class") || // NOI18N
                                    key.equals("exception") || // NOI18N
                                    key.equals("function") || // NOI18N
                                    key.equals("method") || // NOI18N
                                    key.equals("data") || // NOI18N
                                    key.equals("module")) {  // NOI18N


                                if (key.equals("module") || key.equals("currentmodule")) {  // NOI18N
                                    // TODO - determine package name
                                    module = m.group(2);
                                    document = factory.createDocument(DEFAULT_DOC_SIZE, overrideUrl);
                                    documents.add(document);
                                    document.addPair(FIELD_MODULE_NAME, module, true);
                                    String moduleAttrs = "S";
                                    if (isDeprecated(lines, lineno)) {
                                        moduleAttrs = "SD";
                                    }
                                    document.addPair(FIELD_MODULE_ATTR_NAME, moduleAttrs, false); // NOI18N
                                } else {
                                    // Methods described in an rst without an actual module definition...
                                    if (document == null) {
                                        document = factory.createDocument(DEFAULT_DOC_SIZE, overrideUrl);
                                        documents.add(document);
                                        document.addPair(FIELD_MODULE_NAME, module, true);
                                        document.addPair(FIELD_MODULE_ATTR_NAME, "S", false); // NOI18N
                                    }
                                    if (key.equals("method") || key.equals("attribute")) { // NOI18N) { // NOI18N
                                        String signature = m.group(2);

                                        int dot = signature.indexOf('.');
                                        if (dot != -1) {
                                            int paren = signature.indexOf('(');
                                            if (paren == -1 || paren > dot) {
                                                assert signature.matches("\\w+\\.\\w+.*") : signature;
                                                String dottedName = signature.substring(0, dot);
                                                IndexDocument dottedDoc = classDocs.get(dottedName);
                                                if (dottedDoc != null) {
                                                    currentClass = dottedName;
                                                } else if (currentClass == null) {
                                                    currentClass = dottedName;
                                                    // New class without class:: declaration first.
                                                    IndexDocument classDocument = factory.createDocument(DEFAULT_DOC_SIZE, overrideUrl);
                                                    documents.add(classDocument);
                                                    classDocs.put(currentClass, classDocument);
                                                    classDocument.addPair(FIELD_IN, module, true);

                                                    classDocument.addPair(FIELD_CLASS_NAME, currentClass, true);
                                                    classDocument.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, currentClass.toLowerCase(), true);
                                                }
                                                signature = signature.substring(dot + 1);
                                            }
                                        }


                                        IndexDocument classDocument = classDocs.get(currentClass);
                                        assert classDocs != null;

                                        if (key.equals("method")) {
                                            signature = cleanupSignature(signature);
                                            if (signature.indexOf('(') == -1) {
                                                signature = signature + "()";
                                            }

                                            assert signature.indexOf('(') != -1 && signature.indexOf(')') != -1 &&
                                                    signature.indexOf(')') > signature.indexOf('(') : signature;
                                            int lparen = signature.indexOf('(');
                                            int rparen = signature.indexOf(')', lparen + 1);
                                            if (lparen != -1 && rparen != -1) {
                                                String methodName = signature.substring(0, lparen);
                                                String args = signature.substring(lparen + 1, rparen);
                                                char type;
                                                if (methodName.equals("__init__")) { // NOI18N
                                                    type = 'c';
                                                } else {
                                                    type = 'F';
                                                }
                                                StringBuilder sig = new StringBuilder();
                                                sig.append(methodName);

                                                int symFlags = 0;
                                                if (SymInfo.isPrivateName(methodName)) {
                                                    symFlags |= ScopeConstants.PRIVATE;
                                                }
                                                // TODO - look up deprecated etc.
                                                SymInfo fakeSym = new SymInfo(symFlags);

                                                int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY;
                                                if (isDeprecated(lines, lineno)) {
                                                    flags |= IndexedElement.DEPRECATED;
                                                }

                                                appendFlags(sig, type, fakeSym, flags);
                                                sig.append(args);
                                                sig.append(';');

                                                classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                                            }
                                        } else {
                                            assert key.equals("attribute");

                                            StringBuilder sig = new StringBuilder();
                                            sig.append(signature);
                                            int symFlags = 0;
                                            if (SymInfo.isPrivateName(signature)) {
                                                symFlags |= ScopeConstants.PRIVATE;
                                            }
                                            // TODO - look up deprecated etc.
                                            SymInfo fakeSym = new SymInfo(symFlags);

                                            int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY;
                                            if (isDeprecated(lines, lineno)) {
                                                flags |= IndexedElement.DEPRECATED;
                                            }


                                            appendFlags(sig, 'A', fakeSym, flags);
                                            classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                                        }
                                    } else if (key.equals("class") || key.equals("exception")) { // NOI18N
                                        assert module != null;
                                        String cls = m.group(2);

                                        int paren = cls.indexOf('(');
                                        String constructor = null;
                                        if (paren != -1) {
                                            // Some documents specify a constructor here
                                            constructor = cleanupSignature(cls);
                                            cls = cls.substring(0, paren);
                                        }
                                        currentClass = cls;

                                        IndexDocument classDocument = factory.createDocument(DEFAULT_DOC_SIZE, overrideUrl);
                                        classDocs.put(currentClass, classDocument);
                                        documents.add(classDocument);
                                        classDocument.addPair(FIELD_IN, module, true);

                                        if (key.equals("exception") && !"Exception".equals(cls)) { // NOI18N
                                            classDocument.addPair(FIELD_EXTENDS_NAME, "Exception", true); // NOI18N
                                        }

                                        classDocument.addPair(FIELD_CLASS_NAME, cls, true);
                                        int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY | IndexedElement.CONSTRUCTOR;
                                        if (isDeprecated(lines, lineno)) {
                                            flags |= IndexedElement.DEPRECATED;
                                        }
                                        if (flags != 0) {
                                            // TODO - store Documented, Deprecated, DocOnly, etc.
                                            classDocument.addPair(FIELD_CLASS_ATTR_NAME, IndexedElement.encode(flags), false);
                                        }
                                        classDocument.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, cls.toLowerCase(), true);

                                        // TODO - determine extends
                                        //document.addPair(FIELD_EXTENDS_NAME, superClass, true);

                                        if (constructor != null) {
                                            assert constructor.indexOf('(') != -1 && constructor.indexOf(')') != -1 &&
                                                    constructor.indexOf(')') > constructor.indexOf('(') : constructor;

                                            String signature = constructor;
                                            int lparen = signature.indexOf('(');
                                            int rparen = signature.indexOf(')', lparen + 1);
                                            if (lparen != -1 && rparen != -1) {
                                                String methodName = signature.substring(0, lparen);
                                                String args = signature.substring(lparen + 1, rparen);
                                                StringBuilder sig = new StringBuilder();
                                                sig.append(methodName);
                                                int symFlags = 0;
                                                if (SymInfo.isPrivateName(methodName)) {
                                                    symFlags |= ScopeConstants.PRIVATE;
                                                }
                                                // TODO - look up deprecated etc.
                                                SymInfo fakeSym = new SymInfo(symFlags);

                                                flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY | IndexedElement.CONSTRUCTOR;
                                                if (isDeprecated(lines, lineno)) {
                                                    flags |= IndexedElement.DEPRECATED;
                                                }

                                                appendFlags(sig, 'c', fakeSym, flags);
                                                sig.append(args);
                                                sig.append(';');

                                                classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                                            }

                                        }
                                    } else if (key.equals("function") || key.equals("data") && m.group(2).contains("(")) { // NOI18N
                                        // constants.rst for example registers a data item for "quit" which is really a function

                                        String signature = m.group(2);
                                        int dot = signature.indexOf('.');
                                        if (dot != -1) {
                                            int paren = signature.indexOf('(');
                                            if (paren == -1 || paren > dot) {
                                                assert signature.matches("\\w+\\.\\w+.*") : signature;
                                                signature = signature.substring(dot + 1);
                                            }
                                        }
                                        signature = cleanupSignature(signature);
                                        if (signature.indexOf('(') == -1) {
                                            signature = signature + "()";
                                        } else if (signature.indexOf(')') == -1) {
                                            //signature = signature + ")";
                                            assert signature.indexOf(')') != -1;
                                        }
                                        int lparen = signature.indexOf('(');
                                        int rparen = signature.indexOf(')', lparen + 1);
                                        if (lparen != -1 && rparen != -1) {
                                            String methodName = signature.substring(0, lparen);
                                            String args = signature.substring(lparen + 1, rparen);

                                            StringBuilder sig = new StringBuilder();
                                            sig.append(methodName);
                                            int symFlags = 0;
                                            if (SymInfo.isPrivateName(methodName)) {
                                                symFlags |= ScopeConstants.PRIVATE;
                                            }
                                            // TODO - look up deprecated etc.
                                            SymInfo fakeSym = new SymInfo(symFlags);

                                            int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY;
                                            if (isDeprecated(lines, lineno)) {
                                                flags |= IndexedElement.DEPRECATED;
                                            }


                                            appendFlags(sig, 'F', fakeSym, flags);
                                            sig.append(args);
                                            sig.append(';');

                                            document.addPair(FIELD_ITEM, sig.toString(), true);
                                        }
                                    } else if (key.equals("data")) { // NOI18N
                                        String data = m.group(2);

                                        StringBuilder sig = new StringBuilder();
                                        sig.append(data);
                                        int symFlags = 0;
                                        if (SymInfo.isPrivateName(data)) {
                                            symFlags |= ScopeConstants.PRIVATE;
                                        }
                                        // TODO - look up deprecated etc.
                                        SymInfo fakeSym = new SymInfo(symFlags);

                                        int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY;
                                        if (isDeprecated(lines, lineno)) {
                                            flags |= IndexedElement.DEPRECATED;
                                        }

                                        appendFlags(sig, 'D', fakeSym, flags);

                                        document.addPair(FIELD_ITEM, sig.toString(), true);
                                    } else {
                                        // TODO Handle deprecated attribute!

                                        //    currentmodule::
                                        //    deprecated::
                                        //    doctest::
                                        //    envvar::
                                        //    epigraph::
                                        //    highlight::
                                        //    highlightlang::
                                        //    index::
                                        //    literalinclude::
                                        //    moduleauthor::
                                        //    note::
                                        //    opcode::
                                        //    productionlist::
                                        //    rubric::
                                        //    sectionauthor::
                                        //    seealso::
                                        //    testcode::
                                        //    testsetup::
                                        //    toctree::
                                        //    versionadded::
                                        //    versionchanged::
                                        //    warning::
                                    }
                                }
                            }
                        } else if (line.startsWith(".. _bltin-file-objects:")) { // NOI18N
                            if (currentClass != null) {
                                currentClass = null;
                            }
                        }
                    }

                    for (String clz : classDocs.keySet()) {
                        StringBuilder sig = new StringBuilder();
                        sig.append(clz);
                        int symFlags = 0;
                        if (SymInfo.isPrivateName(clz)) {
                            symFlags |= ScopeConstants.PRIVATE;
                        }
                        // TODO - look up deprecated etc.
                        SymInfo fakeSym = new SymInfo(symFlags);
                        appendFlags(sig, 'C', fakeSym, IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY);

                        document.addPair(FIELD_ITEM, sig.toString(), true);
                    }

                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return documents;
    }

    private List<IndexDocument> scanEgg(ParserResult result, IndexDocumentFactory factory) {
        List<IndexDocument> documents = new ArrayList<IndexDocument>();

        FileObject fo = result.getFile().getFileObject();
        if (fo == null) {
            return documents;
        }

        try {
            String s = fo.getURL().toExternalForm() + "!"; // NOI18N
            URL u = new URL("jar:" + s); // NOI18N
            FileObject root = URLMapper.findFileObject(u);
            String rootUrl = u.toExternalForm();
            indexScriptDocRecursively(factory, documents, root, rootUrl);
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }

        return documents;
    }

    /**
     * Method which recursively indexes directory trees, such as the yui/ folder
     * for example
     */
    private void indexScriptDocRecursively(IndexDocumentFactory factory, List<IndexDocument> documents, final FileObject fo, String url) {
        if (fo.isFolder()) {
            for (FileObject c : fo.getChildren()) {
                indexScriptDocRecursively(factory, documents, c, url + "/" + c.getNameExt()); // NOI18N
            }
            return;
        }

        String ext = fo.getExt();

        if ("py".equals(ext)) { // NOI18N
            DefaultParseListener listener = new DefaultParseListener();
            List<ParserFile> files = Collections.<ParserFile>singletonList(new DefaultParserFile(fo, null, false));
            SourceFileReader reader = new SourceFileReader() {
                public CharSequence read(ParserFile file) throws IOException {
                    BaseDocument doc = GsfUtilities.getDocument(fo, true);
                    if (doc != null) {
                        try {
                            return doc.getText(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                    return "";
                }

                public int getCaretOffset(ParserFile file) {
                    return -1;
                }
            };
            Job job = new Job(files, listener, reader, null);
            new PythonParser().parseFiles(job);
            ParserResult parserResult = listener.getParserResult();
            if (parserResult != null && parserResult.isValid()) {
                documents.addAll(new IndexTask((PythonParserResult)parserResult, factory, url).scan());
            }
        } else if ("rst".equals(ext)) { // NOI18N
            documents.addAll(scanRst(fo, factory, url));
        }
    }

    private FileObject getLibDir() {
        // TODO - fetch from projects!!!!
        PythonPlatformManager manager = PythonPlatformManager.getInstance();
        PythonPlatform platform = manager.getPlatform(manager.getDefaultPlatform());
        if (platform != null) {
            String cmd = platform.getInterpreterCommand();
            File file = new File(cmd);
            if (file.exists()) {
                file = file.getAbsoluteFile();
                File home = file.getParentFile().getParentFile();
                if (home != null) {
                    // Look for Lib - Jython style
                    File lib = new File(home, "Lib"); // NOI18N
                    boolean exists = lib.exists();
                    if (!exists) { // Unix style
                        lib = new File(home, "lib" + File.separator + "python"); // NOI18N
                        exists = lib.exists();
                    }
                    if (exists) {
                        return FileUtil.toFileObject(lib);
                    }
                }
            }
        }

        return null;
    }
}
