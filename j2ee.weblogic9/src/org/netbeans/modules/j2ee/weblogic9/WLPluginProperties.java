/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
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
public class WLPluginProperties {
    
    
    private static WLPluginProperties pluginProperties = null;
    private String installLocation;
    
    
    public static WLPluginProperties getInstance(){
        if(pluginProperties==null){
            pluginProperties = new WLPluginProperties();
        }
        return pluginProperties;
    }
    
    
    
    /** Creates a new instance of */
    private WLPluginProperties() {
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
    
    
    private  FileObject propertiesFile = null;
    
    private FileObject getPropertiesFile() throws java.io.IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject dir = fs.findResource("J2EE");
        FileObject retVal = null;
        if (null != dir) {
            retVal = dir.getFileObject("weblogic","properties"); // NOI18N
            if (null == retVal) {
                retVal = dir.createData("weblogic","properties"); //NOI18N
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
    
    private static Collection fileColl = new java.util.ArrayList();
    
    static {
        fileColl.add("common");        // NOI18N
        fileColl.add("javelin");       // NOI18N
        fileColl.add("uninstall");     // NOI18N
        fileColl.add("common/bin");    // NOI18N
        fileColl.add("server/lib/weblogic.jar"); // NOI18N
    }
    public static boolean isGoodServerLocation(File candidate){
        if (null == candidate ||
                !candidate.exists() ||
                !candidate.canRead() ||
                !candidate.isDirectory()  ||
                !hasRequiredChildren(candidate, fileColl)) {
            return false;
        }
        return true;
    }
    
    private static boolean hasRequiredChildren(File candidate, Collection requiredChildren) {
        if (null == candidate)
            return false;
        String[] children = candidate.list();
        if (null == children)
            return false;
        if (null == requiredChildren)
            return true;
        Iterator iter = requiredChildren.iterator();
        while (iter.hasNext()){
            String next = (String)iter.next();
            File test = new File(candidate.getPath()+File.separator+next);
            if (!test.exists())
                return false;
        }
        return true;
    }
    
    public boolean isCurrentServerLocationValid(){
        if (getInstallLocation()!=null)
            return (isGoodServerLocation(new File(getInstallLocation())));
        else
            return false;
    }
    
    
    public void setInstallLocation(String installLocation){
        if ( installLocation.endsWith("/") || installLocation.endsWith("\\") ){
            installLocation = installLocation.substring(0, installLocation.length() - 1 );
        }
        
        this.installLocation = installLocation;
        WLDeploymentFactory.resetWLClassLoader(installLocation);
    }
    public String getInstallLocation(){
        return this.installLocation;
    }
    
}
