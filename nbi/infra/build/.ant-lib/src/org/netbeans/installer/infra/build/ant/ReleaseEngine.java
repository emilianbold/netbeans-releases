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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task that is capable or releasing an NBI engine to the 
 * registries server.
 * 
 * @author Kirill Sorokin
 */
public class ReleaseEngine extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * URL of the registries server to which the engine should be released.
     */
    private String url;
    
    /**
     * The engine distributive file.
     */
    private File archive;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'url' property.
     * 
     * @param url The new value of the 'url' property.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Setter for the 'archive' property.
     * 
     * @param path The new value of the 'archive' property.
     */
    public void setArchive(String path) {
        archive = new File(path);
        if (!archive.equals(archive.getAbsoluteFile())) {
            archive = new File(getProject().getBaseDir(), path);
        }
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. This method sends an HTTP POST request tyo the server, 
     * uploading the engine's archive.
     * 
     * @throws org.apache.tools.ant.BuildException if an I/O error occurs.
     */
    public void execute() throws BuildException {
        try {
            final Map<String, Object> args = new HashMap<String, Object>();
            
            args.put("archive", archive);                                   // NOI18N
            
            log(Utils.post(url + "/update-engine", args));               // NOI18N
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
