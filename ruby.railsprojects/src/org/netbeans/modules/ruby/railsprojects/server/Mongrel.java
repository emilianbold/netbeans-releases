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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.railsprojects.server;

import org.netbeans.modules.ruby.railsprojects.server.nodes.RubyServerNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * TODO:doc
 * 
 * @author Erno Mononen
 */
class Mongrel implements RubyServer, ServerInstanceImplementation {

    static final String GEM_NAME = "mongrel";
    private static final Pattern PATTERN = Pattern.compile("\\bMongrel.+available at.+", Pattern.DOTALL);
    private final List<RailsApplication> applications;
    private final RubyPlatform platform;
    private Node node;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    Mongrel(RubyPlatform platform) {
        this.platform = platform;
        this.applications = new ArrayList<RailsApplication>();
    }

    public String getName() {
        return NbBundle.getMessage(Mongrel.class, "LBL_Mongrel");
    }

    // RubyServer  methods
    public String getStartupParam() {
        return null;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(Mongrel.class, "LBL_ServerDisplayName", getName(), platform.getLabel());
    }

    public String getServerPath() {
        return "script" + File.separator + "server";
    }

    public boolean isStartupMsg(String outputLine) {
        return PATTERN.matcher(outputLine).find();
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
        return NbBundle.getMessage(Mongrel.class, "LBL_ServerDisplayName", getDisplayName(), platform.getLabel());
    }

    public Node getFullNode() {
        if (this.node == null) {
            this.node = new RubyServerNode(this);
        }
        return node;
    }

    public Node getBasicNode() {
        if (this.node == null) {
            this.node = new RubyServerNode(this);
        }
        return node;
    }

    public JComponent getCustomizer() {
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
        return "MONGREL";
    }

    public String getServerState() {
        // TODO: currently handled in Rails project
        return null;
    }

    public boolean startServer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stopServer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean deploy(String applicationName, File applicationDir) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stop(String applicationName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
