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

package org.netbeans.modules.php.editor.index;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocPropertyTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
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
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPIndexer implements Indexer {
    static final boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing");
    private static final FileSystem MEM_FS = FileUtil.createMemoryFileSystem();
    private static final Map<String,FileObject> EXT2FO = new HashMap<String,FileObject>();
    // a workaround for issue #132388
    private static final Collection<String>INDEXABLE_EXTENSIONS = Arrays.asList(
            "php", "php3", "php4", "php5", "phtml", "inc"); //NOI18N

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

    static final String FIELD_BASE = "base"; //NOI18N
    static final String FIELD_EXTEND = "extend"; //NOI18N
    static final String FIELD_CLASS = "clz"; //NOI18N
    static final String FIELD_IFACE = "iface"; //NOI18N
    static final String FIELD_CONST = "const"; //NOI18N
    static final String FIELD_CLASS_CONST = "clz.const"; //NOI18N
    static final String FIELD_FIELD = "field"; //NOI18N
    static final String FIELD_METHOD = "method"; //NOI18N
    static final String FIELD_INCLUDE = "include"; //NOI18N
    static final String FIELD_IDENTIFIER = "identifier_used"; //NOI18N
    static final String FIELD_IDENTIFIER_DECLARATION = "identifier_declaration"; //NOI18N

    static final String FIELD_VAR = "var"; //NOI18N
    /** This field is for fast access top level elemnts */
    static final String FIELD_TOP_LEVEL = "top"; //NOI18N

    public boolean isIndexable(ParserFile file) {
        // Cannot call file.getFileObject().getMIMEType() here for several reasons:
        // (1) when cleaning up the index for deleted files, file.getFileObject().getMIMEType()
        //   may return "content/unknown", and in some cases, file.getFileObject() returns null
        // (2) file.getFileObject() can be expensive during startup indexing when we're
        //   rapidly scanning through lots of directories to determine which files are
        //   indexable. This is done using the java.io.File API rather than the more heavyweight
        //   FileObject, and each file.getFileObject() will perform a FileUtil.toFileObject() call.
        // Since the mime resolver for PHP is simple -- it's just based on the file extension,
        // we perform the same check here:
        //if (PHPLanguage.PHP_MIME_TYPE.equals(file.getFileObject().getMIMEType())) { // NOI18N
        if (INDEXABLE_EXTENSIONS.contains(file.getExtension().toLowerCase())) {
            return true;
        }

        return isPhpFile(file);
    }

    private boolean isPhpFile(ParserFile file) {
        FileObject fo = null;
        String ext = file.getExtension();
        synchronized (EXT2FO) {
            fo = (ext != null) ? EXT2FO.get(ext) : null;
            if (fo == null) {
                try {
                    fo = FileUtil.createData(MEM_FS.getRoot(), file.getNameExt());
                    if (ext != null && fo != null) {
                        EXT2FO.put(ext, fo);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        assert fo != null;
        return PHPLanguage.PHP_MIME_TYPE.equals(FileUtil.getMIMEType(fo, PHPLanguage.PHP_MIME_TYPE));
    }

    public String getPersistentUrl(File file) {
        String url;
        try {
            url = file.toURI().toURL().toExternalForm();
            // Make relative URLs for urls in the libraries
            return PHPIndex.getPreindexUrl(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return file.getPath();
        }

    }

    public List<IndexDocument> index(ParserResult result, IndexDocumentFactory factory) throws IOException {
        PHPParseResult r = (PHPParseResult)result;

        if (r.getProgram() == null){
            return Collections.<IndexDocument>emptyList();
        }

        TreeAnalyzer analyzer = new TreeAnalyzer(r, factory);
        analyzer.analyze();

        return analyzer.getDocuments();
    }

    public String getIndexVersion() {
        // If you chane the index number, you have to regenerate preindexed
        // php runtime files. Go to the php.project/tools, modify and run
        // preindex.sh script. Also change the number of license in
        // php.project/external/preindexed-php-license.txt
        return "0.5.6"; // NOI18N
    }

    public String getIndexerName() {
        return "php"; // NOI18N
    }

    private static class TreeAnalyzer {
        private final ParserFile file;
        private String url;
        private final PHPParseResult result;
        private Program root;
        //private final BaseDocument doc;
        private IndexDocumentFactory factory;
        private List<IndexDocument> documents = new ArrayList<IndexDocument>();

        private TreeAnalyzer(PHPParseResult result, IndexDocumentFactory factory) {
            this.result = result;
            this.file = result.getFile();
            this.factory = factory;

            /*FileObject fo = file.getFileObject();

            if (fo != null) {
                this.doc = NbUtilities.getBaseDocument(fo, true);
            } else {
                this.doc = null;
            }
            */
            try {
                url = file.getFile().toURI().toURL().toExternalForm();

                // Make relative URLs for urls in the libraries
                url = PHPIndex.getPreindexUrl(url);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        List<IndexDocument> getDocuments() {
            return documents;
        }

        private void indexFieldsDeclaration(FieldsDeclaration fieldsDeclaration, IndexDocument document) {
            for (SingleFieldDeclaration field : fieldsDeclaration.getFields()) {
                if (field.getName().getName() instanceof Identifier) {
                    Identifier identifier = (Identifier) field.getName().getName();
                    String type = getFieldTypeFromPHPDoc(field);
                    String signature = createFieldsDeclarationRecord(identifier.getName(), type, fieldsDeclaration.getModifier(), field.getStartOffset());
                    document.addPair(FIELD_FIELD, signature, false);
                }
            }
        }

        private String createFieldsDeclarationRecord(String name, String type, int modifier, int offset) {
            StringBuilder fieldSignature = new StringBuilder();
            fieldSignature.append(name + ";"); //NOI18N
            fieldSignature.append(offset + ";"); //NOI18N
            fieldSignature.append(modifier + ";"); //NOI18N
            if (type != null){
                fieldSignature.append(type);
            }
            fieldSignature.append(";"); //NOI18N
            return fieldSignature.toString();
        }

        private class IndexerVisitor extends DefaultTreePathVisitor{
            private List<IndexDocument> documents;
            private IndexDocument defaultDocument;
            private final IndexDocument identifierDocument = factory.createDocument(10);
            private Map<String, IdentifierSignature> identifiers = new HashMap<String, IdentifierSignature>();

            public IndexerVisitor(List<IndexDocument> documents, IndexDocument defaultDocument) {
                this.documents = documents;
                this.defaultDocument = defaultDocument;
                documents.add(identifierDocument);
            }
            public void addIdentifierPairs() {
                Collection<IdentifierSignature> values = identifiers.values();
                for (IdentifierSignature idSign : values) {
                    identifierDocument.addPair(FIELD_IDENTIFIER, idSign.getSignature(), true);
                }
            }
            @Override
            public void visit(Identifier node) {
                IdentifierSignature.add(node, identifiers);
                super.visit(node);
            }
            @Override
            public void visit(ClassDeclaration node) {
                // create a new document for each class
                IndexDocument classDocument = factory.createDocument(10);
                documents.add(classDocument);
                indexClass((ClassDeclaration) node, classDocument);
                List<IdentifierSignature> idSignatures = new ArrayList<IdentifierSignature>();
                IdentifierSignature.add(node, Utils.getPropertyTags(root, node), idSignatures);
                for (IdentifierSignature idSign : idSignatures) {
                    identifierDocument.addPair(FIELD_IDENTIFIER_DECLARATION, idSign.getSignature(), true);
                }
                super.visit(node);
            }

            @Override
            public void visit(FunctionDeclaration node) {
                if (getPath().get(0) instanceof MethodDeclaration){
                    super.visit(node);
                    return;
                }

                indexFunction((FunctionDeclaration)node, defaultDocument);
                super.visit(node);
            }

            @Override
            public void visit(ExpressionStatement node) {
                indexConstant(node, defaultDocument);
                super.visit(node);
            }

            @Override
            public void visit(InterfaceDeclaration node) {
                IndexDocument ifaceDocument = factory.createDocument(10);
                documents.add(ifaceDocument);
                indexInterface((InterfaceDeclaration) node, ifaceDocument);
                List<IdentifierSignature> idSignatures = new ArrayList<IdentifierSignature>();
                IdentifierSignature.add(node, idSignatures);
                for (IdentifierSignature idSign : idSignatures) {
                    identifierDocument.addPair(FIELD_IDENTIFIER_DECLARATION, idSign.getSignature(), true);
                }
                super.visit(node);
            }
        }

        public void analyze() throws IOException {

            IndexDocument defaultDocument = factory.createDocument(40); // TODO - measure!
            documents.add(defaultDocument);

            root = result.getProgram();
            IndexerVisitor indexerVisitor = new IndexerVisitor(documents, defaultDocument);
            root.accept(indexerVisitor);
            indexerVisitor.addIdentifierPairs();

            String processedFileURL = null;

            try {
                processedFileURL = result.getFile().getFileObject().getURL().toExternalForm();

            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }

            assert processedFileURL.startsWith("file:");
            String processedFileAbsPath = processedFileURL.substring("file:".length());
            StringBuilder includes = new StringBuilder();

            for (Statement statement : root.getStatements()){
                if (statement instanceof ExpressionStatement){
                    ExpressionStatement expressionStatement = (ExpressionStatement) statement;

                    if (expressionStatement.getExpression() instanceof Assignment) {
                        Assignment assignment = (Assignment) expressionStatement.getExpression();
                        indexVarsInAssignment(assignment, defaultDocument);
                    }

                    if (expressionStatement.getExpression() instanceof Include) {
                        Include include = (Include) expressionStatement.getExpression();

                        Expression argExpression = include.getExpression();

                        if (argExpression instanceof ParenthesisExpression) {
                            ParenthesisExpression parenthesisExpression = (ParenthesisExpression) include.getExpression();
                            argExpression = parenthesisExpression.getExpression();
                        }

                        if (argExpression instanceof Scalar) {
                            Scalar scalar = (Scalar) argExpression;
                            String rawInclude = scalar.getStringValue();

                            // check if the string is really quoted
                            if (isQuotedString(rawInclude)) {
                                String incl = PHPIndex.resolveRelativeURL(processedFileAbsPath, dequote(rawInclude));
                                includes.append(incl + ";"); //NOI18N
                            }
                        }
                    }
                }
            }

            defaultDocument.addPair(FIELD_INCLUDE, includes.toString(), false);
        }

        private void indexClass(ClassDeclaration classDeclaration, IndexDocument document) {
            StringBuilder classSignature = new StringBuilder();
            classSignature.append(classDeclaration.getName().getName().toLowerCase() + ";"); //NOI18N
            classSignature.append(classDeclaration.getName().getName() + ";"); //NOI18N
            classSignature.append(classDeclaration.getStartOffset() + ";"); //NOI18N

            String superClass = ""; //NOI18N

            if (classDeclaration.getSuperClass() instanceof Identifier) {
                Identifier identifier = (Identifier) classDeclaration.getSuperClass();
                superClass = identifier.getName();
            }

            classSignature.append(superClass + ";"); //NOI18N
            document.addPair(FIELD_CLASS, classSignature.toString(), true);
            document.addPair(FIELD_TOP_LEVEL, classDeclaration.getName().getName().toLowerCase(), true);

            for (Statement statement : classDeclaration.getBody().getStatements()){
                if (statement instanceof MethodDeclaration) {
                    MethodDeclaration methodDeclaration = (MethodDeclaration) statement;
                    indexMethod(methodDeclaration.getFunction(), methodDeclaration.getModifier(), document);
                } else if (statement instanceof FieldsDeclaration) {
                    FieldsDeclaration fieldsDeclaration = (FieldsDeclaration) statement;
                    indexFieldsDeclaration(fieldsDeclaration, document);
                } else if (statement instanceof ClassConstantDeclaration) {
                    ClassConstantDeclaration constDeclaration = (ClassConstantDeclaration) statement;

                    for (Identifier id : constDeclaration.getNames()){
                        StringBuilder signature = new StringBuilder();
                        signature.append(id.getName() + ";");
                        signature.append(constDeclaration.getStartOffset() + ";");
                        document.addPair(FIELD_CLASS_CONST, signature.toString(), false);
                    }
                }
            }
            for (PHPDocPropertyTag tag : Utils.getPropertyTags(root, classDeclaration)) {
                String signature = createFieldsDeclarationRecord(tag.getFieldName(), tag.getFieldType(), BodyDeclaration.Modifier.PUBLIC, tag.getStartOffset());
                document.addPair(FIELD_FIELD, signature, false);
            }
        }

        private void indexInterface(InterfaceDeclaration ifaceDecl, IndexDocument document) {
            StringBuilder ifaceSign = new StringBuilder();
            ifaceSign.append(ifaceDecl.getName().getName().toLowerCase() + ";"); //NOI18N
            ifaceSign.append(ifaceDecl.getName().getName() + ";"); //NOI18N
            ifaceSign.append(ifaceDecl.getStartOffset() + ";"); //NOI18N

            for (Iterator<Identifier> it = ifaceDecl.getInterfaes().iterator(); it.hasNext();) {
                Identifier id = it.next();
                ifaceSign.append(id.getName());

                if (it.hasNext()){
                    ifaceSign.append(',');
                }
            }

            ifaceSign.append(';');
            document.addPair(FIELD_IFACE, ifaceSign.toString(), true);
            document.addPair(FIELD_TOP_LEVEL, ifaceDecl.getName().getName().toLowerCase(), true);

            for (Statement statement : ifaceDecl.getBody().getStatements()){
                if (statement instanceof MethodDeclaration) {
                    MethodDeclaration methodDeclaration = (MethodDeclaration) statement;
                    indexMethod(methodDeclaration.getFunction(), methodDeclaration.getModifier(), document);
                } else if (statement instanceof FieldsDeclaration) {
                    FieldsDeclaration fieldsDeclaration = (FieldsDeclaration) statement;
                    indexFieldsDeclaration(fieldsDeclaration, document);
                } else if (statement instanceof ClassConstantDeclaration) {
                    ClassConstantDeclaration constDeclaration = (ClassConstantDeclaration) statement;

                    for (Identifier id : constDeclaration.getNames()){
                        StringBuilder signature = new StringBuilder();
                        signature.append(id.getName() + ";");
                        signature.append(constDeclaration.getStartOffset() + ";");
                        document.addPair(FIELD_CLASS_CONST, signature.toString(), false);
                    }
                }

            }
        }

        private void indexVarsInAssignment(Assignment assignment, IndexDocument document) {
            if (assignment.getLeftHandSide() instanceof Variable) {
                Variable var = (Variable) assignment.getLeftHandSide();
                String varType = CodeUtils.extractVariableTypeFromAssignment(assignment);
                String varName = CodeUtils.extractVariableName(var);
                if (varName != null) {
                    String varNameNoDollar = varName.startsWith("$") ? varName.substring(1) : varName;

                    if (!PredefinedSymbols.isSuperGlobalName(varNameNoDollar)) {
                        StringBuilder signature = new StringBuilder();
                        signature.append(varName.toLowerCase() + ";" + varName + ";");

                        if (varType != null) {
                            signature.append(varType);
                        }

                        signature.append(";"); //NOI18N
                        signature.append(var.getStartOffset() + ";");
                        document.addPair(FIELD_VAR, signature.toString(), true);
                        document.addPair(FIELD_TOP_LEVEL, varName.toLowerCase(), true);
                    }
                }
            }

            if (assignment.getRightHandSide() instanceof Assignment) {
                Assignment embeddedAssignment = (Assignment) assignment.getRightHandSide();
                indexVarsInAssignment(embeddedAssignment, document);
            }
        }

        private void indexConstant(Statement statement, IndexDocument document) {
            ExpressionStatement exprStmt = (ExpressionStatement) statement;
            Expression expr = exprStmt.getExpression();

            if (expr instanceof FunctionInvocation) {
                FunctionInvocation invocation = (FunctionInvocation) expr;

                if (invocation.getFunctionName().getName() instanceof Identifier) {
                    Identifier id = (Identifier) invocation.getFunctionName().getName();

                    if ("define".equals(id.getName())) {
                        if (invocation.getParameters().size() >= 2) {
                            Expression paramExpr = invocation.getParameters().get(0);

                            if (paramExpr instanceof Scalar) {
                                String constName = ((Scalar) paramExpr).getStringValue();

                                // check if const name is really quoted
                                if (isQuotedString(constName)) {
                                    String defineVal = dequote(constName);
                                    StringBuilder signature = new StringBuilder();
                                    signature.append(defineVal.toLowerCase());
                                    signature.append(';');
                                    signature.append(defineVal);
                                    signature.append(';');
                                    signature.append(invocation.getStartOffset());
                                    signature.append(';');
                                    document.addPair(FIELD_CONST,  signature.toString(), true);
                                    document.addPair(FIELD_TOP_LEVEL, defineVal.toLowerCase(), true);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void indexFunction(FunctionDeclaration functionDeclaration, IndexDocument document) {
            StringBuilder signature = new StringBuilder(functionDeclaration.getFunctionName().getName().toLowerCase() + ";");
            signature.append(getBaseSignatureForFunctionDeclaration(functionDeclaration));

            document.addPair(FIELD_BASE, signature.toString(), true);
            document.addPair(FIELD_TOP_LEVEL, functionDeclaration.getFunctionName().getName().toLowerCase(), true);
        }

        private void indexMethod(FunctionDeclaration functionDeclaration, int modifiers, IndexDocument document) {
            StringBuilder signature = new StringBuilder();
            signature.append(getBaseSignatureForFunctionDeclaration(functionDeclaration));
            signature.append(modifiers + ";"); //NOI18N

            document.addPair(FIELD_METHOD, signature.toString(), false);
        }

        private String getBaseSignatureForFunctionDeclaration(FunctionDeclaration functionDeclaration){
            StringBuilder signature = new StringBuilder();
            signature.append(functionDeclaration.getFunctionName().getName() + ";");
            StringBuilder defaultArgs = new StringBuilder();
            int paramCount = functionDeclaration.getFormalParameters().size();

            for (int i = 0; i < paramCount; i++) {
                FormalParameter param = functionDeclaration.getFormalParameters().get(i);

                String paramName = CodeUtils.getParamDisplayName(param);
                signature.append(paramName);

                if (i < paramCount - 1) {
                    signature.append(",");
                }

                if (param.getDefaultValue() != null){
                    if (defaultArgs.length() > 0){
                        defaultArgs.append(',');
                    }

                    defaultArgs.append(Integer.toString(i));
                }
            }

            signature.append(';');
            signature.append(functionDeclaration.getStartOffset() + ";"); //NOI18N
            signature.append(defaultArgs + ";");

            String type = getReturnTypeFromPHPDoc(functionDeclaration);

            if (type != null && !PredefinedSymbols.MIXED_TYPE.equalsIgnoreCase(type)){
                signature.append(type);
            }

            signature.append(";"); //NOI18N

            return signature.toString();
        }


        private String getReturnTypeFromPHPDoc(FunctionDeclaration functionDeclaration) {
            return getTypeFromPHPDoc(functionDeclaration, PHPDocTag.Type.RETURN);
        }

        private String getFieldTypeFromPHPDoc(SingleFieldDeclaration field){
            return getTypeFromPHPDoc(field, PHPDocTag.Type.VAR);
        }

        private String getTypeFromPHPDoc(ASTNode node, PHPDocTag.Type tagType){
            Comment comment = Utils.getCommentForNode(root, node);

            if (comment instanceof PHPDocBlock) {
                PHPDocBlock phpDoc = (PHPDocBlock) comment;

                for (PHPDocTag tag : phpDoc.getTags()) {
                    if (tag.getKind() == tagType) {
                        String parts[] = tag.getValue().split("\\s+", 2); //NOI18N

                        if (parts.length > 0) {
                            String type = parts[0].split("\\;", 2)[0];
                            return type;
                        }

                        break;
                    }
                }
            }

            return null;
        }
    }

    static boolean isQuotedString(String txt) {
        if (txt.length() < 2) {
            return false;
        }

        char firstChar = txt.charAt(0);
        return firstChar == txt.charAt(txt.length() - 1) && firstChar == '\'' || firstChar == '\"';
    }

    static String dequote(String string){
        assert isQuotedString(string);
        return string.substring(1, string.length() - 1);
    }

    public File getPreindexedData() {
        return null;
    }

    private static FileObject preindexedDb;

    /** For testing only */
    public static void setPreindexedDb(FileObject preindexedDb) {
        PHPIndexer.preindexedDb = preindexedDb;
    }

    public FileObject getPreindexedDb() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * As the above documentation states, this is a temporary solution / hack
     * for 6.1 only.
     */
     public boolean acceptQueryPath(String url) {
        // Filter out JavaScript stuff
        return url.indexOf("jsstubs") == -1 && // NOI18N
                // Filter out Ruby stuff
                url.indexOf("/ruby2/") == -1 &&  // NOI18N
                url.indexOf("/gems/") == -1 &&  // NOI18N
                url.indexOf("lib/ruby/") == -1; // NOI18N
     }
}
