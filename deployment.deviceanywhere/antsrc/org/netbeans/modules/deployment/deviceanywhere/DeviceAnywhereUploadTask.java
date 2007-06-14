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
    private CommandlineJava commandline;    
    
    private String user;
    private String password;
    private int deviceId;
    private File jadFile;
    private File jarFile;
        
    @Override
    public void execute() throws BuildException {
        org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIService service = null;
        org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPI port = null;
        try {
            service = new org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIService();
            port = service.getApplicationAPI();
            ApplicationAPIGetLockedDevicesReturn lockedDevices = port.getLockedDevices(user, password);
            handleReturnCode(lockedDevices.getReturnCode());
            List<ApplicationAPIDeviceWrapper> result = lockedDevices.getDeviceWrappers().getItem();
            if (result.size() == 0){
                throw new BuildException("There are no locked devices!");
            }
            log("Found following devices:", Project.MSG_VERBOSE);
            boolean deviceLockedOK = false;
            for (ApplicationAPIDeviceWrapper elem : result) {
                log("Device: " + String.valueOf(elem.getId()), Project.MSG_VERBOSE);
                if (deviceId== elem.getId()){
                    deviceLockedOK = true;
                    log("Device " + deviceId + " found", Project.MSG_VERBOSE);
                }
            }
            if (!deviceLockedOK){
                throw new BuildException("Requested device is not locked by user, can not upload!");
            }
        } catch (Exception ex) {
            if (ex instanceof ClassNotFoundException){
                throw new BuildException(ex);
            }
            throw new BuildException("Can not connect to service!");
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
                if (line.startsWith("MicroEdition-"))
                    continue;
                filtered.append(line).append("\r\n");
            }
            jadData = filtered.toString().getBytes();
            //end hack for Sprint
            log("JAD file read", Project.MSG_VERBOSE);
        } catch (Exception ex) {
            throw new BuildException("Error reading jad data!");
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
            throw new BuildException("Error reading jar data!");
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
            log("Starting upload", Project.MSG_VERBOSE);
            System.out.println(new String(jadData));
            ApplicationAPIUploadApplicationReturn uploadReturn = port.uploadApplication(user, password,  name, jarData, jadData);
            handleReturnCode(uploadReturn.getReturnCode());
            applicationId = uploadReturn.getApplicationId();
            log("Application id: " + applicationId, Project.MSG_VERBOSE);
        } catch (Exception e){
            throw new BuildException("Error uploading data!");
        }

        try {
            log("Starting script", Project.MSG_VERBOSE);
            ApplicationAPIStartDownloadScriptReturn scriptReturn = port.startDownloadScript(user, password, deviceId, applicationId);
            handleReturnCode(scriptReturn.getReturnCode());
            log("Script executed", Project.MSG_VERBOSE);
        } catch (Exception e){
            throw new BuildException("Error running remote script!");
        }
        log("Done!");
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

    private static void handleReturnCode(int returnCode) {
        if (returnCode == ReturnCodes.INTERNAL_ERROR){
            throw new BuildException("There was an internal error in Device Anywhere infrastructure!");
        }
        if (returnCode == ReturnCodes.LOGIN_FAILED){
            throw new BuildException("Wrong login!");
        }
        if (returnCode == ReturnCodes.INVALID_APPLICATION_NAME){
            throw new BuildException("Invalid application name!");
        }
        if (returnCode == ReturnCodes.JAD_FILE_PARSE_ERROR){
            throw new BuildException("JAD file can not be parsed!");
        }
        if (returnCode == ReturnCodes.DEVICE_NOT_FOUND){
            throw new BuildException("Device has not been found!");
        }
        if (returnCode == ReturnCodes.APPLICATION_NOT_FOUND){
            throw new BuildException("Application has not been found!");
        }
    }
}
