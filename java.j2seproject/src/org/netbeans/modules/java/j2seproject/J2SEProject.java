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

package org.netbeans.modules.java.j2seproject;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.java.j2seproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.j2seproject.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.java.j2seproject.queries.JavadocForBinaryQueryImpl;
import org.netbeans.modules.java.j2seproject.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.java.j2seproject.ui.J2SECustomizerProvider;
import org.netbeans.modules.java.j2seproject.ui.J2SEPhysicalViewProvider;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.SourceContainers;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Represents one plain J2SE project.
 * @author Jesse Glick, et al.
 */
final class J2SEProject implements Project, AntProjectListener {
    
    private static final Image J2SE_PROJECT_ICON = Utilities.loadImage( "org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.gif" ); // NOI18N
        
    private final AntProjectHelper helper;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final PropertyChangeSupport pcs;
    
    J2SEProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux);
        genFilesHelper = new GeneratedFilesHelper(helper);
        lookup = createLookup(aux);
        pcs = new PropertyChangeSupport(this);
        helper.addAntProjectListener(this);
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String getName() {
        return helper.getName();
    }

    public String getDisplayName() {
        return helper.getDisplayName();
    }

    public String toString() {
        return "J2SEProject[" + getName() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(new String[] {
            "${src.dir}/*.java", // NOI18N
            "${test.src.dir}/*.java", // NOI18N
        }, new String[] {
            "${build.classes.dir}/*.class", // NOI18N
            "${build.test.classes.dir}/*.class", // NOI18N
        });
        return Lookups.fixed(new Object[] {
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new J2SEActionProvider( this, helper ),
            new J2SEPhysicalViewProvider(this, helper, spp),
            new J2SECustomizerProvider( this, helper, refHelper ),
            new ClassPathProviderImpl(helper),
            new CompiledSourceForBinaryQuery(helper),
            new JavadocForBinaryQueryImpl(helper),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new UnitTestForSourceQueryImpl(helper),
            SourceContainers.genericOnly( this ),
            fileBuilt,
        });
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            pcs.firePropertyChange(PROP_NAME, null, null);
            pcs.firePropertyChange(PROP_DISPLAY_NAME, null, null);
        }
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored
    }
    
    // Package private methods -------------------------------------------------
    
    FileObject getSourceDirectory() {
        String srcDir = helper.evaluate("src.dir"); // NOI18N
        return helper.resolveFileObject(srcDir);
    }
    
    FileObject getTestSourceDirectory() {
        String testSrcDir = helper.evaluate("test.src.dir"); // NOI18N
        return helper.resolveFileObject(testSrcDir);
    }
    
    File getTestClassesDirectory() {
        String testClassesDir = helper.evaluate("build.test.classes.dir"); // NOI18N
        return helper.resolveFile(testClassesDir);
    }
    
    public Image getIcon() {
        return J2SE_PROJECT_ICON;
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}
        
        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                J2SEProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_XML_PATH,
                J2SEProject.class.getResource("resources/build.xsl"),
                false);
        }
        
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            // Check up on build scripts.
            try {
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    J2SEProject.class.getResource("resources/build-impl.xsl"),
                    true);
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    J2SEProject.class.getResource("resources/build.xsl"),
                    true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // register project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(J2SEProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(J2SEProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(AntArtifact.TYPE_JAR, "dist.jar", "jar", "clean"), // NOI18N
            };
        }

    }
    
}
