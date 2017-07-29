/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.odcs.cnd.execution;

import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.odcs.cnd.api.DevelopVMExecutionEnvironment;

public class DevelopVMExecutionEnvironmentImpl extends DevelopVMExecutionEnvironment {

    private final String serverUrl;
    private final String machineId;
    //        return String.format("%s://%s@%s", CLOUD_PREFIX, developEE)
    private String displayName;

    DevelopVMExecutionEnvironmentImpl(String serverUrl, String machineId, String displayName) {
        this.serverUrl = serverUrl;
        this.machineId = machineId;
        this.displayName = displayName;
    }

    DevelopVMExecutionEnvironmentImpl(String serverUrl, String machineId) {
        this(serverUrl, machineId, encode(serverUrl, machineId));
    }

    @Override
    public String getHost() {
        // TODO ???
        return machineId;
    }

    @Override
    public String getHostAddress() {
        return new DevelopVMExecutionClient(this).getHostIP();
    }

    // TODO should fetch displayName?
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getUser() {
        return "ilia";
    }

    @Override
    public int getSSHPort() {
        return new DevelopVMExecutionClient(this).getSSHPort();
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getMachineId() {
        return machineId;
    }
}
