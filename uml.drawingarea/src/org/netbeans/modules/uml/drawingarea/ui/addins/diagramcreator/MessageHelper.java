/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator;

import java.util.HashMap;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
//import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;

/**
 * @author brettb
 *
 * This class is used to track all the information about the messages during SQD CDFS
 */
public class MessageHelper
{
   private IInteraction m_interaction;      /// Interaction the sequence diagram is being created from
   private ETList< IMessage > m_messages;   /// Current list of messages from the interaction
   private int m_iMessageCnt;               /// Number of messages in m_messages, updated via GetMessages()

   /// map used to keep track of synchronous message and its result message
   private HashMap< IMessage, IMessage > m_mapMessageToResult = new HashMap< IMessage, IMessage >();

   /// map used to keep track of the draw engine associated with a message
//   private HashMap< String, IMessageEdgeDrawEngine > m_mapMessageToDE = new HashMap< String, IMessageEdgeDrawEngine >();
   
   
   private final int NEEDS_UPDATE = -1;


   public MessageHelper( IInteraction interaction )
   {
      m_interaction = interaction;
      m_iMessageCnt = NEEDS_UPDATE;
      
      getInteraction();
   }


   public IInteraction getInteraction()
   {
      if( null == m_interaction )   throw new IllegalStateException();
      return m_interaction;
   }

   public ETList< IMessage > getMessages()
   {
      return getMessages( false );
   }
   public ETList< IMessage > getMessages( boolean bRefresh )
   {
      if( bRefresh ||
          (null == m_messages) )
      {
         // Refresh our list of messages, and the count
         m_messages = getInteraction().getMessages();
         m_iMessageCnt = NEEDS_UPDATE;
      }

      if( null == m_messages )   throw new IllegalStateException();
      return m_messages;
   }

   public int getMessageCnt()
   {
      if( NEEDS_UPDATE == m_iMessageCnt )
      {
         m_iMessageCnt = getMessages().getCount();
      }

      return m_iMessageCnt;
   }

   // / Looks for the corresponding result message in the member map
   public IMessage getResultMessage( final IMessage message )
   {
      return m_mapMessageToResult.get( message );
   }

   /**
    * Validates the interaction's elements
    * This ensures all the synchronous messages have result messages
    */
   public void validateInteraction()
   {
      prepareMapMessageToResult();

      for( int iIndx=0; iIndx<getMessageCnt(); iIndx++ )
      {
         IMessage message = getMessages().get( iIndx );
         if( message != null )
         {
            // only process synchronous messages
            int nKind = message.getKind();
            if( IMessageKind.MK_SYNCHRONOUS == nKind )
            {
               iIndx = ensureCorrespondingResultMessage( message, iIndx );
            }
         }
      }
      
      // Uncomment this code to see an output of the messages
      // TEST dumpMessages();
   }

   /**
    * Keep track of the draw engine associated with the input message
    */
//   public void rememberMessageDE( final IMessage message,
//                                  IMessageEdgeDrawEngine messageEdgeDE )
//   {
//      final String strXmiID = getXmiID( message );
//      m_mapMessageToDE.put( strXmiID, messageEdgeDE );
//   }

   /**
    * Get a message's draw engine from the internal map
    */
//   public IMessageEdgeDrawEngine getMessageEdgeDE( final IMessage message )
//   {
//      IMessageEdgeDrawEngine messageEdgeDE = null;
//
//      final String strXmiID = getXmiID( message );
//      return m_mapMessageToDE.get( strXmiID );
//   }


   /**
    * Search through all the messages for results, and place them and their parent into the member map
    */
   protected void prepareMapMessageToResult()
   {
      final int iCnt = getMessageCnt();
      for( int iIndx=0; iIndx<iCnt; iIndx++ )
      {
         IMessage message = getMessages().get( iIndx );
         if( message != null )
         {
            IMessage sendingMessage = message.getSendingMessage();
            if( sendingMessage != null )
            {
               m_mapMessageToResult.put( sendingMessage, message );
            }
         }
      }
   }

   /**
    * Make sure there is a result message for the input message
    */
   protected int ensureCorrespondingResultMessage( final IMessage message,
                                                   int iIndx )
   {
      if( null == message )   throw new IllegalArgumentException();

      final int iOriginalIndx = iIndx;

      ILifeline sendingLifeline = message.getSendingLifeline();
      ILifeline receivingLifeline = message.getReceivingLifeline();
      if( (sendingLifeline != null )&&
          (receivingLifeline != null ) )
      {
         for( iIndx++; iIndx<getMessageCnt(); iIndx++ )
         {
            IMessage currentMessage = getMessages().get( iIndx );
            if( currentMessage != null )
            {
               final int nKind = currentMessage.getKind();
               switch( nKind )
               {
               case IMessageKind.MK_SYNCHRONOUS:
                  {
                     ILifeline lifeline = currentMessage.getSendingLifeline();
                     if( lifeline != null )
                     {
                        boolean bIsSame = receivingLifeline.isSame( lifeline );
                        if( bIsSame )
                        {
                           iIndx = ensureCorrespondingResultMessage( currentMessage, iIndx );
                        }
                        else
                        {
                           return createResultMessage( message, iOriginalIndx, iIndx );
                        }
                     }
                  }
                  break;

               case IMessageKind.MK_RESULT:
                  {
                     ILifeline lifeline = currentMessage.getSendingLifeline();
                     if( lifeline != null )
                     {
                        boolean bIsSame = receivingLifeline.isSame( lifeline );
                        if( bIsSame )
                        {
                           lifeline = currentMessage.getReceivingLifeline();
                           if( lifeline != null )
                           {
                              bIsSame = sendingLifeline.isSame( lifeline );
                              if( bIsSame )
                              {
                                 return iIndx;
                              }
                           }
                        }
                     }

                     return createResultMessage( message, iOriginalIndx, iIndx );
                  }
                  // not needed in java:  break;

               case IMessageKind.MK_CREATE:
               case IMessageKind.MK_ASYNCHRONOUS:
                  // ignore these
                  break;

               default:
                  assert ( false );  // did we add another message kind?
                  break;
               }
            }
         }

         iIndx = createResultMessage( message, iOriginalIndx, iIndx );
      }

      return iIndx;
   }

   /**
    * Create the for the input synchronous message before the input index
    */
   protected int createResultMessage( IMessage message,
                                      int iOriginalIndx,
                                      int iIndx )
   {
      if( null == message )   throw new IllegalArgumentException();

      // Fix W7456:  Ensure that a result message does not already exist for this message
      {
         if( getResultMessage( message ) != null )
         {
            return iIndx;
         }
      }

      ILifeline sendingLifeline = message.getSendingLifeline();
      ILifeline receivingLifeline = message.getReceivingLifeline();
      IInteractionOperand interactionOperand = message.getInteractionOperand();

      if( (sendingLifeline != null) &&
          (receivingLifeline != null) )
      {
         // For message-to-self just create the result message after the message-to-self
         boolean bIsMessageToSelf = sendingLifeline.isSame( receivingLifeline);
         if( bIsMessageToSelf )
         {
            iIndx = iOriginalIndx + 1;
         }
      
         IMessage beforeMessage = null;
         if( iIndx < getMessageCnt() )
         {
            beforeMessage = getMessages().get( iIndx );
         }

         // Insert the message, and associate with the input synchronous message
         IMessage newMessage =
            receivingLifeline.insertMessage( beforeMessage,
                                             interactionOperand,
                                             sendingLifeline,
                                             interactionOperand,
                                             null,
                                             IMessageKind.MK_RESULT );
         if( newMessage != null )
         {
            newMessage.setSendingMessage( message );

            m_mapMessageToResult.put( message, newMessage );
         }

         getMessages( true );
      }

      return iIndx;
   }

   /**
    * Returns the xmi.id of the input element
    */
   protected String getXmiID( IElement element )
   {
      return element.getXMIID();
   }
}
