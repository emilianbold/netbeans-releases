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
 * DeviceAnywhereUploadTask.java
 *
 * Created on April 27, 2007, 6:01 PM
 */

package org.netbeans.modules.deployment.deviceanywhere;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIDeviceWrapper;
import org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIGetLockedDevicesReturn;
import org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIStartDownloadScriptReturn;
import org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIUploadApplicationReturn;
import org.netbeans.modules.deployment.deviceanywhere.service.ReturnCodes;

/**
 * @author suchys
 */
public class DeviceAnywhereUploadTask extends Task {
    private ResourceBundle bundle;
    
    private String user;
    private String password;
    private int deviceId;
    private File jadFile;
    private File jarFile;
    private String career;

    @Override
    public void execute() throws BuildException {
        bundle = ResourceBundle.getBundle("org/netbeans/modules/deployment/deviceanywhere/messages"); //NOI18N
        
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DeviceAnywhereUploadTask.class.getClassLoader());         

        System.setProperty("javax.xml.stream.XMLInputFactory", "com.sun.xml.stream.ZephyrParserFactory"); //NOI18N
        System.setProperty("javax.xml.stream.XMLOutputFactory", "com.sun.xml.stream.ZephyrWriterFactory"); //NOI18N        
        try {
            org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIService serviceApi = new org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIService();
            org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPI port = serviceApi.getApplicationAPI();
            boolean deviceLockedOK = false;
            try {
                ApplicationAPIGetLockedDevicesReturn lockedDevices = port.getLockedDevices(user, password);
                handleReturnCode(lockedDevices.getReturnCode());
                List<ApplicationAPIDeviceWrapper> result = lockedDevices.getDeviceWrappers().getDeviceWrappers();
                log("Found following devices:", Project.MSG_VERBOSE); //NOI18N
                for (ApplicationAPIDeviceWrapper elem : result) {
                    log("Device: " + String.valueOf(elem.getId()), Project.MSG_VERBOSE);
                    if (deviceId== elem.getId()){
                        deviceLockedOK = true;
                        log("Device " + deviceId + " found", Project.MSG_VERBOSE); //NOI18N
                    }
                }
            } catch (Exception ex) {
                if (ex instanceof ClassNotFoundException){
                    throw new BuildException(ex);
                }
                throw new BuildException(bundle.getString("can_not_connect"));
            }
            if (!deviceLockedOK){
                throw new BuildException(bundle.getString("device_not_locked"));
            }

            byte[] jadData = null;
            byte[] jarData = null;
            DataInputStream jad = null;
            DataInputStream jar = null;
            try {
                jad = new DataInputStream(new BufferedInputStream(new FileInputStream(jadFile)));
                jadData = new byte[jad.available()];
                jad.read(jadData);
                //hack for Sprint
                StringBuffer filtered = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(jadData)));
                String line;
                while ((line = br.readLine()) != null){
                    if (line.startsWith("MicroEdition-")) //NOI18N
                        continue;
                    filtered.append(line).append("\r\n");
                }
                jadData = filtered.toString().getBytes();
                //end hack for Sprint
                log("JAD file read", Project.MSG_VERBOSE);
            } catch (Exception ex) {
                throw new BuildException(bundle.getString("error_reading_jad")); 
            } finally {
                if (jad != null){
                    try {
                        jad.close();
                    } catch (IOException ex) {
                    }
                }
            }

            try {
                jar = new DataInputStream(new BufferedInputStream(new FileInputStream(jarFile)));
                jarData = new byte[jar.available()];
                jar.read(jarData);
                log("JAR file read", Project.MSG_VERBOSE);
            } catch (Exception ex) {
                throw new BuildException(bundle.getString("error_reading_jar")); 
            } finally {
                if (jar != null){
                    try {
                        jar.close();
                    } catch (IOException ex) {
                    }
                }
            }

            String name = jadFile.getName();
            int index = name.indexOf('.');
            name = name.substring(0, index != -1 ? index : name.length() - 1);
            int applicationId = -1;
            try{
                log("Starting upload", Project.MSG_VERBOSE); //NOI18N
                //System.out.println(new String(jadData));
                ApplicationAPIUploadApplicationReturn uploadReturn = port.uploadApplication(user, password,  name, jarData, jadData);
                handleReturnCode(uploadReturn.getReturnCode());
                applicationId = uploadReturn.getApplicationId();
                log("Application id: " + applicationId, Project.MSG_VERBOSE); //NOI18N
            } catch (Exception e){
                throw new BuildException(bundle.getString("error_uploading_data"));
            }

            try {
                log("Starting script", Project.MSG_VERBOSE); //NOI18N
                ApplicationAPIStartDownloadScriptReturn scriptReturn = port.startDownloadScript(user, password, deviceId, applicationId);
                handleReturnCode(scriptReturn.getReturnCode());
                log("Script executed", Project.MSG_VERBOSE); //NOI18N
            } catch (Exception e){
                throw new BuildException(bundle.getString("error_running_remote_script"));
            }
        } catch (IOException ioEx){
            throw new BuildException(ioEx);
        } finally {
            System.setProperty("javax.xml.stream.XMLInputFactory", ""); //NOI18N
            System.setProperty("javax.xml.stream.XMLOutputFactory", ""); //NOI18N
            Thread.currentThread().setContextClassLoader(oldClassLoader); 
        }
        log("Done!", Project.MSG_VERBOSE); //NOI18N
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setJadFile(File jadFile) {
        this.jadFile = jadFile;
    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }
    
    private void handleReturnCode(int returnCode) {
        if (returnCode == ReturnCodes.INTERNAL_ERROR){
            throw new BuildException(bundle.getString("intenal_error"));
        }
        if (returnCode == ReturnCodes.LOGIN_FAILED){
            throw new BuildException(bundle.getString("wrong_login"));
        }
        if (returnCode == ReturnCodes.INVALID_APPLICATION_NAME){
            throw new BuildException(bundle.getString("invalid_application_name"));
        }
        if (returnCode == ReturnCodes.JAD_FILE_PARSE_ERROR){
            throw new BuildException(bundle.getString("jad_file_cant_parse"));
        }
        if (returnCode == ReturnCodes.DEVICE_NOT_FOUND){
            throw new BuildException(bundle.getString("device_not_found"));
        }
        if (returnCode == ReturnCodes.APPLICATION_NOT_FOUND){
            throw new BuildException(bundle.getString("application_not_found"));
        }
    }
}
