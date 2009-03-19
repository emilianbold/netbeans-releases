/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
     * jbiServiceUnits folder
     */
    public static final String FOLDER_JBIPROJECTS = "projs/caps"; // NOI18N

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

    public static final String SU_BUILD_ARTIFACT_RELATIVE_PATH = "build/SEDeployment.jar" ; //NOI18N
    // J2EE add-on...
    public static final String JAVA_EE_SE_COMPONENT_NAME = "sun-javaee-engine" ; // NOI18N
    public static final String JAVA_SE_PROJECT_CLASS_NAME = "org.netbeans.modules.java.j2seproject.J2SEProject"; // NOI18N    
    public static final String JAVA_SE_POJO_ENGINE="openesb-pojo-engine";// NOI18N
    public static final String POJO_SE_PROJECT_ANT_ARTIFACT_TYPE = "CAPS.asa:"+JAVA_SE_POJO_ENGINE; // NOI18N    
    public static final String POJO_PROJECT_PROPERTY ="pojo.project.version" ; // NOI18N   
    // Do not use "j2ee_ear_archive" as EJB Jar also has this type and is corresponds to 
    // Ejb jar built will not have library jars.
    //public static final String JAVA_EE_EAR_COMPONENT_ARCHIVE = "j2ee_ear_archive"; // NOI18N
    public static final String EJB_ARCHIVE = "j2ee_archive"; //NOI18N
    public static final String WEB_ARCHIVE = "war"; //NOI18N
    public static final String JAVA_EE_EAR_ARCHIVE = "ear" ; //NOI18N
    public static final List<String> JAVA_EE_AA_TYPES = new ArrayList<String>();
    static {
        // For both EJB and Web Projects use the same target
        JbiProjectConstants.JAVA_EE_AA_TYPES.add(EJB_ARCHIVE); 
        JbiProjectConstants.JAVA_EE_AA_TYPES.add(WEB_ARCHIVE); 
        JbiProjectConstants.JAVA_EE_AA_TYPES.add(JAVA_EE_EAR_ARCHIVE);
    } ;

    private JbiProjectConstants() {
    }
}
