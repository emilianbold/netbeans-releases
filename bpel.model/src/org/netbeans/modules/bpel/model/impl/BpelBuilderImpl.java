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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
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
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.Extension;
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
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.ChangeEventSupport;
import org.netbeans.modules.bpel.model.api.support.ActivityDescriptor;
import org.netbeans.modules.bpel.model.api.support.ActivityDescriptor.ActivityType;
import org.netbeans.modules.bpel.model.spi.EntityFactory;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class BpelBuilderImpl implements BPELElementsBuilder {

    public BpelBuilderImpl( BpelModelImpl model ) {
        myModel = model;
        mySupport = new ChangeEventSupport();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createActivity(org.netbeans.modules.soa.model.bpel20.api.support.ActivityDescriptor)
     */
    public ExtendableActivity createActivity( ActivityDescriptor descriptor ) {
        ActivityBuilder builder = getActivityBuilder( descriptor );

        if (builder == null) {
            return null;
        }
        return builder.build( this );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createProcess()
     */
    public Process createProcess() {
        return new ProcessImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createEmpty()
     */
    public Empty createEmpty() {
        return new EmptyImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createInvoke()
     */
    public Invoke createInvoke() {
        return new InvokeImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createReceive()
     */
    public Receive createReceive() {
        return new ReceiveImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createReply()
     */
    public Reply createReply() {
        return new ReplyImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createAssign()
     */
    public Assign createAssign() {
        return new AssignImpl(this, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createAssign()
     */
    public Assign createJavaScript() {
        return new AssignImpl(this, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createWait()
     */
    public Wait createWait() {
        return new WaitImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createThrow()
     */
    public Throw createThrow() {
        return new ThrowImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createTerminate()
     */
    public Exit createExit() {
        return new ExitImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createFlow()
     */
    public Flow createFlow() {
        return new FlowImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createWhile()
     */
    public While createWhile() {
        return new WhileImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createSequence()
     */
    public Sequence createSequence() {
        return new SequenceImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createPick()
     */
    public Pick createPick() {
        return new PickImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createScope()
     */
    public Scope createScope() {
        return new ScopeImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createPartnerLinkContainer()
     */
    public PartnerLinkContainer createPartnerLinkContainer() {
        return new PartnerLinkContainerImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createPartnerLink()
     */
    public PartnerLink createPartnerLink() {
        return new PartnerLinkImpl(this);
    }

     /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createFaultHandlers()
     */
    public FaultHandlers createFaultHandlers() {
        return new FaultHandlersImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCatch()
     */
    public Catch createCatch() {
        return new CatchImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createEventHandlers()
     */
    public EventHandlers createEventHandlers() {
        return new EventHandlersImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createOnMessage()
     */
    public OnMessage createOnMessage() {
        return new OnMessageImpl(this);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCompenstaionHandler()
     */
    public CompensationHandler createCompenstaionHandler() {
        return new CompensationHandlerImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createVariableContainer()
     */
    public VariableContainer createVariableContainer() {
        return new VariableContainerImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createVariable()
     */
    public Variable createVariable() {
        return new VariableImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCorrelationSetContainer()
     */
    public CorrelationSetContainer createCorrelationSetContainer() {
        return new CorrelationSetContainerImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCorrelationSet()
     */
    public CorrelationSet createCorrelationSet() {
        return new CorrelationSetImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createSource()
     */
    public Source createSource() {
        return new SourceImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createTarget()
     */
    public Target createTarget() {
        return new TargetImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCorrelationContainer()
     */
    public CorrelationContainer createCorrelationContainer() {
        return new CorrelationContainerImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCorrelation()
     */
    public Correlation createCorrelation() {
        return new CorrelationImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createPatternedCorrelation()
     */
    public PatternedCorrelation createPatternedCorrelation() {
        return new PatternedCorrelationImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createPatternedCorrelationContainer()
     */
    public PatternedCorrelationContainer createPatternedCorrelationContainer() {
        return new PatternedCorrelationContainerImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createTo()
     */
    public To createTo() {
        return new ToImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createFrom()
     */
    public From createFrom() {
        return new FromImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCompensate()
     */
    public Compensate createCompensate() {
        return new CompensateImpl(this);
    }

        /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCompensateScope()
     */
    public CompensateScope createCompensateScope() {
        return new CompensateScopeImpl(this);
    }
    
     
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createLinkContainer()
     */
    public LinkContainer createLinkContainer() {
        return new LinkContainerImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createLink()
     */
    public Link createLink() {
        return new LinkImpl(this);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCopy()
     */
    public Copy createCopy() {
        return new CopyImpl(this);
    }
    
    /** {@inheritDoc} */
    public ExtensionAssignOperation createExtensionAssignOperation() {
        return new ExtensionAssignOperationImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCatchAll()
     */
    public CatchAll createCatchAll() {
        return new CatchAllImpl(this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createForEach()
     */
    public ForEach createForEach() {
        return new ForEachImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createIf()
     */
    public If createIf() {
        return new IfImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createRepeatUntil()
     */
    public RepeatUntil createRepeatUntil() {
        return new RepeatUntilImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createRethrow()
     */
    public ReThrow createRethrow() {
        return new ReThrowImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createValidate()
     */
    public Validate createValidate() {
        return new ValidateImpl( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createBoolean()
     */
    public BooleanExpr createCondition() {
        return new BooleanExprImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createBranches()
     */
    public Branches createBranches() {
        return new BranchesImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createCompletionCondition()
     */
    public CompletionCondition createCompletionCondition() {
        return new CompletionConditionImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createTransitionCondition()
     */
    public Condition createTransitionCondition() {
        return new ConditionImpl( this , 
                BpelElements.TRANSITION_CONDITION.getName() );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createJoinCondition()
     */
    public Condition createJoinCondition() {
        return new ConditionImpl( this , BpelElements.JOIN_CONDITION.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createDeadlineExpression()
     */
    public DeadlineExpression createUntil() {
        return new DeadlineExpressionImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createDocumentation()
     */
    public Documentation createDocumentation() {
        return new DocumentationImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createElse()
     */
    public Else createElse() {
        return new ElseImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createToPart()
     */
    public ToPart createToPart() {
        return new ToPartImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createTerminationHandler()
     */
    public TerminationHandler createTerminationHandler() {
        return new TerminationHandlerImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createTargetContainer()
     */
    public TargetContainer createTargetContainer() {
        return new TargetContainerImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createStartCounterValue()
     */
    public StartCounterValue createStartCounterValue() {
        return new StartCounterValueImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createSourceContainer()
     */
    public SourceContainer createSourceContainer() {
        return new SourceContainerImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createRepeatEvery()
     */
    public RepeatEvery createRepeatEvery() {
        return new RepeatEveryImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createOnEvent()
     */
    public OnEvent createOnEvent() {
        return new OnEventImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createOnAlarmPick()
     */
    public OnAlarmPick createOnAlarmPick() {
        return new OnAlarmPickImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createOnAlarmEvent()
     */
    public OnAlarmEvent createOnAlarmEvent() {
        return new OnAlarmEventImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createExtensionContainer()
     */
    public ExtensionContainer createExtensionContainer() {
        return new ExtensionContainerImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createExtension()
     */
    public Extension createExtension() {
        return new ExtensionImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createFinalCounterValue()
     */
    public FinalCounterValue createFinalCounterValue() {
        return new FinalCounterValueImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createLiteral()
     */
    public Literal createLiteral() {
        return new LiteralImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createImport()
     */
    public Import createImport() {
        return new ImportImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createFromPart()
     */
    public FromPart createFromPart() {
        return new FromPartImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#createFor()
     */
    public For createFor() {
        return new ForImpl( this );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BPELElementsBuilder#createElseIf()
     */
    public ElseIf createElseIf() {
        return new ElseIfImpl( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BPELElementsBuilder#createMessageExchangeContainer()
     */
    public MessageExchangeContainer createMessageExchangeContainer() {
        return new MessageExchangeContainerImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BPELElementsBuilder#createMessageExchange()
     */
    public MessageExchange createMessageExchange() {
        return new MessageExchangeImpl( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BPELElementsBuilder#createServiceRef()
     */
    public ServiceRef createServiceRef() {
        return new ServiceRefImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BPELElementsBuilder#createToPartContainer()
     */
    public ToPartContainer createToPartContainer() {
        return new ToPartContainerImpl( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BPELElementsBuilder#createFromPartContainer()
     */
    public FromPartContainer createFromPartContainer() {
        return new FromPartConainerImpl( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BPELElementsBuilder#createQuery()
     */
    public Query createQuery() {
        return new QueryImpl( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BPELElementsBuilder#createExtensionEntity(java.lang.Class)
     */
    public <T extends ExtensionEntity> T createExtensionEntity( Class<T> clazz ) {
        Collection<EntityFactory> factories = 
            getModel().getEntityRegistry().getFactories();
        for (EntityFactory factory : factories) {
            T result = factory.create( this, clazz );
            if ( result!= null ){
                return result;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#addEntityChangeListener(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEventListener)
     */
    public void addEntityChangeListener( ChangeEventListener listener ) {
        mySupport.addChangeEventListener(listener);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BPELElementsBuilder#removeEntityChangeListener(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEventListener)
     */
    public void removeEntityChangeListener( ChangeEventListener listener ) {
        mySupport.removeChangeEventListener(listener);
    }
    
    void fireChangeEvent( ChangeEvent event ) {
        mySupport.fireChangeEvent(event);
    }

    public BpelModelImpl getModel() {
        return myModel;
    }
    
    private ActivityBuilder getActivityBuilder( ActivityDescriptor descr ){
        return ACTIVITY_BUILDERS.get( descr.getType() );
    }

    private BpelModelImpl myModel;
    private ChangeEventSupport mySupport;
    private static final Map<ActivityType,ActivityBuilder> PRIVATE_BUILDERS = new HashMap<ActivityType,ActivityBuilder>();
    private static final Map<ActivityType,ActivityBuilder> ACTIVITY_BUILDERS = Collections.unmodifiableMap( PRIVATE_BUILDERS );

    static {
        PRIVATE_BUILDERS.put( ActivityType.EMPTY, new EmptyBuilder() );
        PRIVATE_BUILDERS.put( ActivityType.INVOKE, new InvokeBuilder());
        PRIVATE_BUILDERS.put( ActivityType.RECEIVE, new ReceiveBuilder());
        PRIVATE_BUILDERS.put( ActivityType.REPLY, new ReplyBuilder());
        PRIVATE_BUILDERS.put( ActivityType.ASSIGN, new AssignBuilder());
        PRIVATE_BUILDERS.put( ActivityType.WAIT, new WaitBuilder());
        PRIVATE_BUILDERS.put( ActivityType.THROW, new ThrowBuilder());
        PRIVATE_BUILDERS.put( ActivityType.EXIT, new ExitBuilder());
        PRIVATE_BUILDERS.put( ActivityType.FLOW, new FlowBuilder());
        PRIVATE_BUILDERS.put( ActivityType.WHILE, new WhileBuilder());
        PRIVATE_BUILDERS.put( ActivityType.SEQUENCE, new SequenceBuilder());
        PRIVATE_BUILDERS.put( ActivityType.SCOPE, new ScopeBuilder());
        PRIVATE_BUILDERS.put( ActivityType.PICK, new PickBuilder()); 
        PRIVATE_BUILDERS.put( ActivityType.COMPENSATE, new CompensateBuilder());
        PRIVATE_BUILDERS.put( ActivityType.FOR_EACH, new ForEachBuilder());
        PRIVATE_BUILDERS.put( ActivityType.IF, new IfBuilder());
        PRIVATE_BUILDERS.put( ActivityType.REPEAT_UNTIL, new RepeatUntilBuilder());
        PRIVATE_BUILDERS.put( ActivityType.RETHROW, new RethrowBuilder());
        PRIVATE_BUILDERS.put( ActivityType.VALIDATE, new ValidateBuilder());
        PRIVATE_BUILDERS.put( ActivityType.COMPENSATE_SCOPE, new CompensateScopeBuilder());
    }

    public static interface ActivityBuilder {
        
        /**
         * @param type Type of activity for which should be built activity.
         * @return Is this builder is applicable for type in argument.
         */
        boolean isApplicable( ActivityType type );
        
        /**
         * @param builder Common elements builder.
         * @return Instantiated activity.
         */
        ExtendableActivity build( BpelBuilderImpl builder );
        
        /**
         * @return Type for which this builder is applicable.
         */
        ActivityType getType();
        
        /**
         * Creates element in <code>mode</code> with specified peer <code>element</code>. 
         * @param model OM model.
         * @param element DOM element.
         * @return Instatiated activity.
         */
        BpelEntity build( BpelModelImpl model , Element element );
    }

    static abstract class AbstractBuilder implements ActivityBuilder {
        
        public boolean isApplicable( ActivityType type ){
            return getType() == type;
        }
    }

    public static class EmptyBuilder extends AbstractBuilder {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createEmpty();
        }

        public ActivityType getType() {
            return ActivityType.EMPTY;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new EmptyImpl( model , element );
        }
    }

    public static class InvokeBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createInvoke();
        }

        public ActivityType getType() {
            return ActivityType.INVOKE;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new InvokeImpl( model , element );
        }
    }

    public static class ReceiveBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createReceive();
        }

        public ActivityType getType() {
            return ActivityType.RECEIVE;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new ReceiveImpl( model , element );
        }
    }

    public static class ReplyBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createReply();
        }

        public ActivityType getType() {
            return ActivityType.REPLY;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new ReplyImpl( model , element );
        }
    }

    public static class AssignBuilder extends AbstractBuilder  {


        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createAssign();
        }

        public ActivityType getType() {
            return ActivityType.ASSIGN;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new AssignImpl( model , element );
        }

    }

    public static class WaitBuilder extends AbstractBuilder {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createWait();
        }

        public ActivityType getType() {
            return ActivityType.WAIT;
        }
        
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new WaitImpl(model, element);
        }
    }

    public static class ThrowBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createThrow();
        }

        public ActivityType getType() {
            return ActivityType.THROW;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new ThrowImpl( model , element );
        }
    }

    public static class ExitBuilder extends AbstractBuilder {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createExit();
        }

        public ActivityType getType() {
            return ActivityType.EXIT;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new ExitImpl( model , element );
        }
    }

    public static class FlowBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createFlow();
        }

        public ActivityType getType() {
            return ActivityType.FLOW;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new FlowImpl( model , element );
        }
    }

    public static class WhileBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createWhile();
        }

        public ActivityType getType() {
            return ActivityType.WHILE;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new WhileImpl( model , element );
        }
    }

    public static class SequenceBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createSequence();
        }

        public ActivityType getType() {
            return ActivityType.SEQUENCE;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new SequenceImpl( model , element );
        }
    }

    public static class ScopeBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createScope();
        }

        public ActivityType getType() {
            return ActivityType.SCOPE;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new ScopeImpl( model , element );
        }
    }

    public static class PickBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createPick();
        }

        public ActivityType getType() {
            return ActivityType.PICK;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new PickImpl( model , element );
        }
    }

    public static class CompensateBuilder extends AbstractBuilder {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createCompensate();
        }

        public ActivityType getType() {
            return ActivityType.COMPENSATE;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new CompensateImpl( model , element );
        }
    }

    public static class ForEachBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createForEach();
        }

        public ActivityType getType() {
            return ActivityType.FOR_EACH;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new ForEachImpl( model , element );
        }
    }

    public static class IfBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createIf();
        }

        public ActivityType getType() {
            return ActivityType.IF;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new IfImpl( model , element );
        }
    }

    public static class RepeatUntilBuilder extends AbstractBuilder  {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createRepeatUntil();
        }

        public ActivityType getType() {
            return ActivityType.REPEAT_UNTIL;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new RepeatUntilImpl( model , element );
        }
    }

    public static class RethrowBuilder extends AbstractBuilder {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createRethrow();
        }

        public ActivityType getType() {
            return ActivityType.RETHROW;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new ReThrowImpl( model , element );
        }
    }

    public static class ValidateBuilder extends AbstractBuilder {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createValidate();
        }

        public ActivityType getType() {
            return ActivityType.VALIDATE;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new ValidateImpl( model , element );
        }
    }

    public static class CompensateScopeBuilder extends AbstractBuilder {

        public ExtendableActivity build( BpelBuilderImpl builder ) {
            return builder.createCompensateScope();
        }

        public ActivityType getType() {
            return ActivityType.COMPENSATE_SCOPE;
        }
        
        public BpelEntity build( BpelModelImpl model, Element element ) {
            return new CompensateScopeImpl( model , element );
        }
    }
}
