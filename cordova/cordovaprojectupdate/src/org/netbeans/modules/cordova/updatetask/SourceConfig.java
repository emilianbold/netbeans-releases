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
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * API for config.xml
 * @author Jan Becicka
 */
public class SourceConfig extends XMLFile {
    
    public static final String ANDROID_PLATFORM = "android";
    public static final String IOS_PLATFORM = "ios";

    public SourceConfig(InputStream resource) throws IOException {
        super(resource);
    }

    public SourceConfig(File f) throws IOException {
        super(f);
    }

    public String getName() {
        return getTextContent("/widget/name");
    }
    
    public void setName(String name) {
        setTextContent("/widget/name", name);
    }

    public String getDescription() {
        return getTextContent("/widget/description");
    }

    public void setDescription(String description) {
        setTextContent("/widget/description", description);
    }

    public String getAuthor() {
        return getTextContent("/widget/author");
    }

    public void setAuthor(String author) {
        setTextContent("/widget/author", author);
    }
    
    public String getAuthorHref() {
        return getAttributeText("/widget/author", "href");
    }

    public void setAuthorHref(String href) {
        setAttributeText("/widget/author", "href", href);
    }

    public String getAuthorEmail() {
        return getAttributeText("/widget/author", "email");
    }

    public void setAuthorEmail(String email) {
        setAttributeText("/widget/author", "email", email);
    }
    
    public String getId() {
        return getAttributeText("/widget", "id");
    }
    
    public String getVersion() {
        return getAttributeText("/widget", "version");
    }
    
    public String getAccess() {
        return getAttributeText("/widget/access", "origin");
    }
    
    public void setAccess(String access) {
        setAttributeText("/widget/access", "origin", access);
    }

    public void setId(String id) {
        setAttributeText("/widget", "id", id);
    }
    
    public void setVersion(String version) {
        setAttributeText("/widget", "version", version);
    }

    public String getIcon(String platform, int width, int height) {
        return getSplashOrIcon("icon", platform, width, height);
    }

    public void setIcon(String platform, int width, int height, String value) {
        setSplashOrIcon("icon", platform, width, height, value);
    }
    
    
    private String getSplashOrIcon(String name, String platform, int width, int height) {
        Node node = getSplashOrIconNode(name, platform, width, height);
        if (node == null) {
            return null;
        }
        return getAttributeText(node, "src");
    }
    
    private Node getSplashOrIconNode(String name, String platform, int width, int height) {
        final NodeList icons = doc.getElementsByTagName(name);
        for (int i=0; i < icons.getLength();i++) {
            Node n = icons.item(i);
            if (platform.equals(getAttributeText(n, "gap:platform")) &&
                    Integer.toString(width).equals(getAttributeText(n, "width")) &&
                    Integer.toString(height).equals(getAttributeText(n, "height"))) {
                return n;
            }
        }
        return null;
    }
    
    private void setSplashOrIcon(String name, String platform, int width, int height, String value) {
        Node n = getSplashOrIconNode(name, platform, width, height);
        if (n!=null) {
            final Attr src = doc.createAttribute("src");
            src.setValue(value);
            n.getAttributes().setNamedItem(src);
        } else {
            Element element = doc.createElement(name);
            element.setAttribute("src", value);
            element.setAttribute("width", Integer.toString(width));
            element.setAttribute("height", Integer.toString(height));
            element.setAttribute("gap:platform", platform);
            NodeList elementsByTagName = doc.getElementsByTagName(name);
            Node widget = getNode("/widget");
            if (elementsByTagName!=null && elementsByTagName.getLength()>0) {
                widget.insertBefore(element, elementsByTagName.item(0));
            } else {
                widget.appendChild(element);
            }
        }
    }

    
    public String getSplash(String platform, int width, int height) {
        return getSplashOrIcon("gap:splash", platform, width, height);
    }
    
    public void setSplash(String platform, int width, int height, String value) {
        setSplashOrIcon("gap:splash", platform, width, height, value);
    }
    
    public String getPreference(String name) {
        final NodeList pref = doc.getElementsByTagName("preference");
        for (int i=0; i < pref.getLength();i++) {
            Node n = pref.item(i);
            if (name.equals(getAttributeText(n, "name"))) {
                return getAttributeText(n, "value");
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
    
    public void setIcon(String platform, String value) {
        if (platform.equals("ios")) {
            setIcon(platform, 144, 144, value);
        } else {
            setIcon(platform, 96, 96, value);
        }
    }
}
