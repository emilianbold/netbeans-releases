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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceSupport;
import org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
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
import org.openide.util.Exceptions;

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
    
    public String addEjbLocalReference(EjbReference localRef, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        return addReference(localRef, ejbRefName, true, referencingFile, referencingClass);
    }
    
    public String addEjbReference(EjbReference ref, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        return addReference(ref, ejbRefName, false, referencingFile, referencingClass);
    }
    
    private String addReference(final EjbReference ejbReference, String ejbRefName, boolean local, FileObject referencingFile, String referencingClass) throws IOException {
        String refName = null;
        WebApp webApp = getWebApp();
        
        MetadataModel<EjbJarMetadata> ejbReferenceMetadataModel = ejbReference.getEjbModule().getMetadataModel();
        final String[] ejbName = new String[1];
        FileObject ejbReferenceEjbClassFO = ejbReferenceMetadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
            public FileObject run(EjbJarMetadata metadata) throws Exception {
                ejbName[1] = metadata.findByEjbClass(ejbReference.getEjbClass()).getEjbName();
                return metadata.findResource(ejbReference.getEjbClass().replace('.', '/') + ".java");
            }
        });

        Project project = FileOwnerQuery.getOwner(ejbReferenceEjbClassFO);
        AntArtifact[] antArtifacts = AntArtifactQuery.findArtifactsByType(project, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        boolean hasArtifact = (antArtifacts != null && antArtifacts.length > 0);
        final AntArtifact moduleJarTarget = hasArtifact ? antArtifacts[0] : null;
        // only first URI is taken, if more of them are defined, just first one is taken
        String[] names = new String[] { "" };
        if (moduleJarTarget != null) {
            names = moduleJarTarget.getArtifactLocations()[0].getPath().split("/");  //NOI18N
        }

        String jarName = names[names.length - 1] + "#";
        final String ejbLink = jarName + ejbName[1];
        
        if (local) {
            refName = getUniqueName(getWebApp(), "EjbLocalRef", "EjbRefName", ejbRefName); //NOI18N
            // EjbLocalRef can come from Ejb project
            try {
                EjbLocalRef newRef = (EjbLocalRef)webApp.createBean("EjbLocalRef"); //NOI18N
                newRef.setEjbLink(ejbLink);
                newRef.setEjbRefName(refName);
                newRef.setEjbRefType(ejbReference.getEjbRefType());
                newRef.setLocal(ejbReference.getLocal());
                newRef.setLocalHome(ejbReference.getLocalHome());
                getWebApp().addEjbLocalRef(newRef);
            } catch (ClassNotFoundException ex){}
        } else {
            refName = getUniqueName(getWebApp(), "EjbRef", "EjbRefName", ejbRefName); //NOI18N
            // EjbRef can come from Ejb project
            try {
                EjbRef newRef = (EjbRef)webApp.createBean("EjbRef"); //NOI18N
                newRef.setEjbRefName(refName);
                newRef.setEjbRefType(ejbReference.getEjbRefType());
                newRef.setHome(ejbReference.getRemoteHome());
                newRef.setRemote(ejbReference.getRemote());
                getWebApp().addEjbRef(newRef);
            } catch (ClassNotFoundException ex){}
        }
        
        WebProjectClassPathExtender cpExtender = (WebProjectClassPathExtender) webProject.getLookup().lookup(WebProjectClassPathExtender.class);
        if (cpExtender != null) {
            try {
                AntArtifactChooser.ArtifactItem artifactItems[] = new AntArtifactChooser.ArtifactItem [1];
                artifactItems[0] = new AntArtifactChooser.ArtifactItem(moduleJarTarget, moduleJarTarget.getArtifactLocation());
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
    
    private void writeDD(FileObject referencingFile, final String referencingClass) throws IOException {
        ClassPathProviderImpl cppImpl = webProject.getLookup().lookup(ClassPathProviderImpl.class);
        ClasspathInfo classpathInfo = ClasspathInfo.create(
            cppImpl.getProjectSourcesClassPath(ClassPath.BOOT), 
            cppImpl.getProjectSourcesClassPath(ClassPath.COMPILE), 
            cppImpl.getProjectSourcesClassPath(ClassPath.SOURCE) 
        );
        JavaSource javaSource = JavaSource.create(classpathInfo, Collections.<FileObject>emptyList());
        WebModuleImplementation jp = (WebModuleImplementation) webProject.getLookup().lookup(WebModuleImplementation.class);
        
        // test if referencing class is injection target
        final boolean[] isInjectionTarget = {false};
        CancellableTask task = new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    Elements elements = controller.getElements();
                    TypeElement thisElement = elements.getTypeElement(referencingClass);
                    if (thisElement!=null)
                        isInjectionTarget[0] = InjectionTargetQuery.isInjectionTarget(controller, thisElement);
                }
                public void cancel() {}
        };
        JavaSource refFile = JavaSource.forFileObject(referencingFile);
        if (refFile!=null) {
            refFile.runUserActionTask(task, true);
        }
        
        boolean shouldWrite = isDescriptorMandatory(jp.getJ2eePlatformVersion()) || !isInjectionTarget[0];
        if (shouldWrite) {
            FileObject fo = jp.getDeploymentDescriptor();
            getWebApp().write(fo);
        }
    }
    
    public String addResourceRef(ResourceReference ref, FileObject referencingFile, String referencingClass) throws IOException {
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
            ResourceRef resourceRef = createResourceRef();
            EnterpriseReferenceSupport.populate(ref, resourceRefName, resourceRef);
            wa.addResourceRef(resourceRef);
            writeDD(referencingFile, referencingClass);
        }
        return resourceRefName;
    }
    
    public String addDestinationRef(MessageDestinationReference ref, FileObject referencingFile, String referencingClass) throws IOException {
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

        MessageDestinationRef messageDestinationRef = createDestinationRef();
        EnterpriseReferenceSupport.populate(ref, refName, messageDestinationRef);
        try {
            getWebApp().addMessageDestinationRef(messageDestinationRef);
            writeDD(referencingFile, referencingClass);
        } catch (VersionNotSupportedException ex){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return refName;
    }
    
    public ResourceRef createResourceRef() throws IOException {
        try {
            return (ResourceRef) getWebApp().createBean("ResourceRef");
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
 
    public MessageDestinationRef createDestinationRef() throws IOException {
        try {
            return (org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) getWebApp().createBean("MessageDestinationRef");
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
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
    private static boolean isResourceRefUsed(WebApp webApp, ResourceReference resRef) {
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
