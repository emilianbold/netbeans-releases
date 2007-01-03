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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.netbeans.modules.j2ee.ejbcore.test.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.EjbJarProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.FakeJavaDataLoaderPool;
import org.netbeans.modules.j2ee.ejbcore.test.FileOwnerQueryImpl;
import org.netbeans.modules.j2ee.ejbcore.test.ProjectImpl;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class SessionGeneratorTest extends TestBase {
    
    private EjbJarProviderImpl ejbJarProvider;
    private ClassPathProviderImpl classPathProvider;
    private ProjectImpl projectImpl;
    private FileObject dataDir;
    private EJBNameOptions ejbNames;

    public SessionGeneratorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws IOException {
        clearWorkDir();
        File file = new File(getWorkDir(),"cache");	//NOI18N
        file.mkdirs();
        IndexUtil.setCacheFolder(file);
        ejbJarProvider = new EjbJarProviderImpl();
        classPathProvider = new ClassPathProviderImpl();
        projectImpl = new ProjectImpl();
        setLookups(
                ejbJarProvider, 
                classPathProvider, 
                new FileOwnerQueryImpl(projectImpl),
                new FakeJavaDataLoaderPool()
                );
        dataDir = FileUtil.toFileObject(getDataDir());
        ejbNames = new EJBNameOptions();
    }

    public void testGenerate() throws Exception {
        FileObject ddFileObject = dataDir.getFileObject("EJBModule1/src/conf/ejb-jar.xml");
        FileObject[] sources = new FileObject[] {dataDir.getFileObject("EJBModule1/src/java")};
        ejbJarProvider.setEjbModule(EjbProjectConstants.J2EE_14_LEVEL, ddFileObject, sources);
        classPathProvider.setClassPath(sources);
        projectImpl.setProjectDirectory(dataDir.getFileObject("EJBModule1"));
        
        // Session EJB 2.1
        FileObject pkg = sources[0].getFileObject("ejb21");
        if (pkg != null) {
            pkg.delete();
        }
        pkg = sources[0].createFolder("ejb21");
        // stateless with remote and local interfaces
        checkEJB21("StatelessEJB21RL", pkg, ddFileObject, true, true, false);
        // stateful with remote interface
//        checkEJB21("StatefulEJB21R", pkg, ddFileObject, true, false, true);
        
        // Session EJB 3.0
    }
    
    private void checkEJB21(String name, FileObject pkg, FileObject ddFileObject,
            boolean hasRemote, boolean hasLocal, boolean isStateful) throws IOException {
        SessionGenerator sessionGenerator = SessionGenerator.create(name, pkg, hasRemote, hasLocal, isStateful, false, false, true);
        sessionGenerator.generate();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ddFileObject);
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        String ejbName = ejbNames.getSessionEjbNamePrefix() + name + ejbNames.getSessionEjbNameSuffix();
        Session session = (Session) enterpriseBeans.findBeanByName(EnterpriseBeans.SESSION, Session.EJB_NAME, ejbName);
        final String JAVA = "java";
        assertEquals(session.getSessionType(), isStateful ? Session.SESSION_TYPE_STATEFUL : Session.SESSION_TYPE_STATELESS);
        
        FileObject ejbClass = pkg.getFileObject(ejbNames.getSessionEjbClassPrefix() + name + ejbNames.getSessionEjbClassSuffix(), JAVA);
        assertNotNull(ejbClass);
        checkEjbClass21(ejbClass, name, session);
        
        FileObject remote = pkg.getFileObject(ejbNames.getSessionRemotePrefix() + name + ejbNames.getSessionRemoteSuffix(), JAVA);
        FileObject remoteHome = pkg.getFileObject(ejbNames.getSessionRemoteHomePrefix() + name + ejbNames.getSessionRemoteHomeSuffix(), JAVA);
        if (hasRemote) {
            assertNotNull(remote);
            checkRemote21(remote, name, session);
            assertNotNull(remoteHome);
            checkRemoteHome21(remoteHome, name, session);
        } else {
            assertNull(remote);
            assertNull(session.getRemote());
            assertNull(remoteHome);
            assertNull(session.getHome());
        }
        
        FileObject local = pkg.getFileObject(ejbNames.getSessionLocalPrefix() + name + ejbNames.getSessionLocalSuffix(), JAVA);
        FileObject localHome = pkg.getFileObject(ejbNames.getSessionLocalHomePrefix() + name + ejbNames.getSessionLocalHomeSuffix(), JAVA);
        if (hasLocal) {
            assertNotNull(local);
            checkLocal21(local, name, session);
            assertNotNull(localHome);
            checkLocalHome21(localHome, name, session);
        } else {
            assertNull(local);
            assertNull(session.getLocal());
            assertNull(localHome);
            assertNull(session.getLocalHome());
        }
        
    }
    
    private void checkEjbClass21(final FileObject ejbClass, final String name, final Session session) throws IOException {
        runUserActionTask(ejbClass, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                final String VOID = "void";
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionEjbClassPrefix() + name + ejbNames.getSessionEjbClassSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.SessionBean"});
                assertTrue(contains(controller, clazz, MethodModel.create(
                            "setSessionContext", VOID, "", 
                            Collections.singletonList(MethodModel.Variable.create("javax.ejb.SessionContext", "aContext")), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(contains(controller, clazz, MethodModel.create(
                            "ejbActivate", VOID, "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(contains(controller, clazz, MethodModel.create(
                            "ejbPassivate", VOID, "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(contains(controller, clazz, MethodModel.create(
                            "ejbRemove", VOID, "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(contains(controller, clazz, MethodModel.Variable.create("javax.ejb.SessionContext", "context")));
                assertTrue(clazz.getQualifiedName().contentEquals(session.getEjbClass()));
            }
        });
    }
    
    private void checkRemote21(final FileObject remote, final String name, final Session session) throws IOException {
        runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionRemotePrefix() + name + ejbNames.getSessionRemoteSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.EJBObject"});
                assertTrue(clazz.getEnclosedElements().isEmpty());
                assertTrue(clazz.getQualifiedName().contentEquals(session.getRemote()));
            }
        });
    }
    
    private void checkRemoteHome21(final FileObject remote, final String name, final Session session) throws IOException {
        runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionRemoteHomePrefix() + name + ejbNames.getSessionRemoteHomeSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.EJBHome"});
                assertTrue(clazz.getEnclosedElements().isEmpty());
                assertTrue(clazz.getQualifiedName().contentEquals(session.getHome()));
            }
        });
    }
    
    private void checkLocal21(final FileObject remote, final String name, final Session session) throws IOException {
        runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionLocalPrefix() + name + ejbNames.getSessionLocalSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.EJBLocalObject"});
                assertTrue(clazz.getEnclosedElements().isEmpty());
                assertTrue(clazz.getQualifiedName().contentEquals(session.getLocal()));
            }
        });
    }
    
    private void checkLocalHome21(final FileObject remote, final String name, final Session session) throws IOException {
        runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionLocalHomePrefix() + name + ejbNames.getSessionLocalHomeSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.EJBLocalHome"});
                assertTrue(clazz.getEnclosedElements().isEmpty());
                assertTrue(clazz.getQualifiedName().contentEquals(session.getLocalHome()));
            }
        });
    }
    
    private static void assertDirectlyImplements(CompilationController controller, TypeElement typeElement, String[] interfaces) {
        List<? extends TypeMirror> foundInterfaces = typeElement.getInterfaces();
        assertTrue("Superclass or interface not found? Probably not on classpath?", foundInterfaces.size() == interfaces.length);
        for (TypeMirror typeMirror : foundInterfaces) {
            TypeElement element = (TypeElement) controller.getTypes().asElement(typeMirror);
            assertTrue(containsName(interfaces, element.getQualifiedName()));
        }
    }
    
    private static boolean contains(CompilationController controller, TypeElement typeElement, MethodModel methodModel) {
        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (MethodModelSupport.isSameMethod(controller, executableElement, methodModel)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean contains(CompilationController controller, TypeElement typeElement, MethodModel.Variable field) {
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (getTypeName(controller, variableElement.asType()).equals(field.getType()) &&
                    variableElement.getSimpleName().contentEquals(field.getName())) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean containsName(String[] stringNames, Name name) {
        for (String stringName : stringNames) {
            if (name.contentEquals(stringName)) {
                return true;
            }
        }
        return false;
    }
    
    private static void runUserActionTask(FileObject javaFile, CancellableTask<CompilationController> taskToTest) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }
    
    // see #90968
    private static String getTypeName(CompilationController controller, TypeMirror typeMirror) {
        TypeKind typeKind = typeMirror.getKind();
        switch (typeKind) {
            case BOOLEAN : return "boolean"; // NOI18N
            case BYTE : return "byte"; // NOI18N
            case CHAR : return "char"; // NOI18N
            case DOUBLE : return "double"; // NOI18N
            case FLOAT : return "float"; // NOI18N
            case INT : return "int"; // NOI18N
            case LONG : return "long"; // NOI18N
            case SHORT : return "short"; // NOI18N
            case VOID : return "void"; // NOI18N
            case DECLARED : 
                Element element = controller.getTypes().asElement(typeMirror);
                return ((TypeElement) element).getQualifiedName().toString();
            case ARRAY : 
                ArrayType arrayType = (ArrayType) typeMirror;
                Element componentTypeElement = controller.getTypes().asElement(arrayType.getComponentType());
                return ((TypeElement) componentTypeElement).getQualifiedName().toString() + "[]";
            case ERROR :
            case EXECUTABLE :
            case NONE :
            case NULL :
            case OTHER :
            case PACKAGE :
            case TYPEVAR :
            case WILDCARD :
                break;
        }
        return null;
    }

}
