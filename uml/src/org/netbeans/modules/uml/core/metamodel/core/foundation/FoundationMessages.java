/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * File       : FoundationMessages.java
 * Created on : Oct 8, 2003
 * Author     : aztec
 */
package org.netbeans.modules.uml.core.metamodel.core.foundation;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Resource bundle front-end for Foundation messages.
 * @author aztec
 */
public class FoundationMessages
{
    /**
     * Returns a resource string, given its key.
     *
     * @param resourceKey The key of the resource to retrieve.
     * @return The resource string, if found, or a modified key if the resource
     *         is unavailable.
     */
    public static String getString(String resourceKey)
    {
        try
        {
            return BUNDLE.getString(resourceKey);
        }
        catch (MissingResourceException e)
        {
            return '!' + resourceKey + '!';
        }
    }
    
    /**
     * Obtains a resource string, given its key, and applies a MessageFormat to
     * it.
     * 
     * @param resourceKey The key of the resource to retrieve.
     * @param parameters  The parameters to be inserted into the formatted 
     *                    string. If <code>null</code>, the function behaves
     *                    exactly as <code>getString(String)</code>.
     * @return The formatted resource string, if found, or a modified key if the
     *         resource is unavailable.
     */
    public static String getString(String resourceKey, Object[] parameters)
    {
        String s = getString(resourceKey);
        return parameters != null? MessageFormat.format(s, parameters) : s;
    }
    
    private static final String BUNDLE_CLASS = 
        "org.netbeans.modules.uml.core.metamodel.core.foundation.Bundle";
    private static final ResourceBundle BUNDLE = 
        ResourceBundle.getBundle(BUNDLE_CLASS);
}