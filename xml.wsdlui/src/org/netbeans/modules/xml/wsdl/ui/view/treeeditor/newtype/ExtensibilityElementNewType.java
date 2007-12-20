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

import java.io.IOException;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.NewType;

public class ExtensibilityElementNewType extends NewType {
    private WSDLComponent mDef = null;
    private String name;

    private WSDLExtensibilityElementInfo info;
    
    public ExtensibilityElementNewType(WSDLComponent def, WSDLExtensibilityElementInfo info) {
        mDef = def;
        this.info = info;
        DataObject dataObject = this.info.getDataObject();
        Node delegateNode = dataObject.getNodeDelegate();
        if (delegateNode != null) {
            this.name = delegateNode.getDisplayName();
        } else {
            this.name = dataObject.getName();
        }
        
    }
    
    
    @Override
    public String getName() {
        return name;
    }
    
    
    @Override
    public void create() throws IOException {
        Utility.addExtensibilityElement(mDef, info.getElement(), info.getPrefix());
    }
    
}
