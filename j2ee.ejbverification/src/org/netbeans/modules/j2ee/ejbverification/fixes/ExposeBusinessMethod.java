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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

        JavaSource javaSource = JavaSource.forFileObject(targetFileObject);

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
