/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * Command Line Interface and User Directory Locker support class.
 * Subclasses may be registered into the system to handle special command-line options.
 * To be registered, use {@link org.openide.util.lookup.ServiceProvider}
 * in a JAR file in the startup or dynamic class path (e.g. <samp>lib/ext/</samp>
 * or <samp>lib/</samp>).
 * @author Jaroslav Tulach
 * @since org.netbeans.core/1 1.18
 * @see "#32054"
 * @see <a href="http://openide.netbeans.org/proposals/arch/cli.html">Specification</a>
 */
public abstract class CLIHandler extends Object {
    /** lenght of the key used for connecting */
    private static final int KEY_LENGTH = 10;
    private static final byte[] VERSION = {
        'N', 'B', 'C', 'L', 'I', 0, 0, 0, 0, 1
    };
    /** ok reply */
    private static final int REPLY_OK = 1;
    /** sends exit code */
    private static final int REPLY_EXIT = 2;
    /** fail reply */
    private static final int REPLY_FAIL = 0;
    /** the server is active, but cannot compute the value now */
    private static final int REPLY_DELAY = 3;
    
    /** request to read from input stream */
    private static final int REPLY_READ = 10;
    /** request to write */
    private static final int REPLY_WRITE = 11;
    /** request to find out how much data is available */
    private static final int REPLY_AVAILABLE = 12;
    /** request to write to stderr */
    private static final int REPLY_ERROR = 13;
    /** returns version of the protocol */
    private static final int REPLY_VERSION = 14;
    
    /**
     * Used during bootstrap sequence. Should only be used by core, not modules.
     */
    public static final int WHEN_BOOT = 1;
    /**
     * Used during later initialization or while NetBeans is up and running.
     */
    public static final int WHEN_INIT = 2;
     /** Extra set of inits.
     */
    public static final int WHEN_EXTRA = 3;
    
    /** reference to our server.
     */
    private static Server server;
    
    /** Testing output of the threads.
     */
    private static final Logger OUTPUT = Logger.getLogger(CLIHandler.class.getName());
    
    private int when;
    
    /**
     * Create a CLI handler and indicate its preferred timing.
     * @param when when to run the handler: {@link #WHEN_BOOT} or {@link #WHEN_INIT}
     */
    protected CLIHandler(int when) {
        this.when = when;
    }
    
    /**
     * Process some set of command-line arguments.
     * Unrecognized or null arguments should be ignored.
     * Recognized arguments should be nulled out.
     * @param args arguments
     * @return error value or 0 if everything is all right
     */
    protected abstract int cli(Args args);
    
    protected static void showHelp(PrintWriter w, Collection<? extends CLIHandler> handlers, int when) {
        for (CLIHandler h : handlers) {
            if (when != -1 && when != h.when) {
                continue;
            }
            
            h.usage(w);
        }
    }

    /**
     * Print usage information for this handler.
     * @param w a writer to print to
     */
    protected abstract void usage(PrintWriter w);
    
    /** For testing purposes we can block the
     * algorithm in any place in the initialize method.
     */
    private static void enterState(int state, Integer block) {
        if (OUTPUT.isLoggable(Level.FINEST)) {
            synchronized (OUTPUT) {
                // for easier debugging of CLIHandlerTest
                OUTPUT.finest("state: " + state + " thread: " + Thread.currentThread()); // NOI18N
            }
        }
        
        if (block == null) return;

        
        synchronized (block) {
            if (state == block.intValue()) {
                if (OUTPUT.isLoggable(Level.FINEST)) {
                    OUTPUT.finest(state + " blocked"); // NOI18N
                }
                block.notifyAll();
                try {
                    block.wait();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException();
                }
            } else {
                if (OUTPUT.isLoggable(Level.FINEST)) {
                    OUTPUT.finest(state + " not blocked"); // NOI18N
                }
            }
        }
    }
    
    private static boolean checkHelp(Args args, Collection<? extends CLIHandler> handlers) {
        String[] argv = args.getArguments();
        for (int i = 0; i < argv.length; i++) {
            if (argv[i] == null) {
                continue;
            }

            if (argv[i].equals("-?") || argv[i].equals("--help") || argv[i].equals ("-help")) { // NOI18N
                // disable all logging from standard logger (which prints to stdout) to prevent help mesage disruption
                Logger.getLogger("").setLevel(Level.OFF); // NOI18N
                PrintWriter w = new PrintWriter(args.getOutputStream());
                showHelp(w, handlers, -1);
                w.flush();
                return true;
            }
        }
        
        return false;
    }
    
    /** Notification of available handlers.
     * @return non-zero if one of the handlers fails
     */
    protected static int notifyHandlers(Args args, Collection<? extends CLIHandler> handlers, int when, boolean failOnUnknownOptions, boolean consume) {
        try {
            int r = 0;
            for (CLIHandler h : handlers) {
                if (h.when != when) continue;

                r = h.cli(args);
                //System.err.println("notifyHandlers: exit code " + r + " from " + h);
                if (r != 0) {
                    return r;
                }
            }
            String[] argv = args.getArguments();
            if (failOnUnknownOptions) {
                argv = args.getArguments();
                for (int i = 0; i < argv.length; i++) {
                    if (argv[i] != null) {
                        // Unhandled option.
                        PrintWriter w = new PrintWriter(args.getOutputStream());
                        w.println("Ignored unknown option: " + argv[i]); // NOI18N

                        // XXX(-ttran) not good, this doesn't show the help for
                        // switches handled by the launcher
                        //
                        //showHelp(w, handlers);
                        
                        w.flush();
                        return 2;
                    }
                }
            }
            return 0;
        } finally {
            args.reset(consume);
        }
    }
    
    /**
     * Represents result of initialization.
     * @see #initialize(String[], ClassLoader)
     * @see #initialize(Args, Integer, List)
     */
    static final class Status {
        public static final int CANNOT_CONNECT = -255;
        
        private final File lockFile;
        private final int port;
        private int exitCode;
        private Task parael;
        /**
         * General failure.
         */
        Status() {
            this(0);
        }
        /**
         * Failure due to a parse problem.
         * @param c bad status code (not 0)
         * @see #cli(Args)
         */
        Status(int c) {
            this(null, 0, c, null);
        }
        /**
         * Some measure of success.
         * @param l the lock file (not null)
         * @param p the server port (not 0)
         * @param c a status code (0 or not)
         */
        Status(File l, int p, int c, Task parael) {
            lockFile = l;
            port = p;
            exitCode = c;
            this.parael = parael;
        }
        
        private void waitFinished() {
            if (parael != null) {
                parael.waitFinished();
            }
        }
        
        /**
         * Get the lock file, if available.
         * @return the lock file, or null if there is none
         */
        public File getLockFile() {
            waitFinished();
            return lockFile;
        }
        /**
         * Get the server port, if available.
         * @return a port number for the server, or 0 if there is no port open
         */
        public int getServerPort() {
            return port;
        }
        /**
         * Get the CLI parse status.
         * @return 0 for success, some other value for error conditions
         */
        public int getExitCode() {
            return exitCode;
        }
    }
    
    
    /** Initializes the system by creating lock file.
     *
     * @param args the command line arguments to recognize
     * @param classloader to find command CLIHandlers in
     * @param failOnUnknownOptions if true, fail (status 2) if some options are not recognized (also checks for -? and -help)
     * @param cleanLockFile removes lock file if it appears to be dead
     * @return the file to be used as lock file or null parsing of args failed
     */
    static Status initialize(
        String[] args, 
        InputStream is, 
        OutputStream os, 
        java.io.OutputStream err,         
        MainImpl.BootClassLoader loader,
        boolean failOnUnknownOptions, 
        boolean cleanLockFile,
        Runnable runWhenHome
    ) {
        return initialize(
            new Args(args, is, os, err, System.getProperty ("user.dir")), 
            (Integer)null, 
            loader.allCLIs(), 
            failOnUnknownOptions, 
            cleanLockFile, 
            runWhenHome
        );
    }
    
    /**
     * What to do later when {@link #finishInitialization} is called.
     * May remain null, otherwise contains list of Execute
     */
    private static List<Execute> doLater = new ArrayList<Execute> ();
    static interface Execute {
        /** @return returns exit code */
        public int exec ();
    }
    
    /** Execute this runnable when finishInitialization method is called.
     */
    private static int registerFinishInstallation (Execute run) {
        boolean runNow;
    
        synchronized (CLIHandler.class) {
            if (doLater != null) {
                doLater.add (run);
                runNow = false;
            } else {
                runNow = true;
            }
        }
        
        if (runNow) {
            return run.exec ();
        }
        
        return 0;
    }

    /**
     * Run any {@link #WHEN_INIT} handlers that were passed to the original command line.
     * Should be called when the system is up and ready.
     * Cancels any existing actions, in case it is called twice.
     * @return the result of executing the handlers
     */
    static int finishInitialization (boolean recreate) {
        OUTPUT.log(Level.FINER, "finishInitialization {0}", recreate);
        List<Execute> toRun;
        synchronized (CLIHandler.class) {
            toRun = doLater;
            doLater = recreate ? new ArrayList<Execute> () : null;
            if (OUTPUT.isLoggable(Level.FINER)) {
                OUTPUT.finer("Notify: " + toRun);
            }
            if (!recreate) {
                CLIHandler.class.notifyAll ();
            }
        }
        
        if (toRun != null) {
            for (Execute r : toRun) {
                int result = r.exec ();
                if (result != 0) {
                    return result;
                }
            }
        }
        return 0;
    }
    
    /** Blocks for a while and waits if the finishInitialization method
     * was called.
     * @param timeout ms to wait
     * @return true if finishInitialization is over
     */
    private static synchronized boolean waitFinishInstallationIsOver (int timeout) {
        if (doLater != null) {
            try {
                CLIHandler.class.wait (timeout);
            } catch (InterruptedException ex) {
                // go on, never mind
            }
        }
        return doLater == null;
    }
    
    /** Stops the server.
     */
    public static synchronized void stopServer () {
        Server s = server;
        if (s != null) {
            s.stopServer ();
        }
    }
    
    /** Enhanced search for localhost address that works also behind VPN
     */
    private static InetAddress localHostAddress () throws IOException {
        java.net.NetworkInterface net = java.net.NetworkInterface.getByName ("lo");
        if (net == null || !net.getInetAddresses().hasMoreElements()) {
            net = java.net.NetworkInterface.getByInetAddress(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
        }
        if (net == null || !net.getInetAddresses().hasMoreElements()) {
            return InetAddress.getLocalHost();
        }
        else {
            return net.getInetAddresses().nextElement();
        }
    }
    
    /** Initializes the system by creating lock file.
     *
     * @param args the command line arguments to recognize
     * @param block the state we want to block in
     * @param handlers all handlers to use
     * @param failOnUnknownOptions if true, fail (status 2) if some options are not recognized (also checks for -? and -help)
     * @param cleanLockFile removes lock file if it appears to be dead
     * @param runWhenHome runnable to be executed when netbeans.user property is set
     * @return a status summary
     */
    static Status initialize(
        final Args args, final Integer block, 
        final Collection<? extends CLIHandler> handlers,
        final boolean failOnUnknownOptions, 
        boolean cleanLockFile,
        Runnable runWhenHome
    ) {
        // initial parsing of args
        {
            int r = notifyHandlers(args, handlers, WHEN_BOOT, false, failOnUnknownOptions);
            if (r != 0) {
                return new Status(r);
            }
        }
        
        // get the value
        String home = System.getProperty("netbeans.user"); // NOI18N
        if (home == null) {
            home = System.getProperty("user.home"); // NOI18N
            System.setProperty ("netbeans.user", home); // NOI18N
        }
    
        if ("memory".equals(home)) { // NOI18N
            return new Status(0);
        }
        
        if (runWhenHome != null) {
            // notify that we have successfully identified the home property
            runWhenHome.run ();
        }
        
        
        File lockFile = new File(home, "lock"); // NOI18N
        
        for (int i = 0; i < 5; i++) {
            // try few times to succeed
            try {
                if (lockFile.exists()) {
                    enterState(5, block);
                    throw new IOException("EXISTS"); // NOI18N
                }
                
                if (i == 0 && checkHelp(args, handlers)) {
                    return new Status(2);
                }
                
                lockFile.getParentFile().mkdirs();
                lockFile.createNewFile();
                lockFile.deleteOnExit();
                secureAccess(lockFile);
                
                enterState(10, block);
                
                final byte[] arr = new byte[KEY_LENGTH];
                new Random().nextBytes(arr);
                
                server = new Server(arr, block, handlers, failOnUnknownOptions);
                
                final DataOutputStream os = new DataOutputStream(new FileOutputStream(lockFile));
                int p = server.getLocalPort();
                os.writeInt(p);
                os.flush();
                
                enterState(20, block);
                
                Task parael = new RequestProcessor("Secure CLI Port").post(new Runnable() { // NOI18N
                    public void run() {
                        SecureRandom random = null;
                        enterState(95, block);
                        try {
                            random = SecureRandom.getInstance("SHA1PRNG"); // NOI18N
                        } catch (NoSuchAlgorithmException e) {
                            // #36966: IBM JDK doesn't have it.
                            try {
                                random = SecureRandom.getInstance("IBMSecureRandom"); // NOI18N
                            } catch (NoSuchAlgorithmException e2) {
                                // OK, disable server...
                                server.stopServer();
                            }
                        }
                        
                        enterState(96, block);
                        
                        if (random != null) {
                            random.nextBytes(arr);
                        }
                        
                        enterState(97, block);

                        try {
                            os.write(arr);
                            os.flush();

                            enterState(27,block);
                            // if this turns to be slow due to lookup of getLocalHost
                            // address, it can be done asynchronously as nobody needs
                            // the address in the stream if the server is listening
                            byte[] host = InetAddress.getLocalHost().getAddress();
                            if (block != null && block.intValue() == 667) {
                                // this is here to emulate #64004
                                throw new UnknownHostException("dhcppc0"); // NOI18N
                            }
                            for (int all = 0; all < host.length; all++) {
                                os.write(host[all]);
                            }
                        } catch (UnknownHostException unknownHost) {
                            if (!"dhcppc0".equals(unknownHost.getMessage())) { // NOI18N, see above
                                // if we just cannot get the address, we can go on
                                unknownHost.printStackTrace();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        try {
                            os.close();
                        } catch (IOException ex) {
                            // ignore
                        }
                    }
                });
                
                int execCode = registerFinishInstallation (new Execute () {
                    public int exec () {
                        return notifyHandlers(args, handlers, WHEN_INIT, failOnUnknownOptions, failOnUnknownOptions);
                    }
                    public @Override String toString() {
                        return handlers.toString();
                    }
                });
                
                enterState(0, block);
                return new Status(lockFile, server.getLocalPort(), execCode, parael);
            } catch (IOException ex) {
                if (!"EXISTS".equals(ex.getMessage())) { // NOI18N
                    ex.printStackTrace();
                }
                // already exists, try to read
                byte[] key = null;
                byte[] serverAddress = null;
                int port = -1;
                DataInputStream is = null;
                try {
                    enterState(21, block);
                    if (OUTPUT.isLoggable(Level.FINER)) {
                        OUTPUT.log(Level.FINER, "Reading lock file {0}", lockFile); // NOI18N
                    }
                    is = new DataInputStream(new FileInputStream(lockFile));
                    port = is.readInt();
                    enterState(22, block);
                    key = new byte[KEY_LENGTH];
                    is.readFully(key);
                    enterState(23, block);
                    byte[] x = new byte[4];
                    is.readFully(x);
                    enterState(24, block);
                    serverAddress = x;
                } catch (EOFException eof) {
                    // not yet fully written down
                    if (port != -1) {
                        try {
                            enterState(94, block);
                            try {
                                Socket socket = new Socket(localHostAddress (), port);
                                socket.close();
                            } catch (Exception ex3) {
                                // socket is not open, remove the file and try once more
                                lockFile.delete();
                                continue;
                            }
                            // just wait a while
                            Thread.sleep(2000);
                        } catch (InterruptedException inter) {
                            inter.printStackTrace();
                        }
                        continue;
                    }
                } catch (IOException ex2) {
                    // ok, try to read it once more
                    enterState(26, block);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex3) {
                            // ignore here
                        }
                    }
                    enterState(25, block);
                }
                
                if (key != null && port != -1) {
                    int version = -1;
                    RESTART: for (;;) try {
                        // ok, try to connect
                        enterState(28, block);
                        Socket socket = new Socket(localHostAddress (), port);
                        // wait max of 1s for reply
                        socket.setSoTimeout(5000);
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        if (version == -1) {
                            os.write(VERSION);
                        } else {
                            os.write(key);
                        }
                        assert VERSION.length == key.length;
                        os.flush();
                        
                        enterState(30, block);
                        
                        DataInputStream replyStream = new DataInputStream(socket.getInputStream());
                        byte[] outputArr = new byte[4096];
                        
                        COMMUNICATION: for (;;) {
                            enterState(32, block);
                            int reply = replyStream.read();
                            //System.err.println("reply=" + reply);
                            enterState(34, block);
                            
                            switch (reply) {
                                case REPLY_VERSION:
                                    version = replyStream.readInt();
                                    os.write(key);
                                    os.flush();
                                    break;
                                case REPLY_FAIL:
                                    if (version == -1) {
                                        os.close();
                                        replyStream.close();
                                        socket.close();
                                        version = 0;
                                        continue RESTART;
                                    }
                                    enterState(36, block);
                                    break COMMUNICATION;
                                case REPLY_OK:
                                    enterState(38, block);
                                    // write the arguments
                                    String[] arr = args.getArguments();
                                    os.writeInt(arr.length);
                                    for (int a = 0; a < arr.length; a++) {
                                        os.writeUTF(arr[a]);
                                    }
                                    os.writeUTF (args.getCurrentDirectory().toString()); 
                                    os.flush();
                                    break;
                                case REPLY_EXIT:
                                    int exitCode = replyStream.readInt();
                                    if (exitCode == 0) {
                                        // to signal end of the world
                                        exitCode = -1;
                                    }
                                    
                                    os.close();
                                    replyStream.close();
                                    
                                    enterState(0, block);
                                    return new Status(lockFile, port, exitCode, null);
                                case REPLY_READ: {
                                    enterState(42, block);
                                    int howMuch = replyStream.readInt();
                                    if (howMuch > outputArr.length) {
                                        outputArr = new byte[howMuch];
                                    }
                                    int really = args.getInputStream().read(outputArr, 0, howMuch);
                                    os.write(really);
                                    if (really > 0) {
                                        os.write(outputArr, 0, really);
                                    }
                                    os.flush();
                                    break;
                                }
                                case REPLY_WRITE: {
                                    enterState(44, block);
                                    int howMuch = replyStream.readInt();
                                    if (howMuch > outputArr.length) {
                                        outputArr = new byte[howMuch];
                                    }
                                    replyStream.read(outputArr, 0, howMuch);
                                    args.getOutputStream().write(outputArr, 0, howMuch);
                                    break;
                                }
                                case REPLY_ERROR: {
                                    enterState(45, block);
                                    int howMuch = replyStream.readInt();
                                    if (howMuch > outputArr.length) {
                                        outputArr = new byte[howMuch];
                                    }
                                    replyStream.read(outputArr, 0, howMuch);
                                    args.getErrorStream().write(outputArr, 0, howMuch);
                                    break;
                                }
                                case REPLY_AVAILABLE:
                                    enterState(46, block);
                                    os.writeInt(args.getInputStream().available());
                                    os.flush();
                                    break;
                                case REPLY_DELAY:
                                    enterState(47, block);
                                    // ok, try once more
                                    break;
                                case -1:
                                    enterState(48, block);
                                    // EOF. Why does this happen?
                                    break COMMUNICATION;
                                default:
                                    enterState(49, block);
                                    assert false : reply;
                            }
                        }
                        
                        // connection ok, butlockFile secret key not recognized
                        // delete the lock file
                        break RESTART;
                    } catch (java.net.SocketTimeoutException ex2) {
                        // connection failed, the port is dead
                        enterState(33, block);
                        break RESTART;
                    } catch (java.net.ConnectException ex2) {
                        // connection failed, the port is dead
                        enterState(33, block);
                        break RESTART;
                    } catch (IOException ex2) {
                        // some strange exception
                        ex2.printStackTrace();
                        enterState(33, block);
                        break RESTART;
                    }
                    
                    boolean isSameHost = true;
                    if (serverAddress != null) {
                        try {
                            isSameHost = Arrays.equals(InetAddress.getLocalHost().getAddress(), serverAddress);
                        } catch (UnknownHostException ex5) {
                            // ok, we will not try to connect
                            enterState(999, block);
                        }
                    }
                    
                    if (cleanLockFile || isSameHost) {
                        // remove the file and try once more
                        lockFile.delete();
                    } else {
                        return new Status (Status.CANNOT_CONNECT);
                    }
                }
            }
            
            try {
                enterState(83, block);
                Thread.sleep((int)(Math.random() * 1000.00));
                enterState(85, block);
            } catch (InterruptedException ex) {
                // means nothing
            }
        }
        
        // failure
        return new Status();
    }

    /** Make the file readable just to its owner.
     */
    private static void secureAccess(final File file) throws IOException {
        file.setReadable(false, false);
        file.setReadable(true, true);
    }

    /** Class that represents available arguments to the CLI
     * handlers.
     */
    public static final class Args extends Object {
        private String[] args;
        private final String[] argsBackup;
        private InputStream is;
        private OutputStream os;
        private OutputStream err;
        private File currentDir;
        private boolean closed;
        
        Args(String[] args, InputStream is, OutputStream os, java.io.OutputStream err, String currentDir) {
            argsBackup = args;
            reset(false);
            this.is = is;
            this.os = os;
            this.err = err;
            this.currentDir = new File (currentDir);
        }
        
        /**
         * Restore the arguments list to a clean state.
         * If not consuming arguments, it is just set to the original list.
         * If consuming arguments, any nulled-out arguments are removed from the list.
         */
        void reset(boolean consume) {
            if (consume) {
                String[] a = args;
                if (a == null) {
                    a = argsBackup;
                }
                List<String> l = new ArrayList<String>(Arrays.asList(a));
                l.removeAll(Collections.singleton(null));
                args = l.toArray(new String[l.size()]);
            } else {
                args = argsBackup.clone();
            }
        }
        
        /** Closes the connection.
         */
        final void close() {
            closed = true;
        }
        
        /**
         * Get the command-line arguments.
         * You may not modify the returned array except to set some elements
         * to null as you recognize them.
         * @return array of string arguments, may contain nulls
         */
        public String[] getArguments() {
            return args;
        }
        
        /**
         * Get an output stream to which data may be sent.
         * @return stream to write to
         */
        public OutputStream getOutputStream() {
            return os;
        }
        
        /** Access to error stream.
         * @return the stream to write error messages to
         */
        public OutputStream getErrorStream() {
            return err;
        }
        
        public File getCurrentDirectory () {
            return currentDir;
        }
        
        /**
         * Get an input stream that may supply additional data.
         * @return stream to read from
         */
        public InputStream getInputStream() {
            return is;
        }
        
        /** Is open? True if the connection is still alive. Can be
         * used with long running computations to find out if the
         * consumer of the output has not been interupted.
         *
         * @return true if the connection is still alive
         */
        public boolean isOpen() {
            return !closed;
        }
        
    } // end of Args
    
    /** Server that creates local socket and communicates with it.
     */
    private static final class Server extends Thread {
        private byte[] key;
        private ServerSocket socket;
        private Integer block;
        private Collection<? extends CLIHandler> handlers;
        private Socket work;
        private static volatile int counter;
        private final boolean failOnUnknownOptions;

        private static long lastReply;
        /** by default wait 100ms before sending a REPLY_FAIL message */
        private static long failDelay = 100;
        
        public Server(byte[] key, Integer block, Collection<? extends CLIHandler> handlers, boolean failOnUnknownOptions) throws IOException {
            super("CLI Requests Server"); // NOI18N
            this.key = key;
            this.setDaemon(true);
            this.block = block;
            this.handlers = handlers;
            this.failOnUnknownOptions = failOnUnknownOptions;
            
            socket = new ServerSocket(0, 50, localHostAddress());
            start();
        }
        
        public Server(Socket request, byte[] key, Integer block, Collection<? extends CLIHandler> handlers, boolean failOnUnknownOptions) throws IOException {
            super("CLI Handler Thread Handler: " + ++counter); // NOI18N
            this.key = key;
            this.setDaemon(true);
            this.block = block;
            this.handlers = handlers;
            this.work = request;
            this.failOnUnknownOptions = failOnUnknownOptions;
            
            start();
        }
        
        public int getLocalPort() {
            return socket.getLocalPort();
        }
        
        public @Override void run() {
            if (work != null) {
                // I am a worker not listener server
                try {
                    handleConnect(work);
                } catch (IOException ex) {
                    OUTPUT.log(Level.WARNING, null, ex);
                }
                return;
            }
            
            ServerSocket toClose = socket;
            if (toClose == null) {
                return;
            }
            
            while (socket != null) {
                try {
                    enterState(65, block);
                    Socket s = socket.accept();
                    if (socket == null) {
                        enterState(66, block);
                        s.getOutputStream().write(REPLY_FAIL);
                        enterState(67, block);
                        s.close();
                        continue;
                    }
                    
                    // spans new request handler
                    new Server(s, key, block, handlers, failOnUnknownOptions);
                } catch (InterruptedIOException ex) {
                    if (socket != null) {
                        ex.printStackTrace();
                    }
                    // otherwise ignore, we've just been asked by the stopServer
                    // to stop
                } catch (java.net.SocketException ex) {
                    if (socket != null) {
                        ex.printStackTrace();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            
            try {
                toClose.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        final void stopServer () {
            socket = null;
            // interrupts the listening server
            interrupt();
        }
        
        private void handleConnect(Socket s) throws IOException {
            int requestedVersion;
            byte[] check = new byte[key.length];
            DataInputStream is = new DataInputStream(s.getInputStream());
            
            enterState(70, block);

            is.readFully(check);

            final DataOutputStream os = new DataOutputStream(s.getOutputStream());

            boolean match = true;
            for (int i = 0; i < VERSION.length - 1; i++) {
                if (VERSION[i] != check[i]) {
                    match = false;
                }
            }
            if (match) {
                requestedVersion = check[VERSION.length - 1];
                os.write(REPLY_VERSION);
                os.writeInt(VERSION[VERSION.length - 1]);
                os.flush();
                is.readFully(check);
            } else {
                requestedVersion = 0;
            }
            
            enterState(90, block);
            
            if (Arrays.equals(check, key)) {
                while (!waitFinishInstallationIsOver (2000)) {
                    os.write (REPLY_DELAY);
                    os.flush ();
                }
                
                enterState(93, block);
                os.write(REPLY_OK);
                os.flush();
                
                // continue with arguments
                int numberOfArguments = is.readInt();
                String[] args = new String[numberOfArguments];
                for (int i = 0; i < args.length; i++) {
                    args[i] = is.readUTF();
                }
                final String currentDir = is.readUTF ();
                
                final Args arguments = new Args(
                    args, 
                    new IS(is, os), 
                    new OS(os, REPLY_WRITE), 
                    new OS(os, REPLY_ERROR), 
                    currentDir
                );

                class ComputingAndNotifying extends Thread {
                    public int res;
                    public boolean finished;
                    
                    public ComputingAndNotifying () {
                        super ("Computes values in handlers");
                    }
                    
                    public @Override void run() {
                        try {
                            if (checkHelp(arguments, handlers)) {
                                res = 2;
                            } else {
                                res = notifyHandlers (arguments, handlers, WHEN_INIT, failOnUnknownOptions, false);
                            }

                            if (res == 0) {
                                enterState (98, block);
                            } else {
                                enterState (99, block);
                            }
                        } finally {
                            synchronized (this) {
                                finished = true;
                                notifyAll ();
                            }
                        }
                    }
                    
                    public synchronized void waitForResultAndNotifyOthers () {
                        // execute the handlers in another thread
                        start ();
                        while (!finished) {
                            try {
                                wait (1000);
                                os.write (REPLY_DELAY);
                                os.flush ();
                            } catch (SocketException ex) {
                                if (isClosedSocket(ex)) { // NOI18N
                                    // mark the arguments killed
                                    arguments.close();
                                    // interrupt this thread
                                    interrupt();
                                } else {
                                    ex.printStackTrace();
                                }
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                ComputingAndNotifying r = new ComputingAndNotifying ();
                r.waitForResultAndNotifyOthers ();
                try {
                    os.write(REPLY_EXIT);
                    os.writeInt(r.res);
                } catch (SocketException ex) {
                    if (isClosedSocket(ex)) { // NOI18N
                        // mark the arguments killed
                        arguments.close();
                        // interrupt r thread
                        r.interrupt();
                    } else {
                        throw ex;
                    }
                }
            } else {
                enterState(103, block);
                long toWait = lastReply + failDelay - System.currentTimeMillis();
                if (toWait > 0) {
                    try {
                        Thread.sleep(toWait);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    failDelay *= 2;
                } else {
                    failDelay = 100;
                }
                lastReply = System.currentTimeMillis();
                os.write(REPLY_FAIL);
            }
            
            
            enterState(120, block);
            
            os.close();
            is.close();
        }
        
        /** A method to find out on various systems whether an exception is
         * a signal of closed socket, especially if the peer is killed or exited.
         * @param ex the exception to investigate
         */
        static final boolean isClosedSocket(SocketException ex) {
            if (ex.getMessage().equals("Broken pipe")) { // NOI18N
                return true;
            }
            if (ex.getMessage().startsWith("Connection reset by peer")) { // NOI18N
                return true;
            }
            
            return false;
        }
        
        private static final class IS extends InputStream {
            private DataInputStream is;
            private DataOutputStream os;
            
            public IS(DataInputStream is, DataOutputStream os) {
                this.is = is;
                this.os = os;
            }
            
            public int read() throws IOException {
                byte[] arr = new byte[1];
                if (read(arr) == 1) {
                    return arr[0];
                } else {
                    return -1;
                }
            }
            
            public @Override void close() throws IOException {
                super.close();
            }
            
            public @Override int available() throws IOException {
                // ask for data
                os.write(REPLY_AVAILABLE);
                os.flush();
                // read provided data
                return is.readInt();
            }
            
            public @Override int read(byte[] b) throws IOException {
                return read(b, 0, b.length);
            }
            
            public @Override int read(byte[] b, int off, int len) throws IOException {
                // ask for data
                os.write(REPLY_READ);
                os.writeInt(len);
                os.flush();
                // read provided data
                int really = is.read ();
                if (really > 0) {
                    return is.read(b, off, really);
                } else {
                    return really;
                }
            }
            
        } // end of IS
        
        private static final class OS extends OutputStream {
            private DataOutputStream os;
            private int type;
            
            public OS(DataOutputStream os, int type) {
                this.os = os;
                this.type = type;
            }
            
            public void write(int b) throws IOException {
                byte[] arr = { (byte)b };
                write(arr);
            }
            
            public @Override void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
            
            public @Override void close() throws IOException {
                super.close();
            }
            
            public @Override void flush() throws IOException {
                os.flush();
            }
            
            public @Override void write(byte[] b, int off, int len) throws IOException {
                os.write(type);
                os.writeInt(len);
                os.write(b, off, len);
            }
            
        } // end of OS
        
    } // end of Server
    
    
}
