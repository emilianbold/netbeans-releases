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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.gems.GemInfo;
import org.netbeans.modules.ruby.railsprojects.RailsProjectUtil.RailsVersion;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * This class represents a GlassFish V3 gem installation.
 * 
 * @author Peter Williams
 * @author Erno Mononen
 */
class GlassFishGem implements RubyServer, ServerInstanceImplementation {

    static final String GEM_NAME = "glassfish";
    /**
     * The pattern for recognizing when an instance of GlassFish has started.
     */
    private static final Pattern[] PATTERNS = {
        Pattern.compile(".*INFO: Glassfish v3 started.*", Pattern.DOTALL),
        Pattern.compile(".*[0-9] milliseconds.*", Pattern.DOTALL)
    };
    
    private final List<RailsApplication> applications = new ArrayList<RailsApplication>();
    private final RubyPlatform platform;
    private final String version;
    private final String location;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    private Node node;

    GlassFishGem(RubyPlatform platform, GemInfo gemInfo) {
        Parameters.notNull("platform", platform); //NOI18N
        this.platform = platform;
        this.version = gemInfo.getVersion();
        this.location = getGemFolder(gemInfo.getSpecFile());
    }

    private String getGemFolder(File specFile) {
        String gemFolderName = specFile.getName();
        if(gemFolderName.endsWith(".gemspec")) {
            gemFolderName = gemFolderName.substring(0, gemFolderName.length() - 8);
        }
        
        return new File(specFile.getParentFile().getParentFile(),
                "gems" + File.separatorChar + gemFolderName).getAbsolutePath();
    }

    private Node getNode() {
        if (this.node == null) {
            this.node = new RubyServerNode(this);
        }
        return node;
    }
    
    // RubyServer  methods
    public String getNodeName() {
        return NbBundle.getMessage(GlassFishGem.class, "LBL_ServerNodeName", getDisplayName(), platform.getLabel());
    }
    
    public String getLocation() {
        return location;
    }

    public List<String> getStartupParams(RailsVersion version) {
        return Collections.emptyList();
    }

    public String getScriptPrefix() {
        return "-S";
    }

    public String getServerPath(RailsVersion version) {
        // glassfish_rails is deprecated in 0.9.4 and newer
        return compareVersion("0.9.4") >= 0 ? "glassfish" : "glassfish_rails";
    }

    public boolean isStartupMsg(String outputLine) {
        for (Pattern each : PATTERNS) {
            if (each.matcher(outputLine).matches()) {
                return true;
            }
        }
        return false;
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

    public int compareVersion(String targetVersion) {
        int [] sv = extractVersion(version);
        int [] tv = extractVersion(targetVersion);
        for(int i = 0; i < 3; i++) {
            if(sv[i] < tv[i]) {
                return -1;
            } else if(sv[i] > tv[i]) {
                return 1;
            }
        }
        return 0;
    }

    private int [] extractVersion(String vs) {
        int [] v = new int [] { 0, 0, 0 };
        String [] parts = vs.split("\\.");
        for(int i = 0; i < 3 && i < parts.length; i++) {
            v[i] = Integer.valueOf(parts[i]);
        }
        return v;
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
        return "GLASSFISH";
    }

    public String getDisplayName() {
        return NbBundle.getMessage(GlassFishGem.class, "LBL_GlassFish", version);
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
        final GlassFishGem other = (GlassFishGem) obj;
        if (this.platform != other.platform && (this.platform == null || !this.platform.equals(other.platform))) {
            return false;
        }
        if (this.version != other.version && (this.version == null || !this.version.equals(other.version))) {
            return false;
        }
        if (this.location != other.location && (this.location == null || !this.location.equals(other.location))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.platform != null ? this.platform.hashCode() : 0);
        hash = 47 * hash + (this.version != null ? this.version.hashCode() : 0);
        return hash;
    }

}
