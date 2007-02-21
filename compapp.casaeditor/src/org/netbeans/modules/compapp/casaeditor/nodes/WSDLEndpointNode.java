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

/*
 * ServiceUnitNode.java
 *
 * Created on November 2, 2006, 8:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.WSDLEndpointAction;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import javax.swing.Action;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;

/**
 *
 * @author Josh Sandusky
 */
public class WSDLEndpointNode extends CasaNode {
    
    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/WSDLEndpointNode.png");
    
    private static final String CHILD_ID_PROVIDES = "Provides";
    private static final String CHILD_ID_CONSUMES = "Consumes";
    
    
    public WSDLEndpointNode(CasaComponent component, Children children, Lookup lookup) {
        super(component, children, lookup);
    }
    
    public WSDLEndpointNode(CasaComponent component, Lookup lookup) {
        super(component, new MyChildren(component, lookup), lookup);
    }
    
    
    @Override
    public String getName() {
        CasaPort endpoint = (CasaPort) getData();
        if (endpoint != null) {
            return ((CasaWrapperModel)endpoint.getModel()).getEndpointName(endpoint);
            //return endpoint.getQName().getLocalPart(); // TMP FIXME
        }
        return super.getName();
    }
    
    @Override
    public String getHtmlDisplayName() {
        try {
            String htmlDisplayName = getName();
            CasaPort endpoint = (CasaPort) getData();
            String decoration = null;
            if (endpoint != null) {
                decoration = NbBundle.getMessage(WSDLEndpointNode.class, "LBL_NameAttr",
                        ((CasaWrapperModel)endpoint.getModel()).getEndpointName(endpoint));
            }
            if (decoration == null) {
                return htmlDisplayName;
            }
            return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";
        } catch (Throwable t) {
            // getHtmlDisplayName MUST recover gracefully.
            return getBadName();
        }
    }
    
    public Action[] getActions(boolean context) {
        List actions = new ArrayList();
        Action[] parentActions = super.getActions(context);
        for (Action parentAction : parentActions) {
            actions.add(parentAction);
        }
        actions.add(null);
        actions.add(SystemAction.get(WSDLEndpointAction.class));
        return (Action[]) actions.toArray(new Action[actions.size()]);
    }
    
    protected void setupPropertySheet(Sheet sheet) {
        final CasaPort endpoint = (CasaPort) getData();
        if (endpoint == null) {
            return;
        }
        
        Sheet.Set identificationProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.IDENTIFICATION_SET);
        Node.Property nameSupport = new PropertySupport.ReadOnly(
                "name", // NO18N
                String.class,
                NbBundle.getMessage(getClass(), "PROP_EndpointName"),
                "") {
            public String getValue() {
                return ((CasaWrapperModel)endpoint.getModel()).getEndpointName(endpoint);
            }
        };
        identificationProperties.put(nameSupport);
        
        Node.Property componentNameSupport = new PropertySupport.ReadOnly(
                "componentName", // NO18N
                String.class,
                NbBundle.getMessage(getClass(), "PROP_ComponentName"),
                "") {
            public String getValue() {
                return ((CasaWrapperModel)endpoint.getModel()).getBindingComponentName(endpoint); 
            }
        };
        identificationProperties.put(componentNameSupport);
    }
    
    
    private static class MyChildren extends CasaNodeChildren {
        public MyChildren(CasaComponent component, Lookup lookup) {
            super(component, lookup);
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof Port) {
                Node pn = NodesFactory.getInstance().create((Port) key);
                //((WSDLElementNode) pn).setEditable(false);
                return new Node[] { pn };
            } else if (key instanceof Binding) {
                Node bn = NodesFactory.getInstance().create((Binding) key);
                //((WSDLElementNode) bn).setEditable(false);
                return new Node[] { bn };
            }
            
            // todo: 12/18/06 removed the consume and provide node within WSDL port
            /*
            assert key instanceof String;
            CasaPort endpoint = (CasaPort) getData();
            if (endpoint != null) {
                String keyName = (String) key;
                if (keyName.equals(CHILD_ID_CONSUMES)) {
                    return new Node[] { new ConsumesNode(endpoint.getConsumes(true), mLookup) };
                } else if (keyName.equals(CHILD_ID_PROVIDES)) {
                    return new Node[] { new ProvidesNode(endpoint.getProvides(true), mLookup) };
                }
            }
             */
            return null;
        }
        
        public Object getChildKeys(Object data)  {
            List children = new ArrayList();
            CasaPort endpoint = (CasaPort) getData();
            if (endpoint != null) {
                
                // todo: 12/15/06 test code...
                Port p = ((CasaWrapperModel) endpoint.getModel()).getLinkedWSDLPort(endpoint);
                if (p != null) {
                    children.add(p);
                    // TMP: the try/catch is added temporarly to ignore an 
                    // illegal state exception thrown when executing the steps
                    // in BUG #13.
                    // I probably need to fire some event when port/binding/service 
                    // is removed from wsdl model or need to change the order 
                    // that things get deleted in the model. 
                    // -Jun 01/26/07
                    try { 
                        Binding b = p.getBinding().get();
                        if (b != null) {
                            children.add(b);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (endpoint.getConsumes() != null) {
                    children.add(CHILD_ID_CONSUMES);
                }
                if (endpoint.getProvides() != null) {
                    children.add(CHILD_ID_PROVIDES);
                }
            }
            return children;
        }
    }
    
    public Image getIcon(int type) {
        return ICON;
    }
    
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    public boolean isEditable(String propertyType) {
        CasaPort port = (CasaPort) getData();
        if (port != null) {
            return getModel().isEditable(port, propertyType);
        }
        return false;
    }
    
    public boolean isDeletable() {
        CasaPort port = (CasaPort) getData();
        if (port != null) {
            return getModel().isDeletable(port);
        }
        return false;
    }
}
