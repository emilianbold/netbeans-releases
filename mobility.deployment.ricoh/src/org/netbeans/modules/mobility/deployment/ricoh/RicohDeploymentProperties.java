/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    //SD card property
     static String PROP_RICOH_DEPLOY_SDCARD_PATH = "ricoh.deployment.sdcard.path"; //NOI18N    

    //HTTP POST property
     static String PROP_RICOH_DEPLOY_HTTP_PORT      = "ricoh.deployment.http.port"; //NOI18N
     static String PROP_RICOH_DEPLOY_HTTP_PLATFORM  = "ricoh.deployment.http.platform"; //NOI18N
     //Login properties
     static String PROP_RICOH_DEPLOY_HTTP_SERVER   = "ricoh.deployment.server.name"; //NOI18N
     static String PROP_RICOH_DEPLOY_HTTP_USERNAME = "ricoh.deployment.server.user"; //NOI18N
     static String PROP_RICOH_DEPLOY_HTTP_PASSWORD = "ricoh.deployment.server.password"; //NOI18N
     
    //Constants
    
    private static final String DEFAULT_OSGI_PORT = "8080";
    private static final String DEFAULT_SSHKEY_EXT  = File.separator + ".ssh" + File.separator + "known_hosts"; //NOI18N
    //tries to find a "good" default ssh path; either ~/.ssh for unix systems or %USERPROFILE%/.ssh for windows systems, or the default home directory for each
    /*private static final String DEFAULT_SSHKEY_PATH = org.openide.util.Utilities.isUnix() ?
                                                           (new File("~").getAbsolutePath() + DEFAULT_SSHKEY_EXT) :  //NOI18N
                                                           (new File(System.getenv("USERPROFILE") + File.separator + ".ssh").exists() ?  //NOI18N
                                                                (new File(System.getenv("USERPROFILE")).getAbsolutePath() + DEFAULT_SSHKEY_EXT) :  //NOI18N
                                                               (new File(System.getenv("USERPROFILE")).getAbsolutePath())); //NOI18N */
    
    private static HashMap<String,Object> projectProp = new HashMap<String,Object>();
    static
    {
        projectProp.put(PROP_RICOH_SIGN_KEYFILE,"");
        projectProp.put(PROP_RICOH_SIGN_MANIFEST,"");
        projectProp.put(PROP_RICOH_SIGN_KEYPASS,"");
        projectProp.put(PROP_RICOH_SIGN_ALIAS,"");
        projectProp.put(PROP_RICOH_DEPLOY_METHOD,DeploymentComboBoxModel.SD_CARD_DEPLOY);
        
        
        
        projectProp.put(PROP_RICOH_DEPLOY_SDCARD_PATH,"");
        
        projectProp.put(PROP_RICOH_DEPLOY_HTTP_SERVER,"");
        projectProp.put(PROP_RICOH_DEPLOY_HTTP_USERNAME,"");
        projectProp.put(PROP_RICOH_DEPLOY_HTTP_PASSWORD,"");
        projectProp.put(PROP_RICOH_DEPLOY_HTTP_PORT,DEFAULT_OSGI_PORT);
        projectProp.put(PROP_RICOH_DEPLOY_HTTP_PLATFORM,"SDK/J &2.0+");
    }
    
    public static Map<String,Object> getDefaultProjectProperties()
    {
        return Collections.unmodifiableMap(projectProp);
    }
}
