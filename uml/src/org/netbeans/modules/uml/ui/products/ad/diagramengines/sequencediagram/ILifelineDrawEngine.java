/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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


