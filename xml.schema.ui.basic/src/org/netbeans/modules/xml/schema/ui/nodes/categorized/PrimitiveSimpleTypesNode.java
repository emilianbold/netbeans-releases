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
	    Image badgeImage = Utilities.loadImage("org/netbeans/modules/xml/schema/ui/nodes/resources/simpleType_badge.png"); // NOI18N
	    return Utilities.mergeImages(main, badgeImage, 8, 8);
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
