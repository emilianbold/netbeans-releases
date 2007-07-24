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

/*
 * RicohDeploymentProperties.java
 *
 * Created on 09 July 2007, 16:17
 *
 */

package org.netbeans.modules.mobility.deployment.ricoh;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Lukas Waldmann
 */
public class RicohDeploymentProperties
{    
    /**
     * project deployment properties
     */
    //Deploy Method type
     static String PROP_RICOH_DEPLOY_METHOD = "ricoh.deployment.deploy.method"; //NOI18N
    
    //security cert. properties 
     static String PROP_RICOH_SIGN_KEYFILE        = "ricoh.deployment.keystore.file"; //NOI18N
     static String PROP_RICOH_SIGN_MANIFEST       = "ricoh.deployment.keystore.manifest"; //NOI18N
     static String PROP_RICOH_SIGN_KEYPASS        = "ricoh.deployment.keystore.password"; //NOI18N
     static String PROP_RICOH_SIGN_ALIAS          = "ricoh.deployment.keystore.alias"; //NOI18N
    
    
    /**
     * global deployment properties
     */
    
    //Login properties
     static String PROP_RICOH_DEPLOY_SERVER   = "ricoh.deployment.server.name"; //NOI18N
     static String PROP_RICOH_DEPLOY_USERNAME = "ricoh.deployment.server.user"; //NOI18N
     static String PROP_RICOH_DEPLOY_PASSWORD = "ricoh.deployment.server.password"; //NOI18N
    
    //SD card property
     static String PROP_RICOH_DEPLOY_SDCARD_PATH = "ricoh.deployment.sdcard.path"; //NOI18N    

    //HTTP POST property
     static String PROP_RICOH_DEPLOY_HTTP_PORT      = "ricoh.deployment.http.port"; //NOI18N
     static String PROP_RICOH_DEPLOY_HTTP_PLATFORM  = "ricoh.deployment.http.platform"; //NOI18N
     
    //Constants
    
    private static final String DEFAULT_OSGI_PORT = "8080";
    private static final String DEFAULT_SSHKEY_EXT  = File.separator + ".ssh" + File.separator + "known_hosts"; //NOI18N
    //tries to find a "good" default ssh path; either ~/.ssh for unix systems or %USERPROFILE%/.ssh for windows systems, or the default home directory for each
    private static final String DEFAULT_SSHKEY_PATH = org.openide.util.Utilities.isUnix() ? 
                                                           (new File("~").getAbsolutePath() + DEFAULT_SSHKEY_EXT) :  //NOI18N
                                                           (new File(System.getenv("USERPROFILE") + File.separator + ".ssh").exists() ?  //NOI18N
                                                                (new File(System.getenv("USERPROFILE")).getAbsolutePath() + DEFAULT_SSHKEY_EXT) :  //NOI18N
                                                                (new File(System.getenv("USERPROFILE")).getAbsolutePath())); //NOI18N
    
    private static HashMap<String,Object> projectProp = new HashMap<String,Object>();
    static
    {
        projectProp.put(PROP_RICOH_SIGN_KEYFILE,"");
        projectProp.put(PROP_RICOH_SIGN_MANIFEST,"");
        projectProp.put(PROP_RICOH_SIGN_KEYPASS,"");
        projectProp.put(PROP_RICOH_SIGN_ALIAS,"");
        projectProp.put(PROP_RICOH_DEPLOY_METHOD,DeploymentComboBoxModel.SD_CARD_DEPLOY);
    }
    
    private static HashMap<String,Object> globalProp = new HashMap<String,Object>();
    static
    {
        globalProp.put(PROP_RICOH_DEPLOY_SERVER,"");
        globalProp.put(PROP_RICOH_DEPLOY_USERNAME,"");
        globalProp.put(PROP_RICOH_DEPLOY_PASSWORD,"");
        
        globalProp.put(PROP_RICOH_DEPLOY_SDCARD_PATH,"");
        
        globalProp.put(PROP_RICOH_DEPLOY_HTTP_PORT,DEFAULT_OSGI_PORT);
        globalProp.put(PROP_RICOH_DEPLOY_HTTP_PLATFORM,"SDK/J &2.0+");        
    }
    
    public static Map<String,Object> getDefaultGlobalProperties()
    {
        return Collections.unmodifiableMap(globalProp);
    }
    
    public static Map<String,Object> getDefaultProjectProperties()
    {
        return Collections.unmodifiableMap(projectProp);
    }
}
