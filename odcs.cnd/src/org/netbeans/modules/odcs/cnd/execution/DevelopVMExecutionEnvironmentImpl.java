/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.odcs.cnd.execution;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.cnd.json.VMDescriptor;
import org.openide.util.Mutex;

// Thread safe
public class DevelopVMExecutionEnvironmentImpl extends DevelopVMExecutionEnvironment {

    private static String DEFAULT_IP = "0.0.0.0";

    private final Mutex mutex = new Mutex();

    // Immutable data
    private final String user;
    private final String machineId;
    private final String serverUrl;

    private String displayName;
    private String ip = DEFAULT_IP;
    private int port = 0;

    DevelopVMExecutionEnvironmentImpl(String user, String machineId, int port, String serverUrl, String displayName) {
        this.serverUrl = serverUrl;
        this.user = user;
        this.machineId = machineId;
        this.port = port;

        mutex.writeAccess(() -> {
            this.displayName = displayName;
        });
    }

    DevelopVMExecutionEnvironmentImpl(String user, String machineId, int port, String serverUrl) {
        this(user, machineId, port, serverUrl, encode(user, machineId, port, serverUrl));
    }

    @Override
    public String getHost() {
        return mutex.readAccess(() -> {
            return ip;
        });
    }

    @Override
    public String getHostAddress() {
        return mutex.readAccess(() -> {
            return ip;
        });
    }

    @Override
    public String getDisplayName() {
        return mutex.readAccess(() -> {
            return displayName;
        });
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public int getSSHPort() {
        return mutex.readAccess(() -> {
            return port;
        });
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public void prepareForConnection() throws IOException, ConnectionManager.CancellationException {
        CndUtils.assertNonUiThread();
        VMDescriptor vmDescriptor = new DevelopVMExecutionClient(this).getVMDescriptor();

        if (vmDescriptor == null) {
            throw new ConnectionManager.CancellationException("Cancelling connection: Oracle DCS server is not connected");
        }

        mutex.writeAccess(() -> {
            this.ip = vmDescriptor.getHostname();
            // this.port // Math.toIntExact(vmDescriptor.getPort());
            this.displayName = user + "@" + vmDescriptor.getDisplayName();
        });
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getMachineId() {
        return machineId;
    }

    @Override
    public void initializeOrWait() {
        try {
            LOG.fine(() -> "Fetching data for cloud execution environment " + this.getDisplayName());
            prepareForConnection();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Error initializing cloud execution environment", ex);
        } catch (ConnectionManager.CancellationException ex) {
            // ignore
        }
    }

    private static final Logger LOG = Logger.getLogger(DevelopVMExecutionEnvironmentImpl.class.getName());
}
