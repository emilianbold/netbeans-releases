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

package org.netbeans.modules.iep.model.util;

import java.text.MessageFormat;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

import org.netbeans.modules.tbls.model.ConfigProperties;

public class Bundle {
    private static Map mBundles = new HashMap();
    
    private ConfigProperties mProps = null;

    private Bundle() {
        ResourceBundle bundle = new NullBundle();
        mProps = resourceBundle2Properties(bundle);
    }
    
    private Bundle(String name) {
        ResourceBundle bundle = ResourceBundle.getBundle(name);
        mProps = resourceBundle2Properties(bundle);
    }

    public synchronized static Bundle getInstance(String name) {
        Bundle bundle = (Bundle) mBundles.get(name);
        if (bundle == null) {
            try {
                bundle = new Bundle(name);
            } catch (Exception e) {
                e.printStackTrace();
                bundle = new Bundle();
            }
            mBundles.put(name, bundle);
        }
        return bundle;
    }
    
    public String getString(String key) {
        return getString(key, null, null);
    }

    public String getString(String key, Object[] args) {
        return getString(key, args, null);
    }
    
    public String getString(String key, String def) {
        return getString(key, null, def);
    }

    public String getString(String key, Object[] args, String def) {
        String ret;
        try {
            ret = format(mProps.getProperty(key), args);
            if (def != null) {
                if (ret == null || ret.equals("")) {
                    ret = def;
                }
            }
        } catch (Exception e) {
            System.err.println(
                "Exception: " + e);
            try {
                ret = format(def, args);
            } catch (Exception e2) {
                System.err.println(
                    "Exception: " + e2);
                ret = def;
            }
        }    
        return ret;
    }

    public Enumeration getKeys() {
        return mProps.propertyNames();
    }
    
    private String format(String text, Object[] args) {
        String ret = text;
        if (text != null && args != null) {
            ret = MessageFormat.format(text, args);
        }
        return ret;
    }

    private static ConfigProperties resourceBundle2Properties(ResourceBundle bundle) {
        ConfigProperties props = new ConfigProperties();
        for (Enumeration e = bundle.getKeys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            props.put(key, bundle.getString(key));
        }
        return props;
    }
    
    private static class NullBundle extends ListResourceBundle {
        private Object[][] contents = {};

        public Object[][] getContents() {
            return contents;
        }
    }
}
