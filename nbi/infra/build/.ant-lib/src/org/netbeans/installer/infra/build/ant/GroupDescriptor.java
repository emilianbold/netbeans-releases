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
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.AntUtils;

public class GroupDescriptor extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String path  = null;
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setFile(final String file) {
        this.path = file;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        Project project = getProject();
        AntUtils.setProject(project);
        
        StringBuilder xml = new StringBuilder();
        
        // header ///////////////////////////////////////////////////////////////////
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<registry xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"registry.xsd\">\n");
        xml.append("    <components>\n");
        
        // core data ////////////////////////////////////////////////////////////////
        String uid     = project.getProperty("group.uid");
        String offset  = project.getProperty("group.offset");
        String expand  = project.getProperty("group.expand");
        String visible = project.getProperty("group.visible");
        
        xml.append("        <group uid=\"" + uid + "\" " +
                "offset=\"" + offset + "\" " +
                "expand=\"" + expand + "\" " +
                "built=\"" + new Date().getTime() + "\" " +
                "visible=\"" + visible + "\">\n");
        
        // locales //////////////////////////////////////////////////////////////////
        String locales = project.getProperty("group.locales.list").trim();
        
        // display name /////////////////////////////////////////////////////////////
        String displayName = project.getProperty("group.display.name.default");
        xml.append("            <display-name>\n");
        xml.append("                <default><![CDATA[" + displayName + "]]></default>\n");
        if (!locales.equals("")) {
            for (String locale: locales.split(" ")) {
                displayName = project.getProperty("group.display.name." + locale);
                xml.append("                <localized locale=\"" + locale + "\"><![CDATA[" +
                        displayName + "]]></localized>\n");
            }
        }
        xml.append("            </display-name>\n");
        
        // description //////////////////////////////////////////////////////////////
        String description = project.getProperty("group.description.default");
        xml.append("            <description>\n");
        xml.append("                <default><![CDATA[" + description + "]]></default>\n");
        if (!locales.equals("")) {
            for (String locale: locales.split(" ")) {
                description = project.getProperty("group.description." + locale);
                xml.append("                <localized locale=\"" + locale + "\"><![CDATA[" +
                        description + "]]></localized>\n");
            }
        }
        xml.append("            </description>\n");
        
        // icon /////////////////////////////////////////////////////////////////////
        String size  = project.getProperty("group.icon.size");
        String md5   = project.getProperty("group.icon.md5");
        String uri   = project.getProperty("group.icon.uri");
        
        xml.append("            <icon " +
                "size=\"" + size + "\" " +
                "md5=\"" + md5 + "\">\n");
        xml.append("                <default-uri>" +
                uri.replace(" ", "%20") + "</default-uri>\n");
        xml.append("            </icon>\n");
        
        // properties ///////////////////////////////////////////////////////////////
        int length = Integer.parseInt(project.getProperty("group.properties.length"));
        if (length > 0) {
            xml.append("            <properties>\n");
            for (int i = 1; i <= length; i++) {
                String name = project.getProperty("group.properties." + i + ".name");
                String value = project.getProperty("group.properties." + i + ".value");
                xml.append("                <property name=\"" + name + "\">" +
                        value + "</property>\n");
            }
            xml.append("            </properties>\n");
        }
        
        xml.append("        </group>\n");
        xml.append("    </components>\n");
        xml.append("</registry>\n");
        
        
        try {
            File file = new File(path);
            if (!file.equals(file.getAbsoluteFile())) {
                file = new File(project.getBaseDir(), path);
            }
            
            AntUtils.write(file, xml);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
