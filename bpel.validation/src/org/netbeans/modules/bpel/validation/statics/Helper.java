/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.validation.statics;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BaseCorrelation;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CreateInstanceActivity;
import org.netbeans.modules.bpel.model.api.Empty;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.Link;
import org.netbeans.modules.bpel.model.api.LinkContainer;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.Source;
import org.netbeans.modules.bpel.model.api.SourceContainer;
import org.netbeans.modules.bpel.model.api.Target;
import org.netbeans.modules.bpel.model.api.TargetContainer;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.impl.Utils;
import org.netbeans.modules.bpel.model.impl.Utils.Pair;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.bpel.validation.core.Outcome;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public final class Helper {
    
    public Helper(Validator validator) {
        myValidator = validator;
    }
    
    public void checkNotificationPortType( BpelEntity bpelEntity,
            WSDLReference<PortType> portType) {
        if ( portType == null || portType.get() == null){
            return;
        }
        Collection<Operation> operations = portType.get().getOperations();
        for(Operation operation: operations) {
            if(operation instanceof NotificationOperation) {
                addError( FIX_WSDLOPERATION_SOLICIT_RESPONSE_NOTIFICATION, bpelEntity );
            }
        }
    }
    
    public void checkSolicitResponsePortType( BpelEntity bpelEntity,
            WSDLReference<PortType> portType) {
        if ( portType == null || portType.get() == null){
            return;
        }
        Collection<Operation> operations = portType.get().getOperations();
        for(Operation operation: operations) {
            if(operation instanceof SolicitResponseOperation) {
                addError( FIX_WSDLOPERATION_SOLICIT_RESPONSE_NOTIFICATION , bpelEntity );
            }
        }
    }
    
    public void checkOverloadedPortTypeOperation( BpelEntity bpelEntity,
            WSDLReference<PortType> portType) {
        if(portType == null || portType.get()==null)
            return;
        
        Collection<Operation> operations = portType.get().getOperations();
        Set<String> operationsSet = new HashSet<String>();
        
        for(Operation operation: operations) {
            operationsSet.add(operation.getName());
        }
        
        if(operationsSet.size() != operations.size()) {
            addError( FIX_PORT_TYPE_OVERLOADED_OPERATION_NAME, bpelEntity );
        }
    }
    
    public void checkInputVariableToPartCombination(Invoke invoke) {
        if(invoke.getInputVariable() != null)
            if(invoke.getToPartContaner()!=null && 
                    invoke.getToPartContaner().sizeOfToParts()!=0) 
            {
                addError( FIX_INPUTVARIABLE_TOPART_COMBINATION , invoke );
            }
    }
    
    public void checkOutputVariableFromPartCombination(Invoke invoke) {
        if(invoke.getOutputVariable() != null)
            if(invoke.getFromPartContaner()!=null && 
                    invoke.getFromPartContaner().sizeOfFromParts()!=0) 
            {
                addError( FIX_OUTPUTVARIABLE_FROMPART_COMBINATION, invoke );
            }
    }
    
    public void checkValidPartAttributeFromPartElement(Invoke invoke) {
        // Only if one or more <fromPart> elements is defined.
        if(invoke.getFromPartContaner()!=null && 
                invoke.getFromPartContaner().sizeOfFromParts()!=0) 
        {
            // For each <fromPart> element.
            FromPart[] fromParts = invoke.getFromPartContaner().getFromParts();
            for (FromPart fromPart : fromParts) {
                boolean valid = false;
                
                if(fromPart==null || fromPart.getPart() == null) {
                    return;
                }
                WSDLReference<Part> part = fromPart.getPart();
                
                Part partRef = part.get();
                
                if(partRef != null) {
                    // This will be handled by another rule.
                    if(invoke.getOperation()==null || 
                            invoke.getOperation().get() ==null ||
                            invoke.getOperation().get().getOutput()==null ||
                            invoke.getOperation().get().getOutput().
                                getMessage() == null ||
                            invoke.getOperation().get().getOutput().
                                getMessage().get() == null ||
                            invoke.getOperation().get().getOutput().getMessage().
                                get().getParts() == null  )
                    {
                        return;
                    }
                    for(Part wsdlPart: invoke.getOperation().get().getOutput().
                            getMessage().get().getParts()) 
                    {
                        if(wsdlPart.equals(partRef)) {
                            valid = true;
                            break;
                        }
                    }
                }
                
                if(!valid) {
                    addError( FIX_INVALID_FROMPART_PARTATTR ,fromPart);
                }
            }
        }
    }
    
    public void checkValidPartAttributeToPartElement(Invoke invoke) {
        // Only if one or more <toPart> elements is defined.
        if(invoke.getToPartContaner()!=null && 
                invoke.getToPartContaner().sizeOfToParts()!=0) 
        {
            // For each <toPart> element.
            ToPart[] toParts = invoke.getToPartContaner().getToParts();
            for (ToPart toPart : toParts) {
                boolean valid = false;
                
                if(toPart==null || toPart.getPart() == null){
                    return;
                }
                WSDLReference<Part> part = toPart.getPart();
                
                Part partRef = part.get();
                if(partRef != null) {
                    // This will be handled by another rule.
                    if(invoke.getOperation()==null || invoke.getOperation().
                            get() ==null ||
                            invoke.getOperation().get().getInput()==null ||
                            invoke.getOperation().get().getInput().
                                getMessage() == null ||
                            invoke.getOperation().get().getInput().getMessage().
                                get() == null ||
                            invoke.getOperation().get().getInput().getMessage().
                                get().getParts() == null  )
                    {
                        return;
                    }
                    for(Part wsdlPart: invoke.getOperation().get().getInput().
                            getMessage().get().getParts()) 
                    {
                        if(wsdlPart.equals(partRef)) {
                            valid = true;
                            break;
                        }
                    }
                }
                
                if(!valid) {
                    addError( FIX_INVALID_TOPART_PARTATTR ,toPart);
                }
            }
        }
    }
    
    /**
     * For toPart usage, check whether all parts in the WSDL message are 
     * completely assigned.
     */
    public void checkAnyMissingToPartElementInInvoke(Invoke invoke) {
        
        // This will be handled by another rule.
        if(invoke.getOperation()==null || invoke.getOperation().get() ==null ||
                invoke.getOperation().get().getInput()==null ||
                invoke.getOperation().get().getInput().getMessage() == null ||
                invoke.getOperation().get().getInput().getMessage().get() == null ||
                invoke.getOperation().get().getInput().getMessage().get().
                getParts() == null  ) 
        {
            return;
        } else {
            if(invoke.getToPartContaner()==null || 
                    invoke.getToPartContaner().sizeOfToParts()==0) 
            {
                return;
            }
            
            for(Part wsdlPart: invoke.getOperation().get().getInput().getMessage().
                    get().getParts()) 
            {
                // Each part in the wsdl message must be assigned via a <toPart>
                boolean assigned = false;
                ToPart[] toParts = invoke.getToPartContaner().getToParts();
                for (ToPart toPart : toParts) {
                    // # 84147
                    WSDLReference<Part> partRef = toPart.getPart();
                    if ( partRef == null ){
                        continue;
                    }
                    Part part =  partRef.get();
                    if( part != null && wsdlPart.equals(part)){
                        assigned = true;
                        break;
                    }
                }
                if(!assigned) {
                    addError( FIX_WSDL_MESSAGE_NOT_COMPLETELY_INITIALISED, invoke);
                    break;
                }
            }
        }
    }
    
    public void checkAnyMissingToPartElementInReply(Reply reply) {
        if(reply.getOperation()==null || reply.getOperation().get() ==null ||
                reply.getOperation().get().getOutput()==null ||
                reply.getOperation().get().getOutput().getMessage() == null ||
                reply.getOperation().get().getOutput().getMessage().get() == null ||
                reply.getOperation().get().getOutput().getMessage().get().
                    getParts() == null  ) 
        {
            return;
        } else {
            if(reply.getToPartContaner()==null || 
                    reply.getToPartContaner().sizeOfToParts()==0)
            {
                return;
            }
            
            for(Part wsdlPart: reply.getOperation().get().getOutput().getMessage().
                    get().getParts()) 
            {
                // Each part in the wsdl message must be assigned via a <toPart>
                boolean assigned = false;
                ToPart[] toParts = reply.getToPartContaner().getToParts();

                for (ToPart toPart : toParts) {
                    WSDLReference<Part> partRef = toPart.getPart();
                    if ( partRef == null ){
                        continue;
                    }
                    Part part =  partRef.get();
                    if(part != null && wsdlPart.equals(part)){
                        assigned = true;
                        break;
                    }
                }
                if(!assigned) {
                    addError( FIX_WSDL_MESSAGE_NOT_COMPLETELY_INITIALISED, reply);
                    break;
                }
            }
        }
    }    
    
    public void checkReceiveVariableFromPartCombination(Receive receive) {
        if (receive == null) {
            return;
        }
        if (receive.getVariable() != null) {
            if(receive.getFromPartContaner() != null && 
                    receive.getFromPartContaner().sizeOfFromParts() != 0) 
            {
                addError(FIX_RECEIVE_VARIABLE_FROMPART_COMBINATION, receive);
            }
        }
        // Rule SA00047
        WSDLReference<Operation> operationRef = receive.getOperation();

        if (operationRef == null) {
            return;
        }
        Operation operation = operationRef.get();

        if (operation == null) {
            return;
        }
        if (isAbsentOutputVaribale(receive, operation)) {
            addError(FIX_ABSENT_OUTPUT_VARIABLE, receive);
        }
    }
    
    public void checkOnMessageVariableFromPartCombination(OnMessage onMessage) {
        if(onMessage == null) {
            return;
        }
        
        if(onMessage.getVariable() != null) {
            if(onMessage.getFromPartContaner()!=null && 
                    onMessage.getFromPartContaner().sizeOfFromParts()!=0) 
            {
                addError( FIX_ONMESSAGE_VARIABLE_FROMPART_COMBINATION, onMessage );
            }
        }
    }    
    
    public void checkReplyVariableToPartCombination(Reply reply) {
        if(reply == null) {
            return;
        }
        if(reply.getVariable() != null) {
            if(reply.getToPartContaner()!=null && 
                    reply.getToPartContaner().sizeOfToParts()!=0)
            {
                addError( FIX_REPLY_VARIABLE_TOPART_COMBINATION , reply );
            }
        }
        
        // Rule SA00047
        WSDLReference<Operation> operationRef = reply.getOperation();

        if ( operationRef == null ) {
            return;
        }
        Operation operation = operationRef.get();
        
        if ( operation == null ) {
            return;
        }
        if (isAbsentInputVaribale(reply, operation)) {
            addError(FIX_ABSENT_INPUT_VARIABLE, reply);
        }
    }
    
    void checkPortTypeCombinedPartnerLink( PortTypeReference portTypeReference ) {
        if ( portTypeReference instanceof PartnerLinkReference ){
            PartnerLinkReference partnerLinkReference =
                    (PartnerLinkReference) portTypeReference;
            WSDLReference<PortType> portTypeDirectRef =
                    portTypeReference.getPortType();
            if ( portTypeDirectRef == null ){
                return;
            }
            BpelReference<PartnerLink> partnerLinkRef =
                    partnerLinkReference.getPartnerLink();
            NamedComponentReference<PortType> portTypeRef = Utils.
                    getPortTypeRef( partnerLinkRef , (Component)portTypeReference );
            if ( portTypeRef == null ||
                    !Utils.equals( portTypeDirectRef.get() , portTypeRef.get())) {
                addError( FIX_DIFFERENT_PORT_TYPES , (BpelEntity)portTypeReference );
            }
        }
    }
    
    void addNamedToMap( BpelEntity named, Map<String, Collection<Component>> map  ) {
        addNamedToMap(named, map, DEFAULT_NAME_ACESS);
    }
    
    void addErrorForNamed( Map<String, Collection<Component>> map, String key) {
        for( Entry<String, Collection<Component>> entry : map.entrySet()) {
            String name = entry.getKey();
            assert name != null;
            Collection<Component> collection = entry.getValue();

            if (collection!= null && collection.size() > 1 ) {
                for (Component component : collection) {
                    addError(key, component);
                }
            }
        }
    }
    
    void addNamedToMap( BpelEntity entity, Map<String, Collection<Component>> map, NameAccess access ) {
        String name = access.getName(entity);

        if (name== null) {
            return;
        }
        Collection<Component> collection =  map.get( name );
        
        if ( collection == null ) {
            collection = new LinkedList<Component>();
            map.put( name, collection );
        }
        collection.add( entity );
    }
    
    void addCompensateError(  Activity compensate ) {
        addError( FIX_COMPENSATE_OCCURANCE , compensate, compensate.getPeer().getLocalName());
    }
    
    void checkCompensateOccurance( Activity compensate ) {
        /*
         *  Rule : The <compensate(Scope)> activity MUST only be used from within a faultHandler,
         *  another compensationHandler, or a terminationHandler.
         */
        if ( Utils.hasAscendant( compensate , FaultHandlers.class ) ){
            return;
        }
        if ( Utils.hasAscendant( compensate ,CompensationHandler.class )) {
            return;
        }
        if ( Utils.hasAscendant( compensate , TerminationHandler.class ) ) {
            return;
        }
        addCompensateError(  compensate );
    }
    
    void addNamedActivity( BpelEntity entity, Map<String, Collection<Component>> map ) {
        if ( entity instanceof ExtendableActivity &&
                entity instanceof NamedElement ) {
            String name = ((NamedElement) entity ).getName();
            if ( name == null ) {
                return;
            }
            Collection<Component> collection = map.get(name);
            if ( collection == null ) {
                collection = new LinkedList<Component>();
                map.put(name, collection);
            }
            collection.add( (Component)entity );
        }
    }
    
    void collectIsolatedScopes( BpelContainer container,
            Collection<Component> collection ) {
        List<BpelEntity> children = container.getChildren();
        for (BpelEntity entity : children) {
            if ( entity instanceof Scope && TBoolean.YES.equals(
                    ((Scope) entity).getIsolated() )) {
                collection.add( entity );
            }
            if ( entity instanceof BpelContainer ) {
                collectIsolatedScopes( (BpelContainer)entity, collection );
            }
        }
    }

    void checkOrderOfActivities( CreateInstanceActivity activity ) {
        if ( TBoolean.YES.equals( activity.getCreateInstance())  )
        {
            /* 
             * I will put into this set visited container ( sequence and flow )
             * for avoiding visiting them one more time while following up of tree.
             * 
             * This will fix bug #85727
             */
            Set<CompositeActivity> set = new HashSet<CompositeActivity>();
            ExtendableActivity beforeOrSimultaneously = 
                findPreviouslyPerformedActivities( (Activity) activity , set );
            if ( beforeOrSimultaneously != null ) {
                Collection<Component> collection = new ArrayList<Component>( 2 );
                collection.add( (Activity)activity );
                collection.add( beforeOrSimultaneously );
                addError(FIX_START_ACTIVITY_IS_NOT_FIRST_EXECUTED, collection);
            }
        }
    }

    void visitBaseScope( BaseScope baseScope ) {
        /*
         * Rule : The name of a named activity MUST be unique amongst
         * all named activities present within the same immediately
         * enclosing scope. This requirement MUST be statically enforced.
         */
        Map<String,Collection<Component>> map =
                new HashMap<String, Collection<Component>>();
        collectActivitiesInScope( baseScope , map );
        addErrorForNamed( map, FIX_MULTIPLE_NAMED_ACTIVITIES);
        
        /*
         * Rule : If the value of exitOnStandardFault of a <scope> or <process>
         * is set to "yes", then a fault handler that explicitly targets the
         * WS-BPEL standard faults MUST NOT be used in that scope.
         * A process definition that violates this condition MUST be
         * detected and rejected by static analysis.
         */
        if ( TBoolean.YES.equals( baseScope.getExitOnStandardFault() ) &&
                baseScope.getFaultHandlers()!= null ) {
            Catch[] catches = baseScope.getFaultHandlers().getCatches();
            Collection<Component> collection = new LinkedList<Component>();
            for (Catch catc : catches) {
                QName qName = catc.getFaultName();
                if ( qName!= null && BpelEntity.BUSINESS_PROCESS_NS_URI.equals(
                        qName.getNamespaceURI() ) ) {
                    /*
                     *  suggest that all qnames in bpws namespace are standart
                     *  faults ( may be this is wrong sugestion )
                     */
                    collection.add( catc );
                }
            }
            if ( collection.size() >0 ){
                addError(FIX_EXIT_ON_STANDART_FAULT, collection);
            }
        }
    }
    
    void collectActivitiesInScope( BpelContainer container, Map<String,Collection<Component>> map ) {
        List<BpelEntity> children = container.getChildren();
        for (BpelEntity entity : children) {
            addNamedActivity( entity, map );
            if ( entity instanceof BaseScope ) {
                // we do not need to go further in scope for searching activities with duplicate names.
                continue;
            }
            if ( entity instanceof BpelContainer ) {
                collectActivitiesInScope( (BpelContainer)entity, map);
            }
        }
    }
    
    void checkImportType( Import imp ) {
        String importType = imp.getImportType();
        if ( !Import.WSDL_IMPORT_TYPE.equals( importType) &&
                !Import.SCHEMA_IMPORT_TYPE.equals( importType ) ) {
            addError( FIX_BAD_IMPORT_TYPE , imp );
        }
    }
    
    void checkInstantiableActivities( Process process ) {
        Collection<Activity> collection = getInstantiableActivities( process );
        if ( collection.size() ==0 ){
                addError( FIX_NO_PICK_OR_RECEIVE_WITH_CREATE_INSTANCE , process );
        }
        
        if ( collection.size() >0 ) {
            checkCorellations( collection );
        }
    }

    void checkMessageType( OnEvent onEvent ) {
        WSDLReference<Message> messageRef = onEvent.getMessageType();
        WSDLReference<Operation> operationRef = onEvent.getOperation();
        if ( operationRef == null || operationRef.get()==null ){
            return;
        }
        Operation operation = operationRef.get();
        if ( messageRef!= null){
            if ( !checkMessageTypeInOnEvent( messageRef, operation ) ){
                addError( FIX_MESSAGE_TYPE_IN_ON_EVENT , onEvent );
            }
        } else {
            SchemaReference<GlobalElement> elementRef = onEvent.getElement();
            if ( elementRef == null ){
                // do not need to do anything. Both messageType and element attributes could be absent.
                return;
            }
            if ( !checkElementInOnEvent( elementRef, operation) ){
                addError( FIX_ELEMENT_IN_ON_EVENT , onEvent );
            }
        }
    }
    
    void checkLinks( Flow flow ) {
        LinkContainer linkContainer = flow.getLinkContainer();
        if ( linkContainer == null ){
            return;
        }
        Link[] links = linkContainer.getLinks();
        Set<Link> list = new HashSet<Link>( Arrays.asList( links) );
        List<BpelEntity> children = flow.getChildren();
        
        Map<Link,Collection<Component>> sources =
                new HashMap<Link,Collection<Component>>();
        
        Map<Link,Collection<Component>> targets =
                new HashMap<Link,Collection<Component>>();
        
        for (BpelEntity child : children) {
            collectLinks( child , list , sources , targets );
        }
        
        Map<Pair<Component>,Collection<Link>> foundSourcesAndTargets =
                new HashMap<Pair<Component>,Collection<Link>>();
        
        for( Link link : list ){
            boolean isUsed = false;
            Collection<Component> collection = sources.get( link );
            isUsed =  checkLink(link, collection , FIX_MILTIPLE_LINK_SOURCE );
            Component source = null;
            if ( isUsed ) {
                source = collection.iterator().next();
            }
            
            collection = targets.get( link );
            if ( !isUsed ||
                    !checkLink(link, collection , FIX_MILTIPLE_LINK_TARGET ) ) {
                addError( FIX_LINK_IS_NOT_USED , link );
            } else {
                Component target = collection.iterator().next();
                /*
                 * Here we perform one more check. It will be started only when
                 * link appear between  source and target ( from previous check )
                 * for just ONE activity as source and ONE activity as target.
                 */
                checkLinkSingleton( foundSourcesAndTargets, link, source, target);
                
                checkLinkBoundaries( link , (BpelEntity)source ,
                        (BpelEntity)target );
            }
        }
    }
    
    String getTargetNamespace( Import imp ) {
        assert imp!= null;
        String location = imp.getLocation();
        if ( location == null ) {
            return null;
        }
        try {
            URI uri = new URI( location );
            ModelSource source = CatalogModelFactory.getDefault().
                    getCatalogModel( imp.getModel().getModelSource())
                    .getModelSource(uri, imp.getModel().getModelSource());
            if ( Import.WSDL_IMPORT_TYPE.equals( imp.getImportType()) ){
                WSDLModel model = WSDLModelFactory.getDefault().getModel(
                        source );
                if (model == null) {
                  return null;
                }
                return model.getDefinitions()==null? null :
                    model.getDefinitions().getTargetNamespace();
            } else if (Import.SCHEMA_IMPORT_TYPE.equals( imp.getImportType()) ){
                SchemaModel model = SchemaModelFactory.getDefault().getModel(
                        source );
                if (model == null) {
                  return null;
                }
                if (model.getState() == Model.State.VALID) {
                    return model.getSchema()==null? null :
                        model.getSchema().getTargetNamespace();
                }
            }
        } catch( URISyntaxException e ) {
            return null;
        } catch (CatalogModelException e) {
            return null;
        }
        return null;
    }
    
    void checkInputOutputVariableOperation( Invoke invoke ) {
        WSDLReference<Operation> operationRef = invoke.getOperation();
        if ( operationRef == null ) {
            return;
        }
        Operation operation = operationRef.get();

        if (operation instanceof RequestResponseOperation) {
            if (isAbsentInputVaribale(invoke, operation) || isAbsentOutputVaribale(invoke, operation)) {
                addError(FIX_ABSENT_INPUT_OUTPUT_VARIABLES, invoke);
            }
            else {
                /*
                 *  only if all is ok we start the next check - equality of
                 *  variable types and message type 
                 */ 
                checkInputVariable( invoke , operation );
                checkOutputVariable( invoke , operation );
            }
        }
        else if (operation instanceof OneWayOperation)
        {
            if (invoke.getOutputVariable() != null ||
             (invoke.getFromPartContaner() != null && invoke.getFromPartContaner().sizeOfFromParts() > 0))
            {
                addError(FIX_OUTPUT_VARIABLE_FOR_ONE_WAY_OP, invoke);
            }           
            if (isAbsentInputVaribale(invoke, operation)) {
                addError(FIX_ABSENT_INPUT_VARIABLE_FOR_ONE_WAY_OP, invoke);
            }
            else {
                /*
                 *  only if all is ok we start the next check - equality of
                 *  variable types and message type 
                 */ 
                checkInputVariable( invoke , operation );
            }
        }
    }

    // vlv
    private void addError(String key, Collection<Component> collection) {
      for (Component component : collection) {
        addError(key, component);
      }
    }

    private void addError(String key, Component component) {
      getValidator().addError(key, component);
    }

    private void addError(String key, Component component, String param) {
      getValidator().addError(key, component, param);
    }

    private void addError(String key, Component component, String param1, String param2) {
//out("add error: " + key + " " + param1 + " " + param2);
      getValidator().addError(key, component, param1, param2);
    }

    private void addWarning(String key, Component component) {
      getValidator().addWarning(key, component);
    }

    void checkFCTScope( BpelContainer container ) {
        Collection<Scope> scopes = getScopes( container );
        for (Scope scope : scopes) {
            if ( scope.getCompensationHandler()!= null ){
                Collection<Component> collection =
                        new ArrayList<Component>( 2 );
                collection.add( container );
                collection.add( scope );
                addError(FIX_SCOPE_INSIDE_FCT_CONTAINS_COMPENSATION_HANDLER, collection);
            }
        }
    }
    
    void checkPropertyAliasMultiplicity( Process process ) {
        Collection<PropertyAlias> aliases = 
            getPropertyAliases( null , null, process.getBpelModel() );
        Set<Pair<QName>> qNames = new HashSet<Pair<QName>>(); // # 80412
        for (PropertyAlias alias : aliases) {
            NamedComponentReference<CorrelationProperty> propRef = 
                alias.getPropertyName();
            NamedComponentReference<Message> messageRef = alias.getMessageType();
            if ( propRef!= null && messageRef != null ) {
                QName name = propRef.getQName();
                QName forCheckName = new QName( name.getNamespaceURI() , 
                        name.getLocalPart());
                QName message = messageRef.getQName();      // # 80412
                QName forCheckMessage = new QName (message.getNamespaceURI(),
                        message.getLocalPart() );
                Pair<QName> pair = new Pair<QName>( forCheckName , 
                        forCheckMessage);                   // # 80412
                if ( qNames.contains( pair )) {
                    addError( FIX_MULTIPLE_PROPERTY_ALIAS_FOR_PROPERTY , 
                            process );
                }
                else {
                    qNames.add( pair );
                }
            }
        }
    }
    
    void checkPropertyUsageInInputMessage( OperationReference reference, BaseCorrelation[] correlations) {
        if ( correlations.length == 0) {
            return;
        }
        WSDLReference<Operation> operationRef = reference.getOperation();
        if ( operationRef == null ) {
            return;
        }
        Operation operation = operationRef.get();
        if ( operation == null ) {
            return;
        }
        Message message = getInputMessage( operation );
        if ( message == null ) {
            return;
        }
        for (BaseCorrelation correlation : correlations) {
            boolean flag = true;
            if ( correlation instanceof PatternedCorrelation ){
                Pattern pattern = ((PatternedCorrelation)correlation).getPattern();
                flag = Pattern.isRequestApplicable( pattern );
            }
            if ( flag ) {
                checkPropertyList(correlation, message );
            }
        }
    }
    
    void checkPropertyUsageInOutputMessage( OperationReference reference, 
            BaseCorrelation[] correlations ) 
    {
        if ( correlations.length == 0) {
            return;
        }
        WSDLReference<Operation> operationRef = reference.getOperation();
        if ( operationRef == null ) {
            return;
        }
        Operation operation = operationRef.get();
        if ( operation == null ) {
            return;
        }
        Message message = getOutputMessage( operation );
        if ( message == null ) {
            return;
        }
        for (BaseCorrelation correlation : correlations) {
            boolean flag = true;
            if ( correlation instanceof PatternedCorrelation ){
                Pattern pattern = ((PatternedCorrelation)correlation).getPattern();
                flag = Pattern.isResponseApplicable( pattern );
            }
            if ( flag ) { 
                checkPropertyList(correlation, message );
            }
        }
    }
    
    void checkVariableContainer( VariableContainer container ) {
        /*
         * Rule : The name of a variable MUST be unique amongst the names of all
         * variables defined within the same immediately enclosing scope. This
         * requirement MUST be statically enforced.
         */
        Variable[] variables = container.getVariables();
        if (variables == null) {
            return;
        }

        Map<String, Collection<Component>> map = new HashMap<String, Collection<Component>>();

        for (Variable variable : variables) {
            addNamedToMap(variable, map);
        }

        addErrorForNamed(map, FIX_DUPLICATE_VARIABLE_NAME);

    }

    void checkImplicitlyDeclaredVars( OnEvent onEvent ) {
        FromPartContainer parts = onEvent.getFromPartContaner();
        if (parts == null || parts.getFromParts()==null) {
            return;
        }
        /*
         * This map is used for collecting duplicate variable declarations in
         * fromParts.
         */
        Map<String, Collection<Component>> map = new HashMap<String, Collection<Component>>();
        /*
         * This map is used for collecting all fromParts names. It will be used
         * for checking absence of duplicate varaible names.
         */
        Map<String, Component> mapVarsInScope = new HashMap<String, Component>();
        for (VariableDeclaration decl : parts.getFromParts()) {
            addNamedToMap(decl, map, LazyHolder.VAR_DECL_NAME_ACCESS);
            mapVarsInScope.put(decl.getVariableName(), decl);
        }

        addNamedToMap(onEvent, map, LazyHolder.VAR_DECL_NAME_ACCESS);
        mapVarsInScope.put(onEvent.getVariableName(), onEvent);

        Scope scope = onEvent.getScope();
        if (scope != null && scope.getVariableContainer() != null) {
            VariableContainer container = scope.getVariableContainer();
            Variable[] variables = container.getVariables();
            for (Variable variable : variables) {
                String name = variable.getName();
                if (mapVarsInScope.containsKey(name)) {
                    addNamedToMap(variable, map,
                            LazyHolder.VAR_DECL_NAME_ACCESS);
                }
            }
        }

        addErrorForNamed(map, FIX_DUPLICATE_VARIABLE_NAME_ON_EVENT);
    }
    
    private void checkCorellations( Collection<Activity> collection ) {
        Set<CorrelationSet> sharedCorrelations = new HashSet<CorrelationSet>();
        boolean first = true;
        Collection<Component> components = new ArrayList<Component>( 
                collection.size() );
        for (Activity activity : collection) {
            components.add( activity );
            Collection<CorrelationSet> correlations = 
                getJoinedCorrelationSets( activity );
            if ( first ) {
                first = false;
                sharedCorrelations.addAll( correlations );
            }
            else {
                sharedCorrelations.retainAll( correlations );
                checEmptySet( sharedCorrelations , components );
            }
        }
    }

    private void checEmptySet( Set<CorrelationSet> sharedCorrelations, 
            Collection<Component> components ) 
    {
        if ( sharedCorrelations.size() ==0 ) {
            addError( FIX_ABSENT_SHARED_JOINED_CORRELATION_SET, components );
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<CorrelationSet> getJoinedCorrelationSets( 
            Activity activity )
    {
        if ( activity.getElementType().equals( Receive.class )) {
            Receive receive = (Receive)activity;
            return getJoinedCorrelationSets(receive.getCorrelationContainer());
        }
        else if ( activity.getElementType().equals( Pick.class ) ) {
            Pick pick = (Pick)activity;
            OnMessage[] onMessages = pick.getOnMessages();
            Collection<CorrelationSet> collection = null;
            for (OnMessage onMessage : onMessages) {
                if ( collection == null ) {
                    collection = getJoinedCorrelationSets(
                            onMessage.getCorrelationContainer());
                }
                else {
                    collection.addAll( getJoinedCorrelationSets(
                            onMessage.getCorrelationContainer() ));
                }
            }
            // # 83773
            if ( collection == null ) {
                collection = Collections.EMPTY_LIST;
            }
            return collection;
        }
        return Collections.EMPTY_LIST;
    }

    private void checkPropertyList( BaseCorrelation correlation , Message message) {
        BpelReference<CorrelationSet> setRef = correlation.getSet();
        if ( setRef == null ) {
            return;
        }
        CorrelationSet set = setRef.get();

        if ( set == null ) {
            return;
        }
        List<WSDLReference<CorrelationProperty>> list = set.getProperties();
        if ( list == null ) {
            return; // # 80696
        }
        for (WSDLReference<CorrelationProperty> reference : list) {
            if ( reference == null ) {
                continue;
            }
            Collection<PropertyAlias> collection = getPropertyAliases(reference.getQName(), message, correlation.getBpelModel());

            if (collection.size() == 0) {
                addError("FIX_AbsentPropertyAliasForMessage", correlation, reference.get().getName(), set.getName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<PropertyAlias> getPropertyAliases(QName name, Message message, BpelModel model) {
        Import[] imports = model.getProcess().getImports();
        if ( imports.length == 0 ) {
            return Collections.EMPTY_SET;
        }
        else {
            Set<PropertyAlias> list = new HashSet<PropertyAlias>();
            for ( Import imp : imports ) {
                collectPropertyAliases( name , message , imp , list );
            }
            return list;
        }
    }

    private void collectPropertyAliases( QName name, Message message, Import imp, Set<PropertyAlias> list) {
        WSDLModel model = ImportHelper.getWsdlModel(imp);

        if (model == null) {
            return;
        }
        if ( model.getState() != State.VALID ) {
            return;
        }
        List<PropertyAlias> properties = 
            model.getDefinitions().getExtensibilityElements( PropertyAlias.class );
        for (PropertyAlias alias : properties) {
            NamedComponentReference<CorrelationProperty> propRef = 
                alias.getPropertyName();
            if ( propRef == null ) {
                continue;
            }
            if ( name!= null && !Utils.equals( propRef.getQName() , name )){
                continue;
            }
            if ( message == null ) {
                list.add(alias);
                continue;
            }
            NamedComponentReference<Message> messageRef = alias.getMessageType();
            if (messageRef == null) {
                continue;
            }
            else if (messageRef.references(message)) {
                list.add(alias);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<CorrelationSet> getJoinedCorrelationSets( 
            CorrelationContainer container ) 
    {
        Collection<CorrelationSet> collection = new LinkedList<CorrelationSet>();
        if ( container == null ) {
            return Collections.EMPTY_LIST;
        }
        Correlation[] correlations = container.getCorrelations();
        for (Correlation correlation : correlations) {
            if (Initiate.JOIN.equals(correlation.getInitiate())) {
                BpelReference<CorrelationSet> setRef = correlation.getSet();
                if ( setRef!= null && setRef.get()!= null ) {
                    collection.add( setRef.get() );
                }
            }
        }
        return collection;
    }

    private Message getInputMessage( Operation operation  ) {
        Input input = operation.getInput();
        if ( input == null ){
            return null;
        }
        NamedComponentReference<Message> messageRef = input.getMessage();
        if ( messageRef == null ) {
            return null;
        }
        return messageRef.get();
    }
    
    private Message getOutputMessage( Operation operation  ) {
        Output output = operation.getOutput();
        if ( output == null ){
            return null;
        }
        NamedComponentReference<Message> messageRef = output.getMessage();
        if ( messageRef == null ) {
            return null;
        }
        return messageRef.get();
    }

    private void checkInputVariable( Invoke invoke, Operation operation ) {
        BpelReference<VariableDeclaration> varRef = invoke.getInputVariable();
        Message message = getInputMessage(operation);
        checkVariable(invoke, varRef, message);
    }
    
    private void checkOutputVariable( Invoke invoke, Operation operation ) {
        BpelReference<VariableDeclaration> varRef = invoke.getOutputVariable();
        Message message = getOutputMessage(operation);
        checkVariable(invoke, varRef, message);
    }
    
    private void checkVariable( Invoke invoke, 
            BpelReference<VariableDeclaration> varRef , Message message ) 
    {
        if ( varRef == null ) {
            return;
        }
        VariableDeclaration variable = varRef.get();
        if ( variable == null ) {
            return;
        }
        if ( message == null ) {
            return;
        }
        WSDLReference<Message> messageRef = variable.getMessageType();
        if ( messageRef!= null) {
            if ( !messageRef.references(message) ) {
                addError( FIX_BAD_VARIABLE_MESSAGE_TYPE , invoke );
            }
        }
        else if ( variable.getElement()!= null )
        {
            SchemaReference<GlobalElement> varElement = variable.getElement();
            if ( !checkElementType(message, varElement) ) {
                addError( FIX_BAD_VARIABLE_ELEMENT_TYPE , invoke );
            }
        }
    }

    private boolean checkElementType( Message message, 
            SchemaReference<GlobalElement> varElement ) 
    {
        if (message.getParts().size() != 1) {
            return false;
        }
        Part part = message.getParts().iterator().next();
        NamedComponentReference<GlobalElement> elementRef = part.getElement();
        if (elementRef == null) {
            return false;
        }
        GlobalElement element = elementRef.get();
        if (element == null) {
            return false;
        }
        if (varElement.references(element)) {
            return true;
        }
        return false;
    }
    
    private boolean isAbsentInputVaribale(Invoke invoke, Operation operation) {
        Message message = getInputMessage(operation);

        if (message == null) {
            return false;
        }
        // # 109292
        if (message.getParts().size() == 0 && invoke.getInputVariable() != null) {
          addWarning("FIX_MentionedInputVariableForOneWayOp", invoke); // NOI18N
          return false;
        }
        if (message.getParts().size() != 0 &&
             invoke.getInputVariable() == null &&
            (invoke.getToPartContaner() == null ||
             invoke.getToPartContaner().sizeOfToParts() == 0))
        {
            return true;
        }
        else if ( message.getParts().size() == 0 ) {
            return invoke.getToPartContaner() == null || 
                invoke.getToPartContaner().sizeOfToParts() == 0;
        }
        return false;
    }
    
    private boolean isAbsentInputVaribale(Reply reply ,Operation operation) {
        Message message = getInputMessage(operation);

        if (message == null) {
            return false;
        }
        // # 109292
        if (message.getParts().size() == 0 && reply.getVariable() != null) {
          addWarning("FIX_MentionedInputVariable", reply); // NOI18N
          return false;
        }
        if (message.getParts().size() != 0 &&
              reply.getVariable() == null &&
             (reply.getToPartContaner() == null ||
              reply.getToPartContaner().sizeOfToParts() == 0))
        {
            return true;
        }
        else if ( message.getParts().size() == 0 ) {
            return reply.getToPartContaner() == null || 
                    reply.getToPartContaner().sizeOfToParts() == 0;
        }
        return false;
    }
    
    private boolean isAbsentOutputVaribale(Invoke invoke, Operation operation) {
        Message message = getOutputMessage(operation);

        if (message == null) {
            return false;
        }
        // # 109292
        if (message.getParts().size() == 0 && invoke.getOutputVariable() != null) {
          addWarning("FIX_MentionedInputOutputVariables", invoke); // NOI18N
          return false;
        }
        if (message.getParts().size() != 0 &&
             invoke.getOutputVariable() == null &&
            (invoke.getFromPartContaner() == null ||
             invoke.getFromPartContaner().sizeOfFromParts() == 0))
        {
            return true;
        }
        else if ( message.getParts().size() == 0 ) {
            return  invoke.getFromPartContaner() == null ||
                invoke.getFromPartContaner().sizeOfFromParts() == 0;
        }
        return false;
    }

    private boolean isAbsentOutputVaribale(Receive receive, Operation operation) {
        Message message = getOutputMessage(operation);

        if (message == null) {
            return false;
        }
        // # 109292
        if (message.getParts().size() == 0 && receive.getVariable() != null) {
          addWarning("FIX_MentionedOutputVariable", receive); // NOI18N
          return false;
        }
        if (message.getParts().size() != 0 &&
            receive.getVariable() == null &&
           (receive.getFromPartContaner() == null ||
            receive.getFromPartContaner().sizeOfFromParts() == 0))
        {
            return true;
        }
        else if (message.getParts().size() == 0) {
            return receive.getFromPartContaner() == null ||
                   receive.getFromPartContaner().sizeOfFromParts() == 0;
        }
        return false;
    }
    
    private boolean checkElementInOnEvent( SchemaReference<GlobalElement> ref,
            Operation operation ) {
        GlobalElement element = ref.get();
        if ( element == null ){
            return false;
        }
        NamedComponentReference<Message>  messageOperation = getMessageRef(
                operation);
        if (messageOperation == null) {
            return false;
        }
        Message message = messageOperation.get();
        if (message == null) {
            return false;
        }
        Collection<Part> parts = message.getParts();
        if ( parts.size() != 1){
            return false;
        }
        Part part = parts.iterator().next();
        NamedComponentReference<GlobalElement> elementOperationRef =
                part.getElement();
        if ( elementOperationRef == null ){
            return false;
        }
        return elementOperationRef.references( element );
    }
    
    private boolean checkMessageTypeInOnEvent( WSDLReference<Message> messageRef,
            Operation operation ) {
        Message message = messageRef.get();
        if ( message == null ){
            return false;
        }
        
        NamedComponentReference<Message>  messageOperation = getMessageRef(
                operation);
        if (messageOperation == null) {
            return false;
        }
        return messageOperation.references( message );
    }
    
    private NamedComponentReference<Message> getMessageRef( Operation operation ){
        Input input = operation.getInput();
        if (input == null) {
            return null;
        }
        return input.getMessage();
    }
    
    private Collection<Scope> getScopes( BpelContainer container ) {
        Collection<Scope> collection = new LinkedList<Scope>();
        collectScopes( container , collection );
        return collection;
    }
    
    private void collectScopes( BpelEntity container,
            Collection<Scope> collection ) {
        if ( container instanceof Scope ){
            collection.add( (Scope)container );
        } else {
            List<BpelEntity> children = container.getChildren();
            for (BpelEntity child : children) {
                collectScopes( child , collection );
            }
        }
    }
    
    private void checkLinkSingleton( Map<Pair<Component>, Collection<Link>> map,
            Link link, Component source, Component target ) {
        Pair<Component> pair = new Pair<Component>( source , target);
        Collection<Link> linkCollection = map.get( pair );
        if ( linkCollection == null ) {
            linkCollection = new HashSet<Link>();
            map.put( pair, linkCollection);
        }
        if ( linkCollection.size() > 0 ) {
            Collection<Component> components =
                    new LinkedList<Component>( linkCollection );
            components.add( link );
            components.add( source );
            components.add( target );
            addError( FIX_MULTIPLE_LINKS_WITH_SAME_SOURCE_AND_TARGET ,
                    components );
        } else {
            linkCollection.add( link );
        }
    }
    
    private boolean checkLink( Link link, Collection<Component> collection ,
            String bundleKey ) {
        if ( collection != null && collection.size()>1 ) {
            collection.add( link );
            addError( bundleKey , collection );
        }
        return collection!= null && collection.size()>0;
    }
    
    private Collection<Activity> getInstantiableActivities( 
            BpelContainer container ) 
    {
        Collection<Activity> collection = new LinkedList<Activity>();
        collectInstantiableActivities(container, collection);
        return collection;
    }
    
    private void collectInstantiableActivities( BpelContainer container,
            Collection<Activity> collection )
    {
        List<BpelEntity> list = container.getChildren();
        for (BpelEntity entity : list) {
            if (entity.getElementType().equals(Receive.class)
                    || entity.getElementType().equals(Pick.class))
            {
                TBoolean isInstance = ((CreateInstanceActivity) entity)
                        .getCreateInstance();
                if (TBoolean.YES.equals(isInstance)) {
                    collection.add((Activity) entity);
                }
            }
            else if (entity instanceof BpelContainer) {
                collectInstantiableActivities((BpelContainer) entity,
                        collection);
            }
        }
    }
    
    private void collectLinks( BpelEntity entity, Set<Link> set ,
            Map<Link,Collection<Component>> sourcesMap,
            Map<Link,Collection<Component>> targetsMap) {
        if ( entity instanceof Activity ){
            collectLinkInTargets( (Activity) entity , set, targetsMap );
            collectLinkInSources( (Activity) entity , set, sourcesMap );
        }
        
        List<BpelEntity> children = entity.getChildren();
        for (BpelEntity child : children) {
            collectLinks( child , set , sourcesMap , targetsMap );
        }
    }
    
    private void collectLinkInTargets( Activity activity , Set<Link> set,
            Map<Link,Collection<Component>> targetsMap) {
        TargetContainer targetContainer = activity.getTargetContainer();
        if ( targetContainer!= null ){
            Target[] targets = targetContainer.getTargets();
            for (Target target : targets) {
                BpelReference<Link> ref = target.getLink();
                collectLinks(activity, set, targetsMap, ref );
            }
        }
    }
    
    private void collectLinkInSources( Activity activity , Set<Link> set,
            Map<Link,Collection<Component>> sourcesMap ) {
        SourceContainer sourceContainer = activity.getSourceContainer();
        if ( sourceContainer!= null ){
            Source[] sources = sourceContainer.getSources();
            for (Source source : sources) {
                BpelReference<Link> ref = source.getLink();
                collectLinks(activity, set, sourcesMap, ref );
            }
        }
    }
    
    private void collectLinks( Activity activity, Set<Link> set,
            Map<Link, Collection<Component>> targetsMap,
            BpelReference<Link> reference ) {
        if ( reference == null ){
            return;
        }
        Link link = reference.get();
        if ( set.contains( link )) {
            Collection<Component> collection = targetsMap.get( link );
            if (collection == null) {
                collection = new LinkedList<Component>();
                targetsMap.put( link, collection);
            }
            collection.add(activity);
        }
    }
    
    private void checkLinkBoundaries( Link link, BpelEntity source,
            BpelEntity target ) {
        checkFTBoundaries(link, source, target);
        
        checkRepeatableConstract(link, source, target);
    }
    
    private void checkRepeatableConstract( Link link, BpelEntity source,
            BpelEntity target ) {
        /*
         * Rule :A link MUST NOT cross the boundary of a repeatable construct or
         * the <compensationHandler> element. This means, a link used within a
         * repeatable construct (<while>, <repeatUntil>, <forEach>, <eventHandlers>)
         * or a <compensationHandler>  MUST be declared in a <flow> that is itself
         * nested inside the repeatable construct or <compensationHandler>.
         */
        BpelContainer flow = link.getParent().getParent();
        Class[] containers = new Class[]{
            While.class, RepeatUntil.class,
            ForEach.class, EventHandlers.class, CompensationHandler.class
        };
        boolean targetInside = getContainer( target , flow , containers ) != null;
        boolean sourceInside = getContainer( source , flow, containers ) != null;
        Collection<Component> collection = new ArrayList<Component>(3);
        if ( targetInside ){
            collection.add( target );
        }
        if ( sourceInside ) {
            collection.add( source );
        }
        if ( collection.size() >0 ){
            collection.add( link );
            addError(FIX_LINK_CROSS_BOUNDARY_REPEATABLE_CONSTRUCT, collection);
        }
    }
    
    private void checkFTBoundaries( Link link, BpelEntity source,
            BpelEntity target ) {
        BpelContainer flow = link.getParent().getParent();
        /*
         * Rule : A link that crosses a <faultHandlers> or <terminationHandler>
         * element boundary MUST be outbound only, that is, it MUST have its
         * source activity within the <faultHandlers> or <terminationHandler>,
         * and its target activity outside of the scope associated with the handler.
         *
         */
        boolean badHandlersBoundaries = true;
        Class[] containers = new Class[]{ FaultHandlers.class,
        TerminationHandler.class };
        BpelContainer targetParentHandler = getContainer( target , flow,
                containers );
        
        if ( targetParentHandler == null ||
                hasParent( source , targetParentHandler , flow) ) {
            // source should be inside the same FT container as target
            // otherwise link will be inbound
            badHandlersBoundaries = false;
        }
        if ( !badHandlersBoundaries ){
            BpelContainer sourceParentHandler = getContainer( source , flow ,
                    containers );
            if ( sourceParentHandler!= null ){
                BpelContainer scope = sourceParentHandler.getParent();
                if ( hasParent( target , scope , flow) &&
                        !hasParent( target, sourceParentHandler, flow )) {
                    badHandlersBoundaries = true;
                }
            }
        }
        if ( badHandlersBoundaries ){
            Collection<Component> collection = new ArrayList<Component>(3);
            collection.add( target );
            collection.add( source );
            collection.add( link );
            addError(FIX_BAD_HANDLERS_LINK_BOUNDARIES, collection);
        }
    }
    
    @SuppressWarnings("unchecked")
    private BpelContainer getContainer( BpelEntity child, BpelContainer parent,
            Class...classes ) {
        BpelContainer container = child.getParent();
        while( container!= null && !container.equals( parent) ){
            for (Class clazz : classes) {
                if ( clazz.isAssignableFrom( container.getClass())){
                    return container;
                }
            }
            container = container.getParent();
        }
        return null;
    }
    
    private boolean hasParent( BpelEntity entity , BpelContainer container,
            BpelContainer parent ) {
        BpelEntity child = entity;
        while( child!= null && child!=parent ){
            if ( child == container ){
                return true;
            }
            child= child.getParent();
        }
        return false;
    }

    private ExtendableActivity findPreviouslyPerformedActivities( 
            ExtendableActivity activity , Set<CompositeActivity> set ) 
    {

        if ( !isAcceptableActivity(activity) ) {
            return activity;
        }
        BpelContainer container = activity.getParent();
        if ( !(container instanceof ExtendableActivity)) {
            return null;
        }
        if ( container instanceof ActivityHolder ) {
            return findPreviouslyPerformedActivities( 
                    (ExtendableActivity)container , set );
        }
        if ( container instanceof CompositeActivity ) {
            ExtendableActivity found = findExecutableActivity( 
                    (CompositeActivity)container , activity , set );
            
            if ( found == null ){
                found = findPreviouslyPerformedActivities( 
                        (ExtendableActivity)container , set );
            }
            return found;
        }
        return null;
    }

    private ExtendableActivity findExecutableActivity( CompositeActivity container, 
            ExtendableActivity activity , Set<CompositeActivity> set ) 
    {
        // # 85727
        set.add( container );
        if ( container instanceof Sequence ) {
            Sequence sequence = (Sequence) container;
            int i = sequence.indexOf( ExtendableActivity.class , activity);
            return findExecutableActivityInSequence( sequence , i , set );
        }
        else if ( container instanceof Flow ) {
            return findExecutableActivityInFlow( (Flow) container ,
                    activity , set );
        }
        else {
            assert false;
        }
        return null;
    }

    private ExtendableActivity findExecutableActivityInFlow( Flow flow, 
            ExtendableActivity activity , 
            Set<CompositeActivity> compositeActivities ) 
    {
        Set<ExtendableActivity> set = getLogicallyPreceding( activity );
        ExtendableActivity found = findDescendantActivity(set);
        if ( found!= null ) {
            return found;
        }
        return getUntargetedUnacceptableActivity( flow , compositeActivities );
    }

    private ExtendableActivity getUntargetedUnacceptableActivity( 
            Activity activity , Set<CompositeActivity> set ) 
    {
        /*
         * We are trying to find here unacceptable activity inside flow that 
         * do not have target at all.
         * If there is some activity with target then it precede some other
         * activity ( may be situated inside ascendant flow, not this flow ),
         * so when we appear in appropriate flow we find this preceding
         * activity. If it has "acceptable" activity then all ok, because
         * all following ( by links order ) activity will be after
         * "acceptable". If it does not have acceptable activity then we will
         * find it on this step. 
         */
        List<Activity> children = activity.getChildren( Activity.class );
        for (Activity child : children) {
            if ( set.contains(child) ){
                continue;
            }
            TargetContainer container = child.getTargetContainer();
            if ( container == null || container.getTargets().length==0 ) {
                if ( !isAcceptableActivity( child )) {
                    return child;
                }
                ExtendableActivity found = 
                    getUntargetedUnacceptableActivity( child , set );
                if ( found != null ) {
                    return found;
                }
            }
        }
        return null;
    }

    private ExtendableActivity findDescendantActivity( 
            Set<ExtendableActivity> set ) 
    {
        for (ExtendableActivity preceding : set) {
            ExtendableActivity found = findDescendantActivity( preceding );
            if ( found != null) {
                return found;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Set<ExtendableActivity> getLogicallyPreceding( 
            ExtendableActivity activity  )
    {
        /*
         * This method collect all preceding activities for activity.
         * So resulting set will contain activities that are source 
         * for some links and those links have activity as target.
         * Then we put on the place  "activity" found source activities
         * and search sources for them. And so on.  
         */
        if ( !(activity instanceof Activity)) {
            return Collections.EMPTY_SET;
        }
        Set<ExtendableActivity> set = new HashSet<ExtendableActivity>();
        collectPreceding( (Activity) activity , set );
        return set;
    }

    private void collectPreceding( Activity activity , 
            Set<ExtendableActivity> set) 
    {
        if ( set.contains(activity)) {
            return;
        }
        TargetContainer container = activity.getTargetContainer();
        if ( container == null ) {
            return;
        }
        Target[] targets = container.getTargets();
        for (Target target : targets) {
            BpelReference<Link> linkRef = target.getLink();
            if ( linkRef == null ) {
                continue;
            }
            Link link = linkRef.get();
            if ( link == null ) {
                continue;
            }
            BpelContainer flow = link.getParent().getParent();
            Activity source = findSource( flow, link );
            if ( source!= null ) {
                set.add( source );
                collectPreceding( source , set);
            }
        }
    }

    private Activity findSource( BpelContainer container, Link link ) {
        List<Activity> children = container.getChildren( Activity.class );
        for (Activity child : children) {
            SourceContainer sourceContainer = child.getSourceContainer();
            if ( sourceContainer != null && checkSource(link, sourceContainer) ) {
                    return child;
            }
            if ( child instanceof BpelContainer ) {
                Activity found = findSource( (BpelContainer) child , link );
                if ( found != null ) {
                    return found;
                }
            }
        }
        return null;
    }

    private boolean checkSource( Link link, SourceContainer sourceContainer ) {
        Source[] sources = sourceContainer.getSources();
        for (Source source : sources) {
            BpelReference<Link> linkRef = source.getLink();
            if ( linkRef != null && linkRef.references(link)) {
                return true;
            }
        }
        return false;
    }

    private ExtendableActivity findExecutableActivityInSequence( Sequence sequence, 
            int i , Set<CompositeActivity> set ) 
    {
        ExtendableActivity[] children = sequence.getActivities();
        for  (int j=0; j<i; j++) {
            if ( set.contains( children[j] ) ){
                continue;
            }
            ExtendableActivity found = findDescendantActivity( children[j] );
            if ( found != null ) {
                return found;
            }
        }
        return null;
    }

    private ExtendableActivity findDescendantActivity( ExtendableActivity activity){
        if ( !isAcceptableActivity(activity)) {
            return activity;
        }
        List<ExtendableActivity> children = activity.getChildren( ExtendableActivity.class );
        for (ExtendableActivity child : children) {
            ExtendableActivity found = findDescendantActivity(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    private boolean isAcceptableActivity( ExtendableActivity activity ) {
        if ( activity instanceof CreateInstanceActivity ) {
            if ( TBoolean.YES.equals(
                    ((CreateInstanceActivity) activity).getCreateInstance()) )
            {
                return true;
            }
        }
        Class clazz = activity.getElementType();
        return clazz.equals( Scope.class ) || clazz.equals( Flow.class ) ||
            clazz.equals( Sequence.class ) || clazz.equals( Empty.class );
    }
    
    private Validator getValidator() {
        return myValidator;
    }
    
    static class DefaultNameAccess implements Helper.NameAccess{
        
        public String getName( BpelEntity entity ) {
            if ( entity instanceof NamedElement ) {
                return ((NamedElement)entity).getName();
            }
            return null;
        }
    }
    
    /**
     * This class allow collect instances that needs to be lazy initialized
     * ( this is the safe way to do this if we care about thread-safe ,
     * but may be here we do not need to care about thread-safety ).
     * @author ads
     *
     */
    static final class LazyHolder {
        
        static final Helper.NameAccess SOURCE_LINK_NAME_ACCESS =
                new Helper.NameAccess() 
        {
            public String getName( BpelEntity entity ) {
                if ( entity instanceof Source ) {
                    BpelReference<Link> ref = ((Source)entity).getLink();
                    if ( ref != null && ref.getRefString()!= null) {
                        return ref.getRefString();
                    }
                }
                return null;
            }
        };
        
        static final Helper.NameAccess TARGET_LINK_NAME_ACCESS = new Helper.NameAccess() {
            public String getName( BpelEntity entity ) {
                if ( entity instanceof Target ) {
                    BpelReference<Link> ref = ((Target)entity).getLink();
                    if ( ref != null && ref.getRefString()!= null) {
                        return ref.getRefString();
                    }
                }
                return null;
            }
        };
        
        static final Helper.NameAccess VAR_DECL_NAME_ACCESS = new Helper.NameAccess() {
            public String getName( BpelEntity entity ) {
                if (entity instanceof VariableDeclaration) {
                    return ((VariableDeclaration) entity).getVariableName();
                }
                return null;
            }
        };
    }
    
    static interface NameAccess {
        String getName( BpelEntity entity );
    }
    
    static final String FIX_PORT_TYPE_OVERLOADED_OPERATION_NAME =
            "FIX_PortTypeOverloadedOperationName";                          // NOI18N
    
    static final String FIX_WSDLOPERATION_SOLICIT_RESPONSE_NOTIFICATION =
            "FIX_WSDLOperationSolicitResponseNotification";                 // NOI18N
    
    static final String FIX_INPUTVARIABLE_TOPART_COMBINATION =
            "FIX_InputVariableToPartCombination";                           // NOI18N
    
    static final String FIX_OUTPUTVARIABLE_FROMPART_COMBINATION =
            "FIX_OutputVariableFromPartCombination";                        // NOI18N
    
    static final String FIX_INVALID_FROMPART_PARTATTR =
            "FIX_InvalidFromPartPartAttribute";                             // NOI18N
    
    static final String FIX_INVALID_TOPART_PARTATTR =
            "FIX_InvalidToPartPartAttribute";                               // NOI18N
    
    static final String FIX_RECEIVE_VARIABLE_FROMPART_COMBINATION =
            "FIX_ReceiveVariableFromPartCombination";                       // NOI18N
    
    static final String FIX_REPLY_VARIABLE_TOPART_COMBINATION =
            "FIX_ReplyVariableToPartCombination";                           // NOI18N
    
    static final String FIX_COMPENSATE_OCCURANCE =
            "FIX_CompensateOccurance";                                      // NOI18N
    
    static final String FIX_MULTIPLE_NAMED_ACTIVITIES =
            "FIX_MultipleNamedActivities";                                  // NOI18N
    
    static final String FIX_EXIT_ON_STANDART_FAULT =
            "FIX_ExitOnStandartFault";                                      // NOI18N
    
    static final String FIX_DIFFERENT_PORT_TYPES =
            "FIX_DifferentPortTypes";                                       // NOI18N
    
    static final String FIX_BAD_IMPORT_TYPE =
            "FIX_BadImportType";                                            // NOI18N
    
    static final String FIX_NO_PICK_OR_RECEIVE_WITH_CREATE_INSTANCE =
            "FIX_NoPickReceiveWithCreateInstance";                          // NOI18N
    
    static final String FIX_MILTIPLE_LINK_SOURCE =
            "FIX_MultipleLinkSource";                                       // NOI18N
    
    static final String FIX_MILTIPLE_LINK_TARGET =
            "FIX_MultipleLinkTarget";                                       // NOI18N
    
    static final String FIX_LINK_IS_NOT_USED =
            "FIX_LinkIsNotUsed";                                            // NOI18N
    
    static final String FIX_MULTIPLE_LINKS_WITH_SAME_SOURCE_AND_TARGET =
            "FIX_MultipleLinksWithSameSourceAndTarget";                     // NOI18N
    
    static final String FIX_SCOPE_INSIDE_FCT_CONTAINS_COMPENSATION_HANDLER =
            "FIX_ScopeWithCompenstationHandlerInsideFCT";                   // NOI18N
    
    static final String FIX_MESSAGE_TYPE_IN_ON_EVENT =
            "FIX_MessageTypeInOnEvent";                                     // NOI18N
    
    static final String FIX_ELEMENT_IN_ON_EVENT =
            "FIX_ElementInOnEvent";                                         // NOI18N
    
    static final String FIX_LINK_CROSS_BOUNDARY_REPEATABLE_CONSTRUCT =
            "FIX_LinkCrossBoundaryRepeatableConstract";                     // NOI18N
    
    static final String FIX_BAD_HANDLERS_LINK_BOUNDARIES =
            "FIX_BadHandlersLinkBoundaries";                                // NOI18N
    
    static final String FIX_WSDL_MESSAGE_NOT_COMPLETELY_INITIALISED = 
            "FIX_WSDL_Message_Not_Completely_Initialised";                  // NOI18N
    
    static final String FIX_ONMESSAGE_VARIABLE_FROMPART_COMBINATION =
            "FIX_OnMessage_Variable_FromPart_Combination";                  // NOI18N
    
    static final String FIX_OUTPUT_VARIABLE_FOR_ONE_WAY_OP =
        "FIX_OutputVariableForOneWayOperation";                             // NOI18N
    
    static final String FIX_ABSENT_INPUT_VARIABLE_FOR_ONE_WAY_OP =
        "FIX_AbsentInputVariableForOneWayOp";                               // NOI18N
    
    static final String FIX_ABSENT_INPUT_OUTPUT_VARIABLES =
        "FIX_AbsentInputOutputVariables";                                   // NOI18N
    
    static final String FIX_BAD_VARIABLE_MESSAGE_TYPE =
        "FIX_BadVariableMessageType";                                       // NOI18N
    
    static final String FIX_BAD_VARIABLE_ELEMENT_TYPE =
        "FIX_BadVariableElementType";                                       // NOI18N
    
    static final String FIX_START_ACTIVITY_IS_NOT_FIRST_EXECUTED =
        "FIX_StartActivityHasPreceding";                                    // NOI18N
    
    static final String FIX_ABSENT_SHARED_JOINED_CORRELATION_SET =
        "FIX_AbsentSharedJoinedCorrelationSet";                             // NOI18N
    
    static final String FIX_MULTIPLE_PROPERTY_ALIAS_FOR_PROPERTY =
        "FIX_MultiplePropertyAliasForProperty";                             // NOI18N

    static final String FIX_ABSENT_OUTPUT_VARIABLE = 
        "FIX_AbsentOutputVariable";                                         // NOI18N

    static final String FIX_ABSENT_INPUT_VARIABLE = 
        "FIX_AbsentInputVariable";                                          // NOI18N
    
    static final String FIX_DUPLICATE_VARIABLE_NAME =
           "FIX_DUPLICATE_VARIABLE_NAME";                                   // NOI18N
    
    static final String FIX_DUPLICATE_VARIABLE_NAME_ON_EVENT =
           "FIX_DuplicateVariableNameOnEvent";                              // NOI18N

    private Validator myValidator;
    private static final DefaultNameAccess DEFAULT_NAME_ACESS = new DefaultNameAccess();
}
