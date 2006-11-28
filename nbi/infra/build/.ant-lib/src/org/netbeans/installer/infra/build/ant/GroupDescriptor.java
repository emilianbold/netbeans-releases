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
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.AntUtils;

public class GroupDescriptor extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String file  = null;
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setFile(final String file) {
        this.file = file;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        Project project = getProject();
        AntUtils.setProject(project);
        
        StringBuilder builder = new StringBuilder();
        
        // header ///////////////////////////////////////////////////////////////////
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        // core data ////////////////////////////////////////////////////////////////
        String uid       = project.getProperty("group.uid");
        String offset    = project.getProperty("group.offset");
        String preExpand = project.getProperty("group.pre-expand");
        
        builder.append("<group uid=\"" + uid + "\" " +
                "offset=\"" + offset + "\" " +
                "pre-expand=\"" + preExpand + "\">\n");
        
        // locales //////////////////////////////////////////////////////////////////
        String[] locales = project.getProperty("group.locales.list").split(" ");
        
        // display name /////////////////////////////////////////////////////////////
        String displayName = project.getProperty("group.display.name.default");
        builder.append("    <display-name>\n");
        builder.append("        <default>" + displayName + "</default>\n");
        for (String locale: locales) {
            displayName = project.getProperty("group.display.name." + locale);
            builder.append("        <localized locale=\"" + locale + "\">" + 
                    displayName + "</localized>\n");
        }
        builder.append("    </display-name>\n");
        
        // description //////////////////////////////////////////////////////////////
        String description = project.getProperty("group.description.default");
        builder.append("    <description>\n");
        builder.append("        <default>" + description + "</default>\n");
        for (String locale: locales) {
            description = project.getProperty("group.description." + locale);
            builder.append("        <localized locale=\"" + locale + "\">" + 
                    description + "</localized>\n");
        }
        builder.append("    </description>\n");
        
        // icon /////////////////////////////////////////////////////////////////////
        String size  = project.getProperty("group.icon.size");
        String md5   = project.getProperty("group.icon.md5");
        String sha1  = project.getProperty("group.icon.sha1");
        String crc32 = project.getProperty("group.icon.crc32");
        String uri   = project.getProperty("group.icon.uri");
        
        builder.append("    <icon size=\"" + size + "\" crc32=\"" + crc32 + 
                "\" md5=\"" + md5 + "\" sha1=\"" + sha1 + "\">" + uri + "</icon>\n");
        
        // properties ///////////////////////////////////////////////////////////////
        int length = Integer.parseInt(project.getProperty("group.properties.length"));
        builder.append("    <properties>\n");
        for (int i = 1; i <= length; i++) {
            String name = project.getProperty("group.properties." + i + ".name");
            String value = project.getProperty("group.properties." + i + ".value");
            builder.append("        <property name=\"" + name + "\">" + 
                    value + "</property>\n");
        }
        builder.append("    </properties>\n");
        
        builder.append("</group>\n");
        
        try {
            AntUtils.writeFile(new File(file), builder);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
