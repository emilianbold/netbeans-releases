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
public abstract class ResourceUtils {
    ////////////////////////////////////////////////////////////////////////////
    // Static
    private static Map<String, ResourceBundle> loadedBundles = new HashMap<String, ResourceBundle>();
    
    private static final int BUF_SIZE=102400;
    
    private static ResourceBundle loadBundle(String baseName, Locale locale, ClassLoader loader) {
        String bundleId = loader.toString() + baseName;
        
        ResourceBundle bundle = (ResourceBundle) loadedBundles.get(bundleId);
        
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(baseName, locale, loader);
            loadedBundles.put(bundleId, bundle);
        }
        
        return bundle;
    }
    
    private static ResourceBundle loadBundle(Class clazz, Locale locale) {
        return loadBundle(clazz.getPackage().getName() + "." + "Bundle", locale, clazz.getClassLoader());
    }
    
    public static String getString(String baseName, String key) {
        return loadBundle(baseName, Locale.getDefault(), ResourceUtils.class.getClassLoader()).getString(key);
    }
    
    public static String getString(String baseName, String key, Object... arguments) {
        return StringUtils.format(getString(baseName, key), arguments);
    }
    
    public static String getString(String baseName, String key, ClassLoader loader) {
        return loadBundle(baseName, Locale.getDefault(), loader).getString(key);
    }
    
    public static String getString(String baseName, String key, ClassLoader loader, Object... arguments) {
        return StringUtils.format(getString(baseName, key, loader), arguments);
    }
    
    public static String getString(Class clazz, String key) {
        return loadBundle(clazz, Locale.getDefault()).getString(key);
    }
    
    public static String getString(Class clazz, String key, Object... arguments) {
        return StringUtils.format(getString(clazz, key), arguments);
    }
    
    public static InputStream getResource(String name) {
        return getResource(name, ResourceUtils.class.getClassLoader());
    }
    
    public static InputStream getResource(String path, ClassLoader loader) {
        return loader.getResourceAsStream(path);
    }
    /**
     * Returns the size of the resource file.
     * @param resource Resource name
     * @return size of the resource or 
     *      <i>-1</i> if the resource was not found or any other error occured
     */
    public static long getResourceSize(String resource) {
        InputStream is = null;
        long size = 0;
        try {
            is = getResource(resource);
            if(is==null) { // resource was not found
                return -1;
            }
            byte [] buf = new byte [BUF_SIZE];
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
    
    public static String getResourceFileName(String resource) {
        return resource.substring(resource.lastIndexOf("/")+1);
    }
}