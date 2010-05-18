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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Model for our proprietary deployment.xml, which the IDE edits and the instantiation ant task
 * consumes. 
 *
 * @author Tim Boudreau
 */
public class DeploymentXmlModel extends FileModel<DeploymentXmlAppletEntry> {
    private static final String DEPLOYMENT_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    public DeploymentXmlModel() {

    }

    public DeploymentXmlModel (InputStream in) throws IOException {
        this (in, null);
    }

    public DeploymentXmlModel (InputStream in, ParseErrorHandler handler) throws IOException {
        handler = handler == null ? ParseErrorHandler.DEFAULT : handler;
        handler = new WrapperParseErrorHandler(handler, this);
        try {
            handler = new WrapperParseErrorHandler(handler, this);
            try {
                org.w3c.dom.Document doc = null;
                doc = Portability.parse(in);
                NodeList root = doc.getElementsByTagName("deploy"); //NOI18N
                if (root == null) {
                    handler.handleError(new IOException(
                            "No root element <deploy>")); //NOI18N
                } else if (root.getLength() > 1 || root.getLength() == 0) {
                    handler.handleError(new IOException(
                            "Missing or multiple <deploy> elements: " + root.getLength())); //NOI18N
                }
                NodeList applets = doc.getElementsByTagName("applet"); //NOI18N
                int len = applets.getLength();
                int ix = 0;
                for (int i = 0; i < len; i++) {
                    Node nd = applets.item (i);
                    if (nd.getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) nd;
                        try {
                            add(new DeploymentXmlAppletEntry(el, ix, handler));
                            ix++;
                        } catch (IOException ioe) {
                            handler.handleError (ioe);
                        }
                    }
                }
            } catch (IOException x) {
                handler.handleError (x);
            }
        } finally {
            close();
        }
    }

    @Override
    protected String getProblemInternal() {
        List<AID> allInstanceAids = new ArrayList<AID>();
        for (DeploymentXmlAppletEntry e : getData()) {
            for (DeploymentXmlInstanceEntry i : e.getData()) {
                AID aid = i.getInstanceAID();
                if (aid != null) {
                    allInstanceAids.add(aid);
                }
            }
        }
        HashSet<AID> hs;
        if ((hs = new HashSet<AID>(allInstanceAids)).size() < allInstanceAids.size()) {
            return Portability.getString ("DUPLICATE_AIDS"); //NOI18N
        }
        if (isEmpty()) {
            return Portability.getString("EMPTY_DEPLOYMENT_MODEL");
        }
        return null;
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder(DEPLOYMENT_HEADER);
        sb.append (Portability.getString("WARNING_COMMENT"));
        sb.append ("<deploy>\n");
        for (DeploymentXmlAppletEntry a : getData()) {
            sb.append (a.toXml());
            sb.append ('\n');
        }
        sb.append ("</deploy>\n");
        return sb.toString();
    }

    //equals() && hashCode() handled by superclass correctly
}
