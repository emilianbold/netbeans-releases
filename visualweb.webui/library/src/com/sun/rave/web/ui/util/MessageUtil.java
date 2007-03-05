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
package com.sun.rave.web.ui.util;

import java.text.*;
import java.util.*;

import javax.faces.context.FacesContext;


/**
 * Factory class for retrieving server-side i18n messages within the JSF
 * framework. Note that the ServletResponse locale, content type, and character
 * encoding are not set here. Since tags may be used outside the Sun Web
 * Console, that task will most likely be done in the console's session filter.
 * <p>
 * Example:
 * </p><code>
 * ResponseWriter w = FacesContext.getCurrentInstance().getResponseWriter();
 * w.write(MessageUtil.getMessage("com.sun.rave.web.ui.Resources", "key"));
 * </code>
 *
 * @author Dan Labrecque
 */
public class MessageUtil extends Object {
    // Default constructor.
    protected MessageUtil() {
    }

    /**
     * Get a message from a desired resource bundle.
     *
     * @param context The FacesContext object used to obtain locale.
     * @param baseName The fully qualified name of the resource bundle.
     * @param key The key for the desired string.
     * @throws NullPointerException if context or baseName is null.
     */     
    public static String getMessage(FacesContext context, String baseName,
            String key) {
        return getMessage(context, baseName, key, null);
    }

    /**
     * Get a formatted message from a desired resource bundle.
     *
     * @param context The FacesContext object used to obtain locale.
     * @param baseName The fully qualified name of the resource bundle.
     * @param key The key for the desired string.
     * @param args The arguments to be inserted into the string.
     * @throws NullPointerException if context or baseName is null.
     */    
    public static String getMessage(FacesContext context, String baseName, 
            String key, Object args[]) {
        return getMessage(getLocale(context), baseName, key, args);
    }  

    /**
     * Get a message from a desired resource bundle.
     *
     * @param baseName The fully qualified name of the resource bundle.
     * @param key The key for the desired string.
     * @throws NullPointerException if baseName is null.
     */    
    public static String getMessage(String baseName, String key) {
        return getMessage(baseName, key, null);
    }

    /**
     * Get a formatted message from a desired resource bundle.
     *
     * @param baseName The fully qualified name of the resource bundle.
     * @param key The key for the desired string.
     * @param args The arguments to be inserted into the string.
     * @throws NullPointerException if baseName is null.
     */
    public static String getMessage(String baseName, String key, 
            Object args[]) {
	return getMessage(getLocale(), baseName, key, args);
    }

    /**
     * Get a formatted message from a desired resource bundle.
     *
     * @param locale The locale for which a resource bundle is desired.
     * @param baseName The fully qualified name of the resource bundle.
     * @param key The key for the desired string.
     * @param args The arguments to be inserted into the string.
     * @throws NullPointerException if locale or baseName is null.
     */
    public static String getMessage(Locale locale, String baseName, String key,
            Object args[]) {
        ClassLoader loader =
                ClassLoaderFinder.getCurrentLoader(MessageUtil.class);
        // First try the context CL
        return getMessage(locale, baseName, key, args, loader);
    }

    /**
     * Get a formatted message from a desired resource bundle.
     *
     * @param locale The locale for which a resource bundle is desired.
     * @param baseName The fully qualified name of the resource bundle.
     * @param key The key for the desired string.
     * @param args The arguments to be inserted into the string.
     * @param loader The class loader used to load the resource bundle.
     * @throws NullPointerException if locale, baseName, or loader is null.
     */
    public static String getMessage(Locale locale, String baseName, String key, 
            Object args[], ClassLoader loader) {
        if (key == null)
            return key;
        else if (locale == null || baseName == null || loader == null)
            throw new NullPointerException("One or more parameters is null");
                
        ResourceBundle bundle = ResourceBundleManager.getInstance().getBundle(baseName, locale, 
            loader);
        
	if (null == bundle)
            throw new NullPointerException("Could not obtain resource bundle");

        String message = null;
        
        try {
            message = bundle.getString(key);
	} catch (MissingResourceException e) {
	}
	
        return getFormattedMessage((message != null) ? message : key, args);
    }
    
    /**
     * Format message using given arguments.
     *
     * @param message The string used as a pattern for inserting arguments.
     * @param args The arguments to be inserted into the string.
     */
    protected static String getFormattedMessage(String message, Object args[]) {
        if ((args == null) || (args.length == 0)) {
            return message;
	}
        
        String result = null;
        
        try {
            MessageFormat mf = new MessageFormat(message);
            result = mf.format(args);
        } catch (NullPointerException e) {
        }        

        return (result != null) ? result : message;
    }
    
    /**
     * Get locale from current FacesContext instance.
     */
    protected static Locale getLocale() {
        return getLocale(FacesContext.getCurrentInstance());
    }

    /**
     * Get locale from given FacesContext object.
     *
     * @param context The FacesContext object used to obtain locale.
     */
    protected static Locale getLocale(FacesContext context) {
        if (context == null) {
	    return Locale.getDefault();
	}
                
        Locale locale = null;
        
        // context.getViewRoot() may not have been initialized at this point.
        if (context.getViewRoot() != null)
            locale = context.getViewRoot().getLocale();
        
        return (locale != null) ? locale : Locale.getDefault();
    }
    
    /**
     * Get current class loader from given object.
     *
     * @param o Object used to obtain fallback class loader.
     */
    public static ClassLoader getCurrentLoader(Object o) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
	return (loader != null) ? loader : o.getClass().getClassLoader();
    }
}
