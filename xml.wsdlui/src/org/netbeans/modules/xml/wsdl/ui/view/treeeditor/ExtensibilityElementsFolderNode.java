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
 * Created on May 17, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.cookies.AddChildWSDLElementCookie;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 * @author Ritesh Adval
 *
 *
 */
public class ExtensibilityElementsFolderNode extends FolderNode {
    
    private Definitions mDef = null;
    private Set<String> mSpecialTargetNamespaces;
    
    
    public ExtensibilityElementsFolderNode(Definitions element, Set<String> specialTargetNamespaces) {
        super(new ExtensibilityElementsFolderChildren(element, specialTargetNamespaces),
                element, ExtensibilityElement.class);
        mDef = element;
        mSpecialTargetNamespaces = specialTargetNamespaces;
        this.setDisplayName(NbBundle.
                getMessage(ExtensibilityElementsFolderNode.class,
                "EXTENSIBILITY_ELEMENTS_FOLDER_NODE_NAME"));
        getLookupContents().add(new AddChildWSDLElementCookie(element));
        this.addNodeListener(new WSDLNodeListener(this));
    }

    
    @Override
    public final NewType[] getNewTypes() {
        if (isEditable()) {
            return new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_DEFINITIONS).getNewTypes(mDef);
        }
        return new NewType[] {};
    }
    
    
    public Object getWSDLConstruct() {
        return mDef;
    }
    
    public static final class ExtensibilityElementsFolderChildren extends GenericWSDLComponentChildren {
        private Set<String> specialTargetNS;
        public ExtensibilityElementsFolderChildren(Definitions definitions, Set<String> specialTargetNamespaces) {
            super(definitions);
            specialTargetNS = specialTargetNamespaces;
        }
        
        @Override
        protected Collection getKeys() {
            Definitions def = (Definitions) getWSDLComponent();
            
            List<ExtensibilityElement> list = def.getExtensibilityElements();
            List<ExtensibilityElement> finalList = new ArrayList<ExtensibilityElement>();
            if (list != null) {
                for (ExtensibilityElement element : list) {
                    if (specialTargetNS.contains(element.getQName().getNamespaceURI())) {
                        continue;
                    }
                    finalList.add(element);
                }
            }
            return finalList;
        }
    }

    @Override
    public Class getType() {
        return ExtensibilityElement.class;
    }
}

