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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.*;
import java.awt.Image;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.web.project.ui.J2SECustomizerProvider;
import org.netbeans.modules.web.project.ui.J2SEPhysicalViewProvider;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
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
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Represents one plain J2SE project.
 * @author Jesse Glick, et al.
 */
final class WebProject implements Project, AntProjectListener {
    
    private static final Image WEB_PROJECT_ICON = Utilities.loadImage( "org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif" ); // NOI18N
    private final AntProjectHelper helper;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final PropertyChangeSupport pcs;
    private final ProjectWebModule webModule;
    
    WebProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux);
        genFilesHelper = new GeneratedFilesHelper(helper);
        webModule = new ProjectWebModule (this, helper);
        lookup = createLookup(aux);
        pcs = new PropertyChangeSupport(this);
        helper.addAntProjectListener(this);
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public Image getIcon() {
        return WEB_PROJECT_ICON;
    }
        
    public String getName() {
        return helper.getName();
    }

    public String getDisplayName() {
        return helper.getDisplayName();
    }

    public String toString() {
        return "WebProject[" + getName() + "]"; // NOI18N
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
            webModule,
            J2eeModuleProvider.createJ2eeProjectMarker (webModule),
            new J2SEActionProvider( this, helper ),
            new J2SEPhysicalViewProvider(this, helper, spp),
            new J2SECustomizerProvider( this, helper, refHelper ),
            new ClassPathProviderImpl(helper),
            new CompiledSourceForBinaryQuery(helper),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
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
    
    ProjectWebModule getWebModule () {
        return webModule;
    }
    
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
    
    // Private innerclasses ----------------------------------------------------
    
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}
        
        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                WebProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_XML_PATH,
                WebProject.class.getResource("resources/build.xsl"),
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
                    WebProject.class.getResource("resources/build-impl.xsl"),
                    true);
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    WebProject.class.getResource("resources/build.xsl"),
                    true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            //check the config context path
            String ctxRoot = webModule.getContextPath ();
            if (ctxRoot == null || ctxRoot.equals ("")) {
                String sysName = "/" + getProjectDirectory ().getName (); //NOI18N
                sysName = sysName.replace (' ', '_'); //NOI18N
                webModule.setContextPath (sysName);
            }
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                // XXX probably this is nonsense:
                helper.createSimpleAntArtifact(AntArtifact.TYPE_JAR, "dist.jar", "jar", "clean"), // NOI18N
            };
        }

    }
    
}
