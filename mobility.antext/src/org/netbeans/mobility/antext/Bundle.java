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

/*
 * Bundle.java
 *
 * Created on 15. prosinec 2003, 11:28
 */
package org.netbeans.mobility.antext;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Helper class for getting messages from resource bundle which is located in the same package as this class.
 * @author  Adam Sotona
 */
public class Bundle
{
    
    private Bundle()
    {
        //Avoid instantiation of this class
    }
    
    private static final ResourceBundle bundle = ResourceBundle.getBundle(Bundle.class.getName());
    
    /**
     * Gets message of key.
     * @param key name of message
     * @return message
     */
    static public String getMessage(final String key)
    {
        return bundle.getString(key);
    }
    
    /**
     * Gets message of key and replaces 1 argument using MessageFormat class.
     * @param key name of message
     * @param arg0 1. argument
     * @return formated message
     */
    static public String getMessage(final String key, final String arg0)
    {
        return MessageFormat.format(bundle.getString(key), new Object[] {arg0});
    }
    
    /**
     * Gets message of key and replaces 2 arguments using MessageFormat class.
     * @param key name of message
     * @param arg0 1. argument
     * @param arg1 2. argument
     * @return formated message
     */
    static public String getMessage(final String key, final String arg0, final String arg1)
    {
        return MessageFormat.format(bundle.getString(key), new Object[] {arg0, arg1});
    }
    
    /**
     * Gets message of key and replaces 3 arguments using MessageFormat class.
     * @param key name of message
     * @param arg0 1. argument
     * @param arg1 2. argument
     * @param arg2 3. argument
     * @return formated message
     */
    static public String getMessage(final String key, final String arg0, final String arg1, final String arg2)
    {
        return MessageFormat.format(bundle.getString(key), new Object[] {arg0, arg1, arg2});
    }
}
