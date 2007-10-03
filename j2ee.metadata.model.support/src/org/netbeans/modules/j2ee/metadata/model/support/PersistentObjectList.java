/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        boolean fireChange;
        if (list.size() > 0) {
            type2Objects.put(typeHandle, list);
            fireChange = true;
        } else {
            List<T> oldList = type2Objects.remove(typeHandle);
            fireChange = oldList != null;
        }
        if (fireChange) {
            changeSupport.fireChange();
        }
    }

    public List<T> remove(ElementHandle<TypeElement> typeHandle) {
        List<T> oldList = type2Objects.remove(typeHandle);
        if (oldList != null) {
            changeSupport.fireChange();
        }
        return oldList;
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
