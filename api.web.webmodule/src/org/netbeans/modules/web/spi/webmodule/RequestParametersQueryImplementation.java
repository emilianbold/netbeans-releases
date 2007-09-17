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

package org.netbeans.modules.web.spi.webmodule;

import org.openide.filesystems.FileObject;

/**
 * This is the SPI counterpart of {@link org.netbeans.modules.web.api.webmodule.RequestParametersQuery}.
 * Register an instance of this provider in the default lookup to provide
 * access to the file part of URL and request parameters.
 *
 * @author Pavel Buzek
 */
public interface RequestParametersQueryImplementation {

    /**
     * Return the part of URL for access the file. It can include the query string.
     *
     * @param  file the file for find the request parameters for; never null.
     * @return path fom the context; can be null.
     */
    String getFileAndParameters (FileObject file);
}
