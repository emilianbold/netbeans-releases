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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi.api;

import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 *
 * @author jqian
 */
public class JbiProjectHelper {
    
    /** 
     * Gets the name of the given JBI project.
     * 
     * @param p 
     * @return 
     */
    public static String getJbiProjectName(Project p) {
        JbiProjectProperties props = ((JbiProject)p).getProjectProperties();
    
        String name = (String) props.get(JbiProjectProperties.SERVICE_ASSEMBLY_ID);
        if (name == null) { // for backward compatibility until project is updated
            name = (String) props.get(JbiProjectProperties.ASSEMBLY_UNIT_UUID);
        }
        return name;
    }
    
    /** 
     * Gets the description of the service assembly.
     * 
     * @param p 
     * @return 
     */
    public static String getServiceAssemblyDescription(Project p) {
        JbiProjectProperties props = ((JbiProject)p).getProjectProperties();
    
        String name = (String) props.get(JbiProjectProperties.SERVICE_ASSEMBLY_DESCRIPTION);
        if (name == null) { // for backward compatibility until project is updated
            name = (String) props.get(JbiProjectProperties.ASSEMBLY_UNIT_DESCRIPTION);
        }
        return name;
    }
    
    /** 
     * Gets the template description for service unit.
     * 
     * @param p 
     * @return 
     */
    public static String getServiceUnitDescription(Project p) {
        JbiProjectProperties props = ((JbiProject)p).getProjectProperties();
    
        String name = (String) props.get(JbiProjectProperties.SERVICE_UNIT_DESCRIPTION);
        if (name == null) { // for backward compatibility until project is updated
            name = (String) props.get(JbiProjectProperties.APPLICATION_SUB_ASSEMBLY_DESCRIPTION);
        }
        return name;
    }
    
    /** 
     * Sets the name of the given JBI project.
     * 
     * @param props 
     * @param name  
     */
    public static void setJbiProjectName(
            EditableProperties props, String name) {
        
        if (props.getProperty(JbiProjectProperties.SERVICE_ASSEMBLY_ID) != null) {
            props.setProperty(JbiProjectProperties.SERVICE_ASSEMBLY_ID, name);
        } else { // for backward compatibility until project is updated
            props.setProperty(JbiProjectProperties.ASSEMBLY_UNIT_UUID, name);
        }
    }
    
    /**
     * Update service assembly description.
     * 
     * @param props 
     * @param oldName old project name
     * @param newName new project name
     */
    public static void updateServiceAssemblyDescription(
            EditableProperties props,
            String oldName, String newName) {  
        
        String saDescription = props.getProperty(JbiProjectProperties.SERVICE_ASSEMBLY_DESCRIPTION);
        if (saDescription == null) { // for backward compatibility until project is updated
            saDescription = props.getProperty(JbiProjectProperties.ASSEMBLY_UNIT_DESCRIPTION);
        }
        
        if (saDescription.contains(oldName)) {
            saDescription = saDescription.replaceAll(oldName, newName);
            
            if (props.getProperty(JbiProjectProperties.SERVICE_ASSEMBLY_DESCRIPTION) != null) {
                props.setProperty(JbiProjectProperties.SERVICE_ASSEMBLY_DESCRIPTION, saDescription);
            } else { // for backward compatibility until project is updated
                props.setProperty(JbiProjectProperties.ASSEMBLY_UNIT_DESCRIPTION, saDescription);
            }
        }
    }
}
