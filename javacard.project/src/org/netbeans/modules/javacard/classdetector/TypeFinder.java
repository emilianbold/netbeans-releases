/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.classdetector;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.filesystems.FileObject;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scans a classpath and finds all subtypes of a given type.
 *
 * @author Tim Boudreau
 */
public class TypeFinder implements CancellableTask<CompilationController> {
    private volatile boolean cancelled;
    private final ClassPath classpath;
    private final String fqn;
    private Callback callback;

    public TypeFinder(String fqn, ClassPath path) {
        this.classpath = path;
        this.fqn = fqn;
    }

    public interface Callback {
        public void foundFileObject(FileObject fo, String className);
    }

    public void findTypes(Callback callback) {
        //Don't ever do this kind of IO-heavy work on the event thread
        assert !EventQueue.isDispatchThread();
        this.callback = callback;
        //recursively iterate all children
        for (FileObject fo : classpath.getRoots()) {
            analyze(fo);
        }
    }


    private void analyze(FileObject fo) {
        if (fo.isFolder()) {
            for (FileObject child : fo.getChildren()) {
                analyze(child);
            }
        } else if ("java".equals(fo.getExt())) { //NOI18N
            JavaSource src = JavaSource.forFileObject(fo);
            try {
                src.runUserActionTask(this, true);
            } catch (IOException ex) {
                Logger.getLogger(TypeFinder.class.getName()).log(Level.INFO,
                        "Problem scanning " + fo.getPath(), ex); //NOI18N
            }
        }
    }

    public void reset() {
        cancelled = false;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(CompilationController compiler) throws Exception {
        if (cancelled) {
            return;
        }
        compiler.toPhase(Phase.RESOLVED);
        if (cancelled) {
            return;
        }
        List <? extends Tree> types =
                compiler.getCompilationUnit().getTypeDecls();
        for (Tree tree: types) {
            if (cancelled) {
                return;
            }
            TypeMirror mirror = compiler.getTrees().getTypeMirror(
                    TreePath.getPath(compiler.getCompilationUnit(),
                    tree));
            if (tree instanceof ClassTree && match(mirror, compiler.getTypes())) {
                callback.foundFileObject(compiler.getFileObject(),
                        mirror.toString());
            }
        }
    }

    boolean match(Trees trees, Types types, CompilationUnitTree unit, Tree t) {
        TypeMirror mirror = trees.getTypeMirror(TreePath.getPath(unit, t));
        return match(mirror, types);
    }

    boolean match(TypeMirror mirror, Types types) {
        boolean result = false;
        if (mirror != null && 
            !"java.lang.Object".equals(mirror.toString())) { //NOI18N

            result = fqn.equals(mirror.toString());
            if (!result) {
                List<? extends TypeMirror> l = 
                        types.directSupertypes(mirror);

                for (TypeMirror tm : l) {
                    result = match(tm, types);
                    if (cancelled) return result;
                    if (result) break;
                }
            }
        }
        return result;
    }
}
