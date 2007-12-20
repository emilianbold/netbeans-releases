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
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ImportSchemaNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.SchemaNewType;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;



/**
 *
 * @author Ritesh Adval
 *
 * 
 */
public class TypesNode extends WSDLElementNode<Types> {

    private static final Image ICON  = Utilities.loadImage
    ("org/netbeans/modules/xml/wsdl/ui/view/resources/schema_folder_badge_var3.png");

    protected Types mWSDLConstruct;

    public TypesNode(Types wsdlConstruct) {
        super(new GenericWSDLComponentChildren<Types>(wsdlConstruct), wsdlConstruct, new TypesNewTypesFactory());
        mWSDLConstruct = wsdlConstruct;

        this.setDisplayName(NbBundle.getMessage(TypesNode.class, "TYPES_NODE_NAME"));
    }

    @Override
    public Image getIcon(int type) {
        Image folderIcon = FolderNode.FolderIcon.getIcon(type);
        if (ICON != null) {
            return Utilities.mergeImages(folderIcon, ICON, 8, 8);
        }

        return folderIcon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        Image folderIcon = FolderNode.FolderIcon.getOpenedIcon(type);
        if (ICON != null) {
            return Utilities.mergeImages(folderIcon, ICON, 8, 8);
        }

        return folderIcon;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(TypesNode.class);
    }

    public static final class TypesNewTypesFactory implements NewTypesFactory{

        public NewType[] getNewTypes(WSDLComponent def) {
            Types types = (Types) def;
            ArrayList<NewType> list = new ArrayList<NewType>();
            if (def.getDocumentation() == null) {
                list.add(new DocumentationNewType(def));
            }

            list.add(new SchemaNewType(types));
            list.add(new ImportSchemaNewType(types));

            return list.toArray(new NewType[list.size()]);
        }
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(TypesNode.class, "LBL_TypesNode_TypeDisplayName");
    }

    @Override
    protected void createPasteTypes(Transferable transferable,
            List<PasteType> list) {
        super.createPasteTypes(transferable, list);
        Node[] nodes = Utility.getNodes(transferable);
        if (nodes.length == 1) {
            Node node = nodes[0];
            DataObject dObj = node.getLookup().lookup(DataObject.class);
            PasteType p = Utility.getWSDLOrSchemaPasteType(dObj, getWSDLComponent().getModel(), true, false);
            if (p != null) {
                list.add(p);
            }
        }
    }

    @Override
    public PasteType getDropType(Transferable transferable, int action,
            int index) {
        Node[] nodes = Utility.getNodes(transferable);
        if (nodes.length == 1) {
            Node node = nodes[0];
            DataObject dObj = node.getLookup().lookup(DataObject.class);
            PasteType p = Utility.getWSDLOrSchemaPasteType(dObj, getWSDLComponent().getModel(), true, false);
            if (p != null) {
                return p;
            }
        }
        return super.getDropType(transferable, action, index);
    }
}
