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
public class ArrayUpdateEvent<T extends BpelEntity> extends ChangeEvent {

    private static final long serialVersionUID = -952921787574357005L;

    /**
     * Constructor for ArrayUpdateEvent with source ref.
     *
     * @param source
     *            source of event.
     * @param parent
     *            parent container which caused by array changing.
     * @param name
     *            name of tag ( not always have sense. F.e. sequence contains
     *            activitis with its own tags and we cannot put the same name
     *            for each activity. But f.e. for VariableContainer it will be
     *            "variable" ).
     * @param oldArray  Old array value.
     * @param newArray  New array value.
     */
    public ArrayUpdateEvent( Object source, BpelEntity parent, String name,
            T[] oldArray, T[] newArray )
    {
        super(source, parent, name);
        myOldArray = oldArray;
        myNewArray = newArray;
    }

    /**
     * Constructor for ArrayUpdateEvent without source ref.
     * 
     * @param parent
     *            parent container which caused by array changing.
     * @param name
     *            name of tag - see comments for previous CTOR.
     * @param oldArray  Old array value.
     * @param newArray  New array value.
     */
    public ArrayUpdateEvent( BpelEntity parent, String name, T[] oldArray,
            T[] newArray )
    {
        this(Thread.currentThread(), parent, name, oldArray, newArray);
    }

    /**
     * @return array before event appeared.
     */
    public T[] getOldArray() {
        return myOldArray;
    }

    /**
     * @return array after changing.
     */
    public T[] getNewArray() {
        return myNewArray;
    }

    private T[] myOldArray;

    private T[] myNewArray;
}
