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

import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.w3c.dom.Element;
import org.netbeans.modules.bpel.model.api.support.Utils;

public abstract class ActivityHolderImpl extends ExtensibleElementsImpl implements ActivityHolder {

    ActivityHolderImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ActivityHolderImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
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
        setChild(activity, ExtendableActivity.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.ActivityHolder#removeActivity()
     */
    public void removeActivity() {
        removeChild(ExtendableActivity.class);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        BpelEntity entity = Utils.createActivityGroup(getModel(), element);
        if ( entity == null ){
            return super.create( element );
        }
        return entity;
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
        if ( getChildType( entity).equals(ExtendableActivity.class ) ){
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
}
