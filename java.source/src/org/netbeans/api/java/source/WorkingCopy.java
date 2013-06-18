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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.java.source;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.text.BadLocationException;
import javax.tools.JavaFileObject;

import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TryTree;
import com.sun.source.util.DocTrees;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.modules.java.source.parsing.JavacParserResult;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import static org.netbeans.api.java.source.ModificationResult.*;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.save.CasualDiff.Diff;
import org.netbeans.modules.java.source.builder.TreeFactory;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.pretty.ImportAnalysis2;
import org.netbeans.modules.java.source.save.CasualDiff;
import org.netbeans.modules.java.source.save.DiffContext;
import org.netbeans.modules.java.source.save.DiffUtilities;
import org.netbeans.modules.java.source.save.ElementOverlay;
import org.netbeans.modules.java.source.save.ElementOverlay.FQNComputer;
import org.netbeans.modules.java.source.save.OverlayTemplateAttributesProvider;
import org.netbeans.modules.java.source.transform.ImmutableDocTreeTranslator;
import org.netbeans.modules.java.source.transform.ImmutableTreeTranslator;
import org.netbeans.modules.java.source.transform.TreeDuplicator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**XXX: extends CompilationController now, finish method delegation
 *
 * @author Dusan Balek, Petr Hrebejk, Tomas Zezul
 */
public class WorkingCopy extends CompilationController {

    static Reference<WorkingCopy> instance;
    private Map<Tree, Tree> changes;
    private Map<Tree, Map<DocTree, DocTree>> docChanges;
    private Map<JavaFileObject, CompilationUnitTree> externalChanges;
    private List<Diff> textualChanges;
    private Map<Integer, String> userInfo;
    private boolean afterCommit = false;
    private TreeMaker treeMaker;
    private Map<Tree, Object> tree2Tag;
    private final ElementOverlay overlay;
    
    WorkingCopy(final CompilationInfoImpl impl, ElementOverlay overlay) {
        super(impl);
        this.overlay = overlay;
    }

    private synchronized void init() {
        if (changes != null) //already initialized
            return;
        
        treeMaker = new TreeMaker(this, TreeFactory.instance(getContext()));
        changes = new IdentityHashMap<Tree, Tree>();
        docChanges = new IdentityHashMap<Tree, Map<DocTree, DocTree>>();
        tree2Tag = new IdentityHashMap<Tree, Object>();
        externalChanges = null;
        textualChanges = new ArrayList<Diff>();
        userInfo = new HashMap<Integer, String>();

        //#208490: force the current ElementOverlay:
        getContext().put(ElementOverlay.class, (ElementOverlay) null);
        getContext().put(ElementOverlay.class, overlay);
    }
    
    private Context getContext() {
        return impl.getJavacTask().getContext();
    }
    
    // API of the class --------------------------------------------------------

    /**
     * Returns an instance of the {@link WorkingCopy} for
     * given {@link Parser.Result} if it is a result
     * of a java parser.
     * @param result for which the {@link WorkingCopy} should be
     * returned.
     * @return a {@link WorkingCopy} or null when the given result
     * is not a result of java parsing.
     * @since 0.42
     */
    public static @NullUnknown WorkingCopy get (final @NonNull Parser.Result result) {
        Parameters.notNull("result", result); //NOI18N
        WorkingCopy copy = instance != null ? instance.get() : null;
        if (copy != null && result instanceof JavacParserResult) {
            final JavacParserResult javacResult = (JavacParserResult)result;
            CompilationController controller = javacResult.get(CompilationController.class);
            if (controller != null && controller.impl == copy.impl)
                return copy;
        }
        return null;
    }

    @Override
    public @NonNull JavaSource.Phase toPhase(@NonNull JavaSource.Phase phase) throws IOException {
        //checkConfinement() called by super
        JavaSource.Phase result = super.toPhase(phase);
        
        if (result.compareTo(JavaSource.Phase.PARSED) >= 0) {
            init();
        }
        
        return result;
    }        
    
    public synchronized @NonNull TreeMaker getTreeMaker() throws IllegalStateException {
        checkConfinement();
        if (treeMaker == null)
            throw new IllegalStateException("Cannot call getTreeMaker before toPhase.");
        return treeMaker;
    }
    
    Map<Tree, Tree> getChangeSet() {
        return changes;
    }
    
    /**
     * Replaces the original tree <code>oldTree</code> with the new one -
     * <code>newTree</code>.
     * <p>
     * To create a new file, use
     * <code>rewrite(null, compilationUnitTree)</code>. Use
     * {@link GeneratorUtilities#createFromTemplate GeneratorUtilities.createFromTemplate()}
     * to create a new compilation unit tree from a template.
     * <p>
     * <code>newTree</code> cannot be <code>null</code>, use methods in
     * {@link TreeMaker} for tree element removal. If <code>oldTree</code> is
     * null, <code>newTree</code> must be of kind
     * {@link Kind#COMPILATION_UNIT COMPILATION_UNIT}.
     * 
     * 
     * @param oldTree  tree to be replaced, use tree already represented in
     *                 source code. <code>null</code> to create a new file.
     * @param newTree  new tree, either created by <code>TreeMaker</code>
     *                 or obtained from different place. <code>null</code>
     *                 values are not allowed.
     * @throws IllegalStateException if <code>toPhase()</code> method was not
     *         called before.
     * @throws IllegalArgumentException when <code>null</code> was passed to the 
     *         method.
     * @see GeneratorUtilities#createFromTemplate
     * @see TreeMaker
     */
    public synchronized void rewrite(@NullAllowed Tree oldTree, @NonNull Tree newTree) {
        checkConfinement();
        if (changes == null) {
            throw new IllegalStateException("Cannot call rewrite before toPhase.");
        }
        if (oldTree == newTree) {
            // no change operation called.
            return;
        }
        if (oldTree == null && Kind.COMPILATION_UNIT == newTree.getKind()) {
            createCompilationUnit((JCTree.JCCompilationUnit) newTree);
            return;
        }
        if (oldTree == null || newTree == null)
            throw new IllegalArgumentException("Null values are not allowed.");
        
        changes.put(oldTree, newTree);
    }
    
    /**
     * Replaces the original doctree <code>oldTree</code> with the new one -
     * <code>newTree</code> for a specific tree.
     * <p>
     * To create a new javadoc comment, use
     * <code>rewrite(tree, null, docCommentTree)</code>.
     * <p>
     * <code>tree</code> and <code>newTree</code> cannot be <code>null</code>.
     * If <code>oldTree</code> is null, <code>newTree</code> must be of kind
     * {@link DocTree.Kind#DOC_COMMENT DOC_COMMENT}.
     * 
     * @param tree     the tree to which the doctrees belong.
     * @param oldTree  tree to be replaced, use tree already represented in
     *                 source code. <code>null</code> to create a new file.
     * @param newTree  new tree, either created by <code>TreeMaker</code>
     *                 or obtained from different place. <code>null</code>
     *                 values are not allowed.
     * @throws IllegalStateException if <code>toPhase()</code> method was not
     *         called before.
     * @since 0.124
     */
    public synchronized void rewrite(@NonNull Tree tree, @NonNull DocTree oldTree, @NonNull DocTree newTree) {
        checkConfinement();
        if (docChanges == null) {
            throw new IllegalStateException("Cannot call rewrite before toPhase.");
        }
        
        if (oldTree == newTree) {
            // no change operation called.
            return;
        }
        
        Map<DocTree, DocTree> changesMap = docChanges.get(tree);
        if(changesMap == null) {
            changesMap = new IdentityHashMap<DocTree, DocTree>();
            docChanges.put(tree, changesMap);
        }
        
        changesMap.put(oldTree, newTree);
    }
              
    /**
     * Replace a part of a comment token with the given text.
     * 
     * Please note that this is a special purpose method to handle eg.
     * "Apply Rename in Comments" option in the Rename refactoring.
     * 
     * It is caller's responsibility to ensure that replacements done by this method
     * will not clash with replacements done by the general-purpose method
     * {@link #rewrite(Tree,Tree)}.
     * 
     * @param start absolute offset in the original text to start the replacement
     * @param length how many characters should be deleted from the original text
     * @param newText new text to be inserted at the specified offset
     * @throws java.lang.IllegalArgumentException when an attempt is made to replace non-comment text
     * @since 0.23
     */
    public synchronized void rewriteInComment(int start, int length, @NonNull String newText) throws IllegalArgumentException {
        checkConfinement();
        TokenSequence<JavaTokenId> ts = getTokenHierarchy().tokenSequence(JavaTokenId.language());
        
        ts.move(start);
        
        if (!ts.moveNext()) {
            throw new IllegalArgumentException("Cannot rewriteInComment start=" + start + ", text length=" + getText().length());
        }
        
        if (ts.token().id() != JavaTokenId.LINE_COMMENT && ts.token().id() != JavaTokenId.BLOCK_COMMENT && ts.token().id() != JavaTokenId.JAVADOC_COMMENT) {
            throw new IllegalArgumentException("Cannot rewriteInComment: attempt to rewrite non-comment token: " + ts.token().id());
        }
        
        if (ts.offset() + ts.token().length() < start + length) {
            throw new IllegalArgumentException("Cannot rewriteInComment: attempt to rewrite text after comment token. Token end offset: " + (ts.offset() + ts.token().length()) + ", rewrite end offset: " + (start + length));
        }
        
        int commentPrefix;
        int commentSuffix;
        
        switch (ts.token().id()) {
            case LINE_COMMENT: commentPrefix = 2; commentSuffix = 0; break;
            case BLOCK_COMMENT: commentPrefix = 2; commentSuffix = 2; break;
            case JAVADOC_COMMENT: commentPrefix = 3; commentSuffix = 2; break;
            default: throw new IllegalStateException("Internal error");
        }
        
        if (ts.offset() + commentPrefix > start) {
            throw new IllegalArgumentException("Cannot rewriteInComment: attempt to rewrite comment prefix");
        }
        
        if (ts.offset() + ts.token().length() - commentSuffix < start + length) {
            throw new IllegalArgumentException("Cannot rewriteInComment: attempt to rewrite comment suffix");
        }
        
        textualChanges.add(Diff.delete(start, start + length));
        textualChanges.add(Diff.insert(start + length, newText));
        userInfo.put(start, NbBundle.getMessage(CasualDiff.class,"TXT_RenameInComment")); //NOI18N
    }
    
    /**
     * Tags a tree. Used in {@code ModificationResult} to determine position of tree inside document.
     * @param t the tree to be tagged
     * @param tag an {@code Object} used as tag
     * @since 0.37
     */
    public synchronized void tag(@NonNull Tree t, @NonNull Object tag) {
        tree2Tag.put(t, tag);
    }

    /**Returns the tree into which the given tree was rewritten using the
     * {@link #rewrite(com.sun.source.tree.Tree, com.sun.source.tree.Tree) } method,
     * transitively.
     * Will return the input tree if the input tree was never passed as the first
     * parameter of the {@link #rewrite(com.sun.source.tree.Tree, com.sun.source.tree.Tree) }
     * method.
     *
     * <p>Note that the returned tree will be exactly equivalent to a tree passed as
     * the second parameter to {@link #rewrite(com.sun.source.tree.Tree, com.sun.source.tree.Tree) }.
     * No attribution or other information will be added (or removed) to (or from) the tree.
     *
     * @param in the tree to inspect
     * @return tree into which the given tree was rewritten using the
     * {@link #rewrite(com.sun.source.tree.Tree, com.sun.source.tree.Tree) } method,
     * transitively
     * @since 0.102
     */
    public synchronized @NonNull Tree resolveRewriteTarget(@NonNull Tree in) {
        Map<Tree, Tree> localChanges = new IdentityHashMap<Tree, Tree>(changes);

        while (localChanges.containsKey(in)) {
            in = localChanges.remove(in);
        }

        return in;
    }
    
    // Package private methods -------------------------------------------------        
    
    private static String codeForCompilationUnit(CompilationUnitTree topLevel) throws IOException {
        return ((JCTree.JCCompilationUnit) topLevel).sourcefile.getCharContent(true).toString();
    }
    
    class Translator extends ImmutableTreeTranslator {
        private Map<Tree, Tree> changeMap;

        public Translator() {
            super(WorkingCopy.this);
        }

        Tree translate(Tree tree, Map<Tree, Tree> changeMap) {
            this.changeMap = new HashMap<Tree, Tree>(changeMap);
            return translate(tree);
        }

        @Override
        public Tree translate(Tree tree) {
            assert changeMap != null;
            if (tree == null) {
                return null;
            }
            Tree repl = changeMap.remove(tree);
            Tree newRepl;
            if (repl != null) {
                newRepl = translate(repl);
            } else {
                newRepl = super.translate(tree);
            }
            return newRepl;
        }
    }
            
    private static boolean REWRITE_WHOLE_FILE = Boolean.getBoolean(WorkingCopy.class.getName() + ".rewrite-whole-file");

    private void addSyntheticTrees(DiffContext diffContext, Tree node) {
        if (node == null) return ;
        
        if (((JCTree) node).pos == (-1)) {
            diffContext.syntheticTrees.add(node);
            return ;
        }
        
        if (node.getKind() == Kind.EXPRESSION_STATEMENT) {
            ExpressionTree est = ((ExpressionStatementTree) node).getExpression();

            if (est.getKind() == Kind.METHOD_INVOCATION) {
                ExpressionTree select = ((MethodInvocationTree) est).getMethodSelect();

                if (select.getKind() == Kind.IDENTIFIER && ((IdentifierTree) select).getName().contentEquals("super")) {
                    if (getTreeUtilities().isSynthetic(diffContext.origUnit, node)) {
                        diffContext.syntheticTrees.add(node);
                    }
                }
            }
        }
    }

    private List<Difference> processCurrentCompilationUnit(final DiffContext diffContext, Map<?, int[]> tag2Span) throws IOException, BadLocationException {
        final Set<TreePath> pathsToRewrite = new LinkedHashSet<TreePath>();
        final Map<TreePath, Map<Tree, Tree>> parent2Rewrites = new IdentityHashMap<TreePath, Map<Tree, Tree>>();
        final Map<Tree, Pair<DocCommentTree, DocCommentTree>> tree2Doc = new IdentityHashMap<Tree, Pair<DocCommentTree, DocCommentTree>>();
        boolean fillImports = true;
        
        Map<Integer, String> userInfo = new HashMap<Integer, String>();
        final Set<Tree> oldTrees = new HashSet<Tree>();

        if (CasualDiff.OLD_TREES_VERBATIM) {
            new TreeScanner<Void, Void>() {
                private boolean synthetic = false;
                @Override
                public Void scan(Tree node, Void p) {
                    if (node == null) return null;
                    boolean oldSynthetic = synthetic;
                    try {
                        synthetic |= getTreeUtilities().isSynthetic(diffContext.origUnit, node);
                        if (!synthetic) {
                            oldTrees.add(node);
                        }
                        addSyntheticTrees(diffContext, node);
                        return super.scan(node, p);
                    } finally {
                        synthetic = oldSynthetic;
                    }
                }

                @Override
                public Void visitForLoop(ForLoopTree node, Void p) {
                    try {
                        return super.visitForLoop(node, p);
                    } finally {
                        oldTrees.removeAll(node.getInitializer());
                    }
                }

                @Override
                public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
                    try {
                        return super.visitEnhancedForLoop(node, p);
                    } finally {
                        oldTrees.remove(node.getVariable());
                    }
                }

                @Override
                public Void visitTry(TryTree node, Void p) {
                    try {
                        return super.visitTry(node, p);
                    } finally {
                        oldTrees.removeAll(node.getResources());
                    }
                }
                
            }.scan(diffContext.origUnit, null);
        } else {
            new TreeScanner<Void, Void>() {
                @Override
                public Void scan(Tree node, Void p) {
                    addSyntheticTrees(diffContext, node);
                    return super.scan(node, p);
                }
            }.scan(diffContext.origUnit, null);
        }

        if (!REWRITE_WHOLE_FILE) {
            new TreePathScanner<Void, Void>() {
                private TreePath currentParent;
                private final Map<Tree, TreePath> tree2Path = new IdentityHashMap<Tree, TreePath>();
                private final FQNComputer fqn = new FQNComputer();
                private final Set<Tree> rewriteTarget;
                
                {
                    rewriteTarget = Collections.newSetFromMap(new IdentityHashMap<Tree, Boolean>());
                    rewriteTarget.addAll(changes.values());
                }

                private TreePath getParentPath(TreePath tp, Tree t) {
                    Tree parent;
                    
                    if (tp != null) {
                        while (tp.getLeaf().getKind() != Kind.COMPILATION_UNIT && getTreeUtilities().isSynthetic(tp)) {
                            tp = tp.getParentPath();
                        }
                        parent = tp.getLeaf();
                    } else {
                        parent = t;
                    }
                    TreePath c = tree2Path.get(parent);

                    if (c == null) {
                        c = tp != null ? tp : new TreePath((CompilationUnitTree) t);
                        tree2Path.put(parent, c);
                    }

                    return c;
                }

                @Override
                public Void scan(Tree tree, Void p) {
                    if (changes.containsKey(tree) || docChanges.containsKey(tree)) {
                        boolean clearCurrentParent = false;
                        if (currentParent == null) {
                            clearCurrentParent = true;
                            currentParent = getParentPath(getCurrentPath(), tree);
                            if (currentParent.getParentPath() != null && currentParent.getParentPath().getLeaf().getKind() == Kind.COMPILATION_UNIT) {
                                currentParent = currentParent.getParentPath();
                            }
                            pathsToRewrite.add(currentParent);
                            if (!parent2Rewrites.containsKey(currentParent)) {
                                parent2Rewrites.put(currentParent, new IdentityHashMap<Tree, Tree>());
                            }
                        }
                        if(changes.containsKey(tree)) {
                            Map<Tree, Tree> rewrites = parent2Rewrites.get(currentParent);

                            Tree rev = changes.remove(tree);

                            rewrites.put(tree, rev);

                            scan(rev, p);
                        } else {
                            super.scan(tree, p);
                        }
                        if (clearCurrentParent) {
                            currentParent = null;
                        }
                    } else {
                        super.scan(tree, p);
                    }
                    return null;
                }

                @Override
                public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                    fqn.setCompilationUnit(node);
                    return super.visitCompilationUnit(node, p);
                }

                @Override
                public Void visitClass(ClassTree node, Void p) {
                    String parent = fqn.getFQN();
                    fqn.enterClass(node);
                    overlay.registerClass(parent, fqn.getFQN(), node, rewriteTarget.contains(node));
                    super.visitClass(node, p);
                    fqn.leaveClass();
                    return null;
                }

            }.scan(diffContext.origUnit, null);
        } else {
            TreePath topLevel = new TreePath(diffContext.origUnit);
            
            pathsToRewrite.add(topLevel);
            parent2Rewrites.put(topLevel, changes);
            fillImports = false;
        }
        
        List<Diff> diffs = new ArrayList<Diff>();
        final ImportAnalysis2 ia = new ImportAnalysis2(this);
        
        boolean importsFilled = false;

        for (final TreePath path : pathsToRewrite) {
            List<ClassTree> classes = new ArrayList<ClassTree>();

            if (path.getParentPath() != null) {
                for (Tree t : path.getParentPath()) {
                    if (t.getKind() == Kind.COMPILATION_UNIT && !importsFilled) {
                        CompilationUnitTree cutt = (CompilationUnitTree) t;
                        ia.setCompilationUnit(cutt);
                        ia.setPackage(cutt.getPackageName());
                        ia.setImports(cutt.getImports());
                        importsFilled = true;
                    }
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                        classes.add((ClassTree) t);
                    }
                }
            } else if (path.getLeaf().getKind() == Kind.COMPILATION_UNIT && parent2Rewrites.get(path).size() == 1) { // XXX: not true if there are doc changes.
                //short-circuit import-only changes:
                CompilationUnitTree origCUT = (CompilationUnitTree) path.getLeaf();
                Tree nue = parent2Rewrites.get(path).get(origCUT);

                if (nue != null && nue.getKind() == Kind.COMPILATION_UNIT) {
                    CompilationUnitTree nueCUT = (CompilationUnitTree) nue;

                    if (   Utilities.compareObjects(origCUT.getPackageAnnotations(), nueCUT.getPackageAnnotations())
                        && Utilities.compareObjects(origCUT.getPackageName(), nueCUT.getPackageName())
                        && Utilities.compareObjects(origCUT.getTypeDecls(), nueCUT.getTypeDecls())) {
                        fillImports = false;
                        diffs.addAll(CasualDiff.diff(getContext(), diffContext, origCUT.getImports(), nueCUT.getImports(), userInfo, tree2Tag, tree2Doc, tag2Span, oldTrees));
                        continue;
                    }
                }
            }

            Collections.reverse(classes);
            
            for (ClassTree ct : classes) {
                ia.classEntered(ct);
                ia.enterVisibleThroughClasses(ct);
            }
            final Map<Tree, Tree> rewrites = parent2Rewrites.get(path);
            
            ImmutableDocTreeTranslator itt = new ImmutableDocTreeTranslator(this) {
                private @NonNull Map<Tree, Tree> map = new HashMap<Tree, Tree>(rewrites);
                private @NonNull Map<DocTree, DocTree> docMap = null;
                private final TreeVisitor<Tree, Void> duplicator = new TreeDuplicator(getContext());

                @Override
                public Tree translate(Tree tree) {
                    if(docChanges.containsKey(tree)) {
                        Tree newTree = null;
                        if(!map.containsKey(tree)) {
                            Tree importComments = GeneratorUtilities.get(WorkingCopy.this).importComments(tree, getCompilationUnit());
                            newTree = importComments.accept(duplicator, null);
                            map.put(tree, newTree);
                        }
                        docMap = docChanges.remove(tree);
                        DocCommentTree oldDoc;
                        DocCommentTree newDoc;
                        Pair<DocCommentTree, DocCommentTree> docChange;
                        if(docMap.size() == 1 && docMap.containsKey(null)) {
                            newDoc = (DocCommentTree) translate((DocCommentTree) docMap.get(null)); // Update QualIdent Trees
                            docChange = Pair.of((DocCommentTree)null, (DocCommentTree)newDoc);
                        } else {
                            oldDoc = ((DocTrees)getTrees()).getDocCommentTree(new TreePath(path, tree));
                            newDoc = (DocCommentTree) translate(oldDoc);
                            docChange = Pair.of(oldDoc, newDoc);
                        }
                        tree2Doc.put(tree, docChange);
                        if(tree != newTree) {
                            tree2Doc.put(newTree, docChange);
                        }
                    }
                    Tree translated = map.remove(tree);

                    if (translated != null) {
                        return translate(translated);
                    } else {
                        return super.translate(tree);
                    }
                }
                
                @Override
                public DocTree translate(DocTree tree) {
                    if(docMap != null) {
                        DocTree translated = docMap.remove(tree);
                        if (translated != null) {
                            return translate(translated);
                        }
                    }
                    return super.translate(tree);
                }
            };
            Context c = impl.getJavacTask().getContext();
            itt.attach(c, ia, tree2Tag);
            Tree brandNew = itt.translate(path.getLeaf());

            //tagging debug
            //System.err.println("brandNew=" + brandNew);
            
            for (ClassTree ct : classes) {
                ia.classLeft();
            }
            
            if (brandNew.getKind() == Kind.COMPILATION_UNIT) {
                fillImports = false;
            }
            diffs.addAll(CasualDiff.diff(getContext(), diffContext, path, (JCTree) brandNew, userInfo, tree2Tag, tree2Doc, tag2Span, oldTrees));
        }

        if (fillImports) {
            Set<? extends Element> nueImports = ia.getImports();

            if (nueImports != null && !nueImports.isEmpty()) { //may happen if no changes, etc.
                CompilationUnitTree ncut = GeneratorUtilities.get(this).addImports(diffContext.origUnit, nueImports);
                diffs.addAll(CasualDiff.diff(getContext(), diffContext, diffContext.origUnit.getImports(), ncut.getImports(), userInfo, tree2Tag, tree2Doc, tag2Span, oldTrees));
            }
        }
        
        diffs.addAll(textualChanges);
        
        userInfo.putAll(this.userInfo);
        
        try {
            return DiffUtilities.diff2ModificationResultDifference(diffContext.file, diffContext.positionConverter, userInfo, codeForCompilationUnit(diffContext.origUnit), diffs);
        } catch (IOException ex) {
            if (!diffContext.file.isValid()) {
                Logger.getLogger(WorkingCopy.class.getName()).log(Level.FINE, null, ex);
                return Collections.emptyList();
            }
            throw ex;
        }
    }
    
    private List<Difference> processExternalCUs(Map<?, int[]> tag2Span, Set<Tree> syntheticTrees) {
        if (externalChanges == null) {
            return Collections.<Difference>emptyList();
        }
        
        List<Difference> result = new LinkedList<Difference>();
        
        for (CompilationUnitTree t : externalChanges.values()) {
            try {
                FileObject targetFile = doCreateFromTemplate(t);
                CompilationUnitTree templateCUT = impl.getJavacTask().parse(FileObjects.nbFileObject(targetFile, targetFile.getParent())).iterator().next();
                CompilationUnitTree importComments = GeneratorUtilities.get(this).importComments(templateCUT, templateCUT);

                changes.put(importComments, t);

                StringWriter target = new StringWriter();

                ModificationResult.commit(targetFile, processCurrentCompilationUnit(new DiffContext(this, templateCUT, codeForCompilationUnit(templateCUT), new PositionConverter(), targetFile, syntheticTrees, getFileObject() != null ? getCompilationUnit() : null, getFileObject() != null ? getText() : null), tag2Span), target);
                result.add(new CreateChange(t.getSourceFile(), target.toString()));
                target.close();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return result;
    }

    String template(ElementKind kind) {
        if(kind == null) {
            return "Templates/Classes/Empty.java"; // NOI18N
        }
        switch (kind) {
            case CLASS: return "Templates/Classes/Class.java"; // NOI18N
            case INTERFACE: return "Templates/Classes/Interface.java"; // NOI18N
            case ANNOTATION_TYPE: return "Templates/Classes/AnnotationType.java"; // NOI18N
            case ENUM: return "Templates/Classes/Enum.java"; // NOI18N
            case PACKAGE: return "Templates/Classes/package-info.java"; // NOI18N
            default:
                Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, "Cannot resolve template for {0}", kind);
                return "Templates/Classes/Empty.java"; // NOI18N
        }
    }

    FileObject doCreateFromTemplate(CompilationUnitTree cut) throws IOException {
        ElementKind kind;
        if ("package-info.java".equals(cut.getSourceFile().getName())) {
            kind = ElementKind.PACKAGE;
        } else if (cut.getTypeDecls().isEmpty()) {
            kind = null;
        } else {
            switch (cut.getTypeDecls().get(0).getKind()) {
                case CLASS:
                    kind = ElementKind.CLASS;
                    break;
                case INTERFACE:
                    kind = ElementKind.INTERFACE;
                    break;
                case ANNOTATION_TYPE:
                    kind = ElementKind.ANNOTATION_TYPE;
                    break;
                case ENUM:
                    kind = ElementKind.ENUM;
                    break;
                default:
                    Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, "Cannot resolve template for {0}", cut.getTypeDecls().get(0).getKind());
                    kind = null;
            }
        }
        FileObject template = FileUtil.getConfigFile(template(kind));
        return doCreateFromTemplate(template, cut.getSourceFile());
    }

    FileObject doCreateFromTemplate(FileObject template, JavaFileObject sourceFile) throws IOException {
        FileObject scratchFolder = FileUtil.createMemoryFileSystem().getRoot();

        if (template == null) {
            return scratchFolder.createData("out", "java");
        }

        DataObject templateDO = DataObject.find(template);

        if (!templateDO.isTemplate()) {
            return scratchFolder.createData("out", "java");
        }

        File pack = Utilities.toFile(sourceFile.toUri()).getParentFile();

        while (FileUtil.toFileObject(pack) == null) {
            pack = pack.getParentFile();
        }

        FileObject targetFolder = FileUtil.toFileObject(pack);
        DataObject targetDataFolder = DataFolder.findFolder(targetFolder);

        scratchFolder.setAttribute(OverlayTemplateAttributesProvider.ATTR_ORIG_FILE, targetDataFolder);

        String name = FileObjects.getName(sourceFile, true);
        DataObject newFile = templateDO.createFromTemplate(DataFolder.findFolder(scratchFolder), name);

        return newFile.getPrimaryFile();
    }

    List<Difference> getChanges(Map<?, int[]> tag2Span) throws IOException, BadLocationException {
        if (afterCommit)
            throw new IllegalStateException("The commit method can be called only once on a WorkingCopy instance");   //NOI18N
        afterCommit = true;
        
        if (changes == null) {
            //may happen when the modification task does not call toPhase at all.
            return null;
        }
        
        if (externalChanges != null) {
            for (CompilationUnitTree t : externalChanges.values()) {
                final FQNComputer fqn = new FQNComputer();

                fqn.setCompilationUnit(t);
                overlay.registerPackage(fqn.getFQN());

                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        String parent = fqn.getFQN();
                        fqn.enterClass(node);
                        overlay.registerClass(parent, fqn.getFQN(), node, true);
                        super.visitClass(node, p);
                        fqn.leaveClass();
                        return null;
                    }
                }.scan(t, null);
            }
        }
        List<Difference> result = new LinkedList<Difference>();
        Set<Tree> syntheticTrees = new HashSet<Tree>();
        
        if (getFileObject() != null) {
            result.addAll(processCurrentCompilationUnit(new DiffContext(this, syntheticTrees), tag2Span));
        }
        
        result.addAll(processExternalCUs(tag2Span, syntheticTrees));

        overlay.clearElementsCache();
        
        return result;
    }
    
    private void createCompilationUnit(JCTree.JCCompilationUnit unitTree) {
        if (externalChanges == null) externalChanges = new HashMap<JavaFileObject, CompilationUnitTree>();
        externalChanges.put(unitTree.getSourceFile(), unitTree);
        return;
    }
    
}
