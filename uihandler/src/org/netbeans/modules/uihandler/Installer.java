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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.lib.uihandler.LogRecords;
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
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Registers and unregisters loggers.
 */
public class Installer extends ModuleInstall {
    private static Queue<LogRecord> logs = new LinkedList<LogRecord>();
    private static UIHandler ui = new UIHandler(logs, false);
    private static UIHandler handler = new UIHandler(logs, true);
    static final Logger LOG = Logger.getLogger(Installer.class.getName());
        
    
    
    public void restored() {
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        log.setLevel(Level.FINEST);
        log.addHandler(ui);
        Logger all = Logger.getLogger("");
        all.addHandler(handler);
        
        if (Integer.getInteger("netbeans.exception.report.min.level", 0).intValue() < 1000) {
            System.setProperty("netbeans.exception.report.min.level", "1001");
        }
        
        
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
    
    public void close() {
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        log.removeHandler(ui);
        Logger all = Logger.getLogger(""); // NOI18N
        all.removeHandler(handler);
    }
    
    public static List<LogRecord> getLogs() {
        synchronized (UIHandler.class) {
            return new ArrayList<LogRecord>(logs);
        }
    }
    
    private static void clearLogs() {
        synchronized (UIHandler.class) {
            logs.clear();
        }
    }
    
    public boolean closing() {
        if (getLogs().isEmpty()) {
            return true;
        }
        
        return displaySummary("WELCOME_URL"); // NOI18N
    }
    
    private static ThreadLocal<Object> DISPLAYING = new ThreadLocal<Object>();
    static boolean displaySummary(String msg) {
        if (DISPLAYING.get() != null) {
            return true;
        }
        
        boolean dontAsk = NbPreferences.forModule(Installer.class).getBoolean("ask.never.again." + msg, false); // NOI18N
        if (dontAsk) {
            LOG.log(Level.INFO, "UI Gesture Collector's ask.never.again.{0} is true, exiting", msg); // NOI18N
            return true;
        }
        
        
        boolean v = true;
        try {
            DISPLAYING.set(msg);
            v = doDisplaySummary(msg);
        } finally {
            DISPLAYING.set(null);
        }
        return v;
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
    static Object[] parseButtons(InputStream is, Object defaultButton) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);

        List<Object> buttons = new ArrayList<Object>();
        
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
                    
                    if ("hidden".equals(type) && "submit".equals(name)) { // NOI18N
                        f.submitValue = value;
                        JButton b = new JButton();
                        Mnemonics.setLocalizedText(b, f.submitValue);
                        b.setActionCommand("submit"); // NOI18N
                        b.putClientProperty("url", f.url); // NOI18N
                        b.setDefaultCapable(buttons.isEmpty());
                        buttons.add(b);
                        continue;
                    }
                    
                    if ("hidden".equals(type)) { // NOI18N
                        JButton b = new JButton();
                        Mnemonics.setLocalizedText(b, value);
                        b.setActionCommand(name);
                        b.setDefaultCapable(buttons.isEmpty());
                        buttons.add(b);
                    }
                }
            }
        }
        if (defaultButton != null) {
            buttons.add(defaultButton);
        }
        return buttons.toArray();
    }
    
    static String decodeButtons(Object res, URL[] url) {
        if (res instanceof JButton) {
            JButton b = (JButton)res;
            Object post = b.getClientProperty("url"); // NOI18N
            if (post instanceof String) {
                try {
                    url[0] = new URL((String) post);
                } catch (MalformedURLException ex) {
                    url[0] = null;
                }
            }
            return b.getActionCommand();
        }
        return (String)res;
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
    
    private static final class Submit implements ActionListener {
        private String msg;
        boolean okToExit;
        private DialogDescriptor dd;
        private Dialog d;
        private SubmitPanel panel;
        
        public Submit(String msg) {
            this.msg = msg;
        }
        
        public void doShow() {
            Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
            for (Deactivated a : Lookup.getDefault().lookupAll(Deactivated.class)) {
                a.deactivated(log);
            }

            String exitMsg = NbBundle.getMessage(Installer.class, "MSG_EXIT"); // NOI18N
            URL url = null;
            Object[] buttons = new Object[] { exitMsg };
            try {
                String uri = NbBundle.getMessage(Installer.class, msg);
                if (uri != null && uri.length() > 0) {
                    url = new URL(uri); // NOI18N
                    URLConnection conn = url.openConnection();
                    conn.setReadTimeout(2000);
                    File tmp = File.createTempFile("uigesture", ".html");
                    tmp.deleteOnExit();
                    FileOutputStream os = new FileOutputStream(tmp);
                    FileUtil.copy(conn.getInputStream(), os);
                    os.close();
                    conn.getInputStream().close();
                    InputStream is = new FileInputStream(tmp);
                    Object[] newB = parseButtons(is, exitMsg);
                    if (newB != null) {
                        buttons = newB;
                    }
                    is.close();
                    url = tmp.toURI().toURL();
                } else {
                    okToExit = true;
                    return;
                }
            } catch (ParserConfigurationException ex) {
                LOG.log(Level.WARNING, null, ex);
            } catch (SAXException ex) {
                LOG.log(Level.WARNING, url.toExternalForm(), ex);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, url.toExternalForm(), ex);
            }

            List<LogRecord> recs = getLogs();
            
            
            
            HtmlBrowser browser = new HtmlBrowser();
            browser.setURL(url);
            browser.setEnableLocation(false);
            browser.setEnableHome(false);
            browser.setStatusLineVisible(false);
            browser.setToolbarVisible(false);

            //        AbstractNode root = new AbstractNode(new Children.Array());
            //        root.setName("root"); // NOI18N
            //        root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
            //        root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
            //        for (LogRecord r : recs) {
            //            root.getChildren().add(new Node[] { UINode.create(r) });
            //        }
            //
            //        panel.getExplorerManager().setRootContext(root);

            dd = new DialogDescriptor(browser, NbBundle.getMessage(Installer.class, "MSG_SubmitDialogTitle"));
            dd.setOptions(buttons);
            dd.setClosingOptions(new Object[] { exitMsg });
            dd.setButtonListener(this);
            dd.setModal(true);
            d = DialogDisplayer.getDefault().createDialog(dd);
            d.setVisible(true);
            
            Object res = dd.getValue();

            if (res == exitMsg) {
                okToExit = true;
            }
        }
    
    
        public void actionPerformed(ActionEvent e) {
            URL[] url = new URL[1];
            String actionURL = decodeButtons(e.getSource(), url);

            if ("submit".equals(e.getActionCommand())) { // NOI18N
                List<LogRecord> recs = getLogs();
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
                    root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
                    root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
                    for (LogRecord r : recs) {
                        root.getChildren().add(new Node[] { UINode.create(r) });
                        panel.addRecord(r);
                    }
                    panel.getExplorerManager().setRootContext(root);
                }
                dd.setMessage(panel);
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

        }
    } // end Submit
}
