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

package org.netbeans.modules.j2ee.spi.ejbjar.support;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/** Default implementation of {@link org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer}.
 *
 * @author Chris Webster
 */
public final class EjbEnterpriseReferenceContainerSupport {
    
    private EjbEnterpriseReferenceContainerSupport() {
    }
    
    public static EnterpriseReferenceContainer createEnterpriseReferenceContainer(Project project, AntProjectHelper helper) {
        return new ERC(project, helper);
    }
    
    private static class ERC implements EnterpriseReferenceContainer {
        
        private Project ejbProject;
        private AntProjectHelper antHelper;
        private static final String SERVICE_LOCATOR_PROPERTY = "project.serviceLocator.class"; //NOI18N
        
        private ERC(Project p, AntProjectHelper helper) {
            ejbProject = p;
            antHelper = helper;
        }
        
        public String addEjbLocalReference(EjbLocalRef localRef, FileObject referencingFile, String referencingClass, AntArtifact target) throws IOException {
            return addReference(localRef, referencingFile, referencingClass, target);
        }
        
        public String addEjbReference(EjbRef ref, FileObject referencingFile, String referencingClass, AntArtifact target) throws IOException {
            return addReference(ref, referencingFile, referencingClass, target);
        }
        
        private String addReference(Object ref, FileObject referencingFile, String referencingClass, AntArtifact target) throws IOException {
            String refName = null;
            Ejb model = findEjbForClass(referencingClass);
            // XXX: target may be null (for example for a freeform project which doesn't have jar outputs set)
            // that's the reason of the check for target == null
            boolean fromSameProject = (target == null || ejbProject.equals(target.getProject()));
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJars [] = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(ejbProject);
            assert ejbJars.length > 0;
            // try to write to deployment descriptor anly if there is any
            // in case of metadata written in annotations, model shoud be updatet automaticaly
            if (ejbJars[0].getDeploymentDescriptor() != null) {
                if (model == null) {
                    if (ref instanceof EjbRef) {
                        return ((EjbRef) ref).getEjbRefName();
                    } else {
                        return ((EjbLocalRef) ref).getEjbRefName();
                    }
                }
                // XXX could use visitor here to remove conditional logic
                if (ref instanceof EjbRef) {
                    org.netbeans.modules.j2ee.dd.api.common.EjbRef ejbRef =
                            (org.netbeans.modules.j2ee.dd.api.common.EjbRef) ref;
                    refName = getUniqueName(model, Ejb.EJB_REF,
                            ejbRef.EJB_REF_NAME, ejbRef.getEjbRefName());
                    ejbRef.setEjbRefName(refName);
                    if (fromSameProject) {
                        ejbRef.setEjbLink(stripModuleName(ejbRef.getEjbLink()));
                    }
                    model.addEjbRef(ejbRef);
                } else {
                    org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef ejbRef =
                            (org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef) ref;
                    refName = getUniqueName(model, Ejb.EJB_LOCAL_REF,
                            ejbRef.EJB_REF_NAME, ejbRef.getEjbRefName());
                    ejbRef.setEjbRefName(refName);
                    if (fromSameProject) {
                        ejbRef.setEjbLink(stripModuleName(ejbRef.getEjbLink()));
                    }
                    model.addEjbLocalRef(ejbRef);
                }
                writeDD();
            }
            
            if(!fromSameProject) {
                try {
                    ProjectClassPathExtender pcpe = ejbProject.getLookup().lookup(ProjectClassPathExtender.class);
                    assert pcpe != null;
                    pcpe.addAntArtifact(target, target.getArtifactLocations()[0]);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
            
            ProjectManager.getDefault().saveProject(ejbProject);
            return refName;
        }
        
        public String getServiceLocatorName() {
            EditableProperties ep =
                    antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            return ep.getProperty(SERVICE_LOCATOR_PROPERTY);
        }
        
        public void setServiceLocatorName(String serviceLocator) throws IOException {
            EditableProperties ep = antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty(SERVICE_LOCATOR_PROPERTY, serviceLocator);
            antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            ProjectManager.getDefault().saveProject(ejbProject);
        }
        
        private String stripModuleName(String ejbLink) {
            int index = ejbLink.indexOf('#');
            return ejbLink.substring(index+1);
        }
        
        private Ejb findEjbForClass(String className) throws IOException {
            EjbJar dd = findDD();
            Ejb ejb = null;
            if (dd != null) {
                EnterpriseBeans beans = dd.getEnterpriseBeans();
                if (beans != null) {
                    ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.SESSION, Ejb.EJB_CLASS, className);
                    if (ejb == null) {
                        ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.ENTITY, Ejb.EJB_CLASS, className);
                    }
                    if (ejb == null) {
                        ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.MESSAGE_DRIVEN, Ejb.EJB_CLASS, className);
                    }
                }
            }
            return ejb;
        }
        
        private EjbJar findDD() throws IOException {
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJars [] = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(ejbProject);
            assert ejbJars.length > 0;
            return DDProvider.getDefault().getMergedDDRoot(ejbJars[0].getMetadataUnit());
        }
        
        private void writeDD() throws IOException {
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJars [] = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(ejbProject);
            assert ejbJars.length > 0;
            if (isDescriptorMandatory(ejbJars[0].getJ2eePlatformVersion())) {
                FileObject fo = ejbJars[0].getDeploymentDescriptor();
                if (fo != null){
                    DDProvider.getDefault().getMergedDDRoot(ejbJars[0].getMetadataUnit()).write(fo);
                }
            }
        }
        
        public String addResourceRef(ResourceRef ref, FileObject referencingFile, String referencingClass) throws IOException {
            Ejb ejb = findEjbForClass(referencingClass);
            if (ejb == null) {
                return ref.getResRefName();
            }
            String resourceRefName = ref.getResRefName();
            if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
                if (!isJdbcConnectionAlreadyUsed(ejb, ref)) {
                    resourceRefName = getUniqueName(ejb, Ejb.RESOURCE_REF, org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_REF_NAME, ref.getResRefName());
                    ref.setResRefName(resourceRefName);
                    ejb.addResourceRef((org.netbeans.modules.j2ee.dd.api.common.ResourceRef)ref);
                    writeDD();
                }
            } else {
                if (!isResourceRefUsed(ejb, ref)) {
                    resourceRefName = getUniqueName(ejb, Ejb.RESOURCE_REF, org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_REF_NAME, ref.getResRefName());
                    ref.setResRefName(resourceRefName);
                    ejb.addResourceRef((org.netbeans.modules.j2ee.dd.api.common.ResourceRef)ref);
                    writeDD();
                }
            }
            return resourceRefName;
        }
        
        private boolean isJdbcConnectionAlreadyUsed(Ejb ejb, ResourceRef ref) throws IOException {
            if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
                for (ResourceRef existingRef : ejb.getResourceRef()) {
                    String newDefaultDescription = ref.getDefaultDescription();
                    String existingDefaultDescription = existingRef.getDefaultDescription();
                    boolean canCompareDefDesc = (newDefaultDescription != null && existingDefaultDescription != null);
                    if (javax.sql.DataSource.class.getName().equals(existingRef.getResType()) &&
                            (canCompareDefDesc ? newDefaultDescription.equals(existingDefaultDescription) : true) &&
                            ref.getResRefName().equals(existingRef.getResRefName())) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        /**
         * Searches for given resource reference in given EJB.
         * Two resource references are considered equal if their names and types are equal.
         *
         * @param ejb EJB where resource reference should be found
         * @param resRef resource reference to find
         * @return true id resource reference was found, false otherwise
         */
        private static boolean isResourceRefUsed(Ejb ejb, ResourceRef resRef) {
            String resRefName = resRef.getResRefName();
            String resRefType = resRef.getResType();
            for (ResourceRef existingRef : ejb.getResourceRef()) {
                if (resRefName.equals(existingRef.getResRefName()) && resRefType.equals(existingRef.getResType())) {
                    return true;
                }
            }
            return false;
        }
        
        private String getUniqueName(Ejb bean, String beanName,
                String property, String originalValue) {
            String proposedValue = originalValue;
            int index = 1;
            while (bean.findBeanByName(beanName, property, proposedValue) != null) {
                proposedValue = originalValue+Integer.toString(index++);
            }
            return proposedValue;
        }
        
        public ResourceRef createResourceRef(String className) throws IOException {
            ResourceRef ref = null;
            Ejb ejb = findEjbForClass(className);
            if (ejb != null) {
                ref = ejb.newResourceRef();
            } else {
                try {
                    EjbJar ejbJar = findDD();
                    if (ejbJar != null) {
                        ref = (ResourceRef) ejbJar.createBean("ResourceRef");
                    } else {
                        ErrorManager.getDefault().log(ErrorManager.USER,
                                NbBundle.getMessage(EjbEnterpriseReferenceContainerSupport.class, "MSG_MissingMetadata"));
                    }
                } catch (ClassNotFoundException cnfe) {
                    IOException ioe = new IOException();
                    ioe.initCause(cnfe);
                    throw ioe;
                }
            }
            return ref;
        }
        
        public String addDestinationRef(MessageDestinationRef ref, FileObject referencingFile, String referencingClass) throws IOException {
            Ejb ejb = findEjbForClass(referencingClass);
            if (ejb == null) {
                return ref.getMessageDestinationRefName();
            }
            try {
                // do not add if there is already an existing destination ref (see #85673)
                for (MessageDestinationRef mdRef : ejb.getMessageDestinationRef()){
                    if (mdRef.getMessageDestinationRefName().equals(ref.getMessageDestinationRefName())){
                        return mdRef.getMessageDestinationRefName();
                    }
                }
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
            String destinationRefName = getUniqueName(ejb, Ejb.MESSAGE_DESTINATION_REF,
                    org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME,
                    ref.getMessageDestinationRefName());
            ref.setMessageDestinationRefName(destinationRefName);
            try {
                ejb.addMessageDestinationRef((org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef)ref);
            } catch (VersionNotSupportedException vnse) {
                // this exception should not be generated
            }
            writeDD();
            return destinationRefName;
        }
        
        public MessageDestinationRef createDestinationRef(String className) throws IOException {
            Ejb ejb = findEjbForClass(className);
            MessageDestinationRef ref = null;
            if (ejb != null) {
                try {
                    ref = ejb.newMessageDestinationRef();
                } catch (VersionNotSupportedException vnse) {
                    IOException ioe = new IOException();
                    ioe.initCause(vnse);
                    throw ioe;
                }
            } else {
                try {
                    ref = (MessageDestinationRef) findDD().createBean("MessageDestinationRef");
                } catch (ClassNotFoundException cnfe) {
                    IOException ioe = new IOException();
                    ioe.initCause(cnfe);
                    throw ioe;
                }
            }
            return ref;
        }
        
    }
    
    private static boolean isDescriptorMandatory(String j2eeVersion) {
        if ("1.3".equals(j2eeVersion) || "1.4".equals(j2eeVersion)) {
            return true;
        }
        return false;
    }
    
}
