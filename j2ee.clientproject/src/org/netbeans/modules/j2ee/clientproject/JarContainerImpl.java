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

package org.netbeans.modules.j2ee.clientproject;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AntArtifactChooser;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author jungi
 */
public class JarContainerImpl implements EnterpriseReferenceContainer {
    
    private Project webProject;
    private AntProjectHelper antHelper;
    private static final String SERVICE_LOCATOR_PROPERTY = "project.serviceLocator.class"; //NOI18N
    private AppClient webApp;
    
    /** Creates a new instance of JarContainerImpl */
    public JarContainerImpl(Project p, ReferenceHelper helper, AntProjectHelper antHelper) {
        webProject = p;
        this.antHelper = antHelper;
    }
    
    /**
     * set name of service locator fo this project.
     *
     * @param serviceLocator used in this project
     */
    public void setServiceLocatorName(String serviceLocator) throws IOException {
        EditableProperties ep =
                antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(SERVICE_LOCATOR_PROPERTY, serviceLocator);
        antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(webProject);
    }
    
    /**
     * Create resource ref instance based on current project type.
     *
     * @param className to determine context from
     */
    public ResourceRef createResourceRef(String className) throws IOException {
        ResourceRef ref = null;
        try {
            ref = (ResourceRef) getAppClient().createBean("ResourceRef"); // NOI18N
        } catch (ClassNotFoundException cnfe) {
            IOException ioe = new IOException();
            ioe.initCause(cnfe);
            throw ioe;
        }
        return ref;
    }
    
    public MessageDestinationRef createDestinationRef(String className) throws IOException {
        MessageDestinationRef ref = null;
        try {
            ref = (MessageDestinationRef) getAppClient().createBean("MessageDestinationRef"); // NOI18N
        } catch (ClassNotFoundException cnfe) {
            IOException ioe = new IOException();
            ioe.initCause(cnfe);
            throw ioe;
        }
        return ref;
    }
    
    /**
     * Add given resource reference into the deployment descriptor.
     *
     * @param ref reference to resource used
     * @param referencingClass class which will use the resource
     * @return unique jndi name used in deployment descriptor
     */
    public String addResourceRef(ResourceRef ref, FileObject referencingFile, String referencingClass) throws IOException {
        String resourceRefName = ref.getResRefName();
        AppClient ac = getAppClient();
        // see if jdbc resource has already been used in the app
        // this change requested by Ludo
        if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
            ResourceRef[] refs = ac.getResourceRef();
            for (int i=0; i < refs.length; i++) {
                String newDefaultDescription = ref.getDefaultDescription();
                String existingDefaultDescription = refs[i].getDefaultDescription();
                boolean canCompareDefDesc = (newDefaultDescription != null && existingDefaultDescription != null);
                if (javax.sql.DataSource.class.getName().equals(refs[i].getResType()) &&
                        (canCompareDefDesc ? newDefaultDescription.equals(existingDefaultDescription) : true) &&
                        ref.getResRefName().equals(refs[i].getResRefName())) {
                    return refs[i].getResRefName();
                }
            }
        }
        if (!isResourceRefUsed(ac, ref)) {
            resourceRefName = getUniqueName(ac, "ResourceRef", "ResRefName", ref.getResRefName()); //NOI18N
            ref.setResRefName(resourceRefName);
            getAppClient().addResourceRef(ref);
            writeDD(referencingFile, referencingClass);
        }
        return resourceRefName;
    }
    
    /**
     *
     *
     * @see #addEjbReference(EjbRef, String, AntArtifact)
     */
    public String addEjbLocalReference(EjbLocalRef localRef, FileObject referencingFile, String referencedClassName, AntArtifact target) throws IOException {
        return addReference(localRef, referencingFile, referencedClassName, target);
    }
    
    /**
     * Add given ejb reference into deployment descriptor. This method should
     * also ensure that the supplied target is added to the class path (as the
     * ejb interfaces will be referenced from this class) as well as the
     * deployed manifest. The deployed manifest is the generic J2EE compliant
     * strategy, application server specific behavior such as delegating to the
     * parent class loader could also be used. The main point is not to
     * include the target in the deployed archive but instead reference the
     * interface jar (or standard ejb module) included in the J2EE application.
     *
     * @param ref -- ejb reference this will include the ejb link which assumes
     * root packaging in the containing application. The name of this ref should
     * be considered a hint and made unique within the deployment descriptor.
     * @param referencedClassName -- name of referenced class, this can be used
     * to determine where to add the deployment descriptor entry. This class
     * will be modified with a method or other strategy to obtain the ejb.
     * @param target to include in the build
     * @return actual jndi name used in deployment descriptor
     */
    public String addEjbReference(EjbRef ref, FileObject referencingFile, String referenceClassName, AntArtifact target) throws IOException {
        return addReference(ref, referencingFile, referenceClassName, target);
    }
    
    /**
     * Add given message destination reference into the deployment descriptor
     *
     * @param ref to destination
     * @param referencingClass class using the destination
     * @return unique jndi name used in the deployment descriptor
     */
    public String addDestinationRef(MessageDestinationRef ref, FileObject referencingFile, String referencingClass) throws IOException {
        String refName = getUniqueName(getAppClient(), "MessageDestinationRef", "MessageDestinationRefName", //NOI18N
                ref.getMessageDestinationRefName());
        ref.setMessageDestinationRefName(refName);
        try {
            getAppClient().addMessageDestinationRef(ref);
            writeDD(referencingFile, referencingClass);
        } catch (VersionNotSupportedException ex){}
        return refName;
    }
    
    /**
     *
     *
     * @return name of the service locator defined for this project or null
     * if service locator is not being used
     */
    public String getServiceLocatorName() {
        EditableProperties ep =
                antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        return ep.getProperty(SERVICE_LOCATOR_PROPERTY);
    }
    
    private AppClient getAppClient() throws IOException {
        if (webApp==null) {
            CarImplementation jp = (CarImplementation) webProject.getLookup().lookup(CarImplementation.class);
            FileObject fo = jp.getDeploymentDescriptor();
            webApp = DDProvider.getDefault().getDDRoot(fo);
        }
        return webApp;
    }
    
    private String getUniqueName(AppClient wa, String beanName,
            String property, String originalValue) {
        String proposedValue = originalValue;
        int index = 1;
        while (wa.findBeanByName(beanName, property, proposedValue) != null) {
            proposedValue = originalValue+Integer.toString(index++);
        }
        return proposedValue;
    }
    
    private void writeDD(FileObject referencingFile, final String referencingClassName) throws IOException {
        final CarImplementation jp = (CarImplementation) webProject.getLookup().lookup(CarImplementation.class);
        JavaSource javaSource = JavaSource.forFileObject(referencingFile);
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = controller.getElements().getTypeElement(referencingClassName);
                if (isDescriptorMandatory(jp.getJ2eePlatformVersion()) || 
                        !InjectionTargetQuery.isInjectionTarget(controller, typeElement)) {
                    FileObject fo = jp.getDeploymentDescriptor();
                    getAppClient().write(fo);
                }
            }
        }, true);
    }
    
    private String addReference(Object ref, FileObject referencingFile, String referencingClass, AntArtifact target) throws IOException {
        String refName = null;
        AppClient webApp = getAppClient();
        if (ref instanceof EjbRef) {
            EjbRef ejbRef = (EjbRef) ref;
            refName = getUniqueName(getAppClient(), "EjbRef", "EjbRefName", //NOI18N
                    ejbRef.getEjbRefName());
            ejbRef.setEjbRefName(refName);
            // EjbRef can come from Ejb project
            try {
                EjbRef newRef = (EjbRef)webApp.createBean("EjbRef"); //NOI18N
                try {
                    newRef.setAllDescriptions(ejbRef.getAllDescriptions());
                } catch (VersionNotSupportedException ex) {
                    newRef.setDescription(ejbRef.getDefaultDescription());
                }
                newRef.setEjbRefName(ejbRef.getEjbRefName());
                newRef.setEjbRefType(ejbRef.getEjbRefType());
                newRef.setHome(ejbRef.getHome());
                newRef.setRemote(ejbRef.getRemote());
                getAppClient().addEjbRef(newRef);
            } catch (ClassNotFoundException ex){}
        } else if (ref instanceof EjbLocalRef) {
            System.err.println("### UNSUPPORTED ###");
            /*
            EjbLocalRef ejbRef = (EjbLocalRef) ref;
            refName = getUniqueName(getAppClient(), "EjbLocalRef", "EjbRefName", //NOI18N
                    ejbRef.getEjbRefName());
            ejbRef.setEjbRefName(refName);
            // EjbLocalRef can come from Ejb project
            try {
                EjbLocalRef newRef = (EjbLocalRef)webApp.createBean("EjbLocalRef"); //NOI18N
                try {
                    newRef.setAllDescriptions(ejbRef.getAllDescriptions());
                } catch (VersionNotSupportedException ex) {
                    newRef.setDescription(ejbRef.getDefaultDescription());
                }
                newRef.setEjbLink(ejbRef.getEjbLink());
                newRef.setEjbRefName(ejbRef.getEjbRefName());
                newRef.setEjbRefType(ejbRef.getEjbRefType());
                newRef.setLocal(ejbRef.getLocal());
                newRef.setLocalHome(ejbRef.getLocalHome());
                getAppClient().addEjbLocalRef(newRef);
            } catch (ClassNotFoundException ex){}
             */
        }
        
        ProjectClassPathExtender cpExtender = (ProjectClassPathExtender) webProject.getLookup().lookup(ProjectClassPathExtender.class);
        if (cpExtender != null) {
            try {
                AntArtifactChooser.ArtifactItem artifactItems[] = new AntArtifactChooser.ArtifactItem [1];
                //artifactItems[0] = new AntArtifactChooser.ArtifactItem(target, target.getArtifactLocation());
                cpExtender.addAntArtifact(target, target.getArtifactLocations()[0].normalize());
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        } else {
            ErrorManager.getDefault().log("WebProjectClassPathExtender not found in the project lookup of project: "+webProject.getProjectDirectory().getPath());    //NOI18N
        }
        
        writeDD(referencingFile, referencingClass);
        return refName;
    }

    private static boolean isDescriptorMandatory(String j2eeVersion) {
        if ("1.3".equals(j2eeVersion) || "1.4".equals(j2eeVersion)) {
            return true;
        }
        return false;
    }
    
    /**
     * Searches for given resource reference in given client module.
     * Two resource references are considered equal if their names and types are equal.
     * 
     * @param ac client module where resource reference should be found
     * @param resRef resource reference to find
     * @return true id resource reference was found, false otherwise
     */
    private static boolean isResourceRefUsed(AppClient ac, ResourceRef resRef) {
        String resRefName = resRef.getResRefName();
        String resRefType = resRef.getResType();
        for (ResourceRef existingRef : ac.getResourceRef()) {
            if (resRefName.equals(existingRef.getResRefName()) && resRefType.equals(existingRef.getResType())) {
                return true;
            }
        }
        return false;
    }

}
