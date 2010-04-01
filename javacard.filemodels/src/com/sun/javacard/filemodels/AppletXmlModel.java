/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Boudreau
 */
public final class AppletXmlModel extends FileModel<AppletXmlAppletEntry> {
    private final Logger LOG = Logger.getLogger(AppletXmlModel.class.getName());
    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<applet-app xmlns=\"http://java.sun.com/xml/ns/javacard\"\n" +
            "           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "           xsi:schemaLocation=\"http://java.sun.com/xml/ns/javacard http://java.sun.com/xml/ns/javacard/applet-app_3_0.xsd\"\n" +
            "           version=\"3.0\">\n";
    private static final String[] KNOWN_TAGS = new String[]{
        "display-name", //NOI18N
        "applet", //NOI18N
        "applet-class", //NOI18N
        "applet-AID", //NOI18N
        "applet-app", //NOI18N
    };

    //for unit tests
    public AppletXmlModel(InputStream in, ParseErrorHandler handler) throws IOException {
        if (in == null) throw new NullPointerException("in");
        parse(in, handler);
        close();
    }

    public AppletXmlModel (InputStream in) throws IOException {
        this (in, null);
    }

    //for unit tests
    public AppletXmlModel() {
    }

    public Iterable<AID> allAIDs() {
        List<AID> result = new ArrayList<AID>();
        for (AppletXmlAppletEntry info : getData()) {
            if (info.aid != null) {
                result.add(info.aid);
            }
        }
        return result;
    }

    public boolean containsClass(String fqn) {
        if (fqn == null) {
            return false;
        }
        for (AppletXmlAppletEntry info : getData()) {
            if (fqn.equals(info.clazz)) {
                return true;
            }
        }
        return false;
    }

    private AppletXmlAppletEntry getByName(String name) {
        for (AppletXmlAppletEntry info : getData()) {
            if (name.equals(info.displayName)) {
                return info;
            }
        }
        return null;
    }

    private AppletXmlAppletEntry getByAID(AID aid) {
        for (AppletXmlAppletEntry info : getData()) {
            if (aid.equals(info.aid)) {
                return info;
            }
        }
        return null;
    }


    private void parse(InputStream in, ParseErrorHandler handler) throws IOException {
        try {
            handler = handler == null ? ParseErrorHandler.DEFAULT : handler;
            handler = new WrapperParseErrorHandler(handler, this);
            org.w3c.dom.Document doc = null;
            doc = Portability.parse(in);
            org.w3c.dom.Node parent;
            org.w3c.dom.Node child;

            NodeList applets = doc.getElementsByTagName("applet"); //NOI18N
            LOG.log(Level.FINEST, "Parse document {0}", doc); //NOI18N
            int len = applets.getLength();
            int ix = 0;
            for (int i = 0; i < len; i++) {
                String displayName = null;
                String clazz = null;
                String aidString = null;
                String description = null;
                parent = applets.item(i);
                NodeList kids = parent.getChildNodes();
                int kl = kids.getLength();
                for (int j = 0; j < kl; j++) {
                    child = kids.item(j);
                    if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        String name = child.getNodeName();
                        LOG.log(Level.FINER, "Enter element node {0}", name);
                        if ("display-name".equals(name)) { //NOI18N
                            displayName = child.getTextContent();
                        } else if ("applet-class".equals(name)) { //NOI18N
                            clazz = child.getTextContent();
                        } else if ("applet-AID".equals(name)) { //NOI18N
                            aidString = child.getTextContent();
                        } else if ("description".equals(name)) {
                            description = child.getTextContent();
                            //XXX handle
                        } else {
                            LOG.log(Level.FINER, "Unknown element {0}", name); //NOI18N
                            handler.unrecognizedElementEncountered(name);
                        }
                    }
                }
                LOG.log (Level.FINE, "Element: {0} {1} {2}", new Object[] { displayName, clazz, aidString }); //NOI18N
                if (displayName != null && clazz != null) {
                    AppletXmlAppletEntry info = getByName(displayName);
                    AID aid = null;
                    Exception ex = null;
                    try {
                        aid = aidString == null ? null : AID.parse(aidString);
                    } catch (IllegalArgumentException e) {
                        handler.handleBadAIDError(e, aidString);
                        ex = e;
                    }
                    if (aid == null) {
                        int lastDot = clazz.lastIndexOf('.');
                        String pkg = clazz.substring(0, lastDot);
                        String cl;
                        if (lastDot != pkg.length() - 1) {
                            cl = clazz.substring(lastDot + 1, clazz.length());
                        } else {
                            cl = clazz;
                        }
                        LOG.log(Level.INFO,
                                "Invalid AID in applet.xml " + aidString + " - " + //NOI18N
                                "generating random one " + aid, ex == null ? new Exception() : ex); //NOI18N
                    }
                    if (info == null) {
                        info = new AppletXmlAppletEntry(displayName, clazz, aid, ix);
                        ix++;
                        add(info);
                    }
                }
            }
        } catch (IOException ex) {
            handler.handleError(ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Portability.logException(ex);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        boolean result = o != this && o != null && o.getClass() == AppletXmlModel.class;
        if (result) {
            AppletXmlModel other = (AppletXmlModel) o;
            List<? extends AppletXmlAppletEntry> odata = other.getData();
            List<? extends AppletXmlAppletEntry> myData = getData();
            result = myData.equals(odata);
        }
        return result;
    }

    public boolean deploymentDataEquals(AppletXmlModel mdl) {
        if (!equals(mdl)) {
            return false;
        }
        for (AppletXmlAppletEntry info : getData()) {
            AID aid = info.aid;
            if (aid == null) {
                continue;
            }
            AppletXmlAppletEntry other = mdl.getByAID(aid);
            if (other == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        List<? extends AppletXmlAppletEntry> myData = getData();
        hash = 23 * hash + myData.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return toXml();
    }

    @Override
    public String toXml() {
        StringBuilder sb = new StringBuilder(HEADER);
        List<? extends AppletXmlAppletEntry> dataCopy = getData();
        for (AppletXmlAppletEntry i : dataCopy) {
            sb.append(i.toXml());
        }
        sb.append("</applet-app>\n"); //NOI18N
        return sb.toString();
    }
}
