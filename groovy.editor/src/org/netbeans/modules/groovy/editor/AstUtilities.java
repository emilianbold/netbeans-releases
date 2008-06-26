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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.groovy.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.parser.GroovyParserResult;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.groovy.editor.elements.AstElement;
import org.netbeans.modules.groovy.editor.elements.IndexedElement;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.parser.SourceUtils;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;

/**
 *
 * @author Martin Adamek
 */
public class AstUtilities {
    

    public static int getAstOffset(CompilationInfo info, int lexOffset) {
        ParserResult result = info.getEmbeddedResult(GroovyTokenId.GROOVY_MIME_TYPE, 0);
        if (result != null) {
            TranslatedSource ts = result.getTranslatedSource();
            if (ts != null) {
                return ts.getAstOffset(lexOffset);
            }
        }
              
        return lexOffset;
    }

    public static BaseDocument getBaseDocument(FileObject fileObject, boolean forceOpen) {
        DataObject dobj;

        try {
            dobj = DataObject.find(fileObject);

            EditorCookie ec = dobj.getCookie(EditorCookie.class);

            if (ec == null) {
                throw new IOException("Can't open " + fileObject.getNameExt());
            }

            Document document;

            if (forceOpen) {
                document = ec.openDocument();
            } else {
                document = ec.getDocument();
            }

            if (document instanceof BaseDocument) {
                return ((BaseDocument)document);
            } else {
                // Must be testsuite execution
                try {
                    Class c = Class.forName("org.netbeans.modules.groovy.editor.test.GroovyTestBase");
                    if (c != null) {
                        @SuppressWarnings("unchecked")
                        java.lang.reflect.Method m = c.getMethod("getDocumentFor", new Class[] { FileObject.class });
                        return (BaseDocument) m.invoke(null, (Object[])new FileObject[] { fileObject });
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        return null;
    }

    public static GroovyParserResult getParseResult(CompilationInfo info) {
        ParserResult result = info.getEmbeddedResult(GroovyTokenId.GROOVY_MIME_TYPE, 0);

        if (result == null) {
            return null;
        } else {
            return ((GroovyParserResult)result);
        }
    }

    // TODO use this from all the various places that have this inlined...
    public static ModuleNode getRoot(CompilationInfo info) {
        ParserResult result = info.getEmbeddedResult(GroovyTokenId.GROOVY_MIME_TYPE, 0);

        if (result == null) {
            return null;
        }

        return getRoot(result);
    }

    public static ModuleNode getRoot(ParserResult r) {
        assert r instanceof GroovyParserResult;

        GroovyParserResult result = (GroovyParserResult)r;

        ParserResult.AstTreeNode ast = result.getAst();

        if (ast == null) {
            return null;
        }

        return (ModuleNode) ast.getAstNode();
    }

    public static OffsetRange getRangeFull(ASTNode node, BaseDocument doc) {
            if (node.getLineNumber() < 0 || node.getColumnNumber() < 0 || node.getLastLineNumber() < 0 || node.getLastColumnNumber() < 0) {
                return OffsetRange.NONE;
            }
            int start = getOffset(doc, node.getLineNumber(), node.getColumnNumber());
            if (start < 0) {
                start = 0;
            }
            int end = getOffset(doc, node.getLastLineNumber(), node.getLastColumnNumber());
            if (end < 0) {
                end = 0;
            }
            return new OffsetRange(start, end);
    }
    
    public static OffsetRange getRange(ASTNode node, BaseDocument doc) {
        
        // Warning! The implicit class and some other nodes has line/column numbers below 1
        // if line is wrong, let's invalidate also column and vice versa
        int lineNumber = node.getLineNumber();
        int columnNumber = node.getColumnNumber();
        if (lineNumber < 1 || columnNumber < 1) {
            lineNumber = 1;
            columnNumber = 1;
        }

        if (node instanceof FieldNode) {
            int start = getOffset(doc, lineNumber, columnNumber);
            FieldNode fieldNode = (FieldNode) node;
            return new OffsetRange(start, start + fieldNode.getName().length());
        } else if (node instanceof ClassNode) {
            // ok, here we have to move the Range to the first character
            // after the "class" keyword, plus an indefinite nuber of spaces
            // FIXME: have to check what happens with other whitespaces between
            // the keyword and the identifier (like newline)
            
            // happens in some cases when groovy source uses some non-imported java class
            if (doc != null) {
                
                // if we are dealing with an empty groovy-file, we have take into consideration,
                // that even though we're running on an ClassNode, there is no "class " String 
                // in the sourcefile. So take doc.getLength() as maximum.
                
                int start = getOffset(doc, lineNumber, columnNumber) + "class".length(); // NOI18N
                int docLength = doc.getLength();
                
                if (start > docLength) {
                    start = docLength;
                } 
                
                try {
                    start = Utilities.getFirstNonWhiteFwd(doc, start);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                // This seems to happen every now and then ...
                if (start < 0){
                    start = 0;
                }

                ClassNode classNode = (ClassNode) node;
                
                int end = start + classNode.getNameWithoutPackage().length();
                
                if (end > docLength) {
                    end = docLength;
                }
                
                return new OffsetRange(start, end);
            }
        } else if (node instanceof ConstructorNode) {
            int start = getOffset(doc, lineNumber, columnNumber);
            ConstructorNode constructorNode = (ConstructorNode) node;
            return new OffsetRange(start, start + constructorNode.getDeclaringClass().getNameWithoutPackage().length());
        } else if (node instanceof MethodNode) {
            int start = getOffset(doc, lineNumber, columnNumber);
            MethodNode methodNode = (MethodNode) node;
            return new OffsetRange(start, start + methodNode.getName().length());
        } else if (node instanceof VariableExpression) {
            int start = getOffset(doc, lineNumber, columnNumber);
            // In case of variable in GString: "Hello, ${name}", node coordinates 
            // are suggesting '{' (it means begin and end colum info is wrong).
            // Pick up what we really want from this.
            try {
                if (lineNumber == node.getLastLineNumber() &&
                        (node.getLastColumnNumber() - columnNumber == 1) &&
                        "{".equals(doc.getText(start, 1))) {
                    start++;
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            VariableExpression variableExpression = (VariableExpression) node;
            return new OffsetRange(start, start + variableExpression.getName().length());
        } else if (node instanceof Parameter) {
            int end = getOffset(doc, node.getLastLineNumber(), node.getLastColumnNumber());
            Parameter parameter = (Parameter) node;
            String name = parameter.getName();
            if (end - name.length() < 0) {
                return OffsetRange.NONE;
            }
            return new OffsetRange(end - name.length(), end);
        } else if (node instanceof MethodCallExpression) {
            MethodCallExpression methodCall = (MethodCallExpression) node;
            Expression method = methodCall.getMethod();
            lineNumber = method.getLineNumber();
            columnNumber = method.getColumnNumber();
            if (lineNumber < 1 || columnNumber < 1) {
                lineNumber = 1;
                columnNumber = 1;
            }
            int start = getOffset(doc, lineNumber, columnNumber);
            return new OffsetRange(start, start + methodCall.getMethodAsString().length());
        } else if (node instanceof ClassExpression) {
            ClassExpression clazz = (ClassExpression) node;
            int start = getOffset(doc, lineNumber, columnNumber);
            return new OffsetRange(start, start + clazz.getText().length());
        }
        return OffsetRange.NONE;
    }
    
    @SuppressWarnings("unchecked")
    public static List<ASTNode> children(ASTNode root) {
        
        // Logger PRIV_LOG = Logger.getLogger(AstUtilities.class.getName());
        // PRIV_LOG.log(Level.FINEST, "children(ASTNode):Name" + root.getClass().getName() +":"+ root.getText());
        
        List<ASTNode> children = new ArrayList<ASTNode>();
        
        if (root instanceof ModuleNode) {
            ModuleNode moduleNode = (ModuleNode) root;
            children.addAll(moduleNode.getClasses());
            children.add(moduleNode.getStatementBlock());
        } else if (root instanceof ClassNode) {
            ClassNode classNode = (ClassNode) root;
            for (Object object : classNode.getMethods()) {
                MethodNode method = (MethodNode) object;
                // getMethods() returns all methods also from superclasses
                // how to get only methods from source?
                // for now, just check line number, if < 0 it is not from source
                if (method.getLineNumber() >= 0) {
                    children.add(method);
                }
            }
            for (Object object : classNode.getFields()) {
                FieldNode field = (FieldNode) object;
                if (field.getLineNumber() >= 0) {
                    children.add(field);
                }
            }
            
            for (Object object : classNode.getDeclaredConstructors()) {
                ConstructorNode constructor = (ConstructorNode) object;
                
                if (constructor.getLineNumber() >= 0) {
                    children.add(constructor);
                }
                // PRIV_LOG.log(Level.FINEST, "Constructor found: " + constructor.toString());
            }
            
            
            
        } else if (root instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) root;
            children.add(methodNode.getCode());
            for (Parameter parameter : methodNode.getParameters()) {
                children.add(parameter);
            }
        } else if (root instanceof Parameter) {
        } else if (root instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) root;
            Expression expression = fieldNode.getInitialExpression();
            if (expression != null) {
                children.add(expression);
            }
        } else if (root != null) {
            AstChildrenSupport astChildrenSupport = new AstChildrenSupport();
            root.visit(astChildrenSupport);
            children = astChildrenSupport.children();
        }
        
        // PRIV_LOG.log(Level.FINEST, "List:" + children.toString());
        return children;
    }
    
    /**
     * Find offset in text for given line and column
     * Never returns negative number
     */
    public static int getOffset(BaseDocument doc, int lineNumber, int columnNumber) {
        assert lineNumber > 0 : "Line number must be at least 1 and was: " + lineNumber;
        assert columnNumber > 0 : "Column number must be at least 1 ans was: " + columnNumber;

        int offset = Utilities.getRowStartFromLineOffset(doc, lineNumber - 1);
        offset += (columnNumber - 1);
        
        // some sanity checks 
        if (offset < 0){
            offset = 0;
        }
        
        return offset;
    }
    
    public static ASTNode getForeignNode(final IndexedElement o, ASTNode[] foreignRootRet) {

        final ASTNode[] nodes = new ASTNode[1];
        try {
            SourceUtils.runUserActionTask(o.getFileObject(), new CancellableTask<GroovyParserResult>() {
                public void run(GroovyParserResult result) throws Exception {
                    String signature = o.getSignature();
                    for (AstElement element : result.getStructure().getElements()) {
                        if (signature.equals(element.getSignature())) {
                            nodes[0] = element.getNode();
                        }
                    }
                }
                public void cancel() {}
            });
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return nodes[0];
    }
    
    public static String getDefSignature(MethodNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.getName());

        Parameter[] parameters = node.getParameters();
        if (parameters.length > 0) {
            sb.append('(');
            Iterator<Parameter> it = Arrays.asList(parameters).iterator();
            sb.append(it.next().getName());

            while (it.hasNext()) {
                sb.append(',');
                sb.append(it.next().getName());
            }
            sb.append(')');
        }

        return sb.toString();
    }

    
}
