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

package org.netbeans;

import java.util.ResourceBundle;
import javax.swing.JOptionPane;

/** Bootstrap main class.
 * @author Jaroslav Tulach, Jesse Glick
 */
public final class Main extends Object {
    private Main() {
    }

    /** Starts the NetBeans system.
     * @param args the command line arguments
     * @throws Exception for lots of reasons
     */
    public static void main (String args[]) throws Exception {
        // following code has to execute without java5 - e.g. do not use
        // NbBundle or any other library compiled against java5 only
        // also prevent usage of java5 methods and classes
        try {
            Class.forName("java.lang.StringBuilder"); // NOI18N
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(
                null,
                ResourceBundle.getBundle("org.netbeans.Bundle").getString("MSG_InstallJava5"),
                ResourceBundle.getBundle("org.netbeans.Bundle").getString("MSG_NeedsJava5"),
                JOptionPane.WARNING_MESSAGE
            );
            System.exit(10);
        }
        // end of java5 only code

        MainImpl.main(args);
    }
    
    /** Returns string describing usage of the system. Does that by talking to
     * all registered handlers and asking them to show their usage.
     *
     * @return the usage string for the system
     */
    public static String usage () throws Exception {
        return MainImpl.usage();
    }
        
    
    /**
     * Call when the system is up and running, to complete handling of
     * delayed command-line options like -open FILE.
     */
    public static void finishInitialization() {
        MainImpl.finishInitialization();
    }
}
