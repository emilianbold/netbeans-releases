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

package org.netbeans.api.project.ui;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * List of projects open in the GUI.
 * @author Jesse Glick, Petr Hrebejk
 */
public final class OpenProjects {
    
    
    /** Property open projects. */
    public static final String PROPERTY_OPEN_PROJECTS = "OpenProjects";
    
    private static OpenProjects INSTANCE = new OpenProjects();
    
    private OpenProjectsTrampoline trampoline;
    
    private OpenProjects() {
        this.trampoline = Utilities.getOpenProjectsTrampoline();
    }
           
    public static OpenProjects getDefault() {                
        return INSTANCE;
    }
    
    /** Gets array of currently opened projects.
     * @return Array of projects currently opened in the IDEGui
     */
    public Project[] getOpenProjects() {
        return trampoline.getOpenProjectsAPI();
    }
            
    /** Add property change listener on open projects/ 
     * As this class is singletnon, which is not GCed it is good idea to 
     * add WeakListeners or remove the listeners properly.
     * @param listener The listener to be added.
     */    
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        trampoline.addPropertyChangeListenerAPI( listener );
    }
    
    /** Removes property change listener. 
     * @param listener Listener to be removed. 
     */
    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        trampoline.removePropertyChangeListenerAPI( listener );
    }
    
}
