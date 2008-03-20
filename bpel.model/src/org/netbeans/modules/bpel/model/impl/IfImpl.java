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

import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class IfImpl extends ConditionalActivity implements If {


    IfImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    IfImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.IF.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ActivityHolder#getActivity()
     */
    public ExtendableActivity getActivity() {
        return getChild(ExtendableActivity.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ActivityHolder#setActivity(org.netbeans.modules.soa.model.bpel20.api.ExtendableActivity)
     */
    public void setActivity( ExtendableActivity activity ) {
        setChild(activity, ExtendableActivity.class , 
                BpelTypesEnum.ELSE_IF,
                BpelTypesEnum.ELSE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.ActivityHolder#removeActivity()
     */
    public void removeActivity() {
        removeChild(ExtendableActivity.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#getElseIfs()
     */
    public ElseIf[] getElseIfs() {
        readLock();
        try {
            List<ElseIf> list = getChildren( ElseIf.class );
            return list.toArray( new ElseIf[ list.size() ] );
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#setElseIfs(org.netbeans.modules.soa.model.bpel20.api.ElseIf[])
     */
    public void setElseIfs( ElseIf[] value ) {
        setArrayBefore( value , ElseIf.class , BpelTypesEnum.ELSE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#insertEleIf(org.netbeans.modules.soa.model.bpel20.api.ElseIf, int)
     */
    public void insertElseIf( ElseIf value, int i ) {
        insertAtIndex( value , ElseIf.class , i , BpelTypesEnum.ELSE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#addElseIf(org.netbeans.modules.soa.model.bpel20.api.ElseIf)
     */
    public void addElseIf( ElseIf value ) {
        addChildBefore( value , ElseIf.class , BpelTypesEnum.ELSE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#getElseIf(int)
     */
    public ElseIf getElseIf( int i ) {
        return getChild( ElseIf.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#setElseIf(org.netbeans.modules.soa.model.bpel20.api.ElseIf, int)
     */
    public void setElseIf( ElseIf value, int i ) {
        setChildAtIndex( value , ElseIf.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#removeElseIf(int)
     */
    public void removeElseIf( int i ) {
        removeChild( ElseIf.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#getElse()
     */
    public Else getElse() {
        return getChild( Else.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#setElse(org.netbeans.modules.soa.model.bpel20.api.Else)
     */
    public void setElse( Else value ) {
        setChild( value , Else.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#removeElse()
     */
    public void removeElse() {
        removeChild( Else.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return If.class;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.If#sizeElseIfs()
     */
    public int sizeElseIfs(){
        readLock();
        try {
            return getChildren( ElseIf.class ).size();
        }
        finally {
            readUnlock();
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ConditionalActivity#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        BpelEntity entity = Utils.createActivityGroup(getBpelModel(), element);
        if ( entity!= null ) {
            return entity;
        }
        if ( BpelElements.ELSE_IF.getName().equals( element.getLocalName() ) ){
            return new ElseIfImpl( getModel() , element );
        }
        else if ( BpelElements.ELSE.getName().equals( element.getLocalName() ) ){
            return new ElseImpl( getModel() , element );
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelContainerImpl#getChildType(T)
     */
    @Override
    protected <T extends BpelEntity> Class<? extends BpelEntity> 
        getChildType( T entity )
    {       
        if ( entity instanceof ExtendableActivity ){
            return ExtendableActivity.class;
        }
        return super.getChildType(entity);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity ).equals( Else.class) 
                || getChildType( entity ).equals( ExtendableActivity.class))
        {
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
}
