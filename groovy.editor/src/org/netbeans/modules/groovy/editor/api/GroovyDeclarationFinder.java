/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.api;

import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.text.Document;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.elements.IndexedClass;
import org.netbeans.modules.groovy.editor.api.elements.IndexedElement;
import org.netbeans.modules.groovy.editor.api.elements.IndexedMethod;
import org.netbeans.modules.groovy.editor.api.lexer.Call;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
    
/**
 *
 * @author schmidtm
 * @author Martin Adamek
 */
public class GroovyDeclarationFinder implements DeclarationFinder {

    private final Logger LOG = Logger.getLogger(GroovyDeclarationFinder.class.getName());
    Token<? extends GroovyTokenId> tok;

    Document lastDoc = null;
    int lastOffset = -1;
    OffsetRange lastRange = OffsetRange.NONE;

    public GroovyDeclarationFinder() {
        LOG.setLevel(Level.OFF);
    }

    public OffsetRange getReferenceSpan(Document document, int lexOffset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);

        //BaseDocument doc = (BaseDocument)document;

        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(th, lexOffset);

        if (ts == null) {
            return OffsetRange.NONE;
        }

        ts.move(lexOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return OffsetRange.NONE;
        }

        // Determine whether the caret position is right between two tokens
        boolean isBetween = (lexOffset == ts.offset());

        OffsetRange range = getReferenceSpan(ts, th, lexOffset);

        if ((range == OffsetRange.NONE) && isBetween) {
            // The caret is between two tokens, and the token on the right
            // wasn't linkable. Try on the left instead.
            if (ts.movePrevious()) {
                range = getReferenceSpan(ts, th, lexOffset);
            }
        }

        return range;
    }

    public DeclarationLocation findDeclaration(ParserResult info, int lexOffset) {
        GroovyParserResult gpr = AstUtilities.getParseResult(info);

        try {
            Document document = LexUtilities.getDocument(gpr, false);
            if (document == null) {
                return DeclarationLocation.NONE;
            }
            TokenHierarchy<Document> th = TokenHierarchy.get(document);
            BaseDocument doc = (BaseDocument)document;

            int astOffset = AstUtilities.getAstOffset(info, lexOffset);
            if (astOffset == -1) {
                return DeclarationLocation.NONE;
            }

            OffsetRange range = getReferenceSpan(doc, lexOffset);

            if (range == OffsetRange.NONE) {
                return DeclarationLocation.NONE;
            }

            // Determine the bias (if the caret is between two tokens, did we
            // click on a link for the left or the right?
            boolean leftSide = range.getEnd() <= lexOffset;

            ASTNode root = AstUtilities.getRoot(info);

            // FIXME parsing API - source & binary IDs
            GroovyIndex index = GroovyIndex.get(QuerySupport.findRoots(info.getSnapshot().getSource().getFileObject(),
                    Collections.singleton(ClassPath.SOURCE), null, null));

            if (root == null) {
                // No parse tree - try to just use the syntax info to do a simple index lookup
                // for methods and classes
                String text = doc.getText(range.getStart(), range.getLength());

                if ((index == null) || (text.length() == 0)) {
                    return DeclarationLocation.NONE;
                }

                if (Character.isUpperCase(text.charAt(0))) {
                    // A Class or Constant?
                    Set<IndexedClass> classes =
                        index.getClasses(text, QuerySupport.Kind.EXACT, true, false, false);

                    if (classes.size() == 0) {
                        return DeclarationLocation.NONE;
                    }

                    DeclarationLocation l = getClassDeclaration(gpr, classes, null, null, index, doc);
                    if (l != null) {
                        return l;
                    }
                } else {
                    // A method?
                    Set<IndexedMethod> methods =
                        index.getMethods(text, null, QuerySupport.Kind.EXACT);

                    DeclarationLocation l = getMethodDeclaration(gpr, text, methods,
                         null, null, index, astOffset, lexOffset);

                    if (l != null) {
                        return l;
                    }
                } // TODO: @ - field?

                return DeclarationLocation.NONE;
            }

            int tokenOffset = lexOffset;

            if (leftSide && (tokenOffset > 0)) {
                tokenOffset--;
            }

            AstPath path = new AstPath(root, astOffset, doc);
            ASTNode closest = path.leaf();
            ASTNode parent = path.leafParent();

            if (closest instanceof ConstantExpression && parent instanceof MethodCallExpression) {

                String name = ((ConstantExpression) closest).getText();

                Call call = Call.getCallType(doc, th, lexOffset);

                String type = call.getType();
                String lhs = call.getLhs();

                MethodCallExpression methodCall = (MethodCallExpression) parent;
                Expression objectExpression = methodCall.getObjectExpression();
                if (objectExpression instanceof VariableExpression) {
                    VariableExpression variableExpression = (VariableExpression) objectExpression;
                    String typeName = variableExpression.getType().getName();

                    // try to find it in Java
                    FileObject fo = gpr.getSnapshot().getSource().getFileObject();
                    if (fo != null) {
                        final ClasspathInfo cpInfo = ClasspathInfo.create(fo);
                        DeclarationLocation location = findJavaMethod(cpInfo, typeName, methodCall);
                        if (location != DeclarationLocation.NONE) {
                            return location;
                        }
                    }
                }

                if ((type == null) && (lhs != null) && (closest != null) && call.isSimpleIdentifier()) {
                    assert root instanceof ModuleNode;
                    ModuleNode moduleNode = (ModuleNode) root;
                    VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(moduleNode.getContext(), path, doc, lexOffset);
                    scopeVisitor.collect();
                }
                if (type == null) {
                    String fqn = AstUtilities.getFqnName(path);
                    if (call == Call.LOCAL && fqn != null && fqn.length() == 0) {
                        fqn = "java.lang.Object"; // NOI18N
                    }

                    return findMethod(name, fqn, type, call, gpr, astOffset, lexOffset, path, closest, index);
                }

            } else if (closest instanceof VariableExpression) {
                VariableExpression variableExpression = (VariableExpression) closest;
                ASTNode scope = AstUtilities.getScope(path, variableExpression);
                if (scope != null) {
                    ASTNode variable = AstUtilities.getVariable(scope, variableExpression.getName(), path, doc, lexOffset);
                    if (variable != null) {
                        // I am using getRange and not getOffset, because getRange is adding 'def_' to offset of field
                        int offset = AstUtilities.getRange(variable, doc).getStart();
                        // FIXME parsing API
                        return new DeclarationLocation(info.getSnapshot().getSource().getFileObject(), offset);
                    }
                }
            } else if (closest instanceof ConstantExpression && parent instanceof PropertyExpression) {
                PropertyExpression propertyExpression = (PropertyExpression) parent;
                Expression objectExpression = propertyExpression.getObjectExpression();
                Expression property = propertyExpression.getProperty();
                if (objectExpression instanceof VariableExpression && property instanceof ConstantExpression) {
                    VariableExpression variableExpression = (VariableExpression) objectExpression;
                    if ("this".equals(variableExpression.getName())) { // NOI18N
                        ASTNode scope = AstUtilities.getOwningClass(path);
                        if (scope == null) {
                            // we are in script?
                            scope = (ModuleNode) path.root();
                        }
                        ASTNode variable = AstUtilities.getVariable(scope, ((ConstantExpression) property).getText(), path, doc, lexOffset);
                        if (variable != null) {
                            int offset = AstUtilities.getOffset(doc, variable.getLineNumber(), variable.getColumnNumber());
                            // FIXME parsing API
                            return new DeclarationLocation(info.getSnapshot().getSource().getFileObject(), offset);
                        }
                    } else {
                        // find variable type
                        ClassNode type = variableExpression.getType();
                        String typeName = type.getName();
                        String fieldName = ((ConstantExpression) closest).getText();

                        // try to find it in Java
                        FileObject fo = gpr.getSnapshot().getSource().getFileObject();
                        if (fo != null) {
                            final ClasspathInfo cpInfo = ClasspathInfo.create(fo);
                            DeclarationLocation location = findJavaField(cpInfo, typeName, fieldName);
                            if (location != DeclarationLocation.NONE) {
                                return location;
                            }
                        }

                        // TODO try to find it in Groovy
                    }
                }
            } else if (closest instanceof DeclarationExpression ||
                closest instanceof ConstructorCallExpression ||
                closest instanceof ClassExpression || 
                closest instanceof FieldNode) {

                String fqName = getFqNameForNode(closest);
                
                LOG.log(Level.FINEST, "Looking for type: {0}", fqName); // NOI18N
                
                if (doc != null && range != null) {
                    String text = doc.getText(range.getStart(), range.getLength());

                    Set<IndexedClass> classes =
                        index.getClasses(text, QuerySupport.Kind.EXACT, true, false, false);

                    for (IndexedClass indexedClass : classes) {
                        ASTNode node = AstUtilities.getForeignNode(indexedClass);
                        if (node != null) {
                            OffsetRange defRange = null;
                            try {
                                defRange = AstUtilities.getRange(node, (BaseDocument) indexedClass.getDocument());
                            } catch (IOException ex) {
                                LOG.log(Level.FINEST, "IOException while getting destination range : {0}", ex.getMessage()); // NOI18N
                            }
                            if (defRange != null) {
                                LOG.log(Level.FINEST, "Found decl. for : {0}", text); // NOI18N
                                LOG.log(Level.FINEST, "Foreign Node    : {0}", node); // NOI18N
                                LOG.log(Level.FINEST, "Range start     : {0}", defRange.getStart()); // NOI18N
                                
                                return new DeclarationLocation(indexedClass.getFileObject(), defRange.getStart());
                            }
                        }
                    }

                    // so - we haven't found this class using the groovy index, 
                    // then we have to search it as a pure java type.
                    
                    // simple sanity-check that the literal string in the source document
                    // matches the last part of the full-qualified name of the type.
                    // e.g. "String" means "java.lang.String"
                    
                    if(!NbUtilities.stripPackage(fqName).equals(text)){
                        LOG.log(Level.FINEST, "fqName != text");
                        return DeclarationLocation.NONE;
                    }
                    
                    FileObject fileObject = info.getSnapshot().getSource().getFileObject();
                    
                    if (fileObject != null) {
                        final ClasspathInfo cpi = ClasspathInfo.create(fileObject);

                        if (cpi != null) {
                            JavaSource javaSource = JavaSource.create(cpi);

                            if (javaSource != null) {
                                
                                try {
                                    javaSource.runUserActionTask(new SourceLocator(fqName, cpi), false);
                                } catch (IOException ex) {
                                    LOG.log(Level.FINEST, "Problem in runUserActionTask :  {0}", ex.getMessage());
                                    return null;
                                }

                            } else {
                                LOG.log(Level.FINEST, "javaSource == null"); // NOI18N
                            }
                        } else {
                            LOG.log(Level.FINEST, "classpathinfo == null"); // NOI18N
                        }
                    } else {
                        LOG.log(Level.FINEST, "fileObject == null"); // NOI18N
                    }

                    return DeclarationLocation.NONE;
                    
                }

            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
        return DeclarationLocation.NONE;
    }
    
    
    private String getFqNameForNode(ASTNode node) {
        if (node instanceof DeclarationExpression) {
            return ((BinaryExpression) node).getLeftExpression().getType().getName();
        } else if (node instanceof Expression) {
            return ((Expression) node).getType().getName();
        } else if (node instanceof FieldNode) {
            return ((FieldNode) node).getType().getName();
        }

        return "";
    }

    /**
     * Locates and opens in Editor the Java Element given as Full-Qualified name in fqName.
     */
    private class SourceLocator implements Task<CompilationController> {
        
        String fqName;
        ClasspathInfo cpi;

        public SourceLocator(String fqName, ClasspathInfo cpi) {
            this.fqName = fqName;
            this.cpi = cpi;
        }

        public void run(CompilationController info) throws Exception {
            
            Elements elements = info.getElements();

            if (elements != null) {
                final javax.lang.model.element.TypeElement typeElement = elements.getTypeElement(fqName);

                if (typeElement != null) {

                    if (!SwingUtilities.isEventDispatchThread()) {

                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                ElementOpen.open(cpi, typeElement);
                            }
                        });

                    } else {
                        ElementOpen.open(cpi, typeElement);
                    }

                } else {
                    LOG.log(Level.FINEST, "typeElement == null"); // NOI18N
                }
            } else {
                LOG.log(Level.FINEST, "elements == null"); // NOI18N
            }
            
        }
    }
    
    
    
    private OffsetRange getReferenceSpan(TokenSequence<?> ts, TokenHierarchy<Document> th, int lexOffset) {
        Token<?> token = ts.token();
        TokenId id = token.id();

        if (id == GroovyTokenId.IDENTIFIER) {
            if (token.length() == 1 && id == GroovyTokenId.IDENTIFIER && token.text().toString().equals(",")) {
                assert false : "Never planned to be here";
                return OffsetRange.NONE;
            }
        }

        // TODO: Tokens.SUPER, Tokens.THIS, Tokens.SELF ...
        if (id == GroovyTokenId.IDENTIFIER) {
            return new OffsetRange(ts.offset(), ts.offset() + token.length());
        }

        return OffsetRange.NONE;
    }

    private DeclarationLocation getClassDeclaration(GroovyParserResult info, Set<IndexedClass> classes,
            AstPath path, ASTNode closest, GroovyIndex index, BaseDocument doc) {
        final IndexedClass candidate =
            findBestClassMatch(classes, path, closest, index);

        if (candidate != null) {
            IndexedElement com = candidate;
            ASTNode node = AstUtilities.getForeignNode(com);

            DeclarationLocation loc = new DeclarationLocation(com.getFileObject(),
                AstUtilities.getOffset(doc, node.getLineNumber(), node.getColumnNumber()), com);

            return loc;
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findMethod(String name, String possibleFqn, String type, Call call,
        GroovyParserResult info, int caretOffset, int lexOffset, AstPath path, ASTNode closest, GroovyIndex index) {
        Set<IndexedMethod> methods = getApplicableMethods(name, possibleFqn, type, call, index);

        int astOffset = caretOffset;
        DeclarationLocation l = getMethodDeclaration(info, name, methods,
             path, closest, index, astOffset, lexOffset);

        return l;
    }

    private Set<IndexedMethod> getApplicableMethods(String name, String possibleFqn,
            String type, Call call, GroovyIndex index) {
        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();
        String fqn = possibleFqn;
        if (type == null && possibleFqn != null && call.getLhs() == null && call != Call.UNKNOWN) {
            fqn = possibleFqn;

            // methods directly from fqn class
            if (methods.size() == 0) {
                methods = index.getMethods(name, fqn, QuerySupport.Kind.EXACT);
            }

            methods = index.getInheritedMethods(fqn, name, QuerySupport.Kind.EXACT);
        }

        if (type != null && methods.size() == 0) {
            fqn = possibleFqn;

            if (methods.size() == 0) {
                methods = index.getInheritedMethods(fqn + "." + type, name, QuerySupport.Kind.EXACT);
            }

            if (methods.size() == 0) {
                // Add methods in the class (without an FQN)
                methods = index.getInheritedMethods(type, name, QuerySupport.Kind.EXACT);

                if (methods.size() == 0 && type.indexOf(".") == -1) {
                    // Perhaps we specified a class without its FQN, such as "TableDefinition"
                    // -- go and look for the full FQN and add in all the matches from there
                    Set<IndexedClass> classes = index.getClasses(type, QuerySupport.Kind.EXACT, false, false, false);
                    Set<String> fqns = new HashSet<String>();
                    for (IndexedClass cls : classes) {
                        String f = cls.getFqn();
                        if (f != null) {
                            fqns.add(f);
                        }
                    }
                    for (String f : fqns) {
                        if (!f.equals(type)) {
                            methods.addAll(index.getInheritedMethods(f, name, QuerySupport.Kind.EXACT));
                        }
                    }
                }
            }

            // Fall back to ALL methods across classes
            // Try looking at the libraries too
            if (methods.size() == 0) {
                fqn = possibleFqn;
                while ((methods.size() == 0) && fqn != null && (fqn.length() > 0)) {
                    methods = index.getMethods(name, fqn + "." + type, QuerySupport.Kind.EXACT);

                    int f = fqn.lastIndexOf(".");

                    if (f == -1) {
                        break;
                    } else {
                        fqn = fqn.substring(0, f);
                    }
                }
            }
        }

        if (methods.size() == 0) {
            methods = index.getMethods(name, type, QuerySupport.Kind.EXACT);
            if (methods.size() == 0 && type != null) {
                methods = index.getMethods(name, null, QuerySupport.Kind.EXACT);
            }
        }

        return methods;
    }

    private DeclarationLocation getMethodDeclaration(GroovyParserResult info, String name, Set<IndexedMethod> methods,
            AstPath path, ASTNode closest, GroovyIndex index, int astOffset, int lexOffset) {
        BaseDocument doc = LexUtilities.getDocument(info, false);
        if (doc == null) {
            return DeclarationLocation.NONE;
        }

        IndexedMethod candidate =
            findBestMethodMatch(name, methods, doc,
                astOffset, lexOffset, path, closest, index);

        if (candidate != null) {
            FileObject fileObject = candidate.getFileObject();
            if (fileObject == null) {
                return DeclarationLocation.NONE;
            }

            ASTNode node = AstUtilities.getForeignNode(candidate);
            // negative line/column can happen due to bugs in groovy parser
            int nodeOffset = (node != null && node.getLineNumber() > 0 && node.getColumnNumber() > 0)
                    ? AstUtilities.getOffset(doc, node.getLineNumber(), node.getColumnNumber())
                    : 0;

            DeclarationLocation loc = new DeclarationLocation(
                fileObject, nodeOffset, candidate);

            return loc;
        }

        return DeclarationLocation.NONE;
    }

    IndexedClass findBestClassMatch(Set<IndexedClass> classSet,
        AstPath path, ASTNode reference, GroovyIndex index) {
        // Make sure that the best fit method actually has a corresponding valid source location
        // and parse tree
        Set<IndexedClass> classes = new HashSet<IndexedClass>(classSet);

        while (!classes.isEmpty()) {
            IndexedClass clz = findBestClassMatchHelper(classes, path, reference, index);
            if (clz == null) {
                return null;
            }
            ASTNode node = AstUtilities.getForeignNode(clz);

            if (node != null) {
                return clz;
            }

            // TODO: Sort results, then pick candidate number modulo methodSelector
            if (!classes.contains(clz)) {
                // Avoid infinite loop when we somehow don't find the node for
                // the best class and we keep trying it
                classes.remove(classes.iterator().next());
            } else {
                classes.remove(clz);
            }
        }

        return null;
    }

    private IndexedClass findBestClassMatchHelper(Set<IndexedClass> classes,
        AstPath path, ASTNode reference, GroovyIndex index) {
        return null;
    }

    IndexedMethod findBestMethodMatch(String name, Set<IndexedMethod> methodSet,
        BaseDocument doc, int astOffset, int lexOffset, AstPath path, ASTNode call, GroovyIndex index) {
        // Make sure that the best fit method actually has a corresponding valid source location
        // and parse tree

        Set<IndexedMethod> methods = new HashSet<IndexedMethod>(methodSet);

        while (!methods.isEmpty()) {
            IndexedMethod method =
                findBestMethodMatchHelper(name, methods, doc, astOffset, lexOffset, path, call, index);
            ASTNode node = method == null ? null : AstUtilities.getForeignNode(method);

            if (node != null) {
                return method;
            }

            if (!methods.contains(method)) {
                // Avoid infinite loop when we somehow don't find the node for
                // the best method and we keep trying it
                methods.remove(methods.iterator().next());
            } else {
                methods.remove(method);
            }
        }

        // Dynamic methods that don't have source (such as the TableDefinition methods "binary", "boolean", etc.
        if (methodSet.size() > 0) {
            return methodSet.iterator().next();
        }

        return null;
    }

    private IndexedMethod findBestMethodMatchHelper(String name, Set<IndexedMethod> methods,
        BaseDocument doc, int astOffset, int lexOffset, AstPath path, ASTNode callNode, GroovyIndex index) {

        Set<IndexedMethod> candidates = new HashSet<IndexedMethod>();

        if(path == null) {
            return null;
        }

        ASTNode parent = path.leafParent();

        if (callNode instanceof ConstantExpression && parent instanceof MethodCallExpression) {

            String fqn = null;

            MethodCallExpression methodCall = (MethodCallExpression) parent;
            Expression objectExpression = methodCall.getObjectExpression();
            if (objectExpression instanceof VariableExpression) {
                VariableExpression variable = (VariableExpression) objectExpression;
                if ("this".equals(variable.getName())) { // NOI18N
                    fqn = AstUtilities.getFqnName(path);
                } else {
                    fqn = variable.getType().getName();
                }
            }
            if (fqn != null) {
                for (IndexedMethod method : methods) {
                    if (fqn.equals(method.getIn())) {
                        candidates.add(method);
                    }
                }
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            methods = candidates;
        }

        return null;
    }

//    private DeclarationLocation fix(DeclarationLocation location, CompilationInfo info) {
//        if ((location != DeclarationLocation.NONE) && (location.getFileObject() == null) &&
//                (location.getUrl() == null)) {
//            return new DeclarationLocation(info.getFileObject(), location.getOffset(), location.getElement());
//        }
//
//        return location;
//    }

    private static DeclarationLocation findJavaField(ClasspathInfo cpInfo, final String fqn, final String fieldName) {
        final ElementHandle[] handles = new ElementHandle[1];
        final int[] offset = new int[1];
        JavaSource javaSource = JavaSource.create(cpInfo);
        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = controller.getElements().getTypeElement(fqn);
                    if (typeElement != null) {
                        for (VariableElement variable : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                            if (variable.getSimpleName().contentEquals(fieldName)) {
                                handles[0] = ElementHandle.create(variable);
                            }
                        }
                    }
                }
            }, true);
            if (handles[0] != null) {
                FileObject fileObject = SourceUtils.getFile(handles[0], cpInfo);
                if (fileObject != null) {
                    javaSource = JavaSource.forFileObject(fileObject);
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController controller) throws Exception {
                            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element element = handles[0].resolve(controller);
                            Trees trees = controller.getTrees();
                            Tree tree = trees.getTree(element);
                            SourcePositions sourcePositions = trees.getSourcePositions();
                            offset[0] = (int) sourcePositions.getStartPosition(controller.getCompilationUnit(), tree);
                        }
                    }, true);
                    return new DeclarationLocation(fileObject, offset[0]);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return DeclarationLocation.NONE;
    }

    private static DeclarationLocation findJavaMethod(ClasspathInfo cpInfo, final String fqn, final MethodCallExpression methodCall) {
        final ElementHandle[] handles = new ElementHandle[1];
        final int[] offset = new int[1];
        JavaSource javaSource = JavaSource.create(cpInfo);
        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = controller.getElements().getTypeElement(fqn);
                    if (typeElement != null) {
                        for (ExecutableElement javaMethod : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                            if (Methods.isSameMethod(javaMethod, methodCall)) {
                                handles[0] = ElementHandle.create(javaMethod);
                            }
                        }
                    }
                }
            }, true);
            if (handles[0] != null) {
                FileObject fileObject = SourceUtils.getFile(handles[0], cpInfo);
                if (fileObject != null) {
                    javaSource = JavaSource.forFileObject(fileObject);
                    if (javaSource != null) {
                        javaSource.runUserActionTask(new Task<CompilationController>() {
                            public void run(CompilationController controller) throws Exception {
                                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                Element element = handles[0].resolve(controller);
                                Trees trees = controller.getTrees();
                                Tree tree = trees.getTree(element);
                                SourcePositions sourcePositions = trees.getSourcePositions();
                                offset[0] = (int) sourcePositions.getStartPosition(controller.getCompilationUnit(), tree);
                            }
                        }, true);
                    }
                    return new DeclarationLocation(fileObject, offset[0]);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return DeclarationLocation.NONE;
    }

}
