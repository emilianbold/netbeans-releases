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
package org.netbeans.installer.infra.utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author Kirill Sorokin
 */
public class Main {
    public static void main(String[] args) {
        for (String arg: args) {
            if (arg.matches("[0-9]+")) {
                occupy(Integer.parseInt(arg));
            }
            
            if (arg.matches("[0-9]+-[0-9]+")) {
                int start = Integer.parseInt(arg.substring(0, arg.indexOf("-")));
                int end = Integer.parseInt(arg.substring(arg.indexOf("-") + 1));
                
                for (int port = start; port <= end; port++) {
                    occupy(port);
                }
            }
        }
    }
    
    private static void occupy(final int port) {
        new Thread() {
            public void run() {
                try {
                    System.out.println("occupying: " + port);
                    new ServerSocket(port).accept();
                } catch (IOException e) {
                    System.out.println("    failed: " + port + " (" + e.getMessage() + ")");
                }
            }
        }.start();
    }
}
