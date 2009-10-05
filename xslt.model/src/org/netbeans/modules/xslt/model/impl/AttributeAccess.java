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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.QualifiedNameable;
import org.netbeans.modules.xslt.model.ReferenceableXslComponent;
import org.netbeans.modules.xslt.model.XslReference;
import org.netbeans.modules.xslt.model.enums.EnumValue;


/**
 * @author ads
 *
 */
class AttributeAccess {

    AttributeAccess( XslComponentImpl component ){
        myComponent = component;
    }
    
    Object getAttributeValueOf( Attribute attr, String stringValue )
    {
        if (stringValue == null) {
            return null;
        }
        Class clazz = attr.getType();
        if (String.class.isAssignableFrom(clazz)) {
            return stringValue;
        } 
        else {
            for( AttributeValueFactory factory : Lazy.FACTORIES ) {
                if ( factory.isApplicable(attr)) {
                    return factory.getValue(this, attr, stringValue);
                }
            }
        }
        assert false;
        return null;
    }
    
    <T extends QualifiedNameable> GlobalReferenceImpl<T> resolveGlobalReference(
            Class<T> clazz, String value )
    {
        return value == null ? null : new GlobalReferenceImpl<T>(clazz, 
                getComponent(),value);
    }
    
    <T extends QualifiedNameable> List<XslReference<T>> 
        resolveGlobalReferenceList(Class<T> clazz, String value )
    {
        if (value == null) {
            return null;
        }
        for( ReferenceListResolveFactory factory : 
            ReferenceListResolveFactory.Factories.FACTORIES)
        {
            if ( factory.isApplicable( clazz )) {
                return factory.resolve( this , clazz, value);
            }
        }
        
        // in the case when we didn't find appropriate custom resolver
        // we use standard resolving
        StringTokenizer tokenizer = new StringTokenizer(value, " ");
        List<XslReference<T>> references = new LinkedList<XslReference<T>>();
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            XslReference<T> ref = resolveGlobalReference(clazz, next);
            references.add(ref);
        }
        return Collections.unmodifiableList(references);
    }
    
    List<QName> getQNameList( String value ){
        if (value == null) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(value, " ");
        List<QName> result = new LinkedList<QName>();
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            result.add( QNameBuilder.createQName( getComponent(), next) );
        }
        return Collections.unmodifiableList(result);
    }
    
    XslComponentImpl getComponent() {
        return myComponent;
    }
    
    static class Lazy {
        
        static final Collection<AttributeValueFactory> FACTORIES = 
            new LinkedList<AttributeValueFactory>();
        
        static {
            FACTORIES.add( new EnumValueFactory() );
            FACTORIES.add( new DoubleValueFactory() );
            FACTORIES.add( new AttributeValueTemplateFactory() );
            FACTORIES.add( new QNameValueFactory() );
            FACTORIES.add( new BigDecimalValueFactory() );
            FACTORIES.add( new GlobalReferenceValueFactory() );
            FACTORIES.add( new ReferncesListValueFactory() );
            FACTORIES.add( new StringListValueFactory() );
        }
    }
    
    private XslComponentImpl myComponent;
}

interface AttributeValueFactory {
    boolean isApplicable( Attribute attribute );
    
    Object getValue( AttributeAccess access , Attribute attribute, 
            String value );
}

class EnumValueFactory implements AttributeValueFactory {

    @SuppressWarnings("unchecked")
    public Object getValue( AttributeAccess access, Attribute attribute, 
            String value ) 
    {
        Class clazz = attribute.getType();
        Object[] objs = clazz.getEnumConstants();
        assert clazz.isAssignableFrom( EnumValue.class );
        
        Object invalid = null;
        for (Object object : objs) {
            if ( ((EnumValue)object).isInvalid() ) {
                invalid = object;
            }
            if ( value.equals( object.toString()) ) {
                return object;
            }
        }
        return invalid;
    }

    public boolean isApplicable( Attribute attribute ) {
        return Enum.class.isAssignableFrom( attribute.getType() );
    }
    
}

class DoubleValueFactory implements AttributeValueFactory {

    public Object getValue( AttributeAccess access, Attribute attribute, 
            String value ) 
    {
        return Double.parseDouble( value );
    }

    public boolean isApplicable( Attribute attribute ) {
        return Double.class.isAssignableFrom( attribute.getType() );
    }
    
}

class AttributeValueTemplateFactory implements AttributeValueFactory {

    public Object getValue( AttributeAccess access, Attribute attribute, 
            String value ) 
    {
        return AttributeValueTemplateImpl.creatAttributeValueTemplate( 
                access.getComponent() , value );
    }

    public boolean isApplicable( Attribute attribute ) {
        return AttributeValueTemplate.class.isAssignableFrom( attribute.getType() );
    }
    
}

class QNameValueFactory implements AttributeValueFactory {

    public Object getValue( AttributeAccess access, Attribute attribute, String value ) {
        return QNameBuilder.createQName( access.getComponent() , value );
    }

    public boolean isApplicable( Attribute attribute ) {
        return QName.class.isAssignableFrom( attribute.getType() );
    }
    
}

class BigDecimalValueFactory implements AttributeValueFactory {

    public Object getValue( AttributeAccess access, Attribute attribute, 
            String value ) 
    {
        BigDecimal dec = null;
        try {
            dec = new BigDecimal( value );
        }
        catch( NumberFormatException exc ) {
            // ignore this exception
        }
        return dec;
    }

    public boolean isApplicable( Attribute attribute ) {
        return BigDecimal.class.isAssignableFrom( attribute.getType() );
    }
    
}

class GlobalReferenceValueFactory implements AttributeValueFactory {

    @SuppressWarnings("unchecked")
    public Object getValue( AttributeAccess access, Attribute attribute, 
            String value ) 
    {
        return access.resolveGlobalReference( 
                (Class<? extends QualifiedNameable>)attribute.getClass(), 
                value );
    }

    public boolean isApplicable( Attribute attribute ) {
        return ReferenceableXslComponent.class.isAssignableFrom( 
                attribute.getType() );
    }
    
}

class ReferncesListValueFactory implements AttributeValueFactory {

    @SuppressWarnings("unchecked")
    public Object getValue( AttributeAccess access, Attribute attribute, 
            String value ) 
    {
        return access.resolveGlobalReferenceList( attribute.getMemberType() , 
                value );
    }

    @SuppressWarnings("unchecked")
    public boolean isApplicable( Attribute attribute ) {
        return List.class.isAssignableFrom( attribute.getType() ) && 
            attribute.getMemberType().isAssignableFrom( 
                    ReferenceableXslComponent.class );
    }
    
}

class StringListValueFactory implements AttributeValueFactory {

    public Object getValue( AttributeAccess access, Attribute attribute, String value ) {
        if (value == null) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(value, " ");
        List<String> ret = new LinkedList<String>();
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            ret.add(next);
        }
        return Collections.unmodifiableList(ret);
    }

    @SuppressWarnings("unchecked")
    public boolean isApplicable( Attribute attribute ) {
        return List.class.isAssignableFrom( attribute.getType() ) && 
            attribute.getMemberType().isAssignableFrom( String.class );
    }
    
}
