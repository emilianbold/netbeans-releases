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

package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IUsage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISupportEnums;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.SmartDragTool;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSESolidObject;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.TSENode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSOvalShape;
import java.awt.GradientPaint;

/**
 * @author Embarcadero Technologies Inc.
 *
 * 
 */
public class ETInterfaceDrawEngine extends ETNodeDrawEngine {

	private Color m_defaultFillColor = Color.yellow;

	public String getElementType() {
		String type = super.getElementType();
		if (type == null) {
			type = new String("Interface");
		}
		return type;
	}
	
	/**
	 * Returns the IInterface we're attached to
	 *
	 * @param pInterface [out,retval] Returns the IInterface we're attached to
	 */
	private IInterface getInterface()
	{
		return (IInterface)getFirstModelElement();
		
	}
	
	public void onContextMenu(IMenuManager manager)
	{
		IInterface iFace = getInterface();
		if (iFace != null)
		{
			// Add the context menu items dealing with interface edge
			addInterfaceEdgeMenuItems(manager);
		}
		super.onContextMenu(manager);
	}

	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
	{
		 boolean retVal = false;
		 boolean isReadOnly = isParentDiagramReadOnly();
		 if (id.equals("MBK_SHOW_INTERFACENAME"))
		 {
			ILabelManager labelMgr = getLabelManager();
			IInterface pInterface = getInterface();
			if (labelMgr != null && pInterface != null)
			{
				boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_INTERFACE);
				pClass.setChecked(isDisplayed);
		 		
				retVal = isReadOnly ? false : true;
			}
		 }
		 
		 if (!retVal)
		 {
			super.setSensitivityAndCheck(id, pClass);
		 }
		 return retVal;
	}
   
	public boolean onHandleButton(ActionEvent e, String id)
	{
		boolean handled = false;
		if (id.equals("MBK_SHOW_INTERFACENAME"))
		{
		   ILabelManager labelMgr = getLabelManager();
		   if (labelMgr != null)
		   {
			   boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_INTERFACE);
			   labelMgr.showLabel(TSLabelKind.TSLK_INTERFACE, isDisplayed ? false : true);
		 		
			   invalidate();
			   handled = true;
		   }
		}
		
		if (!handled)
		{
			handled = super.onHandleButton(e, id);
		}
		return handled;
	}

	public String getDrawEngineID() 
	{
		return "InterfaceDrawEngine";
	}
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void drawContents(IDrawInfo pDrawInfo) {
		
		FixEdgeConnections();
		
		if(isConnector()) {
			if(isImplementOrInterfaceConnector()) {
				drawLittleLollipop(pDrawInfo);
			}
			drawDish(pDrawInfo);
		}
		else
		{
			drawLollipop(pDrawInfo);
		}
	}
	
	private boolean isImplementationOrInterface(IElement element)
	{
		return (element.getElementType().compareTo("Interface") == 0 
				|| element.getElementType().compareTo("Implementation") == 0 );
	}
	
	private void FixEdgeConnections()
	{
		try
		{
			final boolean isConnector = isConnector();
			 
			INodePresentation myNode = getNodePresentation();
			
			ETList < IETGraphObject > edgeList = myNode.getEdges(true,true);
			
			IteratorT< ETEdge > iter = new IteratorT < ETEdge >(edgeList);
			 
			while(iter.hasNext())
			{
				ETEdge edge = iter.next();

				if(!isConnector){
					RemoveTSConnector(edge);
				}
				else{					
					IElement edgeElement = TypeConversions.getElement((IETGraphObject)edge);
					if( isImplementationOrInterface(edgeElement) )
					{
						ConnectToCenter(edge);
					}
					else if(edgeElement.getElementType().compareTo("Usage") == 0) {
						// RemoveTSConnector(edge);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void ConnectToCenter(ETEdge edge) {
		TSENode thisNode = getNode();
		TSConnector connector = null;
		/* jyothi
		boolean useTarget = (thisNode == edge.getTargetNode());
		
		if(useTarget) {
			connector = edge.getTargetConnector();
		}
		else {
			connector = edge.getSourceConnector();
		}
		
		if(connector == null || connector == thisNode.getDefaultConnector()) {
			connector = thisNode.addConnector();
			
			connector.setVisible(false);
			connector.setMovable(false);
			
			if(useTarget) {
				edge.setTargetConnector(connector);
			}
			else {
				edge.setSourceConnector(connector);
			}
			// TODO repairOrthogonalRouting
		}
                 */
	}
	
	private void RemoveTSConnector(ETEdge edge) {
		TSENode thisNode = getNode();
		TSConnector connector = null;
		/* jyothi
		boolean useTarget = (thisNode == edge.getTargetNode());
		
		if(useTarget) {
			connector = edge.getTargetConnector();
		}
		else {
			connector = edge.getSourceConnector();
		}
		
		if(connector != null && !connector.isSpecified() && connector != thisNode.getDefaultConnector() ) {
			try{
				thisNode.remove(connector);
			} catch (Exception e){
				ETSystem.out.println("!!!Watch out!!! - ETInterfaceDrawEngine.RemoveTSConnector() - TS is throwing a runtime in at com.tomsawyer.drawing.TSDNode.remove(Unknown Source)");
			}
		}
                 */
	}
	
	private void drawLittleLollipop(IDrawInfo pDrawInfo) {
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		
		ETGenericNodeUI parentUI = (ETGenericNodeUI) this.getParent();
		TSESolidObject ownerNode = parentUI.getOwnerNode();
		TSConstRect localBounds = ownerNode.getLocalBounds();
		
		double shrinkWidth = localBounds.getWidth()/4;
		double shrinkHeight = localBounds.getHeight()/4;
		
		TSConstRect littleBounds = new TSConstRect(
			localBounds.getLeft()+shrinkWidth,
			localBounds.getTop()-shrinkHeight, 
			localBounds.getRight()-shrinkWidth,
			localBounds.getBottom()+shrinkHeight); 
		
                float centerX = (float) littleBounds.getCenterX();
                GradientPaint paint = new GradientPaint(centerX, (int) littleBounds.getBottom(), getFillColor(), centerX, (int) littleBounds.getTop(), getLightGradientFillColor());
		//graphics.setColor(parentUI.getFillColor());
		graphics.setPaint(paint);
		graphics.fillOval(littleBounds);
		
		//graphics.setColor(parentUI.getBorderColor());
		graphics.setColor(getBorderColor());
		graphics.drawOval(littleBounds);
	}
	
	private void drawDish(IDrawInfo pDrawInfo) {
		try
		{
			TSEGraphics graphics = pDrawInfo.getTSEGraphics();
			
			ETGenericNodeUI parentUI = (ETGenericNodeUI) this.getParent();
			TSESolidObject ownerNode = parentUI.getOwnerNode();
			TSConstRect boundingRect  = ownerNode.getLocalBounds();
			TSConstPoint centerPoint = boundingRect.getCenter();
			INodePresentation myNode = getNodePresentation();
				
			ETList < IETGraphObject > edgeList = myNode.getEdges(true,true);
				
			IteratorT< ETEdge > iter = new IteratorT < ETEdge >(edgeList);
				 
			while(iter.hasNext())
			{
				ETEdge edge = iter.next();
				IElement edgeElement = TypeConversions.getElement((IETGraphObject)edge);
				if(edgeElement.getElementType().compareTo("Usage") == 0 )
				{
					TSRect arcRect = new TSRect();
					 	
					int startAngle = 0;
					int sweepAngle = 0;
										
					IUsage usage = (IUsage)edgeElement;
					
					if(usage.getSupplier().isSame(getFirstModelElement()))
					{
						TSConstPoint sourcePoint = edge.getSourcePoint();
						
						double xOffset = sourcePoint.getX() - centerPoint.getX();
						double yOffset = sourcePoint.getY() - centerPoint.getY();
				
						if (Math.abs(yOffset) > Math.abs(xOffset))
						{
							// draw either north or south
							arcRect.setLeft(boundingRect.getLeft());
							arcRect.setRight(boundingRect.getRight());
							startAngle = 0;
							
						   	if( yOffset > 0)
						   	{
						      	// Draw Northern
						      	arcRect.setTop(boundingRect.getTop());
								arcRect.setBottom(centerPoint.getY());
								//	double the height to get the right shape								
								arcRect.setBottom(arcRect.getBottom() - arcRect.getHeight());

						      	sweepAngle = 180;						      	
						   	}
						   	else
						   	{
							  	// Draw Southern
							  	arcRect.setTop(centerPoint.getY());
								arcRect.setBottom(boundingRect.getBottom());
								//	double the height to get the right shape
								arcRect.setTop(arcRect.getTop() + arcRect.getHeight());
								
							  	sweepAngle = -180;
						   	}
						}
						else
						{
							// draw either east or west
							arcRect.setTop(boundingRect.getTop());
							arcRect.setBottom(boundingRect.getBottom());
							startAngle = 90;
							
						   	if (xOffset > 0)
						   	{
						      	// Draw Eastern
						      	arcRect.setLeft(centerPoint.getX());
								arcRect.setRight(boundingRect.getRight());
								// double the width to get the right shape
								arcRect.setLeft(arcRect.getLeft() - arcRect.getWidth());
					
						      	sweepAngle = -180;
						   	}
						   	else
						   	{
						    	// Draw Western
						    	arcRect.setLeft(boundingRect.getLeft());
								arcRect.setRight(centerPoint.getX());
								// double the width to get the right shape
								arcRect.setRight(arcRect.getRight() + arcRect.getWidth());			
								
								sweepAngle = 180;
						   	}
						}																								
					}
					graphics.drawArc(arcRect,startAngle,sweepAngle);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
			
	}
	
	private boolean isConnector()
	{
		boolean foundUsage = false;
		try
		{
			INodePresentation myNode = getNodePresentation();
			
			ETList < IETGraphObject > edgeList = myNode.getEdges(true,true);
			
			IteratorT< IETGraphObject > iter = new IteratorT < IETGraphObject >(edgeList);
			 
			while(iter.hasNext())
			{
				IETGraphObject edge = iter.next();
				IElement edgeElement = TypeConversions.getElement(edge);
				if(edgeElement != null && edgeElement.getElementType().compareTo("Usage") == 0 )
				{
					foundUsage = true;
					break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return foundUsage;
	}

	private boolean isImplementOrInterfaceConnector()
	{
		boolean found = false;
		try
		{
			INodePresentation myNode = getNodePresentation();
			
			ETList < IETGraphObject > edgeList = myNode.getEdges(true,true);
			
			IteratorT< IETGraphObject > iter = new IteratorT < IETGraphObject >(edgeList);
			 
			while(iter.hasNext())
			{
				IETGraphObject edge = iter.next();
				IElement edgeElement = TypeConversions.getElement(edge);
				if( isImplementationOrInterface(edgeElement) )
				{
					found = true;
					break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return found;
	}

	private void drawLollipop(IDrawInfo pDrawInfo) {
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
                IETRect pBoundingRect = pDrawInfo.getDeviceBounds();
                float centerX = (float) pBoundingRect.getCenterX();
                GradientPaint paint = new GradientPaint(centerX, pBoundingRect.getBottom(), getFillColor(), centerX, pBoundingRect.getTop(), getLightGradientFillColor());
		GDISupport.drawEllipse(graphics,pBoundingRect.getRectangle(), getBorderColor(), paint);
	/*	
		ETGenericNodeUI parentUI = (ETGenericNodeUI) this.getParent();
		TSESolidObject ownerNode = parentUI.getOwnerNode();
		TSConstRect localBounds = ownerNode.getLocalBounds();
		
		// J1573-Interface (as icon) border has gaps
		localBounds = new TSConstRect(localBounds.getLeft()+1, localBounds.getTop()-1, localBounds.getRight()-1, localBounds.getBottom()+1); 
			
		//graphics.setColor(m_defaultFillColor);
		graphics.setColor(getFillColor());
		graphics.fillOval(localBounds);
		
		//graphics.setColor(parentUI.getBorderColor());
		graphics.setColor(getBorderColor());
		graphics.drawOval(localBounds);
	*/
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
		IETSize retVal = new ETSize( 20, 20 );

      if( !bAt100Pct &&
          (retVal != null) )
      {
         TSTransform transform = pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform();
         retVal = scaleSize( retVal, transform );
      }
      
      return retVal;
	}
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
    */
   public void sizeToContents()
   {
      TSENode ownerNode = getOwnerNode();
      if (ownerNode != null)
      {
         IETSize size = calculateOptimumSize(null, false);
			resize(size, false);
         ownerNode.setShape(new TSOvalShape());
         // CLEAN ownerNode.setResizability( TSESolidObject.RESIZABILITY_LOCKED );
      }
   }
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine#getManagerMetaType()
	 */
	public String getManagerMetaType(int nManagerKind) {
		String sManager = null;

		if (nManagerKind == MK_LABELMANAGER) {
			sManager = "InterfaceLabelManager";
			
		} else if (nManagerKind == MK_EVENTMANAGER) {
			IElement modelElement = getFirstModelElement();

			if (modelElement != null && modelElement instanceof IInterface) {
				sManager = "ADInterfaceEventManager";
			}
		}
		
		return sManager;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButton(java.awt.event.MouseEvent)
	 */
	public boolean handleLeftMouseButtonPressed(MouseEvent pEvent) {
		IDrawingAreaControl daCtrl = getDrawingArea();
		ADGraphWindow graphWindow = daCtrl != null ? daCtrl.getGraphWindow() : null;

		SmartDragTool dragTool = createSmartDragTool(pEvent);
			
		if(dragTool == null)
			return false;
						
		//graphWindow.getCurrentState().setState(dragTool);
                graphWindow.getCurrentTool().setTool(dragTool);
		dragTool.onMousePressed(pEvent);
			
		return true;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		setFillColor("ellipsefill", 251, 233, 126);
                setLightGradientFillColor("ellipselightgradientfill", 254, 254, 254);
		setBorderColor("ellipseborder", Color.BLACK);

		super.initResources();
	}
}
