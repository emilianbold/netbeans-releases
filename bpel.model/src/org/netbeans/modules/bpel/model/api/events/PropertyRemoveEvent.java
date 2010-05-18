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
public class PropertyRemoveEvent extends ChangeEvent {

    private static final long serialVersionUID = -4182029303002677554L;

    /**
     * Constructor for PropertyRemoveEvent with source ref.
     *
     * @param source
     *            source of event ( who produce this event ).
     * @param parent
     *            element in OM that contains event attribute.
     * @param name
     *            attribute name.
     * @param value
     *            attribute value.
     */
    public PropertyRemoveEvent( Object source, BpelEntity parent, String name,
            Object value )
    {
        super(source, parent, name);
        myValue = value;
    }

    /**
     * Constructor that don't have source reference.
     * 
     * @param parent
     *            element in OM, entity that contains attribute with value
     * @param name
     *            attribute name
     * @param value
     *            old attribue value
     */
    public PropertyRemoveEvent( BpelEntity parent, String name, Object value ) {
        this(Thread.currentThread(), parent, name, value);
    }

    /**
     * @return old attribute value.
     */
    public Object getOldValue() {
        return myValue;
    }

    private Object myValue;
}
