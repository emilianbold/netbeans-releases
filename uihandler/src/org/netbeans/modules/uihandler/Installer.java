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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
    private static final Logger LOG = Logger.getLogger(Installer.class.getName());
        
    
    
    public void restored() {
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        log.setLevel(Level.FINEST);
        log.addHandler(ui);
        Logger all = Logger.getLogger("");
        all.addHandler(handler);
        
        if (Integer.getInteger("netbeans.exception.report.min.level", 0).intValue() < 1000) {
            System.setProperty("netbeans.exception.report.min.level", "1001");
        }
        
        
        Lookup.Template/*GENERICS<Activated>GENERICS*/ temp = new Lookup.Template/*GENERICS<Activated>GENERICS*/(Activated.class);
        Lookup.Result/*GENERICS<Activated>GENERICS*/ res = Lookup.getDefault().lookup(temp);
        for (Object o : res.allInstances()) {
            Activated a = (Activated)o;
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
    
    public boolean closing() {
        return displaySummary("WELCOME_URL"); // NOI18N
    }
    
    private static ThreadLocal<Object> DISPLAYING = new ThreadLocal<Object>();
    static boolean displaySummary(String msg) {
        if (DISPLAYING.get() != null) {
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
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        Lookup.Template/*GENERICS<Deactivated>GENERICS*/ temp = new Lookup.Template/*GENERICS<Deactivated>GENERICS*/(Deactivated.class);
        Lookup.Result/*GENERICS<Deactivated>GENERICS*/ result = Lookup.getDefault().lookup(temp);
        for (Object o : result.allInstances()) {
            Deactivated a = (Deactivated)o;
            a.deactivated(log);
        }
        
        String exitMsg = NbBundle.getMessage(Installer.class, "MSG_EXIT"); // NOI18N
        URL url = null;
        Object[] buttons = new Object[] { exitMsg };
        try {
            String uri = NbBundle.getMessage(SubmitPanel.class, msg);
            if (uri != null && uri.length() > 0) {
                url = new URL(uri); // NOI18N
                InputStream is = url.openStream();
                Object[] newB = parseButtons(is, exitMsg);
                if (newB != null) {
                    buttons = newB;
                }
                is.close();
            }
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.WARNING, null, ex);
        } catch (SAXException ex) {
            LOG.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        
        List<LogRecord> recs = getLogs();

        SubmitPanel panel = new SubmitPanel(url);
        
        AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("root"); // NOI18N
        root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
        root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
        for (LogRecord r : recs) {
            root.getChildren().add(new Node[] { UINode.create(r) });
        }
        
        panel.getExplorerManager().setRootContext(root);
        
        NotifyDescriptor dd = new NotifyDescriptor.Message(panel, NotifyDescriptor.INFORMATION_MESSAGE);
        dd.setOptions(buttons);
        Object res = DialogDisplayer.getDefault().notify(dd);
        
        if (res instanceof JButton) {
            JButton b = (JButton)res;
            Object post = b.getClientProperty("url"); // NOI18N
            if (post instanceof String) {
                URL postURL;
                try {
                    postURL = new URL((String) post);
                } catch (MalformedURLException ex) {
                    postURL = null;
                }
                URL nextURL = null;
                try {
                    nextURL = uploadLogs(postURL, Collections.<String,String>emptyMap(), recs);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
                if (nextURL != null) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(nextURL);
                    return false;
                }
            }
            return true;
        }
        
        return res == exitMsg;
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
                    }
                }
            }

            JButton b = new JButton();
            Mnemonics.setLocalizedText(b, f.submitValue);
            b.putClientProperty("url", f.url);
            b.setDefaultCapable(buttons.isEmpty());
            buttons.add(b);
        }
        buttons.add(defaultButton);
        return buttons.toArray();
    }

    static URL uploadLogs(URL postURL, Map<String,String> attrs, List<LogRecord> recs) throws IOException {
        URLConnection conn = postURL.openConnection();
        
        conn.setDoOutput(true);
        conn.setDoInput(true);

        PrintStream os = new PrintStream(conn.getOutputStream());
        
        os.println("POST " + postURL.getPath() + " HTTP/1.1");
        os.println("Pragma: no-cache");
        os.println("Cache-control: no-cache");
        os.println("Content-Type: multipart/form-data; boundary=----------konecbloku");
        os.println();
        
        for (Map.Entry<String, String> en : attrs.entrySet()) {
            os.println("----------konecbloku");
            os.println("Content-Disposition: form-data; name=\"" + en.getKey() + "\"");
            os.println();
            os.println(en.getValue().getBytes());
        }
        
        os.println("----------konecbloku");
        os.println("Content-Disposition: form-data; name=\"logs\"");
        os.println("Content-Type: x-application/gzip");
        os.println();
/*        GZIPOutputStream gzip = new GZIPOutputStream(os);
        for (LogRecord r : recs) {
        }
        gzip.finish();
 */
        ObjectOutputStream oos = new ObjectOutputStream(os);
        for (LogRecord r: recs) {
            oos.writeObject(r);
        }
        oos.flush();
        os.println("----------konecbloku--");
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
        
        Pattern p = Pattern.compile("<meta *http-equiv=.Refresh. *content.*url=['\"]?([^'\" ]*) *['\"]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(redir);
        
        if (m.find()) {
            return new URL(m.group(1));
        } else {
            return null;
        }
    }
    
    static final class Form extends Object {
        final String url;
        String submitValue;
        
        public Form(String u) {
            url = u;
        }
    }
}
