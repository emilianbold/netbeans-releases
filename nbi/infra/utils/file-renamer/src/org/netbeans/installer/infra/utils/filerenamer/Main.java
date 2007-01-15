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
 *
 * $Id$
 */
package org.netbeans.installer.infra.utils.filerenamer;

import java.io.File;

/**
 *
 * @author Kirill Sorokin
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final File file = new File("D:/temp/data");
        
        final String token       = "_";
        final String replacement = ",";
        
        for (File source: file.listFiles()) {
            if (source.isFile()) {
                System.out.println(source);
                
                final String name   = source.getName();
                final File   target = new File(
                        source.getParentFile(), 
                        name.replace(token, replacement));
                
                System.out.println("    ... " + target);
                source.renameTo(target);
            }
        }
    }
    
}
