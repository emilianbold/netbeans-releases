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
package org.netbeans.modules.jshell.launch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.project.ProjectUtils;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.InputOutput;

/**
 * Accumulates information on JShell running as a part of a project launch.
 * The manager intercepts process executions (run, debug) by {@link StartupExetnder},
 * debugger sessions, and tries to link them together. Manages sockets available for
 * JShell remote agents to connect and reports events when a process starts,
 * JShell agent connects, and when the JShell agent disconnects.
 * <p/>
 * 
 * @author sdedic
 */
@ServiceProvider(service = ShellLaunchManager.class)
public final class ShellLaunchManager {
    
    private static final Logger LOG = Logger.getLogger(ShellLaunchManager.class.getName());
    
    /**
     * How long the handshake can take.
     */
    public static final int HANDSHAKE_TIMEOUT = 1000;
    
    /**
     * Timeout for connecting.
     */
    public static final int CONNECT_TIMEOUT = 5000_000;
    
    /**
     * This RP will run one task that waits for the handshakes to begin, and some scheduled short-lived "timeout" tasks.
     */
    private static final RequestProcessor RP = new RequestProcessor("JShell monitor", 5); // NOI18N
    
    /**
     * Random generator to generate agent authentication. Set Loglevel for this class to FINE or more detailed to get seed 0
     */
    private final Random    keyGenerator = new Random(LOG.isLoggable(Level.FINE) ? 0 : System.currentTimeMillis());
    
    /**
     * Session key which pairs invocations to agents and to debuggers.
     */
    private AtomicInteger   sessionKey = new AtomicInteger(1);

    /**
     * Selector for agent handshake sockets
     */
    private Selector servers;
    
    // @GuardedBy(self)
    private List<ShellLaunchListener>   listeners = new ArrayList<>();
    
    public static ShellLaunchManager getInstance() {
        return Lookup.getDefault().lookup(ShellLaunchManager.class);
    }
    
    /**
     * Collects all known or preallocated agents. Keyed by agent authorization key.
     */
    // @GuardedBy(this)
    private Map<String, ShellAgent>    registeredAgents = new HashMap<>();
    
    /**
     * Agents attached to a project. A project may have multiple agents, if the
     * user run the project multiple times.
     */
    // @GuardedBy(this)
    private Map<Project, Collection<ShellAgent>>    projectAgents = new HashMap<>();
    
    // @GuardedBy(this)
    private Set<String> usedKeys = new HashSet<>();
    
    /**
     * Agents queued for registering on the servers selector.
     */
    // @GuardedBy(self)
    private final List<ShellAgent> requests = new ArrayList<>();
    
    /**
     * Registers the connection and initiates the listening socket.
     * @param project the project which is being run / debugged
     * @param debugger if true, the connection will pair with a debugger session.
     */
    public ShellAgent openForProject(Project p, boolean debugger) throws IOException {
        // first check that the project has JShell enabled:
        if (!ProjectUtils.isJShellRunEnabled(p)) {
            LOG.log(Level.FINE, "Request for agent: Project {0} does not enable jshell.", p);
            return null;
        }
        ServerSocket ss;
        
        String encodedKey;
        boolean shouldInit = false;
        
        synchronized (this) {
            shouldInit = usedKeys.isEmpty();
            do {
                BigInteger key = BigInteger.probablePrime(64, keyGenerator);
                encodedKey = key.toString(Character.MAX_RADIX);
            } while (!usedKeys.add(encodedKey));
        }
        
        if (shouldInit) {
            init();
        }
        
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        SocketAddress local = new InetSocketAddress(
            // PENDING: choose something better for remote debugging!
            InetAddress.getLoopbackAddress(),
            0);
        ssc.bind(local);
        ssc.accept();
        ss = ssc.socket();
        LOG.log(Level.FINE, "Creating new server socket {0} for {1}", new Object[] {
            ss, p
        });
        ShellAgent agent = new ShellAgent(this, p, ss, encodedKey, debugger);
        synchronized (this) {
            registeredAgents.put(encodedKey, agent);
        }
        synchronized (requests) {
            servers.wakeup();
            requests.add(agent);
            
        }
        return agent;
    }
    
    private void init() throws IOException {
        if (servers != null) {
            return;
        }
        LOG.log(Level.FINE, "Initializing");
        servers = Selector.open();
        DebuggerManager.getDebuggerManager().addDebuggerListener(new DebuggerManagerAdapter() {
            @Override
            public void sessionAdded(Session session) {
                LOG.log(Level.FINE, "Debugger: session added: {0}", session);
                Project p = ProjectUtils.getSessionProject(session);
                JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
                RP.post(new WaitForDebuggerStart(session, p));
            }
        });
        RP.post(new ShellAgentMonitor());
    }
    
    /**
     * Debugger sessions, which were created, VirtualMachine is available AND
     * they contain the JShell agent code, but the agent did not run far enough
     * yet to produce a asscoiation key.
     */
    // @GuardedBy(self)
    private List<WaitForDebuggerStart> uninitializedDebuggers = new ArrayList<>();
    
    /**
     * Monitors listening sockets for individual projects, in nonblocking mode.
     * When a connection comes, performs the initial handshake (as it is expected to be
     * fairly fast), and attaches the connection to an appropriate JShellConnection.
     */
    private class ShellAgentMonitor implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (requests) {
                        for (ShellAgent a : requests) {
                            ServerSocketChannel ssc = a.getHandshakeSocket().getChannel();
                            try {
                                ssc.register(servers, SelectionKey.OP_ACCEPT, a);
                            } catch (IOException | IllegalStateException ex) {
                                // just ignore
                            }
                        }
                    }
                    servers.select();
                    Set<SelectionKey> keys = servers.selectedKeys();
                    for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); ) {
                        SelectionKey k = it.next();
                        if (!k.isValid()) {
                            break;
                        }
                        if (k.isAcceptable()) {
                            try (SocketChannel ss = ((ServerSocketChannel)k.channel()).accept()) {
                                if (ss == null) {
                                    continue;
                                }
                                LOG.fine("Accepted socket " + ss);
//                                    ((ProjectData)k.attachment()).accept(
//                                        ss
//                                    );
                                processHandshake(ss);
                            } catch (IOException ex) {
                                LOG.log(Level.INFO, "Error during JShell agent handshake", ex);
                            }
                        }
                        it.remove();
                    }
                } catch (ClosedSelectorException ex) {
                    LOG.fine("Selector closed");
                    break;
                } catch (IOException | RuntimeException ex) {
                    // no op
                    LOG.log(Level.FINE, "Error occurred during connection handling", ex);
                }
            }
        }
        
        private void processHandshake(SocketChannel accepted) throws IOException {
            accepted.configureBlocking(true);
            Socket sock = accepted.socket();
            sock.setSoTimeout(HANDSHAKE_TIMEOUT);
            
            ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
            String authorizationKey = is.readUTF();
            LOG.log(Level.FINE, "Approaching agent with authorization key: {0}", authorizationKey);
            ShellAgent agent;
            
            synchronized (ShellLaunchManager.this) {
                agent = registeredAgents.get(authorizationKey);
            }
            if (agent == null) {
                LOG.log(Level.INFO, "Connection on JShell agent port with improper authorization ({0}) from {1}", new Object[] {
                    authorizationKey,
                    sock
                });
                return;
            }
            
            // read the port
            int targetPort = is.readInt();
            InetSocketAddress connectTo = new InetSocketAddress(
                    ((InetSocketAddress)sock.getRemoteSocketAddress()).getAddress(), targetPort);
            
            agent.target(connectTo);
        }
    }
    
    public void addLaunchListener(ShellLaunchListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeLaunchListener(ShellLaunchListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    /* private */ void attachInputOutput(String remoteKey, InputOutput out) {
        ShellAgent ag;
        synchronized (registeredAgents) {
            ag = registeredAgents.get(remoteKey);
        }
        if (ag == null) {
            LOG.log(Level.FINE, "Unregistered agent for key: {0}", remoteKey);
        } else {
            ag.setIO(out);
        }
    }
    
    /**
     * How many times will the debugger connector try to obtain the JDI VirtualMachine
     * before giving up. The VM is not available immediately after a session launch.
     */
    private static final int MAX_PROBE_COUNTER = 10;
    
    /**
     * Delay between attempts to get a VM for the debugging session
     */
    private static final int DEBUGGER_PROBE_DELAY = 200;
    
    /**
     * Waits until the debugger initializes its VirtualMachine and produces a key.
     * Fails after {@link #MAX_PROBE_COUNTER} attempts each with {@link #DEBUGGER_PROBE_DELAY}
     * milliseconds delay.
     */
    private class WaitForDebuggerStart implements Runnable, PropertyChangeListener {
        final Reference<Session>          refSession;
        final Project                     project;
        
        int                               probeCounter;
        boolean                           stop;
        volatile String                   readKey;

        public WaitForDebuggerStart(Session session, Project project) {
            this.refSession = new WeakReference<>(session);
            this.project = project;
        }
        
        private void stop() {
            synchronized (ShellLaunchManager.this) {
                uninitializedDebuggers.remove(this);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JPDADebugger deb = (JPDADebugger)evt.getSource();
            if (deb != null && deb.getState() == JPDADebugger.STATE_DISCONNECTED) {
                deb.removePropertyChangeListener(this);
                stop();
            }
        }
        
        public void run() {
            Session session = refSession.get();
            if (stop || session == null) {
                stop();
                return;
            }
            JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) {
                stop();
                return;
            }
            
            debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, this);
            if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
                stop();
                return;
            }
            
            try {
                if (probeCounter == 0) {
                    waitVirtualMachine(debugger, session);
                } else if (probeCounter >= MAX_PROBE_COUNTER) {
                    stop();
                    return;
                }
                if (probe(debugger, session)) {
                    RP.schedule(this, DEBUGGER_PROBE_DELAY, TimeUnit.MILLISECONDS);
                    probeCounter++;
                }
            } catch (DebuggerStartException ex) {
                // the debugger fails to start, terminate the connection
                LOG.log(Level.FINE, "Exception during debugger start: {0}", ex);
                stop();
            }
        }
        
        private void waitVirtualMachine(JPDADebugger debugger, Session session) throws DebuggerStartException {
            LOG.log(Level.FINE, "Waiting for debugger to create VM for project {0} in thread {1}", new Object[] { project, Thread.currentThread() });
            debugger.waitRunning();
            LOG.log(Level.FINE, "Debugger VirtualMachine created for project {0} in thread {1}", new Object[] { project, Thread.currentThread() });
            synchronized (ShellLaunchManager.this) {
                // add first, so if there's a race between agent and us, the agent will also query the debugger. We cannot hold lock over the remote call.
                uninitializedDebuggers.add(this);
            }
        }
        
        public String getKey() {
            if (readKey != null) {
                return readKey;
            }
            Session s = refSession.get();
            if (s == null) {
                return null;
            }
            return readKey = ShellDebuggerUtils.getAgentKey(s);
        }
    
        private boolean probe(JPDADebugger debugger, Session session) {
            String key = getKey();
            if (key == null) {
                LOG.log(Level.FINE, "NbJshell agent did not execute far enough; queueing until the agent connects back"); // NOI18N
                return true;
            } 
            if ("".equals(key)) { // NOI18N
                return false;
            }
            this.readKey = key;
            ShellAgent agent;
            
            synchronized (ShellLaunchManager.this) {
                agent = registeredAgents.get(key);
                if (agent == null) {
                    LOG.log(Level.FINE, "Could not find agent matching key: {0}", key); // NOI18N
                    return false;
                }
            }
            synchronized (ShellLaunchManager.this) {
                uninitializedDebuggers.remove(this);
            }
            agent.attachDebugger(session);
            // no longer uninitialized :)
            return false;
        }
    }
    
    private String getAgentKey(Session debuggerSession) {
        JPDADebugger debugger = debuggerSession.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return null;
        }
        List<JPDAClassType> classes = debugger.getClassesByName("org.netbeans.lib.jshell.agent.NbJShellAgent"); // NOI18N
        if (classes == null || classes.size() != 1) {
            return null;
        }
        JPDAClassType ct = classes.get(0);
        for (Field ff : ct.staticFields()) {
            if ("debuggerKey".equals(ff.getName())) {  // NOI18N
                String s = ff.getValue();
                if (s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"') {
                    return "";
                } 
                return s.substring(1, s.length() -1);
            }
        }
        return null;
    }
    
    /* package-private, for ShellAgent */ Session findWaitingDebugger(String authKey) {
        List<WaitForDebuggerStart> al;
        synchronized (uninitializedDebuggers) {
            al = new ArrayList<>(uninitializedDebuggers);
        }
        
        for (WaitForDebuggerStart d : al) {
            if (authKey.equals(d.getKey())) {
                synchronized (uninitializedDebuggers) {
                    Session s = d.refSession.get();
                    uninitializedDebuggers.remove(d);
                    return s;
                }
            }
        }
        
        return null;
    }
    
    void fire(Consumer<ShellLaunchListener> c) {
        listeners.stream().forEach(c);
    }
    
    void destroyAgent(String authKey) {
        if (authKey == null || "".equals(authKey)) {
            return;
        }
        ShellAgent agent;
        synchronized (this) {
            agent = registeredAgents.remove(authKey);
            if (agent == null) {
                return;
            }
        }
        try {
            agent.destroy();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "JShell agent shut down unsuccessfully:", ex);
        }
        // PENDING: fire event that agent has been destroyed
    }


    static void queueTask(Runnable run, int delay) {
        RP.post(run);
    }
}
