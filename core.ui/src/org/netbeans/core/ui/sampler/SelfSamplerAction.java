/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.core.ui.sampler;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik, Tomas Hurka
 */
@ActionID(id = "org.netbeans.modules.profiler.actions.SelfSamplerAction", category = "Profile")
@ActionRegistration(iconInMenu = true, displayName = "#SelfSamplerAction_ActionNameStart", iconBase = "org/netbeans/core/ui/sampler/selfSampler.png")
@ActionReferences({
    @ActionReference(path = "Toolbars/Memory", position = 2000),
    @ActionReference(path = "Shortcuts", name = "AS-Y")
})
public class SelfSamplerAction extends AbstractAction implements AWTEventListener {
    // -----
    // I18N String constants
    private static final String ACTION_NAME_START = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ActionNameStart");
    private static final String ACTION_NAME_STOP = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ActionNameStop");
//    private static final String ACTION_DESCR = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ActionDescription");
    private static final String NOT_SUPPORTED = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_NotSupported");
    private static final String SAVE_MSG = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_SavedFile");
    private static final String DEBUG_ARG = "-Xdebug"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(SelfSamplerAction.class.getName());
    private final AtomicReference<Sampler> RUNNING = new AtomicReference<Sampler>();
    private static Boolean debugMode;
    private static String lastReason;
    private static Class defaultDataObject;
    static {
        try {
            defaultDataObject = Class.forName("org.openide.loaders.DefaultDataObject"); // NOI18N
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public SelfSamplerAction() {
        putValue(Action.NAME, ACTION_NAME_START);
        putValue(Action.SHORT_DESCRIPTION, ACTION_NAME_START);
        putValue ("iconBase", "org/netbeans/core/ui/sampler/selfSampler.png"); // NOI18N
        if (System.getProperty(SelfSamplerAction.class.getName() + ".sniff") != null) { //NOI18N
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when an action occurs.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (SamplesOutputStream.isSupported()) {
            Sampler c;
            if (RUNNING.compareAndSet(null, c = new InternalSampler("Self Sampler"))) { // NOI18N
                putValue(Action.NAME, ACTION_NAME_STOP);
                putValue(Action.SHORT_DESCRIPTION, ACTION_NAME_STOP);
                putValue ("iconBase", "org/netbeans/core/ui/sampler/selfSamplerRunning.png"); // NOI18N
                c.run();
            } else if ((c = RUNNING.getAndSet(null)) != null) {
                final Sampler controller = c;

                setEnabled(false);
                SwingWorker worker = new SwingWorker() {

                    @Override
                    protected Object doInBackground() throws Exception {
                        controller.actionPerformed(new ActionEvent(this, 0, "show")); // NOI18N
                        return null;
                    }

                    @Override
                    protected void done() {
                        putValue(Action.NAME, ACTION_NAME_START);
                        putValue(Action.SHORT_DESCRIPTION, ACTION_NAME_START);
                        putValue ("iconBase", "org/netbeans/core/ui/sampler/selfSampler.png"); // NOI18N
                        SelfSamplerAction.this.setEnabled(true);
                    }
                };
                worker.execute();
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
            return new InternalSampler(key);
        }
        return o;
    }

    final boolean isProfileMe(Sampler c) {
        return c == RUNNING.get();
    }
    
    private static synchronized boolean isDebugged() {
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

    private static boolean isRunMode() {
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
        if (!runMode && !reason.equals(lastReason)) {
            LOGGER.log(Level.INFO, "Slowness detector disabled - {0}", reason); // NOI18N
        }
        lastReason = reason;
        return runMode;
    }
    
    private static final class InternalSampler extends Sampler {
        private ProgressHandle progress;
        
        InternalSampler(String thread) {
            super(thread);
        }

        @Override
        protected void printStackTrace(Throwable ex) {
            Exceptions.printStackTrace(ex);
        }

        @Override
        protected void saveSnapshot(byte[] arr) throws IOException {
            // save snapshot
            File outFile = File.createTempFile("selfsampler", SamplesOutputStream.FILE_EXT); // NOI18N
            outFile = FileUtil.normalizeFile(outFile);
            writeToFile(outFile, arr);
            
            File gestures = new File(new File(new File(
                new File(System.getProperty("netbeans.user")), // NOI18N
                "var"), "log"), "uigestures"); // NOI18N
            
            SelfSampleVFS fs;
            if (gestures.exists()) {
                fs = new SelfSampleVFS(
                    new String[] { "selfsampler.npss", "selfsampler.log" }, 
                    new File[] { outFile, gestures }
                );
            } else {
                fs = new SelfSampleVFS(
                    new String[] { "selfsampler.npss" }, 
                    new File[] { outFile }
                );
            }
            
            // open snapshot
            FileObject fo = fs.findResource("selfsampler.npss");
            DataObject dobj = DataObject.find(fo);
            // ugly test for DefaultDataObject
            if (defaultDataObject.isAssignableFrom(dobj.getClass())) {
                String msg = MessageFormat.format(SelfSamplerAction.SAVE_MSG, outFile.getAbsolutePath());
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            } else {
                dobj.getCookie(OpenCookie.class).open();
            }
        }

        private void writeToFile(File file, byte[] arr) {
            try {
                FileOutputStream fstream = new FileOutputStream(file);
                fstream.write(arr);
                fstream.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        protected ThreadMXBean getThreadMXBean() {
            return ManagementFactory.getThreadMXBean();
        }
        
        protected void openProgress(final int steps) {
            if (EventQueue.isDispatchThread()) {
                // log warnining
                return;
            }
            progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(SelfSamplerAction.class, "Save_Progress"));
            progress.start(steps);
        }
        
        protected void closeProgress() {
            if (EventQueue.isDispatchThread()) {
                return;
            }
            progress.finish();
            progress = null;
        }
        
        protected void progress(int i) {
            if (EventQueue.isDispatchThread()) {
                return;
            }
            if (progress != null) {
                progress.progress(i);
            }
        }
    }
}
