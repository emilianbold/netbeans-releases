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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
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
public class EntityGeneratorTest extends TestBase {
    
    private EjbJarProviderImpl ejbJarProvider;
    private ClassPathProviderImpl classPathProvider;
    private ProjectImpl projectImpl;
    private FileObject dataDir;
    private EJBNameOptions ejbNames;

    public EntityGeneratorTest(String testName) {
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
        FileObject pkg = sources[0].getFileObject("entityEjb21");
        if (pkg != null) {
            pkg.delete();
        }
        pkg = sources[0].createFolder("entityEjb21");
        assertNotNull(pkg);
        // CMP with remote and local interfaces
        checkEJB21("CmpEJB21RL", pkg, ddFileObject, true, true, true, "java.lang.Long");
        // BMP with remote and local interface
        checkEJB21("BmpEjb21RL", pkg, ddFileObject, true, true, false, "java.lang.Long");
    }
    
    private void checkEJB21(String name, FileObject pkg, FileObject ddFileObject,
            boolean hasRemote, boolean hasLocal, boolean isCMP, String primaryKeyClassName) throws IOException {
        EntityGenerator entityGenerator = EntityGenerator.create(name, pkg, hasRemote, hasLocal, isCMP, primaryKeyClassName);
        entityGenerator.generate();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ddFileObject);
        checkXml(ejbJar, EjbGenerationUtil.getSelectedPackageName(pkg) + ".", name, hasRemote, hasLocal, isCMP, primaryKeyClassName);
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        String ejbName = ejbNames.getEntityEjbNamePrefix() + name + ejbNames.getEntityEjbNameSuffix();
        Entity entity = (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_NAME, ejbName);
        final String JAVA = "java";

        FileObject ejbClass = pkg.getFileObject(ejbNames.getEntityEjbClassPrefix() + name + ejbNames.getEntityEjbClassSuffix(), JAVA);
        assertNotNull(ejbClass);
        if (isCMP) {
            checkCmpEjbClass(ejbClass, name, entity);
        } else {
            checkBmpEjbClass(ejbClass, name, entity);
        }
        
        FileObject remote = pkg.getFileObject(ejbNames.getEntityRemotePrefix() + name + ejbNames.getEntityRemoteSuffix(), JAVA);
        FileObject remoteHome = pkg.getFileObject(ejbNames.getEntityRemoteHomePrefix() + name + ejbNames.getEntityRemoteHomeSuffix(), JAVA);
        if (hasRemote) {
            assertNotNull(remote);
            if (isCMP) {
                checkCmpRemote(remote, name, entity);
            } else {
                checkBmpRemote(remote, name, entity);
            }
            assertNotNull(remoteHome);
            if (isCMP) {
                checkCmpRemoteHome(remoteHome, name, entity);
            } else {
                checkBmpRemoteHome(remoteHome, name, entity);
            }
        } else {
            assertNull(remote);
            assertNull(entity.getRemote());
            assertNull(remoteHome);
            assertNull(entity.getHome());
        }
        
        FileObject local = pkg.getFileObject(ejbNames.getSessionLocalPrefix() + name + ejbNames.getSessionLocalSuffix(), JAVA);
        FileObject localHome = pkg.getFileObject(ejbNames.getSessionLocalHomePrefix() + name + ejbNames.getSessionLocalHomeSuffix(), JAVA);
        if (hasLocal) {
            assertNotNull(local);
            if (isCMP) {
                checkCmpLocal(local, name, entity);
            } else {
                checkBmpLocal(local, name, entity);
            }
            assertNotNull(localHome);
            if (isCMP) {
                checkCmpLocalHome(localHome, name, entity);
            } else {
                checkBmpLocalHome(localHome, name, entity);
            }
        } else {
            assertNull(local);
            assertNull(entity.getLocal());
            assertNull(localHome);
            assertNull(entity.getLocalHome());
        }
        
    }
    
    private void checkCmpEjbClass(final FileObject ejbClass, final String name, final Entity entity) throws IOException {
        Util.runUserActionTask(ejbClass, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                final String VOID = "void";
                final String LONG = "java.lang.Long";
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionEjbClassPrefix() + name + ejbNames.getSessionEjbClassSuffix()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.EntityBean"}));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "getKey", LONG, null, 
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            new HashSet<Modifier>(Arrays.asList(new Modifier[] { Modifier.PUBLIC, Modifier.ABSTRACT }))
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "setKey", VOID, null, 
                            Collections.singletonList(MethodModel.Variable.create(LONG, "key")), 
                            Collections.<String>emptyList(), 
                            new HashSet<Modifier>(Arrays.asList(new Modifier[] { Modifier.PUBLIC, Modifier.ABSTRACT }))
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "ejbCreate", LONG, 
                            "{" +
                            "if (key == null) {\n" +
                            "    throw new CreateException(\"The field \\\"key\\\" must not be null\");\n" +
                            "}\n\n" +
                            "// TODO add additional validation code, throw CreateException if data is not valid\n" +
                            "setKey(key);\n\n" +
                            "return null;" + 
                            "}",
                            Collections.singletonList(MethodModel.Variable.create(LONG, "key")), 
                            Collections.singletonList("javax.ejb.CreateException"),
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "ejbPostCreate", VOID,
                            "{" +
                            "// TODO populate relationships here if appropriate" +
                            "}",
                            Collections.singletonList(MethodModel.Variable.create(LONG, "key")),
                            Collections.<String>emptyList(), 
                            Collections.singleton(Modifier.PUBLIC)
                            )));
                assertTrue(clazz.getQualifiedName().contentEquals(entity.getEjbClass()));
            }
        });
    }
    
    private void checkBmpEjbClass(final FileObject ejbClass, final String name, final Entity entity) throws IOException {
        
    }
    
    private void checkCmpRemote(final FileObject remote, final String name, final Entity entity) throws IOException {
        Util.runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionRemotePrefix() + name + ejbNames.getSessionRemoteSuffix()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.EJBObject"}));
                assertEquals(1, clazz.getEnclosedElements().size());
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "getKey", "java.lang.Long", null,
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.<Modifier>emptySet()
                            )));
                assertTrue(clazz.getQualifiedName().contentEquals(entity.getRemote()));
            }
        });
    }
    
    private void checkBmpRemote(final FileObject remote, final String name, final Entity entity) throws IOException {
        
    }

    private void checkCmpRemoteHome(final FileObject remote, final String name, final Entity entity) throws IOException {
        Util.runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionRemoteHomePrefix() + name + ejbNames.getSessionRemoteHomeSuffix()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.EJBHome"}));
                assertEquals(2, clazz.getEnclosedElements().size());
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "findByPrimaryKey", entity.getRemote(), null,
                            Collections.singletonList(MethodModel.Variable.create("java.lang.Long", "key")),
                            Arrays.asList(new String[] { "javax.ejb.FinderException", "java.rmi.RemoteException" }),
                            Collections.<Modifier>emptySet()
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "create", entity.getRemote(), null,
                            Collections.singletonList(MethodModel.Variable.create("java.lang.Long", "key")),
                            Arrays.asList(new String[] { "javax.ejb.CreateException", "java.rmi.RemoteException" }),
                            Collections.<Modifier>emptySet()
                            )));
                assertTrue(clazz.getQualifiedName().contentEquals(entity.getHome()));
            }
        });
    }
    
    private void checkBmpRemoteHome(final FileObject remote, final String name, final Entity entity) throws IOException {
        
    }

    private void checkCmpLocal(final FileObject remote, final String name, final Entity entity) throws IOException {
        Util.runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionLocalPrefix() + name + ejbNames.getSessionLocalSuffix()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.EJBLocalObject"}));
                assertEquals(1, clazz.getEnclosedElements().size());
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "getKey", "java.lang.Long", null,
                            Collections.<MethodModel.Variable>emptyList(), 
                            Collections.<String>emptyList(), 
                            Collections.<Modifier>emptySet()
                            )));
                assertTrue(clazz.getQualifiedName().contentEquals(entity.getLocal()));
            }
        });
    }
    
    private void checkBmpLocal(final FileObject remote, final String name, final Entity entity) throws IOException {
        
    }
    
    private void checkCmpLocalHome(final FileObject remote, final String name, final Entity entity) throws IOException {
        Util.runUserActionTask(remote, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                TypeElement clazz = sourceUtils.getTypeElement();
                assertTrue(clazz.getSimpleName().contentEquals(ejbNames.getSessionLocalHomePrefix() + name + ejbNames.getSessionLocalHomeSuffix()));
                assertTrue(Util.directlyImplements(controller, clazz, new String[] {"javax.ejb.EJBLocalHome"}));
                assertEquals(2, clazz.getEnclosedElements().size());
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "findByPrimaryKey", entity.getLocal(), null,
                            Collections.singletonList(MethodModel.Variable.create("java.lang.Long", "key")),
                            Collections.singletonList("javax.ejb.FinderException"),
                            Collections.<Modifier>emptySet()
                            )));
                assertTrue(Util.contains(controller, clazz, MethodModel.create(
                            "create", entity.getLocal(), null,
                            Collections.singletonList(MethodModel.Variable.create("java.lang.Long", "key")),
                            Collections.singletonList("javax.ejb.CreateException"),
                            Collections.<Modifier>emptySet()
                            )));
                assertTrue(clazz.getQualifiedName().contentEquals(entity.getLocalHome()));
            }
        });
    }

    private void checkBmpLocalHome(final FileObject remote, final String name, final Entity entity) throws IOException {
        
    }
    
    private void checkXml(EjbJar ejbJar, String pkgWithDot, String name, boolean hasRemote, boolean hasLocal, boolean isCMP, String primaryKeyClassName) {
        String ejbName = ejbNames.getEntityEjbNamePrefix() + name + ejbNames.getEntityEjbNameSuffix();
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Entity entity = (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_NAME, ejbName);
        assertNotNull(entity);
        assertEquals(entity.getEjbName(), ejbName);
        assertEquals(entity.getPrimKeyClass(), primaryKeyClassName);
        assertEquals(entity.getEjbClass(), pkgWithDot + ejbNames.getEntityEjbClassPrefix() + name + ejbNames.getEntityEjbClassSuffix());
        if (hasLocal) {
            assertEquals(entity.getLocal(), pkgWithDot + ejbNames.getEntityLocalPrefix() + name + ejbNames.getEntityLocalSuffix());
            assertEquals(entity.getLocalHome(), pkgWithDot + ejbNames.getEntityLocalHomePrefix() + name + ejbNames.getEntityLocalHomeSuffix());
        }
        if (hasRemote) {
            assertEquals(entity.getRemote(), pkgWithDot + ejbNames.getEntityRemotePrefix() + name + ejbNames.getEntityRemoteSuffix());
            assertEquals(entity.getHome(), pkgWithDot + ejbNames.getEntityRemoteHomePrefix() + name + ejbNames.getEntityRemoteHomeSuffix());
        }
    }
    
}
