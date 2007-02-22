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

package org.netbeans.modules.xml.schema.abe.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * This node represents a category node. 
 * @author Chris Webster
 */
public class GlobalContentModelsNode extends AbstractNode {
    
    /** Creates a new instance of ABEDocumentNode */
    public GlobalContentModelsNode(ABEUIContext context, AXIDocument document) {
		super(new ContentModels(context, document));
		setName(NbBundle.getMessage(GlobalContentModelsNode.class,
				"LBL_CategoryNode_GlobalContentModelsNode"));		
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
                    CategorizedChildren.getBadgedFolderIcon(i, GlobalComplexType.class);
        }
        
        public Image getIcon(int i) {
            return org.netbeans.modules.xml.schema.ui.nodes.categorized.
                    CategorizedChildren.getOpenedBadgedFolderIcon(i, GlobalComplexType.class);
        }
	
	private static class ContentModels extends Children.Keys
	{
		ContentModels(ABEUIContext context, AXIDocument document) {
			super();
			this.context = context;
                        this.document = document;
		}
		protected Node[] createNodes(Object key)
		{
			if(key instanceof ContentModel)
			{
				Node node = context.getFactory().createNode(getNode(), (ContentModel)key);
				return new Node[] {node};
			}
			assert false;
			return new Node[]{};
		}

		protected void addNotify()
		{
                    List <ContentModel> list = document.getContentModels();
                    List<ContentModel> list2 = new ArrayList<ContentModel>();
                    for(ContentModel cm : list){
                        if(cm.getType() == ContentModel.ContentModelType.COMPLEX_TYPE)
                            list2.add(cm);
                    }
                    setKeys(list2);
		}
		
	    private AXIDocument document;
	    private ABEUIContext context;
	}
	    
}
