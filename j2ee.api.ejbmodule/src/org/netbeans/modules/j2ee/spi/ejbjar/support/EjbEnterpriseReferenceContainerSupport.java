/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.spi.ejbjar.support;

import java.io.File;
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
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;


/** Default implementation of {@link org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer}.
 *
 * @author Chris Webster
 */
public final class EjbEnterpriseReferenceContainerSupport {

    private EjbEnterpriseReferenceContainerSupport() {
    }
    
    public static EnterpriseReferenceContainer createEnterpriseReferenceContainer (Project project, AntProjectHelper helper) {
        return new ERC (project, helper);
    }
    
    private static class ERC extends EnterpriseReferenceContainer {

        private Project ejbProject;
        private AntProjectHelper antHelper;
        private static final String SERVICE_LOCATOR_PROPERTY = "project.serviceLocator.class"; //NOI18N

        private ERC (Project p, AntProjectHelper helper) {
            ejbProject = p;
            antHelper = helper;
        }

        public String addEjbLocalReference(EjbLocalRef localRef, String referencedClassName, AntArtifact target) throws java.io.IOException {
            return addReference(localRef, referencedClassName, target);
        }

        public String addEjbReferernce(EjbRef ref, String referencedClassName, AntArtifact target) throws IOException {
             return addReference(ref, referencedClassName, target);
        }

        private String addReference(Object ref, String referencedClassName, AntArtifact target) throws IOException {
             ReferenceHelper helper= (ReferenceHelper)
                ejbProject.getLookup().lookup(ReferenceHelper.class);
             Ejb model = findEjbForClass(referencedClassName);
             // XXX: target may be null (for example for a freeform project which doesn't have jar outputs set)         
             // that's the reason of the check for target == null
             boolean fromSameProject = (target == null || ejbProject.equals(target.getProject()));
             if (model == null) {
                 if (ref instanceof EjbRef) {
                     return ((EjbRef) ref).getEjbRefName();
                 } else {
                     return ((EjbLocalRef) ref).getEjbRefName();
                 }
             }
             String refName = null;
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

             if(!fromSameProject) {
                 try {
                     ProjectClassPathExtender pcpe = (ProjectClassPathExtender) ejbProject.getLookup().lookup(ProjectClassPathExtender.class);
                     assert pcpe != null;
                     pcpe.addAntArtifact(target, target.getArtifactLocations()[0]);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
             }

            writeDD();
            ProjectManager.getDefault().saveProject(ejbProject);
            return refName;
        }

        public String getServiceLocatorName() {
            EditableProperties ep =
                        antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            return ep.getProperty(SERVICE_LOCATOR_PROPERTY);
        }

        public void setServiceLocatorName(String serviceLocator) throws IOException {
             EditableProperties ep =
                        antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
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
            EnterpriseBeans beans = dd.getEnterpriseBeans();
            Ejb ejb = null;
            if (beans != null) {
                ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.SESSION,
                                                 Ejb.EJB_CLASS,
                                                 className);
                if (ejb == null) {
                    ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.ENTITY,
                                                 Ejb.EJB_CLASS,
                                                 className);
                }

                if (ejb == null) {
                    ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.MESSAGE_DRIVEN,
                                                 Ejb.EJB_CLASS,
                                                 className);
                }
            }
            return ejb;
        }

        private EjbJar findDD() throws IOException {
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJars [] = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(ejbProject);
            assert ejbJars.length > 0;
            return DDProvider.getDefault().getDDRoot(ejbJars[0].getDeploymentDescriptor());
        }

        private void writeDD() throws IOException {
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJars [] = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(ejbProject);
            assert ejbJars.length > 0;
            DDProvider.getDefault().getDDRoot(ejbJars[0].getDeploymentDescriptor()).write(ejbJars[0].getDeploymentDescriptor());
        }

        public String addResourceRef(ResourceRef ref, String referencingClass) throws IOException {
            Ejb ejb = findEjbForClass(referencingClass);
            if (ejb == null) {
                return ref.getResRefName();
            }
            String resourceRefName = getUniqueName(ejb, Ejb.RESOURCE_REF, 
                                                   org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_REF_NAME,
                                                   ref.getResRefName());
            if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
                if (!isJdbcConnectionAlreadyUsed(ref)) {
                    ref.setResRefName(resourceRefName);
                    ejb.addResourceRef((org.netbeans.modules.j2ee.dd.api.common.ResourceRef)ref);
                    writeDD();
                }
            } else {
                ref.setResRefName(resourceRefName);
                ejb.addResourceRef((org.netbeans.modules.j2ee.dd.api.common.ResourceRef)ref);
                writeDD();
            }
            return resourceRefName;
        }

        private boolean isJdbcConnectionAlreadyUsed(ResourceRef ref) throws IOException {
            EjbJar fullDD = findDD();
            assert fullDD.getEnterpriseBeans() != null;
            Ejb[] ejbs = fullDD.getEnterpriseBeans().getEjbs();
            assert ejbs != null;
            for (int bean = 0; bean < ejbs.length; bean++) {
                // see if jdbc resource has already been used in the app
                // this change requested by Ludo
                if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
                    ResourceRef[] refs = ejbs[bean].getResourceRef();
                    for (int i=0; i < refs.length; i++) {
                        if (javax.sql.DataSource.class.getName().equals(refs[i].getResType()) &&
                                ref.getDefaultDescription().equals(refs[i].getDefaultDescription()) && 
                                    ref.getResRefName().equals(refs[i].getResRefName())) {
                            return true;
                        }
                    }
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
                    ref = (ResourceRef) findDD().createBean("ResourceRef");
                } catch (ClassNotFoundException cnfe) {
                    IOException ioe = new IOException();
                    ioe.initCause(cnfe);
                    throw ioe;
                }
            }
            return ref;
        }

        public String addDestinationRef(MessageDestinationRef ref, String referencingClass) throws IOException {
            Ejb ejb = findEjbForClass(referencingClass);
            if (ejb == null) {
                return ref.getMessageDestinationRefName();
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
}
