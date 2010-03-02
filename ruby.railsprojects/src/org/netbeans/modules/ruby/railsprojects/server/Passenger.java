package org.netbeans.modules.ruby.railsprojects.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.RailsProjectUtil.RailsVersion;
import org.netbeans.modules.ruby.railsprojects.server.nodes.RubyServerNode;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance.OperationState;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance.ServerState;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * This class represents passenger (gem) installation
 * 
 * @author Michal Papis
 */
class Passenger  implements RubyServer, ServerInstanceImplementation {

    static final String GEM_NAME = "passenger";
    static final String SERVER_URI = "PASSENGER";
    private final RubyPlatform platform;
    private final String version;
    private final List<RailsApplication> applications = new ArrayList<RailsApplication>();
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private Node node;

    Passenger(RubyPlatform platform, String version) {
        Parameters.notNull("platform", platform); //NOI18N
        this.platform = platform;
        this.version = version;
    }

    private Node getNode() {
        if (this.node == null) {
            this.node = new RubyServerNode(this);
        }
        return node;
    }

    public String getNodeName() {
        return NbBundle.getMessage(Passenger.class, "LBL_ServerNodeName", getDisplayName(), platform.getLabel());
    }

    public String getLocation() {
        return null;
    }

    @Override
    public List<String> getStartupParams(RailsVersion version) {
        if (version.isRails3OrHigher()) {
            return Arrays.asList("server", "passenger");
        }
        return Arrays.asList("passenger");
    }

    public String getScriptPrefix() {
        return null;
    }

    public String getServerPath(RailsVersion version) {
        return null;
    }

    public boolean isStartupMsg(String arg0) {
        //Always is started
        return true;
    }

    public List<RailsApplication> getApplications() {
        return Collections.unmodifiableList(applications);
    }

    public boolean addApplication(RailsApplication application) {
        boolean result = applications.add(application);
        changeSupport.fireChange();
        return result;
    }

    /**
     * Will remove application, instead of http port, the port will carry instance number
     * @param port
     * @return
     */
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

    /**
     *
     * @return upercase server name
     * @see ServerResolver.getExplicitlySpecifiedServer(RailsProject project)
     * @see ServerRegistry.getServer(String serverId, RubyPlatform platform)
     */
    public String getServerUri() {
        return "PASSENGER";
    }

    public String getDisplayName() {
        return NbBundle.getMessage(Passenger.class, "LBL_Passenger",version);
    }

    /**
     * Passenger is meant to be always running, but we could check port if it is listening for connections
     * @return ServerState.RUNNING
     * @see RailsServerManager.ensureRunning is currently doing this job
     */
    public ServerState getServerState() {
        return ServerState.RUNNING;
    }

    public Future<OperationState> startServer(RubyPlatform arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Future<OperationState> stopServer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Future<OperationState> deploy(String arg0, File arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Future<OperationState> stop(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Future<OperationState> runApplication(RubyPlatform arg0, String arg1, File arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isPlatformSupported(RubyPlatform platform) {
        return this.platform.equals(platform);
    }

    public String getContextRoot(String applicationName) {
        return applicationName;
    }

    public int getRailsPort() {
        return 80;
    }

    public String getServerCommand(RubyPlatform arg0, String arg1, File arg2, int arg3, boolean arg4) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Passenger other = (Passenger) obj;
        if (this.platform != other.platform && (this.platform == null || !this.platform.equals(other.platform))) {
            return false;
        }
        if (this.version != other.version && (this.version == null || !this.version.equals(other.version))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 55 * hash + (this.platform != null ? this.platform.hashCode() : 0);
        hash = 55 * hash + (this.version != null ? this.version.hashCode() : 0);
        return hash;
    }
}
