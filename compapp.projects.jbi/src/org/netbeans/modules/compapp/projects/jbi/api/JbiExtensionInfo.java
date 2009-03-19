/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.compapp.projects.jbi.api;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * JBI extension info.
 *
 * @author tli
 */
public class JbiExtensionInfo implements Serializable {

    /**
     * Name of the extension, e.x., "ConfigExtension".
     */
    private String name;
    
    /**
     * Display name of the extension, e.x., "Config Extension".
     */
    private String displayName;

    /**
     * Name of the extension schema file.
     */
    private String file;

    /**
     * Type of the extension, e.x., "port", "endpoint", "connection", or "su".
     */
    private String type;

    /**
     * Subtype of the extension, e.x., "consume" or "provide" under "endpoint".
     */
    private String subType;

    /**
     * Name of the target component in regular expression, 
     * e.x., "sun-http-binding", ".*".
     */
    private String target;

    /**
     * DOCUMENT ME!
     */
    private String description;

    /**
     * DOCUMENT ME!
     */
    private URL icon;

    /**
     * DOCUMENT ME!
     */
    private String ns;

    /**
     * DOCUMENT ME!
     */
    private String provider;

    /**
     * DOCUMENT ME!
     */
    private List<JbiExtensionElement> elements;

    /**
     * DOCUMENT ME!
     *
     * @param name      extension name, e.x., "ConfigExtension"
     * @param displayName  extension diplay name, e.x., "Config Extension"
     * @param type      extension type, e.x., "endpoint", "connection", "port"
     * @param subType   extension subtype, e.x., "consume", "provide"
     * @param target    extension target component name in regular expression, 
     *                  e.x., "sun-http-binding", ".*".
     * @param file      schema file
     * @param ns        extension namespace
     * @param description   extension description
     * @param icon      extension icon resource
     * @param elements  a list of extension elements
     */
    public JbiExtensionInfo(String name, String displayName, 
            String type, String subType, String target,
            String file, String ns, String description, URL icon,
            String provider, List<JbiExtensionElement> elements) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.subType = subType;
        this.target = target;
        this.file = file;
        this.ns = ns;
        this.icon = icon;
        this.provider = provider;
        this.description = description;
        this.elements = elements;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the icon.
     */
    public URL getIcon() {
        return this.icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the description
     */
    public String getName() {
        return this.name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the description
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the description
     */
    public String getType() {
        return this.type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the description
     */
    public String getSubType() {
        return this.subType;
    }

    /**
     * Gets the regular expression of the extension target component name.
     *
     * @return the extension target component name in regular expression
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the description
     */
    public String getFile() {
        return this.file;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the extension namespaces
     */
    public String getNameSpace() {
        return this.ns;
    }


    /**
     * DOCUMENT ME!
     *
     * @return the provider
     */
    public String getProvider() {
        return this.provider;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the extension elements
     */
    public List<JbiExtensionElement> getElements() {
        return this.elements;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JbiExtensionInfo:")
                .append(" name=").append(getName())
                .append(" displayName=").append(getDisplayName())
                .append(" type=").append(getType())
                .append(" subType=").append(getSubType())
                .append(" file=").append(getFile())
                .append(" ns=").append(getNameSpace())
                .append(" target=").append(getTarget())
                .append(" icon=").append(getIcon())
                .append(" provider=").append(getProvider())
                .append(" description=").append(getDescription());
        
        for (JbiExtensionElement element : getElements()) {
            sb.append(System.getProperty("line.separator")); // NOI18N
            sb.append(element.toString());
        }
        
        sb.append(System.getProperty("line.separator")); // NOI18N
        
        return sb.toString();
    }
}
