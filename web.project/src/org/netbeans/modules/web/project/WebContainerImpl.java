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

package org.netbeans.modules.web.project;

import java.io.File;
import java.io.IOException;
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
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Chris Webster
 */
class WebContainerImpl extends EnterpriseReferenceContainer {
    private Project webProject;
    private ReferenceHelper helper;
    private AntProjectHelper antHelper;
    private static final String SERVICE_LOCATOR_PROPERTY = "project.serviceLocator.class"; //NOI18N
    private WebApp webApp;
    
    public WebContainerImpl(Project p, ReferenceHelper helper, AntProjectHelper antHelper) {
        webProject = p;
        this.helper = helper;
        this.antHelper = antHelper;
    }
    
    public String addEjbLocalReference(EjbLocalRef localRef, String referencedClassName, AntArtifact target) throws java.io.IOException {
        return addReference(localRef, target);
    }
    
    public String addEjbReferernce(EjbRef ref, String referencedClassName, AntArtifact target) throws IOException {
         return addReference(ref, target);
    }
    
    
    private String addReference(Object ref, AntArtifact target) throws IOException {
         String refName = null;
         if (ref instanceof EjbRef) {
            EjbRef ejbRef = (EjbRef) ref;
            refName = getUniqueName(getWebApp(), "EjbRef", "EjbRefName", 
                    ejbRef.getEjbRefName());
            ejbRef.setEjbRefName(refName);
            getWebApp().addEjbRef(ejbRef);
         } else {
            EjbLocalRef ejbRef = (EjbLocalRef) ref;
            refName = getUniqueName(getWebApp(), "EjbLocalRef", "EjbRefName", 
                    ejbRef.getEjbRefName());
            ejbRef.setEjbRefName(refName);
            getWebApp().addEjbLocalRef(ejbRef);
         }
         
         if(helper.addReference(target)) {
                EditableProperties ep =
                    antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String s = ep.getProperty(WebProjectProperties.JAVAC_CLASSPATH);
                s += File.pathSeparatorChar + helper.createForeignFileReference(target);
		ep.setProperty(WebProjectProperties.JAVAC_CLASSPATH, s);
                antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(webProject);
        }
         
        writeDD();
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
    
    private void writeDD() throws IOException {
        WebModuleImplementation jp = (WebModuleImplementation) webProject.getLookup().lookup(WebModuleImplementation.class);
        FileObject fo = jp.getDeploymentDescriptor();
        getWebApp().write(fo);
    }

    public String addResourceRef(ResourceRef ref, String referencingClass) throws IOException {
         String resourceRefName = getUniqueName(getWebApp(), "ResourceRef", "ResRefName", //NOI18N
                                               ref.getResRefName());
         // see if jdbc resource has already been used in the app
         // this change requested by Ludo
         if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
             WebApp wa = getWebApp();
             ResourceRef[] refs = wa.getResourceRef();
             for (int i=0; i < refs.length; i++) {
                 if (javax.sql.DataSource.class.getName().equals(refs[i].getResType()) &&
                     ref.getDefaultDescription().equals(refs[i].getDefaultDescription())) {
                     return refs[i].getResRefName();
                 }
             }
         }
         ref.setResRefName(resourceRefName);
         getWebApp().addResourceRef(ref);
         writeDD();
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

    public String addDestinationRef(MessageDestinationRef ref, String referencingClass) throws IOException {
        String refName = getUniqueName(getWebApp(), "MessageDestinationRef", "MessageDestinationRefName", //NOI18N
                                ref.getMessageDestinationRefName());
        ref.setMessageDestinationRefName(refName);
        try {
            getWebApp().addMessageDestinationRef(ref);
            writeDD();
        } catch (VersionNotSupportedException ex){}
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

}
