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

package org.netbeans.modules.ant.freeform.spi;

/**
 * Miscellaneous constants.
 * @author David Konecny
 */
public class ProjectConstants {
    
    private ProjectConstants() {}

    /**
     * This property is stored in project.xml iff Ant script is not in
     * default location, that is not in parent folder of nbproject directory.
     */
    public static final String PROP_ANT_SCRIPT = "ant.script"; // NOI18N

    /** 
     * Location of original project. This property exist only when NB 
     * project metadata are stored in different folder.
     */
    public static final String PROP_PROJECT_LOCATION = "project.dir"; // NOI18N
    
    /** 
     * Prefix used in paths to refer to project location.
     */
    public static final String PROJECT_LOCATION_PREFIX = "${" + PROP_PROJECT_LOCATION + "}/"; // NOI18N

}
