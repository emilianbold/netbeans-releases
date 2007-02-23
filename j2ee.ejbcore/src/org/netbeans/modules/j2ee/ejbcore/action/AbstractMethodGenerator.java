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

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Adamek
 */
public abstract class AbstractMethodGenerator {
    
    protected final EntityAndSession ejb;
    protected final FileObject ejbClassFileObject;
    protected final FileObject ddFileObject;
    
    protected AbstractMethodGenerator(EntityAndSession ejb, FileObject ejbClassFileObject, FileObject ddFileObject) {
        Parameters.notNull("ejb", ejb);
        Parameters.notNull("ejbClassFileObject", ejbClassFileObject);
        this.ejb = ejb;
        this.ejbClassFileObject = ejbClassFileObject;
        this.ddFileObject = ddFileObject;
    }
    
    /**
     * Founds business interface if exists and adds method there, adds method into interface <code>className</code> otherwise
     */
    protected void addMethodToInterface(MethodModel methodModel, String className) throws IOException {
        String commonInterface = findCommonInterface(ejb.getEjbClass(), className);
        if (commonInterface == null) { // there is no 'business' interface
            commonInterface = className;
        }
        FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, commonInterface);
        addMethod(methodModel, fileObject, commonInterface);
    }
    
    /**
     * Adds method to class
     */
    protected static void addMethod(final MethodModel methodModel, FileObject fileObject, final String className) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }
    
    protected void saveXml() throws IOException {
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ddFileObject);
        if (ejbJar != null) {
            ejbJar.write(ddFileObject);
        }
    }
    
    private static String findCommonInterface(final String className1, final String className2) throws IOException {
        //TODO: RETOUCHE
        return null;
    }
    
}
