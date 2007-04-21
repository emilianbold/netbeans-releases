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

package org.netbeans.modules.visualweb.complib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * ResourceBundle used to localize NetBeans libraries. Note this class must be
 * public.
 * 
 * @author Edwin Goei
 */
public class LibraryLocalizationBundle extends ResourceBundle {

    private static File propsFile = new File(IdeUtil.getComplibStateDir(),
            "LibraryLocalizationBundle.properties");

    private static HashMap<Object, Object> map;

    /**
     * The constructor must be public so an instance of it can be created.
     */
    public LibraryLocalizationBundle() {
    }

    private static HashMap<Object, Object> getMap() {
        // Load the last saved state once
        if (map == null) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(propsFile));
            } catch (IOException e) {
                // File may not exist so ignore
            }
            map = new HashMap<Object, Object>(props);
        }
        return map;
    }

    /**
     * Add an entry
     * 
     * @param key
     * @param value
     */
    static void add(String key, String value) {
        getMap().put(key, value);
        save();
    }

    /**
     * Remove an entry
     * 
     * @param key
     */
    static void remove(String key) {
        getMap().remove(key);
        save();
    }

    private static void save() {
        // Transfer data into a Properties object to save it
        Properties props = new Properties();
        Set<Object> keys = map.keySet();
        for (Object object : keys) {
            String key = (String) object;
            String value = (String) map.get(key);
            props.setProperty(key, value);
        }

        try {
            props.store(new FileOutputStream(propsFile), null);
        } catch (IOException e) {
            IdeUtil.logError("Unable to save LibraryLocalizationBundle", e);
        }
    }

    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        return getMap().get(key);
    }

    public Enumeration<String> getKeys() {
        final Set<Object> keys = getMap().keySet();
        return new Enumeration<String>() {
            Iterator<Object> it = keys.iterator();

            public boolean hasMoreElements() {
                return it.hasNext();
            }

            public String nextElement() {
                // Elements are always a String
                return (String) it.next();
            }
        };
    }

}
