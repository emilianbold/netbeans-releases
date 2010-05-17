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
package com.sun.javacard.filemodels;

import com.sun.javacard.AID;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;

public final class DeploymentXmlAppletEntry extends FileModel<DeploymentXmlInstanceEntry> implements FileModelEntry, Cloneable {

    private int order;
    private String clazzHint;
    private AID appletAid;
    private String displayNameHint;

    public DeploymentXmlAppletEntry() {
    }

    public DeploymentXmlAppletEntry(Element appletElement, int order, ParseErrorHandler handler) throws IOException {
        if (handler == null) {
            handler = ParseErrorHandler.DEFAULT;
        }
        handler = new WrapperParseErrorHandler(handler, this);
        this.order = order;
        NodeList kids = appletElement.getChildNodes();
        int len = kids.getLength();
        int instanceOrder = 0;
        for (int i = 0; i < len; i++) {
            Node nd = kids.item(i);
            if (nd.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) nd;
                if ("applet-AID".equals(child.getNodeName())) { //NOI18N
                    String aidString = child.getTextContent();
                    try {
                        AID aid = AID.parse(aidString);
                        setAppletAid(aid);
                    } catch (IllegalArgumentException e) {
                        handler.handleBadAIDError(e, aidString);
                    }
                } else if ("class-name-hint".equals(child.getNodeName())) { //NOI18N
                    setClazzHint(child.getTextContent());
                } else if ("display-name-hint".equals(child.getNodeName())) { //NOI18N
                    setDisplayNameHint(child.getTextContent());
                } else if ("instance".equals(child.getNodeName())) { //NOI18N
                    add(new DeploymentXmlInstanceEntry(child, instanceOrder, handler));
                    instanceOrder++;
                } else {
                    handler.unrecognizedElementEncountered(child.getNodeName());
                }
            }
        }
    }

    public void remove (DeploymentXmlInstanceEntry e) {
        super.remove(e);
    }

    @Override
    public Object clone() {
        DeploymentXmlAppletEntry nue = new DeploymentXmlAppletEntry();
        nue.order = order;
        nue.clazzHint = clazzHint;
        nue.appletAid = appletAid;
        nue.displayNameHint = displayNameHint;
        for (DeploymentXmlInstanceEntry e : getData()) {
            nue.add ((DeploymentXmlInstanceEntry)e.clone());
        }
        return nue;
    }

    public String getClazzHint() {
        return clazzHint;
    }

    public void setClazzHint(String clazzHint) {
        this.clazzHint = clazzHint;
    }

    public AID getAppletAid() {
        return appletAid;
    }

    public void setAppletAid(AID appletAid) {
        this.appletAid = appletAid;
    }

    public String getDisplayNameHint() {
        return displayNameHint;
    }

    public void setDisplayNameHint(String displayNameHint) {
        this.displayNameHint = displayNameHint;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toXml() {
        StringBuilder sb = new StringBuilder("    <applet>\n"); //NOI18N
        sb.append("        <applet-AID>"); //NOI18N
        sb.append(getAppletAid());
        sb.append("</applet-AID>\n"); //NOI18N
        sb.append("        <class-name-hint>"); //NOI18N
        sb.append(getClazzHint());
        sb.append("</class-name-hint>\n"); //NOI18N
        sb.append("        <display-name-hint>"); //NOI18N
        sb.append(getDisplayNameHint());
        sb.append("</display-name-hint>\n"); //NOI18N
        for (DeploymentXmlInstanceEntry e : getData()) {
            sb.append(e.toXml());
        }
        sb.append("    </applet>\n"); //NOI18N
        return sb.toString();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        if (obj == this) return true;
        boolean result = super.equals(obj);
        if (result) {
            final DeploymentXmlAppletEntry other = (DeploymentXmlAppletEntry) obj;
            result = this.order == other.order;
            if (result) {
                if (this.appletAid == null) {
                    result = other.appletAid == null;
                } else {
                    result = this.appletAid == other.appletAid || this.appletAid.equals(other.appletAid);
                }
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.order;
        hash = 53 * hash + (this.appletAid != null ? this.appletAid.hashCode() : 0);
        return hash;
    }

    public int compareTo(FileModelEntry o) {
        assert o == null || getClass() == o.getClass();
        return o == null ? 0 : getOrder() - o.getOrder();
    }
}
