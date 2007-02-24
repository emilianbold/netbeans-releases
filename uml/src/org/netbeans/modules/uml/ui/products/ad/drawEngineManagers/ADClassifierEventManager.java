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


package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PresentationHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSGraph;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSConstSize;
import com.tomsawyer.drawing.geometry.TSConstSize;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;

/**
 * @author KevinM
 *
 */
public class ADClassifierEventManager extends ADEventManager implements IADClassifierEventManager {

	public ADClassifierEventManager()
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#onGraphEvent(int)
	 */
	public void onGraphEvent(/* IGraphEventKind */ int nKind) {
		switch(nKind) {
			case IGraphEventKind.GEK_PRE_LAYOUT:
				onPreLayout();
			break;
			case IGraphEventKind.GEK_POST_LAYOUT:
				onPostLayout();
			break;
		}
	}
	
	protected void onPreLayout() {
		if (this.getOwnerNode() != null)
		{
			ETList<TSConstRect> lollypopBounds = hideLollypops();
			swallowBounds(lollypopBounds);
		}
	}
	
	private TSConstSize m_OriginalSize = null;
	private TSPoint m_OriginalCenterDelta = new TSPoint();
	
	private void swallowBounds(ETList<TSConstRect> bounds) {
		try {
			TSENode classNode = getOwnerNode();

	
			m_OriginalSize = classNode.getSize();
			
			TSConstRect newClassRect = new TSConstRect(classNode.getBounds());
			
			IteratorT<TSConstRect> iterator = new IteratorT<TSConstRect>(bounds);
			while(iterator.hasNext()) {
				TSConstRect rect = iterator.next();
				newClassRect = newClassRect.union(rect);	
			}

			m_OriginalCenterDelta.setX(newClassRect.getCenterX()-classNode.getCenterX());
			m_OriginalCenterDelta.setY(newClassRect.getCenterY()-classNode.getCenterY());
			
			classNode.setLocalBounds(newClassRect);
			classNode.setOriginalSize(newClassRect.getSize());
		}
		catch(InvalidArguments e) {
			e.printStackTrace();
		}
	}

	
	protected void onPostLayout() {
		TSENode classNode = getOwnerNode();
		if (classNode != null)
		{
			classNode.setSize(m_OriginalSize);
			classNode.moveBy(0-m_OriginalCenterDelta.getX(),0-m_OriginalCenterDelta.getY());
			showLollypops();	
		}	
	}
	
	private void showLollypops() {
		try {
			TSENode classNode = getOwnerNode();
			if (classNode == null)
			{
				return;
			}
			
			TSConstPoint classCenter = classNode.getCenter();
			TSGraph tsGraph = classNode.getOwnerGraph();	
						
			IteratorT<InterfaceLocation> iterator = new IteratorT<InterfaceLocation>(m_InterfaceLocations);
			
			while(iterator.hasNext()) {
				InterfaceLocation currentLocation = iterator.next();
				TSENode lollypop = currentLocation.getLollypop();
				TSConstPoint delta = currentLocation.getDelta();
			
				tsGraph.insert(lollypop);
				lollypop.setLocalCenter(classCenter.getX() + delta.getX(),
					classCenter.getY()+delta.getY());					
			}
		}
		catch(InvalidArguments e) {
			e.printStackTrace();
		}
	}

	private ETList<TSConstRect> hideLollypops() {
		ETList<TSConstRect> lollypopRects = new ETArrayList<TSConstRect>();
		m_InterfaceLocations.clear();
		try {
			TSENode classNode = getOwnerNode();
			
			if(m_parentETGraphObject == null || classNode == null) {
				return lollypopRects;
			}

			PresentationHelper.LollypopsAndEdges lae = PresentationHelper.getLollypopsWithOneControllingEdge(m_parentETGraphObject);
			
			int numLollypops = lae.getLollypops().size();
			
			if(numLollypops == 0)
				return lollypopRects;

			TSGraph tsGraph = classNode.getOwnerGraph();				
			if(tsGraph != null) {
				IteratorT<IETGraphObject> lollypops = new IteratorT<IETGraphObject>(lae.getLollypops());
				IteratorT<IETGraphObject> edges = new IteratorT<IETGraphObject>(lae.getEdges());
				
				while(lollypops.hasNext()) {
					IETGraphObject lollypop = lollypops.next();									
					IETGraphObject implementationEdge = edges.next();
					
					if(implementationEdge!=null) {
						IEdgePresentation edgePresentation = TypeConversions.getEdgePresentation(implementationEdge);
						if(edgePresentation != null) {
							edgePresentation.discardAllBends();
						}
					}
					if(lollypop!=null) {
						TSENode lollypopNode = TypeConversions.getOwnerNode(lollypop);
						if(lollypopNode != null) {
							TSConstRect lollypopRect = lollypopNode.getBounds();
							
							lollypopRects.add(lollypopRect);
						}
						
						InterfaceLocation currentLocation = new InterfaceLocation(lollypopNode, classNode); 
						m_InterfaceLocations.add(currentLocation);
						
						tsGraph.remove(lollypopNode);			
					}
				}
			}
		}
		catch(InvalidArguments e) {
			e.printStackTrace();
		}
		return lollypopRects;		
	}
	protected class InterfaceLocation {
		public InterfaceLocation(TSENode lollypopNode,TSENode classNode) {
			m_node = lollypopNode;
			
			TSConstPoint lollypopCenter = lollypopNode.getCenter();
			TSConstPoint classCenter = classNode.getCenter();
			
			m_delta = new TSPoint(lollypopCenter.getX()-classCenter.getX(),lollypopCenter.getY()-classCenter.getY());
		}
		public TSConstPoint getDelta() {
			return m_delta;
		}
		public TSENode getLollypop() {
			return m_node;
		}
		private TSENode m_node;
		private TSPoint m_delta;
	}
	
	protected ETList<InterfaceLocation> m_InterfaceLocations = new ETArrayList<InterfaceLocation>(); 
}
