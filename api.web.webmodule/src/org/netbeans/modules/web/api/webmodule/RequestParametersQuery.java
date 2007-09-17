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

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.web.spi.webmodule.RequestParametersQueryImplementation;
import org.openide.util.Parameters;

/**
 * This query serves for executing single file in the server.
 * It returns the request parameters for a given file.
 *
 * @see org.netbeans.modules.web.spi.webmodule.RequestParametersQueryImplementation
 *
 * @author Pavel Buzek
 */
public final class RequestParametersQuery {

    private static final Lookup.Result<RequestParametersQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(RequestParametersQueryImplementation.class);

    /**
     * Returns the part of URL for access the file. It can include the query string.
     * @param  file the file to find the request parameters for.
     * @return path from the context; can be null.
     * @throws NullPointerException if the <code>file</code> parameter is null.
     */
    public static String getFileAndParameters(FileObject file) {
        Parameters.notNull("file", file); // NOI18N
        for (RequestParametersQueryImplementation impl : implementations.allInstances()) {
            String params = impl.getFileAndParameters(file);
            if (params != null) {
                return params;
            }
        }
        return null;
    }
}
