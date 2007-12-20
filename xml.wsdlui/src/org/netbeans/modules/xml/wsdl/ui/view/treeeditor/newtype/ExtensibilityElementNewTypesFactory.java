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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author skini
 */
public class ExtensibilityElementNewTypesFactory implements NewTypesFactory {
    
    private String mNodeType;
    private String[] mSpecialTargetNamespaces;
    /** Creates a new instance of ExtensibilityElementNewTypesFactory */
    public ExtensibilityElementNewTypesFactory(String nodeType) {
        mNodeType = nodeType;
    }
    
    public ExtensibilityElementNewTypesFactory(String nodeType, String[] specialTargetNS) {
        mNodeType = nodeType;
        mSpecialTargetNamespaces = specialTargetNS;
    }
    
    public NewType[] getNewTypes(WSDLComponent component) {
        ArrayList<ExtensibilityElementNewType> eeNewTypeList = new ArrayList<ExtensibilityElementNewType>();
        try {
            WSDLExtensibilityElements extensibilityElement
                    = WSDLExtensibilityElementsFactory.getInstance().getWSDLExtensibilityElements();
            
            WSDLExtensibilityElement element =  extensibilityElement.getWSDLExtensibilityElement(mNodeType);
            String namespace = getExtensibilityElementNamespace(component);
            if(element != null) {
                Collection<WSDLExtensibilityElementInfo> elementInfos = null;
                if (mSpecialTargetNamespaces != null) {
                    elementInfos = new ArrayList<WSDLExtensibilityElementInfo>();
                    for (String ns : mSpecialTargetNamespaces) {
                        elementInfos.addAll(element.getWSDLExtensibilityElementInfos(ns));
                    }
                    
                } else {
                    if (namespace != null) {
                        elementInfos = element.getWSDLExtensibilityElementInfos(namespace);
                    } else {
                        elementInfos = element.getAllWSDLExtensibilityElementInfos();
                    }
                }
                
                //remove already added ones.
                
                createExtensibilityElementNewTypes(component, elementInfos, eeNewTypeList);
               
            }
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return eeNewTypeList.toArray(new NewType[eeNewTypeList.size()]);
    }
    
    private void createExtensibilityElementNewTypes(WSDLComponent component, Collection<WSDLExtensibilityElementInfo> infos, List<ExtensibilityElementNewType> list) {
        
        if (infos != null) {
            for (WSDLExtensibilityElementInfo info : infos) {
                list.add(new ExtensibilityElementNewType(component, info));
            }
            Collections.sort(list, new ExtensibilityElementNewTypeComparator());
        }
    }
    
    private String getExtensibilityElementNamespace(WSDLComponent comp) {
        Binding binding = null;
        if (comp instanceof BindingOperation 
                || comp instanceof BindingInput 
                || comp instanceof BindingOutput
                || comp instanceof BindingFault)
        {
            WSDLComponent tempComp = comp;
            while (!((tempComp = tempComp.getParent()) instanceof Binding)) {
                //do nothing
            }
            binding = (Binding) tempComp;
        } else if (comp instanceof Port) {
            NamedComponentReference<Binding> ref = ((Port)comp).getBinding();
            if (ref != null && ref.get() != null) {
                binding = ref.get();
            }
        }
        if (binding != null) {
            List<ExtensibilityElement> eeList = binding.getExtensibilityElements();
            if (eeList != null && !eeList.isEmpty()) {
                ExtensibilityElement element = eeList.get(0);
                QName qname = element.getQName();
                if (qname.getNamespaceURI() != null) {
                    return qname.getNamespaceURI();
                } else if (qname.getPrefix() != null) {
                    return Utility.getNamespaceURI(qname.getPrefix(), comp);
                }
            }
        }
        
        return null;
    }
    
    
    
    static class ExtensibilityElementNewTypeComparator implements Comparator<ExtensibilityElementNewType>, Serializable {

        private static final long serialVersionUID = 8682651956156135260L;

        public int compare(ExtensibilityElementNewType o1, ExtensibilityElementNewType o2) {
            return o1.getName().compareTo(o2.getName());
        }
        
    }
   
}
