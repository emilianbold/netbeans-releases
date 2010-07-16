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
package org.netbeans.modules.bpel.model.impl.references;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompensationHandlerHolder;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.Link;
import org.netbeans.modules.bpel.model.api.LinkContainer;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.BpelReferenceable;
import org.netbeans.modules.bpel.model.api.references.MappedReference;
import org.netbeans.modules.bpel.model.impl.BpelEntityImpl;
import org.netbeans.modules.bpel.model.api.support.ContainerIterator;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author ads
 */
public final class BpelReferenceBuilder {

    private BpelReferenceBuilder() {
        myCollection = new ArrayList<BpelReferenceFactory>();
        myCollection.add( new PartnerLinkReferenceFactory() );
        myCollection.add( new LinkReferenceFactory() );
        myCollection.add( new CorrelationSetReferenceFactory() );
        myCollection.add( new VariableReferenceFactory() );
        myCollection.add( new CompensationHandlerHolderReferenceFactory() );
        myCollection.add( new MessageExchangeReferenceFactory());
    }
    
    public static BpelReferenceBuilder getInstance() {
        return INSTANCE;
    }
    
    public <T extends BpelReferenceable> BpelReference<T> 
            build( Class<T> clazz ,AbstractDocumentComponent entity , 
                    Attribute attr )
    {
        BpelReference<T> ref = build( clazz , entity , entity.getAttribute( attr ));
        if ( ref instanceof MappedReference ) {
            ((MappedReference)ref).setAttribute( attr );
        }
        return ref;
    }
    
    public <T extends BpelReferenceable> BpelReference<T> build( 
            Class<T> clazz ,AbstractComponent entity , String refString )
    {
        if ( refString == null ){
            return null;
        }
        for (BpelReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.createUnresolvedReference( clazz , entity , 
                        refString );
            }
        }
        return null;
    }
    
    public <T extends BpelReferenceable> BpelReference<T> build( 
            T target , Class<T> clazz , AbstractComponent entity  )
    {
        for (BpelReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.create( target , clazz , entity );
            }
        }
        return null;
    }
    
    public void setAttribute( BpelReference ref , Attribute attr ) {
        if ( ref instanceof MappedReference ) {
            ((MappedReference)ref).setAttribute( attr );
        }
    }
    
    public BpelAttributesType.AttrType getAttributeType( Attribute attr ) {
        /*Class clazz = null;
        if ( List.class.isAssignableFrom( attr.getType() )){
            clazz = attr.getMemberType();
        }
        else {
            clazz = attr.getType();
        }
        for (BpelReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.getAttributeType();
            }
        }*/
        return BpelAttributesType.AttrType.NCNAME;
    }
    
    interface BpelResolver {
        <T extends BpelReferenceable> T resolve( AbstractReference<T> ref );
        <T extends BpelReferenceable> boolean haveRefString( 
                AbstractReference<T> ref , T entity );
    }

    private static final BpelReferenceBuilder INSTANCE = new BpelReferenceBuilder();
    
    private Collection<BpelReferenceFactory> myCollection;
}

interface BpelReferenceFactory extends BpelReferenceBuilder.BpelResolver {
    
    <T extends BpelReferenceable> boolean isApplicable( Class<T> clazz);
    
    <T extends BpelReferenceable> BpelReference<T> create( T target,
            Class<T> clazz , AbstractComponent entity );
    
    <T extends BpelReferenceable> BpelReference<T> 
        createUnresolvedReference( Class<T> clazz, AbstractComponent entity, 
                String refString );
    
    <T extends BpelReferenceable> BpelReference<T> create( T target,
            Class<T> clazz, AbstractComponent entity, String refString );
    
    BpelAttributesType.AttrType getAttributeType();
}

abstract class AbstractBpelReferenceFactory implements BpelReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#create(T, java.lang.Class, org.netbeans.modules.xml.xam.AbstractComponent, java.lang.String)
     */
    public <T extends BpelReferenceable> BpelReference<T> create( T target, 
            Class<T> clazz, AbstractComponent entity, String refString ) 
    {
        return new BpelReferenceImpl<T>( target, clazz , entity , refString ,this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#createUnresolvedReference(java.lang.Class, org.netbeans.modules.xml.xam.AbstractComponent, java.lang.String)
     */
    public <T extends BpelReferenceable> BpelReference<T> createUnresolvedReference(
            Class<T> clazz, AbstractComponent entity, String refString ) 
    {
        return new BpelReferenceImpl<T>( clazz , entity , refString, this );
    }
    
    public BpelAttributesType.AttrType getAttributeType(){
        return BpelAttributesType.AttrType.NCNAME;
    }
}

abstract class AbstractBpelNamedReferenceFactory extends
        AbstractBpelReferenceFactory
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceBuilder.BpelResolver#haveRefString(org.netbeans.modules.bpel.model.impl.references.AbstractReference, T)
     */
    public <T extends BpelReferenceable> boolean haveRefString( 
            AbstractReference<T> ref, T entity ) 
    {
        if (!( entity instanceof NamedElement )) {
            return false;
        }
        return ref.getRefString().equals( ((NamedElement)entity).getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#create(T, java.lang.Class, org.netbeans.modules.xml.xam.AbstractComponent)
     */
    public <T extends BpelReferenceable> BpelReference<T> create( T target, 
            Class<T> clazz, AbstractComponent entity ) 
    {
        String name = ((NamedElement)target).getName();
        return new BpelReferenceImpl<T>( clazz , entity , name , this );
    }
    
}

class PartnerLinkReferenceFactory extends AbstractBpelNamedReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#isApplicable(java.lang.Class)
     */
    public <T extends BpelReferenceable> boolean isApplicable( Class<T> clazz ) {
        return PartnerLink.class.isAssignableFrom( clazz );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#resolve(java.lang.Class,
     *      org.netbeans.modules.xml.xam.AbstractComponent, java.lang.String)
     */
    public <T extends BpelReferenceable> T resolve(
            AbstractReference<T> reference ) 
    {
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
                .getParent();
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        if ( !( entity instanceof BpelEntityImpl )) {
            return null;
        }
        ContainerIterator<BaseScope> iterator = 
            new ContainerIterator<BaseScope>( (BpelEntity) entity , 
                    BaseScope.class );
        while( iterator.hasNext() ){
            BaseScope scope = iterator.next();
            PartnerLinkContainer container = scope.getPartnerLinkContainer();
            if ( container!= null ){
                for (PartnerLink link : container.getPartnerLinks()) {
                    assert link!=null;
                    if ( refString.equals( link.getName()) ){
                        return clazz.cast( link );
                    }
                }
            }
        }
        return null;
    }

}

class LinkReferenceFactory extends AbstractBpelNamedReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#isApplicable(java.lang.Class)
     */
    public <T extends BpelReferenceable> boolean isApplicable( Class<T> clazz ) {
        return Link.class.isAssignableFrom( clazz );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#resolve(java.lang.Class,
     *      org.netbeans.modules.xml.xam.AbstractComponent, java.lang.String)
     */
    public <T extends BpelReferenceable> T resolve(
            AbstractReference<T> reference )
    {
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
                .getParent();
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        if ( !( entity instanceof BpelEntityImpl )) {
            return null;
        }
        ContainerIterator<Flow> iterator = new ContainerIterator<Flow>( 
                (BpelEntity ) entity, Flow.class );
        while( iterator.hasNext() ){
            Flow flow = iterator.next();
            LinkContainer container = flow.getLinkContainer();
            if ( container!= null ){
                for (Link link : container.getLinks()) {
                    assert link!= null;
                    if ( refString.equals( link.getName())){
                        return clazz.cast(link);
                    }
                }
            }
        }
        return null;
    }
    
}

class CorrelationSetReferenceFactory extends AbstractBpelNamedReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#isApplicable(java.lang.Class)
     */
    public <T extends BpelReferenceable> boolean isApplicable( Class<T> clazz ) {
        return CorrelationSet.class.isAssignableFrom( clazz );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#resolve(java.lang.Class,
     *      org.netbeans.modules.xml.xam.AbstractComponent, java.lang.String)
     */
    public <T extends BpelReferenceable> T resolve(
            AbstractReference<T> reference )
    {
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
                .getParent();
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        if ( !( entity instanceof BpelEntityImpl )) {
            return null;
        }
        ContainerIterator<BaseScope> iterator = new ContainerIterator<BaseScope>(
                (BpelEntity)entity, BaseScope.class);
        while (iterator.hasNext()) {
            BaseScope scope = iterator.next();
            CorrelationSetContainer container = scope
                    .getCorrelationSetContainer();
            if (container == null) {
                continue;
            }
            for (CorrelationSet set : container.getCorrelationSets()) {
                String setName = set.getName();
                if ( refString.equals(setName)) {
                    return clazz.cast(set);
                }
            }
        }
        return null;
    }
    
}

class VariableReferenceFactory extends AbstractBpelReferenceFactory {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceBuilder.BpelResolver#haveRefString(org.netbeans.modules.bpel.model.impl.references.AbstractReference, T)
     */
    public <T extends BpelReferenceable> boolean haveRefString( 
            AbstractReference<T> ref, T entity ) 
    {
        if (!( entity instanceof VariableDeclaration )) {
            return false;
        }
        return ref.getRefString().equals( 
                ((VariableDeclaration)entity).getVariableName());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#isApplicable(java.lang.Class)
     */
    public <T extends BpelReferenceable> boolean isApplicable( Class<T> clazz ) {
        return VariableDeclaration.class.isAssignableFrom( clazz );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#resolve(java.lang.Class,
     *      org.netbeans.modules.xml.xam.AbstractComponent, java.lang.String)
     */
    public <T extends BpelReferenceable> T resolve(
            AbstractReference<T> reference )
    {
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
                .getParent();
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        if ( !( entity instanceof BpelEntityImpl )) {
            return null;
        }
        
        /*
         * This is rule from static analysis: 
         * For <onEvent>, variables
         * referenced by the variable attribute of <fromPart> elements or the
         * variable attribute of an <onEvent> element are implicitly declared in
         * the associated scope of the event handler. Variables of the same
         * names MUST NOT be explicitly declared in the associated scope. The
         * variable references are resolved to the associated scope only and
         * MUST NOT be resolved to the ancestor scopes.
         */
        if (entity instanceof FromPart && entity.getParent() instanceof OnEvent)
        {
            return clazz.cast(entity);
        }
        
        ContainerIterator<VariableDeclarationScope> iterator = 
            new ContainerIterator<VariableDeclarationScope>( (BpelEntity)entity , 
                    VariableDeclarationScope.class );
        while( iterator.hasNext() ){
            VariableDeclarationScope scope = iterator.next();
            if ( scope instanceof BaseScope ){
                VariableContainer container = 
                    ((BaseScope)scope).getVariableContainer();
                if ( container!= null ){
                    for (Variable variable : container.getVariables()) {
                        if ( refString.equals( variable.getName())){
                            return clazz.cast(variable);
                        }
                    }
                }
            }
            else if ( scope instanceof VariableDeclaration ){
                if ( refString.equals( 
                        ((VariableDeclaration)scope).getVariableName()))
                {
                    return clazz.cast(scope);
                }
            }
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#create(T,
     *      java.lang.Class, org.netbeans.modules.xml.xam.AbstractComponent)
     */
    public <T extends BpelReferenceable> BpelReference<T> create( T target,
            Class<T> clazz, AbstractComponent entity )
    {
        String name = (( VariableDeclaration )target).getVariableName();
        return new BpelReferenceImpl<T>( target , clazz , entity , name, this  );
    }
}

class CompensationHandlerHolderReferenceFactory extends
        AbstractBpelNamedReferenceFactory
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#isApplicable(java.lang.Class)
     */
    public <T extends BpelReferenceable> boolean isApplicable( Class<T> clazz ) {
        return CompensationHandlerHolder.class.isAssignableFrom( clazz );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#resolve(java.lang.Class,
     *      org.netbeans.modules.xml.xam.AbstractComponent, java.lang.String)
     */
    public <T extends BpelReferenceable> T resolve(
            AbstractReference<T> reference )
    {
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
                .getParent(); 
        // this entity should be actually compensateScope but we don;t care about it 
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        // entity should be within FaultHandlers CompensationHandler or TerminationHandler
        BpelContainer container = getAscendantFCTHandler( entity );
        if ( container!= null ){
            container = container.getParent();
            /*
             * There could be only two possibility here - Invoke or BaseScope.
             * In case of Invoke we cannot find corresponded entity for "name"
             * attribute, because we need to find CompensationHandlerHolder
             * right inside this <code>container</code>. But Invoke cannot
             * contain any such entity.
             */
           if (BaseScope.class.isAssignableFrom(container.getElementType())) {
                // so from this point we start to find Invoke or Scope
                // that have appropriate name....
                return clazz.cast(findInvokeOrScope(container, refString));
            }
        }
        return null;
    }
    
    private BpelContainer getAscendantFCTHandler( Component component ){
        if ( component == null || !( component instanceof BpelEntity)){
            return null;
        }
        BpelEntity entity = (BpelEntity)component;
        if ( entity.getElementType().equals(FaultHandlers.class) ||
                entity.getElementType().equals(CompensationHandler.class) ||
                entity.getElementType().equals(TerminationHandler.class) )
        {
            return (BpelContainer)component;
        }
        return getAscendantFCTHandler( component.getParent() );
    }
    
    /*private BaseScope getAscendantScope( Component component ){
        if ( component == null ){
            return null;
        }
        if ( component instanceof BaseScope ){
            return (BaseScope)component;
        }
        return getAscendantScope( component.getParent() );
    }*/
    
    private CompensationHandlerHolder findInvokeOrScope(
            BpelEntity container, String name )
    {
        List<BpelEntity> entities = container.getChildren();
        for (BpelEntity entity : entities) {
            if (entity.getElementType().equals(Scope.class)
                    || entity.getElementType().equals(Invoke.class))
            {
                /*
                 * this is simple implementation ..... may be we need more
                 * complex logic because invoke or scope with appropriate name could
                 * be deeply included f.e. in activty container that appear as
                 * first in sequence but on the "sequence" level there also
                 * could exist scope or invoke with the same name. Here is the
                 * question : what entity should be chosen ? Deeply inserted but
                 * whose parent appear firstly or following element with upper
                 * level ? This impl. will choose deeply inserted element ( by
                 * parent order ).
                 * 
                 * Actually this is good algorithm becuase scope MUST contain
                 * unique activity names ( each activity within one scope
                 * should have different names ).    
                 */
                Named<? extends BpelEntity> named = 
                    (Named<? extends BpelEntity>) entity;
                // Is it true that we don't need trying to find activtity inside
                // Scope ????
                if (name.equals(named.getName())) {
                    return (CompensationHandlerHolder) entity;
                }
            }
            else if (entity instanceof BpelContainer) {
                CompensationHandlerHolder holder = findInvokeOrScope(
                        (BpelContainer) entity, name);
                if (holder != null) {
                    return holder;
                }
            }
        }
        return null;
    }
}

class MessageExchangeReferenceFactory extends AbstractBpelNamedReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#isApplicable(java.lang.Class)
     */
    public <T extends BpelReferenceable> boolean isApplicable( Class<T> clazz ) {
        return MessageExchange.class.isAssignableFrom( clazz );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.impl.references.BpelReferenceFactory#resolve(java.lang.Class,
     *      org.netbeans.modules.xml.xam.AbstractComponent, java.lang.String)
     */
    public <T extends BpelReferenceable> T resolve(
            AbstractReference<T> reference ) 
    {
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
                .getParent();
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        if ( !( entity instanceof BpelEntityImpl )) {
            return null;
        }
        ContainerIterator<BaseScope> iterator = 
            new ContainerIterator<BaseScope>( (BpelEntity) entity , 
                    BaseScope.class );
        while( iterator.hasNext() ){
            BaseScope scope = iterator.next();
            MessageExchangeContainer container = 
                scope.getMessageExchangeContainer();
            if ( container!= null ){
                for (MessageExchange exchange : container.getMessageExchanges()) {
                    assert exchange!=null;
                    if ( refString.equals( exchange.getName()) ){
                        return clazz.cast( exchange );
                    }
                }
            }
        }
        return null;
    }
}
