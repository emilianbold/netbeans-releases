/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
