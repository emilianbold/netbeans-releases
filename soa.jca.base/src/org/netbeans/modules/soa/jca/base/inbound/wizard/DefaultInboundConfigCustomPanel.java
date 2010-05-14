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

import org.netbeans.modules.soa.jca.base.Base64;
import org.netbeans.modules.soa.jca.base.spi.GlobalRarProvider;
import org.netbeans.modules.soa.jca.base.spi.InboundConfigCustomPanel;
import java.awt.BorderLayout;
import java.beans.PropertyEditor;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Default implementation of inbound configuration editor.  It uses NetBeans
 * propertysheet to display property-name and property-value pairs.
 *
 * @author echou
 */
public class DefaultInboundConfigCustomPanel extends InboundConfigCustomPanel {

    private PropertySheet ps;
    private Map<String, ActivationStruct> activationConfigs = new HashMap<String, ActivationStruct> ();
    private EjbConfig ejbConfig = new EjbConfig();

    // called by JcaMdbNode EditActivationAction, to edit configuration stored in ejb-jar.xml
    public DefaultInboundConfigCustomPanel() {
        this.ps = new PropertySheet();
        super.setLayout(new BorderLayout());
        super.add(ps);
    }

    // called by inbound mdb wizard to create panel for inbound configuration
    public DefaultInboundConfigCustomPanel(GlobalRarProvider provider) {
        this();
        parseInboundConfig(provider);
        Node[] nodes = getConfigNodes();
        this.ps.setNodes(nodes);
    }

    // parse default inbound configuration
    private void parseInboundConfig(GlobalRarProvider provider) {

        InputStream is = null;
        try {
            is = provider.getInboundConfig();
            if (is == null) {
                NotifyDescriptor d = new NotifyDescriptor.Exception(
                        new Exception(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/wizard/Bundle").getString("unable_to_load_inbound_template_resource:_") +
                            provider.getName()));
                DialogDisplayer.getDefault().notifyLater(d);
                return;
            }

            Document doc = XMLUtil.parse(
                    new InputSource(is),
                    false, false, null, null);
            Element root = doc.getDocumentElement();

            // ejb-config
            NodeList ejbConfigList = root.getElementsByTagName("ejb-config"); // NOI18N
            if (ejbConfigList.getLength() > 0) {
                Element ejbConfigElement = (Element) ejbConfigList.item(0);
                String steadyPoolSize = ejbConfigElement.getAttribute("steady-pool-size"); // NOI18N
                if (steadyPoolSize != null && steadyPoolSize.length() > 0) {
                    ejbConfig.setSteadyPoolSize(Integer.parseInt(steadyPoolSize));
                }
                String resizeQuantity = ejbConfigElement.getAttribute("resize-quantity"); // NOI18N
                if (resizeQuantity != null && resizeQuantity.length() > 0) {
                    ejbConfig.setResizeQuantity(Integer.parseInt(resizeQuantity));
                }
                String maxPoolSize = ejbConfigElement.getAttribute("max-pool-size"); // NOI18N
                if (maxPoolSize != null && maxPoolSize.length() > 0) {
                    ejbConfig.setMaxPoolSize(Integer.parseInt(maxPoolSize));
                }
                String poolIdleTimeout = ejbConfigElement.getAttribute("pool-idle-timeout-in-seconds"); // NOI18N
                if (poolIdleTimeout != null && poolIdleTimeout.length() > 0) {
                    ejbConfig.setPoolIdleTimeout(Long.parseLong(poolIdleTimeout));
                }
                String maxWaitTime = ejbConfigElement.getAttribute("max-wait-time-in-millis"); // NOI18N
                if (maxWaitTime != null && maxWaitTime.length() > 0) {
                    ejbConfig.setMaxWaitTime(Long.parseLong(maxWaitTime));
                }
            }

            // activation-config
            NodeList activationConfigList = root.getElementsByTagName("activation-config"); // NOI18N
            if (activationConfigList.getLength() > 0) {
                NodeList activationConfigPropertyList =
                        ((Element) activationConfigList.item(0)).getElementsByTagName("activation-config-property"); // NOI18N
                for (int i = 0; i < activationConfigPropertyList.getLength(); i++) {
                    Element activationConfigProperty = (Element) activationConfigPropertyList.item(i);
                    Element activationPropName = (Element) activationConfigProperty.getElementsByTagName("activation-config-property-name").item(0); // NOI18N
                    Element activationPropValue = (Element) activationConfigProperty.getElementsByTagName("activation-config-property-value").item(0); // NOI18N
                    boolean editable = Boolean.parseBoolean(activationConfigProperty.getAttribute("editable")); // NOI18N
                    boolean hidden = Boolean.parseBoolean(activationConfigProperty.getAttribute("hidden")); // NOI18N
                    boolean base64 = Boolean.parseBoolean(activationConfigProperty.getAttribute("base64")); // NOI18N
                    boolean cdata = Boolean.parseBoolean(activationConfigProperty.getAttribute("cdata")); // NOI18N

                    String strValue = null;
                    if (base64) {
                        String tempValue = Base64.decode(activationPropValue.getTextContent().substring(6));
                        String str1 = tempValue.substring(9);
                        strValue = str1.substring(0, str1.length() - 3);
                    } else {
                        strValue = activationPropValue.getTextContent();
                    }

                    ActivationStruct acProperty = new ActivationStruct(
                            activationPropName.getTextContent(),
                            strValue);
                    acProperty.setEditable(editable);
                    acProperty.setHidden(hidden);
                    acProperty.setBase64(base64);
                    acProperty.setCdata(cdata);

                    activationConfigs.put(acProperty.getPropertyName(), acProperty);
                }
            }

            // configuration
            NodeList configurationList = root.getElementsByTagName("configuration"); // NOI18N
            if (configurationList.getLength() > 0) {
                Element configurationElement = (Element) configurationList.item(0);
                NodeList templateList = configurationElement.getElementsByTagName("template"); // NOI18N
                if (templateList.getLength() > 0) {
                    Element templateElement = (Element) templateList.item(0);
                    NodeList cfgList = templateElement.getElementsByTagName("cfg"); // NOI18N
                    if (cfgList.getLength() > 0) {
                        Element cfgElement = (Element) cfgList.item(0);

                        // make an instance element out of the template element
                        Element instanceElement = doc.createElement("instance"); // NOI18N
                        org.w3c.dom.Node newCfgElement = doc.importNode(cfgElement, true);
                        instanceElement.appendChild(newCfgElement);
                        configurationElement.appendChild(instanceElement);
                    }
                }

                StringWriter writer = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // NOI18N
                transformer.transform(new DOMSource(configurationElement), new StreamResult(writer));
                ActivationStruct acProperty = new ActivationStruct(
                            "Configuration", // NOI18N
                            writer.toString());
                acProperty.setEditable(true);
                acProperty.setHidden(false);
                acProperty.setBase64(true);
                acProperty.setCdata(true);

                activationConfigs.put(acProperty.getPropertyName(), acProperty);
            }

        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            DialogDisplayer.getDefault().notifyLater(d);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }

    }

    private Node[] getConfigNodes() {
        try {
            ConfigNode configNode = new ConfigNode(ejbConfig, activationConfigs);

            for (ActivationStruct acProperty : activationConfigs.values()) {
                configNode.addProperty(
                        acProperty.getPropertyName(),
                        acProperty.isEditable(),
                        acProperty.isHidden(),
                        acProperty.isBase64());
            }

            return new Node[] { configNode };

        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            DialogDisplayer.getDefault().notifyLater(d);
            return new Node[] {};
        }

    }

    class ConfigNode extends AbstractNode {

        private Map<String, ActivationStruct> activationConfigs;
        private Sheet.Set pset;

        public ConfigNode(final EjbConfig ejbConfig, Map<String, ActivationStruct> activationConfigs) {
            super(Children.LEAF);
            setName("activationnode"); // NOI18N
            this.activationConfigs = activationConfigs;
            this.pset = Sheet.createPropertiesSet();

            Sheet.Set eset = Sheet.createExpertSet();
            eset.setDisplayName("Advanced"); // NOI18N

            Node.Property steadyPoolSize = new PropertySupport.ReadWrite("steady-pool-size", // NOI18N
                Integer.class, "Steady Pool Size", "Steady Pool Size") { // NOI18N
                    public Object getValue() { return Integer.valueOf(ejbConfig.getSteadyPoolSize()); }
                    public void setValue(Object val) { ejbConfig.setSteadyPoolSize(((Integer) val).intValue()); }
            };
            Node.Property resizeQuantity = new PropertySupport.ReadWrite("resize-quantity", // NOI18N
                Integer.class, "Resize Quantity", "Resize Quantity") { // NOI18N
                    public Object getValue() { return Integer.valueOf(ejbConfig.getResizeQuantity()); }
                    public void setValue(Object val) { ejbConfig.setResizeQuantity(((Integer) val).intValue()); }
            };
            Node.Property maxPoolSize = new PropertySupport.ReadWrite("max-pool-size", // NOI18N
                Integer.class, "Max Pool Size", "Max Pool Size") { // NOI18N
                    public Object getValue() { return Integer.valueOf(ejbConfig.getMaxPoolSize()); }
                    public void setValue(Object val) { ejbConfig.setMaxPoolSize(((Integer) val).intValue()); }
            };
            Node.Property poolIdleTimeout = new PropertySupport.ReadWrite("pool-idle-timeout-in-seconds", // NOI18N
                Long.class, "Pool Idle Timeout in Seconds", "Pool Idle Timeout in Seconds") { // NOI18N
                    public Object getValue() { return Long.valueOf(ejbConfig.getPoolIdleTimeout()); }
                    public void setValue(Object val) { ejbConfig.setPoolIdleTimeout(((Long) val).longValue()); }
            };
            Node.Property maxWaitTime = new PropertySupport.ReadWrite("max-wait-time-in-millis", // NOI18N
                Long.class, "Max Wait Time in Milliseconds", "Max Wait Time in Milliseconds") { // NOI18N
                    public Object getValue() { return Long.valueOf(ejbConfig.getMaxWaitTime()); }
                    public void setValue(Object val) { ejbConfig.setMaxWaitTime(((Long) val).longValue()); }
            };
            eset.put(steadyPoolSize);
            eset.put(resizeQuantity);
            eset.put(maxPoolSize);
            eset.put(poolIdleTimeout);
            eset.put(maxWaitTime);

            Sheet sets = getSheet();
            sets.put(pset);
            sets.put(eset);
        }

        public void addProperty(final String propertyName, final boolean editable,
                final boolean isHidden, final boolean isBase64) {
            if (isBase64) {
                Node.Property p = new PropertySupport.ReadWrite(
                        propertyName,
                        List.class,
                        propertyName,
                        propertyName
                        ) {
                            public boolean canWrite() {
                                return false;
                            }
                            public boolean isHidden() {
                                return isHidden;
                            }
                            public Object getValue() {
                                return java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/wizard/Bundle").getString("<_Click_to_Edit_>");
                            }
                            public void setValue(Object val) {}
                            public PropertyEditor getPropertyEditor() {
                                try {
                                    return new ConfigApiEditor(activationConfigs.get(propertyName), editable);
                                } catch (Exception e) {
                                    NotifyDescriptor d = new NotifyDescriptor.Exception(e);
                                    DialogDisplayer.getDefault().notifyLater(d);
                                    return null;
                                }
                            }
                };
                pset.put(p);
            } else {
                Node.Property p = new PropertySupport.ReadWrite(
                        propertyName,
                        String.class,
                        propertyName,
                        propertyName
                        ) {
                            public boolean canWrite() {
                                return editable;
                            }
                            public boolean isHidden() {
                                return isHidden;
                            }
                            public Object getValue() {
                                return activationConfigs.get(propertyName).getPropertyValue();
                            }
                            public void setValue(Object val) {
                                activationConfigs.get(propertyName).setPropertyValue((String) val);
                            }

                };
                pset.put(p);
            }
        }

    }

    @Override
    public String isPanelValid() {
        return null;
    }

    @Override
    public void initFromInboundConfigData(InboundConfigData data) {
        for (Map.Entry<String, String> activationEntry : data.getActivationProps()) {
            String propertyName = activationEntry.getKey();
            String propertyValue = activationEntry.getValue();
            if ((propertyName.equals("Configuration") || propertyName.equals("ProjectInfo")) && // NOI18N
                    propertyValue.startsWith("BASE64")) { // NOI18N
                try {
                    String tempValue = Base64.decode(propertyValue.substring(6));
                    propertyValue = tempValue;
                    ActivationStruct activationStruct = new ActivationStruct(propertyName, propertyValue);
                    activationStruct.setBase64(true);
                    activationStruct.setHidden(false);
                    activationStruct.setEditable(true);
                    activationConfigs.put(propertyName, activationStruct);
                    continue;
                } catch (Exception e) {
                    propertyValue = "invalid base64-encoded string"; // NOI18N
                }

            }
            ActivationStruct activationStruct = new ActivationStruct(propertyName, propertyValue);
            activationStruct.setBase64(false);
            activationStruct.setEditable(false);
            activationStruct.setHidden(true);
            activationStruct.setCdata(false);
            activationConfigs.put(propertyName, activationStruct);
        }

        // ejb-config
        ejbConfig.setMaxPoolSize(data.getMaxPoolSize());
        ejbConfig.setSteadyPoolSize(data.getSteadyPoolSize());
        ejbConfig.setResizeQuantity(data.getResizeQuantity());
        ejbConfig.setMaxWaitTime(data.getMaxWaitTime());
        ejbConfig.setPoolIdleTimeout(data.getPoolIdleTimeout());

        Node[] nodes = getConfigNodes();
        this.ps.setNodes(nodes);
    }

    @Override
    public void storeToInboundConfigData(InboundConfigData data) {
        // populate activation configs
        for (ActivationStruct activationStruct : activationConfigs.values()) {
            String propertyName = activationStruct.getPropertyName();
            String propertyValue;
            if (activationStruct.isBase64()) {
                String cdataStr = //"<![CDATA[" + // NOI18N
                        activationStruct.getPropertyValue();// + "]]>"; // NOI18N
                try {
                    String encodedStr = "BASE64" + Base64.encode(cdataStr); // NOI18N
                    propertyValue = encodedStr;
                } catch (Exception e) {
                    // ignore
                    propertyValue = "invalid base64-encoded string"; // NOI18N
                }
            } else if (activationStruct.isCdata()) {
                String cdataStr = "<![CDATA[" + // NOI18N
                        activationStruct.getPropertyValue() + "]]>"; // NOI18N
                propertyValue = cdataStr;
            } else {
                propertyValue = activationStruct.getPropertyValue();
            }
            data.addActivationProperty(propertyName, propertyValue);
        }

        // populate ejb configs
        data.setMaxPoolSize(ejbConfig.getMaxPoolSize());
        data.setSteadPoolSize(ejbConfig.getSteadyPoolSize());
        data.setResizeQuantity(ejbConfig.getResizeQuantity());
        data.setMaxWaitTime(ejbConfig.getMaxWaitTime());
        data.setPoolIdleTimeout(ejbConfig.getPoolIdleTimeout());
    }

}
