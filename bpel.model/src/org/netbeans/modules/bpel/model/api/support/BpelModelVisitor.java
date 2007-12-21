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
package org.netbeans.modules.bpel.model.api.support;

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.Condition;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.Documentation;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.Empty;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.Exit;
import org.netbeans.modules.bpel.model.api.ExtensibleAssign;
import org.netbeans.modules.bpel.model.api.Extension;
import org.netbeans.modules.bpel.model.api.ExtensionActivity;
import org.netbeans.modules.bpel.model.api.ExtensionContainer;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.Link;
import org.netbeans.modules.bpel.model.api.LinkContainer;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.ReThrow;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.ServiceRef;
import org.netbeans.modules.bpel.model.api.Source;
import org.netbeans.modules.bpel.model.api.SourceContainer;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.Target;
import org.netbeans.modules.bpel.model.api.TargetContainer;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.ToPartContainer;
import org.netbeans.modules.bpel.model.api.Validate;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;


/**
 * This visitor should implement logic of navigation in tree by itself. 
 * 
 * @author ads
 *
 */
public interface BpelModelVisitor  {
    /**
     * Visit process element.
     * 
     * @param process
     *            visited object.
     */
    void visit( Process process );

    /**
     * Visit empty element.
     * 
     * @param empty
     *            visited object.
     */
    void visit( Empty empty );

    /**
     * Visit invoke element.
     * 
     * @param invoke
     *            visited object.
     */
    void visit( Invoke invoke );

    /**
     * Visit receive element.
     * 
     * @param receive
     *            visited object.
     */
    void visit( Receive receive );

    /**
     * Visit reply element.
     * 
     * @param reply
     *            visited object.
     */
    void visit( Reply reply );

    /**
     * Visit assign element.
     * 
     * @param assign
     *            visited object.
     */
    void visit( Assign assign );

    /**
     * Visit wait element.
     * 
     * @param wait
     *            visited object.
     */
    void visit( Wait wait );

    /**
     * Visit throw element.
     * 
     * @param throv
     *            visited object.
     */
    void visit( Throw throv );

    /**
     * Visit terminate element.
     * 
     * @param terminate
     *            visited object.
     */
    void visit( Exit terminate );

    /**
     * Visit flow element.
     * 
     * @param flow
     *            visited object.
     */
    void visit( Flow flow );

    /**
     * Visit while element.
     * 
     * @param whil
     *            visited object.
     */
    void visit( While whil );

    /**
     * Visit sequence element.
     * 
     * @param sequence
     *            visited object.
     */
    void visit( Sequence sequence );

    /**
     * Visit pick element.
     * 
     * @param pick
     *            visited object.
     */
    void visit( Pick pick );

    /**
     * Visit scope element.
     * 
     * @param scope
     *            visited object.
     */
    void visit( Scope scope );

    /**
     * Visit partnerLinks element.
     * 
     * @param container
     *            visited object.
     */
    void visit( PartnerLinkContainer container );

    /**
     * Visit partnerLink element.
     * 
     * @param link
     *            visited object.
     */
    void visit( PartnerLink link );

    /**
     * Visit faultHandlers element.
     * 
     * @param handlers
     *            visited object.
     */
    void visit( FaultHandlers handlers );

    /**
     * Visit catch element.
     * 
     * @param catc
     *            visited object.
     */
    void visit( Catch catc );

    /**
     * Visit eventHandlers element.
     * 
     * @param handlers
     *            visited object.
     */
    void visit( EventHandlers handlers );

    /**
     * Visit onMessage element.
     * 
     * @param message
     *            visited object.
     */
    void visit( OnMessage message );

    /**
     * Visit compensationHandler element.
     * 
     * @param handler
     *            visited object.
     */
    void visit( CompensationHandler handler );

    /**
     * Visit variables element.
     * 
     * @param container
     *            visited object.
     */
    void visit( VariableContainer container );

    /**
     * Visit variable element.
     * 
     * @param variable
     *            visited object.
     */
    void visit( Variable variable );

    /**
     * Visit correlationSets element.
     * 
     * @param container
     *            visited object.
     */
    void visit( CorrelationSetContainer container );

    /**
     * Visit correlationSet element.
     * 
     * @param set
     *            visited object.
     */
    void visit( CorrelationSet set );

    /**
     * Visit source element.
     * 
     * @param source
     *            visited object.
     */
    void visit( Source source );

    /**
     * Visit target element.
     * 
     * @param target
     *            visited object.
     */
    void visit( Target target );

    /**
     * Visit correlations element.
     * 
     * @param container
     *            visited object.
     */
    void visit( CorrelationContainer container );

    /**
     * Visit correlation element.
     * 
     * @param correlation
     *            visited object.
     */
    void visit( Correlation correlation );

    /**
     * Visit correlationWithPattern element.
     * 
     * @param correlation
     *            visited object.
     */
    void visit( PatternedCorrelation correlation );

    /**
     * Visit correlationsWithPattern element.
     * 
     * @param container
     *            visited object.
     */
    void visit( PatternedCorrelationContainer container );

    /**
     * Visit to element.
     * 
     * @param to
     *            visited object.
     */
    void visit( To to );

    /**
     * Visit from element.
     * 
     * @param from
     *            visited object.
     */
    void visit( From from );

    /**
     * Visit compensate element.
     * 
     * @param compensate
     *            visited object.
     */
    void visit( Compensate compensate );

    /**
     * Visit links element.
     * 
     * @param container
     *            visited object.
     */
    void visit( LinkContainer container );

    /**
     * Visit link element.
     * 
     * @param link
     *            visited object.
     */
    void visit( Link link );

    /**
     * Visit copy element.
     * 
     * @param copy
     *            visited object.
     */
    void visit( Copy copy );


    /**
     * Visit activityOrCompensateContainer ( catchAll tag ).
     * 
     * @param holder
     *            visited object.
     */
    void visit( CatchAll holder );

    /**
     * Visit Boolean-expr element.
     * @param expr visited object.
     */
    void visit( BooleanExpr expr );

    /**
     * Visit branches element.
     * @param branches visited object.
     */
    void visit( Branches branches );

    /**
     * Visit completionCondition element.
     * @param condition visited object.
     */
    void visit( CompletionCondition condition );

    /**
     * Visit condition element.
     * @param condition visited object.
     */
    void visit( Condition condition );

    /**
     * Visit Deadline-Expr element.
     * @param expression visited object.
     */
    void visit( DeadlineExpression expression );

    /**
     * Visit Documentation element.
     * @param documentation visited object.
     */
    void visit( Documentation documentation );

    /**
     * Visit Else element.
     * @param els visited object.
     */
    void visit( Else els );
    
    /**
     * Visit ElseIf element.
     * @param elseIf visited object.
     */
    void visit( ElseIf elseIf );

    /**
     * Visit ExtensibleAssign element.
     * @param assign visited object.
     */
    void visit( ExtensibleAssign assign );

    /**
     * Visit ExtensionActivity element.
     * @param activity visited object.
     */
    void visit( ExtensionActivity activity );

    /**
     * Visit Validate element.
     * @param validate visited object.
     */
    void visit( Validate validate );

    /**
     * Visit ToPart element.
     * @param toPart visited object.
     */
    void visit( ToPart toPart );
    
    /**
     * Visit ToPartContainer element.
     * @param toPartContainer visited object.
     */
    void visit( ToPartContainer toPartContainer );

    /**
     * Visit TerminationHandler element.
     * @param handler visited object.
     */
    void visit( TerminationHandler handler );

    /**
     * Visit TargetContainer element.
     * @param container visited object.
     */
    void visit( TargetContainer container );

    /**
     * Visit StartCounterValue element.
     * @param value visited object.
     */
    void visit( StartCounterValue value  );

    /**
     * Visit SourceContainer element.
     * @param container visited object.
     */
    void visit( SourceContainer container );

    /**
     * Visit ReThrow element.
     * @param rethrow visited object.
     */
    void visit( ReThrow rethrow );

    /**
     * Visit RepeatUntil element.
     * @param repeatUntil visited object.
     */
    void visit( RepeatUntil repeatUntil );

    /**
     * Visit RepeatEvery element.
     * @param repeatEvery visited object.
     */
    void visit( RepeatEvery repeatEvery );

    /**
     * Visit OnEvent element.
     * @param event visited object.
     */
    void visit( OnEvent event );

    /**
     * Visit OnAlarmPick element.
     * @param alarmPick visited object.
     */
    void visit( OnAlarmPick alarmPick );

    /**
     * Visit OnAlarmEvent element.
     * @param alarmEvent visited object.
     */
    void visit( OnAlarmEvent alarmEvent );

    /**
     * Visit ExtensionContainer element.
     * @param container visited object.
     */
    void visit( ExtensionContainer container );

    /**
     * Visit Extension element.
     * @param extension visited object.
     */
    void visit( Extension extension );

    /**
     * Visit FinalCounterValue element.
     * @param value  visited object.
     */
    void visit( FinalCounterValue value  );

    /**
     * Visit ForEach element.
     * @param forEach visited object.
     */
    void visit( ForEach forEach );

    /**
     * Visit Literal element.
     * @param literal visited object.
     */
    void visit( Literal literal );

    /**
     * Visit Import element.
     * @param imp visited object.
     */
    void visit( Import imp );

    /**
     * Visit If element.
     * @param iff visited object.
     */
    void visit( If iff );

    /**
     * Visit FromPart element.
     * @param fromPart visited object.
     */
    void visit( FromPart fromPart );
    
    /**
     * Visit FromPartContainer element.
     * @param fromPartContainer visited object.
     */
    void visit( FromPartContainer fromPartContainer );

    /**
     * Visit For element.
     * @param fo visited object.
     */
    void visit( For fo );
    
    /**
     * Visit MessageExchangeContainer element.
     * @param container visited object.
     */
    void visit( MessageExchangeContainer container );
    
    /**
     * Visit MessageExchange element.
     * @param exchange visited object.
     */
    void visit( MessageExchange exchange );

    /**
     * Visit ServiceRef element.
     * @param ref visited object.
     */
    void visit( ServiceRef ref );
    
    /**
     * Visit extension entity.
     * @param entity visited extension object.
     */
    void visit( ExtensionEntity entity );
    
    /**
     * Visit CompensateScope element. 
     * @param compensateScope visited object.
     */
    void visit( CompensateScope compensateScope );
    
    /**
     * Visit Query element. 
     * @param query visited object.
     */
    void visit( Query query );

}
