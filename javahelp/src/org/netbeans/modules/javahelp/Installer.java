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

package org.netbeans.modules.javahelp;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.netbeans.api.javahelp.Help;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    public static final Logger log = Logger.getLogger("org.netbeans.modules.javahelp"); // NOI18N

    public void restored() {
        log.fine("restored module");
        // This ensures the static block will be called ASAP, hence that
        // the AWT listener will actually be started quickly and there
        // will already have been interesting mouse-entered events
        // by the time F1 is first pressed. Otherwise only the second
        // F1 actually gets anything other than the main window help.
        HelpAction.WindowActivatedDetector.install();

        // XXX(-ttran) quick fix for #25470: Help viewer frozen on first open
        // over modal dialogs.  JavaHelp seems to try to be lazy with the
        // installation of its Dialog detector (an AWTEventListener) but it
        // doesn't work on Windows.  Here we force JavaHelp instance to be
        // created and thus its AWTEventListener be registered early enough.
        
        getDefaultHelp();
    }
    
    public void uninstalled() {
        log.fine("uninstalled module");
        if (help != null) {
            help.deactivate();
        }
        HelpAction.WindowActivatedDetector.uninstall();
        // UIManager is too aggressive about caching, and we get CCE's,
        // since JavaHelp's HelpUtilities sets up these defaults, and UIManager
        // caches the actual classes (probably incorrectly). #4675772
        cleanDefaults(UIManager.getDefaults());
        cleanDefaults(UIManager.getLookAndFeelDefaults());
    }
    private static void cleanDefaults(UIDefaults d) {
        Set<Object> badKeys = new HashSet<Object>(10);
        Iterator<Map.Entry<Object, Object>> it = d.entrySet().iterator();
        ClassLoader aboutToDie = Installer.class.getClassLoader();
        while (it.hasNext()) {
            Map.Entry<Object, Object> e;
            try {
                e = it.next();
            } catch (ConcurrentModificationException x) {
                // Seems to be possible during shutdown. Just skip the hack in this case.
                return;
            }
            Object k = e.getKey();
            Object o = e.getValue();
            if (o instanceof Class) {
                Class c = (Class)o;
                if (c.getClassLoader() == aboutToDie) {
                    badKeys.add(k);
                }
            } else if (k instanceof Class) {
                Class c = (Class)k;
                if (c.getClassLoader() == aboutToDie) {
                    badKeys.add(k);
                }
            }
        }
        if (!badKeys.isEmpty()) {
            log.fine("Cleaning up old UIDefaults keys (JRE bug #4675772): " + badKeys);
            for (Object o: badKeys) {
                d.put(o, null);
            }
        }
    }
    
    private static JavaHelp help = null;
    /** @deprecated only for use from the layer */
    public static synchronized Help getDefaultHelp() {
        // Does not work to use Lookup: help set processors called too early.
        if (help == null) {
            help = new JavaHelp();
        }
        return help;
    }

}
