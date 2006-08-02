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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class ResourceUtils {
    private static ResourceUtils instance;
    
    public static synchronized ResourceUtils getInstance() {
        if (instance == null) {
            instance = new PureJavaResourceUtils();
        }
        
        return instance;
    }
    
    public abstract ResourceBundle loadBundle(URL url) throws IOException;
    
    public abstract ResourceBundle loadBundle(File file) throws IOException;
    
    public abstract ResourceBundle loadBundle(String baseName);
    
    public abstract ResourceBundle loadBundle(Class clazz);
    
    public abstract String getString(URL url, String key) throws IOException;
    
    public abstract String getString(File file, String key) throws IOException;
    
    public abstract String getString(String baseName, String key);
    
    public abstract String getString(Class clazz, String key);
    
    public abstract String getString(URL url, String key, Object... arguments) throws IOException;
    
    public abstract String getString(File file, String key, Object... arguments) throws IOException;
    
    public abstract String getString(String baseName, String key, Object... arguments);
    
    public abstract String getString(Class clazz, String key, Object... arguments);
    
    private static class PureJavaResourceUtils extends ResourceUtils {
        private Map<Object, ResourceBundle> loadedBundles = new HashMap<Object, ResourceBundle>();
        
        public ResourceBundle loadBundle(URL url) throws IOException {
            ResourceBundle bundle = (ResourceBundle) loadedBundles.get(url);
            
            if (bundle == null) {
                bundle = new PropertyResourceBundle(url.openStream());
                loadedBundles.put(url, bundle);
            }
            
            return bundle;
        }
        
        public ResourceBundle loadBundle(File file) throws IOException {
            return loadBundle(file.toURL());
        }
        
        public ResourceBundle loadBundle(String baseName) {
            ResourceBundle bundle = (ResourceBundle) loadedBundles.get(baseName);
            
            if (bundle == null) {
                bundle = ResourceBundle.getBundle(baseName);
                loadedBundles.put(baseName, bundle);
            }
            
            return bundle;
        }
        
        public ResourceBundle loadBundle(Class clazz) {
            return loadBundle(clazz.getPackage().getName() + "." + "Bundle");
        }
        
        public String getString(URL url, String key) throws IOException {
            return loadBundle(url).getString(key);
        }
        
        public String getString(File file, String key) throws IOException {
            return loadBundle(file).getString(key);
        }
        
        public String getString(String baseName, String key) {
            return loadBundle(baseName).getString(key);
        }
        
        public String getString(Class clazz, String key) {
            return loadBundle(clazz).getString(key);
        }
        
        public String getString(URL url, String key, Object... arguments) throws IOException {
            return StringUtils.formatMessage(getString(url, key), arguments);
        }
        
        public String getString(File file, String key, Object... arguments) throws IOException {
            return StringUtils.formatMessage(getString(file, key), arguments);
        }
        
        public String getString(String baseName, String key, Object... arguments) {
            return StringUtils.formatMessage(getString(baseName, key), arguments);
        }
        
        public String getString(Class clazz, String key, Object... arguments) {
            return StringUtils.formatMessage(getString(clazz, key), arguments);
        }
    }
}