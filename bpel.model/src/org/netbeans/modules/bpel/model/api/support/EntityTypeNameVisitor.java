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
package org.netbeans.modules.bpel.model.api.support;

import org.netbeans.modules.bpel.model.api.Assign;
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
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;

/**
 * Specifies a name of each BpelEntity type.
 * 
 * @author nk160297
 */
public class EntityTypeNameVisitor implements SimpleBpelModelVisitor {
    
    private String myTypeName;
    
    public EntityTypeNameVisitor() {
    }
    
    public synchronized String getTypeName(BpelEntity bpelEntity) {
        bpelEntity.accept(this);
        return getTypeName();
    }
    
    private String getTypeName() {
        return myTypeName;
    }
    
    public void visit(Process process) {
        myTypeName = "Process"; // NOI18N
    }
    
    public void visit(Empty empty) {
        myTypeName = "Empty"; // NOI18N
    }
    
    public void visit(Invoke invoke) {
        myTypeName = "Invoke"; // NOI18N
    }
    
    public void visit(Receive receive) {
        myTypeName = "Receive"; // NOI18N
    }
    
    public void visit(Reply reply) {
        myTypeName = "Reply"; // NOI18N
        
    }
    
    public void visit(Assign assign) {
        myTypeName = "Assign"; // NOI18N
    }
    
    public void visit(Wait wait) {
        myTypeName = "Wait"; // NOI18N
    }
    
    public void visit(Throw throv) {
        myTypeName = "Throw"; // NOI18N
    }
    
    public void visit(Exit terminate) {
        myTypeName = "Exit"; // NOI18N
    }
    
    public void visit(Flow flow) {
        myTypeName = "Flow"; // NOI18N
    }
    
    public void visit(While whil) {
        myTypeName = "While"; // NOI18N
    }
    
    public void visit(Sequence sequence) {
        myTypeName = "Sequence"; // NOI18N
    }
    
    public void visit(Pick pick) {
        myTypeName = "Pick"; // NOI18N
    }
    
    public void visit(Scope scope) {
        myTypeName = "Scope"; // NOI18N
    }
    
    public void visit(PartnerLinkContainer container) {
        myTypeName = "PartnerLinkContainer"; // NOI18N
    }
    
    public void visit(PartnerLink link) {
        myTypeName = "PartnerLink"; // NOI18N
    }
    
    public void visit(FaultHandlers handlers) {
        myTypeName = "FaultHandlers"; // NOI18N
    }
    
    public void visit(Catch catc) {
        myTypeName = "Catch"; // NOI18N
    }
    
    public void visit(EventHandlers handlers) {
        myTypeName = "EventHandlers"; // NOI18N
    }
    
    public void visit(OnMessage message) {
        myTypeName = "OnMessage"; // NOI18N
    }
    
    public void visit(CompensationHandler handler) {
        myTypeName = "CompensationHandler"; // NOI18N
    }
    
    public void visit(VariableContainer container) {
        myTypeName = "VariableContainer"; // NOI18N
    }
    
    public void visit(Variable variable) {
        myTypeName = "Variable"; // NOI18N
    }
    
    public void visit(CorrelationSetContainer container) {
        myTypeName = "CorrelationSetContainer"; // NOI18N
    }
    
    public void visit(CorrelationSet set) {
        myTypeName = "CorrelationSet"; // NOI18N
    }
    
    public void visit(Source source) {
        myTypeName = "Source"; // NOI18N
    }
    
    public void visit(Target target) {
        myTypeName = "Target"; // NOI18N
    }
    
    public void visit(CorrelationContainer container) {
        myTypeName = "CorrelationContainer"; // NOI18N
    }
    
    public void visit(Correlation correlation) {
        myTypeName = "Correlation"; // NOI18N
    }
    
    public void visit(PatternedCorrelation correlation) {
        myTypeName = "PatternedCorrelation"; // NOI18N
    }
    
    public void visit(PatternedCorrelationContainer container) {
        myTypeName = "PatternedCorrelationContainer"; // NOI18N
    }
    
    public void visit(To to) {
        myTypeName = "To"; // NOI18N
    }
    
    public void visit(From from) {
        myTypeName = "From"; // NOI18N
    }
    
    public void visit(Compensate compensate) {
        myTypeName = "Compensate"; // NOI18N
    }
    
    public void visit(LinkContainer container) {
        myTypeName = "LinkContainer"; // NOI18N
    }
    
    public void visit(Link link) {
        myTypeName = "Link"; // NOI18N
    }
    
    public void visit(Copy copy) {
        myTypeName = "Copy"; // NOI18N
    }
    
    public void visit(CatchAll holder) {
        myTypeName = "CatchAll"; // NOI18N
    }
    
    public void visit(BooleanExpr expr) {
        myTypeName = "BooleanExpr"; // NOI18N
    }
    
    public void visit(Branches branches) {
        myTypeName = "Branches"; // NOI18N
    }
    
    public void visit(CompletionCondition condition) {
        myTypeName = "CompletionCondition"; // NOI18N
    }
    
    public void visit(Condition condition) {
        myTypeName = "Condition"; // NOI18N
    }
    
    public void visit(DeadlineExpression expression) {
        myTypeName = "DeadlineExpression"; // NOI18N
    }
    
    public void visit(Documentation documentation) {
        myTypeName = "Documentation"; // NOI18N
    }
    
    public void visit(Else els) {
        myTypeName = "Else"; // NOI18N
    }
    
    public void visit(ElseIf elseIf) {
        myTypeName = "ElseIf"; // NOI18N
    }
    
    public void visit(ExtensibleAssign assign) {
        myTypeName = "ExtensibleAssign"; // NOI18N
    }
    
    public void visit(ExtensionActivity activity) {
        myTypeName = "ExtensionActivity"; // NOI18N
    }
    
    public void visit(Validate validate) {
        myTypeName = "Validate"; // NOI18N
    }
    
    public void visit(ToPart toPart) {
        myTypeName = "ToPart"; // NOI18N
    }
    
    public void visit(ToPartContainer toPartContainer) {
        myTypeName = "ToPartContainer"; // NOI18N
    }
    
    public void visit(TerminationHandler handler) {
        myTypeName = "TerminationHandler"; // NOI18N
    }
    
    public void visit(TargetContainer container) {
        myTypeName = "TargetContainer"; // NOI18N
    }
    
    public void visit(StartCounterValue value) {
        myTypeName = "StartCounterValue"; // NOI18N
    }
    
    public void visit(SourceContainer container) {
        myTypeName = "SourceContainer"; // NOI18N
    }
    
    public void visit(ReThrow rethrow) {
        myTypeName = "ReThrow"; // NOI18N
    }
    
    public void visit(RepeatUntil repeatUntil) {
        myTypeName = "RepeatUntil"; // NOI18N
    }
    
    public void visit(RepeatEvery repeatEvery) {
        myTypeName = "RepeatEvery"; // NOI18N
    }
    
    public void visit(OnEvent event) {
        myTypeName = "OnEvent"; // NOI18N
    }
    
    public void visit(OnAlarmPick alarmPick) {
        myTypeName = "OnAlarmPick"; // NOI18N
    }
    
    public void visit(OnAlarmEvent alarmEvent) {
        myTypeName = "OnAlarmEvent"; // NOI18N
    }
    
    public void visit(ExtensionContainer container) {
        myTypeName = "ExtensionContainer"; // NOI18N
    }
    
    public void visit(Extension extension) {
        myTypeName = "Extension"; // NOI18N
    }
    
    public void visit(FinalCounterValue value) {
        myTypeName = "FinalCounterValue"; // NOI18N
    }
    
    public void visit(ForEach forEach) {
        myTypeName = "ForEach"; // NOI18N
    }
    
    public void visit(Literal literal) {
        myTypeName = "Literal"; // NOI18N
    }
    
    public void visit(Import imp) {
        myTypeName = "Import"; // NOI18N
    }
    
    public void visit(If iff) {
        myTypeName = "If"; // NOI18N
    }
    
    public void visit(FromPart fromPart) {
        myTypeName = "FromPart"; // NOI18N
    }
    
    public void visit(FromPartContainer fromPartContainer) {
        myTypeName = "FromPartContainer"; // NOI18N
    }
    
    public void visit(For fo) {
        myTypeName = "For"; // NOI18N
    }
    
    public void visit(MessageExchangeContainer container) {
        myTypeName = "MessageExchangeContainer"; // NOI18N
    }
    
    public void visit(MessageExchange exchange) {
        myTypeName = "MessageExchange"; // NOI18N
    }
    
    public void visit(ServiceRef ref) {
        myTypeName = "ServiceRef"; // NOI18N
    }
    
    public void visit(ExtensionEntity entity) {
        myTypeName = "ExtensionEntity"; // NOI18N
    }
    
    public void visit(CompensateScope compensateScope) {
        myTypeName = "CompensateScope"; // NOI18N
    }
    
    public void visit(Query query) {
        myTypeName = "Query"; // NOI18N
    }
    
}
