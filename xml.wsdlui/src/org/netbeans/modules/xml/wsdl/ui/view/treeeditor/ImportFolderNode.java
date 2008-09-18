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

import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ImportWSDLNewType;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;


/**
 *
 * @author Ritesh Adval
 *
 */
public class ImportFolderNode extends FolderNode {
    
    private Definitions mDef = null;
    
    public ImportFolderNode(Definitions element) {
        super(new FolderChildFactory(element, Import.class), element, Import.class);
        mDef = element;
        this.setDisplayName(NbBundle.getMessage(ImportFolderNode.class, 
        "IMPORT_FOLDER_NODE_NAME"));
    }
    
    public static final class ImportFolderChildren extends GenericWSDLComponentChildren<Definitions> {
        public ImportFolderChildren(Definitions definitions) {
            super(definitions);
        }
        
        @Override
        public final Collection<Import> getKeys() {
            Definitions def = getWSDLComponent();
            return def.getImports();
        }
    }
    
    @Override
    public final NewType[] getNewTypes()
    {
        if (isEditable()) {
            return new NewType[] {
                    //new ImportSchemaNewType(mDef),
                    new ImportWSDLNewType(mDef),
            };
        }
        return new NewType[] {};
    }

    @Override
    public Class getType() {
        return Import.class;
    }
    
    @Override
    protected void createPasteTypes(Transferable transferable,
            List<PasteType> list) {
        super.createPasteTypes(transferable, list);
        Node[] nodes = Utility.getNodes(transferable);
        if (nodes.length == 1) {
            Node node = nodes[0];
            DataObject dObj = node.getLookup().lookup(DataObject.class);
            PasteType p = Utility.getWSDLOrSchemaPasteType(dObj, mDef.getModel(), false, true);
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
            PasteType p = Utility.getWSDLOrSchemaPasteType(dObj, mDef.getModel(), false, true);
            if (p != null) {
                return p;
            }
        }   
        return super.getDropType(transferable, action, index);
    }
    
}
