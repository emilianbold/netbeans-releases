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
package com.sun.rave.web.ui.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * <p>
 * Utility methods for localized messages for design time classes. This class
 * expects a resource bundle named <code>Bundle-DT</code> in the same pacakge
 * as the class passed to its constructor. This class is also useful for
 * design-time behavior in component renderers and so exists in this runtime
 * package.
 * </p>
 */
public class Bundle {


    // ------------------------------------------------------------- Constructor


    /**
     * <p>Construct a <code>Bundle</code> instance for the specified
     * class.</p>
     *
     * @param clazz Class for which to construct a bundle instance
     */
    public Bundle(Class clazz) {
        String name = clazz.getName();
        int period = name.lastIndexOf('.'); // NOI18N
        if (period >= 0) {
            name = name.substring(0, period + 1);
        } else {
            name = "";
        }
        name += "Bundle-DT";
        bundle =
          ResourceBundle.getBundle(name, format.getLocale(), clazz.getClassLoader());
    }


    // -------------------------------------------------------- Static Variables


    /**
     * <p>The <code>MessageFormat</code> instance we will use for messages
     * that require parameter replacement.</p>
     */
    private MessageFormat format = new MessageFormat("");


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The <code>ResourceBundle</code> containing our messages.</p>
     */
    private ResourceBundle bundle;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Return the message for the specified key.</p>
     *
     * @param key Message key to look up
     */
    public String message(String key) {
        return bundle.getString(key);
    }


    /**
     * <p>Return the message for the specified key, after substituting
     * the specified parameters.</p>
     *
     * @param key Message key to look up
     * @param params Replacement parameters
     */
    public String message(String key, Object params[]) {
        String pattern = message(key);
        synchronized (format) {
            format.applyPattern(pattern);
            return format.format(params);
        }
    }


}
