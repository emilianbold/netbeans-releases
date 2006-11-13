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

package org.netbeans.modules.web.project;

import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.classpath.WebProjectClassPathExtender;
import org.netbeans.modules.web.project.ui.customizer.AntArtifactChooser;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 */
class WebContainerImpl implements EnterpriseReferenceContainer {
    
    private Project webProject;
    private AntProjectHelper antHelper;
    private static final String SERVICE_LOCATOR_PROPERTY = "project.serviceLocator.class"; //NOI18N
    private WebApp webApp;
    
    public WebContainerImpl(Project p, ReferenceHelper helper, AntProjectHelper antHelper) {
        webProject = p;
        this.antHelper = antHelper;
    }
    
    public String addEjbLocalReference(EjbLocalRef localRef, FileObject referencingFile, String referencingClass, AntArtifact target) throws IOException {
        return addReference(localRef, target, referencingFile, referencingClass);
    }
    
    public String addEjbReference(EjbRef ref, FileObject referencingFile, String referencingClass, AntArtifact target) throws IOException {
        return addReference(ref, target, referencingFile, referencingClass);
    }
    
    private String addReference(Object ref, AntArtifact target, FileObject referencingFile, String referencingClass) throws IOException {
        String refName = null;
        WebApp webApp = getWebApp();
        if (ref instanceof EjbRef) {
            EjbRef ejbRef = (EjbRef) ref;
            refName = getUniqueName(getWebApp(), "EjbRef", "EjbRefName", //NOI18N
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
                getWebApp().addEjbRef(newRef);
            } catch (ClassNotFoundException ex){}
        } else if (ref instanceof EjbLocalRef) {
            EjbLocalRef ejbRef = (EjbLocalRef) ref;
            refName = getUniqueName(getWebApp(), "EjbLocalRef", "EjbRefName", //NOI18N
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
                getWebApp().addEjbLocalRef(newRef);
            } catch (ClassNotFoundException ex){}
        }
        
        WebProjectClassPathExtender cpExtender = (WebProjectClassPathExtender) webProject.getLookup().lookup(WebProjectClassPathExtender.class);
        if (cpExtender != null) {
            try {
                AntArtifactChooser.ArtifactItem artifactItems[] = new AntArtifactChooser.ArtifactItem [1];
                artifactItems[0] = new AntArtifactChooser.ArtifactItem(target, target.getArtifactLocation());
                cpExtender.addAntArtifacts(WebProjectProperties.JAVAC_CLASSPATH, artifactItems, WebProjectProperties.TAG_WEB_MODULE_LIBRARIES);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        } else {
            ErrorManager.getDefault().log("WebProjectClassPathExtender not found in the project lookup of project: "+webProject.getProjectDirectory().getPath());    //NOI18N
        }
        
        writeDD(referencingFile, referencingClass);
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
        ProjectManager.getDefault().saveProject(webProject);
    }
    
    private WebApp getWebApp() throws IOException {
        if (webApp==null) {
            WebModuleImplementation jp = (WebModuleImplementation) webProject.getLookup().lookup(WebModuleImplementation.class);
            FileObject fo = jp.getDeploymentDescriptor();
            webApp = DDProvider.getDefault().getDDRoot(fo);
        }
        return webApp;
    }
    
    private void writeDD(FileObject referencingFile, String referencingClass) throws IOException {
        ClassPathProviderImpl cppImpl = webProject.getLookup().lookup(ClassPathProviderImpl.class);
        ClasspathInfo classpathInfo = ClasspathInfo.create(
            cppImpl.getProjectSourcesClassPath(ClassPath.BOOT), 
            cppImpl.getProjectSourcesClassPath(ClassPath.COMPILE), 
            cppImpl.getProjectSourcesClassPath(ClassPath.SOURCE) 
        );
        JavaSource javaSource = JavaSource.create(classpathInfo, Collections.<FileObject>emptyList());
        WebModuleImplementation jp = (WebModuleImplementation) webProject.getLookup().lookup(WebModuleImplementation.class);
        final boolean[] isInjectionTarget = {false};
        CancellableTask task = new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    
                }
                public void cancel() {}
        };
        
        boolean shouldWrite = isDescriptorMandatory(jp.getJ2eePlatformVersion());// || !InjectionTargetQuery.isInjectionTarget(referencingFile, referencingClass);
        if (shouldWrite) {
            FileObject fo = jp.getDeploymentDescriptor();
            getWebApp().write(fo);
        }
    }
    
    public String addResourceRef(ResourceRef ref, FileObject referencingFile, String referencingClass) throws IOException {
        WebApp wa = getWebApp();
        String resourceRefName = ref.getResRefName();
        // see if jdbc resource has already been used in the app
        // this change requested by Ludo
        if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
            ResourceRef[] refs = wa.getResourceRef();
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
        if (!isResourceRefUsed(wa, ref)) {
            resourceRefName = getUniqueName(wa, "ResourceRef", "ResRefName", ref.getResRefName()); //NOI18N
            ref.setResRefName(resourceRefName);
            wa.addResourceRef(ref);
            writeDD(referencingFile, referencingClass);
        }
        return resourceRefName;
    }
    
    public ResourceRef createResourceRef(String className) throws IOException {
        ResourceRef ref = null;
        try {
            ref = (ResourceRef) getWebApp().createBean("ResourceRef");
        } catch (ClassNotFoundException cnfe) {
            IOException ioe = new IOException();
            ioe.initCause(cnfe);
            throw ioe;
        }
        return ref;
    }
    
    private String getUniqueName(WebApp wa, String beanName,
            String property, String originalValue) {
        String proposedValue = originalValue;
        int index = 1;
        while (wa.findBeanByName(beanName, property, proposedValue) != null) {
            proposedValue = originalValue+Integer.toString(index++);
        }
        return proposedValue;
    }
    
    public String addDestinationRef(MessageDestinationRef ref, FileObject referencingFile, String referencingClass) throws IOException {
        try {
            // do not add if there is already an existing destination ref (see #85673)
            for (MessageDestinationRef mdRef : getWebApp().getMessageDestinationRef()){
                if (mdRef.getMessageDestinationRefName().equals(ref.getMessageDestinationRefName())){
                    return mdRef.getMessageDestinationRefName();
                }
            }
        } catch (VersionNotSupportedException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        String refName = getUniqueName(getWebApp(), "MessageDestinationRef", "MessageDestinationRefName", //NOI18N
                ref.getMessageDestinationRefName());
        
        ref.setMessageDestinationRefName(refName);
        try {
            getWebApp().addMessageDestinationRef(ref);
            writeDD(referencingFile, referencingClass);
        } catch (VersionNotSupportedException ex){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return refName;
    }
    
    public MessageDestinationRef createDestinationRef(String className) throws IOException {
        MessageDestinationRef ref = null;
        try {
            ref = (MessageDestinationRef) getWebApp().createBean("MessageDestinationRef");
        } catch (ClassNotFoundException cnfe) {
            IOException ioe = new IOException();
            ioe.initCause(cnfe);
            throw ioe;
        }
        return ref;
    }
    
    private static boolean isDescriptorMandatory(String j2eeVersion) {
        if ("1.3".equals(j2eeVersion) || "1.4".equals(j2eeVersion)) {
            return true;
        }
        return false;
    }
    
    /**
     * Searches for given resource reference in given web module.
     * Two resource references are considered equal if their names and types are equal.
     *
     * @param webApp web module where resource reference should be found
     * @param resRef resource reference to find
     * @return true id resource reference was found, false otherwise
     */
    private static boolean isResourceRefUsed(WebApp webApp, ResourceRef resRef) {
        String resRefName = resRef.getResRefName();
        String resRefType = resRef.getResType();
        for (ResourceRef existingRef : webApp.getResourceRef()) {
            if (resRefName.equals(existingRef.getResRefName()) && resRefType.equals(existingRef.getResType())) {
                return true;
            }
        }
        return false;
    }
    
}
