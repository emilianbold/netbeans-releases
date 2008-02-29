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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.Extension;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.Link;
import org.netbeans.modules.bpel.model.api.LinkContainer;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.ReThrow;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Requester;
import org.netbeans.modules.bpel.model.api.Responder;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Source;
import org.netbeans.modules.bpel.model.api.SourceContainer;
import org.netbeans.modules.bpel.model.api.Target;
import org.netbeans.modules.bpel.model.api.TargetContainer;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.impl.ContainerIterator;
import org.netbeans.modules.bpel.model.impl.Utils;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.bpel.validation.core.BpelValidator;
import org.netbeans.modules.bpel.validation.core.Outcome;

public final class Validator extends BpelValidator {
    
    @Override 
    public void visit(PartnerLink p) {
        
        // Rule: A partnerLink MUST specify the myRole or the partnerRole, or both.
        // This syntactic constraint MUST be statically enforced.
        if((p.getMyRole() == null || p.getMyRole().equals("")) &&
                (p.getPartnerRole() == null || p.getPartnerRole().equals(""))) 
        {
            addError( FIX_PARTNER_LINK_ERROR, p );
        }
        
        // Rule: The initializePartnerRole attribute MUST NOT be used on a partnerLink
        // that does not have a partner role; this restriction MUST be statically enforced.
        if(p.getPartnerRole() == null || p.getPartnerRole().equals("")) {
            if((p.getInitializePartnerRole() != null) &&
                    (!p.getInitializePartnerRole().equals(
                    TBoolean.INVALID))) 
            {
                addError( FIX_INITIALISE_PARTNER_ROLE, p );
            }
        }
    }
    
    @Override 
    public void visit(Invoke invoke) {
        // Rule: Porttype should not be solicit-response or Notification.
        getHelper().checkSolicitResponsePortType(invoke, invoke.getPortType());
        getHelper().checkNotificationPortType(invoke, invoke.getPortType());
        
        
        // Rule: PortType should not contain overloaded operation names.
        getHelper().checkOverloadedPortTypeOperation(invoke, invoke.getPortType());
        
        //Rule: Input variable and toPart should not be used in combinatation.
        getHelper().checkInputVariableToPartCombination(invoke);
        
        //Rule: Output variable and fromPart should not be used in combinatation.
        getHelper().checkOutputVariableFromPartCombination(invoke);       
        
        // Rule: FromPart element should have a valid part attribute.
        getHelper().checkValidPartAttributeFromPartElement(invoke);
        
        // Rule: toPart element should have a valid part attribute.
        getHelper().checkValidPartAttributeToPartElement(invoke);   
        
        /*
         * Rule :If the portType attribute is included for readability, 
         * in a <receive>, <reply>, <invoke>, <onEvent> or <onMessage>  
         * element, the value of the portType  attribute MUST match the 
         * portType value implied by the combination of the specified 
         * partnerLink and the role implicitly specified by the activity.
         */
        getHelper().checkPortTypeCombinedPartnerLink( invoke );
        
        /*
         * Rule : For <invoke>, one-way invocation requires only the 
         * inputVariable (or its equivalent <toPart>'s) since a response 
         * is not expected as part of the operation. Request-response 
         * invocation requires both an inputVariable 
         * (or its equivalent <toPart>'s) and an outputVariable 
         * (or its equivalent <fromPart>'s). If a WSDL message definition 
         * does not contain any parts, then the associated attributes variable, 
         * inputVariable or outputVariable, or the associated <fromParts> or 
         * <toParts> elements MAY be omitted. The outputVariable (or its 
         * equivalent <fromPart>'s) must be only specified for request-response 
         * invocations.
         * 
         * Rule : When the optional inputVariable and outputVariable 
         * attributes are being used in an <invoke> activity, 
         * the variables referenced by inputVariable and outputVariable 
         * MUST be messageType variables whose QName matches the QName of the 
         * input and output message type used in the operation, respectively, 
         * except as follows: if the WSDL operation used in an <invoke> 
         * activity uses a message containing exactly one part which itself is 
         * defined using an element, then a variable of the same element type 
         * as used to define the part MAY be referenced by the inputVariable 
         * and outputVariable attributes respectively.
         */
        getHelper().checkInputOutputVariableOperation( invoke );
        
        // Rule: All the WSDL message parts must be completely initialised
        // when using <toPart> element.
        getHelper().checkAnyMissingToPartElementInInvoke(invoke);
        
    }
    
    @Override 
    public void visit(Receive receive) {
        // Rule: Porttype should not be solicit-response or Notification.
        getHelper().checkSolicitResponsePortType(receive, receive.getPortType());
        getHelper().checkNotificationPortType(receive, receive.getPortType());
        
        
        // Rule: PortType should not contain overloaded operation names.
        getHelper().checkOverloadedPortTypeOperation(receive, receive.getPortType());
        
        /* 
         * Rule: On <receive> variable and <fromPart> must not be used at the same time.
         *
         * Rule : One-way invocation requires only the inputVariable (or its
         * equivalent <toPart> elements) since a response is not
         * expected as part of the operation (see section 10.4. Providing
         * Web Service Operations Receive and Reply in spec). Requestresponse
         * invocation requires both an inputVariable (or its
         * equivalent <toPart> elements) and an outputVariable (or
         * its equivalent <fromPart> elements). If a WSDL message
         * definition does not contain any parts, then the associated
         * attributes variable, inputVariable or outputVariable,
         * MAY be omitted,and the <fromParts> or <toParts>
         * construct MUST be omitted., IZ #87444
         */
        getHelper().checkReceiveVariableFromPartCombination(receive);
        
        /*
         * Rule :If the portType attribute is included for readability, 
         * in a <receive>, <reply>, <invoke>, <onEvent> or <onMessage>  
         * element, the value of the portType  attribute MUST match the 
         * portType value implied by the combination of the specified 
         * partnerLink and the role implicitly specified by the activity.
         */
        getHelper().checkPortTypeCombinedPartnerLink( receive );
        
        /*
         * A "start activity" is a <receive> or <pick> activity that is 
         * annotated with a createInstance="yes" attribute. 
         * Activities other than the following: start activities, <scope>, 
         * <flow>, <sequence>, and <empty> MUST NOT be performed prior to 
         * or simultaneously with start activities.
         */
        getHelper().checkOrderOfActivities( receive );
        
    }
    
    @Override 
    public void visit(Reply reply) {
        // Rule: Porttype should not be solicit-response or Notification.
        getHelper().checkSolicitResponsePortType(reply, reply.getPortType());
        getHelper().checkNotificationPortType(reply, reply.getPortType());
        
        
        // Rule: PortType should not contain overloaded operation names.
        getHelper().checkOverloadedPortTypeOperation(reply, reply.getPortType());
        
        /* Rule: On <reply> variable and <toPart> must not be used at the same time.
         * 
         * Rule : One-way invocation requires only the inputVariable (or its
         * equivalent <toPart> elements) since a response is not
         * expected as part of the operation (see section 10.4. Providing
         * Web Service Operations Receive and Reply in spec). Requestresponse
         * invocation requires both an inputVariable (or its
         * equivalent <toPart> elements) and an outputVariable (or
         * its equivalent <fromPart> elements). If a WSDL message
         * definition does not contain any parts, then the associated
         * attributes variable, inputVariable or outputVariable,
         * MAY be omitted,and the <fromParts> or <toParts>
         * construct MUST be omitted., IZ #87444

         */
        getHelper().checkReplyVariableToPartCombination(reply);

        /*
         * Rule :If the portType attribute is included for readability, 
         * in a <receive>, <reply>, <invoke>, <onEvent> or <onMessage>  
         * element, the value of the portType  attribute MUST match the 
         * portType value implied by the combination of the specified 
         * partnerLink and the role implicitly specified by the activity.
         */
        getHelper().checkPortTypeCombinedPartnerLink( reply );
        
        // Rule: All the WSDL message parts must be completely initialised
        // when using <toPart> element.
        getHelper().checkAnyMissingToPartElementInReply(reply);            
    }
    
    @Override
    public void visit(OnMessage onMessage) {
        // Rule: Porttype should not be solicit-response or Notification.
        getHelper().checkSolicitResponsePortType(onMessage,
                onMessage.getPortType());
        getHelper().checkNotificationPortType(onMessage, onMessage.getPortType());
        
        
        // Rule: PortType should not contain overloaded operation names.
        getHelper().checkOverloadedPortTypeOperation(onMessage, onMessage.getPortType());
        
        /*
         * Rule :If the portType attribute is included for readability, 
         * in a <receive>, <reply>, <invoke>, <onEvent> or <onMessage>  
         * element, the value of the portType  attribute MUST match the 
         * portType value implied by the combination of the specified 
         * partnerLink and the role implicitly specified by the activity.
         */
        getHelper().checkPortTypeCombinedPartnerLink( onMessage );
        
        // Variable and <fromPart> must not be used at the simultaneously.
        getHelper().checkOnMessageVariableFromPartCombination(onMessage);
    }
    
    @Override
    public void visit( ReThrow reThrow ){
        /* 
         * Rule : The <rethrow> activity MUST only be used within a faultHandler 
         * (i.e. <catch> and <catchAll> elements).
         */
        boolean isInsideFaultHandlers = Utils.hasAscendant( reThrow , 
                FaultHandlers.class ); 
        if ( !isInsideFaultHandlers ){
            addError( FIX_RETHROW_OCCURANCE, reThrow );
        }
    }
    
    @Override
    public void visit( Compensate compensate ){
        getHelper().checkCompensateOccurance( compensate);
    }

    @Override
    public void visit( CompensateScope compensateScope ){
        getHelper().checkCompensateOccurance(compensateScope);
    }
    
    @Override 
    public void visit( From from ) {
        Roles roles = from.getEndpointReference();
        if ( Roles.MY_ROLE.equals(roles) ) {
            BpelReference<PartnerLink> ref = from.getPartnerLink();
            boolean referenceHasMyRole = (ref!= null) && ( ref.get() != null ) 
                && ( ref.get().getMyRole() != null );
            /*
             *  Rule : In the from-spec of the partnerLink variant of <assign> 
             *  the value "myRole" for attribute endpointReference is only 
             *  permitted when the partnerLink specifies the attribute myRole.
             */
            if ( !referenceHasMyRole ) {
                addError( FIX_ENDPOINT_REFRENCE, from, Roles.MY_ROLE.toString());
            }
        }
        else if ( Roles.PARTNER_ROLE.equals(roles) ){
            BpelReference<PartnerLink> ref = from.getPartnerLink();
            boolean referenceHasPartnerRole = (ref!= null) && ( ref.get() != null ) 
                && ( ref.get().getPartnerRole() != null );
            /*
             * In the from-spec of the partnerLink variant of <assign> the 
             * value "partnerRole" for attribute endpointReference is only 
             * permitted when the partnerLink specifies the attribute 
             * partnerRole.
             */
            if ( !referenceHasPartnerRole ) {
                addError( FIX_ENDPOINT_REFRENCE, from, Roles.PARTNER_ROLE.toString() );
            }
        }
    }
    
    @Override
    public void visit( Process process ) {
        getHelper().visitBaseScope( process );
        
        /*
         * Rule : To be instantiated, an executable business process MUST contain at 
         * least one <receive> or <pick> activity annotated with a 
         * createInstance="yes" attribute.
         * 
         * Rule : If a process has multiple start activities with 
         * correlation sets then all such activities MUST share at 
         * least one common correlationSet and all common correlationSets defined 
         * on all the activities MUST have the value of the initiate 
         * attribute be set to "join".
         */
        getHelper().checkInstantiableActivities( process );
        
        /*
         * Rule : A WS-BPEL process definition MUST NOT be accepted for 
         * processing if it defines two or more propertyAliases for the 
         * same property name and WS-BPEL variable type.
         */
        getHelper().checkPropertyAliasMultiplicity( process );
        
        /*
         * Rule : 
         * Determine which languages are referenced by queryLanguage or
         * expressionLanguage attributes either in the WS-BPEL process
         * definition itself or in any WS-BPEL property definitions in
         * associated WSDLs and if any referenced language is unsupported by the
         * WS-BPEL processor then the processor MUST reject the submitted
         * WS-BPEL process definition.
         */
        String query = process.getQueryLanguage();
        String expression = process.getExpressionLanguage();
        if ((query != null && !SUPPORTED_LANGAGE.equals(query))
                || (expression != null && !SUPPORTED_LANGAGE.equals(expression)))
        {
            addError(FIX_SUPPORTED_LANGUAGE, process, SUPPORTED_LANGAGE);
        }
    }

    @Override
    public void visit( Scope scope ) {
        getHelper().visitBaseScope( scope );
        
        /*
         * Rule : A scope with the isolated  attribute set to "yes" is called 
         * an isolated scope. Isolated scopes MUST NOT contain other isolated scopes.
         */
        TBoolean isolated = scope.getIsolated();
        if ( TBoolean.YES.equals( isolated ) ) {
            List<Component> collection = new LinkedList<Component>();
            getHelper().collectIsolatedScopes( scope , collection );

            if ( collection.size() >0 ){
                collection.add( scope );

                for(Component component : collection) {
                    addError(FIX_ISOLATED_SCOPES, component);
                }
            }
        }
    }
    
    @Override
    public void visit( LinkContainer container ) {
        /*
         * Rule : A links name MUST be unique amongst all link names 
         * defined within the same immediately enclosing flow. 
         * This requirement MUST be statically enforced.
         */
        Link[] links = container.getLinks();
        if ( links== null ) {
            return;
        }
        Map<String,Collection<Component>> map = 
            new HashMap<String, Collection<Component>>();
        for (Link link : links) {
            getHelper().addNamedToMap(link, map );
        }
        
        getHelper().addErrorForNamed( map , FIX_MULTIPLE_NAMED_LINKS );
    }

    @Override
    public void visit( SourceContainer container ) {
        /*
         * Rule : An activity MAY declare itself to be the source 
         * of one or more links by including one or more <source> elements. 
         * Each <source> element MUST use a distinct link name.
         */
        Source[] sources = container.getSources();
        if ( sources == null ) {
            return;
        }
        Map<String,Collection<Component>> map = 
            new HashMap<String, Collection<Component>>();
        for (Source source : sources) {
            getHelper().addNamedToMap( source, map, Helper.LazyHolder.SOURCE_LINK_NAME_ACCESS );
        }
        getHelper().addErrorForNamed(map, FIX_MULTIPLE_SOURCE_LINK_REFERENCES );
    }
    
    
    @Override
    public void visit( TargetContainer container ) {
        /*
         * Rule : An activity MAY declare itself to be the target of one or more
         * links by including one or more <target> elements. Each <target> 
         * element associated with a given activity MUST use a link name distinct 
         * from all other <target>  elements at that activity.
         */
        Target[] targets = container.getTargets();
        if ( targets == null ) {
            return;
        }
        Map<String,Collection<Component>> map = 
            new HashMap<String, Collection<Component>>();
        for (Target target : targets) {
            getHelper().addNamedToMap( target, map , Helper.LazyHolder.TARGET_LINK_NAME_ACCESS );
        }
        getHelper().addErrorForNamed(map, FIX_MULTIPLE_TARGET_LINK_REFERENCES );
    }
    
    @Override
    public void visit( ForEach forEach ) {
        /*
         * Rule : For <forEach> the enclosed scope MUST NOT declare a variable 
         * with the same name as specified in the counterName  attribute of <forEach>.
         */
        String counterName = forEach.getCounterName();
        if ( counterName == null ){
            return;
        }
        ContainerIterator<BaseScope> containerIterator = 
            new ContainerIterator<BaseScope>( forEach ,BaseScope.class );
        BaseScope scope = containerIterator.next();
        assert scope!=null;
        VariableContainer container = scope.getVariableContainer();
        if ( container == null ){
            return;
        }
        Variable[] variables = container.getVariables();
        if ( variables == null){
            return;
        }
        for (Variable variable : variables) {
            if ( counterName.equals( variable.getName()) ){
                Collection<Component> collection = new ArrayList<Component>(2);
                collection.add( forEach );
                collection.add( variable );

                for (Component component : collection) {
                  addError(FIX_DUPLICATE_COUNTER_NAME, component, counterName);
                }
                break;
            }
        }
        
        /*
         * Check Variable name
         */
        checkVariableName( forEach );
    }
    
    @Override
    public void visit( VariableContainer container ){
        getHelper().checkVariableContainer( container );
    }
    
    @Override
    public void visit( OnEvent onEvent ) {
        if  ( onEvent.getVariable()!= null &&
                 onEvent.getMessageType() == null && 
                 onEvent.getElement() == null )
        {
            addError(FIX_ON_EVENT_VARAIBLE, onEvent);
        }
        
        /*
         * Rule :If the portType attribute is included for readability, 
         * in a <receive>, <reply>, <invoke>, <onEvent> or <onMessage>  
         * element, the value of the portType  attribute MUST match the 
         * portType value implied by the combination of the specified 
         * partnerLink and the role implicitly specified by the activity.
         */
        getHelper().checkPortTypeCombinedPartnerLink( onEvent );
        
        /*
         * Check variable name.
         */
        checkVariableName( onEvent );
        
        /*
         * Rule : For <onEvent>, the type of the variable (as specified by the 
         * messageType attribute) MUST be the same as the type of the input 
         * message defined by operation referenced by the operation attribute. 
         * Optionally the messageType attribute may be omitted and instead the 
         * element attribute substituted if the message to be received has a 
         * single part and that part is defined with an element type. 
         * That element type MUST be an exact match of the element type 
         * referenced by the element attribute.
         */
        getHelper().checkMessageType( onEvent );
        
        /*
         * For <onEvent>, variables referenced by the variable attribute of
         * <fromPart> elements or the variable attribute of an <onEvent> element
         * are implicitly declared in the associated scope of the event handler.
         * Variables of the same names MUST NOT be explicitly declared in the
         * associated scope. The variable references are resolved to the
         * associated scope only and MUST NOT be resolved to the ancestor
         * scopes.
         */

        getHelper().checkImplicitlyDeclaredVars(onEvent);

        FromPartContainer fromParts = onEvent.getFromPartContaner();
        if (fromParts != null) {
            for (FromPart part : fromParts.getFromParts()) {
                checkVariableName(part);
            }
        }
    }
    
    @Override
    public void visit( EventHandlers handlers ) {
        /*
         * Rule : An event handler MUST contain at least one <onEvent>  
         * or <onAlarm>  element.
         */
        if ( handlers.sizeOfOnAlarms() == 0 && handlers.sizeOfOnEvents() == 0 ) {
            addError( FIX_EVENT_HANDLERS , handlers );
        }
    }

    @Override
    public void visit( Catch catc )
    {
        /*
         * Rule : For the <catch> construct; to have a defined type 
         * associated with the fault variable, the faultVariable 
         * attribute MUST only be used if either the faultMessageType or 
         * faultElement attributes, but not both, accompany it. 
         * The faultMessageType and faultElement attributes MUST NOT be 
         * used unless accompanied by faultVariable attribute.
         */
        String faultVariable = catc.getFaultVariable();
        SchemaReference<GlobalElement> element = catc.getFaultElement();
        WSDLReference<Message> message = catc.getFaultMessageType();
        Outcome item = null;

        if (faultVariable != null && element == null && message == null) {
            addError(FIX_FAULT_VARIABLE_TYPE, catc);
        }
        if (element != null && message != null && item == null) {
            addError(FIX_FAULT_VARIABLE_TYPE, catc);
        }
        if (faultVariable == null && ( element != null || message != null)) {
            addError(FIX_ODD_FAULT_TYPE, catc);
        }
        // Check fault variable name
        checkVariableName( catc);
    }
    
    @Override
    public void visit( FaultHandlers handlers ) {
        /*
         * Rule : There MUST be at least one <catch> or <catchAll>  element 
         * within a <faultHandlers> element.
         */
        if ( handlers.sizeOfCathes() == 0 && handlers.getCatchAll() == null ) {
            addError( FIX_FAULT_HANDLERS, handlers );
        }
        
        /*
         * Rule : The root scope inside a FCT-handler MUST not have a compensation handler.
         */
        getHelper().checkFCTScope( handlers );
    }

    @Override
    public void visit( Import imp ) {
        final Model model = getImportModel(imp);
        
        /*
         * If the model is null -- skip all other checks, but DO NOT generate
         * a warning or an error, as it will be done by BPELImportsValidator.
         */
        if (model == null) {
            return;
        }
        final String namespace = imp.getNamespace();
        final String ns = getHelper().getTargetNamespace( imp );
        
        if ( namespace == null ){
            /*
             * If no namespace is specified then the imported definitions MUST NOT 
             * contain a targetNamespace specification. 
             * This requirement MUST be statically enforced.
             */
            if ( ns != null ){
                addError( FIX_ABSENT_NAMESPACE_IN_IMPORT , imp );
            }
        }
        else {
            /*
             * If a namespace attribute is specified on an <import> then 
             * the imported definitions MUST be in that namespace. 
             * This requirement MUST be statically enforced.
             */
            if ( !namespace.equals( ns ) ){
                addError( FIX_BAD_NAMESPACE_IN_IMPORT , imp );
            }
        }
        
        /*
         * Rule: The value of the importType attribute of element <import>  
         * MUST be set to http://www.w3.org/2001/XMLSchema when importing XML 
         * Schema 1.0 documents, and to http://schemas.xmlsoap.org/wsdl/ when 
         * importing WSDL 1.1 documents.
         */
        getHelper().checkImportType( imp );
    }

    @Override
    public void visit( Variable variable ) {
        checkVariableName( variable );
        
        /*
         * The messageType, type or element attributes are used to 
         * specify the type of a variable. Exactly one of these attributes 
         * MUST be used.
         */
        int count = variable.getType()== null?0:1;
        count+=variable.getMessageType()==null?0:1;
        count+=variable.getElement()==null?0:1;
        if ( count != 1) {
            addError( FIX_VARIABLE_TYPES , variable );
        }
    }

    @Override
    public void visit( Pick pick ) {
        if ( TBoolean.YES.equals( pick.getCreateInstance()) && 
                pick.sizeOfOnAlarms() != 0 )
        {
                Collection<Component> collection = 
                    Arrays.asList( (Component[])pick.getOnAlarms());
                
                for (Component component : collection) {
                  addError(FIX_PICK_MESSAGES, component);
                }
        }
        
        /*
         * A "start activity" is a <receive> or <pick> activity that is 
         * annotated with a createInstance="yes" attribute. 
         * Activities other than the following: start activities, <scope>, 
         * <flow>, <sequence>, and <empty> MUST NOT be performed prior to 
         * or simultaneously with start activities.
         */
        getHelper().checkOrderOfActivities( pick );
    }

    @Override
    public void visit( Flow flow ) {
        /*
         * Rule : Every link declared within a <flow> activity MUST have exactly 
         * one activity within the <flow> as its source and exactly 
         * one activity within the <flow> as its target.
         * 
         * and this method also peform one more rule :
         * 
         * Rule : Two different links MUST NOT share the same source 
         * and target activities; that is, at most one link may be used 
         * to connect two activities.
         * ( check for this rule will be started only when previous rule
         * is succeeded). 
         * 
         *         
         * Rule : A link that crosses a <faultHandlers> or <terminationHandler>  
         * element boundary MUST be outbound only, that is, it MUST have its 
         * source activity within the <faultHandlers> or <terminationHandler>, 
         * and its target activity outside of the scope associated with the handler.
         * 
         * Rule :A link MUST NOT cross the boundary of a repeatable construct or 
         * the <compensationHandler> element. This means, a link used within a 
         * repeatable construct (<while>, <repeatUntil>, <forEach>, <eventHandlers>) 
         * or a <compensationHandler>  MUST be declared in a <flow> that is itself 
         * nested inside the repeatable construct or <compensationHandler>.
         */
        getHelper().checkLinks( flow );
    }

    @Override
    public void visit( CompensationHandler handler ) {
        /*
         * Rule : The root scope inside a FCT-handler MUST not have a compensation handler.
         */
        getHelper().checkFCTScope( handler );
    }

    @Override
    public void visit( TerminationHandler handler ) {
        /*
         * Rule : The root scope inside a FCT-handler MUST not have a compensation handler.
         */
        getHelper().checkFCTScope( handler );
    }

    @Override
    public void visit( CorrelationSet set ) {
        /*
         * Rule : Properties used in a <correlationSet> MUST be defined 
         * using XML Schema simple types. This restriction MUST be statically 
         * enforced.
         */
        List<WSDLReference<CorrelationProperty>> list = set.getProperties();
        if ( list == null ) {
            return;
        }
        for (WSDLReference<CorrelationProperty> reference : list) {
            CorrelationProperty property = reference.get();
            // if it null then reference validator will report about error
            if ( property!= null ) {
                NamedComponentReference<GlobalType> typeRef = property.getType();
                if ( typeRef == null || 
                        !(typeRef.get() instanceof GlobalSimpleType)) 
                {
                    addError( FIX_BAD_CORRELATION_PROPERTY_TYPE , set );
                }
            }
        }
    }

    @Override
    public void visit( PatternedCorrelation correlation ) {
        /*
         * Rule : The pattern attribute used in <correlation>  within 
         * <invoke> is required for request-response operations, and 
         * disallowed when a one-way operation is invoked.
         */
        Pattern pattern = correlation.getPattern();

        BpelContainer container = correlation.getParent().getParent();
        assert container instanceof Invoke;
        Invoke invoke = (Invoke) container;
        
        // If operation is absent or broken then other validators said about this.
        
        WSDLReference<Operation> operationRef = invoke.getOperation();
        if ( operationRef == null ) {
            return;
        }
        Operation operation = operationRef.get();
        if ( operation == null ) {
            return;
        }
        // # 84129
        boolean oneWayOperationPatternExist = 
            pattern != null && operation instanceof OneWayOperation;
        boolean twoWayOperationPatternAbsent = 
            pattern == null && operation instanceof RequestResponseOperation;
        boolean flag = oneWayOperationPatternExist || twoWayOperationPatternAbsent;
        if  ( flag ) {
            addError( FIX_BAD_USAGE_PATTERN_ATTRIBUTE , invoke);
        }
    }

    @Override
    public void visit( CorrelationContainer container ) {
        /*
         * Rule : Static analysis MUST detect property usages where 
         * propertyAliases for the associated variable's type are not found 
         * in any WSDL definitions directly imported by the WS-BPEL process.
         */
        BpelContainer parent = container.getParent();
        if (parent instanceof OperationReference && parent instanceof Responder)
        {
            getHelper().checkPropertyUsageInInputMessage(
                    (OperationReference) parent, container.getCorrelations());
        }
        if (parent instanceof OperationReference && parent instanceof Requester)
        {
            getHelper().checkPropertyUsageInOutputMessage(
                    (OperationReference) parent, container.getCorrelations());
        }
    }
    
    @Override
    public void visit( PatternedCorrelationContainer container ) {
        /*
         * Rule : Static analysis MUST detect property usages where 
         * propertyAliases for the associated variable's type are not found 
         * in any WSDL definitions directly imported by the WS-BPEL process.
         */
        BpelContainer parent = container.getParent();
        if ( parent instanceof OperationReference && parent instanceof Responder ) {
            getHelper().checkPropertyUsageInInputMessage( 
                    (OperationReference) parent , 
                    container.getPatternedCorrelations() );
        }
        if (parent instanceof OperationReference && parent instanceof Requester)
        {
            getHelper().checkPropertyUsageInOutputMessage(
                    (OperationReference) parent, 
                    container.getPatternedCorrelations());
        }
    }
    
    @Override
    public void visit( CorrelationSetContainer container )
    {
        /*
         *  The name of a <correlationSet> MUST be unique amongst the names of 
         *  all <correlationSet> defined within the same immediately 
         *  enclosing scope.
         */
        CorrelationSet[] correlations = container.getCorrelationSets();
        if ( correlations == null ){
            return;
        }
        Map<String,Collection<Component>> map = 
            new HashMap<String, Collection<Component>>();
        for (CorrelationSet correlation : correlations) {
            getHelper().addNamedToMap( correlation , map );
        }
        getHelper().addErrorForNamed(  map , FIX_DUPLICATE_CORRELATION_SET_NAME );
    }
    
    @Override
    public void visit( PartnerLinkContainer container )
    {
        /*
         * The name of a partnerLink MUST be unique amongst the names of all 
         * partnerLinks defined within the same immediately enclosing scope. 
         * This requirement MUST be statically enforced.
         */
        PartnerLink[] links = container.getPartnerLinks();
        if ( links == null ){
            return;
        }
        Map<String,Collection<Component>> map = 
            new HashMap<String, Collection<Component>>();
        for (PartnerLink link : links) {
            getHelper().addNamedToMap( link , map );
        }
        getHelper().addErrorForNamed(  map , FIX_DUPLICATE_PARTNER_LINK_NAME );
    }
    
    @Override
    public void visit( Extension extension )
    {
        /*
         * Rule : In the case of mandatory extensions declared in the
         * <extensions> element not supported by a WS-BPEL implementation, the
         * process definition MUST be rejected.
         */
        TBoolean mustUnderstand = extension.getMustUnderstand();
        if (TBoolean.YES.equals(mustUnderstand)) {
            BpelModel model = extension.getBpelModel();
            assert model instanceof BpelModelImpl;
            BpelModelImpl impl = (BpelModelImpl) model;

            if (!impl.isSupportedExpension(extension.getNamespace())) {
                String ns = extension.getNamespace();
                if (ns == null) {
                    ns = "";
                }
                addError(FIX_UNSUPPORTED_EXTENSION, extension, ns);
            }
        }
    }      

    private Helper getHelper() {
        if(myHelper == null) {
            myHelper = new Helper(this);
        }
        
        return myHelper;
    }
    
    private void checkVariableName( VariableDeclaration declaration ) {
        String name = declaration.getVariableName();
        if ( name!= null && name.indexOf('.')!= -1 ){
            addError( FIX_BAD_VARIABLE_NAME , declaration );
        }
    }
    
    private Model getImportModel(Import imp) {
        if (Import.WSDL_IMPORT_TYPE.equals(imp.getImportType())) {
            return ImportHelper.getWsdlModel(imp , false);
        }
        
        if (Import.SCHEMA_IMPORT_TYPE.equals(imp.getImportType())) {
            return ImportHelper.getSchemaModel(imp , false);
        }
        
        return null;
    }
    
    static final String SUPPORTED_LANGAGE = "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0";
    static final String FIX_INITIALISE_PARTNER_ROLE = "FIX_InitialisePartnerRole";
    static final String FIX_PARTNER_LINK_ERROR = "FIX_PartnerLinkError";
    static final String FIX_RETHROW_OCCURANCE = "FIX_RethrowOccurance";
    static final String FIX_ENDPOINT_REFRENCE = "FIX_EndpointReference";
    static final String FIX_MULTIPLE_NAMED_LINKS = "FIX_MultipleNamedLinks";
    static final String FIX_MULTIPLE_SOURCE_LINK_REFERENCES = "FIX_MultipleSourceLinkReferences";
    static final String FIX_MULTIPLE_TARGET_LINK_REFERENCES = "FIX_MultipleTargetLinkReferences";
    static final String FIX_DUPLICATE_COUNTER_NAME = "FIX_DuplicateCounterName";
    static final String FIX_ISOLATED_SCOPES = "FIX_IsolatedScopes";
    static final String FIX_ON_EVENT_VARAIBLE = "FIX_OnEventVariable";
    static final String FIX_EVENT_HANDLERS = "FIX_EventHandlers";
    static final String FIX_FAULT_VARIABLE_TYPE = "FIX_FaultVariableType";
    static final String FIX_ODD_FAULT_TYPE = "FIX_OddFaultType";
    static final String FIX_FAULT_HANDLERS = "FIX_FaultHandlers";
    static final String FIX_ABSENT_NAMESPACE_IN_IMPORT = "FIX_AbsentNamespaceInImport";
    static final String FIX_BAD_NAMESPACE_IN_IMPORT = "FIX_BadNamespaceInImport";
    static final String FIX_BAD_VARIABLE_NAME = "FIX_BadVariableName";
    static final String FIX_PICK_MESSAGES = "FIX_PickMessages";
    static final String FIX_BAD_CORRELATION_PROPERTY_TYPE = "FIX_BadCorrelationPropertyType";
    static final String FIX_BAD_USAGE_PATTERN_ATTRIBUTE = "FIX_BadUsagePatternAttribute";
    static final String FIX_DUPLICATE_CORRELATION_SET_NAME = "FIX_DuplicateCorrelationSetName";
    static final String FIX_DUPLICATE_PARTNER_LINK_NAME = "FIX_DuplicatePartnerLinkName";
    static final String FIX_VARIABLE_TYPES = "FIX_VariableTypes";
    static final String FIX_SUPPORTED_LANGUAGE ="FIX_SupportedLanguage";
    static final String FIX_UNSUPPORTED_EXTENSION = "FIX_UnsupportedExtension";

    private Helper myHelper;
}
