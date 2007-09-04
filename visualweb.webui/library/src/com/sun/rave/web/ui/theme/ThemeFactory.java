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

import java.beans.Beans;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
//import javax.portlet.PortletContext;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.sun.rave.web.ui.util.ClassLoaderFinder;

/**
 * <p>Factory class responsible for setting up the Sun Web Component
 * application's ThemeManager.</p>
 */
public class ThemeFactory {
    
    /**
     * The init parameter name used to set the default theme name.
     */
    public final static String DEFAULT_THEME =
            "com.sun.rave.web.ui.DEFAULT_THEME"; //NOI18N
    
    /**
     * The init parameter name used to override the default
     * message bundle location.
     */
    public final static String MESSAGES_PARAM =
            "com.sun.rave.web.ui.MESSAGES"; //NOI18N
    
    // Private variables
    private static String messageOverride = null;
    
    // Private message variables
    private final static String WARNING_LOAD =
            "WARNING: the Sun Web Components could not load any themes.";
    private final static String WARNING_BADFILE =
            "WARNING: the Sun Web Components detected a corrupted theme configuration file:\n\t";
    
    // Private attribute names
    public final static String MANIFEST = "META-INF/MANIFEST.MF"; //NOI18N
    public final static String FILENAME = "manifest-file"; //NOI18N
    public final static String COMPONENTS_SECTION = "com/sun/rave/web/ui/"; //NOI18N
    public final static String THEME_SECTION = "com/sun/rave/web/ui/theme/"; //NOI18N
    public final static String THEME_VERSION_REQUIRED = 
        "X-SJWUIC-Theme-Version-Required"; //NOI18N
    public final static String THEME_VERSION = "X-SJWUIC-Theme-Version"; //NOI18N
    public final static String NAME = "X-SJWUIC-Theme-Name"; //NOI18N
    public final static String PREFIX = "X-SJWUIC-Theme-Prefix"; //NOI18N
    public final static String DEFAULT = "X-SJWUIC-Theme-Default"; //NOI18N
    public final static String STYLESHEETS = "X-SJWUIC-Theme-Stylesheets"; //NOI18N
    public final static String JSFILES = "X-SJWUIC-Theme-JavaScript"; //NOI18N
    public final static String CLASSMAPPER = "X-SJWUIC-Theme-ClassMapper"; //NOI18N
    public final static String IMAGES = "X-SJWUIC-Theme-Images"; //NOI18N
    public final static String MESSAGES = "X-SJWUIC-Theme-Messages"; //NOI18N
    
    private static final boolean DEBUG = false;
    
    private ThemeFactory(String messageOverride) {
        this.messageOverride = messageOverride;
    }
    
    /**
     * This method initializes the ThemeManager for the Sun Web
     * Component's application. It is invoked by the ThemeServlet's
     * init method. To initialize the ThemeManager and the Themes
     * during web application startup, declare the load-on-startup
     * property of the ThemeServlet in the web application
     * deployment descriptor.
     * @param context the ServletContext in which the application
     * @param locales a Set of locales supported by the application
     * If this parameter is null, the supported locales will be
     * retrieved from the JSF runtime.
     * is running
     */
    public static ThemeManager initializeThemeManager(ServletContext context,
            Set locales) {
        
        // We don't need to synchronize this method. It must only be
        // invoked during the ThemeServlet's init method.
        
        if(DEBUG) log("initializeThemeManager(ServletContext)");
        if(context.getAttribute(ThemeManager.THEME_MANAGER) != null) {
            if(DEBUG) log("ThemeManager already initialized");
            return null;
        }
        
        // Set the default theme if specified in the DD
        String defaultThemeName =
                processInitParameter(context.getInitParameter(DEFAULT_THEME));
        String messageOverride =
                processInitParameter(context.getInitParameter(MESSAGES_PARAM));
        
        ThemeFactory themeFactory = new ThemeFactory(messageOverride);
        ThemeManager manager = null;
        if(locales == null) {
            manager = themeFactory.createThemeManager(defaultThemeName);
        } else {
            manager = 
                themeFactory.createThemeManager(defaultThemeName, locales, null);
        }
        if(manager == null) {
            return null;
        }
        context.setAttribute(ThemeManager.THEME_MANAGER, manager);
        return manager;
    }
    
    /**
     * This method initializes the ThemeManager for the Sun Web
     * Component's application. It is invoked by the XXX's
     * init method. To initialize the ThemeManager and the Themes
     * during portlet application startup, declare the load-on-startup
     * property of the XXX in the portlet application configuration
     * file.
     * @param context the PortletContext in which the application
     * is running
     */
//    protected static void initializeThemeManager(PortletContext context) {
//        
//        // We don't need to synchronize this method. It must only be
//        // invoked during during the initialization of a
//        // PortletContext
//        
//        if(DEBUG) log("initializeThemeManager(PortletContext)");
//        if(context.getAttribute(ThemeManager.THEME_MANAGER) != null) {
//            if(DEBUG)  log("ThemeManager already initialized");
//        }
//        
//        // Set the default theme if specified in the DD
//        String defaultThemeName =
//                processInitParameter(context.getInitParameter(DEFAULT_THEME));
//        String messageOverride =
//                processInitParameter(context.getInitParameter(MESSAGES_PARAM));
//        
//        ThemeFactory themeFactory = new ThemeFactory(messageOverride);
//        ThemeManager manager =
//                themeFactory.createThemeManager(defaultThemeName);
//        context.setAttribute(ThemeManager.THEME_MANAGER, manager);
//    }
    
    /**
     * Initializes a ThemeManager based on an External Context.
     * This method is invoked by ThemeUtilities.getTheme() if
     * the ThemeManager has not been initialized during the
     * context initialization phase.
     * @param context The ExternalContext used to access Session and Context attributes
     * @return The ThemeManager constructed
     */
    public static ThemeManager initializeThemeManager(ExternalContext context) {
        
        if(DEBUG) log("initializeThemeManager(ExternalContext)");
        // This method must be synchronized, since this is a fallback
        // approach to be used if we failed to initialize the
        // ThemeManager during startup. There could be several
        // simultaneous requests to the container
        
        // Also, I probably need to move the ThemeUtilities into this
        // package or I'm stuck with a public method :(
        
        // Set the default theme if specified in the DD
        String defaultThemeName =
                processInitParameter(context.getInitParameter(DEFAULT_THEME));
        String messageOverride =
                processInitParameter(context.getInitParameter(MESSAGES_PARAM));
        
        ThemeFactory themeFactory = new ThemeFactory(messageOverride);
        ThemeManager manager =
                themeFactory.createThemeManager(defaultThemeName);
        context.getApplicationMap().put(ThemeManager.THEME_MANAGER, manager);
        return manager;
    }
    
    /**
     * <p>Create ThemeInstances for each theme name and supported locale.
     * Each ThemeInstance must be packaged in a Jar. The Jar must
     * contain, in META-INF, a properties file called
     * <code>swc_theme.properties</code> which must contain the following
     * properties:
     * </p>
     * <ul>
     * <li><code>name</code>: the name of the theme</li>
     * <li><code>prefix</code>: the prefix used to retrieve the resource
     * file. This should be the same as the URL mapping used by the ThemeServlet
     * to serve the Theme resources.</li>
     * <li><code>propertyfile</code>: the name of the propertyfile that
     * describes the theme resources (see ThemeFactory)</li>
     * </ul>
     * and may contain the following optional parameter:
     *< ul>
     * <li><code>default</code>: set this to true if the theme
     * specified should be the default theme (the last theme processed
     * wins).</li>
     * </ul>
     */
    private ThemeManager createThemeManager(String defaultThemeName) {
        
        Application app = getApplication();
        if(app == null) {
            // JSF not intialized - try later
            //if(DEBUG) context.log("JSF is not initialized yet");
            return null;
        }
        
        return createThemeManager(defaultThemeName, getLocales(app), 
                                  app.getDefaultLocale());
    }
    
    /**
     * <p>Create ThemeInstances for each theme name and supported locale.
     * Each ThemeInstance must be packaged in a Jar. The Jar must
     * contain, in META-INF, a properties file called
     * <code>swc_theme.properties</code> which must contain the following
     * properties:
     * </p>
     * <ul>
     * <li><code>name</code>: the name of the theme</li>
     * <li><code>prefix</code>: the prefix used to retrieve the resource
     * file. This should be the same as the URL mapping used by the ThemeServlet
     * to serve the Theme resources.</li>
     * <li><code>propertyfile</code>: the name of the propertyfile that
     * describes the theme resources (see ThemeFactory)</li>
     * </ul>
     * and may contain the following optional parameter:
     *< ul>
     * <li><code>default</code>: set this to true if the theme
     * specified should be the default theme (the last theme processed
     * wins).</li>
     * </ul>
     */
    private ThemeManager createThemeManager(String defaultThemeName,
            Set localeSet, Locale defaultLocale) {
        
        if(DEBUG) log("createThemeManager");
        
        String requiredThemeVersion = getRequiredThemeVersion();
        
        ThemeManager manager = new ThemeManager();
        manager.setDefaultLocale(defaultLocale);
        
        Iterator themeAttributesIterator = getThemeAttributes();
        
        if(!themeAttributesIterator.hasNext()) {
            throw new ThemeConfigurationException(WARNING_LOAD);
        }
        
        Attributes themeAttributes = null;
        Iterator locales = null;
        Locale locale = null;
        Theme theme = null;
        String defaultName = defaultThemeName;
        
        while(themeAttributesIterator.hasNext()) {
            
            themeAttributes = (Attributes)(themeAttributesIterator.next());
            
            if(DEBUG) {
                String propFile = themeAttributes.getValue(FILENAME);
                log("Configuring theme from file " + propFile);
            }
            String name = readAttribute(themeAttributes, NAME);
            String version = readAttribute(themeAttributes, THEME_VERSION);
            
            if(DEBUG) {
                log("Required theme version " + //NOI18N
                        String.valueOf(requiredThemeVersion));
                log("Actual theme version " + version); //NOI18N
                log(String.valueOf(requiredThemeVersion.compareTo(version)));
            }
            
            if(requiredThemeVersion != null &&
                    requiredThemeVersion.compareTo(version) > 0) {
                
                throwVersionException(name, version, requiredThemeVersion);
            }
            
            Map map = new HashMap();
            locales = localeSet.iterator();
            while(locales.hasNext()) {
                locale = (Locale)(locales.next());
                // createTheme throws a ThemeConfigurationException if
                // it fails, in which case we abort
                map.put(locale, createTheme(themeAttributes, locale));
            }
            
            manager.addThemeMap(name, map);
            if(defaultName == null) {
                String isDefault = themeAttributes.getValue(DEFAULT);
                if(isDefault != null && isDefault.equals("true")) {
                    defaultName = name;
                }
            }
        }
        
        manager.setDefaultThemeName(defaultThemeName);
        return manager;
    }
    
    public Iterator getThemeAttributes() {
        
        if(DEBUG) log("getThemeAttributes()");
        
        Enumeration manifests = getManifests(); 
        
        if(!manifests.hasMoreElements()) {
            String msg = "No Themes in the classpath!";
            throw new ThemeConfigurationException(msg);
        }
        
        URL url = null;
        URLConnection conn = null;
        InputStream in = null;
        Manifest manifest = null;
        Attributes themeAttributes = null; 
        ArrayList themeProps = new ArrayList(); 
        
        while(manifests.hasMoreElements()) {
            
            url = (URL)(manifests.nextElement());
            
            try {
                if(DEBUG) log("\tExamine " + url.toString());
                conn = url.openConnection();
                in = conn.getInputStream();
                manifest = new Manifest(in);
                themeAttributes = manifest.getAttributes(THEME_SECTION);
                if(themeAttributes != null) {
                    if(DEBUG) log("\tFound a theme section");
                    themeAttributes.putValue(FILENAME, url.toString());
                    themeProps.add(themeAttributes);
                }
            } catch(IOException ioex) {
                // do nothing
            } finally {
                try { in.close(); } catch(Throwable t){}
            }
        }
        return themeProps.iterator();
    }
    
    private Theme createTheme(Attributes themeAttributes, Locale locale)
    throws ThemeConfigurationException {
        
        if(DEBUG) {
            log("\tcreateTheme()");
            log("\tlocale is " + locale.toString());
            Iterator i = themeAttributes.keySet().iterator();
            log("\tAttributes are:");
            while(i.hasNext()) {
                log("\t\t" + i.next().toString());
            }
        }
        
        Theme theme = new Theme(locale);
        ResourceBundle bundle = null;
        
        // Configure the prefix
        if(DEBUG) log("\tSetting the prefix");
        
        String prefix = readAttribute(themeAttributes, PREFIX); //NOI18N
       
        if(!prefix.startsWith("/")) { //NOI18N
            prefix = "/".concat(prefix); //NOI18N
        }
        theme.setPrefix(prefix);
        if(DEBUG) log("\tSet prefix to " + prefix);
        
        
        // Configure the messages
        if(DEBUG) log("\tConfiguring the messages");
        bundle = createResourceBundle(themeAttributes, MESSAGES, locale);
        
        if(messageOverride != null) {
            if(DEBUG) log("Found message override");
            
            try {
                ResourceBundle override =
                        ResourceBundle.getBundle(messageOverride, locale);
                theme.configureMessages(bundle, override);
            } catch(MissingResourceException mre) {
                StringBuffer errorMessage =
                        new StringBuffer("The message resource file ");
                errorMessage.append(messageOverride);
                errorMessage.append(" specified by context parameter ");
                errorMessage.append(MESSAGES_PARAM);
                errorMessage.append(" does not exist.");
                throw new ThemeConfigurationException(errorMessage.toString());
            }
        } else {
            theme.configureMessages(bundle, null);
        }
        
        // Configure the images
        if(DEBUG) log("\tConfiguring the images");
        bundle = createResourceBundle(themeAttributes, IMAGES, locale); 
        theme.configureImages(bundle);
        
        // Configure the javascript files
        bundle = createResourceBundle(themeAttributes, JSFILES, locale); 
        String jsFiles = readAttribute(themeAttributes, JSFILES);
        theme.configureJSFiles(bundle);
        
         // Configure the style sheets
        bundle = createResourceBundle(themeAttributes, STYLESHEETS, locale); 
        theme.configureStylesheets(bundle);
        
        
        // Configure the classmapper
        String classMapper = themeAttributes.getValue(CLASSMAPPER);
        if(classMapper != null && classMapper.length() > 0) {     
            bundle = createResourceBundle(themeAttributes, CLASSMAPPER, locale);
            theme.configureClassMapper(bundle);
        }
        return theme;
    }
    
    private ResourceBundle createResourceBundle(Attributes themeAttributes, 
                                                String propName, 
                                                Locale locale) {
        if(DEBUG) log("createResourceBundle()"); 
        
        String bundleName = readAttribute(themeAttributes, propName);
        try {
            if(DEBUG) log("Resource file is " + bundleName);
            ClassLoader loader =
                    ClassLoaderFinder.getCurrentLoader(ThemeFactory.class);
            return ResourceBundle.getBundle(bundleName, locale, loader);
        } 
        catch(MissingResourceException mre) {           
            StringBuffer msgBuffer = new StringBuffer(300);
            msgBuffer.append("Invalid theme configuration file for theme ");
            msgBuffer.append(themeAttributes.getValue(NAME));
            msgBuffer.append(".\nThemeFactory could not locate resource bundle at ");
            msgBuffer.append(bundleName);
            msgBuffer.append(".");
            throw new ThemeConfigurationException(msgBuffer.toString());
        }      
    }
    
    private String readAttribute(Attributes themeAttributes, String propName) {
        String name = themeAttributes.getValue(propName);
        
        if(name == null || name.length() == 0) {
            
            String propFile = themeAttributes.getValue(FILENAME);
            
            StringBuffer msgBuffer = new StringBuffer(300);
            msgBuffer.append("ThemeConfiguration file "); //NOI18N
            if(propFile != null) {
                msgBuffer.append(propFile);
                msgBuffer.append(" "); //NOI18N
            }
            msgBuffer.append("does not contain required property \""); //NOI18N
            msgBuffer.append(propName);
            msgBuffer.append("\".");
            throw new ThemeConfigurationException(msgBuffer.toString());
        }
        return name;
    }
    
    private void throwVersionException(String name, String version,
            String requiredThemeVersion) {
        
        StringBuffer msgBuffer =
                new StringBuffer(300); //NOI18N
        msgBuffer.append("\n\nTheme \"");
        msgBuffer.append(name);
        msgBuffer.append("\" is not up to date with the component library.\n");
        msgBuffer.append("Its version is ");
        msgBuffer.append(version);
        msgBuffer.append(". Version ");
        msgBuffer.append(requiredThemeVersion);
        msgBuffer.append(" or higher required.\n");
        throw new ThemeConfigurationException(msgBuffer.toString());
    }
    
    private static Application getApplication() {
        ApplicationFactory factory = (ApplicationFactory)
        FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        if(factory == null) {
            return null;
        }
        return factory.getApplication();
    }
    
    private static Set getLocales(Application application) {
        
        if(DEBUG) log("getLocales()");
        
        HashSet localeSet = new HashSet();
        
        Locale locale = application.getDefaultLocale();
        // According to the JSF spec, getDefaultLocale never returns
        // null, but in the Creator simulated environment it does,
        // so we add belt and braces.
        if(locale != null) {
            if(DEBUG) log("\tDefault locale is " + locale.toString());
            localeSet.add(locale);
        } else if(DEBUG) log("\tNo default locale!");
        
        Iterator localeIterator = application.getSupportedLocales();
        // Again, this should not be null, but we need to account for
        // Creator...
        if(localeIterator != null) {
            while(localeIterator.hasNext()) {
                Object localeObject = localeIterator.next();
                if(DEBUG) {
                    log("\tAdding locale " + localeObject.toString());
                }
                localeSet.add(localeObject);
            }
        } else if(DEBUG) log("\tNo supported locales!");
        
        // If things went wrong (= we're in Creator), add the default
        // locale now
        if(localeSet.isEmpty()) {
            if(DEBUG) {
                log("\tAdding default locale which is " + //NOI18N
                        Locale.getDefault().toString());
            }
            localeSet.add(Locale.getDefault());
        }
        return localeSet;
    }
    
    private String missingResourceBundleMessage(Attributes themeAttributes,
            String bundleName) {
        
        String propFile = themeAttributes.getValue(FILENAME);
        StringBuffer msgBuffer =
                new StringBuffer("Invalid theme configuration file for theme ");
        msgBuffer.append(themeAttributes.getValue(NAME));
        
        if(propFile != null) {
            msgBuffer.append(" configured by property file ");
            msgBuffer.append(propFile);
            msgBuffer.append(".");
        }
        msgBuffer.append("ThemeFactory could not locate resource bundle at ");
        msgBuffer.append(bundleName);
        msgBuffer.append(".");
        return msgBuffer.toString();
    }
    
    private static String processInitParameter(Object object) {
        
        if(object == null) {
            return null;
        }
        String string = object.toString();
        // Unfortunately, the Creator simulated environment returns an
        // empty string instead of null when the init parameter does
        // not exist
        if(string.length() == 0) {
            return null;
        }
        return string;
    }
    
    private String getRequiredThemeVersion() {
        
        if(DEBUG) log("getRequiredThemeVersion()");
        
        Enumeration manifests = getManifests();
               
        if(!manifests.hasMoreElements()) {
            if(DEBUG) log("\tNo manifests in the classpath!");
            return null;
        }
        
        URL url = null;
        InputStream in = null;
        Manifest manifest = null;
        String themeVersion = null;
        
        while(themeVersion == null && manifests.hasMoreElements()) {
            
            url = (URL)(manifests.nextElement());
            if(url.toString().indexOf("webui") == -1) {
                continue;
            }
            
            if(DEBUG) log("\tNow processing " + url.toString());
            
            try {
                in = url.openConnection().getInputStream();
                manifest = new Manifest(in);
                Attributes attr = manifest.getAttributes(COMPONENTS_SECTION);
                if(attr != null) {
                    themeVersion = attr.getValue(THEME_VERSION_REQUIRED);
                    if(DEBUG) log("\tFound attribute " + themeVersion);
                }
            } catch(IOException ioex) {
                ioex.printStackTrace();
                // do nothing
            } finally {
                try { in.close(); } catch(Throwable t){}
            }
        }
        return themeVersion;
    }
    
    private Enumeration getManifests() {
        
        Enumeration manifests = null;
        ClassLoader loader = 
            ClassLoaderFinder.getCurrentLoader(ThemeFactory.class);

        // Temporary workaround for a Creator issue; direct questions on this
        // to tor.norbye@sun.com
        if (Beans.isDesignTime() && loader instanceof URLClassLoader) {
            // This is a temporary hack to limit our manifest search for themes
            // to the URLs in the ClassLoader, if it's a URLClassLoader.
            // This is necessary to get theme switching to work when there
            // are multiple simultaneous open projects in Creator.
            Vector v = new Vector();
            URL[] urls = ((URLClassLoader)loader).getURLs();
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                try {
                    URL manifest = new URL(url, MANIFEST);
                    // See if the manifest file exists
                    InputStream is = manifest.openStream();
                    v.addElement(manifest);
                    is.close();
                } catch (IOException ioe) {
                    // No such manifest, so don't add one to the vector
                }
            }
            return v.elements();
        }

        try {
            manifests = loader.getResources(MANIFEST);
        } catch(IOException ioex) {
            if(DEBUG) {
                log("\tIOException using the Context ClassLoader");
            }
        }
        if(!manifests.hasMoreElements()) {
            try {
                manifests = loader.getResources(MANIFEST);
            } catch(IOException ioex) {
                if(DEBUG) {
                    log("\tIOException using ThemeFactory's ClassLoader");
                }
            }
        }
        return manifests; 
    }
    
    private static void log(String s) {
        System.out.println("ThemeFactory::" + s);
    }
}
