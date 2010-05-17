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
import com.sun.javacard.Portability;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;

public final class DeploymentXmlInstanceEntry implements FileModelEntry, Cloneable {

    private AID instanceAID;
    private String deploymentParams = "";
    private int order;

    public DeploymentXmlInstanceEntry(AID instanceAID, String deploymentParams, int order) {
        setInstanceAID(instanceAID);
        setDeploymentParams(deploymentParams);
        this.order = order;
    }

    public DeploymentXmlInstanceEntry() {
    }

    public DeploymentXmlInstanceEntry(Element instanceElement, int instanceOrder, ParseErrorHandler handler) throws IOException {
        if (handler == null) handler = ParseErrorHandler.DEFAULT;
        this.order = instanceOrder;
        NodeList kids = instanceElement.getChildNodes();
        int len = kids.getLength();
        for (int i=0; i < len; i++) {
            Node nd = kids.item(i);
            if (nd.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nd;
                if ("instance-AID".equals(el.getNodeName())) { //NOI18N
                    String aidString = el.getTextContent();
                    try {
                        AID aid = AID.parse (aidString);
                        setInstanceAID(aid);
                    } catch (IllegalArgumentException e) {
                        handler.handleBadAIDError(e, aidString);
                    }
                } else if ("deployment-params".equals(el.getNodeName())) { //NOI18N
                    setDeploymentParams(el.getTextContent());
                }
            }
        }
    }

    @Override
    public Object clone() {
        DeploymentXmlInstanceEntry nue = new DeploymentXmlInstanceEntry();
        nue.instanceAID = instanceAID;
        nue.order = order;
        nue.deploymentParams = deploymentParams;
        return nue;
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder("        <instance>\n"); //NOI18N
        sb.append ("            <instance-AID>"); //NOI18N
        sb.append (getInstanceAID()); //NOI18N
        sb.append ("</instance-AID>\n"); //NOI18N
        sb.append ("            <deployment-params><![CDATA["); //NOI18N
        sb.append (getDeploymentParams()); //NOI18N
        sb.append ("]]></deployment-params>\n"); //NOI18N
        sb.append ("        </instance>\n"); //NOI18N
        return sb.toString();
    }

    public boolean isValid() {
        return getInstanceAID() != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeploymentXmlInstanceEntry other = (DeploymentXmlInstanceEntry) obj;
        if (this.getInstanceAID() != other.getInstanceAID() &&
                (this.getInstanceAID() == null ||
                !this.getInstanceAID().equals(other.getInstanceAID()))) {
            return false;
        }
        if ((this.getDeploymentParams() == null) ? (other.getDeploymentParams() != null) :
                !this.getDeploymentParams().equals(other.getDeploymentParams())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.getInstanceAID() != null ? this.getInstanceAID().hashCode() : 0);
        hash = 47 * hash + (this.getDeploymentParams() != null ? this.getDeploymentParams().hashCode() : 0);
        return hash;
    }

    public AID getInstanceAID() {
        return instanceAID;
    }

    public void setInstanceAID(AID instanceAID) {
        this.instanceAID = instanceAID;
    }

    public String getDeploymentParams() {
        return deploymentParams;
    }

    public void setDeploymentParams(String deploymentParams) {
        this.deploymentParams = deploymentParams;
    }

    public int getOrder() {
        return order;
    }

    public int compareTo(FileModelEntry o) {
        assert o == null || o.getClass() == getClass();
        return getOrder() - o.getOrder();
    }

    public String getProblem() {
        if (getInstanceAID() == null) {
            return Portability.getString("MSG_NO_INSTANCE_AID"); //NOI18N
        }
        return null;
    }
}
