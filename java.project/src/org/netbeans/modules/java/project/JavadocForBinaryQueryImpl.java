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

package org.netbeans.modules.java.project;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
    
    /** Default constructor for lookup. */
    public JavadocForBinaryQueryImpl() {
    }
    
    public JavadocForBinaryQuery.Result findJavadoc(URL binary) {
        Project project = FileOwnerQuery.getOwner(URI.create(binary.toString()));
        if (project != null) {
            JavadocForBinaryQueryImplementation jfbqi =
                (JavadocForBinaryQueryImplementation)project.getLookup().lookup(
                    JavadocForBinaryQueryImplementation.class);
            if (jfbqi != null) {
                return jfbqi.findJavadoc(binary);
            }
        }
        return null;
    }
    
}
