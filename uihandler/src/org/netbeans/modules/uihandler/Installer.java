/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.uihandler.LogRecords;
import org.netbeans.lib.uihandler.PasswdEncryption;
import org.netbeans.modules.exceptions.ExceptionsSettings;
import org.netbeans.modules.exceptions.ReportPanel;
import org.netbeans.modules.exceptions.ReporterResultTopComponent;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.io.NullOutputStream;
import org.openide.windows.WindowManager;
import org.xml.sax.SAXException;

/**
 * Registers and unregisters loggers.
 */
public class Installer extends ModuleInstall implements Runnable {
    static final String IDE_STARTUP = "IDE_STARTUP";
    static final long serialVersionUID = 1L;

    static final String USER_CONFIGURATION = "UI_USER_CONFIGURATION";   // NOI18N
    private static UIHandler ui = new UIHandler(false);
    private static UIHandler handler = new UIHandler(true);
    private static MetricsHandler metrics = new MetricsHandler();
    static final Logger LOG = Logger.getLogger(Installer.class.getName());
    public static final RequestProcessor RP = new RequestProcessor("UI Gestures"); // NOI18N
    public static final RequestProcessor RP_UI = new RequestProcessor("UI Gestures - Create Dialog"); // NOI18N
    public static final RequestProcessor RP_SUBMIT = new RequestProcessor("UI Gestures - Submit Data", 2); // NOI18N
    public static RequestProcessor RP_OPT = null;
    private static final Preferences prefs = NbPreferences.forModule(Installer.class);
    private static OutputStream logStream;
    private static OutputStream logStreamMetrics;
    private static int logsSize;
    private static int logsSizeMetrics;
    private static long logsFirstDateMetric;
    private static URL hintURL;
    private static Object[] selectedExcParams;

    private static boolean logMetricsEnabled = false;
    /** Flag to store status of last metrics upload */
    private static boolean logMetricsUploadFailed = false;
    
    /** Log records currently displaying/uploading */
    private static final ThreadLocal<List<LogRecord>> logRecords = new ThreadLocal<List<LogRecord>>();
    
    private static final  String USAGE_STATISTICS_ENABLED          = "usageStatisticsEnabled"; // NOI18N
    private static final String USAGE_STATISTICS_SET_BY_IDE       = "usageStatisticsSetByIde"; // NOI18N
    private static final String USAGE_STATISTICS_NB_OF_IDE_STARTS = "usageStatisticsNbOfIdeStarts"; // NOI18N
    private static final String CORE_PREF_NODE = "org/netbeans/core"; // NOI18N
    private static final Preferences corePref = NbPreferences.root().node (CORE_PREF_NODE);

    private JButton metricsEnable = new JButton();
    private JButton metricsCancel = new JButton();

    private static final String CMD_METRICS_ENABLE = "MetricsEnable";   // NOI18N
    private static final String CMD_METRICS_CANCEL = "MetricsCancel";   // NOI18N

    /** Action listener for Usage Statistics Reminder dialog */
    private ActionListener l = new ActionListener () {
        @Override
        public void actionPerformed (ActionEvent ev) {
            cmd = ev.getActionCommand();
            if (CMD_METRICS_ENABLE.equals(cmd)) {
                corePref.putBoolean(USAGE_STATISTICS_ENABLED, true);
            } else if (CMD_METRICS_CANCEL.equals(cmd)) {
                corePref.putBoolean(USAGE_STATISTICS_ENABLED, false);
            }
            corePref.putBoolean(USAGE_STATISTICS_SET_BY_IDE, true);
        }
    };

    private String cmd;

    static final String METRICS_LOGGER_NAME = "org.netbeans.ui.metrics"; // NOI18N
    private static Pattern ENCODING = Pattern.compile(
        "<meta.*http-equiv=['\"]Content-Type['\"]" +
        ".*content=.*charset=([A-Za-z0-9\\-]+)['\"]>", Pattern.CASE_INSENSITIVE
    ); // NOI18N

    static boolean preferencesWritable = false;
    static final String preferencesWritableKey = "uihandler.preferences.writable.check"; // NOI18N
    static {
        // #131128 - suppress repetitive exceptions when config/Preferences/org/netbeans/modules/uihandler.properties
        // is not writable for some reason
        long checkTime = System.currentTimeMillis();
        try {
            prefs.putLong(preferencesWritableKey, checkTime);
        } catch (IllegalArgumentException iae) {
            // #164580 - ignore IllegalArgumentException: Malformed \\uxxxx encoding.
            // prefs are now empty, so put again and rewrite broken content.
            prefs.putLong(preferencesWritableKey, checkTime);
        }
        try {
            prefs.flush();
            prefs.sync();
            if(checkTime == prefs.getLong(preferencesWritableKey, 0)) {  //NOI18N
                preferencesWritable = true;
            }
        } catch (BackingStoreException e) {
            // immediatelly show dialog with exception (usually Access is denied)
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * Used to synchronize access to ui log files to avoid writing to/deleting/renaming file
     * which is being parsed in another thread.
     */
    private static final Object UIGESTURE_LOG_LOCK = new Object();

    /**
     * Used to synchronize access to metrics log files to avoid writing to/deleting/renaming file
     * which is being parsed in another thread.
     */
    private static final Object METRICS_LOG_LOCK = new Object();

    private static enum DataType {
        DATA_UIGESTURE,
        DATA_METRICS
    };

    @Override
    public void restored() {
        TimeToFailure.logAction();
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINEST);
        log.addHandler(ui);
        Logger all = Logger.getLogger("");
        all.addHandler(handler);
        logsSize = prefs.getInt("count", 0);
        logsSizeMetrics = prefs.getInt("countMetrics", 0);
        logsFirstDateMetric = prefs.getLong("firstDateMetric", -1);
        logMetricsUploadFailed = prefs.getBoolean("metrics.upload.failed", false); // NOI18N
        corePref.addPreferenceChangeListener(new PrefChangeListener());

        if (!Boolean.getBoolean("netbeans.full.hack") && !Boolean.getBoolean("netbeans.close")) {
            usageStatisticsReminder();
        }
        
        System.setProperty("nb.show.statistics.ui",USAGE_STATISTICS_ENABLED);
        logMetricsEnabled = corePref.getBoolean(USAGE_STATISTICS_ENABLED, false);
        if (logMetricsEnabled) {
            //Handler for metrics
            log = Logger.getLogger(METRICS_LOGGER_NAME);
            log.setUseParentHandlers(true);
            log.setLevel(Level.FINEST);
            log.addHandler(metrics);
            try {
                LogRecord userData = getUserData(log);
                LogRecords.write(logStreamMetrics(), userData);
                List<LogRecord> enabledRec = new ArrayList<LogRecord>();
                List<LogRecord> disabledRec = new ArrayList<LogRecord>();
                getModuleList(log, enabledRec, disabledRec);
                for (LogRecord rec : enabledRec) {
                    LogRecords.write(logStreamMetrics(), rec);
                }
                for (LogRecord rec : disabledRec) {
                    LogRecords.write(logStreamMetrics(), rec);
                }
                LogRecord clusterRec = getClusterList(log);
                LogRecords.write(logStreamMetrics(), clusterRec);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        EarlyHandler.disable();
        CPUInfo.logCPUInfo();
        ScreenSize.logScreenSize();
        logIdeStartup();

        for (Activated a : Lookup.getDefault().lookupAll(Activated.class)) {
            a.activated(log);
        }

        if (logsSize >= UIHandler.MAX_LOGS) {
            WindowManager.getDefault().invokeWhenUIReady(this);
        }
    }
    
    /** Accessed from tests. */
    static int getLogsSizeTest() {
        return logsSize;
    }
    
    private void logIdeStartup() {
        Logger.getLogger("org.netbeans.ui").log(new LogRecord(Level.CONFIG, IDE_STARTUP));
    }

    private void usageStatisticsReminder () {
        //Increment number of IDE starts, stop at 4 because we are interested at second start
        long nbOfIdeStarts = corePref.getLong(USAGE_STATISTICS_NB_OF_IDE_STARTS, 0);
        nbOfIdeStarts++;
        if (nbOfIdeStarts < 4) {
            corePref.putLong(USAGE_STATISTICS_NB_OF_IDE_STARTS, nbOfIdeStarts);
        }
        boolean setByIde = corePref.getBoolean(USAGE_STATISTICS_SET_BY_IDE, false);
        boolean usageEnabled = corePref.getBoolean(USAGE_STATISTICS_ENABLED, false);

        //If "usageStatisticsEnabled" was set by IDE do not ask again.
        if (setByIde) {
            return;
        }
        //If "usageStatisticsEnabled" was not set by IDE, it is false and it is second start ask again
        if (!setByIde && !usageEnabled && (nbOfIdeStarts == 2)) {
            metricsEnable.addActionListener(l);
            metricsEnable.setActionCommand(CMD_METRICS_ENABLE);
            //registerNow.setText(NbBundle.getMessage(RegisterAction.class,"LBL_RegisterNow"));
            Mnemonics.setLocalizedText(metricsEnable, NbBundle.getMessage(
                    Installer.class, "LBL_MetricsEnable"));
            metricsEnable.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(Installer.class,"ACSN_MetricsEnable"));
            metricsEnable.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(Installer.class,"ACSD_MetricsEnable"));
            
            metricsCancel.addActionListener(l);
            metricsCancel.setActionCommand(CMD_METRICS_CANCEL);
            //registerLater.setText(NbBundle.getMessage(RegisterAction.class,"LBL_RegisterLater"));
            Mnemonics.setLocalizedText(metricsCancel, NbBundle.getMessage(
                    Installer.class, "LBL_MetricsCancel"));
            metricsCancel.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(Installer.class,"ACSN_MetricsCancel"));
            metricsCancel.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(Installer.class,"ACSD_MetricsCancel"));
            
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    showDialog();
                }
            });
        }
    }
    
    private void showDialog () {
        final JPanel panel = new ReminderPanel();
        DialogDescriptor descriptor = new DialogDescriptor(
            panel,
            NbBundle.getMessage(Installer.class, "Metrics_title"),
            true,
                new Object[] {metricsEnable, metricsCancel},
                null,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            final Dialog d = dlg;
            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    BufferedImage offImg;
                    offImg = (BufferedImage) panel.createImage(1000,1000);
                    Graphics g = offImg.createGraphics();
                    panel.paint(g);
                    int height = d.getPreferredSize().height;
                    Dimension size = d.getSize();
                    size.height = height;
                    d.setSize(size);
                }

            });
            dlg.setResizable(false);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        //NbConnection.updateStatus(cmd,NbInstaller.PRODUCT_ID);
    }

    @Override
    public void run() {
        if (RP.isRequestProcessorThread()) {
            displaySummary("INIT_URL", false, false, false); // NOI18N
        } else {
            RP.post(this);
        }
    }

    @Override
    public void uninstalled() {
        doClose();
    }

    @Override
    public final void close() {
        UIHandler.flushImmediatelly();
        closeLogStream();
        MetricsHandler.flushImmediatelly();
        closeLogStreamMetrics();
    }

    public final void doClose() {
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        log.removeHandler(ui);
        Logger all = Logger.getLogger(""); // NOI18N
        all.removeHandler(handler);
        log = Logger.getLogger(METRICS_LOGGER_NAME);
        log.removeHandler(metrics);

        closeLogStream();
        closeLogStreamMetrics();
    }
    
    static boolean isImmediateWriteOut(LogRecord r) {
        List<LogRecord> preferredLog = logRecords.get();
        if (preferredLog != null) {
            preferredLog.add(r);
            return true;
        } else {
            return false;
        }
    }

    static void writeOut(LogRecord r) {
        try {
            boolean logOverflow;
            boolean logSizeControl;
            List<LogRecord> logs = null;
            synchronized (UIGESTURE_LOG_LOCK) {
                LogRecords.write(logStream(), r);
                logsSize++;
                logOverflow = logsSize > UIHandler.MAX_LOGS;
                if (preferencesWritable) {
                    if (logOverflow) {
                        prefs.putInt("count", UIHandler.MAX_LOGS);
                    } else if (prefs.getInt("count", 0) < logsSize) {
                        prefs.putInt("count", logsSize);
                    }
                }
                if (logOverflow) {
                    closeLogStream();
                    if (isHintsMode()) {
                        logs = new ArrayList<LogRecord>(getLogs());
                    }
                }
                logSizeControl = (logsSize % 100) == 0 && !logOverflow;
            }
            if (logOverflow) {
                if (isHintsMode()) {
                    final List<LogRecord> recs = logs;
                    class Auto implements Runnable {
                        @Override
                        public void run() {
                            displaySummary("WELCOME_URL", true, true,true, DataType.DATA_UIGESTURE, recs, null);
                        }
                    }
                    RP.post(new Auto()).waitFinished();
                }
                synchronized (UIGESTURE_LOG_LOCK) {
                    File f = logFile(0);
                    File f1 = logFile(1);
                    if (f1.exists()) {
                        f1.delete();
                    }
                    f.renameTo(f1);
                    logsSize = 0;
                }
            }
            if (logSizeControl) {
                synchronized (UIGESTURE_LOG_LOCK) {
                    //This is fallback to avoid growing log file over any limit.
                    File f = logFile(0);
                    File f1 = logFile(1);
                    if (f.exists() && (f.length() > UIHandler.MAX_LOGS_SIZE)) {
                        LOG.log(Level.INFO, "UIGesture Collector log file size is over limit. It will be deleted."); // NOI18N
                        LOG.log(Level.INFO, "Log file:{0} Size:{1} Bytes", new Object[]{f, f.length()}); // NOI18N
                        closeLogStream();
                        logsSize = 0;
                        if (preferencesWritable) {
                            prefs.putInt("count", logsSize);
                        }
                        f.delete();
                    }
                    if (f1.exists() && (f1.length() > UIHandler.MAX_LOGS_SIZE)) {
                        LOG.log(Level.INFO, "UIGesture Collector backup log file size is over limit. It will be deleted."); // NOI18N
                        LOG.log(Level.INFO, "Log file:{0} Size:{1} Bytes", new Object[]{f1, f1.length()}); // NOI18N
                        f1.delete();
                    }
                }
            }
        } catch (IOException ex) {
            // bug #183331 don't log throwable here since it causes recursive writeOut invocation
            LOG.log(Level.INFO, "UIGesture Collector logging has failed: {0}", ex.getMessage()); // NOI18N
        }
    }

    private static LogRecord getUserData (Logger logger) {
        LogRecord userData;
        ArrayList<String> params = new ArrayList<String>();
        params.add(Submit.getOS());
        params.add(Submit.getVM());
        params.add(Submit.getVersion());
        List<String> buildInfo = BuildInfo.logBuildInfo();
        if (buildInfo != null) {
            params.addAll(buildInfo);
        }
        userData = new LogRecord(Level.INFO, "USG_SYSTEM_CONFIG");
        userData.setParameters(params.toArray());
        userData.setLoggerName(logger.getName());
        return userData;
    }
    
    private static boolean uploadMetricsTest() {
        if (logsSizeMetrics >= MetricsHandler.MAX_LOGS) {
            return true;
        }
        int daysSinceFirstMetric = (int) ((System.currentTimeMillis() - logsFirstDateMetric)/(1000*60*60*24));
        if (daysSinceFirstMetric > MetricsHandler.MAX_DAYS) {
            return true;
        }
        return false;
    }

    static void writeOutMetrics (LogRecord r) {
        try {
            boolean upload;
            synchronized (METRICS_LOG_LOCK) {
                LogRecords.write(logStreamMetrics(), r);
                logsSizeMetrics++;
                boolean firstDateMetric = logsFirstDateMetric < 0;
                if (firstDateMetric) {
                    logsFirstDateMetric = System.currentTimeMillis();
                }
                if (preferencesWritable) {
                    prefs.putInt("countMetrics", logsSizeMetrics);
                    if (firstDateMetric) {
                        prefs.putLong("firstDateMetric", logsFirstDateMetric);
                    }
                }
                upload = uploadMetricsTest();
                if (upload) {
                    MetricsHandler.waitFlushed();
                    closeLogStreamMetrics();
                    File f = logFileMetrics(0);
                    File f1 = logFileMetrics(1);
                    if (f1.exists()) {
                        if (logMetricsUploadFailed) {
                            //If last metrics upload failed first check size of backup file
                            if (f1.length() > MetricsHandler.MAX_LOGS_SIZE) {
                                //Size is over limit delete file
                                f1.delete();
                                if (!f.renameTo(f1)) {
                                    LOG.log(Level.INFO, "Failed to rename file:{0} to:{1}", new Object[]{f, f1}); // NOI18N
                                }
                            } else {
                                //Size is below limit, append data
                                appendFile(f, f1);
                            }
                        } else {
                            f1.delete();
                            if (!f.renameTo(f1)) {
                                LOG.log(Level.INFO, "Failed to rename file:{0} to:{1}", new Object[]{f, f1}); // NOI18N
                            }
                        }
                    } else {
                        if (!f.renameTo(f1)) {
                            LOG.log(Level.INFO, "Failed to rename file:{0} to:{1}", new Object[]{f, f1}); // NOI18N
                        }
                    }
                    logsSizeMetrics = 0;
                    logsFirstDateMetric = System.currentTimeMillis();
                    if (preferencesWritable) {
                        prefs.putInt("countMetrics", logsSizeMetrics);
                        prefs.putLong("dateFirstMetric", logsFirstDateMetric);
                    }
                }
            }
            if (upload) {
                //Task to upload metrics data
                final List<LogRecord> recs = null;
                class Auto implements Runnable {
                    @Override
                    public void run() {
                        displaySummary("METRICS_URL", true, true, true, DataType.DATA_METRICS, recs, null);
                    }
                }
                //Must be performed out of lock because it calls getLogsMetrics
                RP.post(new Auto()).waitFinished();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** Append content of source to target */
    private static void appendFile (File source, File target) {
        byte[] buf = new byte[8192];
        FileInputStream is = null;
        FileOutputStream os = null;
        long targetSize = -1;
        try {
            is = new FileInputStream(source);
            targetSize = target.length();
            os = new FileOutputStream(target, true);

            int l;
            while ((l = is.read(buf)) != -1) {
                os.write(buf, 0, l);
            }
            os.flush();
        } catch (IOException ex) {
            if (os != null) {
                // Write failed, to assure consistency of data, truncate the file back to the original size:
                DataConsistentFileOutputStream.truncateFileToConsistentSize(os, targetSize);
            }
            Exceptions.printStackTrace(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {}
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {}
            }
        }
    }

    static void getModuleList (Logger logger, List<LogRecord> enabledRec, List<LogRecord> disabledRec) {
        List<ModuleInfo> enabled = new ArrayList<ModuleInfo>();
        List<ModuleInfo> disabled = new ArrayList<ModuleInfo>();
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.isEnabled()) {
                enabled.add(m);
            } else {
                disabled.add(m);
            }
        }
        if (!enabled.isEmpty()) {
            LogRecord rec = new LogRecord(Level.INFO, "USG_ENABLED_MODULES");
            String[] enabledNames = new String[enabled.size()];
            int i = 0;
            for (ModuleInfo m : enabled) {
                SpecificationVersion specVersion = m.getSpecificationVersion();
                if (specVersion != null){
                    enabledNames[i++]  = m.getCodeName() + " [" + specVersion.toString() + "]";
                }else{
                    enabledNames[i++] = m.getCodeName();
                }
            }
            rec.setParameters(enabledNames);
            rec.setLoggerName(logger.getName());
            enabledRec.add(rec);
        }
        if (!disabled.isEmpty()) {
            LogRecord rec = new LogRecord(Level.INFO, "USG_DISABLED_MODULES");
            String[] disabledNames = new String[disabled.size()];
            int i = 0;
            for (ModuleInfo m : disabled) {
                SpecificationVersion specVersion = m.getSpecificationVersion();
                if (specVersion != null){
                    disabledNames[i++]   = m.getCodeName() + " [" + specVersion.toString() + "]";
                }else{
                    disabledNames[i++] = m.getCodeName();
                }
            }
            rec.setParameters(disabledNames);
            rec.setLoggerName(logger.getName());
            disabledRec.add(rec);
        }
    }

    static LogRecord getClusterList (Logger logger) {
        LogRecord rec = new LogRecord(Level.INFO, "USG_INSTALLED_CLUSTERS");
        String dirs = System.getProperty("netbeans.dirs");
        String [] dirsArray = dirs.split(File.pathSeparator);
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < dirsArray.length; i++) {
            File f = new File(dirsArray[i]);
            if (f.exists()){
                list.add(f.getName());
            }
        }
        rec.setParameters(list.toArray());
        rec.setLoggerName(logger.getName());
        return rec;
    }
    
    public static URL hintsURL() {
        return hintURL;
    }
    public static boolean isHintsMode() {
        return prefs.getBoolean("autoSubmitWhenFull", false);
    }

    static int timesSubmitted() {
        return prefs.getInt("submitted", 0);
    }

    public static int getLogsSize() {
        UIHandler.waitFlushed();
        synchronized (UIGESTURE_LOG_LOCK) {
            return prefs.getInt("count", 0); // NOI18N
        }
    }

    static void readLogs(Handler handler){
        UIHandler.waitFlushed();
        synchronized (UIGESTURE_LOG_LOCK) {

            File f = logFile(0);
            if (f == null || !f.exists()) {
                return ;
            }
            closeLogStream();

            File f1 = logFile(1);
            if (logsSize < UIHandler.MAX_LOGS && f1 != null && f1.exists()) {
                scan(f1, handler);
            }
            scan(f, handler);
        }
    }
    
    private static String reportFileContent(File f) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            StringWriter sw = new StringWriter();
            String line;
            while ((line = br.readLine()) != null) {
                sw.write(line);
                sw.write('\n');
            }
            sw.close();
            return sw.toString();
        } catch (IOException ioex) {
            return ioex.toString();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {}
            }
        }
    }

    private static boolean fileContentReported;
    
    private static void scan(File f, Handler handler){
        try {
            LogRecords.scan(f, handler);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Broken uilogs file, not all UI actions will be submitted", ex);
            if (!fileContentReported) {
                try {
                    LOG.log(Level.INFO, "Problematic file content = "+reportFileContent(f));
                } finally {
                    fileContentReported = true;
                }
            }
        }
    }
    
    static List<LogRecord> getLogs() {
        class H extends Handler {
            List<LogRecord> logs = new LinkedList<LogRecord>();

            @Override
            public void publish(LogRecord r) {
                logs.add(r);
                if (logs.size() > UIHandler.MAX_LOGS) {
                    logs.remove(0);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
        H hndlr = new H();
        readLogs(hndlr);
        return hndlr.logs;
    }

    public static List<LogRecord> getLogsMetrics() {
        synchronized (METRICS_LOG_LOCK) {
        
            class H extends Handler {
                List<LogRecord> logs = new LinkedList<LogRecord>();

                @Override
                public void publish(LogRecord r) {
                    logs.add(r);
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }
            }
            H hndlr = new H();

            File f1 = logFileMetrics(1);
            if ((f1 != null) && f1.exists()) {
                try {
                    LogRecords.scan(f1, hndlr);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(
                        Exceptions.attachMessage(ex, "Broken metrics log file, not all metrics data will be submitted")
                    );
                    if (!fileContentReported) {
                        try {
                            LOG.log(Level.INFO, "Problematic file content = "+reportFileContent(f1));
                        } finally {
                            fileContentReported = true;
                        }
                    }
                }
            }
            
            return hndlr.logs;
        }
    }

    static File getDeadlockDumpFile(){
        return new File(logsDirectory(), "deadlock.dump");
    }

    static File logsDirectory(){
        String ud = System.getProperty("netbeans.user"); // NOI18N
        if (ud == null || "memory".equals(ud)) { // NOI18N
            return null;
        }

        File userDir = new File(ud); // NOI18N
        return new File(new File(userDir, "var"), "log");
    }

    private static File logFile(int revision) {
        File logDir = logsDirectory();
        if (logDir == null){
            return null;
        }
        String suffix = revision == 0 ? "" : "." + revision;

        File logFile = new File(logDir, "uigestures" + suffix);
        return logFile;
    }

    private static File logFileMetrics (int revision) {
        String ud = System.getProperty("netbeans.user"); // NOI18N
        if (ud == null || "memory".equals(ud)) { // NOI18N
            return null;
        }

        String suffix = revision == 0 ? "" : "." + revision;

        File userDir = new File(ud); // NOI18N
        File logFile = new File(new File(new File(userDir, "var"), "log"), "metrics" + suffix);
        return logFile;
    }

    private static OutputStream logStream() throws FileNotFoundException {
        synchronized (Installer.class) {
            if (logStream != null) {
                return logStream;
            }
        }

        OutputStream os;
        File logFile = logFile(0);
        if (logFile != null) {
            logFile.getParentFile().mkdirs();
            os = new DataConsistentFileOutputStream(logFile, true);
        } else {
            os = new NullOutputStream();
        }

        synchronized (Installer.class) {
            if (logStream != null) {
                try {
                    os.close();
                } catch (IOException ex) {}
                return logStream;
            }
            logStream = os;
            return logStream;
        }
    }

    private static OutputStream logStreamMetrics () throws FileNotFoundException {
        synchronized (Installer.class) {
            if (logStreamMetrics != null) {
                return logStreamMetrics;
            }
        }

        OutputStream os;
        File logFile = logFileMetrics(0);
        if (logFile != null) {
            logFile.getParentFile().mkdirs();
            os = new DataConsistentFileOutputStream(logFile, true);
        } else {
            os = new NullOutputStream();
        }

        synchronized (Installer.class) {
            if (logStreamMetrics != null) {
                try {
                    os.close();
                } catch (IOException ex) {}
                return logStreamMetrics;
            }
            logStreamMetrics = os;
            return logStreamMetrics;
        }
    }

    private static void closeLogStream() {
        OutputStream os;
        synchronized (Installer.class) {
            os = logStream;
            logStream = null;
        }
        if (os == null) {
            return;
        }

        try {
            os.close();
        } catch (IOException ex) {}
    }

    private static void closeLogStreamMetrics() {
        OutputStream os;
        synchronized (Installer.class) {
            os = logStreamMetrics;
            logStreamMetrics = null;
        }
        if (os == null) {
            return;
        }

        try {
            os.close();
        } catch (IOException ex) {}
    }

    static void clearLogs() {
        synchronized (UIGESTURE_LOG_LOCK) {
            closeLogStream();

            for (int i = 0; ; i++) {
                File f = logFile(i);
                if (f == null || !f.exists()) {
                    break;
                }
                f.delete();
            }

            logsSize = 0;
            if (preferencesWritable) {
                prefs.putInt("count", 0);
            }
        }
        UIHandler.SUPPORT.firePropertyChange(null, null, null);
    }

    @Override
    public boolean closing() {
        UIHandler.waitFlushed();
        return true;
    }

    static void logDeactivated(){
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        for (Deactivated a : Lookup.getDefault().lookupAll(Deactivated.class)) {
            a.deactivated(log);
        }
    }
    
    private static AtomicReference<String> DISPLAYING = new AtomicReference<String>();

    private static boolean displaySummary(String msg, boolean explicit, boolean auto, boolean connectDialog, DataType dataType, List<LogRecord> recs, SlownessData slownData) {
        if (!DISPLAYING.compareAndSet(null, msg)) {
            return true;
        }

        boolean v = true;
        try {
            if (!explicit) {
                boolean dontAsk = prefs.getBoolean("ask.never.again." + msg, false); // NOI18N
                if (dontAsk) {
                    LOG.log(Level.INFO, "UI Gesture Collector's ask.never.again.{0} is true, exiting", msg); // NOI18N
                    return true;
                }
            }

            Submit submit = auto ? new SubmitAutomatic(msg, Button.SUBMIT, dataType, recs) : new SubmitInteractive(msg, connectDialog, dataType, recs, slownData);
            submit.doShow(dataType);
            v = submit.okToExit;
        } finally {
            DISPLAYING.set(null);
        }
        return v;
    }

    public static boolean displaySummary(String msg, boolean explicit, boolean auto, boolean connectDialog) {
        return displaySummary(msg, explicit, auto, connectDialog, DataType.DATA_UIGESTURE, null, null);
    }

    static boolean displaySummary(String msg, boolean explicit, boolean auto, boolean connectDialog, SlownessData slownessData) {
        return displaySummary(msg, explicit, auto, connectDialog, DataType.DATA_UIGESTURE, null, slownessData);
    }

    /** used only in tests - low performance - should use read logs*/
    static Throwable getThrown() {
        return getThrown(getLogs());
    }

    private static Throwable getThrown(List<LogRecord> recs) {
        LogRecord log = getThrownLog(recs);
        if (log == null){
            return null;
        }else{
            return log.getThrown();
        }
    }

    private static LogRecord getThrownLog(List<LogRecord> list) {
        String firstLine = null;
        String message = null;
        if (selectedExcParams != null){
            if (selectedExcParams[0] instanceof String){
                message = (String)selectedExcParams[0];
            }
            if (selectedExcParams[1] instanceof String){
                firstLine = (String)selectedExcParams[1];
            }
        }
        ListIterator<LogRecord> it = list.listIterator(list.size());
        Throwable thr = null;
        LogRecord result;
        while (it.hasPrevious()){
            result = it.previous();
            if (result.getLevel().intValue() >= Level.WARNING.intValue()){
                thr = result.getThrown();// ignore info messages
                if ((thr != null) && (message != null)) {
                    if (!thr.getMessage().equals(message)){
                        thr = null;//different messages
                    }
                }
                if ((thr != null) && (firstLine != null)) {
                    StackTraceElement[] elems = thr.getStackTrace();
                    if (!(elems == null) && !(elems.length == 0)){
                        StackTraceElement elem = elems[0];
                        String thrLine = elem.getClassName() + "." + elem.getMethodName();
                        if (! thrLine.equals(firstLine)){
                            thr = null;//different first lines
                        }
                    }
                }
            }
            // find first exception from end
            if (thr != null) {
                return result;
            }
        }
        return null;// no throwable found
    }

    protected static void setSelectedExcParams(Object[] params){
        selectedExcParams = params;
    }

    /** Tries to parse a list of buttons provided by given page.
     * @param is the input stream to read the page from
     * @param defaultButton the button to add always to the list
     */
    static void parseButtons(InputStream is, final Object defaultButton, final DialogDescriptor dd)
            throws IOException, ParserConfigurationException, SAXException, InterruptedException, InvocationTargetException {
        final ButtonsParser bp = new ButtonsParser(is);
        bp.parse();
        Runnable buttonsCreation = new Runnable() {
            @Override
            public void run() {
                bp.createButtons();
                List<Object> options = bp.getOptions();
                if (!bp.containsExitButton() && (defaultButton != null)){
                    options.add(defaultButton);
                }
                dd.setOptions(options.toArray());
                dd.setAdditionalOptions(bp.getAditionalOptions().toArray());
                if (bp.getTitle() != null){
                    dd.setTitle(bp.getTitle());
                }
            }
        };
        
        if (EventQueue.isDispatchThread()){
            buttonsCreation.run();
        }else{
            EventQueue.invokeAndWait(buttonsCreation);
        }
    }
    
    static String decodeButtons(Object res, URL[] url) {
        return decodeButtons(res, url, DataType.DATA_UIGESTURE);
    }

    private static String decodeButtons(Object res, URL[] url, DataType dataType) {
        if (res instanceof JButton) {
            JButton b = (JButton)res;
            Object post = b.getClientProperty("url"); // NOI18N
            if (post instanceof String) {
                String replace = null;
                if (dataType == DataType.DATA_UIGESTURE) {
                    replace = System.getProperty("org.netbeans.modules.uihandler.Submit"); // NOI18N
                } else if (dataType == DataType.DATA_METRICS) {
                    replace = System.getProperty("org.netbeans.modules.uihandler.Metrics"); // NOI18N
                }
                if (replace != null) {
                    post = replace;
                }
                try {
                    url[0] = new URL((String) post);
                } catch (MalformedURLException ex) {
                    LOG.log(Level.INFO, "Cannot decode URL: " + post, ex); // NOI18N
                    url[0] = null;
                    return null;
                }
            }
            return b.getActionCommand();
        }
        return res instanceof String ? (String)res : null;
    }

    static URL uploadLogs(URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs, DataType dataType, boolean isErrorReport, SlownessData slownData, boolean isOOM) throws IOException {
        ProgressHandle h = null;
        //Do not show progress UI for metrics upload
        if (dataType != DataType.DATA_METRICS) {
            h = ProgressHandleFactory.createHandle(NbBundle.getMessage(Installer.class, "MSG_UploadProgressHandle"));
        }
        try {
            return uLogs(h, postURL, id, attrs, recs, dataType, isErrorReport, slownData, isOOM);
        } finally {
            if (h != null) {
                h.finish();
            }
        }
    }
    
    static URL uploadLogs(URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs, boolean isErrorReport) throws IOException {
        return uploadLogs(postURL, id, attrs, recs, DataType.DATA_UIGESTURE, isErrorReport, null, false);
    }

    private static URL uLogs
    (ProgressHandle h, URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs,
            DataType dataType, boolean isErrorReport, SlownessData slownData, boolean isOOM) throws IOException {
        if (dataType != DataType.DATA_METRICS) {
            int workUnits = isOOM ? 1100 : 100;
            h.start(workUnits + recs.size());
            h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadConnecting")); // NOI18N
        }
        
        boolean fileProtocol = "file".equals(postURL.getProtocol()); // For tests or other non-http usages
        
        LOG.log(Level.FINE, "uploadLogs, postURL = {0}", postURL); // NOI18N
        URLConnection conn;
        if (fileProtocol) {
            conn = null;
        } else {
            conn = postURL.openConnection();
        }

        if (dataType != DataType.DATA_METRICS) {
            h.progress(10);
        }
        
        if (!fileProtocol) {
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=--------konec<>bloku");
            conn.setRequestProperty("Pragma", "no-cache");
            conn.setRequestProperty("Cache-control", "no-cache");
            conn.setRequestProperty("User-Agent", "NetBeans");
        }
        
        if (dataType != DataType.DATA_METRICS) {
            h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadSending"), 20);
        }
        LOG.log(Level.FINE, "uploadLogs, header sent"); // NOI18N

        PrintStream os;
        if (fileProtocol) {
            os = new PrintStream(new FileOutputStream(postURL.getFile()));
        } else {
            os = new PrintStream(conn.getOutputStream());
        }
        /*
        os.println("POST " + postURL.getPath() + " HTTP/1.1");
        os.println("Pragma: no-cache");
        os.println("Cache-control: no-cache");
        os.println("Content-Type: multipart/form-data; boundary=--------konec<>bloku");
        os.println();
         */
        for (Map.Entry<String, String> en : attrs.entrySet()) {
            os.println("----------konec<>bloku");
            os.println("Content-Disposition: form-data; name=\"" + en.getKey() + "\"");
            os.println();
            os.println(en.getValue().getBytes());
        }
        LOG.log(Level.FINE, "uploadLogs, attributes sent"); // NOI18N
        
        if (dataType != DataType.DATA_METRICS) {
            h.progress(30);
        }

        os.println("----------konec<>bloku");

        if (id == null) {
            id = "uigestures"; // NOI18N
        }

        if (dataType != DataType.DATA_METRICS && isErrorReport) {
            os.println("Content-Disposition: form-data; name=\"messages\"; filename=\"" + id + "_messages.gz\"");
            os.println("Content-Type: x-application/log");
            os.println();
            uploadMessagesLog(os);
            os.println();
            os.println("\n----------konec<>bloku");
        }

        if (slownData != null){
            assert slownData.getNpsContent() != null: "nps param should be not null";
            assert slownData.getNpsContent().length > 0 : "nps param should not be empty";
            os.println("Content-Disposition: form-data; name=\"slowness\"; filename=\"" + id + "_slowness.gz\"");
            os.println("Content-Type: x-application/nps");
            os.println();
            os.write(slownData.getNpsContent());
            os.println();
            os.println("\n----------konec<>bloku");
        }

        if (dataType != DataType.DATA_METRICS) {
            h.progress(70);
        }

        if (isOOM){
            File f = getHeapDump();
            assert (f != null);
            assert (f.exists() && f.canRead());
            assert f.length() != 0 : "Heapdump has zero size!";
            long progressUnit = f.length() / 1000;
            if (progressUnit == 0) progressUnit = 1; //prevent #196630
            long alreadyWritten = 0;
            os.println("Content-Disposition: form-data; name=\"heapdump\"; filename=\"" + id + "_heapdump.gz\"");
            os.println("Content-Type: x-application/heap");
            os.println();
            GZIPOutputStream gzip = new GZIPOutputStream(os);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            byte[] heapDumpData = new byte[8192];
            int read;
            int workunit;
            while ((read = bis.read(heapDumpData)) != -1){
                gzip.write(heapDumpData, 0, read);
                alreadyWritten += read;
                workunit = (int)(alreadyWritten / progressUnit);
                if(workunit < 1000) {
                    h.progress(70 + workunit);
                }
            }
            bis.close();
            gzip.finish();
            os.println();
            os.println("\n----------konec<>bloku");

            h.progress(1070);
            }

        os.println("Content-Disposition: form-data; name=\"logs\"; filename=\"" + id + "\"");
        os.println("Content-Type: x-application/gzip");
        os.println();
        GZIPOutputStream gzip = new GZIPOutputStream(os);
        DataOutputStream data = new DataOutputStream(gzip);
        data.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("utf-8")); // NOI18N
        data.write("<uigestures version='1.0'>\n".getBytes("utf-8")); // NOI18N

        int cnt = isOOM ? 1080 : 80;
        LOG.log(Level.FINE, "uploadLogs, sending records"); // NOI18N
        for (LogRecord r : recs) {
            if (dataType != DataType.DATA_METRICS) {
                h.progress(cnt++);
            }
            if (r != null) {
                LogRecords.write(data, r);
            }
        }
        data.write("</uigestures>\n".getBytes("utf-8")); // NOI18N
        LOG.log(Level.FINE, "uploadLogs, flushing"); // NOI18N
        data.flush();
        gzip.finish();
        os.println();
        os.println("----------konec<>bloku--");
        os.close();

        if (dataType != DataType.DATA_METRICS) {
            h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadReading"), cnt + 10);
        }

        //System.err.println("DONE: Uploaded logs to "+postURL+" in "+Thread.currentThread());
        
        StringBuffer redir = null;
        Matcher m = null;
        if (!fileProtocol) {
            LOG.log(Level.FINE, "uploadLogs, reading reply"); // NOI18N
            InputStream is = conn.getInputStream();
            redir = new StringBuffer();
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                redir.append((char)ch);
            }
            is.close();

            if (dataType != DataType.DATA_METRICS) {
                h.progress(cnt + 20);
            }

            LOG.log(Level.FINE, "uploadLogs, Reply from uploadLogs: {0}", redir);

            Pattern p = Pattern.compile("<meta\\s*http-equiv=.Refresh.\\s*content.*url=['\"]?([^'\" ]*)\\s*['\"]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            m = p.matcher(redir);
        }

        if (isOOM){
            FileObject fo = FileUtil.createData(getHeapDump());
            FileObject folder = fo.getParent();
            String submittedName = fo.getName() + "_submitted"; // NOI18N
            FileObject submittedFO = folder.getFileObject(submittedName, fo.getExt());
            if (submittedFO != null){
                submittedFO.delete();
            }
            FileLock lock = fo.lock();
            fo.rename(lock, submittedName, fo.getExt());
            lock.releaseLock();
        }

        if (!fileProtocol) {
            if (m.find()) {
                LOG.log(Level.FINE, "uploadLogs, found url = {0}", m.group(1)); // NOI18N
                return new URL(m.group(1));
            } else {
                File f = File.createTempFile("uipage", "html");
                f.deleteOnExit();
                FileWriter w = new FileWriter(f);
                w.write(redir.toString());
                w.close();
                LOG.log(Level.FINE, "uploadLogs, temporary url = {0}", Utilities.toURI(f)); // NOI18N
                return Utilities.toURI(f).toURL();
            }
        } else {
            return null;
        }
    }

    private static File getMessagesLog(){
        File directory = logsDirectory();
        if (directory == null){
            return null;
        }
        File messagesLog = new File(directory, "messages.log");
        return messagesLog;
    }

    private static File getHeapDump() {
        String heapDumpPath = null;
        RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
        List<String> lst = RuntimemxBean.getInputArguments();
        for (String arg : lst) {
            if (arg.contains("XX:HeapDumpPath")){
                int index = arg.indexOf('=');
                heapDumpPath = arg.substring(index+1);
            }
        }

        if (heapDumpPath == null){
            LOG.info("XX:HeapDumpPath parametter not specified");
            return null;
        }
        File heapDumpFile = new File(heapDumpPath);
        if (heapDumpFile.exists() && heapDumpFile.canRead() && heapDumpFile.length() > 0) {
            return heapDumpFile;
        }
        LOG.log(Level.INFO, "heap dump was not created at {0}", heapDumpPath);
        LOG.log(Level.INFO, "heapdump file: exists():{0}, canRead():{1}, length:{2}",new Object[] 
                {heapDumpFile.exists(), heapDumpFile.canRead(), heapDumpFile.length()});
        return null;
    }
    
    static void uploadMessagesLog(PrintStream os) throws IOException {
        flushSystemLogs();
        File messagesLog = getMessagesLog();
        if (messagesLog == null){
            return;
        }
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(messagesLog));
        GZIPOutputStream gzip = new GZIPOutputStream(os);
        byte[] buffer = new byte[4096];
        int readLength = is.read(buffer);
        while (readLength != -1){
            gzip.write(buffer, 0, readLength);
            readLength = is.read(buffer);
        }
        is.close();
        gzip.finish();
    }

    private static void flushSystemLogs(){
        System.out.flush();
        System.err.flush();
    }
    
    public static String findIdentity() {
        Preferences p = NbPreferences.root().node("org/netbeans/modules/autoupdate"); // NOI18N
        String id = p.get("qualifiedId", null);
        //Strip id prefix
        if (id != null){
            int ind = id.indexOf("0");
            if (ind != -1) {
                id = id.substring(ind + 1);
            }
        }
        LOG.log(Level.INFO, "findIdentity: {0}", id);
        return id;
    }

    static final class Form extends Object {
        final String url;
        String submitValue;

        public Form(String u) {
            url = u;
        }
    }

    static void copyWithEncoding(InputStream inputStream, OutputStream os) throws IOException {
        byte[] arr = new byte[4096];

        String text = null;
        String enc = "utf-8";
        for (;;) {
            int len = inputStream.read(arr);
            if (len == -1) {
                break;
            }
            boolean first = text == null;
            text = new String(arr, 0, len, enc);
            if (first) {
                Matcher m = ENCODING.matcher(text);
                if (m.find()) {
                    enc = m.group(1);
                    text = new String(arr, 0, len, enc);
                    OUTER: for (;;) {
                        Matcher replace = ENCODING.matcher(text);
                        do {
                            if (!replace.find()) {
                                break OUTER;
                            }
                        } while ("UTF-8".equals(replace.group(1)));
                        text = text.substring(0, replace.start(1)) + "UTF-8" + text.substring(replace.end(1));
                    }
                    LOG.log(Level.FINE, "Downloaded with encoding ''{0}'':\n{1}", new Object[]{enc, text});
                } else {
                    LOG.log(Level.FINE, "Downloaded with utf-8:\n{0}", text);
                }
            }
            os.write(text.getBytes("UTF-8"));
        }
    }

    private static abstract class Submit implements ActionListener, Runnable {

        private enum DialogState {

            NON_CREATED, CREATED, FAILED
        };
        private AtomicBoolean isSubmiting;// #114505 , report is sent two times
        protected String exitMsg;
        private DialogDescriptor dd;
        private final Object ddLock = new Object();
        protected String msg;
        protected final boolean report;//property tells me wheather I'm in report mode
        protected boolean okToExit;
        protected ReportPanel reportPanel;
        private URL url;
        private DialogState dialogState = DialogState.NON_CREATED;
        private boolean checkingResult, refresh = false;
        protected boolean errorPage = false;
        protected DataType dataType = DataType.DATA_UIGESTURE;
        final protected List<LogRecord> recs;
        protected boolean isOOM = false;
        protected ExceptionsSettings settings;
        protected JProgressBar jpb = new JProgressBar();
        
        public Submit(String msg) {
            this(msg,DataType.DATA_UIGESTURE, null);
        }

        public Submit(String msg, DataType dataType, List<LogRecord> recs) {
            this.msg = msg;
            this.dataType = dataType;
            isSubmiting = new AtomicBoolean(false);
            if (recs != null) {
                this.recs = recs;
            } else {
                if (dataType == DataType.DATA_METRICS) {
                    this.recs = getLogsMetrics();
                } else {
                    this.recs = new ArrayList<LogRecord>(getLogs());
                }
            }
            if ("ERROR_URL".equals(msg)) { // NOI18N
                report = true;
            } else {
                report = false;
            }
        }

        protected abstract void createDialog();
        protected abstract Object showDialogAndGetValue(DialogDescriptor dd);
        protected abstract void closeDialog();
        protected abstract void alterMessage(DialogDescriptor dd);
        protected abstract void viewData();
        protected abstract void assignInternalURL(URL u);
        protected abstract void addMoreLogs(List<? super String> params, boolean openPasswd);
        protected abstract void showURL(URL externalURL, boolean inIDE);
        protected abstract SlownessData getSlownessData();
        
        protected final DialogDescriptor findDD() {
            synchronized (ddLock) {
                if (dd == null) {
                    if (report) {
                        dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "ErrorDialogTitle"));
                    } else {
                        dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "MSG_SubmitDialogTitle"));
                    }
                }
                return dd;
            }
        }

        public void doShow(DataType dataType) {
            //System.err.println("doShow("+dataType+")");
            //Thread.dumpStack();
            if (dataType == DataType.DATA_UIGESTURE) {
                try {
                    logRecords.set(recs);
                    logDeactivated();
                } finally {
                    logRecords.remove();
                }
            }
            findDD();
            
            exitMsg = NbBundle.getMessage(Installer.class, "MSG_" + msg + "_EXIT"); // NOI18N

            String defaultURI = NbBundle.getMessage(Installer.class, msg);
            String replace = System.getProperty("org.netbeans.modules.uihandler.LoadURI"); // NOI18N
            if (replace != null) {
                defaultURI = replace;
            }
            LOG.log(Level.FINE, "doShow, exitMsg = {0}, defaultURI = {1}", new Object[] { exitMsg, defaultURI }); // NOI18N
            if (defaultURI == null || defaultURI.length() == 0) {
                okToExit = true;
                return;
            }

            synchronized (this) {
                RP_UI.post(this);
                while (dialogState.equals(DialogState.NON_CREATED)) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                notifyAll();
            }
            if (dialogState.equals(DialogState.FAILED)){
                return;
            }

            LOG.log(Level.FINE, "doShow, dialog has been created"); // NOI18N
            boolean firstRound = true;
            StringBuilder sb = new StringBuilder(1024);
            for (;;) {
                try {
                    if (url == null) {
                        url = new URL(defaultURI); // NOI18N
                    }

                    LOG.log(Level.FINE, "doShow, reading from = {0}", url);
                    sb.append("doShow reading from: ").append(url).append("\n");
                    URLConnection conn = url.openConnection();
                    conn.setRequestProperty("User-Agent", "NetBeans");
                    conn.setConnectTimeout(5000);
                    File tmp = File.createTempFile("uigesture", ".html");
                    tmp.deleteOnExit();
                    FileOutputStream os = new FileOutputStream(tmp);
                    copyWithEncoding(conn.getInputStream(), os);
                    os.close();
                    conn.getInputStream().close();
                    LOG.log(Level.FINE, "doShow, all read from = {0}", url); // NOI18N
                    //Temporary logging to investigate #141497
                    InputStream is = new FileInputStream(tmp);
                    byte [] arr = new byte [is.available()];
                    is.read(arr);
                    sb.append("Content:\n").append(new String(arr)).append("\nEnd of Content");
                    is.close();
                    //End
                    is = new FileInputStream(tmp);
                    DialogDescriptor dd = findDD();
                    parseButtons(is, exitMsg, dd);
                    LOG.log(Level.FINE, "doShow, parsing buttons: {0}", Arrays.toString(dd.getOptions())); // NOI18N
                    alterMessage(dd);
                    is.close();
                    url = Utilities.toURI(tmp).toURL();
                } catch (InterruptedException ex) {
                    LOG.log(Level.WARNING, null, ex);
                } catch (InvocationTargetException ex) {
                    LOG.log(Level.WARNING, null, ex);
                } catch (ParserConfigurationException ex) {
                    LOG.log(Level.WARNING, null, ex);
                } catch (SAXException ex) {
                    LOG.log(Level.INFO, sb.toString());
                    LOG.log(Level.WARNING, url.toExternalForm(), ex);
                } catch (IllegalStateException ex){
                    catchConnectionProblem(ex);
                    continue;
                } catch (java.net.SocketTimeoutException ex) {
                    catchConnectionProblem(ex);
                    continue;
                } catch (UnknownHostException ex) {
                    catchConnectionProblem(ex);
                    continue;
                } catch (NoRouteToHostException ex) {
                    catchConnectionProblem(ex);
                    continue;
                } catch (ConnectException ex) {
                    catchConnectionProblem(ex);
                    continue;
                } catch (IOException ex) {
                    if (firstRound) {
                        catchConnectionProblem(ex);
                        firstRound = false;
                        continue;
                    } else {// preventing from deadlock while reading error page
                        LOG.log(Level.WARNING, url.toExternalForm(), ex);
                    }
                }
                firstRound = true;
                LOG.log(Level.FINE, "doShow, assignInternalURL = {0}", url);
                assignInternalURL(url);
                refresh = false;
                synchronized (this) {
                    while (dialogState.equals(DialogState.CREATED) && !refresh) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                if (refresh){
                    url=null;
                    continue;
                }
                break;
            }
            LOG.log(Level.FINE, "doShow, dialogCreated, exiting");
        }

        protected synchronized final void doCloseDialog() {
            dialogState = DialogState.NON_CREATED;
            closeDialog();
            notifyAll();
            LOG.log(Level.FINE, "doCloseDialog");
        }

        private void catchConnectionProblem(Exception exception){
            LOG.log(Level.INFO, url.toExternalForm(), exception);
            url = getUnknownHostExceptionURL();
            jpb.setVisible(false);
            errorPage = true;
        }

        private URL getUnknownHostExceptionURL() {
            try {
                URL resource = new URL("nbresloc:/org/netbeans/modules/uihandler/UnknownHostException.html"); // NOI18N
                return resource;
            } catch (MalformedURLException ex) {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
            return getClass().getResource("UnknownHostException.html"); // NOI18N
        }

        @Override
        public void run() {
            DialogState newState = DialogState.CREATED;
            try{
                createDialog();
            }catch (RuntimeException e){
                newState = DialogState.FAILED;
                throw e;
            } finally {
                synchronized (this) {
                    dialogState = newState;
                    // dialog created let the code go on
                    notifyAll();


                    try {
                        // wait till the other code runs
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            LOG.log(Level.FINE, "run, showDialogAndGetValue");
            Object res = showDialogAndGetValue(findDD());
            LOG.log(Level.FINE, "run, showDialogAndGetValue, res = {0}", res);

            if (res == exitMsg) {
                okToExit = true;
            }
            LOG.log(Level.FINE, "run, okToExit = {0}", okToExit);
            doCloseDialog();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final URL[] universalResourceLocator = new URL[1];
            String actionURL = decodeButtons(e.getSource(), universalResourceLocator, dataType);

            LOG.log(Level.FINE, "actionPerformed: command = {0}", e.getActionCommand()); // NOI18N
            //System.err.println("actionPerformed: command = "+e.getActionCommand()); // NOI18N
            //Thread.dumpStack();

            boolean submit = Button.SUBMIT.isCommand(actionURL);
            if (Button.AUTO_SUBMIT.isCommand(e.getActionCommand())) {
                submit = true;
                if(preferencesWritable) {
                    prefs.putBoolean("autoSubmitWhenFull", true); // NOI18N
                }
            }

            if (submit) { // NOI18N
                JButton button = null;
                if (e.getSource() instanceof JButton){
                    button = (JButton) e.getSource();
                    button.setEnabled(false);
                }
                final JButton submitButton = button;
                if (isSubmiting.getAndSet(true)) {
                    LOG.info("ALREADY SUBMITTING"); // NOI18N
                    return;
                }
		if (report){
                    reportPanel.showCheckingPassword();
		}
                RP_SUBMIT.post(new Runnable() {

                    @Override
                    public void run() {
                        if (dataType == DataType.DATA_UIGESTURE) {
                            LogRecord userData = getUserData(true, reportPanel);
                            LogRecord thrownLog = getThrownLog(recs);
                            if (thrownLog != null) {
                                recs.add(thrownLog);//exception selected by user
                            }
                            recs.add(BuildInfo.logBuildInfoRec());
                            SlownessData sd = getSlownessData();
                            if (sd != null){
                                recs.add(sd.getLogRec());
                            } else {
                                recs.add(TimeToFailure.logFailure());
                            }
                            recs.add(userData);
                            if ((report) && (!reportPanel.asAGuest())) {
                                if (!checkUserName(reportPanel)) {
                                    EventQueue.invokeLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            submitButton.setEnabled(true);
                                            reportPanel.showWrongPassword();
                                        }
                                    });
                                    isSubmiting.set(false);
                                    return;
                                }
                            }
                            LOG.fine("posting upload UIGESTURES");// NOI18N
                        } else if (dataType == DataType.DATA_METRICS) {
                            LOG.fine("posting upload METRICS");// NOI18N
                        }
                        final List<LogRecord> recsFinal = recs;
                        RP_SUBMIT.post(new Runnable() {
                            @Override
                            public void run() {
                                uploadAndPost(recsFinal, universalResourceLocator[0], dataType, getSlownessData());
                            }
                        });
                        okToExit = false;
                        // this should close the descriptor
                        EventQueue.invokeLater(new Runnable(){
                            @Override
                            public void run() {
                                doCloseDialog();
                            }
                        });
                    }
                });
                return;
            }

            if (Button.REDIRECT.isCommand(e.getActionCommand())){
                if (universalResourceLocator[0] != null) {
                    showURL(universalResourceLocator[0], false);
                }
                doCloseDialog();
                return ;
            }

            if (Button.PROXY.isCommand(e.getActionCommand())){
                if (RP_OPT == null){
                    RP_OPT = new RequestProcessor("UI Gestures - Show Options");
                }
                //show Tools/Options dialog
                RP_OPT.post(new Runnable() {
                    @Override
                    public void run() {
                        OptionsDisplayer.getDefault().open("General"); // NOI18N
                    }
                });
            }

            if (Button.REFRESH.isCommand(e.getActionCommand())){
                refresh = true;
                errorPage = false;
                synchronized(this){
                    notifyAll();
                }
                return;
            }

            if (Button.VIEW_DATA.isCommand(e.getActionCommand())) { // NOI18N
                viewData();
                return;
            }

            if (Button.NEVER_AGAIN.isCommand(e.getActionCommand())) { // NOI18N
                LOG.log(Level.FINE, "Assigning ask.never.again.{0} to true", msg); // NOI18N
                NbPreferences.forModule(Installer.class).putBoolean("ask.never.again." + msg, true); // NOI18N
                okToExit = true;
                // this should close the descriptor
                doCloseDialog();
                return;
            }

            if (Button.EXIT.isCommand(e.getActionCommand())) {
                // this should close the descriptor
                doCloseDialog();
                return;
            }
        }

        private boolean checkUserName(ReportPanel panel) {
            checkingResult = true;
            try {
                String login = URLEncoder.encode(panel.getUserName(), "UTF-8");
                String encryptedPasswd = URLEncoder.encode(PasswdEncryption.encrypt(new String(panel.getPasswdChars())), "UTF-8");
                char[] array = new char[100];
                URL checkingServerURL = new URL(NbBundle.getMessage(Installer.class, "CHECKING_SERVER_URL", login, encryptedPasswd));
                URLConnection connection = checkingServerURL.openConnection();
                connection.setRequestProperty("User-Agent", "NetBeans");
                connection.setReadTimeout(20000);
                Reader reader = new InputStreamReader(connection.getInputStream());
                int length = reader.read(array);
                checkingResult = Boolean.valueOf(new String(array, 0, length));
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception exception) {
                Logger.getLogger(Installer.class.getName()).log(Level.INFO, "Checking password failed", exception); // NOI18N
            }
            return checkingResult;
        }

        private void uploadAndPost(List<LogRecord> recs, URL u, DataType dataType, SlownessData slownData) {
            URL nextURL = null;

            if(preferencesWritable) {
                prefs.putInt("submitted", 1 + prefs.getInt("submitted", 0)); // NOI18N
            }

            try {
                if (dataType == DataType.DATA_METRICS) {
                    logMetricsUploadFailed = false;
                }
                nextURL = uploadLogs(u, findIdentity(), Collections.<String,String>emptyMap(), recs, dataType, report, slownData, isOOM);
            } catch (IOException ex) {
                LOG.log(Level.INFO, null, ex);
                if (dataType == DataType.DATA_METRICS) {
                    logMetricsUploadFailed = true;
                }
                if (dataType != DataType.DATA_METRICS) {
                    String txt;
                    if (!report) {
                        txt = NbBundle.getMessage(Installer.class, "MSG_ConnetionFailed", u.getHost(), u.toExternalForm());
                    } else {
                        txt = NbBundle.getMessage(Installer.class, "MSG_ConnetionFailedReport", u.getHost(), u.toExternalForm());
                    }
                    NotifyDescriptor nd = new NotifyDescriptor.Message(txt, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            }
            if (dataType == DataType.DATA_METRICS) {
                if (preferencesWritable) {
                    prefs.putBoolean("metrics.upload.failed", logMetricsUploadFailed); // NOI18N
                }
            }
            if (nextURL != null) {
                clearLogs();
                showURL(nextURL, report);
            }
        }

        protected final LogRecord getUserData(boolean openPasswd, ReportPanel panel) {
            LogRecord userData;
            ArrayList<String> params = new ArrayList<String>(6);
            params.add(getOS());
            params.add(getVM());
            params.add(getVersion());
            if (panel != null){
                if (panel.asAGuest()){
                    params.add("");
                }else{
                    params.add(panel.getUserName());
                }
            } else if (settings != null) {
                params.add(settings.getUserName());
            }
            addMoreLogs(params, openPasswd);
            userData = new LogRecord(Level.CONFIG, USER_CONFIGURATION);
            userData.setResourceBundle(NbBundle.getBundle(Installer.class));
            userData.setResourceBundleName(Installer.class.getPackage().getName()+".Bundle");
            userData.setParameters(params.toArray());
            return userData;
        }

        private static String getOS(){
            String unknown = "unknown";                                   // NOI18N
            String str = System.getProperty("os.name", unknown)+", "+     // NOI18N
                    System.getProperty("os.version", unknown)+", "+       // NOI18N
                    System.getProperty("os.arch", unknown);               // NOI18N
            return str;
        }

        private static String getVersion(){
            String str = ""; // NOI18N
            try {
                str = MessageFormat.format(
                        NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), // NOI18N
                        new Object[] {System.getProperty("netbeans.buildnumber")} // NOI18N
                );
            } catch (MissingResourceException ex) {
                LOG.log(Level.FINE, ex.getMessage(), ex);
            }
            return str;
        }

        private static String getVM(){
            return System.getProperty("java.vm.name", "unknown") + ", " // NOI18N
                 + System.getProperty("java.vm.version", "") + ", " // NOI18N
                 + System.getProperty("java.runtime.name", "unknown") + ", " // NOI18N
                 + System.getProperty("java.runtime.version", ""); // NOI18N
        }

    } // end of Submit

    protected static String createMessage(Throwable thr){
        //ignore causes with empty stacktraces -> they are just annotations
        while ((thr.getCause() != null) && (thr.getCause().getStackTrace().length != 0)){
            thr = thr.getCause();
        }
        String message = thr.toString();
        if (message.startsWith("java.lang.")){
            message = message.substring(10);
        }
        int indexClassName = message.indexOf(':');
        if (indexClassName == -1){ // there is no message after className
            if (thr.getStackTrace().length != 0){
                StackTraceElement elem = thr.getStackTrace()[0];
                return message + " at " + elem.getClassName()+"."+elem.getMethodName();
            }
        }
        return message;
    }

    static final class SubmitInteractive extends Submit
    implements HyperlinkListener {
        private boolean connectDialog;
        private Dialog d;
        private SubmitPanel panel;
        private JEditorPane browser;
        private boolean urlAssigned;
        private SlownessData slownData = null;
        
        public SubmitInteractive(String msg, boolean connectDialog) {
            this(msg, connectDialog, DataType.DATA_UIGESTURE);
        }

        public SubmitInteractive(String msg, boolean connectDialog, DataType dataType) {
            this(msg, connectDialog, dataType, null, null);
        }

        private SubmitInteractive(String msg, boolean connectDialog, DataType dataType, List<LogRecord> recs, SlownessData slownData) {
            super(msg, dataType, recs);
            this.connectDialog = connectDialog;
            this.slownData = slownData;
        }

        @Override
        protected void createDialog() {
            String message = null;
            if (slownData != null) {
                String time = Long.toString(slownData.getTime());
                if (slownData.getSlownessType() != null){
                    message = String.format("%1$s took %2$s ms.", slownData.getSlownessType(), time);// NOI18N
                }else if (slownData.getLatestActionName() != null) {
                    message = String.format("Invoking %1$s took %2$s ms.", slownData.getLatestActionName(), time);// NOI18N
                } else {
                    message = String.format("AWT thread blocked for %1$s ms.", time); // NOI18N
                }
            } else {
                Throwable t = getThrown(recs);
                if (t != null) {
                    message = createMessage(t);
                    if (message.contains("OutOfMemoryError") && getHeapDump() != null) {
                        isOOM = true;
                    }
                }
            }
            final String summary = message;
            settings = new ExceptionsSettings();
            try {
                EventQueue.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        LOG.log(Level.FINE, "Window system initialized:", WindowManager.getDefault().getMainWindow().isVisible());
                        if (reportPanel==null) {
                            reportPanel = new ReportPanel(isOOM, settings);
                        }
                        if (summary != null){
                            reportPanel.setSummary(summary);
                        }
                        Dimension dim = new Dimension(350, 50);
                        if ("ERROR_URL".equals(msg)) {
                            dim = new Dimension(370, 250);
                        }
                        browser = new JEditorPane();
                        try {
                            browser.setEditable(false);
                            try {
                                URL resource = new URL("nbresloc:/org/netbeans/modules/uihandler/Connecting.html"); // NOI18N
                                browser.setPage(resource); // NOI18N
                            } catch (IOException ex) {
                                LOG.log(Level.SEVERE, ex.getMessage(), ex);
                            }
                            browser.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 0, 8));
                            browser.setPreferredSize(dim);
                            // browser.setEditorKit(new HTMLEditorKit()); // needed up to nb5.5
                            browser.setBackground(new JLabel().getBackground());
                            browser.addHyperlinkListener(SubmitInteractive.this);
                        } catch (NullPointerException x) {
                            LOG.log(Level.WARNING, "Java bug #7050995?", x);
                        }
                        JScrollPane p = new JScrollPane();
                        p.setViewportView(browser);
                        p.setBorder(BorderFactory.createEmptyBorder());
                        p.setPreferredSize(dim);
                        DialogDescriptor descr = findDD();
                        JPanel jp = new JPanel();
                        BoxLayout l = new BoxLayout(jp, BoxLayout.Y_AXIS);
                        jp.setLayout(l);
                        jpb.setVisible(true);
                        jpb.setIndeterminate(true);
                        jp.add(p);
                        jp.add(jpb);
                        descr.setMessage(jp);
                       //        AbstractNode root = new AbstractNode(new Children.Array());
                        //        root.setName("root"); // NOI18N
                        //        root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
                        //        root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
                        //        for (LogRecord r : recs) {
                        //            root.getChildren().add(new Node[] { UINode.create(r) });
                        //        }
                        //
                        //        panel.getExplorerManager().setRootContext(root);
                        Object[] arr = new Object[]{exitMsg};
                        descr.setOptions(arr);
                        descr.setClosingOptions(arr);
                        descr.setButtonListener(SubmitInteractive.this);
                        descr.setModal(true);
                        d = DialogDisplayer.getDefault().createDialog(descr);
                    }
                });
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            } catch (InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
            assert d != null;
        }

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                showURL(e.getURL(), false);
            }
        }

        @Override
        protected void closeDialog() {
            if (d == null) {
                return;
            }
            reportPanel.saveUserData();
            RP_UI.post(new Runnable() {

                @Override
                public void run() {
                    settings.save();
                }
            });
            findDD().setValue(DialogDescriptor.CLOSED_OPTION);
            d.setVisible(false);
            d.dispose(); // fix the issue #137714
            d = null;
        }

        private void procesLog(LogRecord r, LinkedList<Node> nodes, StringBuilder builder){
            Node n = UINode.create(r);
            nodes.add(n);
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int offset = builder.length();
                n.setValue("offset", offset); // NOI18N
                LogRecords.write(os, r);
                builder.append(os.toString("UTF-8"));
            } catch (IOException ex) {
                Installer.LOG.log(Level.WARNING, null, ex);
            }
        }

        private class DataLoader implements Runnable{
            private final StringBuilder panelContent = new StringBuilder();
            private final AbstractNode root = new AbstractNode(new Children.Array());
            @Override
            public void run() {
                if (EventQueue.isDispatchThread()){
                    panel.setText(panelContent.toString());
                    panel.getExplorerManager().setRootContext(root);
                } else {
                    List<LogRecord> displayedRecords = new ArrayList<LogRecord>(recs);
                    LinkedList<Node> nodes = new LinkedList<Node>();
                    root.setName("root"); // NOI18N
                    root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", displayedRecords.size() + 1, new Date()));
                    root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
                    for (LogRecord r : displayedRecords) {
                        procesLog(r, nodes, panelContent);
                    }
                    procesLog(getUserData(false, reportPanel), nodes, panelContent);
                    root.getChildren().add(nodes.toArray(new Node[0]));
                    EventQueue.invokeLater(this);
                }
            }
        }
        
        @Override
        protected void viewData() {
            if (panel == null) {
                TimeToFailure.logAction();
                panel = new SubmitPanel();
                RequestProcessor.getDefault().post(new DataLoader());
                panel.setText(NbBundle.getMessage(Installer.class, "LOADING_TEXT"));
                panel.getExplorerManager().setRootContext(Node.EMPTY);
            }
            DialogDescriptor viewDD;
            if (!report){
                 viewDD = new DialogDescriptor(panel, NbBundle.getMessage(Installer.class, "VIEW_DATA_TILTE"));
            } else {
                flushSystemLogs();
                JTabbedPane tabs = new JTabbedPane();
                tabs.addTab(org.openide.util.NbBundle.getMessage(Installer.class, "UI_TAB_TITLE"), panel);
                tabs.setPreferredSize(panel.getPreferredSize());
                File messagesLog = getMessagesLog();
                try {
                    JEditorPane pane = new JEditorPane(Utilities.toURI(messagesLog).toURL());
                    pane.setEditable(false);
                    pane.setPreferredSize(panel.getPreferredSize());
                    tabs.addTab(org.openide.util.NbBundle.getMessage(Installer.class, "IDE_LOG_TAB_TITLE"), new JScrollPane(pane));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                viewDD = new DialogDescriptor(tabs, NbBundle.getMessage(Installer.class, "VIEW_DATA_TILTE"));
            }
            viewDD.setModal(false);
            Object[] closingOption = new Object[] { DialogDescriptor.CANCEL_OPTION  };
            viewDD.setOptions(closingOption);
            viewDD.setClosingOptions(closingOption);
            List<Object> additionalButtons = new ArrayList<Object>();
            if (slownData != null){
                JButton slownButton = new JButton();
                org.openide.awt.Mnemonics.setLocalizedText(slownButton, 
                        NbBundle.getMessage(Installer.class, "SubmitPanel.profileData.text"));
                slownButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showProfilerSnapshot(e);
                    }
                });
                additionalButtons.add(slownButton);
            }
            if (isOOM){
                JButton heapDumpButton = new JButton();
                org.openide.awt.Mnemonics.setLocalizedText(heapDumpButton,
                        NbBundle.getMessage(Installer.class, "SubmitPanel.heapDump.text"));
                heapDumpButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showHeapDump(e);
                    }
                });
//                additionalButtons.add(heapDumpButton);
            }
            viewDD.setAdditionalOptions(additionalButtons.toArray());
            Dialog view = DialogDisplayer.getDefault().createDialog(viewDD);
            view.setVisible(true);
        }

        private void showProfilerSnapshot(ActionEvent e){
             File tempFile = null;
             try { 
                 tempFile = File.createTempFile("selfsampler", ".npss"); // NOI18N
                 tempFile = FileUtil.normalizeFile(tempFile);
                 OutputStream os = new FileOutputStream(tempFile);
                 os.write(slownData.getNpsContent());
                 os.close();

                 File gestures = new File(new File(new File(
                         new File(System.getProperty("netbeans.user")), // NOI18N
                         "var"), "log"), "uigestures"); // NOI18N

                 SelfSampleVFS fs;
                 if (gestures.exists()) {
                     fs = new SelfSampleVFS(
                             new String[]{"selfsampler.npss", "selfsampler.log"},
                             new File[]{tempFile, gestures});
                 } else {
                     fs = new SelfSampleVFS(
                             new String[]{"selfsampler.npss"},
                             new File[]{tempFile});
                 }
                 FileObject fo = fs.findResource("selfsampler.npss");
                 final Node obj = DataObject.find(fo).getNodeDelegate();
                 Action a = obj.getPreferredAction();
                 if (a instanceof ContextAwareAction) {
                     a = ((ContextAwareAction)a).createContextAwareInstance(obj.getLookup());
                 }
                 a.actionPerformed(e);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (tempFile != null) tempFile.deleteOnExit();
            }
        }

        private void showHeapDump(ActionEvent e){
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override protected void assignInternalURL(final URL u) {
            if (browser != null) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        try {
                            browser.setPage(u);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
            markAssigned();
        }

        private synchronized void markAssigned(){
            urlAssigned = true;
            notifyAll();
        }

        @Override
        protected void showURL(URL u, boolean inIDE) {
            LOG.log(Level.FINE, "opening URL: {0}", u); // NOI18N
            if (inIDE){
                ReporterResultTopComponent.showUploadDone(u);
            }else{
                HtmlBrowser.URLDisplayer.getDefault().showURL(u);
            }
        }

        @Override
        protected void addMoreLogs(List<? super String> params, boolean openPasswd) {
            if ((reportPanel != null) && (report)){
                params.add(reportPanel.getSummary());
                params.add(reportPanel.getComment());
                try {
                    char[] passwd = reportPanel.getPasswdChars();
                    if ((openPasswd) && (passwd.length != 0) && (!reportPanel.asAGuest())){
                        String pwd = new String(passwd);
                        pwd = PasswdEncryption.encrypt(pwd);
                        params.add(pwd);
                    } else {
                        params.add("*********");// NOI18N
                    }
                } catch (GeneralSecurityException exc) {
                    LOG.log(Level.WARNING, "PASSWORD ENCRYPTION ERROR", exc);// NOI18N
                } catch (IOException exc) {
                    LOG.log(Level.WARNING, "PASSWORD ENCRYPTION ERROR", exc);// NOI18N
                }
            }
        }

        @Override
        protected Object showDialogAndGetValue(DialogDescriptor dd) {
            if (!connectDialog) {
                synchronized (this) {
                    while (!urlAssigned) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            d.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent e) {
                    doCloseDialog();
                }
            });
            try {
                EventQueue.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        d.setModal(false);
                        d.setVisible(true);
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            synchronized (this){
                while (d != null && !dontWaitForUserInputInTests){
                    try{
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return dd.getValue();
        }
        
        @Override
        protected void alterMessage(final DialogDescriptor dd) {
            if ("ERROR_URL".equals(msg)&(dd.getOptions().length > 1)){
                Object obj = dd.getOptions()[0];
                AbstractButton abut = null;
                String rptr = null;
                if (obj instanceof AbstractButton ) {
                    abut = (AbstractButton) obj;
                }
                if (abut != null) {
                    rptr = (String) abut.getClientProperty("alt");
                }
                if (reportPanel != null && "reportDialog".equals(rptr)&&!errorPage) {
                    EventQueue.invokeLater(new Runnable(){

                        @Override
                        public void run() {
                            dd.setMessage(reportPanel);
                            reportPanel.setInitialFocus();
                        }
                    });
                }
            }
        }

        @Override
        protected SlownessData getSlownessData() {
            return slownData;
        }
    } // end SubmitInteractive

    private static final class SubmitAutomatic extends Submit {
        final Button def;
        private boolean urlComputed;

        public SubmitAutomatic(String msg, Button def, DataType dataType, List<LogRecord> recs) {
            super(msg, dataType, recs);
            this.def = def;
        }

        public SubmitAutomatic(String msg, Button def) {
            this(msg, def, DataType.DATA_UIGESTURE, null);
        }

        @Override
        protected void createDialog() {
        }

        @Override
        protected void closeDialog() {
        }

        @Override
        protected void viewData() {
            assert false;
        }
        @Override
        protected synchronized void assignInternalURL(URL u) {
            urlComputed = true;
            notifyAll();
        }
        @Override
        protected void showURL(URL u, boolean inIDE) {
            hintURL = u;
        }

        @Override
        protected void addMoreLogs(List<? super String> params, boolean openPasswd) {
        }
        @Override
        protected Object showDialogAndGetValue(final DialogDescriptor dd) {
            //System.err.println("showDialogAndGetValue()");
            //Thread.dumpStack();
            while (!urlComputed) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            final JButton[] buttonPtr = new JButton[] { null };
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        for (Object o : dd.getOptions()) {
                            if (o instanceof JButton) {
                                JButton b = (JButton)o;
                                if (def.isCommand(b.getActionCommand())) {
                                    actionPerformed(new ActionEvent(b, 0, b.getActionCommand()));
                                    buttonPtr[0] = b;
                                    break;
                                }
                            }
                        }
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return DialogDescriptor.CLOSED_OPTION;
        }
        @Override
        protected void alterMessage(DialogDescriptor dd) {
        }

        @Override
        protected SlownessData getSlownessData() {
            return null;
        }
    } // end SubmitAutomatic
    private static final class PrefChangeListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (corePref.equals(evt.getNode()) && USAGE_STATISTICS_ENABLED.equals(evt.getKey())) {
                boolean newVal = Boolean.parseBoolean(evt.getNewValue());
                if (newVal != logMetricsEnabled) {
                    corePref.putBoolean(USAGE_STATISTICS_SET_BY_IDE, true);
                    logMetricsEnabled = newVal;
                    Logger log = Logger.getLogger(METRICS_LOGGER_NAME);
                    if (logMetricsEnabled) {
                        log.setUseParentHandlers(true);
                        log.setLevel(Level.FINEST);
                        log.addHandler(metrics);
                        MetricsHandler.setFlushOnRecord(false);
                    } else {
                        MetricsHandler.flushImmediatelly();
                        closeLogStreamMetrics();
                        log.removeHandler(metrics);
                    }
                }
            }
        }
    }

    static enum Button {
        EXIT("exit"),
        NEVER_AGAIN("never-again"),
        VIEW_DATA("view-data"),
        REDIRECT("redirect"),
        AUTO_SUBMIT("auto-submit"),
        SUBMIT("submit"),
        REFRESH("refresh"),
        PROXY("proxy");

        private final String name;
        Button(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean isCommand(String s) {
            return name.equals(s);
        }

        public static boolean isKnown(String n) {
            for (Button b : Button.values()) {
                if (n.equals(b.getName())) {
                    return true;
                }
            }
            return false;
        }
        public static boolean isSubmitTrigger(String n) {
            return SUBMIT.isCommand(n) || AUTO_SUBMIT.isCommand(n);
        }
    } // end of Buttons

    //  JUST FOR TESTS //
    private static boolean dontWaitForUserInputInTests = false;
    static void dontWaitForUserInputInTests(){
        dontWaitForUserInputInTests = true;
    }
}
