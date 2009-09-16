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

package org.netbeans.modules.xml.wsdl.ui.wsdl.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.PrimitiveSimpleType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.FolderNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;



/**
 *
 * @author radval
 *
 */
public class BuiltInTypeFolderNode extends AbstractNode {
    
    private static final Image ICON  = ImageUtilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/simpleType_badge.png");
    
    
    public BuiltInTypeFolderNode() {
        super(new BuiltInTypeNodeChildren(SchemaModelFactory.getDefault().getPrimitiveTypesModel()));
        this.setName(NbBundle.getMessage(BuiltInTypeFolderNode.class, "BUILTIN_SCHEMATYPE_NAME"));
        this.setDisplayName(NbBundle.getMessage(BuiltInTypeFolderNode.class, "BUILTIN_SCHEMATYPE_NAME"));
        this.setShortDescription(NbBundle.getMessage(BuiltInTypeFolderNode.class, "BUILTIN_SCHEMATYPE_NAME"));
        
    }
    
    @Override
    public Image getIcon(int type) {
        Image icon = FolderNode.FolderIcon.getIcon(type);
        return ImageUtilities.mergeImages(icon, ICON, 8, 8);
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        Image icon = FolderNode.FolderIcon.getOpenedIcon(type);
        return ImageUtilities.mergeImages(icon, ICON, 8, 8);
    }
    
    public static class BuiltInTypeNodeChildren extends Children.Keys<GlobalSimpleType> {
        private SchemaModel model = null;
        private Set<GlobalSimpleType> emptySet = Collections.emptySet();
        
        BuiltInTypeNodeChildren(SchemaModel model) {
            this.model = model;
        }
        
        @Override
        protected Node[] createNodes(GlobalSimpleType simpleType) {
            List<Class<? extends SchemaComponent>> filters = new ArrayList<Class<? extends SchemaComponent>>();
            filters.add(PrimitiveSimpleType.class);
            CategorizedSchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                    model, filters, Lookup.EMPTY);
            Node node = factory.createNode(simpleType);
            return new Node[] { new ChildLessNode(node) };
        }
        
        
        @Override
        protected void addNotify() {
            resetKeys();
        }
        
        @Override
        protected void removeNotify() {
            this.setKeys(emptySet);
            
        }
        
        private void resetKeys() {
            ArrayList<GlobalSimpleType> keys = new ArrayList<GlobalSimpleType>();
            Schema schema = model.getSchema();
            if (schema != null) {
                keys.addAll(schema.getSimpleTypes());
                this.setKeys(keys);
            }
        }
        
    }
    
    private static class ChildLessNode extends FilterNode {

        public ChildLessNode(Node node) {
            super(node, Children.LEAF);
        }
    }
}

