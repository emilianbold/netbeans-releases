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

package org.netbeans.modules.xml.schema.abe.nodes;

import java.awt.Image;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.datatype.CustomDatatype;
import org.netbeans.modules.xml.axi.datatype.DatatypeFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;

/**
 *
 * @author Ayub Khan
 */
public class SimpleTypesNode extends AbstractNode
{
	/** Creates a new instance of PrimitiveSimpleTypesNode */
	public SimpleTypesNode(ABEUIContext context, AXIDocument document)
	{
		super(new TypesChildren(context, document));
		setName(NbBundle.getMessage(PrimitiveSimpleTypesNode.class,
				"LBL_CategoryNode_SimpleTypesNode"));
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
	
            public Image getOpenedIcon(int i) {
                return org.netbeans.modules.xml.schema.ui.nodes.categorized.
                        CategorizedChildren.getBadgedFolderIcon(i, GlobalSimpleType.class);
            }
            
            public Image getIcon(int i) {
                return org.netbeans.modules.xml.schema.ui.nodes.categorized.
                        CategorizedChildren.getOpenedBadgedFolderIcon(i, GlobalSimpleType.class);
            }
        
	private static class TypesChildren extends Children.Keys
	{
		TypesChildren(ABEUIContext context, AXIDocument document) {
			super();
			this.context = context;
                        this.document = document;
		}
		protected Node[] createNodes(Object key)
		{
			if(key instanceof GlobalSimpleType)
			{
				CustomDatatype type = 
					(CustomDatatype) DatatypeFactory.getDefault().getDatatype(
						document.getModel(), (GlobalSimpleType)key);
				Node node = context.getFactory().createNode(getNode(), type);
				return new Node[] {node};
			}
			assert false;
			return new Node[]{};
		}

		protected void addNotify()
		{
			setKeys(document.getModel().getSchemaModel().getSchema().getSimpleTypes());
		}
		
	    private AXIDocument document;
	    private ABEUIContext context;
	}
	
}
