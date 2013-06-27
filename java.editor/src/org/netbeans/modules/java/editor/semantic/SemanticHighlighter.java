/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.imports.UnusedImports;
import org.netbeans.modules.java.editor.semantic.ColoringAttributes.Coloring;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;


/**
 *
 * @author Jan Lahoda
 */
public class SemanticHighlighter extends JavaParserResultTask {
    
    public static List<TreePathHandle> computeUnusedImports(CompilationInfo info) throws IOException {
        final List<TreePathHandle> result = new ArrayList<TreePathHandle>();
        
        for (TreePath unused : UnusedImports.process(info, new AtomicBoolean())) {
            result.add(TreePathHandle.create(unused, info));
        }
        
        return result;
    }
    
    private FileObject file;
    //XXX: correct rescheduling when troubles traversing token list!
//    private SemanticHighlighterFactory fact;
    private AtomicBoolean cancel = new AtomicBoolean();
    
//    SemanticHighlighter(FileObject file) {
//        this(file, null);
//    }
//    
    SemanticHighlighter(FileObject file/*, SemanticHighlighterFactory fact*/) {
        super(Phase.RESOLVED, TaskIndexingMode.ALLOWED_DURING_SCAN);
        this.file = file;
//        this.fact = fact;
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        CompilationInfo info = CompilationInfo.get(result);
        
        if (info == null) {
            return ;
        }
        
        cancel.set(false);
        
        final Document doc = result.getSnapshot().getSource().getDocument(false);
        
        if (!verifyDocument(doc)) return;

        if (process(info, doc)/* && fact != null*/) {
//            fact.rescheduleImpl(file);
        }
    }

    private static boolean verifyDocument(final Document doc) {
        if (doc == null) {
            Logger.getLogger(SemanticHighlighter.class.getName()).log(Level.FINE, "SemanticHighlighter: Cannot get document!");
            return false;
        }

        final boolean[] tokenSequenceNull =  new boolean[1];
        doc.render(new Runnable() {
            public void run() {
                tokenSequenceNull[0] = (TokenHierarchy.get(doc).tokenSequence() == null);
            }
        });
        if (tokenSequenceNull[0]) {
            return false;
        }
        
        return true;
    }
    
    public void cancel() {
        cancel.set(true);
    }
    

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }
    

    
    boolean process(CompilationInfo info, final Document doc) {
        return process(info, doc, ERROR_DESCRIPTION_SETTER);
    }
    
    static Coloring collection2Coloring(Collection<ColoringAttributes> attr) {
        Coloring c = ColoringAttributes.empty();
        
        for (ColoringAttributes a : attr) {
            c = ColoringAttributes.add(c, a);
        }
        
        return c;
    }
    
    boolean process(CompilationInfo info, final Document doc, ErrorDescriptionSetter setter) {
        DetectorVisitor v = new DetectorVisitor(info, doc, cancel);
        
        long start = System.currentTimeMillis();
        
        Map<Token, Coloring> newColoring = new IdentityHashMap<Token, Coloring>();
        List<ErrorDescription> errors = new ArrayList<ErrorDescription>();

        CompilationUnitTree cu = info.getCompilationUnit();
        
        v.scan(cu, null);
        
        if (cancel.get())
            return true;
        
        boolean computeUnusedImports = "text/x-java".equals(FileUtil.getMIMEType(info.getFileObject()));
        
        final List<TreePathHandle> allUnusedImports = computeUnusedImports ? new ArrayList<TreePathHandle>() : null;
        OffsetsBag imports = computeUnusedImports ? new OffsetsBag(doc) : null;

        if (computeUnusedImports) {
            Coloring unused = ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.UNUSED);
            Collection<TreePath> unusedImports = UnusedImports.process(info, cancel);

            if (unusedImports == null) return true;
            
            for (TreePath tree : unusedImports) {
                if (cancel.get()) {
                    return true;
                }

                //XXX: finish
                final int startPos = (int) info.getTrees().getSourcePositions().getStartPosition(cu, tree.getLeaf());
                final int endPos = (int) info.getTrees().getSourcePositions().getEndPosition(cu, tree.getLeaf());

                imports.addHighlight(startPos, endPos, ColoringManager.getColoringImpl(unused));

                TreePathHandle handle = TreePathHandle.create(tree, info);

                allUnusedImports.add(handle);
            }
        }
        
        Map<Token, Coloring> oldColors = LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, doc).getColorings();
        Map<Token, Coloring> removedTokens = new IdentityHashMap<Token, Coloring>(oldColors);
        Set<Token> addedTokens = new HashSet<Token>();
        
        for (Element decl : v.type2Uses.keySet()) {
            if (cancel.get())
                return true;
            
            List<Use> uses = v.type2Uses.get(decl);
            
            for (Use u : uses) {
                if (u.spec == null)
                    continue;
                
                if (u.type.contains(UseTypes.DECLARATION) && org.netbeans.modules.java.editor.semantic.Utilities.isPrivateElement(decl)) {
                    if ((decl.getKind().isField() && !isSerialVersionUID(info, decl)) || isLocalVariableClosure(decl)) {
                        if (!hasAllTypes(uses, EnumSet.of(UseTypes.READ, UseTypes.WRITE))) {
                            u.spec.add(ColoringAttributes.UNUSED);
                        }
                    }
                    
                    if ((decl.getKind() == ElementKind.CONSTRUCTOR && !decl.getModifiers().contains(Modifier.PRIVATE)) || decl.getKind() == ElementKind.METHOD) {
                        if (!hasAllTypes(uses, EnumSet.of(UseTypes.EXECUTE))) {
                            u.spec.add(ColoringAttributes.UNUSED);
                        }
                    }
                    
                    if (decl.getKind().isClass() || decl.getKind().isInterface()) {
                        if (!hasAllTypes(uses, EnumSet.of(UseTypes.CLASS_USE))) {
                            u.spec.add(ColoringAttributes.UNUSED);
                        }
                    }
                }
                
                Coloring c = collection2Coloring(u.spec);
                
                Token t = v.tree2Token.get(u.tree.getLeaf());
                
                if (t != null) {
                    newColoring.put(t, c);

                    Coloring oldColoring = removedTokens.remove(t);

                    if (oldColoring == null || !oldColoring.equals(c)) {
                        addedTokens.add(t);
                    }
                }
            }
        }
        
        if (cancel.get())
            return true;
        
        if (computeUnusedImports) {
            setter.setErrors(doc, errors, allUnusedImports);
            setter.setHighlights(doc, imports);
        }

        setter.setColorings(doc, newColoring, addedTokens, removedTokens.keySet());

        Logger.getLogger("TIMER").log(Level.FINE, "Semantic",
            new Object[] {NbEditorUtilities.getFileObject(doc), System.currentTimeMillis() - start});
        
        return false;
    }
    
        
    private boolean hasAllTypes(List<Use> uses, Collection<UseTypes> types) {
        EnumSet e = EnumSet.copyOf(types);
        
        for (Use u : uses) {
            if (types.isEmpty()) {
                return true;
            }
            
            types.removeAll(u.type);
        }
        
        return types.isEmpty();
    }
    
    private enum UseTypes {
        READ, WRITE, EXECUTE, DECLARATION, CLASS_USE;
    }
    
    private static boolean isLocalVariableClosure(Element el) {
        return el.getKind() == ElementKind.PARAMETER || el.getKind() == ElementKind.LOCAL_VARIABLE
                || el.getKind() == ElementKind.RESOURCE_VARIABLE || el.getKind() == ElementKind.EXCEPTION_PARAMETER;
    }
    
    /** Detects static final long SerialVersionUID 
     * @return true if element is final static long serialVersionUID
     */
    private static boolean isSerialVersionUID(CompilationInfo info, Element el) {
        if (el.getKind().isField() && el.getModifiers().contains(Modifier.FINAL) 
                && el.getModifiers().contains(Modifier.STATIC)
                && info.getTypes().getPrimitiveType(TypeKind.LONG).equals(el.asType())
                && el.getSimpleName().toString().equals("serialVersionUID"))
            return true;
        else
            return false;
    }
        
    private static class Use {
        private Collection<UseTypes> type;
        private TreePath     tree;
        private Collection<ColoringAttributes> spec;
        
        public Use(Collection<UseTypes> type, TreePath tree, Collection<ColoringAttributes> spec) {
            this.type = type;
            this.tree = tree;
            this.spec = spec;
        }
        
        @Override
        public String toString() {
            return "Use: " + type;
        }
    }
    
    private static class DetectorVisitor extends CancellableTreePathScanner<Void, EnumSet<UseTypes>> {
        
        private org.netbeans.api.java.source.CompilationInfo info;
        private Document doc;
        private Map<Element, List<Use>> type2Uses;
        
        private Map<Tree, Token> tree2Token;
        private TokenList tl;
        private long memberSelectBypass = -1;
        
        private SourcePositions sourcePositions;
        private ExecutableElement recursionDetector;
        
        private DetectorVisitor(org.netbeans.api.java.source.CompilationInfo info, final Document doc, AtomicBoolean cancel) {
            super(cancel);
            
            this.info = info;
            this.doc  = doc;
            type2Uses = new HashMap<Element, List<Use>>();
            
            tree2Token = new IdentityHashMap<Tree, Token>();
            
            tl = new TokenList(info, doc, cancel);
            
            this.sourcePositions = info.getTrees().getSourcePositions();
//            this.pos = pos;
        }
        
        private void firstIdentifier(String name) {
            tl.firstIdentifier(getCurrentPath(), name, tree2Token);
        }
        
        @Override
        public Void visitAssignment(AssignmentTree tree, EnumSet<UseTypes> d) {
            handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getVariable()), EnumSet.of(UseTypes.WRITE));
            
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), expr);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            scan(tree.getVariable(), EnumSet.of(UseTypes.WRITE));
            scan(tree.getExpression(), EnumSet.of(UseTypes.READ));
            
            return null;
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree tree, EnumSet<UseTypes> d) {
            Set<UseTypes> useTypes = EnumSet.of(UseTypes.WRITE);
            
            if (d != null) {
                useTypes.addAll(d);
            }
            
            handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getVariable()), useTypes);
            
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), expr);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            scan(tree.getVariable(), EnumSet.of(UseTypes.WRITE));
            scan(tree.getExpression(), EnumSet.of(UseTypes.READ));
            
            return null;
        }

        @Override
        public Void visitReturn(ReturnTree tree, EnumSet<UseTypes> d) {
            if (tree.getExpression() instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            }
            
            super.visitReturn(tree, EnumSet.of(UseTypes.READ));
            return null;
        }
        
        @Override
        public Void visitMemberSelect(MemberSelectTree tree, EnumSet<UseTypes> d) {
            long memberSelectBypassLoc = memberSelectBypass;
            
            memberSelectBypass = -1;
            
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), expr);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getKind().isField()) {
                handlePossibleIdentifier(getCurrentPath(), d == null ? EnumSet.of(UseTypes.READ) : d);
            }
	    
	    if (el != null && (el.getKind().isClass() || el.getKind().isInterface()) && 
		    getCurrentPath().getParentPath().getLeaf().getKind() != Kind.NEW_CLASS) {
		handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.CLASS_USE));
	    }
	    
//            System.err.println("XXXX=" + tree.toString());
//            System.err.println("YYYY=" + info.getElement(tree));
            
            super.visitMemberSelect(tree, d);
            
            tl.moveToEnd(tree.getExpression());
            
            if (memberSelectBypassLoc != (-1)) {
                tl.moveToOffset(memberSelectBypassLoc);
            }
            
            firstIdentifier(tree.getIdentifier().toString());
            
            return null;
        }
        
        private void addModifiers(Element decl, Collection<ColoringAttributes> c) {
            if (decl.getModifiers().contains(Modifier.STATIC)) {
                c.add(ColoringAttributes.STATIC);
            }
            
            if (decl.getModifiers().contains(Modifier.ABSTRACT) && !decl.getKind().isInterface()) {
                c.add(ColoringAttributes.ABSTRACT);
            }
            
            boolean accessModifier = false;
            
            if (decl.getModifiers().contains(Modifier.PUBLIC)) {
                c.add(ColoringAttributes.PUBLIC);
                accessModifier = true;
            }
            
            if (decl.getModifiers().contains(Modifier.PROTECTED)) {
                c.add(ColoringAttributes.PROTECTED);
                accessModifier = true;
            }
            
            if (decl.getModifiers().contains(Modifier.PRIVATE)) {
                c.add(ColoringAttributes.PRIVATE);
                accessModifier = true;
            }
            
            if (!accessModifier && !isLocalVariableClosure(decl)) {
                c.add(ColoringAttributes.PACKAGE_PRIVATE);
            }
            
            if (info.getElements().isDeprecated(decl)) {
                c.add(ColoringAttributes.DEPRECATED);
            }
        }
        
        private Collection<ColoringAttributes> getMethodColoring(ExecutableElement mdecl, boolean nct) {
            Collection<ColoringAttributes> c = new ArrayList<ColoringAttributes>();
            
            addModifiers(mdecl, c);
            
            if (mdecl.getKind() == ElementKind.CONSTRUCTOR) {
                c.add(ColoringAttributes.CONSTRUCTOR);

                //#146820:
                if (nct && mdecl.getEnclosingElement() != null && info.getElements().isDeprecated(mdecl.getEnclosingElement())) {
                    c.add(ColoringAttributes.DEPRECATED);
                }
            } else
                c.add(ColoringAttributes.METHOD);
            
            return c;
        }
        
        private Collection<ColoringAttributes> getVariableColoring(Element decl) {
            Collection<ColoringAttributes> c = new ArrayList<ColoringAttributes>();
            
            addModifiers(decl, c);
            
            if (decl.getKind().isField()) {
                c.add(ColoringAttributes.FIELD);
                
                return c;
            }
            
            if (decl.getKind() == ElementKind.LOCAL_VARIABLE || decl.getKind() == ElementKind.RESOURCE_VARIABLE
                    || decl.getKind() == ElementKind.EXCEPTION_PARAMETER) {
                c.add(ColoringAttributes.LOCAL_VARIABLE);
                
                return c;
            }
            
            if (decl.getKind() == ElementKind.PARAMETER) {
                c.add(ColoringAttributes.PARAMETER);
                
                return c;
            }
            
            assert false;
            
            return null;
        }

        private static final Set<Kind> LITERALS = EnumSet.of(Kind.BOOLEAN_LITERAL, Kind.CHAR_LITERAL, Kind.DOUBLE_LITERAL, Kind.FLOAT_LITERAL, Kind.INT_LITERAL, Kind.LONG_LITERAL, Kind.STRING_LITERAL);

        private void handlePossibleIdentifier(TreePath expr, Collection<UseTypes> type) {
            handlePossibleIdentifier(expr, type, null, false, false);
        }
        
        private void handlePossibleIdentifier(TreePath expr, Collection<UseTypes> type, Element decl, boolean providesDecl, boolean nct) {
            
            if (Utilities.isKeyword(expr.getLeaf())) {
                //ignore keywords:
                return ;
            }

            if (expr.getLeaf().getKind() == Kind.PRIMITIVE_TYPE) {
                //ignore primitive types:
                return ;
            }

            if (LITERALS.contains(expr.getLeaf().getKind())) {
                //ignore literals:
                return ;
            }

            decl = !providesDecl ? info.getTrees().getElement(expr) : decl;
            
            Collection<ColoringAttributes> c = null;
            
            //causes NPE later, as decl is put into list of declarations to handle:
//            if (decl == null) {
//                c = Collections.singletonList(ColoringAttributes.UNDEFINED);
//            }
            
            if (decl != null && (decl.getKind().isField() || isLocalVariableClosure(decl))) {
                c = getVariableColoring(decl);
            }
            
            if (decl != null && decl instanceof ExecutableElement) {
                c = getMethodColoring((ExecutableElement) decl, nct);
            }
            
            if (decl != null && (decl.getKind().isClass() || decl.getKind().isInterface())) {
                //class use make look like read variable access:
                if (type.contains(UseTypes.READ)) {
                    type = EnumSet.copyOf(type);
                    type.remove(UseTypes.READ);
                    type.add(UseTypes.CLASS_USE);
                }
                
                c = new ArrayList<ColoringAttributes>();
                
                addModifiers(decl, c);
                
                switch (decl.getKind()) {
                    case CLASS: c.add(ColoringAttributes.CLASS); break;
                    case INTERFACE: c.add(ColoringAttributes.INTERFACE); break;
                    case ANNOTATION_TYPE: c.add(ColoringAttributes.ANNOTATION_TYPE); break;
                    case ENUM: c.add(ColoringAttributes.ENUM); break;
                }
            }
            
            if (decl != null && type.contains(UseTypes.DECLARATION)) {
                if (c == null) {
                    c = new ArrayList<ColoringAttributes>();
                }
                
                c.add(ColoringAttributes.DECLARATION);
            }
            
            if (c != null) {
                addUse(decl, type, expr, c);
            }
        }
        
        private void addUse(Element decl, Collection<UseTypes> useTypes, TreePath t, Collection<ColoringAttributes> c) {
            if (decl == recursionDetector) {
                useTypes.remove(UseTypes.EXECUTE); //recursive execution is not use
            }
            
            List<Use> uses = type2Uses.get(decl);
            
            if (uses == null) {
                type2Uses.put(decl, uses = new ArrayList<Use>());
            }
            
            Use u = new Use(useTypes, t, c);
            
            uses.add(u);
        }

        @Override
        public Void visitTypeCast(TypeCastTree tree, EnumSet<UseTypes> d) {
            Tree expr = tree.getExpression();
            
            if (expr.getKind() == Kind.IDENTIFIER) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
            }
            
            Tree cast = tree.getType();
            
            if (cast.getKind() == Kind.IDENTIFIER) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), cast), EnumSet.of(UseTypes.READ));
            }
            
            super.visitTypeCast(tree, d);
            return null;
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree tree, EnumSet<UseTypes> d) {
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
            }
            
            TreePath tp = new TreePath(getCurrentPath(), tree.getType());
            handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            
            super.visitInstanceOf(tree, null);
            
            //TODO: should be considered
            return null;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree tree, EnumSet<UseTypes> d) {
	    //ignore package X.Y.Z;:
	    //scan(tree.getPackageDecl(), p);
            tl.moveBefore(tree.getImports());
	    scan(tree.getImports(), d);
            tl.moveBefore(tree.getPackageAnnotations());
	    scan(tree.getPackageAnnotations(), d);
            tl.moveToEnd(tree.getImports());
	    scan(tree.getTypeDecls(), d);
	    return null;
        }

        private long startOf(List<? extends Tree> trees) {
            if (trees.isEmpty()) return -1;
            return sourcePositions.getStartPosition(info.getCompilationUnit(), trees.get(0));
        }

        private void handleMethodTypeArguments(TreePath method, List<? extends Tree> tArgs) {
            //the type arguments are before the last identifier in the select, so we should return there:
            //not very efficient, though:
            tl.moveBefore(tArgs);
            
            for (Tree expr : tArgs) {
                if (expr instanceof IdentifierTree) {
                    handlePossibleIdentifier(new TreePath(method, expr), EnumSet.of(UseTypes.CLASS_USE));
                }
            }
        }
        
        @Override
        public Void visitMethodInvocation(MethodInvocationTree tree, EnumSet<UseTypes> d) {
            Tree possibleIdent = tree.getMethodSelect();
            boolean handled = false;
            
            if (possibleIdent.getKind() == Kind.IDENTIFIER) {
                //handle "this" and "super" constructors:
                String ident = ((IdentifierTree) possibleIdent).getName().toString();
                
                if ("super".equals(ident) || "this".equals(ident)) { //NOI18N
                    Element resolved = info.getTrees().getElement(getCurrentPath());
                    
                    addUse(resolved, EnumSet.of(UseTypes.EXECUTE), null, null);
                    handled = true;
                }
            }
            
            if (!handled) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), possibleIdent), EnumSet.of(UseTypes.EXECUTE));
            }
            
            List<? extends Tree> ta = tree.getTypeArguments();
            long afterTypeArguments = ta.isEmpty() ? -1 : info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), ta.get(ta.size() - 1));
            
            switch (tree.getMethodSelect().getKind()) {
                case IDENTIFIER:
                case MEMBER_SELECT:
                    memberSelectBypass = afterTypeArguments;
                    scan(tree.getMethodSelect(), EnumSet.of(UseTypes.READ));
                    memberSelectBypass = -1;
                    break;
                default:
                    //todo: log
                    scan(tree.getMethodSelect(), EnumSet.of(UseTypes.READ));
            }

            handleMethodTypeArguments(getCurrentPath(), ta);
            
            scan(tree.getTypeArguments(), null);
            
//            if (tree.getMethodSelect().getKind() == Kind.MEMBER_SELECT && tree2Token.get(tree.getMethodSelect()) == null) {
////                if (ts.moveNext()) ???
//                    firstIdentifier(((MemberSelectTree) tree.getMethodSelect()).getIdentifier().toString());
//            }

            if (tree.getArguments() != null) {
                for (Tree expr : tree.getArguments()) {
                    if (expr instanceof IdentifierTree) {
                        handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
                    }
                }
            }
            
            scan(tree.getArguments(), EnumSet.of(UseTypes.READ));
            
//            super.visitMethodInvocation(tree, null);
            return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, EnumSet<UseTypes> d) {
            if (info.getTreeUtilities().isSynthetic(getCurrentPath()))
                return null;
//            if ("l".equals(tree.toString())) {
//                Thread.dumpStack();
//            }
//            handlePossibleIdentifier(tree);
//            //also possible type: (like in Collections.EMPTY_LIST):
//            resolveType(tree);
//            Thread.dumpStack();
            
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            
            if (memberSelectBypass != (-1)) {
                tl.moveToOffset(memberSelectBypass);
                memberSelectBypass = -1;
            }
            
            tl.identifierHere(tree, tree2Token);
            
            if (d != null) {
                handlePossibleIdentifier(getCurrentPath(), d);
            }
            super.visitIdentifier(tree, null);
            return null;
        }
//
        @Override
        public Void visitMethod(MethodTree tree, EnumSet<UseTypes> d) {
            if (info.getTreeUtilities().isSynthetic(getCurrentPath())) {
                return super.visitMethod(tree, d);
            }
//            Element decl = pi.getAttribution().getElement(tree);
//            
//            if (decl != null) {
//                assert decl instanceof ExecutableElement;
//                
//                Coloring c = getMethodColoring((ExecutableElement) decl);
//                HighlightImpl h = createHighlight(decl.getSimpleName(), tree, c, null);
//                
//                if (h != null) {
//                    highlights.add(h);
//                }
//            }

            //#170338: constructor without modifiers:
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));

            handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.DECLARATION));
            
            for (Tree t : tree.getThrows()) {
                TreePath tp = new TreePath(getCurrentPath(), t);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            EnumSet<UseTypes> paramsUseTypes;
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && (el.getModifiers().contains(Modifier.ABSTRACT) || el.getModifiers().contains(Modifier.NATIVE) || !el.getModifiers().contains(Modifier.PRIVATE))) {
                paramsUseTypes = EnumSet.of(UseTypes.WRITE, UseTypes.READ);
            } else {
                paramsUseTypes = EnumSet.of(UseTypes.WRITE);
            }
        
            scan(tree.getModifiers(), null);
            tl.moveToEnd(tree.getModifiers());
            scan(tree.getTypeParameters(), null);
            tl.moveToEnd(tree.getTypeParameters());
            scan(tree.getReturnType(), EnumSet.of(UseTypes.CLASS_USE));
            tl.moveToEnd(tree.getReturnType());
            
            String name;
            
            if (tree.getReturnType() != null) {
                //method:
                name = tree.getName().toString();
            } else {
                //constructor:
                TreePath tp = getCurrentPath();
                
                while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                    tp = tp.getParentPath();
                }
                
                if (tp != null && TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                    name = ((ClassTree) tp.getLeaf()).getSimpleName().toString();
                } else {
                    name = null;
                }
            }
            
            if (name != null) {
                firstIdentifier(name);
            }
            
            scan(tree.getParameters(), paramsUseTypes);
            scan(tree.getThrows(), null);
            scan(tree.getDefaultValue(), null);

            recursionDetector = (el != null && el.getKind() == ElementKind.METHOD) ? (ExecutableElement) el : null;
            
            scan(tree.getBody(), null);

            recursionDetector = null;
        
            return null;
        }

        @Override
        public Void visitExpressionStatement(ExpressionStatementTree tree, EnumSet<UseTypes> d) {
//            if (tree instanceof IdentifierTree) {
//                handlePossibleIdentifier(tree, EnumSet.of(UseTypes.READ));
//            }
            
            super.visitExpressionStatement(tree, null);
            return null;
        }

        @Override
        public Void visitParenthesized(ParenthesizedTree tree, EnumSet<UseTypes> d) {
            ExpressionTree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
            }
            
            super.visitParenthesized(tree, d);
            return null;
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree tree, EnumSet<UseTypes> d) {
            scan(tree.getVariable(), EnumSet.of(UseTypes.WRITE));
            
            if (tree.getExpression().getKind() == Kind.IDENTIFIER)
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            
            scan(tree.getExpression(), null);
            scan(tree.getStatement(), null);
            
            return null;
        }
        
        private boolean isStar(ImportTree tree) {
            Tree qualIdent = tree.getQualifiedIdentifier();
            
            if (qualIdent == null || qualIdent.getKind() == Kind.IDENTIFIER) {
                return false;
            }
            
            return ((MemberSelectTree) qualIdent).getIdentifier().contentEquals("*");
        }

        @Override
        public Void visitVariable(VariableTree tree, EnumSet<UseTypes> d) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));

            if (tree.getType() != null) {
            TreePath type = new TreePath(getCurrentPath(), tree.getType());
            
            if (type.getLeaf() instanceof ArrayTypeTree) {
                type = new TreePath(type, ((ArrayTypeTree) type.getLeaf()).getType());
            }
            
            if (type.getLeaf().getKind() == Kind.IDENTIFIER)
                handlePossibleIdentifier(type, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            Collection<UseTypes> uses = null;
            
            Element e = info.getTrees().getElement(getCurrentPath());
            if (tree.getInitializer() != null) {
                uses = EnumSet.of(UseTypes.DECLARATION, UseTypes.WRITE);
                if (tree.getInitializer().getKind() == Kind.IDENTIFIER)
                    handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getInitializer()), EnumSet.of(UseTypes.READ));
            } else {
                if (e != null && e.getKind() == ElementKind.FIELD) {
                    uses = EnumSet.of(UseTypes.DECLARATION, UseTypes.WRITE);
                } else {
                    uses = EnumSet.of(UseTypes.DECLARATION);
                }
            }
            
            if (d != null) {
                Set<UseTypes> ut = new HashSet<UseTypes>();
                
                ut.addAll(uses);
                ut.addAll(d);
                
                uses = EnumSet.copyOf(ut);
            }
            
            handlePossibleIdentifier(getCurrentPath(), uses);
            
            scan(tree.getModifiers(), null);
            
            tl.moveToEnd(tree.getModifiers());
            
            scan(tree.getType(), null);
            
            int[] span = info.getTreeUtilities().findNameSpan(tree);
            if (span != null)
                tl.moveToOffset(span[0]);
            else
                tl.moveToEnd(tree.getType());
            
//            System.err.println("tree.getName().toString()=" + tree.getName().toString());
            
            firstIdentifier(tree.getName().toString());
            
            tl.moveNext();
            
            scan(tree.getInitializer(), EnumSet.of(UseTypes.READ));
            
            return null;
        }
        
        @Override
        public Void visitAnnotation(AnnotationTree tree, EnumSet<UseTypes> d) {
//            System.err.println("tree.getType()= " + tree.toString());
//            System.err.println("tree.getType()= " + tree.getClass());
//        
            TreePath tp = new TreePath(getCurrentPath(), tree.getAnnotationType());
            handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            super.visitAnnotation(tree, EnumSet.noneOf(UseTypes.class));
            //TODO: maybe should be considered
            return null;
        }

        @Override
        public Void visitNewClass(NewClassTree tree, EnumSet<UseTypes> d) {
//            if (info.getTreeUtilities().isSynthetic(getCurrentPath()))
//                return null;
//            
            Tree exp = tree.getEnclosingExpression();
            if (exp instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), exp), EnumSet.of(UseTypes.READ));
            }
            
            TreePath tp;
            Tree ident = tree.getIdentifier();
            
            if (ident.getKind() == Kind.PARAMETERIZED_TYPE) {
                tp = new TreePath(new TreePath(getCurrentPath(), ident), ((ParameterizedTypeTree) ident).getType());
            } else {
                tp = new TreePath(getCurrentPath(), ident);
            }
            
            handlePossibleIdentifier(tp, EnumSet.of(UseTypes.EXECUTE), info.getTrees().getElement(getCurrentPath()), true, true);
            
            Element clazz = info.getTrees().getElement(tp);
            
            if (clazz != null) {
                addUse(clazz, EnumSet.of(UseTypes.CLASS_USE), null, null);
            }
	    
            for (Tree expr : tree.getArguments()) {
                if (expr instanceof IdentifierTree) {
                    handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
                }
            }
            
            scan(tree.getEnclosingExpression(), null);
            scan(tree.getIdentifier(), null);
            scan(tree.getTypeArguments(), null);
            scan(tree.getArguments(), EnumSet.of(UseTypes.READ));
            scan(tree.getClassBody(), null);
            
            return null;
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree tree, EnumSet<UseTypes> d) {
            boolean alreadyHandled = false;
            
            if (getCurrentPath().getParentPath().getLeaf().getKind() == Kind.NEW_CLASS) {
                NewClassTree nct = (NewClassTree) getCurrentPath().getParentPath().getLeaf();
                
                alreadyHandled = nct.getTypeArguments().contains(tree) || nct.getIdentifier() == tree;
            }
            
            if (getCurrentPath().getParentPath().getParentPath().getLeaf().getKind() == Kind.NEW_CLASS) {
                NewClassTree nct = (NewClassTree) getCurrentPath().getParentPath().getParentPath().getLeaf();
                Tree leafToTest = getCurrentPath().getParentPath().getLeaf();

                alreadyHandled = nct.getTypeArguments().contains(leafToTest) || nct.getIdentifier() == leafToTest;
            }
            
            if (!alreadyHandled) {
                //NewClass has already been handled as part of visitNewClass:
                TreePath tp = new TreePath(getCurrentPath(), tree.getType());
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            for (Tree t : tree.getTypeArguments()) {
                TreePath tp = new TreePath(getCurrentPath(), t);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
                
//                HighlightImpl h = createHighlight("", t, TYPE_PARAMETER);
//                
//                if (h != null)
//                    highlights.add(h);
            }
            
            super.visitParameterizedType(tree, null);
            return null;
        }

        @Override
        public Void visitBinary(BinaryTree tree, EnumSet<UseTypes> d) {
            Tree left = tree.getLeftOperand();
            Tree right = tree.getRightOperand();
            
            if (left instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), left);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            if (right instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), right);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            super.visitBinary(tree, EnumSet.of(UseTypes.READ));
            return null;
        }

        @Override
        public Void visitClass(ClassTree tree, EnumSet<UseTypes> d) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            for (TypeParameterTree t : tree.getTypeParameters()) {
                for (Tree bound : t.getBounds()) {
                    TreePath tp = new TreePath(new TreePath(getCurrentPath(), t), bound);
                    handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
                }
            }
            
            if(getCurrentPath().getParentPath().getLeaf().getKind() != Kind.NEW_CLASS) {
                //NEW_CLASS already handeled by visitnewClass
                Tree extnds = tree.getExtendsClause();

                if (extnds != null) {
                    TreePath tp = new TreePath(getCurrentPath(), extnds);
                    handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
                }

                for (Tree t : tree.getImplementsClause()) {
                    TreePath tp = new TreePath(getCurrentPath(), t);
                    handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
                }
            }
            
            handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.DECLARATION));
            
            scan(tree.getModifiers(), null);
            
//            System.err.println("tree.getModifiers()=" + tree.getModifiers());
//            System.err.println("mod end=" + sourcePositions.getEndPosition(info.getCompilationUnit(), tree.getModifiers()));
//            System.err.println("class start=" + sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            tl.moveToEnd(tree.getModifiers());
            firstIdentifier(tree.getSimpleName().toString());
            
            //XXX:????
            scan(tree.getTypeParameters(), null);
            scan(tree.getExtendsClause(), null);
            scan(tree.getImplementsClause(), null);

            ExecutableElement prevRecursionDetector = recursionDetector;

            recursionDetector = null;
            
            scan(tree.getMembers(), null);

            recursionDetector = prevRecursionDetector;
            
            //XXX: end ???
            
            return null;
        }
        
        @Override
        public Void visitUnary(UnaryTree tree, EnumSet<UseTypes> d) {
            if (tree.getExpression() instanceof IdentifierTree) {
                switch (tree.getKind()) {
                    case PREFIX_INCREMENT:
                    case PREFIX_DECREMENT:
                    case POSTFIX_INCREMENT:
                    case POSTFIX_DECREMENT:
                        Set<UseTypes> useTypes = EnumSet.of(UseTypes.WRITE);
                        if (d != null) {
                            useTypes.addAll(d);
                        }
                        handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), useTypes);
                        break;
                    default:
                        handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
                }
            }
            super.visitUnary(tree, d);
            return null;
        }

        @Override
        public Void visitArrayAccess(ArrayAccessTree tree, EnumSet<UseTypes> d) {
            scan(tree.getExpression(), EnumSet.of(UseTypes.READ));
            scan(tree.getIndex(), EnumSet.of(UseTypes.READ));
            
            return null;
        }

        @Override
        public Void visitArrayType(ArrayTypeTree node, EnumSet<UseTypes> p) {
            if (node.getType() != null) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), node.getType()), EnumSet.of(UseTypes.CLASS_USE));
            }
            return super.visitArrayType(node, p);
        }

        @Override
        public Void visitUnionType(UnionTypeTree node, EnumSet<UseTypes> p) {
            for (Tree tree : node.getTypeAlternatives()) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree), EnumSet.of(UseTypes.CLASS_USE));
            }
            return super.visitUnionType(node, p);
        }

        @Override
        public Void visitNewArray(NewArrayTree tree, EnumSet<UseTypes> d) {
            if (tree.getType() != null) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getType()), EnumSet.of(UseTypes.CLASS_USE));
            }
            
            scan(tree.getType(), null);
            scan(tree.getDimensions(), EnumSet.of(UseTypes.READ));
            scan(tree.getInitializers(), EnumSet.of(UseTypes.READ));
            
            return null;
        }
        
        @Override
        public Void visitCatch(CatchTree tree, EnumSet<UseTypes> d) {
            scan(tree.getParameter(), EnumSet.of(UseTypes.WRITE));
            scan(tree.getBlock(), null);
            return null;
        }

        @Override
        public Void visitConditionalExpression(ConditionalExpressionTree node, EnumSet<UseTypes> p) {
            return super.visitConditionalExpression(node, EnumSet.of(UseTypes.READ));
        }
        
        @Override
        public Void visitAssert(AssertTree tree, EnumSet<UseTypes> p) {
            if (tree.getCondition().getKind() == Kind.IDENTIFIER)
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getCondition()), EnumSet.of(UseTypes.READ));
            if (tree.getDetail() != null && tree.getDetail().getKind() == Kind.IDENTIFIER)
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getDetail()), EnumSet.of(UseTypes.READ));
            
            return super.visitAssert(tree, EnumSet.of(UseTypes.READ));
        }
        
        @Override
        public Void visitCase(CaseTree tree, EnumSet<UseTypes> p) {
            if (tree.getExpression() != null && tree.getExpression().getKind() == Kind.IDENTIFIER) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            }
            
            return super.visitCase(tree, null);
        }
        
        @Override
        public Void visitThrow(ThrowTree tree, EnumSet<UseTypes> p) {
            if (tree.getExpression() != null && tree.getExpression().getKind() == Kind.IDENTIFIER) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            }
            
            return super.visitThrow(tree, p);
        }

        @Override
        public Void visitTypeParameter(TypeParameterTree tree, EnumSet<UseTypes> p) {
            for (Tree bound : tree.getBounds()) {
                if (bound.getKind() == Kind.IDENTIFIER) {
                    TreePath tp = new TreePath(getCurrentPath(), bound);
                    
                    handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
                }
            }
            return super.visitTypeParameter(tree, p);
        }

        @Override
        public Void visitForLoop(ForLoopTree node, EnumSet<UseTypes> p) {
            if (node.getCondition() != null && node.getCondition().getKind() == Kind.IDENTIFIER) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), node.getCondition()), EnumSet.of(UseTypes.READ));
            }
            
            return super.visitForLoop(node, p);
        }

        @Override
        public Void visitWildcard(WildcardTree node, EnumSet<UseTypes> p) {
            if (node.getBound() != null && node.getBound().getKind() == Kind.IDENTIFIER) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), node.getBound()), EnumSet.of(UseTypes.CLASS_USE));
            }
            return super.visitWildcard(node, p);
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree node, EnumSet<UseTypes> p) {
            scan(node.getParameters(), EnumSet.of(UseTypes.WRITE));
            scan(node.getBody(), EnumSet.noneOf(UseTypes.class));
            return null;
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree node, EnumSet<UseTypes> p) {
            scan(node.getQualifierExpression(), EnumSet.of(UseTypes.READ));
            tl.moveToEnd(node.getQualifierExpression());
            scan(node.getTypeArguments(), null);
            tl.moveToEnd(node.getTypeArguments());
            handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.EXECUTE));
            firstIdentifier(node.getName().toString());
            return null;
        }

    }

    public static interface ErrorDescriptionSetter {
        
        public void setErrors(Document doc, List<ErrorDescription> errors, List<TreePathHandle> allUnusedImports);
        public void setHighlights(Document doc, OffsetsBag highlights);
        public void setColorings(Document doc, Map<Token, Coloring> colorings, Set<Token> addedTokens, Set<Token> removedTokens);
    }
    
    static ErrorDescriptionSetter ERROR_DESCRIPTION_SETTER = new ErrorDescriptionSetter() {
        
        public void setErrors(Document doc, List<ErrorDescription> errors, List<TreePathHandle> allUnusedImports) {}
        
        public void setHighlights(final Document doc, final OffsetsBag highlights) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getImportHighlightsBag(doc).setHighlights(highlights);
                }
            });
        }
    
        public void setColorings(final Document doc, final Map<Token, Coloring> colorings, final Set<Token> addedTokens, final Set<Token> removedTokens) {
            SwingUtilities.invokeLater(new Runnable () {
                public void run() {
                    LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, doc).setColorings(colorings, addedTokens, removedTokens);
                }                
            });            
        }
    };

    private static final Object KEY_UNUSED_IMPORTS = new Object();
    static OffsetsBag getImportHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(KEY_UNUSED_IMPORTS);
        
        if (bag == null) {
            doc.putProperty(KEY_UNUSED_IMPORTS, bag = new OffsetsBag(doc));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (stream instanceof DataObject) {
//                TimesCollector.getDefault().reportReference(((DataObject) stream).getPrimaryFile(), "ImportsHighlightsBag", "[M] Imports Highlights Bag", bag);
            }
        }
        
        return bag;
    }

}
