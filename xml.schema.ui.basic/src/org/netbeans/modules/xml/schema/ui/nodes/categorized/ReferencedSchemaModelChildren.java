/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.ui.nodes.ReadOnlyCookie;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.xam.Model;

/**
 *
 * @author  Ajit Bhate
 */
public class ReferencedSchemaModelChildren<C extends SchemaModelReference> 
		extends CategorizedChildren<C>
{
	/**
	 *
	 *
	 */
	public ReferencedSchemaModelChildren(SchemaUIContext context,
			SchemaComponentReference<C> reference)
	{
		super(context,reference);
	}
	
	
	@Override
	protected void removeNotify()
	{
		setKeys(Collections.emptyList());
	}
	
	@Override
	protected void addNotify()
	{
		Children.MUTEX.postWriteRequest(new Runnable()
		{
			public void run()
			{
				setKeys(createKeys());
			}
		});
	}
	
	@Override
	public void refreshChildren()
	{
		Children.MUTEX.postWriteRequest(new Runnable()
		{
			public void run()
			{
				setKeys(createKeys());
			}
		});
	}


	/**
	 *
	 *
	 */
	protected List<Node> createKeys()
	{
		List<Node> keys=super.createKeys();
		
		// do not show reference children in readonly context
		ReadOnlyCookie roc = (ReadOnlyCookie) getContext().getLookup().lookup(
				ReadOnlyCookie.class);
		if(roc!=null && roc.isReadOnly()) return keys;

		C component = getReference().get();
		if(component instanceof Import &&
				component.getModel()!=getContext().getModel())
			return keys;
		SchemaModel refModel;
		try
		{
			refModel = component.resolveReferencedModel();
		} catch (CatalogModelException ex)
		{
			refModel = null;
		}
		if (refModel != null && refModel.getRootComponent() != null &&
                refModel.getState() != Model.State.NOT_WELL_FORMED)
		{
			Node refNode = getContext().getFactory().createNode(refModel.getSchema());
			Node dummyNode = new DummySchemaNode(refNode);
			for(Node child:dummyNode.getChildren().getNodes())
			{
				keys.add(new ReadOnlySchemaComponentNode(child));
			}
		}
		else
		{
			AbstractNode node=new AbstractNode(Children.LEAF);
			node.setIconBaseWithExtension(
					"org/netbeans/modules/xml/schema/core/resources/Schema_File.png"); // NOI18N
			node.setDisplayName(
					NbBundle.getMessage(ReferencedSchemaModelChildren.class,
					"LBL_BrokenReference")); // NOI18N
			keys.add(node);
		}
		return keys;
	}
}
