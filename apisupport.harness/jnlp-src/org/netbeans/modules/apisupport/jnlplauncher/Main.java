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

package org.netbeans.modules.apisupport.jnlplauncher;


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
            java.io.File.separator + 
            userDir.substring(uh + PREFIX.length()); 
        System.setProperty("netbeans.user", newDir); // NOI18N
    }
}