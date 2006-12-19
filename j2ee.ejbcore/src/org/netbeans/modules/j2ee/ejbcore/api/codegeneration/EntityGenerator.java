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
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EntityGenerator {

    private boolean isCMP = false;
    private String primaryKeyFQN;
    private String primaryKeySimpleName;

    public EntityGenerator() {
    }

    public EntityGenerator(String primaryKeySimpleName) {
        this.primaryKeySimpleName = primaryKeySimpleName;
    }

    public void generateEntity(String ejbName, FileObject pkg, boolean hasRemote, boolean hasLocal, Project project,
            boolean isCMP, String primaryKeyClassName) throws IOException {
//        this.isCMP = isCMP;
//        DDProvider provider = DDProvider.getDefault();
//        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
//        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = provider.getMergedDDRoot(ejbModule.getMetadataUnit());
//        ClassPath cp = ClassPath.getClassPath(pkg, ClassPath.SOURCE);
//
//        this.primaryKeyFQN = primaryKeyClassName;
//        this.primaryKeySimpleName = primaryKeyClassName;
//
//        //TODO: RETOUCHE remove XSLT generation and all these simple/fqn names handling
//
//        ejbName = EjbGenerationUtil.uniqueSingleEjbName(ejbName, ejbJar);
//
//        String pkgName = EjbGenerationUtil.getSelectedPackageName(pkg, project);
//        Bean b = genUtil.getDefaultBean();
//        b.setCommentDataEjbName(ejbName + "Bean");
//        b.setClassname(true);
//        b.setClassnameName(EjbGenerationUtil.getBeanClassName(ejbName)); //NOI18N;
//        if (pkgName != null) {
//            b.setClassnamePackage(pkgName);
//        }
//        b.setKey(true);
//        b.setKeyFullname(this.primaryKeySimpleName);
//
//        J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
//        pwm.getConfigSupport().ensureConfigurationReady();
//
//        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
//        if (beans == null) {
//            beans = ejbJar.newEnterpriseBeans();
//            ejbJar.setEnterpriseBeans(beans);
//        }
//        Entity e = beans.newEntity();
//        e.setEjbName(b.getCommentDataEjbName());
//        e.setEjbClass(EjbGenerationUtil.getFullClassName(b.getClassnamePackage(), b.getClassnameName()));
//        e.setPrimKeyClass(this.primaryKeyFQN);
////        e.setPrimKeyClass(this.primaryKeySimpleName);
//        e.setReentrant(false);
//        e.setDisplayName(EjbGenerationUtil.getEjbNameBase(e.getEjbName()) + "EB");
//        final String beanTemplate;
//        if (isCMP) {
//            beanTemplate = EjbGenerationUtil.TEMPLATE_BASE + "CMPBean.xml";
//            populateCMP(b, e);
//        } else {
//            beanTemplate = EjbGenerationUtil.TEMPLATE_BASE + "BMPBean.xml";
//            populateBMP(e);
//        }
//
//        // generate bean class
//        String beanClass = genUtil.generateBeanClass(beanTemplate, b, pkgName, pkg, false);
//        e.setEjbClass(beanClass);
//
//        FileObject bFile = pkg.getFileObject(EjbGenerationUtil.getBaseName(beanClass),"java"); //NOI18N
//
//        String remoteBusinessIntfName = null;
//        if (hasRemote) {
//            String remoteName = generateRemote(pkgName, pkg, EjbGenerationUtil.getRemoteName(pkgName, ejbName),
//                    ejbName);
//            e.setRemote(remoteName);
//            String remoteHomeName = generateHome(pkgName, pkg, EjbGenerationUtil.getHomeName(pkgName, ejbName),
//                    remoteName, ejbName);
//            e.setHome(remoteHomeName);
//            remoteBusinessIntfName = EjbGenerationUtil.getBusinessInterfaceName(pkgName, ejbName);
//            genUtil.generateBusinessInterfaces(pkgName, pkg, remoteBusinessIntfName, ejbName, beanClass, remoteName);
//            genUtil.addPKGetter(e, cp.findResource(remoteBusinessIntfName.replace('.', '/') + ".java"), true);
//        }
//
//        String localBusinessIntfName = null;
//        if (hasLocal) {
//            String localName = generateLocal(pkgName, pkg, EjbGenerationUtil.getLocalName(pkgName, ejbName), ejbName);
//            e.setLocal(localName);
//            String localHomeName = generateLocalHome(pkgName, pkg, EjbGenerationUtil.getLocalHomeName(pkgName, ejbName),
//                    localName, ejbName);
//            e.setLocalHome(localHomeName);
//            localBusinessIntfName = EjbGenerationUtil.getLocalBusinessInterfaceName(pkgName, ejbName);
//            genUtil.generateBusinessInterfaces(pkgName, pkg, localBusinessIntfName, ejbName, beanClass, localName);
//            genUtil.addPKGetter(e, cp.findResource(localBusinessIntfName.replace('.', '/') + ".java"), false);
//        }
//
//        beans.addEntity(e);
//        // add transaction requirements
//        AssemblyDescriptor ad = ejbJar.getSingleAssemblyDescriptor();
//        if (ad == null) {
//            ad = ejbJar.newAssemblyDescriptor();
//            ejbJar.setAssemblyDescriptor(ad);
//        }
//        ContainerTransaction ct = ad.newContainerTransaction();
//        ct.setTransAttribute("Required"); //NOI18N;
//        org.netbeans.modules.j2ee.dd.api.ejb.Method m = ct.newMethod();
//        m.setEjbName(ejbName + "Bean");
//        m.setMethodName("*"); //NOI18N;
//        ct.addMethod(m);
//        ad.addContainerTransaction(ct);
//        ejbJar.write(ejbModule.getDeploymentDescriptor());
//
//        DataObject dobj = DataObject.find(bFile);
//        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
//        ec.open();

    }
    
//    protected MethodMo[] getPrimaryMethods(String local, String remote) {
//        // findByPrimaryKey method
//        Method find = new Method();
//        find.setName("findByPrimaryKey");
//        if(local != null) {
//            find.setLocalReturn(local);
//        }
//        if(remote != null) {
//            find.setRemoteReturn(remote);
//        }
//        find.addParam(true);
//        find.addParamName("key");
//        find.addParamType(this.primaryKeyFQN);
//        find.addException(true);
//        find.addExceptionType("javax.ejb.FinderException");
//        
//        if (isCMP) {
//            // create method
//            Method create = new Method();
//            create.setName("create");
//            if(local != null) {
//                create.setLocalReturn(local);
//            }
//            if(remote != null) {
//                create.setRemoteReturn(remote);
//            }
//            create.addParam(true);
//            create.addParamName("key");
//            create.addParamType(this.primaryKeyFQN);
//            create.addException(true);
//            create.addExceptionType("javax.ejb.CreateException");
//
//            return new Method[] {find, create};
//        }
//        
//        return new Method[] {find};
//    }

    public void setCMP(boolean cmp) {
        isCMP = cmp;
    }
}
