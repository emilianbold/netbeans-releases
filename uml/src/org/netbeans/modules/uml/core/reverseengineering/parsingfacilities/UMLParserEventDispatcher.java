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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.reverseengineering.reframework.*;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 */
public class UMLParserEventDispatcher extends EventDispatcher
    implements IUMLParserEventDispatcher
{
    /**
     * IEventDispatcher override.  Returns the number of registered sinks
     */
    public int getNumRegisteredSinks()
    {
        return m_ParserSink.getNumListeners();
    }

    public void revokeUMLParserSink(IUMLParserEventsSink sink)
    {
        m_ParserSink.removeListener(sink);
    }

    public void registerForUMLParserEvents(IUMLParserEventsSink handler, String filename)
    {
        m_ParserSink.addListener(handler, null);
    }

    /**
     * Used to fire the OnPackageFound event to all registered listeners
     *
     * @param filename [in] The file that is parsed.
     * @param data [in] The event information to be sent.
     * @param payload [in] Extra event information.
     */
    public void firePackageFound(String filename, IPackageEvent data, 
                                 IEventPayload payload)
    {
        m_ParserSink.notifyListeners(
                getEventFunctor("PackageFound", IUMLParserEventsSink.class, 
                null, data, payload) );
    }

    /**
     * Used to fire the OnDependencyFound event to all registered listeners.
     *
     * @param filename [in] The file that is parsed.
     * @param data [in] The event information to be sent.
     * @param payload [in] Extra event information.
     */
    public void fireDependencyFound(String filename, IDependencyEvent data, 
                                    IEventPayload payload)
    {
        m_ParserSink.notifyListeners(
                getEventFunctor("DependencyFound", IUMLParserEventsSink.class, 
                                null, data, payload) );
    }

    /**
     * Used to fire the OnClassFound event to all registered listeners.
     *
     * @param filename [in] The file that is parsed.
     * @param data [in] The event information to be sent.
     * @param payload [in] Extra event information.
     */
    public void fireClassFound(String filename, IClassEvent data, IEventPayload payload)
    {
        m_ParserSink.notifyListeners(
                getEventFunctor("ClassFound", IUMLParserEventsSink.class, 
                                null, data, payload) );
    }

    /**
     * Used to fire the OnBeginParseFile event to all registered listeners.
     *
     * @param filename [in] The file that is parsed.
     * @param payload [in] Extra event information.
     */
    public void fireBeginParse(String filename, IEventPayload payload)
    {
        m_ParserSink.notifyListeners(
                getEventFunctor("BeginFileParse", IUMLParserEventsSink.class, 
                "onBeginParseFile", filename, payload) );
    }

    /**
     * Used to fire the OnEndParseFile event to all registered listeners.
     *
     * @param filename [in] The file that is parsed.
     * @param payload [in] Extra event information.
     */
    public void fireEndParse(String filename, IEventPayload payload)
    {
        m_ParserSink.notifyListeners(
                getEventFunctor("EndFileParse", IUMLParserEventsSink.class, 
                "onEndParseFile", filename, payload) );
    }

    /**
     * Used to fire the OnError event to all registered listeners.
     *
     * @param filename [in] The file that is parsed.
     * @param data [in] The error information.
     * @param payload [in] Extra event information.
     */
    public void fireError(String filename, IErrorEvent data, IEventPayload payload)
    {
        m_ParserSink.notifyListeners(
                getEventFunctor("ErrorFound", IUMLParserEventsSink.class, 
                                "onError", data, payload) );
    }

    /**
     * Removes a operation detail event sink from the dispatcher.
     *
     * @param cookie [in] The sink to remove.
     */
    public void revokeOperationDetailsSink(IUMLParserOperationEventSink sink)
    {
        m_OpParserSink.removeListener(sink);
    }

    /**
     * Registers the operation detail event sink.
     *
     * @param handler [in] The sink to register.
     * @param cookie [in] The cookie that represents the sink.
     */
    public void registerForOperationDetailsEvent(IUMLParserOperationEventSink handler)
    {
        if (handler != null)
            m_OpParserSink.addListener(handler, null);
    }

    /**
     * Used to fire the OnCreateAction event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireCreateAction(ICreationEvent event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("CreateAction", 
                    IUMLParserOperationEventSink.class, 
                    null, event, payload) );
    }

    /**
     * Used to fire the OnReferencedVariable event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireReferencedVariable(IReferenceEvent event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("ReferencedVariable", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnMethodCall event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireMethodCall(IMethodEvent e, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("MethodCall", 
                IUMLParserOperationEventSink.class, 
                    null, e, payload) );
    }

    /**
     * Used to fire the OnEndReturnAction event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireReturnAction(IReturnEvent event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("ReturnAction", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnDestroyAction event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireDestroyAction(IDestroyEvent event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("DestroyAction", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Notifies the listeners that the parser is about to start the processing of a loop.
     *
     * @param payload [in] The events payload.
     */
    public void fireBeginLoop(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginLoop", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Notifies the listeners that the parser is about to start the processing of a Loop.
     *
     * @param payload [in] The events payload.
     */
    public void fireEndLoop(IRELoop event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndLoop", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Notifies the listeners that the parser is about to stop the processing of a Conditional.
     *
     * @param payload [in] The events payload.
     */
    public void fireBeginConditional(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginConditional", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Notifies the listeners that the parser is about to start the processing of a Conditional.
     *
     * @param payload [in] The events payload.
     */
    public void fireEndConditional(IREConditional event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndConditional", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Notifies the listeners that the parser is about to stop the processing of a Conditional.
     *
     * @param payload [in] The events payload.
     */
    public void fireBeginCriticalSection(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginCriticalSection", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Notifies the listeners that the parser is about to start the processing of a Conditional.
     *
     * @param payload [in] The events payload.
     */
    public void fireEndCriticalSection(IRECriticalSection event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndCriticalSection", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Notifies the listeners that the parser is about to stop the processing of a Clause.
     *
     * @param payload [in] The events payload.
     */
    public void fireBeginClause(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginClause", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Notifies the listeners that the parser is about to start the processing of a Clause.
     *
     * @param payload [in] The events payload.
     */
    public void fireEndClause(IREClause event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndClause", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnBeginInitialize event to all operation detail listeners.
     *
     * @param payload [in] The events payload.
     */
    public void fireBeginInitialize(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginInitialize", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Used to fire the OnEndInitialize  event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireEndInitialize(IInitializeEvent event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndInitialize", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnBeginTest event to all operation detail listeners.
     *
     * @param payload [in] The events payload.
     */
    public void fireBeginTest(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginTest", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Used to fire the OnEndTest  event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireEndTest(ITestEvent event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndTest", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnBeginTest event to all operation detail listeners.
     *
     * @param payload [in] The events payload.
     */
    public void fireBeginPostProcessing(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginPostProcessing", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Used to fire the OnEndTest  event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireEndPostProcessing(IPostProcessingEvent event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndPostProcessing", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnJumpEvent event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireJumpEvent(IJumpEvent event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("JumpEvent", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }
    
    /**
     * Used to fire the OnJumpEvent event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireLoop(IRELoop event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("RELoop", 
                IUMLParserOperationEventSink.class, 
                "onLoop", event, payload) );
    }
    
    /**
     * Notifies the listeners that loop statement was found in the source code.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireConditional(IREConditional event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("REConditional", 
                IUMLParserOperationEventSink.class, 
                "onConditional", event, payload) );
    }

    /**
     * Notifies the listeners that a critical section was found in the source code.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireCriticalSection(IRECriticalSection event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("RECriticalSection", 
                IUMLParserOperationEventSink.class, 
                "onCriticalSection", event, payload) );
    }

    /**
     * Notifies the listeners that a binary operator statement was found in the source code.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireBinaryOperator(IREBinaryOperator event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("REBinaryOperator", 
                IUMLParserOperationEventSink.class, 
                "onBinaryOperator", event, payload) );
    }

    /**
     * Notifies the listeners that a unary operator statement was found in the source code.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireUnaryOperator(IREUnaryOperator event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("REUnaryOperator", 
                IUMLParserOperationEventSink.class, 
                "onUnaryOperator", event, payload) );
    }

    /**
     * Used to fire the FireBeginRaisedException event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireBeginRaisedException(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginRaisedException", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Used to fire the OnRaisedException event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireEndRaisedException(IRaisedException event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndRaisedException", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnBeginExceptionProcessing event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireBeginExceptionProcessing(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginExceptionProcessing", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Used to fire the OnEndExceptionProcessing event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireEndExceptionProcessing(IREExceptionProcessingEvent event, 
                                           IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndExceptionProcessing", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnBeginExceptionJumpHandler event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireBeginExceptionJumpHandler(IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("BeginExceptionJumpHandler", 
                IUMLParserOperationEventSink.class, 
                null, payload) );
    }

    /**
     * Used to fire the OnEndExceptionJumpHandler event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireEndExceptionJumpHandler(IREExceptionJumpHandlerEvent event, IEventPayload payload)
    {
        m_OpParserSink.notifyListeners(
                getEventFunctor("EndExceptionJumpHandler", 
                IUMLParserOperationEventSink.class, 
                null, event, payload) );
    }

    /**
     * Registers the atomic events event sink.
     *
     * @param handler [in] The sink to register.
     * @param cookie [in] The cookie that represents the sink.
     */
    public void registerForUMLParserAtomicEvents(IUMLParserAtomicEventsSink handler, String filename)
    {
        if (handler != null)
            m_AtomicParserSink.addListener( handler, null );
    }

    /**
     * Removes a atomic events event sink from the dispatcher.
     *
     * @param cookie [in] The sink to remove.
     */
    public void revokeUMLParserAtomicSink(IUMLParserAtomicEventsSink sink)
    {
        m_AtomicParserSink.removeListener( sink );
    }

    /**
     * Used to fire the OnOperationFound event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireOperationFound(IOperationEvent event, IEventPayload payload)
    {
        m_AtomicParserSink.notifyListeners(
                getEventFunctor("OperationFound", 
                IUMLParserAtomicEventsSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnAttributeFound event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireAttributeFound(IAttributeEvent event, IEventPayload payload)
    {
        m_AtomicParserSink.notifyListeners(
                getEventFunctor("AttributeFound", 
                IUMLParserAtomicEventsSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnGeneralizationFound event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireGeneralizationFound(IREGeneralization event, IEventPayload payload)
    {
        m_AtomicParserSink.notifyListeners(
                getEventFunctor("GeneralizationFound", 
                IUMLParserAtomicEventsSink.class, 
                null, event, payload) );
    }

    /**
     * Used to fire the OnImplementationFound event to all operation detail listeners.
     *
     * @param event [in] The event to send to the listeners.
     * @param payload [in] The events payload.
     */
    public void fireImplementationFound(IRERealization event, IEventPayload payload)
    {
        m_AtomicParserSink.notifyListeners(
                getEventFunctor("ImplementationFound", 
                IUMLParserAtomicEventsSink.class, 
                null, event, payload) );
    }

    
    protected EventFunctor getEventFunctor(String eventName, Class sinkClass, 
                                           String eventMethod, Object data, 
                                           IEventPayload payload)
    {
        if (validateEvent(eventName, data))
        {
            IResultCell cell = prepareResultCell(payload);
            EventFunctor func = findFunctor(eventName, sinkClass, eventMethod);
            func.setParameters(new Object[] { data, cell });
            return func;
        }
        return null;
    }
    
    protected EventFunctor getEventFunctor(String eventName, Class sinkClass, 
                                           String eventMethod,
                                           IEventPayload payload)
    {
        if (validateEvent(eventName, null))
        {
            IResultCell cell = prepareResultCell(payload);
            EventFunctor func = findFunctor(eventName, sinkClass, eventMethod);
            func.setParameters(new Object[] { cell });
            return func;
        }
        return null;
    }
    
    protected EventFunctor findFunctor(String eventName, Class sink, 
                                       String method)
    {
       //method = "on" + eventName;
       if(method == null)
       {
          method = "on" + eventName;
       }
       return new EventFunctor(sink,method);
    }
    
    private EventManager< IUMLParserEventsSink > m_ParserSink = 
                    new EventManager< IUMLParserEventsSink >();
    private EventManager< IUMLParserOperationEventSink > m_OpParserSink =
                    new EventManager< IUMLParserOperationEventSink >();
    
    /// Handles OperationFound and AttributeFound events
    private EventManager< IUMLParserAtomicEventsSink > m_AtomicParserSink =
                    new EventManager< IUMLParserAtomicEventsSink >();
}