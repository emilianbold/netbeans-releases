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
        super(wsdlConstruct, new TypesNewTypesFactory());
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
