/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.compapp.casaeditor.model.casa;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author jqian
 */
public class JBIServiceUnitTransferObject {
//    public static final DataFlavor ServiceUnitDataFlavor =
//            new DataFlavor(JBIServiceUnitTransferObject.class, "JBIServiceUnitDataFlavor" ) {  // NOI18N
//    };
    private String serviceUnitName;
    private String serviceUnitDescription;
    private String componentName;
    private boolean isBC;
    private Document doc;
    private List<Endpoint> providesList;
    private List<Endpoint> consumesList;

    public JBIServiceUnitTransferObject(String serviceUnitName,
            String componentName,
            String serviceUnitDescription,
            String descriptor) {
        this.serviceUnitName = serviceUnitName;
        this.componentName = componentName;
        this.serviceUnitDescription = serviceUnitDescription;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(descriptor)));
            Element services = (Element) doc.getElementsByTagName("services").item(0);
            isBC = services.getAttribute("binding-component").equals("true");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isBindingComponent() {
        return isBC;
    }

    public String getServiceUnitName() {
        return serviceUnitName;
    }

    public String getServiceUnitDescription() {
        return serviceUnitDescription;
    }

    public String getComponentName() {
        return componentName;
    }

    public List<Endpoint> getProvidesEndpoints() {
        if (providesList == null) {
            providesList = new ArrayList<Endpoint>();

            NodeList endpointList = doc.getElementsByTagName("provides");

            for (int i = 0; i < endpointList.getLength(); i++) {
                Element pc = (Element) endpointList.item(i);
                String endpointName = pc.getAttribute("endpoint-name");
                QName serviceQName =
                        getNSName(pc, pc.getAttribute("service-name"));
                QName interfaceQName =
                        getNSName(pc, pc.getAttribute("interface-name"));
                Endpoint endpoint = new Endpoint(endpointName, serviceQName, interfaceQName);
                providesList.add(endpoint);
            }
        }

        return providesList;
    }

    public List<Endpoint> getConsumesEndpoints() {
        if (consumesList == null) {
            consumesList = new ArrayList<Endpoint>();

            NodeList endpointList = doc.getElementsByTagName("consumes");

            for (int i = 0; i < endpointList.getLength(); i++) {
                Element pc = (Element) endpointList.item(i);
                String endpointName = pc.getAttribute("endpoint-name");
                QName serviceQName =
                        getNSName(pc, pc.getAttribute("service-name"));
                QName interfaceQName =
                        getNSName(pc, pc.getAttribute("interface-name"));
                Endpoint endpoint = new Endpoint(endpointName, serviceQName, interfaceQName);
                consumesList.add(endpoint);
            }
        }

        return consumesList;
    }

    private QName getNSName(Element e, String qname) {
        if (qname == null) {
            return null;
        }
        int i = qname.indexOf(':');
        if (i > 0) {
            String name = qname.substring(i + 1);
            String prefix = qname.substring(0, i);
            return new QName(getNamespace(e, prefix), name);
        }

        return null; // qname;
    }

    /**
     * Gets the namespace from the qname.
     *
     * @param prefix name prefix of service
     *
     * @return namespace namespace of service
     */
    public String getNamespace(Element el, String prefix) {
        if ((prefix == null) || (prefix.length() < 1)) {
            return "";
        }

        NamedNodeMap map = el.getAttributes();
        for (int j = 0; j < map.getLength(); j++) {
            org.w3c.dom.Node n = map.item(j);
            String localName = n.getLocalName();
            if (localName != null) {
                if (n.getLocalName().trim().equals(prefix.trim())) {
                    return n.getNodeValue();
                }
            }
        }

        return getNamespace((Element) el.getParentNode(), prefix);
    }

    public class Endpoint {

        private String endpointName;
        private QName serviceQName;
        private QName interfaceQName;

        Endpoint(String endpointName, QName serviceQName, QName interfaceQName) {
            this.endpointName = endpointName;
            this.serviceQName = serviceQName;
            this.interfaceQName = interfaceQName;
        }

        public String getEndpointName() {
            return endpointName;
        }

        public QName getServiceQName() {
            return serviceQName;
        }

        public QName getInterfaceQName() {
            return interfaceQName;
        }
    }
}
    /*
    private static class ItemDataTransferable implements Transferable {
        private String data;
        public ItemDataTransferable(String data) {
            this.data = data;
        }
     
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {ServiceUnitDataFlavor};
        }
     
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return ServiceUnitDataFlavor.equals( flavor );
        }
     
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if( !isDataFlavorSupported( flavor ) ) {
                throw new UnsupportedFlavorException( flavor );
            }
            return data;
        }
    }*/