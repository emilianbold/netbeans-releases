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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.spring.beans.refactoring.plugins;



import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.spring.beans.refactoring.RetoucheUtils;
import org.openide.filesystems.FileObject;

/**
 * @author John Baker
 */
public abstract class SpringBeansJavaRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {
    private volatile CancellableTask currentTask;
    protected volatile boolean cancelRequest = false;

    public SpringBeansJavaRefactoringPlugin() {

    }

    public Problem preCheck() {
        return null;
    }

    public Problem prepare(final RefactoringElementsBag elements) {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }
    
    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, CancellableTask<WorkingCopy> task) {
        return processFiles(files, task, null);
    }

    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, CancellableTask<WorkingCopy> task, ClasspathInfo info) {
        currentTask = task;
        Collection<ModificationResult> results = new LinkedList<ModificationResult>();
        try {
            Iterable<? extends List<FileObject>> work = groupByRoot(files);
            for (List<FileObject> fos : work) {
                if (cancelRequest) {
                    return Collections.<ModificationResult>emptyList();
                }
                final JavaSource javaSource = JavaSource.create(info==null?ClasspathInfo.create(fos.get(0)):info, fos);
                try {
                    results.add(javaSource.runModificationTask(task));
                } catch (IOException ex) {
                    throw (RuntimeException) new RuntimeException().initCause(ex);
                }
            }
        } finally {
            currentTask = null;
        }
        return results;
    }
    
    private Iterable<? extends List<FileObject>> groupByRoot (Iterable<? extends FileObject> data) {
        Map<FileObject,List<FileObject>> result = new HashMap<FileObject,List<FileObject>> ();
        for (FileObject file : data) {
            if (cancelRequest) {
                return Collections.emptyList();
            }
            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
            if (cp != null) {
                FileObject root = cp.findOwnerRoot(file);
                if (root != null) {
                    List<FileObject> subr = result.get (root);
                    if (subr == null) {
                        subr = new LinkedList<FileObject>();
                        result.put (root,subr);
                    }
                    subr.add (file);
                }
            }
        }
        return result.values();
    }    
    
    public void cancelRequest() {
        cancelRequest = true;
        if (currentTask!=null) {
            currentTask.cancel();
        }
        RetoucheUtils.cancel = true;
    }
    
    protected ClasspathInfo getClasspathInfo(AbstractRefactoring refactoring) {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        if (cpInfo==null) {
            Collection<? extends TreePathHandle> handles = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
            if (!handles.isEmpty()) {
                cpInfo = RetoucheUtils.getClasspathInfoFor(handles.toArray(new TreePathHandle[handles.size()]));
            } else {
                cpInfo = RetoucheUtils.getClasspathInfoFor((FileObject)null);
            }
            refactoring.getContext().add(cpInfo);
        }
        return cpInfo;
    }

}
