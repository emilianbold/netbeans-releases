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

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.ui.cookies.AddChildWSDLElementCookie;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingNewType;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;

/**
 * @author Ritesh Adval
 *
 * 
 */
public class BindingFolderNode extends FolderNode {

    private Definitions mDef = null;
    
    public BindingFolderNode(Definitions element) {
        super(new BindingFolderChildren(element), element, Binding.class);
         mDef = element;
        this.setDisplayName(NbBundle.
                    getMessage(BindingFolderNode.class, 
                               "BINDING_FOLDER_NODE_NAME"));
        getLookupContents().add(new AddChildWSDLElementCookie(element));
        this.addNodeListener(new WSDLNodeListener(this));
        BADGE_ICON  = Utilities.loadImage
        ("org/netbeans/modules/xml/wsdl/ui/view/resources/binding_badge.png");
    }

    @Override
    public final NewType[] getNewTypes()
    {
        if (isEditable()) {
            return new NewType[] {new BindingNewType(mDef)};
        }
        return new NewType[] {};
    }
    public Object getWSDLConstruct() {
        return mDef;
    }
    public static final class BindingFolderChildren extends GenericWSDLComponentChildren {
        public BindingFolderChildren(Definitions definitions) {
            super(definitions);
        }

        @Override
        protected Collection getKeys() {
            Definitions def = (Definitions) getWSDLComponent();
            return def.getBindings();
        }
    }
    @Override
    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
    }

    @Override
    public Transferable drag() throws IOException {
        return super.drag();
    }

    @Override
    public Class getType() {
        return Binding.class;
    }
    
    
}


