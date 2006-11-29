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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.fold;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.timers.TimesCollector;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.ext.java.JavaFoldManager;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.modules.java.editor.semantic.ScanningCancellableTask;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class JavaElementFoldManager extends JavaFoldManager {
    
    private FoldOperation operation;
    private FileObject    file;
    private JavaElementFoldTask task;
    
    // Folding presets
    private boolean foldImportsPreset;
    private boolean foldInnerClassesPreset;
    private boolean foldJavadocsPreset;
    private boolean foldCodeBlocksPreset;
    private boolean foldInitialCommentsPreset;
    
    /** Creates a new instance of JavaElementFoldManager */
    public JavaElementFoldManager() {
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
        
        settingsChange(null);
    }

    public synchronized void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od != null) {
            currentFolds = new HashMap<FoldInfo, Fold>();
            task = JavaElementFoldTask.getTask(od.getPrimaryFile());
            task.setJavaElementFoldManager(JavaElementFoldManager.this);
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
            task.setJavaElementFoldManager(null);
        
        task         = null;
        file         = null;
        currentFolds = null;
        importsFold  = null;
        initialCommentFold = null;
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        // Get folding presets
        foldInitialCommentsPreset = getSetting(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT);
        foldImportsPreset = getSetting(JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT);
        foldCodeBlocksPreset = getSetting(JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD);
        foldInnerClassesPreset = getSetting(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS);
        foldJavadocsPreset = getSetting(JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC);
    }
    
    private boolean getSetting(String settingName){
        JTextComponent tc = operation.getHierarchy().getComponent();
        return SettingsUtil.getBoolean(org.netbeans.editor.Utilities.getKitClass(tc), settingName, false);
    }
    
    static final class JavaElementFoldTask extends ScanningCancellableTask<CompilationInfo> {
        
        //XXX: this will hold JavaElementFoldTask as long as the FileObject exists:
        private static Map<FileObject, JavaElementFoldTask> file2Task = new WeakHashMap();
        
        static JavaElementFoldTask getTask(FileObject file) {
            JavaElementFoldTask task = file2Task.get(file);
            
            if (task == null) {
                file2Task.put(file, task = new JavaElementFoldTask());
            }
            
            return task;
        }
        
        private Reference<JavaElementFoldManager> manager;
        
        synchronized void setJavaElementFoldManager(JavaElementFoldManager manager) {
            this.manager = new WeakReference(manager);
        }
        
        public void run(final CompilationInfo info) {
            resume();
            
            JavaElementFoldManager manager;
            
            //the synchronized section should be as limited as possible here
            //in particular, "scan" should not be called in the synchronized section
            //or a deadlock could appear: sy(this)+document read lock against
            //document write lock and this.cancel/sy(this)
            synchronized (this) {
                manager = this.manager != null ? this.manager.get() : null;
            }
            
            if (manager == null)
                return ;
            
            long startTime = System.currentTimeMillis();

            final CompilationUnitTree cu = info.getCompilationUnit();
            final JavaElementFoldVisitor v = manager.new JavaElementFoldVisitor(info, cu, info.getTrees().getSourcePositions());
            
            scan(v, cu, null);
            
            if (v.stopped || isCancelled())
                return ;
            
            //check for initial fold:
            v.checkInitialFold();
            
            if (v.stopped || isCancelled())
                return ;
            
            SwingUtilities.invokeLater(manager.new CommitFolds(v.folds));
            
            long endTime = System.currentTimeMillis();
            
            TimesCollector.getDefault().reportTime(info.getFileObject(), "java-folds-1", "Folds - 1", endTime - startTime);
        }
        
    }
    
    private class CommitFolds implements Runnable {
        
        private boolean insideRender;
        private List<FoldInfo> infos;
        private long startTime;
        
        public CommitFolds(List<FoldInfo> infos) {
            this.infos = infos;
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
                    
                    Map<FoldInfo, Fold> added   = new TreeMap();
                    List<FoldInfo>      removed = new ArrayList<FoldInfo>(currentFolds.keySet());
                    
                    for (FoldInfo i : infos) {
                        if (removed.remove(i)) {
                            continue ;
                        }
                        
                        int start = i.start.getOffset();
                        int end   = i.end.getOffset();
                        
                        if (end > start) {
                            Fold f    = operation.addToHierarchy(i.template.getType(),
                                                                 i.template.getDescription(),
                                                                 i.collapseByDefault,
                                                                 start,
                                                                 end,
                                                                 i.template.getStartGuardedLength(),
                                                                 i.template.getEndGuardedLength(),
                                                                 i,
                                                                 tr);
                            
                            added.put(i, f);
                            
                            if (i.template == IMPORTS_FOLD_TEMPLATE) {
                                importsFold = f;
                            }
                            if (i.template == INITIAL_COMMENT_FOLD_TEMPLATE) {
                                initialCommentFold = f;
                            }
                        }
                    }
                    
                    for (FoldInfo i : removed) {
                        Fold f = currentFolds.remove(i);
                        
                        operation.removeFromHierarchy(f, tr);
                        
                        if (importsFold == f ) {
                            importsFold = null;
                        }
                        
                        if (initialCommentFold == f) {
                            initialCommentFold = f;
                        }
                    }
                    
                    currentFolds.putAll(added);
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                } finally {
                    tr.commit();
                }
            } finally {
                operation.getHierarchy().unlock();
            }
            
            long endTime = System.currentTimeMillis();
            
            TimesCollector.getDefault().reportTime(file, "java-folds-2", "Folds - 2", endTime - startTime);
        }
    }
    
    private Map<FoldInfo, Fold> currentFolds;
    private Fold initialCommentFold;
    private Fold importsFold;
    
    private final class JavaElementFoldVisitor extends CancellableTreePathScanner {
        
        private List<FoldInfo> folds = new ArrayList();
        private CompilationInfo info;
        private CompilationUnitTree cu;
        private SourcePositions sp;
        private boolean stopped;
        
        public JavaElementFoldVisitor(CompilationInfo info, CompilationUnitTree cu, SourcePositions sp) {
            this.info = info;
            this.cu = cu;
            this.sp = sp;
        }
        
        public void checkInitialFold() {
            try {
                TokenHierarchy th = info.getTokenHiearchy();
                TokenSequence<JavaTokenId>  ts = th.tokenSequence(JavaTokenId.language());
                
                while (ts.moveNext()) {
                    Token<JavaTokenId> token = ts.token();
                    
                    if (token.id() == JavaTokenId.BLOCK_COMMENT) {
                        Document doc   = operation.getHierarchy().getComponent().getDocument();
                        int startOffset = ts.offset();
                        boolean collapsed = foldInitialCommentsPreset;
                        
                        if (initialCommentFold != null) {
                            collapsed = initialCommentFold.isCollapsed();
                        }
                        
                        folds.add(new FoldInfo(doc, startOffset, startOffset + token.length(), INITIAL_COMMENT_FOLD_TEMPLATE, collapsed));
                    }
                    
                    if (token.id() != JavaTokenId.WHITESPACE)
                        break;
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
            
            TokenHierarchy th = info.getTokenHiearchy();
            TokenSequence<JavaTokenId>  ts = th.tokenSequence(JavaTokenId.language());
            
            if (ts.move(start) == Integer.MAX_VALUE) {
                return;//nothing
            }
            
            while (ts.movePrevious()) {
                Token<JavaTokenId> token = ts.token();
                
                if (token.id() == JavaTokenId.JAVADOC_COMMENT) {
                    Document doc   = operation.getHierarchy().getComponent().getDocument();
                    int startOffset = ts.offset();
                    folds.add(new FoldInfo(doc, startOffset, startOffset + token.length(), JAVADOC_FOLD_TEMPLATE, foldJavadocsPreset));
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
                    Document doc = operation.getHierarchy().getComponent().getDocument();
                    int start = (int)sp.getStartPosition(cu, node);
                    int end   = (int)sp.getEndPosition(cu, node);
                    
                    if (start != (-1) && end != (-1))
                        folds.add(new FoldInfo(doc, start, end, CODE_BLOCK_FOLD_TEMPLATE, foldCodeBlocksPreset));
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
            handleTree(node, null, p != Boolean.TRUE);
            return null;
        }
        
        @Override
        public Object visitVariable(VariableTree node,Object p) {
            super.visitVariable(node, p);
            handleTree(node, null, true);
            return null;
        }
        
        @Override
        public Object visitBlock(BlockTree node, Object p) {
            super.visitBlock(node, p);
            //check static/dynamic initializer:
            TreePath path = getCurrentPath();
            
            if (path.getParentPath().getLeaf().getKind() == Kind.CLASS) {
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
                try {
                    Document doc   = operation.getHierarchy().getComponent().getDocument();
                    boolean collapsed = foldImportsPreset;
                    
                    if (importsFold != null) {
                        collapsed = importsFold.isCollapsed();
                    }
                    
                    folds.add(new FoldInfo(doc, importsStart + 7/*"import ".length()*/, importsEnd, IMPORTS_FOLD_TEMPLATE, collapsed));
                } catch (BadLocationException e) {
                    //the document probably changed, stop
                    stopped = true;
                }
            }
            return super.visitCompilationUnit(node, p);
        }

    }
    
    protected static final class FoldInfo implements Comparable {
        
        private Position start;
        private Position end;
        private FoldTemplate template;
        private boolean collapseByDefault;
        
        public FoldInfo(Document doc, int start, int end, FoldTemplate template, boolean collapseByDefault) throws BadLocationException {
            this.start = doc.createPosition(start);
            this.end   = doc.createPosition(end);
            this.template = template;
            this.collapseByDefault = collapseByDefault;
        }
        
        public int hashCode() {
            return 1;
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof FoldInfo))
                return false;
            
            return compareTo(o) == 0;
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
            
            return 0;
        }
        
    }
    
}
