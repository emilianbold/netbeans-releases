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

import java.util.List;
import java.util.ArrayList;

/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class JbiProjectConstants {
    
    /**
     * jbiasa folder
     */
    public static final String FOLDER_JBIASA = "jbiasa"; // NOI18N
    
    /**
     * jbiServiceUnits folder
     */
    public static final String FOLDER_JBISERVICEUNITS = "jbiServiceUnits"; // NOI18N
    
    /**
     * Jbi package root sources type.
     *
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_JBI = "java"; // NOI18N

    /**
     * Standard artifact type representing a JAR file, presumably used as a Java library of some
     * kind.
     *
     * @see org.netbeans.api.project.ant.AntArtifact
     */
    public static final String ARTIFACT_TYPE_JBI_ASA = "CAPS.asa"; // NOI18N

    /**
     * Standard artifact type representing a JAR file, presumably used as a Java library of some
     * kind.
     *
     * @see org.netbeans.api.project.ant.AntArtifact
     */
    public static final String ARTIFACT_TYPE_JBI_AU = "CAPS.au"; // NOI18N

    /**
     * Standard command for running Javadoc on a project.
     *
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_JAVADOC = "javadoc"; // NOI18N

    /**
     * Standard command for reloading a class in a foreign VM and continuing debugging.
     *
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_DEBUG_FIX = "debug.fix"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String COMMAND_DEPLOY = "redeploy"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String COMMAND_REDEPLOY = "redeploy"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String COMMAND_UNDEPLOY = "undeploy"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String COMMAND_JBIBUILD = "jbiserver-build"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMMAND_JBICLEANCONFIG = "jbi-clean-config"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String COMMAND_JBICLEANBUILD = "jbiserver-clean_build"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String COMMAND_VALIDATEPORTMAPS = "validate-portmaps"; // NOI18N

    // Start Test Framework
    /**
     * DOCUMENT ME!
     */
    public static final String COMMAND_TEST = "test"; // NOI18N
    public static final String COMMAND_TEST_SINGLE = "test-single"; // NOI18N
    public static final String COMMAND_DEBUG_SINGLE = "debug-single"; // NOI18N
    // End Test Framework

    // J2EE add-on...
    public static final String JAVA_EE_SE_COMPONENT_NAME = "sun-javaee-engine" ; // No I18N
    public static final List<String> JAVA_EE_AA_TYPES = new ArrayList<String>();
    static {
        // For both EJB and Web Projects use the same target
        JbiProjectConstants.JAVA_EE_AA_TYPES.add("j2ee_ear_archive"); //NOI18N
    } ;

    private JbiProjectConstants() {
    }
}
