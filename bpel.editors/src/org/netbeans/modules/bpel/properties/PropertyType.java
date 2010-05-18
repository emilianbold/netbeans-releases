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
package org.netbeans.modules.bpel.properties;

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants.AlarmEventType;
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants.AlarmType;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.AtomicTxType;
import org.netbeans.modules.bpel.model.api.support.BinaryCopy;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.properties.props.editors.AlarmTypeEditor;
import org.netbeans.modules.bpel.properties.props.editors.ForExprEditor;
import org.netbeans.modules.bpel.properties.props.editors.InitiateEditor;
import org.netbeans.modules.bpel.properties.props.editors.AtomicTxTypeEditor;
import org.netbeans.modules.bpel.properties.props.editors.BinaryCopyEditor;
import org.netbeans.modules.bpel.properties.props.editors.QNamePropEditor;
import org.netbeans.modules.bpel.properties.props.editors.RepeatEveryExprEditor;
import org.netbeans.modules.bpel.properties.props.editors.StartCounterExprEditor;
import org.netbeans.modules.bpel.properties.props.editors.StringPropEditor;
import org.netbeans.modules.bpel.properties.props.editors.VariablesEditor;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.properties.props.editors.AlarmEventTypeEditor;
import org.netbeans.modules.bpel.properties.props.editors.BooleanExprEditor;
import org.netbeans.modules.bpel.properties.props.editors.CompensationTargetEditor;
import org.netbeans.modules.bpel.properties.props.editors.FaultNamePropertyEditor;
import org.netbeans.modules.bpel.properties.props.editors.FaultTypePropEditor;
import org.netbeans.modules.bpel.properties.props.editors.FinalCounterExprEditor;
import org.netbeans.modules.bpel.properties.props.editors.FromPropEditor;
import org.netbeans.modules.bpel.properties.props.editors.MessageExchangePropEditor;
import org.netbeans.modules.bpel.properties.props.editors.ModelReferenceEditor;
import org.netbeans.modules.bpel.properties.props.editors.PatternEditor;
import org.netbeans.modules.bpel.properties.props.editors.TBooleanEditor;
import org.netbeans.modules.bpel.properties.props.editors.ToPropEditor;
import org.netbeans.modules.bpel.properties.props.editors.UntilExprEditor;
import org.netbeans.modules.bpel.properties.props.editors.VariablePropertyEditor;
import org.netbeans.modules.bpel.properties.props.editors.VariableStereotypeEditor;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.Reference;

/**
 * @author Vitaly Bychkov
 * @author nk160297
 */
public enum PropertyType implements org.netbeans.modules.soa.ui.properties.PropertyType {
    NAME(String.class, StringPropEditor.class),
    VARIABLES(String.class, VariablesEditor.class),
    DOCUMENTATION(String.class, StringPropEditor.class),
    VERSION(String.class, StringPropEditor.class), 
    AUTHOR(String.class, StringPropEditor.class), 
    LANGUAGE(String.class, StringPropEditor.class), 
    TARGET_NAMESPACE(String.class, StringPropEditor.class), 
    QUERY_LANGUAGE(String.class, StringPropEditor.class), 
    EXPRESSION_LANGUAGE(String.class, StringPropEditor.class), 
    FAULT_NAME(QName.class, FaultNamePropertyEditor.class), 
    FAULT_NAME_RO(QName.class, QNamePropEditor.class), // Read-only variant
    FAULT_VARIABLE(VariableDeclaration.class, VariablePropertyEditor.class),
    FAULT_VARIABLE_REF(BpelReference.class, ModelReferenceEditor.class), 
    FAULT_VARIABLE_NAME(String.class, StringPropEditor.class),
    FAULT_VARIABLE_TYPE(TypeContainer.class, FaultTypePropEditor.class), 
    PARTNER_LINK(BpelReference.class, ModelReferenceEditor.class), 
    PORT_TYPE(WSDLReference.class, ModelReferenceEditor.class), 
    OPERATION(WSDLReference.class, ModelReferenceEditor.class), 
    INPUT(BpelReference.class, ModelReferenceEditor.class), 
    OUTPUT(BpelReference.class, ModelReferenceEditor.class), 
    EVENT_VARIABLE_NAME(String.class, StringPropEditor.class),
    TRANSIENT_CONDITION(String.class, StringPropEditor.class), 
    JOIN_CONDITION(String.class, StringPropEditor.class), 
    WHILE_CONDITION(BooleanExpr.class), 
    TIME_EXPRESSION(String.class, StringPropEditor.class), 
    WSDL_FILE(String.class, StringPropEditor.class), 
    PARTNER_LINK_TYPE(WSDLReference.class, ModelReferenceEditor.class), 
    MY_ROLE(WSDLReference.class, ModelReferenceEditor.class), 
    PARTNER_ROLE(WSDLReference.class, ModelReferenceEditor.class), 
    CREATE_INSTANCE(Boolean.class), 
    MESSAGE_EXCHANGE(BpelReference.class, MessageExchangePropEditor.class), 
    SCOPE_NAME(String.class, StringPropEditor.class), 
    SCOPE(BpelReference.class, ModelReferenceEditor.class),
    LABEL(String.class, StringPropEditor.class), 
    STATE(String.class, StringPropEditor.class), 
    ANNOTATION(String.class, StringPropEditor.class), 
    VARIABLE_STEREOTYPE(VariableStereotype.class, VariableStereotypeEditor.class), 
    VARIABLE_TYPE(Reference.class, ModelReferenceEditor.class),
    VARIABLE_TYPE_QNAME(QName.class, QNamePropEditor.class),
    CORRELATON_PROPERTY_TYPE(GlobalSimpleType.class), 
    CORRELATON_PROPERTY_TYPE_NAME(String.class, StringPropEditor.class), 
    CORR_PROPERTY(CorrelationProperty.class), 
    CORR_PROPERTY_NAME(QName.class, QNamePropEditor.class), 
    MESSAGE_TYPE(Message.class), 
    MESSAGE_TYPE_NAME(QName.class, QNamePropEditor.class), 
    PART(String.class, StringPropEditor.class), 
    QUERY(String.class, StringPropEditor.class), 
    COPY_FROM(From.class, FromPropEditor.class), 
    IGNORE_MISSING_FROM_DATA(TBoolean.class, TBooleanEditor.class),
    VALIDATE(TBoolean.class, TBooleanEditor.class),
    BINARY_COPY(BinaryCopy.class, BinaryCopyEditor.class), 
    ATOMIC_TX_TYPE(AtomicTxType.class, AtomicTxTypeEditor.class), 
    KEEP_SRC_ELEMENT_NAME(TBoolean.class, TBooleanEditor.class), 
    COPY_TO(To.class, ToPropEditor.class), 
    TIMER_FOR(String.class, StringPropEditor.class), 
    TIMER_UNTIL(String.class, StringPropEditor.class), 
    CORRELATION_SET(BpelReference.class, ModelReferenceEditor.class), 
    CORRELATION_INITIATE(Initiate.class, InitiateEditor.class), 
    CORRELATION_PATTERN(Pattern.class, PatternEditor.class), 
    XPATH(String.class, StringPropEditor.class), 
    ASSIGNMENT_COUNT(int.class), 
    BOOLEAN_EXPRESSION(BooleanExpr.class, BooleanExprEditor.class), 
    IMPORT_LOCATION(String.class, StringPropEditor.class), 
    IMPORT_TYPE(String.class, StringPropEditor.class), 
    IMPORT_NAMESPACE(String.class, StringPropEditor.class), 
    PARALLEL(Boolean.class), 
    COUNTER_NAME(String.class, StringPropEditor.class), 
    START_COUNTER_EXPR(StartCounterValue.class, StartCounterExprEditor.class), 
    FINAL_COUNTER_EXPR(FinalCounterValue.class, FinalCounterExprEditor.class), 
    COMPLETION_CONDITION(String.class, StringPropEditor.class), 
    SUPPORT_COMPLETION_CONDITION(Boolean.class), 
    COUNT_COMPLETED_BRANCHES_ONLY(Boolean.class), 
    ALARM_TYPE(AlarmType.class, AlarmTypeEditor.class), 
    ALARM_EVENT_TYPE(AlarmEventType.class, AlarmEventTypeEditor.class), 
    FOR_EXPRESSION(TimeEvent.class, ForExprEditor.class), 
    UNTIL_EXPRESSION(TimeEvent.class, UntilExprEditor.class), 
    REPEAT_EVERY_EXPRESSION(RepeatEvery.class, RepeatEveryExprEditor.class), 
    COMPENSATION_TARGET(BpelReference.class, CompensationTargetEditor.class), 
    ATOMIC_PROCESS(Boolean.class),
    WAITING_REQUEST_LIFE_SPAN(Integer.class),
    PERSISTENCE_OPT_OUT(TBoolean.class, TBooleanEditor.class);

    private Class<?> myClass;
    private String myDisplayName;
    private Class myPropertyEditorClass;

    PropertyType(Class aClass) {
        this(aClass, null);
    }
    
    PropertyType(Class<?> aClass, Class propertyEditorClass) {
        this.myClass = aClass;
        this.myPropertyEditorClass = propertyEditorClass;
    }
    
    public Class<?> getPropertyClass() {
        return myClass;
    }
    
    public String getDisplayName() {
        if (myDisplayName == null) {
            try {
                myDisplayName = NbBundle.getMessage(PropertyType.class, 
                        this.toString());
            } catch (Exception ex) {
                myDisplayName = name();
            }
        }
        return myDisplayName;
    }
    
    public Class getPropertyEditorClass() {
        return myPropertyEditorClass;
    }
}
