/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uihandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.uihandler.Installer.Button;
import org.openide.awt.Mnemonics;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Jindrich Sedek
 */
final class ButtonsParser {

    private final InputStream is;
    private String title;
    private List<Object> options;
    private List<Object> additionalOptions;
    private boolean containsExitButton = false;
    private List<Node> nodes;
    private String url;

    public ButtonsParser(InputStream is) {
        this.is = is;
    }

    void parse() throws IOException, ParserConfigurationException, SAXException, InterruptedException, InvocationTargetException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        PushbackInputStream isWithProlog = new PushbackInputStream(is, 255);
        byte[] xmlHeader = new byte[5];
        int len = isWithProlog.read(xmlHeader);
        isWithProlog.unread(xmlHeader, 0, len);

        if (len < 5 || xmlHeader[0] != '<' ||
                xmlHeader[1] != '?' ||
                xmlHeader[2] != 'x' ||
                xmlHeader[3] != 'm' ||
                xmlHeader[4] != 'l') {
            String header = "<?xml version='1.0' encoding='utf-8'?>";
            isWithProlog.unread(header.getBytes("utf-8"));
        }

        nodes = new ArrayList<Node>();
        Document doc = builder.parse(isWithProlog);
        NodeList forms = doc.getElementsByTagName("form");
        for (int i = 0; i < forms.getLength(); i++) {
            String action = forms.item(i).getAttributes().getNamedItem("action").getNodeValue();
            if ((action == null) || ("".equals(action))) {// logging for issue #145167
                Logger logger = Logger.getLogger("org.netbeans.ui.logger.Installer");
                LogRecord rec = new LogRecord(Level.WARNING, "invalid action from doc:");
                String[] params = new String[]{forms.item(i).toString(), doc.getTextContent(), doc.getDocumentURI()};
                rec.setParameters(params);
                logger.log(rec);
            }
            url = action;
            NodeList inputs = doc.getElementsByTagName("input");
            for (int j = 0; j < inputs.getLength(); j++) {
                if (isChild(inputs.item(j), forms.item(i))) {
                    org.w3c.dom.Node in = inputs.item(j);
                    String type = attrValue(in, "type");
                    if ("hidden".equals(type)) { // NOI18N
                        nodes.add(in);
                    }
                }
            }
        }

        NodeList titlesList = doc.getElementsByTagName("title");
        for (int i = 0; i < titlesList.getLength(); i++) {
            String t = titlesList.item(i).getTextContent();
            if (t != null) {
                title = t;
                break;
            }
        }
    }

    public void createButtons() {
        options = new ArrayList<Object>();
        additionalOptions = new ArrayList<Object>();
        for (Node node : nodes) {
            String name = attrValue(node, "name");
            String value = attrValue(node, "value");
            String align = attrValue(node, "align");
            String alt = attrValue(node, "alt");
            boolean enabled = !"true".equals(attrValue(node, "disabled")); // NOI18N

            List<Object> addTo = "left".equals(align) ? additionalOptions : options;

            if (Button.isSubmitTrigger(name)) { // NOI18N
                String submitValue = value;
                JButton b = new JButton();
                Mnemonics.setLocalizedText(b, submitValue);
                b.setActionCommand(name); // NOI18N
                b.putClientProperty("url", url); // NOI18N
                b.setDefaultCapable(addTo.isEmpty() && addTo == options);
                b.putClientProperty("alt", alt); // NOI18N
                b.putClientProperty("now", submitValue); // NOI18N
                b.setEnabled(enabled);
                addTo.add(b);
            } else {
                JButton b = new JButton();
                Mnemonics.setLocalizedText(b, value);
                b.setActionCommand(name);
                b.setDefaultCapable(addTo.isEmpty() && addTo == options);
                b.putClientProperty("alt", alt); // NOI18N
                b.putClientProperty("now", value); // NOI18N
                b.setEnabled(enabled && Button.isKnown(name));
                addTo.add(b);
                if (Button.EXIT.isCommand(name)) {
                    containsExitButton = true;
                }
                if (Button.REDIRECT.isCommand(name)) {
                    b.putClientProperty("url", url); // NOI18N
                }
            }
        }
    }

    List<Object> getOptions() {
        return options;
    }

    List<Object> getAditionalOptions() {
        return additionalOptions;
    }

    String getTitle() {
        return title;
    }

    boolean containsExitButton() {
        return containsExitButton;
    }

    private static String attrValue(org.w3c.dom.Node in, String attrName) {
        org.w3c.dom.Node n = in.getAttributes().getNamedItem(attrName);
        return n == null ? null : n.getNodeValue();
    }

    private static boolean isChild(org.w3c.dom.Node child, org.w3c.dom.Node parent) {
        while (child != null) {
            if (child == parent) {
                return true;
            }
            child = child.getParentNode();
        }
        return false;
    }

}
