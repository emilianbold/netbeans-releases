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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.netbeans.modules.j2ee.ejbcore.test.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.EjbJarProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.FakeJavaDataLoaderPool;
import org.netbeans.modules.j2ee.ejbcore.test.FileOwnerQueryImpl;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Martin Adamek
 */
public class SessionGeneratorTest extends TestBase {
    
    private EjbJarProviderImpl ejbJarProvider;
    private ClassPathProviderImpl classPathProvider;
    private FileObject dataDir;
    private EJBNameOptions ejbNames;

    public SessionGeneratorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File file = new File(getWorkDir(),"cache");	//NOI18N
        file.mkdirs();
        IndexUtil.setCacheFolder(file);
        ejbJarProvider = new EjbJarProviderImpl();
        classPathProvider = new ClassPathProviderImpl();
        setLookups(
                ejbJarProvider, 
                classPathProvider, 
                new FileOwnerQueryImpl(),
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
        
        // Session EJB 2.1
        FileObject pkg = sources[0].getFileObject("stateless21");
        if (pkg != null) {
            pkg.delete();
        }
        pkg = sources[0].createFolder("stateless21");
        final String name = "Stateless21";
        SessionGenerator sessionGenerator = SessionGenerator.create(name, pkg, true, true, false, false, false, true);
        sessionGenerator.generate();
        FileObject ejbClass = pkg.getFileObject(ejbNames.getSessionEjbClassPrefix() + name + ejbNames.getSessionEjbClassSuffix() + ".java");
        assertNotNull(ejbClass);
        checkEjbClass21(ejbClass, name);
        FileObject remote = pkg.getFileObject(ejbNames.getSessionRemotePrefix() + name + ejbNames.getSessionRemoteSuffix() + ".java");
        assertNotNull(remote);
        checkRemote21(remote, name);
        FileObject remoteHome = pkg.getFileObject(ejbNames.getSessionRemoteHomePrefix() + name + ejbNames.getSessionRemoteHomeSuffix() + ".java");
        assertNotNull(remoteHome);
        checkRemoteHome21(remoteHome, name);
        FileObject local = pkg.getFileObject(ejbNames.getSessionLocalPrefix() + name + ejbNames.getSessionLocalSuffix() + ".java");
        assertNotNull(local);
        checkLocal21(local, name);
        FileObject localHome = pkg.getFileObject(ejbNames.getSessionLocalHomePrefix() + name + ejbNames.getSessionLocalHomeSuffix() + ".java");
        assertNotNull(localHome);
        checkLocalHome21(localHome, name);
    }
    
    private void checkEjbClass21(final FileObject ejbClass, final String name) throws Exception {
        runUserActionTask(ejbClass, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionEjbClassPrefix() + name + ejbNames.getSessionEjbClassSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.SessionBean"});
                assertContains(controller, clazz, new MethodModel[] {
                    MethodModel.create(
                            "setSessionContext", "void", "", 
                            Collections.singletonList(MethodModel.Variable.create("javax.ejb.SessionContext", "aContext")), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)),
                    MethodModel.create(
                            "ejbActivate", "void", "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)),
                    MethodModel.create(
                            "ejbPassivate", "void", "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)),
                    MethodModel.create(
                            "ejbRemove", "void", "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)),
                });
                assertContains(controller, clazz, new MethodModel.Variable[] {
                    MethodModel.Variable.create("javax.ejb.SessionContext", "context")
                });
            }
        });
    }
    
    private void checkRemote21(final FileObject remote, final String name) throws Exception {
        runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionRemotePrefix() + name + ejbNames.getSessionRemoteSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.EJBObject"});
                assertTrue(clazz.getEnclosedElements().isEmpty());
            }
        });
    }
    
    private void checkRemoteHome21(final FileObject remote, final String name) throws Exception {
        runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionRemoteHomePrefix() + name + ejbNames.getSessionRemoteHomeSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.EJBHome"});
                assertTrue(clazz.getEnclosedElements().isEmpty());
            }
        });
    }
    
    private void checkLocal21(final FileObject remote, final String name) throws Exception {
        runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionLocalPrefix() + name + ejbNames.getSessionLocalSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.EJBLocalObject"});
                assertTrue(clazz.getEnclosedElements().isEmpty());
            }
        });
    }
    
    private void checkLocalHome21(final FileObject remote, final String name) throws Exception {
        runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionLocalHomePrefix() + name + ejbNames.getSessionLocalHomeSuffix()));
                assertDirectlyImplements(controller, clazz, new String[] {"javax.ejb.EJBLocalHome"});
                assertTrue(clazz.getEnclosedElements().isEmpty());
            }
        });
    }
    
    private static void assertDirectlyImplements(CompilationController controller, TypeElement typeElement, String[] interfaces) {
        List<? extends TypeMirror> foundInterfaces = typeElement.getInterfaces();
        assertTrue(foundInterfaces.size() == interfaces.length);
        for (TypeMirror typeMirror : foundInterfaces) {
            TypeElement element = (TypeElement) controller.getTypes().asElement(typeMirror);
            assertTrue(containsName(interfaces, element.getQualifiedName()));
        }
    }
    
    private static void assertContains(CompilationController controller, TypeElement typeElement, MethodModel[] methods) {
        
    }
    
    private static void assertContains(CompilationController controller, TypeElement typeElement, MethodModel.Variable[] fields) {
        
    }
    
    private static boolean containsName(String[] stringNames, Name name) {
        for (String stringName : stringNames) {
            if (name.contentEquals(stringName)) {
                return true;
            }
        }
        return false;
    }
    
    private static void runUserActionTask(FileObject javaFile, CancellableTask<CompilationController> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }
    
}
