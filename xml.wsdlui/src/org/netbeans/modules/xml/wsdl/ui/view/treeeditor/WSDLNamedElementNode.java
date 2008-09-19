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
import org.openide.nodes.ChildFactory;
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
    
    public WSDLNamedElementNode(T element,
            NewTypesFactory newTypesFactory) {
        super(element, newTypesFactory);
    }
    
    public WSDLNamedElementNode(T element) {
        super(element);
    }
    
    public WSDLNamedElementNode(ChildFactory factory, T element,
            NewTypesFactory newTypesFactory) {
        super(factory, element, newTypesFactory);
    }

    public WSDLNamedElementNode(ChildFactory factory, T element) {
        super(factory, element);
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
