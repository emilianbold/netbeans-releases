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
import java.util.Collections;
import javax.jmi.reflect.JmiException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Annotation;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.JMIGenerationUtil;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Bean;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Method;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class SessionGenerator extends EntityAndSessionGenerator {
    private static final String SESSION_TEMPLATE = EjbGenerationUtil.TEMPLATE_BASE+"SessionBean.xml"; //NOI18N
    private static final String SESSION_TEMPLATE_WS_JAVAEE5 = EjbGenerationUtil.TEMPLATE_BASE+"SessionBean_WS_JavaEE5.xml"; //NOI18N
    
    public FileObject generateSessionBean(String ejbName, FileObject pkg,
            boolean hasRemote, boolean hasLocal,
            boolean isStateful, Project project)
            throws IOException {
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
        String beanClass = null;
        if (simplified) {
            boolean rollback = true;
            JMIUtils.beginJmiTransaction(true);
            try {
                JavaClass javaClass = JMIGenerationUtil.createClass(pkg, EjbGenerationUtil.getBeanClassName(ejbName));
                String annotationTypeName = isStateful ? "javax.ejb.Stateful" : "javax.ejb.Stateless";
                Annotation sessionType = JMIGenerationUtil.createAnnotation(javaClass, annotationTypeName, Collections.EMPTY_LIST);
                javaClass.getAnnotations().add(sessionType);
                beanClass = javaClass.getName();
                rollback = false;
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            } catch (JmiException jmie) {
                ErrorManager.getDefault().notify(jmie);
            } finally {
                JMIUtils.endJmiTransaction(rollback);
            }
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
            beanClass = genUtil.generateBeanClass(SESSION_TEMPLATE, b, pkgName, pkg, false);
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
            genUtil.generateBusinessInterfaces(pkgName, pkg, remoteBusinessIntfName, ejbName, beanClass, remoteName, simplified);
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
            genUtil.generateBusinessInterfaces(pkgName, pkg, localBusinessIntfName, ejbName, beanClass, localName, simplified);
        }
        
        if (simplified && hasLocal) {
            JavaClass localIF = JMIUtils.findClass(localBusinessIntfName, pkg);
            Annotation localAnn = JMIGenerationUtil.createAnnotation(localIF, "javax.ejb.Local", Collections.EMPTY_LIST);
            localIF.getAnnotations().add(localAnn);
            JMIUtils.saveClass(localIF);
        }
        if (simplified && hasRemote) {
            JavaClass remoteIF = JMIUtils.findClass(remoteBusinessIntfName, pkg);
            Annotation remoteAnn = JMIGenerationUtil.createAnnotation(remoteIF, "javax.ejb.Remote", Collections.EMPTY_LIST);
            remoteIF.getAnnotations().add(remoteAnn);
            JMIUtils.saveClass(remoteIF);
        }
        
        //put these lines in a common function at the appropriate place after EA1
        //something like public EjbJar getEjbJar()
        //This method will be used whereever we construct/get DD object graph to ensure
        //corresponding config listners attached to it.
        
        ///
        J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        ///
        
        FileObject beanFO = pkg.getFileObject(EjbGenerationUtil.getBaseName(beanClass), "java"); // NOI18N
            
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
            s.setEjbClass(beanClass);
            
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
            
            // use simple names in all generated classes, use imports
            boolean rollback = true;
            JMIUtils.beginJmiTransaction(true);
            try {
                JavaMetamodel.getManager().setClassPath(beanFO);
                JMIUtils.fixImports(s.getEjbClass());
                JMIUtils.fixImports(s.getLocal());
                JMIUtils.fixImports(s.getLocalHome());
                JMIUtils.fixImports(s.getRemote());
                JMIUtils.fixImports(s.getHome());
                JMIUtils.fixImports(remoteBusinessIntfName);
                JMIUtils.fixImports(localBusinessIntfName);
                rollback = false;
            } finally {
                JMIUtils.endJmiTransaction(rollback);
            }
            
            JMIUtils.saveClass(s.getLocal(), beanFO);
            JMIUtils.saveClass(s.getLocalHome(), beanFO);
            JMIUtils.saveClass(s.getRemote(), beanFO);
            JMIUtils.saveClass(s.getHome(), beanFO);
            JMIUtils.saveClass(remoteBusinessIntfName, beanFO);
            JMIUtils.saveClass(localBusinessIntfName, beanFO);
        }
        JMIUtils.saveClass(beanClass, beanFO);
        return beanFO;
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
}
