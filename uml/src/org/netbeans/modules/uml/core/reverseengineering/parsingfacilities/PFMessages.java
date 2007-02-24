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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import org.netbeans.modules.uml.core.support.umlutils.MessagesBundle;

/**
 */
public class PFMessages
{
    private static final String BUNDLE_CLASS = "org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Bundle";
    private static final MessagesBundle BUNDLE = new MessagesBundle(BUNDLE_CLASS);

    /**
     * Returns a resource string, given its key.
     *
     * @param resourceKey The key of the resource to retrieve.
     * @return The resource string, if found, or a modified key if the resource
     *         is unavailable.
     */
    public static String getString(String resourceKey)
    {
        return BUNDLE.getString(resourceKey);
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
        return BUNDLE.getString(resourceKey, parameters);
    }
}