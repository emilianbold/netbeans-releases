/*
 * Copyright 2005, 2007 Nokia Corporation. All rights reserved.
 *  
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). See LICENSE.TXT for exact terms.
 * You may not use this file except in compliance with the License.  You can obtain a copy of the
 * License at http://www.netbeans.org/cddl.html
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
     * Main method that deploys the given application to the given devices. The device list must be separated
     * by new lines ('\n'), this is to simplify the deployment ant script.
     */
    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("usage: DeployerMain <jad file> <jar file> [list of device names to deploy to]");
            
        } else {
            // Don't continue if OS is not supported
            if (!CONA.getInstance().isOSSupportsDeployment()) {
                System.out.println("Deployment only supported on Windows operating system");
                System.exit(0);
            }

            // Don't continue if dll's were not found
            if (!CONA.getInstance().isConnAPIDllFound()) {
                System.out.println("ConnAPI.dll not found");
                System.exit(0);
            }
            if (!CONA.getInstance().isConnJNIDllFound()) {
                System.out.println("ConnJNI.dll not found");
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
                System.out.println("Deployment aborted, connection failed");
                System.exit(0);
            }
            
            List<String> allConnectedTerminals = deployer.getTerminals();
            
            // If there are no terminals, don't continue
            if (allConnectedTerminals.size() == 0) {
                System.out.println("No devices found");
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
