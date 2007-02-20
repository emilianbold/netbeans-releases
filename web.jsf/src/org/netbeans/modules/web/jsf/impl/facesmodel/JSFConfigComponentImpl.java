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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.ComponentInfo;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Petr Pisl
 */
public abstract class JSFConfigComponentImpl extends AbstractDocumentComponent <JSFConfigComponent>
        implements JSFConfigComponent {
    
    /** Creates a new instance of JSFConfigComponentImp */
    public JSFConfigComponentImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public JSFConfigModelImpl getModel(){
        return (JSFConfigModelImpl)super.getModel();
    }
    protected void populateChildren(List<JSFConfigComponent> children) {
        NodeList nodeList = getPeer().getChildNodes();
        if (nodeList != null){
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node instanceof Element) {
                    JSFConfigModel model = getModel();
                    JSFConfigComponent comp = (JSFConfigComponent) model.getFactory().create((Element)node, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }
    
    static public Element createElementNS(JSFConfigModel model,JSFConfigQNames jsfqname) {
        return model.getDocument().createElementNS(jsfqname.getQName(model.getVersion()).getNamespaceURI(), jsfqname.getQualifiedName(model.getVersion()));
    }
    
    public static abstract class ComponentInfoImpl extends JSFConfigComponentImpl implements ComponentInfo {
        
        public ComponentInfoImpl(JSFConfigModelImpl model, Element element) {
            super(model, element);
        }
        
        public String getDescription() {
            return getChildElementText(JSFConfigQNames.DESCRIPTION.getQName(getModel().getVersion()));
        }
        
        public void setDescription(String description) {
            setChildElementText(DESCRIPTION, description, JSFConfigQNames.DESCRIPTION.getQName(getModel().getVersion()));
        }
        
        public String getDisplayName() {
            return getChildElementText(JSFConfigQNames.DISPLAY_NAME.getQName(getModel().getVersion()));
        }
        
        public void setDisplayName(String displayName) {
            setChildElementText(DISPLAY_NAME, displayName, JSFConfigQNames.DISPLAY_NAME.getQName(getModel().getVersion()));
        }
        
        public String getIcon() {
            return getChildElementText(JSFConfigQNames.ICON.getQName(getModel().getVersion()));
        }
        
        public void setIcon(String icon) {
            setChildElementText(ICON, icon, JSFConfigQNames.ICON.getQName(getModel().getVersion()));
        }
        
        
        /*public String getName() {
            return "Named.getName()";
        }
         
        public void setName(String name) {
            //super.setAttribute(Nameable.NAME_PROPERTY, RegistryAttributes.NAME, name);
        }*/
    }
}
