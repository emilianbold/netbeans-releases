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


package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelineCompartmentPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiece;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;
import org.netbeans.modules.uml.ui.support.relationshipVerification.EdgeVerificationImpl;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSDEdge;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;

/**
 * The ADEdgeVerificationImpl provides drawing support for an TSGraphObject.
 * There is a one to one relationship between an TSGraphObject and an 
 * ADEdgeVerificationImpl.
 * 
 * @author Trey Spiva
 */
public class ADEdgeVerificationImpl extends EdgeVerificationImpl
{

   public ADEdgeVerificationImpl()
   {
      super();
   }

   /**
    * Create the model element for the message.  The from and to elements must
    * be either a lifeline or an interaction fragment.
    *
    * @param pEdge TS edge representing the IMessage to be created
    * @param sInitializationString Original string used to create the Tom Sawyer edge
    * @param pFromNode TS node at the start of the edge
    * @param pToNode TS node at the finish of the edge
    * @param pFromElement Element associated with the TS node at the start of the edge
    * @param pToElement Element associated with the TS node at the finish of the edge
    * @return The created IMessage
    * @see org.netbeans.modules.uml.ui.support.relationshipVerification.EdgeVerificationImpl#processMessage(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge, java.lang.String, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
    */                                        
   protected IElement processMessage(IETEdge   pEdge, 
                                     String   sInitializationString, 
                                     IETNode  pFromNode, 
                                     IETNode  pToNode, 
                                     IElement pFromElement, 
                                     IElement pToElement)
   {
      IMessage retVal = null;

      if ((pEdge != null)                 || 
          (sInitializationString != null) || 
          (pFromNode != null)             || 
          (pToNode != null)               || 
          (pFromElement != null)          || 
          (pToElement != null))
      {
         boolean isFromLifeline = pFromElement instanceof ILifeline;
         boolean isFromFragment = pFromElement instanceof IInteractionFragment;
         boolean isToLifeline = pToElement instanceof ILifeline;
         boolean isToFragment = pToElement instanceof IInteractionFragment;

         // In force the restriction:
         // The from element must be either a lifeline or an interaction fragment.
         // The to element must be either a lifeline or an interaction fragment
         if (((isFromLifeline == true) || (isFromFragment == true)) && ((isToLifeline == true) || (isToFragment == true)))
         {
            IDiagram diagram = TypeConversions.getDiagram(pEdge);
            IDiagramEngine engine = TypeConversions.getDiagramEngine(diagram);

            if (engine instanceof IADSequenceDiagEngine)
            {
               IADSequenceDiagEngine sqdEngine = (IADSequenceDiagEngine)engine;

               // Determine if the the message was placed inside a combined 
               // fragment
               ETPairT < IInteractionOperand, ICompartment > interactionData = sqdEngine.getEdgesInteractionOperand(pEdge);

               // Find the message that the new message is inserted before
               IMessage beforeMessage = findBeforeMessage(sqdEngine, (TSDEdge)pEdge);

               int messageKind = determineMessageTypeFromInitializationString(sInitializationString);
               retVal = createMessage(pFromElement, 
                                      pToElement,
                                      beforeMessage, 
                                      interactionData.getParamOne(), 
                                      messageKind);

               // If we have a synchronous messge then we need to create a 
               // return message.
               if ((retVal != null) && (messageKind == IMessageKind.MK_SYNCHRONOUS))
               {
                  IMessage returnMessage = createMessage(pToElement, 
                                                         pFromElement, 
                                                         beforeMessage, 
                                                         interactionData.getParamOne(), 
                                                         IMessageKind.MK_RESULT);

                  if (returnMessage != null)
                  {
                     returnMessage.setSendingMessage(retVal);

                     // Create the TS edge for the message.
                     TSNode tsToNode = null;
                     if (pToNode instanceof TSNode)
                     {
                        tsToNode = (TSNode)pToNode;
                     }

                     TSNode tsFromNode = null;
                     if (pFromNode instanceof TSNode)
                     {
                        tsFromNode = (TSNode)pFromNode;
                     }

                     TSEdge returnEdge = createReturnEdge(sqdEngine, returnMessage, tsToNode, tsFromNode);
                     updateReturnEdgeLocation((TSDEdge)pEdge, (TSDEdge)returnEdge);
                  }
               }
            }
         }
      }

      return retVal;
   }

   /**
    * Determine the 1st message located below the input TS edge.
    * 
    * @param engine The diagram engine to search.
    * @param edge The edge to use a reference.
    * @return The edge before the reference edge.
    */
   protected IMessage findBeforeMessage(IADSequenceDiagEngine engine, TSDEdge edge)
   {
      IMessage retVal = null;

      if (edge != null)
      {
         TSConnector connector = edge.getTargetConnector();
         if (connector == null)
         {
            connector = edge.getSourceConnector();
         }

         if (connector != null)
         {
            IElement element = engine.findFirstElementBelow("Message", (int)connector.getCenterY());
            if (element instanceof IMessage)
            {
               retVal = (IMessage)element;
            }
         }
      }

      return retVal;
   }

   /**
    * Create the specific message kind between the input elements, and before the specified message
    *
    * @param pFromElement The source of the message.
    * @param pToElement The target of the message.
    * @param pBeforeMessage The message before the new message.
    * @param pInteractionOperand The interaction operand that will contian the
    *                            message.  The interaction operand may be null.
    * @param kind The type of message.  Must be one of the IMessageKind values.
    * @return The new message.
    * 
    * @see IMessageKind
    */
   protected IMessage createMessage(IElement fromElement, 
                                    IElement toElement, 
                                    IMessage beforeMessage, 
                                    IInteractionOperand interaction, 
                                    int messageKind)
   {
      IMessage retVal = null;

      if ((fromElement != null) && (toElement != null))
      {
         boolean isFromLifeline = fromElement instanceof ILifeline;
         boolean isFromFragment = fromElement instanceof IInteractionFragment;
         boolean isToLifeline = toElement instanceof ILifeline;
         boolean isToFragment = toElement instanceof IInteractionFragment;

         // In force the restriction:
         // The from element must be either a lifeline or an interaction fragment.
         // The to element must be either a lifeline or an interaction fragment
         if (((isFromLifeline == true) || 
              (isFromFragment == true)) && 
              ((isToLifeline == true) || 
               (isToFragment == true)))
         {                
            // The operation is not known when the user 1st creates the message,
            // So, pass in NULL for the operation
            if(isFromLifeline == true)
            {
               ILifeline fromLifeline = (ILifeline)fromElement;
               retVal = fromLifeline.insertMessage(beforeMessage, 
                                                   interaction, 
                                                   toElement, 
                                                   interaction, 
                                                   null, 
                                                   messageKind);    
            }
            else if(isFromFragment == true)
            {
               IInteractionFragment fromFrag = (IInteractionFragment)fromElement;
               
               IInteraction curInteraction = null;//(IInteraction)fromFrag;
               if(fromFrag instanceof IInteraction)
               { 
                  curInteraction = (IInteraction)fromFrag;                                 
               }
               else if (fromFrag instanceof IInteractionOccurrence)
               {
                  IInteractionOccurrence occurence = (IInteractionOccurrence)fromFrag;
                  curInteraction = occurence.getInteraction();
               }               
               else if (fromFrag.getOwner() instanceof IInteraction)
               {
                  curInteraction = (IInteraction)fromFrag.getOwner();                  
               }
               
               if(curInteraction != null)
               {
                  retVal = curInteraction.insertMessage(beforeMessage, 
                                                        toElement, 
                                                        interaction, 
                                                        null, 
                                                        messageKind);
               }
            }
         }
      }

      return retVal;
   }

   /**
    * Creates a return TS edge between the two nodes.
    *
    * @param engine The dagram engine that will recieve the return message.
    * @param pMessage The message to associated with the TS edge, also 
    *                 determines edge type
    * @param pFromNode The node where the edge starts from
    * @param pToNode The node where the edge finishes
    * @reutrn The created edge
    */
   protected TSEdge createReturnEdge(IDiagramEngine engine, 
                                     IMessage message, 
                                     TSNode fromNode, 
                                     TSNode toNode)
   {
      TSEdge retVal = null;

      if((engine != null) && 
         (message != null) &&
         (fromNode != null) &&
         (toNode   != null))
      {
         IDrawingAreaControl ctrl = engine.getDrawingArea();
         if(ctrl != null)
         {
            ctrl.setModelElement(message);
            
            try
            {
               retVal = ctrl.addEdge("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message result", 
                                     fromNode, toNode, false, false);
            }
            catch (ETException e)
            {
               retVal = null;
            }
            
            ctrl.setModelElement(null);
         }
      }
      
      return retVal;
   }

   protected void updateReturnEdgeLocation(TSDEdge edge, TSDEdge returnEdge)
   {
      if((edge != null) && (returnEdge != null))
      {
         double xCenter = 0;
         double yCenter = 0;
         
         TSConnector targetConnector = edge.getTargetConnector();
         if(targetConnector != null)
         {
            TSConnector returnConnector = createReturnConnector(targetConnector);
            if(returnConnector != null)
            {
               returnEdge.setSourceConnector(returnConnector);
               xCenter = returnConnector.getCenterX();
               yCenter = returnConnector.getCenterY();
            }
         }
         
         TSConnector sourceConnector = edge.getSourceConnector();
         if(sourceConnector != null)
         {
            TSConnector returnConnector = createReturnConnector(sourceConnector);
            if(returnConnector != null)
            {
               returnEdge.setTargetConnector(returnConnector);
            }
         }
         
         if((xCenter != 0) && (yCenter != 0))
         {
            IDrawEngine drawEngine = TypeConversions.getDrawEngine(returnEdge);
            if (drawEngine instanceof IMessageEdgeDrawEngine)
            {
               IMessageEdgeDrawEngine messageDE = (IMessageEdgeDrawEngine)drawEngine;
               messageDE.move((int)yCenter, false);
            }
         }
      }
   }

   protected TSConnector createReturnConnector(TSConnector edgeConnector)
   {
      TSConnector retVal = null;

      if(edgeConnector != null)
      {
         LifelinePiece piece = new LifelinePiece(edgeConnector);
         if(piece.isValid() == true)
         {
            int returnCorner = LifelineConnectorLocation.LCL_BOTTOMRIGHT;
            if(piece.getConnectorLocation() == LifelineConnectorLocation.LCL_TOPLEFT)
            {
               returnCorner = LifelineConnectorLocation.LCL_BOTTOMLEFT;
            }
            
            retVal = piece.createConnector(returnCorner);
         }
         else
         {
            // Invalid piece, so create our own connector
            TSGraphObject tsObj = edgeConnector.getOwner();
            if (tsObj instanceof TSENode)
            {
               TSENode node = (TSENode)tsObj;
               double yOffset = edgeConnector.getConstantYOffset() + LifelineCompartmentPiece.PIECE_HEIGHT;
               
               retVal = node.addConnector();
               retVal.setProportionalXOffset(edgeConnector.getProportionalXOffset());
               retVal.setConstantYOffset(yOffset);
               retVal.setVisible(false);              
            }
         }
      }

      return retVal;
   }
}
