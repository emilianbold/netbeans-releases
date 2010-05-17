/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.registry.jaxrpc;


public class WsdlType {
    private java.net.URL location;
    private java.lang.String packageName;

    /**
     * Normal starting point constructor.
     */
    public WsdlType() {
        packageName = "";
    }

    /**
     * Required parameters constructor
     */
    public WsdlType(java.net.URL location, java.lang.String packageName) {
        location = location;
        packageName = packageName;
    }


    public void setLocation(java.net.URL value) {
        location = value;
    }
    
    public java.net.URL getLocation() {
        return location;
    }
    
    public void setPackageName(java.lang.String value) {
        packageName = value;
    }
    
    public java.lang.String getPackageName() {
        return packageName;
    }
    
    
    
    public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
        out.write(indent);
        out.write("<");
        out.write(nodeName);
        // location is an attribute
        if (location != null) {
            out.write(" location");	// NOI18N
            out.write("='");	// NOI18N
            Configuration.writeXML(out, location.toString(), true);
            out.write("'");	// NOI18N
        }
        // packageName is an attribute
        if (packageName != null && packageName.length() > 0) {
            out.write(" packageName");	// NOI18N
            out.write("='");	// NOI18N
            Configuration.writeXML(out, packageName, true);
            out.write("'");	// NOI18N
        }
        out.write(">\n");
        String nextIndent = indent + "	";
        out.write(indent);
        out.write("</"+nodeName+">\n");
    }
    
    public void readNode(org.w3c.dom.Node node) {
        if (node.hasAttributes()) {
            org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
            org.w3c.dom.Attr attr;
            java.lang.String attrValue;
            attr = (org.w3c.dom.Attr) attrs.getNamedItem("location");
            try {
                if (attr != null) {
                    attrValue = attr.getValue();
                } else {
                    attrValue = null;
                }
                location = new java.net.URL(attrValue);
            }
            catch (java.net.MalformedURLException e) {
                throw new java.lang.RuntimeException(e);
            }
            attr = (org.w3c.dom.Attr) attrs.getNamedItem("packageName");
            if (attr != null) {
                attrValue = attr.getValue();
            } else {
                attrValue = null;
            }
            packageName = attrValue;
        }
        org.w3c.dom.NodeList children = node.getChildNodes();
        for (int i = 0, size = children.getLength(); i < size; ++i) {
            org.w3c.dom.Node childNode = children.item(i);
            String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
            String childNodeValue = "";
            if (childNode.getFirstChild() != null) {
                childNodeValue = childNode.getFirstChild().getNodeValue();
            }
            else {
                // Found extra unrecognized childNode
            }
        }
    }
}


