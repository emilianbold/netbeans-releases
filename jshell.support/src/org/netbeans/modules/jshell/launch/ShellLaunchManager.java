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

import com.sun.jdi.VirtualMachine;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
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
import java.util.function.BiConsumer;
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
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.jshell.project.ProjectUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
    private static final int HANDSHAKE_TIMEOUT = 1000;
    
    /**
     * Timeout for connecting.
     */
    private static final int CONNECT_TIMEOUT = 5000_000;
    
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
    
    /**
     * Terminates the JShellConnection on socket close. Relies on that JShell
     * processor loops reading the stream, and will receive EOF when the socket
     * closes.
     */
    private static final class NotifyInputStream extends FilterInputStream {
        JShellConnection c;
        Runnable callback;
        
        public NotifyInputStream(InputStream in) {
            super(in);
            this.c = c;
        }

        @Override
        public int read() throws IOException {
            int r = super.read();
            if (r == -1 && c != null) {
                // socket was closed
                c.getMachineAgent().disconnected(c);
            }
            return r;
        }
        
        private boolean closed;

        @Override
        public void close() throws IOException {
            boolean cl;
            synchronized (this) {
                cl = closed;
                closed  = true;
            }
            if (cl) {
                this.c.getMachineAgent().disconnected(c);
            }
            super.close();
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
    
    private static final int MAX_PROBE_COUNTER = 10;
    private static final int DEBUGGER_PROBE_DELAY = 200;
    
    /**
     * Waits until the debugger initializes its VirtualMachine and produces a key.
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
        
        String getKey() {
            if (readKey != null) {
                return readKey;
            }
            Session s = refSession.get();
            if (s == null) {
                return null;
            }
            return readKey = getAgentKey(s);
        }
    
        private boolean probe(JPDADebugger debugger, Session session) {
            String key = getKey();
            if (key == null) {
                LOG.log(Level.FINE, "NbJshell agent did not execute far enough; queueing until the agent connects back");
                return true;
            } 
            if ("".equals(key)) {
                return false;
            }
            this.readKey = key;
            ShellAgent agent;
            
            synchronized (ShellLaunchManager.this) {
                agent = registeredAgents.get(key);
                if (agent == null) {
                    LOG.log(Level.FINE, "Could not find agent matching key: {0}", key);
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
        return getAgentKey(debugger);
    }
    
    private String getAgentKey(JPDADebugger debugger) {
        List<JPDAClassType> classes = debugger.getClassesByName("org.netbeans.lib.jshell.agent.NbJShellAgent");
        if (classes == null || classes.size() != 1) {
            return null;
        }
        JPDAClassType ct = classes.get(0);
        for (Field ff : ct.staticFields()) {
            if ("debuggerKey".equals(ff.getName())) {
                String s = ff.getValue();
                if (s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"') {
                    return "";
                } 
                return s.substring(1, s.length() -1);
            }
        }
        return null;
    }
    
    private Session findWaitingDebugger(String authKey) {
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
    
    /**
     * Encapsulates management around JShell (remote) agent. The instance is (pre) allocated
     * when a port is requested for the agent to connect in. When the agent calls home, the instance
     * will receive address/port to connect with when agent service(s) is demanded. The listening
     * socket can be then closed.
     * <p/>
     */
    public static class ShellAgent {
        private final ShellLaunchManager    mgr;
        private final Project       project;
        private final ServerSocket  handshakeSocket;
        /**
         * True, if the agent is expected to be paired with a debugger session
         */
        private final boolean       expectDebugger;
        
        private final String        authorizationKey;


        private Session             debuggerSession;
        private VirtualMachine      debuggerMachine;
        
        private InetSocketAddress   connectAddress;
        
        private InputOutput         io;
        
        private boolean             closed;
        
        /**
         * The active instance, possibly null. Potentially may
         * contain multiple JShellConnections, if multiple parallel
         * JShells is supported in the future.
         */
        private JShellConnection    connection;
        
        public ShellAgent(ShellLaunchManager mgr, Project project, ServerSocket handshakeSocket, String authKey, boolean expectDebugger) {
            this.mgr = mgr;
            this.authorizationKey = authKey;
            this.project = project;
            this.handshakeSocket = handshakeSocket;
            this.expectDebugger = expectDebugger;
            
            LOG.log(Level.FINE, "ShellAgent allocated. Project = {0}, socket = {1}, authKey = {2}, debugger = {3}", new Object[] {
                project, handshakeSocket, authKey, expectDebugger
            });
        }
        
        ServerSocket getHandshakeSocket() {
            return handshakeSocket;
        }
        
        public InputOutput getIO() {
            return io;
        }
        
        void setIO(InputOutput io) {
            this.io = io;
        }

        public Project getProject() {
            return project;
        }

        public Session getDebuggerSession() {
            return debuggerSession;
        }

        public VirtualMachine getDebuggerMachine() {
            return debuggerMachine;
        }
        
        void destroy() throws IOException {
            LOG.log(Level.FINE, "ShellAgent destroyed: authKey = {0}, socket = {1}", new Object[] { authorizationKey, handshakeSocket });
            synchronized (this) {
                handshakeSocket.close();
                if (closed) {
                    return;
                }
                closed = true;
            }
            disconnected(connection);
            ShellLaunchEvent ev = new ShellLaunchEvent(mgr, this);
            mgr.fire(l -> l.agentDestroyed(ev));
        }
        
        public InetSocketAddress getHandshakeAddress() {
            return (InetSocketAddress)handshakeSocket.getLocalSocketAddress();
        }

        public String getAuthorizationKey() {
            return authorizationKey;
        }
        
        public void target(InetSocketAddress addr) throws IOException {
            Session curSession;
            
            synchronized (this) {
                if (this.connectAddress != null) {
                    throw new IOException("Duplicated handshake from agent {0}: " + addr);
                }
                this.connectAddress = addr;
                curSession = debuggerSession;
            }
            // FIXME: for non-debugger run, make the agent live and attach it to
            // its project.
            if (expectDebugger) {
                LOG.log(Level.FINE, "Agent authorized with {0}, expecting debuggger, have: {1}", 
                        new Object[] { authorizationKey, curSession});
                if (curSession == null) {
                    Session debSession = mgr.findWaitingDebugger(authorizationKey);
                    LOG.log(Level.FINE, "Searched for debugger session, got: {0}", debSession);
                    attachDebugger(debSession);
                    return;
                }
            }
            ShellLaunchEvent ev = new ShellLaunchEvent(mgr, this);
            mgr.fire(l -> l.handshakeCompleted(ev));
        }
        
        void attachDebugger(Session s) {
            boolean complete;
            if (s == null) {
                return;
            }
            synchronized (this) {
                if (debuggerSession != null && s != debuggerSession) {
                    throw new IllegalStateException("Debugger already attached");
                }
                LOG.log(Level.FINE, "Attaching debugger session {0}, current session = {1}, connectAddress = {2}", new Object[] { s, debuggerSession, connectAddress } );
                if (debuggerSession == s) {
                    // race between debugger and handshake
                    return;
                }
                debuggerSession = s;
                JPDADebugger dbg = s.lookupFirst(null, JPDADebugger.class);
                debuggerMachine = ((JPDADebuggerImpl)dbg).getVirtualMachine();
                dbg.addPropertyChangeListener(JPDADebugger.PROP_STATE, e -> {
                    if (dbg.getState() == JPDADebugger.STATE_DISCONNECTED) {
                        // destroy the agent
                        RP.post(() -> {
                            mgr.destroyAgent(authorizationKey);
                        }, 5000);
                    }
                });
                complete =  connectAddress != null;
            }
            
            if (complete) {
                LOG.log(Level.FINE, "Firing handshake complete for {0}", this);
                ShellLaunchEvent ev = new ShellLaunchEvent(mgr, this);
                mgr.fire(l -> l.handshakeCompleted(ev));
            }
        }
        
        public void disconnected(JShellConnection c) {
            synchronized (this) {
                if (c == null || connection != c) {
                    return;
                }
                this.connection = null;
            }
            c.notifyDisconnected();
            if (closed) {
                // do not fire closed events after the agent was destroyed
                return;
            }
            if (handshakeSocket.isClosed()) {
                try {
                    destroy();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                ShellLaunchEvent ev = new ShellLaunchEvent(mgr, c);
                mgr.fire(l -> l.connectionClosed(ev));
            }
        }
        
        @NbBundle.Messages({
            "MSG_AgengConnectionBroken=Control connection with JShell VM is broken, could not connect to agent"
        })
        /**
         * Creates a connection to the JShell agent. Throws IOException if 
         * the connection fails. If a connection already exists, return the existing
         * connection.
         * <p/>
         * Will return {@code null}, if the agent is not yet initialized.
         * @return 
         */
        public JShellConnection createConnection() throws IOException {
            JShellConnection old;
            synchronized (this) {
                if (closed) {
                    throw new IOException(Bundle.MSG_AgengConnectionBroken());
                }
                if (expectDebugger && debuggerMachine == null) {
                    return null;
                }
                if (connection != null && connection.isValid()) {
                    return connection;
                }
                old = connection;
                connection = null;
            }
            if (old != null) {
                old.notifyDisconnected();
            }
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(true);
            Socket sock = sc.socket();
            sock.connect(connectAddress, CONNECT_TIMEOUT);
            
            // turn to nonblocking mode
            sc.configureBlocking(false);
            boolean notify = false;
            JShellConnection con = new JShellConnection(this, sock.getChannel());
            synchronized (this) {
                if (connection == null) {
                    connection = con;
                    notify = true;
                } else {
                    con = connection;
                }
            }
            if (notify) {
                ShellLaunchEvent ev = new ShellLaunchEvent(mgr, con);
                mgr.fire(l -> l.connectionInitiated(ev));
            }
            return con;
        }
        
    }
    
    /* package */ void destroyAgent(String authKey) {
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
}
