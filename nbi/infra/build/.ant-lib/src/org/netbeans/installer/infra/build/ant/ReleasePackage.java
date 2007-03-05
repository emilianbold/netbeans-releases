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
package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.AntUtils;

public class ReleasePackage extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String url       = "";
    
    private String registry  = "";
    private String uid       = "";
    private String version   = "";
    private String platforms = "";
    private String archive   = "";
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setRegistry(String registry) {
        this.registry = registry;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public void setPlatforms(String platforms) {
        this.platforms = platforms;
    }
    
    public void setArchive(String archive) {
        this.archive = archive;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        try {
            final Map<String, Object> args = new HashMap<String, Object>();
            
            File file = new File(archive);
            if (!file.equals(file.getAbsoluteFile())) {
                file = new File(getProject().getBaseDir(), archive);
            }
            
            args.put("registry", registry);
            args.put("uid", uid);
            args.put("version", version);
            args.put("platforms", platforms);
            args.put("archive", file);
            
            log(AntUtils.post(url + "/add-package", args));
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
