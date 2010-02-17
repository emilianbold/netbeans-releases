/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import org.netbeans.api.keyring.Keyring;

import org.netbeans.modules.glassfish.eecommon.api.RegisterDatabase;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.Asenv;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.netbeans.modules.j2ee.sun.ide.editors.CharsetDisplayPreferenceEditor;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;
import org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel;
import org.netbeans.modules.j2ee.sun.share.CharsetMapping;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author  ludo
 */
public class PluginProperties  {
    
    java.util.logging.Logger jsr88Logger =
        java.util.logging.Logger.getLogger("com.sun.enterprise.tools.jsr88.spi");
    
    private  String logLevel = null;
    
    final private  FileObject propertiesFile;
    /* for handling import issues between 5.0 and 5.5. Now we know which IDE the options are from in import.
     **/
    private static final String PLUGIN_PROPERTIES_VERSION = "version"; // NOI18N
    
    /* this will need to be bumped for 6.0. Needed from import setting between versions, and pre-regsitered App Server(8.2, 9. or later 9.x)
     *
     **/
    private static final String PLUGIN_CURRENT_VERSION = "5.5"; // NOI18N
    private static final String PRINCIPAL_PREFIX = "principalEntry."; // NOI18N
    private static final String GROUP_PREFIX = "groupEntry."; // NOI18N
    private static final String LOG_LEVEL_KEY = "logLevel";  // NOI18N
    private static final String CHARSET_DISP_PREF_KEY = "charsetDisplayPreference"; // NOI18N
    private static final String PROP_FIRST_RUN = "first_run";
    
    public static final String COBUNDLE_DEFAULT_INSTALL_PATH ="AS9.0";  //NOI18N
    public static final String COBUNDLE_DEFAULT_INSTALL_PATH2 ="AS8.2";  //NOI18N
    
    public static final String REMOVED_DEFAULT_GFV2 = "removedDefaultGFV2"; //NOI18N

    static private PluginProperties thePluginProperties=null;

    public static PluginProperties getDefault(){
        if (thePluginProperties==null) {
            thePluginProperties= new PluginProperties();
        }
        return thePluginProperties;
    }
    
    
    
    private  PluginProperties(){
        java.io.InputStream inStream = null;
        propertiesFile = getPropertiesFile();
        try {
            try {
                if (null != propertiesFile){
                    inStream = propertiesFile.getInputStream();
                }
            } catch (java.io.FileNotFoundException fnfe) {
                Constants.pluginLogger.info(NbBundle.getMessage(PluginProperties.class, "INFO_NO_PROPERTY_FILE")); //NOI18N
            } catch (java.io.IOException ioe) {                
                Constants.pluginLogger.info(NbBundle.getMessage(PluginProperties.class, "ERR_READING_PROPERTIES")); //NOI18N
                Constants.pluginLogger.throwing(PluginProperties.class.getName(), "<init>", //NOI18N
                        ioe);
            } finally {               
                Properties inProps = new Properties();
                if (null != inStream){
                    inProps.load(inStream);
                    inStream.close();
                }
                loadPluginProperties(inProps);
            }
        } catch (java.io.IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING,ioe);
        }
        
    }
    
    private FileObject getPropertiesFile() { // throws java.io.IOException {
        FileObject dir = FileUtil.getConfigFile("J2EE");
        FileObject retVal = null;
        if (null != dir) {
            retVal = dir.getFileObject("platform","properties"); // NOI18N
            if (null == retVal) {
                try {
                    retVal = dir.createData("platform","properties"); //NOI18N
                } catch (IOException ioe) {
                    Constants.pluginLogger.info(NbBundle.getMessage(PluginProperties.class, "ERR_READING_PROPERTIES")); //NOI18N
                    Constants.pluginLogger.throwing(PluginProperties.class.getName(), "<init>", //NOI18N
                            ioe);
                }
            }
        }
        return retVal;
    }
    
    
    void loadPluginProperties(Properties inProps) {
        
        logLevel = inProps.getProperty(LOG_LEVEL_KEY, java.util.logging.Level.OFF.toString());
        String[] inputUsers = getArrayPropertyValue(inProps, PRINCIPAL_PREFIX);
        String[] inputGroups = getArrayPropertyValue(inProps, GROUP_PREFIX);
        
        setCharsetDisplayPreferenceStatic(Integer.valueOf(inProps.getProperty(CHARSET_DISP_PREF_KEY, "1")));
        String version = inProps.getProperty(PLUGIN_PROPERTIES_VERSION);//old style 5.0: we need to import and refresh
        boolean needToRegisterDefaultServer = false;

        if (!NbPreferences.forModule(PluginProperties.class).getBoolean(PROP_FIRST_RUN, false)) {
            if ((version==null)||(version!=PLUGIN_CURRENT_VERSION)){ //we are currently on a 5.5
                needToRegisterDefaultServer = true;
            }
            NbPreferences.forModule(PluginProperties.class).putBoolean(PROP_FIRST_RUN, true);
            String prop = System.getProperty(ServerLocationManager.INSTALL_ROOT_PROP_NAME);
            RegisterDatabase.getDefault().setupDerby(prop);
        } else {
            if (!NbPreferences.forModule(PluginProperties.class).getBoolean(REMOVED_DEFAULT_GFV2, false)) {
                needToRegisterDefaultServer = true;
            }
        }

        if (needToRegisterDefaultServer){
            final File platformRoot = new File(getDefaultInstallRoot());
            
            if (isGoodAppServerLocation(platformRoot)){
                registerDefaultDomain(platformRoot);
                saveProperties();
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
    
    static public void configureDefaultServerInstance(){
        PluginProperties.getDefault();//call needed for init for this

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
        int len = pModel.getRowCount();
        String retVal[] = new String[len];
        for (int i = 0; i < len; i++) {
            String foo = pModel.getRow(i).toString();
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
        if (setUserListStatic(list)) {
            saveProperties();
        }
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
        if (setGroupListStatic(list)) {
            saveProperties();
        }
    }
    
    
    boolean containsSameElements(SecurityMasterListModel pModel, String[] list) {
        int len = pModel.getRowCount();
        if (len != list.length)
            return false;
        else
            for (int i = 0; i < len; i++) {
                if (!pModel.contains(list[i])) {
                    return false;
                }
            }
        return true;
    }
    
    
    
    private void fillMasterList(SecurityMasterListModel pModel, String[] values) {
        int len = values.length;
        for (int i = pModel.getRowCount() - 1; i >= 0; i--) {
            pModel.removeElementAt(i);
        }
        for (int i = 0; i < len; i++) {
            if (!pModel.contains(values[i])) {
                pModel.addElement(values[i]);
            }
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
            saveProperties();
        }
    }
    
    
    private void saveProperties(){
        Properties outProp = new Properties();
      
        // we store our current version
        outProp.setProperty(PLUGIN_PROPERTIES_VERSION, PLUGIN_CURRENT_VERSION);
        
        setArrayPropertyValue(outProp, PRINCIPAL_PREFIX, getUserList());
        
        setArrayPropertyValue(outProp, GROUP_PREFIX, getGroupList());
        
        if (!logLevel.equals(Level.OFF.toString())){
            outProp.setProperty(LOG_LEVEL_KEY, logLevel);
        }
        if (!getCharsetDisplayPreferenceStatic().equals(CharsetDisplayPreferenceEditor.DEFAULT_PREF_VAL)){
	    outProp.setProperty(CHARSET_DISP_PREF_KEY, getCharsetDisplayPreferenceStatic().toString());
        }

        FileLock l = null;
        java.io.OutputStream outStream = null;
        try {
            if (null != propertiesFile) {
                try {
                    l = propertiesFile.lock();
                    outStream = propertiesFile.getOutputStream(l);
                    if (null != outStream){
                        outProp.store(outStream, "");
                    }
                } catch (java.io.IOException ioe) {
                    Constants.pluginLogger.severe(
                            NbBundle.getMessage(PluginProperties.class, "ERR_SAVING_PROPERTIES") // NOI18N
                            );
                    Constants.pluginLogger.throwing(PluginProperties.class.toString(), "saveChange", //NOI18N
                            ioe);
                } finally {
                    if (null != outStream){
                        outStream.close();
                    }
                    if (null != l){
                        l.releaseLock();
                    }
                }
            }
        } catch (java.io.IOException ioe) {
            Constants.pluginLogger.throwing(PluginProperties.class.toString(), "saveChange",ioe);
        }
    }
    



    
    public  static String getDefaultInstallRoot() {
        String candidate = System.getProperty(ServerLocationManager.INSTALL_ROOT_PROP_NAME); //NOI18N
        if (null != candidate){            
            File f = new File(candidate);
            if (f.exists()){
                return candidate;
            }            
        }
        
        File ff = new File(System.getProperty("netbeans.home"));
        
        File f3 = new File(ff.getParentFile(),COBUNDLE_DEFAULT_INSTALL_PATH);
        if ((f3!=null)&&(f3.exists())){
            return f3.getAbsolutePath();
        }
        f3 = new File(ff.getParentFile(),COBUNDLE_DEFAULT_INSTALL_PATH2);
        if ((f3!=null)&&(f3.exists())){
            return f3.getAbsolutePath();
        }
        
        
        return "";
    }
    
    
    
    static boolean  hasRequiredChildren(File candidate, Collection requiredChildren) {
        if (null == candidate) {
            return false;
        }
        String[] children = candidate.list();
        if (null == children) {
            return false;
        }
        if (null == requiredChildren) {
            return true;
        }
        java.util.List kidsList = java.util.Arrays.asList(children);
        return kidsList.containsAll(requiredChildren);
    }
    
    private static Collection fileColl = new java.util.ArrayList();
    
    static {
        fileColl.add("bin");//NOI18N
        fileColl.add("lib");//NOI18N
        fileColl.add("config");//NOI18N
    }
    

    
    private static boolean isGoodAppServerLocation(File candidate){
        if (null == candidate || !candidate.exists() || !candidate.canRead() ||
                !candidate.isDirectory()  || !hasRequiredChildren(candidate, fileColl)) {
           
            return false;
        }
        //now test for AS 9 (J2EE 5.0) which should work for this plugin
        if(ServerLocationManager.isGlassFish(candidate)){
           return true;//we are as9
        }
        
        //one extra test to detect 8.0 versus 8.1: dom.jar has to be in lib not endorsed anymore:
//        File f = new File(candidate.getAbsolutePath()+"/lib/dom.jar");
//        return f.exists();
        return true;
    }

    
    private static void registerDefaultDomain(File platformRoot){
        String username ="admin";//default//NOI18N
        String password ="adminadmin";//default//NOI18N
        
        //try to read real password default:
        File f = new File(System.getProperty("user.home")+"/.asadminprefs"); //NOI18N
        FileInputStream fis = null;
        if (f.exists()) {
            try{
                
                Properties p = new Properties();
                fis = new FileInputStream(f);
                p.load(fis);
                
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
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,e);
            } finally {
                if (null != fis) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,e);                        
                    }
                }
            }
            
        }
        // Get the asenv data
        Asenv asenvContent = new Asenv(platformRoot);
        String defDomainsDirName = asenvContent.get(Asenv.AS_DEF_DOMAINS_PATH);
        File domains = new File(defDomainsDirName);
        if (domains.exists() && domains.isDirectory() ) {
            File[] domainsList= domains.listFiles();
            if(domainsList==null) {
                return;
            }
            for (int i=0;i<domainsList.length;i++){
                
                try {
                    
                    File confDir = new File(domainsList[i].getAbsolutePath()+"/config");//NOI18N
                    // if it is writable
                    if (confDir.exists() && confDir.isDirectory() && Utils.canWrite(confDir)) {
                        // try to get the host/port data
                        String hp = Util.getHostPort(domainsList[i],platformRoot);
                        if (hp!=null){
                            
                            
                            String dmUrl = "["+platformRoot.getAbsolutePath()+"]" +SunURIManager.SUNSERVERSURI+hp; //NOI18N
                            String displayName = NbBundle.getMessage(PluginProperties.class, "OpenIDE-Module-Name") ;//NOI18N
                            if (ServerLocationManager.isGlassFish(platformRoot)){
                                int versionId = ServerLocationManager.getAppServerPlatformVersion(platformRoot);
                                if (versionId == ServerLocationManager.GF_V1) {
                                    displayName = NbBundle.getMessage(PluginProperties.class, "LBL_GLASSFISH_V1");  // NOI18N   
                                } else if (versionId == ServerLocationManager.GF_V2) {
                                    displayName = NbBundle.getMessage(PluginProperties.class, "LBL_GLASSFISH_V2");  // NOI18N
                                } else if (versionId == ServerLocationManager.GF_V2point1) {
                                    displayName = NbBundle.getMessage(PluginProperties.class, "LBL_GLASSFISH_V2point1");  // NOI18N
                                } else if (versionId == ServerLocationManager.GF_V2point1point1) {
                                    displayName = NbBundle.getMessage(PluginProperties.class, "LBL_GLASSFISH_V2point1point1");  // NOI18N
                                }
                            }

                            FileObject dir = FileUtil.getConfigFile("J2EE/InstalledServers"); // NOI18N
                            FileObject instanceFOs[] = dir.getChildren();

                            displayName = generateDisplayName(displayName, instanceFOs);

                            FileObject instanceFO = null;
                            for (int j = 0; j < instanceFOs.length; j++) {
                                if (dmUrl.equals(instanceFOs[j].getAttribute(InstanceProperties.URL_ATTR))) {
                                    return;
                                }
                            }
                            if (instanceFO == null) {
                                String name = FileUtil.findFreeFileName(dir, "instance", null); // NOI18N
                                instanceFO = dir.createData(name);
                            }
                            instanceFO.setAttribute(InstanceProperties.URL_ATTR, dmUrl);
                            instanceFO.setAttribute(InstanceProperties.USERNAME_ATTR, username);
                            instanceFO.setAttribute(InstanceProperties.PASSWORD_ATTR, GlassfishModule.PASSWORD_CONVERTED_FLAG);
                            Keyring.save(dmUrl,password.toCharArray(), " a GlassFish/SJSAS password");
                            instanceFO.setAttribute(InstanceProperties.DISPLAY_NAME_ATTR, displayName);
                            instanceFO.setAttribute("DOMAIN", domainsList[i].getName()); // NOI18N
                            //  The LOCATION is the domains directory, not the install root now...
                            instanceFO.setAttribute("LOCATION", asenvContent.get(Asenv.AS_DEF_DOMAINS_PATH)); // NOI18N
                            instanceFO.setAttribute(DeploymentManagerProperties.HTTP_MONITOR_ATTR,
                                Boolean.FALSE.toString());
                        }
                    }
                } catch (IOException ioe){
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    ///  Util.showInformation(e.getLocalizedMessage(), NbBundle.getMessage(RegisterServerAction.class, "LBL_RegServerFailed"));
                }
            }
        }
            
            
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
                if (null != entry) {
                    l.add(entry);
                }
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
        if (null != values)  {
            len = values.length;
        }
        int index = 0;
        for (int i = 0; i < len; i++) {
            if (null != values[i]) {
                props.setProperty(prefix+index,values[i]);
                index++;
            }
        }
    }

    //Fix for 144222- picked up from the IDE codebase to mimic behaviour in Add Server wizard first panel
    private static String generateDisplayName(String serverName, FileObject[] instanceFOs) {
        String name;
        int count = 0;

        do {
            name = serverName;
            if (count != 0){
                name += " (" + String.valueOf(count) + ")"; //NOI18N
            }
            count++;
        } while (displayNameExists(name, instanceFOs) != false);
        return name;
    }

    private static boolean displayNameExists(String serverName, FileObject[] instanceFOs) {
        for (int j = 0; j < instanceFOs.length; j++) {
            if (serverName.equals(instanceFOs[j].getAttribute(InstanceProperties.DISPLAY_NAME_ATTR))) {
                return true;
            }
        }
        return false;
    }
 }
