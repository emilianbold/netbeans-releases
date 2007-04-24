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
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 * @author Kirill Sorokin
 */
public final class ResourceUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Map<String, ResourceBundle> loadedBundles = 
            new HashMap<String, ResourceBundle>();
    
    // strings //////////////////////////////////////////////////////////////////////
    public static String getString(
            final String baseName, 
            final String key) {
        return loadBundle(
                baseName, 
                Locale.getDefault(), 
                ResourceUtils.class.getClassLoader()).getString(key);
    }
    
    public static String getString(
            final String baseName, 
            final String key, 
            final Object... arguments) {
        return StringUtils.format(getString(baseName, key), arguments);
    }
    
    public static String getString(
            final String baseName, 
            final String key, 
            final ClassLoader loader) {
        return loadBundle(baseName, Locale.getDefault(), loader).getString(key);
    }
    
    public static String getString(
            final String baseName, 
            final String key, 
            final ClassLoader loader, 
            final Object... arguments) {
        return StringUtils.format(getString(baseName, key, loader), arguments);
    }
    
    public static String getString(
            final Class clazz, 
            final String key) {
        return loadBundle(clazz, Locale.getDefault()).getString(key);
    }
    
    public static String getString(
            final Class clazz, 
            final String key, 
            final Object... arguments) {
        return StringUtils.format(getString(clazz, key), arguments);
    }
    
    // resources ////////////////////////////////////////////////////////////////////
    public static InputStream getResource(
            final String name) {
        return getResource(name, ResourceUtils.class.getClassLoader());
    }
    
    public static InputStream getResource(
            final String path, 
            final ClassLoader loader) {
        return loader.getResourceAsStream(path);
    }
    
    /**
     * Returns the size of the resource file.
     * @param resource Resource name
     * @return size of the resource or 
     *      <i>-1</i> if the resource was not found or any other error occured
     */
    public static long getResourceSize(
            final String resource) {
        InputStream is = null;
        long size = 0;
        try {
            is = getResource(resource);
            if(is==null) { // resource was not found
                return -1;
            }
            byte [] buf = new byte [BUFFER_SIZE];
            while(is.available()>0) {
                size += is.read(buf);
            }
        } catch (IOException ex) {
            size = -1;
        } finally {
            try {
                if(is!=null) {
                    is.close();
                }
            } catch (IOException e){
            }
        }
        return size;
    }
    
    public static String getResourceFileName(
            final String resource) {
        return resource.substring(resource.lastIndexOf("/")+1);
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static ResourceBundle loadBundle(
            final String baseName, 
            final Locale locale, 
            final ClassLoader loader) {
        final String bundleId = loader.toString() + baseName;
        
        ResourceBundle bundle = (ResourceBundle) loadedBundles.get(bundleId);
        
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(baseName, locale, loader);
            loadedBundles.put(bundleId, bundle);
        }
        
        return bundle;
    }
    
    private static ResourceBundle loadBundle(
            final Class clazz, 
            final Locale locale) {
        return loadBundle(
                clazz.getPackage().getName() + BUNDLE_FILE_SUFFIX, 
                locale, 
                clazz.getClassLoader());
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private ResourceUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int BUFFER_SIZE = 
            40960; // NOMAGI
    
    public static final String BUNDLE_FILE_SUFFIX = 
            ".Bundle"; // NOI18N
}