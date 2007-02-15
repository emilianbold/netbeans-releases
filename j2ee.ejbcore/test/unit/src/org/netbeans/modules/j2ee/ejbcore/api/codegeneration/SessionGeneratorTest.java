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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class SessionGeneratorTest extends TestBase {
    
    public SessionGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerate() throws IOException {
        // Session EJB 2.1
        TestModule testModule = ejb14();
        String packageName = "ejb21";
        FileObject pkg = testModule.getSources()[0].getFileObject(packageName);
        if (pkg != null) {
            pkg.delete();
        }
        pkg = testModule.getSources()[0].createFolder(packageName);
        // stateless with remote and local interfaces
        checkEJB21("StatelessEJB21RL", pkg, testModule.getDeploymentDescriptor(), true, true, false);
        // stateful with remote interface
        checkEJB21("StatefulEJB21R", pkg, testModule.getDeploymentDescriptor(), true, false, true);
        
        // Session EJB 3.0
    }
    
    private void checkEJB21(String name, FileObject pkg, FileObject ddFileObject,
            boolean hasRemote, boolean hasLocal, boolean isStateful) throws IOException {
        SessionGenerator sessionGenerator = SessionGenerator.create(name, pkg, hasRemote, hasLocal, isStateful, false, false, true);
        sessionGenerator.generate();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ddFileObject);
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Session session = (Session) enterpriseBeans.findBeanByName(EnterpriseBeans.SESSION, Session.EJB_NAME, name + "Bean");
        final String JAVA = "java";
        assertEquals(session.getSessionType(), isStateful ? Session.SESSION_TYPE_STATEFUL : Session.SESSION_TYPE_STATELESS);
        
        FileObject ejbClass = pkg.getFileObject(name + "Bean", JAVA);
        assertNotNull(ejbClass);
        checkEjbClass21(ejbClass, name, session);
        
        FileObject remote = pkg.getFileObject(name + "Remote", JAVA);
        FileObject remoteHome = pkg.getFileObject(name + "RemoteHome", JAVA);
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
        
        FileObject local = pkg.getFileObject(name + "Local", JAVA);
        FileObject localHome = pkg.getFileObject(name + "LocalHome", JAVA);
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
        Util.runUserActionTask(ejbClass, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                final String VOID = "void";
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(name + "Bean"));
                assertTrue(clazz.getQualifiedName().contentEquals(session.getEjbClass()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.SessionBean"}));
                assertEquals(5, ElementFilter.methodsIn(clazz.getEnclosedElements()).size());
                assertEquals(1, ElementFilter.fieldsIn(clazz.getEnclosedElements()).size());
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "setSessionContext", VOID, "", 
                            Collections.singletonList(MethodModel.Variable.create("javax.ejb.SessionContext", "aContext")), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "ejbActivate", VOID, "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "ejbPassivate", VOID, "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "ejbRemove", VOID, "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "ejbCreate", VOID, "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.Variable.create("javax.ejb.SessionContext", "context")));
            }
        });
    }
    
    private void checkRemote21(final FileObject remote, final String name, final Session session) throws IOException {
        Util.runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(name + "Remote"));
                assertTrue(clazz.getQualifiedName().contentEquals(session.getRemote()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.EJBObject"}));
                assertTrue(clazz.getEnclosedElements().isEmpty());
            }
        });
    }
    
    private void checkRemoteHome21(final FileObject remote, final String name, final Session session) throws IOException {
        Util.runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(name + "RemoteHome"));
                assertTrue(clazz.getQualifiedName().contentEquals(session.getHome()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.EJBHome"}));
                assertEquals(1, ElementFilter.methodsIn(clazz.getEnclosedElements()).size());
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "create", session.getRemote(), "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Arrays.asList("java.rmi.RemoteException", "javax.ejb.CreateException"), 
                            Collections.<Modifier>emptySet()
                            )));
            }
        });
    }
    
    private void checkLocal21(final FileObject remote, final String name, final Session session) throws IOException {
        Util.runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(name + "Local"));
                assertTrue(clazz.getQualifiedName().contentEquals(session.getLocal()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.EJBLocalObject"}));
                assertTrue(clazz.getEnclosedElements().isEmpty());
            }
        });
    }
    
    private void checkLocalHome21(final FileObject remote, final String name, final Session session) throws IOException {
        Util.runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(name + "LocalHome"));
                assertTrue(clazz.getQualifiedName().contentEquals(session.getLocalHome()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.EJBLocalHome"}));
                assertEquals(1, ElementFilter.methodsIn(clazz.getEnclosedElements()).size());
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "create", session.getLocal(), "", 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Arrays.asList("javax.ejb.CreateException"), 
                            Collections.<Modifier>emptySet()
                            )));
            }
        });
    }

}
