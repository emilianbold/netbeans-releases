/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
            JavadocForBinaryQueryImplementation jfbqi =
                (JavadocForBinaryQueryImplementation)project.getLookup().lookup(
                    JavadocForBinaryQueryImplementation.class);
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
