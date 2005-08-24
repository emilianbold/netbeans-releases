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
import java.util.Collection;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EjbGenerationUtil;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EntityAndSessionGenerator;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Bean;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Method;
import org.netbeans.modules.javacore.ClassIndex;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;



/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EntityGenerator extends EntityAndSessionGenerator {

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
        this.isCMP = isCMP;
        DDProvider provider = DDProvider.getDefault();
        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = provider.getDDRoot(ejbModule.getDeploymentDescriptor());
        ClassPath cp = ClassPath.getClassPath(pkg, ClassPath.SOURCE);

        this.primaryKeyFQN = primaryKeyClassName;
        this.primaryKeySimpleName = primaryKeyClassName;

        if (primaryKeyClassName.indexOf(".") == -1) {
            Collection classes = JMIUtils.getClassesBySimpleName(primaryKeyClassName, cp);
            if (!classes.isEmpty()) {
                JavaClass jc = (JavaClass) classes.toArray()[0];
                this.primaryKeyFQN = jc.getName();
                this.primaryKeySimpleName = jc.getSimpleName();
            } else {
                JavaClass jc = (JavaClass) JMIUtils.resolveType("java.lang." + primaryKeyClassName);
                if (!(jc instanceof UnresolvedClass)) {
                    this.primaryKeyFQN = jc.getName();
                    this.primaryKeySimpleName = jc.getSimpleName();
                }
            }
        } else {
            JavaClass jc = (JavaClass) JMIUtils.resolveType(primaryKeyClassName);
            if (!(jc instanceof UnresolvedClass)) {
                this.primaryKeyFQN = jc.getName();
                this.primaryKeySimpleName = jc.getSimpleName();
            }
        }

        ejbName = EjbGenerationUtil.uniqueSingleEjbName(ejbName, ejbJar);

        String pkgName = EjbGenerationUtil.getSelectedPackageName(pkg, project);
        Bean b = genUtil.getDefaultBean();
        b.setCommentDataEjbName(ejbName + "Bean");
        b.setClassname(true);
        b.setClassnameName(EjbGenerationUtil.getBeanClassName(ejbName)); //NOI18N;
        if (pkgName != null) {
            b.setClassnamePackage(pkgName);
        }
        b.setKey(true);
        b.setKeyFullname(this.primaryKeySimpleName);

        J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();

        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        if (beans == null) {
            beans = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(beans);
        }
        Entity e = beans.newEntity();
        e.setEjbName(b.getCommentDataEjbName());
        e.setEjbClass(EjbGenerationUtil.getFullClassName(b.getClassnamePackage(), b.getClassnameName()));
        e.setPrimKeyClass(this.primaryKeyFQN);
//        e.setPrimKeyClass(this.primaryKeySimpleName);
        e.setReentrant(false);
        e.setDisplayName(EjbGenerationUtil.getEjbNameBase(e.getEjbName()) + "EB");
        final String beanTemplate;
        if (isCMP) {
            beanTemplate = EjbGenerationUtil.TEMPLATE_BASE + "CMPBean.xml";
            populateCMP(b, e);
        } else {
            beanTemplate = EjbGenerationUtil.TEMPLATE_BASE + "BMPBean.xml";
            populateBMP(e);
        }

        // generate bean class
        String beanClass = genUtil.generateBeanClass(beanTemplate, b, pkgName, pkg, false);
        e.setEjbClass(beanClass);

        FileObject bFile = pkg.getFileObject(EjbGenerationUtil.getBaseName(beanClass),"java"); //NOI18N


        if (hasRemote) {
            String remoteName = generateRemote(pkgName, pkg, EjbGenerationUtil.getRemoteName(pkgName, ejbName),
                    ejbName);
            e.setRemote(remoteName);
            String remoteHomeName = generateHome(pkgName, pkg, EjbGenerationUtil.getHomeName(pkgName, ejbName),
                    remoteName, ejbName);
            e.setHome(remoteHomeName);
            String businessIntfName = EjbGenerationUtil.getBusinessInterfaceName(pkgName, ejbName);
            genUtil.generateBusinessInterfaces(pkgName, pkg, businessIntfName, ejbName, beanClass, remoteName);
            genUtil.addPKGetter(e, cp.findResource(businessIntfName.replace('.', '/') + ".java"), true);
        }

        if (hasLocal) {
            String localName = generateLocal(pkgName, pkg, EjbGenerationUtil.getLocalName(pkgName, ejbName), ejbName);
            e.setLocal(localName);
            String localHomeName = generateLocalHome(pkgName, pkg, EjbGenerationUtil.getLocalHomeName(pkgName, ejbName),
                    localName, ejbName);
            e.setLocalHome(localHomeName);
            String businessIntfName = EjbGenerationUtil.getLocalBusinessInterfaceName(pkgName, ejbName);
            genUtil.generateBusinessInterfaces(pkgName, pkg, businessIntfName, ejbName, beanClass, localName);
            genUtil.addPKGetter(e, cp.findResource(businessIntfName.replace('.', '/') + ".java"), false);
        }

        DataObject dobj = DataObject.find(bFile);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        ec.open();

        beans.addEntity(e);
        // add transaction requirements
        AssemblyDescriptor ad = ejbJar.getSingleAssemblyDescriptor();
        if (ad == null) {
            ad = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(ad);
        }
        ContainerTransaction ct = ad.newContainerTransaction();
        ct.setTransAttribute("Required"); //NOI18N;
        org.netbeans.modules.j2ee.dd.api.ejb.Method m = ct.newMethod();
        m.setEjbName(ejbName + "Bean");
        m.setMethodName("*"); //NOI18N;
        ct.addMethod(m);
        ad.addContainerTransaction(ct);
        ejbJar.write(ejbModule.getDeploymentDescriptor());
    }
    
    private void populateCMP(Bean genData, Entity e) {
        genData.addCmField(true);
        genData.setCmFieldMethodName(0, "Key");
        genData.setCmFieldClassname(0, this.primaryKeySimpleName);
        genData.addCmFieldName("key");
        e.setPersistenceType(Entity.PERSISTENCE_TYPE_CONTAINER);
        CmpField f = e.newCmpField();
        f.setFieldName(genData.getCmFieldName(0));
        e.addCmpField(f);
        e.setPrimkeyField(genData.getCmFieldName(0));
        e.setAbstractSchemaName(EjbGenerationUtil.getEjbNameBase(e.getEjbName()));
    }

    private void populateBMP(Entity e) {
        e.setPersistenceType(Entity.PERSISTENCE_TYPE_BEAN);
    }

    protected Method[] getPrimaryMethods(String local, String remote) {
        // findByPrimaryKey method
        Method find = new Method();
        find.setName("findByPrimaryKey");
        if(local != null) {
            find.setLocalReturn(local);
        }
        if(remote != null) {
            find.setRemoteReturn(remote);
        }
        find.addParam(true);
        find.addParamName("key");
        find.addParamType(this.primaryKeySimpleName);
        find.addException(true);
        find.addExceptionType("javax.ejb.FinderException");
        
        if (isCMP) {
            // create method
            Method create = new Method();
            create.setName("create");
            if(local != null) {
                create.setLocalReturn(local);
            }
            if(remote != null) {
                create.setRemoteReturn(remote);
            }
            create.addParam(true);
            create.addParamName("key");
            create.addParamType(this.primaryKeySimpleName);
            create.addException(true);
            create.addExceptionType("javax.ejb.CreateException");

            return new Method[] {find, create};
        }
        
        return new Method[] {find};
    }

    public void setCMP(boolean cmp) {
        isCMP = cmp;
    }
}
