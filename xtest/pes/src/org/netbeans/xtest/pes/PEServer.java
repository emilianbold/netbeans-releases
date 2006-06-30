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

/*
 * PEServer.java
 *
 * Created on May 27, 2002, 10:59 AM
 */

package org.netbeans.xtest.pes;

import java.io.*;
import java.util.logging.Level;

/**
 *
 * @author  mb115822
 */
public class PEServer {


    public static File getXTestHome() throws IOException {
        String xtestHome = System.getProperty("xtest.home");
        if (xtestHome == null) {
            throw new IOException("System property xtest.home is not set");

        }
        return new File(xtestHome);
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            File config = new File(System.getProperty("pes.config",""));
            String command = System.getProperty("pes.command","run");
            String pesHome = System.getProperty("xtest.home");
            
            // version handling !!!
            if (command.equalsIgnoreCase("version")) {
                /// just printout the versino and leave
                System.out.println("XTest PES version "+Version.getVersion(pesHome));
                return;
            }
            
            ServerEngine se = ServerEngine.createServerEngine(config);            
            try {
                se.runCommand(command);
            } catch (IllegalArgumentException iae) {
                    System.err.println("Bad command specified:"+command);
            }
        } catch (InstantiationException ie) {
            System.out.println("Cannot load PES configuration file: "+ie.getMessage());        
        } catch (Exception e) {
            System.out.println("Exception caught when runnig PES");
            e.printStackTrace();
            PESLogger.logger.log(Level.SEVERE,"Exception cautgh when running PES",e);
        }
    }
    
    

    
    
    
}
