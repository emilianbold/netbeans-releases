/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.MissingResourceException;
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
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.uihandler.LogRecords;
import org.netbeans.lib.uihandler.PasswdEncryption;
import org.netbeans.modules.exceptions.ReportPanel;
import org.netbeans.modules.exceptions.ExceptionsSettings;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.io.NullOutputStream;
import org.openide.windows.WindowManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Registers and unregisters loggers.
 */
public class Installer extends ModuleInstall implements Runnable {
    static final long serialVersionUID = 1L;

    static final String USER_CONFIGURATION = "UI_USER_CONFIGURATION";   // NOI18N
    private static UIHandler ui = new UIHandler(false);
    private static UIHandler handler = new UIHandler(true);
    private static MetricsHandler metrics = new MetricsHandler();
    static final Logger LOG = Logger.getLogger(Installer.class.getName());
    public static final RequestProcessor RP = new RequestProcessor("UI Gestures"); // NOI18N
    public static final RequestProcessor RP_UI = new RequestProcessor("UI Gestures - Create Dialog"); // NOI18N
    public static RequestProcessor RP_OPT = null;
    private static final Preferences prefs = NbPreferences.forModule(Installer.class);
    private static OutputStream logStream;
    private static OutputStream logStreamMetrics;
    private static int logsSize;
    private static int logsSizeMetrics;
    private static URL hintURL;
    private static Object[] selectedExcParams;

    private static boolean logMetricsEnabled = false;
    /** Flag to store status of last metrics upload */
    private static boolean logMetricsUploadFailed = false;
    
    private static String USAGE_STATISTICS_ENABLED = "usageStatisticsEnabled"; // NOI18N
    private static String CORE_PREF_NODE = "org/netbeans/core"; // NOI18N
    private static Preferences corePref = NbPreferences.root().node (CORE_PREF_NODE);

    private JButton metricsEnable = new JButton();
    private JButton metricsCancel = new JButton();

    private static final String CMD_METRICS_ENABLE = "MetricsEnable";   // NOI18N
    private static final String CMD_METRICS_CANCEL = "MetricsCancel";   // NOI18N

    /** Action listener for Usage Statistics Reminder dialog */
    private ActionListener l = new ActionListener () {
        public void actionPerformed (ActionEvent ev) {
            cmd = ev.getActionCommand();
            if (CMD_METRICS_ENABLE.equals(cmd)) {
                corePref.putBoolean(USAGE_STATISTICS_ENABLED, true);
            } else if (CMD_METRICS_CANCEL.equals(cmd)) {
                corePref.putBoolean(USAGE_STATISTICS_ENABLED, false);
            }
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
        prefs.putLong(preferencesWritableKey, checkTime);  //NOI18N
        try {
            prefs.flush();
            prefs.sync();
            if(checkTime == prefs.getLong(preferencesWritableKey, 0)) {  //NOI18N
                preferencesWritable = true;
            }
        } catch (BackingStoreException e) {
            // immediatelly show dialog with exception (usually Access is denied)
            NotifyDescriptor.Exception eDesc = new NotifyDescriptor.Exception(e);
            DialogDisplayer.getDefault().notify(eDesc);
        }
    }

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
        logMetricsUploadFailed = prefs.getBoolean("metrics.upload.failed", Boolean.FALSE); // NOI18N
        corePref.addPreferenceChangeListener(new PrefChangeListener());

        if ((System.getProperty ("netbeans.full.hack") == null) && (System.getProperty ("netbeans.close") == null)) {
            usageStatisticsReminder();
        }
        
        System.setProperty("nb.show.statistics.ui",USAGE_STATISTICS_ENABLED);
        logMetricsEnabled = corePref.getBoolean(USAGE_STATISTICS_ENABLED,Boolean.FALSE);
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
                List<LogRecord> clusterRec = new ArrayList<LogRecord>();
                getClusterList(log, clusterRec);
                for (LogRecord rec : clusterRec) {
                    LogRecords.write(logStreamMetrics(), rec);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        EarlyHandler.disable();
        ScreenSize.logScreenSize();

        for (Activated a : Lookup.getDefault().lookupAll(Activated.class)) {
            a.activated(log);
        }

        if (logsSize >= UIHandler.MAX_LOGS) {
            WindowManager.getDefault().invokeWhenUIReady(this);
        }
    }
    
    private void usageStatisticsReminder () {
        boolean keyExists = false;
        String [] keys = null;
        try {
            keys = corePref.keys();
            for (int i = 0; i < keys.length; i++) {
                if (USAGE_STATISTICS_ENABLED.equals(keys[i])) {
                    keyExists = true;
                    break;
                }
            }
        } catch (BackingStoreException exc) {
            Exceptions.printStackTrace(exc);
            //Not able to confirm if key exists or not
            keyExists = true;
        }

        if (keyExists) {
            //If key exists do not ask user
            return;
        }

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
            public void run() {
                showDialog();
            }
        });
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

    static void writeOut(LogRecord r) {
        try {
            LogRecords.write(logStream(), r);
            if (logsSize >= UIHandler.MAX_LOGS) {
                if(preferencesWritable) {
                    prefs.putInt("count", UIHandler.MAX_LOGS);
                }
                closeLogStream();
                if (isHintsMode()) {
                    class Auto implements Runnable {
                        public void run() {
                            displaySummary("WELCOME_URL", true, true,true);
                        }
                    }
                    RP.post(new Auto()).waitFinished();
                }
                File f = logFile(0);
                f.renameTo(new File(f.getParentFile(), f.getName() + ".1"));
                logsSize = 0;
            } else {
                logsSize++;
                if (prefs.getInt("count", 0) < logsSize && preferencesWritable) {
                    prefs.putInt("count", logsSize);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
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

    static void writeOutMetrics (LogRecord r) {
        try {
            LogRecords.write(logStreamMetrics(), r);
            logsSizeMetrics++;
            if (preferencesWritable) {
                prefs.putInt("countMetrics", logsSizeMetrics);
            }
            if (logsSizeMetrics >= MetricsHandler.MAX_LOGS) {
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
                                LOG.log(Level.INFO, "Failed to rename file:" + f + " to:" + f1); // NOI18N
                            }
                        } else {
                            //Size is below limit, append data
                            appendFile(f, f1);
                        }
                    } else {
                        f1.delete();
                        if (!f.renameTo(f1)) {
                            LOG.log(Level.INFO, "Failed to rename file:" + f + " to:" + f1); // NOI18N
                        }
                    }
                } else {
                    if (!f.renameTo(f1)) {
                        LOG.log(Level.INFO, "Failed to rename file:" + f + " to:" + f1); // NOI18N
                    }
                }
                logsSizeMetrics = 0;
                if (preferencesWritable) {
                    prefs.putInt("countMetrics", logsSizeMetrics);
                }
                //Task to upload metrics data
                class Auto implements Runnable {
                    public void run() {
                        displaySummary("METRICS_URL", true, true, true, DataType.DATA_METRICS);
                    }
                }
                //Will be enabled as soon as server side will be adjusted to handle metrics data
                RP.post(new Auto()).waitFinished();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /** Apend content of source to target */
    private static void appendFile (File source, File target) {
        try {
            FileInputStream is = null;
            FileOutputStream os = null;
            try {
                is = new FileInputStream(source);
                BufferedInputStream bis = new BufferedInputStream(is);

                os = new FileOutputStream(target, true);
                BufferedOutputStream bos = new BufferedOutputStream(os);

                int c;
                while ((c = bis.read()) != -1) {
                    bos.write(c);
                }
                bos.flush();
            } finally {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
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

    static void getClusterList (Logger logger, List<LogRecord> clusterRec) {
        LogRecord rec = new LogRecord(Level.INFO, "USG_INSTALLED_CLUSTERS");
        String dirs = System.getProperty("netbeans.dirs");
        String [] k = dirs.split(File.pathSeparator);
        String [] l = new String[k.length];
        for (int i = 0; i < k.length; i++) {
            File f = new File(k[i]);
            l[i] = f.getName();
        }
        rec.setParameters(l);
        rec.setLoggerName(logger.getName());
        clusterRec.add(rec);
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
        return prefs.getInt("count", 0); // NOI18N
    }

    public static List<LogRecord> getLogs() {
        UIHandler.waitFlushed();

        File f = logFile(0);
        if (f == null || !f.exists()) {
            return new ArrayList<LogRecord>();
        }
        closeLogStream();

        class H extends Handler {
            List<LogRecord> logs = new LinkedList<LogRecord>();

            public void publish(LogRecord r) {
                logs.add(r);
                if (logs.size() > UIHandler.MAX_LOGS) {
                    logs.remove(0);
                }
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        H hndlr = new H();


        InputStream is = null;
        File f1 = logFile(1);
        if (logsSize < UIHandler.MAX_LOGS && f1 != null && f1.exists()) {
            try {
                is = new FileInputStream(f1);
                LogRecords.scan(is, hndlr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        try {
            is = new FileInputStream(f);
            LogRecords.scan(is, hndlr);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return hndlr.logs;
    }

    public static List<LogRecord> getLogsMetrics() {
        
        class H extends Handler {
            List<LogRecord> logs = new LinkedList<LogRecord>();

            public void publish(LogRecord r) {
                logs.add(r);
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        H hndlr = new H();

        InputStream is = null;
        File f1 = logFileMetrics(1);
        if ((f1 != null) && f1.exists()) {
            try {
                is = new FileInputStream(f1);
                LogRecords.scan(is, hndlr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        /*try {
            is = new FileInputStream(f);
            LogRecords.scan(is, hndlr);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }*/

        return hndlr.logs;
    }

    private static File logFile(int revision) {
        String ud = System.getProperty("netbeans.user"); // NOI18N
        if (ud == null || "memory".equals(ud)) { // NOI18N
            return null;
        }

        String suffix = revision == 0 ? "" : "." + revision;

        File userDir = new File(ud); // NOI18N
        File logFile = new File(new File(new File(userDir, "var"), "log"), "uigestures" + suffix);
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
            os = new BufferedOutputStream(new FileOutputStream(logFile, true));
        } else {
            os = new NullOutputStream();
        }

        synchronized (Installer.class) {
            logStream = os;
        }

        return os;
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
            os = new BufferedOutputStream(new FileOutputStream(logFile, true));
        } else {
            os = new NullOutputStream();
        }

        synchronized (Installer.class) {
            logStreamMetrics = os;
        }

        return os;
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
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
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
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    static void clearLogs() {
        closeLogStream();

        for (int i = 0; ; i++) {
            File f = logFile(i);
            if (f == null || !f.exists()) {
                break;
            }
            f.delete();
        }

        logsSize = 0;
        if(preferencesWritable) {
            prefs.putInt("count", 0);
        }
        UIHandler.SUPPORT.firePropertyChange(null, null, null);
    }

    @Override
    public boolean closing() {
        UIHandler.waitFlushed();

        if (getLogsSize() == 0) {
            return true;
        }

        return displaySummary("EXIT_URL", false, false,true); // NOI18N
    }

    private static AtomicReference<String> DISPLAYING = new AtomicReference<String>();

    private static boolean displaySummary(String msg, boolean explicit, boolean auto, boolean connectDialog, DataType dataType) {
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

            v = doDisplaySummary(msg, auto, connectDialog, dataType);
        } finally {
            DISPLAYING.set(null);
        }
        return v;
    }

    public static boolean displaySummary(String msg, boolean explicit, boolean auto, boolean connectDialog) {
        return displaySummary(msg, explicit, auto, connectDialog, DataType.DATA_UIGESTURE);
    }

    protected static Throwable getThrown(){
        LogRecord log = getThrownLog();
        if (log == null){
            return null;
        }else{
            return log.getThrown();
        }
    }

    protected static LogRecord getThrownLog(){
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
        List<LogRecord> list = getLogs();
        ListIterator<LogRecord> it = list.listIterator(list.size());
        Throwable thr = null;
        LogRecord result = null;
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

    private static boolean doDisplaySummary(String msg, boolean auto, boolean connectDialog, DataType dataType) {
        Submit submit = auto ? new SubmitAutomatic(msg, Button.SUBMIT, dataType) : new SubmitInteractive(msg, connectDialog, dataType);
        submit.doShow(dataType);
        return submit.okToExit;
    }


    private static boolean isChild(org.w3c.dom.Node child, org.w3c.dom.Node parent) {
        while (child != null) {
            if (child == parent) {
                return true;
            }
            child = child.getParentNode();
        }
        return false;
    }

    private static String attrValue(org.w3c.dom.Node in, String attrName) {
        org.w3c.dom.Node n = in.getAttributes().getNamedItem(attrName);
        return n == null ? null : n.getNodeValue();
    }

    /** Tries to parse a list of buttons provided by given page.
     * @param u the url to read the page from
     * @param defaultButton the button to add always to the list
     */
    static void parseButtons(InputStream is, Object defaultButton, DialogDescriptor dd)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        PushbackInputStream isWithProlog = new PushbackInputStream(is, 255);
        byte[] xmlHeader = new byte[5];
        int len = isWithProlog.read(xmlHeader);
        isWithProlog.unread(xmlHeader, 0, len);

        if (len < 5 || xmlHeader[0] != '<' ||
            xmlHeader[1] != '?' ||
            xmlHeader[2] != 'x' ||
            xmlHeader[3] != 'm' ||
            xmlHeader[4] != 'l'
        ) {
            String header = "<?xml version='1.0' encoding='utf-8'?>";
            isWithProlog.unread(header.getBytes("utf-8"));
        }

        Document doc = builder.parse(isWithProlog);

        List<Object> buttons = new ArrayList<Object>();
        List<Object> left = new ArrayList<Object>();

        NodeList forms = doc.getElementsByTagName("form");
        for (int i = 0; i < forms.getLength(); i++) {
            String action = forms.item(i).getAttributes().getNamedItem("action").getNodeValue();
            if ((action == null) || ("".equals(action))){// logging for issue #145167
                Logger logger = Logger.getLogger("org.netbeans.ui.logger.Installer");
                LogRecord rec = new LogRecord(Level.WARNING, "invalid action from doc:");
                String[] params = new String[]{forms.item(i).toString(), doc.getTextContent(), doc.getDocumentURI()};
                rec.setParameters(params);
                logger.log(rec);
            }
            Form f = new Form(action);
            NodeList inputs = doc.getElementsByTagName("input");
            for (int j = 0; j < inputs.getLength(); j++) {
                if (isChild(inputs.item(j), forms.item(i))) {
                    org.w3c.dom.Node in = inputs.item(j);
                    String type = attrValue(in, "type");
                    String name = attrValue(in, "name");
                    String value = attrValue(in, "value");
                    String align = attrValue(in, "align");
                    String alt = attrValue(in, "alt");
                    boolean enabled = !"true".equals(attrValue(in, "disabled")); // NOI18N

                    List<Object> addTo = "left".equals(align) ? left : buttons;

                    if ("hidden".equals(type) && Button.isSubmitTrigger(name)) { // NOI18N
                        f.submitValue = value;
                        JButton b = new JButton();
                        Mnemonics.setLocalizedText(b, f.submitValue);
                        b.setActionCommand(name); // NOI18N
                        b.putClientProperty("url", f.url); // NOI18N
                        b.setDefaultCapable(addTo.isEmpty() && addTo == buttons);
                        b.putClientProperty("alt", alt); // NOI18N
                        b.putClientProperty("now", f.submitValue); // NOI18N
                        b.setEnabled(enabled);
                        addTo.add(b);
                        continue;
                    }


                    if ("hidden".equals(type)) { // NOI18N
                        JButton b = new JButton();
                        Mnemonics.setLocalizedText(b, value);
                        b.setActionCommand(name);
                        b.setDefaultCapable(addTo.isEmpty() && addTo == buttons);
                        b.putClientProperty("alt", alt); // NOI18N
                        b.putClientProperty("now", value); // NOI18N
                        b.setEnabled(enabled && Button.isKnown(name));
                        addTo.add(b);
                        if (Button.EXIT.isCommand(name)) { // NOI18N
                            defaultButton = null;
                        }else if (Button.REDIRECT.isCommand(name)){
                            b.putClientProperty("url", f.url); // NOI18N
                        }
                    }
                }
            }
        }
        if (defaultButton != null) {
            buttons.add(defaultButton);
        }
        dd.setOptions(buttons.toArray());
        dd.setAdditionalOptions(left.toArray());

        NodeList title = doc.getElementsByTagName("title");
        for (int i = 0; i < title.getLength(); i++) {
            String t = title.item(i).getTextContent();
            if (t != null) {
                dd.setTitle(t);
                break;
            }
        }
    }
    
    static String decodeButtons(Object res, URL[] url) {
        return decodeButtons(res, url, DataType.DATA_UIGESTURE);
    }

    static String decodeButtons(Object res, URL[] url, DataType dataType) {
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
                    LOG.log(Level.WARNING, "Cannot decode URL: " + post, ex); // NOI18N
                    url[0] = null;
                    return null;
                }
            }
            return b.getActionCommand();
        }
        return res instanceof String ? (String)res : null;
    }

    static URL uploadLogs(URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs, DataType dataType) throws IOException {
        ProgressHandle h = null;
        //Do not show progress UI for metrics upload
        if (dataType != DataType.DATA_METRICS) {
            h = ProgressHandleFactory.createHandle(NbBundle.getMessage(Installer.class, "MSG_UploadProgressHandle"));
        }
        try {
            return uLogs(h, postURL, id, attrs, recs, dataType);
        } finally {
            if (h != null) {
                h.finish();
            }
        }
    }
    
    static URL uploadLogs(URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs) throws IOException {
        return uploadLogs(postURL, id, attrs, recs, DataType.DATA_UIGESTURE);
    }

    private static URL uLogs
    (ProgressHandle h, URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs, DataType dataType) throws IOException {
        if (dataType != DataType.DATA_METRICS) {
            h.start(100 + recs.size());
            h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadConnecting")); // NOI18N
        }
        
        LOG.log(Level.FINE, "uploadLogs, postURL = {0}", postURL); // NOI18N
        URLConnection conn = postURL.openConnection();

        if (dataType != DataType.DATA_METRICS) {
            h.progress(50);
        }
        
        conn.setReadTimeout(20000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=--------konec<>bloku");
        conn.setRequestProperty("Pragma", "no-cache");
        conn.setRequestProperty("Cache-control", "no-cache");
        conn.setRequestProperty("User-Agent", "NetBeans");
        
        if (dataType != DataType.DATA_METRICS) {
            h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadSending"), 60);
        }
        LOG.log(Level.FINE, "uploadLogs, header sent"); // NOI18N

        PrintStream os = new PrintStream(conn.getOutputStream());
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
            h.progress(70);
        }

        os.println("----------konec<>bloku");

        if (id == null) {
            id = "uigestures"; // NOI18N
        }

        os.println("Content-Disposition: form-data; name=\"logs\"; filename=\"" + id + "\"");
        os.println("Content-Type: x-application/gzip");
        os.println();
        GZIPOutputStream gzip = new GZIPOutputStream(os);
        DataOutputStream data = new DataOutputStream(gzip);
        data.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("utf-8")); // NOI18N
        data.write("<uigestures version='1.0'>\n".getBytes("utf-8")); // NOI18N

        int cnt = 80;
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
        
        LOG.log(Level.FINE, "uploadLogs, reading reply"); // NOI18N
        InputStream is = conn.getInputStream();
        StringBuffer redir = new StringBuffer();
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
        Matcher m = p.matcher(redir);


        if (m.find()) {
            LOG.log(Level.FINE, "uploadLogs, found url = {0}", m.group(1)); // NOI18N
            return new URL(m.group(1));
        } else {
            File f = File.createTempFile("uipage", "html");
            f.deleteOnExit();
            FileWriter w = new FileWriter(f);
            w.write(redir.toString());
            w.close();
            LOG.log(Level.FINE, "uploadLogs, temporary url = {0}", f.toURI()); // NOI18N
            return f.toURI().toURL();
        }
    }

    private static String findIdentity() {
        Preferences p = NbPreferences.root().node("org/netbeans/modules/autoupdate"); // NOI18N
        String id = p.get("qualifiedId", null);
        //Strip id prefix
        int ind = id.indexOf("0");
        if (ind != -1) {
            id = id.substring(ind + 1);
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
                    LOG.fine("Downloaded with encoding '" + enc + "':\n" + text);
                } else {
                    LOG.log(Level.FINE, "Downloaded with utf-8:\n{0}", text);
                }
            }
            os.write(text.getBytes("UTF-8"));
        }
    }

    private static abstract class Submit implements ActionListener, Runnable {
        private AtomicBoolean isSubmiting;// #114505 , report is sent two times
        protected String exitMsg;
        protected DialogDescriptor dd;
        protected String msg;
        protected boolean report;//property tells me wheather I'm in report mode
        protected boolean okToExit;
        protected ReportPanel reportPanel;
        private URL url;
        private boolean dialogCreated;
        private boolean checkingResult, refresh = false;
        protected boolean errorPage = false;
        protected DataType dataType = DataType.DATA_UIGESTURE;

        public Submit(String msg) {
            this(msg,DataType.DATA_UIGESTURE);
        }

        public Submit(String msg, DataType dataType) {
            this.msg = msg;
            this.dataType = dataType;
            isSubmiting = new AtomicBoolean(false);
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
        protected abstract boolean viewData();
        protected abstract void assignInternalURL(URL u);
        protected abstract void saveUserName();
        protected abstract void addMoreLogs(List<? super String> params, boolean openPasswd);
        protected abstract void showURL(URL externalURL);


        public void doShow(DataType dataType) {
            if (dataType == DataType.DATA_UIGESTURE) {
                Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
                for (Deactivated a : Lookup.getDefault().lookupAll(Deactivated.class)) {
                    a.deactivated(log);
                }
            }
            if (report) {
                dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "ErrorDialogTitle"));
            } else {
                dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "MSG_SubmitDialogTitle"));
            }

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
                while (!dialogCreated) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                notifyAll();
            }

            LOG.log(Level.FINE, "doShow, dialog has been created"); // NOI18N
            boolean firstRound = true;
            for (;;) {
                try {
                    if (url == null) {
                        url = new URL(defaultURI); // NOI18N
                    }

                    LOG.log(Level.FINE, "doShow, reading from = {0}", url);
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
                    InputStream is = new FileInputStream(tmp);
                    parseButtons(is, exitMsg, dd);
                    LOG.log(Level.FINE, "doShow, parsing buttons: " + Arrays.toString(dd.getOptions())); // NOI18N
                    alterMessage(dd);
                    is.close();
                    url = tmp.toURI().toURL();
                } catch (ParserConfigurationException ex) {
                    LOG.log(Level.WARNING, null, ex);
                } catch (SAXException ex) {
                    LOG.log(Level.WARNING, url.toExternalForm(), ex);
                } catch (java.net.SocketTimeoutException ex) {
                    catchConnectionProblem(ex);
                    continue;
                } catch (UnknownHostException ex) {
                    catchConnectionProblem(ex);
                    continue;
                } catch (NoRouteToHostException ex) {
                    catchConnectionProblem(ex);
                    continue;
                }catch (ConnectException ex){
                    catchConnectionProblem(ex);
                    continue;
                } catch (IOException ex) {
                    if (firstRound){
                        catchConnectionProblem(ex);
                        firstRound = false;
                        continue;
                    }else{// preventing from deadlock while reading error page
                        LOG.log(Level.WARNING, url.toExternalForm(), ex);
                    }
                }
                LOG.log(Level.FINE, "doShow, assignInternalURL = {0}", url);
                assignInternalURL(url);
                refresh = false;
                synchronized (this) {
                    while (dialogCreated && !refresh) {
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

        private synchronized final void doCloseDialog() {
            dialogCreated = false;
            closeDialog();
            notifyAll();
            LOG.log(Level.FINE, "doCloseDialog");
        }

        private void catchConnectionProblem(Exception exception){
            LOG.log(Level.INFO, url.toExternalForm(), exception);
            url = getUnknownHostExceptionURL();
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

        public void run() {
            createDialog();
            synchronized (this) {
                dialogCreated = true;
                // dialog created let the code go on
                notifyAll();


                try {
                    // wait till the other code runs
                    wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            LOG.log(Level.FINE, "run, showDialogAndGetValue");
            Object res = showDialogAndGetValue(dd);
            LOG.log(Level.FINE, "run, showDialogAndGetValue, res = {0}", res);

            if (res == exitMsg) {
                okToExit = true;
            }
            LOG.log(Level.FINE, "run, okToExit = {0}", okToExit);
            doCloseDialog();
        }

        public void actionPerformed(ActionEvent e) {
            final URL[] universalResourceLocator = new URL[1];
            String actionURL = decodeButtons(e.getSource(), universalResourceLocator, dataType);

            LOG.log(Level.FINE, "actionPerformed: command = {0}", e.getActionCommand()); // NOI18N

            boolean submit = Button.SUBMIT.isCommand(actionURL);
            if (Button.AUTO_SUBMIT.isCommand(e.getActionCommand())) {
                submit = true;
                if(preferencesWritable) {
                    prefs.putBoolean("autoSubmitWhenFull", true); // NOI18N
                }
            }

            if (submit) { // NOI18N
                if (isSubmiting.getAndSet(true)) {
                    LOG.info("ALREADY SUBMITTING"); // NOI18N
                    return;
                }
                try {
                    List<LogRecord> recs = null;
                    if (dataType == DataType.DATA_UIGESTURE) {
                        recs = getLogs();
                        saveUserName();
                        LogRecord userData = getUserData(true);
                        LogRecord thrownLog = getThrownLog();
                        if (thrownLog != null) {
                            recs.add(thrownLog);//exception selected by user
                        }
                        recs.add(TimeToFailure.logFailure());
                        recs.add(BuildInfo.logBuildInfoRec());
                        recs.add(userData);
                        if ((report) && !(reportPanel.asAGuest())) {
                            if (!checkUserName()) {
                                reportPanel.showWrongPassword();
                                return;
                            }
                        }
                        LOG.fine("posting upload UIGESTRUE");// NOI18N
                    } else if (dataType == DataType.DATA_METRICS) {
                        recs = getLogsMetrics();
                        saveUserName();
                        if ((report) && !(reportPanel.asAGuest())) {
                            if (!checkUserName()) {
                                reportPanel.showWrongPassword();
                                return;
                            }
                        }
                        LOG.fine("posting upload METRICS");// NOI18N
                    }
                    final List<LogRecord> recsFinal = recs;
                    RP.post(new Runnable() {
                        public void run() {
                            uploadAndPost(recsFinal, universalResourceLocator[0], dataType);
                        }
                    });
                    okToExit = false;
                    // this should close the descriptor
                    doCloseDialog();
                } catch (InterruptedException exc) {
                    LOG.log(Level.INFO, "submitting data failed", exc);// NOI18N
                } finally {
                    isSubmiting.set(false);
                }
                return;
            }

            if (Button.REDIRECT.isCommand(e.getActionCommand())){
                if (universalResourceLocator[0] != null) {
                    showURL(universalResourceLocator[0]);
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
                if (viewData() && (e.getSource() instanceof AbstractButton)) {
                    AbstractButton abut = (AbstractButton)e.getSource();
                    String alt = (String) abut.getClientProperty("alt"); // NOI18N
                    if (alt != null) {
                        String now = (String)abut.getClientProperty("now"); // NOI18N
                        Mnemonics.setLocalizedText(abut, alt);
                        abut.putClientProperty("alt", now); // NOI18N
                        abut.putClientProperty("now", alt); // NOI18N
                    }
                }
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

        private boolean checkUserName() throws InterruptedException{
            checkingResult=true;
            RequestProcessor.Task checking;
            checking = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ExceptionsSettings settings = new ExceptionsSettings();
                String login = settings.getUserName();
                String passwd = settings.getPasswd();
                try {
                    char[] array = new char[100];
                    URL url = new URL(NbBundle.getMessage(Installer.class, "CHECKING_SERVER_URL", login, passwd));
                    URLConnection connection = url.openConnection();
                    connection.setRequestProperty("User-Agent", "NetBeans");
                    Reader reader = new InputStreamReader(connection.getInputStream());
                    int length = reader.read(array);
                    checkingResult = new Boolean(new String(array, 0, length));
                } catch (Exception exception) {
                    Logger.getLogger(Installer.class.getName()).log(Level.INFO, "CHECKING PASSWORD FAILED", exception); // NOI18N
                }
            }
            });
            checking.waitFinished(10000);
            return checkingResult;
        }

        private void uploadAndPost(List<LogRecord> recs, URL u, DataType dataType) {
            URL nextURL = null;

            if(preferencesWritable) {
                prefs.putInt("submitted", 1 + prefs.getInt("submitted", 0)); // NOI18N
            }

            try {
                if (dataType == DataType.DATA_METRICS) {
                    logMetricsUploadFailed = false;
                }
                nextURL = uploadLogs(u, findIdentity(), Collections.<String,String>emptyMap(), recs, dataType);
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
                showURL(nextURL);
            }
        }

        protected final LogRecord getUserData(boolean openPasswd) {
            LogRecord userData;
            ExceptionsSettings settings = new ExceptionsSettings();
            ArrayList<String> params = new ArrayList<String>(6);
            params.add(getOS());
            params.add(getVM());
            params.add(getVersion());
            saveUserName();
            params.add(settings.getUserName());
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

        public SubmitInteractive(String msg, boolean connectDialog, DataType dataType) {
            super(msg,dataType);
            this.connectDialog = connectDialog;
        }

        public SubmitInteractive(String msg, boolean connectDialog) {
            this(msg,connectDialog,DataType.DATA_UIGESTURE);
        }

        protected void createDialog() {
            Dimension dim = new Dimension(450, 50);

            if (reportPanel==null) {
                reportPanel = new ReportPanel();
            }
            Throwable t = getThrown();
            if (t != null && reportPanel !=null){
                reportPanel.setSummary(createMessage(t));
                if ("ERROR_URL".equals(msg)) {
                    dim = new Dimension(470, 450);
                }
            }
            browser = new JEditorPane();
            try {
                URL resource = new URL("nbresloc:/org/netbeans/modules/uihandler/Connecting.html"); // NOI18N
                browser.setPage(resource); // NOI18N
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }

            browser.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 0, 8));
            browser.setPreferredSize(dim);
            browser.setEditable(false);
            browser.setEditorKit(new HTMLEditorKit()); // needed up to nb5.5
            browser.setBackground(new JLabel().getBackground());
            browser.addHyperlinkListener(this);

            JScrollPane p = new JScrollPane();
            p.setViewportView(browser);
            p.setBorder(BorderFactory.createEmptyBorder());
            p.setPreferredSize(dim);


            dd.setMessage(p);

            //        AbstractNode root = new AbstractNode(new Children.Array());
            //        root.setName("root"); // NOI18N
            //        root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
            //        root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
            //        for (LogRecord r : recs) {
            //            root.getChildren().add(new Node[] { UINode.create(r) });
            //        }
            //
            //        panel.getExplorerManager().setRootContext(root);

            Object[] arr = new Object[] { exitMsg };
            dd.setOptions(arr);
            dd.setClosingOptions(arr);
            dd.setButtonListener(this);
            dd.setModal(true);
            d = DialogDisplayer.getDefault().createDialog(dd);
        }

        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                showURL(e.getURL());
            }
        }

        protected void closeDialog() {
            if (d == null) {
                return;
            }

            dd.setValue(DialogDescriptor.CLOSED_OPTION);
            d.setVisible(false);
            d = null;
        }

        protected boolean  viewData() {
            if (panel == null) {
                panel = new SubmitPanel();
                AbstractNode root = new AbstractNode(new Children.Array());
                root.setName("root"); // NOI18N
                List<LogRecord> recs = getLogs();
                recs.add(getUserData(false));
                root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
                root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
                LinkedList<Node> nodes = new LinkedList<Node>();
                for (LogRecord r : recs) {
                    Node n = UINode.create(r);
                    nodes.add(n);
                    panel.addRecord(r, n);
                }
                root.getChildren().add(nodes.toArray(new Node[0]));
                panel.getExplorerManager().setRootContext(root);
            }

            DialogDescriptor viewDD = new DialogDescriptor(panel, "Data");
            viewDD.setModal(true);
            viewDD.setOptions(new Object[] { DialogDescriptor.CLOSED_OPTION  });
            Dialog view = DialogDisplayer.getDefault().createDialog(viewDD);
            view.setVisible(true);
            return false;
        }
        protected synchronized void assignInternalURL(URL u) {
            if (browser != null) {
                try {
                    browser.setPage(u);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            urlAssigned = true;
            notifyAll();
        }
        protected void showURL(URL u) {
            LOG.log(Level.FINE, "opening URL: " + u); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(u);
        }
        protected void saveUserName() {
            if (reportPanel != null && report) {
                reportPanel.saveUserName();
            }
        }

        protected void addMoreLogs(List<? super String> params, boolean openPasswd) {
            if ((reportPanel != null)&&(report)){
                params.add(reportPanel.getSummary());
                params.add(reportPanel.getComment());
                try {
                    if (openPasswd) {
                        String passwd = new ExceptionsSettings().getPasswd();
                        if ((passwd.length() != 0) && (!reportPanel.asAGuest())){
                            passwd = PasswdEncryption.encrypt(passwd);
                        }
                        params.add(passwd);
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
            d.setVisible(true);
            return dd.getValue();
        }
        protected void alterMessage(DialogDescriptor dd) {
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
                if ("reportDialog".equals(rptr)&&!errorPage) {
                    dd.setMessage(reportPanel);
                }
            }
        }
    } // end SubmitInteractive

    private static final class SubmitAutomatic extends Submit {
        Button def;
        private boolean urlComputed;

        public SubmitAutomatic(String msg, Button def, DataType dataType) {
            super(msg,dataType);
            this.def = def;
        }

        public SubmitAutomatic(String msg, Button def) {
            this(msg,def,DataType.DATA_UIGESTURE);
        }

        protected void createDialog() {
        }

        protected void closeDialog() {
        }

        protected boolean viewData() {
            assert false;
            return false;
        }
        protected synchronized void assignInternalURL(URL u) {
            urlComputed = true;
            notifyAll();
        }
        protected void showURL(URL u) {
            hintURL = u;
        }
        protected void saveUserName() {
        }
        protected void addMoreLogs(List<? super String> params, boolean openPasswd) {
        }
        protected Object showDialogAndGetValue(DialogDescriptor dd) {
            while (!urlComputed) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            for (Object o : dd.getOptions()) {
                if (o instanceof JButton) {
                    JButton b = (JButton)o;
                    if (def.isCommand(b.getActionCommand())) {
                        actionPerformed(new ActionEvent(b, 0, b.getActionCommand()));
                        return b;
                    }
                }
            }
            return DialogDescriptor.CLOSED_OPTION;
        }
        protected void alterMessage(DialogDescriptor dd) {
        }
    } // end SubmitAutomatic

    private static final class PrefChangeListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            if (corePref.equals(evt.getNode()) && USAGE_STATISTICS_ENABLED.equals(evt.getKey())) {
                boolean newVal = Boolean.parseBoolean(evt.getNewValue());
                if (newVal != logMetricsEnabled) {
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

    private static enum Button {
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
}
