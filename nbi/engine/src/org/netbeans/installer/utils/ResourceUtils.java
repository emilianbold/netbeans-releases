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
    private static ResourceUtils instance;
    
    public static synchronized ResourceUtils getInstance() {
        if (instance == null) {
            instance = new GenericResourceUtils();
        }
        
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance
    public abstract String getString(String baseName, String key);
    
    public abstract String getString(String baseName, String key, ClassLoader loader);
    
    public abstract String getString(String baseName, String key, Object... arguments);
    
    public abstract String getString(String baseName, String key, ClassLoader loader, Object... arguments);
    
    public abstract String getString(Class clazz, String key);
    
    public abstract String getString(Class clazz, String key, Object... arguments);
    
    public abstract InputStream getResource(String path);
    
    public abstract InputStream getResource(String path, ClassLoader loader);
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class GenericResourceUtils extends ResourceUtils {
        private Map<Object, ResourceBundle> loadedBundles = new HashMap<Object, ResourceBundle>();
        
        private ResourceBundle loadBundle(String baseName, Locale locale, ClassLoader loader) {
            ResourceBundle bundle = (ResourceBundle) loadedBundles.get(baseName);
            
            if (bundle == null) {
                bundle = ResourceBundle.getBundle(baseName, locale, loader);
                loadedBundles.put(baseName, bundle);
            }
            
            return bundle;
        }
        
        private ResourceBundle loadBundle(Class clazz, Locale locale) {
            return loadBundle(clazz.getPackage().getName() + "." + "Bundle", locale, clazz.getClassLoader());
        }
        
        public String getString(String baseName, String key) {
            return loadBundle(baseName, Locale.getDefault(), getClass().getClassLoader()).getString(key);
        }
        
        public String getString(String baseName, String key, Object... arguments) {
            return StringUtils.getInstance().formatMessage(getString(baseName, key), arguments);
        }
        
        public String getString(String baseName, String key, ClassLoader loader) {
            return loadBundle(baseName, Locale.getDefault(), loader).getString(key);
        }
        
        public String getString(String baseName, String key, ClassLoader loader, Object... arguments) {
            return StringUtils.getInstance().formatMessage(getString(baseName, key, loader), arguments);
        }
        
        public String getString(Class clazz, String key) {
            return loadBundle(clazz, Locale.getDefault()).getString(key);
        }
        
        public String getString(Class clazz, String key, Object... arguments) {
            return StringUtils.getInstance().formatMessage(getString(clazz, key), arguments);
        }
        
        public InputStream getResource(String name) {
            return getResource(name, getClass().getClassLoader());
        }
        
        public InputStream getResource(String path, ClassLoader loader) {
            return loader.getResourceAsStream(path);
        }
    }
}