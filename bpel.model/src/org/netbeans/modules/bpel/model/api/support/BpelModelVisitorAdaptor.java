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

import org.netbeans.modules.bpel.model.api.Activity;
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
import org.netbeans.modules.bpel.model.api.ExtensionAssignOperation;
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
 * @author ads
 *
 */
public class BpelModelVisitorAdaptor implements BpelModelVisitor {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Process)
     */
    /** {@inheritDoc} */
    public void visit( Process process ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Empty)
     */
    /** {@inheritDoc} */
    public void visit( Empty empty ) {
        visit( ( Activity) empty );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Invoke)
     */
    /** {@inheritDoc} */
    public void visit( Invoke invoke ) {
        visit( ( Activity) invoke );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Receive)
     */
    /** {@inheritDoc} */
    public void visit( Receive receive ) {
        visit( ( Activity) receive );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Reply)
     */
    /** {@inheritDoc} */
    public void visit( Reply reply ) {
        visit( ( Activity) reply );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Assign)
     */
    /** {@inheritDoc} */
    public void visit( Assign assign ) {
        visit( ( Activity) assign );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Wait)
     */
    /** {@inheritDoc} */
    public void visit( Wait wait ) {
        visit( ( Activity) wait );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Throw)
     */
    /** {@inheritDoc} */
    public void visit( Throw throv ) {
        visit( ( Activity) throv );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Exit)
     */
    /** {@inheritDoc} */
    public void visit( Exit terminate ) {
        visit( ( Activity) terminate );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Flow)
     */
    /** {@inheritDoc} */
    public void visit( Flow flow ) {
        visit( ( Activity) flow );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.While)
     */
    /** {@inheritDoc} */
    public void visit( While whil ) {
        visit( ( Activity) whil );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Sequence)
     */
    /** {@inheritDoc} */
    public void visit( Sequence sequence ) {
        visit( ( Activity) sequence );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Pick)
     */
    /** {@inheritDoc} */
    public void visit( Pick pick ) {
        visit( ( Activity) pick );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Scope)
     */
    /** {@inheritDoc} */
    public void visit( Scope scope ) {
        visit( ( Activity) scope );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.PartnerLinkContainer)
     */
    /** {@inheritDoc} */
    public void visit( PartnerLinkContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.PartnerLink)
     */
    /** {@inheritDoc} */
    public void visit( PartnerLink link ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.FaultHandlers)
     */
    /** {@inheritDoc} */
    public void visit( FaultHandlers handlers ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Catch)
     */
    /** {@inheritDoc} */
    public void visit( Catch catc ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.EventHandlers)
     */
    /** {@inheritDoc} */
    public void visit( EventHandlers handlers ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.OnMessage)
     */
    /** {@inheritDoc} */
    public void visit( OnMessage message ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.CompensationHandler)
     */
    /** {@inheritDoc} */
    public void visit( CompensationHandler handler ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.VariableContainer)
     */
    /** {@inheritDoc} */
    public void visit( VariableContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Variable)
     */
    /** {@inheritDoc} */
    public void visit( Variable variable ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.CorrelationSetContainer)
     */
    /** {@inheritDoc} */
    public void visit( CorrelationSetContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.CorrelationSet)
     */
    /** {@inheritDoc} */
    public void visit( CorrelationSet set ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Source)
     */
    /** {@inheritDoc} */
    public void visit( Source source ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Target)
     */
    /** {@inheritDoc} */
    public void visit( Target target ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.CorrelationContainer)
     */
    /** {@inheritDoc} */
    public void visit( CorrelationContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Correlation)
     */
    /** {@inheritDoc} */
    public void visit( Correlation correlation ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.PatternedCorrelation)
     */
    /** {@inheritDoc} */
    public void visit( PatternedCorrelation correlation ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer)
     */
    /** {@inheritDoc} */
    public void visit( PatternedCorrelationContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.To)
     */
    /** {@inheritDoc} */
    public void visit( To to ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.From)
     */
    /** {@inheritDoc} */
    public void visit( From from ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Compensate)
     */
    /** {@inheritDoc} */
    public void visit( Compensate compensate ) {
        visit( ( Activity) compensate );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.LinkContainer)
     */
    /** {@inheritDoc} */
    public void visit( LinkContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Link)
     */
    /** {@inheritDoc} */
    public void visit( Link link ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Copy)
     */
    /** {@inheritDoc} */
    public void visit( Copy copy ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.CatchAll)
     */
    /** {@inheritDoc} */
    public void visit( CatchAll holder ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.BooleanExpr)
     */
    /** {@inheritDoc} */
    public void visit( BooleanExpr expr ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Branches)
     */
    /** {@inheritDoc} */
    public void visit( Branches branches ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.CompletionCondition)
     */
    /** {@inheritDoc} */
    public void visit( CompletionCondition condition ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Condition)
     */
    /** {@inheritDoc} */
    public void visit( Condition condition ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.DeadlineExpression)
     */
    /** {@inheritDoc} */
    public void visit( DeadlineExpression expression ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Documentation)
     */
    /** {@inheritDoc} */
    public void visit( Documentation documentation ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Else)
     */
    /** {@inheritDoc} */
    public void visit( Else els ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ElseIf)
     */
    /** {@inheritDoc} */
    public void visit( ElseIf elseIf ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ExtensibleAssign)
     */
    /** {@inheritDoc} */
    public void visit( ExtensibleAssign assign ) {
    }

    /** {@inheritDoc} */
    public void visit( ExtensionAssignOperation extAssignOp ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ExtensionActivity)
     */
    /** {@inheritDoc} */
    public void visit( ExtensionActivity activity ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Validate)
     */
    /** {@inheritDoc} */
    public void visit( Validate validate ) {
        visit( ( Activity) validate );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ToPart)
     */
    /** {@inheritDoc} */
    public void visit( ToPart toPart ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.TerminationHandler)
     */
    /** {@inheritDoc} */
    public void visit( TerminationHandler handler ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.TargetContainer)
     */
    /** {@inheritDoc} */
    public void visit( TargetContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.StartCounterValue)
     */
    /** {@inheritDoc} */
    public void visit( StartCounterValue value ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.SourceContainer)
     */
    /** {@inheritDoc} */
    public void visit( SourceContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ReThrow)
     */
    /** {@inheritDoc} */
    public void visit( ReThrow rethrow ) {
        visit( ( Activity) rethrow );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.RepeatUntil)
     */
    /** {@inheritDoc} */
    public void visit( RepeatUntil repeatUntil ) {
        visit( ( Activity) repeatUntil );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.RepeatEvery)
     */
    /** {@inheritDoc} */
    public void visit( RepeatEvery repeatEvery ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.OnEvent)
     */
    /** {@inheritDoc} */
    public void visit( OnEvent event ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.OnAlarmPick)
     */
    /** {@inheritDoc} */
    public void visit( OnAlarmPick alarmPick ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.OnAlarmEvent)
     */
    /** {@inheritDoc} */
    public void visit( OnAlarmEvent alarmEvent ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ExtensionContainer)
     */
    /** {@inheritDoc} */
    public void visit( ExtensionContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Extension)
     */
    /** {@inheritDoc} */
    public void visit( Extension extension ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.FinalCounterValue)
     */
    /** {@inheritDoc} */
    public void visit( FinalCounterValue value ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ForEach)
     */
    /** {@inheritDoc} */
    public void visit( ForEach forEach ) {
        visit( ( Activity) forEach );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Literal)
     */
    /** {@inheritDoc} */
    public void visit( Literal literal ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Import)
     */
    /** {@inheritDoc} */
    public void visit( Import imp ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.If)
     */
    /** {@inheritDoc} */
    public void visit( If iff ) {
        visit( ( Activity) iff );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.FromPart)
     */
    /** {@inheritDoc} */
    public void visit( FromPart fromPart ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.For)
     */
    /** {@inheritDoc} */
    public void visit( For fo ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.MessageExchangeContainer)
     */
    /** {@inheritDoc} */
    public void visit( MessageExchangeContainer container ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.MessageExchange)
     */
    /** {@inheritDoc} */
    public void visit( MessageExchange exchange ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ServiceRef)
     */
    /** {@inheritDoc} */
    public void visit( ServiceRef ref ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ExtensionEntity)
     */
    /** {@inheritDoc} */
    public void visit( ExtensionEntity entity ) {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ToPartContainer)
     */
    /** {@inheritDoc} */
    public void visit( ToPartContainer toPartContainer ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.FromPartContainer)
     */
    /** {@inheritDoc} */
    public void visit( FromPartContainer fromPartContainer ) {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Query)
     */
    /** {@inheritDoc} */
    public void visit( Query query ) {
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.CompensateScope)
     */
    /** {@inheritDoc} */
    public void visit( CompensateScope compensateScope ) {
        visit( ( Activity) compensateScope );
    }

    protected void visit( Activity activity ) {
    }

}
