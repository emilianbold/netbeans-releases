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

package org.netbeans.modules.web.freeform;

/**
 * Contains constants for various properties used in the web project
 *
 * @author Andrei Badea
 */
public class WebFreeformProperties {

    /**
     * JPDA debug session name
     */
    public final static String JPDA_SESSION_NAME = "jpda.session.name"; // NOI18N

    /**
     * JPDA transport type
     */
    public final static String JPDA_TRANSPORT = "jpda.transport"; // NOI18N

    /**
     * JPDA host to connect to
     */
    public final static String JPDA_HOST = "jpda.host"; // NOI18N
    
    public final static String JPDA_ADDRESS = "jpda.address"; // NOI18N
    
    /**
     * The full client URL (e.g., http://localhost:8084/AppName)
     */
    public final static String CLIENT_URL = "client.url"; // NOI18N
    
    public final static String SRC_FOLDERS = "src.folders"; // NOI18N
    
    public final static String WEB_DOCBASE_DIR = "web.docbase.dir"; // NOI18N
    
    public final static String DEBUG_SOURCEPATH = "debug.sourcepath"; // NOI18N
    
    private WebFreeformProperties() {
    }
    
}
