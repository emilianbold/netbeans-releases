/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap;

import org.netbeans.modules.soa.ldap.properties.ConnectionProperties;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author anjeleevich
 */
public class LDAPConnection {
    private ConnectionProperties properties;
    private String fileName;


    LDAPConnection(String fileName, ConnectionProperties properties) {
        this.fileName = fileName;
        this.properties = properties;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDisplayName() {
        return properties.getConnectionName();
    }

    public ConnectionProperties getProperties() {
        return new ConnectionProperties(properties);
    }

    void setConnectionProperties(ConnectionProperties connectionProperties) {
        this.properties
                = new ConnectionProperties(connectionProperties);
    }

    public String getURL() {
        StringBuilder builder = new StringBuilder();
        if (properties.isUseSSL()) {
            builder.append("ldaps://");
        } else {
            builder.append("ldap://");
        }

        builder.append(properties.getHost());

        if (properties.getPort() != 0) {
            builder.append(":");
            builder.append(properties.getPort());
        }

        String baseDN = properties.getBaseDN();
        if (baseDN != null) {
            baseDN = baseDN.trim();
            if (baseDN.length() > 0) {
                builder.append("/");
                builder.append(baseDN);
            } 
        } 

        return builder.toString();
    }

    private static String getText(Element root, String localName) {
        NodeList nodeList = (root == null) ? null
                : root.getElementsByTagName(localName);

        return ((nodeList != null) && (nodeList.getLength() > 0))
                ? nodeList.item(0).getTextContent()
                : null;
    }

    void setProperties(ConnectionProperties newConnectionProperties) {
        this.properties = new ConnectionProperties(newConnectionProperties);
    }

    @Override
    public String toString() {
        return properties.getConnectionName();
    }
}
