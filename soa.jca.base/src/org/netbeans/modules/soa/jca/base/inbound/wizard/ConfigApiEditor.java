/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.base.inbound.wizard;

import org.netbeans.modules.soa.jca.base.ScEncrypt;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * in-place editor for config-api
 *
 * @author echou
 */
public class ConfigApiEditor extends java.beans.PropertyEditorSupport {

    private ActivationStruct activationStruct;
    private Document configDoc;
    private PropertySheet ps;
    private boolean editable;

    public ConfigApiEditor(ActivationStruct activationStruct, boolean editable) throws Exception {
        this.activationStruct = activationStruct;
        this.editable = editable;
        this.ps = new PropertySheet();
        ConfigApiNode configApiNode = new ConfigApiNode();
        StringReader reader = new StringReader(activationStruct.getPropertyValue());
        this.configDoc = XMLUtil.parse(
                    new InputSource(reader),
                    false, false, null, null);
        Element configurationRoot = configDoc.getDocumentElement();
        NodeList instanceList = configurationRoot.getElementsByTagName("instance"); // NOI18N
        if (instanceList.getLength() > 0) {
            Element instanceElement = (Element) instanceList.item(0);
            NodeList cfgList = instanceElement.getElementsByTagName("cfg"); // NOI18N
            if (cfgList.getLength() > 0) {
                Element cfgElement = (Element) cfgList.item(0);
                NodeList configurationList = cfgElement.getElementsByTagName("configuration"); // NOI18N
                if (configurationList.getLength() > 0) {
                    Element configurationElement = (Element) configurationList.item(0);
                    NodeList sectionList = configurationElement.getElementsByTagName("section"); // NOI18N
                    for (int i = 0; i < sectionList.getLength(); i++) {
                        Element sectionElement = (Element) sectionList.item(i);
                        NodeList parameterList = sectionElement.getElementsByTagName("parameter"); // NOI18N
                        for (int n = 0; n < parameterList.getLength(); n++) {
                            Element parameterElement = (Element) parameterList.item(n);
                            configApiNode.addProperty(parameterElement);
                        }
                    }
                }
            }
        }
        configApiNode.done();
        ps.setNodes(new Node[] { configApiNode });
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public java.awt.Component getCustomEditor() {
        return ps;
    }

    private void writeValueToXml() {
        try {
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            //Setup indenting to "pretty print"
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // NOI18N
            //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(new DOMSource(configDoc), new StreamResult(writer));

            activationStruct.setPropertyValue(writer.toString());
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            DialogDisplayer.getDefault().notifyLater(d);
        }
    }

    class ConfigApiNode extends AbstractNode {

        private Sheet.Set pset;

        public ConfigApiNode() {
            super(Children.LEAF);
            setName("configapinode"); // NOI18N
            this.pset = Sheet.createPropertiesSet();
        }

        public void addProperty(final Element parameterElement) throws Exception {
            String name = parameterElement.getAttribute("name"); // NOI18N
            String displayName = parameterElement.getAttribute("displayName"); // NOI18N
            if (displayName == null || displayName.length() == 0) {
                displayName = name;
            }

            Element valueElement = null;
            NodeList paramChildrenList = parameterElement.getChildNodes();
            for (int i = 0; i < paramChildrenList.getLength(); i++) {
                org.w3c.dom.Node curChild = paramChildrenList.item(i);
                if (curChild instanceof Element) {
                    Element curChildElem = (Element) curChild;
                    if (curChildElem.getTagName().equals("value")) { // NOI18N
                        valueElement = curChildElem;
                        break;
                    }
                    if (curChildElem.getTagName().equals("default")) { // NOI18N
                        valueElement = (Element) curChildElem.getElementsByTagName("value").item(0); // NOI18N
                        break;
                    }
                }
            }

            //Element valueElement = (Element) parameterElement.getElementsByTagName("value").item(0); // NOI18N
            if (valueElement == null) {
                valueElement = configDoc.createElement("value"); // NOI18N
                parameterElement.appendChild(valueElement);
            }
            final Element valueElementRef = valueElement;

            boolean encrypted = Boolean.parseBoolean(parameterElement.getAttribute("isEncrypted"));
            if (encrypted) {
                String key = parameterElement.getAttribute("key"); // NOI18N
                final String decryptedKey = ScEncrypt.decrypt("as8%232.nJXIN384103 bp5md-+z2123", key); // NOI18N
                Node.Property p = new PropertySupport.ReadWrite(
                        name,
                        String.class,
                        displayName,
                        name
                        ) {
                            public boolean canWrite() {
                                return editable;
                            }
                            public Object getValue() {
                                try {
                                    String val = valueElementRef.getTextContent();
                                    if (val == null || val.length() == 0) {
                                        return val;
                                    }
                                    String decryptVal = ScEncrypt.decrypt(decryptedKey, val);
                                    return decryptVal;
                                } catch (Exception e) {
                                    return null;
                                }
                            }
                            public void setValue(Object val) {
                                String encryptVal = ScEncrypt.encrypt(decryptedKey, (String) val);
                                valueElementRef.setTextContent(encryptVal);
                                writeValueToXml();
                            }
                };
                pset.put(p);
            } else {
                Node.Property p = new PropertySupport.ReadWrite(
                        name,
                        String.class,
                        displayName,
                        name
                        ) {
                            public boolean canWrite() {
                                return editable;
                            }
                            public Object getValue() {
                                return valueElementRef.getTextContent();
                            }
                            public void setValue(Object val) {
                                valueElementRef.setTextContent((String) val);
                                writeValueToXml();
                            }
                };
                pset.put(p);
            }
        }

        public void done() {
            Sheet sets = getSheet();
            sets.put(pset);
        }

    }
}
