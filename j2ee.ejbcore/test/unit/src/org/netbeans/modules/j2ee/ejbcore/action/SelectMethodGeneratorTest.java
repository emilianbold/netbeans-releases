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

import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class SelectMethodGeneratorTest extends TestBase {
    
    public SelectMethodGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateCmpCardinalityOne() throws IOException {
        TestModule testModule = createEjb21Module();

        // add create method into local and remote interfaces of CMP Entity EJB 
        FileObject beanClass = testModule.getSources()[0].getFileObject("cmplr/CmpLRBean.java");
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        SelectMethodGenerator generator = SelectMethodGenerator.create("cmplr.CmpLRBean", beanClass);
        final MethodModel methodModel = MethodModel.create(
                "ejbSelectTest",
                "int",
                "",
                Collections.singletonList(MethodModel.Variable.create("java.lang.String", "name")),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        String ejbql = "SELECT COUNT(o) FROM CmpLR o";
        generator.generate(methodModel, true, true, true, ejbql);
        
        final boolean[] found = new boolean[] { false };
        JavaSource javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRBean");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("ejbSelectTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        assertSame(TypeKind.INT, executableElement.getReturnType().getKind());
                        TypeElement finderException = workingCopy.getElements().getTypeElement("javax.ejb.FinderException");
                        assertTrue(executableElement.getThrownTypes().contains(finderException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // entry in deployment descriptor
        Entity entity = (Entity) ejbJar.getEnterpriseBeans().findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, "cmplr.CmpLRBean");
        boolean queryFound = false;
        for (Query query : entity.getQuery()) {
            QueryMethod queryMethod = query.getQueryMethod();
            if ("ejbSelectTest".equals(queryMethod.getMethodName())) {
                assertEquals(ejbql, query.getEjbQl());
                queryFound = true;
            }
        }
        assertTrue(queryFound);
        
    }

}
