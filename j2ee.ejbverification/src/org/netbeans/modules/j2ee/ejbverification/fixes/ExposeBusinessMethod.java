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

package org.netbeans.modules.j2ee.ejbverification.fixes;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.logging.Level;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemFinder;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public class ExposeBusinessMethod implements Fix {

    private FileObject fileObject;
    private ElementHandle<TypeElement> targetClassHandle;
    private ElementHandle<ExecutableElement> methodHandle;
    private boolean local;

    public ExposeBusinessMethod(FileObject fileObject, ElementHandle<TypeElement> targetClassHandle, ElementHandle<ExecutableElement> methodHandle, boolean local) {
        this.fileObject = fileObject;
        this.targetClassHandle = targetClassHandle;
        this.methodHandle = methodHandle;
        this.local = local;
    }

    public ChangeInfo implement() {
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement targetClass = targetClassHandle.resolve(workingCopy);
                ExecutableElement originalMethod = methodHandle.resolve(workingCopy);

                ClassTree clazzTree = workingCopy.getTrees().getTree(targetClass);
                TreeMaker make = workingCopy.getTreeMaker();
                MethodTree newMethod = make.Method(originalMethod, null);
                GeneratorUtilities generator = GeneratorUtilities.get(workingCopy);
                ClassTree newClass = generator.insertClassMember(clazzTree, newMethod);

                workingCopy.rewrite(clazzTree, newClass);
            }
        };
        
        ClasspathInfo cpInfo = ClasspathInfo.create(fileObject);
        FileObject targetFileObject = SourceUtils.getFile(targetClassHandle, cpInfo);

        JavaSource javaSource = JavaSource.create(cpInfo, fileObject, targetFileObject);

        try {
            javaSource.runModificationTask(task).commit();
        } catch (IOException e) {
            EJBProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        
        return null;
    }

    public int hashCode() {
        return 1;
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public String getText() {
        String className = JavaUtils.getShortClassName(targetClassHandle.getQualifiedName());
        
        return NbBundle.getMessage(ExposeBusinessMethod.class,
                local ? "LBL_ExposeBusinessMethodLocal" : "LBL_ExposeBusinessMethodRemote",
                className);
    }
}
