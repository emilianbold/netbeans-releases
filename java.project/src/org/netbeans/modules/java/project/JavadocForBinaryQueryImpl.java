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

package org.netbeans.modules.java.project;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;

/**
 * Delegates {@link JavadocForBinaryQueryImplementation} to the project which
 * owns the binary file.
 */
public class JavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(JavadocForBinaryQueryImpl.class.getName());
    
    /** Default constructor for lookup. */
    public JavadocForBinaryQueryImpl() {
    }
    
    public JavadocForBinaryQuery.Result findJavadoc(URL binary) {
        boolean log = ERR.isLoggable(ErrorManager.INFORMATIONAL);
        Project project = FileOwnerQuery.getOwner(URI.create(binary.toString()));
        if (project != null) {
            JavadocForBinaryQueryImplementation jfbqi = project.getLookup().lookup(JavadocForBinaryQueryImplementation.class);
            if (jfbqi != null) {
                JavadocForBinaryQuery.Result result = jfbqi.findJavadoc(binary);
                if (log) ERR.log("Project " + project + " reported for " + binary + ": " + (result != null ? Arrays.asList(result.getRoots()) : null));
                return result;
            } else {
                if (log) ERR.log("Project " + project + " did not have any JavadocForBinaryQueryImplementation");
            }
        } else {
            if (log) ERR.log("No project found for " + binary + "; cannot find Javadoc");
        }
        return null;
    }
    
}
