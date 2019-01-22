/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.cnd;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.RemoteMachineAccessor;
import org.netbeans.modules.team.server.ui.spi.RemoteMachineHandle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * 
 */
@ServiceProvider(service=RemoteMachineAccessor.class)
public class RemoteMachineAccessorImpl extends RemoteMachineAccessor<ODCSProject>{

    private static final Logger LOG = Logger.getLogger(RemoteMachineAccessorImpl.class.getName());
    
    @Override
    public Class<ODCSProject> type() {
        return ODCSProject.class;
    }
    
    @Override
    public boolean hasRemoteMachines(ProjectHandle<ODCSProject> project) {
        // XXX should check if there are any for the given project
        return true; 
    }

    @Override
    public List<RemoteMachineHandle> getRemoteMachines(ProjectHandle<ODCSProject> project) {
        if(hasRemoteMachines(project)) {            
            Collection<String> rms = getRemoteMachinesIntern(project);
            List<RemoteMachineHandle> ret = new LinkedList<>();
            for (String rm : rms) {
                ret.add(new RemoteMachineHandleImpl(rm));
            }
            
            // XXX propagate list of remote machines to cnd ...
            
            return ret;
        }
        return null;
    }
    
    /**
     * TODO - Dummy implementation. 
     * 
     * Should be changed depending on how the remote machines for a DCS project 
     * will be retrieved:
     * 
     * - either via the org.netbeans.modules.odcs.client.ODCSClientImpl which 
     *   is based on the existing ODCS service API 
     * 
     * - or if remote machines will have their own way how to get the necessary 
     *   info own api/client impl based merely on an url provided by the project/server
     * 
     * @return 
     */
    private List<String> getRemoteMachinesIntern(ProjectHandle<ODCSProject> project) {
        waitAMoment(500);
        List<String> ret = readFromFile();
        return ret != null ? ret : Arrays.asList(new String[] {"Tester Remote Host,tester@192.168.1.1:5555", "BetaTester Remote Host,beta_tester@192.168.1.2:22"});
    }

    /**
     * Mock data. 
     * 
     * odcs.mock.remoteMachinesFile should point to a CSV file, 
     * where each line contains a remote host description.
     * [host display name],[host url]
     * Some Host,tester@192.168.1.1:8888
     * 
     * @return
     */
    private List<String> readFromFile() {
        String remoteMachinesFile = System.getProperty("odcs.mock.remoteMachinesFile");
        if (remoteMachinesFile != null && !remoteMachinesFile.trim().isEmpty()) {
            File f = new File(remoteMachinesFile);
            if (f.exists()) {
                try (final BufferedReader br = new BufferedReader(new FileReader(f))) {
                    List<String> ret = new LinkedList<>();
                    String line;
                    while( (line = br.readLine()) != null) {
                        ret.add(line);
                    }
                    return ret;
                }catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }
    
    private void waitAMoment(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static class RemoteMachineHandleImpl extends RemoteMachineHandle {
        private final String name;
        private final String url;

        public RemoteMachineHandleImpl(String rm) {
            String[] s = rm.split(",");
            this.name = s[0];
            this.url = s[1];
        }
        
        @Override
        public String getDisplayName() {
            return name + ": " + url;
        }

        @Override
        public Action getDefaultAction() {
            return new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectNode(url); // XXX fixme!
                }
            };
        }
        
    }
    
    /**
     * Try to select a node somewhere beneath the root node in the Services tab.
     * @param path a path as in {@link NodeOp#findPath(Node, String[])}
     */
    public static void selectNode(final String... path) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                TopComponent tab = WindowManager.getDefault().findTopComponent("services"); // NOI18N
                if (tab == null) {
                    // XXX have no way to open it, other than by calling ServicesTabAction
                    LOG.fine("No ServicesTab found");
                    return;
                }
                tab.open();
                tab.requestActive();
                if (!(tab instanceof ExplorerManager.Provider)) {
                    LOG.fine("ServicesTab not an ExplorerManager.Provider");
                    return;
                }
                final ExplorerManager mgr = ((ExplorerManager.Provider) tab).getExplorerManager();
                final Node root = mgr.getRootContext();
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {                        
                        Node buildHosts = NodeOp.findChild(root, "remote");
                        if (buildHosts == null) {
                            LOG.fine("ServicesTab does not contain C/C++ Build Hosts");
                            return;
                        }
                        Node _selected;
                        try {
                            _selected = NodeOp.findPath(buildHosts, path);
                        } catch (NodeNotFoundException x) {
                            LOG.log(Level.FINE, "Could not find subnode", x);
                            ExecutionEnvironment ee = ExecutionEnvironmentFactory.fromUniqueID(path[0]);
                            if (ee != null) {
                                ServerRecord serverRecord = ServerList.addServer(ee, null, RemoteSyncFactory.getDefault(), false, true);
                                if (serverRecord != null) {
                                    try {
                                        _selected = NodeOp.findPath(buildHosts, path);
                                    } catch (NodeNotFoundException ex) {
                                        LOG.log(Level.FINE, "Could not find created subnode", ex);
                                        _selected = x.getClosestNode();
                                    }
                                } else {
                                    LOG.log(Level.FINE, "Could not create subnode", ee.toString());
                                    _selected = x.getClosestNode();
                                }
                            } else {
                                _selected = x.getClosestNode();
                            }
                        }
                        final Node selected = _selected;
                        Mutex.EVENT.readAccess(new Runnable() {
                            public void run() {
                                try {
                                    mgr.setSelectedNodes(new Node[] {selected});
                                } catch (PropertyVetoException x) {
                                    LOG.log(Level.FINE, "Could not select path", x);
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
