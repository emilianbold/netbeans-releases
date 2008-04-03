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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
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
public class PHPIndexer implements Indexer {
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

    static final String FIELD_BASE = "base"; //NOI18N
    static final String FIELD_EXTEND = "extend"; //NOI18N
    static final String FIELD_CLASS = "clz"; //NOI18N
    static final String FIELD_CONST = "const"; //NOI18N
    static final String FIELD_FIELD = "field"; //NOI18N
    static final String FIELD_METHOD = "method"; //NOI18N
    static final String FIELD_INCLUDE = "include"; //NOI18N
    
    public boolean isIndexable(ParserFile file) {
        if (file != null && file.getFileObject() != null 
                && PHPLanguage.PHP_MIME_TYPE.equals(file.getFileObject().getMIMEType())) { // NOI18N
            return true;
        }
        
        return false;
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
        
        TreeAnalyzer analyzer = new TreeAnalyzer(r, factory);
        analyzer.analyze();
        
        return analyzer.getDocuments();
    }
    
    public String getIndexVersion() {
        return "0.2.3"; // NOI18N
    }

    public String getIndexerName() {
        return "php"; // NOI18N
    }
    
    private static class TreeAnalyzer {
        private final ParserFile file;
        private String url;
        private final PHPParseResult result;
        private final BaseDocument doc;
        private IndexDocumentFactory factory;
        private List<IndexDocument> documents = new ArrayList<IndexDocument>();
        
        private TreeAnalyzer(PHPParseResult result, IndexDocumentFactory factory) {
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
                url = PHPIndex.getPreindexUrl(url);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        List<IndexDocument> getDocuments() {
            return documents;
        }

        public void analyze() throws IOException {
            
            IndexDocument document = factory.createDocument(40); // TODO - measure!
            documents.add(document);

            Program program = result.getProgram();
            String processedFileURL = null;

            try {
                processedFileURL = result.getFile().getFileObject().getURL().toExternalForm();

            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            assert processedFileURL.startsWith("file:");
            String processedFileAbsPath = processedFileURL.substring("file:".length());
            StringBuilder includes = new StringBuilder();
            
            for (Statement statement : program.getStatements()){
                if (statement instanceof FunctionDeclaration){
                    indexFunction(FIELD_BASE, (FunctionDeclaration)statement, document);
                } else if (statement instanceof ExpressionStatement){
                    ExpressionStatement expressionStatement = (ExpressionStatement) statement;
                    if (expressionStatement.getExpression() instanceof Include) {
                        Include include = (Include) expressionStatement.getExpression();
                        if (include.getExpression() instanceof Scalar) {
                            Scalar scalar = (Scalar) include.getExpression();
                            String rawInclude = scalar.getStringValue();
                            String incl = PHPIndex.resolveRelativeURL(processedFileAbsPath , PHPIndex.dequote(rawInclude));
                            includes.append(incl + ";");
                        }
                    }
                    
                    indexConstant(statement, document);
                } else if (statement instanceof ClassDeclaration){
                    // create a new document for each class
                    IndexDocument classDocument = factory.createDocument(10);
                    documents.add(classDocument);
                    indexClass((ClassDeclaration)statement, classDocument);
                }
            }
            
            document.addPair(FIELD_INCLUDE, includes.toString(), true);
        }

        private void indexClass(ClassDeclaration classDeclaration, IndexDocument document) {
            StringBuilder classSignature = new StringBuilder();
            classSignature.append(classDeclaration.getName().getName() + ";"); //NOI18N
            classSignature.append(classDeclaration.getStartOffset() + ";"); //NOI18N
            document.addPair(FIELD_CLASS, classSignature.toString(), true);
            // index 
            
            classDeclaration.getSuperClass();
            
            for (Statement statement : classDeclaration.getBody().getStatements()){
                if (statement instanceof MethodDeclaration) {
                    MethodDeclaration methodDeclaration = (MethodDeclaration) statement;
                    indexFunction(FIELD_METHOD, methodDeclaration.getFunction(), document);
                }
                
                if (statement instanceof FieldsDeclaration) {
                    FieldsDeclaration fieldsDeclaration = (FieldsDeclaration) statement;
                    
                    for (SingleFieldDeclaration field : fieldsDeclaration.getFields()){
                        if (field.getName().getName() instanceof Identifier) {
                            Identifier identifier = (Identifier) field.getName().getName();
                            String fieldSignature = identifier.getName() + ";" + field.getStartOffset() + ";";
                            document.addPair(FIELD_FIELD, fieldSignature, true);
                        }
                    }
                }

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
                        if (invocation.getParameters().size() == 2) {
                            Expression paramExpr = invocation.getParameters().get(0);

                            if (paramExpr instanceof Scalar) {
                                String constName = ((Scalar) paramExpr).getStringValue();
                                char firstChar = constName.charAt(0);
                                
                                // check if const name is really quoted
                                if (firstChar == constName.charAt(constName.length() - 1) 
                                        && firstChar == '\'' || firstChar == '\"') {
                                    String defineVal = PHPIndex.dequote(constName);

                                    document.addPair(FIELD_CONST, defineVal + ";" + invocation.getStartOffset() + ";", true);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void indexFunction(String field, FunctionDeclaration functionDeclaration, IndexDocument document) {
            StringBuilder signature = new StringBuilder();
            signature.append(functionDeclaration.getFunctionName().getName() + ";");

            for (Iterator<FormalParameter> it = functionDeclaration.getFormalParameters().iterator(); it.hasNext();) {

                FormalParameter param = it.next();
                Expression paramNameExpr = param.getParameterName();
                String paramName = null;

                if (paramNameExpr instanceof Variable) {
                    Variable var = (Variable) paramNameExpr;
                    Identifier id = (Identifier) var.getName();
                    paramName = id.getName();
                }

                signature.append(paramName);

                if (it.hasNext()) {
                    signature.append(",");
                }
            }
            signature.append(";");
            signature.append(functionDeclaration.getStartOffset() + ";"); //NOI18N

            document.addPair(field, signature.toString(), true);
        }
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

    public boolean acceptQueryPath(String url) {
        return true;
    }
}
