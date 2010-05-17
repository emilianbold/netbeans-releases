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
package org.netbeans.modules.vmd.api.model;

import java.util.Set;
import java.util.HashMap;

/**
 * The class represents a document change event. There is an only one event type for document changes. The class tracks
 * all types of changes. Types of changes are: Any change of a property value, components hierarchy, or selection.
 * <p>
 * The event also contains all values of property values. These old values are one that were store at the begining of the transaction.
 * When a property is overriden many times, only the newest value is stored in the document and the oldest value is stored
 * in the event.
 * <p>
 * When a component hierarchy is changed, the event remembers which components were affected during the transaction.
 * The newest hierarchy is stored in the document. There is no way how to resolve state before the transaction. Also
 * when a component is removed from and then added back to structure, so hierarchy is the same as before, an event is fired
 * anyway and the component and its parent are marked as hierarchy-modified.
 * <p>
 * When a component is created, the event remembers all those components that were created during the transaction.
 *
 * @author David Kaspar
 */
public final class DesignEvent {

    private final long eventID;

    private final Set<DesignComponent> fullyComponents;
    private final Set<DesignComponent> partlyComponents;

    private final Set<DesignComponent> fullyHierarchies;
    private final Set<DesignComponent> partlyHierarchies;

    private Set<DesignComponent> descriptorChangedComponents;

    private Set<DesignComponent> createdComponents;

    // NOTE - do not expose this
    private HashMap<DesignComponent, HashMap<String, PropertyValue>> oldPropertyValues;

    private boolean selectionChanged;

    private boolean structureChanged;

    DesignEvent (long eventID, Set<DesignComponent> fullyComponents, Set<DesignComponent> partlyComponents, Set<DesignComponent> fullyHierarchies, Set<DesignComponent> partlyHierarchies, Set<DesignComponent> descriptorChangedComponents, Set<DesignComponent> createdComponents, HashMap<DesignComponent, HashMap<String, PropertyValue>> oldPropertyValues, boolean selectionChanged) {
        this.eventID = eventID;
        this.fullyComponents = fullyComponents;
        this.partlyComponents = partlyComponents;
        this.fullyHierarchies = fullyHierarchies;
        this.partlyHierarchies = partlyHierarchies;
        this.descriptorChangedComponents = descriptorChangedComponents;
        this.createdComponents = createdComponents;
        this.oldPropertyValues = oldPropertyValues;
        this.selectionChanged = selectionChanged;

        this.structureChanged = ! (fullyComponents.isEmpty () && partlyComponents.isEmpty () && fullyHierarchies.isEmpty () && partlyHierarchies.isEmpty () && descriptorChangedComponents.isEmpty () && createdComponents.isEmpty () && oldPropertyValues.isEmpty ());
    }

    /**
     * Returns an event id. The id is increasing non-negative number.
     * @return the event id
     */
    public long getEventID () {
        return eventID;
    }

    /**
     * Returns a set of all components that have at least one property changed during a transaction.
     * @return the set of fully property-affected components
     */
    public Set<DesignComponent> getFullyAffectedComponents () {
        return fullyComponents;
    }

    /**
     * Returns a set of all components that have changed their placement in components-hierarchy during a transaction.
     * <p>
     * An affected component is the one that is added to or removed from a parent component. The parent component is
     * an affected component too.
     * @return the set of fully hierarchy-affected components
     */
    public Set<DesignComponent> getFullyAffectedHierarchies () {
        return fullyHierarchies;
    }

    /**
     * Returns a set of all components where they or one of their sub-components have at least one property changed during a transaction.
     * @return the set of partly property-affected components
     */
    public Set<DesignComponent> getPartlyAffectedComponents () {
        return partlyComponents;
    }

    /**
     * Returns a set of all components where they or one of their sub-components have changed their placement
     * in components-hierarchy during a transaction.
     * <p>
     * An affected component is the one that is added to or removed from a parent component. The parent component is
     * an affected component too.
     * @return the set of partly hierarchy-affected components
     */
    public Set<DesignComponent> getPartlyAffectedHierarchies () {
        return partlyHierarchies;
    }

    /**
     * Returns a set of all components whose component descriptor is changed during a transaction because of component-descriptor-registry update.
     * @return the set of components with changed descriptor
     */
    public Set<DesignComponent> getDescriptorChangedComponents () {
        return descriptorChangedComponents;
    }

    /**
     * Returns a set of all components that were created during a transaction.
     * @return the set of created components
     */
    public Set<DesignComponent> getCreatedComponents () {
        return createdComponents;
    }

    /**
     * Returns an old value of a property of a component.
     * @param component the component
     * @param propertyName the property name
     * @return the old value, null if a property is not changed
     */
    public PropertyValue getOldPropertyValue (DesignComponent component, String propertyName) {
        HashMap<String, PropertyValue> properties = oldPropertyValues.get (component);
        if (properties == null)
            return null;
        return properties.get (propertyName);
    }

    /**
     * Returns whether a property of a component is changed.
     * @param component the component
     * @param propertyName the property name
     * @return true, if a property is changed
     */
    public boolean isComponentPropertyChanged (DesignComponent component, String propertyName) {
        HashMap<String, PropertyValue> properties = oldPropertyValues.get (component);
        return properties != null  &&  properties.containsKey (propertyName);
    }

    /**
     * Returns whether a selection is changed. The actual selection is store in a document.
     * @return true if changed.
     */
    public boolean isSelectionChanged () {
        return selectionChanged;
    }

    /**
     * Returns whether a document structure is changed. It means a property, hierarchy, or a descriptor is changed.
     * @return true if a structure is changed
     */
    public boolean isStructureChanged () {
        return structureChanged;
    }

}
