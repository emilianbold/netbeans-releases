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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.theme;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
//import javax.portlet.PortletRequest;

import com.sun.rave.web.ui.component.Icon;
import com.sun.rave.web.ui.util.ClassLoaderFinder;
import com.sun.rave.web.ui.util.ClientSniffer;
import com.sun.rave.web.ui.util.ClientType;
import com.sun.rave.web.ui.util.MessageUtil;
import java.beans.Beans;


/**
 * <p>The Theme class is responsible for managing application
 * resources such as style sheets, JavaScript files and message
 * files.</p>
 * <p>Theme resources are delived in the form of Jar files which are
 * placed in the classpath of the application or of the Servlet
 * container. Themes must include a file called
 * <code>META-INF/swc_theme.properties</code> which describe the
 * resources available to the theme.</p>
 * <p>To configure the default Theme to be used by an application,
 * ensure that the theme jar is the application's classpath and
 * configure the application's deployment descriptor to set the
 * context parameter <code>com.sun.rave.web.ui.DEFAULT_THEME</code> to the
 * Theme's name. </code> 
 * <p>If you need to add additional locale support, you have two
 * options:</p> 
 * <ul> 
 * <li>If your application only uses a single Theme, you can find out
 * what the message file for the Theme is called and provide a message
 * file for your locale using the same namebase in the application's
 * classpath. The usual fallback mechanism will apply. </li> 
 * <li> If your application uses multiple Themes, you can specify an
 * alternative message file to be used by all Themes using the context
 * parameter <code>com.sun.rave.web.ui.MESSAGES</code>. The themes will
 * attempt to retrieve messages from the bundle(s) of this basename
 * first. If the message key does not resolve, the Theme's default
 * bundles are used instead. </li> 
 */

public class Theme  {
    
    private ResourceBundle bundle = null; 
    private ResourceBundle fallbackBundle = null; 
    private ResourceBundle classMapper = null;
    private ResourceBundle imageResources = null; 
    private ResourceBundle jsFiles = null; 
    private ResourceBundle stylesheets = null; 
    private String[] globalJSFiles = null;
    private String[] globalStylesheets = null;
     
    /**
     * Attribute name used to store the user's theme name in the Session
     */
    public static final String THEME_ATTR = "com.sun.rave.web.ui.Theme";
    /** The context parameter name used to specify a console path, if one is used. */
    public static final String RESOURCE_PATH_ATTR = 
	"com.sun.web.console.resource_path";


    private static final String HEIGHT_SUFFIX = "_HEIGHT";
    private static final String WIDTH_SUFFIX = "_WIDTH";
    private static final String GLOBAL_JSFILES = ThemeJavascript.GLOBAL;
    private static final String GLOBAL_STYLESHEETS = ThemeStyles.GLOBAL;
    private static final String MASTER_STYLESHEET = ThemeStyles.MASTER;
    private String prefix = null; 
    private String path = null; 
    private Locale locale = null; 
    private boolean realServer = true;
    
    
    private static final boolean DEBUG = false;
   
    public Theme(Locale locale) { 
        realServer = !java.beans.Beans.isDesignTime();   
        this.locale = locale;
    }
    
    /**
     * Use this method to retrieve a String array of URIs
     * to the JavaScript files that should be included 
     * with all pages of this application
     * @return String array of URIs to the JavaScript files
     */
    public String[] getGlobalJSFiles() {
	 
	if(DEBUG) log("getGlobalJSFiles()"); 

	if(globalJSFiles == null) { 

	    try {

		String files = jsFiles.getString(GLOBAL_JSFILES);
		StringTokenizer tokenizer = new StringTokenizer(files, " ");
		String pathKey = null;
		String path = null; 

		ArrayList fileNames = new ArrayList();

		while(tokenizer.hasMoreTokens()) {
		    pathKey = tokenizer.nextToken();
		    path = jsFiles.getString(pathKey); 
		    fileNames.add(translateURI(path));
		}
		int numFiles = fileNames.size(); 
		globalJSFiles = new String[numFiles]; 
		for(int i=0;i<numFiles; ++i) { 
		    globalJSFiles[i] = fileNames.get(i).toString(); 
		}
	    } 
	    catch(MissingResourceException npe) {
		// Do nothing - there are no global javascript files
		globalJSFiles = new String[0];
	    }
	}       
        return globalJSFiles; 
    }

    /**
     * Use this method to retrieve a String array of URIs
     * to the CSS stylesheets files that should be included 
     * with all pages of this application
     * @return String array of URIs to the stylesheets
     */
    public String[] getGlobalStylesheets() {
	if(globalStylesheets == null) { 

	    try {
		String files = stylesheets.getString(GLOBAL_STYLESHEETS);
		StringTokenizer tokenizer = new StringTokenizer(files, " ");
		String pathKey = null; 
		String path = null;
		ArrayList fileNames = new ArrayList();

		while(tokenizer.hasMoreTokens()) {

		    pathKey = tokenizer.nextToken();
		    path = stylesheets.getString(pathKey); 
		    fileNames.add(translateURI(path));
		}
		int numFiles = fileNames.size(); 
		globalStylesheets = new String[numFiles]; 
		for(int i=0;i<numFiles; ++i) { 
		    globalStylesheets[i] = fileNames.get(i).toString(); 
		}
 
	    } catch(MissingResourceException npe) {
		// There was no "global" key
		// Do nothing
		globalStylesheets = new String[0];
	    }
	}
	return globalStylesheets;
    }

    /**
     * Returns a String that represents a valid path to the JavaScript
     * file corresponding to the key
     * @return Returns a String that represents a valid path to the JavaScript
     * file corresponding to the key
     * @param key Key to retrieve the javascript file
     */
    public String getPathToJSFile(String key) {
	if(DEBUG) log("getPathToJSFile()"); 
        String path = jsFiles.getString(key); 
	if(DEBUG) log("path is " + translateURI(path)); 
        return translateURI(path); 
    } 
 

    /**
     * Returns a String that represents a valid path to the CSS stylesheet
     * corresponding to the key
     * @param context FacesContext of the request
     * @return  A String that represents a valid path to the CSS stylesheet
     * corresponding to the key
     */
    public String getPathToStylesheet(FacesContext context) {
        
	if(DEBUG) log("getPathToStyleSheet()"); 

        ClientType clientType = ClientSniffer.getClientType(context);
        if(DEBUG) log("Client type is " + clientType.toString());
        try { 
            String path = stylesheets.getString(clientType.toString()); 
            if(DEBUG) { 
                log(path);
                log(translateURI(path)); 
            } 
            if (path == null || path.length() == 0) {
                return null;
            } else {
                return translateURI(path); 
            }
        }
        catch(MissingResourceException mre) { 
            StringBuffer msgBuffer = new StringBuffer("Could not find propery ");
            msgBuffer.append(clientType.toString()); 
            msgBuffer.append(" in ResourceBundle "); 
            msgBuffer.append(stylesheets.toString());
            throw new RuntimeException(msgBuffer.toString());
        }
    }
    
    /**
     * Returns a String that represents a valid path to the CSS stylesheet
     * corresponding to the key
     * @return  A String that represents a valid path to the CSS stylesheet
     * corresponding to the key
     */
    public String getPathToMasterStylesheet() {
        
        try { 
            String path = stylesheets.getString(MASTER_STYLESHEET); 
            if (path == null || path.length() == 0) {
                return null;
            } else {
                return translateURI(path); 
            }
        }
        catch(MissingResourceException mre) { 
            StringBuffer msgBuffer = new StringBuffer("Could not find master ");
            msgBuffer.append("stylesheet in ResourceBundle "); 
            msgBuffer.append(stylesheets.toString());
            throw new RuntimeException(msgBuffer.toString());
        }
    }
    
     /**
     * Returns a String that represents a valid path to the CSS stylesheet
     * corresponding to the key
     * @return  A String that represents a valid path to the CSS stylesheet
     * corresponding to the key
     */
    public String getPathToStylesheet(String clientName) {
        
	if(DEBUG) log("getPathToStyleSheet()"); 

        try { 
            String path = stylesheets.getString(clientName); 
            if (path == null || path.length() == 0) {
                return null;
            } else {
                return translateURI(path); 
            }
        }
        catch(MissingResourceException mre) { 
            StringBuffer msgBuffer = new StringBuffer("Could not find propery ");
            msgBuffer.append(clientName); 
            msgBuffer.append(" in ResourceBundle "); 
            msgBuffer.append(stylesheets.toString());
            throw new RuntimeException(msgBuffer.toString());
        }
    }

    /**
     * Returns the name of a CSS style. If the Theme includes a class
     * mapper, the method checks it for the presence of a mapping for
     * the CSS class name passed in with the argument. If there 
     * is no mapping, the name is used as is. 
     * 
     * up in the class mapper if there is one, a valid path to the CSS stylesheet
     * corresponding to the key
     * @param name The style class name to be used
     * @return the name of a CSS style.
     */  
    public String getStyleClass(String name) {
        if(classMapper == null) { 
            return name; 
        }
        String styleClass = classMapper.getString(name); 
        return (styleClass == null) ? name : styleClass; 
    }

    /**
     * Retrieves a message from the appropriate ResourceBundle.
     * If the web application specifies a bundle that overrides
     * the standard bundle, that one is tried first. If no override 
     * bundle is specified, or if the bundle does not contain the 
     * key, the key is resolved from the Theme's default bundle.
     * @param key The key used to retrieve the message
     * @return A localized message string
     */
    public String getMessage(String key) {
	String message = null; 
        try { 
            message = bundle.getString(key); 
        }
        catch(MissingResourceException mre) { 
            try { 
                message = fallbackBundle.getString(key); 
            }
            catch(NullPointerException npe) {
                throw mre;  
            }
        }
	return message;
    }


    /**
     * Retrieves a message from the appropriate ResourceBundle.
     * If the web application specifies a bundle that overrides
     * the standard bundle, that one is tried first. If no override 
     * bundle is specified, or if the bundle does not contain the 
     * key, the key is resolved from the Theme's default bundle.
     * @param key The key used to retrieve the message
     * @param params An object array specifying the parameters of
     * the message
     * @return A localized message string
     */
    public String getMessage(String key, Object[] params) {
	String message = getMessage(key);
	MessageFormat mf = new MessageFormat(message, locale); 
        return mf.format(params); 
    }


    // Sets the prefix to be unconditionally prepended for any URI given out
    // by theme.
    /**
     * Sets the prefix to be prepended to the path names of the resources
     * @param p prefix for all URIs in the theme
     */
    protected void setPrefix(String p) {
        prefix = p;     
    }
    
    /**
     * Configures a resource bundle which overrides the standard keys for 
     * retrieving style class names.
     * @param classMapper A ResourceBundle that overrides the standard style
     * class keys
     */
    protected void configureClassMapper(ResourceBundle classMapper) {
        this.classMapper = classMapper;
    }
    
    /**
     * <p>Configures the message bundles. All Themes must contain a default 
     * ResourceBundle for messages, which is configured in the Theme 
     * configuration file. This bundle is passed in as the first parameter
     * (base).</p>
     * <p>Optionally, the web application developer can override 
     * the messages from all themes by specifying a resource bundle
     * in a context init parameter (if they haven't done so, the second 
     * parameter will be null). If the second parameter is non-null, 
     * Theme.getMessage tries to get the message from the override bundle first. 
     * If that fails (or if there is no override bundle), getMessage() tries 
     * the base bundle. </p>
     * @param base The message bundle specified by the Theme 
     * configuration file.
     * @param override A message bundle configured by the user
     * in a context parameter, to override messages from the base bundle.
     */
    protected void configureMessages(ResourceBundle base, ResourceBundle override) {
        if(DEBUG) log("configureMessages()"); 
        if(override == null) { 
            if(DEBUG) log("override is null, bundle is " + override.toString());
            bundle = base;
        }
        else { 
            bundle = override; 
            fallbackBundle = base;
        }
    }
    
    /**
     * <p>Configures the image resource bundle.</p>
     *
     * @param imageResources A ResourceBundle whose keys specify 
     * the available images. 
     */
    protected void configureImages(ResourceBundle imageResources) {
        this.imageResources = imageResources;
    }
    
    /**
     * <p>Configures the JS resource files.</p>
     *
     * @param jsFiles A ResourceBundle whose keys specify the available 
     * JavaScript files
     */
    protected void configureJSFiles(ResourceBundle jsFiles) {
        this.jsFiles = jsFiles;
    }
    /**
     * <p>Configures the stylesheets.</p>
     *
     * @param stylesheets A resource bundle specifying the stylesheet for
     * each @link ClientType 
     */
    protected void configureStylesheets(ResourceBundle stylesheets) {
        this.stylesheets = stylesheets;
    }
    
    /**
     * <p>This method needs to be refactored. The information about what 
     * type of path to generate is available when the Theme is configured, 
     * and it does not vary from request to request. So it should be
     * fixed on startup. </p>
     * @param context FacesContext of the calling application
     * @param uri URI to be translated
     * @return translated URI String
     */
    private String translateURI(String uri) {
        if (uri == null || uri.length() == 0) {
            return null;
        }
        
        if(DEBUG) log("translateURI(). URI is " + uri);
        if(path == null) {
            initializePath(); 
        }
        if(realServer) { 
            if(DEBUG) log("\tPath is " + path.concat(uri));
            return path.concat(uri);
        }    
        if(DEBUG) log("Generating a URL for design view");
        ClassLoader loader =
                ClassLoaderFinder.getCurrentLoader(Theme.class);
       
        if(Beans.isDesignTime()) {
            // NB6 gives warnings if the path has a leading "/". So, strip it off if it has one
            uri = uri.startsWith("/") ? uri.substring(1) : uri;
        }
        
        URL url = loader.getResource(uri);
        if(DEBUG) log("URL is " + url);
        return url.toString();
    }    
    
    private void initializePath() { 

        if(DEBUG) log("initializePath()");
        FacesContext context = FacesContext.getCurrentInstance();
        Object consolePath = context.getExternalContext().getApplicationMap().
	    get(Theme.RESOURCE_PATH_ATTR); 
        if(consolePath == null) { 
            if(DEBUG) log("\tNo console path, use path prefix");
            path = context.getApplication().getViewHandler().
		getResourceURL(context, prefix); 
            if(DEBUG) log("Path is " + path); 
            return;
     
        }
        if(DEBUG) log("\tFound console path..." + consolePath.toString());
        Object request = context.getExternalContext().getRequest(); 
       
        String protocol = null;
        String server = null;
        int port;
        
        if(request instanceof ServletRequest) { 
            ServletRequest sr = (ServletRequest)request; 
            protocol = sr.getScheme();
            server = sr.getServerName(); 
            port = sr.getServerPort();
        }
       
//        else if(request instanceof PortletRequest) { 
//	    PortletRequest pr = (PortletRequest)request; 
//	    protocol = pr.getScheme();
//	    server = pr.getServerName(); 
//	    port = pr.getServerPort();
//        }
        else { 
            String message = "REquest opbject is " + request.getClass().getName(); 
            throw new RuntimeException(message);
        }
        URL url = null;
        try { 
            if(DEBUG) { 
                log("protocol: " + protocol); 
                log("server: " + server);
                log("port " + String.valueOf(port)); 
                log(" consolepath " + consolePath.toString().concat(prefix));
            }
            url = new URL(protocol, server, port, consolePath.toString().concat(prefix));
        }
        catch(MalformedURLException mue) { 
            throw new ThemeConfigurationException("Couldn't figure out resource path");
        }
        
        path = url.toString(); 
        if(DEBUG) log("\tPath is " + path);
    }
    
    public void initializePath(ServletContext context, 
                               HttpServletRequest request) {
  
        if(DEBUG) log("initializePath(ServletContext)");

        if(path != null) {
            return;
        }       

        String pathString = null; 
        Object consolePath = context.getAttribute(Theme.RESOURCE_PATH_ATTR);
        if(consolePath == null) {
            if(DEBUG) log("\tNo console path attribute! Set to " + String.valueOf(consolePath));
            pathString = request.getContextPath(); 
        }
        else { 
            if(DEBUG) log("\tFound console path..." + consolePath.toString());
            pathString = consolePath.toString(); 
            if(pathString.length() > 0 && !pathString.startsWith("/")) 
                pathString = "/".concat(path);
        } 
        path = pathString.concat(prefix);
    }
    
    private void log(String s) { 
	System.out.println(getClass().getName() + "::" + s); //NOI18N
    }

    public Icon getIcon(String identifier) {
   
       Icon icon = new Icon(); 
       icon.setIcon(identifier);  
       if (identifier != null) {
           
            //make sure to setIcon on parent and not the icon itself (which
            //now does the theme stuff in the component
           
            String path = null; 
            try { 
                path = imageResources.getString(identifier);
            } 
            catch(MissingResourceException mre) { 
                Object[] params = { identifier }; 
                String message = MessageUtil.getMessage
                        ("com.sun.rave.web.ui.resources.LogMessages", 
                         "Theme.noIcon", params); 
                throw new RuntimeException(message, mre);
            } 
            
            path = translateURI(path);        
            icon.setUrl(path);
            try { 
                String height = 
                    imageResources.getString(identifier.concat(HEIGHT_SUFFIX));
                int ht = Integer.parseInt(height); 
                icon.setHeight(ht);
            }
            catch(Exception ex) { 
                // Don't do anything...
            }

            try { 
                String width = 
                    imageResources.getString(identifier.concat(WIDTH_SUFFIX));
                int wt = Integer.parseInt(width); 
                icon.setWidth(wt);
            }
            catch(Exception ex) { 
                // Don't do anything...
            }    
        }
        return icon;
    }
}
