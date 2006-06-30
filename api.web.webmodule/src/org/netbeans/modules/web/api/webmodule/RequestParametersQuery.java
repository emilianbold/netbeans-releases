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
