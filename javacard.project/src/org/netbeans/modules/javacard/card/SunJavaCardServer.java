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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.card;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.javacard.api.Card;
import static org.netbeans.modules.javacard.api.CardState.*;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import org.netbeans.modules.javacard.api.CardState;

/**
 *
 * @author Anki R Nelaturu
 */
abstract class SunJavaCardServer extends Card {

    protected String userName = "";
    protected String password = "";
    private Process serverProcess = null;
    private Process debugProxyProcess = null;
    //Rather than a ModuleInstall, use a VM shutdown hook to terminate
    //emulator processes.  That will avoid the startup time hit of having
    //a ModuleInstall
    private static volatile boolean shutdownHookAdded;
    private static Set<Process> processes =
            Collections.synchronizedSet(new WeakSet<Process>());
    private final JavacardPlatform platform;

    /**
     * @param id
     * @param userName
     * @param password
     */
    public SunJavaCardServer(JavacardPlatform platform, String id,
                             String userName, String password) {
        super(platform, id);
        this.userName = userName;
        this.password = password;
        this.platform = platform;
    }
   
    @Override
    public boolean isValid() {
        return true;
    }
    
    /**
     * 
     * @return
     */
    public abstract String[] getStartCommandLine(boolean forDebug);

    /**
     * 
     * @return
     */
    public abstract String[] getDebugProxyCommandLine(Object... args);

    /**
     * 
     * @return
     */
    public abstract String[] getResumeCommandLine();

    /**
     * 
     * @return
     */
    public abstract File getProcessDir();


    private class C implements Condition {
        private volatile int steps;
        C (int steps) {
            this.steps = steps;
        }

        void countdown() {
            steps--;
            if (steps == 0) {
                signalAll();
            }
        }

        public void await() throws InterruptedException {
            synchronized(this) {
                this.wait();
            }
        }

        public void awaitUninterruptibly() {
            try {
                synchronized(this) {
                    await();
                }
            } catch (InterruptedException e) {
                awaitUninterruptibly();
            }
        }

        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            synchronized(this) {
                this.wait(0, (int) nanosTimeout);
            }
            return 0;
        }

        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            long millis = unit.convert(time, TimeUnit.MILLISECONDS);
            synchronized(this) {
                wait (millis);
            }
            return getState().isRunning();
        }

        public boolean awaitUntil(Date deadline) throws InterruptedException {
            long time = deadline.getTime() - System.currentTimeMillis();
            await (time, TimeUnit.MILLISECONDS);
            return getState().isRunning();
        }

        public void signal() {
            synchronized(this) {
                notify();
            }
        }

        public void signalAll() {
            synchronized(this) {
                notifyAll();
            }
        }
    }
    
    public final Condition startServer(final boolean debug, Object... args) {
        if (!isNotRunning()) {
            return null;
        }
        final String[] cls = getStartCommandLine(debug);
        if (cls == null || cls.length <= 0) {
            throw new IllegalStateException ("0 length start command line for " +  this);
        }

        final C c = new C(debug ? 2 : 1);

        ExecutionDescriptor ed = new ExecutionDescriptor().controllable(true).
                frontWindow(true).preExecution(new Runnable(){
            public void run() {
                setState(STARTING);
            }
        }).postExecution(new Runnable() {
            public void run() {
                setState(NOT_RUNNING);
            }
        });

        ExecutionService server = ExecutionService.newService(new Callable<Process>() {

            public Process call() throws Exception {
                try {
                    ProcessBuilder pb = new ProcessBuilder(cls);
                    pb.directory(getProcessDir());
                    pb.redirectErrorStream();

                    pb.redirectErrorStream(true);
                    if (!shutdownHookAdded) {
                        installShutdownHook();
                    }
                    Process p = pb.start();
                    processes.add(p);
                    synchronized (SunJavaCardServer.this) {
                        serverProcess = p;
                    }
                    return p;
                } finally {
                    c.countdown();
                    setState (debug ? RUNNING_IN_DEBUG_MODE : RUNNING);
                }
            }
        }, ed, getId());

        server.run();

        if (debug && isReferenceImplementation()) {
            final String[] cmdLine = getDebugProxyCommandLine(args);
            if (cls.length <= 0) {
                return null;
            }
            ed = new ExecutionDescriptor().controllable(true).frontWindow(true);
            ExecutionService debugService = ExecutionService.newService(new Callable<Process>() {
                public Process call() throws Exception {
                    try {
                        ProcessBuilder pb = new ProcessBuilder(cmdLine);
                        pb.directory(getProcessDir());

                        pb.redirectErrorStream(true);
                        debugProxyProcess = pb.start();
                        processes.add(debugProxyProcess);
                        return debugProxyProcess;
                    } finally {
                        c.countdown();
                    }
                }
            }, ed, NbBundle.getMessage(SunJavaCardServer.class, "DEBUG_TAB_NAME", getId()));   //NOI18N
            debugService.run();
        }
        return c;
    }

    public final void stopServer() {
        if (isNotRunning()) {
            return;
        }
        String msg = NbBundle.getMessage (SunJavaCardServer.class, "MSG_STOPPING", getId()); //NOI18N
        StatusDisplayer.getDefault().setStatusText(msg);

        try {
            // stop the card
            Process sProc;
            Process dProc;
            synchronized (this) {
                sProc = serverProcess;
                dProc = debugProxyProcess;
                serverProcess = null;
                debugProxyProcess = null;
            }
            if (sProc == null) {
                return;
            }
            sProc.destroy();
            sProc.waitFor();
            processes.remove(sProc);

            if (sProc.exitValue() == 0) {
                msg = NbBundle.getMessage (SunJavaCardServer.class, "MSG_DONE"); //NOI18N
                StatusDisplayer.getDefault().setStatusText(msg);
            } else {
                msg = NbBundle.getMessage (SunJavaCardServer.class, "MSG_DONE_WITH_EXIT_CODE", //NOI18N
                        sProc.exitValue());
                StatusDisplayer.getDefault().setStatusText(msg);
            }
            if (dProc != null) {
                dProc.destroy();
                dProc.waitFor();
            }
            processes.remove(dProc);
            //XXX replace busywait with future
            while (getState() != CardState.NOT_RUNNING) {
                Thread.sleep (20);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public final void resumeServer() {
        if (!isNotRunning()) {
            return;
        }
        String[] cls = getResumeCommandLine();
        if (cls == null || cls.length <= 0) {
            return;
        }
        setState (NOT_RUNNING);

        final List<String> cmd = Arrays.asList(cls);
        ExecutionDescriptor ed = new ExecutionDescriptor().controllable(true).frontWindow(true).preExecution(
                new Runnable() {
                    public void run() {
                        setState (RESUMING);
                    }
                }
        ).postExecution(new Runnable() {
            public void run() {
                setState (NOT_RUNNING);
            }
        });
        ExecutionService server = ExecutionService.newService(new Callable<Process>() {

            public Process call() throws Exception {
                try {
                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.directory(getProcessDir());
                    pb.redirectErrorStream(true);
                    if (!shutdownHookAdded) {
                        installShutdownHook();
                    }
                    Process p = pb.start();
                    processes.add(serverProcess);
                    synchronized (SunJavaCardServer.this) {
                        serverProcess = p;
                    }
                    return p;
                } finally {
                    setState (RUNNING);
                    String msg = NbBundle.getMessage(SunJavaCardServer.class, 
                            "MSG_READY", getId()); //NOI18N
                    StatusDisplayer.getDefault().setStatusText(msg);
                }
            }
        }, ed, getId());
        server.run();
    }

    /**
     * Gets the root URL of this server. All deployed content URL are under
     * this URL. To calculate the URL of an applicaton deployed on this server
     * one should append the web context path of that application to the value 
     * returned by this method. Note, that web context path starts with forward
     * slash.
     * 
     * @return URL of this server.
     */
    public abstract String getServerURL();

    /**
     * Gets the URL of the Card Manager Application that should be used for
     * all card management operation (load, create, unload...) with this server.
     * 
     * @return URL of the Card Manager Application.
     */
    public abstract String getCardManagerURL();

    /**
     * @return the jcdkHome
     */
    public final File getJcdkHome() {
        return platform.getHome();
    }

    /**
     * @return the userName
     */
    public final String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public final void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the password
     */
    public final String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public final void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        boolean result = o != null && o.getClass() == SunJavaCardServer.class;
        if (result) {
            SunJavaCardServer other = (SunJavaCardServer) o;
            result = other.getId().equals(getId());
            if (result) {
                result = other.getPlatform().equals(getPlatform());
            }
        }
        return result;
    }

    /**
     * 
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (getId() != null ? getId().hashCode() : 0);
        hash = 11 * hash + getPlatform().hashCode();
        return hash;
    }

    private synchronized void installShutdownHook() {
        shutdownHookAdded = true;
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown(),
                "JavaCard Server Process Destroyer Shutdown Hook")); //NOI18N
    }

    private static final class Shutdown implements Runnable {

        public void run() {
            for (Iterator<Process> i = processes.iterator(); i.hasNext();) {
                Process p = i.next();
                if (p != null) { //WeakSet can return null
                    p.destroy();
                }
            }
        }
    }
}
