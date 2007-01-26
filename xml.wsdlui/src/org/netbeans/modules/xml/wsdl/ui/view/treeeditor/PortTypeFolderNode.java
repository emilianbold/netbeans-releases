/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.ui.cookies.AddChildWSDLElementCookie;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.PortTypeNewType;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;


/**
 *
 * @author Ritesh Adval
 *
 */
public class PortTypeFolderNode extends FolderNode {

    private Definitions mDef = null;
    
    
    public PortTypeFolderNode(Definitions element) {
        super(new PortTypeFolderChildren(element), element, PortType.class);
        mDef = element;
        this.setDisplayName(NbBundle.getMessage(MessageFolderNode.class, 
        "PORTTYPE_FOLDER_NODE_NAME"));
        getLookupContents().add(new AddChildWSDLElementCookie(element));
        this.addNodeListener(new WSDLNodeListener(this));
        BADGE_ICON= Utilities.loadImage
        ("org/netbeans/modules/xml/wsdl/ui/view/resources/port_type_badge.png");
    }


    @Override
    public final NewType[] getNewTypes()
    {
        if (isEditable()) {
            return new NewType[] {new PortTypeNewType(mDef)};
        }
        return new NewType[] {};
    }
    
    public Object getWSDLConstruct() {
        return mDef;
    }

    public static final class PortTypeFolderChildren extends GenericWSDLComponentChildren {
        public PortTypeFolderChildren(Definitions definitions) {
            super(definitions);
        }

        @Override
        protected Collection getKeys() {
            Definitions def = (Definitions) getWSDLComponent();
            return def.getPortTypes();
        }
    }

    @Override
    public Class getType() {
        return PortType.class;
    }
}
