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
public class EntityUpdateEvent<T extends BpelEntity> extends IndexedChangeEvent
        implements OutOfModelEvent
{

    private static final long serialVersionUID = 6512333724051189804L;

    /**
     * Constructor for EntityUpdateEvent with source ref.
     *
     * @param source
     *            source of event ( who produce this event ).
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param oldValue
     *            old entity value.
     * @param newValue
     *            old entity value.
     */
    public EntityUpdateEvent( Object source, BpelEntity parent, String name,
            T oldValue, T newValue )
    {
        super(source, parent, name, -1);
        myOldValue = oldValue;
        myNewValue = newValue;
    }

    /**
     * Constructor for EntityUpdateEvent with source ref and index.
     * 
     * @param source
     *            source of event ( who produce this event ).
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param oldValue
     *            old entity value.
     * @param newValue
     *            new entity value.
     * @param index
     *            index of child entity in parent.
     */
    public EntityUpdateEvent( Object source, BpelEntity parent, String name,
            T oldValue, T newValue, int index )
    {
        super(source, parent, name, index);
        myOldValue = oldValue;
        myNewValue = newValue;
    }

    /**
     * Constructor for EntityUpdateEvent without source ref.
     * 
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param oldValue
     *            old entity value.
     * @param newValue
     *            new entity value.
     */
    public EntityUpdateEvent( BpelEntity parent, String name, T oldValue,
            T newValue )
    {
        this(Thread.currentThread(), parent, name, oldValue, newValue);
    }

    /**
     * Constructor for EntityUpdateEvent with index and without source ref.
     * 
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param oldValue
     *            old entity value.
     * @param newValue
     *            new entity value.
     * @param index
     *            index of child entity in parent.
     */
    public EntityUpdateEvent( BpelEntity parent, String name, T oldValue,
            T newValue, int index )
    {
        this(Thread.currentThread(), parent, name, oldValue, newValue, index);
    }

    /**
     * @return old value.
     */
    public T getOldValue() {
        return myOldValue;
    }

    /**
     * @return new value.
     */
    public T getNewValue() {
        return myNewValue;
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

    private T myOldValue;

    private T myNewValue;

}
