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
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods for deployment operations.
 */
public class Deployer {
    
    /**
     * Current platform connection status.
     */
    private boolean connected = false;
    
    /**
     * Default constructor, opens the connection layer.
     */
    public Deployer(){
        connected = false;
        
        // Open connection
        openConnectionLayer();
        
        if (!connected) {
            System.out.println("connection failed");
        }
    }
   
    /**
     * Returns the current connection status.
     * @return true if connection to service layer is open, otherwise false
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Returns whether PC Suite is installed or not.
     * @return boolean PC suite installation status.
     */
    public boolean isPCSuiteInstalled() {
        return CONA.getInstance().isConnAPIDllFound();
    }

    /**
     * Returns whether deployment is supported on the current OS or not.
     * @return boolean deployment support status.
     */
    public boolean isOSSupportsDeployment() {
        return CONA.getInstance().isOSSupportsDeployment();
    }

    /**
     * Opens the platform connection layer using native method.
     */
    public void openConnectionLayer() {
        if (CONA.getInstance().isConnAPIDllFound()
                && CONA.getInstance().isConnJNIDllFound()
                && !connected) {
            connected = CONA.getInstance().connect();
        }
    }
    
    /**
     * Closes the platform connection layer using native method.
     */
    public void closeConnectionLayer() {
        try {
            Thread.sleep(3050);
        } catch (InterruptedException e) {
            // No error reporting
        }
        if (connected) {
            boolean disconnected = CONA.getInstance().disconnect();
            if (!disconnected) {
                System.out.println("Failed to close service layer");
            }
            connected = !disconnected;
        }
    }

    /**
     * Returns the terminals that are currently connected to the computer. The
     * name includes an ID number for the session to identify them from each
     * other.
     *
     * @return terminal names as a String array, if no terminals are connected,
     *         returns a zero-length String array.
     */
    public List<String> getTerminals() {
        if (!connected) {
            openConnectionLayer();
        }
        
        List<String> terminalList = new ArrayList<String>();
        
        addDevicesToList(CONA.getInstance().getDevices(CONA.CONAPI_MEDIA_BLUETOOTH),
                terminalList);
        addDevicesToList(CONA.getInstance().getDevices(CONA.CONAPI_MEDIA_IRDA),
                terminalList);
        addDevicesToList(CONA.getInstance().getDevices(CONA.CONAPI_MEDIA_SERIAL),
                terminalList);
        addDevicesToList(CONA.getInstance().getDevices(CONA.CONAPI_MEDIA_USB),
                terminalList);
        
        return terminalList;
    }
    
    /**
     * Returns the connection type of a particular terminal connected to the
     * computer.
     *
     * @param terminal
     *            name of the terminal, complete with session ID
     * @return connection type ("RS232", "IrDA", "USB" or "Bluetooth") of the
     *         terminal
     */
    public String getConnectionType(String terminal) {
        if (!connected) {
            openConnectionLayer();
        }
        int id = getID(terminal);
        
        return CONA.getInstance().getDeviceType(id);
    }
    
    /**
     * Deploys the JAR and JAD files to all connected terminals.
     *
     * @param jad
     *            JAD file to deploy
     * @param jar
     *            JAR file to deploy
     * @return boolean whether deployment was successful or not.
     * @see #getTerminals
     */
    public List<String> deployToAllTerminals(File jad, File jar) {
        List<String> allTerminals = getTerminals();
        List<String> notOk = new ArrayList<String>();
        for (String terminal:allTerminals) {
            try {
                boolean success = deploy(jad, jar, terminal);
                if(success) {
                    System.out.println("Deployment succeeded!");
                } else {
                    notOk.add(terminal);
                }
            } catch (UnsatisfiedLinkError e) {
                e.printStackTrace();
                notOk.add(terminal);
            } catch (IOException e) {
                e.printStackTrace();
                notOk.add(terminal);
            }
        }
        return notOk;
    }

    /**
     * Deploys the JAR and JAD files to the selected terminal.
     *
     * @param jad
     *            JAD file to deploy
     * @param jar
     *            JAR file to deploy
     * @param terminal
     *            to deploy to, use the name you got from {@link #getTerminals}
     * @throws IOException
     *             if there is an error opening or reading the JAR or JAD file
     * @throws UnsatisfiedLinkError
     *             if ConnAPI.dll has not been loaded correctly. Either the
     *             DLL was not found or it was an old and incompatible version.
     * @return boolean whether deployment was successful or not.
     * @see #getTerminals
     *
     */
    public boolean deploy(File jad, File jar, String terminal) throws IOException,
            UnsatisfiedLinkError {
        if (!CONA.getInstance().isConnAPIDllFound()) {
            throw new UnsatisfiedLinkError("Failed to open ConnAPI.dll");
        }
        if (!CONA.getInstance().isConnJNIDllFound()) {
            throw new UnsatisfiedLinkError("Failed to open ConnJNI.dll");
        }
        if (!connected) {
            openConnectionLayer();
            if (!connected) {
                System.err.println("Failed to open service layer");
                return false;
            } else {
                System.out.println("Service layer opened");
            }
        }
        
        int id = getID(terminal);
        
        if (!jad.exists()) {
            throw new IOException("JAD file not found:"
                    + jad.getAbsolutePath());
        }
        
        if (!jar.exists()) {
            throw new IOException("JAR file not found:"
                    + jar.getAbsolutePath());
        }
        
        if (id != -1) {
            return this.synchronizedDeploy(jad, jar, id);
        } else {
            System.err.println("Failed to find destination device:" + terminal);
            return false;
        }
    }
        
  
   /**
    * Opens a connection to the given device using native method.
    * @param ind the id of the device to open.
    */
   private boolean openConnection(int ind) {
        boolean openC = CONA.getInstance().openFileSystem(ind);
        if (openC) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Closes the connection to device using native method.
     */
    private boolean closeConnection() {
        return CONA.getInstance().closeFileSystem();
    }
    
    /**
     * Utility method that adds the given device names to the given list.
     */
    private void addDevicesToList(String devices, List<String> list) {
        if(devices == null) {
            return;
        }
        StringTokenizer devicesTokenizer = new StringTokenizer(devices, ","); //$NON-NLS-1$
        while(devicesTokenizer.hasMoreTokens()) {
            list.add(devicesTokenizer.nextToken());
        }
    }
    
    /**
     * Utility method that returns the ID from the given terminal name.
     */
    private int getID(String terminal) {
        String idString = terminal.substring(terminal.indexOf("(ID:") + 4, //$NON-NLS-1$
                terminal.indexOf(")")).trim(); //$NON-NLS-1$
        return Integer.parseInt(idString);
    }
    
    /**
     * Opens a connection to the given terminal and deploys the given application to it.
     * @param jad
     *            JAD file to deploy
     * @param jar
     *            JAR file to deploy
     * @param id
     *            to id of the terminal to deploy to, use the name you got from {@link #getTerminals}
     * @throws IOException
     *             if there is an error opening or reading the JAR or JAD file
     * @throws UnsatisfiedLinkError
     *             if ConnAPI.dll has not been loaded correctly. Either the
     *             DLL was not found or it was an old and incompatible version.
     * @see #getTerminals
     */
    private boolean synchronizedDeploy(File jad, File jar, int id) {
        boolean success = true;
        String jarfilepath = jar.getParentFile().getAbsolutePath();
        String jadfilename = jad.getName();
        String jarfilename = jar.getName();
        
        boolean bopenconnection = openConnection(id);
        
        if (bopenconnection) {
            
            String strFilepath = jarfilepath + "\\"; //$NON-NLS-1$
            
            success = CONA.getInstance().installApplication(strFilepath,
                                                            jarfilename,
                                                            jadfilename,
                                                            CONA.CONA_APPLICATION_TYPE_JAVA,
                                                            true);
        } else {
            // open connection failed
            return false;
        }
        
        closeConnection();
        return true;
    }    
}