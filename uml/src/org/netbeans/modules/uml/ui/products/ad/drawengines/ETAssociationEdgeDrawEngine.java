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

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IAssociationClassEventManager;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IBridgeElements;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IAssociationEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISupportEnums;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/*
 * @author KevinM
 *
 */
public class ETAssociationEdgeDrawEngine extends ETEdgeDrawEngine {
	protected IAssociationEnd m_StartEnd = null;
	protected IAssociationEnd m_FinishEnd = null;
	protected int m_ContextMenuLocation = CMPK_MIDDLE;

	public static int CMPK_START = 0;
	public static int CMPK_END = 1;
	public static int CMPK_MIDDLE = 2;

	public ETAssociationEdgeDrawEngine() {
		super();
	}

	public String getElementType() {
		String type = super.getElementType();
		if (type == null) {
			type = new String("Association");
		}
		return type;
	}

	protected int getStartArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	protected int getEndArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	public void doDraw(IDrawInfo drawInfo) {
		try {
			boolean bDidDraw = false;
			TSEColor edgeColor = this.getStateColor(); // getColor(getResourceID(CK_BORDERCOLOR));

			// At extreeme zoom levels we don't want to hit the metadata, so just
			// draw a line.  SimpleDrawEdge looks at the zoom and draws just an edge
			// if the user can't see an arrowhead anyway.
			if (simpleDrawEdge(drawInfo, getLineKind()) == false) {
				IElement pModelElement = null;
				IEdgePresentation pPE = getIEdgePresentation();

				if (pPE != null) {
					pModelElement = pPE.getFirstSubject();
				}

				if (pModelElement == null)
					pModelElement = getUI().getModelElement();

				if (pModelElement != null) {
					String sElementType = pModelElement.getElementType();

					IAggregation pAggregation = getIAggregation(pModelElement);

					if (pAggregation != null) {
						bDidDraw = drawAggregation(drawInfo, pPE, pAggregation, edgeColor);
					} else if (sElementType.equals("Association")) {
						IAssociation pAssociation = getIAssociation(pModelElement);

						if (pAssociation != null) {
							bDidDraw = drawAssociation(drawInfo, pPE, pAssociation, edgeColor);
						}
					} else if (sElementType.equals("AssociationClass")) {
						IAssociationClass pAssociationClass = getIAssociationClass(pModelElement);
						if (pAssociationClass != null) {
							bDidDraw = drawAssociationClass(drawInfo, pPE, pAssociationClass, edgeColor);
						}
					}
				}
			} else
				bDidDraw = true;

			if (bDidDraw == false) {
				// Some error happened, draw a line
				super.doDraw(drawInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// ( err );
		}
	}

	/*
	 * Draw aggregation edges
	 */
	protected boolean drawAggregation(IDrawInfo drawInfo, IEdgePresentation pPE, IAggregation pAggregation, TSEColor edgeColor) {
		boolean bDidDraw = false;
		try {
			if (drawInfo == null || pAggregation == null)
				return false;

			IAssociationEnd pAggregateEnd = pAggregation.getAggregateEnd();
			IAssociationEnd pPartEnd = pAggregation.getPartEnd();
			boolean bIsComposite = pAggregation.getIsComposite();

			if (pAggregateEnd != null && pPartEnd != null) {
				// See if the aggregation end is a diamond filled or unfilled
				int nEndKind = pPE.getNodeEnd2(pAggregateEnd);

				// Now we can figure out how to draw.
				int startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
				int endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;

				boolean bIsNavigable = false;
				if (nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_BOTH) {
					setEnds(pAggregateEnd, pPartEnd);

					bIsNavigable = m_StartEnd.getIsNavigable();

					if (bIsNavigable) {
						startArrowheadKind = (bIsComposite) ? DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND_NAVIGABLE : DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND_NAVIGABLE;
					} else {
						startArrowheadKind = (bIsComposite) ? DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND : DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND;
					}

					// Now how do we draw the other side?
					INavigableEnd pNavEnd = getINavigableEnd(pPartEnd);
					if (pNavEnd != null) {
						endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
					}
				} else if (nEndKind == NodeEndKindEnum.NEK_TO) {
					setEnds(pPartEnd, pAggregateEnd);

					bIsNavigable = m_FinishEnd.getIsNavigable();

					if (bIsNavigable) {
						endArrowheadKind = (bIsComposite) ? DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND_NAVIGABLE : DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND_NAVIGABLE;
					} else {
						endArrowheadKind = (bIsComposite) ? DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND : DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND;
					}

					// Now how do we draw the other side?
					INavigableEnd pNavEnd = getINavigableEnd(pPartEnd);
					if (pNavEnd != null) {
						startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
					}
				}

				this.drawEdge(drawInfo, startArrowheadKind, endArrowheadKind, getLineKind());
				bDidDraw = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			bDidDraw = false;
		}

		return bDidDraw;
	}

	/*
	 * Draw Association
	 */
	protected boolean drawAssociation(IDrawInfo drawInfo, IEdgePresentation pPE, IAssociation pAssociation, TSEColor edgeColor) {
		boolean bDidDraw = false;

		try {
			if (drawInfo != null && pAssociation != null) {
				ETList < IAssociationEnd > pAssociationEnds = pAssociation.getEnds();
				int numEnds = pAssociationEnds.size();

				if (numEnds == 2) {
					IAssociationEnd pFirstEnd = pAssociationEnds.get(0);
					IAssociationEnd pSecondEnd = pAssociationEnds.get(1);

					// See if either end is navigable (shown with an arrowhead)
					INavigableEnd pNavFirstEnd = getINavigableEnd(pFirstEnd);
					INavigableEnd pNavSecondEnd = getINavigableEnd(pSecondEnd);

					if (pFirstEnd != null && pSecondEnd != null)
					{
						// See if the aggregation end is a diamond filled or unfilled
						int nEndKind = NodeEndKindEnum.NEK_UNKNOWN;

						nEndKind = pPE.getNodeEnd(pFirstEnd);

						int startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
						int endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
						if (nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_BOTH) {
							setEnds(pFirstEnd, pSecondEnd);

							if (pNavFirstEnd != null) {
								startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
							}

							if (pNavSecondEnd != null) {
								endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
							}
						} else if (nEndKind == NodeEndKindEnum.NEK_TO) {
							setEnds(pSecondEnd, pFirstEnd);

							if (pNavFirstEnd != null) {
								endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
							}

							if (pNavSecondEnd != null) {
								startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
							}
						}

						bDidDraw = drawEdge(drawInfo, startArrowheadKind, endArrowheadKind, getLineKind());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bDidDraw;
	}

	/*
	 * Draws the association class.
	 */
	protected boolean drawAssociationClass(IDrawInfo drawInfo, IEdgePresentation pPE, IAssociationClass pAssociationClass, TSEColor edgeColor) {
		boolean bDidDraw = false;

		boolean hr = true;
		try
		{
			ETTripleT < IAssociationEnd, IAssociationEnd, Integer > result = getAssociationEnd();
			IAssociationEnd pThisEnd = result.getParamOne();
			IAssociationEnd pOtherEnd = result.getParamTwo();
			int nOurIndex = result.getParamThree() != null ? result.getParamThree().intValue() : 0;

			  // We are either the dashed line or the one of the solid lines associated
			  // with the source or target association end.
			  if (pThisEnd != null)
			  {
				 // We are one of the end segments.  Treat as if we're near the end of a normal association
				 // link.
				 if (nOurIndex == 0)
				 {
					setEnds(pThisEnd, pOtherEnd);
				 }
				 else if (nOurIndex == 1)
				 {
					setEnds(pOtherEnd, pThisEnd);
				 }

				 bDidDraw = drawAssociationEnd(drawInfo, pPE, pAssociationClass, pThisEnd, edgeColor);
			  }
			  else if (pThisEnd == null || pOtherEnd == null)
			  {
				  IDrawEngine pFromDrawEngine  = pPE != null ? pPE.getEdgeFromDrawEngine() : null;
				  IDrawEngine pToDrawEngine = pPE != null ? pPE.getEdgeToDrawEngine() : null;
  

				 if (pFromDrawEngine == null || pToDrawEngine == null)
				 {
					// We are reconnecting, just draw a solid line
					drawEdge(drawInfo, 
								 DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD, 
								 DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD,
								 this.getLineKind());
					bDidDraw = true;
				 }
				 else
				 {
					drawEdge(drawInfo, 
								 DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD, 
								 DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD,
								 DrawEngineLineKindEnum.DELK_DASH);
					bDidDraw = true;
				 }
			  }
		}
		catch ( Exception e )
		{
		   e.printStackTrace();
		}
		return bDidDraw;
	}

	/**
	 Draws an associationend
	 */
	protected boolean drawAssociationEnd(IDrawInfo info, 
																   IEdgePresentation pPE,
																   IAssociationClass pAssociationClass,
																   IAssociationEnd pAssociationEnd, 
																   TSEColor edgeColor)
	{
	   boolean bDidDraw = false;

	   try
	   {
		  if (pAssociationEnd != null  && pPE != null && pAssociationClass != null)
		  {
			 //
			 // AssociationClasses in the Pre UML 2.0 spec don't support aggregations because
			 // they are derived off of IAssociation so they don't support IAggregation.  So in
			 // Describe 6.5 and the release of the UML 2.0 spec we will fix this - IAggregation and
			 // the end types disappear so they've changed stuff.
			 //
			 // For sp1 we go to the xml directly to get the aggregation properties.  We'll fix this
			 // after IAssociation is changed to match the UML 2.0 release.
			 //
			boolean bHasIsComposite = false;
			boolean bIsComposite = false;
			boolean bThisIsAggregateEnd = false;
			String sAggregateEnd = "";
			String sPartEnd = "";


			 getAssociationClassAggregationHack(pAssociationClass,
													pAssociationEnd,
													bHasIsComposite,
													bIsComposite,
													bThisIsAggregateEnd,
													sAggregateEnd,
													sPartEnd);

			 // See if either end is navigable (shown with an arrowhead)
			 INavigableEnd  pNavEnd = pAssociationEnd instanceof INavigableEnd ? (INavigableEnd)pAssociationEnd : null;

			 String sDrawEngineID = null;
			 int startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
			 int endArrowheadKind   = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;

			IDrawEngine pFromNode = pPE.getEdgeFromDrawEngineWithID("AssociationClassConnectorDrawEngine");
			IDrawEngine pToNode = pPE.getEdgeToDrawEngineWithID("AssociationClassConnectorDrawEngine");
	
			 if (bHasIsComposite)
			 {
				// Setup the start arrowhead
				if (bThisIsAggregateEnd)
				{
				   if (pFromNode != null && pNavEnd != null )
				   {
					  endArrowheadKind = (bIsComposite)? DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND_NAVIGABLE:DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND_NAVIGABLE;
				   }
				   else if (pFromNode != null && bThisIsAggregateEnd)
				   {
					  endArrowheadKind = (bIsComposite)?DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND:DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND;
				   }
				}
				else
				{
				   if (pFromNode != null && pNavEnd != null)
				   {
					  endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
				   }
				}
            
				// Setup the finish arrowhead
				if (bThisIsAggregateEnd)
				{
				   if (pToNode != null && pNavEnd != null)
				   {
					  startArrowheadKind = (bIsComposite)?DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND_NAVIGABLE:DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND_NAVIGABLE;
				   }
				   else if (pToNode != null && bThisIsAggregateEnd)
				   {
					  startArrowheadKind = (bIsComposite)?DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND:DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND;
				   }
				}
				else
				{
				   if (pToNode != null && pNavEnd != null)
				   {
					  startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
				   }
				}
			 }
			 else
			 {
				if (pFromNode != null && pNavEnd != null)
				{
				   endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
				}
				if (pToNode != null && pNavEnd != null)
				{
				   startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
				}
			 }

			 drawEdge(info, 
						  startArrowheadKind, 
						  endArrowheadKind, this.getLineKind());
			 bDidDraw = true;
		  }
	   }
	   catch ( Exception e )
	   {
		  e.printStackTrace();
	   }
	   return bDidDraw;
	}
	
	//
	// Interface Crackers.
	//
	
	protected IAggregation getIAggregation(IElement pModelElement) {
		if (pModelElement instanceof IAggregation) {
			return (IAggregation) pModelElement;
		}
		return null;
	}

	protected IAssociation getIAssociation(IElement pModelElement) {
		if (pModelElement instanceof IAssociation) {
			return (IAssociation) pModelElement;
		}
		return null;
	}

	protected IAssociationClass getIAssociationClass(IElement pModelElement) {
		if (pModelElement instanceof IAssociationClass) {
			return (IAssociationClass) pModelElement;
		}
		return null;
	}

	protected INavigableEnd getINavigableEnd(IAssociationEnd pEnd) {
		if (pEnd instanceof INavigableEnd) {
			return (INavigableEnd) pEnd;
		}
		return null;
	}

	protected void setEnds(IAssociationEnd pStartEnd, IAssociationEnd pFinishEnd) {
		m_StartEnd = pStartEnd;
		m_FinishEnd = pFinishEnd;
	}

	protected void setEnd(boolean bStart, IAssociationEnd pEnd) {
		if (bStart) {
			m_StartEnd = pEnd;
		} else {
			m_FinishEnd = pEnd;
		}
	}

	public String getPresentationType() {
		IElement element = getUI().getModelElement();
		if (element == null)
			return null;
		if (getIAssociation(element) != null || getIAssociationClass(element) != null) {
			return new String("AssociationEdgePresentation");
		}
		return new String("AggregationEdgePresentation");
	}

	public void onContextMenu(IMenuManager manager) {
		m_ContextMenuLocation = CMPK_MIDDLE;
		// See what side of the edge we're on
		TSEEdge pTSEdge = getEdge();
		IElement modEle = getFirstModelElement();
		IAssociation pAssociation = null;
		IAssociationClass pAssociationClass = null;
		IAggregation pAggregation = null;
		//Point pt = manager.getLocation();

		if (modEle != null) {
			if (modEle instanceof IAssociation) {
				pAssociation = (IAssociation) modEle;
			}
			if (modEle instanceof IAssociationClass) {
				pAssociationClass = (IAssociationClass) modEle;
			}
			if (modEle instanceof IAggregation) {
				pAggregation = (IAggregation) modEle;
			}
		}

		if (pAssociationClass != null) {
			ETTripleT < IAssociationEnd, IAssociationEnd, Integer > result = getAssociationEnd();
			IAssociationEnd pThisEnd = result.getParamOne();
			IAssociationEnd pOtherEnd = result.getParamTwo();
			int nOurIndex = result.getParamThree() != null ? result.getParamThree().intValue() : 0;

			if (pThisEnd != null) {
				// We are one of the end segments.  Treat as if we're near the end of a normal association
				// link.
				if (nOurIndex == 0) {
					m_ContextMenuLocation = CMPK_START;
					setEnds(pThisEnd, pOtherEnd);
				} else if (nOurIndex == 1) {
					m_ContextMenuLocation = CMPK_END;
					setEnds(pThisEnd, pOtherEnd);
				}
			} else {
				// We are the dashed line.  Treat as if we're in the center of an association link
				m_ContextMenuLocation = CMPK_MIDDLE;
				setEnds(null, null);
				m_StartEnd = pAssociation.getEndAtIndex(0);
				m_FinishEnd = pAssociation.getEndAtIndex(1);
			}
		} else {
			if (pTSEdge != null) {
				TSConstRect rect = pTSEdge.getBounds(); // getBoundingRect();
				TSRect scaledBoundingRect = new TSRect(rect);

				scaledBoundingRect.setHeight(rect.getHeight() / 4);
				scaledBoundingRect.setWidth(rect.getWidth() / 4);

				// Get the current point
				Point devicePt = manager.getLocation();
				// Use the logical space.
				TSPoint pt = PointConversions.newTSPoint(getDrawingArea(), new ETPoint(devicePt));
                                boolean bWeAreNearMiddle = true;
                                
                                // (LLS) Adding the buildContext logic to support A11Y issues.  The
                                // user should be able to use the CTRL-F10 keystroke to activate
                                // the context menu.  In the case of the keystroke the location
                                // will not be valid.  Therefore, we have to just check if the
                                // compartment is selected.
                                if(pt != null)
                                {
                                    bWeAreNearMiddle = isPointNearTheMiddle(pt, scaledBoundingRect);
                                }

				if (bWeAreNearMiddle == false)
                                {
                                    determineTheClosestEnd(pt, pTSEdge);
                                }
			}
		}

		// Add the label menu items
		addAssociationAndAggregationEdgeMenuItems(manager, modEle);
		if (m_ContextMenuLocation == CMPK_MIDDLE) {
			addAssociationMultiLabelSelectionsPullright(manager, true);
		} else {
			addAssociationEndLabelsPullright(manager);
			if (pAssociationClass == null) {
				// Don't allow assciationclasses to add qualifiers
				addQualifiersButton(manager);
			}
			addAssociationMultiLabelSelectionsPullright(manager, false);
			addAssociationEndSetMultiplicityMenuItems(manager);
		}

		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_STEREOTYPE, manager);

		// Call the base class
		super.onContextMenu(manager);
	}

    private void determineTheClosestEnd(final TSPoint pt, final TSEEdge pTSEdge)
    {
        // We're on the border of the edge.  Find what side.
        TSConstPoint fromPoint = pTSEdge.getSourcePoint();
        TSConstPoint toPoint = pTSEdge.getTargetPoint();

        if (fromPoint != null && toPoint != null) {
        	TSENode pNodeClosestToPoint = null;

        	double fromPointDistance = fromPoint.distanceSquared(pt);
        	double toPointDistance = toPoint.distanceSquared(pt);

        	if (fromPointDistance < toPointDistance) {
        		m_ContextMenuLocation = CMPK_START;
        		pNodeClosestToPoint = (TSENode) pTSEdge.getSourceNode();
        	} else {
        		m_ContextMenuLocation = CMPK_END;
        		pNodeClosestToPoint = (TSENode) pTSEdge.getTargetNode();
        	}

        	if (pNodeClosestToPoint != null && (pTSEdge.getSourceNode() != pTSEdge.getTargetNode())) {

        		IElement pElementNearPoint = TypeConversions.getElement((TSNode) pNodeClosestToPoint);

        		if (pElementNearPoint != null) {
        			IClassifier pClassifierNearPoint = pElementNearPoint instanceof IClassifier ? (IClassifier) pElementNearPoint : null;
        			if (pClassifierNearPoint != null) {
        				m_ContextMenuLocation = CMPK_END;
        				if (m_StartEnd != null) {
        					boolean bIsSame = m_StartEnd.isSameParticipant(pClassifierNearPoint);

        					if (bIsSame) {
        						m_ContextMenuLocation = CMPK_START;
        					}
        				}
        			}
        		}
        	}
        }
    }

    protected boolean isPointNearTheMiddle(final TSPoint pt, final TSRect scaledBoundingRect)
    {

        // We scale the bounding rect by 1/2.  If the point is inside the scaled rect
        // then we consider that we're too close to the middle to determine the edge so
        // we keep the flag indicating we're in the middle
        double nWidth = scaledBoundingRect.getWidth();
        double nHeight = scaledBoundingRect.getHeight();

        // Use the width and height to determine if we've got a close-to horizontal or
        // close to vertical edge
        boolean bWeAreNearMiddle = true;
        if (nWidth < 3) {
        	if (!(scaledBoundingRect.getTop() >= pt.getY() && scaledBoundingRect.getBottom() <= pt.getY())) {
        		bWeAreNearMiddle = false;
        	}
        } else if (nHeight < 3) {
        	if (!(scaledBoundingRect.getLeft() <= pt.getX() && scaledBoundingRect.getRight() >= pt.getX())) {
        		bWeAreNearMiddle = false;
        	}
        } else if (scaledBoundingRect.contains(pt) == false) {
        	bWeAreNearMiddle = false;
        }
        return bWeAreNearMiddle;
    }

	/**
	 * Returns the IAssociationEnd we represent (used for AssociationClasses)
	 */
	private ETTripleT < IAssociationEnd, IAssociationEnd, Integer > getAssociationEnd() {
		IAssociationEnd pThisEnd = null;
		IAssociationEnd pOtherEnd = null;
		int nOurIndex = 0;

		// We are either the dashed line or the one of the solid lines associated
		// with the source or target association end.
		IEdgePresentation pPE = getIEdgePresentation();
		if (pPE != null) {
			IDrawEngine fromDrawEngine = null;
			IDrawEngine toDrawEngine = null;
			IElement fromEle = null;
			IElement toEle = null;
			IElement pThisEle = TypeConversions.getElement(this);

			ETPairT < IDrawEngine, IDrawEngine > result = pPE.getEdgeFromAndToDrawEngines();
			if (result != null) {
				fromDrawEngine = result.getParamOne();
				toDrawEngine = result.getParamTwo();
			}

			fromEle = TypeConversions.getElement(fromDrawEngine);
			toEle = TypeConversions.getElement(toDrawEngine);

			if (fromEle != null && toEle != null && pThisEle instanceof IAssociation) {
				IAssociation pAssocClassAsAssoc = (IAssociation) pThisEle;

				boolean isSame = pAssocClassAsAssoc.isSame(fromEle);
				IElement pParticipandDrawEngineEle = null;
				if (!isSame) {
					pParticipandDrawEngineEle = fromEle;
				} else {
					isSame = pAssocClassAsAssoc.isSame(toEle);
					if (!isSame) {
						pParticipandDrawEngineEle = toEle;
					}
				}

				if (pParticipandDrawEngineEle != null) {
					// The from draw engine is pointing to a different element.  Get the association
					// end and draw
					boolean isReflexive = pAssocClassAsAssoc.getIsReflexive();
					if (isReflexive) {
						// This is complicated.  We need to figure out what side of the
						// reflexive link we represent.  We use the event manager and have it tell us
						IEventManager pEventManager = getEventManager();
						IAssociationClassEventManager pAssocEventManager = pEventManager instanceof IAssociationClassEventManager ? (IAssociationClassEventManager) pEventManager : null;
						if (pAssocEventManager != null) {
							IBridgeElements bridgeElements = pAssocEventManager.getBridgeElements();
							IETGraphObject pSourceEdge = bridgeElements.getSourceEdge();
							IETGraphObject pSmallNode = bridgeElements.getSmallNode();
							IETGraphObject pTargetEdge = bridgeElements.getTargetEdge();
							IETGraphObject pDottedEdge = bridgeElements.getDottedEdge();
							IETGraphObject pSourceNode = bridgeElements.getSourceNode();
							IETGraphObject pTargetNode = bridgeElements.getTargetNode();
							IDrawEngine pSourceEdgeDrawEngine = TypeConversions.getDrawEngine(pSourceEdge);

							if (pSourceEdgeDrawEngine != null) {
								if (pSourceEdgeDrawEngine == this) {
									// We are end #0
									pThisEnd = pAssocClassAsAssoc.getEndAtIndex(0);
									nOurIndex = 0;
									pOtherEnd = pAssocClassAsAssoc.getEndAtIndex(1);
								}
							}

							if (pThisEnd == null) {
								// We are end #1
								pThisEnd = pAssocClassAsAssoc.getEndAtIndex(1);
								nOurIndex = 1;
								pOtherEnd = pAssocClassAsAssoc.getEndAtIndex(0);
							}
						}
					} else {
						pThisEnd = pAssocClassAsAssoc.getFirstEndWithParticipant(pParticipandDrawEngineEle);
						if (pThisEnd != null) {
							nOurIndex = pAssocClassAsAssoc.getEndIndex(pThisEnd);
							if (nOurIndex == 0) {
								pOtherEnd = pAssocClassAsAssoc.getEndAtIndex(1);
							} else {
								pOtherEnd = pAssocClassAsAssoc.getEndAtIndex(0);
							}
						}
					}
				}
			}
		}
		return new ETTripleT < IAssociationEnd, IAssociationEnd, Integer > (pThisEnd, pOtherEnd, new Integer(nOurIndex));
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler#setSensitivityAndCheck(java.lang.String, org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass)
	 */
	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass actionClass) {
		boolean retVal = handleStandardLabelSensitivityAndCheck(id, actionClass);

		if (!retVal) {
			boolean isReadOnly = isParentDiagramReadOnly();
			boolean bFlag = false;
			ILabelManager labelMgr = getLabelManager();
			ITSGraphObject pETElement = getParentETElement();
			IElement pElement = null;
			if (pETElement != null) {
				pElement = TypeConversions.getElement((IETGraphObject) pETElement);
			}
			IAssociation pAssociation = null;
			IAssociationClass pAssociationClass = null;
			IAggregation pAggregation = null;
			if (pElement instanceof IAssociation) {
				pAssociation = (IAssociation) pElement;
			}
			if (pElement instanceof IAssociationClass) {
				pAssociationClass = (IAssociationClass) pElement;
			}
			if (pElement instanceof IAggregation) {
				pAggregation = (IAggregation) pElement;
			}

			// Get the nearest end
			IAssociationEnd pNearestEnd = null;
			if (m_ContextMenuLocation == CMPK_START) {
				pNearestEnd = m_StartEnd;
			} else if (m_ContextMenuLocation == CMPK_END) {
				pNearestEnd = m_FinishEnd;
			}

			// Get the index of the nearest end
			int index = 0;
			if (pNearestEnd != null && pAssociation != null) {
				index = pAssociation.getEndIndex(pNearestEnd);
			}

			//set the check state
			if (id.equals("MBK_LINK_END_ORDINARY_AGGREGATE") || id.equals("MBK_LINK_END_COMPOSITE_AGGREGATE")) {
				if (pAggregation != null && pNearestEnd != null) {
					boolean isAggEnd = pAggregation.isAggregateEnd(pNearestEnd);
					if (isAggEnd) {
						boolean isComposite = pAggregation.getIsComposite();
						if (id.equals("MBK_LINK_END_ORDINARY_AGGREGATE")) {
							if (!isComposite) {
								actionClass.setChecked(true);
							} else {
								bFlag = isReadOnly ? false : true;
							}
						} else if (id.equals("MBK_LINK_END_COMPOSITE_AGGREGATE")) {
							if (isComposite) {
								actionClass.setChecked(true);
							} else {
								bFlag = isReadOnly ? false : true;
							}
						}
					}
				} else if (pAssociationClass != null && pNearestEnd != null) {
					boolean hasIsComposite = false;
					boolean isComposite = false;
					boolean thisIsAggEnd = false;
					String aggEnd = "";
					String partEnd = "";

					// Get the Aggregation stuff for the AssociationClass which is fixed in UML 2.0, but not SP1
					getAssociationClassAggregationHack(pAssociationClass, pNearestEnd, hasIsComposite, isComposite, thisIsAggEnd, aggEnd, partEnd);
					if (thisIsAggEnd && hasIsComposite) {
						if (id.equals("MBK_LINK_END_ORDINARY_AGGREGATE")) {
							if (!isComposite) {
								actionClass.setChecked(true);
							} else {
								bFlag = isReadOnly ? false : true;
							}
						} else if (id.equals("MBK_LINK_END_COMPOSITE_AGGREGATE")) {
							if (isComposite) {
								actionClass.setChecked(true);
							} else {
								bFlag = isReadOnly ? false : true;
							}
						}
					} else {
						bFlag = isReadOnly ? false : true;
					}
				} else if (pAggregation == null && pNearestEnd != null) {
					bFlag = isReadOnly ? false : true;
				}
			} else if (id.equals("MBK_LINK_END_NAVIGABLE")) {
				if (pNearestEnd != null && pNearestEnd instanceof INavigableEnd) {
					actionClass.setChecked(true);
				}

				// Valid if we're on an end.
				if (m_ContextMenuLocation == CMPK_START || m_ContextMenuLocation == CMPK_END) {
					bFlag = isReadOnly ? false : true;
				}
			} else if (id.equals("MBK_SHOW_ASSOCIATION_NAME")) {
				boolean isDisplayed = false;
				if (labelMgr != null) {
					isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_ASSOCIATION_NAME);
				}
				actionClass.setChecked(isDisplayed);
				bFlag = isReadOnly ? false : true;
			} else if (id.equals("MBK_SHOW_ROLENAME")) {
				boolean isDisplayed = false;
				if (labelMgr != null && pNearestEnd != null && pAssociation != null) {
					isDisplayed = labelMgr.isDisplayed((index == 0) ? TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME : TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME);
				}
				actionClass.setChecked(isDisplayed);
				bFlag = isReadOnly ? false : true;
			} else if (id.equals("MBK_SHOW_MULTIPLICITY")) {
				boolean isDisplayed = false;
				if (labelMgr != null && pNearestEnd != null && pAssociation != null) {
					isDisplayed = labelMgr.isDisplayed((index == 0) ? TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY : TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY);
				}
				actionClass.setChecked(isDisplayed);
				bFlag = isReadOnly ? false : true;
			} else if (id.equals("MBK_SHOW_BOTH_ROLENAMES")) {
				boolean isDisplayed1 = false;
				boolean isDisplayed2 = false;
				if (labelMgr != null) {
					isDisplayed1 = labelMgr.isDisplayed(TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME);
					isDisplayed2 = labelMgr.isDisplayed(TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME);
				}
				if (isDisplayed1 && isDisplayed2) {
					actionClass.setChecked(true);
				}
				bFlag = isReadOnly ? false : true;
			} else if (id.equals("MBK_SHOW_BOTH_MULTIPLICITIES")) {
				boolean isDisplayed1 = false;
				boolean isDisplayed2 = false;
				if (labelMgr != null) {
					isDisplayed1 = labelMgr.isDisplayed(TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY);
					isDisplayed2 = labelMgr.isDisplayed(TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY);
				}
				if (isDisplayed1 && isDisplayed2) {
					actionClass.setChecked(true);
				}
				bFlag = isReadOnly ? false : true;
			} else if (id.equals("MBK_QUALIFIERS")) {
				IPresentationElement pPE = getPresentationElement();
				if (pPE != null && pPE instanceof IAssociationEdgePresentation) {
					IPresentationElement pQualPE = null;
					if (m_ContextMenuLocation == CMPK_START) {
						pQualPE = ((IAssociationEdgePresentation) pPE).getSourceQualifier();
					} else if (m_ContextMenuLocation == CMPK_END) {
						pQualPE = ((IAssociationEdgePresentation) pPE).getTargetQualifier();
					}

					if (pQualPE != null) {
						actionClass.setChecked(true);
					}
				}
				bFlag = isReadOnly ? false : true;
			}

			//set the sensitivities for remaining ids.
			if (id.equals("MBK_LINK_END_REMOVE_AGGREGATE")) {
				if (pAggregation != null && pNearestEnd != null) {
					boolean isAggEnd = pAggregation.isAggregateEnd(pNearestEnd);
					if (isAggEnd) {
						bFlag = isReadOnly ? false : true;
					}
				} else if (pAssociationClass != null && pNearestEnd != null) {
					boolean hasIsComposite = false;
					boolean isComposite = false;
					boolean thisIsAggEnd = false;
					String aggEnd = "";
					String partEnd = "";

					// Get the Aggregation stuff for the AssociationClass which is fixed in UML 2.0, but not SP1
					getAssociationClassAggregationHack(pAssociationClass, pNearestEnd, hasIsComposite, isComposite, thisIsAggEnd, aggEnd, partEnd);

					if (hasIsComposite && thisIsAggEnd) {
						bFlag = isReadOnly ? false : true;
					}
				}
			} else if (
				id.equals("MBK_LINK_END_REVERSE_ENDS")
					|| id.equals("MBK_SET_MULTIPLICITY_0_1")
					|| id.equals("MBK_SET_MULTIPLICITY_0_STAR")
					|| id.equals("MBK_SET_MULTIPLICITY_STAR")
					|| id.equals("MBK_SET_MULTIPLICITY_1")
					|| id.equals("MBK_SET_MULTIPLICITY_1_STAR")) {
				bFlag = isReadOnly ? false : true;
			}

			retVal = bFlag;
		}

		if (!retVal) {
			retVal = super.setSensitivityAndCheck(id, actionClass);
		}

		return retVal;
	}

	/**
	 * Get the Aggregation stuff for the AssociationClass which is fixed in UML 2.0, but not SP1
	 */
	private void getAssociationClassAggregationHack(IAssociationClass pAssociationClass, IAssociationEnd pNearestEnd, boolean hasIsComposite, boolean isComposite, boolean thisIsAggEnd, String aggEnd, String partEnd) {
		if (pAssociationClass != null && pNearestEnd != null) {
			Node pNode = pAssociationClass.getNode();
			if (pNode != null) {
				Node value = XMLManip.getAttribute(pNode, "isComposite");
				if (value != null) {
					hasIsComposite = true;
					isComposite = XMLManip.getAttributeBooleanValue(pNode, "isComposite");
					partEnd = XMLManip.getAttributeValue(pNode, "partEnd");
					aggEnd = XMLManip.getAttributeValue(pNode, "aggregateEnd");

					// See if this end we're drawing is the aggregate end
					String xmiid = pNearestEnd.getXMIID();
					if (xmiid.equals(aggEnd)) {
						thisIsAggEnd = true;
					}
				}
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler#onHandleButton(java.awt.event.ActionEvent, java.lang.String)
	 */
	public boolean onHandleButton(ActionEvent e, String id) {
		boolean handled = handleStandardLabelSelection(e, id);

		if (!handled) {
			IDrawingAreaControl pDiagram = getDrawingArea();
			IETGraphObject pETElement = (IETGraphObject) getParentETElement();
			ILabelManager labelMgr = getLabelManager();
			IPresentationElement pPE = getPresentationElement();
			IElement pElement = null;
			if (pETElement != null) {
				pElement = TypeConversions.getElement(pETElement);
			}

			//get the nearest end
			IAssociationEnd pNearestEnd = null;
			if (m_ContextMenuLocation == CMPK_START) {
				pNearestEnd = m_StartEnd;
			} else if (m_ContextMenuLocation == CMPK_END) {
				pNearestEnd = m_FinishEnd;
			}

			IAssociation pAssociation = null;
			IAssociationClass pAssociationClass = null;
			IAggregation pAggregation = null;
			if (pElement instanceof IAssociation) {
				pAssociation = (IAssociation) pElement;
			}
			if (pElement instanceof IAssociationClass) {
				pAssociationClass = (IAssociationClass) pElement;
			}
			if (pElement instanceof IAggregation) {
				pAggregation = (IAggregation) pElement;
			}

			if (pETElement != null && pDiagram != null) {
				if (id.equals("MBK_LINK_END_ORDINARY_AGGREGATE")) {
					if (pAssociationClass != null) {
						handleAssociationClassTransformHack(id, pAssociationClass, pNearestEnd);
					} else {
						// Transforms the link to an aggregation.  The end we're currently on remains an
						// a non-composite
						pDiagram.transform(pETElement, "Aggregation");
					}
				} else if (id.equals("MBK_LINK_END_COMPOSITE_AGGREGATE")) {
					if (pAssociationClass != null) {
						handleAssociationClassTransformHack(id, pAssociationClass, pNearestEnd);
					} else {
						// Transforms the link to an aggregation.  The end we're currently on remains an
						// a composite
						pDiagram.transform(pETElement, "Composite Aggregation");
					}
				} else if (id.equals("MBK_LINK_END_REMOVE_AGGREGATE")) {
					if (pAssociationClass != null) {
						handleAssociationClassTransformHack(id, pAssociationClass, pNearestEnd);
					} else {
						// Transforms the link to an association
						pDiagram.transform(pETElement, "Association");
					}
				} else if (id.equals("MBK_LINK_END_NAVIGABLE")) {
					// Transforms the end to be navigable.
					if (pNearestEnd != null) {
						if (pNearestEnd instanceof INavigableEnd) {
							IAssociationEnd pEnd = ((INavigableEnd) pNearestEnd).makeNonNavigable();
							if (m_ContextMenuLocation == CMPK_START) {
								setEnd(true, pEnd);
							} else if (m_ContextMenuLocation == CMPK_END) {
								setEnd(false, pEnd);
							}
						} else {
							INavigableEnd pEnd = pNearestEnd.makeNavigable();
							if (m_ContextMenuLocation == CMPK_START) {
								setEnd(true, pEnd);
							} else if (m_ContextMenuLocation == CMPK_END) {
								setEnd(false, pEnd);
							}
						}
					}
				} else if (id.equals("MBK_LINK_END_REVERSE_ENDS")) {
					if (pAssociationClass != null) {
						Node pNode = pAssociationClass.getNode();
						String partEndVal = XMLManip.getAttributeValue(pNode, "partEnd");
						String aggEndVal = XMLManip.getAttributeValue(pNode, "aggregateEnd");
						if (partEndVal != null && aggEndVal != null) {
							//switch the ends
							UMLXMLManip.setAttributeValue(pAssociationClass, "partEnd", aggEndVal);
							UMLXMLManip.setAttributeValue(pAssociationClass, "aggregateEnd", partEndVal);
						}
					} else {
						//reverse the ends
						if (pAggregation != null) {
							pAggregation.reverseEnds();
						}
					}

					// Switch the navigability of the ends
					INavigableEnd startEnd = null;
					INavigableEnd finishEnd = null;
					if (m_StartEnd instanceof INavigableEnd) {
						startEnd = (INavigableEnd) m_StartEnd;
					}
					if (m_FinishEnd instanceof INavigableEnd) {
						finishEnd = (INavigableEnd) m_FinishEnd;
					}

					if ((startEnd == null && finishEnd != null) || (finishEnd == null && startEnd != null)) {
						IAssociationEnd pNewEnd = null;
						INavigableEnd pNewNavEnd = null;
						if (startEnd != null) {
							pNewEnd = startEnd.makeNonNavigable();
							pNewNavEnd = m_FinishEnd.makeNavigable();
							setEnds(pNewEnd, pNewNavEnd);
						} else if (finishEnd != null) {
							pNewNavEnd = m_StartEnd.makeNavigable();
							pNewEnd = finishEnd.makeNonNavigable();
							setEnds(pNewNavEnd, pNewEnd);
						}
					}
				} else if (id.equals("MBK_SHOW_ASSOCIATION_NAME")) {
					if (labelMgr != null) {
						boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_ASSOCIATION_NAME);
						labelMgr.showLabel(TSLabelKind.TSLK_ASSOCIATION_NAME, isDisplayed ? false : true);

						if (!isDisplayed) {
							IPresentationElement presEle = labelMgr.getLabel(TSLabelKind.TSLK_ASSOCIATION_NAME);
							if (presEle != null && presEle instanceof ILabelPresentation) {
								pDiagram.postEditLabel((ILabelPresentation) presEle);
							}
						}
					}
				} else if (id.equals("MBK_SHOW_ROLENAME")) {
					if (labelMgr != null && pNearestEnd != null && pAssociation != null) {
						// Get the index of the nearest end
						int index = pAssociation.getEndIndex(pNearestEnd);

						boolean isDisplayed = labelMgr.isDisplayed((index == 0) ? TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME : TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME);
						labelMgr.showLabel((index == 0) ? TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME : TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME, isDisplayed ? false : true);

						if (!isDisplayed) {
							IPresentationElement presEle = labelMgr.getLabel((index == 0) ? TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME : TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME);
							if (presEle != null && presEle instanceof ILabelPresentation) {
								pDiagram.postEditLabel((ILabelPresentation) presEle);
							}
						}
					}
				} else if (id.equals("MBK_SHOW_MULTIPLICITY")) {
					if (labelMgr != null && pNearestEnd != null && pAssociation != null) {
						// Get the index of the nearest end
						int index = pAssociation.getEndIndex(pNearestEnd);

						boolean isDisplayed = labelMgr.isDisplayed((index == 0) ? TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY : TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY);
						labelMgr.showLabel((index == 0) ? TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY : TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY, isDisplayed ? false : true);

						if (!isDisplayed) {
							IPresentationElement presEle = labelMgr.getLabel((index == 0) ? TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY : TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY);
							if (presEle != null && presEle instanceof ILabelPresentation) {
								pDiagram.postEditLabel((ILabelPresentation) presEle);
							}
						}
					}
				} else if (id.equals("MBK_SHOW_BOTH_ROLENAMES")) {
					if (labelMgr != null) {
						boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME);
						labelMgr.showLabel(TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME, isDisplayed ? false : true);
						labelMgr.showLabel(TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME, isDisplayed ? false : true);
					}
				} else if (id.equals("MBK_SHOW_BOTH_MULTIPLICITIES")) {
					if (labelMgr != null) {
						boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY);
						labelMgr.showLabel(TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY, isDisplayed ? false : true);
						labelMgr.showLabel(TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY, isDisplayed ? false : true);
					}
				} else if (id.equals("MBK_SET_MULTIPLICITY_0_1") || id.equals("MBK_SET_MULTIPLICITY_0_STAR") || id.equals("MBK_SET_MULTIPLICITY_STAR") || id.equals("MBK_SET_MULTIPLICITY_1") || id.equals("MBK_SET_MULTIPLICITY_1_STAR")) {
					if (labelMgr != null && pNearestEnd != null) {
						IMultiplicity pMult = pNearestEnd.getMultiplicity();
						if (pMult != null) {
							if (id.equals("MBK_SET_MULTIPLICITY_0_1")) {
								pMult.setRange2("0", "1");
							} else if (id.equals("MBK_SET_MULTIPLICITY_0_STAR")) {
								pMult.setRange2("0", "*");
							} else if (id.equals("MBK_SET_MULTIPLICITY_STAR")) {
								pMult.setRange("*");
							} else if (id.equals("MBK_SET_MULTIPLICITY_1")) {
								pMult.setRange2("1", "1");
							} else if (id.equals("MBK_SET_MULTIPLICITY_1_STAR")) {
								pMult.setRange2("1", "*");
							}

							if (m_ContextMenuLocation == CMPK_START) {
								labelMgr.showLabel(TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY, true);
								labelMgr.resetLabelsText();
							} else {
								labelMgr.showLabel(TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY, true);
								labelMgr.resetLabelsText();
							}
						}
					}
				} else if (id.equals("MBK_QUALIFIERS")) {
					if (pPE != null && pPE instanceof IAssociationEdgePresentation) {
						IAssociationEdgePresentation assocEdgePE = (IAssociationEdgePresentation) pPE;
						IPresentationElement pQualPE = null;
						if (m_ContextMenuLocation == CMPK_START) {
							pQualPE = assocEdgePE.getSourceQualifier();
							if (pQualPE != null) {
								assocEdgePE.removeQualifierNodeAtSourceLocation();
							} else {
								assocEdgePE.createQualifierNodeAtSourceLocation();
							}
						} else if (m_ContextMenuLocation == CMPK_END) {
							pQualPE = assocEdgePE.getTargetQualifier();
							if (pQualPE != null) {
								assocEdgePE.removeQualifierNodeAtTargetLocation();
							} else {
								assocEdgePE.createQualifierNodeAtTargetLocation();
							}
						}
					}
				}

				pDiagram.refresh(false);
			}
		}

		if (!handled) {
			handled = super.onHandleButton(e, id);
		}
		return handled;
	}

	/**
	 * Handles the transformation of the association class ends.  Will be fixed with UML 2.0.
	 */
	private void handleAssociationClassTransformHack(String id, IAssociationClass pAssociationClass, IAssociationEnd pNearestEnd) {
		if (pAssociationClass != null && pNearestEnd != null && m_StartEnd != null && m_FinishEnd != null) {
			Node pNode = pAssociationClass.getNode();

			String startEnd = m_StartEnd.getXMIID();
			String finishEnd = m_FinishEnd.getXMIID();
			String nearestEnd = pNearestEnd.getXMIID();

			if (id.equals("MBK_LINK_END_ORDINARY_AGGREGATE")) {
				// Typical XML Fragment- 
				// <UML:AssociationClass xmlns:UML="omg.org/UML/1.4" xmi.id="_0.0-6452.17688..148_" 
				//       name="a15" visibility="public" isAbstract="false" 
				//       owner="DCE.906DB957-ECF2-4F5A-93A9-DF99353F8C66" isTransient="false" 
				//       isFinal="false" aggregateEnd="_0.0-3133.8975..148_AE0001" isComposite="false" 
				//       partEnd="_0.0-3133.8975..148_AE0002">
				// - <UML:Association.end>
				//    - <UML:AssociationEnd xmi.id="_0.0-3133.8975..148_AE0001" name="" visibility="public"  
				//       appliedStereotype="" type="_0.0-2284.6860..148_" association="_0.0-6452.17688..148_">
				//       - <UML:TypedElement.multiplicity>
				//          <UML:Multiplicity xmi.id="MULT__0.0-3133.8975..148_AE0001" /> 
				//       </UML:TypedElement.multiplicity>
				//    </UML:AssociationEnd>
				//    - <UML:NavigableEnd xmi.id="_0.0-3133.8975..148_AE0002" name="" visibility="public"  
				//       aggregation="none" appliedStereotype="" type="_0.0-2405.7204..148_"  
				//       association="_0.0-6452.17688..148_">
				//       - <UML:TypedElement.multiplicity>
				//          <UML:Multiplicity xmi.id="MULT__0.0-3133.8975..148_AE0002" /> 
				//       </UML:TypedElement.multiplicity>
				//    </UML:NavigableEnd>
				// </UML:Association.end>
				Node value = XMLManip.getAttribute(pNode, "isComposite");
				if (value != null) {
					UMLXMLManip.setAttributeValue(pAssociationClass, "isComposite", "false");
				} else {
					// Create the attributes that make this an attribute
					UMLXMLManip.setAttributeValue(pAssociationClass, "isComposite", "false");
					UMLXMLManip.setAttributeValue(pAssociationClass, "aggregateEnd", nearestEnd);

					if (nearestEnd.equals(startEnd)) {
						UMLXMLManip.setAttributeValue(pAssociationClass, "partEnd", finishEnd);
					} else {
						UMLXMLManip.setAttributeValue(pAssociationClass, "partEnd", startEnd);
					}
				}
			} else if (id.equals("MBK_LINK_END_COMPOSITE_AGGREGATE")) {
				// Typical XML Fragment- 
				// <UML:AssociationClass xmlns:UML="omg.org/UML/1.4" xmi.id="_0.0-6452.17688..148_" 
				//       name="a15" visibility="public" isAbstract="false" 
				//       owner="DCE.906DB957-ECF2-4F5A-93A9-DF99353F8C66" isTransient="false" 
				//       isFinal="false" aggregateEnd="_0.0-3133.8975..148_AE0001" isComposite="true" 
				//       partEnd="_0.0-3133.8975..148_AE0002">
				// - <UML:Association.end>
				//    - <UML:AssociationEnd xmi.id="_0.0-3133.8975..148_AE0001" name="" visibility="public"  
				//       appliedStereotype="" type="_0.0-2284.6860..148_" association="_0.0-6452.17688..148_">
				//       - <UML:TypedElement.multiplicity>
				//          <UML:Multiplicity xmi.id="MULT__0.0-3133.8975..148_AE0001" /> 
				//       </UML:TypedElement.multiplicity>
				//    </UML:AssociationEnd>
				//    - <UML:NavigableEnd xmi.id="_0.0-3133.8975..148_AE0002" name="" visibility="public"  
				//       aggregation="none" appliedStereotype="" type="_0.0-2405.7204..148_"  
				//       association="_0.0-6452.17688..148_">
				//       - <UML:TypedElement.multiplicity>
				//          <UML:Multiplicity xmi.id="MULT__0.0-3133.8975..148_AE0002" /> 
				//       </UML:TypedElement.multiplicity>
				//    </UML:NavigableEnd>
				// </UML:Association.end>
				Node value = XMLManip.getAttribute(pNode, "isComposite");
				if (value != null) {
					UMLXMLManip.setAttributeValue(pAssociationClass, "isComposite", "true");
				} else {
					// Create the attributes that make this an attribute
					UMLXMLManip.setAttributeValue(pAssociationClass, "isComposite", "true");
					UMLXMLManip.setAttributeValue(pAssociationClass, "aggregateEnd", nearestEnd);

					if (nearestEnd.equals(startEnd)) {
						UMLXMLManip.setAttributeValue(pAssociationClass, "partEnd", finishEnd);
					} else {
						UMLXMLManip.setAttributeValue(pAssociationClass, "partEnd", startEnd);
					}
				}
			} else if (id.equals("MBK_LINK_END_REMOVE_AGGREGATE")) {
				// Typical XML Fragment- 
				// <UML:AssociationClass xmlns:UML="omg.org/UML/1.4"  
				//       xmi.id="DCE.367193D4-5C37-4F51-8B38-4633D1CD70A5"  
				//       owner="DCE.82254AB6-7BB1-4D1D-AB66-649F4E6FAF79" name="new">
				// - <UML:Association.end>
				//    - <UML:AssociationEnd xmi.id="DCE.159414ED-DD5D-4A11-8052-92EEF90A3F7E"  
				//          visibility="private" association="DCE.367193D4-5C37-4F51-8B38-4633D1CD70A5"  
				//          type="DCE.D3166D20-FCF1-49F1-A76E-8CEB0BDB7086">
				//       - <UML:TypedElement.multiplicity>
				//            <UML:Multiplicity xmi.id="DCE.34038187-A16B-4874-8B8D-C6D08010018D" /> 
				//         </UML:TypedElement.multiplicity>
				//      </UML:AssociationEnd>
				//    - <UML:AssociationEnd xmi.id="DCE.1BEEA0FD-78E8-456F-9927-065EB70477FC"  
				//       visibility="private" association="DCE.367193D4-5C37-4F51-8B38-4633D1CD70A5"  
				//       type="DCE.462E082E-25D2-452C-B450-4213BEE2F206">
				//       - <UML:TypedElement.multiplicity>
				//            <UML:Multiplicity xmi.id="DCE.1F0FB772-D3DE-4DEC-9116-C1A217A21D04" /> 
				//         </UML:TypedElement.multiplicity>
				//      </UML:AssociationEnd>
				//   </UML:Association.end>
				// </UML:AssociationClass>
				Element pNodeEle = (Element) pNode;
				Attribute attr = pNodeEle.attribute("aggregateEnd");
				if (attr != null) {
					pNodeEle.remove(attr);
				}
				attr = pNodeEle.attribute("partEnd");
				if (attr != null) {
					pNodeEle.remove(attr);
				}
				attr = pNodeEle.attribute("isComposite");
				if (attr != null) {
					pNodeEle.remove(attr);
				}
			}
		}
	}

	public String getDrawEngineID() {
		return "AssociationEdgeDrawEngine";
	}

	protected final static String NN_NN_META_TYPE = "NN NN";
	protected final static String NN_NA_META_TYPE = "NN NA";
	protected final static String CO_NN_META_TYPE = "CO NN";
	protected final static String AG_NN_META_TYPE = "AG NN";
	protected final static String CO_NA_META_TYPE = "CO NA";
	protected final static String AG_NA_META_TYPE = "AG NA";

	public String getMetaTypeInitString() {
		return NN_NN_META_TYPE;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine#getManagerType(int)
	 */
	public String getManagerMetaType(int nManagerKind) {
		String sManager = null;

		//NL TODO The following looks a bit weird (why not use an if else?) but I am leaving it as is
		String metaType = getMetaTypeOfElement();
		if (metaType != null && !metaType.equals("AssociationClass")) {
			if (nManagerKind == MK_LABELMANAGER) {
				sManager = "AssociationLabelManager";
			}
		}

		if (metaType != null && metaType.equals("AssociationClass")) {
			// Override if we've got an association class
			if (nManagerKind == MK_LABELMANAGER) {
				sManager = "AssociationClassLabelManager";
			} else if (nManagerKind == MK_EVENTMANAGER) {
				sManager = "AssociationClassEventManager";
			}
		}

		return sManager;
	}
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
	 */
	public boolean isDrawEngineValidForModelElement() {

		String currentMetaType = getMetaTypeOfElement();
		return (currentMetaType != null && (currentMetaType.equals("Association") || currentMetaType.equals("Aggregation") || currentMetaType.equals("AssociationEnd")));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine#verifyEdgeEnds()
	 */
	public void verifyEdgeEnds() {
		try {
			IElement pModelElement = this.getPresentation().getFirstSubject();

			setEnds(null, null);

			IAssociation pAssociation = pModelElement instanceof IAssociation ? (IAssociation) pModelElement : null;
			IAssociationClass pAssociationClass = pModelElement instanceof IAssociationClass ? (IAssociationClass) pModelElement : null;

			if (pAssociationClass == null && pAssociation != null) {
				ETList < IAssociationEnd > pAssociationEnds = pAssociation.getEnds();
				long numEnds = pAssociation.getNumEnds();

				if (numEnds == 2) {
					IAssociationEnd pFirstEnd = pAssociationEnds.get(0);
					IAssociationEnd pSecondEnd = pAssociationEnds.get(1);

					// See if either end is navigable (shown with an arrowhead)
					INavigableEnd pNavFirstEnd = pFirstEnd instanceof INavigableEnd ? (INavigableEnd) pFirstEnd : null;
					INavigableEnd pNavSecondEnd = pSecondEnd instanceof INavigableEnd ? (INavigableEnd) pSecondEnd : null;

					//assert (pFirstEnd &pSecondEnd);
					if (pFirstEnd != null && pSecondEnd != null) {
						// See if the aggregation end is a diamond filled or unfilled
						int nEndKind = this.getIEdgePresentation().getNodeEnd(pFirstEnd);

						if (nEndKind == NodeEndKindEnum.NEK_TO) {
							if (!parentDiagramIsReadOnly()) {
								// This is wrong.  It ends up with an incorrect parent/child relationship.  
								// Switch it unless the diagram is readonly
								postSwapEdgeEnds();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onGraphEvent(int)
	 */
	public void onGraphEvent(int nKind) {
		super.onGraphEvent(nKind);
		if (nKind == IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED) {
			selectAllAttachedQualifiers(true);
		}
	}

	/**
	 Selects all the qualifiers
	 *
	 @param bSelect [in] true to select all the qualifiers, otherwise deselect
	 */
	public void selectAllAttachedQualifiers(boolean bSelect) {
		try {
			IPresentationElement pThisPE = getPresentationElement();

			IAssociationEdgePresentation pAssociationEdgePE = pThisPE instanceof IAssociationEdgePresentation ? (IAssociationEdgePresentation) pThisPE : null;

			//assert (pAssociationEdgePE);
			if (pAssociationEdgePE != null) {
				IPresentationElement pQualifierPE = pAssociationEdgePE.getSourceQualifier();

				IProductGraphPresentation pGraphPE = pQualifierPE instanceof IProductGraphPresentation ? (IProductGraphPresentation) pQualifierPE : null;
				if (pGraphPE != null) {
					pGraphPE.setSelected(true);
				}

				pQualifierPE = pAssociationEdgePE.getTargetQualifier();

				pGraphPE = pQualifierPE instanceof IProductGraphPresentation ? (IProductGraphPresentation) pQualifierPE : null;
				if (pGraphPE != null) {
					pGraphPE.setSelected(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("associationedgecolor", Color.BLACK);
		super.initResources();
	}
}
