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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
        
    
    
    public void restored() {
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
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
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        for (Deactivated a : Lookup.getDefault().lookupAll(Deactivated.class)) {
            a.deactivated(log);
        }
        
        String exitMsg = NbBundle.getMessage(Installer.class, "MSG_EXIT"); // NOI18N
        URL url = null;
        Object[] buttons = new Object[] { exitMsg };
        try {
            String uri = NbBundle.getMessage(SubmitPanel.class, "WELCOME_URL");
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
            Logger.getAnonymousLogger().log(Level.WARNING, null, ex);
        } catch (SAXException ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, null, ex);
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
                URL nextURL = uploadLogs(postURL, recs);
                if (nextURL != null) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(nextURL);
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
                    
                    if ("hidden".equals(type) && "submitAndExit".equals(name)) { // NOI18N
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

    private URL uploadLogs(URL postURL, List<LogRecord> recs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    static final class Form extends Object {
        final String url;
        String submitValue;
        
        public Form(String u) {
            url = u;
        }
    }
}
