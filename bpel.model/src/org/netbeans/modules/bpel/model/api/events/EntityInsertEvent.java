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
public class EntityInsertEvent<T extends BpelEntity> extends IndexedChangeEvent
{

    private static final long serialVersionUID = 3559485179223140644L;

    /**
     * Constructor for EntityInsertEvent with source ref.
     *
     * @param source
     *            source of event ( who produce this event ).
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param value
     *            entity value.
     */
    public EntityInsertEvent( Object source, BpelEntity parent, String name,
            T value )
    {
        super(source, parent, name, -1);
        myValue = value;
    }

    /**
     * Constructor for EntityInsertEvent with source ref and index.
     * 
     * @param source
     *            source of event ( who produce this event ).
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param value
     *            entity value.
     * @param index
     *            index of child entity in parent.
     */
    public EntityInsertEvent( Object source, BpelEntity parent, String name,
            T value, int index )
    {
        super(source, parent, name, index);
        myValue = value;
    }

    /**
     * Constructor for EntityInsertEvent without source ref.
     * 
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param value
     *            entity value.
     */
    public EntityInsertEvent( BpelEntity parent, String name, T value ) {
        this(Thread.currentThread(), parent, name, value);
    }

    /**
     * Constructor for EntityInsertEvent with index and without source ref.
     * 
     * @param parent
     *            element in OM that contains event entity.
     * @param name
     *            entity name.
     * @param value
     *            entity value.
     * @param index
     *            index of child entity in parent.
     */
    public EntityInsertEvent( BpelEntity parent, String name, T value, int index )
    {
        this(Thread.currentThread(), parent, name, value, index);
    }

    /**
     * @return inserted value.
     */
    public T getValue() {
        return myValue;
    }

    private T myValue;
}
