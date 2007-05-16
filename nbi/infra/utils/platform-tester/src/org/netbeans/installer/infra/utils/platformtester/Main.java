/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.infra.utils.platformtester;

import java.util.Properties;
import java.util.TreeSet;

/**
 *
 * @author ks152834
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final Properties properties = System.getProperties();
        
        System.out.println(
                "os.name => " + properties.get("os.name")); // NOI18N
        System.out.println(
                "os.arch => " + properties.get("os.arch")); // NOI18N
        System.out.println(
                "os.version => " + properties.get("os.version")); // NOI18N
        
        System.out.println(
                ""); // NOI18N
        System.out.println(
                "---------------------------------------------"); // NOI18N
        System.out.println(
                "Other properties:"); // NOI18N
        System.out.println(
                ""); // NOI18N
        
        for (Object key: new TreeSet<Object> (properties.keySet())) {        
            System.out.println(key.toString() + 
                    " => " + properties.get(key).toString()); // NOI18N
        }
    }
}
