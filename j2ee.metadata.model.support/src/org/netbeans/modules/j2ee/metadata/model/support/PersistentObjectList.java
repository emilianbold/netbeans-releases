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

package org.netbeans.modules.j2ee.metadata.model.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.*;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectList<T extends PersistentObject> {

    private static final Logger LOGGER = Logger.getLogger(PersistentObjectManager.class.getName());

    private final Map<ElementHandle<TypeElement>, List<T>> type2Objects = new HashMap<ElementHandle<TypeElement>, List<T>>();
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void add(List<T> objects) {
        for (T newObject : objects) {
            List<T> list = type2Objects.get(newObject.getTypeElementHandle());
            if (list == null) {
                list = new ArrayList<T>();
                type2Objects.put(newObject.getTypeElementHandle(), list);
            }
            list.add(newObject);
        }
        changeSupport.fireChange();
    }

    public void put(ElementHandle<TypeElement> typeHandle, List<T> objects) {
        List<T> list = new ArrayList<T>();
        for (T object : objects) {
            ElementHandle<TypeElement> sourceHandle = object.getTypeElementHandle();
            if (sourceHandle.equals(typeHandle)) {
                list.add(object);
            } else {
                LOGGER.log(Level.WARNING, "setObjects: ignoring object with incorrect ElementHandle {0} (expected {1})", new Object[] { sourceHandle, typeHandle }); // NOI18N
            }
        }
        if (list.size() > 0) {
            type2Objects.put(typeHandle, list);
        } else {
            type2Objects.remove(typeHandle);
        }
        changeSupport.fireChange();
    }

    public List<T> remove(ElementHandle<TypeElement> typeHandle) {
        List<T> list = type2Objects.remove(typeHandle);
        if (list != null) {
            changeSupport.fireChange();
        }
        return list;
    }

    public void clear() {
        type2Objects.clear();
        changeSupport.fireChange();
    }

    public List<T> get() {
        List<T> result = new ArrayList<T>(type2Objects.size() * 2);
        for (List<T> list : type2Objects.values()) {
            result.addAll(list);
        }
        return Collections.unmodifiableList(result);
    }

    public List<T> get(ElementHandle<TypeElement> typeHandle) {
        List<T> list = type2Objects.get(typeHandle);
        return list != null ? Collections.unmodifiableList(list) : null;
    }
}
