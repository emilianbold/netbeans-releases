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



package org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler;
import org.netbeans.modules.uml.ui.products.ad.compartments.SmartDragHelper;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ActivationBar;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ConnectorPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelineCompartmentPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiece;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETHorzDragCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.SmartDragTool;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObject;
//import com.tomsawyer.editor.state.TSEMoveSelectedState;
import com.tomsawyer.editor.tool.TSEMoveSelectedTool;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import org.openide.util.NbPreferences;

/**
 * The MessageEdgeDrawEngine provides drawing support for an TSGraphObject.
 * There is a one to one relationship between an TSGraphObject and an 
 * MessageEdgeDrawEngine.
 * 
 * @author Trey Spiva
 */
public class MessageEdgeDrawEngine extends ETEdgeDrawEngine implements IMessageEdgeDrawEngine
{
   private final static String MDE_SHOW = "Show";
   private final static String MDE_SHOWMESSAGETYPE = "ShowMessageType";

   /**
    * Stores the type of line to draw.  Return messages will draw dashed lines
    * all other messages will be solid.
    */
   private int m_LineKind = DrawEngineLineKindEnum.DELK_UNKNOWN;

   private boolean m_IsMessageToSelf = false;

   private boolean m_Show = true;

   
   private int m_ShowMessageType = IShowMessageType.SMT_UNKNOWN;

   private LifelinePiece m_PieceSelf = null;
   //   private MessageRelocator m_MessageRelocator = null;
   private final static int FUDGE_DRAW_ENGINE_ABOVE = 2;
   private final static int ACTIVATION_BAR_BUFFER = 10;

   /** 
    * Stores the type of arrow head to display.  The arrow head is determined
    * by the model type.
    */
   private int m_ArrowHeadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;


   public MessageEdgeDrawEngine()
   {
       //kris richards - issue 113794 - getting default value from pref
       m_ShowMessageType = 
               NbPreferences.forModule(MessageEdgeDrawEngine.class).getInt("UML_SQD_DEFAULT_MSG", IShowMessageType.SMT_UNKNOWN);
   }

   public String getElementType()
   {
      String type = super.getElementType();
      if (type == null)
      {
         type = new String("Message");
      }
      return type;
   }

   /**
    * This is the name of the drawengine used when storing and reading from 
    * the product archive
    *
    * @return A unique identifier for this draw engine.  Used when persisting 
    *         to the etlp file.
    */
   public String getDrawEngineID()
   {
      return "MessageEdgeDrawEngine";
   }

   private static SmartDragHelper m_smartDragHelper = null;

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButtonPressed(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, java.awt.event.MouseEvent )
    */
   public boolean handleLeftMouseButtonPressed(MouseEvent pEvent)
   {
      IDrawingAreaControl daCtrl = getDrawingArea();
      ADGraphWindow graphWindow = daCtrl != null ? daCtrl.getGraphWindow() : null;

      SmartDragTool dragTool = createSmartDragTool(pEvent);

      if (dragTool != null)
      {
         m_smartDragHelper = new SmartDragHelper("Layout");

         TSEEdge edge = this.getEdge();

         if (m_IsMessageToSelf)
         {
            if (m_PieceSelf == null)
               initializePieceSelf(edge);
            dragTool.setDraggingPiece(m_PieceSelf, getMessageKind() != IMessageKind.MK_RESULT);
         }
         else
         {
            dragTool.setDraggingEdge(edge, 0);
         }

         dragTool.setDragRestrictionType(SmartDragTool.DR.VERTICAL_MOVE_ONLY);

         IDiagram diagram = getDiagram();
         if (diagram != null)
         {
            IDiagramEngine diagramEngine = TypeConversions.getDiagramEngine(diagram);
            IADSequenceDiagEngine sqdEngine = null;
            if (diagramEngine instanceof IADSequenceDiagEngine)
               sqdEngine = (IADSequenceDiagEngine)diagramEngine;

            if (sqdEngine != null)
            {
               final IETRect rectThisMessage = getLogicalBoundingRect();

               IDrawEngine engine = sqdEngine.findFirstDrawEngineAbove("Message", rectThisMessage.getTop() + FUDGE_DRAW_ENGINE_ABOVE);

               if (engine != null)
               {
                  final IETRect rectAboveMessage = TypeConversions.getLogicalBoundingRect(engine);

                  int aboveMessageBottom = rectAboveMessage.getBottom() - (m_IsMessageToSelf ? 2 * ACTIVATION_BAR_BUFFER : ACTIVATION_BAR_BUFFER);

                  IETRect rect = new ETRect(Integer.MIN_VALUE / 2, aboveMessageBottom, Integer.MAX_VALUE, Integer.MAX_VALUE / 2);

                  dragTool.setRestrictedArea(rect);
               }
            }
         }
         //graphWindow.getCurrentState().setState(dragTool);
         graphWindow.getCurrentTool().setTool(dragTool);
         dragTool.onMousePressed(pEvent);
      }

      return true;
   }

   /**
    * Activate the smart drag tool so we can move the edges up and down, maintaining their
    * horizontal alignment.
    *
    * @param pMouseEvent [in] Information about the event that just occured
    * @param pTool [out] Return a tool here to make it the current tool on the GET
    * @param bHandled [out] true to cancel the event.  This indicates we handled it.
    */
   public boolean handleSetCursor( ISetCursorEvent event )
   {
      if( null == event ) throw new IllegalArgumentException();

      // TODO support using the control key
//      if( getAsyncKeyState( VK_CONTROL ))
//      {
//         event.setCursor( afxGetApp().loadCursor( IDC_SPLIT_V_MOVE ));
//      }
//      else
      {
         event.setCursor( ETHorzDragCursor.getCursor() );
      }
      
      return true;
   }

   /**
    * Tells the draw engine to write its data to the IProductArchive
    *
    * @param productArchive [in] The archive we're saving to
    * @param parentElement [in] The current element, or parent for any new attributes or elements.
    */
   public long writeToArchive( IProductArchive productArchive, IProductArchiveElement parentElement )
   {
      if( null == productArchive ) throw new IllegalArgumentException();
      if( null == parentElement ) throw new IllegalArgumentException();

      super.writeToArchive( productArchive, parentElement );

      IProductArchiveElement engineElement = parentElement.getElement( IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING );
      if( engineElement != null )
      {
         engineElement.addAttributeBool(
            IProductArchiveDefinitions.MESSAGEEDGEENGINE_ISSELFTOMESSAGE_BOOL, m_IsMessageToSelf );

         if( !m_Show )
         {
             engineElement.addAttributeBool( MDE_SHOW, m_Show );
         }

          engineElement.addAttributeLong( MDE_SHOWMESSAGETYPE, m_ShowMessageType );
      }

      return 0;
   }

   /**
    * Tells the draw engine to read its data to the IProductArchive
    *
    * @param productArchive [in] The archive we're reading from
    * @param pEngineElement [in] The element where this draw engine's information should exist.
    */
   public long readFromArchive( IProductArchive productArchive, IProductArchiveElement parentElement )
   {
      if( null == productArchive ) throw new IllegalArgumentException();
      if( null == parentElement ) throw new IllegalArgumentException();

      super.readFromArchive( productArchive , parentElement );

      // Fix W6942:  No need to use the member function here
      m_IsMessageToSelf = parentElement.getAttributeBool( IProductArchiveDefinitions.MESSAGEEDGEENGINE_ISSELFTOMESSAGE_BOOL );

      m_Show = parentElement.getAttributeBool( MDE_SHOW, m_Show );

      m_ShowMessageType = (int)parentElement.getAttributeLong( MDE_SHOWMESSAGETYPE );

      return 0;
   }

   /**
    * Performs the rendering of the message.
    */
   public void doDraw(IDrawInfo drawInfo)
   {
      if( m_Show )
      {
         super.doDraw(drawInfo);
      }
   }

   /**
    * The rectangle used for last drawing operation, in logical coordinates
    */
   public IETRect getLogicalBoundingRect( boolean bIncludeLabels )
   {
      // parameters checked by super.getLogicalBoundingRect()
      IETRect rectBounding = super.getLogicalBoundingRect( bIncludeLabels );

      if( rectBounding.getIntHeight() == 0 )
      {
         // atempt to take the arrow head into account
          rectBounding.setTop( rectBounding.getTop() + 5 );
          rectBounding.setBottom( rectBounding.getBottom() - 5 );
      }
      
      return rectBounding;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
    */
   public void initResources()
   {
      this.setLineColor("messagecolor", Color.BLACK);
      super.initResources();
   }

   /**
    * Notifies the node an event has been generated at the graph.
    */
   public void onGraphEvent(int nKind)
   {
      super.onGraphEvent(nKind);

      switch (nKind)
      {
         case IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED :
            onPreDeleteGatherSelected();
            break;

         case IGraphEventKind.GEK_PRE_DELETE :
            if (isMessageToSelf() == true)
            {
               IDrawingAreaControl ctrl = getDrawingArea();
               if (ctrl != null)
               {
                  TSEEdge edge = getEdge();
                  if (edge != null)
                  {
                     // Validate the node associated with this edge to remove the activation bars
                     TSNode sourceNode = edge.getSourceNode();

                     IDrawEngine engine = TypeConversions.getDrawEngine(sourceNode);
                     if (engine != null)
                     {
                        engine.validateNode();
                     }
                  }
               }
            }
            break;

         default :
            break;
      }
   }

   /**
    * Notifier that the model element has changed, if available the changed 
    * IFeature is passed along.
    */
   public long modelElementHasChanged(INotificationTargets targets)
   {
      if (targets != null)
      {
         int kind = targets.getKind();

         if (ModelElementChangedKind.MECK_ELEMENTMODIFIED == kind)
         {
            ILabelManager labelManager = getLabelManager();
            if (labelManager != null)
            {
               labelManager.resetLabelsText();
            }
         }
      }
      return 0;
   }

   /**
    * Tell the element that the model element has been deleted
    *
    * @param pTargets Details about what got deleted
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
   public long modelElementDeleted(INotificationTargets pTargets)
   {
      IElement modelElement = pTargets.getChangedModelElement();
      IElement pSecondaryChangedME = pTargets.getSecondaryChangedModelElement();
      IFeature pFeature = null;

      if (pSecondaryChangedME instanceof IFeature)
      {
         pFeature = (IFeature)pSecondaryChangedME;
      }

      boolean isFromFeature = false;

      // This avoids the problem of handling too many events
      IMessage message = null;
      if (modelElement instanceof IMessage)
      {
         message = (IMessage)modelElement;
      }

      IOperation operation = null;
      if (modelElement instanceof IOperation)
      {
         operation = (IOperation)modelElement;
      }

      // Make sure the message being deleted is this draw engine's message
      if (message != null)
      {
         // Fix W3322:  This special case is where the model element is not 
         // present for the draw engine In this case, we are assuming that 
         // the input message is OK.
         IElement element = TypeConversions.getElement(this);
         if (element != null)
         {
            String thisXMIID = element.getXMIID();
            String messageXMIID = message.getXMIID();

            if (thisXMIID.equals(messageXMIID) == false)
            {
               message = null;
            }
         }
      }

      if ((message == null) && (operation == null))
      {
         if (pFeature instanceof IOperation)
         {
            operation = (IOperation)pFeature;
            isFromFeature = true;

            // Fix W299:  We need to detect how we got this message
            // If the message is from a change on the SQD,
            // then the model element won't be the message's receiving classifier.
            // In this case we don't want anything deleted.
            if (modelElement instanceof IClassifier)
            {
               IClassifier inputClassifier = (IClassifier)modelElement;

               IElement firstElement = getFirstModelElement();
               if (firstElement instanceof IMessage)
               {
                  IMessage firstMessage = (IMessage)firstElement;

                  IClassifier receivingClassifier = firstMessage.getReceivingClassifier();

                  if (inputClassifier.isSame(receivingClassifier) == false)
                  {
                     // Don't delete any presentation information
                     message = null;
                     operation = null;
                     isFromFeature = false;
                  }
               }
            }
         }
      }

      if ((message != null) || (operation != null))
      {
         // Fix W2745:  Delete any return message associated with this message
         final int kind = getMessageKind();
         if (IMessageKind.MK_SYNCHRONOUS == kind)
         {
            IETGraphObject graphObject = (IETGraphObject)getParentETElement();

            TSEEdge pEdgeReturn = ConnectorPiece.getReturnEdge(graphObject);
            if (pEdgeReturn != null)
            {
               // Make sure the presentation element is allowed to delete its model element
               IEdgePresentation returnGraphObject = TypeConversions.getEdgePresentation(pEdgeReturn);
               if (returnGraphObject != null)
               {
                  returnGraphObject.invalidate();

                  pEdgeReturn.getOwnerGraph().remove(pEdgeReturn);
               }
            }
         }

         // Fix W1858:  Delete the pieces attached to the message.
         ConnectorPiece.deleteEdge(getEdge());

         // Fix W3055:  Delete the TS edge, if it still exists
         // This special case is when an operation is dragged out of the class,
         // which is the representing classifier of the lifeline
         // See also CAxDrawingAreaControl::ElementDeleted()
         if (isFromFeature == true)
         {
            TSEEdge edge = getEdge();
            if (edge != null)
            {
               edge.getOwnerGraph().remove(edge);
            }
         }
      }
      return 0;
   }


   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine#getAllowReconnection()
    */
   public boolean getAllowReconnection()
   {
      boolean bAllowReconnection = !m_IsMessageToSelf;

      if( bAllowReconnection )
      {
         IElement element = getFirstModelElement();
         if (element instanceof IMessage)
         {
            IMessage message = (IMessage)element;
            
            int kind = message.getKind();

            bAllowReconnection = ((IMessageKind.MK_SYNCHRONOUS == kind) || (IMessageKind.MK_ASYNCHRONOUS == kind));
         }
      }

      return bAllowReconnection;
   }


   //**************************************************
   // IMessageEdgeDrawEngine Implmenetations
   //**************************************************

   /**
    * Indicates that the message starts and ends on the same node
    *
    * @param bIsMessageToSelf <code>true</code> if the message start and finish 
    *                         nodes are the same.
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine#setIsMessageToSelf(boolean)
    */
   public void setIsMessageToSelf(boolean bIsMessageToSelf)
   {
      m_IsMessageToSelf = bIsMessageToSelf;
      if (m_IsMessageToSelf == true)
      {
         TSEEdge edge = getEdge();
         if (edge != null)
         {
            // Remember the suspension bar of the message-to-self
            // To be used when the user drags either of the attached messages
            initializePieceSelf(edge);

            // Determine the connector that is connected to the activation bar
            TSConnector activationBarConnector = getActivationBarConnector(edge);

            if (activationBarConnector != null)
            {
               LifelineCompartmentPiece piece = ConnectorPiece.getPieceAttachedToConnector(activationBarConnector);
               if (piece instanceof ActivationBar)
               {
                  ActivationBar bar = (ActivationBar)piece;
                  bar.setMessageToSelf(true);
               }
            }
         }
      }
      else
      {
         m_PieceSelf = null;
      }
   }

   /**
    * Indicates that the message starts and ends on the same node
    * 
    * @return <code>true</code> if the message start and finish nodes are the 
    *         same.
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine#isMessageToSelf()
    */
   public boolean isMessageToSelf()
   {
      boolean retVal = m_IsMessageToSelf;

      if ((retVal == true) && (m_PieceSelf != null))
      {
         initializePieceSelf(getEdge());
      }

      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine#move(int, boolean)
    */
   public void move(double nY, boolean bDoItNow)
   {
      TSEEdge ownerEdge = getEdge();
      if (ownerEdge != null)
      {
         TSConnector connector = null;

         if (!m_IsMessageToSelf)
         {
            connector = ownerEdge.getTargetConnector();
            moveConnector(connector, nY, bDoItNow);
            connector = null;
         }

         connector = ownerEdge.getSourceConnector();
         moveConnector(connector, nY, bDoItNow);
      }

   }

   /* (non-Javadoc)
    * Set the show property.
    * 
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine#setShow(boolean)
    */
   public void setShow(boolean bShow)
   {
      m_Show = bShow;
   }

   /* (non-Javadoc)
    * Retrieves the show property.
    * 
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine#getShow()
    */
   public boolean getShow()
   {
      return m_Show;
   }

   /* (non-Javadoc)
    * Set the show messge type property.
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine#setShowMessageType(int)
    */
   public void setShowMessageType(int type)
   {
      m_ShowMessageType = type;
   }

   /* (non-Javadoc)
    * Retrieves the show message type property.
    * 
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine#getShowMessageType()
    */
   public int getShowMessageType()
   {
      return m_ShowMessageType;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine#associatedResultMessage()
    */
   public IEdgePresentation getAssociatedResultMessage()
   {
      IEdgePresentation edgePresentation = null;

      final int kind = getMessageKind();
      switch (kind)
      {
         case IMessageKind.MK_CREATE :
         case IMessageKind.MK_SYNCHRONOUS :
            {
               IETGraphObject etGraphObject = TypeConversions.getETGraphObject(this);
               TSEEdge edgeReturn = ConnectorPiece.getReturnEdge(etGraphObject);
               if (edgeReturn != null)
               {
                  edgePresentation = TypeConversions.getEdgePresentation(edgeReturn);
               }
            }
            break;

         case IMessageKind.MK_RESULT :
            {
               edgePresentation = TypeConversions.getEdgePresentation(this);
            }
            break;

         default :
            break;
      }

      return edgePresentation;
   }



   /** 
    * Determines the end type of arrow head to draw from the 
    * IPresentationElement's initialization string.
    * 
    * @return The kind of arrowhead for this message.  The value will be one
    *         of the DrawEngineArrowheadKindEnum values.
    * @see DrawEngineArrowheadKindEnum
    */
   protected int getEndArrowKind()
   {
      initLineStyle();
      return m_ArrowHeadKind;
   }

   /**
    * Determines the type of line to draw from the IPresentationElement's 
    * initialization string
    *
    * @return The kind of line for this message.  The value will be one
    *         of the DrawEngineArrowheadKindEnum values.
    * @see DrawEngineArrowheadKindEnum
    */
   protected int getLineKind()
   {
      initLineStyle();
      return m_LineKind;
   }

   /**
    * Determines the type of line, and arrow head to draw from the 
    * IPresentationElement's initialization string
    */
   protected void initLineStyle()
   {
      if ((m_LineKind == DrawEngineLineKindEnum.DELK_UNKNOWN) || (m_ArrowHeadKind == DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD))
      {
         int kind = getMessageKind();
         switch (kind)
         {
            case IMessageKind.MK_CREATE :
               m_LineKind = DrawEngineLineKindEnum.DELK_DOT;
               m_ArrowHeadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
               break;
            default :
            case IMessageKind.MK_SYNCHRONOUS :
               m_LineKind = DrawEngineLineKindEnum.DELK_SOLID;
               m_ArrowHeadKind = DrawEngineArrowheadKindEnum.DEAK_FILLED;
               break;
            case IMessageKind.MK_ASYNCHRONOUS :
               m_LineKind = DrawEngineLineKindEnum.DELK_SOLID;
               m_ArrowHeadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
               break;
            case IMessageKind.MK_RESULT :
               m_LineKind = DrawEngineLineKindEnum.DELK_DOT;
               m_ArrowHeadKind = DrawEngineArrowheadKindEnum.DEAK_FILLED;
               break;

         }
      }

   }

   /**
    * Determines the message kind from either the atached message, or the 
    * initialization string
    * 
    * @return The message type.  The value will be one of the IMessageKind values.
    * @see IMessageKind
    */
   protected int getMessageKind()
   {
      int retVal = IMessageKind.MK_SYNCHRONOUS;

      IETGraphObjectUI parent = getParent();
      if (parent != null)
      {
         IElement element = parent.getModelElement();
         if (element instanceof IMessage)
         {
            IMessage message = (IMessage)element;
            retVal = message.getKind();
         }
         else
         {
            String initStr = getInitializationString();
            int pos = initStr.indexOf(' ');
            initStr = initStr.substring(pos + 1);

            if (initStr.equals("Message create") == true)
            {
               retVal = IMessageKind.MK_CREATE;
            }
            else if (initStr.equals("Message asynchronous") == true)
            {
               retVal = IMessageKind.MK_ASYNCHRONOUS;
            }
            else if (initStr.equals("Message result") == true)
            {
               retVal = IMessageKind.MK_RESULT;
            }
            else
            {
               retVal = IMessageKind.MK_SYNCHRONOUS;
            }
         }

      }

      return retVal;
   }

   /**
    * Move the connector within its associated compartment
    * 
    * @param connector[in]
    */
   protected void moveConnector(TSConnector connector, double nY, boolean doItNow)
   {
      IConnectorsCompartment compartment = (IConnectorsCompartment)TypeConversions.getCompartment(connector, IConnectorsCompartment.class);

      if (compartment != null)
      {
         compartment.moveConnector(connector, nY, doItNow, false);
      }
   }
   
   /**
    * Returns the metatype of the label manager we should use
    *
    * @param The metatype in essentialconfig.etc that defines the label 
    *        manager
    * @return The name of the manager.
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getManagerMetaType(int)
    */
   public String getManagerMetaType(int nManagerKind)
   {
      String retVal = "";

      if (nManagerKind == ETDrawEngine.MK_LABELMANAGER)
      {
         retVal = "MessageLabelManager";
      }

      return retVal;
   }

   /**
    * Inializes the member m_PieceSelf
    */
   protected void initializePieceSelf( TSEEdge edge )
   {
      if( null == edge ) throw new IllegalArgumentException();

      TSConnector pieceConnector = null;

      final int kind = getMessageKind();
      switch( kind )
      {
      default:
         assert ( false );  // can we support this message kind w/ message-to-self?
         // fall through

      case IMessageKind.MK_SYNCHRONOUS:
         pieceConnector = edge.getSourceConnector();
         break;

      case IMessageKind.MK_RESULT:
         pieceConnector = edge.getTargetConnector();
         break;
      }

      // Remember the suspension bar of the message-to-self
      // To be used when the user drags either of the attached messages
      if( pieceConnector != null )
      {
         m_PieceSelf = new LifelinePiece( pieceConnector );
      }
   }

   /**
    * Handles the PreDeleteGatherSelected event by selecting the other end of a message to self
    */
   public void onPreDeleteGatherSelected()
   {
      // Fix W7536:  We need to select the "other" edge for sync, and result messages.
      //             see MessageLabelManagerImpl::OnPreDeleteGatherSelected()
      //             for a related fix.  Before this fix we only did this for the message-to-self.

      IDrawingAreaControl ctrl = getDrawingArea();
      if (ctrl != null)
      {
         IMessage otherMessage = null;

         IElement element = getFirstModelElement();
         if (element instanceof IMessage)
         {
            IMessage message = (IMessage)element;

            if (message != null)
            {
               int kind = message.getKind();
               switch (kind)
               {
                  case IMessageKind.MK_SYNCHRONOUS :
                     otherMessage = getResultFromSender(message);
                     break;

                  case IMessageKind.MK_RESULT :
                     otherMessage = message.getSendingMessage();
                     break;

                  default :
                     // do nothing
                     break;
               }

               if (otherMessage != null)
               {
                  ETList < IPresentationElement > elements = ctrl.getAllItems2(otherMessage);
                  if (elements != null)
                  {
                     for (Iterator < IPresentationElement > iter = elements.iterator(); iter.hasNext();)
                     {
                        IPresentationElement presentation = iter.next();
//                        Debug.assertNull(presentation);//Jyothi

                        if (presentation instanceof IGraphPresentation)
                        {
                           IGraphPresentation gPresentation = (IGraphPresentation)presentation;
                           gPresentation.setSelected(true);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Retrieves the result message associated with the input sending message
    */
   protected IMessage getResultFromSender(IMessage message)
   {
      IMessage retVal = null;

      if (message != null)
      {
         String xmiId = message.getXMIID();
         if (xmiId.length() > 0)
         {
            // Search the entire DOM for the class's lifeline(s)
            IElementLocator elementLocator = new ElementLocator();

            if (elementLocator != null)
            {
               String query = "../UML:Message[@sendingMessage='" + xmiId + "']";

               // Use the element locator to find the model element
               IElement element = elementLocator.findSingleElementByQuery(message, query);
               if (element != null)
               {
                  if (element instanceof IMessage)
                  {
                     retVal = (IMessage)element;
                  }
               }
            }
         }
      }

      return retVal;
   }

// Code needed for the Java port

   /**
    * Retrieve the connector that connects the edge to an activation bar.
    * 
    * @param edge The edge that should connect to an activation bar.
    * @return The Tom Sawyer connector.
    */
   protected TSConnector getActivationBarConnector(TSEEdge edge)
   {
      TSConnector retVal = null;
      switch (getMessageKind())
      {
         default :
         case IMessageKind.MK_SYNCHRONOUS :
            retVal = edge.getTargetConnector();
            break;

         case IMessageKind.MK_RESULT :
            retVal = edge.getSourceConnector();
      }

      return retVal;
   }


   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADEdgeDrawEngine#createButtonHandler()
    */
   public IADDrawEngineButtonHandler createButtonHandler()
   {
      // Not sure if this is needed
      return null;
   }

   //**************************************************
   // Helper Methods
   //**************************************************

   /** 
    * Determines the type of arrow head to draw from the IPresentationElement's 
    * initialization string.
    * 
    * @return The kind of arrowhead for this message.  The value will be one
    *         of the DrawEngineArrowheadKindEnum values.
    * @see DrawEngineArrowheadKindEnum
    */
   protected int getStartArrowKind()
   {
      return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButtonPressed(java.awt.event.MouseEvent)
    */

   public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos)
   {
      return false;
   }
}
