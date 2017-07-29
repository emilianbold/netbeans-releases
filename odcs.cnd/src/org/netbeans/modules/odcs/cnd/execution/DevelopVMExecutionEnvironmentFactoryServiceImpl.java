/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.odcs.cnd.execution;

import java.util.HashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService;
import org.netbeans.modules.odcs.cnd.api.DevelopVMExecutionEnvironment;
import static org.netbeans.modules.odcs.cnd.api.DevelopVMExecutionEnvironment.CLOUD_PREFIX;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ExecutionEnvironmentFactoryService.class, position = 50)
public class DevelopVMExecutionEnvironmentFactoryServiceImpl implements ExecutionEnvironmentFactoryService {

    private static final HashMap<String, ExecutionEnvironment> CACHE = new HashMap<>();

    @Override
    public ExecutionEnvironment getLocal() {
        return null;
    }

    @Override
    public ExecutionEnvironment createNew(String uri) {
        return fromUniqueID(uri);
    }

    @Override
    public ExecutionEnvironment createNew(String user, String host) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ExecutionEnvironment createNew(String user, String host, int port) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toUniqueID(ExecutionEnvironment executionEnvironment) {
        if (executionEnvironment instanceof DevelopVMExecutionEnvironment) {
            DevelopVMExecutionEnvironment env = (DevelopVMExecutionEnvironment) executionEnvironment;
            return DevelopVMExecutionEnvironment.encode(env.getMachineId(), env.getServerUrl());
        }
        return null;
    }

    @Override
    public ExecutionEnvironment fromUniqueID(String hostKey) {
        return CACHE.computeIfAbsent(hostKey, DevelopVMExecutionEnvironmentFactoryServiceImpl::parseString);
    }

    private static ExecutionEnvironment parseString(String hostKey) {
        String machineAtHost = hostKey.substring((CLOUD_PREFIX + "://").length());

        int delimIdx = machineAtHost.indexOf('@');

        String machine = machineAtHost.substring(0, delimIdx - 1);
        String host = machineAtHost.substring(delimIdx + 1);
        
        String displayName = String.join("@", machine, host);

        return new DevelopVMExecutionEnvironmentImpl(host, machine, displayName);
    }
}
