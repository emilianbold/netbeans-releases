/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;

import org.netbeans.api.debugger.*;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.ServerSocket;
import java.beans.PropertyChangeEvent;

import com.sun.jdi.connect.*;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.Bootstrap;

/**
 * Contains support functionality for unit tests.
 *
 * @author Maros Sandor
 */
public class JPDASupport implements DebuggerManagerListener {

    private DebuggerManager dm;
    private Process     process;
    private ProcessIO   pio;
    private JPDADebugger debugger;

    private Object debuggerStartLock;
    private Object [] stepLock = new Object[1];

    private JPDASupport() {
        dm = DebuggerManager.getDebuggerManager();
    }

    public ActionsManager getActionsManager() {
        return DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager();
    }

    public static Process launchVM(String mainClass, String connectorAddress, boolean vmListens) throws IOException {

        URLClassLoader ucl = (URLClassLoader) JPDASupport.class.getClassLoader();
        URL [] urls = ucl.getURLs();

        StringBuffer cp = new StringBuffer(200);
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            cp.append(url.getPath());
            cp.append(File.pathSeparatorChar);
        }

        String [] cmdArray = new String [] {
            System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java",
            "-Xdebug",
            "-Xnoagent",
            "-Xrunjdwp:transport=" + "dt_socket" + ",address=" + connectorAddress + ",suspend=y,server=" + (vmListens ? "y" : "n"),
            "-Djava.compiler=NONE",
            "-classpath",
            cp.substring(0, cp.length() -1),
            mainClass
        };

        return Runtime.getRuntime().exec(cmdArray);
    }

    private void connect(String mainClass, boolean stopInMain) throws IOException, IllegalConnectorArgumentsException, DebuggerStartException {

        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List lconnectors = vmm.listeningConnectors();
        ListeningConnector connector = null;
        for (Iterator i = lconnectors.iterator(); i.hasNext();) {
            ListeningConnector lc = (ListeningConnector) i.next();
            Transport t = lc.transport ();
            if (t != null && t.name().equals("dt_socket")) {
                connector = lc;
                break;
            }
        }
        if (connector == null) throw new RuntimeException("No listening socket connector available");

        Map args = connector.defaultArguments();
        String address = connector.startListening(args);
        String localhostAddres;
        try
        {
            int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
            localhostAddres = "localhost:" + port;
            Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port");
            portArg.setValue(port);
        } catch (Exception e) {
            // this address format is not known, use default
            localhostAddres = address;
        }

        if (stopInMain) addMainMethodBreakpoint(mainClass);

        JPDADebugger.startListening(connector, args, new Object[] { });

        process = launchVM(mainClass, localhostAddres, false);
        pio = new ProcessIO(process);
        pio.go();

        DebuggerEngine engine = dm.getCurrentEngine();
        debugger = (JPDADebugger) engine.lookupFirst(JPDADebugger.class);
        if (debugger == null) throw new DebuggerStartException("JPDA debugger was not started");
        debugger.addPropertyChangeListener(this);

        if (stopInMain) waitDebuggerStarted();
    }

    private void addMainMethodBreakpoint(String mainClass) {
        final MethodBreakpoint mb = MethodBreakpoint.create(mainClass, "main");
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.addJPDABreakpointListener(new JPDABreakpointListener() {
            public void breakpointReached(JPDABreakpointEvent event) {
                dm.removeBreakpoint(mb);
            }
        });
        dm.addBreakpoint(mb);
    }

    private void attachImpl(String mainClass) throws IOException, DebuggerStartException {

        int port = getFreePort();
        Process process = launchVM(mainClass, Integer.toString(port), true);
        pio = new ProcessIO(process);
        pio.go();

        addMainMethodBreakpoint(mainClass);

        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List aconnectors = vmm.attachingConnectors();
        AttachingConnector connector = null;
        for (Iterator i = aconnectors.iterator(); i.hasNext();) {
            AttachingConnector ac = (AttachingConnector) i.next();
            Transport t = ac.transport ();
            if (t != null && t.name().equals("dt_socket")) {
                connector = ac;
                break;
            }
        }
        if (connector == null) throw new RuntimeException("No attaching socket connector available");

        Object [] services = new Object [] { };
        debugger = JPDADebugger.attach("localhost", port, services);
        debugger.addPropertyChangeListener(this);

        waitDebuggerStarted();
    }

    private void waitDebuggerStarted() throws DebuggerStartException {
        debuggerStartLock = new Object();
        synchronized (debuggerStartLock) {
            if (debugger.getState() != JPDADebugger.STATE_STOPPED) {
                try {
                    debuggerStartLock.wait();
                } catch (InterruptedException e) {
                    throw new DebuggerStartException(e);
                }
            }
        }
    }

    private int getFreePort() throws IOException {
        ServerSocket ss = new ServerSocket(0);
        int portNumber = ss.getLocalPort();
        ss.setReuseAddress(false);
        ss.close();
        return portNumber;
    }

    public static JPDASupport listen(String mainClass) throws IOException, IllegalConnectorArgumentsException,
            DebuggerStartException {
        return listen(mainClass, true);
    }

    public static JPDASupport listen(String mainClass, boolean stopInMain) throws IOException, IllegalConnectorArgumentsException, DebuggerStartException {

        JPDASupport jpda = new JPDASupport();
        jpda.connect(mainClass, stopInMain);
        return jpda;
    }

    public static JPDASupport attach(String mainClass) throws IOException, DebuggerStartException {

        JPDASupport jpda = new JPDASupport();
        jpda.attachImpl(mainClass);
        return jpda;
    }

    public void doContinue() {
        if (debugger.getState() != JPDADebugger.STATE_STOPPED) throw new IllegalStateException();
        waitEnabled(DebuggerManager.ACTION_CONTINUE);
        getActionsManager().doAction(DebuggerManager.ACTION_CONTINUE);
    }

    public void stepOver() {
        step(DebuggerManager.ACTION_STEP_OVER);
    }

    public void stepInto() {
        step(DebuggerManager.ACTION_STEP_INTO);
    }

    public void stepOut() {
        step(DebuggerManager.ACTION_STEP_OUT);
    }

    public void step(Object action) {
        if (debugger.getState() != JPDADebugger.STATE_STOPPED) {
            throw new IllegalStateException();
        }
        waitEnabled(action);
        synchronized (stepLock) {
            stepLock[0] = "stepStart";
            getActionsManager().doAction(action);
            try {
                stepLock.wait();
            } catch (InterruptedException e) {
                 throw new IllegalStateException();
            }
        }
    }

    private void waitEnabled(Object action) {
        for (int i = 0; i < 50; i++) {
            if (getActionsManager().isEnabled(action)) return;
            sleep(100);
        }
        throw new IllegalStateException("Action " + action + " is not enabled");
    }

    public void waitDisconnected(int timeoutMillis) {
        long t0 = System.currentTimeMillis();
        while (debugger.getState() != JPDADebugger.STATE_DISCONNECTED) {
            sleep(100);
            if (System.currentTimeMillis() - t0 > timeoutMillis) throw new TimeoutException();
/*
            if (debugger.getState() == JPDADebugger.STATE_STOPPED) {
                System.out.println("Stopped at " + debugger.getCurrentCallStackFrame().getClassName() + ":" +
                                   debugger.getCurrentCallStackFrame().getLineNumber(null));
            }
*/
        }
    }

    public static class TimeoutException extends RuntimeException {
        TimeoutException() {
        }

        TimeoutException(String message) {
            super(message);
        }
    }

    public void waitState(int state, int timeoutMillis) {
        long t0 = System.currentTimeMillis();
        while (debugger.getState() != state) {
            sleep(100);
            if (System.currentTimeMillis() - t0 > timeoutMillis)
                throw new TimeoutException("Waitstate timeout: " + debugger.getState());
        }
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new DisconnectedException(e);
        }
    }

    public void doFinish() {
        doFinish(Integer.MAX_VALUE);
    }

    public void doFinish(int timeoutMillis) {
        if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) return;
        getActionsManager().doAction(DebuggerManager.ACTION_KILL);
        waitDisconnected(timeoutMillis);
    }

    private class DisconnectedException extends RuntimeException {
        public DisconnectedException(Throwable cause) {
            super(cause);
        }

        public DisconnectedException() {
            super();
        }
    }

    public Breakpoint[] initBreakpoints() {
        return new Breakpoint[0];
    }

    public void breakpointAdded(Breakpoint breakpoint) {
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
    }

    public void initWatches() {
    }

    public void watchAdded(Watch watch) {
    }

    public void watchRemoved(Watch watch) {
    }

    public void sessionAdded(Session session) {
    }

    public void sessionRemoved(Session session) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
//        System.out.println(evt.getSource() + " : " + evt.getPropertyName() + " : " + evt.getOldValue() + " : " + evt.getNewValue());
        if (evt.getSource() == debugger) {
            if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
                int os = ((Integer) evt.getOldValue()).intValue();
                int ns = ((Integer) evt.getNewValue()).intValue();
                if (debuggerStartLock != null) {
                    synchronized(debuggerStartLock) {
                        if (JPDADebugger.STATE_RUNNING == os && JPDADebugger.STATE_STOPPED == ns) {
                            debuggerStartLock.notifyAll();
                            debuggerStartLock = null;
                        }
                    }
                }
                synchronized(stepLock) {
                    if (stepLock[0] != null) {
                        if (stepLock[0].equals("stepStart")) {
                            if (os == JPDADebugger.STATE_STOPPED && ns == JPDADebugger.STATE_RUNNING) {
                                stepLock[0] = "stepInProgress";
                            }
                        } else if (stepLock[0].equals("stepInProgress")) {
                            if (os == JPDADebugger.STATE_RUNNING && ns == JPDADebugger.STATE_STOPPED) {
                                stepLock.notifyAll();
                                stepLock[0] = null;
                            }
                        }
                    }
                }
            }
        }
    }

    public JPDADebugger getDebugger() {
        return debugger;
    }

    private static class ProcessIO {

        private Process p;

        public ProcessIO(Process p) {
            this.p = p;
        }

        public void go() {
            InputStream out = p.getInputStream();
            InputStream err = p.getErrorStream();

            new SimplePipe(System.out, out).start();
            new SimplePipe(System.out, err).start();
        }
    }

    private static class SimplePipe extends Thread {
        private OutputStream out;
        private InputStream in;

        public SimplePipe(OutputStream out, InputStream in) {
            this.out = out;
            this.in = in;
            setDaemon(true);
        }

        public void run() {
            byte [] buffer = new byte[1024];
            int n;
            try {
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            } catch (IOException e) {
            } finally {
                try {
                    out.close();
                    in.close();
                } catch (IOException e) {
                }
            }
            System.out.println("PIO QUIT");
        }
    }

}
