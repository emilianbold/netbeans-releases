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

package org.netbeans.modules.web.api.webmodule;

/** Constants ueful for web-based projects;
 *
 * @author  Milan Kuchtiak
 */
public class WebProjectConstants {

    private WebProjectConstants () {}

    /**
     * Document root root sources type (source folders for JSPs, HTML ...)
     * @see org.netbeans.api.project.Sources
     */
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N

    /**
     * WEB-INF sources type (source folders for TLD files ...)
     * @see org.netbeans.api.project.Sources
     */
    public static final String TYPE_WEB_INF="web_inf"; //NOI18N
    /**
     * WEB-INF sources type (source folders for Tag Files ...)
     * @see org.netbeans.api.project.Sources
     */
    public static final String TYPE_TAGS="tags"; //NOI18N
    
    /**
     * Standard command for redeploying a web project.
     * @see ActionProvider
     */
    //TO-DO: should be got from org.netbeans.modules.j2ee.common.J2eeProjectConstants 
    public static final String COMMAND_REDEPLOY = "redeploy" ; //NOI18N
    
    /**
     * Standard artifact type representing a WAR file.
     * @see org.netbeans.api.project.ant.AntArtifact
     */
    public static final String ARTIFACT_TYPE_WAR = "war"; // NOI18N 
    
    /**
     * Standard artifact type representing a WAR file used for adding 
     * Web module into a J2EE Application (ear project).
     * @see org.netbeans.api.project.ant.AntArtifact
     */
    //TO-DO: should be got from org.netbeans.modules.j2ee.common.J2eeProjectConstants 
    public static final String ARTIFACT_TYPE_WAR_EAR_ARCHIVE = "j2ee_ear_archive"; //NOI18N
    
}
