/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.util.Iterator;
import java.util.Arrays;
import org.netbeans.editor.Settings;
import org.openide.options.ContextSystemOption;
import org.openide.options.SystemOption;

/**
 * Listener that adds/removes the initializers corresponding
 * to the options to the Settings and performs the resetting
 * of the Settings.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
class ContextOptionsListener implements BeanContextMembershipListener {
    
    /** Whether debug messages should be displayed */
    private static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.options"); // NOI18N
    
    /** Only one shared instance is used. The side-effect advantage is
     * that BeanContextSupport will not add the same listener twice.
     */
    private static final ContextOptionsListener sharedListener
        = new ContextOptionsListener();

    /** Process the existing options added to the context system option
     * and start listening on the changes.
     */
    static void processExistingAndListen(ContextSystemOption cso) {
        BeanContextChild bcc = cso.getBeanContextProxy();
        
        // Start listening first
        if (bcc instanceof BeanContext) {
            ((BeanContext)bcc).addBeanContextMembershipListener(sharedListener);
        }
        
        // Process all the currently added options
        SystemOption[] sos = cso.getOptions();
        if (sos != null) {
            sharedListener.processInitializers(Arrays.asList(sos).iterator(), false);
        }
    }
    
    private ContextOptionsListener() {
    }

    public void childrenAdded(BeanContextMembershipEvent bcme) {
        processInitializers(bcme.iterator(), false);
    }

    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        processInitializers(bcme.iterator(), true);
    }

    private void processInitializers(Iterator it, boolean remove) {
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof OptionSupport) {
                OptionSupport os = (OptionSupport)o;
                Settings.Initializer si = os.getSettingsInitializer();
                // Remove the old one
                Settings.removeInitializer(si.getName());
                if (!remove) { // add the new one
                    Settings.addInitializer(si, Settings.OPTION_LEVEL);
                }

                if (debug) {
                    System.err.println((remove ? "Removed" : "Refreshed") // NOI18N
                        + " initializer=" + si.getName()); // NOI18N
                }
            }
        }
        
        /* Reset the settings so that the new initializers take effect
         * or the old are removed.
         */
        Settings.reset();
    }

}
