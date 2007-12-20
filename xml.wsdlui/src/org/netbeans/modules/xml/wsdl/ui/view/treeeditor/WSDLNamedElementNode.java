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
 * Created on May 25, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.commands.NamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.xam.Named;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class WSDLNamedElementNode<T extends WSDLComponent> extends WSDLElementNode<T> {
    
    private NamedPropertyAdapter mPropertyAdapter;
    private String nameAttributeName;
    
    public WSDLNamedElementNode(Children children, 
            T element, 
            NewTypesFactory newTypesFactory) {
        super(children, element, newTypesFactory);
    }
    public WSDLNamedElementNode(Children children, 
            T element) {
        super(children, element);
    }
    
    
    public void setNamedPropertyAdapter(NamedPropertyAdapter adapter) {
        nameAttributeName = Named.NAME_PROPERTY;
        this.mPropertyAdapter = adapter;
        if(this.mPropertyAdapter != null) {
            super.setName(this.mPropertyAdapter.getName());
        }
    }
    
    public void setNamedPropertyAdapter(String attributeName, NamedPropertyAdapter adapter) {
        nameAttributeName = attributeName;
        this.mPropertyAdapter = adapter;
        if(this.mPropertyAdapter != null) {
            super.setName(this.mPropertyAdapter.getName());
        }
    }
    
    @Override
    public void setName(String name) {
        if(this.mPropertyAdapter != null && isEditable()) {
            this.mPropertyAdapter.setName(name);
            super.setName(mPropertyAdapter.getName());
        } 
        
    }
    
    @Override
    public String getName() {
        if (super.getName() == null)
            return "";
        return super.getName();
    }
  
    
    //a named element node has a name which can be renamed
    @Override
    public boolean canRename() {
        return isEditable();
    }
    
    @Override
    protected Node.Property createAttributeProperty(QName attrQName) {
        Node.Property attrValueProperty = null;
        try {
            String attrName = attrQName.getLocalPart();
            //name
            if(attrName.equals(nameAttributeName) && mPropertyAdapter != null) { //NOT I18N
                attrValueProperty = new BaseAttributeProperty(mPropertyAdapter, 
                        String.class, Named.NAME_PROPERTY);
                attrValueProperty.setName(attrName);
                attrValueProperty.setDisplayName(NbBundle.getMessage(WSDLNamedElementNode.class, "PROP_NAME_NAME"));
                
            } else {
                attrValueProperty = super.createAttributeProperty(attrQName);
            }
            
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
            ErrorManager.getDefault().notify(ex);
        }
        return attrValueProperty;
    }
    

    
/*    @Override
    public void valueChanged(ComponentEvent evt) {
        Object source = evt.getSource();
        if(!(source instanceof WSDLComponent)) {
            return;
        }
        
        WSDLComponent wsdlComponent = (WSDLComponent) source;
        
        if(!isSameAsMyWSDLElement(wsdlComponent)) {
            return;
        }
        
        String nodeName = getName();
        String nameAttrValue = "";
        if (mPropertyAdapter != null) {
            nameAttrValue = mPropertyAdapter.getName();
        } else {
        	if(nameAttributeName != null) {
        		nameAttrValue = wsdlComponent.getAttribute(new StringAttribute(nameAttributeName));
        	}
        }
        
        if(nameAttrValue != null && !nameAttrValue.equals(nodeName)) {
            super.setName(nameAttrValue);
            setDisplayName(nameAttrValue);
        }
        
        super.valueChanged(evt);
    }*/
    
    
    
    
}
