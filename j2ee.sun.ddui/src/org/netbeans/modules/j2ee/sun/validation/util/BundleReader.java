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
 */

package org.netbeans.modules.j2ee.sun.validation.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.validation.Constants;


/**
 * BundleReader  is a Class  to read properties from the bundle.
 * <code>getValue()</code> method can be used to read the properties
 * from the bundle file(Bundle.properties). Default bundle file used
 * is <code>{ @link Constants }.BUNDLE_FILE</code>. Bundle file to use
 * can be set by using <code>setBundle()</code> method.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class BundleReader {

    /**
     * A resource bundle of this reader.
     */
    private static ResourceBundle resourceBundle;

    
    /** Creates a new instance of BundleReader */
    public BundleReader() {
    }

    /**
     * Gets the value of the the given <code>key</code> from the bundle
     * 
     * @param key the key of which, the value needs to be fetched from
     * the bundle.
     */
    public static String getValue(String key) {
        if(resourceBundle == null)
            return key;
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException missingResourceException) {
            return key;
        }
    }


    /**
     * sets the given bundle file as the file to use by this object.
     */
    public static void setBundle(String bundleFile){
        try {
            resourceBundle = ResourceBundle.getBundle(bundleFile);
        } catch (Exception ex) { }
    }
    

    static {
        try {
            resourceBundle = ResourceBundle.getBundle(Constants.BUNDLE_FILE);
        } catch (Exception ex) { }
    }
}
