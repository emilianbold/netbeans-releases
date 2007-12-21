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

import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ElseIfImpl extends ActivityHolderImpl implements ElseIf, AfterSources {

    ElseIfImpl(BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ElseIfImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.ELSE_IF.getName() );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ConditionHolder#getCondition()
     */
    public BooleanExpr getCondition() {
        return getChild( BooleanExpr.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ConditionHolder#setCondition(org.netbeans.modules.soa.model.bpel20.api.BooleanExpr)
     */
    public void setCondition( BooleanExpr value ) {
        setChild( value , BooleanExpr.class , BpelTypesEnum.ACTIVITIES_GROUP );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ConditionHolder#removeCondition()
     */
    public void removeCondition() {
        removeChild( BooleanExpr.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return ElseIf.class;
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ){
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ActivityImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ){
        if ( BpelElements.CONDITION.getName().equals( 
                element.getLocalName()) )
        {
            return new BooleanExprImpl( getModel() , element );
        }
        return super.create(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity).equals( BooleanExpr.class) ){
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
    
}
