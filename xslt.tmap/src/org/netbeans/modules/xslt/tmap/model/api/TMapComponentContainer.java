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
package org.netbeans.modules.xslt.tmap.model.api;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface TMapComponentContainer extends TMapComponent {

    /**
     * Common method for deleting child from this parent.
     *
     * @param <T>
     *            type of component.
     * @param component
     *            element for remove.
     */
    <T extends TMapComponent> void remove( T component );

    /**
     * Returns index of <code>entity</code> in children list with 
     * specified <code>type</code>.
     * If component is not child of this container then it returns -1.
     * @param <T> TMapComponent type.
     * @param type TMapComponent class.
     * @param component Child component in this container. 
     * @return Index of <code>component</code>.
     */
    <T extends TMapComponent> int indexOf( Class<T> type , T component );
}
