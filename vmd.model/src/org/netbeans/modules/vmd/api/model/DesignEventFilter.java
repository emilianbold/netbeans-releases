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

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;

/**
 * This class represents an event filter. This allows to filter some events that a listener is not interested in.
 * Basically all events are fired to a listener when they contain at least one change that is recognized by a filter.
 *
 * @author David Kaspar
 */
public final class DesignEventFilter {

    private DesignEventFilter[] childFilters;

    // HINT - merge ComponentItem and HierarchyItem classes

    private static final class ComponentItem {
        private long componentID;
        private boolean includingChildren;
    }

    private static final class HierarchyItem {
        private long componentID;
        private boolean includingChildren;
    }

    private static final class DescentItem {
        private long componentID;
        private String propertyName;
        private HashSet<DesignComponent> components; // TODO - this should be converted to HashSet<Long>
    }

    private static final class ParentItem {
        private long componentID;
        private int levels;
        private ArrayList<DesignComponent> components; // TODO - this should be converted to HashSet<Long>
        public boolean hierarchyOnly;
    }

    private boolean global;
    private boolean creation;
    private boolean selection;

    private final ArrayList<ComponentItem> components = new ArrayList<ComponentItem> ();
    private final ArrayList<HierarchyItem> hierarchy = new ArrayList<HierarchyItem> ();
    private final ArrayList<DescentItem> descents = new ArrayList<DescentItem> ();
    private final ArrayList<ParentItem> parents = new ArrayList<ParentItem> ();
    private final ArrayList<Long> descriptors = new ArrayList<Long> ();

    /**
     * Creates a new instance of an event filter.
     */
    public DesignEventFilter (DesignEventFilter... childFilters) {
        assert constructorAssert (childFilters);
        this.childFilters = childFilters;
    }

    private boolean constructorAssert (DesignEventFilter... childFilters) {
        for (DesignEventFilter filter : childFilters)
            assert filter != null;
        return true;
    }

    /**
     * Sets whether all changes are important.
     * @param global true if a listener should be notified about any change
     * @return this filter
     */
    public DesignEventFilter setGlobal (boolean global) {
        this.global = global;
        return this;
    }

    /**
     * Sets whether a component creation change is important.
     * @param creation true if it is important
     * @return this filter
     */
    public DesignEventFilter setCreation (boolean creation) {
        this.creation = creation;
        return this;
    }

    /**
     * Sets whether a selection change is important.
     * @param selection true if it is important
     * @return this filter
     */
    public DesignEventFilter setSelection (boolean selection) {
        this.selection = selection;
        return this;
    }

    /**
     * Adds a specific component into important component, so a listener is notified whenever any property of the component is changed.
     * Also it could be specified whether its children (both direct and indirect) are important too.
     * @param component the important component
     * @param includingChildren true if its children of the important component should be taken as important too
     * @return this filter
     */
    public DesignEventFilter addComponentFilter (DesignComponent component, boolean includingChildren) {
        ComponentItem item = new ComponentItem ();
        item.componentID = component.getComponentID ();
        item.includingChildren = includingChildren;
        components.add (item);
        return this;
    }

    /**
     * Removes a specific component from a collection of important components for property changes.
     * <p>
     * Note: The parameters must be exactly the same as they were when a addComponentFilter method was called.
     * @param component the component
     * @param includingChildren the including children flag
     * @return this filter
     */
    public DesignEventFilter removeComponentFilter (DesignComponent component, boolean includingChildren) {
        long componentID = component.getComponentID ();
        for (Iterator<ComponentItem> it = components.iterator (); it.hasNext ();) {
            ComponentItem componentItem = it.next ();
            if (componentItem.componentID == componentID && componentItem.includingChildren == includingChildren) {
                it.remove ();
                break;
            }
        }
        return this;
    }

    /**
     * Adds a specific component into important component, so a listener is notified whenever a hierarchy of the component is changed
     * (added into or removed from its parent component.
     * Also it could be specified whether its children (both direct and indirect) are important too.
     * @param component the important component
     * @param includingChildren true if its children of the important component should be taken as important too
     * @return this filter
     */
    public DesignEventFilter addHierarchyFilter (DesignComponent component, boolean includingChildren) {
        HierarchyItem item = new HierarchyItem ();
        item.componentID = component.getComponentID ();
        item.includingChildren = includingChildren;
        hierarchy.add (item);
        return this;
    }

    /**
     * Removes a specific component from a collection of important components for hierarchy-changed.
     * <p>
     * Note: The parameters must be exactly the same as they were when a addHierarchyFilter method was called.
     * @param component the component
     * @param includingChildren the including children flag
     * @return this filter
     */
    public DesignEventFilter removeHierarchyFilter (DesignComponent component, boolean includingChildren) {
        long componentID = component.getComponentID ();
        for (Iterator<HierarchyItem> it = hierarchy.iterator (); it.hasNext ();) {
            HierarchyItem  hierarchyItem = it.next ();
            if (hierarchyItem.componentID == componentID  &&  hierarchyItem.includingChildren == includingChildren) {
                it.remove ();
                break;
            }
        }
        return this;
    }

    /**
     * Adds a parent filter that notifies a listener whenever a hierarchy of any parent component in the tree is changed.
     * @param component the component
     * @param levels the number of levels of parent components that are inspected, 0 means the component itself only, 1 means its parent component too, ...
     * @param hierarchyOnly if true, then only hierarchy changes are important;
     *           if false, then both hierarchy and component property changes are important
     * @return this filter
     */
    public DesignEventFilter addParentFilter (DesignComponent component, int levels, boolean hierarchyOnly) {
        ParentItem item = new ParentItem ();
        item.componentID = component.getComponentID ();
        item.levels = levels;
        item.hierarchyOnly = hierarchyOnly;
        parents.add (item);
        return this;
    }

    /**
     * Removes a parent filter for a specific component and levels.
     * <p>
     * Note: The parameters must be exactly the same as they were when a addParentFilter method was called.
     * @param component the component
     * @param levels the number of levels of parent components that are inspected
     * @param hierarchyOnly if true, then only hierarchy changes are important;
     *           if false, then both hierarchy and component property changes are important
     * @return this filter
     */
    public DesignEventFilter removeParentFilter (DesignComponent component, int levels, boolean hierarchyOnly) {
        long componentID = component.getComponentID ();
        for (Iterator<ParentItem> it = parents.iterator (); it.hasNext ();) {
            ParentItem parentItem = it.next ();
            if (parentItem.componentID == componentID  &&  parentItem.levels == levels  &&  parentItem.hierarchyOnly == hierarchyOnly) {
                it.remove ();
                break;
            }
        }
        return this;
    }

    /**
     * Adds a descent filter that notifies a listener whenever a specific property of a specific component is changed or
     * any property of components that are referenced in the property value is changed.
     * @param component    the component
     * @param propertyName the property name
     * @return this filter
     */
    public DesignEventFilter addDescentFilter (DesignComponent component, String propertyName) {
        DescentItem item = new DescentItem ();
        item.componentID = component.getComponentID ();
        item.propertyName = propertyName;
        descents.add (item);
        return this;
    }

    /**
     * Remove a descent filter for a specific property of a specific component.
     * <p>
     * Note: The parameters must be exactly the same as they were when a addDescentFilter method was called.
     * @param component    the component
     * @param propertyName the property name
     * @return this filter
     */
    public DesignEventFilter removeDescentFilter (DesignComponent component, String propertyName) {
        long componentID = component.getComponentID ();
        for (Iterator<DescentItem> it = descents.iterator (); it.hasNext ();) {
            DescentItem descentItem = it.next ();
            if (descentItem.componentID == componentID  &&  descentItem.propertyName.equals (propertyName)) {
                it.remove ();
                break;
            }
        }
        return this;
    }

    /**
     * Adds a specific component into a collection of important components for descriptor-changed.
     * A listener is notified whenever a descriptor of the component is changed.
     * @param component the important component
     * @return this filter
     */
    public DesignEventFilter addDescriptorFilter (DesignComponent component) {
        descriptors.add (component.getComponentID ());
        return this;
    }

    /**
     * Removes a specific component from a collection of important components for descriptor-changed.
     * @param component the component
     * @return this filter
     */
    public DesignEventFilter removeDescriptorFilter (DesignComponent component) {
        descriptors.remove (component.getComponentID ());
        return this;
    }

    boolean isGlobal () {
        return global;
    }

    boolean isAffected (DesignDocument document, DesignEvent event) {
        assert Debug.isFriend (ListenerManager.class, "fireEventCore")  ||  Debug.isFriend (DesignEventFilter.class, "isAffected"); // NOI18N

        boolean changed = false;
        for (DescentItem item : descents) {
            DesignComponent component = document.getComponentByUID (item.componentID);
            if (component == null)
                continue;

            if (event.isComponentPropertyChanged (component, item.propertyName)) {
                item.components = null;
                changed = true;
            }
        }

        if (! parents.isEmpty ()) {
            Set<DesignComponent> fullyAffectedHierarchies = event.getFullyAffectedHierarchies ();
            for (ParentItem item : parents) {
                DesignComponent component = document.getComponentByUID (item.componentID);
                if (component == null)
                    continue;

                if (item.components != null) {
                    for (DesignComponent parentComponent : item.components) {
                        if (fullyAffectedHierarchies.contains (parentComponent)) {
                            item.components = null;
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }

        for (DesignEventFilter filter : childFilters)
            if (filter.isAffected (document, event))
                return true;

        if (changed)
            return true;

        if (global)
            return true;
        if (creation  &&  ! event.getCreatedComponents ().isEmpty ())
            return true;
        if (selection  &&  event.isSelectionChanged ())
            return true;

        Set<DesignComponent> fullyComponents = event.getFullyAffectedComponents ();
        Set<DesignComponent> partlyComponents = event.getPartlyAffectedComponents ();
        Set<DesignComponent> fullyHierarchies = event.getFullyAffectedHierarchies ();
        Set<DesignComponent> partlyHierarchies = event.getPartlyAffectedHierarchies ();
        Set<DesignComponent> fullyDescriptors = event.getDescriptorChangedComponents ();

        for (ComponentItem item : components) {
            DesignComponent component = document.getComponentByUID (item.componentID);
            if (component == null)
                continue;

            if (fullyComponents.contains (component))
                return true;

            if (item.includingChildren  &&  partlyComponents.contains (component))
                return true;
        }

        for (HierarchyItem item : hierarchy) {
            DesignComponent component = document.getComponentByUID (item.componentID);
            if (component == null)
                continue;

            if (fullyHierarchies.contains (component))
                return true;

            if (item.includingChildren && partlyHierarchies.contains (component))
                return true;
        }

        for (Long componentID : descriptors) {
            DesignComponent component = document.getComponentByUID (componentID);
            if (component == null)
                continue;
            if (fullyDescriptors.contains (component))
                return true;
        }

        for (DescentItem item : descents) {
            DesignComponent component = document.getComponentByUID (item.componentID);
            if (component == null)
                continue;

            if (item.components == null) {
                PropertyValue value = component.readProperty (item.propertyName);
                Debug.collectAllComponentReferences (value, item.components = new HashSet<DesignComponent> ());
            }

            for (DesignComponent child : item.components)
                if (fullyComponents.contains (child))
                    return true;
        }

        for (ParentItem item : parents) {
            DesignComponent component = document.getComponentByUID (item.componentID);
            if (component == null)
                continue;

            if (item.components == null) {
                DesignComponent parentComponent = component;
                item.components = new ArrayList<DesignComponent> ();
                for (int a = 0; a <= item.levels; a ++) {
                    item.components.add (parentComponent);
                    if (parentComponent == null)
                        break;
                    parentComponent = parentComponent.getParentComponent ();
                }
            }

            for (DesignComponent parent : item.components) {
                if (fullyHierarchies.contains (parent))
                    return true;
                if (! item.hierarchyOnly) {
                    if (fullyComponents.contains (parent))
                        return true;
                }
            }
        }

        return false;
    }

}
