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
package org.netbeans.installer.sandbox.download;

import java.util.Properties;

/**
 *
 * @author Kirill Sorokin
 */
public class DownloadOptions {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    // Pre download conditions
    public static final String CHECK_EXISTANCE = "check.existance";
    public static final String CHECK_SIZE = "check.size";
    public static final String CHECK_LAST_MODIFIED = "check.last.modified";
    public static final String CHECK_MD5 = "check.md5";
    public static final String CHECK_CRC = "check.crc";
    
    // Post download conditions
    public static final String VERIFY_MD5 = "verify.md5";
    public static final String VERIFY_CRC = "verify.crc";
    
    public static final String MD5_SUM = "md5.sum";
    public static final String CRC_SUM = "crc.sum";
    
    public static final String CLASSLOADER = "classloader";
    
    public static final String CACHING_ENABLED = "caching.enabled";
    
    public static final String IGNORE_PROXIES = "ignore.proxies";
    
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    
    public static final String MAX_THREADS = "max.threads";
    
    public static final String MAX_ERRORS = "max.errors";
    
    public static final String MAX_SPEED = "max.speed";
    
    // defaults
    public static final boolean DEFAULT_BOOLEAN = false;
    public static final int     DEFAULT_INT     = 0;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static DownloadOptions getDefaults() {
        DownloadOptions options = new DownloadOptions();
        
        options.put(CHECK_EXISTANCE, true);
        options.put(CHECK_SIZE, true);
        options.put(CHECK_LAST_MODIFIED, true);
        
        options.put(VERIFY_MD5, true);
        options.put(VERIFY_CRC, true);
        
        options.put(CACHING_ENABLED, true);
        
        options.put(MAX_THREADS, 5);
        options.put(MAX_ERRORS, 25);
        
        options.put(MAX_SPEED, -1);
        
        return options;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Properties properties = new Properties();
    
    private DownloadOptions() {
    }
    
    public void put(String name, Object value) {
        properties.put(name, value);
    }
    
    public Object get(String name) {
        return properties.get(name);
    }
    
    public boolean getBoolean(String name) {
        Object value = get(name);
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return false;
        }
    }
    
    public int getInt(String name) {
        Object value = get(name);
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return 0;
        }
    }
    
    public String getString(String name) {
        Object value = get(name);
        
        if (value instanceof String) {
            return (String) value;
        } else {
            return null;
        }
    }
}
