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

import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Condition;
import org.netbeans.modules.bpel.model.api.Target;
import org.netbeans.modules.bpel.model.api.TargetContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class TargetContainerImpl extends ExtensibleElementsImpl implements
        TargetContainer
{

    TargetContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    TargetContainerImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.TARGETS.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#getTargets()
     */
    public Target[] getTargets() {
        readLock();
        try {
            List<Target> list = getChildren( Target.class );
            return list.toArray( new Target[ list.size() ] );
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#getTarget(int)
     */
    public Target getTarget( int i ) {
        return getChild( Target.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#removeTarget(int)
     */
    public void removeTarget( int i ) {
        removeChild( Target.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#setTarget(org.netbeans.modules.soa.model.bpel20.api.Target, int)
     */
    public void setTarget( Target target, int i ) {
        setChildAtIndex( target , Target.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#addTarget(org.netbeans.modules.soa.model.bpel20.api.Target)
     */
    public void addTarget( Target target ) {
        addChild( target , Target.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#setTargets(org.netbeans.modules.soa.model.bpel20.api.Target[])
     */
    public void setTargets( Target[] target ) {
        setArrayBefore( target , Target.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#insertTarget(org.netbeans.modules.soa.model.bpel20.api.Target, int)
     */
    public void insertTarget( Target target, int i ) {
        insertAtIndex( target , Target.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#getJoinCondition()
     */
    public Condition getJoinCondition() {
        return getChild( Condition.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#setJoinCondition(org.netbeans.modules.soa.model.bpel20.api.Condition)
     */
    public void setJoinCondition( Condition condition ) {
        setChild( condition , Condition.class , BpelTypesEnum.TARGET );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.TargetContainer#removeJoinCondition()
     */
    public void removeJoinCondition() {
        removeChild( Condition.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return TargetContainer.class;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ExtensibleElementsImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.TARGET.getName().equals( element.getLocalName()) ){
            return new TargetImpl( getModel(), element );
        }
        else if ( BpelElements.JOIN_CONDITION.getName().equals( 
                element.getLocalName()))
        {
            return new ConditionImpl( getModel(), element );
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity ) {
        if ( getChildType( entity).equals(Condition.class ))  {
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
}
