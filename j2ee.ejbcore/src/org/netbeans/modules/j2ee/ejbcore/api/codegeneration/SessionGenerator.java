/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import java.io.IOException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Bean;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Method;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;


/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class SessionGenerator extends EntityAndSessionGenerator {
    private static final String SESSION_TEMPLATE = EjbGenerationUtil.TEMPLATE_BASE+"SessionBean.xml"; //NOI18N
    
    public void generateSessionBean(String ejbName, FileObject pkg,
    boolean hasRemote, boolean hasLocal,
    boolean isStateful, Project project)
    throws IOException {
        DDProvider provider = DDProvider.getDefault();
        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = provider.getDDRoot(ejbModule.getDeploymentDescriptor());

        ejbName = EjbGenerationUtil.uniqueSingleEjbName(ejbName, ejbJar);
        
        String remoteName = null,
        homeName = null,
        localName = null,
        localHomeName = null;
        String pkgName = EjbGenerationUtil.getSelectedPackageName(pkg, project);
        Bean b = genUtil.getDefaultBean();
        b.setCommentDataEjbName(ejbName + "Bean");
        b.setClassname(true);
        b.setClassnameName(EjbGenerationUtil.getBeanClassName(ejbName)); //NOI18N
        if (pkgName!=null) {
            b.setClassnamePackage(pkgName);
        }
        
        // generate bean class
        String beanClass = genUtil.generateBeanClass(SESSION_TEMPLATE, b, pkgName, pkg, false);
        
        if (hasRemote) {
            remoteName = generateRemote(pkgName, pkg, EjbGenerationUtil.getRemoteName(pkgName, ejbName), ejbName);
            homeName = generateHome(pkgName, pkg, EjbGenerationUtil.getHomeName(pkgName, ejbName), remoteName, ejbName);
            String businessIntfName = EjbGenerationUtil.getBusinessInterfaceName(pkgName, ejbName);
            genUtil.generateBusinessInterfaces(pkgName, pkg, businessIntfName, ejbName, beanClass, remoteName);
        }
        
        if (hasLocal) {
            localName = generateLocal(pkgName, pkg, EjbGenerationUtil.getLocalName(pkgName, ejbName), ejbName);
            localHomeName = generateLocalHome(pkgName, pkg, EjbGenerationUtil.getLocalHomeName(pkgName, ejbName),
                    localName, ejbName);
            String businessIntfName = EjbGenerationUtil.getLocalBusinessInterfaceName(pkgName, ejbName);
            genUtil.generateBusinessInterfaces(pkgName, pkg, businessIntfName, ejbName, beanClass, localName);
        }
        FileObject bFile = pkg.getFileObject(EjbGenerationUtil.getBaseName(beanClass),"java"); //NOI18N
        DataObject dobj = DataObject.find(bFile);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        ec.open();
        
        //put these lines in a common function at the appropriate place after EA1
        //something like public EjbJar getEjbJar()
        //This method will be used whereever we construct/get DD object graph to ensure
        //corresponding config listners attached to it.
        
        ///
        J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        ///
        
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
        return genUtil.generateBeanClass(SESSION_TEMPLATE, b, pkgName, pkg);
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
