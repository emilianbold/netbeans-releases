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
package org.netbeans.modules.uml.ui.addins.diagramcreator;

import java.util.HashMap;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;

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
   private HashMap< String, IMessageEdgeDrawEngine > m_mapMessageToDE = new HashMap< String, IMessageEdgeDrawEngine >();
   
   
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
   public void rememberMessageDE( final IMessage message,
                                  IMessageEdgeDrawEngine messageEdgeDE )
   {
      final String strXmiID = getXmiID( message );
      m_mapMessageToDE.put( strXmiID, messageEdgeDE );
   }

   /**
    * Get a message's draw engine from the internal map
    */
   public IMessageEdgeDrawEngine getMessageEdgeDE( final IMessage message )
   {
      IMessageEdgeDrawEngine messageEdgeDE = null;

      final String strXmiID = getXmiID( message );
      return m_mapMessageToDE.get( strXmiID );
   }


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
   
   /**
    * Output the interaction's messages to the output console
    */
   private void dumpMessages()
   {
      if( m_interaction != null )
      {
         ETList< IMessage > allMessages = m_interaction.getMessages();
         if( allMessages != null )
         {
            // Track the number of indents to use during the print
            int indentIndex = 0;
            int synchronousCnt = 0;
            int resultCnt = 0;

            int count = allMessages.getCount();
            for( int index=0; index<count; index++ )
            {
               IMessage message = allMessages.item( index );
               if( message != null )
               {
                  ILifeline sendingLifeline = message.getSendingLifeline();
                  ILifeline receivingLifeline = message.getReceivingLifeline();
                  if( (sendingLifeline != null) &&
                      (receivingLifeline != null) )
                  {
                     int nKind = message.getKind();
                     if( IMessageKind.MK_RESULT == nKind )
                     {
                        indentIndex--;
                        resultCnt++;
                     }

                     assert ( indentIndex >= 0 );

                     String strMessageXMIID = message.getXMIID();
                     String strSendingXMIID = sendingLifeline.getXMIID();
                     String strReceivingXMIID = receivingLifeline.getXMIID();

                     for( int i=0; i<indentIndex; i++ )
                     {
                        ETSystem.out.print("   ");
                     }

                     ETSystem.out.print( "(" + nKind + " " + strMessageXMIID + ") " );

                     switch( nKind )
                     {
                     case IMessageKind.MK_CREATE:
                        ETSystem.out.println( strSendingXMIID + " -! " + strReceivingXMIID );
                        break;

                     case IMessageKind.MK_SYNCHRONOUS:
                        ETSystem.out.println( strSendingXMIID + " -> " + strReceivingXMIID );
                        break;

                     case IMessageKind.MK_ASYNCHRONOUS:
                        ETSystem.out.println( strSendingXMIID + " >> " + strReceivingXMIID );
                        break;

                     case IMessageKind.MK_RESULT:
                        {
                           ETSystem.out.println( strReceivingXMIID + " <- " + strSendingXMIID );

                           String strSendingMessageXMIID = "";
                           IMessage sendingMessage = message.getSendingMessage();
                           if( sendingMessage != null )
                           {
                              strSendingMessageXMIID = sendingMessage.getXMIID();
                           }

                           ETSystem.out.println( "(" + strSendingMessageXMIID + ")" );
                        }
                        break;
                     }

                     if( IMessageKind.MK_SYNCHRONOUS == nKind )
                     {
                        indentIndex++;
                        synchronousCnt++;
                     }
                  }
               }
            }
            assert ( resultCnt == synchronousCnt );
         }
      }
   }
}
