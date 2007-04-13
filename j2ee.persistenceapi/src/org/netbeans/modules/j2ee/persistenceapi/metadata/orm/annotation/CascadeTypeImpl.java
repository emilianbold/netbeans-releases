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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class CascadeTypeImpl implements CascadeType {

    // XXX CascadeType and this impl should be replaced with something simpler

    private final EmptyType all, persist, merge, remove, refresh;

    public CascadeTypeImpl() {
        all = null;
        persist = null;
        merge = null;
        remove = null;
        refresh = null;
    }

    public CascadeTypeImpl(List<AnnotationValue> cascadeValues) {
        EmptyType tempAll = null, tempPersist = null, tempMerge = null, tempRemove = null, tempRefresh = null;
        for (AnnotationValue value : cascadeValues) {
            Name valueName = ((VariableElement)value.getValue()).getSimpleName();
            if (valueName.contentEquals("ALL")) { // NOI18N
                tempAll = new EmptyTypeImpl();
            } else if (valueName.contentEquals("PERSIST")) { // NOI18N
                tempPersist = new EmptyTypeImpl();
            } else if (valueName.contentEquals("MERGE")) { // NOI18N
                tempMerge = new EmptyTypeImpl();
            } else if (valueName.contentEquals("REMOVE")) { // NOI18N
                tempRemove = new EmptyTypeImpl();
            } else if (valueName.contentEquals("REFRESH")) { // NOI18N
                tempRefresh = new EmptyTypeImpl();
            }
        }
        all = tempAll;
        persist = tempPersist;
        merge = tempMerge;
        remove = tempRemove;
        refresh = tempRefresh;
    }

    public void setCascadeAll(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadeAll() {
        return all;
    }

    public EmptyType newEmptyType() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setCascadePersist(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadePersist() {
        return persist;
    }

    public void setCascadeMerge(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadeMerge() {
        return merge;
    }

    public void setCascadeRemove(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadeRemove() {
        return remove;
    }

    public void setCascadeRefresh(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadeRefresh() {
        return refresh;
    }
}
