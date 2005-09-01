/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.config;

import javax.swing.SwingUtilities;

/**
 * Helper class.
 *
 * @author sherold
 */
public final class Utils {

    // !PW These are deliberately not public for now, I don't see any use of them
    // outside this package.  I suppose this premise could change though.
    static final String SERVER_ID_AS81 = "J2EE"; // NOI18N
    static final String SERVER_ID_AS90 = "JavaEE5"; // NOI18N
    
    /**
     * Check that current target server is Sun AppServer.
     */
    public static boolean isSunServer(String serverId) {
        boolean result = false;
        if(SERVER_ID_AS81.equals(serverId) || SERVER_ID_AS90.equals(serverId)) {
            result = true;
        }
        return result;
    }
    
    /**
     * Ensure that the specified ruannable task will run only in the event dispatch 
     * thread.
     */
    public static void runInEventDispatchThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
    
}
