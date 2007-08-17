/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * Thsi class is an ant task which converts an URI to a relative path. This is
 * useful for caching the downloads.
 *
 * @author Kirill Sorokin
 */
public class UriToPath extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * URI which should be converted.
     */
    private String uriString;
    
    /**
     * Name of the property whose value should contain the size.
     */
    private String property;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'uri' property.
     *
     * @param uri New value for the 'uri' property.
     */
    public void setUri(final String uri) {
        this.uriString = uri;
    }
    
    /**
     * Setter for the 'property' property.
     *
     * @param property New value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task.
     */
    public void execute() throws BuildException {
        try {
            final URI uri = new URI(uriString);
            
            String path = uri.getSchemeSpecificPart();
            while (path.startsWith("/")) { // NOI18N
                path = path.substring(1).replace(":", "_");
            }
            
            if (uri.getScheme().equals("file")) {
                path = "local/" + path.replace(":", "_");
            }
            
            getProject().setProperty(property, path);
        } catch (URISyntaxException e) {
            throw new BuildException("Cannot parse URI."); // NOI18N
        }
    }
}
