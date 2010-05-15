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
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterImport;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class FaultHandlersImpl extends ExtensibleElementsImpl implements
        FaultHandlers, AfterImport, AfterSources
{

    FaultHandlersImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    FaultHandlersImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.FAULT_HANDLERS.getName() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#getCatches()
     */
    public Catch[] getCatches() {
        readLock();
        try {
            List<Catch> list = getChildren(Catch.class);
            return list.toArray(new Catch[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#getCatch(int)
     */
    public Catch getCatch( int i ) {
        return getChild(Catch.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#removeCatch(int)
     */
    public void removeCatch( int i ) {
        removeChild(Catch.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#setCatches(org.netbeans.modules.soa.model.bpel20.api.Catch[])
     */
    public void setCatches( Catch[] catches ) {
        setArrayBefore(catches, Catch.class, BpelTypesEnum.CATCH_ALL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#setCatch(org.netbeans.modules.soa.model.bpel20.api.Catch,
     *      int)
     */
    public void setCatch( Catch catc, int i ) {
        setChildAtIndex(catc, Catch.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#addCatch(org.netbeans.modules.soa.model.bpel20.api.Catch)
     */
    public void addCatch( Catch catc ) {
        addChildBefore(catc, Catch.class, BpelTypesEnum.CATCH_ALL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#insertCatch(org.netbeans.modules.soa.model.bpel20.api.Catch,
     *      int)
     */
    public void insertCatch( Catch catc, int i ) {
        insertAtIndex(catc, Catch.class, i , BpelTypesEnum.CATCH_ALL );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#getCatchAll()
     */
    public CatchAll getCatchAll() {
        return getChild(CatchAll.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#setCatchAll(org.netbeans.modules.soa.model.bpel20.CatchAll)
     */
    public void setCatchAll( CatchAll value ) {
        setChild(value, CatchAll.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#removeCatchAll()
     */
    public void removeCatchAll() {
        removeChild(CatchAll.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#sizeOfCathes()
     */
    public int sizeOfCathes() {
        readLock();
        try {
            return getChildren(Catch.class).size();
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return FaultHandlers.class;
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.CATCH.getName().equals(element.getLocalName())) {
            return new CatchImpl(getModel(), element);
        }
        else if (BpelElements.CATCH_ALL.getName().equals(element
                .getLocalName()))
        {
            return new CatchAllImpl(getModel(), element);
        }

        return super.create( element );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity).equals( CatchAll.class) ){
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }

}
