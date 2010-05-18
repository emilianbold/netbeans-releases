/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.support.ActivityDescriptor;

/**
 * This is factory class for entities creation. Entity that was created via this
 * builder will not have UID. Method getUID will return null for such entity.
 *
 * @author ads
 */
public interface BPELElementsBuilder {

    /**
     * Creates activity by its <code>descriptor</code>.
     *
     * @param descriptor
     *            description of activity.
     * @return instantiated activity
     */
    ExtendableActivity createActivity( ActivityDescriptor descriptor );

    /**
     * @return instantiated process.
     */
    Process createProcess();

    /**
     * @return instantiated empty.
     */
    Empty createEmpty();

    /**
     * @return instantiated invoke.
     */
    Invoke createInvoke();

    /**
     * @return instantiated receive.
     */
    Receive createReceive();

    /**
     * @return instantiated reply.
     */
    Reply createReply();

    /**
     * @return instantiated assign.
     */
    Assign createAssign();

    /**
     * @return instantiated JavaScript.
     */
    Assign createJavaScript();

    /**
     * @return instantiated wait.
     */
    Wait createWait();

    /**
     * @return instantiated throw.
     */
    Throw createThrow();

    /**
     * @return instantiated terminate.
     */
    Exit createExit();

    /**
     * @return instantiated flow.
     */
    Flow createFlow();

    /**
     * @return instantiated while.
     */
    While createWhile();

    /**
     * @return instantiated sequence.
     */
    Sequence createSequence();

    /**
     * @return instantiated pick.
     */
    Pick createPick();

    /**
     * @return instantiated scope.
     */
    Scope createScope();
    
    /**
     * @return instantiated  forEach.
     */
    ForEach createForEach();
    
    /**
     * @return instantiated if.
     */
    If createIf();
    
    /**
     * @return instantiated repeatUntil.
     */
    RepeatUntil createRepeatUntil();
    
    /**
     * @return instantiated rethrow.
     */
    ReThrow createRethrow();
    
    /**
     * @return instantiated  validate.
     */
    Validate createValidate();

    /**
     * @return instantiated partnerLinks.
     */
    PartnerLinkContainer createPartnerLinkContainer();

    /**
     * @return instantiated partnerLink.
     */
    PartnerLink createPartnerLink();

    /**
     * @return instantiated faultHandlers.
     */
    FaultHandlers createFaultHandlers();

    /**
     * @return instantiated catch.
     */
    Catch createCatch();

    /**
     * @return instantiated eventHandlers.
     */
    EventHandlers createEventHandlers();

    /**
     * @return instantiated onMessage.
     */
    OnMessage createOnMessage();

    /**
     * @return instantiated compensationHandler.
     */
    CompensationHandler createCompenstaionHandler();

    /**
     * @return instantiated variables.
     */
    VariableContainer createVariableContainer();

    /**
     * @return instantiated variable.
     */
    Variable createVariable();

    /**
     * @return instantiated correlationSets.
     */
    CorrelationSetContainer createCorrelationSetContainer();

    /**
     * @return instantiated correlationSet
     */
    CorrelationSet createCorrelationSet();

    /**
     * @return instantiated source.
     */
    Source createSource();

    /**
     * @return instantiated target.
     */
    Target createTarget();

    /**
     * @return instantiated correlations.
     */
    CorrelationContainer createCorrelationContainer();

    /**
     * @return instantiated correlation.
     */
    Correlation createCorrelation();

    /**
     * @return instantiated correlationWithPattern.
     */
    PatternedCorrelation createPatternedCorrelation();

    /**
     * @return instantiated correlationsWithPattern.
     */
    PatternedCorrelationContainer createPatternedCorrelationContainer();

    /**
     * @return instantiated to.
     */
    To createTo();

    /**
     * @return instantiated from.
     */
    From createFrom();

    /**
     * @return instantiated compensate.
     */
    Compensate createCompensate();


    /**
     * @return instantiated compensateScope.
     */
    CompensateScope createCompensateScope();

    /**
     * @return instantiated links.
     */
    LinkContainer createLinkContainer();

    /**
     * @return instantiated link.
     */
    Link createLink();

    /**
     * @return instantiated copy.
     */
    Copy createCopy();

    /**
     * @return instantiated ExtensionAssignOperation.
     */
    ExtensionAssignOperation createExtensionAssignOperation();

    /**
     * @return instantiated catchAll ( activityOrCompensateContainer ).
     */
    CatchAll createCatchAll();
    
    /**
     * @return instantiated boolean expression.
     */
    BooleanExpr createCondition();
    
    /**
     * @return instantiated branches.
     */
    Branches createBranches();
    
    /**
     * @return instantiated completionCondition.
     */
    CompletionCondition createCompletionCondition();
    
    /**
     * @return instantiated transitionCondition.
     */
    Condition createTransitionCondition();
    
    /**
     * @return instantiated joinCondition.
     */
    Condition createJoinCondition();
    
    /**
     * @return instantiated Deadline Expression.
     */
    DeadlineExpression createUntil();
    
    /**
     * @return instantiated documentation.
     */
    Documentation createDocumentation();
    
    /**
     * @return instantiated  else.
     */
    Else createElse();
    
    /**
     * @return instantiated elseif.
     */
    ElseIf createElseIf();
    
    /**
     * @return instantiated toPart.
     */
    ToPart createToPart();
    
    /**
     * @return instantiated toParts.
     */
    ToPartContainer createToPartContainer();
    
    /**
     * @return instantiated terminationHandler.
     */
    TerminationHandler createTerminationHandler();
    
    /**
     * @return instantiated targets.
     */
    TargetContainer createTargetContainer();
    
    /**
     * @return instantiated startCounterValue.
     */
    StartCounterValue createStartCounterValue();
    
    /**
     * @return instantiated sources.
     */
    SourceContainer createSourceContainer();
    
    /**
     * @return instantiated repeatEvery.
     */
    RepeatEvery createRepeatEvery();
    
    /**
     * @return instantiated onEvent.
     */
    OnEvent createOnEvent();
    
    /**
     * @return instantiated onAlarm for Pick OnAlarmPick element.
     */
    OnAlarmPick createOnAlarmPick();
    
    /**
     * @return instantiated onAlarm for EventHandlers OnAlarmEvent element.
     */
    OnAlarmEvent createOnAlarmEvent();
    
    /**
     * @return instantiated extensions.
     */
    ExtensionContainer createExtensionContainer();
    
    /**
     * @return instantiated extension.
     */
    Extension createExtension();
    
    
    /**
     * @return instantiated finalCounterValue.
     */
    FinalCounterValue createFinalCounterValue();
    
    /**
     * @return instantiated literal.
     */
    Literal createLiteral();
    
    /**
     * @return instantiated import.
     */
    Import createImport();
    
    /**
     * @return instantiated fromPart.
     */
    FromPart createFromPart();
    
    /**
     * @return instantiated fromParts.
     */
    FromPartContainer createFromPartContainer();
    
    
    /**
     * @return instantiated for.
     */
    For createFor();
    
    /**
     * @return instantiated messageExchages element.
     */
    MessageExchangeContainer createMessageExchangeContainer();
    
    /**
     * @return instantiated messageExchage element.
     */
    MessageExchange createMessageExchange();
    
    /**
     * @return instantiated "service-ref" element.
     */
    ServiceRef createServiceRef();
    
    /**
     * @return instantiated query element.
     */
    Query createQuery();
    
    /**
     * Creates extension element.
     * @param <T> Class for element type.
     * @param clazz Type for extension element.
     * @return instantiated extension element with specified type.
     */
    <T extends ExtensionEntity> T createExtensionEntity( Class<T> clazz ); 

    /**
     * Add change listener to builder. Those listeners will be notified with
     * only events that concern unattached elements.
     * 
     * @param listener
     *            listener for add.
     */
    void addEntityChangeListener( ChangeEventListener listener );

    /**
     * Removes change listener from builder.
     * 
     * @param listener
     *            listener for remove.
     */
    void removeEntityChangeListener( ChangeEventListener listener );
    
}
