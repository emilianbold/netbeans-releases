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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
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
import java.util.Comparator;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import javax.tools.JavaFileObject;
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
import org.netbeans.modules.java.source.engine.SourceReader;
import org.netbeans.modules.java.source.engine.SourceRewriter;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.pretty.ImportAnalysis2;
import org.netbeans.modules.java.source.save.CasualDiff;
import org.netbeans.modules.java.source.save.DiffContext;
import org.netbeans.modules.java.source.save.ElementOverlay;
import org.netbeans.modules.java.source.save.ElementOverlay.FQNComputer;
import org.netbeans.modules.java.source.save.OverlayTemplateAttributesProvider;
import org.netbeans.modules.java.source.transform.ImmutableTreeTranslator;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

/**XXX: extends CompilationController now, finish method delegation
 *
 * @author Dusan Balek, Petr Hrebejk, Tomas Zezula
 */
public class WorkingCopy extends CompilationController {

    static Reference<WorkingCopy> instance;
    private Map<Tree, Tree> changes;
    private Map<JavaFileObject, CompilationUnitTree> externalChanges;
    private Set<Diff> textualChanges;
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
        tree2Tag = new IdentityHashMap<Tree, Object>();
        externalChanges = null;
        textualChanges = new HashSet<Diff>();
        userInfo = new HashMap<Integer, String>();

        if (getContext().get(ElementOverlay.class) == null) {
            getContext().put(ElementOverlay.class, overlay);
        }
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
     * <code>newTree</code>. <code>null</code> values are not allowed.
     * Use methods in {@link TreeMaker} for tree element removal.
     * 
     * @param oldTree  tree to be replaced, use tree already represented in
     *                 source code.
     * @param newTree  new tree, either created by <code>TreeMaker</code>
     *                 or obtained from different place.
     * @throws IllegalStateException if <code>toPhase()</code> method was not
     *         called before.
     * @throws IllegalArgumentException when <code>null</code> was passed to the 
     *         method.
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
    
    // Package private methods -------------------------------------------------        
    
    private static void commit(CompilationUnitTree topLevel, List<Diff> diffs, SourceRewriter out) throws IOException, BadLocationException {
        SourceReader in = null;
        try {
            String s = ((JCTree.JCCompilationUnit) topLevel).sourcefile.getCharContent(true).toString();
            char[] buf = s.toCharArray();
            in = new SourceReader(buf);

            // Copy any leading comments.
            for (Diff d : diffs) {
                switch (d.type) {
                    case INSERT:
                        out.copyTo(in, d.getPos());
                        out.writeTo(d.getText());
                        break;
                    case DELETE:
                        out.copyTo(in, d.getPos());
                        out.skipThrough(in, d.getEnd());
                        break;
                    default:
                        throw new AssertionError("unknown CasualDiff type: " + d.type);
                }
            }
            out.copyRest(in);
        } finally {
            if (in != null)
                in.close();
        }
        
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
    
    private List<Difference> processCurrentCompilationUnit(DiffContext diffContext, Map<?, int[]> tag2Span) throws IOException, BadLocationException {
        final Set<TreePath> pathsToRewrite = new LinkedHashSet<TreePath>();
        final Map<TreePath, Map<Tree, Tree>> parent2Rewrites = new IdentityHashMap<TreePath, Map<Tree, Tree>>();
        boolean fillImports = true;
        
        Map<Integer, String> userInfo = new HashMap<Integer, String>();
        final Set<Tree> oldTrees = new HashSet<Tree>();

        if (CasualDiff.OLD_TREES_VERBATIM) {
            new TreeScanner<Void, Void>() {
                @Override
                public Void scan(Tree node, Void p) {
                    oldTrees.add(node);
                    return super.scan(node, p);
                }
            }.scan(diffContext.origUnit, null);
        }
        
        if (!REWRITE_WHOLE_FILE) {
            new TreePathScanner<Void, Void>() {
                private TreePath currentParent;
                private final Map<Tree, TreePath> tree2Path = new IdentityHashMap<Tree, TreePath>();
                private final FQNComputer fqn = new FQNComputer();

                private TreePath getParentPath(TreePath tp, Tree t) {
                    Tree parent = tp != null ? tp.getLeaf() : t;
                    TreePath c = tree2Path.get(parent);

                    if (c == null) {
                        c = tp != null ? tp : new TreePath((CompilationUnitTree) t);
                        tree2Path.put(parent, c);
                    }

                    return c;
                }

                @Override
                public Void scan(Tree tree, Void p) {
                    boolean clearCurrentParent = false;
                    if (changes.containsKey(tree)) {
                        if (currentParent == null) {
                            clearCurrentParent = true;
                            currentParent = getParentPath(getCurrentPath(), tree);
                            pathsToRewrite.add(currentParent);
                            if (!parent2Rewrites.containsKey(currentParent)) {
                                parent2Rewrites.put(currentParent, new IdentityHashMap<Tree, Tree>());
                            }
                        }

                        Map<Tree, Tree> rewrites = parent2Rewrites.get(currentParent);

                        Tree rev = changes.remove(tree);
                        
                        rewrites.put(tree, rev);
                        
                        scan(rev, p);
                        
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
                    overlay.registerClass(parent, fqn.getFQN(), node);
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
        ImportAnalysis2 ia = new ImportAnalysis2(this);
        
        boolean importsFilled = false;

        for (TreePath path : pathsToRewrite) {
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
            }

            Collections.reverse(classes);
            
            for (ClassTree ct : classes) {
                ia.classEntered(ct);
            }

            Tree brandNew = getTreeUtilities().translate(path.getLeaf(), parent2Rewrites.get(path), ia, tree2Tag);

            //tagging debug
            //System.err.println("brandNew=" + brandNew);
            
            for (ClassTree ct : classes) {
                ia.classLeft();
            }
            
            if (brandNew.getKind() == Kind.COMPILATION_UNIT) {
                fillImports = false;
            }

            diffs.addAll(CasualDiff.diff(getContext(), diffContext, path, (JCTree) brandNew, userInfo, tree2Tag, tag2Span, oldTrees));
        }

        if (fillImports) {
            Set<? extends Element> nueImports = ia.getImports();

            if (nueImports != null && !nueImports.isEmpty()) { //may happen if no changes, etc.
                CompilationUnitTree ncut = GeneratorUtilities.get(this).addImports(diffContext.origUnit, nueImports);
                diffs.addAll(CasualDiff.diff(getContext(), diffContext, diffContext.origUnit.getImports(), ncut.getImports(), userInfo, tree2Tag, tag2Span, oldTrees));
            }
        }
        
        diffs.addAll(textualChanges);
        
        userInfo.putAll(this.userInfo);
        
        Collections.sort(diffs, new Comparator<Diff>() {
            public int compare(Diff o1, Diff o2) {
                return o1.getPos() - o2.getPos();
            }
        });

        Rewriter r = new Rewriter(diffContext.file, diffContext.positionConverter, userInfo);
        commit(diffContext.origUnit, diffs, r);

        return r.diffs;
    }
    
    private List<Difference> processExternalCUs(Map<?, int[]> tag2Span) {
        if (externalChanges == null) {
            return Collections.<Difference>emptyList();
        }
        
        List<Difference> result = new LinkedList<Difference>();
        
        for (CompilationUnitTree t : externalChanges.values()) {
            try {
                FileObject targetFile = doCreateFromTemplate(t);
                CompilationUnitTree templateCUT = impl.getJavacTask().parse(FileObjects.nbFileObject(targetFile, targetFile.getParent())).iterator().next();

                changes.put(templateCUT, t);

                StringWriter target = new StringWriter();

                ModificationResult.commit(targetFile, processCurrentCompilationUnit(new DiffContext(this, templateCUT, targetFile.asText(), new PositionConverter(), targetFile), tag2Span), target);
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

    private static String template(CompilationUnitTree cut) {
        if ("package-info.java".equals(cut.getSourceFile().getName())) return "Templates/Classes/package-info.java";
        if (cut.getTypeDecls().isEmpty()) return "Templates/Classes/Empty.java";

        switch (cut.getTypeDecls().get(0).getKind()) {
            case CLASS: return "Templates/Classes/Class.java"; // NOI18N
            case INTERFACE: return "Templates/Classes/Interface.java"; // NOI18N
            case ANNOTATION_TYPE: return "Templates/Classes/AnnotationType.java"; // NOI18N
            case ENUM: return "Templates/Classes/Enum.java"; // NOI18N
            default:
                Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, "Cannot resolve template for {0}", cut.getTypeDecls().get(0).getKind());
                return "Templates/Classes/Empty.java";
        }
    }

    private static FileObject doCreateFromTemplate(CompilationUnitTree t) throws IOException {
        FileObject scratchFolder = FileUtil.createMemoryFileSystem().getRoot();

        FileObject template = FileUtil.getConfigFile(template(t));

        if (template == null) {
            return scratchFolder.createData("out", "java");
        }

        DataObject templateDO = DataObject.find(template);

        if (!templateDO.isTemplate()) {
            return scratchFolder.createData("out", "java");
        }

        File pack = new File(t.getSourceFile().toUri()).getParentFile();

        while (FileUtil.toFileObject(pack) == null) {
            pack = pack.getParentFile();
        }

        FileObject targetFolder = FileUtil.toFileObject(pack);
        DataObject targetDataFolder = DataFolder.findFolder(targetFolder);

        scratchFolder.setAttribute(OverlayTemplateAttributesProvider.ATTR_ORIG_FILE, targetDataFolder);

        DataObject newFile = templateDO.createFromTemplate(DataFolder.findFolder(scratchFolder));

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
                        overlay.registerClass(parent, fqn.getFQN(), node);
                        super.visitClass(node, p);
                        fqn.leaveClass();
                        return null;
                    }
                }.scan(t, null);
            }
        }
        List<Difference> result = new LinkedList<Difference>();
        
        if (getFileObject() != null) {
            result.addAll(processCurrentCompilationUnit(new DiffContext(this), tag2Span));
        }
        
        result.addAll(processExternalCUs(tag2Span));

        overlay.clearElementsCache();
        
        return result;
    }
    
    private void createCompilationUnit(JCTree.JCCompilationUnit unitTree) {
        if (externalChanges == null) externalChanges = new HashMap<JavaFileObject, CompilationUnitTree>();
        externalChanges.put(unitTree.getSourceFile(), unitTree);
        return;
    }
    
    // Innerclasses ------------------------------------------------------------
    private static class Rewriter implements SourceRewriter {

        private int offset = 0;
        private CloneableEditorSupport ces;
        private PositionConverter converter;
        private List<Difference> diffs = new LinkedList<Difference>();
        private Map<Integer, String> userInfo;

        public Rewriter(FileObject fo, PositionConverter converter, Map<Integer, String> userInfo) throws IOException {
            this.converter = converter;
            this.userInfo = userInfo;
            if (fo != null) {
                DataObject dObj = DataObject.find(fo);
                ces = dObj != null ? (CloneableEditorSupport)dObj.getCookie(EditorCookie.class) : null;
            }
            if (ces == null)
                throw new IOException("Could not find CloneableEditorSupport for " + FileUtil.getFileDisplayName (fo)); //NOI18N
        }

        public void writeTo(String s) throws IOException, BadLocationException {                
            Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
            if (diff != null && diff.getKind() == Difference.Kind.REMOVE && diff.getEndPosition().getOffset() == offset) {
                diff.kind = Difference.Kind.CHANGE;
                diff.newText = s;
            } else {
                int off = converter != null ? converter.getOriginalPosition(offset) : offset;
                if (off >= 0)
                    diffs.add(new Difference(Difference.Kind.INSERT, ces.createPositionRef(off, Bias.Forward), ces.createPositionRef(off, Bias.Backward), null, s, userInfo.get(offset)));
            }
        }

        public void skipThrough(SourceReader in, int pos) throws IOException, BadLocationException {
            char[] buf = in.getCharsTo(pos);
            Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
            if (diff != null && diff.getKind() == Difference.Kind.INSERT && diff.getStartPosition().getOffset() == offset) {
                diff.kind = Difference.Kind.CHANGE;
                diff.oldText = new String(buf);
            } else {
                int off = converter != null ? converter.getOriginalPosition(offset) : offset;
                if (off >= 0)
                    diffs.add(new Difference(Difference.Kind.REMOVE, ces.createPositionRef(off, Bias.Forward), ces.createPositionRef(off + buf.length, Bias.Backward), new String(buf), null, userInfo.get(offset)));
            }
            offset += buf.length;
        }

        public void copyTo(SourceReader in, int pos) throws IOException {
            char[] buf = in.getCharsTo(pos);
            offset += buf.length;
        }

        public void copyRest(SourceReader in) throws IOException {
        }

        public void close(boolean save) {
        }
    }
}
