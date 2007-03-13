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
 * Utilities.java
 * Created on November 5, 2004, 5:08 PM
 */
 
package org.netbeans.modules.css;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.JFileChooser;

/**
 * Some useful utilities
 * @author  Winston Prakash
 * @version 1.0
 */
public class Utilities {
    
    /**
     * This does a special instantiation of JFileChooser
     * to workaround floppy access bug 5037322.
     * Using privileged code block.
     */
    public static JFileChooser getJFileChooser() {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                return new JFileChooser() ;
            }
        });
    }
    
    public static JFileChooser getJFileChooser(final String currentDirectoryPath) {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                return new JFileChooser(currentDirectoryPath);
            }
        });
    }
    
    public static JFileChooser getJFileChooser(final java.io.File currentDirectory) {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                return new JFileChooser(currentDirectory) ;
            }
        });
    }
    
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }
        
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }
}
