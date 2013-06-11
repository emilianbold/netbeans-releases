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
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.spi.editor.fold.FoldInfo;
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
    
    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    public synchronized void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        Object od = doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od instanceof DataObject) {
            FileObject file = ((DataObject)od).getPrimaryFile();

            task = JavaElementFoldTask.getTask(file);
            task.setJavaElementFoldManager(JavaElementFoldManager.this, file);
        }
    }
    
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        invalidate();
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        invalidate();
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void removeEmptyNotify(Fold emptyFold) {
        removeDamagedNotify(emptyFold);
    }

    public void removeDamagedNotify(Fold damagedFold) {
    }

    public void expandNotify(Fold expandedFold) {
    }

    public synchronized void release() {
        if (task != null)
            task.setJavaElementFoldManager(this, null);
        
        task         = null;
        file         = null;
    }
    
    private synchronized void invalidate() {
        if (task != null) {
            task.invalidate();
        }
    }
    
    static final class JavaElementFoldTask extends ScanningCancellableTask<CompilationInfo> {
        
        //XXX: this will hold JavaElementFoldTask as long as the FileObject exists:
        private final static Map<DataObject, JavaElementFoldTask> file2Task = new WeakHashMap<DataObject, JavaElementFoldTask>();
        
        private AtomicLong version = new AtomicLong(0);
        
        static JavaElementFoldTask getTask(FileObject file) {
            try {
                DataObject od = DataObject.find(file);
                synchronized (file2Task) {
                    JavaElementFoldTask task = file2Task.get(od);

                    if (task == null) {
                        file2Task.put(od,
                                task = new JavaElementFoldTask());
                    }
                    return task;
                }
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
        
        void invalidate() {
            version.incrementAndGet();
        }

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
            final Document doc = info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return;
            }
            
            final JavaElementFoldVisitor v = new JavaElementFoldVisitor(info, 
                    cu, info.getTrees().getSourcePositions(), doc);
            
            scan(v, cu, null);
            
            final long stamp = version.get();
            
            if (v.stopped || isCancelled())
                return ;
            
            //check for initial fold:
            v.checkInitialFold();
            
            if (v.stopped || isCancelled())
                return ;
            
            if (mgrs instanceof JavaElementFoldManager) {
                SwingUtilities.invokeLater(
                        ((JavaElementFoldManager)mgrs).new CommitFolds(doc, v.folds, version, stamp)
                );
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    Collection<JavaElementFoldManager> jefms = (Collection<JavaElementFoldManager>)mgrs;
                    public void run() {
                        for (JavaElementFoldManager jefm : jefms) {
                            jefm.new CommitFolds(doc, v.folds, version, stamp).run();
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
        private Document doc;
        private List<FoldInfo> infos;
        private long startTime;
        private AtomicLong version;
        private long stamp;
        
        public CommitFolds(Document doc, List<FoldInfo> infos, AtomicLong version, long stamp) {
            this.doc = doc;
            this.infos = infos;
            this.version = version;
            this.stamp = stamp;
        }
        
        public void run() {
            if (!insideRender) {
                startTime = System.currentTimeMillis();
                insideRender = true;
                
                // retain import & initial comment states
                operation.getHierarchy().getComponent().getDocument().render(this);
                
                return;
            }
            operation.getHierarchy().lock();
            try {
                if (version.get() != stamp || operation.getHierarchy().getComponent().getDocument() != doc) {
                    return;
                }
                Map<FoldInfo, Fold> folds = operation.update(infos, null, null);
                if (folds == null) {
                    // manager has been released.
                    return;
                }
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            } finally {
                operation.getHierarchy().unlock();
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 2",
                    new Object[] {file, endTime - startTime});
        }
    }

    private static final class JavaElementFoldVisitor extends CancellableTreePathScanner<Object, Object> {
        
        private List<FoldInfo> folds = new ArrayList<FoldInfo>();
        private CompilationInfo info;
        private CompilationUnitTree cu;
        private SourcePositions sp;
        private boolean stopped;
        private int initialCommentStopPos = Integer.MAX_VALUE;
        private Document doc;
        
        public JavaElementFoldVisitor(CompilationInfo info, CompilationUnitTree cu, SourcePositions sp, Document doc) {
            this.info = info;
            this.cu = cu;
            this.sp = sp;
            this.doc = doc;
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
                        folds.add(FoldInfo.range(startOffset, startOffset + token.length(), INITIAL_COMMENT_FOLD_TYPE));
                        break;
                    }
                }
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
                    folds.add(FoldInfo.range(startOffset, startOffset + token.length(), JAVADOC_FOLD_TYPE));
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
                    
                    if (start != (-1) && end != (-1)) {
                        folds.add(FoldInfo.range(start, end, CODE_BLOCK_FOLD_TYPE));
                    }
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
                    
                    if (start != (-1) && end != (-1)) {
                        folds.add(FoldInfo.range(start, end, INNERCLASS_TYPE));
		      }
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
                if (importsStart < initialCommentStopPos) {
                    initialCommentStopPos = importsStart;
                }
                importsStart += 7/*"import ".length()*/;

                if (importsStart < importsEnd) {
                    folds.add(FoldInfo.range(importsStart , importsEnd, IMPORTS_FOLD_TYPE));
                }
            }
            return super.visitCompilationUnit(node, p);
        }
    }
    
}
