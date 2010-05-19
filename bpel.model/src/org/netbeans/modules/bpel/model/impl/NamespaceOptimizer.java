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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.bpel.model.impl.services.InnerEventDispatcherAdapter;
import org.netbeans.modules.bpel.model.impl.services.MarkBuilderElement;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent.PrefixAttribute;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author ads
 * This class removes namespaces added 
 * to element as result of setting QName attribute in unattached element.
 * Any such namespace ( is any ) should be removed from current element
 * and moved to root element.  
 * 
 * Moving namespace to root element already implemented in XAM 
 * but it works ONLY for copied element.
 * This service need care about existing namespace and change 
 * new prefix with existing namespace to existing prefix.
 * This is done in preDispatch.  
 * 
 * Moving to root namespaces declaration that is not found in root
 * is performed in postDispatch. Also one need to care about 
 * updating prefix appropriately because it can be changed after moving
 * namespace to root. 
 * 
 * This service need two more services:  one will
 * identified element as created via builder ( cookie flag ),
 * second should remove this flag from cookie after element is inserted 
 * into tree. Only elements with this flag needs to be handled by this
 * service. Because otherwise there will be double work for "copied" element. 
 */
public class NamespaceOptimizer extends InnerEventDispatcherAdapter {
    
    static final String XMLNS = "xmlns";        // NOI18N

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {

        boolean flag = ( event instanceof EntityInsertEvent ) || 
                ( event instanceof EntityUpdateEvent ) ||
                ( event instanceof ArrayUpdateEvent );
        
        if ( !flag ) {
            return false;
        }
        
        if ( event.getParent().getBpelModel().inSync() ) {
            return false;
        }
        
        if ( !((BpelEntityImpl)event.getParent()).isInTree() ) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#preDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void preDispatch( ChangeEvent event ) {
        
        /*
         * In preDispatch I will find declared namespaces
         * in subject entities that already defined somewhere in parent.
         * Such namespaces already not needed. They could be removed 
         * and corresponding prefixes that used in attribute values
         * should be changed to prefix for this namespace that exist in parent.     
         */
        
        
        BpelEntity entity = getSingleEntity( event );
        BpelEntity[] entities = getEntityArray( event );
        

        if ( entity != null ){
            /*
             *  Only element that was built we will handle. 
             *  Elements that was created via "copy" we don't want to handle.
             */ 
            if ( !isMarked( entity ) ) {
                return;
            }
            Map<String,String> map = new HashMap<String,String>();
            removeExistedNamespaces( entity , event.getParent(), map  );
            addEvent(event);
        }
        else if ( entities!= null ){
            for (BpelEntity ent : entities) {
                /*
                 *  Only element that was built we will handle. 
                 *  Elements that was created via "copy" we don't want to handle.
                 */
                if ( !isMarked( entity ) ) {
                    continue;
                }
                Map<String,String> map = new HashMap<String,String>();
                removeExistedNamespaces( ent , event.getParent(), map );
            }
            addEvent(event);
        }
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        
        if ( mySet.get()==null || !mySet.get().remove(event) ) {
            // if set didn't contain element then return.
            return;
        }
        
        /*
         * This method will move found namespaces from subject entity
         * to upper element ( basicaly root of tree ).
         * 
         * We also need care about prefix here because it could be changed 
         * after lifting prefix up.  
         */
        
        BpelEntity entity = getSingleEntity( event );
        BpelEntity[] entities = getEntityArray( event );
        

        if ( entity != null ){
            moveNSDeclarationToRoot( entity , new HashMap<String,String>());
        }
        else if ( entities!= null ){
            for (BpelEntity ent : entities) {
                moveNSDeclarationToRoot( ent , new HashMap<String,String>());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#reset(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void reset( ChangeEvent event ) {
        Set<ChangeEvent> set = mySet.get();
        if ( set!= null ) {
            set.remove(event);
        }
    }
    
    private void addEvent( ChangeEvent event ) {
        Set<ChangeEvent> set = mySet.get();
        if ( set== null ) {
            set = new HashSet<ChangeEvent>();
            mySet.set( set );
        }
        set.add( event );
    }

    
    private BpelEntity getSingleEntity( ChangeEvent event ){
        BpelEntity entity = null;
        if ( event instanceof EntityInsertEvent ){
            entity = ((EntityInsertEvent)event).getValue();
        }
        else if ( event instanceof EntityUpdateEvent ){
            entity = ((EntityUpdateEvent)event).getNewValue();
        }
        return entity;
    }
    
    private BpelEntity[] getEntityArray( ChangeEvent event ){
        BpelEntity[] entities = null;
        if ( event instanceof ArrayUpdateEvent ){
            entities = ((ArrayUpdateEvent)event).getNewArray();
        }
        return entities;
    }
    

    private void removeExistedNamespaces( BpelEntity entity, BpelEntity parent,
            Map<String, String> prefixMap ) 
    {
        if ( !( entity instanceof BpelEntityImpl) ){
            return;
        }
        if ( parent == null ){
            return;
        }
        Element element = ((BpelEntityImpl)entity).getPeer();
        NamedNodeMap map = element.getAttributes();
        for ( int i = 0; i<map.getLength(); i++ ){
            Node node = map.item( i );
            assert node instanceof Attr;
            Attr attr = (Attr)node;
            if ( attr.getValue() == null ){
                continue;
            }
            handleNsAttribute(entity, parent, prefixMap, attr );
        }
        updatePrefixes(entity, prefixMap);
        for ( BpelEntity child : entity.getChildren()){
            removeExistedNamespaces( child , parent , prefixMap );
        }
    }

    private void handleNsAttribute( BpelEntity entity, BpelEntity parent, 
            Map<String, String> prefixMap, Attr attr ) 
    {
        String namespaceURI = attr.getValue();
        if (XMLNS.equals(attr.getName()) || (XMLNS.equals(attr.getPrefix()))) {
            if ( namespaceURI.equals( entity.getPeer().getNamespaceURI()) ) {
                // do not touch namespace that correspond namespace of current element  
                return;
            }
            ExNamespaceContext context = parent.getNamespaceContext();
            Iterator<String> iterator = context.getPrefixes();
            while ( iterator.hasNext() ){
                String next = iterator.next();
                String namespace = context.getNamespaceURI( next );
                if ( namespaceURI.equals( namespace ) ){
                    String prefixName = attr.getLocalName();
                    // put prefix corresponding found namespace into map for changing it further
                    if( !prefixName.equals( next ) ) {
                        prefixMap.put( prefixName , next );
                    }
                    // remove namespace delcaration.
                    ((BpelEntityImpl) entity).setAttribute( attr.getName(),
                            new PrefixAttribute(attr.getName()), null);
                }
            }
        }
    }


    /**
     * walk through collected mapping oldPrefix -> newPrefix and update them respecively.
     */
    private void updatePrefixes( BpelEntity entity, Map<String, String> prefixMap ) {
        for( Entry<String,String> entry : prefixMap.entrySet() ){
            String key = entry.getKey();
            String value = entry.getValue();
            if( XMLNS.equals( key) && ( value== null) ){
                // we put before prefix with defult NS into map. NULL prefix correspond default NS.
                continue;
            }
            Attribute[] attrs = ((BpelEntityImpl) entity).getDomainAttributes();
            for (Attribute attribute : attrs) {
                updatePrefixInAttribute(entity, key, value, attribute);
            }
        }
    }

    private void updatePrefixInAttribute( BpelEntity entity, String key, 
            String value, Attribute attribute ) 
    {
        if ( entity.getAttribute( attribute ) == null ){
            return;
        }
        if ( !Utils.canUpdatePrefix(attribute) ) {
            return;
        }
        
        Class clazz = attribute.getType();
        if ( QName.class.isAssignableFrom(clazz) 
                || Referenceable.class.isAssignableFrom(clazz)) 
        {
            String newValue = getUpdatedReferenceAttribute(entity, 
                    entity.getAttribute( attribute ) , key, value );
            if ( newValue == null ){
                return;
            }
            ((BpelEntityImpl)entity).setAttribute( 
                    attribute.getName() , attribute , newValue );
        }
        else if (  List.class.isAssignableFrom( clazz)  && 
                Referenceable.class.isAssignableFrom( 
                        attribute.getMemberType()))
        {
            updateListAttribute(entity, key, value, attribute);
        }
    }

    private void updateListAttribute( BpelEntity entity, String oldPrefix, 
            String newPrefix, Attribute attribute ) 
    {
        StringTokenizer tokenizer = new StringTokenizer( 
                entity.getAttribute( attribute ) , " " );
        StringBuilder builder = new StringBuilder();
        boolean change = false;
        while( tokenizer.hasMoreTokens() ){
            String next = tokenizer.nextToken();
            String newValue = getUpdatedReferenceAttribute( entity , 
                    next , oldPrefix , newPrefix );
            if ( newValue == null ){
                builder.append( next );
                builder.append( " " );
            }
            else {
                change = true;
                builder.append( newValue );
                builder.append( " " );
            }
        }
        if ( !change ) {
            return;
        }
        String resultValue = null;
        if ( builder.length() > 0 ){
            resultValue = builder.substring( 0 , builder.length() -1 );
        }
        if ( resultValue!= null ){
            ((BpelEntityImpl)entity).setAttribute( 
                    attribute.getName() , attribute , resultValue );
        }
    }

    /**
     * Update prefix for given attribute.
     */
    private String getUpdatedReferenceAttribute( BpelEntity entity, 
            String attrValue , String oldPrefix, String newPrefix ) 
    {
        int i = attrValue.indexOf(":");
        if ( i == attrValue.length() -1 ){
            return null;
        }
        if ( i==-1 && XMLNS.equals( oldPrefix) ){// default NS should be changed to new NS
            return newPrefix+":"+attrValue;
        }
        else if ( XMLNS.equals( oldPrefix) ){
            return null;
        }
        else if ( attrValue.startsWith( oldPrefix+":") ){
            return  newPrefix+":"+attrValue.substring( i+1 );
        }
        return null;
    }
    
    private void moveNSDeclarationToRoot( BpelEntity entity, Map<String,String> 
        prefixMap) 
    {
        if ( !( entity instanceof BpelEntityImpl) ){
            return;
        }
        Element element = ((BpelEntityImpl)entity).getPeer();
        NamedNodeMap map = element.getAttributes();
        for ( int i = 0; i<map.getLength(); i++ ){
            Node node = map.item( i );
            assert node instanceof Attr;
            Attr attr = (Attr)node;
            if ( attr.getValue() == null ){
                continue;
            }
            try {
                if (XMLNS.equals(attr.getName())
                        || (XMLNS.equals(attr.getPrefix())))
                {
                    SingletonMap pMap = moveNsDeclaration(entity, attr);
                    if ( !pMap.keyEqualsValue() ) {
                        prefixMap.put( pMap.getKey() , pMap.getValue() );
                    }
                }
            }
            catch (InvalidNamespaceException e) {
                // This is the case
                // when namespace was originally incorrect and we do not move it
                // to root. We don't do anything.
            }
        }
        /*
         *  we need to update prefixes for those declarations that changed 
         *  its perfix after lifing up decl.
         */
        updatePrefixes(entity, prefixMap);
        for ( BpelEntity child : entity.getChildren()){
            moveNSDeclarationToRoot( child , prefixMap );
        }
    }

    private SingletonMap moveNsDeclaration( BpelEntity entity, 
            Attr attr ) throws InvalidNamespaceException 
    {
        String namespaceURI = attr.getValue();
        ExNamespaceContext context = entity.getNamespaceContext();
        /*
         * Fantom addition of namespace. This call will not do anything,
         * it just check correcttness of namespaceURI. If it bad then 
         * InvalidNamespaceException will appear and we go to catch.
         */ 
        context.addNamespace( namespaceURI );
        
        // here we remove namespace declararation
        ((BpelEntityImpl) entity).setAttribute( attr.getName(),
                new PrefixAttribute(attr.getName()), null);
        
        String localName = attr.getLocalName();
        Iterator<String> iterator = context.getPrefixes();
        boolean usePrefix = true;
        while ( iterator.hasNext() && localName!=null){
            String prefix = iterator.next();
            if ( localName.equals( prefix ) ){
                usePrefix = false;
            }
        }
        String prefix = null;
        if ( XMLNS.equals(attr.getName()) || localName == null || 
                !usePrefix ) 
        {
            prefix = context.addNamespace(attr.getValue());
        }
        else {
            prefix = localName;
            context.addNamespace( localName , attr.getValue());
        }
        return new SingletonMap( localName , prefix );
    }
    
    private boolean isMarked( BpelEntity entity ){
        Set<Object> set = ((BpelEntityImpl)entity).getCookies().keySet();
        for (Object object : set) {
            if ( object!= null && object.getClass().getCanonicalName().
                    equals( MarkBuilderElement.CLASS_MARK_NAME ) )
            {
                return true;
            }
        }
        return false;
    }
    
    private ThreadLocal<Set<ChangeEvent>> mySet = 
        new ThreadLocal<Set<ChangeEvent>>();

    private static final class SingletonMap {
        
        SingletonMap( String key, String value){
            myKey = key;
            myValue = value;
        }
        
        String getKey(){
            return myKey;
        }
        
        String getValue() {
            return myValue;
        }
        
        boolean keyEqualsValue() {
            if ( myKey == null ) {
                return myValue == null;
            }
            return myKey.equals( myValue );
        }
        
        private String myKey;
        private String myValue;
    }
}
