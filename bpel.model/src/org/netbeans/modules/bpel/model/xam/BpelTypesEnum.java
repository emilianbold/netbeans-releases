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
package org.netbeans.modules.bpel.model.xam;

import org.netbeans.modules.bpel.model.api.AssignChild;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.Condition;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.Documentation;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.ExtensionContainer;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.FromChild;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.LinkContainer;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.SourceContainer;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.Target;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.ToPartContainer;
import org.netbeans.modules.bpel.model.api.VariableContainer;


/**
 * @author ads
 *
 */
public enum BpelTypesEnum implements BpelTypes {
    PARTNERLINK_CONTAINER( PartnerLinkContainer.class ),
    ACTIVITIES_GROUP( ExtendableActivity.class ),
    VARIABLE_CONTAINER( VariableContainer.class ),
    CORRELATION_SET_CONTAINER( CorrelationSetContainer.class ),
    FAULT_HANDLERS( FaultHandlers.class ),
    COMPENSATION_HANDLER(CompensationHandler.class),
    EVENT_HANDLERS( EventHandlers.class ),
    TERMINATION_HANDLER( TerminationHandler.class ),
    CATCH_ALL( CatchAll.class ),
    CATCH( Catch.class ),
    ON_ALARM_EVENT( OnAlarmEvent.class ),
    EXTENSION_CONTAINER( ExtensionContainer.class ),
    IMPORT( Import.class ),
    SOURCE_CONTAINER( SourceContainer.class ),
    TO( To.class ),
    LINK_CONTAINER( LinkContainer.class ),
    ON_ALARM_PICK( OnAlarmPick.class ),
    DOCUMENTATION( Documentation.class ),
    REPEAT_EVERY( RepeatEvery.class ),
    SCOPE( Scope.class ),
    TARGET( Target.class ),
    TO_PARTS( ToPartContainer.class ),
    FROM_PARTS( FromPartContainer.class ),
    ELSE_IF( ElseIf.class ),
    ELSE( Else.class ),
    CONDITION( Condition.class ),
    CORRELATION_CONTAINER( CorrelationContainer.class ),
    COMPLETION_CONDITION( CompletionCondition.class ),
    FINAL_COUNTER_VALUE( FinalCounterValue.class ),
    MESSAGE_EXCHANGE_CONTAINER( MessageExchangeContainer.class ),
    FOR_OR_UNTIL_GROUP( TimeEvent.class ),
    BOOLEAN_EXPR( BooleanExpr.class ),
    ON_MESSAGE( OnMessage.class ),
    PATTERNED_CORRELATION_CONTAINER( PatternedCorrelationContainer.class ),
    START_COUNTER_VALUE( StartCounterValue.class ),
    COPY_OR_EXTENSIBLE_ASSIGN( AssignChild.class ),
    AFTER_IMPORTS( AfterImport.class ),
    AFTER_EXTENSIONS( AfterExtensions.class ),
    AFTER_SOURCES( AfterSources.class ),
    AFTER_TARGETS( AfterTargets.class ),
    FROM_CHILD( FromChild.class ),
    ;
    
    BpelTypesEnum( Class<? extends BpelEntity> clazz ) {
        myClass = clazz;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.BpelTypes#getComponentType()
     */
    /** {@inheritDoc} */
    public Class<? extends BpelEntity> getComponentType(){
        return myClass;
    }
    
    private Class<? extends BpelEntity> myClass;
}
