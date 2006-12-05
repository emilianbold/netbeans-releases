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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Bean;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Method;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class SessionGenerator extends EntityAndSessionGenerator {
    private static final String SESSION_TEMPLATE = EjbGenerationUtil.TEMPLATE_BASE+"SessionBean.xml"; //NOI18N
    private static final String SESSION_TEMPLATE_WS_JAVAEE5 = EjbGenerationUtil.TEMPLATE_BASE+"SessionBean_WS_JavaEE5.xml"; //NOI18N
    
    public FileObject generateSessionBean(String ejbName, FileObject pkg, boolean hasRemote, boolean hasLocal,
            final boolean isStateful, Project project) throws IOException {
        DDProvider provider = DDProvider.getDefault();
        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
        boolean simplified = ejbModule.getJ2eePlatformVersion().equals(J2eeModule.JAVA_EE_5);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = provider.getMergedDDRoot(ejbModule.getMetadataUnit());
        
        ejbName = EjbGenerationUtil.uniqueSingleEjbName(ejbName, ejbJar);
        
        String remoteName = null,
                homeName = null,
                localName = null,
                localHomeName = null;
        String pkgName = EjbGenerationUtil.getSelectedPackageName(pkg, project);
        
        // generate bean class
        final String[] beanClass = new String[1];
        FileObject beanClassFO = null;
        if (simplified) {
            beanClassFO = GenerationUtils.createClass(pkg, EjbGenerationUtil.getBeanClassName(ejbName), null);
            String annotationTypeName = isStateful ? "javax.ejb.Stateful" : "javax.ejb.Stateless";
            addSimpleAnnotationToClass(beanClassFO, annotationTypeName);
            JavaSource javaSource = JavaSource.forFileObject(beanClassFO);
            ModificationResult modificationResult = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                    GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
                    TypeElement javaClass = generationUtils.getTypeElement();
                    beanClass[0] = javaClass.getQualifiedName().toString();
                }
            });
            modificationResult.commit();
        } else {
            Bean b = genUtil.getDefaultBean();
            b.setCommentDataEjbName(ejbName + "Bean");
            b.setClassname(true);
            b.setClassnameName(EjbGenerationUtil.getBeanClassName(ejbName)); //NOI18N
            if (simplified) {
                b.setClassnameAnnotation(isStateful ? "Stateful" : "Stateless");
            }
            if (pkgName!=null) {
                b.setClassnamePackage(pkgName);
            }
            beanClass[0] = genUtil.generateBeanClass(SESSION_TEMPLATE, b, pkgName, pkg, false);
        }
        
        String remoteBusinessIntfName = null;
        if (hasRemote) {
            if (simplified) {
                remoteBusinessIntfName = EjbGenerationUtil.getRemoteName(pkgName, ejbName);
            } else {
                remoteName = generateRemote(pkgName, pkg, EjbGenerationUtil.getRemoteName(pkgName, ejbName), ejbName);
                homeName = generateHome(pkgName, pkg, EjbGenerationUtil.getHomeName(pkgName, ejbName), remoteName, ejbName);
                remoteBusinessIntfName = EjbGenerationUtil.getBusinessInterfaceName(pkgName, ejbName);
            }
            genUtil.generateBusinessInterfaces(pkgName, pkg, remoteBusinessIntfName, ejbName, beanClass[0], remoteName, simplified);
        }
        
        String localBusinessIntfName = null;
        if (hasLocal) {
            if (simplified) {
                localBusinessIntfName = EjbGenerationUtil.getLocalName(pkgName, ejbName);
            } else {
                localName = generateLocal(pkgName, pkg, EjbGenerationUtil.getLocalName(pkgName, ejbName), ejbName);
                localHomeName = generateLocalHome(pkgName, pkg, EjbGenerationUtil.getLocalHomeName(pkgName, ejbName),
                        localName, ejbName);
                localBusinessIntfName = EjbGenerationUtil.getLocalBusinessInterfaceName(pkgName, ejbName);
            }
            genUtil.generateBusinessInterfaces(pkgName, pkg, localBusinessIntfName, ejbName, beanClass[0], localName, simplified);
        }
        
        final String localBusIfName = localBusinessIntfName;
        if (simplified && hasLocal) {
            JavaSource javaSource = JavaSource.forFileObject(beanClassFO);
            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = workingCopy.getElements().getTypeElement(localBusIfName);
                    addSimpleAnnotationToClass(workingCopy, typeElement, "javax.ejb.Local");
                }
            });
        }
        final String remoteBusIfName = remoteBusinessIntfName;
        if (simplified && hasRemote) {
            JavaSource javaSource = JavaSource.forFileObject(beanClassFO);
            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = workingCopy.getElements().getTypeElement(remoteBusIfName);
                    addSimpleAnnotationToClass(workingCopy, typeElement, "javax.ejb.Remote");
                }
            });
        }
        
        //put these lines in a common function at the appropriate place after EA1
        //something like public EjbJar getEjbJar()
        //This method will be used whereever we construct/get DD object graph to ensure
        //corresponding config listners attached to it.
        
        ///
        J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        ///
        
        // for annotable EJBs it will be discovered by annotation listener
        if (!simplified) {
            EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
            Session s = null;
            if (beans == null) {
                beans  = ejbJar.newEnterpriseBeans();
                ejbJar.setEnterpriseBeans(beans);
            }
            s = beans.newSession();
            s.setEjbName(ejbName + "Bean");
            s.setDisplayName(ejbName+"SB");
            s.setEjbClass(beanClass[0]);
            
            s.setRemote(remoteName);
            s.setLocal(localName);
            s.setHome(homeName);
            s.setLocalHome(localHomeName);
            String sessionType = "Stateless";
            if (isStateful) {
                sessionType="Stateful";
            }
            s.setSessionType(sessionType);
            s.setTransactionType("Container");
            beans.addSession(s);
            // add transaction requirements
            AssemblyDescriptor ad = ejbJar.getSingleAssemblyDescriptor();
            if (ad == null) {
                ad = ejbJar.newAssemblyDescriptor();
                ejbJar.setAssemblyDescriptor(ad);
            }
            ContainerTransaction ct = ad.newContainerTransaction();
            ct.setTransAttribute("Required"); //NOI18N
            org.netbeans.modules.j2ee.dd.api.ejb.Method m = ct.newMethod();
            m.setEjbName(ejbName + "Bean");
            m.setMethodName("*"); //NOI18N
            ct.addMethod(m);
            ad.addContainerTransaction(ct);
            ejbJar.write(ejbModule.getDeploymentDescriptor());
            
        }
        return beanClassFO;
    }
    
    /**
     * Special case for generating a Session implementation bean for web services
     */
    public String generateWebServiceImplBean(String ejbName, FileObject pkg, Project project, String delegateData) throws java.io.IOException {
        String pkgName = EjbGenerationUtil.getSelectedPackageName(pkg, project);
        Bean b = genUtil.getDefaultBean();
        b.setCommentDataEjbName(ejbName);
        b.setClassname(true);
        b.setClassnameName(EjbGenerationUtil.getBeanClassName(ejbName)); //NOI18N
        b.setDelegateData(delegateData);
        if (pkgName!=null) {
            b.setClassnamePackage(pkgName);
        }
        
        // generate bean class
        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
        boolean simplified = ejbModule.getJ2eePlatformVersion().equals(J2eeModule.JAVA_EE_5);
        return genUtil.generateBeanClass(simplified ? SESSION_TEMPLATE_WS_JAVAEE5 : SESSION_TEMPLATE, b, pkgName, pkg);
    }
    
    protected Method[] getPrimaryMethods(String local, String remote) {
        Method create = new Method();
        create.setName("create");
        if(local != null) {
            create.setLocalReturn(local);
        }
        if(remote != null) {
            create.setRemoteReturn(remote);
        }
        create.addException(true);
        create.addExceptionType("javax.ejb.CreateException");
        return new Method[] {create};
    }
    
    private static void addSimpleAnnotationToClass(FileObject beanClassFO, final String annotationTypeName) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(beanClassFO);
        ModificationResult modificationResult = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
                TypeElement javaClass = generationUtils.getTypeElement();
                addSimpleAnnotationToClass(workingCopy, javaClass, annotationTypeName);
            }
        });
        modificationResult.commit();
    }

    private static void addSimpleAnnotationToClass(WorkingCopy workingCopy, TypeElement typeElement, final String annotationTypeName) {
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        Trees trees = workingCopy.getTrees();
        ClassTree clazz = trees.getTree(typeElement);
        AnnotationTree annotationTree = treeMaker.Annotation(
                trees.getTree(workingCopy.getElements().getTypeElement(annotationTypeName)),
                Collections.<ExpressionTree>emptyList()
                );
        ModifiersTree modifiersTree = treeMaker.Modifiers(
                clazz.getModifiers(),
                Collections.<AnnotationTree>singletonList(annotationTree)
                );
        ClassTree modifiedClazz = treeMaker.Class(
                modifiersTree,
                clazz.getSimpleName(),
                clazz.getTypeParameters(),
                clazz.getExtendsClause(),
                (List<ExpressionTree>) clazz.getImplementsClause(), //TODO: RETOUCHE ???
                clazz.getMembers()
                );
        workingCopy.rewrite(clazz, modifiedClazz);
    }
    
}
