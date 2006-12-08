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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.api.project.Project;
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
    
    private static final String EJB30_EJBCLASS = "TODO"; // NOI18N
    private static final String EJB30_LOCAL = "TODO"; // NOI18N
    private static final String EJB30_REMOTE = "TODO"; // NOI18N

    private final EJBNameOptions ejbNameOptions;
    
    private String ejbName = null;
    private String ejbClassName = null;
    private String remoteName = null;
    private String remoteHomeName = null;
    private String localName = null;
    private String localHomeName = null;

    public SessionGenerator() {
        ejbNameOptions = new EJBNameOptions();
    }
    
    public FileObject generateSessionBean(Model model) throws IOException {
        if (model.isSimplified) {
            return generateEJB30Classes(model);
        } else {
            return generateEJB21Classes(model);
        }
    }

    private FileObject generateEJB21Classes(Model model) throws IOException {
        ejbName = ejbNameOptions.getSessionEjbNamePrefix() + model.ejbClassName + ejbNameOptions.getSessionEjbNameSuffix();
        ejbClassName = ejbNameOptions.getSessionEjbClassPrefix() + model.ejbClassName + ejbNameOptions.getSessionEjbClassSuffix();
        FileObject ejbClassFO = GenerationUtils.createClass(EJB21_EJBCLASS,  model.pkg, ejbClassName, null);
        if (model.hasRemote) {
            remoteName = ejbNameOptions.getSessionRemotePrefix() + model.ejbClassName + ejbNameOptions.getSessionRemoteSuffix();
            GenerationUtils.createClass(EJB21_REMOTE,  model.pkg, remoteName, null);
            remoteHomeName = ejbNameOptions.getSessionRemoteHomePrefix() + model.ejbClassName + ejbNameOptions.getSessionRemoteHomeSuffix();
            GenerationUtils.createClass(EJB21_REMOTEHOME, model.pkg, remoteHomeName, null);
        }
        if (model.hasLocal) {
            localName = ejbNameOptions.getSessionLocalPrefix() + model.ejbClassName + ejbNameOptions.getSessionLocalSuffix();
            GenerationUtils.createClass(EJB21_LOCAL, model.pkg, localName, null);
            localHomeName = ejbNameOptions.getSessionLocalHomePrefix() + model.ejbClassName + ejbNameOptions.getSessionLocalHomeSuffix();
            GenerationUtils.createClass(EJB21_LOCALHOME, model.pkg, localHomeName, null);
        }

        //put these lines in a common function at the appropriate place after EA1
        //something like public EjbJar getEjbJar()
        //This method will be used whereever we construct/get DD object graph to ensure
        //corresponding config listners attached to it.
        Project project = FileOwnerQuery.getOwner(model.pkg);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();
        
        if (model.isXmlBased) {
            generateEJB21Xml(model);
        }
        return ejbClassFO;
    }
    
    private FileObject generateEJB30Classes(Model model) throws IOException {
        ejbName = ejbNameOptions.getSessionEjbNamePrefix() + model.ejbClassName + ejbNameOptions.getSessionEjbNameSuffix();
        ejbClassName = ejbNameOptions.getSessionEjbClassPrefix() + model.ejbClassName + ejbNameOptions.getSessionEjbClassSuffix();
        FileObject ejbClassFO = GenerationUtils.createClass(EJB30_EJBCLASS,  model.pkg, ejbClassName, null);
        if (model.hasRemote) {
            remoteName = ejbNameOptions.getSessionRemotePrefix() + model.ejbClassName + ejbNameOptions.getSessionRemoteSuffix();
            GenerationUtils.createClass(EJB30_REMOTE,  model.pkg, remoteName, null);
        }
        if (model.hasLocal) {
            localName = ejbNameOptions.getSessionLocalPrefix() + model.ejbClassName + ejbNameOptions.getSessionLocalSuffix();
            GenerationUtils.createClass(EJB30_LOCAL, model.pkg, localName, null);
        }

        //put these lines in a common function at the appropriate place after EA1
        //something like public EjbJar getEjbJar()
        //This method will be used whereever we construct/get DD object graph to ensure
        //corresponding config listners attached to it.
        Project project = FileOwnerQuery.getOwner(model.pkg);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();
        
        if (model.isXmlBased) {
            generateEJB30Xml(model);
        }
        return ejbClassFO;
    }

    private void generateEJB21Xml(Model model) throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(model.pkg);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = DDProvider.getDefault().getMergedDDRoot(ejbModule.getMetadataUnit());
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session session = null;
        if (beans == null) {
            beans  = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(beans);
        }
        session = beans.newSession();
        session.setEjbName(ejbName);
        session.setDisplayName(ejbName); // TODO: add SB suffix?
        Project project = FileOwnerQuery.getOwner(model.pkg);
        session.setEjbClass(EjbGenerationUtil.getSelectedPackageName(model.pkg, project) + ejbClassName);

        session.setRemote(remoteName);
        session.setLocal(localName);
        session.setHome(remoteHomeName);
        session.setLocalHome(localHomeName);
        String sessionType = Session.SESSION_TYPE_STATELESS;
        if (model.isStateful) {
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
    
    private void generateEJB30Xml(Model model) throws IOException {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }
    
//    public FileObject generateSessionBeanOld(String ejbName, FileObject pkg, boolean hasRemote, boolean hasLocal,
//            final boolean isStateful, Project project) throws IOException {
//        final DDProvider provider = DDProvider.getDefault();
//        final EjbJar ejbModule = EjbJar.getEjbJar(pkg);
//        final boolean simplified = ejbModule.getJ2eePlatformVersion().equals(J2eeModule.JAVA_EE_5);
//        final org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = provider.getMergedDDRoot(ejbModule.getMetadataUnit());
//        
//        ejbName = EjbGenerationUtil.uniqueSingleEjbName(ejbName, ejbJar);
//        
//        String remoteName = null;
//        String homeName = null;
//        String localName = null;
//        String localHomeName = null;
//        String pkgName = EjbGenerationUtil.getSelectedPackageName(pkg, project);
//        
//        // generate bean class
//        final String[] beanClass = new String[1];
//        FileObject beanClassFO = null;
//        if (simplified) {
//            beanClassFO = GenerationUtils.createClass(pkg, EjbGenerationUtil.getBeanClassName(ejbName), null);
//            String annotationTypeName = isStateful ? "javax.ejb.Stateful" : "javax.ejb.Stateless";
//            addSimpleAnnotationToClass(beanClassFO, annotationTypeName);
//            JavaSource javaSource = JavaSource.forFileObject(beanClassFO);
//            ModificationResult modificationResult = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
//                public void run(WorkingCopy workingCopy) throws Exception {
//                    workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
//                    GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
//                    TypeElement javaClass = generationUtils.getTypeElement();
//                    beanClass[0] = javaClass.getQualifiedName().toString();
//                }
//            });
//            modificationResult.commit();
//        } else {
//            beanClassFO = pkg.getFileObject(EjbGenerationUtil.getBaseName(beanClass[0]), "java"); // NOI18N
//            Bean b = genUtil.getDefaultBean();
//            b.setCommentDataEjbName(ejbName + "Bean");
//            b.setClassname(true);
//            b.setClassnameName(EjbGenerationUtil.getBeanClassName(ejbName)); //NOI18N
//            if (simplified) {
//                b.setClassnameAnnotation(isStateful ? "Stateful" : "Stateless");
//            }
//            if (pkgName!=null) {
//                b.setClassnamePackage(pkgName);
//            }
//            beanClass[0] = null;//genUtil.generateBeanClass(SESSION_TEMPLATE, b, pkgName, pkg, false);
//        }
//        
//        String remoteBusinessIntfName = null;
//        if (hasRemote) {
//            if (simplified) {
//                remoteBusinessIntfName = EjbGenerationUtil.getRemoteName(pkgName, ejbName);
//            } else {
//                remoteName = generateRemote(pkgName, pkg, EjbGenerationUtil.getRemoteName(pkgName, ejbName), ejbName);
//                homeName = generateHome(pkgName, pkg, EjbGenerationUtil.getHomeName(pkgName, ejbName), remoteName, ejbName);
//                remoteBusinessIntfName = EjbGenerationUtil.getBusinessInterfaceName(pkgName, ejbName);
//            }
//            genUtil.generateBusinessInterfaces(pkgName, pkg, remoteBusinessIntfName, ejbName, beanClass[0], remoteName, simplified);
//        }
//        
//        String localBusinessIntfName = null;
//        if (hasLocal) {
//            if (simplified) {
//                localBusinessIntfName = EjbGenerationUtil.getLocalName(pkgName, ejbName);
//            } else {
//                localName = generateLocal(pkgName, pkg, EjbGenerationUtil.getLocalName(pkgName, ejbName), ejbName);
//                localHomeName = generateLocalHome(pkgName, pkg, EjbGenerationUtil.getLocalHomeName(pkgName, ejbName),
//                        localName, ejbName);
//                localBusinessIntfName = EjbGenerationUtil.getLocalBusinessInterfaceName(pkgName, ejbName);
//            }
//            genUtil.generateBusinessInterfaces(pkgName, pkg, localBusinessIntfName, ejbName, beanClass[0], localName, simplified);
//        }
//        
//        final String localBusIfName = localBusinessIntfName;
//        if (simplified && hasLocal) {
//            JavaSource javaSource = JavaSource.forFileObject(beanClassFO);
//            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
//                public void run(WorkingCopy workingCopy) throws Exception {
//                    workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
//                    TypeElement typeElement = workingCopy.getElements().getTypeElement(localBusIfName);
//                    addSimpleAnnotationToClass(workingCopy, typeElement, "javax.ejb.Local");
//                }
//            });
//        }
//        final String remoteBusIfName = remoteBusinessIntfName;
//        if (simplified && hasRemote) {
//            JavaSource javaSource = JavaSource.forFileObject(beanClassFO);
//            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
//                public void run(WorkingCopy workingCopy) throws Exception {
//                    workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
//                    TypeElement typeElement = workingCopy.getElements().getTypeElement(remoteBusIfName);
//                    addSimpleAnnotationToClass(workingCopy, typeElement, "javax.ejb.Remote");
//                }
//            });
//        }
//        
//        //put these lines in a common function at the appropriate place after EA1
//        //something like public EjbJar getEjbJar()
//        //This method will be used whereever we construct/get DD object graph to ensure
//        //corresponding config listners attached to it.
//        
//        ///
//        J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
//        pwm.getConfigSupport().ensureConfigurationReady();
//        ///
//        
//        // for annotable EJBs it will be discovered by annotation listener
//        if (!simplified) {
//            EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
//            Session s = null;
//            if (beans == null) {
//                beans  = ejbJar.newEnterpriseBeans();
//                ejbJar.setEnterpriseBeans(beans);
//            }
//            s = beans.newSession();
//            s.setEjbName(ejbName + "Bean");
//            s.setDisplayName(ejbName+"SB");
//            s.setEjbClass(beanClass[0]);
//            
//            s.setRemote(remoteName);
//            s.setLocal(localName);
//            s.setHome(homeName);
//            s.setLocalHome(localHomeName);
//            String sessionType = "Stateless";
//            if (isStateful) {
//                sessionType="Stateful";
//            }
//            s.setSessionType(sessionType);
//            s.setTransactionType("Container");
//            beans.addSession(s);
//            // add transaction requirements
//            AssemblyDescriptor ad = ejbJar.getSingleAssemblyDescriptor();
//            if (ad == null) {
//                ad = ejbJar.newAssemblyDescriptor();
//                ejbJar.setAssemblyDescriptor(ad);
//            }
//            ContainerTransaction ct = ad.newContainerTransaction();
//            ct.setTransAttribute("Required"); //NOI18N
//            org.netbeans.modules.j2ee.dd.api.ejb.Method m = ct.newMethod();
//            m.setEjbName(ejbName + "Bean");
//            m.setMethodName("*"); //NOI18N
//            ct.addMethod(m);
//            ad.addContainerTransaction(ct);
//            ejbJar.write(ejbModule.getDeploymentDescriptor());
//            
//        }
//        return beanClassFO;
//    }
    
    /**
     * Special case for generating a Session implementation bean for web services
     */
    public String generateWebServiceImplBean(String ejbName, FileObject pkg, Project project, String delegateData) throws java.io.IOException {
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
        return null;//genUtil.generateBeanClass(simplified ? SESSION_TEMPLATE_WS_JAVAEE5 : SESSION_TEMPLATE, b, pkgName, pkg);
    }
    
//    protected Method[] getPrimaryMethods(String local, String remote) {
//        Method create = new Method();
//        create.setName("create");
//        if(local != null) {
//            create.setLocalReturn(local);
//        }
//        if(remote != null) {
//            create.setRemoteReturn(remote);
//        }
//        create.addException(true);
//        create.addExceptionType("javax.ejb.CreateException");
//        return new Method[] {create};
//    }
//    
//    private static void addSimpleAnnotationToClass(FileObject beanClassFO, final String annotationTypeName) throws IOException {
//        JavaSource javaSource = JavaSource.forFileObject(beanClassFO);
//        ModificationResult modificationResult = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
//            public void run(WorkingCopy workingCopy) throws Exception {
//                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
//                GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
//                TypeElement javaClass = generationUtils.getTypeElement();
//                addSimpleAnnotationToClass(workingCopy, javaClass, annotationTypeName);
//            }
//        });
//        modificationResult.commit();
//    }
//
//    private static void addSimpleAnnotationToClass(WorkingCopy workingCopy, TypeElement typeElement, final String annotationTypeName) {
//        TreeMaker treeMaker = workingCopy.getTreeMaker();
//        Trees trees = workingCopy.getTrees();
//        ClassTree clazz = trees.getTree(typeElement);
//        AnnotationTree annotationTree = treeMaker.Annotation(
//                trees.getTree(workingCopy.getElements().getTypeElement(annotationTypeName)),
//                Collections.<ExpressionTree>emptyList()
//                );
//        ModifiersTree modifiersTree = treeMaker.Modifiers(
//                clazz.getModifiers(),
//                Collections.<AnnotationTree>singletonList(annotationTree)
//                );
//        ClassTree modifiedClazz = treeMaker.Class(
//                modifiersTree,
//                clazz.getSimpleName(),
//                clazz.getTypeParameters(),
//                clazz.getExtendsClause(),
//                (List<ExpressionTree>) clazz.getImplementsClause(), //TODO: RETOUCHE ???
//                clazz.getMembers()
//                );
//        workingCopy.rewrite(clazz, modifiedClazz);
//    }
    
    /** immutable model of data entered in wizard */
    public static final class Model {
        
        private final String ejbClassName;
        private final FileObject pkg;
        private final boolean hasRemote;
        private final boolean hasLocal;
        private final boolean isStateful;
        private final boolean isSimplified;
        private final boolean hasBusinessInterface;
        private final boolean isXmlBased;
        
        public static Model create(String ejbClassName, FileObject pkg, boolean hasRemote, boolean hasLocal, boolean isStateful, boolean isSimplified, boolean hasBusinessInterface, boolean isXmlBased) {
            return new Model(ejbClassName, pkg, hasRemote, hasLocal, isStateful, isSimplified, hasBusinessInterface, isXmlBased);
        }
        
        private Model(String ejbClassName, FileObject pkg, boolean hasRemote, boolean hasLocal, boolean isStateful, boolean isSimplified, boolean hasBusinessInterface, boolean isXmlBased) {
            this.ejbClassName = ejbClassName;
            this.pkg = pkg;
            this.hasRemote = hasRemote;
            this.hasLocal = hasLocal;
            this.isStateful = isStateful;
            this.isSimplified = isSimplified;
            this.hasBusinessInterface = hasBusinessInterface;
            this.isXmlBased = isXmlBased;
        }
        
    }
    
}
