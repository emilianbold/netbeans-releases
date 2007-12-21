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
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.AssignChild;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
/**
 * @author ads
 *
 */
public class AssignImpl extends ActivityImpl implements Assign {

    AssignImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    AssignImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.ASSIGN.getName() );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#getAssignChildren()
     */
    public AssignChild[] getAssignChildren() {
        readLock();
        try {
            List<AssignChild> list = getChildren(AssignChild.class);
            return list.toArray(new AssignChild[list.size()]);
        }
        finally {
            readUnlock();
        }
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#getAssignChild(int)
     */
    public AssignChild getAssignChild( int i ) {
        return getChild(AssignChild.class, i);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#removeAssignChild(int)
     */
    public void removeAssignChild( int i ) {
        removeChild(Copy.class, i);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#setAssignChild(org.netbeans.modules.soa.model.bpel20.api.AssignChild, int)
     */
    public void setAssignChild( AssignChild child, int i ) {
        setChildAtIndex(child, AssignChild.class, i);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#addAssignChild(org.netbeans.modules.soa.model.bpel20.api.AssignChild)
     */
    public void addAssignChild( AssignChild child ) {
        addChildBefore(child, AssignChild.class);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#insertAssignChild(org.netbeans.modules.soa.model.bpel20.api.AssignChild, int)
     */
    public void insertAssignChild( AssignChild child, int i ) {
        insertAtIndex(child, AssignChild.class, i);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#setAssignChildren(org.netbeans.modules.soa.model.bpel20.api.AssignChild[])
     */
    public void setAssignChildren( AssignChild[] children ) {
        setArrayBefore(children, AssignChild.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#sizeOfAssignChildren()
     */
    public int sizeOfAssignChildren() {
        readLock();
        try {
            return getChildren(AssignChild.class).size();
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
        return Assign.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#getValidate()
     */
    public TBoolean getValidate() {
        return getBooleanAttribute( BpelAttributes.VALIDATE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#setValidate(org.netbeans.modules.soa.model.bpel20.api.support.TBoolean)
     */
    public void setValidate( TBoolean value ) {
        setBpelAttribute( BpelAttributes.VALIDATE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#removeValidate()
     */
    public void removeValidate() {
        removeAttribute( BpelAttributes.VALIDATE );
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelContainerImpl#getChildType(T)
     */
    @Override
    protected <T extends BpelEntity> Class<? extends BpelEntity> 
        getChildType( T entity )
    {
        if ( entity instanceof AssignChild ){
            return AssignChild.class;
        }
        return super.getChildType(entity);
    }

    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.COPY.getName().equals(element.getLocalName())) {
            return new CopyImpl(getModel(), element);
        }
        else if ( BpelElements.EXTENSIBLE_ASSIGN.getName().
                equals(element.getLocalName())) {
            return new ExtensibleAssignImpl(getModel(), element);
        } 
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.VALIDATE;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
