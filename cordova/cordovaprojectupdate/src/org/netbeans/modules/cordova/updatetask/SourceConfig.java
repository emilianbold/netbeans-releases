/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.updatetask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * API for config.xml
 * @author Jan Becicka
 */
public class SourceConfig extends XMLFile {

    public SourceConfig(InputStream resource) throws IOException {
        super(resource);
    }

    public SourceConfig(File f) throws IOException {
        super(f);
    }

    public String getName() {
        return getNode("name").getTextContent();
    }
    
    public void setName(String name) {
        getNode("name").setTextContent(name);
    }

    public String getDescription() {
        return getNode("description").getTextContent();
    }

    public void setDescription(String description) {
        getNode("description").setTextContent(description);
    }

    public String getAuthor() {
        return getNode("author").getTextContent();
    }

    public void setAuthor(String author) {
        getNode("author").setTextContent(author);
    }
    
    public String getId() {
        return getNode("widget").getAttributes().getNamedItem("id").getTextContent();
    }
    
    public String getAccess() {
        return getNode("access").getAttributes().getNamedItem("origin").getTextContent();
    }
    
    public void setAccess(String access) {
        getNode("access").getAttributes().getNamedItem("origin").setTextContent(access);
    }

    public void setId(String id) {
        getNode("widget").getAttributes().getNamedItem("id").setTextContent(id);
    }

    public String getIcon(String platform, int width, int height) {
        final NodeList icons = doc.getElementsByTagName("icon");
        for (int i=0; i < icons.getLength();i++) {
            final NamedNodeMap attributes = icons.item(i).getAttributes();
            if (platform.equals(attributes.getNamedItem("gap:platform").getTextContent()) &&
                    Integer.toString(width).equals(attributes.getNamedItem("width").getTextContent()) &&
                    Integer.toString(height).equals(attributes.getNamedItem("height").getTextContent())) {
                return attributes.getNamedItem("src").getTextContent();
            }
        }
        return null;
    }
    
    public String getSplash(String platform, int width, int height) {
        final NodeList splash = doc.getElementsByTagName("gap:splash");
        for (int i=0; i < splash.getLength();i++) {
            final NamedNodeMap attributes = splash.item(i).getAttributes();
            if (platform.equals(attributes.getNamedItem("gap:platform").getTextContent()) &&
                    Integer.toString(width).equals(attributes.getNamedItem("width").getTextContent()) &&
                    Integer.toString(height).equals(attributes.getNamedItem("height").getTextContent())) {
                return attributes.getNamedItem("src").getTextContent();
            }
        }
        return null;
    }
    
    public String getPreference(String name) {
        final NodeList splash = doc.getElementsByTagName("preference");
        for (int i=0; i < splash.getLength();i++) {
            final NamedNodeMap attributes = splash.item(i).getAttributes();
            if (name.equals(attributes.getNamedItem("name").getTextContent())) {
                return attributes.getNamedItem("value").getTextContent();
            }
        }
        return null;
    }
    
        
    public String getIcon(String platform) {
        if (platform.equals("ios")) {
            return getIcon(platform, 144,144);
        } else {
            return getIcon(platform, 96,96);
        }
    }    
}
