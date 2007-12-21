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
package org.netbeans.modules.bpel.model.impl;

import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.AssignChild;
import org.netbeans.modules.bpel.model.api.BaseFaultHandlers;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
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
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.ExtensibleAssign;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
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
import org.netbeans.modules.bpel.model.api.FromChild;
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
import org.netbeans.modules.bpel.model.api.ReThrow;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.ScopeHolder;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.ServiceRef;
import org.netbeans.modules.bpel.model.api.Source;
import org.netbeans.modules.bpel.model.api.SourceContainer;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.Target;
import org.netbeans.modules.bpel.model.api.TargetContainer;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.ToPartContainer;
import org.netbeans.modules.bpel.model.api.Validate;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.xam.BpelTypes;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.xam.ComponentUpdater;


/**
 * @author ads
 *
 */
public class SyncUpdateVisitor implements ComponentUpdater<BpelEntity>,
    BpelModelVisitor 
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.xdm.ComponentUpdater#update(C, C, org.netbeans.modules.xml.xam.xdm.ComponentUpdater.Operation)
     */
    public void update( BpelEntity target, BpelEntity child, Operation operation )
    {
        update(target, child, -1, operation);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.xdm.ComponentUpdater#update(C, C, int, org.netbeans.modules.xml.xam.xdm.ComponentUpdater.Operation)
     */
    public void update( BpelEntity target, BpelEntity child, int index,
            Operation operation )
    {
        assert target != null;
        assert child != null;
        assert operation == Operation.ADD || operation == Operation.REMOVE;

        myApiParent = target;

        if ( myApiParent instanceof BpelContainerImpl ) {
            myParent = (BpelContainerImpl) target;
        }
        else {
            myParent = null;
        }
        myIndex = index;
        myOperation = operation;
        
        if ( operation == Operation.REMOVE  && myParent!=null) {
            myParent.remove( child );
            return;
        }
        try {
            child.accept(this);
        }
        catch ( NullPointerException e ) {
            /*
             * It can occures when wrong situation appears :
             * this syncer was called with BPEL impl child element
             * but target element is not BPEL impl element.
             * Such situations should not appear actually
             * becuase target element in this case is extension element.
             * It cannot contain BPEL impl element.
             * Such child element should provide its own updater.  
             */  
            //assert false;
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Process)
     */
    public void visit( Process process ) {
        // we perform visiting children , so we should never appear in root.
        assert false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Empty)
     */
    public void visit( Empty empty ) {
        visitActivity(empty);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Invoke)
     */
    public void visit( Invoke invoke ) {
        visitActivity(invoke);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Receive)
     */
    public void visit( Receive receive ) {
        visitActivity(receive);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Reply)
     */
    public void visit( Reply reply ) {
        visitActivity(reply);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Assign)
     */
    public void visit( Assign assign ) {
        visitActivity(assign);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Wait)
     */
    public void visit( Wait wait ) {
        visitActivity(wait);
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Throw)
     */
    public void visit( Throw throv ) {
        visitActivity(throv);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Terminate)
     */
    public void visit( Exit terminate ) {
        visitActivity(terminate);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Flow)
     */
    public void visit( Flow flow ) {
        visitActivity(flow);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.While)
     */
    public void visit( While whil ) {
        visitActivity(whil);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Sequence)
     */
    public void visit( Sequence sequence ) {
        visitActivity(sequence);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Pick)
     */
    public void visit( Pick pick ) {
        visitActivity(pick);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Scope)
     */
    public void visit( Scope scope ) {
        assert checkScope();

        visitActivity(scope);

        // Fix for #87719 - Undo/Redo in OnEvent, OnAlarm and ForEach can create
        // broken diagram
        /*
         * This could happen only when previous method call <code>visitActivity</code>
         * didn't do anything because scope is not usual activity in some
         * activity container but it is Scope element in its own ScopeHolder. In
         * this case we need to use another logic for scope addition.
         */
        if (myParent instanceof ScopeHolder) {
            add(scope, Scope.class);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.CompensateScope)
     */
    public void visit( CompensateScope compensateScope ) {
        visitActivity( compensateScope );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer)
     */
    public void visit( PartnerLinkContainer container ) {
        if (!insert(container, PartnerLinkContainer.class)) {
            add(container, PartnerLinkContainer.class,
                    BpelTypesEnum.ACTIVITIES_GROUP,
                    BpelTypesEnum.MESSAGE_EXCHANGE_CONTAINER,
                    BpelTypesEnum.VARIABLE_CONTAINER,
                    BpelTypesEnum.CORRELATION_SET_CONTAINER,
                    BpelTypesEnum.FAULT_HANDLERS,
                    BpelTypesEnum.COMPENSATION_HANDLER,
                    BpelTypesEnum.EVENT_HANDLERS,
                    BpelTypesEnum.TERMINATION_HANDLER);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.PartnerLink)
     */
    public void visit( PartnerLink link ) {
        if (!insert(link, PartnerLink.class)) {
            assert myParent instanceof PartnerLinkContainer;
            PartnerLinkContainer container = (PartnerLinkContainer) myParent;
            container.addPartnerLink(link);
        }

    }/**
         * /* (non-Javadoc)
         * 
         * @see org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.FaultHandlers)
         */
    public void visit( FaultHandlers handlers ) {
        if (!insert(handlers, FaultHandlers.class)) {
            add(handlers,FaultHandlers.class,
                    BpelTypesEnum.ACTIVITIES_GROUP,
                    BpelTypesEnum.COMPENSATION_HANDLER,
                    BpelTypesEnum.EVENT_HANDLERS, 
                    BpelTypesEnum.TERMINATION_HANDLER);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Catch)
     */
    public void visit( Catch catc ) {
        if (!insert(catc, Catch.class)) {
            assert myParent instanceof BaseFaultHandlers;
            BaseFaultHandlers handlers = (BaseFaultHandlers) myParent;
            handlers.addCatch(catc);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.EventHandlers)
     */
    public void visit( EventHandlers handlers ) {
        if (!insert(handlers, EventHandlers.class)) {
            add( handlers, EventHandlers.class, BpelTypesEnum.ACTIVITIES_GROUP);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.OnMessage)
     */
    public void visit( OnMessage message ) {
        if (!insert(message, OnMessage.class)) {
            assert myParent instanceof Pick;
            Pick pick = (Pick) myParent;
            pick.addOnMessage(message);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.CompensationHandler)
     */
    public void visit( CompensationHandler handler ) {
        if (!insert(handler, CompensationHandler.class)) {
            add( handler, CompensationHandler.class , 
                    BpelTypesEnum.TO_PARTS,
                    BpelTypesEnum.FROM_PARTS,
                    BpelTypesEnum.ACTIVITIES_GROUP, 
                    BpelTypesEnum.EVENT_HANDLERS,
                    BpelTypesEnum.TERMINATION_HANDLER );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.VariableContainer)
     */
    public void visit( VariableContainer container ) {
        if (!insert(container, VariableContainer.class)) {
            add( container , VariableContainer.class,
                    BpelTypesEnum.ACTIVITIES_GROUP,
                    BpelTypesEnum.CORRELATION_SET_CONTAINER,
                    BpelTypesEnum.FAULT_HANDLERS,
                    BpelTypesEnum.COMPENSATION_HANDLER,
                    BpelTypesEnum.EVENT_HANDLERS,
                    BpelTypesEnum.TERMINATION_HANDLER );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Variable)
     */
    public void visit( Variable variable ) {
        if (!insert(variable, Variable.class)) {
            assert myParent instanceof VariableContainer;
            VariableContainer container = (VariableContainer) myParent;
            container.addVariable(variable);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer)
     */
    public void visit( CorrelationSetContainer container ) {
        if (!insert(container, CorrelationSetContainer.class)) {
            add( container , CorrelationSetContainer.class,
                    BpelTypesEnum.ACTIVITIES_GROUP, 
                    BpelTypesEnum.FAULT_HANDLERS,
                    BpelTypesEnum.COMPENSATION_HANDLER,
                    BpelTypesEnum.EVENT_HANDLERS,
                    BpelTypesEnum.TERMINATION_HANDLER);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.CorrelationSet)
     */
    public void visit( CorrelationSet set ) {
        if (!insert(set, CorrelationSet.class)) {
            assert myParent instanceof CorrelationSetContainer;
            CorrelationSetContainer container = (CorrelationSetContainer) myParent;
            container.addCorrelationSet(set);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Source)
     */
    public void visit( Source source ) {
        if (!insert(source, Source.class)) {
            assert myParent instanceof SourceContainer;
            SourceContainer container = (SourceContainer) myParent;
            container.addSource(source);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Target)
     */
    public void visit( Target target ) {
        if (!insert(target, Target.class)) {
            assert myParent instanceof TargetContainer;
            TargetContainer container = (TargetContainer) myParent;
            container.addTarget(target);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer)
     */
    public void visit( CorrelationContainer container ) {
        if (!insert(container, CorrelationContainer.class)) {
            add(container, CorrelationContainer.class, 
                    BpelTypesEnum.CATCH,
                    BpelTypesEnum.CATCH_ALL,
                    BpelTypesEnum.COMPENSATION_HANDLER, 
                    BpelTypesEnum.TO_PARTS,
                    BpelTypesEnum.FROM_PARTS, 
                    BpelTypesEnum.ACTIVITIES_GROUP);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Correlation)
     */
    public void visit( Correlation correlation ) {
        if (!insert(correlation, Correlation.class)) {
            assert myParent instanceof CorrelationContainer;
            CorrelationContainer container = (CorrelationContainer) myParent;
            container.addCorrelation(correlation);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelation)
     */
    public void visit( PatternedCorrelation correlation ) {  
        if (!insert(correlation, PatternedCorrelation.class)) {
            assert myParent instanceof PatternedCorrelationContainer;
            PatternedCorrelationContainer container = 
                (PatternedCorrelationContainer) myParent;
            container.addPatternedCorrelation(correlation);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer)
     */
    public void visit( PatternedCorrelationContainer container ) {
        if (!insert(container, PatternedCorrelationContainer.class)) {
            add( container , PatternedCorrelationContainer.class,
                    BpelTypesEnum.CATCH,
                    BpelTypesEnum.CATCH_ALL,
                    BpelTypesEnum.COMPENSATION_HANDLER,
                    BpelTypesEnum.TO_PARTS,
                    BpelTypesEnum.FROM_PARTS );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.To)
     */
    public void visit( To to ) {
        if (!insert(to, To.class)) {
            add( to , To.class );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.From)
     */
    public void visit( From from ) {
        if (!insert(from, From.class)) {
            add( from , From.class , BpelTypesEnum.TO );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Compensate)
     */
    public void visit( Compensate compensate ) {
        visitActivity(compensate);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.LinkContainer)
     */
    public void visit( LinkContainer container ) {
        if (!insert( container , LinkContainer.class)) {
            add( container , LinkContainer.class , 
                    BpelTypesEnum.ACTIVITIES_GROUP );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Link)
     */
    public void visit( Link link ) {
        if (!insert(link, Link.class)) {
            assert myParent instanceof LinkContainer;
            LinkContainer container = (LinkContainer) myParent;
            container.addLink(link);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Copy)
     */
    public void visit( Copy copy ) {
        visitAssignChild( copy );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.CatchAll)
     */
    public void visit( CatchAll holder ) {
        if (!insert( holder , CatchAll.class)) {
            add( holder, CatchAll.class ,
                    BpelTypesEnum.COMPENSATION_HANDLER,
                    BpelTypesEnum.TO_PARTS,
                    BpelTypesEnum.FROM_PARTS );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.BooleanExpr)
     */
    public void visit( BooleanExpr expr ) {
        if (!insert( expr , BooleanExpr.class)) {
            add( expr , BooleanExpr.class,
                    BpelTypesEnum.ELSE_IF,
                    BpelTypesEnum.ELSE,
                    BpelTypesEnum.ACTIVITIES_GROUP );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Branches)
     */
    public void visit( Branches branches ) {
        if (!insert( branches , Branches.class)) {
            add( branches ,Branches.class );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.CompletionCondition)
     */
    public void visit( CompletionCondition condition ) {
        if (!insert( condition , CompletionCondition.class)) {
            add( condition , CompletionCondition.class, BpelTypesEnum.SCOPE);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Condition)
     */
    public void visit( Condition condition ) {
        if ( insert( condition , Condition.class)) {
            return;
        }
        if (myParent instanceof Source) {
            add( condition , Condition.class );
        }
        else if (myParent instanceof TargetContainer) {
            add( condition , Condition.class, BpelTypesEnum.TARGET);
        }
        assert false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.DeadlineExpression)
     */
    public void visit( DeadlineExpression expression ) {
        visitTimeEvent( expression );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Documentation)
     */
    public void visit( Documentation documentation ) {
        if (!insert(documentation, Documentation.class)) {
            assert myParent instanceof ExtensibleElements;
            ((ExtensibleElements) myParent).addDocumentation(documentation);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Else)
     */
    public void visit( Else els ) {
        if (!insert( els , Else.class)) {
            add( els , Else.class  );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.ExtensibleAssign)
     */
    public void visit( ExtensibleAssign assign ) {
        visitAssignChild( assign );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.ExtensionActivity)
     */
    public void visit( ExtensionActivity activity ) {
        visitActivity( activity );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Validate)
     */
    public void visit( Validate validate ) {
        visitActivity( validate );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.ToPart)
     */
    public void visit( ToPart toPart ) {
        assert myParent instanceof ToPartContainer;
        if (!insert(toPart, ToPart.class)) {
            ((ToPartContainer) myParent).addToPart(toPart);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.TerminationHandler)
     */
    public void visit( TerminationHandler handler ) {
        if (!insert( handler , TerminationHandler.class)) {
            add( handler , TerminationHandler.class, 
                    BpelTypesEnum.EVENT_HANDLERS, 
                    BpelTypesEnum.ACTIVITIES_GROUP ); 
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.TargetContainer)
     */
    public void visit( TargetContainer container ) {
        if (!insert( container , TargetContainer.class)) {
            add( container , TargetContainer.class , 
                    BpelTypesEnum.AFTER_TARGETS);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.StartCounterValue)
     */
    public void visit( StartCounterValue value ) {
        if (!insert( value , StartCounterValue.class)) {
            add( value , StartCounterValue.class ,  
                    BpelTypesEnum.FINAL_COUNTER_VALUE,
                    BpelTypesEnum.COMPLETION_CONDITION,
                    BpelTypesEnum.SCOPE);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.SourceContainer)
     */
    public void visit( SourceContainer container ) {
        if (!insert( container , SourceContainer.class)) {
            add( container , SourceContainer.class , BpelTypesEnum.AFTER_SOURCES );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.ReThrow)
     */
    public void visit( ReThrow rethrow ) {
        visitActivity( rethrow );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.RepeatUntil)
     */
    public void visit( RepeatUntil repeatUntil ) {
        visitActivity( repeatUntil );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.RepeatEvery)
     */
    public void visit( RepeatEvery repeatEvery ) {
        if ( !insert( repeatEvery , RepeatEvery.class )){
            assert myParent instanceof OnAlarmEvent;
            add( repeatEvery , RepeatEvery.class, BpelTypesEnum.SCOPE );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.OnEvent)
     */
    public void visit( OnEvent event ) {
        if (!insert(event, OnEvent.class)) {
            assert myParent instanceof EventHandlers;
            ((EventHandlers) myParent).addOnEvent(event);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.OnAlarmPick)
     */
    public void visit( OnAlarmPick alarmPick ) {
        if (!insert(alarmPick, OnAlarmPick.class)) {
            assert myParent instanceof Pick;
            ((Pick) myParent).addOnAlarm(alarmPick);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.OnAlarmEvent)
     */
    public void visit( OnAlarmEvent alarmEvent ) {
        if (!insert(alarmEvent, OnAlarmEvent.class)) {
            assert myParent instanceof EventHandlers;
            ((EventHandlers) myParent).addOnAlarm(alarmEvent);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer)
     */
    public void visit( ExtensionContainer container ) {
        if (!insert( container , ExtensionContainer.class)) {
            add( container , ExtensionContainer.class, 
                    BpelTypesEnum.AFTER_EXTENSIONS);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Extension)
     */
    public void visit( Extension extension ) {
        if (!insert(extension, Extension.class)) {
            assert myParent instanceof ExtensionContainer;
            ((ExtensionContainer) myParent).addExtension(extension);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.FinalCounterValue)
     */
    public void visit( FinalCounterValue value ) {
        if (!insert( value , FinalCounterValue.class)) {
            add( value , FinalCounterValue.class,
                    BpelTypesEnum.COMPLETION_CONDITION,
                    BpelTypesEnum.SCOPE );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.ForEach)
     */
    public void visit( ForEach forEach ) {
        visitActivity( forEach );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Literal)
     */
    public void visit( Literal literal ) {
        visitFromChild(literal);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.Import)
     */
    public void visit( Import imp ) {
        if (!insert(imp, Import.class)) {
            assert myParent instanceof Process;
            ((Process) myParent).addImport(imp);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.If)
     */
    public void visit( If iff ) {
        visitActivity( iff );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.FromPart)
     */
    public void visit( FromPart fromPart ) {
        if (!insert(fromPart, FromPart.class)) {
            assert myParent instanceof FromPartContainer;
            ((FromPartContainer) myParent).addFromPart(fromPart);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.For)
     */
    public void visit( For fo ) {
        visitTimeEvent( fo );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor#visit(org.netbeans.modules.soa.model.bpel20.api.ElseIf)
     */
    public void visit( ElseIf elseIf ) {
        if (!insert(elseIf, ElseIf.class)) {
            assert myParent instanceof If;
            ((If) myParent).addElseIf(elseIf);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.MessageExchangeContainer)
     */
    public void visit( MessageExchangeContainer container ) {
        if (!insert( container , MessageExchangeContainer.class)) {
            add(container , MessageExchangeContainer.class ,
                    BpelTypesEnum.ACTIVITIES_GROUP,
                    BpelTypesEnum.VARIABLE_CONTAINER,
                    BpelTypesEnum.CORRELATION_SET_CONTAINER,
                    BpelTypesEnum.FAULT_HANDLERS,
                    BpelTypesEnum.COMPENSATION_HANDLER,
                    BpelTypesEnum.EVENT_HANDLERS,
                    BpelTypesEnum.TERMINATION_HANDLER );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.MessageExchange)
     */
    public void visit( MessageExchange exchange ) {
        if (!insert(exchange, MessageExchange.class)) {
            assert myParent instanceof MessageExchangeContainer;
            ((MessageExchangeContainer) myParent).addMessageExchange(exchange);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ServiceRef)
     */
    public void visit( ServiceRef ref ) {
        if (!insert(ref, ServiceRef.class)) {
            assert myParent instanceof Literal || myParent instanceof From
                    || myParent instanceof org.netbeans.modules.bpel.model.api.Query ;
            myParent.addChildBefore(ref, ServiceRef.class, 
                    BpelTypesEnum.FROM_CHILD);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ExtensionEntity)
     */
    public void visit( ExtensionEntity entity ) {
        EntityUpdater updater = entity.getEntityUpdater();
        updater.update(myApiParent, entity, myOperation);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.ToPartContainer)
     */
    public void visit( ToPartContainer toPartContainer ) {
        if (!insert( toPartContainer , ToPartContainer.class)) {
            add( toPartContainer , ToPartContainer.class, 
                    BpelTypesEnum.FROM_PARTS );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.FromPartContainer)
     */
    public void visit( FromPartContainer fromPartContainer ) {
        if (!insert( fromPartContainer , FromPartContainer.class)) {
            add( fromPartContainer , FromPartContainer.class  );
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitor#visit(org.netbeans.modules.bpel.model.api.Query)
     */
    public void visit( org.netbeans.modules.bpel.model.api.Query query ) {
        visitFromChild( query );
    }

    private void visitFromChild( FromChild child ) {
        if (!insert( child , FromChild.class)) {
            add( child , FromChild.class );
        }
    }
    
    private void visitTimeEvent( TimeEvent event ) {
        if (!insert( event , TimeEvent.class)) {
            add( event , TimeEvent.class ,
                    BpelTypesEnum.REPEAT_EVERY,
                    BpelTypesEnum.ACTIVITIES_GROUP );
        }
    }

    private void visitActivity( ExtendableActivity activity ) {
        if (myParent instanceof ActivityHolder
                || myParent instanceof CompositeActivity)
        {
            if (!insert(activity, ExtendableActivity.class)) {
                if (myParent instanceof ActivityHolder) {
                    add(activity, ExtendableActivity.class);
                }
                else if (myParent instanceof CompositeActivity) {
                    ((CompositeActivity) myParent).addActivity(activity);
                }
            }
        }
    }
    

    private void visitAssignChild( AssignChild child ) {
        if (!insert(child, AssignChild.class)) {
            assert myParent instanceof Assign;
            Assign assign = (Assign) myParent;
            assign.addAssignChild(child);
        }
    }
    
    private boolean checkScope() {
        boolean isUsualActivity = myParent instanceof ActivityHolder
                || myParent instanceof CompositeActivity;
        boolean isInScopeHolder = myParent instanceof ScopeHolder;
        return isUsualActivity ^ isInScopeHolder;
    }
    
    /**
     * @return true if index is appropraite to inerting . It means that index is
     *         less then absolute index last of children with appropriate type.
     *         So false wil be returned when inserting should be actually
     *         addition.
     */
    private <T extends BpelEntity> boolean insert( T child, Class<T> type ) {
        return myParent.insertAtAbsoluteIndex( child , type , myIndex );
    }
    
    private <T extends BpelEntity> void add( T child , Class<T> type , 
            BpelTypes... types )
    {
        myParent.addChildBefore( child , type , types );
    }
    
    private BpelEntity myApiParent;

    private BpelContainerImpl myParent;

    private int myIndex;
    
    private Operation myOperation;

}
