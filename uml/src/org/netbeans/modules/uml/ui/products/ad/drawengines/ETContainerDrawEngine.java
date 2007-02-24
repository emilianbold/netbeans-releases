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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Dimension;
import java.util.List;
import java.util.Iterator;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADZonesCompartment;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.DiagramEngine;
//import com.tomsawyer.editor.TSEGraphChangeEvent;
import com.tomsawyer.graph.event.TSGraphChangeEvent;
//import com.tomsawyer.editor.TSEGraphChangeListener;
import com.tomsawyer.graph.event.TSGraphChangeListener;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.editor.tool.TSEMoveSelectedTool;
import com.tomsawyer.editor.event.TSEViewportChangeListener;
import com.tomsawyer.editor.event.TSEViewportChangeEvent;
import com.tomsawyer.editor.event.TSESelectionChangeListener;
import com.tomsawyer.editor.event.TSESelectionChangeEvent;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine.ETHiddenNodesAndEdges;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
/*
 * 
 * Base class for all container draw engines (nodes that have nodes within it's bounding rectangle)
 */
public class ETContainerDrawEngine extends ADNodeDrawEngine implements IADContainerDrawEngine
{
	// Member variable that allows the graphical containment to be turned "off"
	protected boolean m_isGraphicalContainer = true;

	// When true the container will grow to contain it contained elements
	protected boolean m_maintainContainment = false;

	// Member viariables used to limit the resize

	// Mouse quadrant is the quadrant of the node the user is resizing
	protected int m_mqResize = MouseQuadrantEnum.MQ_UNKNOWN;

	// the minimum allowable rectangle for this container
	protected IETRect m_rectMinimumResize = new ETRect();

	// elements in the container before a GUI operation
	private ETList < IPresentationElement > m_cpPrePEs = new ETArrayList < IPresentationElement > ();

	// The type of this container
	private int m_ContainerType = ContainmentTypeEnum.CT_GRAPHICAL;

	// Nodes and edges hidden during layout
	private ETNodeDrawEngine.ETHiddenNodeList m_hiddenNodes;
	GraphChangeListener graphChangeListener = null;
        
        SelectionChangeListener selectionChangeListener = null;
        ViewportChangeListener viewportChangeListener = null;
        
	private boolean drawContained = false;

	/*
	 * Default constructor.
	 */
	public ETContainerDrawEngine()
	{
		super();		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#init()
	 */
	public void init() throws ETException
	{
		super.init();
		m_isGraphicalContainer = true;
		m_maintainContainment = false;
		m_mqResize = MouseQuadrantEnum.MQ_UNKNOWN;
		m_rectMinimumResize = new ETRect(0, 0, 0, 0);
		m_ContainerType = ContainmentTypeEnum.CT_GRAPHICAL; // graphical is the default from C++

		addGraphChangeListener();
                addSelectionChangeListener();
                addViewportChangeListener();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getElementType()
	 */
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Container");
		}
		return type;
	}

	// Tells the container that it should start containing the argument presentation element
	public void beginContainment(INodePresentation pPreviousContainer, IPresentationElement pPresentationElement)
	{
		processContainment(pPreviousContainer, pPresentationElement);
			
		//PostDelayedAction( SPAK_MOVEBEHINDCONTAINED );
		moveInBackOf(pPresentationElement);
	}

	// Tells the container that it should start containing the argument presentation elements
	public void beginContainment(INodePresentation pPreviousContainer, ETList < IPresentationElement > pPresentationElements)
	{
		if (pPresentationElements != null)
		{
			Iterator iter = pPresentationElements.iterator();

			while (iter.hasNext())
			{
				IPresentationElement cpPE = (IPresentationElement) iter.next();
				this.processContainment(pPreviousContainer, cpPE);
			}

			//PostDelayedAction( SPAK_MOVEBEHINDCONTAINED );
			moveInBackOf(pPresentationElements);
		}
	}

	// Tells the container that it should stop containing the argument presentation elements
	public void endContainment(ETList < IPresentationElement > pPresentationElements)
	{
		boolean isContainerType = false;

		// The following is some really goofy code carried over from C++
		if (pPresentationElements == null)
		{
			return;
		}

		if (isContainerType(ContainmentTypeEnum.CT_NAMESPACE))
		{
			isContainerType = true;

			Iterator < IPresentationElement > iter = pPresentationElements.iterator();
			while (iter.hasNext())
			{
				IPresentationElement pPE = iter.next();

				if (pPE != null)
				{
					// Move the objects to the namespace of the diagram
					IDiagram cpDiagram = this.getDiagram();

					if (cpDiagram != null)
					{
						INamespace cpNamespace = cpDiagram.getNamespaceForCreatedElements();

						if (cpNamespace != null)
						{
							IElement cpElement = TypeConversions.getElement(pPE);

							INamedElement cpNamedElement;

							if (cpElement instanceof INamedElement && !(cpElement instanceof IPort))
							{
								cpNamedElement = (INamedElement) cpElement;
							}
							else
							{
								cpNamedElement = null;
							}

							if (cpNamedElement != null)
							{
								cpNamedElement.setNamespace(cpNamespace);
							}
						}
					}
				}
			}
		}

		if (isContainerType(ContainmentTypeEnum.CT_STATE_REGION))
		{
			isContainerType = true;

			Iterator < IPresentationElement > iter = pPresentationElements.iterator();
			while (iter.hasNext())
			{
				IPresentationElement pPE = iter.next();

				// Move the objects to the namespace of the diagram
				IDiagram cpDiagram = this.getDiagram();

				if (cpDiagram != null)
				{
					INamespace pNamespace = cpDiagram.getNamespaceForCreatedElements();

					// This state diagram should only be on a statemachine whose
					// namespace for newly created elements is an IRegion

					IRegion pRegion = pNamespace instanceof IRegion ? (IRegion) pNamespace : null;

					if (pRegion != null)
					{
						IElement pFirstSubject = pPE.getFirstSubject();

						if (pFirstSubject instanceof IStateVertex)
						{
							IStateVertex pFirstSubjectAsVertex = (IStateVertex) pFirstSubject;
							pFirstSubjectAsVertex.setContainer(pRegion);
						}
					}
				}
			}
		}

		if (isContainerType(ContainmentTypeEnum.CT_ACTIVITYGROUP))
		{
			isContainerType = true;

			Iterator < IPresentationElement > iter = pPresentationElements.iterator();
			while (iter.hasNext())
			{
				IElement cpElement = TypeConversions.getElement(iter.next());
				if (cpElement instanceof IActivityNode)
				{
					// Fix W6340:  For now, graphically we only support the node being in one group, so
					// Remove the node from all its groups
					removeActivityGroups((IActivityNode) cpElement);
				}
			}
		}

		//		UPDATE we need a new container type for simple owned elements //)
		if (!isContainerType)
		{
			int count = pPresentationElements.size(); // Move the objects to the namespace of the diagram
			IDiagram cpDiagram = this.getDiagram();
			if (cpDiagram != null)
			{
				INamespace cpNamespace = cpDiagram.getNamespaceForCreatedElements();
				if (cpNamespace != null)
				{
					for (int i = 0; i < count; i++)
					{
						IPresentationElement pPE = pPresentationElements.get(i);
						if (pPE != null)
						{
							IElement cpElement = TypeConversions.getElement(pPE);
							if (cpElement instanceof ICombinedFragment)
							{
								ICombinedFragment cpCF = (ICombinedFragment) cpElement;
								cpCF.setOwner(cpNamespace);
							}
						}
					}
				}
			}
		}

	}

	private TSConstRect buildMinBoundingRect(List pPresentationElements)
	{
		TSConstRect retValue = null;
		if (pPresentationElements != null)
		{
			Iterator < IPresentationElement > peIterator = pPresentationElements.iterator();
			while (peIterator.hasNext())
			{
				IPresentationElement pe = peIterator.next();
				if (pe instanceof INodePresentation)
				{
					TSENode element = ((INodePresentation) pe).getTSNode();
					if (retValue == null)
					{
						retValue = element.getBounds();
					}
					else
					{
						retValue = retValue.union(element.getBounds());
					}
				}
			}
		}
		return retValue;
	}

	// Retrieve the presentation elements contained within this container node
	public ETList < IPresentationElement > getContained()
	{
		return getContained(false, false);
	}

	public ETList < IPresentationElement > getDeepContained()
	{
		return getContained(false, true);
	}

	/**
	* This helper function gets at least one presentation element that is
	* graphically contained with this container node.
	* If bNeedOnlyOne is true then pPresentationElements is allowed to be NULL.
	* This is used in the case when only looking to determine if there are any number of contained nodes.
	*/
        //Fix for Bug#6327072
	protected ETList < IPresentationElement > getContained(boolean bNeedOnlyOne, boolean deep)
        {
            return getContained(false, true, true);
        }
        
        public ETList < IPresentationElement > getContained(boolean bNeedOnlyOne, 
                                                               boolean deep,
                                                               boolean verify)
	{
		ETList < IPresentationElement > pPresentationElements = new ETArrayList < IPresentationElement > ();

		if (DiagramEngine.isContainmentOK())
		{
			if (m_isGraphicalContainer)
			{
				INodePresentation cpThisNodePE = TypeConversions.getNodePresentation(this);
				if (cpThisNodePE != null)
				{
					// Find all the presentation elements inside this draw engine's bounding rectangle
					//	IZ 80339, getContained() returns elements *fully* contained in the container element,
					//	in combined fragment case, lifeline only touches the boundry, use below utility to move
					//  container to the back of the stack, same change applies to handlePostCreate()
					ETList < IPresentationElement > cpOtherPEsInThisBoundingRect=null;
					
					if (getElementType().equals("CombinedFragment"))
						cpOtherPEsInThisBoundingRect = cpThisNodePE.getPEsViaBoundingRect(false);
					else
						cpOtherPEsInThisBoundingRect = cpThisNodePE.getPEsViaBoundingRect(false);
                                        
					if (cpOtherPEsInThisBoundingRect != null)
					{
						if (!deep)
						{
							removeDeepContained(cpOtherPEsInThisBoundingRect);
						}

						// Since containment is graphical we call Verify to make sure that
						// the contain PE's are actually contained according to the metamodel
                                                if(verify == true)
                                                {
                                                    verifyContainment(cpOtherPEsInThisBoundingRect);
                                                }
                                                pPresentationElements.addAll(cpOtherPEsInThisBoundingRect);
                                        
                                        }
                                }
			}
		}

		return pPresentationElements;
	}

   protected ETList < IADContainerDrawEngine > getNestedContainers(ETList < IPresentationElement > pPresentationElements)
   {
      ETList < IADContainerDrawEngine > nestedContainers = new ETArrayList < IADContainerDrawEngine > ();
      IETRect worldBounds = this.getLogicalBoundingRect(false);
      if (worldBounds != null)
      {
         IteratorT < IPresentationElement > iter = new IteratorT < IPresentationElement > (pPresentationElements);
         while (iter.hasNext())
         {
            IDrawEngine de = TypeConversions.getDrawEngine(iter.next());

            if (de instanceof IADContainerDrawEngine)
            {
               if (de == this)
               {
                  continue;
               }
               else if (worldBounds.equals((Object)de.getLogicalBoundingRect(false)))
               {
                  // We need a node pe.
                  if (getNodePresentation() == null)
                     continue;

                  // We have two equal sized containers on top of each other
                  List nodes = this.getGraphWindow().getGraph().nodes();

                  // See whos first, to determine the containment.
                  int thisIndex = nodes.indexOf((Object)this.getParentETElement());
                  int deIndex = nodes.indexOf((Object)de.getParentETElement());

                  if (thisIndex > deIndex)
                  {
                     // He is before us in the drawing order so he contains us, make ourselves smaller.                                    
                     this.getNodePresentation().resize(worldBounds.getWidth() - 5.0, worldBounds.getHeight() - 5.0, false);
                     continue;
                  }
                  else
                  {
                     // We are before him the in drawing order make ourselves a bit bigger.
                     this.getNodePresentation().resize(worldBounds.getWidth() + 5.0, worldBounds.getHeight() + 5.0, false);
                  }
               }
               nestedContainers.add((IADContainerDrawEngine)de);
            }
         }
      }

      return nestedContainers;
   }

   /*
    * Removes any nested containers contained objects.
    */
   protected void removeDeepContained(ETList < IPresentationElement > pPresentationElements)
   {
      // See if we have any nested containers, then remove their contained elements.
      IteratorT < IADContainerDrawEngine > iter = new IteratorT < IADContainerDrawEngine > (getNestedContainers(pPresentationElements));
      while (iter.hasNext())
      {
         IADContainerDrawEngine container = iter.next();
         // Double check we don't return ourselves as a nested container.
         if (container == this || container == null)
         {
            // This can happen if we equal size continers are on top of each other.
            pPresentationElements.remove(this);
            continue;
         }

         ETList < IPresentationElement > nestedContained = container.getContained();
         if (nestedContained != null)
         {
            pPresentationElements.removeThese(nestedContained);
         }
      }
   }

	// The type of container this guy is. 
	public void setContainmentType(int nContainmentType)
	{
		m_ContainerType = nContainmentType;
	}

	// The type of container this guy is
	public int getContainmentType()
	{
		return m_ContainerType;
	}

	// Allows you to turn on and off the containment mechanism of this node
	public boolean getIsGraphicalContainer()
	{
		return m_isGraphicalContainer;
	}

	public void setIsGraphicalContainer(boolean pNewValue)
	{
		m_isGraphicalContainer = pNewValue;
	}

	/**
	* Returns the model element for the draw engine, or contianer containing the input presentation element
	*/
	public IElement getContainingModelElement(IPresentationElement pPresentationElement)
	{
		IElement ppElement = null;
		// For now just get the model element from the draw engine.
		//return getFirstModelElement();
		IElement cpElement = null;
		// This is a special case which we know the zones compartment can process
		// See if the presentation element is contained in one of the zone compartments
		IADZonesCompartment cpZones = getCompartmentByKind(IADZonesCompartment.class);
		if (cpZones != null)
		{
			ETList < ICompartment > cpCompartments = cpZones.getCompartments();
			if (cpCompartments != null)
			{
				int lCnt = cpCompartments.size();
				IETRect rectPE = TypeConversions.getLogicalBoundingRect(pPresentationElement);
				if (rectPE != null)
				{
					for (int lIndx = 0; lIndx < lCnt; lIndx++)
					{
						ICompartment cpCompartment = cpCompartments.get(lIndx);
						if (cpCompartment != null)
						{
							IETRect rectCompartment = TypeConversions.getLogicalBoundingRect(cpCompartment);
							if (rectCompartment.contains(rectPE))
							{
								cpElement = cpCompartment.getModelElement();
								break;
							}
						}
					}
				}
			}
		}

		if (cpElement != null)
		{
			ppElement = cpElement;
		}
		else
		{ // Didn't find a compartment containing the presentation element,
			// so use the Draw Engine's model element
			ppElement = getFirstModelElement();
		}

		return ppElement;
	}

	/**
	* Handle the post create graph event
	* by ensuring all graphically contained nodes are moved in front of this container.
	*/
	protected void handlePostCreate()
	{
		moveInBackOf(getAllContainedPES());
		this.postInvalidate();
	}

	/*
	 * Returns a list of edges attached to pNode that are full contained to another node within this containor. 
	 */
	protected ETList < IPresentationElement > getContainedConnectedEdges(IETNode pNode)
	{
		if (pNode == null)
			return null;

		ETList < IPresentationElement > containedEdges = new ETArrayList < IPresentationElement > ();
		ETList < IPresentationElement > cpContainedPEs = getContained();
		IteratorT < IETEdge > edgeIter = new IteratorT < IETEdge > (pNode.getEdges());
		while (edgeIter.hasNext())
		{
			// Make sure both ends are within this containor.
			IETEdge pEdge = edgeIter.next();
			if (cpContainedPEs.find(pEdge.getFromNode().getPresentationElement()) && cpContainedPEs.find(pEdge.getToNode().getPresentationElement()))
			{
				containedEdges.add(pEdge.getPresentationElement());
			}
		}
		return containedEdges;
	}

	protected void onPreMoveSelect(IPresentationElement cpPE)
	{
		IETGraphObject pObject = TypeConversions.getETGraphObject(cpPE);
		if (pObject != null && m_cpPrePEs != null && !m_cpPrePEs.find(cpPE))
		{
			if (pObject.isNode())
			{
				// Now select any fully contained edges.
				ETList < IPresentationElement > containedEdges = getContainedConnectedEdges((IETNode) pObject);
				dispatchPreMoveEvents(containedEdges);
			}

			if (!pObject.isSelected())
			{
				// Select and remember that we selected
				// We use the low-level routine to avoid sending the events.
				// 20021120:  Sending the events was problematic when a deep sync was performed on a class
				pObject.setSelected(true);

				//Note: New fix send by Pat from 6.2+
				// Send a pre move event so it selects any items it contains
				IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(cpPE);

				if (pDrawEngine != null)
				{
					pDrawEngine.onGraphEvent(IGraphEventKind.GEK_PRE_MOVE);
				}

				m_cpPrePEs.add(cpPE);
			}
		}
	}

	/*
	 * Calls onPreMovseSelected for each cpContained presentation element.
	 */
	protected void dispatchPreMoveEvents(ETList < IPresentationElement > cpContainedPEs)
	{
		if (cpContainedPEs != null)
		{
			Iterator < IPresentationElement > iterator = cpContainedPEs.iterator();
			while (iterator.hasNext())
			{
				onPreMoveSelect(iterator.next());
			}
		}
	}

	/**
	* Handle the pre move graph event
	* by selecting all the nodes that are not already selected, and
	* remembering which nodes needed to be selected so they can be deselected in HandlePostMove()
	*/
	protected void handlePreMove()
	{
		// recreate the list.
		m_cpPrePEs = new ETArrayList < IPresentationElement > ();
		dispatchPreMoveEvents(this.getDeepContained());
	}

	/**
	* Handle the post move graph event
	* by deselecting all the nodes that were selected in HandlePreMove()
	*/
	protected void handlePostMove()
	{
		if (m_cpPrePEs != null)
		{
			Iterator < IPresentationElement > iterator = m_cpPrePEs.iterator();
			while (iterator.hasNext())
			{
				IETGraphObject pObject = TypeConversions.getETGraphObject(iterator.next());

				if (pObject != null)
				{
					pObject.invalidate();
					pObject.setSelected(false);
				}
			}
			m_cpPrePEs.clear();
		}
		// Now update the screen, we have changed the selection list.
		this.getDrawingArea().refresh(true);
		m_cpPrePEs = null;
	}

	/**
	* Handle the pre resize graph event
	* by determining the minimum resize rectangle for the resize operation,
	* which is the rectangle that encompasses all the graphically contained nodes.
	* This rectangle is used in validateResize() to restrict the user from resizing
	* this node smaller than the contained nodes.
	*
	* Also, the quadrant being resized is determined.  This quadrant value is used in
	* validateResize() to determine the proper size to be returned.
	*
	* @see ADContainerDrawEngineImpl.handlePostResize()
	* @see ADContainerDrawEngineImpl.validateResize()
	*/
	void handlePreResize()
	{
		IETNode pNode = this.getOwnerNode() instanceof IETNode ? (IETNode) getOwnerNode() : null;
		if (pNode != null)
		{
			// needed by the component with attached interfaces.
			pNode.invalidateEdges();
		}

		m_rectMinimumResize.setRectEmpty();
		m_cpPrePEs = getContained();

		if (m_cpPrePEs != null)
		{
			m_rectMinimumResize = TypeConversions.getLogicalBoundingRect(m_cpPrePEs, false);
			if (m_rectMinimumResize != null && !m_rectMinimumResize.isZero())
			{
				// Give a little room for rounding errors.
				m_rectMinimumResize.inflate(2);
			}
		}

		// All we are really trying to do in the C++ code is determine what quadrant
		// is being used for the resize.  TS has this available on the state so we'll
		// use that capabaility here.

		m_mqResize = MouseQuadrantEnum.getQuadrant(getGraphWindow().getCurrentTool());

		// From C++
		//		m_rectMinimumResize.setZero();
		//
		//		m_cpPrePEs = NULL;
		//		_VH( get_Contained( &m_cpPrePEs ));
		//		if( m_cpPrePEs != NULL )
		//		{
		//			m_rectMinimumResize = TSRect( CTypeConversions::GetLogicalBoundingRect( m_cpPrePEs ));
		//		}
		//
		//		// Determine which quadrant is being resized
		//		const CPoint ptWinScaledOwnerCursorPosition = GetTransform().GetWinScaledOwnerCursorPosition();
		//		const CRect rectWinScaledOwnerClient = GetTransform().GetWinScaledOwnerRect();
		//		const CPoint ptCenterWinScaledOwner = GetTransform().GetWinScaledOwnerRect().CenterPoint();
		//
		//		// The quadrant "locaton" is based on the TS logical coordinates using the node's center
		//		long lQuadrant = MQ_UNKNOWN;
		//		lQuadrant |= (ptWinScaledOwnerCursorPosition.y < ptCenterWinScaledOwner.y) ? MQ_TOP  : MQ_BOTTOM;
		//		lQuadrant |= (ptWinScaledOwnerCursorPosition.x < ptCenterWinScaledOwner.x) ? MQ_LEFT : MQ_RIGHT;
		//		m_mqResize = (MouseQuadrant)lQuadrant;
		//
		//		ATLASSERT( (m_mqResize == MQ_TOPLEFT) ||
		//					  (m_mqResize == MQ_TOPRIGHT) ||
		//					  (m_mqResize == MQ_BOTTOMLEFT) ||
		//					  (m_mqResize == MQ_BOTTOMRIGHT) );
	}

	/**
	* Handle the pre resize graph event
	* by ensuring all graphically contained nodes are moved in front of this container, and
	* ensuring that they are promoted to the proper metadata containment.
	*/
	protected void handlePostResize()
	{
		ETList < IPresentationElement > cpPEs = getContained();

		moveInBackOf(cpPEs);

		// Ensure all graphically contained elements are promoted to the proper metadata containment.
		this.beginContainment(null, cpPEs);

	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine#createHiddenList()
	 */
	protected ETNodeDrawEngine.ETHiddenNodeList createHiddenList(ETList < IPresentationElement > pPES)
	{
		return new ETHiddenNodesAndEdges(pPES, true);
	}

	/**
	 * Handle the pre layout graph event
	 * by remembering all the contained nodes, and hiding them.
	 * Hiding the contained nodes keeps TS from laying them out.
	 * Also, this container's center point is remembered, so the contained nodes can me moved back
	 * into the container in HandlePostLayout().
	 */
	protected void handlePreLayout()
	{
		ETList < IPresentationElement > containedPEs = this.getDeepContained();

		if (containedPEs != null)
		{
			m_hiddenNodes = this.createHiddenList(containedPEs);
			if (m_hiddenNodes != null)
				m_hiddenNodes.hide();
		}

	}

	/**
	* Handle the post layout graph event
	* by moving all the remembered contained nodes from HandlePreLayout() to be
	* contained by this container, and making the contained nodes visible again.
	*/
	protected void handlePostLayout()
	{
		if (m_hiddenNodes != null)
		{
			m_hiddenNodes.unHide();
			m_hiddenNodes = null;
		}
	}

	/**
	* Verify that the contained items are contained according to the metamodel
	*/
	protected void verifyContainment(ETList < IPresentationElement > pPresentationElements)
	{
		if (pPresentationElements != null)
		{
			if (isContainerType(ContainmentTypeEnum.CT_NAMESPACE))
			{ // Nothing Yet
			}

			if (isContainerType(ContainmentTypeEnum.CT_STATE_REGION))
			{
				verifyStateRegionContainment(pPresentationElements);
			}

			if (isContainerType(ContainmentTypeEnum.CT_ACTIVITYGROUP))
			{
				verifyActivityGroupContainment(pPresentationElements);
			}
		}
	}

	/**
	* Move this container behind the input presentation element
	*/
	protected void moveInBackOf(IPresentationElement pPresentationElement)
	{
		if (pPresentationElement != null)
		{
			ETList < IPresentationElement > pes = new ETArrayList < IPresentationElement > ();
			pes.add(pPresentationElement);
			moveInBackOf(pes);
		}
	}

	protected ETList < IETNode > getNodesAfter(int index)
	{
		ETList < IETNode > nodes = new ETArrayList < IETNode > ();
		IteratorT < IETNode > iter = new IteratorT < IETNode > ((List) null);
		iter.reset(this.getGraphWindow().getGraph().nodes(), index);
		while (iter.hasNext())
		{
			nodes.add(iter.next());
		}
		return nodes;
	}

	/*
	 * 
	 * @author KevinM
	 *
	 * Stores data about a nodes state before it gets moved in the z order.
	 * Its used by the drawing order (Stacking) methods.
	 */
	class RemovedNodeData
	{
		public RemovedNodeData(IETNode pNode)
		{
			node = pNode;
			wasSelected = pNode != null ? pNode.isSelected() : false;
		}

		public IETNode node;
		public boolean wasSelected = false;
	}

	protected ETList < RemovedNodeData > removeThese(ETList < IETNode > pNodes)
	{
		ETList < RemovedNodeData > hiddenNodes = new ETArrayList < RemovedNodeData > ();
		IteratorT < IETNode > iter = new IteratorT < IETNode > (pNodes);
		ETGraph graph = (ETGraph) this.getGraphWindow().getGraph();
		while (iter.hasNext())
		{
			IETNode node = iter.next();
			if (node != this.getOwnerNode())
			{
				hiddenNodes.add(new RemovedNodeData(node));
				graph.remove((TSNode) node);
			}
		}
		return hiddenNodes;
	}

	protected void addThese(ETList < RemovedNodeData > pHiddenNodes)
	{
		IteratorT < RemovedNodeData > iter = new IteratorT < RemovedNodeData > (pHiddenNodes);
		ETGraph graph = (ETGraph) this.getGraphWindow().getGraph();
		while (iter.hasNext())
		{
			RemovedNodeData data = iter.next();
			graph.insert((TSNode) data.node);
			if (data.wasSelected)
			{
				data.node.setSelected(true);
			}
		}
	}

	/**
	 * Move this container behind the input presentation elements
	 */
	protected void moveInBackOf(ETList < IPresentationElement > pPresentationElements)
	{
		if (pPresentationElements != null && this.getDrawingArea() != null && getGraphWindow() != null)
		{
			ETGraph graph = (ETGraph) this.getGraphWindow().getGraph();
			int index = graph.nodes().indexOf(this.getOwnerNode());
			IteratorT < IGraphPresentation > iter = new IteratorT < IGraphPresentation > (pPresentationElements);
			boolean somethingMoved = false;
			while (iter.hasNext())
			{
				IGraphPresentation presentation = iter.next();
				if (presentation == null)
					continue;

				IETGraphObject object = presentation.getETGraphObject();
				if (object != null && object.isNode())
				{
					int indexOfObject = graph.nodes().indexOf(object);
					if (indexOfObject < index)
					{
						// We need to preserve the drawing order, so we remove everything 
						// after the contained element, and move the contained element
						// after continers owner, and reinsert the rest.
						ETList < IETNode > etnodes = getNodesAfter(indexOfObject + 1);
						ETList < RemovedNodeData > removedData = removeThese(etnodes);

						somethingMoved = true;
						boolean wasSelected = object.isSelected();
						graph.remove((TSNode) object);
						graph.insert((TSNode) object);
						if (wasSelected)
						{
							object.setSelected(true);
						}

						addThese(removedData);
					}
				}
			}

			//if (somethingMoved)
			{
				invalidateContained();
			}
		}
	}

	/**
	 * Change the metadata containment of the input element
	 *
	 * pPresentationElement: The element to be contained by this container
	 */
	protected void processContainment(INodePresentation pPreviousContainer, IPresentationElement pPE)
	{
		boolean isContainerType = false;

		//The following is some really goofy code carried over from C++
		if (pPE != null)
		{
			IElement cpContainingElement = this.getContainingModelElement(pPE);
			IElement cpElement = TypeConversions.getElement(pPE);

			if (cpContainingElement != null && cpElement != null)
			{
				if (isContainerType(ContainmentTypeEnum.CT_NAMESPACE))
				{
					isContainerType = true;
					INamespace pThisNamespace = (cpContainingElement instanceof INamespace) ? (INamespace) cpContainingElement : null;
					INamedElement pNewNamedElement = null;

					// Ports and AssociationEnds are not allowed to be contained.
					if (cpElement instanceof INamedElement && !(cpElement instanceof IPort || cpElement instanceof IAssociationEnd))
					{
						pNewNamedElement = (INamedElement) cpElement;
					}

					if (pThisNamespace != null && pNewNamedElement != null && !pNewNamedElement.isSame(pThisNamespace))
					{
						// Make sure we don't try to contain our own namespace.
						// org.dom4j.tree.DefaultElement.getDocument(DefaultElement.java:121)
						if(Util.hasNameCollision(pThisNamespace, pNewNamedElement.getName(), 
								pNewNamedElement.getElementType(), pNewNamedElement))
						{
						   DialogDisplayer.getDefault().notify(
								new NotifyDescriptor.Message(NbBundle.getMessage(
									DiagramEngine.class, "IDS_NAMESPACECOLLISION")));
						   return;
						}
						pNewNamedElement.setNamespace(pThisNamespace);
					}
				}

				if (isContainerType(ContainmentTypeEnum.CT_STATE_REGION))
				{
					isContainerType = true;
					// Get the compartment that contains the model element
					IElement cpElement1 = getContainingModelElement(pPE);

					IRegion cpNewRegion = (cpElement1 instanceof IRegion) ? (IRegion) cpElement1 : null;

					if (cpNewRegion != null)
					{
						IElement pFirstSubject = pPE.getFirstSubject();

						IStateVertex pFirstSubjectAsVertex = (pFirstSubject instanceof IStateVertex) ? (IStateVertex) pFirstSubject : null;

						if (pFirstSubjectAsVertex != null)
						{
							pFirstSubjectAsVertex.setContainer(cpNewRegion);
						}
					}
				}

				if (isContainerType(ContainmentTypeEnum.CT_ACTIVITYGROUP))
				{
					isContainerType = true;
					IActivityGroup cpActivityGroup = (cpContainingElement instanceof IActivityGroup) ? (IActivityGroup) cpContainingElement : null;

					IActivityNode cpActivityNode = (cpElement instanceof IActivityNode) ? (IActivityNode) cpElement : null;

					if (cpActivityGroup != null && cpActivityNode != null)
					{
						// Fix W6340:  For now, graphically we only support the node being in one group, so
						// Remove the node from all its groups
						removeActivityGroups(cpActivityNode);

						cpActivityGroup.addNodeContent(cpActivityNode);
					}
				}

				// UPDATE we need a new container type for simple owned elements
				if (!isContainerType)
				{
					IInteractionOperand cpContianingCF = (cpContainingElement instanceof IInteractionOperand) ? (IInteractionOperand) cpContainingElement : null;
					ICombinedFragment cpCF = (cpElement instanceof ICombinedFragment) ? (ICombinedFragment) cpElement : null;

					if (cpContianingCF != null && cpCF != null)
					{
						cpElement.setOwner(cpContainingElement);
					}
				}
			}
		}
	}

	/**
	  * Is this the indicated type of container
	  *
	  * @param nType [in] The type of container to query
	  * @return true if this container is of type nType.
	  */
	protected boolean isContainerType(int nType)
	{
		return (m_ContainerType & nType) == nType;
	}

	//	 Returns true when there are contained presentation elements
	public boolean hasContained()
	{
		ETList < IPresentationElement > containedPEs = this.getContained();
		return containedPEs != null && containedPEs.size() > 0;
	}

	// Populates this container with what it's contents should be
	public boolean populate()
	{
		return false;
	}

	/**
	* Indicate to the draw engine that it is being stretched.
	*
	* @param pStretchContext [in] Information about the stretch
	*/
	public void stretch(IStretchContext stretchContext)
	{
		if (null == stretchContext)
			throw new IllegalArgumentException();

		IADZonesCompartment zonesCompartment = getCompartmentByKind(IADZonesCompartment.class);
		if (zonesCompartment != null)
		{
			zonesCompartment.stretch(stretchContext);
		}
		else
		{
			super.stretch(stretchContext);
		}
	}

	public void onGraphEvent(int nKind)
	{
		super.onGraphEvent(nKind);
		if (m_isGraphicalContainer)
		{
			switch (nKind)
			{
				case IGraphEventKind.GEK_POST_CREATE :
					handlePostCreate();
					ETSystem.out.println("ETContainerDrawEngine.onGraphEvent(GEK_POST_CREATE)");
					break;

				case IGraphEventKind.GEK_PRE_MOVE :
					handlePreMove();
					ETSystem.out.println("ETContainerDrawEngine.onGraphEvent(GEK_PRE_MOVE)");
					break;

				case IGraphEventKind.GEK_POST_MOVE :
					handlePostMove();
					ETSystem.out.println("ETContainerDrawEngine.onGraphEvent(GEK_POST_MOVE)");
					break;

				case IGraphEventKind.GEK_PRE_RESIZE :
					handlePreResize();
					ETSystem.out.println("ETContainerDrawEngine.onGraphEvent(GEK_PRE_RESIZE)");
					break;

				case IGraphEventKind.GEK_POST_RESIZE :
					handlePostResize();
					ETSystem.out.println("ETContainerDrawEngine.onGraphEvent(GEK_POST_RESIZE)");
					break;

				case IGraphEventKind.GEK_PRE_LAYOUT :
					handlePreLayout();
					ETSystem.out.println("ETContainerDrawEngine.onGraphEvent(GEK_PRE_LAYOUT)");
					break;

				case IGraphEventKind.GEK_POST_LAYOUT :
					handlePostLayout();
					ETSystem.out.println("ETContainerDrawEngine.onGraphEvent(GEK_POST_LAYOUT)");
					break;
			}
		}

		// Code to ensure resizing only changes the 
		if (IGraphEventKind.GEK_PRE_RESIZE == nKind)
		{
			IADZonesCompartment zonesCompartment = getCompartmentByKind(IADZonesCompartment.class);
			if (zonesCompartment != null)
			{
				IETRect rectMin = zonesCompartment.getMinimumRect();

				if (m_rectMinimumResize.isZero())
				{
					m_rectMinimumResize = rectMin;
				}
				else
				{
					// TS does not support the just one dimension being "don't care"
					// The rectangle coming back from IADZonesCompartment.get_MinimumRect() contains
					// one either the horizontal or vertical "don't care what size it is", so
					// we have to recognize that and not process those sides.

					IETRect rectNewMinResize = (IETRect) m_rectMinimumResize.clone();
					if ((rectMin.getLeft() != 0) || (rectMin.getRight() != 0))
					{
						rectNewMinResize.setLeft(Math.min(m_rectMinimumResize.getLeft(), rectMin.getLeft()));
						rectNewMinResize.setRight(Math.max(m_rectMinimumResize.getRight(), rectMin.getRight()));
					}
					if ((rectMin.getTop() != 0) || (rectMin.getBottom() != 0))
					{
						rectNewMinResize.setTop(Math.max(m_rectMinimumResize.getTop(), rectMin.getTop()));
						rectNewMinResize.setBottom(Math.min(m_rectMinimumResize.getBottom(), rectMin.getBottom()));
					}
					m_rectMinimumResize = rectNewMinResize;
				}
			}
		}
	}

	public Dimension validateResize(int x, int y)
	{
		// We can at least make sure the size does not get too small
		if (!m_rectMinimumResize.isZero())
		{
			// The m_rectMinimumResize is the minimum allowable rectangle,
			// so don't allow any of the sides to end up inside this rectangle

			IETRect rectCurrent = getLogicalBoundingRect();

			if ((m_rectMinimumResize.getLeft() != 0) || (m_rectMinimumResize.getRight() != 0))
			{
				if (MouseQuadrantEnum.MQ_LEFT == (m_mqResize & MouseQuadrantEnum.MQ_LEFT))
				{
					final int iLeft = m_rectMinimumResize.getLeft();
					final int iRight = rectCurrent.getRight();
					x = Math.max(iRight - iLeft, x);
				}
				else if(MouseQuadrantEnum.MQ_RIGHT == (m_mqResize & MouseQuadrantEnum.MQ_RIGHT))
				{
					assert(MouseQuadrantEnum.MQ_RIGHT == (m_mqResize & MouseQuadrantEnum.MQ_RIGHT));

					final int iLeft = rectCurrent.getLeft();
					final int iRight = m_rectMinimumResize.getRight();
					x = Math.max(iRight - iLeft, x);
				}

				assert(x > 0);
			}

			if ((m_rectMinimumResize.getTop() != 0) || (m_rectMinimumResize.getBottom() != 0))
			{
				if (MouseQuadrantEnum.MQ_TOP == (m_mqResize & MouseQuadrantEnum.MQ_TOP))
				{
					final int iTop = m_rectMinimumResize.getTop();
					final int iBottom = rectCurrent.getBottom();
					y = Math.max(iTop - iBottom, y);
				}
				else if(MouseQuadrantEnum.MQ_BOTTOM == (m_mqResize & MouseQuadrantEnum.MQ_BOTTOM))
				{
					assert(MouseQuadrantEnum.MQ_BOTTOM == (m_mqResize & MouseQuadrantEnum.MQ_BOTTOM));

					final int iTop = rectCurrent.getTop();
					final int iBottom = m_rectMinimumResize.getBottom();
					y = Math.max(iTop - iBottom, y);
				}

				assert(y > 0);
			}
		}

		return new Dimension(x, y);
	}

	/**
	 * Remove all the associated activity groups from this activity node
	 */
	public void removeActivityGroups(IActivityNode pActivityNode)
	{

		// Fix W6340:  For now, graphically we only support the node being in one group, so
		// Remove the node from all its groups

		ETList < IActivityGroup > cpActivityGroups = pActivityNode.getGroups();

		if (cpActivityGroups != null)
		{
			int lCnt = cpActivityGroups.size();

			for (int lIndx = 0; lIndx < lCnt; lIndx++)
			{
				IActivityGroup cpGroup = cpActivityGroups.get(lIndx);

				if (cpGroup != null)
				{
					pActivityNode.removeGroup(cpGroup);
				}
			}
		}
	}

	/**
	 * Verifies containment according to state region rules
	 */
	protected void verifyStateRegionContainment(ETList < IPresentationElement > pPresentationElements)
	{

		if (pPresentationElements == null)
		{
			return;
		}

		IElement pThisModelElement = getFirstModelElement();

		if (pThisModelElement != null)
		{
			Node pNode = pThisModelElement.getNode();

			if (pNode != null)
			{
				int count = pPresentationElements.size();

				for (int i = 0; i < count; i++)
				{
					IPresentationElement pThisPE = (IPresentationElement) pPresentationElements.get(i);

					IElement pFirstSubject = (pThisPE != null) ? pThisPE.getFirstSubject() : null;

					IStateVertex pStateVertex = (pFirstSubject instanceof IStateVertex) ? (IStateVertex) pFirstSubject : null;

					if (pStateVertex != null)
					{
						String sXMIID = pStateVertex.getXMIID();

						// Make sure this state vertex is owned somewhere under us
						String sQuery = ".//*[@xmi.id=\"";

						sQuery += sXMIID;
						sQuery += "\"]";

						List nodes = pNode.selectNodes(sQuery);

						if (nodes != null)
						{
							int len = nodes.size();

							if (len == 0)
							{
								// We have an item that isn't part of this partition.  Don't contain it.
								pPresentationElements.remove(i);
								i--;
								count = pPresentationElements.size();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Verifies containment according to activity group rules
	 */
	protected void verifyActivityGroupContainment(ETList < IPresentationElement > pPresentationElements)
	{
		IElement pThisModelElement = getFirstModelElement();

		if (pThisModelElement != null)
		{
			Node pNode = pThisModelElement.getNode();

			if (pNode != null)
			{
				int count = pPresentationElements.size();

				for (int i = 0; i < count; i++)
				{
					IPresentationElement pThisPE = (IPresentationElement) pPresentationElements.get(i);

					IElement pFirstSubject = (pThisPE != null) ? pThisPE.getFirstSubject() : null;

					IActivityNode pActNode = (pFirstSubject instanceof IActivityNode) ? (IActivityNode) pFirstSubject : null;

					if (pActNode != null)
					{
						String sXMIID = pActNode.getXMIID();

						// Make sure this activity node is in one of our partitions
						String sQuery = ".//@nodeContents[contains(.,\"";

						sQuery += sXMIID;
						sQuery += "\")]";

						List nodes = pNode.selectNodes(sQuery);

						if (nodes != null)
						{
							int len = nodes.size();

							if (len == 0)
							{
								// We have an item that isn't part of this partition.  Don't contain it.
								pPresentationElements.remove(i);
								i--;
								count = pPresentationElements.size();
							}
						}
					}
				}
			}
		}
	}

	/*
	 * This method addes the dependent objects (Edges, Labels) to the allContained list.  getContained only returns
	 * nodes,  
	 */
	private void addDependencies(IETGraphObject object, ETList < IETGraphObject > allContained)
	{
		if (object != null && allContained != null)
		{
			allContained.addIfNotInList(object);
			if (object.isNode())
			{
				IETNode node = (IETNode) object;
				node.getEdges();
				IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (node.getEdges());
				while (iter.hasNext())
				{
					allContained.addIfNotInList(iter.next());
				}
				iter.reset(node.getLabels());
				while (iter.hasNext())
				{
					allContained.addIfNotInList(iter.next());
				}
			}
			else if (object.isEdge())
			{
				IETEdge edge = (IETEdge) object;
				IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (edge.getLabels());
				while (iter.hasNext())
				{
					allContained.addIfNotInList(iter.next());
				}
			}
		}
	}

	/*
	 * Returns all contained graph objects including edges and labels.
	 */
	protected ETList < IETGraphObject > getAllContained()
	{
		ETList < IETGraphObject > allContained = new ETArrayList < IETGraphObject > ();
		IteratorT < IPresentationElement > iter = new IteratorT < IPresentationElement > (getContained());
		while (iter.hasNext())
		{
			addDependencies(TypeConversions.getETGraphObject(iter.next()), allContained);
		}
		return allContained;
	}

	protected ETList < IPresentationElement > getAllContainedPES()
	{
		ETList < IETGraphObject > graphObjects = getAllContained();
		ETList < IPresentationElement > pes = new ETArrayList < IPresentationElement > ();
		IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (graphObjects);
		while (iter.hasNext())
		{
			pes.add(iter.next().getPresentationElement());
		}
		return pes;
	}

	public void invalidateContained()
	{
		this.invalidate();

		ETList < IETGraphObject > allContained = getAllContained();

		// Q one up.
		if (allContained != null && allContained.size() > 0 && getDrawingArea() != null)
		{
			this.drawContained = true;
			//getDrawingArea().refresh(true);
		}
	}

	protected void drawContained(IDrawInfo pDrawInfo)
	{
		ETList < IETGraphObject > allContained = getAllContained();
		IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (allContained);
		while (iter.hasNext())
		{
			IETGraphObject obj = iter.next();
			// Don't draw the edges, they are drawn after nodes.
//			if (obj == null || obj.isEdge() || pDrawInfo == null)
                        if (obj == null || pDrawInfo == null)
                            continue;
                        				
			if ( obj != null &&
                            !(obj.getEngine() instanceof IADContainerDrawEngine) &&
                            obj.getETUI() != null )
				obj.getETUI().draw(pDrawInfo.getTSEGraphics());
			else if (obj.getEngine() != null)
			{
				//	Make sure we don't have two containers on top of each other with equal sizes.
				IETRect thisBounds = getBoundingRect();
				IETRect containedBounds = obj.getEngine().getBoundingRect();

				if (thisBounds != null && containedBounds != null && !thisBounds.getTopLeft().equals(containedBounds.getTopLeft()))
					obj.getETUI().draw(pDrawInfo.getTSEGraphics());
			}
		}                
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void doDraw(IDrawInfo pDrawInfo)
        {
            if ( this.getGraphWindow().getCurrentTool() instanceof TSEMoveSelectedTool)
            {
                this.setDrawContained(false);
            }
            else
            {
                this.setDrawContained(true);
            }
            super.doDraw(pDrawInfo);
            
            if (drawContained)
            {
                boolean onMainDisplay = pDrawInfo.getDrawingToMainDrawingArea();
                if (onMainDisplay)
                {
                    this.drawContained(pDrawInfo);
                    drawContained = false;
                }
            }
        }

	/*
	 * 
	 * @author KevinM
	 *
	 * This listener reponds to graph change events, verifing the stacking order. 
	
	public class GraphChangeListener implements TSEGraphChangeListener        
	{
		public void graphChanged(TSEGraphChangeEvent event)                
		{                    
			switch (event.getChangeType())
			{
				
				//case TSEGraphChangeEvent.NEW_GRAPH:              
				//	break;
				
				case TSEGraphChangeEvent.SELECTION_CHANGE :
					{
						onSelectionChange();
						break;
					}
				case TSEGraphChangeEvent.VIEW_CHANGE :
					//if (!event.wasPainted())
					invalidateContained();
					break;
				case TSEGraphChangeEvent.GRAPH_CHANGE :
					onGraphChanged();
					break;
			}                 
		}
	}
         */
        
        public class GraphChangeListener implements TSGraphChangeListener {
            public void graphChanged(TSGraphChangeEvent event) {                
                if (event.getType() == TSGraphChangeEvent.ANY_CHANGE) {
                    onGraphChanged();
                }
            }
        }
        
        public class SelectionChangeListener implements TSESelectionChangeListener {
            public void selectionChanged(TSESelectionChangeEvent event) {
                if (event.getType() == TSESelectionChangeEvent.ANY_CHANGE) {
                    onSelectionChange();
                }
            }
        }
        
        public class ViewportChangeListener implements TSEViewportChangeListener {
            public void viewportChanged(TSEViewportChangeEvent event) {
                if (event.getType() == TSEViewportChangeEvent.ANY_CHANGE ) {
                    invalidateContained();
                }
            }
        }
        
	/*
	 * Invoked when a graph change event occurs.
	 */
	protected void onGraphChanged()
	{
		if (this.getOwnerNode() == null)
		{
			removeChangeListener();
		}
		else
		{
			validateStackingOrder();
		}
	}

	/*
	 * Makes sure all the contined are in the correct drawing order for this container.
	 */
	protected void validateStackingOrder()
	{
		this.beginContainment(null, getAllContainedPES());
	}

	protected void onSelectionChange()
	{
		//this.invalidateContained();
	}

	protected void addGraphChangeListener()
	{
		if (graphChangeListener == null && this.getDrawingArea() != null && this.getGraphWindow() != null)
		{                    
			graphChangeListener = new GraphChangeListener();
			//getGraphWindow().addGraphChangeListener(graphChangeListener);
		}
	}

	protected void removeChangeListener()
	{
		if (graphChangeListener != null && this.getDrawingArea() != null && this.getGraphWindow() != null)
		{
			//this.getGraphWindow().removeGraphChangeListener(graphChangeListener);
			graphChangeListener = null;
		}
	}
	protected void addSelectionChangeListener()
	{
		if (selectionChangeListener == null && this.getDrawingArea() != null && this.getGraphWindow() != null)
		{
			selectionChangeListener = new SelectionChangeListener();			
		}
	}

	protected void removeSelectionChangeListener()
	{
		if (selectionChangeListener != null && this.getDrawingArea() != null && this.getGraphWindow() != null)
		{			
			selectionChangeListener = null;
		}
	}
	protected void addViewportChangeListener()
	{
		if (viewportChangeListener == null && this.getDrawingArea() != null && this.getGraphWindow() != null)
		{
			viewportChangeListener = new ViewportChangeListener();			
		}
	}

	protected void removeViewportChangeListener()
	{
		if (viewportChangeListener != null && this.getDrawingArea() != null && this.getGraphWindow() != null)
		{			
			viewportChangeListener = null;
		}
	}
    
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementDeleted(INotificationTargets pTargets)
	{
		removeChangeListener();
		return super.modelElementDeleted(pTargets);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#postLoad()
	 */
	public long postLoad()
	{
		long retval = super.postLoad();
		addGraphChangeListener();
		return retval;
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onDiscardParentETElement()
    */
   public void onDiscardParentETElement()
   {
		removeChangeListener();
      super.onDiscardParentETElement();
   }
   
   //Jyothi
   public boolean getDrawContained() {       
       return this.drawContained;
   }
   public void setDrawContained(boolean val) {
       this.drawContained = val;
   }
   
   public String getResizeBehavior() {
       return "PSK_RESIZE_EXPANDONLY";
   }
   
   public void handleNameListCompartmentDrawForContainers(IDrawInfo pDrawInfo,IETRect boundingRect) {
       handleNameListCompartmentDrawForContainers(pDrawInfo, boundingRect, 0, 0, false, 0);
   }
   
//   public void handleNameListCompartmentDrawForContainers(IDrawInfo pDrawInfo,IETRect boundingRect, int nMinNameCompartmentX,int nMinNameCompartmentY, boolean bLeftJustifyNameCompartment, int nLeftJustifyOffset) {
//       if (boundingRect == null || boundingRect.getWidth() == 0) {
//           boundingRect = pDrawInfo.getBoundingRect();
//       }
//       Iterator<ICompartment> iterator = getCompartments().iterator();
//       IETSize nameSize = null;
//       while (iterator.hasNext()) {
//           ICompartment compartment = iterator.next();
//           if (compartment instanceof ETClassNameListCompartment) {
//               nameSize = compartment.calculateOptimumSize(pDrawInfo, false);
//               IETRect nameRect = (IETRect)boundingRect.clone();
////               sizeToContents();
//               
//               nameRect.setTop(nameRect.getBottom() - nameSize.getHeight());
//               nameRect.setRight(nameRect.getRight() - ((pDrawInfo.getDeviceBounds()).getIntWidth() - boundingRect.getIntWidth()));
//               compartment.draw(pDrawInfo, nameRect);
//               
//           }           
//       }
//   }
   
   public void handleNameListCompartmentDrawForContainers(IDrawInfo pDrawInfo,IETRect boundingRect, int nMinNameCompartmentX,int nMinNameCompartmentY, boolean bLeftJustifyNameCompartment, int nLeftJustifyOffset) {
       try {
           if (pDrawInfo == null)
               return;
           
           // Get the bounding rectangle of the node.
           if (boundingRect == null || boundingRect.getWidth() == 0) {
               boundingRect = pDrawInfo.getBoundingRect();
           }
           
           IETRect deviceRect = boundingRect instanceof ETDeviceRect ? boundingRect : new ETDeviceRect(boundingRect.getRectangle());
           
           // Make sure we have a visible compartment
           int nTempX = 0;
           int nTempY = 0;
           int currentBottom = 0;
           
           clearVisibleCompartments();
           int numCompartments = getNumCompartments();
           for( int i = 0; i < numCompartments; i++ ) {
               ICompartment pCompartment = getCompartment(i);
               
               if (pCompartment != null) {
                   // Get the size at the current zoom level
                   IETSize size = pCompartment.calculateOptimumSize(pDrawInfo, false);
                   nTempX = size.getWidth();
                   nTempY = size.getHeight();
                   INameListCompartment pNameListCompartment = pCompartment instanceof INameListCompartment ? (INameListCompartment)pCompartment : null;
                   if (pNameListCompartment != null) {
                       nTempX = Math.max(nTempX, nMinNameCompartmentX);
                       nTempY = Math.max(nTempY, nMinNameCompartmentY);
                   }
                   
                   // add it to our list of visible compartments
                   addVisibleCompartment(pCompartment);
                   
                   currentBottom += nTempY;
                   if (currentBottom >= deviceRect.getBottom()) {
                       break;
                   }
               }
           }
           
           // Draw each compartment, giving the last compartment the rest of the size should the
           // node exceed the size needed by the compartments.
           currentBottom = 0;
           numCompartments = getNumVisibleCompartments();
           for(int i = 0; i < numCompartments; i++ ) {
               ICompartment pCompartment = getVisibleCompartment(i);
               
               if (pCompartment != null) {
                   IETSize size = pCompartment.calculateOptimumSize(pDrawInfo, false);
                   nTempX = size.getWidth();
                   nTempY = size.getHeight();
                   
                   INameListCompartment pNameListCompartment = pCompartment instanceof INameListCompartment ? (INameListCompartment)pCompartment : null;
                   if (pNameListCompartment != null) {
                       nTempX = Math.max(nTempX, nMinNameCompartmentX);
                       nTempY =  Math.max(nTempY, nMinNameCompartmentY);
//                       pNameListCompartment.setResizeToFitCompartments(true);
                   }
                   
                   // Now offset the rectangle
                   ETDeviceRect pThisCompartmentBoundingRect = ETDeviceRect.ensureDeviceRect( (IETRect)deviceRect.clone() );
                   
                   pThisCompartmentBoundingRect.setTop(pThisCompartmentBoundingRect.getTop() + currentBottom);
                   
                   if (i == numCompartments - 1 ) {
                       pThisCompartmentBoundingRect.setBottom(deviceRect.getBottom());
                   } else {
                       pThisCompartmentBoundingRect.setBottom(Math.min( (pThisCompartmentBoundingRect.getTop() + nTempY), deviceRect.getBottom()));
                       currentBottom+= nTempY;
                   }
                   
                   // If we're told to left justify the name compartment then do that
                   if (pNameListCompartment != null && bLeftJustifyNameCompartment) {
                       if (nLeftJustifyOffset > 0) {
                           if ( (pThisCompartmentBoundingRect.getLeft() + nLeftJustifyOffset) < pThisCompartmentBoundingRect.getRight()) {
                               pThisCompartmentBoundingRect.setLeft(pThisCompartmentBoundingRect.getLeft() + nLeftJustifyOffset);
                               pThisCompartmentBoundingRect
				   .setRight(Math.min(pThisCompartmentBoundingRect.getLeft() + nTempX, 
						      pThisCompartmentBoundingRect.getRight()));
                               nTempX += nLeftJustifyOffset;
                           } else {
                               pThisCompartmentBoundingRect.setRight(Math.min(nTempX, pThisCompartmentBoundingRect.getRight()));
                           }
                       } else {
                           pThisCompartmentBoundingRect.setRight(Math.min(nTempX, pThisCompartmentBoundingRect.getRight()));
                       }
                   }
                   
                   pCompartment.draw(pDrawInfo, pThisCompartmentBoundingRect);
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

}
