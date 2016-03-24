/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.lib.jshell.agent;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NetBeans JShell agent wrapper. Handles the initial handshake between NetBeans IDE and JShell,
 * and listens for incoming connections. The initial handshake is initiated by this process, it 
 * sends back the authorization key and port number opened for listening. The IDE then connects
 * back as necessary, when the user opens or resets a JShell terminal.
 * <p/>
 * The real agent execution is a little peculiar, since the agent needs to manipulate bytecode and
 * I didn't want to do all that manually; so it uses ObjectWeb ASM library - but the lib should not
 * appear on application classpath for possible clashes with app's own libraries. A private classloader
 * is used to load the agent itself.
 * 
 * @author sdedic
 */
public class NbJShellAgent implements Runnable {
    /**
     * This field will be initialized at startup. The IDE will grab the value
     * using JDI to associate a debugger Session with the appropriate incoming socket.
     */
    public volatile static String debuggerKey = ""; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(NbJShellAgent.class.getName());
    
    private static NbJShellAgent INSTANCE;
    
    private InetAddress  address;
    private int          port;
    private List<String>    libraries = Collections.emptyList();
    private boolean useReflection;
    private String  className;
    private String  field;
    private String  method;
    private String  key;
    private Instrumentation instrumentation;
    
    private ClassLoader agentClassLoader;

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }
    public String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    public InetAddress getAddress() {
        return address;
    }

    void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public boolean isUseReflection() {
        return useReflection;
    }

    void setUseReflection(boolean useReflection) {
        this.useReflection = useReflection;
    }

    public String getClassName() {
        return className;
    }

    void setClassName(String className) {
        this.className = className;
    }

    public String getField() {
        return field;
    }

    void setField(String field) {
        this.field = field;
    }

    public String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }
    
    public ClassLoader createClassLoader() {
        if (agentClassLoader != null) {
            return agentClassLoader;
        }
        if (libraries.isEmpty()) {
            LOG.log(Level.FINE, "Creating standard classloader");
            return getClass().getClassLoader();
            
        }
        LOG.log(Level.FINE, "Creating custom classloader");
        List<URL> urls = new ArrayList<>(libraries.size());
        for (String s : libraries) {
            try {
                urls.add(new File(s).toURI().toURL());
                LOG.log(Level.FINE, "Adding library: {0}", s);
            } catch (MalformedURLException ex) {
                // skip
                LOG.log(Level.WARNING, "Unable to add library {0}: {1}", new Object[] { s,  ex });
            }
        }
        return agentClassLoader = new URLClassLoader(
                urls.toArray(new URL[urls.size()]), 
                getClass().getClassLoader());
    }
    
    public static void premain(String args, Instrumentation inst) {
        LOG.log(Level.FINE, "NbJShell agent starting, parameters: {0}", args);
        LOG.log(Level.FINE, "Properties: " + System.getProperties().toString().replace(",", "\n"));
        
        NbJShellAgent agent = new NbJShellAgent();

        agent.instrumentation = inst;
        
        String[] pars = args.split(",");
        for (String param : pars) {
            String[] nameVal = param.split("=");
            if (nameVal == null || nameVal.length != 2) {
                continue;
            }
            switch (nameVal[0]) {
                case "address": 
                    try {
                        agent.setAddress(InetAddress.getByName(nameVal[1]));
                    } catch (UnknownHostException ex) {
                        LOG.log(Level.SEVERE, "Invalid host address: {0}", ex);
                    }
                    break;
                    
                case "port":
                    agent.setPort(Integer.valueOf(nameVal[1]));
                    break;
                    
                case "":
                    agent.setLibraries(Arrays.asList(nameVal[1].split(";")));
                    break;
                    
                case "key":
                    debuggerKey = nameVal[1];
                    LOG.log(Level.FINE, "Association key: " + debuggerKey);
                    agent.setKey(nameVal[1]);
                    break;
                case "className":
                    agent.setClassName(param);
                    break;
                case "field":
                    agent.setField(param);
                    break;
                case "method":
                    agent.setMethod(param);
                    break;
            }
        }
        
        try {
            ThreadGroup tg = new ThreadGroup("NetBeans JSHell agent support");
            Thread t = new Thread(tg, agent, "JShell VM Agent Connector");
            t.setDaemon(true);
            t.setContextClassLoader(agent.createClassLoader());
            t.start();
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    private Class workerClass;
    private Constructor workerCtor;
    
    public void run() {
        try {
            workerClass = createClassLoader().loadClass("jdk.internal.jshell.remote.AgentWorker");
            workerCtor = workerClass.getConstructor(NbJShellAgent.class, Socket.class);
        } catch (ReflectiveOperationException ex) {
            LOG.log(Level.WARNING, "Could not load worker class: ", ex);
            return;
        }        
        ServerSocket socket;
        try {
             socket = new ServerSocket();
             socket.bind(null);
            LOG.log(Level.FINE, "NetBeans JShell agent starting at port {0}", socket.getLocalPort());
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to allocate callback socket: ", ex);
            return;
        }
        
        LOG.log(Level.FINE, "Opening socket to {0}:{1}", new Object[] {
            getAddress(), getPort()
        });
        Socket handshake;
        try {
            // do NOT close the handshake socket, its close be used as an indication that the 
            // agent completely terminated.
            handshake = new Socket(getAddress(), getPort());
            LOG.log(Level.FINE, "Connection to master granted, creating OS");
            ObjectOutputStream ostm = new ObjectOutputStream(handshake.getOutputStream());
            // send an authorization
            LOG.log(Level.FINE, "Authorizing with key {0}, local port for callback is: {1}", new Object[] {
                key, socket.getLocalPort()
            });
            ostm.writeUTF(key);
            ostm.writeInt(socket.getLocalPort());
            ostm.flush();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Initial handshake failed: ", ex);
            return;
        }
        
        int counter = 1;
        
        try {
            while (true) {
                try {
                    Socket newConnection = socket.accept();
                    Runnable r = (Runnable) workerCtor.newInstance(this, newConnection);
                    Thread t = new Thread(r, "JShell agent #" + (counter++));
                    t.setDaemon(true);
                    LOG.log(Level.FINE, "Forking JShell agent " + r.hashCode());
                    t.start();
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Error during accept: ", ex);
                } catch (ReflectiveOperationException | IllegalArgumentException ex) {
                    LOG.log(Level.WARNING, "Could not initialize the worker", ex);
                }
            }
        } finally {
            try {
                handshake.close();
            } catch (IOException ex) {
                Logger.getLogger(NbJShellAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
