/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.core.ui.sampler;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.MessageFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik, Tomas Hurka
 */
class SelfSamplerAction extends AbstractAction implements AWTEventListener {

    final private static class Singleton {

        static final SelfSamplerAction INSTANCE = new SelfSamplerAction();
    }
    // -----
    // I18N String constants
    private static final String ACTION_NAME_START = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ActionNameStart");
    private static final String ACTION_NAME_STOP = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ActionNameStop");
//    private static final String ACTION_DESCR = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ActionDescription");
    private static final String THREAD_NAME = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ThreadName");
    private static final String NOT_SUPPORTED = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_NotSupported");
    private static final String SAVE_MSG = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_SavedFile");
    private static final String DEBUG_ARG = "-Xdebug"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(SelfSamplerAction.class.getName());
    private static final int SAMPLER_RATE = 10;
    private static final double MAX_AVERAGE = SAMPLER_RATE * 3;
    private static final double MAX_STDDEVIATION = SAMPLER_RATE * 4;
    private final AtomicReference<Controller> RUNNING = new AtomicReference<Controller>();
    private Boolean debugMode;
    private String lastReason;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    private SelfSamplerAction() {
        putValue(Action.NAME, ACTION_NAME_START);
//        putValue(Action.SHORT_DESCRIPTION, ACTION_DESCR);
        putValue(Action.SMALL_ICON,
                ImageUtilities.loadImageIcon("org/netbeans/core/ui/sampler/selfSampler.png" //NOI18N
                , false));
        if (System.getProperty(SelfSamplerAction.class.getName() + ".sniff") != null) { //NOI18N
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        }
    }

    public static SelfSamplerAction getInstance() {
        return Singleton.INSTANCE;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Invoked when an action occurs.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (SamplesOutputStream.isSupported()) {
            Controller c;
            if (RUNNING.compareAndSet(null, c = new Controller(THREAD_NAME))) {
                putValue(Action.NAME, ACTION_NAME_STOP);
                putValue(Action.SMALL_ICON,
                        ImageUtilities.loadImageIcon(
                        "org/netbeans/core/ui/sampler/selfSamplerRunning.png" //NOI18N
                        , false));
                c.run();
            } else if ((c = RUNNING.getAndSet(null)) != null) {
                putValue(Action.NAME, ACTION_NAME_START);
                putValue(Action.SMALL_ICON,
                        ImageUtilities.loadImageIcon(
                        "org/netbeans/core/ui/sampler/selfSampler.png" //NOI18N
                        , false));
                c.actionPerformed(new ActionEvent(this, 0, "show")); // NOI18N
            }
        } else {
            NotifyDescriptor d = new NotifyDescriptor.Message(NOT_SUPPORTED, NotifyDescriptor.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        KeyEvent kevent = (KeyEvent) event;
        if (kevent.getID() == KeyEvent.KEY_RELEASED && kevent.getKeyCode() == KeyEvent.VK_ALT_GRAPH) { // AltGr
            actionPerformed(new ActionEvent(this, event.getID(), "shortcut")); //NOI18N
            kevent.consume();
        }
    }

    @Override
    public Object getValue(String key) {
        Object o = super.getValue(key);
        if (o == null && key.startsWith("logger-") && SamplesOutputStream.isSupported() && isRunMode()) { // NOI18N
            return new Controller(key);
        }
        return o;
    }

    private synchronized boolean isDebugged() {
        if (debugMode == null) {
            debugMode = Boolean.FALSE;

            // check if we are debugged
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            List<String> args = runtime.getInputArguments();
            if (args.contains(DEBUG_ARG)) {
                debugMode = Boolean.TRUE;
            }
        }
        return debugMode.booleanValue();
    }

    private boolean isRunMode() {
        boolean runMode = true;
        String reason = null;

        if (isDebugged()) {
            reason = "running in debug mode";   // NOI18N
            runMode = false;
        }
        if (runMode) {
            // check if netbeans is profiled
            try {
                Class.forName("org.netbeans.lib.profiler.server.ProfilerServer", false, ClassLoader.getSystemClassLoader()); // NO18N
                reason = "running under profiler";   // NOI18N
                runMode = false;
            } catch (ClassNotFoundException ex) {
            }
        }
        if (!runMode && reason != lastReason) {
            LOGGER.log(Level.INFO, "Slowness detector disabled - " + reason); // NOI18N
        }
        lastReason = reason;
        return runMode;
    }

    private static final class Controller implements Runnable, ActionListener {

        private static Class defaultDataObject;
        private final String name;
        private Timer timer;
        private ByteArrayOutputStream out;
        private SamplesOutputStream samplesStream;
        private long startTime;
        private long samples, laststamp;
        private double max, min = Long.MAX_VALUE, sum, devSquaresSum;
        private volatile boolean stopped;

        static {
            try {
                defaultDataObject = Class.forName("org.openide.loaders.DefaultDataObject"); // NOI18N
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Controller(String n) {
            name = n;
        }

        private void updateStats(long timestamp) {
            if (laststamp != 0) {
                double diff = (timestamp - laststamp) / 1000000.0;
                samples++;
                sum += diff;
                devSquaresSum += (diff - SAMPLER_RATE) * (diff - SAMPLER_RATE);
                if (diff > max) {
                    max = diff;
                } else if (diff < min) {
                    min = diff;
                }
            }
            laststamp = timestamp;
        }

        @Override
        public void run() {
            final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

            out = new ByteArrayOutputStream(64 * 1024);
            try {
                samplesStream = new SamplesOutputStream(out);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            startTime = System.currentTimeMillis();
            timer = new Timer(name);
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    synchronized (Controller.this) {
                        if (stopped) {
                            return;
                        }
                        try {
                            ThreadInfo[] infos = threadBean.dumpAllThreads(false, false);
                            long timestamp = System.nanoTime();
                            samplesStream.writeSample(infos, timestamp, Thread.currentThread().getId());
                            updateStats(timestamp);
                        } catch (Throwable ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }, SAMPLER_RATE, SAMPLER_RATE);
        }

        @Override
        public synchronized void actionPerformed(ActionEvent e) {
            try {
                stopped = true;
                timer.cancel();
                if ("cancel".equals(e.getActionCommand())) {    // NOI18N
                    return;
                }
                double average = sum / samples;
                double std_deviation = Math.sqrt(devSquaresSum / samples);
                boolean writeCommand = "write".equals(e.getActionCommand());    // NOI18N

                if (writeCommand) {
                    Object[] params = new Object[]{
                        startTime,
                        "Samples", samples, // NOI18N
                        "Average", average, // NOI18N
                        "Minimum", min, // NOI18N
                        "Maximum", max, // NOI18N
                        "Std. deviation", std_deviation // NOI18N
                    };
                    Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Snapshot statistics", params);   // NOI18N
                    if (average > MAX_AVERAGE || std_deviation > MAX_STDDEVIATION) { // do not take snapshot if the sampling was not regular enough 
                        return;
                    }
                }
                samplesStream.close();
                if (writeCommand) {
                    DataOutputStream dos = (DataOutputStream) e.getSource();
                    dos.write(out.toByteArray());
                    dos.close();
                    return;
                }
                // save snapshot
                File outFile = File.createTempFile("selfsampler", SamplesOutputStream.FILE_EXT);
                writeToFile(outFile);
                // open snapshot
                FileObject fo = FileUtil.toFileObject(outFile);
                DataObject dobj = DataObject.find(fo);
                if (defaultDataObject.isAssignableFrom(dobj.getClass())) {  // ugly test for DefaultDataObject
                    String msg = MessageFormat.format(SAVE_MSG, outFile.getAbsolutePath());
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                } else {
                    dobj.getCookie(OpenCookie.class).open();
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                out = null;
                samplesStream = null;
            }
        }

        private void writeToFile(File file) {
            try {
                FileOutputStream fstream = new FileOutputStream(file);

                fstream.write(out.toByteArray());
                fstream.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
