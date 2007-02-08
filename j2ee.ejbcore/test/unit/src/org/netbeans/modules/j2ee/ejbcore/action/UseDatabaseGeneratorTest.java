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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.ejbcore.EjbInjectionTargetQueryImplementation;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.test.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.EjbJarProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.FileOwnerQueryImpl;
import org.netbeans.modules.j2ee.ejbcore.test.ProjectImpl;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public class UseDatabaseGeneratorTest extends TestBase {
    
    private EjbJarProviderImpl ejbJarProvider;
    private ClassPathProviderImpl classPathProvider;
    private FileOwnerQueryImpl fileOwnerQuery;
    private FileObject dataDir;

    public UseDatabaseGeneratorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws IOException {
        clearWorkDir();
        File file = new File(getWorkDir(), "cache");	//NOI18N
        file.mkdirs();
        IndexUtil.setCacheFolder(file);
        ejbJarProvider = new EjbJarProviderImpl();
        classPathProvider = new ClassPathProviderImpl();
        fileOwnerQuery = new FileOwnerQueryImpl();
        setLookups(
                ejbJarProvider, 
                classPathProvider,
                fileOwnerQuery,
                new EjbInjectionTargetQueryImplementation()
                );
        dataDir = FileUtil.toFileObject(getDataDir());
    }

    public void testGenerate() throws IOException {
        UseDatabaseGenerator generator = new UseDatabaseGenerator();
        
        // EJB 2.1 Stateless Session Bean
        FileObject beanClass = dataDir.getFileObject("EJBModule1/src/java/foo/NewSessionBean.java");
        FileObject ddFileObject = dataDir.getFileObject("EJBModule1/src/conf/ejb-jar.xml");
        FileObject[] sources = new FileObject[] {dataDir.getFileObject("EJBModule1/src/java")};
        ejbJarProvider.setEjbModule(EjbProjectConstants.J2EE_14_LEVEL, ddFileObject, sources);
        classPathProvider.setClassPath(sources);
        fileOwnerQuery.setProject(new ProjectImpl("2.1"));
        Node node = new AbstractNode(Children.LEAF, Lookups.singleton(beanClass));
        final ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(node);
        Datasource datasource = new DatasourceImpl();
        generator.generate(beanClass, elementHandle, datasource, false, null);
        
        JavaSource javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = elementHandle.resolve(workingCopy);
                MethodModel methodModel = MethodModel.create(
                        "getTestJndiName",
                        javax.sql.DataSource.class.getName(),
                        null,
                        Collections.<MethodModel.Variable>emptyList(),
                        Collections.singletonList(javax.naming.NamingException.class.getName()),
                        Collections.singleton(Modifier.PRIVATE)
                        );
                assertTrue(containsMethod(workingCopy, methodModel, typeElement));
            }
        });
        
        // EJB 3.0 Stateless Session Bean
        beanClass = dataDir.getFileObject("EJB30Module1/src/java/slessrl/StatelessRLBean.java");
        sources = new FileObject[] {dataDir.getFileObject("EJB30Module1/src/java")};
        ejbJarProvider.setEjbModule(EjbProjectConstants.JAVA_EE_5_LEVEL, null, sources);
        classPathProvider.setClassPath(sources);
        fileOwnerQuery.setProject(new ProjectImpl("3.0"));
        node = new AbstractNode(Children.LEAF, Lookups.singleton(beanClass));
        final ElementHandle<TypeElement> elementHandle2 = _RetoucheUtil.getJavaClassFromNode(node);
        datasource = new DatasourceImpl();
        generator.generate(beanClass, elementHandle2, datasource, false, null);
        
        javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = elementHandle2.resolve(workingCopy);
                //TODO: RETOUCHE not working because of missing annotation based model
//                checkDatasourceField(workingCopy, typeElement, "testJndiName", "testJndiName");
            }
        });
    }
    
    // private helpers =========================================================
    
    private static boolean containsMethod(WorkingCopy workingCopy, MethodModel methodModel, TypeElement typeElement) {
        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (MethodModelSupport.isSameMethod(workingCopy, executableElement, methodModel)) {
                return true;
            }
        }
        return false;
    }
    
    private static void checkDatasourceField(WorkingCopy workingCopy, TypeElement typeElement, String name, String jndiName) {
        List<VariableElement> elements = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
        VariableElement variableElement = (VariableElement) elements.get(0);
        assertTrue(variableElement.getSimpleName().contentEquals(name)); // field name
        DeclaredType declaredType = (DeclaredType) variableElement.asType();
        TypeElement returnTypeElement = (TypeElement) declaredType.asElement();
        assertTrue(returnTypeElement.getQualifiedName().contentEquals("javax.sql.DataSource")); // field type
        AnnotationMirror annotationMirror = variableElement.getAnnotationMirrors().get(0);
        DeclaredType annotationDeclaredType = annotationMirror.getAnnotationType();
        TypeElement annotationTypeElement = (TypeElement) annotationDeclaredType.asElement();
        assertTrue(annotationTypeElement.getQualifiedName().contentEquals("javax.annotation.Resource")); // annotation type
        Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry = annotationMirror.getElementValues().entrySet().iterator().next();
        String attributeName = entry.getKey().getSimpleName().toString();
        String attributeValue = (String) entry.getValue().getValue();
        assertEquals("name", attributeName); // attributes
        assertEquals(jndiName, attributeValue);
    }

    private static class DatasourceImpl implements Datasource {
        
        public DatasourceImpl() {}
        
        public String getJndiName() { return "testJndiName"; }

        public String getUrl() { return "testUrl"; }

        public String getUsername() { return "testUsername"; }

        public String getPassword() { return "testPassword"; }

        public String getDriverClassName() { return "testDriverClassName"; }

        public String getDisplayName() { return "testDisplayName"; }
        
    }
    
}
