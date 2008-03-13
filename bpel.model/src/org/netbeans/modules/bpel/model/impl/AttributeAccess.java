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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.BpelReferenceable;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.support.EnumValue;
import org.netbeans.modules.bpel.model.api.references.MappedReference;
import org.netbeans.modules.bpel.model.api.references.SchemaReferenceBuilder;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.api.support.Utils.Pair;
import org.netbeans.modules.bpel.model.impl.references.BpelAttributesType;
import org.netbeans.modules.bpel.model.impl.references.BpelReferenceBuilder;
import org.netbeans.modules.bpel.model.impl.references.WSDLReference;
import org.netbeans.modules.bpel.model.impl.references.WSDLReferenceBuilder;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.netbeans.modules.bpel.model.api.support.Utils;

/**
 * This is "utility" class that incapuslate inside itself
 * all attribute logic.   
 * @author ads
 */
class AttributeAccess {

    AttributeAccess( BpelEntityImpl entity ) {
        myEntity = entity;
    }
    
    TBoolean getBooleanAttribute(Attribute attr ){
        readLock();
        try {
            String value = getEntity().getAttribute(attr);
            return TBoolean.forString( value );
        }
        finally {
            readUnlock();
        }
    }
    
    @SuppressWarnings("unchecked") // NOI18N
    <T extends BpelReferenceable> BpelReference<T> getBpelReference( Attribute attr,
            Class<T> clazz )
    {
        /* 
         * here I suggest that resolving of reference will happen
         * a) right after model is constructed
         * b) each event that could have impact on reference 
         * will be dipatched by inner visitor and this method will 
         * be protected by write lock. 
         */
        readLock();
        try {
            return resolveBpelReference( attr, clazz );
        }
        finally {
            readUnlock();
        }
    }
    
    @SuppressWarnings("unchecked") // NOI18N
    <T extends ReferenceableWSDLComponent> WSDLReference<T> getWSDLReference( 
            Attribute attr , Class<T> clazz )
    {
        readLock();
        try {
            return resolveWSDLReference( attr, clazz );
        }
        finally {
            readUnlock();
        }
    }
    
    @SuppressWarnings("unchecked") // NOI18N
    <T extends ReferenceableSchemaComponent> SchemaReference<T> 
        getSchemaReference( Attribute attr , Class<T> clazz )
    {
        readLock();
        try {
            return resolveSchemaReference( attr, clazz );
        }
        finally {
            readUnlock();
        }   
    }
    
    <T extends BpelReferenceable> List<BpelReference<T>> getBpelReferenceList(
            Attribute attr, Class<T> type )
    {
        readLock();
        try {
            String str = getEntity().getAttribute(attr);
            return getBpelReferenceList(type, str, attr);
        }
        finally {
            readUnlock();
        }
    }
    
    Object getAttributeValueOf( Attribute attr, String stringValue ) {
        if (stringValue == null) {
            return null;
        }
        Class c = attr.getType();
        if (String.class.isAssignableFrom(c)) {
            return stringValue;
        }
        else if (QName.class.isAssignableFrom(c)) {
            return Utils.getQName(stringValue, getEntity());
        }
        else if (Enum.class.isAssignableFrom(c)) {
            return Utils.parse(c, stringValue);
        }
        else if ( Referenceable.class.isAssignableFrom( c )){
            Reference ref = getReferenceValueOf(attr, stringValue, c);
            return ref;
        }
        else if ( List.class.isAssignableFrom( c ) ){
            List list = getReferenceListOf(attr, stringValue);
            return list;
        }
        assert false; // should never reached within this model implementation
        return null;
    }
    
    <T extends ReferenceableWSDLComponent> 
        List<org.netbeans.modules.bpel.model.api.references.WSDLReference<T>> 
        getWSDLReferenceList( Attribute attr, Class<T> type )
    {
        readLock();
        try {
            String str = getEntity().getAttribute(attr);
            return getWSDLReferenceList(type, str , attr );
        }
        finally {
            readUnlock();
        }
    }
    
    <T extends ReferenceableSchemaComponent> List<SchemaReference<T>> 
            getSchemaReferenceList(Attribute attr, Class<T> type )
    {
        readLock();
        try {
            String str = getEntity().getAttribute(attr);
            return getSchemaReferenceList(type, str, attr );
        }
        finally {
            readUnlock();
        }
    }
    
    QName getQNameAttribute( Attribute attr ){
        readLock();
        try {
            String str = getEntity().getAttribute( attr );
            return Utils.getQName( str , getEntity() );
        }
        finally {
            readUnlock();
        }
    }
    
    void setBpelAttribute( Attribute attr, String value )
            throws VetoException
    {
        writeLock();
        try {
            String oldValue = getEntity().getAttribute(attr);
            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(),
                    oldValue, value);
            setAttribute(attr, value);
            getEntity().postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }
    
    void setBpelAttribute(  Attribute attr, Enum value ) {
        writeLock();
        try {
            if ( value instanceof EnumValue ){
                if ( ((EnumValue)value).isInvalid() ){
                    throw new IllegalStateException("Trying to set " + // NOI18N
                            "invalid enumeration value"); // NOI18N
                }
            }
            Object obj = getAttributeValueOf(attr, getEntity().getAttribute(attr));
            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), obj,
                    value);
            setAttribute(  attr , value.toString());
            getEntity().postGlobalEvent(event);
        }
        catch (VetoException e) {
            assert false;
        }
        finally {
            writeUnlock();
        }
    }
    
    void setBpelAttribute( Attribute attr, QName qName ) throws VetoException {
        writeLock();
        try {
            Object obj = getAttributeValueOf(attr, getEntity().getAttribute(attr));
            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), obj,
                    qName);
            String attributeValue = prepareQNameAttribute(qName);
            setAttribute( attr, attributeValue);
            getEntity().postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }
    
    <T extends BpelReferenceable> void setBpelReference( Attribute attr, 
            BpelReference<T> ref )
    {
        writeLock();
        try {
            BpelReference<T> old = getBpelReference(attr, ref.getType());

            String str = ref.getRefString();
            /* ???
             * ref=BpelReferenceBuilder.getInstance().build( ref.getType() , this , str );
             * setting as new value in event new created reference is more appropriate
             * bacause reference can contain wrong referencable for this element
             * ( variable f.e. could be in upper scope, but there exist 
             * another variable in narrow scope ).
             */
            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), old, 
                    ref );
            setAttribute(attr, str);
            getEntity().postGlobalEvent(event);
        }
        catch (VetoException e) {
            assert false;
        }
        finally {
            writeUnlock();
        }
    }
    
    <T extends ReferenceableWSDLComponent> void setWSDLReference( Attribute attr , 
            org.netbeans.modules.bpel.model.api.references.WSDLReference<T> ref )
    {
        writeLock();
        try {
            WSDLReference<T> old = getWSDLReference( attr , ref.getType());
            
            String str = ref.getRefString();

            boolean isQname = ((BpelAttributesType)ref)
                    .getAttributeType() == BpelAttributesType.AttrType.QNAME;
            if (isQname) {
                str = prepareQNameAttribute(ref.getQName());
            }
            /*
             * ref = WSDLReferenceBuilder.getInstance().build( ref.getType() , 
             *              this , str )
             * setting in event new created reference is more appropriate
             * actually.
             */
            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), old,
                   ref );
            
            setAttribute( attr , str);
            getEntity().postGlobalEvent(event);
        }
        catch (VetoException e) {
            assert false;
        }
        finally {
            writeUnlock();
        }
    }
    
    <T extends ReferenceableSchemaComponent> void setSchemaReference( 
            Attribute attr , SchemaReference<T> ref )
    {
        writeLock();
        try {
            SchemaReference<T> old = getSchemaReference( attr , ref.getType() );
            
            String str = prepareQNameAttribute( ref.getQName());
            /*
             * ref = SchemaReferenceBuilder.getInstance().build( ref.getType() , 
             *              this , str )
             * setting new created reference in event is more appropriate.              
             */
            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), old,
                   ref );

            setAttribute( attr, str);

            getEntity().postGlobalEvent(event);
        }
        catch (VetoException e) {
            assert false;
        }
        finally {
            writeUnlock();
        }
    }
    
    <T extends ReferenceableWSDLComponent> void setWSDLReferenceList( 
            Attribute attr, Class<T> type , 
            List<org.netbeans.modules.bpel.model.api.references.WSDLReference<T>> 
            list  )
    {
        writeLock();
        try {
            List<org.netbeans.modules.bpel.model.api.references.WSDLReference<T>> 
                oldList = getWSDLReferenceList( attr, type );
            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), oldList,
                    list );
            StringBuilder builder = new StringBuilder();
            for (org.netbeans.modules.bpel.model.api.references.WSDLReference<T> 
                reference : list) 
            {
                String value = reference.getRefString();

                boolean isQname = 
                    ((BpelAttributesType) reference).getAttributeType() == 
                        BpelAttributesType.AttrType.QNAME;
                if (isQname) {
                    value = prepareQNameAttribute(reference.getQName());
                }
                builder.append( value );
                builder.append( " " );
            }
            String value = "";
            if ( builder.length() >0 ){
                value = builder.substring( 0 , builder.length() -1 );
            }
            setAttribute(attr, value);
            getEntity().postGlobalEvent( event );
        }
        catch (VetoException e) {
            assert false;
        }
        finally {
            writeUnlock();
        }
    }
    
    void removeAttribute( Attribute attr ) {
        writeLock();
        try {
            if ( getEntity().getAttribute(attr) == null ) {
                return;
            }
            Object obj = getAttributeValueOf(attr, getEntity().getAttribute(attr));
            PropertyRemoveEvent event = preRemoveAttribute(attr.getName(), obj);
            setAttribute( attr, null);
            getEntity().postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }
    
    private <T extends ReferenceableWSDLComponent> 
        List<org.netbeans.modules.bpel.model.api.references.WSDLReference<T>> 
            getWSDLReferenceList( Class<T> type, String str , Attribute attr )
    {
        if (str == null) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(str, " ");
        List<org.netbeans.modules.bpel.model.api.references.WSDLReference<T>> 
            list = new LinkedList<org.netbeans.modules.bpel.model.api.references.
            WSDLReference<T>>();
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            WSDLReference<T> ref = WSDLReferenceBuilder.getInstance().build(
                    type, getEntity(), next);
            WSDLReferenceBuilder.getInstance().setAttribute(ref, attr);
            list.add(ref);
        }
        return Collections.unmodifiableList(list);
    }
    
    @SuppressWarnings("unchecked")
    private Reference getReferenceValueOf( Attribute attr, String stringValue, 
            Class c ) 
    {
        if (BpelReferenceable.class.isAssignableFrom(c)) {
            BpelReference ref = BpelReferenceBuilder.getInstance().build(c,
                    getEntity(), stringValue);
            BpelReferenceBuilder.getInstance().setAttribute(ref, attr);
            return ref;
        }
        else if ( ReferenceableWSDLComponent.class.isAssignableFrom( c)){
            WSDLReference ref = WSDLReferenceBuilder.getInstance().build( c , 
                    getEntity() , stringValue );
            WSDLReferenceBuilder.getInstance().setAttribute(ref, attr);
            return ref;
        }
        else if ( ReferenceableSchemaComponent.class.isAssignableFrom( c )){
            SchemaReference ref = SchemaReferenceBuilder.getInstance().build( c , 
                    getEntity() , stringValue );
            SchemaReferenceBuilder.getInstance().setAttribute(ref, attr);
            return ref; 
        }
        assert false;
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private List getReferenceListOf( Attribute attr, String stringValue  ) {
        Class clazz = attr.getMemberType();
        if ( BpelReferenceable.class.isAssignableFrom( clazz ) ){
            return getBpelReferenceList( clazz , stringValue, attr );
        }
        else if ( ReferenceableWSDLComponent.class.isAssignableFrom( clazz )){
            return getWSDLReferenceList( clazz , stringValue , attr );
        }
        else if ( ReferenceableSchemaComponent.class.isAssignableFrom( clazz )){
            return getSchemaReferenceList( clazz , stringValue, attr );
        }
        assert false;
        return null;
    }
    
    private <T extends BpelReferenceable> List<BpelReference<T>> 
            getBpelReferenceList( Class<T> type, String str, Attribute attr )
    {
        if (str == null) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(str, " ");
        List<BpelReference<T>> list = new LinkedList<BpelReference<T>>();
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            BpelReference<T> ref = BpelReferenceBuilder.getInstance().build(
                    type, getEntity(), next);
            BpelReferenceBuilder.getInstance().setAttribute(ref, attr);
            list.add(ref);
        }
        return Collections.unmodifiableList(list);
    }
    
    private <T extends ReferenceableSchemaComponent> List<SchemaReference<T>> 
            getSchemaReferenceList( Class<T> type, String str, Attribute attr )
    {
        if (str == null) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(str, " ");
        List<SchemaReference<T>> list = new LinkedList<SchemaReference<T>>();
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            SchemaReference<T> ref = SchemaReferenceBuilder.getInstance()
                    .build(type, getEntity(), next);
            SchemaReferenceBuilder.getInstance().setAttribute(ref, attr);
            list.add(ref);
        }
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Prepare XML for setting qName as value. 1) Corresponded namespace
     * possibly should be added. 2) attribute value will be returned as String (
     * prefix will be determined and value will be formed ).
     */
    private String prepareQNameAttribute( QName qName ) {
        String attributeValue;
        String prefix = qName.getPrefix();
        String localPart = qName.getLocalPart();
        String uri = qName.getNamespaceURI();

        ExNamespaceContext context = getEntity().getNamespaceContext();

        String existedPrefix = context.getPrefix(uri);
        if (existedPrefix != null) {
            attributeValue = existedPrefix + ":" + localPart;
        }
        else {
            boolean isGoodPrefix = true;

            /*
             * check for presence prefix already in context. If It exists then
             * it have different uri ( becuase we don't have this uri here at
             * all - cathed previous case in "if" ).
             */
            if ((prefix != null) && (prefix.length() > 0)) {
                Iterator<String> iterator = context.getPrefixes();
                while (iterator.hasNext()) {
                    String pref = iterator.next();
                    if (pref.equals(prefix)) {
                        isGoodPrefix = false;
                        break;
                    }
                }
            }
            else {
                isGoodPrefix = false;
            }

            try {
                if (isGoodPrefix) {
                    context.addNamespace(prefix, uri);
                }
                else {
                    prefix = context.addNamespace(uri);
                }
            }
            catch (InvalidNamespaceException e) {
                /*
                 * we should not appear here - becuase we check presence of
                 * prefix..... ???? wrong prefix ( not NCName) ??? ( Qname
                 * doens't check correctness of prefix ).
                 */
                assert false;
            }

            // we have added namespace to context ,
            // at least we set value of attribute
            attributeValue = prefix + ":" + localPart;
        }
        return attributeValue;
    }
    
    private <T extends BpelReferenceable> BpelReference<T> resolveBpelReference( 
            Attribute attr , Class<T> clazz ) 
    {
        if ( getEntity().getAttribute( attr ) == null ){
            return null;
        }
        BpelReference<T> ref = BpelReferenceBuilder.getInstance().build( clazz , 
                getEntity() , attr ); 
        return ref;
    }
    
    private <T extends ReferenceableWSDLComponent> WSDLReference<T> 
        resolveWSDLReference( Attribute attr , Class<T> clazz ) 
    {
        if ( getEntity().getAttribute( attr ) == null ){
            return null;
        }
        WSDLReference<T> ref = WSDLReferenceBuilder.getInstance().build( clazz , 
                getEntity() , attr );
        return ref;
    }
    
    private <T extends ReferenceableSchemaComponent> SchemaReference<T> 
        resolveSchemaReference( Attribute attr, Class<T> clazz )
    {
        if ( getEntity().getAttribute( attr ) == null ){
            return null;
        }
        SchemaReference<T> ref = SchemaReferenceBuilder.getInstance().build( 
                clazz , getEntity() ,  attr ); 
        return ref;
    }
    
    <T extends BpelReferenceable> void setBpelReferenceList( 
            Attribute attr, Class<T> type , List list  )
    {
        writeLock();
        try {
            List<BpelReference<T>> oldList = getBpelReferenceList( attr, type );
            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), oldList,
                    list );
            StringBuilder builder = new StringBuilder();
            for ( Object obj : list) {
                assert obj instanceof BpelReference;
                BpelReference reference = (BpelReference)obj;
                builder.append( reference.getRefString());
                builder.append( " " );
            }
            String value = "";
            if ( builder.length() >0 ){
                value = builder.substring( 0 , builder.length() -1 );
            }
            setAttribute(attr, value);
            getEntity().postGlobalEvent( event );
        }
        catch (VetoException e) {
            assert false;
        }
        finally {
            writeUnlock();
        }
    }
    
    void setText( String text ) throws VetoException {
        writeLock();
        try {
            String oldValue = getEntity().getCorrectedText();
            PropertyUpdateEvent event = preUpdateAttribute(
                    ContentElement.CONTENT_PROPERTY, oldValue , text );
            getEntity().setText( ContentElement.CONTENT_PROPERTY , text );
            getEntity().postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }
    
    /**
     * This method changes old reference <code>reference</code>
     * to new reference with referenceable object <code>subject</code>
     */
    @SuppressWarnings("unchecked") 
    void updateReference( Reference reference, BpelReferenceable subject ) 
    {
        assert reference !=null;
        if ( !(reference instanceof BpelReference )) {
            return;
        }
        Attribute attr = null;
        if ( reference instanceof MappedReference) {
            attr = ((MappedReference) reference).getAttribute();
        }
        if (!(attr instanceof BpelAttributes)) {
            return;
        }
        Class type = ((BpelAttributes) attr).getType();
        if (BpelReferenceable.class.isAssignableFrom(type)) {
            BpelReference ref = getEntity().createReference(subject, type);
            if (ref.equals(reference)) {
                // don't need to change attribute value.
                return;
            }
            setBpelReference(attr, ref);
        }
        else if (List.class.isAssignableFrom(type)) {
            Class memberType = attr.getMemberType();
            assert BpelReferenceable.class.isAssignableFrom(memberType);
            List<BpelReference> oldList = getBpelReferenceList(attr, memberType);
            List<BpelReference> newList = new ArrayList<BpelReference>(oldList
                    .size());
            boolean change = false;
            for (BpelReference ref : oldList) {
                if (reference.equals(ref)) {
                    BpelReference newRef = getEntity().createReference(subject,
                            memberType);
                    if (newRef.equals(reference)) {
                        // this reference is not changed, skip changes
                        newList.add(ref);
                    }
                    else {
                        change = true;
                        newList.add(newRef);
                    }
                }
                else {
                    newList.add(ref);
                }
            }
            if (change) {
                setBpelReferenceList(attr, memberType, newList);
            }
        }
        else {
            assert false;
        }
    }
    
    void fireUpdateReference( Reference reference ){
        assert reference !=null;
        Attribute attr = null;
        if ( reference instanceof MappedReference) {
            attr = ((MappedReference) reference).getAttribute();
        }
        if (!(attr instanceof BpelAttributes)) {
            return;
        }
        String name = attr.getName();
        try {
            PropertyUpdateEvent event = preUpdateAttribute( name , reference , 
                reference);
            getEntity().postGlobalEvent( event );
        }
        catch ( VetoException e ){
            /*
             *  In this case VetoException could appear becuase
             *  this method could be called not only in 'sync'.
             *  But in this could happen only in the case 
             *  whren old reference was bad ( actually 
             *  there are no old reference - this is the same 
             *  reference , we just need to reresolve it ). 
             */
            // ignore it
        }
    }
    
    void handleAttributeChange( Node oldAttr, Node newAttr ) {
        Node notNull = oldAttr == null ? newAttr : oldAttr;
        if (notNull == null) {
            return;
        }
        Node other = notNull == oldAttr ? newAttr : oldAttr;
        if ( !isChanged( notNull , other) ){
            return;
        }

        String name = notNull.getLocalName();

        /*
         * Here we take care only about attributes without namespace.
         */
        if ( notNull.getNamespaceURI()!= null ){
            return;
        }
        
        if ( handleNamespaceChange( oldAttr, newAttr ) ){
            return;
        }

        Attribute[] attrs = getEntity().getDomainAttributes();
        BpelAttributes bpelAttr = null;
        for (Attribute attr : attrs) {
            if (attr.getName().equals(name)) {
                if ( attr instanceof BpelAttributes ){
                    bpelAttr = (BpelAttributes)attr;
                }
                break;
            }
        }

        firePropertyUpdateEvent(oldAttr, newAttr, name, bpelAttr);
    }
    
    /**
     * Check whether attribute is correspond to namespace and perform
     * action respectively.
     * Return true if attribute is namespace declaration.
     */
    private boolean handleNamespaceChange( Node oldAttr, Node newAttr ) {
        Node notNull = oldAttr == null ? newAttr : oldAttr;
        assert notNull instanceof Attr;
        Attr attr = (Attr) notNull;
        if (NamespaceOptimizer.XMLNS.equals(attr.getName()) ||
                NamespaceOptimizer.XMLNS.equals(attr.getPrefix()))
        {
            String oldNamespaceURI = null;
            if ( oldAttr != null ){
                oldNamespaceURI = ((Attr) oldAttr).getValue(); // it cannot be null
                // 'namespaceURI' could be just prefix that already defined in another declaration
                // ( this declaration WAS NOT CHANGED , so we can access to it via NamespaceContext).
                // So we need to check presence such prefix and try to access to namespace URI
                ExNamespaceContext context = getEntity().getNamespaceContext();
                String ns = context.getNamespaceURI(oldNamespaceURI); // we assume that 'namespaceURI' is prefix
                if (ns != null) {
                    oldNamespaceURI = ns; // only if we found ns declaration
                                            // with 'namespaceURI' as prefix we
                                            // reassign
                }
            }
            
            // here we determine the prefix 
            assert notNull instanceof Attr;
            String prefix;
            if ( ((Attr)notNull).getName().equals( NamespaceOptimizer.XMLNS) ){
                prefix = null;
            }
            else {
                prefix = notNull.getLocalName();
            }
            
            /*
             *  We start from current entity and walk through all its children.
             *  We need to find all elements that have reference ( QName type )
             *  attribute and perform firing of event.
             */
            
            Set<String> affectedPrefixes = new HashSet<String>();
            affectedPrefixes.add( prefix );
            /*
             * The set above need for avoiding cycles.
             * We will collect all prefixes that was visited ( as reference )
             * and will not call method handleNamespaceChange for prefixes in this set.
             * 
             * The situation that can happen :
             * xmlns:ns1="ns2" xmlns:ns2="ab"
             * this changed to 
             * xmlns:ns1="ns2" xmlns:ns2="ns1"
             * then we will call recursively 'handleNamespaceChange'  for
             * first prefix and it will call recursively 'handleNamespaceChange' 
             * for second prefix and this will be infinite loop.
             */
            
            handleNamespaceChange( getEntity() , prefix , oldNamespaceURI , 
                    affectedPrefixes);
            return true;
        }
        return false;
    }
    
    private void handleNamespaceChange( BpelEntityImpl entity , String prefix , 
            String oldNamespaceURI , Set<String> affectedPrefixes )
    {
        if ( entity == getEntity() || !checkPrefixPresence( entity, prefix )) {
            /* We need to fire event for entity where namespace was changed.
             * for other entities - (deep) children of getEntity() we need to check
             * presence of namespace declaration with the same prefix. 
             * If it exists then we  
             * don't need to go further for firing event.
             */ 
            fireEventOnNSChange(entity, prefix, oldNamespaceURI);
        }
        
        /*
         * This handle situation for prefixes that are also affected 
         * when namespace declaration is changed. 
         * F.e. let we have tag with decl:
         * xmlns:ns1="ns2" xmlns:ns2="aaa"
         * When ns2 is changed somehow then ns1 should be also changed
         * because it is referenced to "ns2". 
         *   
         */
        handleReferencedNS(entity, prefix, oldNamespaceURI , affectedPrefixes );
        
        
        List<BpelEntityImpl> children = entity.getChildren( BpelEntityImpl.class );
        for (BpelEntityImpl child : children) {
            handleNamespaceChange( child , prefix, oldNamespaceURI , 
                    affectedPrefixes );
        }
    }

    private void handleReferencedNS( BpelEntityImpl entity, String prefix, 
            String oldNamespaceURI ,  Set<String> affectedPrefixes ) 
    {
        NamedNodeMap map = entity.getPeer().getAttributes();
        for ( int i=0; i<map.getLength(); i++) {
            Node node = map.item(i);
            assert node instanceof Attr;
            Attr attribute = (Attr) node;
            String referencedPrefix = null;
            boolean isNS = false;
            if (NamespaceOptimizer.XMLNS.equals(attribute.getName())){
                referencedPrefix = null;
                isNS = true;
            }
            if ( NamespaceOptimizer.XMLNS.equals(attribute.getPrefix())){
                referencedPrefix = attribute.getLocalName();
                isNS = true;
            }
            
            if ( isNS ) {
                String value = attribute.getValue();
                if ( value == null || affectedPrefixes.contains( prefix) ){
                    continue;
                }
                if ( value.equals( prefix ) ){ 
                    affectedPrefixes.add( referencedPrefix );
                    // this is the case when we found NS decl referenced to changed NS decl
                    handleNamespaceChange( entity, referencedPrefix, 
                            oldNamespaceURI , affectedPrefixes );
                }
            }
        }
    }


    private void fireEventOnNSChange( BpelEntityImpl entity, String prefix, 
            String oldNamespaceURI ) 
    {
        Attribute[] attrs = entity.getDomainAttributes();
        for (Attribute attribute : attrs) {
            String attrValue = entity.getAttribute( attribute );
            if ( attrValue == null ){
                continue;
            }
            if (!( attribute instanceof BpelAttributes )){
                continue;
            }
            BpelAttributes attr = (BpelAttributes) attribute;
            
            Pair pair = getOldNewValues( entity , attr , prefix , oldNamespaceURI );
            if ( pair != null ) {
                try {
                    PropertyUpdateEvent event = preUpdateAttribute( entity ,
                        attribute.getName(), pair.getFirst() , pair.getSecond() );
                    getEntity().postGlobalEvent( event , true );
                }
                catch ( VetoException e ){
                    // here actually VetoException. Becuase inner dispatchers
                    // should not throws this excpetions in "sync" mode.
                    // so we just ignore it.
                    assert false;
                }
            }
        }
    }


    private void setAttribute( Attribute attr, String str ) {
        getEntity().setAttribute( attr.getName() , attr, str);
    }

    private void firePropertyUpdateEvent( Node oldAttr, Node newAttr, String name, 
            BpelAttributes bpelAttr ) 
    {
        if ( bpelAttr == null ){
            return;
        }
        String oldString = oldAttr == null ? null : oldAttr.getNodeValue();
        String newString = newAttr == null ? null : newAttr.getNodeValue();
        
        Object old = oldString;
        Object value = newString;
        if ( bpelAttr != null ){
            old = getAttributeValueOf( bpelAttr , oldString );
            value = getAttributeValueOf( bpelAttr , newString );
        }

        try {
            PropertyUpdateEvent event = preUpdateAttribute( name , old , value );
            getEntity().postGlobalEvent( event , true );
        }
        catch ( VetoException e ){
            // this cannot happen because inner dispatchers should not
            // throw VetoException in "sync" mode. So we just ignore it.
            assert false;
        }
    }
    
    private Pair getOldNewValues( BpelEntityImpl entity, BpelAttributes attr ,
            String prefix , String oldNamespaceURI ) 
    {
        String attrValue = entity.getAttribute( attr );
        Class clazz = attr.getType();
        if ( QName.class.isAssignableFrom( clazz) ) {
            return getOldNewQNames(entity, prefix, attrValue, oldNamespaceURI );    
        }
        else {
            return getOldNewReferences( attr, attrValue, prefix , clazz );
        }
    }
    
    private Pair getOldNewQNames( BpelEntityImpl entity, String prefix, 
            String attrValue , String oldNamespaceURI) 
    {
        String[] splited = new String[2];
        Utils.splitQName(attrValue, splited);
        if (!Utils.isEquals(prefix, splited[0])) {
            return null;
        }
        QName newValue = Utils.getQName(attrValue, entity);
        QName old = null;
        if (oldNamespaceURI == null) { // this is the case when NEW NS decl appeares
            /*
             * Create QName with parent context because getEntity() already have
             * ADDED namespace delcaration. But we need context without ADDED
             * namespace declaration.
             */
            BpelEntityImpl parent = getEntity().getParent();
            old = parent == null ? new QName(null, splited[1]) : Utils
                    .getQName(attrValue, parent);

        }
        else {

            old = new QName(oldNamespaceURI, splited[1]);
        }

        return new Pair<QName>(old, newValue);
    }
    
    private Pair getOldNewReferences(BpelAttributes attr , String value,
            String prefix, Class clazz )
    {
        if ( Referenceable.class.isAssignableFrom( clazz )){
            if ( BpelReferenceable.class.isAssignableFrom( clazz ) ){
                // we don't need to fire events for inner ( inside BPEL ) references.
                return null;
            }
            Reference ref = getReferenceValueOf( attr , value , clazz );
            if ( ref instanceof BpelAttributesType && 
                    ((BpelAttributesType)ref).getAttributeType()== 
                        BpelAttributesType.AttrType.QNAME) 
            { 
                /* 
                 * We take care only on QName types refereces. 
                 * Other references SHOULD be handled by clients of OM.
                 * 
                 * There is one more possibility for this - 
                 * inner dispatcher can provide additional event firing
                 * for NCName type references ( that can depends on 
                 * changed references ).   
                 * 
                 * The example of such dependency - "operation" attribute.
                 * It depdends from either "partnerLink" attribute or "portType"
                 * attribute. So when they are changed - "operation" attribute
                 * should be also 'marked' as changed.
                 * 
                 *  There is more difficult situation - f.e. 'part' is resolved
                 *  via "variable" reference. So when referenced "variable"
                 *  is changed then we need also fire event about change 'port'
                 *  attribute.
                 *  Currently handling this situation is responsibility of client of OM.  
                 */
                String[] splited = new String[2]; 
                Utils.splitQName( value , splited );
                if ( !Utils.isEquals( prefix , splited[0] ) ){
                    return null;
                }
                return new Pair<Reference>( ref , ref );
            }
        }
        else if ( List.class.isAssignableFrom(clazz) ){
            if (Referenceable.class.isAssignableFrom(attr.getMemberType())){
                if ( BpelReferenceable.class.isAssignableFrom( 
                        attr.getMemberType() ) )
                {
                    // we don't need to fire events for inner ( inside BPEL ) references.
                    return null;
                }
                // we neeed to check presence elements in list with appropriate prefix
                // TODO : this is BAD! we need to check prefix! But this is not a bug, will need to do this later.  
                
                List list = getReferenceListOf( attr , value );
                return new Pair<List>( list , list );
            }
            else {
                assert false;
            }
        }
        return null;   
    }

    /*
     * Checks presence of prefix <code>prefix</code> for <code>entity</code>
     * ( declaration in this tag ).
     * Returns true if prefix is found.
     */
    private boolean checkPrefixPresence( BpelEntityImpl entity, String prefix ) {
        NamedNodeMap map = entity.getPeer().getAttributes();
        for ( int i=0; i<map.getLength(); i++) {
            Node node = map.item(i);
            assert node instanceof Attr;
            Attr attribute = (Attr) node;
            if ( NamespaceOptimizer.XMLNS.equals(attribute.getName() )) {
                if ( prefix == null ){
                    return true;
                }
            }
            else if (NamespaceOptimizer.XMLNS.equals( attribute.getPrefix() )){
                return attribute.getLocalName().equals( prefix );
            }
        }
        return false;
    }

    private boolean isChanged( Node attr1 , Node attr2){
        assert attr1!=null;
        if (attr2 != null) {
            String v1 = attr1.getNodeValue();
            String v2 = attr2.getNodeValue();

            // check changes is attribute value . If it doesn't change - skip it.
            if (v1 == null) {
                if (v2 == null) {
                    return false;
                }
            }
            else {
                if (v1.equals(v2)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    //==========================================================================
    //
    // Event framework.
    //
    //==========================================================================
    
    private PropertyUpdateEvent preUpdateAttribute( String attrName,
            Object oldValue, Object value ) throws VetoException
    {
        return preUpdateAttribute( getEntity() , attrName , oldValue , value );
    }
    
    private PropertyUpdateEvent preUpdateAttribute( BpelEntityImpl entity,
            String attrName, Object oldValue, Object value ) throws VetoException
    {
        entity.checkDeleted();
        PropertyUpdateEvent event = new PropertyUpdateEvent(entity.getModel().
                getSource(), entity, attrName, oldValue, value);
        entity.getModel().preInnerEventNotify(event);
        return event;
    }
    
    private PropertyRemoveEvent preRemoveAttribute( String name, Object obj ) {
        getEntity().checkDeleted();
        PropertyRemoveEvent event = new PropertyRemoveEvent(getEntity().getModel()
                .getSource(), getEntity(), name, obj);

        try {
            getEntity().getModel().preInnerEventNotify(event);
            return event;
        }
        catch (VetoException e) {
            assert false;
        }
        return null;
    }
    
    //==========================================================================
    
    private BpelEntityImpl getEntity(){
        return myEntity;
    }
    
    private void readLock(){
        getEntity().readLock();
    }
    
    private void readUnlock(){
        getEntity().readUnlock();
    }
    
    private void writeLock(){
        getEntity().writeLock();
    }
    
    private void writeUnlock(){
        getEntity().writeUnlock();
    }
    
    private BpelEntityImpl myEntity;
}
