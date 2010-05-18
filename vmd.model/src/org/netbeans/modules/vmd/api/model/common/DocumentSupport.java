/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.api.model.common;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.IdentifiablePresenter;
import org.netbeans.modules.vmd.api.model.presenters.OrderablePresenter;

import java.util.*;

/**
 * @author David Kaspar
 */
public class DocumentSupport {
    
    /**
     * Gathers all components in main tree of components in a specific document.
     * It returns a list of components that are or inherits a specific type-id using DescriptorRegistry.isInHierarchy method.
     * @param document the document
     * @param typeID the required typeid of components
     * @return the filtered list of components
     */
    public static List<DesignComponent> gatherAllComponentsOfTypeID(DesignDocument document, TypeID typeID) {
        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> ();
        gatherAllComponentsOfTypeID(list, typeID, document.getRootComponent());
        return list;
    }
    
    /**
     * Gathers all components in a document that are containing presenter of presenterClass.
     * @param document the document
     * @param presenterClass the presenterClass
     * @return the list of components
     */
    public static <T extends Presenter> List<DesignComponent> gatherAllComponentsContainingPresenterClass(DesignDocument document, Class<T> presenterClass) {
        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> ();
        DesignComponent rootComponent = document.getRootComponent();
        if (rootComponent != null)
            gatherAllComponentsContainingPresenterClass(list, rootComponent, presenterClass);
        return list;
    }
    
    private static <T extends Presenter> void gatherAllComponentsContainingPresenterClass(ArrayList<DesignComponent> list, DesignComponent component, Class<T> presenterClass) {
        if (component.getPresenter(presenterClass) != null)
            list.add(component);
        for (DesignComponent child : component.getComponents())
            gatherAllComponentsContainingPresenterClass(list, child, presenterClass);
    }
    
    /**
     * Gathers all components under specified component.
     * It returns a list of components that are or inherits a specific type-id using DescriptorRegistry.isInHierarchy method.
     * @param component the component underneath which it searches
     * @param typeID the required typeid of components
     * @return the filtered list of components
     */
    public static List<DesignComponent> gatherAllComponentsOfTypeID(DesignComponent component, TypeID typeID) {
        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> ();
        gatherAllComponentsOfTypeID(list, typeID, component);
        return list;
    }
    
    private static void gatherAllComponentsOfTypeID(Collection<? super DesignComponent> list, TypeID typeID, DesignComponent component) {
        if (component.getDocument().getDescriptorRegistry().isInHierarchy(typeID, component.getType()))
            list.add(component);
        for (DesignComponent child : component.getComponents())
            gatherAllComponentsOfTypeID(list, typeID, child);
    }
    
    /**
     * Takes a specified list of presenters and removed those presenters which are compatible with specified presenter class.
     * @param presenters the list of presenters
     * @param presenterClass the presenter class
     */
    public static void removePresentersOfClass(ArrayList<? super Presenter> presenters, Class presenterClass) {
        for (Iterator<? super Presenter> iterator = presenters.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (presenterClass.isInstance(object))
                iterator.remove();
        }
    }

    /**
     * Takes a specified list of presenters and removed those presenters which are provided in second parameter.
     * @param presenters the list of presenters
     * @param presenter to remove
     */
    public static void removePresenter(ArrayList<? super Presenter> presenters, Presenter presenter) {
        for (Iterator<? super Presenter> iterator = presenters.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (presenter == object) {
                iterator.remove();
                return;
            }
        }
    }
    
    /**
     * Takes a specified list of presenters and removed those presenters which are compatible with specified presenter id.
     * @param presenters the list of presenters
     * @param presenterID the presenter id
     */
    public static void removePresentersOfPresenterID(ArrayList<Presenter> presenters, String presenterID) {
        if (presenterID == null)
            return;
        for (Iterator<Presenter> iterator = presenters.iterator(); iterator.hasNext();) {
            Presenter presenter = iterator.next();
            if (presenter instanceof IdentifiablePresenter)
                if (presenterID.equals(((IdentifiablePresenter) presenter).getPresenterID()))
                    iterator.remove();
        }
    }
    
    /**
     * Sorts a list of presenters by their order defined by OrderablePresenter.
     * @param presenters the list of presenters
     */
    public static void sortPresentersByOrder(ArrayList<? extends OrderablePresenter> presenters) {
        Collections.sort(presenters, new Comparator<OrderablePresenter>() {
            public int compare(OrderablePresenter o1, OrderablePresenter o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
    }
    
    /**
     * Returns a list of children components for a specified component. The children components will be only of a specified typeID.
     * @param component the component which children are used
     * @param typeID the type id of found children
     * @return the list of found children components
     */
    public static List<DesignComponent> gatherSubComponentsOfType(DesignComponent component, TypeID typeID) {
        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> ();
        for (DesignComponent child : component.getComponents()) {
            if (typeID.equals(child.getType()))
                list.add(child);
        }
        return list;
    }
    
    /**
     * Gathers all presenters of a specified class that are currently attached to a component in the main tree of components in a specific document.
     * @param document the document
     * @param clazz the presenter class that is searched for
     * @return a collection of found presenters
     */
    public static <T extends Presenter> Collection<T> gatherAllPresentersOfClass(DesignDocument document, Class<T> clazz) {
        ArrayList<T> list = new ArrayList<T> ();
        gatherAllPresentersOfClass(list, document.getRootComponent(), clazz);
        return list;
    }
    
    private static <T extends Presenter> void gatherAllPresentersOfClass(ArrayList<T> list, DesignComponent component, Class<T> clazz) {
        list.addAll(component.getPresenters(clazz));
        for (DesignComponent child : component.getComponents())
            gatherAllPresentersOfClass(list, child, clazz);
    }
    
    /**
     * Returns a collection of all presenters of a specified class that are in a specified collection.
     * @param presenters the collec
     * @param clazz the class of presenters to be found
     * @return the collection of found presenters
     */
    public static <T extends Presenter> Collection<T> filterPresentersForClass(Collection<? extends Presenter> presenters, Class<T> clazz) {
        ArrayList<T> list = new ArrayList<T> ();
        for (Presenter presenter : presenters)
            if (clazz.isInstance(presenter))
                list.add((T) presenter);
        return list;
    }

    /**
     * Returns component producers for given type.
     * @param document the document
     * @param typeID type of searched producers
     * @return the producers
     */
    public static Collection<ComponentProducer> getComponentProducers(DesignDocument document, TypeID typeID) {
        Collection<ComponentProducer> producers = new HashSet<ComponentProducer>();
        DescriptorRegistry registry = document.getDescriptorRegistry ();
        for (ComponentProducer producer : registry.getComponentProducers()) {
            if (registry.isInHierarchy (typeID, producer.getMainComponentTypeID ()))
                producers.add(producer);
        }
        return producers;
    }

    /**
     * Returns a design component producer for given producer id.
     * @param document the document
     * @param producerID producer id of searched producer
     * @return the producer
     */
    public static ComponentProducer getComponentProducer(DesignDocument document, String producerID) {
        for (ComponentProducer producer : document.getDescriptorRegistry().getComponentProducers()) {
            if (producer.getProducerID ().equals(producerID))
                return producer;
        }
        return null;
    }

}
