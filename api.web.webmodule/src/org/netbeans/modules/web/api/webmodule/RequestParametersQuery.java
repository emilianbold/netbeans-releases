/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.api.webmodule;

import java.util.Iterator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.web.spi.webmodule.RequestParametersProvider;

/** This query serves for executing single file in the server. 
 * It returns request parameters for a given file.
 * 
 * @see {@link org.netbeans.modules.web.spi.RequestParametersProvider} if you 
 * want to implement this query.
 *
 * @author Pavel Buzek
 */
public final class RequestParametersQuery {
    
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(RequestParametersProvider.class));
    
    /** Return the part of URL for access the file. It can include the query string.
     * @return path fom the context.
     */    
    public static String getFileAndParameters(FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to RequestParametersQuery.getRequestParameters(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            RequestParametersProvider impl = (RequestParametersProvider)it.next();
            String params = impl.getFileAndParameters(f);
            if (params != null) {
                return params;
            }
        }
        return null;
    }
}
