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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.impl.projects.J2eeDeploymentLookup;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

import org.openide.util.actions.SystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.nodes.Sheet;

import org.netbeans.modules.j2ee.deployment.execution.ServerExecSupport;
import org.netbeans.modules.j2ee.deployment.impl.projects.JSPServletFinderImpl;
import org.netbeans.modules.j2ee.deployment.config.ConfigSupportImpl;

/**
 *
 * @author  Pavel Buzek
 */
public abstract class J2eeModuleProvider {
    
    public abstract J2eeModule getJ2eeModule ();
// settings will be stored in filesystems attributes of {@link getModuleFolder}
//    public abstract Context getContext ();
    public abstract ModuleChangeReporter getModuleChangeReporter ();
    
    /** A folder that contains the module sources.
     * Module needs to provide ModuleFolderCookie on this folder.
     */
    public abstract FileObject getModuleFolder ();
    
    public boolean useDirectoryPath() {
        return true;
    }
    
    /** This object must be added by modules into project lookup. */
    public static final Object createJ2eeProjectMarker (J2eeModuleProvider provider) {
        return new J2eeDeploymentLookup (provider);
    }
    
    /**
     * Returns execution support for the given DataObject.
     */
    public static org.openide.loaders.ExecutionSupport getExecutionSupport (DataObject dobj) {
        return new ServerExecSupport (((MultiDataObject) dobj).getPrimaryEntry ());
    }

    /** Returns JSPServletFinder for the project that contains given file.
     * @return null if the file is not in any project
     */
    public static JSPServletFinder getJSPServletFinder(FileObject f) {
        Project prj = FileOwnerQuery.getOwner (f);
        return prj == null ? null : new JSPServletFinderImpl(prj);
    }
    
    /**
     * Returns configuration support for the given project.
     */
    public static final ConfigSupport getConfigSupport(Project prj) {
        J2eeDeploymentLookup deployment = (J2eeDeploymentLookup) prj.getLookup ().lookup (J2eeDeploymentLookup.class);
        return new ConfigSupportImpl(deployment);
    }
    
    /**
     * Configuration support to allow development module code to access well-known 
     * configuration propeties, such as web context root, cmp mapping info...
     */
    public static interface ConfigSupport {
        public void setWebContextRoot(String contextRoot);
        public String getWebContextRoot();
    }
    
    public static interface ModuleFolderCookie extends Node.Cookie {
        public J2eeModule getJ2eeModule();
    }
}
