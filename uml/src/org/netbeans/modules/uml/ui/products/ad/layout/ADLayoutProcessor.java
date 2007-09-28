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



package org.netbeans.modules.uml.ui.products.ad.layout;


import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;
import com.tomsawyer.drawing.TSDGraph;
import com.tomsawyer.editor.TSEGraphManager;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.service.layout.jlayout.TSLayoutInputTailor;
import com.tomsawyer.service.TSServiceInputData;
import com.tomsawyer.service.layout.TSLayoutConstants;
public class ADLayoutProcessor
{

	/**
	 * This constructor may not be invoked by the user.
	 */
	public ADLayoutProcessor()
	{
		this.nodeDataTable = new Hashtable();
	}


	/**
	 * This constructor creates a new layout processor for the
	 * specified graph.
	 */
	public ADLayoutProcessor(TSEGraph graph)
	{
		this();

		if (graph == null)
		{
			throw new IllegalArgumentException("null graph");
		}

		this.graph = graph;
		this.graphManager = null;
	}


	/**
	 * This constructor creates a new layout processor for the
	 * specified graph manager.
	 */
	public ADLayoutProcessor(TSEGraphManager graphManager)
	{
		this();

		if (graphManager == null)
		{
			throw new IllegalArgumentException("null graphManager");
		}

		this.graphManager = graphManager;
		this.graph = null;
	}


	/**
	 * This method preprocesses the graph or graph manager
	 * associated with this processor and prepares for
	 * postprocessing it later.
	 */
	public void preprocess()
	{
            TSServiceInputData inputData = new TSServiceInputData();
            TSLayoutInputTailor layoutInputTailor = new TSLayoutInputTailor(inputData);
            
		if (this.graphManager != null)
		{
			Iterator graphIterator =
				this.graphManager.graphs(false).iterator();

			while (graphIterator.hasNext())
			{
				TSEGraph graph = (TSEGraph) graphIterator.next();
				//String layoutStyle = graph.getLayoutStyle();
				int layoutStyle = layoutInputTailor.getLayoutStyle(graph);                               
				if (this.layoutCanChangeSize(layoutStyle))
				{
					this.preprocess(graph.nodes());
				}
			}
		}
		else
		{
			//String layoutStyle = this.graph.getLayoutStyle();
                        int layoutStyle = layoutInputTailor.getLayoutStyle(this.graph);
		
			if (this.layoutCanChangeSize(layoutStyle))
			{
				this.preprocess(this.graph.nodes());
			}
		}
	}


	/**
	 * This method preprocesses all the nodes in the specififed
	 * list by zeroing their resizability. It also stores their
	 * previous size & resizability for use during
	 * postprocessing.
	 */
	protected void preprocess(List nodeList)
	{
		Iterator nodeIterator = nodeList.iterator();

		while (nodeIterator.hasNext())
		{
			TSENode node = (TSENode)nodeIterator.next();

			if (!node.isExpanded())
			{
				NodeData nodeData = new NodeData();

				nodeData.width = (int)node.getOriginalWidth();
				nodeData.height = (int)node.getOriginalHeight();
				nodeData.resizability = node.getResizability();

				this.nodeDataTable.put(node, nodeData);

				node.setResizability(0);
			}
		}
	}


	/**
	 * This class stores the data needed to postprocess a node.
	 */
	static class NodeData
	{
		int width;
		int height;
		int resizability;
	}


	/**
	 * This method postprocesses all the nodes in the specififed
	 * list by checking them against their pre-layout sizes
	 * and restores their pre-layout resizability if possible.
	 */
	protected void postprocess(List nodeList)
	{
		Iterator nodeIterator = nodeList.iterator();

		while (nodeIterator.hasNext())
		{
			TSENode node = (TSENode)nodeIterator.next();

			if (!node.isExpanded())
			{
				NodeData nodeData =
					(NodeData)this.nodeDataTable.get(node);
				
				if (((int)node.getWidth() == nodeData.width) &&
					((int)node.getHeight() == nodeData.height))
				{
					node.setResizability(nodeData.resizability);
				}
			}
		}
	}


	/**
	 * This method postprocesses the graph or graph manager
	 * associated with this processor and prepares for
	 * postprocessing it later.
	 */
	public void postprocess()
	{
            TSServiceInputData inputData = new TSServiceInputData();
            TSLayoutInputTailor layoutInputTailor = new TSLayoutInputTailor(inputData);
		if (this.graphManager != null)
		{
			Iterator graphIterator =
				this.graphManager.graphs(false).iterator();

			while (graphIterator.hasNext())
			{
				TSEGraph graph = (TSEGraph) graphIterator.next();
				//String layoutStyle = graph.getLayoutStyle();
                                int layoutStyle = layoutInputTailor.getLayoutStyle(graph);
				
				if (this.layoutCanChangeSize(layoutStyle))
				{
					this.postprocess(graph.nodes());
				}
			}
		}
		else
		{
			//String layoutStyle = this.graph.getLayoutStyle();
                        int layoutStyle = layoutInputTailor.getLayoutStyle(this.graph);
                        
			if (this.layoutCanChangeSize(layoutStyle))
			{
				this.postprocess(this.graph.nodes());
			}
		}
	}
	
	
	/**
	 * This method returns true if the layout style passed to it
	 * can change the size of nodes.
	 */
	//private boolean layoutCanChangeSize(String layoutStyle)
        private boolean layoutCanChangeSize(int layoutStyle)
	{
		//return (layoutStyle.equalsIgnoreCase(TSDGraph.HIERARCHICAL) || layoutStyle.equalsIgnoreCase(TSDGraph.ORTHOGONAL));
            return (layoutStyle == TSLayoutConstants.LAYOUT_STYLE_HIERARCHICAL) || (layoutStyle == TSLayoutConstants.LAYOUT_STYLE_ORTHOGONAL);
	}
	

	/**
	 * This variable stores the graphManager that is being processed,
	 * if any.
	 */
	TSEGraphManager graphManager;

	/**
	 * This variable stores the graph that is being processed,
	 * if any.
	 */
	TSEGraph graph;

	/**
	 * This variable stores the data necessary to postprocess all nodes.
	 */
	Hashtable nodeDataTable;
}

