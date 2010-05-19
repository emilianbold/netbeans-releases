/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the Nokia Deployment.                      
 * The Initial Developer of the Original Software is Nokia Corporation.
 * Portions created by Nokia Corporation Copyright 2005, 2007.         
 * All Rights Reserved.                                                
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

package com.nokia.phone.deploy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for deployment functionality. Called from deployment script.
 */
public class DeployerMain {

    /**
     * Main method that deploys the given application to the given devices.
     * The device list must be separated by new lines ('\n'), this is to
     * simplify the deployment ant script.
     */
    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("usage: DeployerMain <jad file> <jar file> [list of device names to deploy to]");
            
        } else {
            // Don't continue if OS is not supported
            if (!CONA.getInstance().isOSSupportsDeployment()) {
                System.err.println("Deployment aborted, only supported on Windows operating system");
                System.exit(0);
            }

            // Don't continue if dll's were not found
            if (!CONA.getInstance().isConnAPIDllFound()) {
                System.err.println("Deployment aborted, ConnAPI.dll not found.");
                System.exit(0);
            }
            if (!CONA.getInstance().isConnJNIDllFound()) {
                System.err.println("Deployment aborted, ConnJNI.dll not found.");
                System.exit(0);
            }
            
            String jad = args[0];
            String jar = args[1];
            
            File jadFile = new File(jad);
            File jarFile = new File(jar);
            
            ArrayList<String> selectedTerminals = null;

            if(args.length > 2) {
                String[] deviceNamesList = args[2].split("\n");
                selectedTerminals = new java.util.ArrayList<java.lang.String>();
                for(int i=0; i < deviceNamesList.length; i++) {
                    String deviceName = deviceNamesList[i].trim();
                    if(!deviceName.equals("")) {
                        selectedTerminals.add(deviceName);
                    }
                }
            }
            
            Deployer deployer = new Deployer();
            
            // Open connection, if it doesn't work out, don't continue
            deployer.openConnectionLayer();
            if (!deployer.isConnected()) {
                System.err.println("Deployment aborted, connection failed.");
                System.exit(0);
            }
            
            List<String> allConnectedTerminals = deployer.getTerminals();
            
            // If there are no terminals, don't continue
            if (allConnectedTerminals.size() == 0) {
                System.err.println("Deployment aborted, no devices found.");
                deployer.closeConnectionLayer();
                System.exit(0);
            }
            
            List<String> notOkTerminals = new ArrayList<String>();
            
            if(selectedTerminals != null) {

                // check whether the selected terminals are still connected
                for(String terminal:selectedTerminals) {
                    if(!allConnectedTerminals.contains(terminal)) {
                        System.err.println("Could not deploy. Selected terminal: " + terminal + " is not currently connected.");
                        notOkTerminals.add(terminal);
                    } else {
                        System.out.println("Deploying to: '" + terminal
                            + "' using "
                            + deployer.getConnectionType(terminal));
                        try {
                            boolean success = deployer.deploy(new File(jad), new File(jar), terminal);
                            if(success) {
                                System.out.println("Deployment succeeded!");
                            } else {
                                notOkTerminals.add(terminal);
                            }
                        } catch(IOException e) {
                            e.printStackTrace();
                            notOkTerminals.add(terminal);
                        } catch(UnsatisfiedLinkError e) {
                            e.printStackTrace();
                            notOkTerminals.add(terminal);
                        }
                    }
                }
                selectedTerminals.removeAll(notOkTerminals);
            } else {
                System.out.println("Deploying to all connected terminals.");
                notOkTerminals = deployer.deployToAllTerminals(jadFile, jarFile);
            }
            
            for(String notOk:notOkTerminals) {
                System.err.println("Could not deploy to terminal: " + notOk);
            }
            
            System.out.println("Deployment done, closing connection layer");
            
            deployer.closeConnectionLayer();
        }
    }
}
