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

package org.netbeans.xtest.driver;

import java.io.IOException;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.xtest.driver.ide.BuildInfo;

/**
 * From given IDE platform dir it parses build number, project name and timestamp.
 * IDE platform dir is the directory with lib/org-openide-util.jar, core/core.jar and other libraries.
 */
public class ParseBuildInfo extends Task {
    
    private String buildNumberProperty = "buildinfo.number";
    private String buildDateProperty = "buildinfo.date";
    private String buildInfoParsedProperty = "buildinfo.parsed";
    private String buildProjectProperty = "buildinfo.project";
    private String buildProject = "Unknown";
    private String buildTimestampProperty = null;
    
    private String idePlatformDir;
    
    public void setIdePlatformDir(String idePlatformDir) {
        this.idePlatformDir = idePlatformDir;
    }
    
    public String getIdePlatformDir() {
        return idePlatformDir;
    }

    public void setBuildNumberProperty(String property) {
        this.buildNumberProperty = property;
    }
    
    public void setBuildDateProperty(String property) {
        this.buildDateProperty = property;
    }
    
    public void setBuildProjectNameProperty(String property) {
        this.buildProjectProperty = property;
    }
    
    public void setBuildTimestampProperty(String property) {
        this.buildTimestampProperty = property;
    }
    
    /** Parse build number to timestamp string. For input like this 200303231200
     * or 20030323-1200 (continuous builds)
     * it returns 2003-03-23 12:00 +0000. For build numbers like 030323
     * or 030323_1 it returns null.
     */
    private String getTimestamp(String buildNumber) {
        Date date = null;
        try {
            // pattern for regular builds
            String pattern = "yyyyMMddHHmm";
            if(buildNumber.indexOf('-') > -1) {
                // pattern for continuous builds 
                pattern = "yyyyMMdd-HHmm";
            }
            date = new SimpleDateFormat(pattern).parse(buildNumber);
        } catch (ParseException e) {
            // wrong format
            return null;
        }
        // convert date to timestamp format
        StringBuffer timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm +0000").
                            format(date, new StringBuffer(), new FieldPosition(0));
        return timestamp.toString();
    }
    
    public void execute() throws BuildException {
        log("Parsing product's build_info");
        
        BuildInfo bi;
        try {
            bi = new BuildInfo(idePlatformDir);
        } catch (IOException e) {
            log("Not setting properties! Getting build info from IDE with supplied platform directory "+
                idePlatformDir+" failed for the following reason: "+e.getMessage()+".",
                Project.MSG_WARN);
            return;
        }
        
        String buildNumber = bi.getBuildNumber();
        
        // setting all these properties for this project
        getProject().setProperty(buildNumberProperty, buildNumber);
        // buildinfo.date will be used by cvs update
        getProject().setProperty(buildDateProperty, buildNumber);
        // set also project info
        if (getProject().getProperty(buildProjectProperty) == null) {
            getProject().setProperty(buildProjectProperty, bi.getProductName());
        }
        
        if (buildTimestampProperty != null) {
            String timestamp = getTimestamp(buildNumber);
            if (timestamp != null)
                getProject().setUserProperty(buildTimestampProperty,timestamp);
        }
        
        // build info file was succesfully parsed
        getProject().setProperty(buildInfoParsedProperty, "true");
    }
    
}
