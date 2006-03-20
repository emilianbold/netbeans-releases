/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.api;

import java.net.URL;
import javax.swing.Icon;

import org.netbeans.modules.project.ui.ProjectInfoAccessor;

/**
 * Lite version of information about project.
 * @author Milan Kubec
 * @since 1.9.0
 */
public final class UnloadedProjectInformation {
    
    private String displayName;
    private Icon icon;
    private URL url;
    
    static {
        ProjectInfoAccessor.DEFAULT = new ProjectInfoAccessorImpl();
    }
    
    /**
     * Creates a new instance of UnloadedProjectInformation
     */
    UnloadedProjectInformation(String displayName, Icon icon, URL url) {
        this.displayName = displayName;
        this.icon = icon;
        this.url = url;
    }
    
    /**
     * Gets a human-readable display name for the project.
     * May contain spaces, international characters, etc.
     * @return a display name for the project
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets icon for given project.
     * Usually determined by the project type.
     * @return icon of the project.
     */
    public Icon getIcon() {
        return icon;
    }
    
    /**
     * Gets URL of the project folder location
     * Use {@link ProjectManager#findProject} to get the project
     * @return url of the project folder
     */
    public URL getURL() {
        return url;
    }
    
}
