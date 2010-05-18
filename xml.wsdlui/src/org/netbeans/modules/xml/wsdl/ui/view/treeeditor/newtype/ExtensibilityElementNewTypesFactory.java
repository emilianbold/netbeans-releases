/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
