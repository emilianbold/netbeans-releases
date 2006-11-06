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


package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.io.IOException;
import java.util.ArrayList;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.dnd.EjbReferenceImpl;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;


/**
 * This class provides controller capabilities for ejb logical views. The nodes
 * should delegate non ui tasks to this class.
 * @author  Chris Webster
 * @author Martin Adamek
 */
public class EjbViewController {
    private final Ejb model;
    private final EjbJar module;
    private final Project myProject;
    private final ClassPath srcPath;
    
    public EjbViewController(Ejb model, EjbJar module, ClassPath src) {
        this.model = model;
        this.module = module;
        srcPath = src;
        myProject = FileOwnerQuery.getOwner(srcPath.getRoots()[0]);
    }
    
    public String getDisplayName() {
        String name = model.getDefaultDisplayName();
        if (name == null) {
            name = model.getEjbName();
        }
        return name;
    }
    
    public void delete(boolean deleteClasses) throws IOException {
        EnterpriseBeans beans = module.getEnterpriseBeans();
        J2eeModuleProvider pwm = (J2eeModuleProvider) myProject.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        deleteTraces();
        // for MDBs remove message destination from assembly descriptor
        if (model instanceof MessageDriven) {
            try {
                AssemblyDescriptor assemblyDescriptor = module.getSingleAssemblyDescriptor();
                String mdLinkName = ((MessageDriven) model).getMessageDestinationLink();
                MessageDestination[] messageDestinations = assemblyDescriptor.getMessageDestination();
                for (int i = 0; i < messageDestinations.length; i++) {
                    if (messageDestinations[i].getMessageDestinationName().equals(mdLinkName)) {
                        assemblyDescriptor.removeMessageDestination(messageDestinations[i]);
                        break;
                    }
                }
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        beans.removeEjb(model);
        writeDD();
        if (deleteClasses) {
            deleteClasses();
        }
    }
    
    public EjbReference createEjbReference() {
        AntArtifact[] antArtifacts = AntArtifactQuery.findArtifactsByType(myProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        AntArtifact moduleJarTarget;
        if (antArtifacts == null || antArtifacts.length == 0) {
            moduleJarTarget = null;
        } else {
            moduleJarTarget = antArtifacts[0];
        }
        //TODO: RETOUCHE
        return null;
//        return new EjbReferenceImpl(moduleJarTarget, (EntityAndSession)model);
    }
    
    private FileObject findBeanFo() {
        return srcPath.findResource(model.getEjbClass().replace('.','/')+".java"); // NOI18N
    }
    
    public DataObject getBeanDo() {
        FileObject src = findBeanFo();
        try {
            if (src != null) {
                return DataObject.find(src);
            }
        } catch (DataObjectNotFoundException ex) {
            // should not happen now
        }
        return null;
    }
    
    public TypeElement getBeanClass() {
        try {
            JavaSource javaSource = JavaSource.forFileObject(findBeanFo());
            final TypeElement[] result = new TypeElement[1];
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController cc) throws Exception {
                    result[0] = cc.getElements().getTypeElement(model.getEjbClass());
                }
            }, true);
            return result[0];
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
    }
    
    /**
     * gets an ejb reference representation
     * @return the xml code corresponding to this ejb
     */
    public String getRemoteStringRepresentation(String ejbType) {
        assert model instanceof EntityAndSession;
        EntityAndSession refModel = (EntityAndSession)model;
        String s ="\t<ejb-ref>\n" +
                "\t\t<ejb-ref-name>ejb/"+model.getEjbName()+"</ejb-ref-name>\n"+
                "\t\t<ejb-ref-type>"+ejbType+"</ejb-ref-type>\n"+
                "\t\t<home>"+refModel.getHome()+"</home>\n"+
                "\t\t<remote>"+refModel.getRemote()+"</remote>\n"+
                "\t</ejb-ref>\n";
        return s;
    }
    
    public String getLocalStringRepresentation(String ejbType) {
        assert model instanceof EntityAndSession;
        EntityAndSession refModel = (EntityAndSession)model;
        String s ="\t<ejb-local-ref>\n" +
                "\t\t<ejb-ref-name>ejb/"+model.getEjbName()+"</ejb-ref-name>\n"+
                "\t\t<ejb-ref-type>"+ejbType+"</ejb-ref-type>\n"+
                "\t\t<local-home>"+refModel.getLocalHome()+"</local-home>\n"+
                "\t\t<local>"+refModel.getLocal()+"</local>\n"+
                "\t</ejb-local-ref>\n";
        return s;
    }
    
    
    private void writeDD() throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar apiEjbJar = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(srcPath.getRoots()[0]);
        FileObject ddFile = apiEjbJar.getDeploymentDescriptor();
        DDProvider.getDefault().getMergedDDRoot(apiEjbJar.getMetadataUnit()).write(ddFile);
    }
    
    private boolean isEjbUsed(EjbRelationshipRole role, String ejbName) {
        return role != null &&
                role.getRelationshipRoleSource() != null &&
                ejbName.equals(role.getRelationshipRoleSource().getEjbName());
    }
    
    private void deleteRelationships(String ejbName) {
        Relationships r = module.getSingleRelationships();
        if (r != null) {
            EjbRelation[] relations = r.getEjbRelation();
            if (relations != null) {
                for (int i = 0; i < relations.length; i++) {
                    if (isEjbUsed(relations[i].getEjbRelationshipRole(), ejbName)
                    ||
                            isEjbUsed(relations[i].getEjbRelationshipRole2(), ejbName)) {
                        r.removeEjbRelation(relations[i]);
                    }
                }
                if (r.sizeEjbRelation() == 0) {
                    module.setRelationships(null);
                }
            }
        }
    }
    
    private void deleteTraces() {
        String ejbName = model.getEjbName();
        String ejbNameCompare = ejbName+"";
        deleteRelationships(ejbName);
        AssemblyDescriptor ad = module.getSingleAssemblyDescriptor();
        if (ad != null) {
            ContainerTransaction[] ct = ad.getContainerTransaction();
            for (int i = 0; i < ct.length; i++) {
                Method[] methods = ct[i].getMethod();
                methods = methods== null ? new Method[0]:methods;
                for (int method =0; method < methods.length; method++) {
                    if (ejbNameCompare.equals(methods[method].getEjbName())) {
                        ct[i].removeMethod(methods[method]);
                        if (ct[i].sizeMethod() == 0) {
                            ad.removeContainerTransaction(ct[i]);
                        }
                    }
                }
            }
            MethodPermission[] permissions = ad.getMethodPermission();
            for (int i =0; i < permissions.length; i++) {
                Method[] methods = permissions[i].getMethod();
                methods = methods== null ? new Method[0]:methods;
                for (int method =0; method < methods.length; method++) {
                    if (ejbNameCompare.equals(methods[method].getEjbName())) {
                        permissions[i].removeMethod(methods[method]);
                        if (permissions[i].sizeMethod() == 0) {
                            ad.removeMethodPermission(permissions[i]);
                        }
                    }
                }
            }
        }
    }
    
    private FileObject getFileObject(String className) {
        assert className != null: "cannot find null className";
        return srcPath.findResource(className.replace('.', '/') + ".java");
    }
    
    private void deleteClasses() {
        ArrayList/*<FileObject>*/ classFileObjects = new ArrayList();
        classFileObjects.add(getFileObject(model.getEjbClass()));
        if (model instanceof EntityAndSession) {
            EntityAndSession entityAndSessionfModel = (EntityAndSession)model;
            if (entityAndSessionfModel.getLocalHome() != null) {
                classFileObjects.add(getFileObject(entityAndSessionfModel.getLocalHome()));
                classFileObjects.add(getFileObject(entityAndSessionfModel.getLocal()));
                classFileObjects.add(getFileObject(entityAndSessionfModel.getLocal() + "Business"));
            }
            if (entityAndSessionfModel.getHome() != null) {
                classFileObjects.add(getFileObject(entityAndSessionfModel.getHome()));
                classFileObjects.add(getFileObject(entityAndSessionfModel.getRemote()));
                classFileObjects.add(getFileObject(entityAndSessionfModel.getRemote() + "Business"));
            }
        }
        for (int i = 0; i < classFileObjects.size(); i++) {
            FileObject fo = (FileObject) classFileObjects.get(i);
            if (fo != null) {
                try {
                    DataObject dataObject = DataObject.find(fo);
                    assert dataObject != null: ("cannot find DataObject for " + fo.getPath());
                    if (dataObject != null) {
                        dataObject.delete();
                    }
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.USER, ioe);
                }
            }
        }
    }
}
