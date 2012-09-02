/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.javaee;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;

/**
 * Common GlassFish cloud local server and remote cloud life cycle services
 * from the IDE.
 * <p/>
 * JavaEE server will use these services to automatically start or stop
 * administration server and managed (virtual) target servers (in debug mode)
 * during deployment or debugging execution.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class GlassFishStartServer extends StartServer {

    /**
     * Returns <code>true</code> if the administration server is also the given
     * target server (share the same VM).
     * <p/>
     * Start/stopping/debug apply to both servers.  When the given target
     * server is <code>null</code>, service should return true when
     * administration server is also some target.
     * <p/>
     * @param target The target server in question; could be null.
     * @return <code>true</code> when administration is also target server.
     */
    @Override
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }

    /**
     * Returns <code>true</code> if the administration server can be started
     * through this SPI.
     * <p/>
     * At this moment we do not support starting administration server from
     * NetBeans.
     * <p/>
     * @return <code>true</code> when administration server can be started
     *         through this SPI.
     */
    @Override
    public boolean supportsStartDeploymentManager() {
        return false;
    }

    /**
     * Starts the administration server.
     * <p/>
     * Note that this means that the DeploymentManager was originally created
     * disconnected. After calling this, the DeploymentManager will
     * be connected, so any old cached DeploymentManager will be discarded.
     * All diagnostics should be communicated through ProgressObject without
     * exceptions thrown.
     * <p/>
     * At this moment we do not support starting administration server from
     * NetBeans.
     * <p/>
     * @return ProgressObject object used to monitor start server progress.
     */
    @Override
    public ProgressObject startDeploymentManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Stops the administration server.
     * <p/>
     * The DeploymentManager object will be disconnected. All diagnostic
     * should be communicated through ServerProgres with no exceptions thrown.
     * <p/>
     * At this moment we do not support starting administration server from
     * NetBeans.
     * <p/>
     * @return ServerProgress object used to monitor start server progress
     */
    @Override
    public ProgressObject stopDeploymentManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns <code>true</code> if the administration server should be started
     * before server deployment configuration.
     * <p/>
     * @returns <code>true</code> when the administration server should be
     *          started before server deployment configuration.
     */
    @Override
    public boolean needsStartForConfigure() {
        return false;
    }

    /**
     * Returns <code>true</code> if the administration server should be started
     * before asking for target list.
     * <p/>
     * @returns <code>true</code> when the administration server should be
     *          started before asking for target list.
     */
    @Override
    public boolean needsStartForTargetList() {
        return false;
    }

    /**
     * Returns <code>true</code> if the administration server should be started
     * before administrative configuration.
     * <p/>
     * @return <code>true</code> when the administration server should be
     *          started before administrative configuration.
     */
    @Override
    public boolean needsStartForAdminConfig() {
        return false;
    }

    /**
     * Returns <code>true</code> if the administration server is running.
     * <p/>
     * @return <code>true</code> when the administration server is running.
     */
    @Override
    public boolean isRunning() {
        // TODO: Add some real check for both cloud and local server.
        return true;
    }

    /**
     * Returns <code>true</code> if the given target is in debug mode.
     * <p/>
     * @return <code>true</code> when the given target is in debug mode.
     */
    @Override
    public boolean isDebuggable(Target target) {
        return false;
    }

    /**
     * Start or restart the target in debug mode.
     * <p/>
     * If target is also domain administration, the administration is restarted
     * in debug mode. All diagnostic should be communicated through
     * <code>ServerProgres</code> with no exceptions thrown.
     * <p/>
     * @param target The target server.
     * @return <code>ServerProgres</code> object to monitor progress on start
     *         operation.
     */
    @Override
    public ProgressObject startDebugging(Target target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the host/port necessary for connecting to the server's
     * debug information.
     * <p/>
     * @return The host/port necessary for connecting to the server's debug
     *         information.
     */
    @Override
    public ServerDebugInfo getDebugInfo(Target target) {
        return null;
    }
    
}
