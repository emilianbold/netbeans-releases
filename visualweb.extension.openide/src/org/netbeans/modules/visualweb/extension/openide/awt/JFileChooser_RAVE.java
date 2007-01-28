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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * JFileChooser_RAVE.java
 *
 * Created on June 7, 2004, 10:49 AM
 */


package org.netbeans.modules.visualweb.extension.openide.awt;


import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.JFileChooser ;


/**
 *
 * This does a special instantiation of JFileChooser
 * to workaround floppy access bug 5037322.
 * Using privileged code block.
 *
 * @author  jfbrown
 */
public class JFileChooser_RAVE {

    /** factory methods only */
    private JFileChooser_RAVE() {
    }

    public static JFileChooser getJFileChooser() {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new JFileChooser() ;
            }
        });
    }

    public static JFileChooser getJFileChooser(final String currentDirectoryPath) {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new JFileChooser(currentDirectoryPath);
            }
        });
    }

    public static JFileChooser getJFileChooser(final java.io.File currentDirectory) {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new JFileChooser(currentDirectory) ;
            }
        });
    }

}
