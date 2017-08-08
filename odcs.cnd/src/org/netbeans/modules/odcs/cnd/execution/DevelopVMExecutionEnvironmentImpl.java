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
import org.openide.util.Mutex;

public class DevelopVMExecutionEnvironmentImpl extends DevelopVMExecutionEnvironment {

    private final String user;
    private final String machineId;
    private final String serverUrl;

    private final AtomicReference<String> displayName = new AtomicReference<>();
    private final AtomicReference<String> ip = new AtomicReference<>();
    private final AtomicReference<Integer> port = new AtomicReference<>();

    DevelopVMExecutionEnvironmentImpl(String user, String machineId, String serverUrl, String displayName) {
        this.serverUrl = serverUrl;
        this.user = user;
        this.machineId = machineId;

        this.displayName.set(displayName);
    }

    DevelopVMExecutionEnvironmentImpl(String user, String machineId, String serverUrl) {
        this(user, machineId, serverUrl, encode(user, machineId, serverUrl));
    }

    @Override
    public String getHost() {
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
        return user;
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

    private void checkLoaded() {

    }

    @Override
    public void prepareForConnection() throws IOException, ConnectionManager.CancellationException {
        VMDescriptor vmDescriptor = new DevelopVMExecutionClient(this).getVMDescriptor();

        Mutex lock = new Mutex();

        ip.set(vmDescriptor.getHostname());
        port.set(Math.toIntExact(vmDescriptor.getPort()));

        String name = user + "@" + vmDescriptor.getDisplayName();
        displayName.set(name);
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
