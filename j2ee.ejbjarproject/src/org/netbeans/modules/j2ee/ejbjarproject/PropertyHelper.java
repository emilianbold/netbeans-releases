/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;

/**
 * A helper class to easily read and write properties. 
 * @author Andrei Badea
 */
public final class PropertyHelper {
    // TODO: AB: maybe in the future add methods to evaluate properties
    
    private Project project;
    private UpdateHelper updateHelper;
    
    /** Creates a new instance of PropertyHelper */
    public PropertyHelper(Project project, UpdateHelper updateHelper) {
        this.project = project;
        this.updateHelper = updateHelper;
    }
    
    /** 
     * Returns the property as a string
     * @param path the property path
     * @param key the property name
     * @returns the property value or null if it was not defined
     */
    public String getProperty(String path, String key) {
        return getProperties(path).getProperty(key);
    }
    
    /** 
     * Sets the property as a string. It puts the changed properties to
     * the UpdateHelper. The caller should hold ProjectManager.mutex() write access.
     * @param path the property path
     * @param key the property name
     * @param value the property value
     */
    public void setProperty(String path, String key, String value) {
        assert ProjectManager.mutex().isWriteAccess() : "You must have write access to ProjectManager.mutex(). You can also use @see #saveProperty if you only need to save one property";
        
        EditableProperties ep = getProperties(path);
        ep.setProperty(key, value);
        putProperties(path, ep);
    }
    
    /** 
     * Convenience method to set a single property and save the project.
     * Acquires ProjectManager.mutex() write access.
     * @param path the property path
     * @param key the property name
     * @param value the property value
     */
    public void saveProperty(final String path, final String key, final String value) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                setProperty(path, key, value);
                save();
            }
        });
    }
    
    /** 
     * Convenience method to save the project.
     */
    public void save() {
        assert ProjectManager.mutex().isWriteAccess() : "You must have write access to ProjectManager.mutex().";
        
        try {
            ProjectManager.getDefault().saveProject(project);
        }
        catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private EditableProperties getProperties(String path) {
        return updateHelper.getProperties(path);
    }
    
    private void putProperties(String path, EditableProperties ep) {
        updateHelper.putProperties(path, ep);
    }
}
