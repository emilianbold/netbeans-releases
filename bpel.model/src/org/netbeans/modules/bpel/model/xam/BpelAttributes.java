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
package org.netbeans.modules.bpel.model.xam;

import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.CompensationHandlerHolder;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CreateInstanceActivity;
import org.netbeans.modules.bpel.model.api.Documentation;
import org.netbeans.modules.bpel.model.api.Extension;
import org.netbeans.modules.bpel.model.api.FaultNameReference;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.JoinFailureSuppressor;
import org.netbeans.modules.bpel.model.api.Link;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeReference;
import org.netbeans.modules.bpel.model.api.MessageTypeReference;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.PropertyReference;
import org.netbeans.modules.bpel.model.api.QueryLanguageSpec;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.ServiceRef;
import org.netbeans.modules.bpel.model.api.Target;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.Validate;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author ads
 */
public enum BpelAttributes implements Attribute {
    EXIT_ON_STANDART_FAULT( BaseScope.EXIT_ON_STANDART_FAULT , TBoolean.class),
    NAME( NamedElement.NAME , String.class , AttrType.NCNAME ),
    SUPPRESS_JOIN_FAILURE( JoinFailureSuppressor.SUPPRESS_JOIN_FAILURE , 
            TBoolean.class ),
    EXPRESSION_LANGUAGE( Process.EXPRESSION_LANGUAGE, String.class , AttrType.URI ),
    QUERY_LANGUAGE( QueryLanguageSpec.QUERY_LANGUAGE, String.class , AttrType.URI ),
    TARGET_NAMESPACE( Process.TARGET_NAMESPACE, String.class , AttrType.URI ),
    VALIDATE( Assign.VALIDATE , TBoolean.class ),
    CREATE_INSTANCE( CreateInstanceActivity.CREATE_INSTANCE , TBoolean.class ),
    ISOLATED( Scope.ISOLATED , TBoolean.class ),
    SOURCE( Documentation.SOURCE , String.class , AttrType.URI ),
    LOCATION( Import.LOCATION , String.class , AttrType.URI ),
    IMPORT_TYPE( Import.IMPORT_TYPE , String.class , AttrType.URI ),
    NAMESPACE( Import.NAMESPACE , String.class , AttrType.URI ),
    MUST_UNDERSTAND( Extension.MUST_UNDERSTAND , TBoolean.class ),
    INITIATE( Correlation.INITIATE , Initiate.class ),
    PATTERN( PatternedCorrelation.PATTERN , Pattern.class ),
    OPAQUE( From.OPAQUE , TBoolean.class ),
    ENDPOINT_REFERENCE( From.ENDPOINT_REFERENCE , Roles.class ),
    COUNTER_NAME( ForEach.COUNTER_NAME , String.class , AttrType.VARIABLE ),
    PARALLEL( ForEach.PARALLEL , TBoolean.class ),
    COUNT_COMPLETED_BRANCHES_ONLY( Branches.COUNT_COMPLETED_BRANCHES_ONLY , 
            TBoolean.class ),
    INITIALIZE_PARTNER_ROLE( PartnerLink.INITIALIZE_PARTNER_ROLE , TBoolean.class ),
    FAULT_VARIABLE( Catch.FAULT_VARIABLE , String.class , AttrType.VARIABLE ),
    FAULT_VARIABLE_REF( Throw.FAULT_VARIABLE , VariableDeclaration.class ),
    LANGUAGE( Documentation.LANGUAGE , String.class , AttrType.STRING ),
    VARIABLE( OnEvent.VARIABLE , String.class  , AttrType.VARIABLE ),
    VARIABLE_REF( VariableReference.VARIABLE, VariableDeclaration.class ),
    FAULT_MESSAGE_TYPE( Catch.FAULT_MESSAGE_TYPE , 
            ReferenceableWSDLComponent.class ),
    FAULT_ELEMENT( Catch.FAULT_ELEMENT, GlobalElement.class ),
    SET( Correlation.SET , CorrelationSet.class ), 
    TARGET( CompensateScope.TARGET, CompensationHandlerHolder.class ),
    LINK_NAME( Target.LINK_NAME , Link.class ),
    PARTNER_LINK( PartnerLinkReference.PARTNER_LINK , PartnerLink.class ),
    PORT_TYPE( PortTypeReference.PORT_TYPE , PortType.class ),
    OPERATION( OperationReference.OPERATION, Operation.class ),
    INPUT_VARIABLE( Invoke.INPUT_VARIABLE , VariableDeclaration.class ),
    OUTPUT_VARIABLE( Invoke.OUTPUT_VARIABLE , VariableDeclaration.class ),
    MESSAGE_TYPE( MessageTypeReference.MESSAGE_TYPE , Message.class ),
    ELEMENT( Variable.ELEMENT , GlobalElement.class ),
    PART( PartReference.PART , Part.class ),
    PROPERTY( PropertyReference.PROPERTY , CorrelationProperty.class ),
    TYPE( Variable.TYPE , GlobalType.class ),
    FROM_VARIABLE( ToPart.FROM_VARIABLE , VariableDeclaration.class ),
    MY_ROLE( PartnerLink.MY_ROLE , Role.class ),
    PARTNER_LINK_TYPE( PartnerLink.PARTNER_LINK_TYPE , 
            PartnerLinkType.class),
    PARTNER_ROLE( PartnerLink.PARTNER_ROLE , Role.class ), 
    TO_VARIABLE( FromPart.TO_VARIABLE , VariableDeclaration.class ),
    MESSAGE_EXCHANGE( MessageExchangeReference.MESSAGE_EXCHANGE , 
            MessageExchange.class),
    PROPERTIES( CorrelationSet.PROPERTIES , List.class , 
            CorrelationProperty.class ),
    VARIABLES( Validate.VARIABLES , List.class , VariableDeclaration.class ),
    CONTENT( ContentElement.CONTENT_PROPERTY , String.class , AttrType.STRING), // this is pseudo attribute.
    FAULT_NAME( FaultNameReference.FAULT_NAME , QName.class ), 
    KEEP_SRC_ELEMENT_NAME( Copy.KEEP_SRC_ELEMENT_NAME , TBoolean.class ),
    REFERENCE_SCHEME( ServiceRef.REFERENCE_SCHEME , String.class , AttrType.URI ),
    IGNORE_MISSING_FROM_DATA( Copy.IGNORE_MISSING_FROM_DATA , TBoolean.class ),
    ;
    
    public static enum AttrType {
        STRING,
        NCNAME,
        URI,
        VARIABLE
    }

    BpelAttributes( String name, Class type ) {
        this(name, type, (Class)null);
    }
    
    BpelAttributes( String name, Class type , AttrType attrType ) {
        this(name, type, (Class)null);
        myType = attrType;
    }

    BpelAttributes( String name, Class type, Class subtype ) {
        myAttributeName = name;
        myAttributeType = type;
        myAttributeTypeInContainer = subtype;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    /** {@inheritDoc} */
    public String toString() {
        return myAttributeName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.Attribute#getType()
     */
    /** {@inheritDoc} */
    public Class getType() {
        return myAttributeType;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.Attribute#getName()
     */
    /** {@inheritDoc} */
    public String getName() {
        return myAttributeName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.Attribute#getMemberType()
     */
    /** {@inheritDoc} */
    public Class getMemberType() {
        return myAttributeTypeInContainer;
    }
    
    /**
     * @return type of attribute value
     */
    public AttrType getAttributeType(){
        return myType;
    }
    
    /**
     * @param name String representation of enum.
     * @return Enum that have <code>name</code> representaion.
     */
    public static Attribute forName( String name ){
        for (BpelAttributes attr : values()) {
            if ( attr.getName().equals(name) &&
                    attr.getType().equals( String.class) )
            {
                return attr;
            }
        }
        return null;
    }

    private String myAttributeName;

    private Class myAttributeType;

    private Class myAttributeTypeInContainer;
    
    private AttrType myType;
}
