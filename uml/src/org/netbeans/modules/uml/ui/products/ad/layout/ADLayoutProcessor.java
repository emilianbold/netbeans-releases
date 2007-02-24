/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

