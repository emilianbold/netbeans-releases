/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Pattern;

import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.nodes.Node;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Global IDE settings.
 *
 * @author Ian Formanek
 */
public class IDESettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 801136840705717911L;

    /** showToolTipsInIDE property name */
    public static final String PROP_SHOW_TOOLTIPS_IN_IDE = "showToolTipsInIDE"; // NOI18N    
    /** confirmDelete property name */
    public static final String PROP_CONFIRM_DELETE = "confirmDelete"; // NOI18N
    /** home page property name */
    public static final String PROP_HOME_PAGE = "homePage"; // NOI18N
    /** use proxy property name */
    public static final String PROP_USE_PROXY = "useProxy"; // NOI18N
    /** proxy host property name */
    public static final String PROP_PROXY_HOST = "proxyHost"; // NOI18N
    /** proxy port property name */
    public static final String PROP_PROXY_PORT = "proxyPort"; // NOI18N
    /** show file extensions property name */
    public static final String PROP_SHOW_FILE_EXTENSIONS = "showFileExtensions"; // NOI18N
    /** sorting style of Modules node */
    public static final String PROP_MODULES_SORT_MODE = "modulesSortMode"; // NOI18N
    /** Web Browser prefered by user */
    public static final String PROP_WWWBROWSER = "WWWBrowser"; // NOI18N
    /** UI Mode */
    public static final String PROP_UIMODE = "UIMode"; // NOI18N

    /** files that should be ignored 
     * 
     * DO NOT CHANGE THIS PROPERTY NAME without checking that
     * this property name was changed also in GlobalVisibilityQueryImpl
     * in module org.netbeans.modules.masterfs.
     *   
     */
    public static final String PROP_IGNORED_FILES = "IgnoredFiles"; // NOI18N
    
    /** proxy host VM property key */
    public static final String KEY_PROXY_HOST = "http.proxyHost"; // NOI18N
    /** proxy port VM property key */
    public static final String KEY_PROXY_PORT = "http.proxyPort"; // NOI18N
    /** non proxy hosts VM property key */
    public static final String KEY_NON_PROXY_HOSTS = "http.nonProxyHosts"; // NOI18N
    
    public static final int MODULES_SORT_UNSORTED = 0;
    public static final int MODULES_SORT_DISPLAYNAME = 1;
    public static final int MODULES_SORT_CODENAME = 2;
    public static final int MODULES_SORT_ENABLED = 3;
    public static final int MODULES_SORT_URL = 4;
    public static final int MODULES_SORT_CATEGORY = 5;

    // ------------------------------------------
    // properties

    private static boolean showToolTips = true;    
    private static boolean showTips = true;
    private static int lastTip = -1;
    private static boolean confirmDelete = true;
    private static int modulesSortMode = MODULES_SORT_CATEGORY;

    private static boolean useProxy = false;
    private static String proxyHost = System.getProperty(KEY_PROXY_HOST, "");
    private static String proxyPort = System.getProperty(KEY_PROXY_PORT, "");
    
    private static int uiMode = 2; // MDI default
    
    /**
     * GlobalVisibilityQueryImpl in module org.netbeans.modules.masterfs reads this property (hidden dependency).   
     */ 
    private static String ignoredFiles = "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store))$|^\\.[#_]|~$"; //NOI18N    

    /** Getter for properties file with proxy properties. Installer provides
     * this file.*/
    private static Properties getPreInitProxyProps () throws IOException {
        final String ideHome = System.getProperty("netbeans.home"); //NOI18N
        final String propsRelPath = "installer/pre-settings.properties"; //NOI18N        
        final InputStream propsIStream = new FileInputStream (new File(ideHome, propsRelPath));
        
        Properties propsRetVal = new Properties ();
        propsRetVal.load(propsIStream);
        propsIStream.close();
        return propsRetVal;
    }
    
    // do NOT use constructore for setting default values
    protected void initialize () {
        // Set default values of properties        
        super.initialize ();
        
        /** default values for proxy properties taken from installer **/
        String newProxyHost = proxyHost;
        String newProxyPort =  proxyPort ;
        if (proxyHost.length() == 0 || proxyPort.length() == 0) {
            try {
                Properties props =  getPreInitProxyProps();
                newProxyHost = props.getProperty("proxy_host"); //NOI18N
                newProxyPort = props.getProperty("proxy_port"); //NOI18N
            } catch (IOException iox) {
                ;// ProxyHost and ProxyPort won`t be overtaken from installer
            }                      

            if (newProxyHost != null && newProxyPort != null) {
                setProxyHost (newProxyHost);
                setProxyPort(newProxyPort);                
                setUseProxy (newProxyHost.length() != 0 && newProxyPort.length() != 0);
            }
        }        
        putProperty(PROP_WWWBROWSER, "", false);
    }
            
    // ------------------------------------------
    // property access methods

    /** A utility method to avoid unnecessary creation of second URL */
    public static URL getRealHomeURL () {
        try {
            return new URL(NbBundle.getMessage(IDESettings.class, "URL_default_home_page"));
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public int getModulesSortMode () {
        return modulesSortMode;
    }

    public void setModulesSortMode (int nue) {
        int oldValue = modulesSortMode;
        modulesSortMode = nue;
        firePropertyChange (PROP_MODULES_SORT_MODE, new Integer (oldValue), new Integer (nue));
    }

    /** Getter for ShowToolTipsInIDE
     * @return true if dialog will be shown*/
    public boolean getShowToolTipsInIDE() {
        return showToolTips;
    }

    /** Setter for ShowToolTipsInIDE
    * @param value true if on the next start of corona the dialog will be shown
    *              false otherwise */
    public void setShowToolTipsInIDE(boolean value) {
        if (showToolTips == value) return;
        showToolTips = value;
        javax.swing.ToolTipManager.sharedInstance().setEnabled(value);
        // fire the PropertyChange
        firePropertyChange (PROP_SHOW_TOOLTIPS_IN_IDE,
                            !showToolTips ? Boolean.TRUE : Boolean.FALSE,
                            showToolTips ? Boolean.TRUE : Boolean.FALSE);
    }
    
    /** Getter for ConfirmDelete
     * @param true if the user should asked for confirmation of object delete, false otherwise */
    public boolean getConfirmDelete() {
        return confirmDelete;
    }

    /** Setter for ConfirmDelete
     * @param value if true the user is asked for confirmation of object delete, not if false */
    public void setConfirmDelete(boolean value) {
        if (value == confirmDelete) return;
        Boolean oldValue = confirmDelete ? Boolean.TRUE : Boolean.FALSE;
        confirmDelete = value;

        org.openide.explorer.ExplorerPanel.setConfirmDelete (value);

        // fire the PropertyChange
        firePropertyChange (PROP_CONFIRM_DELETE, oldValue, confirmDelete ? Boolean.TRUE : Boolean.FALSE);
    }

    /** This method must be overriden. It returns display name of this options.
    */
    public String displayName () {
        return NbBundle.getBundle(IDESettings.class).getString("CTL_IDESettings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (IDESettings.class);
    }

    /** Getter for home page used in html viewer.
    */
    public String getHomePage () {
        return HtmlBrowser.getHomePage ();
    }

    /** Setter for home page used in html viewer.
    */
    public void setHomePage (String homePage) {
        HtmlBrowser.setHomePage (homePage);
    }

    /** Getter for proxy set flag.
    */
    public boolean getUseProxy () {
        return useProxy;
    }

    /** Setter for proxy set flag.
    */
    public void setUseProxy (boolean value) {
        if (useProxy != value) {
            boolean oldValue = useProxy;
            useProxy = value;
            if (value) {
                // apply the current proxyHost:proxyPort settings
                System.setProperty(KEY_PROXY_HOST, getProxyHost());
                System.setProperty(KEY_PROXY_PORT, getProxyPort());
                System.setProperty(KEY_NON_PROXY_HOSTS, getDefaultNonProxyHosts());
            } else {
                // reset properties so that they don't apply
                System.setProperty(KEY_PROXY_HOST, ""); // NOI18N
                System.setProperty(KEY_PROXY_PORT, ""); // NOI18N
                System.setProperty(KEY_NON_PROXY_HOSTS, ""); // NOI18N
            }
            // notify listeners
            firePropertyChange(PROP_USE_PROXY,
                               oldValue ? Boolean.TRUE : Boolean.FALSE,
                               value ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    /** Getter for proxy host.
    */
    public String getProxyHost () {
        return proxyHost;
    }

    /** Setter for proxy host.
    */
    public void setProxyHost (String value) {
        if (!proxyHost.equals(value)) {
            String oldValue = proxyHost;
            proxyHost = value;
            if (getUseProxy()) {
                System.setProperty(KEY_PROXY_HOST, proxyHost);
            }
            firePropertyChange(PROP_PROXY_HOST, oldValue, proxyHost);
        }
    }

    /** Getter for proxy port. my ffjBuild -update 
    */
    public String getProxyPort () {
        return proxyPort;
    }

    /** Setter for proxy port.
    */
    public void setProxyPort (String value) {
        if (!proxyPort.equals(value)) {
            String oldValue = proxyPort;
            proxyPort = value;
            if (getUseProxy()) {
                System.setProperty (KEY_PROXY_PORT, proxyPort);
            }
            firePropertyChange(PROP_PROXY_PORT, oldValue, proxyPort);
        }
    }

    /** Getter for showing file extensions.
    * @return whether to show them
    */
    public boolean getShowFileExtensions () {
        return DataNode.getShowFileExtensions ();
    }

    /** Setter for showing file extensions.
    * @param s whether to show them
    */
    public void setShowFileExtensions (boolean s) {
        boolean old = getShowFileExtensions ();
        DataNode.setShowFileExtensions (s);
        firePropertyChange (PROP_SHOW_FILE_EXTENSIONS, Boolean.valueOf(old), Boolean.valueOf(s));
    }

    /** Getter for preffered web browser.
     *
     * First time when this function is called Lookup is used 
     * to find prefered browser factory in a browser registry.
     * 
     * @return prefered browser, 
     * may return null if it is not possible to get the browser
     */
    public HtmlBrowser.Factory getWWWBrowser() {
        try {
            Object obj = getProperty (PROP_WWWBROWSER);
            
            if (obj instanceof String && !"".equals (obj)) {
                // use new style
                Lookup.Item item = Lookup.getDefault ().lookupItem (new Lookup.Template (HtmlBrowser.Factory.class, (String)obj, null));
                return item == null ? null : (HtmlBrowser.Factory)item.getInstance ();
            }

            // the browser is not set yet - find the first one
            if (obj == null || "".equals (obj)) {
                Lookup.Result res = Lookup.getDefault ().lookup (new Lookup.Template (HtmlBrowser.Factory.class));
                java.util.Iterator it = res.allInstances ().iterator ();
                while (it.hasNext ()) {
                    Object brow = it.next ();
                    
                    // check if it is not set to be hidden
                    FileObject fo = Repository.getDefault ()
                        .getDefaultFileSystem ().findResource ("Services/Browsers");   // NOI18N
                    
                    DataFolder folder = DataFolder.findFolder (fo);
                    DataObject [] dobjs = folder.getChildren ();
                    for (int i = 0; i<dobjs.length; i++) {
                        Object o = null;
                        try {
                            if (Boolean.TRUE.equals (dobjs[i].getPrimaryFile ().getAttribute ("hidden"))) // NOI18N see LookupNode.EA_HIDDEN
                                continue;
                            
                            InstanceCookie cookie = (InstanceCookie)dobjs[i].getCookie (InstanceCookie.class);
                            if (cookie == null)
                                continue;
                            
                            o = cookie.instanceCreate ();
                            if (o != null 
                            && o.equals (brow)) {
                                return (HtmlBrowser.Factory)brow;
                            }
                        }
                        // exceptions are thrown if module is uninstalled 
                        catch (java.io.IOException ex) {
                            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ex);
                        } catch (ClassNotFoundException ex) {
                            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                    
                }
                return null;
            }
        }
        catch (Exception ex) {
            ErrorManager.getDefault ().notify (ex);
        }
        return null;
    }

    /** Setter for preffered browser.
     *
     *  Actually Node.Handle of node that represent browser in lookup folder is stored.
     *
     * @param brow prefered browser capable of providing implementation
     */
    public void setWWWBrowser(HtmlBrowser.Factory brow) {
        try {
            if (brow == null) {
                putProperty(PROP_WWWBROWSER, "", true);    
                return;
            }
            
            Lookup.Item item = Lookup.getDefault().lookupItem(new Lookup.Template (HtmlBrowser.Factory.class, null, brow));
            if (item != null) {
                putProperty (PROP_WWWBROWSER, item.getId (), true);
            } else {
                // strange
                ErrorManager.getDefault().log ("IDESettings: Cannot find browser in lookup");// NOI18N
                putProperty (PROP_WWWBROWSER, "", true);
            }
        }
        catch (Exception ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }

    /** Sets ui mode (MDI, SDI, Dialog SDI...) */ 
    public void setUIMode (int uiMode) {
        if (this.uiMode == uiMode) {
            return;
        }
        int oldValue = this.uiMode;
        this.uiMode = uiMode;
        firePropertyChange (PROP_UIMODE, new Integer(oldValue), new Integer(uiMode));
    }

    public int getUIMode () {
        return uiMode;
    }

    // PRIVATE METHODS
    
    /** Returns the default value for the http.nonProxyHosts system property. <br>
     *  PENDING: should be a user settable property
     * @return sensible default for non-proxy hosts, including 'localhost'
     */
    private String getDefaultNonProxyHosts() {
        String nonProxy = "localhost"; // NOI18N
        try {
            String localhost = InetAddress.getLocalHost().getHostName();
            if (!localhost.equals("localhost")) { // NOI18N
                nonProxy = nonProxy + "|" + localhost; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        }
        catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
        return nonProxy;
    }

    public String getIgnoredFiles() {
        return ignoredFiles;
    }

    public void setIgnoredFiles(String ignoredFiles) throws IllegalArgumentException {
        if (!this.ignoredFiles.equals(ignoredFiles)) {
            try {
                String oldIgnoredfiles = this.ignoredFiles;
                Pattern.compile(ignoredFiles);
                IDESettings.ignoredFiles = ignoredFiles;
                firePropertyChange (PROP_IGNORED_FILES, oldIgnoredfiles, ignoredFiles);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(e.getLocalizedMessage());
            }
        }
    }
}
