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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectManager<T extends PersistentObject> implements JavaContextListener {

    // XXX perhaps also initialize temporary when classpath not registered in GPR
    // (since then there would be no events)

    private static final Logger LOGGER = Logger.getLogger(PersistentObjectManager.class.getName());
    private static final boolean NO_EVENTS = Boolean.getBoolean("netbeans.metadata.model.noevents"); // NOI18N
    // XXX for M9 only
    // XXX causes PersistentObjectManagerTest to fail; currently excluded in test/cfg-unit.xml
    // private static final boolean NO_EVENTS = true;

    private final AnnotationModelHelper helper;
    private final ObjectProvider<T> provider;
    private final Map<ElementHandle<TypeElement>, List<T>> objects = new HashMap<ElementHandle<TypeElement>, List<T>>();

    private boolean initialized = false;
    // not private because used in unit tests
    boolean temporary = false;

    static <V extends PersistentObject> PersistentObjectManager<V> create(AnnotationModelHelper helper, ObjectProvider<V> provider) {
        PersistentObjectManager<V> pom = new PersistentObjectManager<V>(helper, provider);
        if (NO_EVENTS) {
            LOGGER.log(Level.FINE, "ignoring events"); // NOI18N
        }
        helper.addJavaContextListener(pom);
        return pom;
    }

    private PersistentObjectManager(AnnotationModelHelper helper, ObjectProvider<T> provider) {
        this.helper = helper;
        this.provider = provider;
    }

    public Collection<T> getObjects() {
        ensureInitialized();
        List<T> result = new ArrayList<T>(objects.size() * 2);
        for (List<T> list : objects.values()) {
            result.addAll(list);
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "getObjects returning {0} objects: {1}", new Object[] { result.size(), result}); // NOI18N
        } else {
            LOGGER.log(Level.FINE, "getObjects returning {0} objects", result.size()); // NOI18N
        }
        return Collections.unmodifiableList(result);
    }

    private void ensureInitialized() {
        if (!initialized) {
            boolean scanInProgress = SourceUtils.isScanInProgress();
            temporary = NO_EVENTS | scanInProgress;
            if (temporary) {
                LOGGER.log(Level.FINE, "initalizing temporarily (scanInProgress: {0})", scanInProgress); // NOI18N
            } else {
                LOGGER.log(Level.FINE, "intializing"); // NOI18N
            }
            List<T> objects = provider.createInitialObjects();
            LOGGER.log(Level.FINE, "created initial objects {0}", objects); // NOI18N
            putObjects(objects);
            initialized = true;
        }
    }

    private void deinitialize() {
        initialized = false;
        objects.clear();
    }

    void typesAdded(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        // XXX assert not in AMH java context
        if (!initialized) {
            return;
        }
        LOGGER.log(Level.FINE, "typesAdded called with {0}", typeHandles); // NOI18N
        List<TypeElement> types = new ArrayList<TypeElement>();
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            TypeElement type = typeHandle.resolve(helper.getCompilationController());
            if (type == null) {
                LOGGER.log(Level.WARNING, "typesAdded: type {0} has dissapeared", typeHandle); // NOI18N
                continue;
            }
            List<T> newObjects = provider.createObjects(type);
            LOGGER.log(Level.FINE, "typesAdded: new objects {0}", newObjects); // NOI18N
            putObjects(newObjects);
        }
    }

    void typesRemoved(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        // XXX assert not in AMH java context
        if (!initialized) {
            return;
        }
        LOGGER.log(Level.FINE, "typesRemoved called with {0}", typeHandles); // NOI18N
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            List<T> list = objects.remove(typeHandle);
            if (list != null) {
                LOGGER.log(Level.FINE, "typesRemoved: removing objects {0}", list); // NOI18N
            }
        }
    }

    void typesChanged(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        // XXX assert not in AMH java context
        if (!initialized) {
            return;
        }
        LOGGER.log(Level.FINE, "typesChanged called with {0}", typeHandles); // NOI18N
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            List<T> list = objects.get(typeHandle);
            if (list != null){
                // we have objects based on this type
                for (Iterator<T> i = list.iterator(); i.hasNext();) {
                    T object = i.next();
                    boolean valid = object.sourceElementChanged();
                    if (valid) {
                        // the object changed as the type changed
                        LOGGER.log(Level.FINE, "typesChanged: changing object {0}", object); // NOI18N
                    } else {
                        // as the type changed the object is not valid anymore, removing it
                        i.remove();
                        LOGGER.log(Level.FINE, "typesChanged: removing object {0}", object); // NOI18N
                    }
                }
            } else {
                // we don't have any object based on this type
                TypeElement type = typeHandle.resolve(helper.getCompilationController());
                if (type == null) {
                    LOGGER.log(Level.WARNING, "typesChanged: type {0} has dissapeared", typeHandle); // NOI18N
                    continue;
                }
                List<T> newObjects = provider.createObjects(type);
                LOGGER.log(Level.FINE, "typesChanged: new objects {0}", newObjects); // NOI18N
                putObjects(newObjects);
            }
        }
    }

    private void putObjects(List<T> newObjects) {
        for (T newObject : newObjects) {
            List<T> list = objects.get(newObject.getSourceElementHandle());
            if (list == null) {
                list = new LinkedList<T>();
                objects.put(newObject.getSourceElementHandle(), list);
            }
            list.add(newObject);
        }
    }

    void rootsChanged() {
        // XXX assert not in AMH java context
        LOGGER.log(Level.FINE, "rootsChanged called"); // NOI18N
        deinitialize();
    }

    public void javaContextLeft() {
        if (temporary) {
            LOGGER.log(Level.FINE, "discarding temporary manager"); // NOI18N
            deinitialize();
        }
    }
}
