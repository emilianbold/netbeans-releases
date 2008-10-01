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

/*
 * PrimitiveSimpleTypesNode.java
 *
 * Created on April 13, 2006, 9:49 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Administrator
 */
public class PrimitiveSimpleTypesNode extends AbstractNode
{
	/** Creates a new instance of PrimitiveSimpleTypesNode */
	public PrimitiveSimpleTypesNode(SchemaUIContext context)
	{
		super(new TypesChildren(context));
		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/"+
			"folder_with_elementBadge.png");
		setName(NbBundle.getMessage(CategoryNode.class,
				"LBL_CategoryNode_PrimitiveSimpleTypes"));
	}

	private Node getFolderNode() {
	    FileObject fo =
		Repository.getDefault().getDefaultFileSystem().getRoot();
	    Node n = null;
	    try {
		DataObject dobj = DataObject.find(fo);
		n = dobj.getNodeDelegate();
	    } catch (DataObjectNotFoundException ex) {
		// cannot get the node for this, this shouldn't happen
		// so just ignore
	    }
	    
	    return n;
	}
	
	@Override
	public Image getIcon(int type) {
	    Node n = getFolderNode();
	    Image i = super.getIcon(type);
	    if (n != null) {
		i = n.getIcon(type);
	    }
	    return badgeImage(i);
	}

	@Override
	public Image getOpenedIcon(int type) {
	    Node n = getFolderNode();
	    Image i = super.getOpenedIcon(type);
	    if (n != null) {
		i = n.getOpenedIcon(type);
	    }
	    return badgeImage(i);
	}
	
	private Image badgeImage(Image main) {
	    Image badgeImage = ImageUtilities.loadImage("org/netbeans/modules/xml/schema/ui/nodes/resources/simpleType_badge.png"); // NOI18N
	    return ImageUtilities.mergeImages(main, badgeImage, 8, 8);
	}
	
	public boolean canRename()
	{
		return false;
	}

	public boolean canDestroy()
	{
		return false;
	}

	public boolean canCut()
	{
		return false;
	}

	public boolean canCopy()
	{
		return false;
	}
	
	public static class TypesChildren extends Children.Keys
	{
		TypesChildren(SchemaUIContext context) {
			super();
			this.context = context;
		}
		protected Node[] createNodes(Object key)
		{
			if(key instanceof GlobalSimpleType)
			{
				Node node = context.getFactory().createNode((GlobalSimpleType)key);
				return new Node[] {new TypeNode(node)};
			}
			assert false;
			return new Node[]{};
		}

		protected void addNotify()
		{
			setKeys(SchemaModelFactory.getDefault().getPrimitiveTypesModel().
					getSchema().getSimpleTypes());
		}
		
	    private SchemaUIContext context;
	}
	
	public static class TypeNode extends ReadOnlySchemaComponentNode {
		
		private TypeNode(Node original)
		{
			super(original, Children.LEAF, new InstanceContent());
			final String details = NbBundle.getMessage(
					PrimitiveSimpleTypesNode.class,"MSG_"+getOriginal().getName());
			setShortDescription(details);
		}
		
		public String getHtmlDisplayName()
		{
			return getDefaultDisplayName();
		}
		
	}
	
}
