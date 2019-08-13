/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.odcs.cnd.execution;

import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.api.ODCSServer;
import static org.netbeans.modules.odcs.cnd.execution.DevelopVMExecutionEnvironment.CLOUD_PREFIX;
import org.netbeans.modules.odcs.cnd.impl.ODCSAuthManager;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ExecutionEnvironmentFactoryService.class, position = 50)
public class DevelopVMExecutionEnvironmentFactoryServiceImpl implements ExecutionEnvironmentFactoryService {

    private static final RequestProcessor RP = new RequestProcessor("Fetching a cloud execution environment", 3);

    private static final Map<String, ExecutionEnvironment> CACHE = new HashMap<>();

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
        return null;
    }

    @Override
    public ExecutionEnvironment createNew(String user, String host, int port) {
        return null;
    }

    @Override
    public String toUniqueID(ExecutionEnvironment executionEnvironment) {
        if (executionEnvironment instanceof DevelopVMExecutionEnvironment) {
            DevelopVMExecutionEnvironment env = (DevelopVMExecutionEnvironment) executionEnvironment;
            return DevelopVMExecutionEnvironment.encode(env.getUser(), env.getMachineId(), env.getSSHPort(), env.getServerUrl());
        }
        return null;
    }

    @Override
    public ExecutionEnvironment fromUniqueID(String hostKey) {
        if (!hostKey.startsWith(CLOUD_PREFIX)) {
            return null;
        }
        return CACHE.computeIfAbsent(hostKey, (key) -> {
            DevelopVMExecutionEnvironment env = DevelopVMExecutionEnvironment.decode(key);

            ODCSServer server = ODCSManager.getDefault().getServer(env.getServerUrl());

            boolean loggedInNow = ODCSAuthManager.getInstance().onLogin(env.getServerUrl(), (PasswordAuthentication pa) -> {
                RP.post(env::initializeOrWait);
            });

            if (!loggedInNow) {
                ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/cnd/resources/odcs.png", true);
                NotificationDisplayer.getDefault().notify(Bundle.connection_title(), icon, Bundle.connection_text(env.getDisplayName(), env.getServerUrl()), null);
            }

            return env;
        });
    }
}
