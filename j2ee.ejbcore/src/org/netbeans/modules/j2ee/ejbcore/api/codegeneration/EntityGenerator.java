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

import java.util.Arrays;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EntityGenerator {

    private static final String BMP_EJBCLASS = "Templates/J2EE/EJB21/BmpEjbClass.java"; // NOI18N
    private static final String BMP_LOCAL = "Templates/J2EE/EJB21/BmpLocal.java"; // NOI18N
    private static final String BMP_LOCALHOME = "Templates/J2EE/EJB21/BmpLocalHome.java"; // NOI18N
    private static final String BMP_REMOTE = "Templates/J2EE/EJB21/BmpRemote.java"; // NOI18N
    private static final String BMP_REMOTEHOME = "Templates/J2EE/EJB21/BmpRemoteHome.java"; // NOI18N

    private static final String CMP_EJBCLASS = "Templates/J2EE/EJB21/CmpEjbClass.java"; // NOI18N
    private static final String CMP_LOCAL = "Templates/J2EE/EJB21/CmpLocal.java"; // NOI18N
    private static final String CMP_LOCALHOME = "Templates/J2EE/EJB21/CmpLocalHome.java"; // NOI18N
    private static final String CMP_REMOTE = "Templates/J2EE/EJB21/CmpRemote.java"; // NOI18N
    private static final String CMP_REMOTEHOME = "Templates/J2EE/EJB21/CmpRemoteHome.java"; // NOI18N

    // informations collected in wizard
    private final FileObject pkg;
    private final boolean hasRemote;
    private final boolean hasLocal;
    private final boolean isCMP;
    private final String primaryKeyClassName;
    
    // EJB naming options
    private final EJBNameOptions ejbNameOptions;
    private final String ejbName;
    private final String ejbClassName;
    private final String remoteName;
    private final String remoteHomeName;
    private final String localName;
    private final String localHomeName;
    private final String displayName;
    
    private final String packageNameWithDot;

    public static EntityGenerator create(String wizardTargetName, FileObject pkg, boolean hasRemote, boolean hasLocal, 
            boolean isCMP, String primaryKeyClassName) {
        return new EntityGenerator(wizardTargetName, pkg, hasRemote, hasLocal, isCMP, primaryKeyClassName);
    }
    
    private EntityGenerator(String wizardTargetName, FileObject pkg, boolean hasRemote, boolean hasLocal, 
            boolean isCMP, String primaryKeyClassName) {
        this.pkg = pkg;
        this.hasRemote = hasRemote;
        this.hasLocal = hasLocal;
        this.isCMP = isCMP;
        this.primaryKeyClassName = primaryKeyClassName;
        this.ejbNameOptions = new EJBNameOptions();
        this.ejbName = ejbNameOptions.getEntityEjbNamePrefix() + wizardTargetName + ejbNameOptions.getEntityEjbNameSuffix();
        this.ejbClassName = ejbNameOptions.getEntityEjbClassPrefix() + wizardTargetName + ejbNameOptions.getEntityEjbClassSuffix();
        this.remoteName = ejbNameOptions.getEntityRemotePrefix() + wizardTargetName + ejbNameOptions.getEntityRemoteSuffix();
        this.remoteHomeName = ejbNameOptions.getEntityRemoteHomePrefix() + wizardTargetName + ejbNameOptions.getEntityRemoteHomeSuffix();
        this.localName = ejbNameOptions.getEntityLocalPrefix() + wizardTargetName + ejbNameOptions.getEntityLocalSuffix();
        this.localHomeName = ejbNameOptions.getEntityLocalHomePrefix() + wizardTargetName + ejbNameOptions.getEntityLocalHomeSuffix();
        this.displayName = ejbNameOptions.getEntityDisplayNamePrefix() + wizardTargetName + ejbNameOptions.getEntityDisplayNameSuffix();
        this.packageNameWithDot = EjbGenerationUtil.getSelectedPackageName(pkg) + ".";
    }

    public FileObject generate() throws IOException {
        FileObject resultFileObject = null;
        if (isCMP) {
            resultFileObject = generateCmpClasses();
        } else {
            resultFileObject = generateBmpClasses();
        }

        //put these lines in a common function at the appropriate place after EA1
        //something like public EjbJar getEjbJar()
        //This method will be used whereever we construct/get DD object graph to ensure
        //corresponding config listners attached to it.
        Project project = FileOwnerQuery.getOwner(pkg);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();

        generateXml();
        
        return resultFileObject;
    }
    
    private FileObject generateBmpClasses() throws IOException {
        FileObject ejbClassFO = GenerationUtils.createClass(BMP_EJBCLASS,  pkg, ejbClassName, null);
        createAndAddMethod(ejbClassFO, bmpFindByPrimaryKeyImpl());
        if (hasRemote) {
            GenerationUtils.createClass(BMP_REMOTE,  pkg, remoteName, null);
            FileObject remoteHomeFO = GenerationUtils.createClass(BMP_REMOTEHOME, pkg, remoteHomeName, null);
            createAndAddMethod(remoteHomeFO, findByPrimaryKeyRemoteHome());
        }
        if (hasLocal) {
            GenerationUtils.createClass(BMP_LOCAL, pkg, localName, null);
            FileObject localHomeFO = GenerationUtils.createClass(BMP_LOCALHOME, pkg, localHomeName, null);
            createAndAddMethod(localHomeFO, findByPrimaryKeyLocalHome());
        }
        return ejbClassFO;
    }

    private FileObject generateCmpClasses() throws IOException {
        FileObject ejbClassFO = GenerationUtils.createClass(CMP_EJBCLASS,  pkg, ejbClassName, null);
        MethodModel[] methods = new MethodModel[] {
            cmpGetKeyImpl(),
            cmpSetKeyImpl(),
            cmpEjbCreateImpl(),
            cmpEjbPostCreateImpl()
        };
        createAndAddMethods(ejbClassFO, Arrays.asList(methods));
        if (hasRemote) {
            FileObject remoteFO = GenerationUtils.createClass(CMP_REMOTE,  pkg, remoteName, null);
            FileObject remoteHomeFO = GenerationUtils.createClass(CMP_REMOTEHOME, pkg, remoteHomeName, null);
            createAndAddMethod(remoteFO, cmpGetKeyRemote());
            methods = new MethodModel[] {
                findByPrimaryKeyRemoteHome(),
                cmpCreateRemoteHome()
            };
            createAndAddMethods(remoteHomeFO, Arrays.asList(methods));
        }
        if (hasLocal) {
            FileObject localFO = GenerationUtils.createClass(CMP_LOCAL, pkg, localName, null);
            FileObject localHomeFO = GenerationUtils.createClass(CMP_LOCALHOME, pkg, localHomeName, null);
            createAndAddMethod(localFO, cmpGetKeyLocal());
            methods = new MethodModel[] {
                findByPrimaryKeyLocalHome(),
                cmpCreateLocalHome()
            };
            createAndAddMethods(localHomeFO, Arrays.asList(methods));
        }
        return ejbClassFO;
    }

    private void generateXml() throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(pkg);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = DDProvider.getDefault().getMergedDDRoot(ejbModule.getMetadataUnit());
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        if (enterpriseBeans == null) {
            enterpriseBeans = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(enterpriseBeans);
        }
        Entity entity = enterpriseBeans.newEntity();
        entity.setEjbName(ejbName);
        entity.setEjbClass(packageNameWithDot + ejbClassName);
        entity.setPrimKeyClass(primaryKeyClassName);
        entity.setReentrant(false);
        entity.setDisplayName(displayName);
        if (hasRemote) {
            entity.setRemote(packageNameWithDot + remoteName);
            entity.setHome(packageNameWithDot + remoteHomeName);
        }
        if (hasLocal) {
            entity.setLocal(packageNameWithDot + localName);
            entity.setLocalHome(packageNameWithDot + localHomeName);
        }
        enterpriseBeans.addEntity(entity);
        // add transaction requirements
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        if (assemblyDescriptor == null) {
            assemblyDescriptor = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(assemblyDescriptor);
        }
        ContainerTransaction containerTransaction = assemblyDescriptor.newContainerTransaction();
        containerTransaction.setTransAttribute("Required"); //NOI18N;
        org.netbeans.modules.j2ee.dd.api.ejb.Method method = containerTransaction.newMethod();
        method.setEjbName(ejbName);
        method.setMethodName("*"); //NOI18N;
        containerTransaction.addMethod(method);
        assemblyDescriptor.addContainerTransaction(containerTransaction);
        ejbJar.write(ejbModule.getDeploymentDescriptor());
    }

    private void createAndAddMethod(FileObject fileObject, final MethodModel method) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                // called from wizard, there is only one class in file, so it's ok to call this
                ClassTree classTree = SourceUtils.newInstance(workingCopy).getClassTree();
                MethodTree implMethodTree = MethodModelSupport.createMethodTree(workingCopy, method);
                ClassTree modifiedClassTree = workingCopy.getTreeMaker().addClassMember(classTree, implMethodTree);
                workingCopy.rewrite(classTree, modifiedClassTree);
            }
        }).commit();
    }
    
    private void createAndAddMethods(FileObject fileObject, final List<MethodModel> methods) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                // called from wizard, there is only one class in file, so it's ok to call this
                ClassTree classTree = SourceUtils.newInstance(workingCopy).getClassTree();
                ClassTree newClassTree = classTree;
                for (MethodModel method : methods) {
                    MethodTree implMethodTree = MethodModelSupport.createMethodTree(workingCopy, method);
                    newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, implMethodTree);
                }
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }
    
    private MethodModel bmpFindByPrimaryKeyImpl() {
        return MethodModel.create(
                "ejbFindByPrimaryKey", // NOI18N
                primaryKeyClassName,
                "{" +
                "// See EJB 2.0 and EJB 2.1 section 12.2.5\n" +
                "// TODO add code to locate aKey from persistent storage\n" +
                "// throw javax.ejb.ObjectNotFoundException if aKey is not in\n" +
                "// persistent storage.\n" +
                " return null;" + 
                "}",
                Collections.singletonList(MethodModel.Variable.create(primaryKeyClassName, "key")),
                Collections.singletonList("javax.ejb.FinderException"), // NOI18N
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    private MethodModel findByPrimaryKeyLocalHome() {
        return MethodModel.create(
                "findByPrimaryKey",
                packageNameWithDot + localName,
                null,
                Collections.singletonList(MethodModel.Variable.create(primaryKeyClassName, "key")),
                Collections.singletonList("javax.ejb.FinderException"), // NOI18N
                Collections.<Modifier>emptySet()
                );
    }
    
    private MethodModel findByPrimaryKeyRemoteHome() {
        String[] exceptions = new String[] { "javax.ejb.FinderException", "java.rmi.RemoteException" };
        return MethodModel.create(
                "findByPrimaryKey",
                packageNameWithDot + remoteName,
                null,
                Collections.singletonList(MethodModel.Variable.create(primaryKeyClassName, "key")),
                Arrays.asList(exceptions),
                Collections.<Modifier>emptySet()
                );
    }

    private MethodModel cmpGetKeyImpl() {
        Modifier[] modifiers = new Modifier[] {Modifier.PUBLIC, Modifier.ABSTRACT};
        return MethodModel.create(
                "getKey",
                primaryKeyClassName,
                null,
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                new HashSet<Modifier>(Arrays.asList(modifiers))
                );
    }
    
    private MethodModel cmpSetKeyImpl() {
        Modifier[] modifiers = new Modifier[] {Modifier.PUBLIC, Modifier.ABSTRACT};
        return MethodModel.create(
                "setKey",
                "void",
                null,
                Collections.singletonList(MethodModel.Variable.create(primaryKeyClassName, "key")),
                Collections.<String>emptyList(),
                new HashSet<Modifier>(Arrays.asList(modifiers))
                );
    }

    private MethodModel cmpEjbCreateImpl() {
        return MethodModel.create(
                "ejbCreate",
                primaryKeyClassName,
                "{" +
                "if (key == null) {\n" +
                "    throw new CreateException(\"The field \\\"key\\\" must not be null\");\n" +
                "}\n\n" +
                "// TODO add additional validation code, throw CreateException if data is not valid\n" +
                "setKey(key);\n\n" +
                "return null;" + 
                "}",
                Collections.singletonList(MethodModel.Variable.create(primaryKeyClassName, "key")),
                Collections.singletonList("javax.ejb.CreateException"), // NOI18N
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    private MethodModel cmpEjbPostCreateImpl() {
        return MethodModel.create(
                "ejbPostCreate",
                "void",
                "{" +
                "// TODO populate relationships here if appropriate" +
                "}",
                Collections.singletonList(MethodModel.Variable.create(primaryKeyClassName, "key")),
                Collections.<String>emptyList(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    private MethodModel cmpGetKeyLocal() {
        return MethodModel.create(
                "getKey",
                primaryKeyClassName,
                null,
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
    }

    private MethodModel cmpCreateLocalHome() {
        return MethodModel.create(
                "create",
                packageNameWithDot + localName,
                null,
                Collections.singletonList(MethodModel.Variable.create(primaryKeyClassName, "key")),
                Collections.singletonList("javax.ejb.CreateException"), // NOI18N
                Collections.<Modifier>emptySet()
                );
    }
    
    private MethodModel cmpGetKeyRemote() {
        return MethodModel.create(
                "getKey",
                primaryKeyClassName,
                null,
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList("java.rmi.RemoteException"), // NOI18N
                Collections.<Modifier>emptySet()
                );
    }

    private MethodModel cmpCreateRemoteHome() {
        String[] exceptions = new String[] { "javax.ejb.CreateException", "java.rmi.RemoteException" };
        return MethodModel.create(
                "create",
                packageNameWithDot + remoteName,
                null,
                Collections.singletonList(MethodModel.Variable.create(primaryKeyClassName, "key")),
                Arrays.asList(exceptions),
                Collections.<Modifier>emptySet()
                );
    }
    
}
