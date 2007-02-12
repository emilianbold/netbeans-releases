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

import com.sun.source.tree.ClassTree;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.openide.filesystems.FileObject;

/**
 * Generator of Session EJBs for EJB 2.1 and 3.0
 * 
 * @author Martin Adamek
 */
public final class SessionGenerator {
    
    private static final String EJB21_EJBCLASS = "Templates/J2EE/EJB21/SessionEjbClass.java"; // NOI18N
    private static final String EJB21_LOCAL = "Templates/J2EE/EJB21/SessionLocal.java"; // NOI18N
    private static final String EJB21_LOCALHOME = "Templates/J2EE/EJB21/SessionLocalHome.java"; // NOI18N
    private static final String EJB21_REMOTE = "Templates/J2EE/EJB21/SessionRemote.java"; // NOI18N
    private static final String EJB21_REMOTEHOME = "Templates/J2EE/EJB21/SessionRemoteHome.java"; // NOI18N
    
    private static final String EJB30_STATELESS_EJBCLASS = "Templates/J2EE/EJB30/StatelessEjbClass.java"; // NOI18N
    private static final String EJB30_STATEFUL_EJBCLASS = "Templates/J2EE/EJB30/StatefulEjbClass.java"; // NOI18N
    private static final String EJB30_LOCAL = "Templates/J2EE/EJB30/SessionLocal.java"; // NOI18N
    private static final String EJB30_REMOTE = "Templates/J2EE/EJB30/SessionRemote.java"; // NOI18N

    // informations collected in wizard
    private final FileObject pkg;
    private final boolean hasRemote;
    private final boolean hasLocal;
    private final boolean isStateful;
    private final boolean isSimplified;
//    private final boolean hasBusinessInterface;
    private final boolean isXmlBased;
    
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

    public static SessionGenerator create(String wizardTargetName, FileObject pkg, boolean hasRemote, boolean hasLocal, 
            boolean isStateful, boolean isSimplified, boolean hasBusinessInterface, boolean isXmlBased) {
        return new SessionGenerator(wizardTargetName, pkg, hasRemote, hasLocal, isStateful, isSimplified, hasBusinessInterface, isXmlBased);
    } 
    
    private SessionGenerator(String wizardTargetName, FileObject pkg, boolean hasRemote, boolean hasLocal, 
            boolean isStateful, boolean isSimplified, boolean hasBusinessInterface, boolean isXmlBased) {
        this.pkg = pkg;
        this.hasRemote = hasRemote;
        this.hasLocal = hasLocal;
        this.isStateful = isStateful;
        this.isSimplified = isSimplified;
//        this.hasBusinessInterface = hasBusinessInterface;
        this.isXmlBased = isXmlBased;
        this.ejbNameOptions = new EJBNameOptions();
        this.ejbName = ejbNameOptions.getSessionEjbNamePrefix() + wizardTargetName + ejbNameOptions.getSessionEjbNameSuffix();
        this.ejbClassName = ejbNameOptions.getSessionEjbClassPrefix() + wizardTargetName + ejbNameOptions.getSessionEjbClassSuffix();
        this.remoteName = ejbNameOptions.getSessionRemotePrefix() + wizardTargetName + ejbNameOptions.getSessionRemoteSuffix();
        this.remoteHomeName = ejbNameOptions.getSessionRemoteHomePrefix() + wizardTargetName + ejbNameOptions.getSessionRemoteHomeSuffix();
        this.localName = ejbNameOptions.getSessionLocalPrefix() + wizardTargetName + ejbNameOptions.getSessionLocalSuffix();
        this.localHomeName = ejbNameOptions.getSessionLocalHomePrefix() + wizardTargetName + ejbNameOptions.getSessionLocalHomeSuffix();
        this.displayName = ejbNameOptions.getSessionDisplayNamePrefix() + wizardTargetName + ejbNameOptions.getSessionDisplayNameSuffix();
        this.packageNameWithDot = EjbGenerationUtil.getSelectedPackageName(pkg) + ".";
    }
    
    public FileObject generate() throws IOException {
        FileObject resultFileObject = null;
        if (isSimplified) {
            resultFileObject = generateEJB30Classes();
            
            //put these lines in a common function at the appropriate place after EA1
            //something like public EjbJar getEjbJar()
            //This method will be used whereever we construct/get DD object graph to ensure
            //corresponding config listners attached to it.
            Project project = FileOwnerQuery.getOwner(pkg);
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();

            if (isXmlBased) {
                generateEJB30Xml();
            }
        } else {
            resultFileObject = generateEJB21Classes();

            //put these lines in a common function at the appropriate place after EA1
            //something like public EjbJar getEjbJar()
            //This method will be used whereever we construct/get DD object graph to ensure
            //corresponding config listners attached to it.
            Project project = FileOwnerQuery.getOwner(pkg);
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();

            if (isXmlBased) {
                generateEJB21Xml();
            }
        }
        return resultFileObject;
    }

    private FileObject generateEJB21Classes() throws IOException {
        FileObject ejbClassFO = GenerationUtils.createClass(EJB21_EJBCLASS, pkg, ejbClassName, null);
        if (hasRemote) {
            GenerationUtils.createClass(EJB21_REMOTE,  pkg, remoteName, null);
            GenerationUtils.createClass(EJB21_REMOTEHOME, pkg, remoteHomeName, null);
        }
        if (hasLocal) {
            GenerationUtils.createClass(EJB21_LOCAL, pkg, localName, null);
            GenerationUtils.createClass(EJB21_LOCALHOME, pkg, localHomeName, null);
        }
        return ejbClassFO;
    }
    
    private FileObject generateEJB30Classes() throws IOException {
        String ejbClassTemplateName = isStateful ? EJB30_STATEFUL_EJBCLASS : EJB30_STATELESS_EJBCLASS;
        FileObject ejbClassFO = GenerationUtils.createClass(ejbClassTemplateName,  pkg, ejbClassName, null);
        if (hasRemote) {
            GenerationUtils.createClass(EJB30_REMOTE, pkg, remoteName, null);
        }
        if (hasLocal) {
            GenerationUtils.createClass(EJB30_LOCAL, pkg, localName, null);
        }
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
                ClassTree classTree = generationUtils.getClassTree();
                ClassTree newClassTree = classTree;
                if (hasRemote) {
                    newClassTree = generationUtils.addImplementsClause(newClassTree, packageNameWithDot + remoteName);
                }
                if (hasLocal) {
                    newClassTree = generationUtils.addImplementsClause(newClassTree, packageNameWithDot + localName);
                }
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
        return ejbClassFO;
    }

    private void generateEJB21Xml() throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(pkg);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = DDProvider.getDefault().getMergedDDRoot(ejbModule.getMetadataUnit());
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session session = null;
        if (beans == null) {
            beans  = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(beans);
        }
        session = beans.newSession();
        session.setEjbName(ejbName);
        session.setDisplayName(displayName);
        session.setEjbClass(packageNameWithDot + ejbClassName);

        if (hasRemote) {
            session.setRemote(packageNameWithDot + remoteName);
            session.setHome(packageNameWithDot + remoteHomeName);
        }
        if (hasLocal) {
            session.setLocal(packageNameWithDot + localName);
            session.setLocalHome(packageNameWithDot + localHomeName);
        }
        String sessionType = Session.SESSION_TYPE_STATELESS;
        if (isStateful) {
            sessionType = Session.SESSION_TYPE_STATEFUL;
        }
        session.setSessionType(sessionType);
        session.setTransactionType("Container"); // NOI18N
        beans.addSession(session);
        // add transaction requirements
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        if (assemblyDescriptor == null) {
            assemblyDescriptor = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(assemblyDescriptor);
        }
        ContainerTransaction containerTransaction = assemblyDescriptor.newContainerTransaction();
        containerTransaction.setTransAttribute("Required"); //NOI18N
        org.netbeans.modules.j2ee.dd.api.ejb.Method method = containerTransaction.newMethod();
        method.setEjbName(ejbName);
        method.setMethodName("*"); //NOI18N
        containerTransaction.addMethod(method);
        assemblyDescriptor.addContainerTransaction(containerTransaction);
        ejbJar.write(ejbModule.getDeploymentDescriptor());
    }
    
    private void generateEJB30Xml() throws IOException {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }
    
      //TODO: RETOUCHE WS
//    /**
//     * Special case for generating a Session implementation bean for web services
//     */
//    public String generateWebServiceImplBean(String ejbName, FileObject pkg, Project project, String delegateData) throws java.io.IOException {
//        String pkgName = EjbGenerationUtil.getSelectedPackageName(pkg, project);
//        Bean b = genUtil.getDefaultBean();
//        b.setCommentDataEjbName(ejbName);
//        b.setClassname(true);
//        b.setClassnameName(EjbGenerationUtil.getBeanClassName(ejbName)); //NOI18N
//        b.setDelegateData(delegateData);
//        if (pkgName!=null) {
//            b.setClassnamePackage(pkgName);
//        }
//        
//        // generate bean class
//        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
//        boolean simplified = ejbModule.getJ2eePlatformVersion().equals(J2eeModule.JAVA_EE_5);
//        return null;//genUtil.generateBeanClass(simplified ? SESSION_TEMPLATE_WS_JAVAEE5 : SESSION_TEMPLATE, b, pkgName, pkg);
//    }
    
}
