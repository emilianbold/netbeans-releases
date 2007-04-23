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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;

/**
 *
 * @author abadea
 */
public class PersistentObjectManager<T extends PersistentObject> {

    private final AnnotationModelHelper helper;
    private final ObjectProvider<T> provider;
    private final Map<ElementHandle<TypeElement>, T> objects = new HashMap<ElementHandle<TypeElement>, T>();

    private boolean initialized = false;

    PersistentObjectManager(AnnotationModelHelper helper, ObjectProvider<T> provider) {
        this.helper = helper;
        this.provider = provider;
    }

    public AnnotationModelHelper getHelper() {
        return helper;
    }

    private void ensureInitialized() {
        if (!initialized) {
            for (T object : provider.createInitialObjects()) {
                objects.put(object.getSourceElementHandle(), object);
            }
            initialized = true;
        }
    }

    public Collection<T> getObjects() {
        ensureInitialized();
        return objects.values();
    }

    void typesAdded(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        List<TypeElement> types = new ArrayList<TypeElement>();
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            TypeElement type = typeHandle.resolve(helper.getCompilationController());
            if (type == null) {
                continue;
            }
            types.add(type);
        }
        for (T object : provider.createObjects(types)) {
            objects.put(object.getSourceElementHandle(), object);
        }
    }

    void typesRemoved(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            objects.remove(typeHandle);
        }
    }

    void typesChanged(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            T object = objects.get(typeHandle);
            if (object != null) {
                object.sourceElementChanged();
            }
        }
    }

    void rootsChanged() {
        initialized = false;
        objects.clear();
    }
}
