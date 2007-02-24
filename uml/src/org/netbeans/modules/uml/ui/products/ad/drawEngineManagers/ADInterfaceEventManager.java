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

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IPortDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.QuadrantKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IConnectedNode;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PresentationHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSENode;

/**
 * @author josephg
 *
 */
public class ADInterfaceEventManager extends ADEventManager implements IADInterfaceEventManager {
	private static final int s_EdgeLength = 60;
	private static final int s_EdgeLengthIncrement = 20;
	private static final int s_PortSize = 10;

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#onGraphEvent(int)
	 */
	public void onGraphEvent(int nKind) {
		switch(nKind) {
			case IGraphEventKind.GEK_POST_SMARTDRAW_MOVE:
				ETSystem.out.println("ADInterfaceEventManager.onGraphEvent(GEK_POST_SMARTDRAW_MOVE)");
			break;
			
			case IGraphEventKind.GEK_POST_MOVE:
				ETSystem.out.println("ADInterfaceEventManager.onGraphEvent(GEK_POST_MOVE)");
				onPostMove();
			break;
		}
		super.onGraphEvent(nKind);
	}
	
	private void onPostMove() {
		PresentationHelper presentationHelper = new PresentationHelper();
		TSENode classNode = getOwnerNode();
		if(presentationHelper != null && classNode != null) {
			if(presentationHelper.isOwnerNodeInterfaceDrawnAsLollypopWithOneControllingEdge(m_parentETGraphObject)) {
				IEdgePresentation edgePresentation = presentationHelper.getLollipopControllingEdge(m_parentETGraphObject);
				
				if(edgePresentation != null) {
					edgePresentation.discardAllBends();
				}
				else {
					// should never happend cause we established that there was one and only one first
				}
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IADInterfaceEventManager#distributeAttachedInterfaces(boolean)
	 */
   public void distributeAttachedInterfaces(boolean bRedraw)
   {
      if (isControllingPort())
      {
         IPresentationElement thisPE = getParentPresentationElement();
         IDrawEngine drawEngine = getParentDrawEngine();
         IComponentDrawEngine componentDE = null;
         int nSideKind = QuadrantKindEnum.QK_ERROR;

         INodePresentation thisNodePE = null;
         if (thisPE instanceof INodePresentation)
            thisNodePE = (INodePresentation)thisPE;
         IPortDrawEngine portDE = null;
         if (drawEngine instanceof IPortDrawEngine)
            portDE = (IPortDrawEngine)drawEngine;

         if (portDE != null)
         {
            componentDE = portDE.getComponentDrawEngine();
            nSideKind = portDE.getComponentSide();
         }

         if (thisNodePE != null && componentDE != null && portDE != null)
         {
            ETList < IConnectedNode > connectedNodes = thisNodePE.getEdgeConnectedNodes();

            if (connectedNodes != null)
            {
               int count = connectedNodes.getCount();

               if (count > 0)
               {
                  for (int index = 0; index < count; index++)
                  {
                     IConnectedNode connectedNode = connectedNodes.item(index);

                     if (connectedNode != null)
                     {
                        IEdgePresentation edgePE = connectedNode.getIntermediateEdge();
                        if (edgePE != null)
                        {
                           IElement element = TypeConversions.getElement(edgePE);
                           String elementType = element.getElementType();

                           if (!(elementType.compareTo("Usage") == 0 || elementType.compareTo("Interface") == 0))
                           {
                              connectedNodes.remove(index);
                              count = connectedNodes.getCount();
                              if (count == 0)
                                 break;
                           }

                        }
                     }
                  }

                  count = connectedNodes.getCount();

                  if (count != 0)
                  {
                     doDistribute(nSideKind, portDE, componentDE, connectedNodes, bRedraw);
                  }
               }
            }

         }
      }
   }

	boolean isControllingPort() {
		IDrawEngine drawEngine = getParentDrawEngine();
		if(drawEngine instanceof IPortDrawEngine) {
			return true;
		}
		return false;
	}
	
   protected void doDistribute(int nSideKind, IPortDrawEngine portDE, IComponentDrawEngine componentDE, ETList < IConnectedNode > interfacesToDistribute, boolean bRedraw)
   {
      if( null == portDE ) throw new IllegalArgumentException();
      if( null == componentDE ) throw new IllegalArgumentException();
      if( null == interfacesToDistribute ) throw new IllegalArgumentException();
      
      int count = interfacesToDistribute.getCount();
      if (count > 0)
      {
         IETRect portBoundingRect = portDE.getLogicalBoundingRect(false);
         IETRect componentBoundingRect = componentDE.getLogicalBoundingRect(false);
         IDrawingAreaControl control = getDrawingArea();

         if (portBoundingRect != null && componentBoundingRect != null && control != null)
         {
            // the spacing is the size of the interface node
            int spacing = 0;
            IConnectedNode firstConnectedNode = interfacesToDistribute.item(0);
            if (firstConnectedNode != null)
            {
               INodePresentation interfaceNode = firstConnectedNode.getNodeAtOtherEnd();

               if (interfaceNode != null)
               {
                  // Get the spacing - the size of the interface, use this to determine spacing
                  IETRect location = interfaceNode.getLocation();
                  spacing = location.getIntWidth();
               }
            }
            
            if (spacing != 0)
            {
               // Try to resize the port so all interfaces can line up
               int optimumSize = count * spacing;
               int nPortWidth = portBoundingRect.getIntWidth();
               int nPortHeight = portBoundingRect.getIntHeight();

               if (nSideKind == QuadrantKindEnum.QK_TOP || nSideKind == QuadrantKindEnum.QK_BOTTOM)
               {
                  int nComponentWidth = componentBoundingRect.getIntWidth();

                  if (nPortWidth != 0 && nPortHeight != 0 & optimumSize < nComponentWidth)
                  {
                     portDE.resize(optimumSize, s_PortSize, false);
                  }
                  else
                  {
                     if (nComponentWidth > spacing)
                     {
                        portDE.resize(nComponentWidth = 4, s_PortSize, false);
                     }
                  }
               } // if top or bottom
               else
               {
                  int nComponentHeight = componentBoundingRect.getIntHeight();

                  if (nPortWidth != 0 && nPortHeight != 0 && optimumSize != 0 && optimumSize < nComponentHeight)
                  {
                     portDE.resize(s_PortSize, optimumSize, false);
                  }
                  else
                  {
                     if (nComponentHeight > spacing)
                     {
                        portDE.resize(s_PortSize, nComponentHeight - 4, false);
                     }
                  }
               }

               portBoundingRect = portDE.getLogicalBoundingRect(false);

               int nPortLeft = 0;
               int nPortTop = 0;
               int nPortRight = 0;
               int nPortBottom = 0;

               if (portBoundingRect != null)
               {
                  nPortLeft = portBoundingRect.getLeft();
                  nPortTop = portBoundingRect.getTop();
                  nPortRight = portBoundingRect.getRight();
                  nPortBottom = portBoundingRect.getBottom();
               }

               int numPasses = 1;
               int spacingIndex = 0;
               int initialOffset = spacing / 2 - 1;

               for (int index = 0; index < count; index++)
               {
                  IConnectedNode connectedNode = interfacesToDistribute.item(index);
                  IEdgePresentation intermediateEdge = null;
                  INodePresentation interfaceNode = null;

                  if (connectedNode != null)
                  {
                     intermediateEdge = connectedNode.getIntermediateEdge();
                     interfaceNode = connectedNode.getNodeAtOtherEnd();
                  }

                  if (interfaceNode != null && intermediateEdge != null)
                  {
                     int x = 0;
                     int y = 0;

                     if (nSideKind == QuadrantKindEnum.QK_TOP)
                     {
                        x = nPortLeft + (spacingIndex * spacing) + initialOffset;
                        y = nPortTop + (numPasses * s_EdgeLength) + (spacingIndex * s_EdgeLengthIncrement);

                        if (x > nPortRight)
                        {
                           spacingIndex = 0;
                           numPasses++;
                           x = nPortLeft + (spacingIndex * spacing) + initialOffset;
                        }
                     }
                     else if (nSideKind == QuadrantKindEnum.QK_BOTTOM)
                     {
                        x = nPortLeft + (spacingIndex * spacing) + initialOffset;
                        y = nPortBottom - (numPasses * s_EdgeLength) - (spacingIndex * s_EdgeLengthIncrement);

                        if (x > nPortRight)
                        {
                           spacingIndex = 0;
                           numPasses++;
                           x = nPortLeft + (spacingIndex * spacing) + initialOffset;
                        }
                     }
                     else if (nSideKind == QuadrantKindEnum.QK_LEFT)
                     {
                        x = nPortLeft - (numPasses * s_EdgeLength) - (spacingIndex * s_EdgeLengthIncrement);
                        y = nPortTop - (spacingIndex * spacing) - initialOffset;

                        if (y < nPortBottom)
                        {
                           spacingIndex = 0;
                           numPasses++;
                           y = nPortTop - (spacingIndex * spacing) - initialOffset;
                        }
                     }
                     else if (nSideKind == QuadrantKindEnum.QK_RIGHT)
                     {
                        x = nPortRight + (numPasses * s_EdgeLength) + (spacingIndex * s_EdgeLengthIncrement);
                        y = nPortTop - (spacingIndex * spacing) - initialOffset;

                        if (y < nPortBottom)
                        {
                           spacingIndex = 0;
                           numPasses++;
                           y = nPortTop - (spacingIndex * spacing) - initialOffset;
                        }
                     }

                     intermediateEdge.discardAllBends();

                     interfaceNode.moveTo(x, y, MoveToFlags.MTF_MOVEX | MoveToFlags.MTF_MOVEY | MoveToFlags.MTF_LOGICALCOORD);

                     // tell the ndoe about the move
                     IDrawEngine interfaceDE = TypeConversions.getDrawEngine(interfaceNode);

                     if (interfaceDE != null)
                     {
                        IEventManager eventManager = interfaceDE.getEventManager();

                        if (eventManager != null)
                        {
                           eventManager.onGraphEvent(IGraphEventKind.GEK_POST_SMARTDRAW_MOVE);

                           control.executeStackingCommand(interfaceNode, IDrawingAreaControl.SOK_MOVETOFRONT, bRedraw);
                        }
                     }
                  }

                  spacingIndex++;
               }
            }
         }
      }
   }
}

