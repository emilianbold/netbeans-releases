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

// XXX is this really an appropriate package? Perhaps move to e.g. org.netbeans.jnlplauncher.
package org.netbeans.modules.apisupport.jnlplauncher;

import java.io.File;
import java.security.Policy;

/** The JNLP entry point. Does not do much, in future it can do more
 * of JNLP related stuff.
 *
 * @author Jaroslav Tulach
 */
public class Main extends Object {

    /** Starts NetBeans
     * @param args the command line arguments
     * @throws Exception for lots of reasons
     */
    public static void main (String args[]) throws Exception {
        fixPolicy();
        fixNetBeansUser();
        org.netbeans.Main.main(args);
    }
    
    /** Fixes value of netbeans.user property.
     */
    final static void fixNetBeansUser() {
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        if (userDir == null) {
            return;
        }
        final String PREFIX = "${user.home}/"; // NOI18N
        int uh = userDir.indexOf(PREFIX);
        if (uh == -1) {
            return;
        }
        String newDir = 
            userDir.substring(0, uh) + 
            System.getProperty("user.home") + // NOI18N
            File.separator + 
            userDir.substring(uh + PREFIX.length()); 
        System.setProperty("netbeans.user", newDir); // NOI18N
    }
    
    /**
     * Attempt to give the rest of NetBeans all the
     * permissions. The jars besides the one containing this class
     * don't have to be signed with this.
     */
    final static void fixPolicy() {
        if (Boolean.getBoolean("netbeans.jnlp.fixPolicy")) { // NOI18N
            // Grant all the code all persmission
            Policy.setPolicy(new RuntimePolicy());
            // Replace the security manager by a fresh copy
            // that does the delegation to the permissions system
            // -- just to make sure that there is nothing left
            //    from the JWS
            System.setSecurityManager(new SecurityManager());
        }
    }
}
