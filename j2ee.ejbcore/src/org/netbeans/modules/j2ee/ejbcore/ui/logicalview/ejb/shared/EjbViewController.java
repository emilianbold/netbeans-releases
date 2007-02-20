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
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.dnd.EjbReferenceImpl;

/**
 * This class provides controller capabilities for ejb logical views. The nodes
 * should delegate non ui tasks to this class.
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class EjbViewController {
    
    private final Ejb ejb;
    private final EjbJar ejbJar;
    private final Project project;
    private final ClassPath classPath;
    
    public EjbViewController(Ejb ejb, EjbJar ejbJar, ClassPath classPath) {
        this.ejb = ejb;
        this.ejbJar = ejbJar;
        this.classPath = classPath;
        project = FileOwnerQuery.getOwner(classPath.getRoots()[0]);
    }
    
    public String getDisplayName() {
        String name = ejb.getDefaultDisplayName();
        if (name == null) {
            name = ejb.getEjbName();
        }
        return name;
    }
    
    public void delete(boolean deleteClasses) throws IOException {
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();
        deleteTraces();
        // for MDBs remove message destination from assembly descriptor
        if (ejb instanceof MessageDriven) {
            try {
                AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
                String mdLinkName = ((MessageDriven) ejb).getMessageDestinationLink();
                MessageDestination[] messageDestinations = assemblyDescriptor.getMessageDestination();
                for (MessageDestination messageDestination : messageDestinations) {
                    if (messageDestination.getMessageDestinationName().equals(mdLinkName)) {
                        assemblyDescriptor.removeMessageDestination(messageDestination);
                        break;
                    }
                }
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        beans.removeEjb(ejb);
        writeDD();
        if (deleteClasses) {
            deleteClasses();
        }
    }
    
    public EjbReference createEjbReference() {
        AntArtifact[] antArtifacts = AntArtifactQuery.findArtifactsByType(project, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        AntArtifact moduleJarTarget;
        if (antArtifacts == null || antArtifacts.length == 0) {
            moduleJarTarget = null;
        } else {
            moduleJarTarget = antArtifacts[0];
        }
        return new EjbReferenceImpl(moduleJarTarget, (EntityAndSession) ejb);
    }
    
    private FileObject findBeanFo() {
        return classPath.findResource(ejb.getEjbClass().replace('.','/')+".java"); // NOI18N
    }
    
    public DataObject getBeanDo() {
        FileObject src = findBeanFo();
        try {
            if (src != null) {
                return DataObject.find(src);
            }
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }
    
    public ElementHandle<TypeElement> getBeanClass() {
        try {
            JavaSource javaSource = JavaSource.forFileObject(findBeanFo());
            final List<ElementHandle<TypeElement>> result = new ArrayList<ElementHandle<TypeElement>>(1);
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController compilationController) throws IOException {
                    compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = compilationController.getElements().getTypeElement(ejb.getEjbClass());
                    result.add(ElementHandle.create(typeElement));
                }
            }, true);
            return result.get(0);
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
        assert ejb instanceof EntityAndSession;
        EntityAndSession refModel = (EntityAndSession) ejb;
        String result ="\t<ejb-ref>\n" +
                "\t\t<ejb-ref-name>ejb/" + ejb.getEjbName() + "</ejb-ref-name>\n"+
                "\t\t<ejb-ref-type>" + ejbType + "</ejb-ref-type>\n"+
                "\t\t<home>" + refModel.getHome() + "</home>\n"+
                "\t\t<remote>" + refModel.getRemote() + "</remote>\n"+
                "\t</ejb-ref>\n";
        return result;
    }
    
    public String getLocalStringRepresentation(String ejbType) {
        assert ejb instanceof EntityAndSession;
        EntityAndSession refModel = (EntityAndSession)ejb;
        String result ="\t<ejb-local-ref>\n" +
                "\t\t<ejb-ref-name>ejb/" + ejb.getEjbName() + "</ejb-ref-name>\n"+
                "\t\t<ejb-ref-type>" + ejbType + "</ejb-ref-type>\n"+
                "\t\t<local-home>" + refModel.getLocalHome() + "</local-home>\n"+
                "\t\t<local>" + refModel.getLocal() + "</local>\n"+
                "\t</ejb-local-ref>\n";
        return result;
    }
    
    
    private void writeDD() throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(classPath.getRoots()[0]);
        FileObject ddFile = ejbModule.getDeploymentDescriptor();
        DDProvider.getDefault().getMergedDDRoot(ejbModule.getMetadataUnit()).write(ddFile);
    }
    
    private boolean isEjbUsed(EjbRelationshipRole role, String ejbName) {
        return role != null &&
                role.getRelationshipRoleSource() != null &&
                ejbName.equals(role.getRelationshipRoleSource().getEjbName());
    }
    
    private void deleteRelationships(String ejbName) {
        Relationships relationships = ejbJar.getSingleRelationships();
        if (relationships != null) {
            EjbRelation[] relations = relationships.getEjbRelation();
            if (relations != null) {
                for (EjbRelation ejbRelation : relations) {
                    if (isEjbUsed(ejbRelation.getEjbRelationshipRole(), ejbName) || isEjbUsed(ejbRelation.getEjbRelationshipRole2(), ejbName)) {
                        relationships.removeEjbRelation(ejbRelation);
                    }
                }
                if (relationships.sizeEjbRelation() == 0) {
                    ejbJar.setRelationships(null);
                }
            }
        }
    }
    
    private void deleteTraces() {
        String ejbName = ejb.getEjbName();
        String ejbNameCompare = ejbName + "";
        deleteRelationships(ejbName);
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        if (assemblyDescriptor != null) {
            ContainerTransaction[] containerTransactions = assemblyDescriptor.getContainerTransaction();
            for (ContainerTransaction containerTransaction : containerTransactions) {
                Method[] methods = containerTransaction.getMethod();
                methods = methods == null ? new Method[0] : methods;
                for (Method method : methods) {
                    if (ejbNameCompare.equals(method.getEjbName())) {
                        containerTransaction.removeMethod(method);
                        if (containerTransaction.sizeMethod() == 0) {
                            assemblyDescriptor.removeContainerTransaction(containerTransaction);
                        }
                    }
                }
            }
            MethodPermission[] permissions = assemblyDescriptor.getMethodPermission();
            for (int i =0; i < permissions.length; i++) {
                Method[] methods = permissions[i].getMethod();
                methods = methods== null ? new Method[0]:methods;
                for (int method =0; method < methods.length; method++) {
                    if (ejbNameCompare.equals(methods[method].getEjbName())) {
                        permissions[i].removeMethod(methods[method]);
                        if (permissions[i].sizeMethod() == 0) {
                            assemblyDescriptor.removeMethodPermission(permissions[i]);
                        }
                    }
                }
            }
        }
    }
    
    private FileObject getFileObject(String className) {
        assert className != null: "cannot find null className";
        return classPath.findResource(className.replace('.', '/') + ".java");
    }
    
    private void deleteClasses() {
        ArrayList<FileObject> classFileObjects = new ArrayList<FileObject>();
        classFileObjects.add(getFileObject(ejb.getEjbClass()));
        if (ejb instanceof EntityAndSession) {
            EntityAndSession entityAndSessionfModel = (EntityAndSession) ejb;
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
        for (FileObject fileObject : classFileObjects) {
            if (fileObject != null) {
                try {
                    DataObject dataObject = DataObject.find(fileObject);
                    assert dataObject != null: ("cannot find DataObject for " + fileObject.getPath());
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
