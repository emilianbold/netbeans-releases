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
package jdk.internal.jshell.remote;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import static jdk.internal.jshell.remote.RemoteCodes.RESULT_KILLED;
import org.netbeans.lib.jshell.agent.NbJShellAgent;

/**
 *
 * @author sdedic
 */
public class AgentWorker extends RemoteAgent implements Executor, Runnable {
    private static final Logger LOG = Logger.getLogger("org.netbeans.lib.jshell.agent.AgentWorker"); // NOI18N
    
    public static final String PROPERTY_EXECUTOR = "org.netbeans.lib.jshell.agent.AgentWorker.executor"; // NOI18N
    
    /**
     * Reference set by instrumented classes
     */
    public static volatile ClassLoader referenceClassLoader;
            
    /**
     * The JShell agent main with options read from commandline
     */
    private final NbJShellAgent   agent;
    
    /**
     * The control socket
     */
    private final Socket socket;
    private final int   socketPort;

    /**
     * The Classloader last obtained from a field or method
     */
    private ClassLoader lastClassLoader;
    
    /**
     * Provider for the classloader. Contacted before every user invocation
     * and before every class load.
     */
    private Callable<ClassLoader>   loaderProvider;
    

    /**
     * Executor for user's code. Can be provided from the environment using System.properties,
     * see {@link #PROPERTY_EXECUTOR}.
     */
    private Executor userExecutor;
    
    /**
     * Threads which execute user code, keyed by agent's socket local port.
     */
    // @GuardedBy(self)
    private final static Map<Integer, Thread>     userCodeExecutingThreads = new HashMap<Integer, Thread>();
    
    private AgentWorker() {
        agent = null;
        socket = null;
        socketPort = -1;
        this.userExecutor = findExecutor();
    }
    
    private Executor findExecutor() {
        Object o = System.getProperties().get(PROPERTY_EXECUTOR);;
        if (o instanceof Executor) {
            return this.userExecutor = (Executor)o;
        } else if (o instanceof String) {
            try {
                Class executorClazz = Class.forName((String)o);
                return (Executor)executorClazz.newInstance();
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return this;
    }
    
    private static class LoaderAccessor implements Callable<ClassLoader> {
        private final ClassLoader defaultLoader;
        
        public LoaderAccessor(ClassLoader defaultLoader) {
            this.defaultLoader = defaultLoader;
        }

        @Override
        public ClassLoader call() throws Exception {
            if (referenceClassLoader != null) {
                return referenceClassLoader;
            } else {
                return defaultLoader;
            }
        }
    }
    
    private class LoaderEvaluator implements Callable<ClassLoader> {
        private Class   clazz;
        private Method  method;
        private Field   field;
        
        public ClassLoader call() throws Exception {
            if (clazz == null) {
                try {
                    clazz = Class.forName(agent.getClassName(), false, loader);
                } catch (ClassNotFoundException ex) {
                    // the class may not be loaded yet, use the default loader now
                    return loader;
                }
                String m = agent.getMethod();
                String f = agent.getField();
                if (m != null) {
                    method = clazz.getDeclaredMethod(m);
                    if (!method.getReturnType().isAssignableFrom(ClassLoader.class) ||
                         (method.getModifiers() & Modifier.STATIC) == 0) {
                        throw new IllegalStateException("Loader access method must be static and return ClassLoader");
                    }
                    method.setAccessible(true);
                } else if (f != null) {
                    field = clazz.getDeclaredField(f);
                    field.setAccessible(true);
                    if (!field.getType().isAssignableFrom(ClassLoader.class) ||
                         (field.getModifiers() & Modifier.STATIC) == 0) {
                        throw new IllegalStateException("Loader access field must be static and assignable to ClassLoader");
                    }
                }
            }
            
            if (method != null) {
                return (ClassLoader)method.invoke(null);
            } else if (field != null) {
                return (ClassLoader)field.get(null);
            } else {
                return loader;
            }
        }
    }
    
    private void installNewClassLoader(ClassLoader delegate) {
        lastClassLoader = delegate;
        loader = new NbRemoteLoader(delegate, loader);
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
    
    @Override
    protected void prepareClassLoader() {
        try {
            ClassLoader current = loaderProvider.call();
            if (current != lastClassLoader && current != loader) {
                installNewClassLoader(current);
            }
        } catch (Exception ex) {
            // don't touch
        }
    }
    
    public AgentWorker(NbJShellAgent agent, Socket controlSocket) {
        loader = new NbRemoteLoader(ClassLoader.getSystemClassLoader(), null);
        
        this.agent = agent;
        this.socket = controlSocket;
        this.socketPort = controlSocket.getLocalPort();
        if (agent.getField() != null || agent.getMethod() != null) {
            loaderProvider = new LoaderEvaluator();
        } else if (agent.getClassName() != null) {
            loaderProvider = new LoaderAccessor(loader);
        } else {
            loaderProvider = new Callable<ClassLoader>() {
                public ClassLoader call() {
                    return loader;
                }
            };
        }
        this.userExecutor = findExecutor();
    }
    
    public static void main(String[] args) throws Exception {
        String loopBack = null;
        Socket socket = new Socket(loopBack, Integer.parseInt(args[0]));
        try {
            (new AgentWorker()).commandLoop(new ObjectInputStream(socket.getInputStream()),
                    new ObjectOutputStream(socket.getOutputStream()));
        } catch (EOFException ex) {
            // ignore, forcible close by the tool
        }
    }
    
    @Override
    public void run() {
        // reset the classloader
        ObjectOutputStream osm = null;
        ObjectInputStream ism = null;
        try {
            // will block, but this is necessary so the IDE eventually sets the debuggerKey
            osm = new ObjectOutputStream(socket.getOutputStream());
            // will read immediately
            ism = new ObjectInputStream(socket.getInputStream());
            commandLoop(ism, osm);
        } catch (EOFException ex) {
            // expected.
        } catch (IOException ex) {
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (osm != null) {
                try {
                    osm.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
            if (ism != null) {
                try {
                    ism.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }
    
    /**
     * Collect and send information about the executing VM.
     */
    public static final int CMD_VM_INFO   = 100;
    
    public static final int CMD_REDEFINE   = 101;
    
    public static final int CMD_STOP        = 102;
    
    public static final int CMD_CLASSID     = 103;
    
    /**
     * Find out reference identity of a class.
     */
    public static final int CMD_TYPE_ID   = 101;
    
    private Pattern EXCLUDE_CLASSPATH_ITEMS = Pattern.compile(
              "lib/tools.jar$|"
            + "modules/ext/nb-custom-jshell-probe.jar$"
    );
        

    private void returnVMInfo(ObjectOutputStream o) throws IOException {
        Map<String, String>  result = new HashMap<String, String>();
        Properties props = System.getProperties();
        for (String s : props.stringPropertyNames()) {
            if (!s.startsWith("java")) { // NOI18N
                continue;
            }
            result.put(s, props.getProperty(s));
        }
        LOG.log(Level.FINE, "Sending properties: " + props);
        
        prepareClassLoader();
        StringBuilder cp = new StringBuilder();
        for (URL u: loader.getURLs()) {
            try {
                File f = new File(u.toURI());
                String s = f.getPath();
                if (EXCLUDE_CLASSPATH_ITEMS.matcher(s).find()) {
                    continue;
                }
                if (cp.length() > 0) {
                    cp.append(":"); // NOI18N
                }
                cp.append(f.getPath());
            } catch (URISyntaxException ex) {
                cp.append(u.toExternalForm());
            }
        }
        for (String s : props.getProperty("java.class.path").split(File.pathSeparator)) {  // NOI18N
            if (s.isEmpty()) {
                continue;
            }
            if (EXCLUDE_CLASSPATH_ITEMS.matcher(s).find()) {
                continue;
            }
            if (cp.length() > 0) {
                cp.append(":"); // NOI18N
            }
            cp.append(s);
        }
        LOG.log(Level.FINE, "Classloader path: " + cp);
        result.put("nb.class.path", cp.toString()); // NOI18N
        
        o.writeInt(result.size());
        for (String s : result.keySet()) {
            o.writeUTF(s);
            o.writeUTF(result.get(s));
        }
        o.flush();
    }
    
    private ClassLoader contextLoader;
    
    void clientCodeEnter() {
        LOG.log(Level.FINER, "Entering client code");
        this.contextLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        super.clientCodeEnter();
    }
    
    private AtomicBoolean killed = new AtomicBoolean();
//    private boolean killed;
    
    /**
     * Removes our thread from the user-executing map.
     * It's important that clientCodeLeave is called BEFORE the agent starts to send
     * response code: either the thread is removed here, and the response code is sent by
     * the original Agent's code, or the entry is removed by the killer agent before/while
     * executing agent is waiting to lock the map - and in that case the `killed' will be set 
     * to true and response code is produced in performExecute. Since ThreadDeath is thrown, 
     * the executing agent will not complete the synchronized block normally
     */
    void clientCodeLeave() {
        super.clientCodeLeave();
        LOG.log(Level.FINER, "Exiting client code");
        Thread.currentThread().setContextClassLoader(contextLoader);
        synchronized (userCodeExecutingThreads) {
            Thread t = userCodeExecutingThreads.get(socketPort);
            if (t == Thread.currentThread()) {
                killed.set(userCodeExecutingThreads.remove(socketPort) != null);
            }
            LOG.log(Level.FINER, "User code killed: {0}", killed);
        }
    }
    
    private void performExecute(int cmd, ObjectInputStream in, ObjectOutputStream out, CountDownLatch latch) throws IOException {
        killed.set(false);
        try {
            synchronized (userCodeExecutingThreads) {
                userCodeExecutingThreads.put(socketPort, Thread.currentThread());
            }
            super.performCommand(cmd, in, out);
            return;
        } catch (ThreadDeath td) {
            LOG.log(Level.FINE, "Received ThreadDeath, killed: {0}", killed);
            if (!killed.get()) {
                throw td;
            }
        } catch (Throwable t) {
            killed.set(true);
        } finally {
            latch.countDown();
        }
        out.writeInt(RESULT_KILLED);
        out.flush();
    }
    
    @Override
    protected void performCommand(final int cmd, final ObjectInputStream in, final ObjectOutputStream out) throws IOException {
        try {
            switch (cmd) {
                case RemoteCodes.CMD_INVOKE: {
                    final IOException [] err = new IOException[1];
                    final CountDownLatch execLatch = new CountDownLatch(1);
                    // for Graalists :)
                    userExecutor.execute(new Runnable() {
                        public void run() {
                            try {
                                performExecute(cmd, in, out, execLatch);
                            } catch (IOException ex) {
                                err[0] = ex;
                            }
                        }
                    });
                    execLatch.await();
                    if (err[0] != null) {
                        throw err[0];
                    }
                    break;
                }
                case CMD_VM_INFO:
                    returnVMInfo(out);
                    break;
                case CMD_REDEFINE:
                    performRedefineClasses(in, out);
                    break;
                case CMD_CLASSID:
                    performClassId(in, out);
                    break;
                case CMD_STOP:
                    performStop(in, out);
                    break;
                default:
                    super.performCommand(cmd, in, out);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.err.flush();
        }
    }
    
    /**
     * Executes STOP command. Note that STOP comes through <b>different</b> agent than
     * the one which should stop the user's code; the controlling process opens connection
     * and instructs the VM to stop running thread.
     * @param in
     * @param out
     * @throws IOException 
     */
    @SuppressWarnings({"deprecation", "CallToThreadStopSuspendOrResumeManager"})
    private void performStop(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        int agentId = in.readInt();
        Thread targetThread;
        
        synchronized (userCodeExecutingThreads) {
            targetThread = userCodeExecutingThreads.remove(agentId);
            if (targetThread != null) {
                // throw ThreadDeath in the target thread
                targetThread.stop();
            }
        }
        if (targetThread != null) {
            out.writeInt(RemoteCodes.RESULT_SUCCESS);
        } else {
            out.writeInt(RemoteCodes.RESULT_FAIL);
        }
        out.flush();
    }
    
    private void performRedefineClasses(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        int count = in.readInt();
        try {
            for (int i = 0; i < count; i++) {
                long classId = in.readLong();
                byte[] replaceBytecode = (byte[]) in.readObject();
                Class defined = ((NbRemoteLoader) loader).getClassOfId(classId);

                agent.getInstrumentation().redefineClasses(
                        new ClassDefinition(defined, replaceBytecode)
                );
            }
            out.writeInt(RemoteCodes.RESULT_SUCCESS);
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.FINE, "Class redefinition failed");
            out.writeInt(RemoteCodes.RESULT_FAIL);
            out.writeInt(RemoteCodes.RESULT_SUCCESS);
        } catch (LinkageError ex) {
            handleRedefineException(ex, out);
        } catch (NullPointerException ex) {
            handleRedefineException(ex, out);
        } catch (ClassNotFoundException ex) {
            handleRedefineException(ex, out);
        } catch (UnmodifiableClassException ex) {
            handleRedefineException(ex, out);
        }
        out.flush();
    }
    
    private void handleRedefineException(Throwable ex, ObjectOutputStream out) throws IOException {
        LOG.log(Level.WARNING, "Could not redefine class: ", ex);
        out.writeInt(RemoteCodes.RESULT_FAIL);
        out.writeInt(RemoteCodes.RESULT_FAIL);
        out.writeUTF(ex.toString());
    }
    
    private void performClassId(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        String className = in.readUTF();
        Long id = ((NbRemoteLoader)loader).getClassId(className);
        out.writeInt(RemoteCodes.RESULT_SUCCESS);
        if (id == null) {
            out.writeLong(-1);
        } else {
            out.writeLong(id);
        }
        out.flush();
    }

    @Override
    protected void handleUnknownCommand(int cmd, ObjectInputStream i, ObjectOutputStream o) throws IOException {
    }
 }
