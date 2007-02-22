/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.lib.uihandler.LogRecords;
import org.netbeans.modules.exceptions.ReportPanel;
import org.netbeans.modules.exceptions.settings.ExceptionsSettings;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Registers and unregisters loggers.
 */
public class Installer extends ModuleInstall {
    public static final String USER_CONFIGURATION = "UI_USER_CONFIGURATION";   // NOI18N
    private static Queue<LogRecord> logs = new LinkedList<LogRecord>();
    private static UIHandler ui = new UIHandler(logs, false);
    private static UIHandler handler = new UIHandler(logs, true);
    static final Logger LOG = Logger.getLogger(Installer.class.getName());
    static final RequestProcessor RP = new RequestProcessor("UI Gestures"); // NOI18N
    
    @Override
    public void restored() {
        File logFile = logFile();
        if (logFile != null && logFile.canRead()) {
            InputStream is = null;
            try {
                is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(logFile)));
                LogRecords.scan(is, ui);
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Cannot read " + logFile, ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception ex) {
                        LOG.log(Level.INFO, "Cannot read " + logFile, ex);
                    }
                }
            }
        }
        
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINEST);
        log.addHandler(ui);
        Logger all = Logger.getLogger("");
        all.addHandler(handler);
        
        for (Activated a : Lookup.getDefault().lookupAll(Activated.class)) {
            a.activated(log);
        }
        /*
        Enumeration<String> en = LogManager.getLogManager().getLoggerNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            if (name.startsWith("org.netbeans.ui")) {
                Logger l = Logger.getLogger(name);
                l.setLevel(Level.FINEST);
            }
        }
         */
    }
    
    @Override
    public void uninstalled() {
        close();
    }
    
    @Override
    public void close() {
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        log.removeHandler(ui);
        Logger all = Logger.getLogger(""); // NOI18N
        all.removeHandler(handler);
        
        File logFile = logFile();
        if (logFile != null) {
            try {
                logFile.getParentFile().mkdirs();
                // flush all the unsend data to disk
                OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(logFile)));
                for (LogRecord r : getLogs()) {
                    LogRecords.write(os, r);
                }
                os.close();
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Cannot write " + logFile, ex);
            }
        }
    }
    
    public static int getLogsSize() {
        return logs.size();
    }
    
    public static List<LogRecord> getLogs() {
        synchronized (UIHandler.class) {
            return new ArrayList<LogRecord>(logs);
        }
    }
    
    private static File logFile() {
        String ud = System.getProperty("netbeans.user"); // NOI18N
        if (ud == null || "memory".equals(ud)) { // NOI18N
            return null;
        }
        
        File userDir = new File(ud); // NOI18N
        File logFile = new File(new File(new File(userDir, "var"), "log"), "uigestures.gz");
        return logFile;
    }
    
    static void clearLogs() {
        synchronized (UIHandler.class) {
            logs.clear();
        }
        UIHandler.SUPPORT.firePropertyChange(null, null, null);
    }
    
    public boolean closing() {
        if (getLogsSize() == 0) {
            return true;
        }
        
        return displaySummary("EXIT_URL", false); // NOI18N
    }
    
    private static AtomicReference<String> DISPLAYING = new AtomicReference<String>();
    static boolean displaySummary(String msg, boolean explicit) {
        if (!DISPLAYING.compareAndSet(null, msg)) {
            return true;
        }
        
        boolean v = true;
        try {
            if (!explicit) {
                boolean dontAsk = NbPreferences.forModule(Installer.class).getBoolean("ask.never.again." + msg, false); // NOI18N
                if (dontAsk) {
                    LOG.log(Level.INFO, "UI Gesture Collector's ask.never.again.{0} is true, exiting", msg); // NOI18N
                    return true;
                }
            }
            
            v = doDisplaySummary(msg);
        } finally {
            DISPLAYING.set(null);
        }
        return v;
    }
    
    protected static Throwable getThrown(){
        List<LogRecord> list = getLogs();
        ListIterator<LogRecord> it = list.listIterator(list.size());
        while (it.hasPrevious()){
            Throwable t = it.previous().getThrown();
            // find first exception from end
            if (t != null) return t;
        }
        return null;// no throwable found
    }
    
    private static boolean doDisplaySummary(String msg) {
        Submit submit = new Submit(msg);
        submit.doShow();
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
        Document doc = builder.parse(is);
        
        List<Object> buttons = new ArrayList<Object>();
        List<Object> left = new ArrayList<Object>();
        
        NodeList forms = doc.getElementsByTagName("form");
        for (int i = 0; i < forms.getLength(); i++) {
            Form f = new Form(forms.item(i).getAttributes().getNamedItem("action").getNodeValue());
            NodeList inputs = doc.getElementsByTagName("input");
            for (int j = 0; j < inputs.getLength(); j++) {
                if (isChild(inputs.item(j), forms.item(i))) {
                    org.w3c.dom.Node in = inputs.item(j);
                    String type = attrValue(in, "type");
                    String name = attrValue(in, "name");
                    String value = attrValue(in, "value");
                    String align = attrValue(in, "align");
                    String alt = attrValue(in, "alt");
                    
                    List<Object> addTo = "left".equals(align) ? left : buttons;
                    
                    if ("hidden".equals(type) && "submit".equals(name)) { // NOI18N
                        f.submitValue = value;
                        JButton b = new JButton();
                        Mnemonics.setLocalizedText(b, f.submitValue);
                        b.setActionCommand("submit"); // NOI18N
                        b.putClientProperty("url", f.url); // NOI18N
                        b.setDefaultCapable(addTo.isEmpty() && addTo == buttons);
                        b.putClientProperty("alt", alt); // NOI18N
                        b.putClientProperty("now", f.submitValue); // NOI18N
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
                        addTo.add(b);
                        if ("exit".equals(name)) { // NOI18N
                            defaultButton = null;
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
    }
    
    static String decodeButtons(Object res, URL[] url) {
        if (res instanceof JButton) {
            JButton b = (JButton)res;
            Object post = b.getClientProperty("url"); // NOI18N
            if (post instanceof String) {
                String replace = System.getProperty("org.netbeans.modules.uihandler.Submit"); // NOI18N
                if (replace != null) {
                    post = replace;
                }
                try {
                    url[0] = new URL((String) post);
                } catch (MalformedURLException ex) {
                    url[0] = null;
                }
            }
            return b.getActionCommand();
        }
        return res instanceof String ? (String)res : null;
    }
    
    static URL uploadLogs(URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs) throws IOException {
        URLConnection conn = postURL.openConnection();
        
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=--------konec<>bloku");
        conn.setRequestProperty("Pragma", "no-cache");
        conn.setRequestProperty("Cache-control", "no-cache");
        
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
        for (LogRecord r : recs) {
            LogRecords.write(data, r);
        }
        data.write("</uigestures>\n".getBytes("utf-8")); // NOI18N
        data.flush();
        gzip.finish();
        os.println("----------konec<>bloku--");
        os.close();
        
        
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
        
        LOG.fine("Reply from uploadLogs:");
        LOG.fine(redir.toString());
        
        Pattern p = Pattern.compile("<meta\\s*http-equiv=.Refresh.\\s*content.*url=['\"]?([^'\" ]*)\\s*['\"]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(redir);
        
        if (m.find()) {
            return new URL(m.group(1));
        } else {
            File f = File.createTempFile("uipage", "html");
            FileWriter w = new FileWriter(f);
            w.write(redir.toString());
            w.close();
            return f.toURI().toURL();
        }
    }
    
    private static String findIdentity() {
        Preferences p = NbPreferences.root().node("org/netbeans/modules/autoupdate"); // NOI18N
        String id = p.get("ideIdentity", null);
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
    
    private static final class Submit implements ActionListener, Mutex.Action<Void> {
        private String msg;
        boolean okToExit;
        private DialogDescriptor dd;
        private Dialog d;
        private SubmitPanel panel;
        private HtmlBrowser browser;
        private URL url;
        private String exitMsg;
        private boolean report;//property tells me wheather I'm in report mode
        private ReportPanel reportPanel;
        
        public Submit(String msg) {
            this.msg = msg;
            if ("ERROR_URL".equals(msg)) report = true; // NOI18N
            else report = false;
        }
        
        private LogRecord getUserData(){
            LogRecord userData;
            ExceptionsSettings settings = new ExceptionsSettings();
            ArrayList<String> params = new ArrayList<String>();
            params.add(getOS());
            params.add(getVM());
            params.add(getVersion());
            reportPanel.saveUserName();
            params.add(settings.getUserName());
            if (reportPanel != null){
                params.add(reportPanel.getSummary());
                params.add(reportPanel.getComment());
            }
            userData = new LogRecord(Level.CONFIG, USER_CONFIGURATION);
            userData.setResourceBundle(NbBundle.getBundle(Installer.class));
            userData.setParameters(params.toArray());
            return userData;
        }
        
        private String getOS(){
            String unknown = "unknown";                                   // NOI18N
            String str = System.getProperty("os.name", unknown)+", "+     // NOI18N
                    System.getProperty("os.version", unknown)+", "+       // NOI18N
                    System.getProperty("os.arch", unknown);               // NOI18N
            return str;
        }
        
        private String getVersion(){
            String str = MessageFormat.format(
                    NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), // NOI18N
                    new Object[] {System.getProperty("netbeans.buildnumber")});                         // NOI18N
            return str;
        }
        
        private String getVM(){
            return System.getProperty("java.vm.name", "unknown") + ", " + System.getProperty("java.vm.version", ""); // NOI18N
        }
        
        public void doShow() {
            Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
            for (Deactivated a : Lookup.getDefault().lookupAll(Deactivated.class)) {
                a.deactivated(log);
            }
            if (report) {
                dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "ErrorDialogTitle"));
            } else {
                dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "MSG_SubmitDialogTitle"));
            }

            exitMsg = NbBundle.getMessage(Installer.class, "MSG_" + msg + "_EXIT"); // NOI18N
            for (;;) {
                try {
                    if (url == null) {
                        String uri = NbBundle.getMessage(Installer.class, msg);
                        if (uri == null || uri.length() == 0) {
                            okToExit = true;
                            return;
                        }
                        url = new URL(uri); // NOI18N
                    }
                    
                    URLConnection conn = url.openConnection();
                    File tmp = File.createTempFile("uigesture", ".html");
                    tmp.deleteOnExit();
                    FileOutputStream os = new FileOutputStream(tmp);
                    FileUtil.copy(conn.getInputStream(), os);
                    os.close();
                    conn.getInputStream().close();
                    InputStream is = new FileInputStream(tmp);
                    parseButtons(is, exitMsg, dd);
                    is.close();
                    url = tmp.toURI().toURL();
                } catch (ParserConfigurationException ex) {
                    LOG.log(Level.WARNING, null, ex);
                } catch (SAXException ex) {
                    LOG.log(Level.WARNING, url.toExternalForm(), ex);
                } catch (java.net.SocketTimeoutException ex) {
                    LOG.log(Level.INFO, url.toExternalForm(), ex);
                    url = getClass().getResource("UnknownHostException.html");
                    msg = null;
                    continue;
                } catch (UnknownHostException ex) {
                    LOG.log(Level.INFO, url.toExternalForm(), ex);
                    url = getClass().getResource("UnknownHostException.html");
                    msg = null;
                    continue;
                } catch (NoRouteToHostException ex) {
                    LOG.log(Level.INFO, url.toExternalForm(), ex);
                    url = getClass().getResource("UnknownHostException.html");
                    msg = null;
                    continue;
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, url.toExternalForm(), ex);
                }
                break;
            }
            Mutex.EVENT.readAccess(this);
        }
        
        public Void run() {
            if ("ERROR_URL".equals(msg)){   // NOI18N
                if (reportPanel==null) reportPanel = new ReportPanel();
                Throwable t = getThrown();
                assert t!= null : "NO THROWABLE FOUND";  // NOI18N
                String summary = t.getClass().getName();
                String[] pieces = summary.split("\\.");
                if (pieces.length > 0) summary = pieces[pieces.length-1];//posledni piece
                if (t.getMessage()!= null)summary = summary.concat(" : " + t.getMessage()); //NOI18N
                reportPanel.setSummary(summary);
                dd.setMessage(reportPanel);
            }else{
                browser = new HtmlBrowser();
                browser.setURL(url);
                browser.setEnableLocation(false);
                browser.setEnableHome(false);
                browser.setStatusLineVisible(false);
                browser.setToolbarVisible(false);
                browser.setPreferredSize(new Dimension(640, 480));
                dd.setMessage(browser);
                
                //        AbstractNode root = new AbstractNode(new Children.Array());
                //        root.setName("root"); // NOI18N
                //        root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
                //        root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
                //        for (LogRecord r : recs) {
                //            root.getChildren().add(new Node[] { UINode.create(r) });
                //        }
                //
                //        panel.getExplorerManager().setRootContext(root);
                
            }
            dd.setClosingOptions(new Object[] { exitMsg });
            dd.setButtonListener(this);
            dd.setModal(true);
            d = DialogDisplayer.getDefault().createDialog(dd);
            d.setVisible(true);
            
            Object res = dd.getValue();
            
            if (res == exitMsg) {
                okToExit = true;
            }
            
            return null;
        }
        
        
        public void actionPerformed(ActionEvent e) {
            URL[] url = new URL[1];
            String actionURL = decodeButtons(e.getSource(), url);
            
            if ("submit".equals(e.getActionCommand())) { // NOI18N
                List<LogRecord> recs = getLogs();
                if (report) reportPanel.saveUserName();
                recs.add(getUserData());
                URL nextURL = null;
                try {
                    nextURL = uploadLogs(url[0], findIdentity(), Collections.<String,String>emptyMap(), recs);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
                if (nextURL != null) {
                    clearLogs();
                    HtmlBrowser.URLDisplayer.getDefault().showURL(nextURL);
                    okToExit = false;
                    // this should close the descriptor
                    dd.setValue(DialogDescriptor.CLOSED_OPTION);
                    d.setVisible(false);
                }
                return;
            }
            
            if ("view-data".equals(e.getActionCommand())) { // NOI18N
                if (panel == null) {
                    panel = new SubmitPanel();
                    AbstractNode root = new AbstractNode(new Children.Array());
                    root.setName("root"); // NOI18N
                    List<LogRecord> recs = getLogs();
                    recs.add(getUserData());
                    root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
                    root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
                    LinkedList<Node> reverted = new LinkedList<Node>();
                    for (LogRecord r : recs) {
                        reverted.addFirst(UINode.create(r));
                        panel.addRecord(r);
                    }
                    root.getChildren().add(reverted.toArray(new Node[0]));
                    panel.getExplorerManager().setRootContext(root);
                }
                
                if (report) {
                    if (dd.getMessage() == reportPanel) {
                        dd.setMessage(panel);
                    } else {
                        dd.setMessage(reportPanel);
                    }
                } else {
                    if (dd.getMessage() == browser) {
                        dd.setMessage(panel);
                    } else {
                        dd.setMessage(browser);
                    }
                }
                if (e.getSource() instanceof AbstractButton) {
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
            
            if ("never-again".equals(e.getActionCommand())) { // NOI18N
                LOG.log(Level.FINE, "Assigning ask.never.again.{0} to true", msg); // NOI18N
                NbPreferences.forModule(Installer.class).putBoolean("ask.never.again." + msg, true); // NOI18N
                okToExit = true;
                // this should close the descriptor
                dd.setValue(DialogDescriptor.CLOSED_OPTION);
                d.setVisible(false);
                return;
            }
            
            if ("exit".equals(e.getActionCommand())) {
                // this should close the descriptor
                dd.setValue(DialogDescriptor.CLOSED_OPTION);
                d.setVisible(false);
                return;
            }
            
        }
    } // end Submit
}
