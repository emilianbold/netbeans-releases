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
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

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
            url = new URL(NbBundle.getMessage(SubmitPanel.class, "WELCOME_URL")); // NOI18N
            InputStream is = url.openStream();
            Object[] newB = parseButtons(is, exitMsg);
            if (newB != null) {
                buttons = newB;
            }
            is.close();
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
        
        return res == exitMsg;
    }
    
    /** Tries to parse a list of buttons provided by given page.
     * @param u the url to read the page from
     * @param defaultButton the button to add always to the list
     */
    static Object[] parseButtons(InputStream is, Object defaultButton) throws IOException {
        byte[] arr = new byte[4096];
        int len = is.read(arr);
        String page = new String(arr, 0, len);
        
        Matcher m = Pattern.compile(
            "<form\\p{Space}+action=", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        ).matcher(page); // NOI18N
        
        if (!m.find()) {
            return null;
        }
        
        Matcher url = Pattern.compile("['\"]([^'\"]*)['\"]").matcher(page);
        if (!url.find(m.end())) {
            Logger.getAnonymousLogger().warning("No action URL:\n" + page); // NOI18N
            return null;
        }
        
        Matcher action = Pattern.compile(
            "INPUT.*TYPE=SUBMIT.*VALUE=['\"]([^'\"]*)['\"].*</form>", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
        ).matcher(page);
        if (!action.find(url.end())) {
            Logger.getAnonymousLogger().warning("No submit button:\n" + page.substring(url.end())); // NOI18N
            return null;
        }
        
        JButton b = new JButton();
        Mnemonics.setLocalizedText(b, action.group(1));
        b.putClientProperty("url", url.group(1));
        
        return new Object[] { b, defaultButton };
    }
}
