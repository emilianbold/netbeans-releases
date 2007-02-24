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


//	 $Date$
package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdgeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.UIResources;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.drawing.TSDEdge;
import com.tomsawyer.drawing.TSGNode;
import com.tomsawyer.drawing.TSPEdge;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.jnilayout.TSPathEdgeIter;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSConstSegment;
import com.tomsawyer.drawing.geometry.TSConstSegment;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

public class MessageConnectorArrowCompartment extends ETCompartment implements IMessageConnectorArrowCompartment {

	private Color m_defaultBorderColor = Color.black;
	private int m_nConnectorArrowColorStringID = -1;


	/**
	 * Create a copy of yourself.
	 *
	 * @param pParentDrawEngine [in] The parent draw engine for this compartment
	 * @param pRetCompartment [out,retval] The clone of this compartment
	 */
	public ICompartment clone(IDrawEngine pParentDrawEngine) {
		ICompartment pRetCompartment = null;

		IMessageConnectorArrowCompartment pNewCompartment = (IMessageConnectorArrowCompartment) CreationFactoryHelper.createCompartment("MessageConnectorArrowCompartment");

		if (pNewCompartment != null) {
			pNewCompartment = this;

			pRetCompartment = pNewCompartment;
			pNewCompartment.setEngine(pParentDrawEngine);
		}

		return pRetCompartment;
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID() {
		return "MessageConnectorArrowCompartment";
	}

	/**
	 * Calculates the "best" size for this compartment.  The calculation sets the member variable m_szCachedOptimumSize,
	 * which represents the "best" size of the compartment at 100%.
	 *
	 * @param pCDC[in] This is the CDC* represented as an OLE_HANDLE
	 * @param nX[out] This is size (width) of "best" size of the compartment
	 * @param nY[out] This is size (height) of "best" size of the compartment
	 * @param bAt100Pct[in] nX,nY is either in current zoom or 100% based on this flag.  If bAt100Pct then it's at 100%.
	 */
	public IETSize calculateOptimumSize(IDrawInfo pInfo, boolean bAt100Pct) {
		
		IETSize returnSize = new ETSize(20,20);

		internalSetOptimumSize(returnSize);
		//return getOptimumSize(bAt100Pct);
		return bAt100Pct ? returnSize : scaleSize(returnSize, pInfo != null ? pInfo.getTSTransform() : getTransform());
	}
	/**
	 * Draws a Package.
	 *
	 * @param pInfo[in] An IDrawInfo structure containing the data to draw
	 * @param inTSBoundingRect[in] A rect describing the bounds in which for this compartment
	 * to draw itself.  The compartment must not draw outside this rect.  The rect is in
	 * device coordinates.
	 */
	public void draw(IDrawInfo pInfo, IETRect inTSBoundingRect) {

		IETRect boundingRect = ETDeviceRect.ensureDeviceRect(inTSBoundingRect);
//		CRectConversions : : ETRectToRECT(inTSBoundingRect, boundingRect);
//
//		TSEDrawInfo * pTSEDrawInfo = CTypeConversions : : GetTSEDrawInfo(pInfo);

		TSEGraphics graphics = pInfo.getTSEGraphics();

		if (pInfo != null && graphics != null) {
			
			graphics = pInfo.getTSEGraphics();

//			COLORREF crBorderColor = GetColorDefaultText(CK_BORDERCOLOR, pTSEDrawInfo - > dc());
//			CGDISupport : : DrawRectangle(pTSEDrawInfo - > dc(), boundingRect, crBorderColor);

			graphics.setColor(m_defaultBorderColor);
			graphics.drawRect(graphics.getTSTransform().boundsToWorld((Rectangle)boundingRect));

			// Call the base class
			super.draw(pInfo, inTSBoundingRect);
		}
	}

	/**
	 * Establish names for our resources.
	 */
	public void initResources()
	{
		m_nConnectorArrowColorStringID = m_ResourceUser.setResourceStringID(m_nConnectorArrowColorStringID, "connectorarrowcolor");
		super.initResources();
	}

	/**
	 * Return our default resource IDs.
	 */
	protected String getDefaultResourceID(int nKind) {

		String sRetVal = "";

		switch (nKind) {
			case UIResources.CK_BORDERCOLOR :
				sRetVal = "ConnectorArrowColor";
				break;
		}
		return sRetVal;
	}

	/**
	 * Notifier that the model element has changed, if available the changed IFeature is passed along.
	 *
	 * @param pTargets[in] Information about what has changed
	 */
	public long modelElementHasChanged(INotificationTargets pTargets) {

		int nKind = ModelElementChangedKind.MECK_UNKNOWN;

		IElement pChangedME = pTargets.getChangedModelElement();
		nKind = pTargets.getKind();

		IMessage pMessage = null;
		if (pChangedME instanceof IMessage)
		{
			pMessage = (IMessage) pChangedME;
		}
		
		IElement cpElement = this.getModelElement();
		
		if (pMessage != null && cpElement != null) {
			boolean bIsSame = false;

			bIsSame = cpElement.isSame(pMessage);
			if (bIsSame) {
				// The kind of message may have changed so we may need to redraw
			}
		}
		
		return 0;
	}
	/**
	 * Returns the IMessage and IMessageConnector we represent
	 *
	 * @param pConnector [out] The message connector the link that owns this label represents
	 * @param pMessage [out] The message this label represents
	 */
	protected ETPairT < IMessageConnector, IMessage > getMetaData() {

		ETPairT < IMessageConnector, IMessage > retVal = new ETPairT < IMessageConnector, IMessage > ();

		IMessageConnector pConnector = null;
		IMessage pMessage = null;

		// The presentation element of othe owning edge should be attached to the message connector 
		TSEEdge ownerEdge = getOwnerEdge();
		
		if (ownerEdge != null) {
			
			IElement pME = TypeConversions.getElement(ownerEdge);
			if (pME != null && pME instanceof IMessageConnector) {
				pConnector = (IMessageConnector)pME;
			}
		}

		// The model element the draw engine is attached to is the message
		IDrawEngine pEngine = getEngine();
		
		if (pEngine != null) {
			IElement pEngineElement = TypeConversions.getElement(pEngine);
			if (pEngineElement != null && pEngineElement instanceof IMessage) {
				pMessage = (IMessage)pEngineElement;
			}
		}

		retVal.setParamOne(pConnector);
		retVal.setParamTwo(pMessage);

		return retVal;
	}
	/**
	 * Draws the message connector compartment and returns where the sibling compartment should lie
	 *
	 * @param pInfo [in] Information about the draw event (ie the DC, are we printing...)
	 * @param boundingRect [in] The bounding rect to draw into
	 * @param pSiblingCompartmentSize [in] The size of the sibling compartment
	 * @param pSiblingCompartmentDrawRect [out,retval] Where the sibling compartment should draw
	 */
	public IETRect draw2(IDrawInfo pInfo, IETRect boundingRect, IETSize pSiblingCompartmentSize)
	{
		TSEGraphics graphics = pInfo.getTSEGraphics();
		TSTransform transform = graphics.getTSTransform();

		IETRect pSiblingCompartmentDrawRect = null;

		TSEEdgeLabel ownerLabel = getOwnerLabel();
		TSEEdge ownerEdge = getOwnerEdge();

		TSConstPoint labelCenter = ownerLabel.getCenter();
			
		ILifeline pReceivingLifeline = null;
			
		int nEndKind = NodeEndKindEnum.NEK_UNKNOWN;
		int nKind = IMessageKind.MK_UNKNOWN;
		
		double x = boundingRect.getLeft();
		double y = boundingRect.getBottom();
		
		double left = x;
		double top = boundingRect.getTop();

		// Figure out what end of the line the arrowhead should be
	    ETPairT < IMessageConnector, IMessage> meta = getMetaData();
		
		IMessageConnector pConnector = meta.getParamOne();
		IMessage pMessage = meta.getParamTwo();
		
		if (pMessage != null && ownerEdge != null)
		{
			nKind = pMessage.getKind();
			pReceivingLifeline = pMessage.getReceivingLifeline();

			IEdgePresentation pEdgePresentation = TypeConversions.getEdgePresentation(ownerEdge);

			if (pEdgePresentation != null)
			{
				nEndKind = pEdgePresentation.getNodeEnd(pReceivingLifeline);
			}
		}

		if (nEndKind != NodeEndKindEnum.NEK_UNKNOWN && ownerLabel != null && labelCenter != null)
		{
//			// Here's the rect that'll contain the retangle where the name compartment (the sibling) should draw
//			pSiblingCompartmentDrawRect = new ETRect(boundingRect.getLeft(), // left
//													boundingRect.getTop(), // top
//													Math.min(boundingRect.getIntWidth(), pSiblingCompartmentSize.getWidth()), // right
//													Math.min(boundingRect.getIntHeight(), pSiblingCompartmentSize.getHeight())); // bottom

			// Get the angle that the arrow should be from horizontal (+ is up), in degrees
			long nAngle = 0;
			double foundDistance = Double.MAX_VALUE;
			
//			ETPairT <Long, Double> angleAndDistance = getAngleFromHorizontal(labelCenter);
//			nAngle = ((Long)angleAndDistance.getParamOne()).longValue();
//			foundDistance = ((Double)angleAndDistance.getParamTwo()).doubleValue();
			
			// use four corner points to calculate shortest distance to the link edge and decide 
			// where to place the arrow comparment
			
			ETPairT <Long, Double> angleAndDistance1 = getAngleFromHorizontal(ownerLabel.getRight(), ownerLabel.getBottom());
			nAngle = ((Long)angleAndDistance1.getParamOne()).longValue();
			double foundDistance1 = ((Double)angleAndDistance1.getParamTwo()).doubleValue();
			
			ETPairT <Long, Double> angleAndDistance2 = getAngleFromHorizontal(ownerLabel.getLeft(), ownerLabel.getBottom());
			double foundDistance2 = ((Double)angleAndDistance2.getParamTwo()).doubleValue();
			
			ETPairT <Long, Double> angleAndDistance3 = getAngleFromHorizontal(ownerLabel.getLeft(), ownerLabel.getTop());
			double foundDistance3 = ((Double)angleAndDistance3.getParamTwo()).doubleValue();
			
			ETPairT <Long, Double> angleAndDistance4 = getAngleFromHorizontal(ownerLabel.getRight(), ownerLabel.getTop());
			double foundDistance4 = ((Double)angleAndDistance4.getParamTwo()).doubleValue();
			
			double[] distances = {foundDistance1, foundDistance2, foundDistance3, foundDistance4};

			// Draw the line through the center of the remaining rectangle
			IETRect remainingRectangle = new ETRect(boundingRect.getLeft(), boundingRect.getTop() + pSiblingCompartmentSize.getHeight(), boundingRect.getIntWidth(), boundingRect.getIntHeight() - pSiblingCompartmentSize.getHeight());

			Color crBorderColor = new Color(m_ResourceUser.getCOLORREFForStringID(m_nConnectorArrowColorStringID));

			double cosAngle = Math.cos((float) nAngle * 3.14 / 180.f);
			double sinAngle = Math.sin((float) nAngle * 3.14 / 180.f);
			
			double deltaWidth = cosAngle * Math.min(remainingRectangle.getWidth() / 2, remainingRectangle.getHeight() / 2);
			double deltaHeight = sinAngle * Math.min(remainingRectangle.getWidth() / 2, remainingRectangle.getHeight() / 2);
			
			Arrays.sort(distances);
			double shortest = distances[0];
			if (shortest == foundDistance1)
			{
				x = boundingRect.getBottomRight().getX();
				y = boundingRect.getBottomRight().getY();
				left = boundingRect.getLeft();
				top = boundingRect.getTop();
			}else if (shortest == foundDistance2)
			{
				x = boundingRect.getBottomLeft().getX();
				y = boundingRect.getBottomLeft().getY();
				left = x + Math.abs(deltaWidth);
				top = boundingRect.getTop();
			}else if (shortest == foundDistance3)
			{
				x = boundingRect.getTopLeft().getX();
				y = boundingRect.getTopLeft().getY();
				left = x + Math.abs(deltaWidth);
				top = y + Math.abs(deltaHeight);
			}else if (shortest == foundDistance4)
			{
				x = boundingRect.getTopRight().getX();
				y = boundingRect.getTopRight().getY();
				left = boundingRect.getLeft();
				top = y + Math.abs(deltaHeight);
			}
			
			IETPoint pt1 = new ETPoint((int)(x + deltaWidth), (int)(y + deltaHeight));
			IETPoint pt2 = new ETPoint((int)(x - deltaWidth), (int)(y - deltaHeight));
			// Here's the rect that'll contain the retangle where the name compartment (the sibling) should draw

			pSiblingCompartmentDrawRect = new ETRect(left, // left
													top, // top
													Math.min(boundingRect.getIntWidth(), pSiblingCompartmentSize.getWidth()) - deltaWidth, // right
													Math.min(boundingRect.getIntHeight(), pSiblingCompartmentSize.getHeight()) - deltaHeight); // bottom
//			pSiblingCompartmentDrawRect = new ETRect(boundingRect.getLeft() + deltaWidth, // left
//													boundingRect.getTop() - deltaHeight, // top
//													Math.min(boundingRect.getIntWidth(), pSiblingCompartmentSize.getWidth()) - deltaWidth, // right
//													Math.min(boundingRect.getIntHeight(), pSiblingCompartmentSize.getHeight()) - deltaHeight); // bottom
//			// Account for some rounding error
//			if (Math.abs(pt1.getX() - pt2.getX()) < 3)
//			{
//				pt1.setX(pt2.getX());
//			}
//			if (Math.abs(pt1.getY() - pt2.getY()) < 3)
//			{
//				pt1.setY(pt2.getY());
//			}
//
			IETPoint arrowheadPt1 = pt1; // Matches nEndKind == NEK_TO
			IETPoint arrowheadPt2 = pt2;
			if (nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_BOTH)
			{
				// Swap the points
				arrowheadPt1 = pt2;
				arrowheadPt2 = pt1;
			}

			// Fix W6725:  Draw the appropriate line, and arrow type based on the message kind
			int nArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW; // Matches Synchronous types
			switch (nKind)
			{
				case IMessageKind.MK_CREATE :
					nArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
					GDISupport.drawDottedLine(pInfo.getTSEGraphics(), arrowheadPt1.asPoint(), arrowheadPt2.asPoint(), crBorderColor);
					break;

				default :
					//ASSERT(false); // did we add another message kind:
					// no break;

				case IMessageKind.MK_SYNCHRONOUS :
					nArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_FILLED;
					GDISupport.drawLine(pInfo.getTSEGraphics(), arrowheadPt1.asPoint(), arrowheadPt2.asPoint(), crBorderColor, 2);
					break;

				case IMessageKind.MK_ASYNCHRONOUS :
					nArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
					GDISupport.drawLine(pInfo.getTSEGraphics(), arrowheadPt1.asPoint(), arrowheadPt2.asPoint(), crBorderColor, 2);
					break;

				case IMessageKind.MK_RESULT :
					nArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_FILLED;
					GDISupport.drawDottedLine(pInfo.getTSEGraphics(), arrowheadPt1.asPoint(), arrowheadPt2.asPoint(), crBorderColor);
					break;
			}

			GDISupport.drawArrowHead(pInfo, (IETEdge)ownerEdge, (IETEdgeUI)ownerEdge.getUI(), new TSConstPoint(transform.xToWorld(arrowheadPt1.getX()), transform.yToWorld(arrowheadPt1.getY())), new TSConstPoint(transform.xToWorld(arrowheadPt2.getX()), transform.yToWorld(arrowheadPt2.getY())), nArrowheadKind, crBorderColor, new Color(0, 0, 0));

			// Call the base class
			//super.draw(pInfo, pRect);

		}
		return pSiblingCompartmentDrawRect;
		//return boundingRect;
	}
	/**
	 * Get the angle that the arrow should be from horizontal , in degrees
	 *
	 * @param centerPoint [in] The center of this label
	 * @param nAngle [out] The angle from horizontal that this arrow wants to be
	 * @param foundDistance [out] The distance to the nearest edge
	 */
//	protected ETPairT < Long, Double > getAngleFromHorizontal(TSConstPoint centerPoint)
//	{
//		ETPairT < Long, Double > retVal = new ETPairT < Long, Double  > ();
//
//		long nAngle = 0;
//		double foundDistance = Double.MAX_VALUE;
//
//		TSEEdge ownerEdge = getOwnerEdge();
//
//		if (ownerEdge != null)
//		{
//			TSPoint tspoint = new TSPoint(centerPoint.getX(), centerPoint.getY());
//			ETPairT<TSPEdge, Double> val = closestPathEdge(tspoint, ownerEdge);
//			TSPEdge pClosestEdge = val.getParamOne();
//			foundDistance = ((Double)val.getParamTwo()).doubleValue();
//			if (pClosestEdge != null)
//			{
//				TSConstPoint sourcePoint = pClosestEdge.getSourcePoint();
//				TSConstPoint targetPoint = pClosestEdge.getTargetPoint();
//
//				double degreesInRad = -Math.atan2(sourcePoint.getY() - targetPoint.getY(), sourcePoint.getX() - targetPoint.getX());
//
//				nAngle = (long) ((double) degreesInRad * 180 / 3.14f);
//			}
//		}
//			
//		retVal.setParamOne(new Long(nAngle));
//		retVal.setParamTwo(new Double(foundDistance));
//
//		return retVal;
//
//	}
	
		protected ETPairT < Long, Double > getAngleFromHorizontal(double x, double y)
	{
		ETPairT < Long, Double > retVal = new ETPairT < Long, Double  > ();

		long nAngle = 0;
		double foundDistance = Double.MAX_VALUE;

		TSEEdge ownerEdge = getOwnerEdge();

		if (ownerEdge != null)
		{
			TSPoint tspoint = new TSPoint(x, y);
			ETPairT<TSPEdge, Double> val = closestPathEdge(tspoint, ownerEdge);
			TSPEdge pClosestEdge = val.getParamOne();
			foundDistance = ((Double)val.getParamTwo()).doubleValue();
			if (pClosestEdge != null)
			{
				TSConstPoint sourcePoint = pClosestEdge.getSourcePoint();
				TSConstPoint targetPoint = pClosestEdge.getTargetPoint();

				double degreesInRad = -Math.atan2(sourcePoint.getY() - targetPoint.getY(), sourcePoint.getX() - targetPoint.getX());

				nAngle = (long) ((double) degreesInRad * 180 / 3.14f);
			}
		}
			
		retVal.setParamOne(new Long(nAngle));
		retVal.setParamTwo(new Double(foundDistance));

		return retVal;

	}
	
	/**
	 * Returns the owner edge
	 *
	 * @param return [in] The edge owning this label
	 */
	protected TSEEdge getOwnerEdge()
	{
		TSEEdge pReturnEdge = null;

		IPresentationElement pPE = null;
		if (this.getEngine() != null) {
			pPE = TypeConversions.getPresentationElement(this.getEngine());
		}

		if (pPE != null) {
			pReturnEdge = TypeConversions.getOwnerEdge(pPE, true);
		}

		return pReturnEdge;
	}

	/**
	 * Returns the owner Label
	 *
	 * @param return [in] The Label owning compartment's draw engine
	 */
	protected TSEEdgeLabel getOwnerLabel() {
		
		TSEEdgeLabel pReturnLabel = null;

		IPresentationElement pPE = null;
			
			if (this.getEngine() != null) {
				pPE = TypeConversions.getPresentationElement(this.getEngine());
			}

			if (pPE != null) {
				pReturnLabel = (TSEEdgeLabel)TypeConversions.getETGraphObject(pPE);
			}

		return pReturnLabel;
	}
	/**
	 * This function returns the distance between the input point and
	 * the point along the input path edge that is closest to the input
	 * point.
	 */
	protected double calculateDistance(TSPoint pQueryPoint, TSPEdge pPathEdge)
	{
		
		TSNode pSourceNode = pPathEdge.getSourceNode();
		TSNode pTargetNode = pPathEdge.getTargetNode();
		TSConstPoint point1 = ((TSGNode)pSourceNode).getBounds().getCenter();
		TSConstPoint point2 = ((TSGNode)pTargetNode).getBounds().getCenter();

		double a = point1.distance(point2);
		double c = point1.distance(pQueryPoint);
		double d = point2.distance(pQueryPoint);

//		double maxDist = Math.max(c, d);
//		double factor = 3.0 * (maxDist / a);
//
//		TSConstPoint newPoint1 = new TSConstPoint((pQueryPoint.getX() + factor * (point2.getY() - point1.getY())), (pQueryPoint.getY() + factor * (point1.getX() - point2.getX())));
//		TSConstPoint newPoint2 = new TSConstPoint((pQueryPoint.getX() - factor * (point2.getY() - point1.getY())), (pQueryPoint.getY() - factor * (point1.getX() - point2.getX())));
//
//		double x1 = point1.getX();
//		double y1 = point1.getY();
//		double x2 = point2.getX();
//		double y2 = point2.getY();

		double result = 0.0;
		
	
		result = Math.sqrt(d*d - (a*a+d*d-c*c)/(2*a)*((a*a+d*d-c*c)/(2*a)));
		

//		if (TSConstSegment.intersection(x1, y1, x2, y2, newPoint1.getX(), newPoint1.getY(), newPoint2.getX(), newPoint2.getY()) != null)
//		{
//			result = point1.distance(pQueryPoint);
//		}
//		else
//		{
//			result = Math.min(point1.distance(pQueryPoint), point2.distance(pQueryPoint));
//		}

		return result;
	}
	
	/**
	 * This method returns the path edge of the input edge that is closest
	 * to the query point.
	 */
	protected ETPairT<TSPEdge, Double> closestPathEdge(TSPoint pQueryPoint, TSDEdge pEdge)
	{
		ETPairT<TSPEdge, Double> retVal = new ETPairT<TSPEdge, Double>();
		
		TSPEdge pClosestSegment = null;
		
		Iterator pathEdgeIter = pEdge.pathIterator();
		
		double distance = Double.MAX_VALUE;
		double newDistance = 0;

		while (pathEdgeIter.hasNext())
		{
			TSPEdge pPathEdge = (TSPEdge)pathEdgeIter.next();

			newDistance = calculateDistance(pQueryPoint, pPathEdge);

			if (newDistance < distance)
			{
				distance = newDistance;
				pClosestSegment = pPathEdge;
			}
		}
		
		retVal.setParamOne(pClosestSegment);
		retVal.setParamTwo(new Double(distance));

		return retVal;
	}

}
