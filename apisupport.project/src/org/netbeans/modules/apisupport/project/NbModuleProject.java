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

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A NetBeans module project.
 * @author Jesse Glick
 */
final class NbModuleProject implements Project {
    
    private static final URL BUILD_XSL = NbModuleProject.class.getResource("resources/build.xsl");
    private static final URL BUILD_IMPL_XSL = NbModuleProject.class.getResource("resources/build-impl.xsl");
    
    private final AntProjectHelper helper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    
    NbModuleProject(AntProjectHelper helper) {
        this.helper = helper;
        genFilesHelper = new GeneratedFilesHelper(helper);
        lookup = Lookups.fixed(new Object[] {
            helper.createExtensibleMetadataProvider(),
            new SavedHook(),
            new OpenedHook(),
            createActionProvider(),
            // XXX need, in rough descending order of importance:
            // ClassPathProvider
            // SourceForBinaryQueryImplementation
            // LogicalViewProvider
            // SubprojectProvider - special impl
            // AntArtifactProvider - should it run netbeans target, or all-foo/bar?
            // CustomizerProvider - ???
        });
    }
    
    public String getName() {
        return helper.getName();
    }
    
    public String getDisplayName() {
        // XXX look up localizing bundle
        return getName();
    }
    
    public String toString() {
        return "NbModuleProject[" + getName() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
    
    private boolean supportsJavadoc() {
        return supportsFeature("javadoc"); // NOI18N
    }
    
    private boolean supportsUnitTests() {
        return supportsFeature("unit-tests"); // NOI18N
    }
    
    private boolean supportsFeature(String name) {
        Element config = helper.getPrimaryConfigurationData(true);
        NodeList nl = config.getElementsByTagNameNS(NbModuleProjectType.NAMESPACE_SHARED, name);
        int length = nl.getLength();
        assert length < 2;
        return length == 1;
    }
    
    private ActionProvider createActionProvider() {
        Map/*<String,String[]>*/ commands = new HashMap();
        commands.put(ActionProvider.COMMAND_BUILD, new String[] {"netbeans"}); // NOI18N
        commands.put(ActionProvider.COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(ActionProvider.COMMAND_REBUILD, new String[] {"clean", "netbeans"}); // NOI18N
        if (supportsJavadoc()) {
            commands.put("javadoc", new String[] {"javadoc-nb"}); // NOI18N
        }
        if (supportsUnitTests()) {
            commands.put("test", new String[] {"test"}); // NOI18N
        }
        // XXX other commands, e.g. reload, nbm, *single*, ...
        return helper.createActionProvider(commands, /*XXX*/null);
    }
    
    private final class SavedHook extends ProjectXmlSavedHook {
        
        SavedHook() {}
        
        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                BUILD_IMPL_XSL, false);
            genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_XML_PATH,
                BUILD_XSL, false);
        }
        
    }
    
    private final class OpenedHook extends ProjectOpenedHook {
        
        OpenedHook() {}
        
        protected void projectOpened() {
            try {
                genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    BUILD_IMPL_XSL, true);
                genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_XML_PATH,
                    BUILD_XSL, true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
        protected void projectClosed() {
            // ignore for now
        }
        
    }
    
}
