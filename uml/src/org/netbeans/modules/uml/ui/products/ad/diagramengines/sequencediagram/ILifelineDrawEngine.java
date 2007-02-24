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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADLifelineCompartment;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.graph.TSEdge;

/**
 * @author sumitabhk
 *
 */
public interface ILifelineDrawEngine extends IADNodeDrawEngine
{
    enum InsertMessageEnum 
    {
        /** Inserts the edge after all other edges on the lifeline. */
        ADD_END, 
        
        /** Inserts a new edge after a specified message. */
        ADD_AFTER, 
        
        /** Inserts a new edge before a specified message. */
        ADD_BEFORE
    };
    
   /** Returns a connector for the target end of create message edge */
   public TSConnector getConnectorForCreateMessage();

   /** Moves this draw engine so the create message, if it exists, is horizontal */
   public void makeCreateMessageHorizontal();

   /** Find 1st message below the logicial vertical location on the draw engine's diagram */
   public IMessage findFirstMessageBelow(int lY);

   /** Find 1st message above the logicial vertical location on the draw engine's diagram */
   public IMessage findFirstMessageAbove(int lY);

   /** Creates a return message on the bottom of the pieces connected to the input connectors */
   public void createReturnMessage(TSEEdge             synchronousEdge, 
                                   IMessage            synchronousMessage, 
                                   IInteractionOperand interactionOperand, 
                                   IMessage            beforeMessage);

   /** Creates a message, type determined by the input message, to the other draw engine */
   public ETPairT<IMessageEdgeDrawEngine,Integer> createMessage(IMessage pMessage, ILifelineDrawEngine pToEngine,
         int plVerticalLocation);

   /** Returns true when there are messages attached to the lifeline */
   public boolean hasMessagesAttached();

   /** Returns true when the lifeline is terminated by a destroy element */
   public boolean isDestroyed();

   /** Removes any incoming operations, and messages if desired, from this lifeline */
   public void removeIncomingOperations(boolean bRemoveAssociatedMessages);

   /** Returns all the message to self edges that are part of this lifeline */
   public ETList < IPresentationElement > getAllMessageToSelfs();
   
   /** Retrieves the lifeline compartment for the draw engine. */
   public IADLifelineCompartment getLifelineCompartment();
   
   /** Returns true when the lifeline is created on this diagram (has a create message attached) */
   public boolean isCreated();
   
   public void insertMessageBefore(ILifelineDrawEngine to, 
                                   int msgType, 
                                   IMessageEdgeDrawEngine relativeMessage);

   public void insertMessageAfter(ILifelineDrawEngine to, 
                                   int msgType, 
                                   IMessageEdgeDrawEngine relativeMessage);

   public void addDestroyMessage();

   public void addMessageToSelf(IMessageEdgeDrawEngine targetMsg, boolean before);

   public void addCreateMessage(IMessageEdgeDrawEngine targetMsg, boolean before);
}


