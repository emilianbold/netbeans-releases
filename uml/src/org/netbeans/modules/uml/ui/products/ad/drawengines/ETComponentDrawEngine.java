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
import java.awt.GradientPaint;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADRelationshipDiscovery;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IADInterfaceEventManager;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PresentationHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.jnilayout.TSSide;
import org.netbeans.modules.uml.ui.support.TSSide;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

public class ETComponentDrawEngine extends ETContainerDrawEngine implements IComponentDrawEngine
{

   protected final int NODE_WIDTH = 180;
   protected final int NODE_HEIGHT = 100;

   private final static int s_PortOffset = 10;
   private final static int s_PortSize = 10;

   // Our original size before layout, restored after layout
   IETRect m_originalSize;

   ETNodeDrawEngine.ETHiddenNodeList m_HiddenNodes = null;
   ETList < PortLocations > m_PortLocations = new ETArrayList < PortLocations > ();

   // Rect used for hiding and unhiding see comment in HideAllPorts
   IETRect m_additionalInvalidateRect;

   // Should we autoroute edges during graph events
   boolean m_autoRouteEdges;
   
   private TSEFont m_defaultTextFont = new TSEFont("Arial-plain-12");
   private TSEFont m_staticTextFont = new TSEFont("Arial-italic-11");

   class PortLocations
   {
      public PortLocations()
      {
      }

      public TSPoint m_delta = new TSPoint();
      public TSENode m_node = null;
      public int m_closestSide; // TSSide
   };

   /* (non-Javadoc)
      * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
      */
   public String getDrawEngineID()
   {
      return "ComponentDrawEngine";
   }

   /* (non-Javadoc)
      * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments()
      */
   public void initCompartments(IPresentationElement element)
   {
      if (null == element)
         throw new IllegalArgumentException();

      try
      {
         // We may get here with no compartments.  This happens if we've been created
         // by the user.  If we read from a file then the compartments have been pre-created and
         // we just need to initialize them.
         long numCompartments = getNumCompartments();
         if (numCompartments == 0)
         {
            createCompartments();
         }

         IElement modelElement = element.getFirstSubject();
         if (modelElement != null)
         {
            INameListCompartment nameCompartment = getCompartmentByKind(INameListCompartment.class);
            if (nameCompartment != null)
            {
               nameCompartment.attach(modelElement);
            }
         }
      }
      catch (ETException e)
      {
         e.printStackTrace();
      }
   }

   /* (non-Javadoc)
      * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
      */
   public void createCompartments() throws ETException
   {
      clearCompartments();

      createAndAddCompartment("ADNameListCompartment", 0);

      // Make sure we have a static text compartment showing the label <<datastore>>
      INameListCompartment pADNameListCompartment = getCompartmentByKind(INameListCompartment.class);
      if (pADNameListCompartment != null)
      {
         pADNameListCompartment.addStaticText("<<component>>");
      }
   }

   /* (non-Javadoc)
      * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
      */
   public void sizeToContents()
   {
      ETList< IElement > allPorts = getPorts2();
      int count = (allPorts != null) ? allPorts.getCount() : 0;

      int nMinWidth  = MIN_NODE_WIDTH;
      int nMinHeight = MIN_NODE_HEIGHT;
      if ( count > 0 )
      {
         // Increase some by the number of ports
         PortsPerSide pps = new PortsPerSide();

         if ( nMinWidth < ( ( (pps.m_nTop + 1) * s_PortSize) + ( (pps.m_nTop + 1) * s_PortOffset) ) )
         {
            nMinWidth = ( ( (pps.m_nTop + 1) * s_PortSize) + ( (pps.m_nTop + 1) * s_PortOffset) );
         }
         if ( nMinWidth < ( ( (pps.m_nBottom + 1) * s_PortSize) + ( (pps.m_nBottom + 1) * s_PortOffset) ) )
         {
            nMinWidth = ( ( (pps.m_nBottom + 1) * s_PortSize) + ( (pps.m_nBottom + 1) * s_PortOffset) );
         }
         if ( nMinHeight < ( ( (pps.m_nLeft + 1) * s_PortSize) + ( (pps.m_nLeft + 1) * s_PortOffset) ) )
         {
            nMinHeight = ( ( (pps.m_nLeft + 1) * s_PortSize) + ( (pps.m_nLeft + 1) * s_PortOffset) );
         }
         if ( nMinHeight < ( ( (pps.m_nRight + 1) * s_PortSize) + ( (pps.m_nRight + 1) * s_PortOffset) ) )
         {
            nMinHeight = ( ( (pps.m_nRight + 1) * s_PortSize) + ( (pps.m_nRight + 1) * s_PortOffset) );
         }
      }

      // Size but keep the current size if possible
      sizeToContentsWithMin( nMinWidth, nMinHeight, false, true);
   }

	
   /* (non-Javadoc)
      * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw()
      */
   public void doDraw(IDrawInfo pDrawInfo)
   {
      TSEGraphics graphics = pDrawInfo.getTSEGraphics();
      IETRect deviceRect = pDrawInfo.getDeviceBounds();
      Color borderColor = getBorderBoundsColor();
      Color fillColor = getBkColor();

      // Draw a rectangle around the entire node
      //
      //    --------------     
      //    |            |  
      //    |            | 
      //    |    Name    | 
      //    |            | 
      //    --------------     
      //
      float centerX = (float)deviceRect.getCenterX();
      GradientPaint paint = new GradientPaint(centerX,
                         deviceRect.getBottom(),
                         fillColor,
                         centerX,
                         deviceRect.getTop(),
                         getLightGradientFillColor());
    
      GDISupport.drawRectangle(graphics, deviceRect.getRectangle(), borderColor, paint);

      // Draw each compartment now
//      handleNameListCompartmentDraw(pDrawInfo, deviceRect);
      handleNameListCompartmentDrawForContainers(pDrawInfo, deviceRect);
      super.doDraw(pDrawInfo);
   }

   /* (non-Javadoc)
      * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getElementType()
      */
   public String getElementType()
   {
      return new String("Component");
   }

   /* (non-Javadoc)
      * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
      */
   public void initResources()
   {
      setFillColor("componentfill", 255, 204, 0);
      setLightGradientFillColor("componentlightgradientfill", 254, 241, 187);
      setBorderColor("componentborder", Color.BLACK);

      super.initResources();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onGraphEvent(int)
    */
   public void onGraphEvent(int nKind)
   {
      switch (nKind)
      {
         case IGraphEventKind.GEK_PRE_MOVE :
         case IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED :
            // Select the ports connected to this component
            this.selectAllPorts(true, true);
            break;
         case IGraphEventKind.GEK_POST_MOVE :
         case IGraphEventKind.GEK_DELETECANCELED :
         {
            // Deselect the ports connected to this component           
				ETPairT < IETRect, IETRect > bounds = getBoundingRectWithLollypops();
				if (bounds != null)
				{
					this.invalidateRect(bounds.getParamTwo());
				}   
				selectAllPorts(false);         
            break;
         }
         case IGraphEventKind.GEK_PRE_RESIZE :
            // Make the ports disappear and remember the positions
            rememberAllPortPositions();
            hideAllPorts(true);
            break;
         case IGraphEventKind.GEK_POST_RESIZE :
         {        
            // Reposition the ports if necessary
            hideAllPorts(false);
            // Move the port to the nearest edge
            restoreAllPortPositions();
            
				ETPairT < IETRect, IETRect > bounds = getBoundingRectWithLollypops();
				if (bounds != null)
				{
					this.invalidateRect(bounds.getParamTwo());
				}
            break;
         }
         case IGraphEventKind.GEK_PRE_LAYOUT :
            {
               INodePresentation nodePE = getNodePresentation();

               if (nodePE != null)
               {
                  // Resize us so we look bigger to the layout mechanism
                  ETPairT < IETRect, IETRect > bounds = getBoundingRectWithLollypops();

                  if (bounds != null)
                  {
                     m_originalSize = bounds.getParamOne();
                     IETRect boundingRectWithEverything = bounds.getParamTwo();

                     double width = m_originalSize.getWidth();
                     double height = m_originalSize.getHeight();

                     if (boundingRectWithEverything != null)
                     {
                        width = boundingRectWithEverything.getWidth();
                        height = boundingRectWithEverything.getHeight();
                     }
                     nodePE.resize((long)width, (long)height, false);
                  }
               }

               // Remember all port positions
               rememberAllPortPositions();
               hideAllPorts(true);
            }
            break;
         case IGraphEventKind.GEK_POST_LAYOUT :
            {
               INodePresentation nodePE = getNodePresentation();

               if (nodePE != null && m_originalSize != null)
               {
                  double width = m_originalSize.getWidth();
                  double height = m_originalSize.getHeight();

                  nodePE.resize((long)width, (long)height, true);
                  m_originalSize = null;
               }

               // Restore all port positions
               hideAllPorts(false);
               // Move the port to the nearest edge
               restoreAllPortPositions();
               // Distribute the ports and their attached lollypops
               distributeInterfacesOnAllPorts(false);
            }
            break;
      }

      super.onGraphEvent(nKind);
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onContextMenu(org.netbeans.modules.uml.ui.products.ad.application.IMenuManager)
    */
   public void onContextMenu(IMenuManager manager)
   {
      // Add the context menu items dealing with ports
      addPortMenuItems(manager);
      super.onContextMenu(manager);
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setSensitivityAndCheck(String,org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass)
    */
   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean retVal = false;
      if (id.equals("MBK_COMPONENT_PORT_NAME") || id.equals("MBK_COMPONENT_PORT_NAME_END"))
      {
         // compute the index of the button kind into the ports list on the component
         int index = 0; //buttonKind - CADDrawEngineButtonHandler::MBK_COMPONENT_PORT_NAME;

         boolean isDisplayed = isDisplayed( index );
         pClass.setChecked( isDisplayed );
      }
      else
      {
         retVal = super.setSensitivityAndCheck(id, pClass);
      }
      return retVal;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onHandleButton(java.awt.event.ActionEvent,String)
    */
   public boolean onHandleButton(ActionEvent e, String id)
   {
      boolean handled = false;
      if (id.equals("MBK_COMPONENT_PORT_NAME") || id.equals("MBK_COMPONENT_PORT_NAME_END"))
      {
         // compute the index of the button kind into the ports list on the component
         int index = 0; //menuSelected - CADDrawEngineButtonHandler::MBK_COMPONENT_PORT_NAME;

         // Now we want to either show or hide this item
         boolean isDisplayed = isDisplayed(index);

         // Get the port at this index
         IPort port = getPortAtIndex(index);

         // Get the parent drawing area control
         IDrawingAreaControl control = getDrawingArea();

         if (control != null && port != null && isDisplayed)
         {
            // Whack this presentation element
            //
            // Go through the displayed ports, find it and then whack the presentation element
            ETList < IPresentationElement > displayedPorts = getPorts();
            if (displayedPorts != null)
            {
               // Go through the displayed ports looking for the port that corresponds to the
               // menu button port pMenuButtonPort.
               int count = displayedPorts.size();
               for (int i = 0; i < count; i++)
               {
                  IPresentationElement thisPort = displayedPorts.get(i);
                  IElement pEle = TypeConversions.getElement(thisPort);
                  if (pEle != null)
                  {
                     boolean isSame = pEle.isSame(port);
                     if (isSame && thisPort instanceof INodePresentation)
                     {
                        control.postDeletePresentationElement(thisPort);
                     }
                  }
               }
            }
         }
         else if (control != null && port != null)
         {
            IDiagramEngine pEngine = control.getDiagramEngine();
            if (pEngine != null)
            {
               ICoreRelationshipDiscovery pRD = pEngine.getRelationshipDiscovery();
               if (pRD != null && pRD instanceof IADRelationshipDiscovery)
               {
                  IADRelationshipDiscovery pADRD = (IADRelationshipDiscovery)pRD;
                  IPresentationElement compPE = getPresentationElement();
                  if (compPE != null)
                  {
                     IPresentationElement presEle = pADRD.createPortPresentationElement(compPE, port);
                     if (presEle != null)
                     {
                        repositionAllPorts();
                     }
                  }
               }
            }
         }
      }
      else
      {
         handled = super.onHandleButton(e, id);
      }
      return handled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
    */
   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
      // In Java in order to get an initial size to work properly we need to override this operation
      IETSize retVal = new ETSize(0, 0);

      IETSize tempSize = super.calculateOptimumSize(pDrawInfo, true);

      retVal.setWidth(Math.max(tempSize.getWidth(), NODE_WIDTH));
      retVal.setHeight(Math.max(tempSize.getHeight(), NODE_HEIGHT));

      TSTransform transform = pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform();
      return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, transform);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#getBoundingRectWithLollypops()
    */
   public ETPairT < IETRect, IETRect > getBoundingRectWithLollypops()
   {
      TSENode componentNode = getOwnerNode();

      if (componentNode != null)
      {
         TSConstRect componentRect = componentNode.getBounds();

         IETRect currentBoundingRect = RectConversions.newETRect(componentRect);

         ETList < IPresentationElement > ports = getPorts();

         if (ports != null)
         {
            Iterator < IPresentationElement > portIter = ports.iterator();
            while (portIter.hasNext())
            {
               IPresentationElement portPE = portIter.next();

               IProductGraphPresentation graphPE = null;
               if (portPE instanceof IProductGraphPresentation)
               {
                  graphPE = (IProductGraphPresentation)portPE;

                  IETRect portRect = graphPE.getBoundingRect();

                  TSRect tsPortRect = RectConversions.etRectToTSRect(portRect);

                  componentRect = tsPortRect != null ? componentRect.union(tsPortRect) : componentRect;

                  TSENode portNode = TypeConversions.getOwnerNode(portPE);

                  if (portNode != null)
                  {
                     ETList < IETGraphObject > interfaces = getLollypopInterfacesControlledByPort(portNode);

                     if (interfaces != null)
                     {
                        Iterator < IETGraphObject > lollypopIter = interfaces.iterator();
                        while (lollypopIter.hasNext())
                        {
                           IETGraphObject anInterface = lollypopIter.next();
                           IPresentationElement interfacePE = TypeConversions.getPresentationElement(anInterface);
                           if (interfacePE instanceof IProductGraphPresentation)
                           {
                              IProductGraphPresentation interfaceGraphPE = (IProductGraphPresentation)interfacePE;
                              IETRect interfaceRect = interfaceGraphPE.getBoundingRect();
                              TSRect tsInterfaceRect = RectConversions.etRectToTSRect(interfaceRect);
                              componentRect = tsInterfaceRect != null ? componentRect.union(tsInterfaceRect) : componentRect;
                           }
                        }
                     }
                  }
               }
            }
         }
         return new ETPairT < IETRect, IETRect > (currentBoundingRect, RectConversions.newETRect(componentRect));
      }

      return null;
   }

// TODO, I (BDB) am not sure how java containment is processing these
//   // Virtual used by IADContainerDrawEngine to do the containment
//   STDMETHOD(ProcessContainment)( INodePresentation* pPreviousContainer,
//                                  IPresentationElement * pPresentationElement );
//
//   /// Ends containment of a list of items
//   STDMETHOD(EndContainment)( /*[in]*/ IPresentationElements * pPresentationElements );

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#getPorts()
    */
   public ETList < IPresentationElement > getPorts()
   {
      IPresentationElement componentPE = getPresentationElement();
      if (componentPE != null)
      {
         return PresentationReferenceHelper.getAllReferredElements(componentPE);
      }
      return null;
   }

   public ETList < IElement > getPorts2()
   {
      ETList< IElement > foundElements = null;
   
      IPresentationElement componentPE = getPresentationElement();
      if ( componentPE != null )
      {
         ETList< IElement > elements = PresentationReferenceHelper.getAllReferredSubjects( componentPE );

         // Now gather up all the ports
         foundElements = new ETArrayList< IElement >();
         if ( foundElements != null )
         {
            int count = (elements != null) ? elements.getCount() : 0;

            for ( int i = 0 ; i < count ; i++ )
            {
               IElement thisElement = elements.item( i );
               if (thisElement instanceof IPort)
               {
                  IPort port = (IPort)thisElement;
                  
                  foundElements.add( port );
               }
            }
         }
      }
      
      return foundElements;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#selectAllPorts(boolean)
    */
   public void selectAllPorts(boolean bSelect)
   {
      selectAllPorts(bSelect, false);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#selectAllPorts(boolean)
    */
   public void selectAllPorts(boolean bSelect, boolean firePreMoveEvent)
   {
      ETList < IPresentationElement > ports = getPorts();

      if (ports != null && ports.getCount() > 0)
      {
         Iterator < IPresentationElement > iter = ports.iterator();

         while (iter.hasNext())
         {
            IPresentationElement thisPort = iter.next();

            TSENode portNode = TypeConversions.getOwnerNode(thisPort);

            if (portNode != null)
            {				
               portNode.setSelected(bSelect);

               // Now make sure the interface lollypops are selected as well
               ETList < IETGraphObject > interfaces = getLollypopInterfacesControlledByPort(portNode);

               if (interfaces != null && interfaces.getCount() > 0)
               {
                  Iterator < IETGraphObject > iterator = interfaces.iterator();

                  while (iterator.hasNext())
                  {
                     IETGraphObject etGraphObject = iterator.next();

                     if (etGraphObject != null)
                     {
                        TSENode ownerNode = TypeConversions.getOwnerNode(etGraphObject);

                        if (ownerNode != null)
                        {
									etGraphObject.getEngine().invalidate();
                           ownerNode.setSelected(bSelect);

                           if (firePreMoveEvent)
                           {
                              IETGraphObject interfaceGraphObject = TypeConversions.getETGraphObject(ownerNode);
                              interfaceGraphObject.onGraphEvent(IGraphEventKind.GEK_PRE_MOVE);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#hideAllPorts(boolean)
    */
   public void hideAllPorts(boolean bHide)
   {
      if (bHide)
      {
         ETList < IPresentationElement > ports = getPorts();
         if (ports == null)
            return;
         m_HiddenNodes = createHiddenList(ports);
         m_HiddenNodes.hide();
      }
      else if (m_HiddenNodes != null)
      {
         m_HiddenNodes.unHide();
         m_HiddenNodes = null;
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#repositionAllPorts()
    */
   public void repositionAllPorts()
   {
      ETList < IPresentationElement > ports = getPorts();

      if (ports == null)
         return;

      TSENode thisNode = getOwnerNode();

      TSConstRect thisRect = thisNode.getBounds();

      Iterator < IPresentationElement > iterator = ports.iterator();

      while (iterator.hasNext())
      {
         IPresentationElement thisPort = iterator.next();

         TSENode portNode = TypeConversions.getOwnerNode(thisPort);
         if (portNode != null)
         {
            TSPoint centerPoint = new TSPoint(portNode.getCenter());

            if (centerPoint != null)
            {
               if (RectConversions.moveToNearestPoint(thisRect, centerPoint))
               {
                  portNode.assignCenter(centerPoint.getX(), centerPoint.getY());
                  
                  if (portNode instanceof IETNode)
                  {
                  	IETNode portObject = (IETNode)portNode;
							portObject.getEngine().invalidate();
							portObject.invalidateEdges();
                  }
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#rememberAllPortPositions()
    */
   public void rememberAllPortPositions()
   {
      m_PortLocations.clear();

      ETList < IPresentationElement > ports = getPorts();
      TSENode thisNode = getOwnerNode();

      if (ports != null)
      {
         if (ports.getCount() > 0 && thisNode != null)
         {
            TSConstRect thisRect = thisNode.getBounds();

            Iterator < IPresentationElement > iter = ports.iterator();

            while (iter.hasNext())
            {
               IPresentationElement thisPort = iter.next();

               TSENode portNode = TypeConversions.getOwnerNode(thisPort);

               if (portNode != null)
               {
                  PortLocations portLocation = new PortLocations();
                  TSConstPoint componentCenter = thisNode.getCenter();
                  TSConstPoint portCenter = portNode.getCenter();

                  if (componentCenter != null && portCenter != null)
                  {
                     portLocation.m_delta.setX(portCenter.getX() - componentCenter.getX());
                     portLocation.m_delta.setY(portCenter.getY() - componentCenter.getY());
                     portLocation.m_node = portNode;
                     portLocation.m_closestSide = RectConversions.getClosestSide(thisRect, portCenter);

                     m_PortLocations.add(portLocation);
                  }
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#restoreAllPortPositions()
    */
   public void restoreAllPortPositions()
   {
      TSENode thisNode = getOwnerNode();

      TSConstRect thisRect = thisNode.getBounds();
      TSPoint centerPoint = new TSPoint(thisRect.getCenter());

      IDrawingAreaControl control = getDrawingArea();

      if (thisNode != null && control != null && centerPoint != null && thisRect != null)
      {
         Iterator < PortLocations > iterator = m_PortLocations.iterator();

         while (iterator.hasNext())
         {
            PortLocations location = iterator.next();
            TSPoint newPoint = new TSPoint();

            newPoint.setX(centerPoint.getX() + location.m_delta.getX());
            newPoint.setY(centerPoint.getY() + location.m_delta.getY());

            if (location.m_closestSide == TSSide.TS_SIDE_RIGHT)
            {
               newPoint.setX(thisRect.getRight());
            }
            else if (location.m_closestSide == TSSide.TS_SIDE_TOP)
            {
               newPoint.setY(thisRect.getTop());
            }
            else if (location.m_closestSide == TSSide.TS_SIDE_LEFT)
            {
               newPoint.setX(thisRect.getLeft());
            }
            else if (location.m_closestSide == TSSide.TS_SIDE_BOTTOM)
            {
               newPoint.setY(thisRect.getBottom());
            }

            if (RectConversions.moveToNearestPoint(thisRect, newPoint))
            {
               INodePresentation nodePresentation = TypeConversions.getNodePresentation(location.m_node);
               if (nodePresentation != null)
               {
                  nodePresentation.invalidate();
               }
               location.m_node.assignCenter(newPoint.getX(), newPoint.getY());
            }
         }
      }

      m_PortLocations.clear();

      repositionAllPorts();

		//if (control != null)
      	//control.refresh(true);
   }

   public ETList < IETGraphObject > getLollypopInterfacesControlledByPort(TSENode portNode)
   {
      if (portNode == null)
         return null;

      IETGraphObject portElement = TypeConversions.getETGraphObject(portNode);

      if (portElement != null)
      {
         PresentationHelper.LollypopsAndEdges result = PresentationHelper.getLollypopsWithOneControllingEdge(portElement);

         if (result != null)
            return result.getLollypops();
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#distributeInterfacesOnAllPorts(boolean)
    */
   public void distributeInterfacesOnAllPorts(boolean bRedraw)
   {
      IDrawingAreaControl control = getDrawingArea();
      ETList < IPresentationElement > portPEs = getPorts();

      ETList < IPresentationElement > rightPorts = new ETArrayList < IPresentationElement > ();
      ETList < IPresentationElement > topPorts = new ETArrayList < IPresentationElement > ();
      ETList < IPresentationElement > leftPorts = new ETArrayList < IPresentationElement > ();
      ETList < IPresentationElement > bottomPorts = new ETArrayList < IPresentationElement > ();

      Iterator < IPresentationElement > iterator = portPEs.iterator();
      while (iterator.hasNext())
      {
         IPresentationElement thisPort = iterator.next();

         if (thisPort != null)
         {
            IDrawEngine drawEngine = TypeConversions.getDrawEngine(thisPort);

            if (drawEngine != null)
            {
               distributeAttachedInterfaces(drawEngine, bRedraw);

               IETRect boundingRect = drawEngine.getBoundingRect();

               int nSide = getPortSide(thisPort);

               if (nSide == TSSide.TS_SIDE_RIGHT)
                  rightPorts.add(thisPort);
               else if (nSide == TSSide.TS_SIDE_TOP)
                  topPorts.add(thisPort);
               else if (nSide == TSSide.TS_SIDE_LEFT)
                  leftPorts.add(thisPort);
               else if (nSide == TSSide.TS_SIDE_BOTTOM)
                  bottomPorts.add(thisPort);
            }
            control.executeStackingCommand(thisPort, IDrawingAreaControl.SOK_MOVETOFRONT, bRedraw);
         }
      }

      boolean foundRightIntersections = PresentationHelper.haveIntersections(rightPorts);
      boolean foundTopIntersections = PresentationHelper.haveIntersections(topPorts);
      boolean foundLeftIntersections = PresentationHelper.haveIntersections(leftPorts);
      boolean foundBottomIntersections = PresentationHelper.haveIntersections(bottomPorts);

      if (foundRightIntersections)
         movePortsToAvoidIntersections(QuadrantKindEnum.QK_RIGHT, rightPorts);
      if (foundTopIntersections)
         movePortsToAvoidIntersections(QuadrantKindEnum.QK_TOP, topPorts);
      if (foundLeftIntersections)
         movePortsToAvoidIntersections(QuadrantKindEnum.QK_LEFT, leftPorts);
      if (foundBottomIntersections)
         movePortsToAvoidIntersections(QuadrantKindEnum.QK_BOTTOM, bottomPorts);

      getDrawingArea().getGraphWindow().updateInvalidRegion();
      control.refresh(false);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#movePortsToAvoidIntersections(int)
    */
   public void movePortsToAvoidIntersections(int nSide, ETList < IPresentationElement > ports)
   {
      IETRect componentRect = getLogicalBoundingRect();

      if (componentRect != null)
      {
         long nLastPortPosition = 0;
         for (int index = 0; index < ports.getCount(); ++index)
         {
            IPresentationElement thisPE = ports.item(index);

            if (thisPE instanceof INodePresentation)
            {
               INodePresentation portNodePE = (INodePresentation)thisPE;

               IDrawEngine portDE = TypeConversions.getDrawEngine(thisPE);

               if (portDE != null)
               {
                  IETRect portBoundingRect = portDE.getLogicalBoundingRect(false);

                  if (portBoundingRect != null)
                  {
                     double nWidth = portBoundingRect.getWidth();
                     double nHeight = Math.abs(portBoundingRect.getHeight());
                     double nCenterX = 0;
                     double nCenterY = 0;

                     switch (nSide)
                     {
                        case QuadrantKindEnum.QK_RIGHT :
                        case QuadrantKindEnum.QK_LEFT :
                           {
                              nWidth = s_PortSize;
                              if (nSide == QuadrantKindEnum.QK_RIGHT)
                              {
                                 nCenterX = componentRect.getRight();
                              }
                              else
                              {
                                 nCenterX = componentRect.getLeft();
                              }

                              if (index == 0)
                              {
                                 nLastPortPosition = Math.max(componentRect.getBottom(), componentRect.getTop());
                              }

                              // Compute the y pos which is the last port y - some slop - 1/2 height
                              nCenterY = nLastPortPosition - s_PortOffset - nHeight / 2;
                              nLastPortPosition = (long) (nCenterY - nHeight / 2);

                              // Make sure we don't below the component
                              if (nCenterY < Math.min(componentRect.getBottom(), componentRect.getTop()))
                              {
                                 nCenterY = Math.min(componentRect.getBottom(), componentRect.getTop()) + nHeight / 2;
                              }
                           }
                           break;
                        case QuadrantKindEnum.QK_TOP :
                        case QuadrantKindEnum.QK_BOTTOM :
                           {
                              nHeight = s_PortSize;
                              if (nSide == QuadrantKindEnum.QK_TOP)
                              {
                                 nCenterY = Math.max(componentRect.getBottom(), componentRect.getTop());
                              }
                              else
                              {
                                 nCenterY = Math.min(componentRect.getBottom(), componentRect.getTop());
                              }
                              if (index == 0)
                              {
                                 nLastPortPosition = componentRect.getLeft();
                              }

                              // Compute the x pos which is the last port x + some slop + 1/2 width
                              nCenterX = nLastPortPosition + s_PortOffset + nWidth / 2;
                              nLastPortPosition = (long) (nCenterX + nWidth / 2);

                              // Make sure we don't go right of the component
                              if (nCenterX > componentRect.getRight())
                              {
                                 // move this guy back
                                 nCenterX = componentRect.getRight() - nWidth / 2;
                              }
                           }
                           break;
                     }
                     TSPoint centerPoint = new TSPoint();
                     centerPoint.setX(nCenterX);
                     centerPoint.setY(nCenterY);

                     portNodePE.moveTo((int)nCenterX, (int)nCenterY, (MoveToFlags.MTF_MOVEX | MoveToFlags.MTF_MOVEY | MoveToFlags.MTF_LOGICALCOORD));
                     portNodePE.resize((int)nWidth, (int)nHeight, false);
                     
                     IDrawEngine drawEngine = TypeConversions.getDrawEngine(portNodePE);
                     if (drawEngine != null)
                     {
                        distributeAttachedInterfaces(drawEngine, true);
                     }
                  }
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#movePortsToSide(int)
    */
   public void movePortsToSide(int nSide)
   {
       ETList < IPresentationElement > portPEs = getPorts();
       movePortsToSide(nSide, portPEs);
   }
   
   public void movePortsToSide(int nSide, ETList<IPresentationElement> portPEs)
   {
      if (portPEs != null)
      {
         movePortsToAvoidIntersections(nSide, portPEs);
         // Refresh the screen
         getDrawingArea().getGraphWindow().updateInvalidRegion();
         getDrawingArea().refresh(true);
      }
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#getAllowAutoRouteEdges()
    */
   public boolean getAllowAutoRouteEdges()
   {
      return m_autoRouteEdges;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine#setAllowAutoRouteEdges(boolean)
    */
   public void setAllowAutoRouteEdges(boolean bAutoRouteEdges)
   {
      m_autoRouteEdges = bAutoRouteEdges; 
   }

   /// Sends a graph event to all related ports
   // CLEAN this is no longer used by anybody STDMETHOD(SendGraphEventToPorts)(GraphEventKind nKind);


   // protected:
   /**
    * This class replaces the getPortsPerSide() from C++
    * Calculates the number of ports per side
    */
   protected class PortsPerSide
   {
      public int m_nLeft = 0;
      public int m_nRight = 0;
      public int m_nTop = 0;
      public int m_nBottom = 0;
      public int m_nNotAssigned = 0;
      
      public PortsPerSide()
      {
         ETList< IPresentationElement > allPorts = getPorts();
         int count = (allPorts != null) ? allPorts.getCount() : 0;

         for (int i = 0 ; i < count ; i++)
         {
            IPresentationElement thisPort = allPorts.item( i );
            if ( thisPort != null )
            {
               int nSide = getPortSide(thisPort);
               switch (nSide)
               {
               case TSSide.TS_SIDE_RIGHT     : m_nRight++; break;
               case TSSide.TS_SIDE_TOP       : m_nTop++; break;
               case TSSide.TS_SIDE_LEFT      : m_nLeft++; break;
               case TSSide.TS_SIDE_BOTTOM    : m_nBottom++; break;
               case TSSide.TS_SIDE_UNDEFINED : m_nNotAssigned++; break;
               }
            }
         }
      }
   }

   /**
    * Returns the port at a specific index
    *
    * @return The port at index nIndex into the components list
    */
   protected IPort getPortAtIndex(int index)
   {
      IPort retObj = null;
      IComponent component = getComponent();
      if (component != null)
      {
         // Get the ports off the component
         ETList < IPort > ports = component.getExternalInterfaces();
         if (ports != null && ports.size() > index)
         {
            retObj = ports.get(index);
         }
      }
      return retObj;
   }
   
   /**
    * returns true if this port at this index is currently displayed
    *
    * @param nPortIndex [in] The port index to be queried
    * @return true if the port port is displayed on the diagram
    */
   protected boolean isDisplayed( int nPortIndex )
   {
      boolean bIsDisplayed = false;
   
      IPort thisPort = getPortAtIndex( nPortIndex );
      if ( thisPort != null )
      {
         bIsDisplayed = isDisplayed(thisPort);
      }

      return bIsDisplayed;
   }
   
   /**
    * returns true if this port is currently displayed
    *
    * @param port [in] The port to be queried
    * @return true if the port port is displayed on the diagram
    */
   protected boolean isDisplayed( IPort port )
   {
      boolean bIsDisplayed = false;
   
      ETList< IPort > ports;
      ETList< IElement > displayedPorts;

      // Get the component
      IComponent component = getComponent();
      if ( component != null )
      {
         // Get the ports off the component
         ports = component.getExternalInterfaces();

         // Get the displayed ports
         displayedPorts = getPorts2();

         if ( (ports != null) &&
              (displayedPorts != null) )
         {
            // See if this port is in the displayed list
            if (port != null)
            {
               bIsDisplayed = displayedPorts.isInList( port );
            }
         }
      }

      return bIsDisplayed;
   }

   /**
    * returns the ICompoent for this draw engine
    *
    * @param component [out,retval] The component this DE represents
    */
   protected IComponent getComponent()
   {
      IComponent retObj = null;
      IElement thisEle = TypeConversions.getElement(this);
      if (thisEle != null && thisEle instanceof IComponent)
      {
         retObj = (IComponent)thisEle;
      }
      return retObj;
   }

   protected int getPortSide(IPresentationElement port)
   {
      int nSide = TSSide.TS_SIDE_BOTTOM;

      if (port != null)
      {
         TSENode thisNode = getOwnerNode();
         if (thisNode != null)
         {
            TSConstRect thisRect = thisNode.getBounds();

            TSENode portNode = TypeConversions.getOwnerNode(port);
            if (portNode != null)
            {
               TSConstPoint portCenter = portNode.getCenter();

               if (portCenter != null)
               {
                  nSide = RectConversions.getClosestSide(thisRect, portCenter);
               }
            }
         }
      }
      return nSide;
   }

   protected void distributeAttachedInterfaces(IDrawEngine portDrawEngine, boolean bRedraw)
   {
      IEventManager eventManager = portDrawEngine.getEventManager();
      IADInterfaceEventManager interfaceEM = null;
      if (eventManager instanceof IADInterfaceEventManager)
         interfaceEM = (IADInterfaceEventManager)eventManager;

      if (interfaceEM != null)
      {
         interfaceEM.distributeAttachedInterfaces(bRedraw);
      }
   }


   /**
    * Adds Port specific stuff.
    *
    * @param pDrawEngine [in] The draw engine we're over
    * @param pContextMenu [in] The context menu about to be displayed
    */
   protected void addPortMenuItems(IMenuManager manager)
   {
      IElement thisEle = TypeConversions.getElement(this);
      if (thisEle != null && thisEle instanceof IComponent)
      {
         IComponent component = (IComponent)thisEle;
         ETList < IPort > ports = component.getExternalInterfaces();
         if (ports != null)
         {
            int count = ports.size();
            if (count > 0)
            {
               IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_PORTS_TITLE"), "");
               if (subMenu != null)
               {
                  // Go through the ports on the component and see if they are displayed
                  for (int i = 0; i < count; i++)
                  {
                     IPort thisPort = ports.get(i);
                     String name = thisPort.getName();
                     if (name == null || name.length() == 0)
                     {
                        name = PreferenceAccessor.instance().getDefaultElementName();
                     }
                     subMenu.add(createMenuAction(name, "MBK_COMPONENT_PORT_NAME" + i));
                  }
                  //manager.add(subMenu);
               }
            }
         }
      }
   }
}
