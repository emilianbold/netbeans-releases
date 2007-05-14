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
package org.netbeans.modules.sql.framework.common.utils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * An internationalization / localization helper class which reduces the bother of
 * handling ResourceBundles and takes care of the sql framework cases of message formating
 * which otherwise require the creation of Object arrays and such.
 * <p>
 * The MessageManager operates on a package basis. One MessageManager per package can be
 * created and accessed via the getManager method call.
 * <p>
 * The MessageManager will look for a ResourceBundle named by the package name given plus
 * the suffix of "LocalStrings". In practice, this means that the localized information
 * will be contained in a LocalStrings.properties file located in the package directory of
 * the classpath.
 * <p>
 * Please see the documentation for java.util.ResourceBundle for more information.
 * 
 * @author sseshachala
 * @version :$Revision$
 */

public class MessageManager {

    private static HashMap managers = new HashMap();

    public static synchronized MessageManager getManager(java.lang.Class theClass) {
        return getManager(theClass.getPackage().getName());
    }

    /**
     * Get the MessageManager for a particular package. If a manager for a package already
     * exists, it will be reused, else a new MessageManager will be created and returned.
     * 
     * @param packageName for which the MessageManager needs to be picked
     * @return MessageManager For the given packageName
     */

    public static synchronized MessageManager getManager(String packageName) {
        if (managers != null) {
            MessageManager mgr = (MessageManager) managers.get(packageName);
            if (mgr == null) {
                mgr = new MessageManager(packageName);
                managers.put(packageName, mgr);
            }
            return mgr;
        }
        managers = new HashMap();
        MessageManager mgr = new MessageManager(packageName);
        managers.put(packageName, mgr);
        return mgr;
    }

    /**
     * The ResourceBundle for this MessageManager.
     */

    private ResourceBundle bundle;

    /**
     * Creates a new MessageManager for a given package. This is a private method and all
     * access to it is arbitrated by the static getManager method call so that only one
     * MessageManager per package will be created.
     * 
     * @param packageName Name of package to create MessageManager for.
     */

    private MessageManager(String packageName) {
        String bundleName = packageName + ".";
        bundleName = bundleName + "LocalStrings";
        bundle = ResourceBundle.getBundle(bundleName);
    }

    /**
     * Get a string from the underlying resource bundle.
     * 
     * @param key for which msg needs to retrieved from HashMap
     * @return String The MessageFormat for given Key
     */

    public synchronized String getString(String key) {
        if (key == null) {
            String msg = "key is null";
            throw new NullPointerException(msg);
        }

        if (bundle == null) {
            String message = " Could load Resources ";
            throw new NullPointerException(message);
        }

        String str;

        try {
            str = bundle.getString(key);
        } catch (MissingResourceException mre) {
            str = "Cannot find message associated with key '" + key + "'";
        }

        return str;
    }

    /**
     * Get a string from the underlying resource bundle and format it with the given
     * object argument. This argument can of course be a String object.
     * 
     * @param key For which String needs to be displayed
     * @param arg That is the result for Info, ERR or Exception
     * @return String MessageString for given Key
     */

    public synchronized String getString(String key, Object arg) {
        Object[] args = new Object[] { arg};
        return getString(key, args);
    }

    /**
     * Get a string from the underlying resource bundle and format it with the given
     * object arguments. These arguments can of course be String objects.
     * 
     * @param key For which Message needs to be constructed
     * @param arg1 used for message construction
     * @param arg2 used for message construction
     * @return String Message String for the given key
     */

    public String getString(String key, Object arg1, Object arg2) {
        Object[] args = new Object[] { arg1, arg2};
        return getString(key, args);
    }

    /**
     * Get a string from the underlying resource bundle and format it with the given
     * object arguments. These arguments can of course be String objects.
     * 
     * @param key For which Message needs to be constructed
     * @param arg1 Used for messageConstruction
     * @param arg2 Used for message construction
     * @param arg3 used for message construction
     * @return String for given key and args.
     */

    public String getString(String key, Object arg1, Object arg2, Object arg3) {
        Object[] args = new Object[] { arg1, arg2, arg3};
        return getString(key, args);
    }

    /**
     * Get a string from the underlying resource bundle and format it with the given
     * object arguments. These arguments can of course be String objects.
     * 
     * @param key For which Message needs to be constructed
     * @param arg1 Used for messageConstruction
     * @param arg2 Used for messageConstruction
     * @param arg3 Used for messageConstruction
     * @param arg4 Used for messageConstruction
     * @return String MessageString for the given String And Args
     */

    public String getString(String key, Object arg1, Object arg2, Object arg3, Object arg4) {
        Object[] args = new Object[] { arg1, arg2, arg3, arg4};
        return getString(key, args);
    }

    /**
     * Get a string from the underlying resource bundle and format it with the given set
     * of arguments.
     * 
     * @param key For which MessageFormat needs to be picked for a key
     * @param args This contains the arguments
     * @return String Message Format for given key and arguments
     */
    public String getString(String key, Object[] args) {
        String iString;
        String value = getString(key);

        try {
            Object nonNullArgs[] = args;
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    if (nonNullArgs == args) {
                        nonNullArgs = (Object[]) args.clone();
                    }
                    nonNullArgs[i] = "null";
                }
            }

            iString = MessageFormat.format(value, nonNullArgs);
        } catch (IllegalArgumentException iae) {
            StringBuilder buf = new StringBuilder();
            buf.append(value);
            for (int i = 0; i < args.length; i++) {
                buf.append(" arg[" + i + "]=" + args[i]);
            }
            iString = buf.toString();
        }

        return iString;
    }

}

