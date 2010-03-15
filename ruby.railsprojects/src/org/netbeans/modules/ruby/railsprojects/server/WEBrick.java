/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.railsprojects.server;

import org.netbeans.modules.ruby.railsprojects.server.nodes.RubyServerNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.RailsProjectUtil.RailsVersion;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * This class represents a WEBrick installation.
 *
 * @author Erno Mononen
 */
class WEBrick implements RubyServer, ServerInstanceImplementation {

    /**
     * The pattern for recognizing when an instance of WEBrick has started.
     */
    private static final Pattern[] STARTUP_PATTERNS = {
        Pattern.compile(".*Rails.*application started on.+", Pattern.DOTALL),
        // rails 2.3
        Pattern.compile(".*WEBrick::HTTPServer#start:.*pid=.+", Pattern.DOTALL),
    };
    
    private final RubyPlatform platform;
    private final List<RailsApplication> applications = new ArrayList<RailsApplication>();
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private Node node;

    WEBrick(RubyPlatform platform) {
        Parameters.notNull("platform", platform); //NOI18N
        this.platform = platform;
    }

    private Node getNode() {
        if (this.node == null) {
            this.node = new RubyServerNode(this);
        }
        return node;
    }
    
    public String getNodeName() {
        return NbBundle.getMessage(WEBrick.class, "LBL_ServerNodeName", getDisplayName(), platform.getLabel());
    }

    // RubyServer methods 
    public String getLocation() {
        return null;
    }
    
    @Override
    public List<String> getStartupParams(RailsVersion version) {
        if (version.isRails3OrHigher()) {
            return Arrays.asList("server", "webrick");
        }
        return Arrays.asList("webrick");
    }

    public String getScriptPrefix() {
        return null;
    }

    public String getServerPath(RailsVersion version) {
        if (version.isRails3OrHigher()) {
            return "script" + File.separator + "rails";
        }
        return "script" + File.separator + "server";
    }

    public boolean isStartupMsg(String outputLine) {
        for (Pattern pattern : STARTUP_PATTERNS) {
            if (pattern.matcher(outputLine).matches()) {
                return true;
            }
        }
        return false;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WEBrick.class, "LBL_WEBrick");
    }

    public List<RailsApplication> getApplications() {
        return Collections.unmodifiableList(applications);
    }

    public boolean addApplication(RailsApplication application) {
        boolean result = applications.add(application);
        changeSupport.fireChange();
        return result;
    }

    public boolean removeApplication(int port) {
        boolean result = false;
        for (RailsApplication app : applications) {
            if (app.getPort() == port) {
                result = applications.remove(app);
                changeSupport.fireChange();
                break;
            }
        }

        return result;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    // ServerInstanceImplementation methods
    public String getServerDisplayName() {
        return getNodeName();
    }

    public Node getFullNode() {
        return getNode();
    }

    public Node getBasicNode() {
        return getNode();
    }

    public JComponent getCustomizer() {
        //TODO
        return null;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isRemovable() {
        return false;
    }

    // RubyInstance methods
    public String getServerUri() {
        return "WEBRICK"; //NOI18N
    }

    public ServerState getServerState() {
        // TODO: currently handled in Rails project
        return null;
    }

    public Future<OperationState> startServer(RubyPlatform platform) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Future<OperationState> stopServer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Future<OperationState> deploy(String applicationName, File applicationDir) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Future<OperationState> stop(String applicationName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Future<OperationState> runApplication(RubyPlatform platform, String applicationName, File applicationDir) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean isPlatformSupported(RubyPlatform platform) {
        return this.platform.equals(platform);
    }

    public String getContextRoot(String applicationName) {
        return ""; // NOI18N
    }

    public int getRailsPort() {
        return 3000;
    }
    
    public String getServerCommand(RubyPlatform platform, String classpath, 
            File applicationDir, int httpPort, boolean debug) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WEBrick other = (WEBrick) obj;
        if (this.platform != other.platform && (this.platform == null || !this.platform.equals(other.platform))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.platform != null ? this.platform.hashCode() : 0);
        return hash;
    }
    
}
