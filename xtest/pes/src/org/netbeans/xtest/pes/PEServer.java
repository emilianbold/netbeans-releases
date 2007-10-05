/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
