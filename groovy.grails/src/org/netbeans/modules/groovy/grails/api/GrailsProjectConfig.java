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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails.api;

import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;


/**
 * Represents IDE configuration of the Grails project.
 *
 * @author schmidtm, Petr Hejl
 */
public final class GrailsProjectConfig {

    private static final String DEFAULT_PORT = "8080"; // NOI18N

    private final Project prj;

    private final GrailsSettings settings = GrailsSettings.getInstance();

    private GrailsProjectConfig(Project prj) {
        this.prj = prj;
    }

    /**
     * Returns the configuration of the given project.
     *
     * @param project project for which the returned configuration will serve
     * @return the configuration of the given project
     */
    public static GrailsProjectConfig forProject(Project project) {
        return new GrailsProjectConfig(project);
    }

    /**
     * Returns the project for wich the configuration is used.
     *
     * @return the project for wich the configuration is used
     */
    public Project getProject() {
        return prj;
    }

    /**
     * Returns the port configured for the project.
     *
     * @return the port configured for the project
     */
    public String getPort() {
        String port = settings.getPortForProject(prj);
        if (port == null) {
            port = DEFAULT_PORT;
        }
        return port;
    }

    /**
     * Sets the port for the project.
     *
     * @param port the port to set
     */
    public void setPort(String port) {
        assert port != null;
        settings.setPortForProject(prj, port);
    }

    /**
     * Returns the environment configured for the project.
     *
     * @return the environment configured for the project or <code>null</code>
     *             if no environment has been configured yet
     */
    public GrailsEnvironment getEnvironment() {
        return settings.getEnvForProject(prj);
    }

    /**
     * Sets the environment for the project.
     *
     * @param env the environment to set
     */
    public void setEnvironment(GrailsEnvironment env) {
        assert env != null;
        settings.setEnvForProject(prj, env);
    }

    /**
     * Returns the deployment dir configured for the project.
     *
     * @return the deployment dir configured for the project or <code>null</code>
     *             if no deployment dir has been configured yet
     */
    public String getDeployDir() {
        return settings.getDeployDirForProject(prj);
    }

    /**
     * Sets the deployment dir for the project.
     *
     * @param dir deployemnt dir to set
     */
    public void setDeployDir(String dir) {
        assert dir != null;
        settings.setDeployDirForProject(prj, dir);
    }

    /**
     * Returns the autodeploy flag of the project.
     *
     * @return the autodeploy flag of the project
     */
    public boolean getAutoDeployFlag() {
        return settings.getAutoDeployFlagForProject(prj);
    }

    /**
     * Sets the autodeploy flag of the project.
     *
     * @param flag the autodeploy flag to set
     */
    public void setAutoDeployFlag(boolean flag) {
        settings.setAutoDeployFlagForProject(prj, flag);
    }

    /**
     * Returns the browser configured for the project.
     *
     * @return the browser configured for the project or <code>null</code>
     *             if no browser has been configured yet
     */
    public String getDebugBrowser() {
        return settings.getDebugBrowserForProject(prj);
    }

    /**
     * Sets the browser for the project.
     *
     * @param browser browser to set
     */
    public void setDebugBrowser(String browser) {
        assert browser != null;
        settings.setDebugBrowserProject(prj, browser);
    }
        
}
