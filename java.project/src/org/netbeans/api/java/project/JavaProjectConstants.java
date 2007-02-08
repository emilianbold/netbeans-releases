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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
     * Package root sources type for resources, if these are not put together with Java sources.
     * @see org.netbeans.api.project.Sources
     * @since org.netbeans.modules.java.project/1 1.11
     */
    public static final String SOURCES_TYPE_RESOURCES = "resources"; // NOI18N

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
