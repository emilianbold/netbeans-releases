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
package org.netbeans.modules.bpel.model.api.events;

import org.netbeans.modules.bpel.model.api.BpelEntity;

/**
 * @author ads
 */
public class EntityRemoveEvent<T extends BpelEntity> extends IndexedChangeEvent
        implements OutOfModelEvent
{

    private static final long serialVersionUID = -7222756911004830278L;

    /**
     * Constructor for EntityRemoveEvent with source ref.
     *
     * @param source
     *            source of event ( who produce this event ).
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param value
     *            old entity value.
     */
    public EntityRemoveEvent( Object source, BpelEntity parent, String name,
            T value )
    {
        super(source, parent, name, -1);
        myValue = value;
    }

    /**
     * Constructor for EntityRemoveEvent with source ref and index.
     * 
     * @param source
     *            source of event ( who produce this event ).
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param value
     *            old entity value.
     * @param index
     *            index of child entity in parent.
     */
    public EntityRemoveEvent( Object source, BpelEntity parent, String name,
            T value, int index )
    {
        super(source, parent, name, index);
        myValue = value;
    }

    /**
     * Constructor for EntityRemoveEvent without source ref.
     * 
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param value
     *            old entity value.
     */
    public EntityRemoveEvent( BpelEntity parent, String name, T value ) {
        this(Thread.currentThread(), parent, name, value);
    }

    /**
     * Constructor for EntityRemoveEvent with index and without source ref.
     * 
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param value
     *            old entity value.
     * @param index
     *            index of child entity in parent.
     */
    public EntityRemoveEvent( BpelEntity parent, String name, T value, int index )
    {
        this(Thread.currentThread(), parent, name, value, index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.events.OutOfModelEvent#getOutOfModelEntity()
     */
    /** {@inheritDoc} */
    public BpelEntity getOutOfModelEntity() {
        return getOldValue();
    }

    /**
     * @return inserted value.
     */
    public T getOldValue() {
        return myValue;
    }

    private T myValue;

}
