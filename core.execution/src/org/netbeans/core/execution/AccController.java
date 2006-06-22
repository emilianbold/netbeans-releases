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

package org.netbeans.core.execution;

import java.security.ProtectionDomain;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PermissionCollection;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Tries to get an IOProtectionDomain from an AccessControlContext.
*
* @author Ales Novak
*/
class AccController {

    /** array of ProtectionDomains */
    static Field context;

    static Field getContextField() throws Exception {
        if (context == null) {
            Field ctx;
            try {
                ctx = AccessControlContext.class.getDeclaredField("context"); // NOI18N
            } catch (NoSuchFieldException nsfe) { // IBM JDK1.5 has different field
                ctx = AccessControlContext.class.getDeclaredField("domainsArray"); // NOI18N
            }
            ctx.setAccessible(true);
            context = ctx;
        }
        return context;
    }


    static ProtectionDomain[] getDomains(AccessControlContext acc) throws Exception {
        Object o = getContextField().get(acc);
        if (o.getClass() == Object[].class) { // 1.2.1 fix
            Object[] array = (Object[]) o;
            ProtectionDomain[] domains = new ProtectionDomain[array.length];
            for (int i = 0; i < array.length; i++) {
                domains[i] = (ProtectionDomain) array[i];
            }
            return domains;
        }
        return (ProtectionDomain[]) o;
    }

    /** @return an IOPermissionCollection or <tt>null</tt> if not found */
    static IOPermissionCollection getIOPermissionCollection() {
        return getIOPermissionCollection(AccessController.getContext());
    }
    
    /** @return an IOPermissionCollection or <tt>null</tt> if not found */
    static IOPermissionCollection getIOPermissionCollection(AccessControlContext acc) {
        try {
            ProtectionDomain[] pds = getDomains(acc);
            PermissionCollection pc;
            for (int i = 0; i < pds.length; i++) {
                pc = pds[i].getPermissions();
                if (pc instanceof IOPermissionCollection) {
                    return (IOPermissionCollection) pc;
                }
            }
            return null;
        } catch (final Exception e) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Logger.global.log(Level.WARNING, null, e);
                }
            });
            return null;
        }
    }
}
