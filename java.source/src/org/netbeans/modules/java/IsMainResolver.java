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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public class IsMainResolver implements CancellableTask<CompilationInfo> {

    private static final Logger LOG = Logger.getLogger(IsMainResolver.class.getName());
    
    private boolean removeFile;
    private AtomicBoolean cancel = new AtomicBoolean();

    public IsMainResolver(boolean removeFile) {
        this.removeFile = removeFile;
    }
    
    public void run(final CompilationInfo parameter) throws Exception {
        if (!JavaNode.SHOW_MAIN_CLASS_BADGE) return ;
        
        cancel.set(false);
        
        long start = System.currentTimeMillis();
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "analyzing: {0}", FileUtil.getFileDisplayName(parameter.getFileObject()));
        }
        
        final boolean[] hasMain = new boolean[1];
        final List<TreePath> candidates = new LinkedList<TreePath>();
        new CandidateScanner(cancel).scan(parameter.getCompilationUnit(), candidates);
        
        if (!candidates.isEmpty()) {
            parameter.getJavaSource().runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    if (cancel.get()) return ;
                    
                    parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                    
                    for (TreePath tp : candidates) {
                        if (cancel.get())
                            return;
                        
                        Element e = parameter.getTrees().getElement(tp);

                        if (e != null && e.getKind() == ElementKind.METHOD && SourceUtils.isMainMethod((ExecutableElement) e)) {
                            hasMain[0] = true;
                            break;
                        }
                    }
                }
            }, true);
        }
        
        if (cancel.get())
            return ;
        
        JavaNode.setExecutable(parameter.getFileObject(), hasMain[0]);
        if (removeFile)
            FactoryImpl.get().removeHead(parameter.getFileObject());//...
        
        long end = System.currentTimeMillis();
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "took: {0}", (end - start));
        }
    }
    
    private static final class CandidateScanner extends TreePathScanner<Void, List<TreePath>> {
        private AtomicBoolean cancel;

        public CandidateScanner(AtomicBoolean cancel) {
            this.cancel = cancel;
        }
        
        @Override
        public Void visitMethod(MethodTree node, List<TreePath> p) {
            if ("main".equals(node.getName().toString())) { // NOI18N
                p.add(getCurrentPath());
            }
            return null;
        }

        @Override
        public Void scan(Tree tree, List<TreePath> p) {
            if (cancel.get())
                return null;
            
            return super.scan(tree, p);
        }

        @Override
        public Void scan(Iterable<? extends Tree> nodes, List<TreePath> p) {
            if (cancel.get())
                return null;

            return super.scan(nodes, p);
        }

    }
    
    public void cancel() {
    }

    public static final class FactoryImpl extends JavaSourceTaskFactory {

        public FactoryImpl() {
            super(Phase.PARSED, Priority.MIN);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new IsMainResolver(true);
        }

        @Override
        protected synchronized Collection<FileObject> getFileObjects() {
            if (files.isEmpty()) {
                return Collections.emptyList();
            } else {
                return Collections.singleton(files.iterator().next());
            }
        }
        
        private Set<FileObject> files = new LinkedHashSet<FileObject>();
        
        public synchronized void addFile(FileObject file) {
            files.add(file);
            if (files.size() == 1) {
                fileObjectsChanged();
            }
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "enqueue: {0}", FileUtil.getFileDisplayName(file));
            }
        }
        
        public synchronized void removeHead(FileObject file) {            
            files.remove(file);
            fileObjectsChanged();
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "removed: {0}", FileUtil.getFileDisplayName(file));
            }
        }
        
        public static FactoryImpl get() {
            return Lookup.getDefault().lookup(FactoryImpl.class);
        }
    }
    
    public static final class EditorBasedFactoryImpl extends EditorAwareJavaSourceTaskFactory {
        
        public EditorBasedFactoryImpl() {
            super(Phase.ELEMENTS_RESOLVED, Priority.MIN);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new IsMainResolver(false);
        }
        
    }

}
