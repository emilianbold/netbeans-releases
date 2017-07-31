/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.odcs.cnd.execution;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.odcs.cnd.api.DevelopVMExecutionEnvironment;
import org.netbeans.modules.odcs.cnd.json.VMDescriptor;
import org.openide.util.Exceptions;

public class DevelopVMExecutionEnvironmentImpl extends DevelopVMExecutionEnvironment {

    private final String serverUrl;
    private final String machineId;
    //        return String.format("%s://%s@%s", CLOUD_PREFIX, developEE)

    private final AtomicReference<String> displayName = new AtomicReference<>();
    private final AtomicReference<String> ip = new AtomicReference<>();
    private final AtomicReference<Integer> port = new AtomicReference<>();

    DevelopVMExecutionEnvironmentImpl(String serverUrl, String machineId, String displayName) {
        this.serverUrl = serverUrl;
        this.machineId = machineId;
    }

    DevelopVMExecutionEnvironmentImpl(String serverUrl, String machineId) {
        this(serverUrl, machineId, encode(serverUrl, machineId));
    }

    @Override
    public String getHost() {
        // TODO ???
        return ip.get();
    }

    @Override
    public String getHostAddress() {
        return ip.get();
    }

    // TODO should fetch displayName?
    @Override
    public String getDisplayName() {
        return displayName.get();
    }

    @Override
    public String getUser() {
        return "FIXME-USER";
    }

    @Override
    public int getSSHPort() {
        return port.get();
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
        VMDescriptor vmDescriptor = new DevelopVMExecutionClient(this).getVMDescriptor();

        ip.set(vmDescriptor.getHostname());
        port.set(Math.toIntExact(vmDescriptor.getPort()));
        displayName.set(vmDescriptor.getDisplayName());
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
    public void init() {
        try {
            prepareForConnection();
        } catch (IOException | ConnectionManager.CancellationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
