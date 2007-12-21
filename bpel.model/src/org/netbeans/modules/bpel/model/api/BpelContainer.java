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

package org.netbeans.modules.bpel.model.api;

/**
 * @author ads
 */
public interface BpelContainer extends BpelEntity {

    /**
     * Common method for deleting child from this parent.
     *
     * @param <T>
     *            type of entity.
     * @param entity
     *            element for remove.
     */
    <T extends BpelEntity> void remove( T entity );

    /**
     * Returns index of <code>entity</code> in children list with 
     * specified <code>type</code>.
     * If entity is not child of this container then it returns -1.
     * @param <T> Bpel entity type.
     * @param type Bpel entity class.
     * @param entity Child entity in this container. 
     * @return Index of <code>entity</code>.
     */
    <T extends BpelEntity> int indexOf( Class<T> type , T entity );
    
}
