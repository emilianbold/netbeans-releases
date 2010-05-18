/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * NewDiagramType.java
 *
 * Created on March 4, 2005, 4:39 PM
 */

package org.netbeans.modules.uml.project.ui.nodes.actions;

import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.project.ui.nodes.UMLPhysicalViewProvider;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class NewDiagramType extends NewType implements INewTypeExt
{
	private Node node;
	private IElement element = null;
	
	/** Creates a new instance of NewDiagramType */
	public NewDiagramType(Node node)
	{
		this.node = node;
	}
	
	public NewDiagramType(IElement element)
	{
		this.element=element;
	}
	
	/**
	 *
	 *
	 */
	public String getName()
	{
		return NbBundle.getMessage(NewDiagramType.class, 
                      "NewType_Diagram_Name"); // NOI18N
	}
	
	 public String getIconResource()
	{
		return NbBundle.getMessage(NewDiagramType.class, 
                      "DIAGRAM_ICON"); // NOI18N
        }
         
	/**
	 *
	 *
	 */
	public HelpCtx getHelpCtx()
	{
		return HelpCtx.DEFAULT_HELP;
	}
	
	
	public void create() throws java.io.IOException
	{
		boolean search = false;
		if (node==null)
		{
			search = true;
			Project project = ProjectUtil.findReferencingProject(element);
			if (project==null)
				return;
			UMLPhysicalViewProvider provider = (UMLPhysicalViewProvider) project.getLookup().lookup(UMLPhysicalViewProvider.class);
			if (provider!= null)
				node = provider.getModelRootNode();	
		}      
		// code based on legacy code from in JProjectTree.NewElementAction
		IProductDiagramManager pdManager =
				ProductHelper.getProductDiagramManager();
		
		if (pdManager != null)
		{
			if (element==null)
                            element = (IElement)node.getCookie(IElement.class);
			
			if (element != null && element instanceof INamespace)
                        {
                            IDiagram newDiagram =
                                    pdManager.newDiagramDialog(
                                    (INamespace)element,
                                    IDiagramKind.DK_UNKNOWN,
                                    IDiagramKind.DK_ALL,
                                    null);
                            
                            // Fixed issue 95782. When a diagram is 1st created, its dirty state is false.
                            // Set the dirty state to true to have the diagram autosaved.
                            if (newDiagram != null )
                            {
                                newDiagram.setDirty(true);
                                newDiagram.save();
                            }
//				if (newDiagram != null )
//				{
//					newDiagram.save();
//					INamespace space = newDiagram.getNamespace();
//					ITreeItem owner;
//					ETList nodes = UMLProjectModule.getProjectTreeEngine().getTreeModel().findNodes(space);
//					while (nodes.isEmpty())
//					{
//						nodes = UMLProjectModule.getProjectTreeEngine().getTreeModel().findNodes(space.getOwner());
//					}
//					if (!nodes.isEmpty())
//						owner = (ITreeItem)nodes.get(0);
//					else
//						owner = (ITreeItem)node;
//
//					if (owner != null)
//					{
//						UMLProjectModule.getProjectTreeEngine().addOwnedElements(owner, newDiagram);
//						ProjectUtil.findElementInProjectTree(newDiagram);
//					}
//				}
                            
                        }
		}
	}

}
