/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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

package org.netbeans.test.j2ee.serverplugins.weblogic;

import java.io.File;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.test.j2ee.serverplugins.api.ConstantsProvider;

/**
 * Weblogic implementation of the ConstantsProvider
 *
 * @author Michal Mocnak
 */
public class WeblogicConstantsProvider implements ConstantsProvider {
    
    private static final String DISPLAY_NAME = "Weblogic Application Server";
    private static final String SERVER_ROOT = System.getProperty("weblogic.server.path");
    private static final String DOMAIN_ROOT = SERVER_ROOT + File.separator + "samples" +
            File.separator + "domains" + File.separator + "wl_server";
    private static final String HOST = "localhost";
    private static final String PORT = "7001";
    private static final String PORT_DEBUGGER = "8787";
    private static final String SERVER_URI = WLDeploymentFactory.URI_PREFIX + HOST +
            ":" + PORT + ":" + SERVER_ROOT;
    private static final String USERNAME = "weblogic";
    private static final String PASSWORD = "weblogic";
    private static final String IS_LOCAL = "true";
    
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
    
    public String getServerRoot() {
        return SERVER_ROOT;
    }
    
    public String getHost() {
        return HOST;
    }
    
    public String getPort() {
        return PORT;
    }
    
    public String getServerURI() {
        return SERVER_URI;
    }
    
    public String getUsername() {
        return USERNAME;
    }
    
    public String getPassword() {
        return PASSWORD;
    }
    
    /**
     * Returns domain's root directory
     *
     * @return domain's root directory
     */
    public String getDomainRoot() {
        return DOMAIN_ROOT;
    }
    
    /**
     * Returns default debugger port
     *
     * @return default debugger port
     */
    public String getPortDebugger() {
        return PORT_DEBUGGER;
    }
    
    /**
     * Returns boolean string true if the server is local
     *
     * @return boolean string
     */
    public String isLocal() {
        return IS_LOCAL;
    }
}
