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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.lang.reflect.Method;
import org.openide.util.NbBundle;

import org.openide.modules.InstalledFileLocator;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel;
import org.netbeans.modules.j2ee.sun.share.CharsetMapping;
import java.util.logging.Level;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.netbeans.modules.j2ee.sun.ide.editors.CharsetDisplayPreferenceEditor;
import org.netbeans.modules.j2ee.sun.ide.Installer;

import org.netbeans.modules.j2ee.sun.ide.ExtendedClassLoader;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;

/**
 *
 * @author  ludo
 */
public class PluginProperties  {
    
    java.util.logging.Logger jsr88Logger =
        java.util.logging.Logger.getLogger("com.sun.enterprise.tools.jsr88.spi");
    
    /** Holds value of property userList. */
    private  String[] userList = new String[0];
    
    private  String logLevel = null;
    private  boolean incrementalDeployPossible = false;
    
    private  FileObject propertiesFile = null;
    
    private static final String INCREMENTAL = "incrementalDeploy"; // NOI18N
    private static final String PRINCIPAL_PREFIX = "principalEntry."; // NOI18N
    private static final String GROUP_PREFIX = "groupEntry."; // NOI18N
    private static final String LOG_LEVEL_KEY = "logLevel";  // NOI18N
    private static final String CHARSET_DISP_PREF_KEY = "charsetDisplayPreference"; // NOI18N
    private static final String INSTALL_ROOT_KEY = "installRoot"; // NOI18N
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot"; //NOI18N
    
    public static String COBUNDLE_DEFAULT_INSTALL_PATH ="SunAppServer8.1";  //NOI18N
    
    /** holds value of com.sun.aas.installRoot */
    private  File installRoot = null;
    
    static private PluginProperties thePluginProperties=null;
    
    public static PluginProperties getDefault(){
        if (thePluginProperties==null)
            thePluginProperties= new PluginProperties();
        return thePluginProperties;
    }
    
    
    
    private  PluginProperties(){
        java.io.InputStream inStream = null;
        try {
            try {
                propertiesFile = getPropertiesFile();
                if (null != propertiesFile)
                    inStream = propertiesFile.getInputStream();
            } catch (java.io.FileNotFoundException fnfe) {
                Constants.pluginLogger.info(NbBundle.getMessage(PluginProperties.class, "INFO_NO_PROPERTY_FILE")); //NOI18N
            } catch (java.io.IOException ioe) {
                Constants.pluginLogger.info(NbBundle.getMessage(PluginProperties.class, "ERR_READING_PROPERTIES")); //NOI18N
                Constants.pluginLogger.throwing(PluginProperties.class.getName(), "<init>", //NOI18N
                        ioe);
            } finally {
                loadPluginProperties(inStream);
                if (null != inStream)
                    inStream.close();
            }
        } catch (java.io.IOException ioe) {
        }
        
    }
    
    private FileObject getPropertiesFile() throws java.io.IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject dir = fs.findResource("J2EE");
        FileObject retVal = null;
        if (null != dir) {
            retVal = dir.getFileObject("platform","properties"); // NOI18N
            if (null == retVal) {
                retVal = dir.createData("platform","properties"); //NOI18N
            }
        }
        return retVal;
    }
    
    
    void loadPluginProperties(java.io.InputStream inStream) {
        Properties inProps = new Properties();
        if (null != inStream)
            try {
                inProps.load(inStream);
            } catch (java.io.IOException ioe) {
                Constants.pluginLogger.throwing(PluginProperties.class.toString(), "loadPluginProperties", ioe); //NOI18N
            }
        
        logLevel = inProps.getProperty(LOG_LEVEL_KEY, java.util.logging.Level.OFF.toString());
        String[] inputUsers = getArrayPropertyValue(inProps, PRINCIPAL_PREFIX);
        String[] inputGroups = getArrayPropertyValue(inProps, GROUP_PREFIX);
        
        setCharsetDisplayPreferenceStatic(Integer.valueOf(inProps.getProperty(CHARSET_DISP_PREF_KEY, "1")));
        String b= inProps.getProperty(INCREMENTAL,"false");//true by default
        incrementalDeployPossible = b.equals("true");
        String loc = inProps.getProperty(INSTALL_ROOT_KEY);
        if (loc==null){// try to get the default value
            installRoot = new File(getDefaultInstallRoot());
            if (isGoodAppServerLocation(installRoot)){
                System.setProperty(INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
                javax.swing.SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        registerDefaultDomain();
                        saveProperties();
                    }
                    
                });
            }
            
        }
	else{
            installRoot = new File(loc);
            if (!isGoodAppServerLocation(installRoot)){
                installRoot = new File("");
                System.out.println(NbBundle.getMessage(PluginProperties.class, "MSG_WrongInstallDir"));
                //remove storage for old defined instances: they would cause errors furthers down, and should not be known.
                File store = new File(System.getProperty("netbeans.user")+"/config/J2EE/InstalledServers/.nbattrs");
                
                try{store.delete();
                }catch(Exception e){
                }
                
                
            } else{
                System.setProperty(INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
                
            }
            
            
        }
        

 
        
        // note these operations trigger a property change, so we probably want
        // them to fire AFTER all the values have been read into place....
        setUserListStatic(inputUsers);
        setGroupListStatic(inputGroups);
        Constants.pluginLogger.setLevel(Level.ALL);
        jsr88Logger.setLevel(Level.ALL);
        Level parsed = null;
        try {
            parsed = Level.parse(logLevel);
            jsr88Logger.setLevel(parsed);
            Constants.pluginLogger.setLevel(parsed);
            Constants.pluginLogger.log(parsed, logLevel);
            jsr88Logger.log(parsed, logLevel);
        } catch (IllegalArgumentException iae) {
            logLevel = Level.ALL.toString();
            Constants.pluginLogger.severe(NbBundle.getMessage(PluginProperties.class, "ERR_ILLEGAL_LEVEL_VALUE")); // NOI18N
        }
        
    }
    
    
    
    public void setIncrementalDeploy(Boolean b){
        
        incrementalDeployPossible = b.booleanValue();
        saveProperties();
        
    }
    public Boolean getIncrementalDeploy(){
        return new Boolean(incrementalDeployPossible);
        
    }
    
    public  boolean isIncrementalDeploy(){
        return incrementalDeployPossible;
        
    }
    
    
    /** Getter for property userList.
     * @return Value of property userList.
     *
     */
    public String[] getUserList() {
        SecurityMasterListModel pModel =
                SecurityMasterListModel.getPrincipalMasterModel();
        String[] ss=masterListToStringArray(pModel);
        return ss;
        //return new String[0];
    }
    
    public String[] getGroupList() {
        SecurityMasterListModel pModel =
                SecurityMasterListModel.getGroupMasterModel();
        
        return masterListToStringArray(pModel);
        // return new String[0];
    }
    
    private String[] masterListToStringArray(SecurityMasterListModel pModel) {
        int len = pModel.getSize();
        String retVal[] = new String[len];
        for (int i = 0; i < len; i++) {
            String foo = (String) pModel.getElementAt(i);
            retVal[i] = foo;
        }
        return retVal;
    }
    
    public  boolean setUserListStatic(String [] list) {
        SecurityMasterListModel pModel =
                SecurityMasterListModel.getPrincipalMasterModel();
        boolean retVal = false;
        if (!containsSameElements(pModel, list)) {
            
            fillMasterList(pModel, list);
            retVal = true;
        }
        return retVal;
    }
    
    public void setUserList(String [] list) {
        if (setUserListStatic(list))
            saveProperties();
    }
    
    public  boolean setGroupListStatic(String [] list) {
        SecurityMasterListModel pModel =
                SecurityMasterListModel.getGroupMasterModel();
        boolean retVal = false;
        if (!containsSameElements(pModel, list)) {
            fillMasterList(pModel, list);
            retVal = true;
        }
        return retVal;
    }
    
    public void setGroupList(String [] list) {
        if (setGroupListStatic(list))
            saveProperties();
    }
    
    
    boolean containsSameElements(SecurityMasterListModel pModel, String[] list) {
        int len = pModel.getSize();
        if (len != list.length)
            return false;
        else
            for (int i = 0; i < len; i++) {
            if (!pModel.contains(list[i]))
                return false;
            }
        return true;
    }
    
    
    
    private void fillMasterList(SecurityMasterListModel pModel, String[] values) {
        int len = values.length;
        for (int i = pModel.getSize() - 1; i >= 0; i--) {
            pModel.removeElementAt(i);
        }
        for (int i = 0; i < len; i++) {
            if (!pModel.contains(values[i]))
                pModel.addElement(values[i]);
        }
    }
    
    public String getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(String ll) {
        String oll = logLevel;
        if (!oll.equals(ll)) {
            Level parsed = null;
            try {
                parsed = Level.parse(ll);
                jsr88Logger.setLevel(parsed);
                Constants.pluginLogger.setLevel(parsed);
                Constants.pluginLogger.log(parsed, ll);
                jsr88Logger.log(parsed, ll);
                
                logLevel = ll;
                saveProperties();
            } catch (IllegalArgumentException iae) {
                //logLevel = Level.ALL.toString();
                Constants.pluginLogger.severe(
                        NbBundle.getMessage(PluginProperties.class, "ERR_ILLEGAL_LEVEL_VALUE")); //NOI18N
            }
        }
    }
    
    
    
    public  Integer getCharsetDisplayPreferenceStatic() {
        return CharsetMapping.getDisplayOption();
    }
    
    public  void setCharsetDisplayPreferenceStatic(Integer displayPreference) {
        CharsetMapping.setDisplayOption(displayPreference);
    }
    
    /** Getter for property charsetDisplayPreference.
     * @return Value of property charsetDisplayPreference.
     *
     */
    public Integer getCharsetDisplayPreference() {
        return  getCharsetDisplayPreferenceStatic();
    }
    
    /** Setter for property charsetDisplayPreference.
     * @param displayPreference New value of property charsetDisplayPreference.
     *
     */
    public void setCharsetDisplayPreference(Integer displayPreference) {
        Integer oldDisplayPreference = getCharsetDisplayPreferenceStatic();
        if(!displayPreference.equals(oldDisplayPreference)) {
            setCharsetDisplayPreferenceStatic(displayPreference);
            Integer newDisplayPreference = getCharsetDisplayPreferenceStatic();
            saveProperties();
        }
    }
    
    
    private void saveProperties(){
        Properties outProp = new Properties();
        setArrayPropertyValue(outProp, PRINCIPAL_PREFIX, getUserList());
        
        setArrayPropertyValue(outProp, GROUP_PREFIX, getGroupList());
        outProp.setProperty(INCREMENTAL, ""+incrementalDeployPossible);
        
        if (!logLevel.equals(Level.OFF.toString()))
            outProp.setProperty(LOG_LEVEL_KEY, logLevel);
        if (!getCharsetDisplayPreferenceStatic().equals(CharsetDisplayPreferenceEditor.DEFAULT_PREF_VAL))
            outProp.setProperty(CHARSET_DISP_PREF_KEY, getCharsetDisplayPreferenceStatic().toString());
        if (installRoot != null)
            outProp.setProperty(INSTALL_ROOT_KEY, installRoot.getAbsolutePath());
        FileLock l = null;
        java.io.OutputStream outStream = null;
        try {
            if (null != propertiesFile) {
                try {
                    l = propertiesFile.lock();
                    outStream = propertiesFile.getOutputStream(l);
                    if (null != outStream)
                        outProp.store(outStream, "");
                } catch (java.io.IOException ioe) {
                    Constants.pluginLogger.severe(
                            NbBundle.getMessage(PluginProperties.class, "ERR_SAVING_PROPERTIES") // NOI18N
                            );
                    Constants.pluginLogger.throwing(PluginProperties.class.toString(), "saveChange", //NOI18N
                            ioe);
                } finally {
                    if (null != outStream)
                        outStream.close();
                    if (null != l)
                        l.releaseLock();
                }
            }
        } catch (java.io.IOException ioe) {
            Constants.pluginLogger.throwing(PluginProperties.class.toString(), "saveChange",
                    ioe);
        }
        
        
    }
    public File getInstallRoot() {
        return installRoot;
    }
    
    
    /** set the plugin's install root
     *
     * this simple Javabean setter has to be preverted to not throw a
     * PropertyvetoException, since netbeans seems to freak out about it.
     *
     * Instead you need to throw an IllegalArgumentException decorated
     * with an ErrorManager annotation.
     */
    public void setInstallRoot(File fo)  {
        
        if (this.isGoodAppServerLocation(fo)){
            installRoot = fo;
            System.setProperty(INSTALL_ROOT_PROP_NAME, fo.getAbsolutePath());
            saveProperties();
            Installer.resetClassLoader();//TODO do  better for next release: separate options to this PluginProperties stuff.
            /////// setDisplayName(factory.getDisplayName()); //NOI18N
        } else{
            //   String mess = pve.getMessage();
            //   org.openide.awt.StatusDisplayer.getDefault().setStatusText(mess);
            //   Util.showInformation(mess);
        }
        
        
    }
    
    private  String getDefaultInstallRoot() {
        String candidate = System.getProperty(INSTALL_ROOT_PROP_NAME); //NOI18N
        if (null != candidate){
            File f = new File(candidate);
            if (f.exists()){
                return candidate;
            }
            
        }
        InstalledFileLocator fff= InstalledFileLocator.getDefault();
        
	File ff = new File(System.getProperty("netbeans.home"));

	File f3 = new File(ff.getParentFile(),COBUNDLE_DEFAULT_INSTALL_PATH);
        if (f3!=null){
	    //System.out.println(f3);
            return f3.getAbsolutePath();
        } 
        
        
        String retVal = "";
        
        return retVal;
    }
    
    
    
    boolean hasRequiredChildren(File candidate, Collection requiredChildren) {
        if (null == candidate)
            return false;
        String[] children = candidate.list();
        if (null == children)
            return false;
        if (null == requiredChildren)
            return true;
        java.util.List kidsList = java.util.Arrays.asList(children);
        return kidsList.containsAll(requiredChildren);
    }
    
    private static Collection fileColl = new java.util.ArrayList();
    
    static {
        fileColl.add("bin");
        fileColl.add("lib");
        fileColl.add("appserv_uninstall.class");
    }
    
    public boolean isGoodAppServerLocation(File candidate){
        if (null == candidate || !candidate.exists() || !candidate.canRead() ||
                !candidate.isDirectory()  || !hasRequiredChildren(candidate, fileColl)) {
            
            return false;
        }
        //now test for AS 9 (J2EE 5.0) which should work for this plugin
        File as9 = new File(candidate.getAbsolutePath()+"/lib/dtds/sun-web-app_2_5-0.dtd");
        if(as9.exists()){
            return true;//we are as9
        }
        
        //one extra test to detect 8.0 versus 8.1: dom.jar has to be in lib not endorsed anymore:
        File f = new File(candidate.getAbsolutePath()+"/lib/dom.jar");
        return f.exists();
        
    }
    public boolean isCurrentAppServerLocationValid(){

        return isGoodAppServerLocation(installRoot);
    }
    
    public void registerDefaultDomain(){
        String username ="admin";//default//NOI18N
        String password ="adminadmin";//default//NOI18N
        
        //try to read real password default:
        File f = new File(System.getProperty("user.home")+"/.asadminprefs"); //NOI18N
        try{
            
            FileInputStream fis = new FileInputStream(f);
            Properties p = new Properties();
            p.load(fis);
            fis.close();
            
            Enumeration e = p.propertyNames() ;
            for ( ; e.hasMoreElements() ;) {
                String v = (String)e.nextElement();
                if (v.equals("AS_ADMIN_USER"))//admin user//NOI18N
                    username = p.getProperty(v );
                else if (v.equals("AS_ADMIN_PASSWORD")){ // admin password//NOI18N
                    password = p.getProperty(v );
                }
            }
            
        } catch (Exception e){
            //either the file does not exist or not available. No big deal, we continue ands NB will popup the request dialog.
        }
        try {
            // Go to the conf dir
            File confDir = new File(installRoot.getAbsolutePath()+"/domains/domain1/config");
            // if it is writable
            if (confDir.exists() && confDir.isDirectory() && confDir.canWrite()) {
                // try to get the host/port data
                String hp = getHostPort(new File(confDir,"domain.xml"));//NOI18N
                if (hp==null){
                    // if we did not get the data out of domain.xml; use a default
                    hp ="localhost:4848";
                }
                String dmUrl = "deployer:Sun:AppServer::"+hp; //NOI18N
                InstanceProperties instanceProperties = InstanceProperties.createInstanceProperties(dmUrl, username, password, hp);
                instanceProperties.setProperty("displayName", NbBundle.getMessage(PluginProperties.class, "OpenIDE-Module-Name"));
                instanceProperties.setProperty("DOMAIN", "domain1");
                //  The LOCATION is the domains directory, not the install root now...
                instanceProperties.setProperty("LOCATION", installRoot.getAbsolutePath()+File.separator+"domains"+File.separator);
            } else {
                // TODO: tell the user we did not register the "default instance"
                // this is hard since most of the time doing UI stuff in this class
                // causes dead-locks.
            }
            
        }catch (InstanceCreationException e){
            ///  Util.showInformation(e.getLocalizedMessage(), NbBundle.getMessage(RegisterServerAction.class, "LBL_RegServerFailed"));
        }
    }
    private String getHostPort(File domainXml){
        String adminHostPort = null;
        try{
            Class[] argClass = new Class[1];
            argClass[0] = File.class;
            Object[] argObject = new Object[1];
            argObject[0] = domainXml;
            
            ExtendedClassLoader loader = Installer.getClassLoader();
            if(loader != null){
                Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.bridge.AppServerBridge");
                Method getHostPort = cc.getMethod("getHostPort", argClass);//NOI18N
                adminHostPort = (String)getHostPort.invoke(null, argObject);
            }
        }catch(Exception ex){
            //Suppressing exception while trying to obtain admin host port value
            ex.printStackTrace();
        }
        return adminHostPort;
    }
    /** Extract a String[] from a Properties
     *
     * The prefix identifies the root property name. Elements of
     * the array are represented in the Properties object as
     * keys that share a common prefix and are numbered, consecutively,
     * starting at zero.
     *
     * This method always returns a String[] object.  It will never
     * return null;
     * @param inProps Properties object the hosts the array.
     * @param prefix The prefix of the key values to identify elements
     * @return The value of properties found with the prefix
     */    
    private static String[] getArrayPropertyValue(Properties inProps, String prefix) {
        String prototype[] = new String[0];
        java.util.List l = new java.util.ArrayList();
            int index = 0;
            String entry = null;
            do {
                entry = inProps.getProperty(prefix+index);
                index++;
                if (null != entry) 
                    l.add(entry);
            }
            while (null != entry);
            Object [] retVal = l.toArray(prototype);
        return (String[]) retVal;
    }
    
    /** Inter a String[] into a Properties object
     *
     * Values are store as properties so they can be extracted
     * from the array by getArrayPropertyValue.
     * @param props Propeties object that will hold the array values.
     * @param prefix The prefix used for creating the property key
     *
     * @param values The array of values to be interred.
     */    
    private static void setArrayPropertyValue(Properties props, String prefix, String[] values) {
        int len = 0;
        if (null != values) 
            len = values.length;
        int index = 0;
        for (int i = 0; i < len; i++) {
            if (null != values[i]) {
                props.setProperty(prefix+index,values[i]);
                index++;
            }
        }
    }}
