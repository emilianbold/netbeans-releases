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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 * This class provides controller capabilities for ejb logical views. The nodes
 * should delegate non ui tasks to this class.
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class EjbViewController {
    
    private final String ejbClass;
    private final org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule;
    private String displayName;
    
    public EjbViewController(String ejbClass, org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule) {
        this.ejbClass = ejbClass;
        this.ejbModule = ejbModule;
    }
    
    public String getDisplayName() {
        if (displayName == null) {
            try {
                displayName = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                    public String run(EjbJarMetadata metadata) throws IOException {
                        Ejb ejb = metadata.findByEjbClass(ejbClass);
                        String name = ejb.getDefaultDisplayName();
                        if (name == null) {
                            name = ejb.getEjbName();
                        }
                        return name;
                    }
                });
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return displayName;
    }
    
    public void delete(boolean deleteClasses) throws IOException {
        
        boolean isEE5 = EjbProjectConstants.JAVA_EE_5_LEVEL.equals(ejbModule.getJ2eePlatformVersion());
        
        if (!isEE5) {
            ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                public Void run(EjbJarMetadata metadata) throws Exception {
                    EjbJar ejbJar = metadata.getRoot();
                    EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
                    // XXX get project (from EjbJar)
//                    J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
//                    j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();
//                    Ejb ejb = metadata.findByEjbClass(ejbClass);
//                    deleteTraces(ejb, ejbJar);
//                    // for MDBs remove message destination from assembly descriptor
//                    if (ejb instanceof MessageDriven) {
//                        try {
//                            AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
//                            String mdLinkName = ((MessageDriven) ejb).getMessageDestinationLink();
//                            MessageDestination[] messageDestinations = assemblyDescriptor.getMessageDestination();
//                            for (MessageDestination messageDestination : messageDestinations) {
//                                if (messageDestination.getMessageDestinationName().equals(mdLinkName)) {
//                                    assemblyDescriptor.removeMessageDestination(messageDestination);
//                                    break;
//                                }
//                            }
//                        } catch (VersionNotSupportedException ex) {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//                    beans.removeEjb(ejb);
                    return null;
                }
            });
            writeDD();
            if (deleteClasses) {
                deleteClasses();
            }
        } else {
            deleteClasses();
        }
    }
    
    public EjbReference createEjbReference() throws IOException {
        Map<String, String> ejbInfo = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Map<String, String>>() {
            public Map<String, String> run(EjbJarMetadata metadata) throws Exception {
                EntityAndSession ejb = (EntityAndSession) metadata.findByEjbClass(ejbClass);
                Map<String, String> result = new HashMap<String, String>();
                result.put(Ejb.EJB_NAME, ejb.getEjbName());
                result.put(EjbRef.EJB_REF_TYPE, ejb instanceof Entity ? EjbRef.EJB_REF_TYPE_ENTITY : EjbRef.EJB_REF_TYPE_SESSION);
                result.put(EntityAndSession.LOCAL, ejb.getLocal());
                result.put(EntityAndSession.LOCAL_HOME, ejb.getLocalHome());
                result.put(EntityAndSession.REMOTE, ejb.getRemote());
                result.put(EntityAndSession.HOME, ejb.getHome());
                return result;
            }
        });
        return EjbReference.create(
                ejbClass,
                ejbInfo.get(EjbRef.EJB_REF_TYPE),
                ejbInfo.get(EntityAndSession.LOCAL),
                ejbInfo.get(EntityAndSession.LOCAL_HOME),
                ejbInfo.get(EntityAndSession.REMOTE),
                ejbInfo.get(EntityAndSession.HOME),
                ejbModule
                );
    }
    
    public DataObject getBeanDo() {
        return getDataObject(ejbClass);
    }
    
    public DataObject getDataObject(String className) {
        FileObject src = findFileObject(className);
        try {
            if (src != null) {
                return DataObject.find(src);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public ElementHandle<TypeElement> getBeanClass() {
        FileObject fileObject = findFileObject(ejbClass);
        if (fileObject == null) {
            return null;
        }
        try {
            JavaSource javaSource = JavaSource.forFileObject(fileObject);
            final List<ElementHandle<TypeElement>> result = new ArrayList<ElementHandle<TypeElement>>(1);
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController compilationController) throws IOException {
                    compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = compilationController.getElements().getTypeElement(ejbClass);
                    result.add(ElementHandle.create(typeElement));
                }
            }, true);
            return result.get(0);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    /**
     * gets an ejb reference representation
     * @return the xml code corresponding to this ejb
     */
    public String getRemoteStringRepresentation(final String ejbType) {
        String result = null;
        try {
            result = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws IOException {
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    assert ejb instanceof EntityAndSession;
                    EntityAndSession refModel = (EntityAndSession) ejb;
                    return "\t<ejb-ref>\n" +
                            "\t\t<ejb-ref-name>ejb/" + ejb.getEjbName() + "</ejb-ref-name>\n"+
                            "\t\t<ejb-ref-type>" + ejbType + "</ejb-ref-type>\n"+
                            "\t\t<home>" + refModel.getHome() + "</home>\n"+
                            "\t\t<remote>" + refModel.getRemote() + "</remote>\n"+
                            "\t</ejb-ref>\n";
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result;
    }
    
    public String getLocalStringRepresentation(final String ejbType) {
        String result = null;
        try {
            result = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws IOException {
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    assert ejb instanceof EntityAndSession;
                    EntityAndSession refModel = (EntityAndSession) ejb;
                    return "\t<ejb-local-ref>\n" +
                            "\t\t<ejb-ref-name>ejb/" + ejb.getEjbName() + "</ejb-ref-name>\n"+
                            "\t\t<ejb-ref-type>" + ejbType + "</ejb-ref-type>\n"+
                            "\t\t<local-home>" + refModel.getLocalHome() + "</local-home>\n"+
                            "\t\t<local>" + refModel.getLocal() + "</local>\n"+
                            "\t</ejb-local-ref>\n";
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result;
    }
    
    
    private void writeDD() throws IOException {
        FileObject ddFile = ejbModule.getDeploymentDescriptor();
        DDProvider.getDefault().getDDRoot(ddFile).write(ddFile); // EJB 2.1
    }
    
    private boolean isEjbUsed(EjbRelationshipRole role, String ejbName) {
        return role != null &&
                role.getRelationshipRoleSource() != null &&
                ejbName.equals(role.getRelationshipRoleSource().getEjbName());
    }
    
    private void deleteRelationships(String ejbName, EjbJar ejbJar) {
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
    
    private void deleteTraces(Ejb ejb, EjbJar ejbJar) {
        String ejbName = ejb.getEjbName();
        String ejbNameCompare = ejbName + "";
        deleteRelationships(ejbName, ejbJar);
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
    
    private FileObject findFileObject(final String className) {
        FileObject beanFO = null;
        try {
            beanFO = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
                public FileObject run(EjbJarMetadata metadata) throws IOException {
                    return metadata.findResource(Utils.toResourceName(className));
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return beanFO;
    }
    
    private void deleteClasses() {
        final ArrayList<FileObject> classFileObjects = new ArrayList<FileObject>();
        
        try {
            ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                public Void run(EjbJarMetadata metadata) throws Exception {
                    classFileObjects.add(metadata.findResource(ejbClass));
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    if (ejb instanceof EntityAndSession) {
                        EntityAndSession entityAndSessionfModel = (EntityAndSession) ejb;
                        if (entityAndSessionfModel.getLocalHome() != null) {
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getLocalHome()));
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getLocal()));
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getLocal() + "Business"));
                        }
                        if (entityAndSessionfModel.getHome() != null) {
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getHome()));
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getRemote()));
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getRemote() + "Business"));
                        }
                    }
                    return null;
                }
            });

            for (FileObject fileObject : classFileObjects) {
                if (fileObject != null) {
                    DataObject dataObject = DataObject.find(fileObject);
                    assert dataObject != null: ("cannot find DataObject for " + fileObject.getPath());
                    if (dataObject != null) {
                        dataObject.delete();
                    }
                }
            }
            
        } catch (IOException ioe) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(ioe));
        }
    }
    
}
