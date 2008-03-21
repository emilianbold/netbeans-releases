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
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.w3c.dom.Element;
import org.netbeans.modules.bpel.model.api.support.Utils;

/**
 * @author ads
 */
public abstract class CompositeActivityImpl extends ActivityImpl implements CompositeActivity {

    CompositeActivityImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    CompositeActivityImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.CompositeActivity#getActivities()
     */
    public ExtendableActivity[] getActivities() {
        readLock();
        try {
            List<ExtendableActivity> list = getChildren(ExtendableActivity.class);
            return list.toArray(new ExtendableActivity[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.CompositeActivity#getActivity(int)
     */
    public ExtendableActivity getActivity( int i ) {
        return getChild(ExtendableActivity.class, i);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CompositeActivity#setActivity(org.netbeans.modules.soa.model.bpel20.api.ExtendableActivity, int)
     */
    public void setActivity( ExtendableActivity activity, int i ) {
        setChildAtIndex(activity, ExtendableActivity.class, i);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.CompositeActivity#removeActivity(int)
     */
    public void removeActivity( int i ) {
        removeChild(ExtendableActivity.class, i);
    }

    public void insertActivity( ExtendableActivity activity, int i ) {
        insertAtIndex(activity, ExtendableActivity.class, i );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.CompositeActivity#addActivity(org.netbeans.modules.soa.model.bpel.api.Activity)
     */
    public void addActivity( ExtendableActivity activity ) {
        addChildBefore(activity, ExtendableActivity.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.CompositeActivity#sizeOfActivities()
     */
    public int sizeOfActivities() {
        readLock();
        try {
            return getChildren(ExtendableActivity.class).size();
        }
        finally {
            readUnlock();
        }
    }

    public void setActivtities( ExtendableActivity[] activities ) {
        setArrayBefore(activities, ExtendableActivity.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        BpelEntity component = Utils.createActivityGroup(this.getModel(),
                element);
        if (component == null) {
            return super.create(element);
        }
        return component;
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
}
