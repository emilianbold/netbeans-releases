/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform.wizard;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class SDKProbe {
    public static void main(String[] args) {
        Properties p = System.getProperties();

        File f = new File(args[0]);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            p.store(fos, null);
            fos.close();
        } catch (Exception exc) {
            //PENDING
            exc.printStackTrace();
        }
    }    
}
