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
package org.netbeans.modules.java.editor.fold;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.java.JavaFoldManager;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.editor.semantic.ScanningCancellableTask;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class JavaElementFoldManager extends JavaFoldManager {
    
    private FoldOperation operation;
    private FileObject    file;
    private JavaElementFoldTask task;
    
    /**
     * Default folding of individual fold types. New instance is created
     * when a new FoldManager opens (= editor appears).
     */
    private static class Presets {
        // Folding presets
        private boolean foldImportsPreset = false;
        private boolean foldInnerClassesPreset = false;
        private boolean foldJavadocsPreset = false;
        private boolean foldCodeBlocksPreset = false;
        private boolean foldInitialCommentsPreset = false;

        public Presets() {
            Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
            foldInitialCommentsPreset = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT, foldInitialCommentsPreset);
            foldImportsPreset = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_COLLAPSE_IMPORT, foldImportsPreset);
            foldCodeBlocksPreset = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_COLLAPSE_METHOD, foldCodeBlocksPreset);
            foldInnerClassesPreset = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_COLLAPSE_INNERCLASS, foldInnerClassesPreset);
            foldJavadocsPreset = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_COLLAPSE_JAVADOC, foldJavadocsPreset);
        }

        private static volatile Presets CURRENT;

        static void refresh() {
            CURRENT = null;
        }

        static Presets get() {
            Presets p = CURRENT;
            if (p != null) {
                return p;
            }
            return CURRENT = new Presets();
        }
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
        Presets.refresh();
    }

    public synchronized void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        Object od = doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od instanceof DataObject) {
            FileObject file = ((DataObject)od).getPrimaryFile();

            currentFolds = new ArrayList<FoldInfo>();
            task = JavaElementFoldTask.getTask(file);
            task.setJavaElementFoldManager(JavaElementFoldManager.this, file);
        }
    }
    
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void removeEmptyNotify(Fold emptyFold) {
        removeDamagedNotify(emptyFold);
    }

    public void removeDamagedNotify(Fold damagedFold) {
        currentFolds.remove(operation.getExtraInfo(damagedFold));
        if (importsFold == damagedFold) {
            importsFold = null;//not sure if this is correct...
        }
        if (initialCommentFold == damagedFold) {
            initialCommentFold = null;//not sure if this is correct...
        }
    }

    public void expandNotify(Fold expandedFold) {
    }

    public synchronized void release() {
        if (task != null)
            task.setJavaElementFoldManager(this, null);
        
        task         = null;
        file         = null;
        currentFolds = null;
        importsFold  = null;
        initialCommentFold = null;
    }
    
    static final class JavaElementFoldTask extends ScanningCancellableTask<CompilationInfo> {
        
        //XXX: this will hold JavaElementFoldTask as long as the FileObject exists:
        private static Map<DataObject, JavaElementFoldTask> file2Task = new WeakHashMap<DataObject, JavaElementFoldTask>();
        
        static JavaElementFoldTask getTask(FileObject file) {
            try {
                DataObject od = DataObject.find(file);
                JavaElementFoldTask task = file2Task.get(od);

                if (task == null) {
                    file2Task.put(od,
                            task = new JavaElementFoldTask());
                }

                return task;
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger(JavaElementFoldManager.class.getName()).log(Level.FINE, null, ex);
                return new JavaElementFoldTask();
            }
        }
        
        /**
         * All managers attched to this fold task
         */
        private Collection<Reference<JavaElementFoldManager>> managers = 
                new ArrayList<Reference<JavaElementFoldManager>>(2);

        synchronized void setJavaElementFoldManager(JavaElementFoldManager manager, FileObject file) {
            if (file == null) {
                for (Iterator<Reference<JavaElementFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                    Reference<JavaElementFoldManager> ref = it.next();
                    JavaElementFoldManager fm = ref.get();
                    if (fm == null || fm == manager) {
                        it.remove();
                        break;
                    }
                }
            } else {
                managers.add(new WeakReference<JavaElementFoldManager>(manager));
                JavaElementFoldManagerTaskFactory.doRefresh(file);
            }
        }
        
        private synchronized Object findLiveManagers() {
            JavaElementFoldManager oneMgr = null;
            List<JavaElementFoldManager> result = null;
            
            for (Iterator<Reference<JavaElementFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                Reference<JavaElementFoldManager> ref = it.next();
                JavaElementFoldManager fm = ref.get();
                if (fm == null) {
                    it.remove();
                    continue;
                }
                if (result != null) {
                    result.add(fm);
                } else if (oneMgr != null) {
                    result = new ArrayList<JavaElementFoldManager>(2);
                    result.add(oneMgr);
                    result.add(fm);
                } else {
                    oneMgr = fm;
                }
            }
            return result != null ? result : oneMgr;
        }
        
        public void run(final CompilationInfo info) {
            resume();
            
            final Object mgrs = findLiveManagers();            
            
            if (mgrs == null) {
                return ;
            }
            
            long startTime = System.currentTimeMillis();

            final CompilationUnitTree cu = info.getCompilationUnit();
            Document doc = info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return;
            }
            
            final JavaElementFoldVisitor v = new JavaElementFoldVisitor(info, 
                    cu, info.getTrees().getSourcePositions(), doc, Presets.get());
            
            scan(v, cu, null);
            
            if (v.stopped || isCancelled())
                return ;
            
            //check for initial fold:
            v.checkInitialFold();
            
            if (v.stopped || isCancelled())
                return ;

            Collections.sort(v.folds);
            if (mgrs instanceof JavaElementFoldManager) {
                SwingUtilities.invokeLater(((JavaElementFoldManager)mgrs).new CommitFolds(v.folds));
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    Collection<JavaElementFoldManager> jefms = (Collection<JavaElementFoldManager>)mgrs;
                    public void run() {
                        for (JavaElementFoldManager jefm : jefms) {
                            jefm.new CommitFolds(v.folds).run();
                        }
                }});
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 1",
                    new Object[] {info.getFileObject(), endTime - startTime});
        }
        
    }
    
    private class CommitFolds implements Runnable {
        
        private boolean insideRender;
        private List<FoldInfo> infos;
        private long startTime;
        
        public CommitFolds(List<FoldInfo> infos) {
            this.infos = infos;
        }
        
        /**
         * For singular folds, if they exist in the FoldManager already
         * ignores the default state, and takes it from the actual state of
         * existing fold.
         */
        private boolean mergeSpecialFoldState(FoldInfo fi) {
            if (fi.template == IMPORTS_FOLD_TEMPLATE) {
                if (importsFold != null) {
                    return importsFold.isCollapsed();
                }
            } else if (fi.template == INITIAL_COMMENT_FOLD_TEMPLATE) {
                if (initialCommentFold != null) {
                    return initialCommentFold.isCollapsed();
                }
            }
            return fi.collapseByDefault;
        }
        
        public void run() {
            if (!insideRender) {
                startTime = System.currentTimeMillis();
                insideRender = true;
                operation.getHierarchy().getComponent().getDocument().render(this);
                
                return;
            }
            
            operation.getHierarchy().lock();
            
            try {
                FoldHierarchyTransaction tr = operation.openTransaction();
                
                try {
                    if (currentFolds == null)
                        return ;

                    List<FoldInfo> updatedFolds = new ArrayList<FoldInfo>(infos.size());
                    Iterator<FoldInfo> itExisting = currentFolds.iterator();
                    Iterator<FoldInfo> itNew = infos.iterator();
                    FoldInfo currentExisting = itExisting.hasNext() ? itExisting.next() : null;
                    FoldInfo currentNew = itNew.hasNext() ? itNew.next() : null;

                    while (currentExisting != null || currentNew != null) {
                        int order = currentExisting != null && currentNew != null ? currentExisting.compareTo(currentNew) : currentExisting != null ? -1 : 1;

                        if (order < 0) {
                            //fold removed:
                            operation.removeFromHierarchy(currentExisting.fold, tr);

                            if (importsFold == currentExisting.fold) {
                                importsFold = null;
                            }

                            if (initialCommentFold == currentExisting.fold) {
                                initialCommentFold = null;
                            }
                            
                            currentExisting = itExisting.hasNext() ? itExisting.next() : null;
                        } else {
                            //added or remains:
                            if (order > 0) {
                                //added:
                                int start = currentNew.start.getOffset();
                                int end   = currentNew.end.getOffset();

                                if (end > start &&
                                        (end - start) > (currentNew.template.getStartGuardedLength() + currentNew.template.getEndGuardedLength())) {
                                    Fold f = operation.addToHierarchy(currentNew.template.getType(),
                                            currentNew.template.getDescription(),
                                            mergeSpecialFoldState(currentNew),
                                            start,
                                            end,
                                            currentNew.template.getStartGuardedLength(),
                                            currentNew.template.getEndGuardedLength(),
                                            currentNew,
                                            tr);

                                    currentNew.fold = f;

                                    if (currentNew.template == IMPORTS_FOLD_TEMPLATE) {
                                        importsFold = f;
                                    }
                                    
                                    if (currentNew.template == INITIAL_COMMENT_FOLD_TEMPLATE) {
                                        initialCommentFold = f;
                                    }

                                    updatedFolds.add(currentNew);
                                }
                            } else {
                                updatedFolds.add(currentExisting);
                                currentExisting = itExisting.hasNext() ? itExisting.next() : null;
                            }

                            FoldInfo newNew = itNew.hasNext() ? itNew.next() : null;

                            // XXX: In some situations infos contains duplicate folds and we don't
                            // want to add the same multiple times. The situation that I came across
                            // was with having an empty enum subclass with javadoc. The javadoc fold
                            // was added twice - once from visitClass and second time from visitMethod
                            // for the <init> node, which for some reason has the same offset as the
                            // the enum inner class.
                            while (newNew != null && currentNew.compareTo(newNew) == 0) {
                                newNew = itNew.hasNext() ? itNew.next() : null;
                            }

                            currentNew = newNew;
                        }
                    }
                    
                    currentFolds = updatedFolds;
                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    tr.commit();
                }
            } finally {
                operation.getHierarchy().unlock();
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 2",
                    new Object[] {file, endTime - startTime});
        }
    }

    //@GuardedBy(FoldOperation.openTransaction())
    private List<FoldInfo> currentFolds; //in natural order
    private Fold initialCommentFold;
    private Fold importsFold;
    
    private static final class JavaElementFoldVisitor extends CancellableTreePathScanner<Object, Object> {
        
        private List<FoldInfo> folds = new ArrayList<JavaElementFoldManager.FoldInfo>();
        private CompilationInfo info;
        private CompilationUnitTree cu;
        private SourcePositions sp;
        private boolean stopped;
        private int initialCommentStopPos = Integer.MAX_VALUE;
        private Document doc;
        private Presets presets;
        
        public JavaElementFoldVisitor(CompilationInfo info, CompilationUnitTree cu, SourcePositions sp, Document doc, Presets presets) {
            this.info = info;
            this.cu = cu;
            this.sp = sp;
            this.doc = doc;
            this.presets = presets;
        }
        
        public void checkInitialFold() {
            try {
                TokenHierarchy<?> th = info.getTokenHierarchy();
                TokenSequence<JavaTokenId>  ts = th.tokenSequence(JavaTokenId.language());
                
                while (ts.moveNext()) {
                    if (ts.offset() >= initialCommentStopPos)
                        break;
                    
                    Token<JavaTokenId> token = ts.token();
                    
                    if (token.id() == JavaTokenId.BLOCK_COMMENT || token.id() == JavaTokenId.JAVADOC_COMMENT) {
                        int startOffset = ts.offset();
                        boolean collapsed = presets.foldInitialCommentsPreset;

                        /*
                        if (initialCommentFold != null) {
                            collapsed = initialCommentFold.isCollapsed();
                        }
                        */
                        
                        folds.add(new FoldInfo(doc, startOffset, startOffset + token.length(), INITIAL_COMMENT_FOLD_TEMPLATE, collapsed));
                        break;
                    }
                }
            } catch (BadLocationException e) {
                //the document probably changed, stop
                stopped = true;
            } catch (ConcurrentModificationException e) {
                //from TokenSequence, document probably changed, stop
                stopped = true;
            }
        }
        
        private void handleJavadoc(Tree t) throws BadLocationException, ConcurrentModificationException {
            int start = (int) sp.getStartPosition(cu, t);
            
            if (start == (-1))
                return ;
            
            if (start < initialCommentStopPos)
                initialCommentStopPos = start;

            TokenHierarchy<?> th = info.getTokenHierarchy();
            TokenSequence<JavaTokenId>  ts = th.tokenSequence(JavaTokenId.language());
            
            if (ts.move(start) == Integer.MAX_VALUE) {
                return;//nothing
            }
            
            while (ts.movePrevious()) {
                Token<JavaTokenId> token = ts.token();
                
                if (token.id() == JavaTokenId.JAVADOC_COMMENT) {
                    int startOffset = ts.offset();
                    folds.add(new FoldInfo(doc, startOffset, startOffset + token.length(), JAVADOC_FOLD_TEMPLATE, presets.foldJavadocsPreset));
                    if (startOffset < initialCommentStopPos)
                        initialCommentStopPos = startOffset;
                }
                if (   token.id() != JavaTokenId.WHITESPACE
                    && token.id() != JavaTokenId.BLOCK_COMMENT
                    && token.id() != JavaTokenId.LINE_COMMENT)
                    break;
            }
        }
        
        private void handleTree(Tree node, Tree javadocTree, boolean handleOnlyJavadoc) {
            try {
                if (!handleOnlyJavadoc) {
                    int start = (int)sp.getStartPosition(cu, node);
                    int end   = (int)sp.getEndPosition(cu, node);
                    
                    if (start != (-1) && end != (-1))
                        folds.add(new FoldInfo(doc, start, end, CODE_BLOCK_FOLD_TEMPLATE, presets.foldCodeBlocksPreset));
                }
                
                handleJavadoc(javadocTree != null ? javadocTree : node);
            } catch (BadLocationException e) {
                //the document probably changed, stop
                stopped = true;
            } catch (ConcurrentModificationException e) {
                //from TokenSequence, document probably changed, stop
                stopped = true;
            }
        }
        
        @Override
        public Object visitMethod(MethodTree node, Object p) {
            super.visitMethod(node, p);
            handleTree(node.getBody(), node, false);
            return null;
        }

        @Override
        public Object visitClass(ClassTree node, Object p) {
            super.visitClass(node, Boolean.TRUE);
            try {
                if (p == Boolean.TRUE) {
                    int start = Utilities.findBodyStart(node, cu, sp, doc);
                    int end   = (int)sp.getEndPosition(cu, node);
                    
                    if (start != (-1) && end != (-1))
                        folds.add(new FoldInfo(doc, start, end, CODE_BLOCK_FOLD_TEMPLATE, presets.foldInnerClassesPreset));
                }
                
                handleJavadoc(node);
            } catch (BadLocationException e) {
                //the document probably changed, stop
                stopped = true;
            } catch (ConcurrentModificationException e) {
                //from TokenSequence, document probably changed, stop
                stopped = true;
            }
            return null;
        }
        
        @Override
        public Object visitVariable(VariableTree node,Object p) {
            super.visitVariable(node, p);
            if (TreeUtilities.CLASS_TREE_KINDS.contains(getCurrentPath().getParentPath().getLeaf().getKind()))
                handleTree(node, null, true);
            return null;
        }
        
        @Override
        public Object visitBlock(BlockTree node, Object p) {
            super.visitBlock(node, p);
            //check static/dynamic initializer:
            TreePath path = getCurrentPath();
            
            if (TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
                handleTree(node, null, false);
            }
            
            return null;
        }
        
        @Override
        public Object visitCompilationUnit(CompilationUnitTree node, Object p) {
            int importsStart = Integer.MAX_VALUE;
            int importsEnd   = -1;
            
            for (ImportTree imp : node.getImports()) {
                int start = (int) sp.getStartPosition(cu, imp);
                int end   = (int) sp.getEndPosition(cu, imp);
                
                if (importsStart > start)
                    importsStart = start;
                
                if (end > importsEnd) {
                    importsEnd = end;
                }
            }
            
            if (importsEnd != (-1) && importsStart != (-1)) {
                if (importsStart < initialCommentStopPos)
                    initialCommentStopPos = importsStart;
                
                try {
                    boolean collapsed = presets.foldImportsPreset;
                    
                    /*
                    if (importsFold != null) {
                        collapsed = importsFold.isCollapsed();
                    }
                    */
                    
                    importsStart += 7/*"import ".length()*/;
                    
                    if (importsStart < importsEnd) {
                        folds.add(new FoldInfo(doc, importsStart , importsEnd, IMPORTS_FOLD_TEMPLATE, collapsed));
                    }
                } catch (BadLocationException e) {
                    //the document probably changed, stop
                    stopped = true;
                }
            }
            return super.visitCompilationUnit(node, p);
        }

    }
    
    protected static final class FoldInfo implements Comparable {
        
        private final Position start;
        private final Position end;
        private final FoldTemplate template;
        private final boolean collapseByDefault;
        //@GUardedBy(FoldOperation.openTransaction())
        private Fold fold;
        
        public FoldInfo(Document doc, int start, int end, FoldTemplate template, boolean collapseByDefault) throws BadLocationException {
            this.start = doc.createPosition(start);
            this.end   = doc.createPosition(end);
            this.template = template;
            this.collapseByDefault = collapseByDefault;
        }
        
        public int compareTo(Object o) {
            FoldInfo remote = (FoldInfo) o;
            
            if (start.getOffset() < remote.start.getOffset()) {
                return -1;
            }
            
            if (start.getOffset() > remote.start.getOffset()) {
                return 1;
            }
            
            if (end.getOffset() < remote.end.getOffset()) {
                return -1;
            }
            
            if (end.getOffset() > remote.end.getOffset()) {
                return 1;
            }

            //XXX: abusing the length of the fold description to implement ordering (the exact order does not matter in this case):
            return template.getDescription().length() - remote.template.getDescription().length();
        }

        @Override
        public String toString() {
            return "FoldInfo[" + start.getOffset() + ", " + end.getOffset() + ", " + template.getDescription() + "]";
        }
    }
    
}
