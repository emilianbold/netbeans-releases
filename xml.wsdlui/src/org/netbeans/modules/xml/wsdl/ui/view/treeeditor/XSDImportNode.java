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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XSDImportNode extends ImportNode {

    Image ICON  = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/import-include-redefine.png");

    public XSDImportNode(Import wsdlConstruct) {
        super(new XSDImportNodeChildren(wsdlConstruct),
                wsdlConstruct);
    }


    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(XSDImportNode.class, "LBL_XSDImportNode_TypeDisplayName");
    }

    @Override
    protected void updateDisplayName() {
        Import imp = (Import) getWSDLComponent();
        setDisplayName(imp.getLocation());
    }

    public static class XSDImportNodeChildren extends GenericWSDLComponentChildren {

        private Import mWsdlConstruct;

        public XSDImportNodeChildren(Import wsdlConstruct) {
            super(wsdlConstruct);
            this.mWsdlConstruct = wsdlConstruct;
        }

        @Override
        protected Node[] createNodes(Object key) {
            if (key instanceof Schema) {
                Schema schema = (Schema) key;
                NodesFactory factory = NodesFactory.getInstance();
                Node node = factory.create(schema);
                node.setDisplayName(this.mWsdlConstruct.getNamespace());
                return new Node[] { node };
            }
            return super.createNodes(key);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Collection getKeys() {
            ArrayList keys = new ArrayList();
            List list = mWsdlConstruct.getModel().findSchemas(mWsdlConstruct.getNamespace());
            if (list != null && list.size() > 0) {
                Schema schema = (Schema) list.get(0);
                if (schema != null) {
                    keys.add(schema);
                }
            }
            keys.addAll(super.getKeys());
            return keys;
        }

    }
}
