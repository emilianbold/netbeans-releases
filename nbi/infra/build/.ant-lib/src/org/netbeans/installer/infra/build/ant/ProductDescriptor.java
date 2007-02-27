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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.AntUtils;

public class ProductDescriptor extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String path;
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setFile(final String file) {
        this.path = file;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        AntUtils.setProject(getProject());
        
        StringBuilder xml = new StringBuilder();
        
        // header ///////////////////////////////////////////////////////////////////
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<registry xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"registry.xsd\">\n");
        xml.append("    <components>\n");
        
        // core data ////////////////////////////////////////////////////////////////
        String uid      = get("product.uid");
        String version  = get("product.version");
        String platform = get("product.platforms");
        String status   = get("product.status");
        String offset   = get("product.offset");
        String expand   = get("product.expand");
        String visible  = get("product.visible");
        String features = get("product.features");
        
        xml.append("        <product uid=\"" + uid + "\" " +
                "version=\"" + version + "\" " +
                "platforms=\"" + platform + "\" " +
                "status=\"" + status + "\" " +
                "offset=\"" + offset + "\" " +
                "expand=\"" + expand + "\" " +
                "built=\"" + new Date().getTime() + "\" " + 
                "visible=\"" + visible + "\" " + 
                "features=\"" + features + "\">\n");
        
        // locales //////////////////////////////////////////////////////////////////
        String locales = get("product.locales.list").trim();
        
        // display name /////////////////////////////////////////////////////////////
        xml.append("            <display-name>\n");
        xml.append("                <default><![CDATA[" +
                get("product.display.name.default") + "]]></default>\n");
        if (!locales.equals("")) {
            for (String locale: locales.split(" ")) {
                xml.append("                <localized locale=\"" + locale + "\"><![CDATA[" +
                        get("product.display.name." + locale) + "]]></localized>\n");
            }
        }
        xml.append("            </display-name>\n");
        
        // description //////////////////////////////////////////////////////////////
        xml.append("            <description>\n");
        xml.append("                <default><![CDATA[" +
                get("product.description.default") + "]]></default>\n");
        if (!locales.equals("")) {
            for (String locale: locales.split(" ")) {
                xml.append("                <localized locale=\"" + locale + "\"><![CDATA[" +
                        get("product.description." + locale) + "]]></localized>\n");
            }
        }
        xml.append("            </description>\n");
        
        // icon /////////////////////////////////////////////////////////////////////
        String size = get("product.icon.size");
        String md5 = get("product.icon.md5");
        String uri = get("product.icon.uri");
        
        xml.append("            <icon " +
                "size=\"" + size + "\" " +
                "md5=\"" + md5 + "\">\n");
        xml.append("                <default-uri>" +
                uri.replace(" ", "%20") + "</default-uri>\n");
        xml.append("            </icon>\n");
        
        // properties ///////////////////////////////////////////////////////////////
        if (getInt("product.properties.length") > 0) {
            xml.append("            <properties>\n");
            for (int i = 1; i <= getInt("product.properties.length"); i++) {
                String name = get("product.properties." + i + ".name");
                String value = get("product.properties." + i + ".value");
                xml.append("                <property name=\"" + name + "\"><![CDATA[" +
                        value + "]]></property>\n");
            }
            xml.append("            </properties>\n");
        }
        
        // configuration logic //////////////////////////////////////////////////////
        xml.append("            <configuration-logic>\n");
        for (int i = 1; i <= getInt("product.logic.length"); i++) {
            size  = get("product.logic." + i + ".size");
            md5   = get("product.logic." + i + ".md5");
            uri   = get("product.logic." + i + ".uri");
            
            xml.append("                <file " +
                    "size=\"" + size + "\" " +
                    "md5=\"" + md5 + "\">\n");
            
            xml.append(
                    "                    <default-uri>" + 
                    uri.replace(" ", "%20") + 
                    "</default-uri>\n");
            xml.append("                </file>\n");
        }
        xml.append("            </configuration-logic>\n");
        
        // installation data ////////////////////////////////////////////////////////
        xml.append("            <installation-data>\n");
        for (int i = 1; i <= getInt("product.data.length"); i++) {
            size  = get("product.data." + i + ".size");
            md5   = get("product.data." + i + ".md5");
            uri   = get("product.data." + i + ".uri");
            
            xml.append("                <file " +
                    "size=\"" + size + "\" " +
                    "md5=\"" + md5 + "\">\n");
            xml.append(
                    "                    <default-uri>" + 
                    uri.replace(" ", "%20") + 
                    "</default-uri>\n");
            xml.append("                </file>\n");
        }
        xml.append("            </installation-data>\n");
        
        // requirements /////////////////////////////////////////////////////////////
        xml.append("            <system-requirements>\n");
        xml.append("                <disk-space>" +
                get("product.disk.space") + "</disk-space>\n");
        xml.append("            </system-requirements>\n");
        
        // dependencies /////////////////////////////////////////////////////////////
        if (getInt("product.requirements.length") + 
                getInt("product.conflicts.length") +
                getInt("product.install-afters.length") > 0) {
            xml.append("            <dependencies>\n");
            
            for (int i = 1; i <= getInt("product.requirements.length"); i++) {
                uid = get("product.requirements." + i + ".uid");
                
                String lower = 
                        get("product.requirements." + i + ".version-lower");
                String upper = 
                        get("product.requirements." + i + ".version-upper");
                
                xml.append("                <requirement " +
                        "uid=\"" + uid + "\" " +
                        "version-lower=\"" + lower + "\" " +
                        "version-upper=\"" + upper + "\"/>\n");
            }
            
            for (int i = 1; i <= getInt("product.conflicts.length"); i++) {
                uid = get("product.conflicts." + i + ".uid");
                
                String lower = get("product.conflicts." + i + ".version-lower");
                String upper = get("product.conflicts." + i + ".version-upper");
                
                xml.append("                <conflict " +
                        "uid=\"" + uid + "\" " +
                        "version-lower=\"" + lower +
                        "\" version-upper=\"" + upper + "\"/>\n");
            }
            
            for (int i = 1; i <= getInt("product.install-afters.length"); i++) {
                uid = get("product.install-afters." + i + ".uid");
                
                xml.append("                <install-after " + "uid=\"" + uid + "\"/>\n");
            }
            
            xml.append("            </dependencies>\n");
        }
        
        xml.append("        </product>\n");
        xml.append("    </components>\n");
        xml.append("</registry>\n");
        
        try {
            File file = new File(path);
            if (!file.equals(file.getAbsoluteFile())) {
                file = new File(getProject().getBaseDir(), path);
            }
            
            AntUtils.write(file, xml);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private String get(String name) {
        return getProject().getProperty(name);
    }
    
    private int getInt(String name) {
        if (get(name) == null) {
            return 0;
        } else {
            return Integer.parseInt(get(name));
        }
    }
}
