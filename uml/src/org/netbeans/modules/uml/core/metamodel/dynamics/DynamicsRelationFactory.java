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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.behavior.ICallAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class DynamicsRelationFactory implements IDynamicsRelationFactory
{
    public IActionOccurrence createActionOccurrence(IInteractionFragment owner,
                                                    ILifeline lifeline,
                                                    String actionType)
    {
        IInteraction interaction = lifeline.getInteraction();
        IInteractionOperand interOper = 
           (IInteractionOperand) (owner instanceof IInteractionOperand?
                                                   owner : interaction);
        IAtomicFragment frag     = createAtomicFragment();
        IGate           fragGate = establishAtomicFragmentGate(frag);
        
        IActionOccurrence accOcc = null;
        if (frag != null && fragGate != null && interaction != null)
        {
            interOper.addFragment(frag);
            accOcc = establishAction(interaction, actionType, 
                    frag, null, true);
            if (accOcc != null)
                establishEventOnLifeline(lifeline, accOcc, true);
        }
        return accOcc;
    }
    
    public IMessage createMessage(IElement fromElement, 
                                  IInteractionFragment fromOwner,
                                  IElement toElement,
                                  IInteractionFragment toOwner,
                                  IOperation oper,
                                  int messageKind)
    {
        return insertMessage(null, fromElement, fromOwner, toElement, toOwner,
                oper, messageKind);
    }
    
    /**
     * Creates a new message between the 'from' and 'to' lifelines.
     * 
     * @param fromBeforeMessage
     * @param fromElement
     * @param fromOwner
     * @param toElement
     * @param toOwner
     * @param oper
     * @param kind
     */
    public IMessage insertMessage(IMessage fromBeforeMessage, 
                                   IElement fromElement, 
								   IInteractionFragment fromOwner, 
								   IElement toElement, 
                                   IInteractionFragment toOwner,
								   IOperation oper, 
                                   int kind)
    {
		if (fromElement == null || toElement == null)
			return null;

        String actionType = "UninterpretedAction";
        if (oper != null)
        {
            switch (kind)
            {
            case BaseElement.MK_CREATE:
                actionType = "CreateAction";
                break;
            case BaseElement.MK_SYNCHRONOUS:
            case BaseElement.MK_ASYNCHRONOUS:
                actionType = "CallAction";
                break;
            case BaseElement.MK_RESULT:
                actionType = "ReturnAction";
                break;
            }
        }
        
        RelationshipEventsHelper helper = new RelationshipEventsHelper("Message");
        
        IMessage message = null;
        if (helper.firePreRelationCreated(fromElement, toElement))
        {
            message = establishMessage(fromBeforeMessage, fromElement, 
                    fromOwner, toElement, toOwner, oper, actionType);
            if (message != null)
            {
                message.setKind(kind);
                IInteraction interaction = message.getInteraction();
                if (interaction != null)
                    interaction.resetAutoNumbers(message);
                
                helper.fireRelationCreated(message);
            }
        }
        return message;
    }
    
    public void moveMessageToInteractionOperands(IMessage message,
                                                 IInteractionOperand fromOwner,
                                                 IInteractionOperand toOwner)
    {
        // Move the sending atomic fragment to the "from" interaction operand
        IEventOccurrence sendEvent =  message.getSendEvent();
        if (sendEvent != null)
        {
			IAtomicFragment frag = OwnerRetriever.getOwnerByType(sendEvent, IAtomicFragment.class);
            if (frag != null)
                fromOwner.addFragment(frag);
        }
        
        // Move the receiving atomic fragment to the "to" interaction operand
        IEventOccurrence receiveEvent = message.getReceiveEvent();
        if (receiveEvent != null)
        {
			IAtomicFragment frag = OwnerRetriever.getOwnerByType(receiveEvent, IAtomicFragment.class);
            if (frag != null)
                toOwner.addFragment(frag);
            
        }
    }
    
    private IMessage establishMessage(IMessage fromBeforeMessage, 
                                      IElement fromElement, 
                                      IInteractionFragment fromOwner, 
                                      IElement toElement, 
                                      IInteractionFragment toOwner, 
                                      IOperation oper, 
                                      String actionType)
    {
		IMessage retMessage = null;
        
    	boolean origBlock = EventBlocker.startBlocking();
    	try
    	{
			ETPairT<IInteraction, Boolean> pair = determineInteraction(fromElement);
			boolean fromIsLifeline = pair.getParamTwo().booleanValue();
			IInteraction fromInteraction = pair.getParamOne();
        
			IInteractionOperand fromOperand = 
				(fromOwner instanceof IInteractionOperand)? 
						(IInteractionOperand) fromOwner : fromInteraction;
        
			pair = determineInteraction(toElement);
			boolean toIsLifeline = pair.getParamTwo().booleanValue();
			IInteraction toInteraction = pair.getParamOne();

			IInteractionOperand toOperand = 
				(toOwner instanceof IInteractionOperand)? 
						(IInteractionOperand) toOwner : toInteraction;
        
			if (fromInteraction == null || toInteraction == null)
				throw new IllegalArgumentException();
        
			// Create two atomic fragments, and their gates.
			// The first is the fragment that contains the start of the message
			// that represents the call of the operation.
			// The first fragment will be owned by the "from" interaction fragment.
			// The second is the receiving fragment on the end of the message.
			// The second fragment will be owned by the "to" interaction fragment.
      
			// Between the two AtomicFragments is an InterGateConnector.
			// This will be owned by the the "to" interaction fragment.
			IAtomicFragment fromFragment = createAtomicFragment(),
							toFragment   = createAtomicFragment();
        
			if (fromFragment != null && toFragment != null)
			{
				IGate fromGate = establishAtomicFragmentGate(fromFragment);
				IGate toGate   = establishAtomicFragmentGate(toFragment);
				IInterGateConnector gateConnector = 
						establishGateConnector(fromGate, toGate);
            
				if (fromInteraction != null && toInteraction != null)
				{
					fromOperand.addFragment(fromFragment);
					toOperand.addFragment(toFragment);
					toInteraction.addGateConnector(gateConnector);
                
					// Create the from action occurrence, and attach to the "from" 
					// lifeline, if necessary
					IActionOccurrence fromAccOcc = 
						establishAction(fromInteraction, actionType, fromFragment, 
							oper, true);
					if (fromIsLifeline)
						establishEventOnLifeline((ILifeline) fromElement, 
								fromAccOcc, true);
                
					// Create the "to" execution occurrence
					// When there is an operation, the execution occurrence should 
					// be a procedure occurrence, otherwise the execution occurrence 
					// should be an action occurrence
					IExecutionOccurrence toExeOcc = null;
					if (oper != null)
					{
						toExeOcc = 
							establishProcOccurrence(
								toInteraction, toFragment, oper);
					}
					else
					{
						toExeOcc = establishAction(toInteraction, actionType, 
								toFragment, oper, false);
					}
                
					if (toIsLifeline && toExeOcc != null)
						establishEventOnLifeline( 
								(ILifeline) toElement, toExeOcc, false);
                
					// Fix W3881:  Need to make sure the message gets stored in the 
					// proper interaction
					IInteraction messageInteraction = toInteraction;
					if (!toInteraction.isSame(fromInteraction) && fromIsLifeline)
						messageInteraction = fromInteraction;
                
					// Create the actual message element
					retMessage = establishMessage(fromBeforeMessage, 
							messageInteraction, fromAccOcc, toExeOcc);
                
					if (retMessage != null)
					{
						fromInteraction.handleMessageAdded(retMessage, 
								fromBeforeMessage);
                    
						if (fromIsLifeline && toIsLifeline)
						{
							IMessageConnector messageConnector =
								createMessageConnector((ILifeline) fromElement,
									(ILifeline) toElement);
                        
							if (messageConnector != null)
								messageConnector.addMessage(retMessage);
						}
					}
				}
			}
    	}
    	finally
    	{
    		EventBlocker.stopBlocking(origBlock);
    	}
        return retMessage;
    }
    
    private IMessage establishMessage(IMessage fromBeforeMessage, 
                                      IInteraction inter, 
                                      IExecutionOccurrence accOcc, 
                                      IExecutionOccurrence procOcc)
    {
        IMessage message = 
                new TypedFactoryRetriever<IMessage>().createType("Message");
        IEventOccurrence sendEvent = accOcc.getStart();
        IEventOccurrence receiveEvent = procOcc.getFinish();
        
        if (sendEvent != null && receiveEvent != null)
        {
            message.setSendEvent(sendEvent);
            message.setReceiveEvent(receiveEvent);
            message.setInitiatingAction(accOcc);
            inter.insertMessageBefore(message, fromBeforeMessage);
        }
        return message;
    }
    
    private IInterGateConnector establishGateConnector(IGate from, IGate to)
    {
        IInterGateConnector connector = 
                new TypedFactoryRetriever<IInterGateConnector>().createType(
                    "InterGateConnector");
        connector.setFromGate(from);
        connector.setToGate(to);
        return connector;
    }
    
    private ETPairT<IInteraction, Boolean> determineInteraction(
            IElement element)
    {
        IInteraction fromInteraction = null;
        boolean fromLifeline = false;
        if (!(element instanceof IInteraction))
        {
            if (element instanceof ILifeline)
            {
                fromInteraction = ((ILifeline) element).getInteraction();
                fromLifeline    = true;
            }
            else if (element instanceof IInteractionOccurrence)
            {
                fromInteraction = ((IInteractionOccurrence) element)
                                        .getInteraction();
            }
            else
            {
                IElement owner = element.getOwner();
                if (owner instanceof IInteraction)
                    fromInteraction = (IInteraction) owner;
            }
        }
        else
        {
            fromInteraction = (IInteraction) element;
        }
        return new ETPairT<IInteraction, Boolean>( 
                fromInteraction,
                new Boolean(fromLifeline) );
    }

    private void establishEventOnLifeline(ILifeline lifeline, 
                                          IExecutionOccurrence exec,
										  boolean start)
    {
        IEventOccurrence event = start? exec.getStart() : exec.getFinish();
        if (event != null)
            event.setLifeline(lifeline);
    }
    
    /**
     * Creates all the necessary elements associated with the starting fragment 
     * in a send message event. This includes the EventOccurrence, the 
     * ActionOccurrence, and the CallAction on the ActionOccurrence.
     *
     * @param owner       
     * @param actionType  The name of the action to create, such as "CallAction" 
     *                    or "ReturnAction"
     * @param frag        The AtomicFragment representing the start of a message
     * @param oper        The Operation that is being executed by the Message.
     * @param startEvent  true if the EventOccurrence is established on the 
     *                    start or finish end of the ExecutionOccurrence.
     * @return The IActionOccurrence created.
     */
    private IActionOccurrence establishAction(IInteraction owner, 
                                              String actionType, 
                                              IAtomicFragment frag, 
                                              IOperation oper, 
                                              boolean startEvent)
    {
        ETPairT<IEventOccurrence, IActionOccurrence> pair = 
                establishAction(owner, actionType, oper);
        IActionOccurrence accOcc = pair.getParamTwo();
        IEventOccurrence eventOcc = pair.getParamOne();
        if (accOcc != null && eventOcc != null)
        {
            if (startEvent)
                eventOcc.setStartExec(accOcc);
            else
                eventOcc.setFinishExec(accOcc);
            frag.setEvent(eventOcc);
        }
        return accOcc;
    }
    
    private ETPairT<IEventOccurrence, IActionOccurrence>
            establishAction(IInteraction owner, String actionType, 
                            IOperation oper)
    {
        IAction action = new TypedFactoryRetriever<IAction>().createType(
                actionType);
        if (owner instanceof INamespace)
        {
            INamespace space = (INamespace) owner;
            space.addElement(action);
            
            if (action instanceof ICallAction)
                ((ICallAction) action).setOperation(oper);
            
            IActionOccurrence accOcc = 
                    new TypedFactoryRetriever<IActionOccurrence>()
                        .createType("ActionOccurrence");
            
            space.addOwnedElement(accOcc);
            accOcc.setAction(action);
            
            IEventOccurrence eventOcc =
                new TypedFactoryRetriever<IEventOccurrence>()
                    .createType("EventOccurrence");
            space.addOwnedElement(eventOcc);
            return new ETPairT<IEventOccurrence, IActionOccurrence>(eventOcc, accOcc);
        }
        return new ETPairT<IEventOccurrence, IActionOccurrence>(null, null);
    }

    /**
     * Creates and returns an atomic fragment.
     * @return <code>An IAtomicFragment</code>
     */
    private IAtomicFragment createAtomicFragment()
    {
        return new TypedFactoryRetriever<IAtomicFragment>()
            .createType("AtomicFragment");
    }
    
    /**
     * Creates and places an implicit gate on the given IAtomicFragment.
     * @param fragment An IAtomicFragment
     * @return The <code>IGate</code> created.
     */
    private IGate establishAtomicFragmentGate(IAtomicFragment fragment)
    {
        IGate g = new TypedFactoryRetriever<IGate>().createType("Gate");
        fragment.setImplicitGate(g);
        return g;
    }
    

    private IProcedureOccurrence establishProcOccurrence(IInteraction owner, 
                                                         IAtomicFragment frag, 
                                                         IOperation oper)
    {
        ETPairT<IEventOccurrence, IProcedureOccurrence> occs =
            establishProcOccurrence(owner, oper);
        
        IEventOccurrence eventOcc = occs.getParamOne();
        IProcedureOccurrence procOcc = occs.getParamTwo();
        
        if (procOcc != null)
            frag.setEvent(eventOcc);
        return procOcc;
    }
    
    private ETPairT<IEventOccurrence, IProcedureOccurrence>
        establishProcOccurrence(IInteraction owner, IOperation oper)
    {
        IEventOccurrence eventOcc = 
                new TypedFactoryRetriever<IEventOccurrence>()
                    .createType("EventOccurrence");
        owner.addEventOccurrence(eventOcc);
        IProcedureOccurrence procOcc =
                new TypedFactoryRetriever<IProcedureOccurrence>()
                    .createType("ProcedureOccurrence");
        procOcc.setOperation(oper);
        eventOcc.setFinishExec(procOcc);
        
        if (owner instanceof INamespace)
            ((INamespace) owner).addOwnedElement(procOcc);
        
        return new ETPairT<IEventOccurrence, IProcedureOccurrence>
            ( eventOcc, procOcc );
    }
    
    private IMessageConnector getMessageConnector(IInteraction interaction,
                                                  ILifeline from,
                                                  ILifeline to)
    {
        ETList<IConnector> connectors = interaction.getConnectors();
        if (connectors != null)
        {
            for (int i = connectors.size() - 1; i >= 0; --i)
            {
                Object connector = connectors.get(i);
                if (connector instanceof IMessageConnector)
                {
                    if (isMessageConnectorBetweenLifelines(
                                    (IMessageConnector) connector, from, to))
                        return (IMessageConnector) connector;
                }
            }
        }
        return null;
    }
    
    private boolean isMessageConnectorBetweenLifelines(
            IMessageConnector connector,
            ILifeline from,
            ILifeline to)
    {
        ILifeline fromLifeline = connector.getFromLifeline();
        ILifeline toLifeline   = connector.getToLifeline();
        if (fromLifeline != null && toLifeline != null)
            return (fromLifeline.isSame(from) && toLifeline.isSame(to)) ||
                (fromLifeline.isSame(to) && toLifeline.isSame(from));
        return false;
    }
    
    public IMessageConnector createMessageConnector(ILifeline from, 
                                                     ILifeline to)
    {
        IMessageConnector connector = null;
        
        ITypedElement fromRep, toRep;
        
        fromRep = from.getRepresents();
        toRep   = to.getRepresents();
        
        if (fromRep != null && toRep != null)
        {
            IInteraction interaction = from.getInteraction();
            if (interaction != null)
            {
                if ((connector = getMessageConnector(interaction, from, to)) 
                        == null)
                {
                    connector =
                        new TypedFactoryRetriever<IMessageConnector>()
                            .createType("MessageConnector");
                    connector.setFromLifeline(from);
                    connector.setToLifeline(to);
                    interaction.addConnector(connector);
                }
            }
        }
        
        return connector;
    }
}
