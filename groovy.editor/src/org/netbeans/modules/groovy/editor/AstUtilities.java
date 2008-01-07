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
import java.util.List;
import javax.swing.text.Document;
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.treewalker.NodeCollector;
import org.codehaus.groovy.antlr.treewalker.PreOrderTraversal;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.parser.GroovyParserResult;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author Martin Adamek
 */
public class AstUtilities {
    
    

    public static int getAstOffset(CompilationInfo info, int lexOffset) {
        return info.getPositionManager().getAstOffset(info.getParserResult(), lexOffset);
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
                    Class c = Class.forName("org.netbeans.modules.ruby.RubyTestBase");
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

    // TODO use this from all the various places that have this inlined...
    public static ASTNode getRoot(CompilationInfo info) {
        ParserResult result = info.getParserResult();

        if (result == null) {
            return null;
        }

        return getRoot(result);
    }

    public static ASTNode getRoot(ParserResult r) {
        assert r instanceof GroovyParserResult;

        GroovyParserResult result = (GroovyParserResult)r;

        // TODO - just call result.getRoot()
        // but I might have to compensate for the new RootNode behavior in JRuby
        ParserResult.AstTreeNode ast = result.getAst();

        if (ast == null) {
            return null;
        }

        ASTNode root = (ASTNode)ast.getAstNode();

        return root;
    }

    public static OffsetRange getRange(ASTNode node, String text) {
        if (node instanceof MethodNode) {
            int start = getOffset(text, node.getLineNumber(), node.getColumnNumber());
            if (start < 0) {
                start = 0;
            }
            MethodNode methodNode = (MethodNode) node;
            return new OffsetRange(start, start + methodNode.getName().length());
        } else if (node instanceof FieldNode) {
            int start = getOffset(text, node.getLineNumber(), node.getColumnNumber());
            if (start < 0) {
                start = 0;
            }
            FieldNode fieldNode = (FieldNode) node;
            return new OffsetRange(start, start + fieldNode.getName().length());
        } else {
            int start = getOffset(text, node.getLineNumber(), node.getColumnNumber());
            int end = getOffset(text, node.getLastLineNumber(), node.getLastColumnNumber());
            return new OffsetRange(start, end);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static List<ASTNode> children(ASTNode root) {
        
        List<ASTNode> children = new ArrayList<ASTNode>();
        
        if (root instanceof ModuleNode) {
            ModuleNode moduleNode = (ModuleNode) root;
            children.addAll(moduleNode.getClasses());
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
        } else if (root instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) root;
            children.add(methodNode.getCode());
            for (Parameter parameter : methodNode.getParameters()) {
                children.add(parameter);
            }
        }
        
        return children;
    }
    
    /**
     * Find offset in text for given line and column
     * Probably terribly slow
     */
    public static int getOffset(String text, int lineNumber, int columnNumber) {
        assert lineNumber >= 0 : "Line number is negative: " + lineNumber;
        assert columnNumber >= 0 : "Column number is negative: " + columnNumber;
        // split text into lines
        String[] lines = text.split("\r\n|\n|\r"); // NOI18N
        
        assert lineNumber <= lines.length : "Line number is higher than lines in text: " + lineNumber;
        
//        final Logger LOG = Logger.getLogger(AstUtilities.class.getName());
//        LOG.log(Level.WARNING, "lineNumber   : " + lineNumber);
//        LOG.log(Level.WARNING, "lines.length : " + lines.length);
      
        int offset = 0;
        for (int i = 0; i < (lineNumber - 1); i++) {
            // increase offset by length of line + new line character lost in split() action
            offset += lines[i].length() + 1;
        }
        // increase offset by column number on given line
        offset += (columnNumber - 1);
        return offset;
    }
    
}
