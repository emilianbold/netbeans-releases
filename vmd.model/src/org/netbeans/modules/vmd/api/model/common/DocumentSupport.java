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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    public static List<DesignComponent> gatherAllComponentsOfTypeID (DesignDocument document, TypeID typeID) {
        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> ();
        gatherAllComponentsOfTypeID (list, typeID, document.getRootComponent ());
        return list;
    }

    /**
     * Gathers all components in a document that are containing presenter of presenterClass.
     * @param document the document
     * @param presenterClass the presenterClass
     * @return the list of components
     */
    public static <T extends Presenter> List<DesignComponent> gatherAllComponentsContainingPresenterClass (DesignDocument document, Class<T> presenterClass) {
        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> ();
        gatherAllComponentsContainingPresenterClass (list, document.getRootComponent (), presenterClass);
        return list;
    }

    private static <T extends Presenter> void gatherAllComponentsContainingPresenterClass (ArrayList<DesignComponent> list, DesignComponent component, Class<T> presenterClass) {
        if (component.getPresenter (presenterClass) != null)
            list.add (component);
        for (DesignComponent child : component.getComponents ())
            gatherAllComponentsContainingPresenterClass (list, child, presenterClass);
    }

    /**
     * Gathers all components under specified component.
     * It returns a list of components that are or inherits a specific type-id using DescriptorRegistry.isInHierarchy method.
     * @param component the component underneath which it searches
     * @param typeID the required typeid of components
     * @return the filtered list of components
     */
    public static List<DesignComponent> gatherAllComponentsOfTypeID (DesignComponent component, TypeID typeID) {
        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> ();
        gatherAllComponentsOfTypeID (list, typeID, component);
        return list;
    }

    private static void gatherAllComponentsOfTypeID (Collection<? super DesignComponent> list, TypeID typeID, DesignComponent component) {
        if (component.getDocument ().getDescriptorRegistry ().isInHierarchy (typeID, component.getType ()))
            list.add (component);
        for (DesignComponent child : component.getComponents ())
            gatherAllComponentsOfTypeID (list, typeID, child);
    }

    /**
     * Takes a specified list of presenters and removed those presenters which are compatible with specified presenter class.
     * @param presenters the list of presenters
     * @param presenterClass the presenter class
     */
    public static void removePresentersOfClass (ArrayList<? super Presenter> presenters, Class presenterClass) {
        for (Iterator<? super Presenter> iterator = presenters.iterator (); iterator.hasNext ();) {
            Object object = iterator.next ();
            if (presenterClass.isInstance (object))
                iterator.remove ();
        }
    }

    /**
     * Takes a specified list of presenters and removed those presenters which are compatible with specified presenter id.
     * @param presenters the list of presenters
     * @param presenterID the presenter id
     */
    public static void removePresentersOfPresenterID (ArrayList<Presenter> presenters, String presenterID) {
        if (presenterID == null)
            return;
        for (Iterator<Presenter> iterator = presenters.iterator (); iterator.hasNext ();) {
            Presenter presenter = iterator.next ();
            if (presenter instanceof IdentifiablePresenter)
                if (presenterID.equals (((IdentifiablePresenter) presenter).getPresenterID ()))
                    iterator.remove ();
        }
    }

    /**
     * Sorts a list of presenters by their order defined by OrderablePresenter.
     * @param presenters the list of presenters
     */
    public static void sortPresentersByOrder (ArrayList<? extends OrderablePresenter> presenters) {
        Collections.sort (presenters, new Comparator<OrderablePresenter>() {
            public int compare (OrderablePresenter o1, OrderablePresenter o2) {
                return o1.getOrder () - o2.getOrder ();
            }
        });
    }

    /**
     * Returns a list of children components for a specified component. The children components will be only of a specified typeID.
     * @param component the component which children are used
     * @param typeID the type id of found children
     * @return the list of found children components
     */
    public static List<DesignComponent> gatherSubComponentsOfType (DesignComponent component, TypeID typeID) {
        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> ();
        for (DesignComponent child : component.getComponents ()) {
            if (typeID.equals (child.getType ()))
                list.add (child);
        }
        return list;
    }

    /**
     * Gathers all presenters of a specified class that are currently attached to a component in the main tree of components in a specific document.
     * @param document the document
     * @param clazz the presenter class that is searched for
     * @return a collection of found presenters
     */
    public static <T extends Presenter> Collection<T> gatherAllPresentersOfClass (DesignDocument document, Class<T> clazz) {
        ArrayList<T> list = new ArrayList<T> ();
        gatherAllPresentersOfClass (list, document.getRootComponent (), clazz);
        return list;
    }

    private static <T extends Presenter> void gatherAllPresentersOfClass (ArrayList<T> list, DesignComponent component, Class<T> clazz) {
        list.addAll (component.getPresenters (clazz));
        for (DesignComponent child : component.getComponents ())
            gatherAllPresentersOfClass (list, child, clazz);
    }

    /**
     * Returns a collection of all presenters of a specified class that are in a specified collection.
     * @param presenters the collec
     * @param clazz the class of presenters to be found
     * @return the collection of found presenters
     */
    public static <T extends Presenter> Collection<T> filterPresentersForClass (Collection<? extends Presenter> presenters, Class<T> clazz) {
        ArrayList<T> list = new ArrayList<T> ();
        for (Presenter presenter : presenters)
            if (clazz.isInstance (presenter))
                list.add ((T) presenter);
        return list;
    }

}
