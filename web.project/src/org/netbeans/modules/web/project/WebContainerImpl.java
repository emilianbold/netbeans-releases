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
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.web.dd.DDProvider;
import org.netbeans.api.web.dd.EjbLocalRef;
import org.netbeans.api.web.dd.EjbRef;
import org.netbeans.api.web.dd.WebApp;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.web.project.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Chris Webster
 */
class WebContainerImpl extends EnterpriseReferenceContainer {
    private Project webProject;
    private ReferenceHelper helper;
    private AntProjectHelper antHelper;
    private static final String J2EE_14_LIBRARY = "j2ee14"; //NOI18N
    
    public WebContainerImpl(Project p, ReferenceHelper helper, AntProjectHelper antHelper) {
        webProject = p;
        this.helper = helper;
        this.antHelper = antHelper;
    }
    
    public void addEjbLocalReference(EjbLocalRef localRef, String referencedClassName, AntArtifact target) throws java.io.IOException {
        addReference(localRef, target);
    }
    
    public void addEjbReferernce(EjbRef ref, String referencedClassName, AntArtifact target) throws IOException {
         addReference(ref, target);
    }
    
    private void addReference(Object ref, AntArtifact target) throws IOException {
         BaseBean bb = findDD();
         // Using basebean here as the web dd implementation classes 
         // perform downcasting. Pavel / Milan can this be resolved
         // this idiom will be used for many other enterprise resources 
         if (ref instanceof EjbRef) {
            bb.addValue("EjbRef", ref);
         } else {
            bb.addValue("EjbLocalRef", ref);
         }
         
         if(helper.addReference(target)) {
                EditableProperties ep =
                    antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String s = ep.getProperty(WebProjectProperties.JAVAC_CLASSPATH);
                s += File.pathSeparatorChar + helper.createForeignFileReference(target);
		ep.setProperty(WebProjectProperties.JAVAC_CLASSPATH, s);
                antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
        addJ2eeLibrary();
         
        writeDD(bb);
    }
    
    public String getServiceLocatorName() {
        return null;
    }
    
    private void addJ2eeLibrary() {
        Library lib = LibraryManager.getDefault().getLibrary(J2EE_14_LIBRARY); 
        WebProjectProperties wp = new WebProjectProperties (webProject,antHelper,helper);
        List vcpis = (List) wp.get (WebProjectProperties.JAVAC_CLASSPATH);
        VisualClassPathItem vcpi = VisualClassPathItem.create (lib, VisualClassPathItem.PATH_IN_WAR_NONE);
        vcpis.add (vcpi);
        wp.put (WebProjectProperties.JAVAC_CLASSPATH, vcpis); 
        wp.store();
    }
    
    private BaseBean findDD() throws IOException {
        WebModuleImplementation jp = (WebModuleImplementation) webProject.getLookup().lookup(WebModuleImplementation.class);
        WebApp wa = DDProvider.getDefault().getDDRootCopy(jp.getDeploymentDescriptor());
        return DDProvider.getDefault().getBaseBean(wa);
    }
    
    private void writeDD(BaseBean bb) throws IOException {
        WebModuleImplementation jp = (WebModuleImplementation) webProject.getLookup().lookup(WebModuleImplementation.class);
        File f = FileUtil.toFile(jp.getDeploymentDescriptor());
        bb.write(f);
    }
}
