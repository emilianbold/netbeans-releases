/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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

package org.netbeans.modules.visualweb.gravy.model.deployment;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.model.ExternalProcess;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;

/**
 * Common class for all application servers.
 */

public class ApplicationServer implements DeploymentTarget, ExternalProcess {
    
    public final static String SERVER_START = "Start";
    public final static String SERVER_START_DEBUG = "Start in Debug Mode";
    public final static String SERVER_RESTART = "Restart";
    public final static String SERVER_STOP = "Stop";
    public final static String SERVER_REMOVE = "Remove";
    public final static String SERVER_CUSTOMIZE = "Customize";
    public final static String SERVER_VIEW_CONSOLE = "View Admin Console";
    public final static String PROPERTIES = "Properties";
    public final static String REFRESH = "Refresh";
    public final static String APPLICATION_UNDEPLOY = "Undeploy";
    
    public String SERVER_VIEW_LOG;
    public String APPLICATION_DISABLE;
    public String APPLICATION_ENABLE;
    
    /**
     * Prefix before application node in the list of web applications.
     */
    public String app_pref;
    
    /**
     * Path to web applications.
     */
    public String web_applications_path;
    
    /**
     * Path to JDBC resources.
     */
    public String jdbc_resources_path;
    
    /**
     * Array of items of Application Server's popup menu.
     */
    public String[][] server_popup;
    
    /**
     * Array of items of application's popup menu.
     */
    public String[] application_popup;
    
    /**
     * Array of items of resource's popup menu.
     */
    public String[] resource_popup;
    
    /**
     * Prefix for request to load application in browser.
     */
    public String requestPrefix;
    
    /**
     * Descriptor of deployment target.
     */
    private DeploymentTargetDescriptor DTDescriptor;

    /**
     * Get descriptor of Application Server.
     * @return descriptor of Application Server.
     */
    public DeploymentTargetDescriptor getDescriptor() {
        return DTDescriptor;
    }

    /**
     * Set descriptor of Application Server.
     * @param DTDescriptor New descriptor of Application Server.
     */
    protected void setDescriptor(DeploymentTargetDescriptor DTDescriptor) {
        this.DTDescriptor = DTDescriptor;
    }

    /**
     * Deploy project to this server.
     * @param project Project to deploy.
     */
    public void deploy(Object project) {
        try {
            Util.getMainWindow().btDeploy().push();
        }
        catch(Exception e) {
            throw new JemmyException("Application can't be deployed to " + getName() + "!", e);
        }
	TestUtils.wait(20000);
    }

    /**
     * Start Application Server.
     */
    public void start() {
        try {
            ServerNavigatorOperator.startServer(getName());
        }
        catch(Exception e) {
            throw new JemmyException(getName() + " can't be started!", e);
        }
    }

    /**
     * Stop Application Server.
     */
    public void stop() {
        try {
            ServerNavigatorOperator.stopServer(getName());
        }
        catch(Exception e) {
            throw new JemmyException(getName() + " can't be stopped!", e);
        }
    }

    /**
     * Instance of Application Server is compared with deployment target, passed with parameter.
     * @param dt Object for comparison.
     * @return true if Object equals to ApplicationServer.
     */
    public boolean equals(Object dt) {
        if ((dt instanceof DeploymentTarget) && ((DeploymentTarget) dt).getName().equals(this.getName())) {
            return true;
        }
        return false;
    }

    /**
     * Get name of Application Server.
     * @return name.
     */
    public String getName() {
        String serverType = DTDescriptor.getProperty(DTDescriptor.SERVER_TYPE_KEY);
        return DTDescriptor.getProperty(DTDescriptor.NAME_KEY + serverType);
    }
}
