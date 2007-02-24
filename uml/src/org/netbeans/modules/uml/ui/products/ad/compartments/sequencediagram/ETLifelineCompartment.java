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



package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram;

import com.tomsawyer.graph.TSNode;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ActivationBar;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ConnectorPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.Lifeline;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelineCompartmentPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiecesKind;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ParentPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.SuspensionArea;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.StretchContextType;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.diagramming.TSResizeControl;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
//import com.tomsawyer.editor.TSEWindowState;
import com.tomsawyer.editor.TSEWindowTool;
//import com.tomsawyer.editor.state.TSEResizeGraphObjectState;
import com.tomsawyer.editor.tool.TSEResizeGraphObjectTool;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;


/**
 * 
 * @author Trey Spiva
 */
public class ETLifelineCompartment extends ETCompartment 
   implements IADLifelineCompartment, IConnectorsCompartment
{
   public final static String SQD_LIFELINE_COMPARTMENT = "Lifeline";
   
   /** The compartments lifeline piece. */
   private Lifeline m_Lifeline = null;

   // Variables valid during the stretch operation
   private ParentPiece m_stretchingPiece = null;
   private int m_previousStretchHeight;
   
   /** Maintain the minimum logical values for the left side of this compartment */
   private long m_MinLeft = 0;
   
   /** Maintain the minimum logical values for the right side of this compartment */
   private long m_MaxRight = 40;
   
   private boolean m_WaitingForValidate = false;
   
   /** 
    * Used to store the size before the resize occured.  
    * <br>
    * <bReason:</b> Used correctly render pieces when resizing the node from the
    *               top.  
    */
   private IETRect m_rectPreResize = null;
   
   /*
    * Default Constructor.
    */
   public ETLifelineCompartment()
   {
       m_Lifeline = new Lifeline(this);
       setIsTSWorldCoordinate(true);
   }
   
   /**
    * The minimum height for the lifeline as determined by its lowest piece (excluding the destructor).
    *
    * @return The minimum height in logical dimensions
    */
   public int getMinimumHeight()
   {
      return m_Lifeline.getCompartmentMinimumHeight();
   }
   
   /**
    * Calculates the "best" size for this compartment.  The calculation sets 
    * the member variable m_szCachedOptimumSize, which represents the "best" 
    * size of the compartment at 100%.
    *
    * @param pDrawInfo The draw info used to perform the calculation.
    * @param bAt100Pct pMinSize is either in current zoom or 100% based on this 
    *                  flag.  If bAt100Pct then it's at 100%
    * @return The optimum size of the compartment.
    */
   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
		TSTransform transform = pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform();
      IETSize retVal = null;
      
      if(m_Lifeline != null && transform != null)
      {
			TSTransform atOneToOne = (TSTransform)transform.clone();				
			atOneToOne.setScale(1.0);
      	// These have to be in device at 1 to 1 it will get scaled later, calculateOptimumSize gets called generically 
         retVal = new ETSize(atOneToOne.widthToDevice(m_MaxRight),
				(int)Math.max(m_Lifeline.getMinimumHeight(), atOneToOne.heightToDevice(m_Lifeline.getHeight())));
      }
      
      return bAt100Pct ? retVal : scaleSize(retVal, transform);
   }
 
   /**
    * Draws this compartment.  The lifeline compartment only draws all of the
    * lifeline pieces.  The lifeline pieces are responsible to draw it's 
    * children pieces.
    * 
    * @param pDrawInfo An IDrawInfo structure containing the data to draw
    * @param pBoundingRect A rect describing the bounds in which for this 
    *                      compartment to draw itself.  The compartment must 
    *                      not draw outside this rect.  The rect is in device 
    *                      coordinates.
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#draw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
   	// We need to save off the current bounding rect if we draw of the overview and update the calculations
   	// everything gets messed up, so save it an redraw again with the saved bounds.
   	IETRect currentBoundingRect = m_boundingRect != null && !pDrawInfo.getDrawingToMainDrawingArea() ? (IETRect)this.m_boundingRect.clone() : null;
   	
		internalDraw(pDrawInfo, pBoundingRect);
		if (!pDrawInfo.getDrawingToMainDrawingArea() && currentBoundingRect != null)
		{
			super.draw(pDrawInfo, currentBoundingRect);
		}
   }
   
   private void internalDraw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
		if (this.isTSWorldCoordinate())      	
			super.draw(pDrawInfo, new ETRectEx(pDrawInfo.getTSTransform().boundsToWorld(pBoundingRect.getRectangle())));
		else
			super.draw(pDrawInfo, pBoundingRect);
				
		if (m_Lifeline != null)
		{
			m_Lifeline.draw(pDrawInfo, pDrawInfo.getOnDrawZoom());
		}
   }
   
   /**
    * Adds a new message after the after message.  If the after message is null,
    * the new message will be after all other messages.  If the recieving engine
    * is the same as this engine, the message will be a messge to self.
    *
    * @param toEngine The engine that will recieve this message.
    * @param afterMsg The relative message.  The new message will be after this 
    *                 message.
    *  @param msgType The type of message which can be IMessageKind.MK_SYNCHRONOUS,
    *                 IMessageKind.MK_ASYNCHRONOUS, or IMessageKind.MK_RESULT.
    *                 If the toEngine is the same as this engine, the message
    *                 type will default to MK_SYNCHRONOUS
    * @see org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind
    */
   public void addMessageAfter(ILifelineDrawEngine toEngine, 
                               IMessageEdgeDrawEngine afterMsg, 
                               int msgType)
   {
       IDrawEngine engine = getEngine();
       if (getEngine() instanceof ILifelineDrawEngine)
       {
           ILifelineDrawEngine fromEngine = (ILifelineDrawEngine)getEngine();
           ILifeline fromLifeline = (ILifeline)TypeConversions.getElement(fromEngine); 
           
           ILifeline toLifeline = (ILifeline)TypeConversions.getElement(toEngine);
           
           // Need to account for the activation and suspension bars.
           int location = findBestLocationForMessage(afterMsg, toEngine, false); 
           
           // A message can not be placed on top of a suspension area.
           ParentPiece piece = m_Lifeline.getPieceAt(location);
           if(piece instanceof SuspensionArea)  
           {
               Toolkit.getDefaultToolkit().beep();
               return;
           }
           
           // The interface for adding the metadata, is to add before
           // another message.  So, lets first find the message after the afterMsg.
           IMessage nextMsg = fromEngine.findFirstMessageBelow(location);
           
           ETPairT<IMessage,Integer> sendMsgInfo = createMessage(location, 
                                                         fromEngine, 
                                                         toEngine, 
                                                         nextMsg, 
                                                         fromLifeline,
                                                         toLifeline,
                                                         msgType);
           
           if(msgType == IMessageKind.MK_SYNCHRONOUS)
           {
               ETPairT<IMessage,Integer> resultMsgInfo = createMessage(sendMsgInfo.getParamTwo().intValue(), 
                                                                  toEngine, 
                                                                  fromEngine,
                                                                  nextMsg, 
                                                                  toLifeline,
                                                                  fromLifeline,
                                                                  IMessageKind.MK_RESULT);
               
               IMessage returnMsg = resultMsgInfo.getParamOne();               
               returnMsg.setSendingMessage(sendMsgInfo.getParamOne());
           }
       }
   }

   /**
    * Moving a create message, moves the entire lifeline.  The attached edges
    * start to slant, because they are not adjusted unless the head bumps into
    * the message.  Therefore, adjust the pieces on the created lifeline, to
    * match the amount that the lifeline was moved.
    *
    * @param delta The amount to adjust the pieces.
    */
    public void movingCreate(int delta)
    {
        if(delta >= 0)
        {
            // The create Message is moving down.
            for(ParentPiece curPiece : m_Lifeline.getPieces())
            {
               curPiece.moveBy(-delta, false); 
            }
        }
        else
        {
            ETList < ParentPiece > pieces = m_Lifeline.getPieces();
            int numOfPieces = pieces.size();
            if(numOfPieces > 0)
            {
                ParentPiece lastPiece = pieces.get(numOfPieces - 1);
                if(lastPiece instanceof ConnectorPiece)
                {
                    ConnectorPiece connectorPiece = (ConnectorPiece)lastPiece;
                    connectorPiece.moveConnectorsBy(-delta, true);
                }
            }
        }
    }
    
    public void lifelineTopHeightChanged(int delta)
    {
        // The create Message is moving down.
        for(ParentPiece curPiece : m_Lifeline.getPieces())
        {
            if(curPiece instanceof ConnectorPiece)
            {
                ConnectorPiece connectorPiece = (ConnectorPiece)curPiece;
//                connectorPiece.moveConnectorsBy(-delta, false);
                connectorPiece.moveConnectorsBy(0, false);
            }
        }
    }
    
    private int findBestLocationForMessage(final IMessageEdgeDrawEngine relativeMsg, 
                                           final ILifelineDrawEngine toEngine,
                                           boolean before)
    {
        
        int location = getLocationOfNextMessage();
        
        if(relativeMsg != null)
        {
            TSEEdge tsEdge = relativeMsg.getEdge();
            TSNode sourceNode = tsEdge.getSourceNode();
            if(sourceNode.equals(getGraphObject()) == true)
            {
                location = (int)tsEdge.getSourceConnector().getCenterY();
            }
            else
            {
                location = (int)tsEdge.getTargetConnector().getCenterY();
            }
           
            if(before == true)
            {
                location += LifelineCompartmentPiece.ACTIVATION_BAR_BUFFER * 2;   
            }
            else
            {
                location -=  LifelineCompartmentPiece.ACTIVATION_BAR_BUFFER * 2;
            }
        }
        
        if(relativeMsg == null)
        {
            int toLocation = toEngine.getLifelineCompartment().getLocationOfNextMessage();

            int toTop = toEngine.getBoundingRect().getTop();
            int fromTop = getEngine().getBoundingRect().getTop();

            int diff = Math.abs(toTop - fromTop);
            
            if((toLocation - diff) < location)
            {
                location = toLocation - diff;                
            }
            
            // We always want to make the message after all other pieces, if
            // there is not relative message.
            if(m_Lifeline.getPieces().size() > 0)
            {
                location -=  (LifelineCompartmentPiece.ACTIVATION_BAR_BUFFER * 2);
            }
        } 
        return location;
    }
   
   /**
    * Adds a new message before the after message.  If the before message is null,
    * the new message will be after all other messages.  If the recieving engine
    * is the same as this engine, the message will be a messge to self.
    *
    * @param toEngine The engine that will recieve this message.
    * @param afterMsg The relative message.  The new message will be before this 
    *                 message.
    *  @param msgType The type of message which can be IMessageKind.MK_SYNCHRONOUS,
    *                 IMessageKind.MK_ASYNCHRONOUS, or IMessageKind.MK_RESULT.
    *                 If the toEngine is the same as this engine, the message
    *                 type will default to MK_SYNCHRONOUS
    * @see org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind
    */
   public void addMessageBefore(ILifelineDrawEngine toEngine, 
                                IMessageEdgeDrawEngine beforeMsg, 
                                int msgType)
   {
       IDrawEngine engine = getEngine();
       if (getEngine() instanceof ILifelineDrawEngine)
       {
           ILifelineDrawEngine fromEngine = (ILifelineDrawEngine)getEngine();
           ILifeline fromLifeline = (ILifeline)TypeConversions.getElement(fromEngine); 
           
           ILifeline toLifeline = (ILifeline)TypeConversions.getElement(toEngine);
           
           IMessage relativeMsg = null;
           
           // Need to account for the activation and suspension bars.
           int location = findBestLocationForMessage(beforeMsg, toEngine, true); 
           
           // A message can not be placed on top of a suspension area.
           ParentPiece piece = m_Lifeline.getPieceAt(location);
           if(piece instanceof SuspensionArea)  
           {
               Toolkit.getDefaultToolkit().beep();
               return;
           }
           
           if(getEngine() == toEngine)
           {
               addDecoration("self", new ETPoint(0, location));
           }
           else if(toEngine != null)
           {    
               ETPairT<IMessage,Integer> sendMsgInfo = createMessage(location, 
                                                             fromEngine, 
                                                             toEngine, 
                                                             relativeMsg, 
                                                             fromLifeline,
                                                             toLifeline,
                                                             msgType);

               if(msgType == IMessageKind.MK_SYNCHRONOUS)
               {
                   ETPairT<IMessage,Integer> resultMsgInfo = createMessage(sendMsgInfo.getParamTwo().intValue(), 
                                                                      toEngine, 
                                                                      fromEngine,
                                                                      relativeMsg, 
                                                                      toLifeline,
                                                                      fromLifeline,
                                                                      IMessageKind.MK_RESULT);

                   IMessage returnMsg = resultMsgInfo.getParamOne();               
                   returnMsg.setSendingMessage(sendMsgInfo.getParamOne());
               }
           }
       }
   }
   
   /**
    * Place a specific type of decoration on the node at the specified location.
    *
    * @param type The type of decoration to add.
    * @param pLocation The location of the decoration.
    */
   public void addDecoration(String type, IETPoint pLocation)
   {
      String shortType = type.substring(type.lastIndexOf(' ') + 1);
//      assert shortType.length() <= 0 : "We should always have a decorator type";

      if(shortType.equals("self") == true)
      {
         IDrawEngine engine = getEngine();
         if (engine instanceof ILifelineDrawEngine)
         {  
            ILifelineDrawEngine lifelineEngine = (ILifelineDrawEngine)engine;
            IMessage beforeMsg = lifelineEngine.findFirstMessageBelow(pLocation.getY());
            
            IElement element = TypeConversions.getElement(lifelineEngine);
            if (element instanceof ILifeline)
            {
               ILifeline lifeline = (ILifeline)element;
               // Create the meta data for the top of the reflexive message
               ETPairT<IMessage,Integer> sendMsgInfo = createMessage(pLocation.getY(), 
                                                         lifelineEngine, 
                                                         lifelineEngine, 
                                                         beforeMsg, 
                                                         lifeline, 
                                                         IMessageKind.MK_SYNCHRONOUS);
               IMessage sendMsg = sendMsgInfo.getParamOne();

               ETPairT<IMessage,Integer> returnMsgInfo = createMessage(sendMsgInfo.getParamTwo().intValue(), 
                                                            lifelineEngine, 
                                                            lifelineEngine, 
                                                            beforeMsg, 
                                                            lifeline, 
                                                            IMessageKind.MK_RESULT);
               IMessage returnMsg = returnMsgInfo.getParamOne();
               
               returnMsg.setSendingMessage(sendMsg);
            }
         }
      }
      else if(shortType.equals("destroy") == true)
      {
         IETPoint newPos = createElement(LifelinePiecesKind.LPK_DESTROY, 
                                         pLocation, 
                                         LifelineConnectorLocation.LCL_UNKNOWN);
         if(newPos != null)
         {
				IDrawEngine engine = getEngine();
				if (engine instanceof ILifelineDrawEngine)
				{
					ILifelineDrawEngine lifelineEngine = (ILifelineDrawEngine)engine;
					IElement element = TypeConversions.getElement(lifelineEngine);
					if (element instanceof ILifeline)
					{
						ILifeline lifeline = (ILifeline)element;
						lifeline.createDestructor();
					}
				}
         }
      }
      
   }

   /**
    * Indicate to the compartment that it is being stretched.
    *
    * @param stretchContext[in] Information about the stretch
    */
   public long stretch( IStretchContext stretchContext )
   {
      if( null == stretchContext ) throw new IllegalArgumentException();

      final int type = stretchContext.getType();
      switch (type)
      {
         case StretchContextType.SCT_START :
            {
               IETPoint ptStart = stretchContext.getStartPoint();

               m_stretchingPiece = (ParentPiece)getClosestPiece( ptStart );
               m_previousStretchHeight = 0;

               // Make sure the stretch operation can not move above the top of the closest piece
               if (m_stretchingPiece != null)
               {
                  IETRect rectRestrictedArea = stretchContext.getRestrictedArea();
                  long lTop = rectRestrictedArea.getTop();

                  final int lRestrictedTop = m_stretchingPiece.getRestrictedY();
                  if (lRestrictedTop < lTop)
                  {
                     rectRestrictedArea.setTop( lRestrictedTop );
                     stretchContext.setRestrictedArea( rectRestrictedArea );
                  }
               }
            }
            break;

         case StretchContextType.SCT_STRETCHING :
            if (m_stretchingPiece != null)
            {
               IETSize sizeStretch = stretchContext.getStretchSize();
               if (sizeStretch.getHeight() != m_previousStretchHeight)
               {
                  IETPoint ptStart = stretchContext.getStartPoint();
                  final IETRect rectBounding = getLogicalBoundingRect();
                  final int iCompartmentY = rectBounding.getTop() - ptStart.getY();

                  // Because TS uses an inverted y-axis, use the inverse stretch size
                  stretchPiece( iCompartmentY, m_previousStretchHeight - sizeStretch.getHeight() );
                  m_previousStretchHeight = sizeStretch.getHeight();
               }
            }
            break;

         case StretchContextType.SCT_CANCELING :
            if (m_stretchingPiece != null)
            {
               IETPoint ptStart = stretchContext.getStartPoint();

               // Return all the pieces to their original sizes
               stretchPiece( ptStart.getY(), m_previousStretchHeight );
            }
            // no break

         case StretchContextType.SCT_FINISH :
            m_stretchingPiece = null;
            m_previousStretchHeight = 0;
            break;

         default :
            assert(false); // Did we add another stretch context type?
            break;
      }
      
      return 0;
   }

   /**
    * Creates a new message between two draw engines.  The model element will 
    * also be created.
    * 
    * @param pLocation The location of the message.
    * @param fromEngine The draw engine that will send the message.
    * @param toEngine The draw engine that will recieve the message.
    * @param beforeMsg The message that wil be berfore the new message.
    * @param lifeline The lifeline used to create the message.
    * @param type The type of message to create.  The value must be one of the
    *             IMessageKind values.
    * @return The new message that was created.
    * @see IMessageKind
    */
   protected ETPairT<IMessage,Integer> createMessage( int yLocation, 
                                                      ILifelineDrawEngine fromEngine,
                                                      ILifelineDrawEngine toEngine, 
                                                      IMessage beforeMsg, 
                                                      ILifeline lifeline,
                                                      int type )
   {
      return createMessage(yLocation, fromEngine, toEngine, beforeMsg, lifeline, lifeline, type);
   }
   
   /**
    * Creates a new message between two draw engines.  The model element will 
    * also be created.
    * 
    * @param pLocation The location of the message.
    * @param fromEngine The draw engine that will send the message.
    * @param toEngine The draw engine that will recieve the message.
    * @param beforeMsg The message that wil be berfore the new message.
    * @param lifeline The lifeline used to create the message.
    * @param type The type of message to create.  The value must be one of the
    *             IMessageKind values.
    * @return The new message that was created.
    * @see IMessageKind
    */
   protected ETPairT<IMessage,Integer> createMessage( int yLocation, 
                                                      ILifelineDrawEngine fromEngine,
                                                      ILifelineDrawEngine toEngine, 
                                                      IMessage beforeMsg, 
                                                      ILifeline fromLifeline,
                                                      ILifeline toLifeline,  
                                                      int type )
   {
       IMessage retMessage = null;
       Integer retLocation = new Integer(yLocation);
       
       try
       {
           retMessage = fromLifeline.insertMessage( beforeMsg, null, toLifeline, null, null, type );
           if(retMessage != null)
           {
               // Create the TS edge for the message
               ETPairT<IMessageEdgeDrawEngine,Integer> messageInfo =
                       fromEngine.createMessage( retMessage, toEngine, yLocation );
               retLocation = messageInfo.getParamTwo();
           }
       }
       catch(Exception e)
       {
           e.printStackTrace();
       }
       
       return new ETPairT<IMessage,Integer>(retMessage,retLocation);
   }
   
   /**
    * Compare the side values and update if they have grown.  This routine is 
    * called by the lifeline pieces.
    * 
    * @param left The left side.
    * @param right The right side.
    */
   public void updateSides(long left, long right)
   {
      m_MinLeft = Math.min(m_MinLeft, left);
      m_MaxRight = Math.max(m_MaxRight, right);
   }
   
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADLifelineCompartment#validatePieces()
	 */
	public void validatePieces()
	{
		// we must call validate 1st, and then update the reflexive messages
      // This is because the connectors must be in their proper location before
      // updating the reflexive messages.  The reason for this is that activation bar
      // maintains the message-to-self flag for its parent suspension area.
      m_Lifeline.validate();
      m_Lifeline.updateReflexiveBends();

      m_WaitingForValidate = false;
	}

	/**
    * Ensures all the pieces are valid, i.e. either have child pieces, or 
    * connectors attached.
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADLifelineCompartment#updateReflexiveBends()
	 */
	public void updateReflexiveBends()
	{
		m_Lifeline.updateReflexiveBends();
	}

    /**
     * Forces any activation bars that should be connected to connect to each 
     * other.
     * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADLifelineCompartment#cleanUpActivationBars()
     */
   public void cleanUpActivationBars()
   {
        m_Lifeline.cleanUpActivationBars();
        m_Lifeline.cleanUpChildrenActivationBars();
   }
        
   /**
    * Returns the location of where the next message should be placed.  The
    * returned location will always be the last message.
    *
    * @return The Y location of where the next message should be placed.
    */
   public int getLocationOfNextMessage()
   {
       // First assume that it will be at the top of the lifline.
       int engineTop = (int)getEngine().getLogicalBoundingRect(false).getTop();
       int engineHeight = (int)getEngine().getLogicalBoundingRect(false).getHeight();
       int compartmentStart = engineTop - (int)getLogicalBoundingRect().getTop();
       
//       int retVal = (int)getBoundingRect().getHeight() / 2;
       int retVal = engineTop + compartmentStart;
       
       ETList < ParentPiece > pieces = m_Lifeline.getPieces();
       
       int lastPos = 0;
       if((pieces != null) && (pieces.size() > 0))
       {
           ParentPiece lastPiece = pieces.get(pieces.size() - 1);
           lastPos = lastPiece.getBottom();
       }
       
       if(lastPos > 0)
       {
//           lastPos += compartmentStart;
//           retVal = (engineHeight / 2) - lastPos;
           retVal = engineTop - compartmentStart - lastPos;
       }
       
       return retVal;
   }

   /**
    * Returns the (normalized) logical bounding rect of the draw engine (node) 
    * containing this piece.
    */
   public IETRect getEngineLogicalBoundingRect(boolean bNormalize)
   {
      IETRect retVal = null;
      
      IDrawEngine engine = getEngine();
      if(engine != null)
      {
         retVal = TypeConversions.getLogicalBoundingRect(engine);
         
         if(bNormalize == true)
         {
            int top = retVal.getTop();
            retVal.setTop(retVal.getBottom());
            retVal.setBottom(top);
         }
      }
      
      return retVal;
   }

   /**
	 * Called when the context menu is about to be displayed.  The compartment should add whatever buttons
	 * it might need.
	 *
	 * @param pContextMenu [in] The context menu about to be displayed
	 * @param logicalX [in] The logical x location of the context menu event
	 * @param logicalY [in] The logical y location of the context menu event
	 */
	public void onContextMenu(IMenuManager manager)
	{
		//m_MessageToSelfBar = null;
		if (getEnableContextMenu())
		{
			// Even though the name implies that the input coordinates are logical, they are NOT
			// The input points are WinScaledOwner.
			// Therefore, we use the special handling via GetClosestPiece() below.
			// Also, we don't ensure that the coordinates are within the bounding rect,
			// because they make be to the right of the bounding rect, and GetClosestPiece() will handle that.
			boolean isDestroyed = getIsDestroyed();
			if (isDestroyed)
			{
				addLifelineRemoveDestroyButton(manager);
			}
			
		}
	}

   /**
    * Posts a delayed action to validate the node.
    */
   public void postValidateNode()
   {
      if( ! m_WaitingForValidate )
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               IDrawEngine engine = getEngine();
               if(engine != null)
               {
                  engine.validateNode();
               }
            }
         });
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADLifelineCompartment#getIsDestroyed()
    */
   public boolean getIsDestroyed()
   {
      boolean retVal = false;
      
      if(m_Lifeline != null)
      {
         retVal = m_Lifeline.isDestroyed();
      }
      
      return retVal;
   }

   /**
	* This is the name of the drawengine used when storing and reading from the product archive.
	*
	* @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	* product archive (etlp file).
	*/
   public String getCompartmentID()
   {
	   return "ADLifelineCompartment";
   }
   
   /**
    * Restore from the product archive.
    *
    * @param pProductArchive The archive we're reading from
    * @param pCompartmentElement The element where this compartment's 
    *                            information should exist
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public void readFromArchive(IProductArchive        pProductArchive, 
                               IProductArchiveElement pCompartmentElement)
   {
      // Make sure we don't validate the node until the connectors are attached
      // see PostLoad()
      m_WaitingForValidate = true;
      
      super.readFromArchive(pProductArchive, pCompartmentElement);
      
      // Read in our stuff
      IProductArchiveElement lifelineElement = pCompartmentElement.getElement(SQD_LIFELINE_COMPARTMENT);
      if(lifelineElement != null)
      {
         m_Lifeline.readFromArchive(lifelineElement);
      }
   }

   /**
    * Notification of a post load event.
    */
   public long postLoad()
   {
      // Make sure the compartment bounding rectangle is up-to-date.
      if( m_engine != null )
      {
         m_engine.layout();
      }

      cleanConnectors();
      m_Lifeline.attachConnectors();

      // Reenable the PostValidate() calls, see ReadFromArchive()
      m_WaitingForValidate = false;

      return 0;
   }

   /**
    * Saves the compartment stuff to the product archive.
    *
    * @param pProductArchive The archive we're saving to
    * @param pElement The current element, or parent for any new attributes
    *                 or elements
    * @return The created element for this compartment's information
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, 
                                                IProductArchiveElement pEngineElement)
   {
      IProductArchiveElement retVal = null;
      
      retVal = super.writeToArchive(pProductArchive, pEngineElement);
      if(retVal != null)
      {
         m_Lifeline.writeToArchive(retVal);
      }
      
      
      return retVal;
   }
   
   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransformOwner#getLogicalBoundingRect()
    */
   public IETRect getLogicalBoundingRect()
   {
   	if (isTSWorldCoordinate())
			return getBoundingRect();
		else
			return new ETRectEx(this.getTransform().boundsToWorld(getBoundingRect().getRectangle()));
   }
   
   /**
    * The the node's logical bounding rectangle's top, except when resizing
    * during a resize the top is from the initial bouding rectangle's top
    * 
    */
   public int getDrawTop()
   {
//      int retVal = getAbsoluteOwnerOrigin().getY();
//      int retVal = getBoundingRect().getTop();
//      int retVal = getEngineLogicalBoundingRect(false).getTop();

      int boundingTop = getBoundingRect().getTop();
      int engineTop   = getEngineLogicalBoundingRect(false).getTop();
      
      int retVal = engineTop - boundingTop;
      
      if(m_rectPreResize != null)
      {
         retVal += (getEngineLogicalBoundingRect(false).getTop() - m_rectPreResize.getTop());
      }
      
      return retVal;
   }
   
   /**
    * Creates a lifeline element in the compartment
    * 
    * @param kind The type of element to create
    * @param pConnector Tom Sawyer connector to be attached to this piece
    * @param lclCorner Enumerated value for the location on the piece where the
    *                  Tom Sawyer connector is to be attached.
    * @return The location of the new element.  The center of the connector
    *         will be updated to reflect the location.
    */ 
   public IETPoint createElement( int         kind,
                                  TSConnector pConnector,
                                  int         lpcCorner )
   {      
      IETPoint retVal = null;
      
      IETPoint compartmentPt = new ETPointEx(pConnector.getCenter());
      int centerY = (int)(pConnector.getCenterY() - getBoundingRect().getTop());
      compartmentPt.setY(Math.abs(centerY));
      
      LifelineCompartmentPiece newPiece = createElement(compartmentPt, kind);
      if((newPiece != null) && (pConnector != null))
      {
         if (newPiece instanceof ConnectorPiece)
         {
            ConnectorPiece connector = (ConnectorPiece)newPiece;
            connector.attachConnector( pConnector, lpcCorner, true );
         }
         
         retVal = new ETPoint((int)pConnector.getCenterX(),
                              newPiece.getLogicalTop());
      }
      
      return retVal;                 
   }
   
   /**
    * Creates a lifeline element in the compartment
    * 
    * @param kind The type of element to create
    * @param point The desired location for the creation of the 
    *              piece, and the actual location for the piece 
    *              after creation, Tom Sawyer logical drawing 
    *              area coordinates.
    * @param lclCorner Enumerated value for the location on the piece where the
    *                  Tom Sawyer connector is to be attached.
    */ 
   public IETPoint createElement( int      kind,
                                  IETPoint point,
                                  int      lpcCorner )
   {      
      IETPoint retVal = null;
      
      IETPoint pt = null;
      if(point == null)
      {
         point = new ETPoint(0, getLogicalBoundingRect().getBottom());   
      }
      pt = logicalToCompartmentLogical(point);
      
      LifelineCompartmentPiece newPiece = createElement(pt, kind);
      if(newPiece != null)
      {      
         // Return the new vertical location    
         retVal = new ETPoint(point.getX(), newPiece.getLogicalTop());
      }
   
      return retVal;                 
   }

   /**
    * Creates the various elements on the lifeline at the specified logical location
    * 
    * @param iVerticalLocation logical vertical location for the new lifeline element
    * @param kind LifelinePiecesKind for the kind of piece to create
    */
   public LifelineCompartmentPiece createElement( int iVerticalLocation, int kind )
   {
      IETPoint ptInCompartmentLogical = logicalToCompartmentLogical( new ETPoint( 0, iVerticalLocation ) );
      return createElement( ptInCompartmentLogical, kind );
   }
   
//   public LifelinePiece CreateLifelinePiece( int kind,
//                                            IETPoint inCompartmentCoordinates )
//   {
//      LifelinePiece retVal = null;
//
//      IETPoint pt = logicalToCompartmentLogical(inCompartmentCoordinates)
//
//      return retVal;
//   }

   /**
    * Connects a return edge to the bottom of the pieces connected to the input 
    * connectors.  The input pieces are used to determine the lifeline pieces 
    * to start/end the newly created return message.
    *
    * @param fromConnector The piece attached to this connector will be the 
    *                      starting piece for the return message.
    * @param toConnector The piece attached to this connector will be the ending
    *                    piece for the return message.
    * @param pReturnEdge The return edge to be connected.
    *
    * @return HRESULT
    */
   public void connectReturnEdge( TSConnector fromConnector,
                                  TSConnector toConnector,
                                  TSEEdge     returnEdge )
   {
      if ((fromConnector != null) && (toConnector != null) && (returnEdge != null))
      {
         cleanUpActivationBars();
         
         TSGraphObject fromObject = fromConnector.getOwner();
         TSGraphObject toObject   = toConnector.getOwner();
         
         boolean isMessageToSelf = false;
         if(toObject.equals(fromObject) == true)
         {
            isMessageToSelf = true;
         }
         
         // Determine which corners should be used for the connector update
         int lclFrom = LifelineConnectorLocation.LCL_BOTTOMRIGHT;
         int lclTo   = LifelineConnectorLocation.LCL_BOTTOMLEFT;
         if( isMessageToSelf )
         {
               lclFrom = LifelineConnectorLocation.LCL_BOTTOMRIGHT;
               lclTo   = LifelineConnectorLocation.LCL_BOTTOMRIGHT;
         }
         else
         {            
            if( toConnector.getCenterX() < fromConnector.getCenterX() )
            {
               lclFrom = LifelineConnectorLocation.LCL_BOTTOMLEFT;
               lclTo   = LifelineConnectorLocation.LCL_BOTTOMRIGHT;
            }
         }
         
         TSConnector sourceConnector = createConnector(fromConnector, lclFrom);
         TSConnector targetConnector = createConnector(toConnector, lclTo);
         
         if((sourceConnector != null) && (targetConnector != null))
         {
            if(isMessageToSelf == false)
            {
               // I believe there is a C++ bug here, because the connectors
               // being compared need to be the source & target, i.e. bottom connectors
               double toY   = targetConnector.getCenterY();
               double fromY = sourceConnector.getCenterY();
   
               if( Math.abs(fromY - toY) >= 1 )
               {
                  ICompartment compartment = TypeConversions.getCompartment(targetConnector, 
                                                                            IConnectorsCompartment.class);
                  
                  if (compartment instanceof IConnectorsCompartment)
                  {
                     IConnectorsCompartment connectorC = (IConnectorsCompartment)compartment;
                     connectorC.moveConnector(targetConnector, 
                                              Math.max(toY, fromY),
                                              false, 
                                              true);
                  }                                                          
               }
            }
            
            returnEdge.setSourceConnector(sourceConnector);
            returnEdge.setTargetConnector(targetConnector);
         }
      }
   }
   
   /**
    * Indicates that a message edge can be started from the current logical 
    * location.
    
    * @param ptLogical Logical view coordinates to test.
    * @param pvbCanStartMessage <code>true</code> if the location is a place 
    *                           where a message can be started.
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADLifelineCompartment#canStartMessage(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public boolean canStartMessage(IETPoint pLogical)
   {
      boolean retVal = false;
      
      LifelineCompartmentPiece piece = getClosestPiece(pLogical);
      if (piece instanceof Lifeline)
      {
         Lifeline lifeline = (Lifeline)piece;
         retVal = true;
      }
      else if (piece instanceof ActivationBar)
      {        
         IETRect pieceRect = piece.getLogicalBoundingRect();
         long maxY = pieceRect.getTop() - LifelineCompartmentPiece.ACTIVATION_BAR_BUFFER;
         
         if(pLogical.getY() <= maxY)
         {
            retVal = true;
         }
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADLifelineCompartment#canFinishMessage(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public boolean canFinishMessage(IETPoint pLogical)
   {
      boolean retVal = true;
      
      LifelineCompartmentPiece piece = getClosestPiece(pLogical);
      
      // Fix W1856:  Message to activation bar does not work
      // Messages are allowed to finish on an activation bar,
      // if there is not a message already starting the activation bar.
      if (piece instanceof ConnectorPiece)
      {
         ConnectorPiece connector = (ConnectorPiece)piece;
         IETPoint compartmentLogical = logicalToCompartmentLogical(pLogical);
         
         retVal = connector.canFinishMessage(compartmentLogical.getY());
      }
      
      return retVal;
   }

   /**
    * Indicates that a message edge can be finished from the current logical location.
    *
    * @param ptLogicalLogical view coordinates to test
    * @return <code>true</code> if the location is a place where a message 
    *         can be finished
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADLifelineCompartment#canFinishMessage(int, int)
    */
   public boolean canFinishMessage(double x, double y)
   {
      return canFinishMessage(new ETPoint((int)x, (int)y));   
   }

   /**
    * Indicates that a message edge can be finished from the current logical location.
    *
    * @param ptLogical[in] Logical view coordinates to test
    *
    * @return true if the location is a place where a message can be finished
    */
   public boolean canReallyFinishMessage( int messageKind,
                                          IADLifelineCompartment fromCompartment,
                                          final IETPoint ptLogical )
   {
      if( null == fromCompartment ) throw new IllegalArgumentException();

      boolean bCanFinishMessage = true;   // default is true
      
      if (IMessageKind.MK_CREATE == messageKind)
      {
         // Fixed issue 82144
         // Now, a "Create Message" can not finish on a receiving lifeline if there's already
         // a message attached to the receiving lifeline.
         ETList attachedMessages = this.getAttachedEdges();
         if (attachedMessages != null && attachedMessages.size() > 0)
         {
            bCanFinishMessage = false;
         }
      }
      else 
      {
         bCanFinishMessage = canFinishMessage( ptLogical );
         if ((bCanFinishMessage) &&(IMessageKind.MK_SYNCHRONOUS == messageKind))
         {
            IElement element = TypeConversions.getElement( fromCompartment );
            if (element instanceof ILifeline)
            {
               ILifeline fromLifeline = (ILifeline)element;
               
               final IETPoint ptCompartmentLogical = logicalToCompartmentLogical( ptLogical );
               ParentPiece piece = m_Lifeline.findActivationBarNear( ptCompartmentLogical.getY());
               
               if (piece instanceof ConnectorPiece)
               {
                  ConnectorPiece connector = (ConnectorPiece)piece;
                  
                  ETList< IMessage > messages = new ETArrayList< IMessage >();
                  if( messages != null )
                  {
                     connector.getPropagatedMessages( messages );
                     
                     boolean bAsyncMsgFound = false;
                     boolean bFromLifelineFound = false;
                     
                     for (Iterator iter = messages.iterator(); iter.hasNext();)
                     {
                        IMessage message = (IMessage)iter.next();
                        
                        // For a synchronous message, an async message can not come back to the from lifeline
                        if( !bAsyncMsgFound )
                        {
                           int kind = message.getKind();
                           if( IMessageKind.MK_ASYNCHRONOUS == kind )
                           {
                              bAsyncMsgFound = true;
                           }
                        }
                        
                        if( !bFromLifelineFound )
                        {
                           ILifeline lifeline = message.getReceivingLifeline();
                           if( lifeline != null )
                           {
                              bFromLifelineFound = lifeline.isSame( fromLifeline );
                           }
                        }
                        
                        if( bAsyncMsgFound && bFromLifelineFound )
                        {
                           bCanFinishMessage = false;
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
      
      return bCanFinishMessage;
   }
   
   //**************************************************
   // IConnectorsCompartment implementation
   //**************************************************
   
   /**
    * Connects a message to this compartment
    *
    * @param pPoint[in] Logical location for the connector
    * @param kind[in] The kind of message being connected
    * @param bMessageIsLeftToRight[in] The direction of the message
    * @result The connector that is attached.
    * 
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IConnectorsCompartment#connectMessage(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, int, int)
    */
   public TSEConnector connectMessage(IETPoint point, int kind, int connectMessageKind, TSEConnector connector )
   {
      TSEConnector retVal = null;
      
      int lpk = LifelinePiecesKind.LPK_UNKNOWN;
      int lcl = LifelineConnectorLocation.LCL_UNKNOWN;
      int disallowedMessageLocations = ConnectorPiece.MLF_TOPLEFT | ConnectorPiece.MLF_TOPRIGHT;
      
      switch(kind)
      {
         case IMessageKind.MK_CREATE:
            if((connectMessageKind & IConnectMessageKind.CMK_START) ==  IConnectMessageKind.CMK_START)
            {
               lpk = LifelinePiecesKind.LPK_ATOMIC_FRAGMENT; 
            }
            else
            {
                lpk = LifelinePiecesKind.LPK_UNKNOWN;
            }
               
            if((connectMessageKind & IConnectMessageKind.CMK_RIGHT_TO_LEFT) ==  IConnectMessageKind.CMK_RIGHT_TO_LEFT)
            {
               lcl = LifelineConnectorLocation.LCL_TOPLEFT; 
            }
            else
            {
               lcl = LifelineConnectorLocation.LCL_TOPLEFT;
            }
            break;
         case IMessageKind.MK_ASYNCHRONOUS:
            if((connectMessageKind & IConnectMessageKind.CMK_FINISH) ==  IConnectMessageKind.CMK_FINISH)
            {
               lpk = LifelinePiecesKind.LPK_ACTIVATION_FINISH; 
            }
            else
            {
                lpk = LifelinePiecesKind.LPK_ATOMIC_FRAGMENT;
            }
                  
            if((connectMessageKind & IConnectMessageKind.CMK_RIGHT_TO_LEFT) ==  IConnectMessageKind.CMK_RIGHT_TO_LEFT)
            {
               lcl = LifelineConnectorLocation.LCL_TOPLEFT; 
            }
            else
            {
               lcl = LifelineConnectorLocation.LCL_TOPLEFT;
            }
            break;
         case IMessageKind.MK_RESULT:
            if((connectMessageKind & IConnectMessageKind.CMK_FINISH) ==  IConnectMessageKind.CMK_FINISH)
            {
               lpk = LifelinePiecesKind.LPK_SUSPENSION; 
            }
            else
            {
                lpk = LifelinePiecesKind.LPK_ACTIVATION_FINISH;
            }
                     
            if((connectMessageKind & IConnectMessageKind.CMK_RIGHT_TO_LEFT) ==  IConnectMessageKind.CMK_RIGHT_TO_LEFT)
            {
               lcl = LifelineConnectorLocation.LCL_BOTTOMRIGHT; 
            }
            else
            {
               lcl = LifelineConnectorLocation.LCL_BOTTOMLEFT;
            }
            disallowedMessageLocations = ConnectorPiece.MLF_BOTTOMRIGHT;
            break;
           
         default:
         case IMessageKind.MK_SYNCHRONOUS:
            if((connectMessageKind & IConnectMessageKind.CMK_FINISH) ==  IConnectMessageKind.CMK_FINISH)
            {
               lpk = LifelinePiecesKind.LPK_ACTIVATION_FINISH; 
            }
            else
            {
                lpk = LifelinePiecesKind.LPK_SUSPENSION;
            }
                        
            if((connectMessageKind & IConnectMessageKind.CMK_RIGHT_TO_LEFT) ==  IConnectMessageKind.CMK_RIGHT_TO_LEFT)
            {
               lcl = LifelineConnectorLocation.LCL_TOPLEFT; 
            }
            else
            {
               lcl = LifelineConnectorLocation.LCL_TOPRIGHT;
            }
            disallowedMessageLocations = ConnectorPiece.MLF_BOTTOMRIGHT;
            break;
      }
      
      IETPoint compartmentLogical = logicalToCompartmentLogical( point );
      
      LifelineCompartmentPiece newPiece = createElement(compartmentLogical, lpk);
      if (newPiece instanceof ConnectorPiece)
      {
         ConnectorPiece connectorPiece = (ConnectorPiece)newPiece;
         TSConnector newConnector = connectorPiece.createConnector(lcl);
         if (newConnector instanceof TSEConnector)
         {
            retVal = (TSEConnector)newConnector;
         }
      }
      
      return retVal;
   }

   /**
    * Updates the TS connectors of all the parts' lifeline.
    *
    * @param pDrawInfo The current drawing context
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IConnectorsCompartment#updateConnectors(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
    */
   public void updateConnectors(IDrawInfo pDrawInfo)
   {
      // If we're drawing to the overview window don't update the connectors
      boolean isDrawingToMainWnd = true;
      if(pDrawInfo != null)
      {
         isDrawingToMainWnd = pDrawInfo.getDrawingToMainDrawingArea();
      }
      
      if (isDrawingToMainWnd && getEngine() != null)
      {
         m_Lifeline.updateConnectorsViaTopCenter();
      }
      
		// Order is important, we need to update the visible connectors first.
		// then update the ones off the screen, and then make sure every thing
		// is horizontal.
		updateOffScreenConnectors(pDrawInfo);	
		updateOnScreenConnectors(pDrawInfo);	
   }

   /**
    * Moves the connector to the vertical location, in logical view coordinates.
    *
    * @param connector The connector to move.
    * @param y Where to move the connector.
    * @param doItNow if <code>true</code> the the change will be rendered.
    * @param setYOfAssociatedPiece This parameter is ignored.
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IConnectorsCompartment#moveConnector(com.tomsawyer.editor.TSConnector, double, boolean, boolean)
    */
   public void moveConnector(TSConnector connector, 
                             double      y, 
                             boolean     doItNow, 
                             boolean     setYOfAssociatedPiece)
   {
      if(connector != null)
      {
         double delta = connector.getCenterY() - y;
         if(delta != 0)
         {
            ConnectorPiece piece = ConnectorPiece.getPieceAttachedToConnector(connector);
            if(piece != null)
            {
               int lcl = piece.getLocation(connector);
               if((lcl == LifelineConnectorLocation.LCL_BOTTOMLEFT) || 
                  (lcl == LifelineConnectorLocation.LCL_BOTTOMRIGHT))
               {
                  int newHeight = (int)(piece.getHeight() + delta);
                  piece.setHeight(newHeight);
               }
               else
               {
                  piece.setLogicalTop((int)y);
               }
               
               //Fixed issue 78491.
               postValidateNode();
               
               if(doItNow == true)
               {
                  if(getEngine() != null)
                  {
                     getEngine().invalidate();
                  }
               }
            }
         }
      }      
   }

   /**
    * Notification that something has happened to the node this engine is attached to.
    *
    * @param nKind [in] The event that just occured.
    */
   public long onGraphEvent( int nKind )
   {
      super.onGraphEvent( nKind );

      switch( nKind )
      {
      case IGraphEventKind.GEK_PRE_RESIZE:
         handlePreResize();
         break;

      case IGraphEventKind.GEK_POST_RESIZE:
         if( m_rectPreResize != null )
         {
            m_Lifeline.resizeTopBy( m_rectPreResize.getTop() - getEngineLogicalBoundingRect( false ).getTop() );
   
            // Make sure the connector offsets are reset to referencing the top
            m_rectPreResize = null;
            m_Lifeline.updateConnectorsViaTopCenter();

            validatePieces();
         }
         break;

      case IGraphEventKind.GEK_POST_SMARTDRAW_MOVE:
         m_Lifeline.updateReflexiveBends();
         break;

      default:
         // do nothing
         break;
      }
      
      return 0;
   }
   
   /**
    * Returns the closest lifeline piece to this point, in logical view 
    * coordinates.  Assumes that the point is specified in diagram coordinates.
    *
    * @param ptLogical Logical view coordinates to test.
    * @return Piece found, which may contain a <code>null</code> value
    */
   public LifelineCompartmentPiece getClosestPiece(IETPoint point)
   {
      return getClosestPiece(point, true);
   }
   
   /**
    * Returns the closest lifeline piece to this point, in logical view 
    * coordinates.
    *
    * @param ptLogical Logical view coordinates to test
    * @return Piece found, which may contain a NULL value
    */
   public LifelinePiece getClosestLifelinePiece( IETPoint logical )
   {
      LifelinePiece retVal = null;

      IETPoint pt = logicalToCompartmentLogical(logical);
      ParentPiece piece = m_Lifeline.findActivationBarNear(pt.getY());
      
      retVal = createLifelinePiece(piece);

      return retVal;
   }
   
   /**
    * Creates a lifeline element in the compartment.
    *
    * @param kind The type of piece to create.  Value must be one of the
    *             LifelineConnectorLocation values.
    * @param logicalCoordinates The desired location for the creation of
    *                           the piece, and the actual location for the piece
    *                           after creation, Tom Sawyer logical drawing area 
    *                           coordinates.
    * @return The created piece, NULL if not created
    * @see LifelineConnectorLocation
    */
   public LifelinePiece createLifelinePiece(int kind,
                                            IETPoint logicalCoordinates)
   {
      LifelinePiece retVal = null;

      IETPoint pt = logicalToCompartmentLogical(logicalCoordinates);
      LifelineCompartmentPiece newPiece = createElement(pt, kind);
      
      if(newPiece != null)
      {
         retVal = createLifelinePiece(newPiece);
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADLifelineCompartment#copyLifelinePiece(org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiece)
    */
   public TSConnector copyLifelinePiece(LifelinePiece piece)
   {
      return piece.copyTo( this );
   }
   
   /**
    * Creates a LifelinePiece that is to be associated with a 
    * LifelineCompartmentPiece.
    * 
    * @param piece The piece to associate with a LifelinePiece.
    * @return The LifelinePiece.
    */
   protected LifelinePiece createLifelinePiece(LifelineCompartmentPiece piece)
   {
      LifelinePiece retVal = null;

      if (piece instanceof ConnectorPiece)
      {
         retVal = new LifelinePiece((ConnectorPiece)piece);
      }

      return retVal;
   }
   
   //**************************************************
   // Protected Methods
   //**************************************************

   /**
    * Creates a specified element on the lifeline.
    *
    * @param inCompartmentLogicial Location within the draw engine to place 
    *                              the new element
    * @param kind The type of element to create @see LifelinePiecesKind
    *
    * @return The created element.
    */
   protected LifelineCompartmentPiece createElement(IETPoint inCompartmentLogicial,
                                          int      kind)
   {
      LifelineCompartmentPiece retVal = null;
      
      if(m_Lifeline != null)
      {
         retVal = m_Lifeline.createPiece(kind, inCompartmentLogicial.getY());
      }
      
      return retVal;
   }

   /**
    * Process the GEK_PRE_RESIZE OnGraphEvent
    */
   protected void handlePreResize()
   {
      m_rectPreResize = getEngineLogicalBoundingRect( false );

      // We are sharing code with ADContainerDrawEngineImpl.handlePreResize(), and
      // should probably make a helper class that supports this type of resize operation

      // All we are really trying to do in the C++ code is determine what quadrant
      // is being used for the resize.  TS has this available on the state so we'll
      // use that capabaility here. 

//      TSEWindowState state = getGraphWindow().getCurrentState();
      TSEWindowTool state = getGraphWindow().getCurrentState();
//      if (state instanceof TSEResizeGraphObjectState)
      if (state instanceof TSEResizeGraphObjectTool)
      {
//         TSEResizeGraphObjectState resizeState = (TSEResizeGraphObjectState)state;
         TSEResizeGraphObjectTool resizeState = (TSEResizeGraphObjectTool)state;
         
         int grapple = resizeState.getGrapple();
         if( TSResizeControl.GRAPPLE_N == (grapple & TSResizeControl.GRAPPLE_N) )
         {
            m_Lifeline.updateConnectorsViaBottomCenter();
         }
      }
   }

   /**
    * Returns the closest lifeline piece to this point, in logical view 
    * coordinates.
    *
    * @param ptLogical Logical view coordinates to test.
    * @param isDiagramCoords Specifies if the pint
    * @return Piece found, which may contain a <code>null</code> value
    */
   protected LifelineCompartmentPiece getClosestPiece(IETPoint point,
                                                      boolean  isDiagramCoords)
   {
      LifelineCompartmentPiece retVal = null;
      
      IETPoint pt = null;
      if(isDiagramCoords == true)
      {
         pt = logicalToCompartmentLogical(point);
      }
      else
      {
         double zoom = getZoomLevel(null);
         pt = new ETPoint((int)(point.getX() / zoom), 
                          (int)(point.getY() / zoom) - getLogicalOffsetInDrawEngineRect().getY());
      }
      
      if(pt.getY() >= 0)
      {
         retVal = m_Lifeline.findPieceAt(pt.getY());
      }
      
      return retVal;
   }

   /**
    * Grow a piece on the lifeline, adjusting all affected pieces.
    *
    * @param iStretchFromY[in]
    * @param iStretchDelta[in]
    */
   protected void stretchPiece( int iStretchFromY, int iStretchDelta )
   {
      if( (iStretchDelta != 0) &&
          (m_stretchingPiece != null) )
      {
         m_stretchingPiece.stretch( iStretchFromY, iStretchDelta );

         if( (m_engine != null) &&
             (m_engine instanceof INodeDrawEngine) )
         {
            INodeDrawEngine nodeDE = (INodeDrawEngine)m_engine;
            if( nodeDE != null )
            {
               nodeDE.resizeToFitCompartment( this, false, false );
            }
         }
      }
   }

   /**
    * Remove the TS saved connector association from all the connectors on the DE
    */
   protected void cleanConnectors()
   {
      // TS persists the connector userField.
      // Use this routine to clear that out.

      ILifelineDrawEngine lifelineDE = (ILifelineDrawEngine)m_engine;
      if( lifelineDE != null )
      {
         // Iterate through the connectors attached to the compartment's node
         TSENode node = lifelineDE.getNode();
         if( node != null )
         {
            List list = node.connectors();
            for (Iterator iter = list.iterator(); iter.hasNext();)
            {
               TSConnector connector = (TSConnector)iter.next();
               
               connector.setUserObject( null );
            }
         }
      }
   }
   
   /**
    * Creates a connector at the indicated location of the piece connected to 
    * the input connector.
    *
    * @param pInputConnector Connector used to determine the node, and piece to 
    *                        associate the connector with
    * @param lclCorner Corner of the piece where the new connector will be 
    *                  attached.  The value must be on of the 
    *                  LifelineConnectorLocation values.
    * @return The created connector
    * @see LifelineConnectorLocation
    */
   protected TSConnector createConnector(TSConnector inputConnector,
                                         int         lclCorner)
   {       
      TSConnector retVal = null;

      if ((inputConnector != null) && (lclCorner != LifelineConnectorLocation.LCL_UNKNOWN))
      {
         assert inputConnector.getUserObject() instanceof ConnectorPiece : 
                "We have a connector without a associated piece.";

         // This check should never fail.  However, to be safe I will make a 
         // sanity check.
         if (inputConnector.getUserObject() instanceof ConnectorPiece)
         {
            ConnectorPiece piece = (ConnectorPiece)inputConnector.getUserObject();
            retVal = piece.createConnector(lclCorner);
         }
      }      

      return retVal;
   }
	/* (non-Javadoc)
	 * This method exists because LifelineCompartment still thinks it should use logical coordinates
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#isPointInCompartment(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
	 */
	public boolean isPointInCompartment(IETPoint pPoint) {
		boolean retValue = false;
		IETRect deviceRect = getDeviceBoundingRect();
		  
		if (deviceRect != null)
		{
			retValue = deviceRect.contains(pPoint);
		}
		return retValue;
	}
	
	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pMenuAction)
	{
		return true;
	}
	
	public boolean onHandleButton(ActionEvent e, String id)
	{
		boolean bHandled = false;
		if (id.equals("MBK_LL_REMOVE_DESTROY"))
		{
			m_Lifeline.setDestroyed(false);
			bHandled = true;
		}
      
      refresh();
      
		return bHandled;
	}
	
	/*
	 * Returns all attached edges to our parent drawEngine. 
	 */
	public ETList<IETEdge> getAttachedEdges()
	{
		ETList<IETEdge> edges = new ETArrayList<IETEdge>();
   	
		IETNodeUI ui = (IETNodeUI)this.getEngine().getUI();
		IETNode node =(IETNode) ui.getOwner();
		return node != null ?  node.getEdges() : null;
	}
		

	/*
	 * Aligns the tops of the offscreen connetors with 'this' the visible compartment.
	 */
	protected void updateOffScreenConnectors(IDrawInfo pDrawInfo, IETEdge edge)
	{
		if (edge == null || pDrawInfo == null)
			return;
			
		IETNode fromNode = edge.getFromNode();			
		IETNode toNode = edge.getToNode();
			
		if (edge.getETUI().isOnTheScreen(pDrawInfo.getTSEGraphics()) && fromNode != null && toNode != null)
		{
			TSEEdge pTSEdge = (TSEEdge)edge;
			TSConnector fromConnector = pTSEdge.getSourceConnector();		
			TSConnector toConnector = pTSEdge.getTargetConnector();
			
			IETGraphObjectUI fromNodeUI = fromNode.getETUI();
				
			if (fromNodeUI != null && !fromNodeUI.isOnTheScreen(pDrawInfo.getTSEGraphics()))
			{
				moveConnector(fromConnector, toConnector.getCenterY(), false, true);												  
				//fromConnector.setCenter(fromConnector.getCenterX(), toConnector.getCenterY());
			}
				
			IETGraphObjectUI toNodeUI = toNode.getETUI();
				
			if (toNodeUI != null && !toNodeUI.isOnTheScreen(pDrawInfo.getTSEGraphics()))
			{
				moveConnector(toConnector, fromConnector.getCenterY(), false, true);
				//toConnector.setCenter(toConnector.getCenterX(), fromConnector.getCenterY());
			}				
		}		
	}
	
	/*
	 * Returns the ETLifelineCompartment that ownes the Connector,
	 */
	protected ETLifelineCompartment getLifeLineConnectorOwner(IETEdge edge, TSConnector connector)
	{
		if (connector == null)
			return null;
			
		TSGraphObject connectorOwner = connector.getOwner();
		if (connectorOwner == this.getEngine().getUI().getOwner())
		{
			// I'm the owner.
			return this;
		}
		else if (edge != null)
		{
			IDrawEngine otherDrawEngine = null;
			
			//Find the other drawEngine.
			IETGraphObject fromOwner = (IETGraphObject)edge.getFromNode().getEngine().getParentETElement();
			IETGraphObject toOwner = (IETGraphObject)edge.getToNode().getEngine().getParentETElement();
			
			if (connectorOwner == fromOwner)
				otherDrawEngine = fromOwner.getEngine();
			else if (connectorOwner == toOwner)
				otherDrawEngine = toOwner.getEngine();
			
			if (otherDrawEngine != null)
			{
				// Loop over the compartments to fine the lifelineCompartment.				
				IteratorT<ICompartment> iter = new IteratorT<ICompartment>(otherDrawEngine.getCompartments());
				while (iter.hasNext())
				{
					ICompartment compartment = iter.next();
					if (compartment instanceof ETLifelineCompartment)
					{
						return (ETLifelineCompartment)compartment;	
					}
				}
			}
		}
		return null;
	}
	
	/*
	 * Notifies both lifeline compartments that there connection position has changed, making sure that the edges stay horizontal.
	 */
	protected void updateOnScreenConnectors(IDrawInfo pDrawInfo, IETEdge edge)
	{
		if (edge == null || pDrawInfo == null)
			return;

		IETNode fromNode = edge.getFromNode();			
		IETNode toNode = edge.getToNode();
			
		if (edge.getETUI().isOnTheScreen(pDrawInfo.getTSEGraphics()) && fromNode != null && toNode != null)
		{
			TSEEdge pTSEdge = (TSEEdge)edge;
			TSConnector fromConnector = pTSEdge.getSourceConnector();		
			TSConnector toConnector = pTSEdge.getTargetConnector();
			
			IETGraphObjectUI fromNodeUI = fromNode.getETUI();
			IETGraphObjectUI toNodeUI = toNode.getETUI();
				
				// Only if both nodes are on the screen should we update.
			if (fromNodeUI.isOnTheScreen(pDrawInfo.getTSEGraphics()) && 
				toNodeUI.isOnTheScreen(pDrawInfo.getTSEGraphics()))
			{
				// make sure they stay horizontal, so avg them together.
				double avgY = (toConnector.getCenterY() + fromConnector.getCenterY()) /2;
				ETLifelineCompartment fromOwner = getLifeLineConnectorOwner(edge,fromConnector);
				ETLifelineCompartment toOwner = getLifeLineConnectorOwner(edge,toConnector);
				
				if (fromOwner != null)
					fromOwner.moveConnector(fromConnector, avgY, false, true);
				
				if (toOwner != null)
					toOwner.moveConnector(toConnector, avgY, false, true);
			}	
		}		
	}
	
	/*
	 * Loops over the owners edges and updates the offscreen connector positions.
	 */
	protected void updateOffScreenConnectors(IDrawInfo pDrawInfo)
	{
		// Now only update the non visible connectors if we are drawing on the main display window.
		if (pDrawInfo != null && pDrawInfo.getDrawingToMainDrawingArea())
		{
			ETList<IETEdge> attachedEdges = this.getAttachedEdges();
			IteratorT<IETEdge> iter = new IteratorT<IETEdge>(attachedEdges);
			while (iter.hasNext())
			{
				updateOffScreenConnectors(pDrawInfo, iter.next());
			}			
		}
	}
	
	/*
	 * Notifies connected lifeline compartments that there connection position has changed, making sure that the edges stay horizontal.
	 */
	protected void updateOnScreenConnectors(IDrawInfo pDrawInfo)
	{
		if (pDrawInfo != null && pDrawInfo.getDrawingToMainDrawingArea())
		{
			ETList<IETEdge> attachedEdges = this.getAttachedEdges();
			IteratorT<IETEdge> iter = new IteratorT<IETEdge>(attachedEdges);
			while (iter.hasNext())
			{
				updateOnScreenConnectors(pDrawInfo, iter.next());
			}			
		}		
	}
}
