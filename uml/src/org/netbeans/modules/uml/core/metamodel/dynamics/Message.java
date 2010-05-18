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


package org.netbeans.modules.uml.core.metamodel.dynamics;

import java.util.Collections;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.behavior.ICallAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;

/**
 * @author josephg
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * @author josephg
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * @author aztec
 */
public class Message extends NamedElement implements IMessage
{
    private static final int ANT_UNKNOWN = -1;
    private static final int ANT_INCREMENT = 0;
    private static final int ANT_NEXT_LEVEL = 1;
    private static final int ANT_INCREMENT_AND_NAME = 2;
    private static final int ANT_NAME_INCREMENT = 3;

    private static final int CFT_UNKNOWN = -1;
    // This message is not in a combined fragment
    private static final int CFT_NONE    =  0;
    // This message is in a different combined fragment from the previous 
    // message
    private static final int CFT_CHANGE  =  1;

    // This message is in the same combined fragment as the previous message
    private static final int CFT_SAME    =  2;
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#delete()
     */
    public void delete()
    {
        IInteraction inter = getInteraction();
        if (inter != null)
            inter.resetAutoNumbers(this);
        super.delete();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getInteraction()
     */
    public IInteraction getInteraction()
    {
        return new ElementCollector<IInteraction>()
            .retrieveSingleElementWithAttrID( this, "interaction", IInteraction.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#setInteraction(org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction)
     */
    public void setInteraction(IInteraction interaction)
    {
        new ElementConnector<IMessage>()
            .setSingleElementAndConnect( 
                this, interaction, "interaction",
                new IBackPointer<IInteraction>()
                {
                    public void execute(IInteraction inter)
                    {
                        inter.addMessage(Message.this);
                    }
                },
                new IBackPointer<IInteraction>()
                {
                    public void execute(IInteraction inter)
                    {
                        inter.removeMessage(Message.this);
                    }
                } );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getConnector()
     */
    public IConnector getConnector()
    {
        return new ElementCollector<IConnector>( )
            .retrieveSingleElementWithAttrID( this, "connector", IConnector.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#setConnector(org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector)
     */
    public void setConnector(IConnector connector)
    {
        setElement( connector, "connector" );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getSendEvent()
     */
    public IEventOccurrence getSendEvent()
    {
        return new ElementCollector<IEventOccurrence>( )
            .retrieveSingleElementWithAttrID( this, "sendEvent", IEventOccurrence.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#setSendEvent(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void setSendEvent(final IEventOccurrence event)
    {
        new ElementConnector<IMessage>( )
            .addChildAndConnect( 
                this, true, "sendEvent", "sendEvent", 
                event,
                new IBackPointer<IMessage>( )
                {
                    public void execute(IMessage mess)
                    {
                        event.setSendMessage(mess);
                    }
                } );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getReceiveEvent()
     */
    public IEventOccurrence getReceiveEvent()
    {
        return new ElementCollector<IEventOccurrence>( )
            .retrieveSingleElementWithAttrID( this, "receiveEvent", IEventOccurrence.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#setReceiveEvent(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void setReceiveEvent(final IEventOccurrence event)
    {
        new ElementConnector<IMessage>( )
            .addChildAndConnect( 
                this, true, "receiveEvent", "receiveEvent", 
                event,
                new IBackPointer<IMessage>( )
                {
                    public void execute(IMessage mess)
                    {
                        event.setReceiveMessage(mess);
                    }
                } );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getInitiatingAction()
     */
    public IExecutionOccurrence getInitiatingAction()
    {
        return new ElementCollector<IExecutionOccurrence>( )
            .retrieveSingleElementWithAttrID( this, "initiatingAction", IExecutionOccurrence.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#setInitiatingAction(org.netbeans.modules.uml.core.metamodel.dynamics.IExecutionOccurrence)
     */
    public void setInitiatingAction(IExecutionOccurrence action)
    {
        setElement( action, "initiatingAction" );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getKind()
     */
    public int getKind()
    {
        return getMessageKind("kind");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#setKind(int)
     */
    public void setKind(int newKind)
    {
        setMessageKind("kind", newKind);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getOperationInvoked()
     */
    public IOperation getOperationInvoked()
    {
        IOperation op = null;
        IEventOccurrence event = getReceiveEvent();
        if (event != null)
        {
            IExecutionOccurrence exec = event.getFinishExec();
            if (exec instanceof IProcedureOccurrence)
                op = ((IProcedureOccurrence) exec).getOperation();
        }
        return op;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#setOperationInvoked(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void setOperationInvoked(IOperation newVal)
    {
        boolean origBlock = EventBlocker.startBlocking();
        try
        {
            IInteraction inter = getInteraction();
            if (inter instanceof INamespace)
            {
                INamespace space = (INamespace) inter;
                IEventOccurrence sendEvent    = getSendEvent(),
                                 receiveEvent = getReceiveEvent();
                if (newVal != null)
                {
                    if (sendEvent != null)
                    {
                        IExecutionOccurrence startExec = 
                                sendEvent.getStartExec();
                        if (startExec instanceof IActionOccurrence)
                        {
                            IActionOccurrence accOcc = 
                                    (IActionOccurrence) startExec;
                            IAction action = accOcc.getAction();
                            if (!(action instanceof ICallAction))
                            {
                                ICallAction ca = 
                                    new TypedFactoryRetriever<ICallAction>()
                                        .createType("CallAction");
                                if (action != null)
                                    action.delete();
                                
                                accOcc.setAction(ca);
                                space.addElement(ca);
                            }
                        }
                        
                        if (receiveEvent != null)
                        {
                            IExecutionOccurrence finishExec = 
                                    receiveEvent.getFinishExec();
                            
                            IProcedureOccurrence procOcc = null;
                            if (!(finishExec instanceof IProcedureOccurrence))
                            {
                                if (finishExec != null)
                                {
                                    finishExec.delete();
                                    // This is a work around. 6.2 works differenctly - JM
                                    String s = XMLManip.getAttributeValue(receiveEvent.getNode(), "finishExec");
                                    if (s != null && s.length() > 0)
                                    {
                                    	XMLManip.setAttributeValue(receiveEvent.getNode(), "finishExec", "");
                                    }
                                }
                                
                                procOcc = 
                                    new TypedFactoryRetriever
                                        <IProcedureOccurrence>()
                                            .createType("ProcedureOccurrence");
                                space.addOwnedElement(procOcc);
                                receiveEvent.setFinishExec(procOcc);
                            }
                            else
                            {    
                                procOcc = (IProcedureOccurrence) finishExec;
                            }
                            
                            if (procOcc != null)
                                procOcc.setOperation(newVal);
                        }
                    }
                }
                else    // newVal == null
                {
                    // The invoked operation is being removed
                    // So, delete the events, which should clean up the rest
                    if (sendEvent != null)
                        sendEvent.delete();
                    if (receiveEvent != null)
                        receiveEvent.delete();
                }
            }
        }
        finally
        {
            EventBlocker.stopBlocking(origBlock);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getReceivingLifeline()
     */
    public ILifeline getReceivingLifeline()
    {
        IEventOccurrence event = getReceiveEvent();
        return event != null? event.getLifeline() : null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getSendingLifeline()
     */
    public ILifeline getSendingLifeline()
    {
        IEventOccurrence event = getSendEvent();
        return event != null? event.getLifeline() : null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getReceivingClassifier()
     */
    public IClassifier getReceivingClassifier()
    {
        ILifeline line = getReceivingLifeline();
        return line != null? retrieveClassifier(line) : null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getSendingClassifier()
     */
    public IClassifier getSendingClassifier()
    {
        ILifeline line = getSendingLifeline();
        return line != null? retrieveClassifier(line) : null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getReceivingOperations()
     */
    public ETList<IOperation> getReceivingOperations()
    {
        IClassifier cl = getReceivingClassifier();
        return cl != null? cl.getOperations() : null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getSendingMessage()
     */
    public IMessage getSendingMessage()
    {
        return new ElementCollector<IMessage>()
                .retrieveSingleElementWithAttrID( this, "sendingMessage", IMessage.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#setSendingMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void setSendingMessage(IMessage value)
    {
        setElement( value, "sendingMessage" );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#recurrence(int, java.lang.StringBuffer)
     */
    public ETPairT<Integer, String> getRecurrence()
    {
        return new ETPairT<Integer, String>( 
                new Integer(m_Operator), m_Recurrence );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getAutoNumber()
     */
    public String getAutoNumber()
    {
        int kind = getKind();
        if (kind != BaseElement.MK_RESULT)
        {
            if (m_AutoNumber == null || m_AutoNumber.length() == 0)
            {
                ETPairT<IMessage, Integer> p = getPreviousMessage();
                IMessage prevMess = p.getParamOne();
                int autoNumberType = p.getParamTwo().intValue();
                
                if (prevMess != null)
                {
                    String prevAutoNumber = prevMess.getAutoNumber();
                    if (prevAutoNumber != null && prevAutoNumber.length() > 0)
                    {
                        switch (autoNumberType)
                        {
                        case ANT_INCREMENT:
                            m_AutoNumber = 
                                incrementAutoNumberLastInteger(prevAutoNumber);
                            break;
                        case ANT_NEXT_LEVEL:
                            if (m_AutoNumber == null)
                                m_AutoNumber = "";
                            m_AutoNumber += prevAutoNumber + ".1";
                            break;
                        case ANT_INCREMENT_AND_NAME:
                            m_AutoNumber = 
                                incrementAutoNumberLastInteger(prevAutoNumber);
                            m_AutoNumber += "a";
                            break;
                        case ANT_NAME_INCREMENT:
                            m_AutoNumber = incrementAutoNumberLastName(prevAutoNumber);
                            break;
                        default:
                            throw new IllegalStateException();
                        }
                    }
                }
                
                if (m_AutoNumber == null || m_AutoNumber.length() == 0)
                    m_AutoNumber = "1";
            }
        }
        
        return m_AutoNumber;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#resetAutoNumber()
     */
    public void resetAutoNumber()
    {
        IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher)
                EventDispatchRetriever.instance().getDispatcher(
                    EventDispatchNameKeeper.modifiedName());
        boolean proceed = true;
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("ElementPreModified");
            proceed = disp.fireElementPreModified(this, payload);
        }
        
        if (proceed)
        {
            m_AutoNumber = m_Recurrence = null;
            
            if (disp != null)
            {
                IEventPayload payload = disp.createPayload("ElementModified");
                disp.fireElementModified(this, payload);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#getInteractionOperand()
     */
    public IInteractionOperand getInteractionOperand()
    {
        IEventOccurrence sendEvent = getSendEvent();
        
        if (sendEvent != null)
        {    
            IInteractionOperand op = 
                    OwnerRetriever.getOwnerByType(sendEvent, 
                            IInteractionOperand.class);
//                new OwnerRetriever<IInteractionOperand>( sendEvent )
//                        .getOwnerByType();
            return op;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessage#setInteractionOperand(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand)
     */
    public void setInteractionOperand(IInteractionOperand value)
    {
        IDynamicsRelationFactory factory = new DynamicsRelationFactory();
        factory.moveMessageToInteractionOperands(this, value, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Message", doc, node);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#performDependentElementCleanup(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement)
     */
    protected void performDependentElementCleanup(IVersionableElement elem)
    {
        deleteEvent(getSendEvent(), true);
        deleteEvent(getReceiveEvent(), false);

        super.performDependentElementCleanup(elem);
    }
    
    /**
     * @param line
     * @return
     */
    private IClassifier retrieveClassifier(ILifeline line)
    {
        return line.getRepresentingClassifier();
    }
    
    private ETPairT<IMessage, Integer> getPreviousMessage()
    {
        int autoNumberType = ANT_INCREMENT;
        IMessage message   = null;
        
        ILifeline sendingLifeline = getSendingLifeline();
        if (sendingLifeline != null)
        {
            ETList<IMessage> messages = 
                    new ElementCollector<IMessage>()
                        .retrieveElementCollection( 
                            this, 
                            "preceding-sibling::*[not(@kind='result')]", IMessage.class );
            
            // dom4j reverses the elements in the prior query, so we need to
            // re-reverse it.
            Collections.reverse(messages);
            if (messages != null && messages.size() > 0)
            {
                int lastMessage = messages.size() - 1;
                IMessage previousMessage = messages.get(lastMessage);
                if (previousMessage != null)
                {
                    ILifeline receivingLifeline = 
                        previousMessage.getReceivingLifeline();
                    if (sendingLifeline.isSame(receivingLifeline))
                        return new 
                            ETPairT<IMessage, Integer>( 
                                previousMessage, new Integer(ANT_NEXT_LEVEL));
                }
                
                for (int i = lastMessage; i >= 0; --i)
                {
                    IMessage precedingMessage = messages.get(i);
                    if (precedingMessage == null) continue;
                    
                    if (sendingLifeline.isSame(
                                    precedingMessage.getSendingLifeline()))
                    {
                        message = precedingMessage;
                        switch (getCombinedFragmentsInformation(
                                        precedingMessage))
                        {
                        case CFT_NONE:
                            autoNumberType = ANT_INCREMENT;
                            break;
                        case CFT_CHANGE:
                            updateRecurrence();
                            autoNumberType = ANT_INCREMENT_AND_NAME;
                            break;
                        case CFT_SAME:
                            autoNumberType = ANT_NAME_INCREMENT;
                            break;
                        }
                        
                        break;
                    }
                }
            }
        }
        
        return new ETPairT<IMessage, Integer>( 
                message, new Integer(autoNumberType) );
    }
    
    private int getCombinedFragmentsInformation(IMessage otherMessage)
    {
        int cft = CFT_NONE;
        
        IInteractionOperand thisOperand = getInteractionOperand();
        ICombinedFragment thisCF = getCombinedFragment(thisOperand);
        
        if (thisCF != null)
        {
            IInteractionOperand otherOperand = 
                    otherMessage.getInteractionOperand();
            ICombinedFragment otherCF = getCombinedFragment(otherOperand);
            
            cft = thisCF.isSame(otherCF)? CFT_SAME : CFT_CHANGE;
        }
        
        return cft;
    }
    
    private ICombinedFragment getCombinedFragment(IInteractionOperand op)
    {
        if (op == null) return null;
        
        // Search the entire DOM for the operations' procedure occurrences
        IElementLocator elementLocator = new ElementLocator();
        String query = "ancestor::UML:CombinedFragment";
        
        // Use the element locator to find the model element
        IElement element = null;
        try   // using ancestor in the XPath query can sometimes throw
        {
           element = elementLocator.findSingleElementByQuery(op, query);
        }
        catch( Exception e )
        {
        }
        return element instanceof ICombinedFragment? 
						(ICombinedFragment) element : null;
    }
    
    /**
     * Finds the last number of an auto-number and increments by 1
     */
    private String incrementAutoNumberLastInteger(String autoNumber)
    {
       String strNewNumber = "";
       long lNewIndex = 0;
       int pos = autoNumber.lastIndexOf( "." );
       if( pos != -1 )
       {
          pos++;
          strNewNumber = autoNumber.substring( 0, pos ); // includes '.'

          // unlike the C++ equivalent, the parseInt call can't handle trailing characters
          lNewIndex = Integer.parseInt( parseOutInteger(autoNumber.substring(pos)) );
       }
       else
       {
          // unlike the C++ equivalent, the parseInt call can't handle trailing characters
          lNewIndex = Integer.parseInt( parseOutInteger(autoNumber) );
       }

       strNewNumber += String.valueOf( ++lNewIndex );

       return strNewNumber;
    }

    /**
     * Makes sure you only get the leading digits from a string, prepares
     * for Integer.parseInt()
     * @param a string with leading integers
     * @return a string with only the leading integers
     */
    private String parseOutInteger(String value)
    {
       int end = 0;
       for(;end<value.length();end++)
       {
         char currentChar = value.charAt(end);
         if( !Character.isDigit(value.charAt(end)) &&
             !(end == 0 && currentChar == '-'))
            break;
       }
       return value.substring(0,end);
    }
    
    
    
    private String incrementAutoNumberLastName(String autoNumber)
    {
        String newNumber = "", oldLastNumber;
        int pos = autoNumber.lastIndexOf('.');
        if (pos != -1)
        {
            pos++;
            newNumber = autoNumber.substring(0, pos);
            oldLastNumber = autoNumber.substring(pos);
        }
        else
        {
            oldLastNumber = autoNumber;
        }
        
        // Retain the previous "integer" from the sequence expression
        int oldIndex = StringUtilities.parseInt(oldLastNumber);
        newNumber += oldIndex;
        
        // Find the old "name" from the sequence expression
        String oldName = StringUtilities.stripLeadingInteger(oldLastNumber);
        return newNumber + StringUtilities.incrementString(oldName);
    }
    
    /**
     * Determines the next value for the input string, which may be a number 
     * or letter.
     */
    private String calculateNewIndex(String oldIndex)
    {
        try
        {
            int newIndex = Integer.parseInt(oldIndex);
            return String.valueOf(newIndex + 1);
        }
        catch (NumberFormatException e)
        {
            return StringUtilities.incrementString(oldIndex);
        }
    }
    
    /**
     * Updates the recurrence value with the current interaction operand's 
     * expression.
     */
    private void updateRecurrence()
    {
        IInteractionOperand operand = getInteractionOperand();
        if (operand != null)
        {
            ICombinedFragment cf = getCombinedFragment(operand);
            if (cf != null)
                m_Operator = cf.getOperator();
            
            IInteractionConstraint cons = operand.getGuard();
            if (cons != null)
                m_Recurrence = cons.getExpression();
        }
    }
    
    /**
     * Deletes the EventOccurrence that is associated with this Message.
     *
     * @param event The occurrence to delete
     * @param start true if this event is at the start of the message, else
     *              false if at the end
     */
    private void deleteEvent(IEventOccurrence event, boolean start)
    {
        if (event != null)
        {
            IExecutionOccurrence exec = 
                    start? event.getStartExec() : event.getFinishExec();
            
            if (exec != null)
                exec.delete();
            
            // Remove the atomic fragment
            IElement owner = event.getOwner();
            if (owner instanceof IAtomicFragment)
                owner.delete();
            
            // The input event is either associated with a ILifeline, or an 
            // IInterGateConnector.
            // If associated with a lifeline we need to remove the association.
            ILifeline line = event.getLifeline();
            if (line != null)
                line.removeEvent(event);
        }
    }
    
    /**
     * Change to lifeline on the sending end of this message.
     */
    public void changeSendingLifeline(ILifeline fromLifeline,
                                       ILifeline toLifeline)
    {
        IEventOccurrence eOcc = getSendEvent();
        if (eOcc != null)
        {
            fromLifeline.removeEvent(eOcc);
            toLifeline.addEvent(eOcc);
            
            eOcc.setLifeline(toLifeline);
        }
    }
    
    /**
     * Change to lifeline on the receiving end of this message.
     */
    public void changeReceivingLifeline(ILifeline fromLifeline,
										 ILifeline toLifeline)
    {
        IEventOccurrence eOcc = getReceiveEvent();
        if (eOcc != null)
        {
            fromLifeline.removeEvent(eOcc);
            
            eOcc.setLifeline(toLifeline);
        }
    }
    
    private String m_AutoNumber;
    private String m_Recurrence;
    private int m_Operator;
}
