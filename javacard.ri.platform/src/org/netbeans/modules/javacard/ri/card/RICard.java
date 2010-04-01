/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.ri.card;

import org.netbeans.modules.javacard.spi.BaseCard;
import com.sun.javacard.filemodels.ParseErrorHandler;
import com.sun.javacard.filemodels.XListModel;
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javacard.api.AntClasspathClosureProvider;
import org.netbeans.modules.javacard.api.RunMode;
import org.netbeans.modules.javacard.common.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.common.NodeRefresher;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.ri.platform.loader.CardChildren;
import org.netbeans.modules.javacard.spi.capabilities.AntTargetInterceptor;
import org.netbeans.modules.javacard.spi.capabilities.UrlCapability;
import org.netbeans.modules.javacard.spi.capabilities.CardContentsProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.CardState;
import org.netbeans.modules.javacard.spi.capabilities.ClearEpromCapability;
import static org.netbeans.modules.javacard.spi.CardState.*;
import org.netbeans.modules.javacard.spi.capabilities.DebugCapability;
import org.netbeans.modules.javacard.spi.capabilities.EpromFileCapability;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.capabilities.AntTarget;
import org.netbeans.modules.javacard.spi.capabilities.CardCustomizerProvider;
import org.netbeans.modules.javacard.spi.capabilities.ContactedProtocol;
import org.netbeans.modules.javacard.spi.capabilities.DeleteCapability;
import org.netbeans.modules.javacard.spi.capabilities.PortKind;
import org.netbeans.modules.javacard.spi.capabilities.PortProvider;
import org.netbeans.modules.javacard.spi.capabilities.ProfileCapability;
import org.netbeans.modules.javacard.spi.capabilities.ResumeCapability;
import org.netbeans.modules.javacard.spi.capabilities.StartCapability;
import org.netbeans.modules.javacard.spi.capabilities.StopCapability;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Implementation of Card for the Java Card Reference Implementation (and
 * possibly others)
 *
 * @author Tim Boudreau
 */
public class RICard extends BaseCard<CardProperties> { //non-final only for unit tests

    protected Process serverProcess;
    protected Process debugProxyProcess;
    private final DataObject dob;

    public RICard(DataObject dob, JavacardPlatform platform, String sysId) {
        super(platform, sysId, CardProperties.class);
        this.dob = dob;
        log("Created a card for " + dob.getPrimaryFile().getPath() + //NOI18N
                " " + platform + " as " + sysId); //NOI18N
    }

    @Override
    protected void clearProcessReferences() {
        synchronized (this) {
            serverProcess = null;
            debugProxyProcess = null;
        }
    }

    @Override
    protected CardProperties loadData() {
        PropertiesAdapter adap = dob.getLookup().lookup(PropertiesAdapter.class);
        assert adap != null;
        CardProperties props = new CardProperties(adap);
        log("Init props " + props); //NOI18N
        return props;
    }

    @Override
    public DeleteCapability createDeleteCapability(CardProperties t) {
        return new Delete();
    }

    @Override
    protected UrlCapability createApduSupport(CardProperties t) {
        return new Apdu();
    }

    @Override
    protected CardContentsProvider createCardContentProvider(CardProperties t) {
        return new Contents();
    }

    @Override
    protected AntTargetInterceptor createAntTargetInterceptor(CardProperties t) {
        return new Interceptor();
    }

    @Override
    protected PortProvider createPortProvider(CardProperties t) {
        return new Ports();
    }

    @Override
    protected CardInfo createCardInfo(CardProperties t) {
        return new Info();
    }

    @Override
    protected StopCapability createStopCapability(CardProperties t) {
        return new Stop();
    }

    @Override
    protected ProfileCapability createProfileCapability(CardProperties t) {
        return new Profile();
    }

    @Override
    protected DebugCapability createDebugCapability(CardProperties t) {
        return new Debug();
    }

    @Override
    protected StartCapability createStartCapability(CardProperties t) {
        return new Start();
    }

    @Override
    protected ClearEpromCapability createClearEpromCapability(CardProperties t) {
        return new ClearEprom();
    }

    @Override
    protected EpromFileCapability createEpromCapability(CardProperties t) {
        return new Eprom();
    }

    @Override
    protected ResumeCapability createResumeCapability(CardProperties t) {
        return new Resume();
    }

    @Override
    protected CardCustomizerProvider createCardCustomizerProvider(CardProperties t) {
        Lookup lkp = Lookups.forPath(CommonSystemFilesystemPaths.SFS_ADD_HANDLER_REGISTRATION_ROOT + getPlatform().getPlatformKind());
        return lkp.lookup(CardCustomizerProvider.class);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && dob.isValid();
    }

    private String[] getDebugProxyCommandLine(Project project) {
        CardProperties p = getCapability(CardProperties.class);
        File jar = AntClasspathClosureProvider.getTargetArtifact(project);
        String classpathClosure = AntClasspathClosureProvider.getClasspathClosure(project);
        if (classpathClosure != null && !"".equals(classpathClosure)) {
            classpathClosure += File.pathSeparator + jar.getAbsolutePath();
        } else {
            classpathClosure = jar.getAbsolutePath();
        }
        return p.getDebugProxyCommandLine(getPlatform().toProperties(),
                classpathClosure);
    }

    private String[] getStartCommandLine(RunMode mode) {
        CardProperties p = getCapability(CardProperties.class);
        assert p != null;
        boolean forDebug = mode.isDebug();
        boolean suspend = forDebug ? false : p.isSuspend();
        Properties props = getPlatform().toProperties();
        return p.getRunCommandLine(props, forDebug, suspend, false);
    }

    private String[] getResumeCommandLine(RunMode mode) {
        CardProperties p = getCapability(CardProperties.class);
        boolean debug = mode.isDebug();
        boolean suspend = debug ? false : p.isSuspend();
        return p.getRunCommandLine(getPlatform().toProperties(), debug, suspend, true);
    }

    private String getDisplayName() {
        return getCapability(CardInfo.class).getDisplayName();
    }

    @Override
    protected void onStateChanged(CardState old, CardState nue) {
        super.onStateChanged(old, nue);
        //Avoid needing to listen for state changes
        NodeRefresher refresh = dob.getLookup().lookup(NodeRefresher.class);
        if (refresh != null) {
            refresh.refreshNode();
        }
    }
    //Kill methods are emergency fallbacks

    private void killProcesses() {
        Process sProc, dProc;
        synchronized (this) {
            sProc = serverProcess;
            dProc = debugProxyProcess;
        }
        kill(sProc);
        kill(dProc);
    }

    private void kill(Process p) {
        if (p != null) {
            try {
                p.destroy();
            } catch (Exception e) {
                Logger.getLogger(RICard.class.getName()).log(Level.WARNING,
                        "Could not destroy " + p + " for " + getSystemId(), e); //NOI18N
            }
        }
    }

    @Override
    public void log(String toLog) {
        super.log (toLog);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getSystemId() + "]"; //NOI18N
    }

    @Override
    public boolean equals (Object o) {
        return o != null && o.getClass() == RICard.class && ((RICard) o).dob.equals(this.dob);
    }

    @Override
    public int hashCode() {
        return dob.hashCode();
    }

    private class Start implements StartCapability {

        public Condition start(RunMode mode, Project project) {
            log(RICard.this + " start " + mode); //NOI18N
            if (project == null && mode != RunMode.RUN) {
                throw new NullPointerException("Project parameter required for DEBUG/PROFILE run modes"); //NOI18N
            }
            final boolean debug = mode == RunMode.DEBUG;
            if (!getState().isNotRunning()) {
                log("Already running, return dummy condition"); //NOI18N
                return new ConditionImpl();
            }
            final ConditionImpl c = new ConditionImpl(debug ? 2 : 1, RICard.this);
            setState(BEFORE_STARTING);
            rp.post(new Starter(c, mode, project));
            return c;
        }
    }

    private final class Starter implements Runnable {

        private final ConditionImpl c;
        private final RunMode mode;
        private final Project project;

        private Starter(ConditionImpl c, RunMode mode, Project project) {
            this.c = c;
            this.mode = mode;
            this.project = project;
        }

        public void run() {
            setState(STARTING);
            try {
                boolean debug = mode.isDebug();
                final String[] cls = getStartCommandLine(mode);
                log(RICard.this + " to start command " + Arrays.asList(cls)); //NOI18N
                if (cls == null || cls.length <= 0) {
                    throw new IllegalStateException("0 length start command line for " + getSystemId()); //NOI18N
                }

                ExecutionDescriptor ed = new ExecutionDescriptor().controllable(true).
                        frontWindow(true).preExecution(new StateChange(STARTING)).postExecution(new StateChange(NOT_RUNNING));

                ProcessLaunch launcher = new ProcessLaunch(cls, debug ? RUNNING_IN_DEBUG_MODE : RUNNING, c, true);
                ExecutionService exeService = ExecutionService.newService(launcher,
                        ed, getDisplayName());
                log("Starting process for " + RICard.this); //NOI18N
                exeService.run();
                if (debug) {
                    final String[] cmdLine = getDebugProxyCommandLine(project);
                    log("Will start debug process " + Arrays.asList(cmdLine)); //NOI18N
                    if (cls.length <= 0) {
                        throw new IllegalStateException("Debug command line empty"); //NOI18N
                    }
                    ed = new ExecutionDescriptor().controllable(true).frontWindow(true);
                    ProcessLaunch debugLaunch = new ProcessLaunch(cmdLine, RUNNING_IN_DEBUG_MODE, c, false);
                    ExecutionService debugService = ExecutionService.newService(debugLaunch,
                            ed, NbBundle.getMessage(RICard.class, "DEBUG_TAB_NAME", getDisplayName()));   //NOI18N
                    log("Starting debug process"); //NOI18N
                    debugService.run();
                }
            } catch (Exception e) {
                Logger.getLogger(RICard.class.getName()).log(Level.SEVERE,
                        "Problem starting " + getSystemId(), e); //NOI18N
                killProcesses();
                setState(NOT_RUNNING);
                c.signalAll();
            }
        }
    }

    private class Stop implements StopCapability {

        public Condition stop() {
            log(RICard.this + " stop"); //NOI18N
            if (getState().isNotRunning()) {
                log("Not actually running, return dummy condition"); //NOI18N
                return new ConditionImpl();
            }
            setState(BEFORE_STOPPING);
            Process sProc;
            Process dProc;
            synchronized (RICard.this) {
                sProc = serverProcess;
                dProc = debugProxyProcess;
            }
            if (sProc == null && dProc == null) {
                setState(NOT_RUNNING);
                return new ConditionImpl();
            }
            int countdown = 1 + (sProc == null ? 1 : 0) + (dProc == null ? 1 : 0);
            final ConditionImpl result = new ConditionImpl(countdown, RICard.this);
            Stopper stopRunnable = new Stopper(sProc, dProc, result);
            log("Posting stop runnable"); //NOi18N
            rp.post(stopRunnable, 0, Thread.MAX_PRIORITY);
            return result;
        }
    }

    private class Stopper implements Runnable {

        private final Process sProc;
        private final Process dProc;
        private final ConditionImpl condition;

        Stopper(Process sProc, Process dProc, ConditionImpl condition) {
            this.sProc = sProc;
            this.dProc = dProc;
            this.condition = condition;
        }

        public void run() {
            log("Stop Runnable for " + RICard.this + " trying to stop process"); //NOI18N
            setState(STOPPING);
            try {
                if (sProc != null) {
                    log("Destroy process " + sProc); //NOI18N
                    sProc.destroy();
                    log("Wait for exit of " + sProc); //NOI18N
                    sProc.waitFor();
                    log(sProc + " exited"); //NOI18N

                    if (sProc.exitValue() != 0) {
                        String msg = NbBundle.getMessage(RICard.class,
                                "MSG_DONE_WITH_EXIT_CODE", //NOI18N
                                getDisplayName(), sProc.exitValue());
                        StatusDisplayer.getDefault().setStatusText(msg);
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                condition.countdown();
                try {
                    if (dProc != null) {
                        log("Destroy debug process " + dProc); //NOI18N
                        dProc.destroy();
                        log("Debug process destroyed"); //NOI18N
                        dProc.waitFor();
                        log("Debug process exited"); //NOI18N
                        if (dProc.exitValue() != 0) {
                            String msg = NbBundle.getMessage(RICard.class,
                                    "MSG_DEBUG_DONE_WITH_EXIT_CODE", //NOI18N
                                    getDisplayName(), sProc.exitValue());
                            StatusDisplayer.getDefault().setStatusText(msg);
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    condition.countdown();
                }
            }
            CardState state = getState();
            log("Enter busywait for state change to NOT_RUNNING.  Current state " + state); //NOI18N
            while ((state = getState()) != CardState.NOT_RUNNING) {
                //XXX busywait
                try {
                    Thread.sleep(60);
                    log("Busywait loop, state " + state); //NOI18N
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            log("Finishing stop - clearing process references " + sProc + " and " + dProc); //NOI18N
            synchronized (RICard.this) {
                //Theoretically could have changed, although it shouldn't
                if (serverProcess == sProc) {
                    serverProcess = null;
                }
                if (debugProxyProcess == dProc) {
                    debugProxyProcess = null;
                }
            }
            log("Completing countdown to notify waiters"); //NOI18N
            condition.countdown();
        }
    }

    private final class StateChange implements Runnable {

        private final CardState newState;

        StateChange(CardState newState) {
            this.newState = newState;
        }

        public void run() {
            log("StateChange set state to " + newState);
            setState(newState);
        }
    }

    private class Resume implements ResumeCapability {

        public Condition resume(RunMode mode) {
            if (!getState().isNotRunning()) {
                return new ConditionImpl();
            }
            String[] cls = getResumeCommandLine(mode);
            if (cls == null || cls.length <= 0) {
                return new ConditionImpl();
            }
            setState(BEFORE_RESUMING);
            final ConditionImpl result = new ConditionImpl(1, RICard.this);
            final List<String> cmd = Arrays.asList(cls);
            ExecutionDescriptor ed = new ExecutionDescriptor().controllable(true).frontWindow(true).preExecution(
                    new StateChange(RESUMING)).postExecution(new StateChange(NOT_RUNNING));
            ExecutionService server = ExecutionService.newService(new Callable<Process>() {

                public Process call() throws Exception {
                    try {
                        ProcessBuilder pb = new ProcessBuilder(cmd);
                        pb.directory(new File(System.getProperty("user.home"))); //NOI18N
                        pb.redirectErrorStream(true);
                        Process p = pb.start();
                        registerProcess(serverProcess);
                        synchronized (RICard.this) {
                            serverProcess = p;
                        }
                        setState(RUNNING);
                        result.countdown();
                        return p;
                    } catch (Exception e) {
                        Logger.getLogger(RICard.class.getName()).log(
                                Level.SEVERE, "Exception resuming " + //NOI18N
                                getSystemId(), e);
                        killProcesses();
                        return null;
                    }
                }
            }, ed, getDisplayName());
            server.run();
            return result;
        }
    }

    private class Ports implements PortProvider {

        public Set<Integer> getClaimedPorts() {
            return getCapability(CardProperties.class).getPorts();
        }

        public Set<Integer> getPortsInUse() {
            CardState state = getState();
            if (state == CardState.RUNNING || state == CardState.RUNNING_IN_DEBUG_MODE) {
                Set<Integer> result = getClaimedPorts();
                if (state != CardState.RUNNING_IN_DEBUG_MODE) {
                    result.remove(Integer.parseInt(getCapability(CardProperties.class).getProxy2idePort()));
                }
                return result;
            }
            return Collections.emptySet();
        }

        public String getHost() {
            return getCapability(CardProperties.class).getHost();
        }

        public int getPort(PortKind kind) {
            CardProperties p = getCapability(CardProperties.class);
            assert p != null;
            String port;
            switch (kind) {
                case HTTP:
                    port = p.getHttpPort();
                    break;
                case CONTACTED:
                    port = p.getContactedPort();
                    break;
                case CONTACTLESS:
                    port = p.getContactlessPort();
                    break;
                case DEBUG_IDE_TO_RUNTIME_PROXY:
                    port = p.getProxy2cjcrePort();
                    break;
                case DEBUG_RUNTIME_TO_IDE_PROXY:
                    port = p.getProxy2idePort();
                    break;
                default:
                    throw new AssertionError("" + kind); //NOI18N
            }
            return port == null ? -1 : Integer.parseInt(port);
        }
    }

    private class Apdu implements UrlCapability {

        public ContactedProtocol getContactedProtocol() {
            String p = getCapability(CardProperties.class).getContactedProtocol();
            return p == null ? null : ContactedProtocol.forString(p);
        }

        public String getURL() {
            PortProvider p = getCapability(PortProvider.class);
            if (p == null || p.getHost() == null || p.getPort(PortKind.HTTP) <= 0) {
                return null;
            }
            return "http://" + p.getHost() + ":" + p.getPort(PortKind.HTTP) + "/"; //NOI18N
        }

        public String getManagerURL() {
            String url = getURL();
            if (url != null && !url.endsWith("/")) { //NOI18N
                url += '/'; //NOI18N
            }
            return url == null ? null : url + "cardmanager"; //NOI18N
        }

        public String getListURL() {
            String mgrUrl = getManagerURL();
            if (mgrUrl == null) {
                return null;
            }
            if (!mgrUrl.endsWith("/")) { //NOI18N
                mgrUrl += '/'; //NOI18N
            }
            return mgrUrl + "xlist"; //NOI18N
        }
    }

    private class Info implements CardInfo {

        public String getSystemId() {
            return RICard.this.getSystemId();
        }

        public String getDisplayName() {
            return dob.getNodeDelegate().getDisplayName();
        }

        public Image getIcon() {
            return dob.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
        }

        public String getDescription() {
            return dob.getNodeDelegate().getShortDescription();
        }
    }

    private class ClearEprom implements ClearEpromCapability, Runnable {

        public void clear() {
            rp.post(this);
            RICard.this.removeCapability(this);
        }

        public void run() {
            try {
                if (getState().isRunning()) {
                    StopCapability stop = getCapability(StopCapability.class);
                    stop.stop().awaitUninterruptibly();
                }
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ClearEprom.class,
                        "MSG_DELETING_EPROM_FILE", getDisplayName())); //NOI18N
                EpromFileCapability epromFileCap = getCapability(EpromFileCapability.class);
                FileObject epromFile = epromFileCap.getEpromFile();
                assert epromFile != null : "ClearEprom should not be able to " + //NOI18N
                        "be invoked if no eprom file exists"; //NOI18N
                epromFile.delete();
                synchronized (this) {
                    notifyAll(); //for unit tests
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                if (RICard.this.getCapability(ClearEpromCapability.class) == null) {
                    RICard.this.addCapability(this);
                }
            }
        }
    }

    private static class Interceptor implements AntTargetInterceptor {

        public boolean onBeforeInvokeTarget(Project p, AntTarget target, Properties antProperties) {
            return true;
        }

        public void onAfterInvokeTarget(Project p, AntTarget target) {
            //do nothing
        }
    }

    private final class ProcessLaunch implements Callable<Process> {

        private final String[] cmdline;
        private final ConditionImpl condition;
        private final boolean isServerProcess;
        private final CardState targetState;

        ProcessLaunch(String[] cmdline, CardState targetState, ConditionImpl condition, boolean isServerProcess) {
            this.cmdline = cmdline;
            this.condition = condition;
            this.isServerProcess = isServerProcess;
            this.targetState = targetState;
        }

        public Process call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmdline);
                pb.directory(new File(System.getProperty("user.home"))); //NOI18N
                pb.redirectErrorStream();

                pb.redirectErrorStream(true);
                Process p = pb.start();
                registerProcess(p);
                synchronized (RICard.this) {
                    if (isServerProcess) {
                        serverProcess = p;
                    } else {
                        debugProxyProcess = p;
                    }
                }
                return p;
            } finally {
                condition.countdown();
                setState(targetState);
            }
        }
    }

    private final class Eprom implements EpromFileCapability {

        public FileObject getEpromFile() {
            FileObject result = null;
            FileObject fld = Utils.sfsFolderForDeviceEepromsForPlatformNamed(
                    getPlatform().getSystemName(), false);
            if (fld != null) {
                result = fld.getFileObject(getSystemId(),
                        JCConstants.EEPROM_FILE_EXTENSION);
            }
            return result;
        }
    }

    private final class Contents implements CardContentsProvider {

        public XListModel getContents() {
            assert !EventQueue.isDispatchThread() : "May not be called on event " + //NOI18N
                    "thread"; //NOI18N
            UrlCapability urlCap = getCapability(UrlCapability.class);
            if (urlCap != null) {
                String url = urlCap.getListURL();
                if (url != null) {
                    InputStream in = null;
                    try {
                        URL connectTo = new URL(url);
                        in = connectTo.openStream();
                        try {
                            if (Thread.interrupted()) {
                                return null;
                            }
                            XListModel mdl = new XListModel(in, ParseErrorHandler.NULL);
                            return mdl;
                        } catch (IOException ioe) {
                            StatusDisplayer.getDefault().setStatusText(
                                    NbBundle.getMessage(CardChildren.class,
                                    "MSG_LOAD_FAILED", url)); //NOI18N
                            Logger.getLogger(CardChildren.class.getName()).log(
                                    Level.INFO, "Could not load children from " + //NOI18N
                                    "xlist command for " + url, ioe); //NOI18N
                        } finally {
                            in.close();
                        }
                    } catch (IOException ex) {
                        //do not log - perfectly normal if remote process is
                        //not running, which is a common occurence
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException ex) {
                        //do not log - perfectly normal if remote process is
                        //not running, which is a common occurence
                        }
                    }
                }
            }
            return null;
        }
    }

    private static final class Debug implements DebugCapability {
    }

    private static final class Profile implements ProfileCapability {
    }

    private final class Delete implements DeleteCapability {
        public void delete() throws IOException {
            dob.delete();
        }
    }
}
