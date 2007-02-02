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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.io.File;
import java.util.Properties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.ErrorManager;

/**
 * Plugin Properties Singleton class
 * @author Ivan Sidorkin
 */
public class JBPluginProperties {
    
    public static final String PROPERTY_DISPLAY_NAME ="displayName";//NOI18N
    public static final String PROPERTY_SERVER = "server";//NOI18N
    public static final String PROPERTY_DEPLOY_DIR = "deploy-dir";//NOI18N
    public static final String PROPERTY_SERVER_DIR = "server-dir";//NOI18N
    public static final String PROPERTY_ROOT_DIR = "root-dir";//NOI18N
    public static final String PROPERTY_HOST = "host";//NOI18N
    public static final String PROPERTY_PORT = "port";//NOI18N
    
    
    private static JBPluginProperties pluginProperties = null;
    private String installLocation;
    private String domainLocation;
    
    
    public static JBPluginProperties getInstance(){
        if(pluginProperties==null){
            pluginProperties = new JBPluginProperties();
        }
        return pluginProperties;
    }
    
    
    
    /** Creates a new instance of */
    private JBPluginProperties() {
        java.io.InputStream inStream = null;
        try {
            try {
                propertiesFile = getPropertiesFile();
                if (null != propertiesFile)
                    inStream = propertiesFile.getInputStream();
            } catch (java.io.FileNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } finally {
                loadPluginProperties(inStream);
                if (null != inStream)
                    inStream.close();
            }
        } catch (java.io.IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
    }
    
    void loadPluginProperties(java.io.InputStream inStream) {
        Properties inProps = new Properties();
        if (null != inStream)
            try {
                inProps.load(inStream);
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        String loc = inProps.getProperty(INSTALL_ROOT_KEY);
        if (loc!=null){// try to get the default value
            setInstallLocation(loc);
        }
    }
    
    private static final String INSTALL_ROOT_KEY = "installRoot"; // NOI18N
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot"; //NOI18N
    
    
    private  FileObject propertiesFile = null;
    
    private FileObject getPropertiesFile() throws java.io.IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject dir = fs.findResource("J2EE");
        FileObject retVal = null;
        if (null != dir) {
            retVal = dir.getFileObject("jb","properties"); // NOI18N
            if (null == retVal) {
                retVal = dir.createData("jb","properties"); //NOI18N
            }
        }
        return retVal;
    }
    
    
    public void saveProperties(){
        Properties outProp = new Properties();
        String installRoot = getInstallLocation();
        if (installRoot != null)
            outProp.setProperty(INSTALL_ROOT_KEY, installRoot);
        
        FileLock l = null;
        java.io.OutputStream outStream = null;
        try {
            if (null != propertiesFile) {
                try {
                    l = propertiesFile.lock();
                    outStream = propertiesFile.getOutputStream(l);
                    if (null != outStream)
                        outProp.store(outStream, "");
                } catch (java.io.IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                } finally {
                    if (null != outStream)
                        outStream.close();
                    if (null != l)
                        l.releaseLock();
                }
            }
        } catch (java.io.IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public boolean isCurrentServerLocationValid(){
        if (getInstallLocation()!=null)
            return 
                JBPluginUtils.isGoodJBServerLocation4x(new File(getInstallLocation())) || 
                JBPluginUtils.isGoodJBServerLocation5x(new File(getInstallLocation()));
        else
            return false;
    }
    
    
    public void setInstallLocation(String installLocation){
        if ( installLocation.endsWith("/") || installLocation.endsWith("\\") ){
            installLocation = installLocation.substring(0, installLocation.length() - 1 );
        }
        
        this.installLocation = installLocation;
    }
    
    public String getInstallLocation(){
        return this.installLocation;
    }
    
    public void setDomainLocation(String domainLocation) {
        if ( domainLocation.endsWith("/") || domainLocation.endsWith("\\") ){
            domainLocation = domainLocation.substring(0, domainLocation.length() - 1 );
        }
        
        this.domainLocation = domainLocation;
    }
    
    public String getDomainLocation() {
        return domainLocation;
    }
}