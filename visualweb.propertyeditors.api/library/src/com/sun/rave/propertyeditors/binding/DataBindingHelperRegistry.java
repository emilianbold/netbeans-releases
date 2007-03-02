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

package com.sun.rave.propertyeditors.binding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

/**
 * A global registry for implementations of {@link DataBindingHelper}. The IDE
 * will provide an implementation at design-time.
 */
public class DataBindingHelperRegistry {
    
    private static DataBindingHelper DATA_BINDING_HELPER;
    
    public static void setDataBindingHelper(DataBindingHelper dataBindingHelper) {
        DATA_BINDING_HELPER = dataBindingHelper;
    }
    
    public static DataBindingHelper getDataBindingHelper() {
        return DATA_BINDING_HELPER;
    }
    
    private static DataBindingHelper findDataBindingHelper(ClassLoader classLoader) {
        try {
            String interfaceName = "META-INF/services/" + DataBindingHelper.class.getName(); // NOI18N
            Enumeration<URL> urls = classLoader.getResources(interfaceName);
            Class dataBindingHelperClass = null;
            while (dataBindingHelperClass == null && urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8")); // NOI18N
                    String content = reader.readLine();
                    if (content != null) {
                        content = content.trim();
                        dataBindingHelperClass = Class.forName(content, false, classLoader);
                        if (!DataBindingHelper.class.isAssignableFrom(dataBindingHelperClass)) {
                            throw new ClassNotFoundException(dataBindingHelperClass.getName()
                            + " is not a subclass of " + DataBindingHelper.class.getName()); // NOI18N
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dataBindingHelperClass = null;
                }
            }
            if (dataBindingHelperClass != null) {
                DataBindingHelper dataBindingHelper = (DataBindingHelper) dataBindingHelperClass.newInstance();
                return dataBindingHelper;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("No implementation of " + DataBindingHelper.class.getName() + " found.");
        return null;
    }
    
}
