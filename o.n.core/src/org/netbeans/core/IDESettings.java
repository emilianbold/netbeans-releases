/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.options.SystemOption;
import org.openide.util.Exceptions;
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
    /** use proxy property name */
    public static final String PROP_PROXY_TYPE = "proxyType"; // NOI18N
    /** proxy host property name */
    public static final String PROP_PROXY_HOST = "userProxyHost"; // NOI18N
    /** proxy port property name */
    public static final String PROP_PROXY_PORT = "userProxyPort"; // NOI18N
    /** show file extensions property name */
    public static final String PROP_SHOW_FILE_EXTENSIONS = "showFileExtensions"; // NOI18N
    /** sorting style of Modules node */
    public static final String PROP_MODULES_SORT_MODE = "modulesSortMode"; // NOI18N
    /** Web Browser prefered by user */
    public static final String PROP_WWWBROWSER = "WWWBrowser"; // NOI18N
    /** UI Mode */
    public static final String PROP_UIMODE = "UIMode"; // NOI18N
     /** Non Proxy Hosts */
    public static final String PROP_NON_PROXY_HOSTS = "userNonProxy"; // NOI18N

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
    /** https proxy host VM property key */
    public static final String KEY_HTTPS_PROXY_HOST = "https.proxyHost"; // NOI18N
    /** https proxy port VM property key */
    public static final String KEY_HTTPS_PROXY_PORT = "https.proxyPort"; // NOI18N
    /** non proxy hosts VM property key */
    public static final String KEY_HTTPS_NON_PROXY_HOSTS = "https.nonProxyHosts"; // NOI18N
    
    public static final int MODULES_SORT_UNSORTED = 0;
    public static final int MODULES_SORT_DISPLAYNAME = 1;
    public static final int MODULES_SORT_CODENAME = 2;
    public static final int MODULES_SORT_ENABLED = 3;
    public static final int MODULES_SORT_URL = 4;
    public static final int MODULES_SORT_CATEGORY = 5;

    /** No proxy is used to connect.*/
    public static final int DIRECT_CONNECTION = 0;
    /** Proxy setting is automaticaly detect in OS.*/
    public static final int AUTO_DETECT_PROXY = 1; // as default
    /** Manualy set proxy host and port.
     * @see setUserProxyHost, setUserProxyPort
     */
    public static final int MANUAL_SET_PROXY = 2;
    // ------------------------------------------
    // properties

    private static boolean showToolTips = true;    
    private static boolean confirmDelete = true;
    private static int modulesSortMode = MODULES_SORT_CATEGORY;

    private static boolean useProxy = false;
    private static int proxyType = -1; // not initialized
    private static String userProxyHost = System.getProperty(KEY_PROXY_HOST, "");
    private static String userProxyPort = System.getProperty(KEY_PROXY_PORT, "");
    private static String userNonProxyHosts;
    private static String defaultNonProxyHosts;
    
    private static int uiMode = 2; // MDI default
    
    /**
     * GlobalVisibilityQueryImpl in module org.netbeans.modules.masterfs reads this property (hidden dependency).   
     */ 
    private static String ignoredFiles = "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store)|_svn)$|^\\.[#_]|~$"; //NOI18N    
    
    // do NOT use constructore for setting default values
    protected void initialize () {
        // Set default values of properties        
        super.initialize ();
        this.defaultNonProxyHosts = getDefaultNonProxyHosts ();
        this.userNonProxyHosts = this.defaultNonProxyHosts;
        setProxy ();
        putProperty(PROP_WWWBROWSER, "", false);
    }

    private void setProxy() {
        String host = getProxyHost ();
        String port = getProxyPort();
        String nonProxyHosts = getNonProxyHosts ();
        System.setProperty (KEY_PROXY_HOST, normalizeProxyHost (host));
        System.setProperty (KEY_PROXY_PORT, port);
        System.setProperty (KEY_NON_PROXY_HOSTS, nonProxyHosts);
        System.setProperty (KEY_HTTPS_PROXY_HOST, normalizeProxyHost (host));
        System.setProperty (KEY_HTTPS_PROXY_PORT, port);
        System.setProperty (KEY_HTTPS_NON_PROXY_HOSTS, nonProxyHosts);
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
     * @deprecated Use <code>getProxyType()</code>
    */
    public boolean getUseProxy () {
        return useProxy;
    }

    /** Setter for proxy set flag.
     * @deprecated Use <code>getProxyType()</code>
    */
    public void setUseProxy (boolean value) {
        if (useProxy != value) {
            if (value) {
                setProxyType (MANUAL_SET_PROXY);
            } else {
                // do auto-detect rather then direct connection
                setProxyType (AUTO_DETECT_PROXY);
            }
        }
    }
    
    /**
     * Gets the type of proxy, the dafault value is <code>AUTO_DETECT_PROXY</code>.
     * @see getProxyType
     * @return type of proxy settings
     */
    public int getProxyType () {
        int type = (proxyType == -1) ? AUTO_DETECT_PROXY /* as default */: proxyType;
        if (type == AUTO_DETECT_PROXY && ! isSystemProxyDetect ()) {
            type = DIRECT_CONNECTION;
        }
        return type;
    }
    
    /**
     * Sets the type of proxy. Possible values are:
     * <ul>
     * <li>IDESettings.DIRECT_CONNECTION</li>
     * <li>IDESettings.AUTO_DETECT_PROXY</li>
     * <li>IDESettings.MANUAL_SET_PROXY</li>
     * </ul>
     * @param type of proxy settings
     */
    public void setProxyType (int value) {
        if (proxyType != value) {
            boolean oldUseProxy = getUseProxy ();
            String oldHost = getProxyHost ();
            String oldPort = getProxyPort ();
            this.proxyType = value;
            
            if (oldUseProxy != getUseProxy ()) {
                firePropertyChange (PROP_USE_PROXY, oldUseProxy ? Boolean.TRUE : Boolean.FALSE, getUseProxy () ? Boolean.TRUE : Boolean.FALSE);
            }
            if (!oldHost.equals (getProxyHost ())) {
                firePropertyChange (PROP_PROXY_HOST, oldHost, getProxyHost ());
            }
            if (!oldPort.equals (getProxyPort ())) {
                firePropertyChange (PROP_PROXY_PORT, oldPort, getProxyPort ());
            }
            setProxy();
        }
    }

    /**
     * Returns name of proxy host.
     * @return proxy host
     */
    public String getUserProxyHost () {
        return userProxyHost;
    }
   
    /**
     * Returns name of proxy port.
     * @return proxy port
     */
    public String getUserProxyPort () {
        return userProxyPort;
    }
    
    /**
     * Sets name of proxy host, will used if the proxy type <code>MANUAL_SET_PROXY</code> is set.
     * @param proxy host
     */
    public void setUserProxyHost (String value) {
        value = value == null ? "" : value;
        String oldUserHost = getUserProxyHost ();
        if (!value.equals (oldUserHost)) {
            this.userProxyHost = value;
            if (MANUAL_SET_PROXY == getProxyType ()) {
                System.setProperty (KEY_PROXY_HOST, normalizeProxyHost (value));
                System.setProperty (KEY_HTTPS_PROXY_HOST, normalizeProxyHost (value));
                firePropertyChange (PROP_PROXY_HOST, oldUserHost, value);
            }
        }
    }
   
    /**
     * Sets name of proxy port, will used if the proxy type <code>MANUAL_SET_PROXY</code> is set.
     * @param proxy port
     */
    public void setUserProxyPort (String value) {
        value = value == null ? "" : value;
        String oldUserPort = getUserProxyPort ();
        if (!value.equals (oldUserPort)) {
            this.userProxyPort = value;
            if (MANUAL_SET_PROXY == getProxyType ()) {
                System.setProperty (KEY_PROXY_PORT, value);
                System.setProperty (KEY_HTTPS_PROXY_PORT, value);
                firePropertyChange (PROP_PROXY_PORT, oldUserPort, value);
            }
        }
    }
    
    /**
     * Getter for proxy host.
     * Same value returns <code>System.getValue("http.proxyHost")</code>.
     * @return proxy host
     */
    public String getProxyHost () {
        switch (getProxyType ()) {
            case AUTO_DETECT_PROXY :
                return getSystemProxyHost ();
            case MANUAL_SET_PROXY :
                return getUserProxyHost ();
            case DIRECT_CONNECTION :
                return ""; // NOI18N
            default:
                throw new AssertionError("Unknown proxy type " + getProxyType());
        }
    }
    
    public void setUserNonProxyHosts (String value) {
        value = value == null ? "" : value;
        if (!value.equals (this.userNonProxyHosts)) {
            System.setProperty (KEY_NON_PROXY_HOSTS, value);
            System.setProperty (KEY_HTTPS_NON_PROXY_HOSTS, value);
            firePropertyChange (KEY_NON_PROXY_HOSTS, this.userNonProxyHosts, value);
            this.userNonProxyHosts = value;
        }
    }
    
    public String getUserNonProxyHosts () {
        return this.userNonProxyHosts;
    }
    
    public void readOldProxyHost (String value) {
        setUserProxyHost (value);
    }
    
    public void readOldProxyPort (String value) {
        setUserProxyPort (value);
    }
    
    /** Setter for proxy host, sets the HTTP proxy host if and only if proxy type is <code>MANUAL_SET_PROXY</code>.
     * @deprecated Use setUserProxyHost(String)
     * @param proxy host
     */
    public void setProxyHost (String value) {
        value = value == null ? "" : value;
        if (MANUAL_SET_PROXY == getProxyType ()) {
            if (!getUserProxyHost().equals (value)) {
                setUserProxyHost (value);
                System.setProperty (KEY_PROXY_HOST, normalizeProxyHost (value));
                System.setProperty (KEY_HTTPS_PROXY_HOST, normalizeProxyHost (value));
            }
        }
        assert false : "Don't set proxy host if proxy type " + getProxyType ();
    }

    /**
     * Getter for proxy port.
     * Same value returns <code>System.getValue("http.proxyPort")</code>
     * @return proxy port
     */
    public String getProxyPort () {
        switch (getProxyType ()) {
            case AUTO_DETECT_PROXY :
                return getSystemProxyPort ();
            case MANUAL_SET_PROXY :
                return getUserProxyPort ();
            case DIRECT_CONNECTION :
                return ""; // NOI18N
            default:
                throw new AssertionError("Unknown proxy type " + getProxyType());
        }
    }

    /** Setter for proxy port, sets the HTTP proxy port if and only if proxy type is <code>MANUAL_SET_PROXY</code>.
     * @deprecated Use setUserProxyPort(String)
     * @param proxy port
     */
    public void setProxyPort (String value) {
        value = value == null ? "" : value;
        if (MANUAL_SET_PROXY == getProxyType ()) {
            if (!getUserProxyPort ().equals (value)) {
                setUserProxyPort (value);
                System.setProperty (KEY_PROXY_PORT, getProxyPort ());
                System.setProperty (KEY_HTTPS_PROXY_PORT, getProxyPort ());
            }
        }
        assert false : "Don't set proxy port if proxy type " + getProxyType ();
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
                Lookup.Item<HtmlBrowser.Factory> item = Lookup.getDefault ().lookupItem (new Lookup.Template<HtmlBrowser.Factory> (HtmlBrowser.Factory.class, (String)obj, null));
                return item == null ? null : item.getInstance ();
            }

            // the browser is not set yet - find the first one
            if (obj == null || "".equals (obj)) {
                Lookup.Result<HtmlBrowser.Factory> res = Lookup.getDefault ().lookupResult(HtmlBrowser.Factory.class);
                java.util.Iterator<? extends HtmlBrowser.Factory> it = res.allInstances ().iterator ();
                while (it.hasNext ()) {
                    HtmlBrowser.Factory brow = it.next ();
                    
                    // check if it is not set to be hidden
                    FileObject fo = Repository.getDefault ()
                        .getDefaultFileSystem ().findResource ("Services/Browsers");   // NOI18N
                    
                    DataFolder folder = DataFolder.findFolder (fo);
                    DataObject [] dobjs = folder.getChildren ();
                    for (int i = 0; i < dobjs.length; i++) {
                        Object o = null;

                        try {
                            if (Boolean.TRUE.equals(dobjs[i].getPrimaryFile().getAttribute("hidden")))
                                continue;
                            InstanceCookie cookie = (InstanceCookie) dobjs[i].getCookie(InstanceCookie.class);

                            if (cookie == null)
                                continue;
                            o = cookie.instanceCreate();
                            if (o != null && o.equals(brow)) {
                                return brow;
                            }
                        }
                        // exceptions are thrown if module is uninstalled
                        catch (java.io.IOException ex) {
                            Logger.global.log(Level.WARNING, null, ex);
                        }
                        catch (ClassNotFoundException ex) {
                            Logger.global.log(Level.WARNING, null, ex);
                        }
                    }
                    
                }
                return null;
            }
        }
        catch (Exception ex) {
            Exceptions.printStackTrace(ex);
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
            
            Lookup.Item<HtmlBrowser.Factory> item = 
		    Lookup.getDefault().lookupItem(new Lookup.Template<HtmlBrowser.Factory> (HtmlBrowser.Factory.class, null, brow));
            if (item != null) {
                putProperty (PROP_WWWBROWSER, item.getId (), true);
            } else {
                // strange
                Logger.global.warning("IDESettings: Cannot find browser in lookup");// NOI18N
                putProperty (PROP_WWWBROWSER, "", true);
            }
        }
        catch (Exception ex) {
            Exceptions.printStackTrace(ex);
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
    private static String getDefaultNonProxyHosts() {
        String userPreset = System.getProperty (KEY_NON_PROXY_HOSTS);
        String nonProxy = (userPreset == null ? "" : userPreset + "|") + "localhost|127.0.0.1"; // NOI18N
        String localhost = ""; // NOI18N
        try {
            localhost = InetAddress.getLocalHost().getHostName();
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
        try {
            String localhost2 = InetAddress.getLocalHost().getCanonicalHostName();
            if (!localhost2.equals("localhost") && !localhost2.equals(localhost)) { // NOI18N
                nonProxy = nonProxy + "|" + localhost2; // NOI18N
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
                IllegalArgumentException iae = new IllegalArgumentException();
                iae.initCause( e );
                UIException.annotateUser(iae, e.getMessage(),
                                         e.getLocalizedMessage(), null, null);
                throw iae;
            }
        }
    }
    
    private boolean isSystemProxyDetect () {
        return System.getProperty ("netbeans.system_http_proxy") != null; // NOI18N
    }
    
    private String getSystemProxyHost () {
        String systemProxy = System.getProperty ("netbeans.system_http_proxy"); // NOI18N
        if (systemProxy == null) {
            return ""; // NOI18N
        }

        int i = systemProxy.lastIndexOf (":"); // NOI18N
        if (i <= 0 || i >= systemProxy.length () - 1) {
            return ""; // NOI18N
        }
        
        return systemProxy.substring (0, i);
    }
    
    private static String normalizeProxyHost (String proxyHost) {
        if (proxyHost.toLowerCase ().startsWith ("http://")) { // NOI18N
            return proxyHost.substring (7, proxyHost.length ());
        } else {
            return proxyHost;
        }
    }
   
    private String getSystemProxyPort () {
        String systemProxy = System.getProperty ("netbeans.system_http_proxy"); // NOI18N
        if (systemProxy == null) {
            return ""; // NOI18N
         }

        int i = systemProxy.lastIndexOf (":"); // NOI18N
        if (i <= 0 || i >= systemProxy.length () - 1) {
            return ""; // NOI18N
        }

        return systemProxy.substring (i+1);
    }

    public String getNonProxyHosts () {
        switch (getProxyType ()) {
            case AUTO_DETECT_PROXY :
                return this.defaultNonProxyHosts;
            case MANUAL_SET_PROXY :
                return getUserNonProxyHosts ();
            case DIRECT_CONNECTION :
                return ""; // NOI18N
        }
        
        assert false : "Unknown proxy type " + getProxyType ();
        return null;
    }
    
}
