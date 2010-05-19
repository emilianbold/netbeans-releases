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

/**
 *
 */
package org.netbeans.modules.bpel.model.impl;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.DurationExpression;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class OnAlarmEventImpl extends ExtensibleElementsImpl implements
        OnAlarmEvent
{

    OnAlarmEventImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    OnAlarmEventImpl( BpelBuilderImpl builder) {
        super(builder, BpelElements.ON_ALARM_EVENT.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.OnAlarmEvent#getRepeatEvery()
     */
    public RepeatEvery getRepeatEvery() {
        return getChild( RepeatEvery.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.OnAlarmEvent#setRepeatEvery(org.netbeans.modules.soa.model.bpel20.api.DurationExpression)
     */
    public void setRepeatEvery( RepeatEvery expression ) {
        setChild( expression , RepeatEvery.class , BpelTypesEnum.SCOPE  );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.OnAlarmEvent#removeRepeatEvery()
     */
    public void removeRepeatEvery() {
        removeChild( RepeatEvery.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ScopeHolder#getScope()
     */
    public Scope getScope() {
        return getChild( Scope.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ScopeHolder#setScope(org.netbeans.modules.soa.model.bpel20.api.Scope)
     */
    public void setScope( Scope scope ) {
        setChild( scope , Scope.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ScopeHolder#removeScope()
     */
    public void removeScope() {
        removeChild( Scope.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return OnAlarmEvent.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TimeEventHolder#getTimeEvent()
     */
    public TimeEvent getTimeEvent() {
        return getChild( TimeEvent.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TimeEventHolder#setTimeEvent(org.netbeans.modules.soa.model.bpel20.api.TimeEvent)
     */
    public void setTimeEvent( TimeEvent event ) {
        setChild( event , TimeEvent.class , BpelTypesEnum.REPEAT_EVERY,
                BpelTypesEnum.SCOPE );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.OnAlarmEvent#removeTimeEvent()
     */
    public void removeTimeEvent() {
        removeChild( TimeEvent.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ){
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.SCOPE.getName().equals( element.getLocalName())){
            return new ScopeImpl( getModel() , element );
        }
        else if ( BpelElements.REPEAT_EVERY.getName().equals( 
                element.getLocalName()) ) 
        {
            return new RepeatEveryImpl( getModel() , element );
        }
        else if ( BpelElements.FOR.getName().equals( element.getLocalName()) ){
            return new ForImpl( getModel() , element );
        }
        else if ( BpelElements.UNTIL.getName().equals( element.getLocalName()) ){
            return new DeadlineExpressionImpl( getModel() , element  );
        }
        return super.create( element );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelContainerImpl#getChildType(T)
     */
    @Override
    protected <T extends BpelEntity> Class<? extends BpelEntity> 
        getChildType( T entity )
    {
        if ( entity instanceof TimeEvent ){
            return TimeEvent.class;
        }
        return super.getChildType(entity);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity ).equals( DurationExpression.class) 
                || getChildType( entity ).equals( Scope.class)
                || getChildType( entity ).equals( TimeEvent.class) )
        {
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }

}
