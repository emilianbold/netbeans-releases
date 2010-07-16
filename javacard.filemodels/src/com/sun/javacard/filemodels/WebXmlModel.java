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

import com.sun.javacard.Portability;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author Tim Boudreau
 */
public final class WebXmlModel extends FileModel<WebXmlServletEntry>{
    private String defaultServlet;
    private String defaultMapping;

    //for unit tests
    public WebXmlModel(InputStream in, ParseErrorHandler handler) throws IOException {
        if (in == null) throw new NullPointerException ("Null input stream"); //NOI18N
        handler = handler == null ? ParseErrorHandler.DEFAULT : handler;
        handler = new WrapperParseErrorHandler(handler, this);
        parse (in, handler);
        close();
    }

    //for unit tests
    public WebXmlModel() {
        
    }

    private WebXmlServletEntry getByName (String name) {
        for (WebXmlServletEntry info : getData()) {
            if (name.equals(info.getName())) {
                return info;
            }
        }
        return null;
    }

    static final String[] KNOWN_TAGS = new String[] {
        "display-name", //NOI18N
        "servlet", //NOI18N
        "servlet-name", //NOI18N
        "servlet-class", //NOI18N
        "servlet-mapping", //NOI18N
        "url-pattern", //NOI18N
        "web-app", //NOI18N
    };

    private void parse(InputStream in, ParseErrorHandler handler) throws IOException {
        assert !EventQueue.isDispatchThread();
        try {
            org.w3c.dom.Document doc = null;
            doc = Portability.parse(in);
            org.w3c.dom.Node parent;
            org.w3c.dom.Node child;
            NodeList names = doc.getElementsByTagName("display-name"); //NOI18N
            if (names.getLength() > 0) {
                parent = names.item(0);
                setDisplayName(parent.getTextContent());
            }
            NodeList servlets = doc.getElementsByTagName("servlet"); //NOI18N
            int len = servlets.getLength();
            int ix = 0;
            for (int i = 0; i < len; i++) {
                String name = null;
                String clazz = null;
                parent = servlets.item(i);
                NodeList kids = parent.getChildNodes();
                int kl = kids.getLength();
                for (int j = 0; j < kl; j++) {
                    child = kids.item(j);
                    if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        if ("servlet-name".equals(child.getNodeName())) { //NOI18N
                            name = child.getTextContent();
                        } else if ("servlet-class".equals(child.getNodeName())) { //NOI18N
                            clazz = child.getTextContent();
                        } else {
                            handler.unrecognizedElementEncountered(child.getNodeName());
                        }
                    }
                    if (name != null && clazz != null) {
                        WebXmlServletEntry info = getByName(name);
                        if (info == null) {
                            info = new WebXmlServletEntry(name, clazz, null, ix);
                            ix++;
                            add (info);
                        }
                    }
                }
            }
            NodeList mappings = doc.getElementsByTagName("servlet-mapping"); //NOI18N
            len = mappings.getLength();
            for (int i = 0; i < len; i++) {
                String name = null;
                String mapping = null;
                parent = mappings.item(i);
                NodeList kids = parent.getChildNodes();
                int kl = kids.getLength();
                for (int j = 0; j < kl; j++) {
                    child = kids.item(j);
                    if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        if ("servlet-name".equals(child.getNodeName())) { //NOI18N
                            name = child.getTextContent();
                        } else if ("url-pattern".equals(child.getNodeName())) { //NOI18N
                            mapping = child.getTextContent();
                        } else {
                            handler.unrecognizedElementEncountered(child.getNodeName());
                        }
                    }
                    if (mapping != null && name != null) {
                        WebXmlServletEntry info = getByName(name);
                        if (info != null) {
                            info.setMapping(mapping);
                        }
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

    public synchronized String defaultServlet() {
        return defaultServlet;
    }

    public synchronized String defaultMapping() {
        return defaultMapping;
    }

    public synchronized void setDefaultServlet (String defaultServlet) {
        this.defaultServlet = defaultServlet;
    }

    public synchronized void setDefaultMapping (String defaultMapping) {
        this.defaultMapping = defaultMapping;
    }

    private String displayName;

    public synchronized void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public synchronized String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = o != null && o.getClass() == WebXmlModel.class;
        if (result) {
            WebXmlModel other = (WebXmlModel) o;
            String odn = other.getDisplayName();
            String dn = getDisplayName();
            result = !((dn == null) ? (odn != null) : !dn.equals(odn));
            if (result) {
                List<? extends WebXmlServletEntry> odata = other.getData();
                List<? extends WebXmlServletEntry> myData = getData();
                result = myData.equals(odata);
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        List<? extends WebXmlServletEntry> myData = getData();
        String dn = getDisplayName();
        hash = 23 * hash + myData.hashCode();
        hash = 23 * hash + (dn != null ? dn.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return toXml();
    }

    private static final String HEADER =
            "<web-app xmlns=\"http://java.sun.com/xml/ns/j2ee\"\n" + //NOI18N
            "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //NOI18N
            "         version=\"2.4\"\n" + //NOI18N
            "         xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee " +
            "http://java.sun.com/xml/ns/jcns/jcweb-app_3_0.xsd\">\n"; //NOI18N

    public String toXml() {
        StringBuilder sb = new StringBuilder(HEADER);
        sb.append ("    <display-name>");
        sb.append (getDisplayName());
        sb.append ("</display-name>\n");
        List<? extends WebXmlServletEntry> dataCopy = getData();
        for (WebXmlServletEntry i : dataCopy) {
            sb.append (i.toXml());
        }
        sb.append ("</web-app>");
        return sb.toString();
    }

    @Override
    protected String getProblemInternal() {
        if (displayName == null || displayName.trim().length() == 0) {
            return Portability.getString("PROBLEM_DISPLAY_NAME_NOT_SET"); //NOI18N
        }
        for (WebXmlServletEntry info : getData()) {
            String problem = info.getProblem();
            if (problem != null) {
                return problem;
            }
        }
        return null;
    }
}
