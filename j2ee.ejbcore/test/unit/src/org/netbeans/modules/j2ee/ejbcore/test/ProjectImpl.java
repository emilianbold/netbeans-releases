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

package org.netbeans.modules.j2ee.ejbcore.test;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public final class ProjectImpl implements Project {
    
    private final Lookup lookup;
    private FileObject projectDirectory;
    
    public ProjectImpl(String moduleVersion, EnterpriseReferenceContainer erContainer) {
        lookup = Lookups.fixed(
                new J2eeModuleProviderImpl(moduleVersion),
                new SourcesImpl(),
                erContainer,
                new EjbJarsInProjectImpl()
                );
    }
    
    public FileObject getProjectDirectory() {
        return projectDirectory;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public void setProjectDirectory(FileObject fileObject) {
        this.projectDirectory = fileObject;
    }
    
    private class SourcesImpl implements Sources {
        
        public SourcesImpl() {}
        
        public SourceGroup[] getSourceGroups(String type) {
            return new SourceGroup[] { new SourceGroupImpl() };
        }
        
        public void addChangeListener(ChangeListener listener) {
        }
        
        public void removeChangeListener(ChangeListener listener) {
        }
        
    }
    
    private class SourceGroupImpl implements SourceGroup {
        
        public SourceGroupImpl() {}
        
        public FileObject getRootFolder() {
            return projectDirectory.getFileObject("src").getFileObject("java");
        }
        
        public String getName() {
            return "Sources";
        }
        
        public String getDisplayName() {
            return "Sources";
        }
        
        public Icon getIcon(boolean opened) {
            return null;
        }
        
        public boolean contains(FileObject file) {
            return FileUtil.isParentOf(projectDirectory, file);
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
    }
    
    private static class J2eeModuleProviderImpl extends J2eeModuleProvider {
        
        private final String moduleVersion;
        
        public J2eeModuleProviderImpl(String moduleVersion) {
            this.moduleVersion = moduleVersion;
        }
        
        public J2eeModule getJ2eeModule() {
            J2eeModuleImplementation j2eeModuleImpl = new J2eeModuleImpl(moduleVersion);
            return J2eeModuleFactory.createJ2eeModule(j2eeModuleImpl);
        }
        
        public ModuleChangeReporter getModuleChangeReporter() {
            return null;
        }
        
        public File getDeploymentConfigurationFile(String name) {
            return null;
        }
        
        public FileObject findDeploymentConfigurationFile(String name) {
            return null;
        }
        
        public void setServerInstanceID(String severInstanceID) {
        }
        
        public String getServerInstanceID() {
            return null;
        }
        
        public String getServerID() {
            return null;
        }
        
    }
    
    private static class J2eeModuleImpl implements J2eeModuleImplementation {
        
        private final String moduleVersion;
        
        public J2eeModuleImpl(String moduleVersion) {
            this.moduleVersion = moduleVersion;
        }
        
        public String getModuleVersion() {
            return moduleVersion;
        }
        
        public Object getModuleType() {
            return J2eeModule.EJB;
        }
        
        public String getUrl() {
            return null;
        }
        
        public void setUrl(String url) {
        }
        
        public FileObject getArchive() throws IOException {
            return null;
        }
        
        public Iterator getArchiveContents() throws IOException {
            return null;
        }
        
        public FileObject getContentDirectory() throws IOException {
            return null;
        }

        public File getResourceDirectory() {
            return null;
        }

        public File getDeploymentConfigurationFile(String name) {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
            return null;
        }
        
    }
    
    private class EjbJarsInProjectImpl implements EjbJarsInProject {
        
        public EjbJarsInProjectImpl() {}
        
        public EjbJar[] getEjbJars() {
            return new EjbJar[] { EjbJar.getEjbJar(projectDirectory) };
        }
    
    }
    
}
