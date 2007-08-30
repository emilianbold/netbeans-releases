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

package org.netbeans.modules.sun.manager.jbi.nodes;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceUnitStatus;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.util.datatransfer.ExTransferable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Node for one JBI Service Unit.
 *
 * @author jqian
 */
public class JBIServiceUnitNode extends AppserverJBIMgmtLeafNode {
    
    private String componentName;
    
    public JBIServiceUnitNode(final AppserverJBIMgmtController controller,
            final String name,
            final String displayName,
            final String description) {
        super(controller, NodeType.SERVICE_UNIT);
        setName(name);
        setDisplayName(displayName);
        setShortDescription(description);
    }
    
    /**
     * Return the SheetProperties to be displayed for this JVM.
     *
     * @return A java.util.Map containing all JVM properties.
     */
    protected Map<Attribute, MBeanAttributeInfo> getSheetProperties() {
        JBIServiceUnitStatus serviceUnit = getServiceUnitStatus();
        return Utils.getIntrospectedPropertyMap(serviceUnit, true);
    }
    
    /**
     *
     */
    public Attribute setSheetProperty(String attrName, Object value) {
        return null;
    }
    
    /**
     *
     * @return
     */
    private JBIServiceUnitStatus getServiceUnitStatus() {
        String assemblyName = getParentNode().getName();
        return getAdminService().getServiceUnitStatus(assemblyName, getName());
    }
    
    /**
     *
     */
    public Image getIcon(int type) {
        
        String baseIconName = IconConstants.SERVICE_UNIT_ICON;
        
        JBIServiceUnitStatus unitStatus = getServiceUnitStatus();
        String status = (unitStatus == null) ? null : unitStatus.getStatus();
        
        String externalBadgeIconName = null;
        if (JBIComponentStatus.SHUTDOWN_STATE.equals(status)) {
            externalBadgeIconName = IconConstants.INSTALLED_ICON;
        } else if (JBIComponentStatus.STOPPED_STATE.equals(status)) {
            externalBadgeIconName = IconConstants.STOPPED_ICON;
        } else if (!JBIComponentStatus.STARTED_STATE.equals(status)) {
            externalBadgeIconName = IconConstants.UNKNOWN_ICON;
        }
        
        return Utils.getBadgedIcon(getClass(), baseIconName, null, externalBadgeIconName);
    }
    
    
    // DnD Support for CASA
    
    public static final DataFlavor ServiceUnitDataFlavor =
            new DataFlavor(Object.class, "JBIServiceUnitDataFlavor" ) {  // NOI18N
    };
    
    public Transferable drag() throws IOException {
        ExTransferable retValue = ExTransferable.create( super.drag() );
        //add the 'data' into the Transferable
        final String suDD = getDeploymentDescriptor();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            // parse SU DD
            Document suDoc = builder.parse(new InputSource(new StringReader(suDD)));
            Element services = (Element) suDoc.getElementsByTagName("services").item(0); // NOI18N
            boolean isBC = services.getAttribute("binding-component").equals("true"); // NOI18N
            
            if (!isBC) {
                retValue.put( new ExTransferable.Single(ServiceUnitDataFlavor) {
                    protected Object getData() throws IOException, UnsupportedFlavorException {
                        List<String> ret = new ArrayList<String>();
                        ret.add("JBIMGR_SU_TRANSFER"); // NOI18N
                        ret.add(getName()); // service unit name
                        ret.add(getComponentName()); 
                        ret.add(getShortDescription());
                        ret.add(suDD);
                        return ret;
                    }
                });
            } 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         
         /*
         return new Transferable() {
             public DataFlavor[] getTransferDataFlavors() {
                 return new DataFlavor[] {
                     JBIServiceUnitTransferObject.ServiceUnitDataFlavor};
             }
             
             public boolean isDataFlavorSupported(DataFlavor flavor) {
                 return JBIServiceUnitTransferObject.ServiceUnitDataFlavor.equals(flavor);
             }
             
             public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                 return suTransfer;
             }
             
         };*/
        
        return retValue;
    }
    
    public String getDeploymentDescriptor() {
        String assemblyName = getParentNode().getName();
        String suDD = getAdminService().getServiceUnitDeploymentDescriptor(assemblyName, getName());
        return suDD;
    }
        
    private String getComponentName() {
        if (componentName == null) {
            JBIServiceAssemblyNode saNode = (JBIServiceAssemblyNode)getParentNode(); 
            String saDD = saNode.getDeploymentDescriptor();
            String myName = getName();
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                
                // parse SA DD
                Document saDoc = builder.parse(new InputSource(new StringReader(saDD)));
                NodeList sus = saDoc.getElementsByTagName("service-unit"); // NOI18N
                for (int i = 0; i < sus.getLength(); i++) {
                    Element su = (Element) sus.item(i);
                    String name = ((Element)su.getElementsByTagName("name").item(0)).getFirstChild().getNodeValue(); // identification/name
                    if (name.equals(myName)) {
                        componentName = ((Element)su.getElementsByTagName("component-name").item(0)).getFirstChild().getNodeValue(); // target/component-name
                        break;
                    }
                }                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            if (componentName == null) {
                componentName = "?"; // NOI18N
            }
        }
        return componentName;
    }
    
}
