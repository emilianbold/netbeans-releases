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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreeScanner;
import org.netbeans.api.timers.TimesCollector;
import org.netbeans.modules.java.editor.semantic.ScanningCancellableTask;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public class JavaElementFoldManager implements FoldManager {
    
    private FoldOperation operation;
    private FileObject    file;
    private JavaElementFoldTask task;
    private boolean released = true;
    
    /** Creates a new instance of JavaElementFoldManager */
    public JavaElementFoldManager() {
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    public synchronized void initFolds(FoldHierarchyTransaction transaction) {
        released = false;
        new Init().run();
    }
    
    private final class Init implements Runnable {
        public void run() {
            synchronized (JavaElementFoldManager.this) {
                if (released)
                    return ;
            }
            
            Document doc = operation.getHierarchy().getComponent().getDocument();
            DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
            
            if (od != null) {
                currentFolds = new HashMap<FoldInfo, Fold>();
                task = JavaElementFoldTask.getTask(od.getPrimaryFile());
                task.setJavaElementFoldManager(JavaElementFoldManager.this);
            }
        }
    }

    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void removeEmptyNotify(Fold emptyFold) {
        currentFolds.remove(operation.getExtraInfo(emptyFold));
    }

    public void removeDamagedNotify(Fold damagedFold) {
        currentFolds.remove(operation.getExtraInfo(damagedFold));
    }

    public void expandNotify(Fold expandedFold) {
    }

    public synchronized void release() {
        released = true;
        if (task != null)
            task.setJavaElementFoldManager(null);
        
        task         = null;
        file         = null;
        currentFolds = null;
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
            final JavaElementFoldVisitor v = manager.new JavaElementFoldVisitor(cu, info.getTrees().getSourcePositions());
            
            scan(v, cu, null);
            
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
                    List<FoldInfo>          removed = new ArrayList<FoldInfo>(currentFolds.keySet());
                    
                    for (FoldInfo i : infos) {
                        if (removed.remove(i)) {
                            continue ;
                        }
                        
                        int start = i.start.getOffset();
                        int end   = i.end.getOffset();
                        
                        if (end > start) {
                            Fold f    = operation.addToHierarchy(i.type, "{...}", false, start, end, 0, 0, i, tr);
                            
                            added.put(i, f);
                        }
                    }
                    
                    for (FoldInfo i : removed) {
                        operation.removeFromHierarchy(currentFolds.remove(i), tr);
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
    
    private static final FoldType TYPE_METHOD = new FoldType("java-element-method");
    private static final FoldType TYPE_CLASS = new FoldType("java-element-class");
    private static final FoldType TYPE_JAVADOC = new FoldType("java-element-javadoc");
    private static final FoldType TYPE_INITIAL = new FoldType("java-element-initial");
    
    private Map<FoldInfo, Fold> currentFolds;
    
    private final class JavaElementFoldVisitor extends CancellableTreeScanner {
        
        private List<FoldInfo> folds = new ArrayList();
        private CompilationUnitTree cu;
        private SourcePositions sp;
        private boolean stopped;
        
        public JavaElementFoldVisitor(CompilationUnitTree cu, SourcePositions sp) {
            this.cu = cu;
            this.sp = sp;
        }
        
        @Override
        public Object visitMethod(MethodTree node, Object p) {
            super.visitMethod(node, p);
            Tree body = node.getBody();
            
            try {
                Document doc = operation.getHierarchy().getComponent().getDocument();
                int start = (int)sp.getStartPosition(cu, body);
                int end   = (int)sp.getEndPosition(cu, body);
                
                if (start != (-1) && end != (-1))
                    folds.add(new FoldInfo(TYPE_METHOD, doc, start, end));
            } catch (BadLocationException e) {
                //the document probably changed, stop
                stopped = true;
            }
            return null;
        }

        @Override
        public Object visitClass(ClassTree node, Object p) {
            super.visitClass(node, Boolean.TRUE);
            if (p == Boolean.TRUE) {
                try {
                    Document doc   = operation.getHierarchy().getComponent().getDocument();
                    int      start = Utilities.findBodyStart(node, cu, sp, doc);
                    int      end   = (int)sp.getEndPosition(cu, node);
                    
                    if (start != (-1) && end != (-1))
                        folds.add(new FoldInfo(TYPE_CLASS, doc, start, end));
                } catch (BadLocationException e) {
                    //the document probably changed, stop
                    stopped = true;
                }
            }
            return null;
        }

    }
    
    protected static final class FoldInfo implements Comparable {
        
        private FoldType type;
        private Position start;
        private Position end;
        
        public FoldInfo(FoldType type, Document doc, int start, int end) throws BadLocationException {
            this.type = type;
            this.start = doc.createPosition(start);
            this.end   = doc.createPosition(end);
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
