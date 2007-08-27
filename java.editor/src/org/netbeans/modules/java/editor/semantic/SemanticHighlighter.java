/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
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
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.java.editor.semantic.ColoringAttributes.Coloring;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;


/**
 *
 * @author Jan Lahoda
 */
public class SemanticHighlighter extends ScanningCancellableTask<CompilationInfo> {
    
    public static List<TreePathHandle> computeUnusedImports(CompilationInfo info) {
        SemanticHighlighter sh = new SemanticHighlighter(info.getFileObject());
        final List<TreePathHandle> result = new ArrayList<TreePathHandle>();
        
        sh.process(info, sh.getDocument(),new ErrorDescriptionSetter() {
            public void setErrors(Document doc, List<ErrorDescription> errors, List<TreePathHandle> allUnusedImports) {
                result.addAll(allUnusedImports);
            }
            public void setHighlights(Document doc, OffsetsBag highlights) {}
            public void setColorings(Document doc, Map<Token, Coloring> colorings, Set<Token> addedTokens, Set<Token> removedTokens) {}
        });
        
        return result;
    }
    
    private FileObject file;
    
    SemanticHighlighter(FileObject file) {
        this.file = file;
    }

    public Document getDocument() {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
            
            if (ec == null)
                return null;
            
            return ec.getDocument();
        } catch (IOException e) {
            Logger.getLogger(SemanticHighlighter.class.getName()).log(Level.INFO, "SemanticHighlighter: Cannot find DataObject for file: " + FileUtil.getFileDisplayName(file), e);
            return null;
        }
    }
    
    public @Override void run(CompilationInfo info) {
        resume();
        
        Document doc = getDocument();

        if (doc == null) {
            Logger.getLogger(SemanticHighlighter.class.getName()).log(Level.INFO, "SemanticHighlighter: Cannot get document!");
            return ;
        }

        process(info, doc);
    }
    
    private static class FixAllImportsFixList implements LazyFixList {
        private Fix removeImport;
        private Fix removeAllUnusedImports;
        private List<TreePathHandle> allUnusedImports;
        
        public FixAllImportsFixList(Fix removeImport, Fix removeAllUnusedImports, List<TreePathHandle> allUnusedImports) {
            this.removeImport = removeImport;
            this.removeAllUnusedImports = removeAllUnusedImports;
            this.allUnusedImports = allUnusedImports;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        public boolean probablyContainsFixes() {
            return true;
        }
        
        private List<Fix> fixes;
        
        public synchronized List<Fix> getFixes() {
            if (fixes != null)
                return fixes;
            
            if (allUnusedImports.size() > 1) {
                fixes = Arrays.asList(removeImport, removeAllUnusedImports);
            } else {
                fixes = Collections.singletonList(removeImport);
            }
            
            return fixes;
        }
        
        public boolean isComputed() {
            return true;
        }
    }
    
    void process(CompilationInfo info, final Document doc) {
        process(info, doc, ERROR_DESCRIPTION_SETTER);
    }
    
    static Coloring collection2Coloring(Collection<ColoringAttributes> attr) {
        Coloring c = ColoringAttributes.empty();
        
        for (ColoringAttributes a : attr) {
            c = ColoringAttributes.add(c, a);
        }
        
        return c;
    }
    
    void process(CompilationInfo info, final Document doc, ErrorDescriptionSetter setter) {
        DetectorVisitor v = new DetectorVisitor(info, doc, canceled);
        
        long start = System.currentTimeMillis();
        
        Map<Token, Coloring> newColoring = new IdentityHashMap<Token, Coloring>();
        List<ErrorDescription> errors = new ArrayList<ErrorDescription>();

        CompilationUnitTree cu = info.getCompilationUnit();
        
        scan(v, cu, null);
        
        if (isCancelled())
            return ;
        
        boolean computeUnusedImports = "text/x-java".equals(FileUtil.getMIMEType(info.getFileObject()));
        
        final List<TreePathHandle> allUnusedImports = computeUnusedImports ? new ArrayList<TreePathHandle>() : null;
        final Fix removeAllUnusedImports = computeUnusedImports ? RemoveUnusedImportFix.create(file, allUnusedImports) : null;
        OffsetsBag imports = computeUnusedImports ? new OffsetsBag(doc) : null;

        if (computeUnusedImports) {
            Coloring unused = ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.UNUSED);

            for (Element el : v.type2Highlight.keySet()) {
                if (isCancelled()) {
                    return;
                }
                final TreePath tree = v.type2Highlight.get(el);

                if (el == null || el.getSimpleName() == null) {
                    continue;
                }
                //XXX: finish
                final int startPos = (int) info.getTrees().getSourcePositions().getStartPosition(cu, tree.getLeaf());
                final int endPos = (int) info.getTrees().getSourcePositions().getEndPosition(cu, tree.getLeaf());

                imports.addHighlight(startPos, endPos, ColoringManager.getColoringImpl(unused));

                int line = (int) info.getCompilationUnit().getLineMap().getLineNumber(startPos);

                TreePathHandle handle = TreePathHandle.create(tree, info);

                final Fix removeImport = RemoveUnusedImportFix.create(file, handle);

                allUnusedImports.add(handle);
                if (RemoveUnusedImportFix.isEnabled()) {
                    errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.VERIFIER, "Unused import", new FixAllImportsFixList(removeImport, removeAllUnusedImports, allUnusedImports), doc, line));
                }
            }
        }
        
        Map<Token, Coloring> oldColors = LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, doc).getColorings();
        Set<Token> removedTokens = new HashSet<Token>(oldColors.keySet());
        Set<Token> addedTokens = new HashSet<Token>();
        
        for (Element decl : v.type2Uses.keySet()) {
            if (isCancelled())
                return ;
            
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
                    
                    if (!removedTokens.remove(t)) {
                        addedTokens.add(t);
                    }
                }
            }
        }
        
        if (isCancelled())
            return ;
        
        if (computeUnusedImports) {
            setter.setErrors(doc, errors, allUnusedImports);
            setter.setHighlights(doc, imports);
        }
        
        setter.setColorings(doc, newColoring, addedTokens, removedTokens);
        
        Logger.getLogger("TIMER").log(Level.FINE, "Semantic",
            new Object[] {((DataObject) doc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile(), System.currentTimeMillis() - start});
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
        return el.getKind() == ElementKind.PARAMETER || el.getKind() == ElementKind.LOCAL_VARIABLE || el.getKind() == ElementKind.EXCEPTION_PARAMETER;
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
        
        public String toString() {
            return "Use: " + type;
        }
    }
    
    private static class DetectorVisitor extends CancellableTreePathScanner<Void, EnumSet<UseTypes>> {
        
        private org.netbeans.api.java.source.CompilationInfo info;
        private Document doc;
        private Map<Element, List<Use>> type2Uses;
        private Map<Element, TreePath/*ImportTree*/> type2Highlight;
        private Set<Element> additionalUsedTypes; //types used *before* the imports has been processed (ie. annotations in package-info.java)
        
        private Map<Tree, Token> tree2Token;
        private TokenList tl;
        private long memberSelectBypass = -1;
        
        private SourcePositions sourcePositions;
        
        private DetectorVisitor(org.netbeans.api.java.source.CompilationInfo info, final Document doc, AtomicBoolean cancel) {
            this.info = info;
            this.doc  = doc;
            type2Uses = new HashMap<Element, List<Use>>();
            this.type2Highlight = new HashMap<Element, TreePath/*ImportTree*/>();
            this.additionalUsedTypes = new HashSet<Element>();
            
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
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            scan(tree.getVariable(), EnumSet.of(UseTypes.WRITE));
            scan(tree.getExpression(), null);
            
            return null;
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree tree, EnumSet<UseTypes> d) {
            handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getVariable()), EnumSet.of(UseTypes.WRITE));
            
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), expr);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            scan(tree.getVariable(), EnumSet.of(UseTypes.WRITE));
            scan(tree.getExpression(), null);
            
            return null;
        }

        @Override
        public Void visitReturn(ReturnTree tree, EnumSet<UseTypes> d) {
            if (tree.getExpression() instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            }
            
            super.visitReturn(tree, null);
            return null;
        }
        
        @Override
        public Void visitMemberSelect(MemberSelectTree tree, EnumSet<UseTypes> d) {
            long memberSelectBypassLoc = memberSelectBypass;
            
            memberSelectBypass = -1;
            
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), expr);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getKind().isField()) {
                handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.READ));
            }
	    
	    if (el != null && (el.getKind().isClass() || el.getKind().isInterface())) {
		handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.CLASS_USE));
	    }
	    
//            System.err.println("XXXX=" + tree.toString());
//            System.err.println("YYYY=" + info.getElement(tree));
            
            super.visitMemberSelect(tree, null);
            
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
        
        private Collection<ColoringAttributes> getMethodColoring(ExecutableElement mdecl) {
            Collection<ColoringAttributes> c = new ArrayList<ColoringAttributes>();
            
            addModifiers(mdecl, c);
            
            if (mdecl.getKind() == ElementKind.CONSTRUCTOR)
                c.add(ColoringAttributes.CONSTRUCTOR);
            else
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
            
            if (decl.getKind() == ElementKind.LOCAL_VARIABLE || decl.getKind() == ElementKind.EXCEPTION_PARAMETER) {
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
            handlePossibleIdentifier(expr, type, null, false);
        }
        
        private void handlePossibleIdentifier(TreePath expr, Collection<UseTypes> type, Element decl, boolean providesDecl) {
            
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
                c = getMethodColoring((ExecutableElement) decl);
            }
            
            if (decl != null && (decl.getKind().isClass() || decl.getKind().isInterface())) {
                //class use make look like read variable access:
                if (type.contains(UseTypes.READ)) {
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
            List<Use> uses = type2Uses.get(decl);
            
            if (uses == null) {
                type2Uses.put(decl, uses = new ArrayList<Use>());
            }
            
            Use u = new Use(useTypes, t, c);
            
            uses.add(u);
        }

        @Override
        public Void visitTypeCast(TypeCastTree tree, EnumSet<UseTypes> d) {
            resolveType(new TreePath(getCurrentPath(), tree.getType()));
            
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
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
            resolveType(tp);
            handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            
            super.visitInstanceOf(tree, null);
            
            //TODO: should be considered
            return null;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree tree, EnumSet<UseTypes> d) {
	    //ignore package X.Y.Z;:
	    //scan(tree.getPackageDecl(), p);
	    scan(tree.getPackageAnnotations(), d);
	    scan(tree.getImports(), d);
	    scan(tree.getTypeDecls(), d);
	    return null;
        }

        private void handleMethodTypeArguments(List<? extends Tree> tArgs) {
            //the type arguments are before the last identifier in the select, so we should return there:
            //not very efficient, though:
            tl.moveBefore(tArgs);
            
            for (Tree expr : tArgs) {
                if (expr instanceof IdentifierTree) {
                    resolveType(new TreePath(getCurrentPath(), expr));
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
                    scan(tree.getMethodSelect(), null);
                    memberSelectBypass = -1;
                    break;
                default:
                    //todo: log
                    scan(tree.getMethodSelect(), null);
            }

            handleMethodTypeArguments(ta);
            
            scan(tree.getTypeArguments(), null);
            
//            if (tree.getMethodSelect().getKind() == Kind.MEMBER_SELECT && tree2Token.get(tree.getMethodSelect()) == null) {
////                if (ts.moveNext()) ???
//                    firstIdentifier(((MemberSelectTree) tree.getMethodSelect()).getIdentifier().toString());
//            }
            
            for (Tree expr : tree.getArguments()) {
                if (expr instanceof IdentifierTree) {
                    handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
                }
            }
            
            scan(tree.getArguments(), null);
            
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
            if (info.getTreeUtilities().isSynthetic(getCurrentPath()))
                return null;
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
            handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.DECLARATION));
            
            for (Tree t : tree.getThrows()) {
                TreePath tp = new TreePath(getCurrentPath(), t);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            if (tree.getReturnType() != null)
                resolveType(new TreePath(getCurrentPath(), tree.getReturnType()));
           
            EnumSet<UseTypes> paramsUseTypes;
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getModifiers().contains(Modifier.ABSTRACT)) {
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
                
                while (tp != null && tp.getLeaf().getKind() != Kind.CLASS) {
                    tp = tp.getParentPath();
                }
                
                if (tp != null && tp.getLeaf().getKind() == Kind.CLASS) {
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
            scan(tree.getBody(), null);
        
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
            
            super.visitParenthesized(tree, null);
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
        
        @Override
        public Void visitImport(ImportTree tree, EnumSet<UseTypes> d) {
            if (!tree.isStatic()) {
                Element decl = info.getTrees().getElement(new TreePath(getCurrentPath(), tree.getQualifiedIdentifier()));
                
                if (   decl != null
                    && decl.asType().getKind() != TypeKind.ERROR //unresolvable imports should not be marked as unused
                    && !additionalUsedTypes.contains(decl)) {
                    type2Highlight.put(decl, getCurrentPath());
                }
//                } else {
//                    //cannot handle package import for now.
//                    //cannot handle static imports for now.
//                }
            }
            super.visitImport(tree, null);
            return null;
        }
        
        @Override
        public Void visitVariable(VariableTree tree, EnumSet<UseTypes> d) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            TreePath type = new TreePath(getCurrentPath(), tree.getType());
            
            if (type.getLeaf() instanceof ArrayTypeTree) {
                type = new TreePath(type, ((ArrayTypeTree) type.getLeaf()).getType());
            }
            
            resolveType(type);
            
            if (type.getLeaf().getKind() == Kind.IDENTIFIER)
                handlePossibleIdentifier(type, EnumSet.of(UseTypes.CLASS_USE));
            
            Collection<UseTypes> uses = null;
            
            if (tree.getInitializer() != null) {
                uses = EnumSet.of(UseTypes.DECLARATION, UseTypes.WRITE);
                if (tree.getInitializer().getKind() == Kind.IDENTIFIER)
                    handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getInitializer()), EnumSet.of(UseTypes.READ));
            } else {
                Element e = info.getTrees().getElement(getCurrentPath());
                
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
            
            tl.moveToEnd(tree.getType());
            
//            System.err.println("tree.getName().toString()=" + tree.getName().toString());
            
            firstIdentifier(tree.getName().toString());
            
            scan(tree.getInitializer(), null);
            
            return null;
        }
        
        @Override
        public Void visitAnnotation(AnnotationTree tree, EnumSet<UseTypes> d) {
//            System.err.println("tree.getType()= " + tree.toString());
//            System.err.println("tree.getType()= " + tree.getClass());
//        
            TreePath tp = new TreePath(getCurrentPath(), tree.getAnnotationType());
            resolveType(tp);
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
            TreePath tp;
            Tree ident = tree.getIdentifier();
            
            if (ident.getKind() == Kind.PARAMETERIZED_TYPE) {
                tp = new TreePath(new TreePath(getCurrentPath(), ident), ((ParameterizedTypeTree) ident).getType());
            } else {
                tp = new TreePath(getCurrentPath(), ident);
            }
            
            resolveType(tp);
	    handlePossibleIdentifier(tp, EnumSet.of(UseTypes.EXECUTE), info.getTrees().getElement(getCurrentPath()), true);
            
            Element clazz = info.getTrees().getElement(tp);
            
            if (clazz != null) {
                addUse(clazz, EnumSet.of(UseTypes.CLASS_USE), null, null);
            }
	    
            for (Tree expr : tree.getArguments()) {
                if (expr instanceof IdentifierTree) {
                    handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
                }
            }
            
            super.visitNewClass(tree, null);
            
            return null;
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree tree, EnumSet<UseTypes> d) {
            if (getCurrentPath().getParentPath().getLeaf().getKind() != Kind.NEW_CLASS) {
                //NewClass has already been handled as part of visitNewClass:
                TreePath tp = new TreePath(getCurrentPath(), tree.getType());
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            for (Tree t : tree.getTypeArguments()) {
                TreePath tp = new TreePath(getCurrentPath(), t);
                resolveType(tp);
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
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            if (right instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), right);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            super.visitBinary(tree, null);
            return null;
        }

        @Override
        public Void visitClass(ClassTree tree, EnumSet<UseTypes> d) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            for (TypeParameterTree t : tree.getTypeParameters()) {
                for (Tree bound : t.getBounds()) {
                    TreePath tp = new TreePath(new TreePath(getCurrentPath(), t), bound);
                    resolveType(tp);
                    handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
                }
            }
            
            Tree extnds = tree.getExtendsClause();
            
            if (extnds != null) {
                TreePath tp = new TreePath(getCurrentPath(), extnds);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            for (Tree t : tree.getImplementsClause()) {
                TreePath tp = new TreePath(getCurrentPath(), t);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
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
            scan(tree.getMembers(), null);
            //XXX: end ???
            
            return null;
        }
        
        @Override
        public Void visitUnary(UnaryTree tree, EnumSet<UseTypes> d) {
            if (tree.getExpression() instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            }
            super.visitUnary(tree, d);
            return null;
        }

        @Override
        public Void visitArrayAccess(ArrayAccessTree tree, EnumSet<UseTypes> d) {
            if (tree.getExpression() != null && tree.getExpression().getKind() == Kind.IDENTIFIER) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            }
            
            if (tree.getIndex() instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getIndex()), EnumSet.of(UseTypes.READ));
            }
            
            super.visitArrayAccess(tree, null);
            return null;
        }

        @Override
        public Void visitArrayType(ArrayTypeTree node, EnumSet<UseTypes> p) {
            if (node.getType() != null) {
                resolveType(new TreePath(getCurrentPath(), node.getType()));
            }
            return super.visitArrayType(node, p);
        }

        @Override
        public Void visitNewArray(NewArrayTree tree, EnumSet<UseTypes> d) {
            if (tree.getType() != null)
                resolveType(new TreePath(getCurrentPath(), tree.getType()));
            
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
            
            return super.visitAssert(tree, null);
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
                    resolveType(tp);
                }
            }
            return super.visitTypeParameter(tree, p);
        }
        
        private void resolveType(TreePath type) {
            FirstIdentTypeVisitor v = new FirstIdentTypeVisitor();
            
            v.scan(type, null);
            
            if (v.first == null)
                return ;
            
            Element decl = info.getTrees().getElement(v.first);
            
            if (decl == null) {
//                System.err.println("Warning: type=" + type);
//                System.err.println("decl=" + decl);
                
                return ;
            }
            
            if (type2Highlight.remove(decl) == null) {
                additionalUsedTypes.add(decl);
            }
        }
        
        private static class FirstIdentTypeVisitor extends TreePathScanner<Void, Void> {
            private TreePath first = null;
            
            public Void visitIdentifier(IdentifierTree tree, Void d) {
                if (first == null) {
                    first = getCurrentPath();
                }
                
                return super.visitIdentifier(tree, null);
            }
            
        }
    }
    
    public static interface ErrorDescriptionSetter {
        
        public void setErrors(Document doc, List<ErrorDescription> errors, List<TreePathHandle> allUnusedImports);
        public void setHighlights(Document doc, OffsetsBag highlights);
        public void setColorings(Document doc, Map<Token, Coloring> colorings, Set<Token> addedTokens, Set<Token> removedTokens);
    }
    
    static ErrorDescriptionSetter ERROR_DESCRIPTION_SETTER = new ErrorDescriptionSetter() {
        
        public void setErrors(Document doc, List<ErrorDescription> errors, List<TreePathHandle> allUnusedImports) {
            HintsController.setErrors(doc, "semantic-highlighter", errors);
        }
        
        public void setHighlights(final Document doc, final OffsetsBag highlights) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getImportHighlightsBag(doc).setHighlights(highlights);
                }
            });
        }
    
        public void setColorings(Document doc, Map<Token, Coloring> colorings, Set<Token> addedTokens, Set<Token> removedTokens) {
            LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, doc).setColorings(colorings, addedTokens, removedTokens);
        }
    };

    static OffsetsBag getImportHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(FixAllImportsFixList.class);
        
        if (bag == null) {
            doc.putProperty(FixAllImportsFixList.class, bag = new OffsetsBag(doc));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (stream instanceof DataObject) {
//                TimesCollector.getDefault().reportReference(((DataObject) stream).getPrimaryFile(), "ImportsHighlightsBag", "[M] Imports Highlights Bag", bag);
            }
        }
        
        return bag;
    }
}
