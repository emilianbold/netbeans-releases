/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javahelp;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.netbeans.api.javahelp.Help;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {
    
    public static final ErrorManager err =
        ErrorManager.getDefault().getInstance("org.netbeans.modules.javahelp"); // NOI18N

    public void restored() {
        err.log("restored module");
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
        err.log("uninstalled module");
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
        Set badKeys = new HashSet(10); // Set<Object>
        Iterator it = d.entrySet().iterator();
        ClassLoader aboutToDie = Installer.class.getClassLoader();
        while (it.hasNext()) {
            Map.Entry e;
            try {
                e = (Map.Entry) it.next();
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
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("Cleaning up old UIDefaults keys (JRE bug #4675772): " + badKeys);
            }
            it = badKeys.iterator();
            while (it.hasNext()) {
                d.put(it.next(), null);
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
