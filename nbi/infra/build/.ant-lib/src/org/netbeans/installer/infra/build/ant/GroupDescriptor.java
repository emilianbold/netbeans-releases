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
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which creates a group package descriptor based on the
 * existing project properties and writes it to the specified file.
 *
 * @author Kirill Sorokin
 */
public class GroupDescriptor extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * File to which the group descriptor should be written.
     */
    private File file;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'file' property.
     * 
     * @param path The new value of the 'file' property.
     */
    public void setFile(final String path) {
        file = new File(path);
        if (!file.equals(file.getAbsoluteFile())) {
            file = new File(getProject().getBaseDir(), path);
        }
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. This method writes the group package descriptor xml code
     * to the specified file.
     * 
     * @throws org.apache.tools.ant.BuildException if a I/O error occurs.
     */
    public void execute() throws BuildException {
        final Project project = getProject();
        Utils.setProject(project);
        
        final StringBuilder xml = new StringBuilder();
        
        // header ///////////////////////////////////////////////////////////////////
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");         // NOI18N
        xml.append("<registry " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +// NOI18N 
                "xsi:noNamespaceSchemaLocation=\"registry.xsd\">\n");       // NOI18N
        xml.append("    <components>\n");                                   // NOI18N
        
        // core data ////////////////////////////////////////////////////////////////
        final String uid = project.getProperty("group.uid");                // NOI18N
        final String offset = project.getProperty("group.offset");          // NOI18N
        final String expand = project.getProperty("group.expand");          // NOI18N
        final String visible = project.getProperty("group.visible");        // NOI18N
        
        xml.append("        <group uid=\"" + uid + "\" " +                  // NOI18N
                "offset=\"" + offset + "\" " +                              // NOI18N
                "expand=\"" + expand + "\" " +                              // NOI18N
                "built=\"" + new Date().getTime() + "\" " +                 // NOI18N
                "visible=\"" + visible + "\">\n");                          // NOI18N
        
        // locales //////////////////////////////////////////////////////////////////
        final String locales = 
                project.getProperty("group.locales.list").trim();           // NOI18N
        
        // display name /////////////////////////////////////////////////////////////
        String displayName = 
                project.getProperty("group.display.name.default");          // NOI18N
        
        xml.append("            <display-name>\n");                         // NOI18N
        xml.append("                <default><![CDATA[" + displayName +     // NOI18N
                "]]></default>\n");                                         // NOI18N
        
        if (!locales.equals("")) {                                          // NOI18N
            for (String locale: locales.split(" ")) {                       // NOI18N
                displayName = project.getProperty(
                        "group.display.name." + locale);                    // NOI18N
                xml.append("                <localized locale=\"" +         // NOI18N
                        locale + "\"><![CDATA[" +                           // NOI18N
                        displayName + "]]></localized>\n");                 // NOI18N
            }
        }
        xml.append("            </display-name>\n");                        // NOI18N
        
        // description //////////////////////////////////////////////////////////////
        String description = 
                project.getProperty("group.description.default");           // NOI18N
        
        xml.append("            <description>\n");                          // NOI18N
        xml.append("                <default><![CDATA[" + description +     // NOI18N
                "]]></default>\n");                                         // NOI18N
        
        if (!locales.equals("")) {                                          // NOI18N
            for (String locale: locales.split(" ")) {                       // NOI18N
                description = project.getProperty(
                        "group.description." + locale);                     // NOI18N
                xml.append("                <localized locale=\"" +         // NOI18N
                        locale + "\"><![CDATA[" +                           // NOI18N
                        description + "]]></localized>\n");                 // NOI18N
            }
        }
        xml.append("            </description>\n");                         // NOI18N
        
        // icon /////////////////////////////////////////////////////////////////////
        final String size = project.getProperty("group.icon.size");         // NOI18N
        final String md5 = project.getProperty("group.icon.md5");           // NOI18N
        final String uri = project.getProperty("group.icon.correct.uri");   // NOI18N
        
        xml.append("            <icon " +                                   // NOI18N
                "size=\"" + size + "\" " +                                  // NOI18N
                "md5=\"" + md5 + "\">\n");                                  // NOI18N
        xml.append("                <default-uri>" +                        // NOI18N
                uri.replace(" ", "%20") + "</default-uri>\n");              // NOI18N
        xml.append("            </icon>\n");                                // NOI18N
        
        // properties ///////////////////////////////////////////////////////////////
        final int length = Integer.parseInt(
                project.getProperty("group.properties.length"));            // NOI18N
        
        if (length > 0) {
            xml.append("            <properties>\n");                       // NOI18N
            for (int i = 1; i <= length; i++) {
                final String name = project.getProperty(
                        "group.properties." + i + ".name");                 // NOI18N
                final String value = project.getProperty(
                        "group.properties." + i + ".value");                // NOI18N
                xml.append("                <property name=\"" +            // NOI18N
                        name + "\">" + value + "</property>\n");            // NOI18N
            }
            xml.append("            </properties>\n");                      // NOI18N
        }
        
        xml.append("        </group>\n");                                   // NOI18N
        xml.append("    </components>\n");                                  // NOI18N
        xml.append("</registry>\n");                                        // NOI18N
        
        
        try {
            Utils.write(file, xml);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
