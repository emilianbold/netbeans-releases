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
package org.netbeans.modules.xslt.model.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.model.ApplyTemplates;
import org.netbeans.modules.xslt.model.AsSpec;
import org.netbeans.modules.xslt.model.AttrValueTamplateHolder;
import org.netbeans.modules.xslt.model.AttributeSet;
import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.CallTemplate;
import org.netbeans.modules.xslt.model.CharacterMap;
import org.netbeans.modules.xslt.model.CollationSpec;
import org.netbeans.modules.xslt.model.CopyNamespacesSpec;
import org.netbeans.modules.xslt.model.DisableOutputExcapingSpec;
import org.netbeans.modules.xslt.model.FormatSpec;
import org.netbeans.modules.xslt.model.XslModelReference;
import org.netbeans.modules.xslt.model.InheritNamespacesSpec;
import org.netbeans.modules.xslt.model.Key;
import org.netbeans.modules.xslt.model.LangSpec;
import org.netbeans.modules.xslt.model.NamespaceSpec;
import org.netbeans.modules.xslt.model.Output;
import org.netbeans.modules.xslt.model.Param;
import org.netbeans.modules.xslt.model.QualifiedNameable;
import org.netbeans.modules.xslt.model.SelectSpec;
import org.netbeans.modules.xslt.model.Sort;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.Template;
import org.netbeans.modules.xslt.model.TestSpec;
import org.netbeans.modules.xslt.model.TypeSpec;
import org.netbeans.modules.xslt.model.UseAttributesSetsSpec;
import org.netbeans.modules.xslt.model.UseCharacterMapsSpec;
import org.netbeans.modules.xslt.model.ValidationSpec;
import org.netbeans.modules.xslt.model.WithParam;
import org.netbeans.modules.xslt.model.enums.Annotaions;
import org.netbeans.modules.xslt.model.enums.DefaultValidation;
import org.netbeans.modules.xslt.model.enums.Standalone;
import org.netbeans.modules.xslt.model.enums.TBoolean;
import org.netbeans.modules.xslt.model.enums.Validation;


/**
 * @author ads
 *
 */
enum XslAttributes implements Attribute {
    HREF( XslModelReference.HREF , String.class ),
    VALIDATION( ValidationSpec.VALIDATION , Validation.class ),
    COPY_NAMESPACES( CopyNamespacesSpec.COPY_NAMESPACES , TBoolean.class ), 
    TEST ( TestSpec.TEST , String.class), 
    REQUIRED( Param.REQUIRED , TBoolean.class ), 
    TUNNEL( Param.TUNNEL , TBoolean.class ), 
    ID( Stylesheet.ID , String.class ), 
    MATCH( Template.MATCH , String.class ), 
    MODE ( ApplyTemplates.MODE , String.class ),
    MODES( Template.MODE, List.class , QName.class ),  // this is mode attribute in Template class, it has different value type
    DISABLE_OUTPUT_ESCAPING( DisableOutputExcapingSpec.DISABLE_OUTPUT_ESCAPING , 
            TBoolean.class ), 
    AVT_NAME( AttrValueTamplateHolder.NAME , AttributeValueTemplate.class ), 
    NAMESPACE( NamespaceSpec.NAMESPACE , AttributeValueTemplate.class ), 
    SELECT( SelectSpec.SELECT , String.class ), 
    PRIORITY( Template.PRIORITY , Double.class ), 
    NAME( QualifiedNameable.NAME , QName.class ),
    USE_ATTRIBUTE_SETS( UseAttributesSetsSpec.USE_ATTRIBUTE_SETS , List.class , 
            AttributeSet.class ), 
    SEPARATOR( org.netbeans.modules.xslt.model.Attribute.SEPARATOR, 
            AttributeValueTemplate.class ), 
    NAME_OF_CALL_TMPL( CallTemplate.NAME , Template.class ), 
    NAME_OF_REF_PARAM( WithParam.NAME , WithParam.class ), 
    COLLATION( CollationSpec.COLLATION , String.class ), 
    INHERIT_NAMESPACES( InheritNamespacesSpec.INHERIT_NAMESPACES, TBoolean.class ),
    ENCODING( Output.ENCODING, String.class ),
    LANG( LangSpec.LANG , AttributeValueTemplate.class ),
    FORMAT( FormatSpec.FORMAT, AttributeValueTemplate.class ),
    INDENT( Output.INDENT , TBoolean.class ),
    STANDALONE( Output.STANDALONE , Standalone.class ),
    TYPE( TypeSpec.TYPE , QName.class ),
    UNDECLARE_PREFIXES( Output.UNDECLARE_PREFIXES, TBoolean.class ),
    USE_CHARACTER_MAPS( UseCharacterMapsSpec.USE_CHARACTER_MAPS , List.class , 
            CharacterMap.class ),
    CASE_ORDER( Sort.CASE_ORDER , AttributeValueTemplate.class ),
    DATA_TYPE( Sort.DATA_TYPE , AttributeValueTemplate.class ),
    ORDER( Sort.ORDER, AttributeValueTemplate.class ),
    STABLE( Sort.STABLE , TBoolean.class ),
    USE( Key.USE , String.class ),
    AS( AsSpec.AS, String.class ),
    XPATH_DEFAULT_NAMESPACE( Stylesheet.XPATH_DEFAULT_NAMESPACE , String.class ),
    VERSION( Stylesheet.VERSION , BigDecimal.class ),
    INPUT_TYPE_ANNOTAIONS( Stylesheet.INPUT_TYPE_ANNOTAIONS, Annotaions.class ), 
    DEFAULT_VALIDATION( Stylesheet.DEFAULT_VALIDATION, DefaultValidation.class ),
    EXTENSION_ELEMENT_PREFIXES( Stylesheet.EXTENSION_ELEMENT_PREFIXES, List.class,
            String.class ),
    DEFAULT_COLLATION( Stylesheet.DEFAULT_COLLATION, List.class, 
            String.class ),
    EXCLUDE_RESULT_PREFIXES( Stylesheet.EXCLUDE_RESULT_PREFIXES, List.class,
            String.class ),
    ;

    XslAttributes(String name, Class type, Class memberType) {
        this.myName = name;
        this.myType = type;
        this.myMemberType = memberType;
    }
        
    XslAttributes( String name , Class type ) {
        this( name , type , null );
    }
    
    public String getName() {
        return myName;
    }
    
    public Class getType() {
        return myType;
    }
    
    public Class getMemberType() {
        return myMemberType;
    }
    
    private final String myName;
    private final Class myType;
    private final Class myMemberType;

}
