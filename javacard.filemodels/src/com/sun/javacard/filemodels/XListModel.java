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
package com.sun.javacard.filemodels;

import com.sun.javacard.Portability;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Models the return value of a call to http://[cardmanagerurl]/xlist
 *
 * @author Tim Boudreau
 */
public class XListModel extends FileModel<XListEntry> {

    public XListModel(InputStream in, ParseErrorHandler handler) throws IOException {
        if (in == null) {
            throw new NullPointerException("Null input stream"); //NOI18N
        }
        handler = handler == null ? ParseErrorHandler.DEFAULT : handler;
        handler = new WrapperParseErrorHandler(handler, this);
        parse(in, handler);
        close();
    }

    public XListModel() {
        
    }

    @Override
    public String toXml() {
        StringBuilder sb = new StringBuilder("<list>\n"); //NOI18N
        for (XListEntry e : getData()) {
            sb.append("    <bundle>\n"); //NOI18N
            sb.append(e.toXml());
            sb.append("    </bundle>\n"); //NOI18N
        }
        sb.append("</list>\n"); //NOI18N
        return sb.toString();
    }

    private void parse(InputStream in, ParseErrorHandler handler) throws IOException {
        assert !EventQueue.isDispatchThread();
        try {
            org.w3c.dom.Document doc = null;
            doc = Portability.parse(in);
            org.w3c.dom.Node parent;
            NodeList bundles = doc.getElementsByTagName("bundle"); //NOI18N
            int len = bundles.getLength();
            int ix = 0;
            for (int i = 0; i < len; i++) {
                if (Thread.interrupted()) {
                    return;
                }
                parent = bundles.item(i);
                NodeList kids = parent.getChildNodes();
                int kidsLen = kids.getLength();
                XListEntry entry = new XListEntry();
                for (int j = 0; j < kidsLen; j++) {
                    Node bundleItem = kids.item(j);
                    if (Thread.interrupted()) {
                        return;
                    }
                    if (bundleItem.getNodeType() == Node.ELEMENT_NODE) {
                        switch (TopLevelTags.valueOf(bundleItem.getNodeName())) {
                            case name:
                                String name = bundleItem.getTextContent();
                                entry.setDisplayName(name);
                                break;
                            case type:
                                String type = bundleItem.getTextContent();
                                entry.setType(type);
                                break;
                            case instances:
                                NodeList insKids = bundleItem.getChildNodes();
                                int iLen = insKids.getLength();
                                for (int k = 0; k < iLen; k++) {
                                    if (Thread.interrupted()) {
                                        return;
                                    }
                                    Node ins = insKids.item(k);
                                    if (ins.getNodeType() == Node.ELEMENT_NODE) {
                                        if ("instance".equals(ins.getNodeName())) { //NOI18N
                                            entry.addInstance(new XListInstanceEntry(ins.getTextContent()));
                                        }
                                    }
                                }
                                break;
                            default:
                                error();
                                Logger.getLogger(XListModel.class.getName()).log(Level.INFO,
                                        "Unknown element " + bundleItem.getNodeName() //NOI18N
                                        + " from xlist"); //NOI18N
                                handler.unrecognizedElementEncountered(bundleItem.getNodeName());
                                break;
                        }
                    }
                }
                if (entry.isValid()) {
                    entry.setOrder(ix++);
                    add(entry);
                }
            }
        } catch (IOException ioe) {
            error();
            throw ioe;
        }
    }

    private static enum TopLevelTags {
        name,
        type,
        instances,
    }
}
