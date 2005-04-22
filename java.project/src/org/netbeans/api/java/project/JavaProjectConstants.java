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

package org.netbeans.api.java.project;

/**
 * Constants useful for Java-based projects.
 * @author Jesse Glick
 */
public class JavaProjectConstants {
    
    private JavaProjectConstants() {}
    
    /**
     * Java package root sources type.
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_JAVA = "java"; // NOI18N
    
    /**
     * Standard artifact type representing a JAR file, presumably
     * used as a Java library of some kind.
     * @see org.netbeans.api.project.ant.AntArtifact
     */
    public static final String ARTIFACT_TYPE_JAR = "jar"; // NOI18N
    
    
    /**
     * Standard artifact type representing a folder containing classes, presumably
     * used as a Java library of some kind.
     * @see org.netbeans.api.project.ant.AntArtifact
     * @since org.netbeans.modules.java.project/1 1.4
     */
    public static final String ARTIFACT_TYPE_FOLDER = "folder"; //NOI18N

    /**
     * Standard command for running Javadoc on a project.
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_JAVADOC = "javadoc"; // NOI18N
    
    /** 
     * Standard command for reloading a class in a foreign VM and continuing debugging.
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_DEBUG_FIX = "debug.fix"; // NOI18N
    
}
