/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import org.openide.util.*;
import org.openide.ErrorManager;

/**
 *
 * @author Tran Duc Trung
 */

class FormLAF {

    private static Map lafDefaults;
    private static Map ideDefaults;
    private static int useIdeLaf = -1;
    private static boolean lafBlockEntered;

    private FormLAF() {}

    static Object executeWithLookAndFeel(final Mutex.ExceptionAction act)
        throws Exception
    {
        try {
            if (checkUseIdeLaf())
                return Mutex.EVENT.readAccess(act);
            else
                return Mutex.EVENT.readAccess(new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        // FIXME(-ttran) needs to hold a lock on UIDefaults to
                        // prevent other threads from creating Swing components
                        // in the mean time
                        synchronized (UIManager.getDefaults()) {
                            boolean restoreAfter = true;
                            try {
                                if (lafBlockEntered)
                                    restoreAfter = false;
                                else {
                                    lafBlockEntered = true;
                                    useDefaultLookAndFeel();
                                    restoreAfter = true;
                                }
                                return act.run();
                            }
                            finally {
                                if (restoreAfter) {
                                    useIDELookAndFeel();
                                    lafBlockEntered = false;
                                }
                            }
                        }
                    }
                });
        }
        catch (MutexException ex) {
            throw ex.getException();
        }
    }

    static void executeWithLookAndFeel(final Runnable run) {
        Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                if (checkUseIdeLaf())
                    run.run();
                else {
                    // FIXME(-ttran) needs to hold a lock on UIDefaults to
                    // prevent other threads from creating Swing components
                    // in the mean time
                    synchronized (UIManager.getDefaults()) {
                        boolean restoreAfter = true;
                        try {
                            if (lafBlockEntered)
                                restoreAfter = false;
                            else {
                                lafBlockEntered = true;
                                useDefaultLookAndFeel();
                                restoreAfter = true;
                            }
                            run.run();
                        }
                        finally {
                            if (restoreAfter) {
                                useIDELookAndFeel();
                                lafBlockEntered = false;
                            }
                        }
                    }
                }
                return null;
            }
        });
    }

    private static boolean checkUseIdeLaf() {
        if (useIdeLaf == -1) {
            if (System.getProperty("netbeans.form.use_idelaf") != null) // NOI18N
                useIdeLaf = 1;
            else
                useIdeLaf = 0;
        }
        return useIdeLaf > 0;
    }
    
    private static void useDefaultLookAndFeel() {
        if (lafDefaults == null) {
            try {
                String lafName = UIManager.getLookAndFeel().getClass().getName();
                LookAndFeel defaultLookAndFeel =
                    (LookAndFeel) Class.forName(lafName).newInstance();
                defaultLookAndFeel.initialize();

                // call src.get() on each key to force LazyValues to be init'ed
                // see javax.swing.UIDefaults to see why
                lafDefaults = defaultLookAndFeel.getDefaults();
                Object[] keys = lafDefaults.keySet().toArray();
                for (int i=0; i < keys.length; i++) {
                    // Do not resolve icons - some L&Fs don't provide icons for all keys (see #44482)
                    if (!(keys[i] instanceof String) || (((String)keys[i])).indexOf("Icon") == -1) { // NOI18N
                        lafDefaults.get(keys[i]);
                    }
                }
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return;
            }
            catch (LinkageError ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return;
            }
        }

        if (ideDefaults == null)
            saveIDELookAndFeelDefaults();

        copyMap(UIManager.getDefaults(), lafDefaults);
    }

    private static void useIDELookAndFeel() {
        if (ideDefaults != null)
            copyMap(UIManager.getDefaults(), ideDefaults);
    }

    private static void saveIDELookAndFeelDefaults() {
        if (checkUseIdeLaf())
            return;
        
        if (ideDefaults != null)
            return;

        UIDefaults defaults = UIManager.getDefaults();
        UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();
        
        ideDefaults = new HashMap(defaults.size() + lafDefaults.size());
        copyMap(ideDefaults, lafDefaults);
        copyMap(ideDefaults, defaults);
    }

    private static void copyMap(Map dest, Map src) {
        dest.putAll(src);
    }
}
