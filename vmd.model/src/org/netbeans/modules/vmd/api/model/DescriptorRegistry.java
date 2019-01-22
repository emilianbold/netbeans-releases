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

import org.openide.util.Mutex;
import org.openide.util.WeakSet;
import org.openide.util.Exceptions;

import java.util.*;

/**
 * This class represents a registry for all component descriptors that could be used in the document. The registry consists of
 * two parts: global registry, project-dependent registry.
 * <p>
 * For registering a component descriptor in global registry you have to put its instance to "vmd/components" folder in layer filesystem.
 * These descriptors will be available for all documents in all projects.
 * <p>
 * For registering a component descriptor in project-dependent registry it is fully up-to the project interface what directory
 * it supply as a source of descriptors (usually it will be nbproject/private/components folder). These component descriptors
 * will be available only for documents that are located in the project.
 * <p>
 * Each component descriptor is registered with the type id that is resolved by
 * ComponentDescriptor.getTypeDescriptor ().getThisType () method call.
 * This type id must be unique across all component descriptors. If there is a conflict of type ids,
 * the one from global registry wins. Otherwise there is no stable rule or priority defined.
 * <p>
 * When a registry is update/refreshed/changed all its document (that are using it) resolves a new component descriptors
 * for all their components. If a component descriptor could not be resolved for some component that the component we still
 * contain all property values and presenters but they will not be accessible/modifiable.
 *
 * 
 */
// TODO - unique producers by their ids
public final class DescriptorRegistry {

    // TODO - where to store, project-dependent descriptor registry - layer/vmd/projects/project_id/components folder?

//    private String projectType;
//    private String projectID;

    private final Mutex mutex = new Mutex ();
    private final WeakSet<DescriptorRegistryListener> listeners = new WeakSet<DescriptorRegistryListener> ();
    private HashMap<TypeID, ComponentDescriptor> descriptors = new HashMap<TypeID, ComponentDescriptor> ();
    private ArrayList<ComponentProducer> producers = new ArrayList<ComponentProducer> ();
    private GlobalDescriptorRegistry globalDescriptorRegistry;
    private DescriptorRegistryListener listener = new DescriptorRegistryListener() {
        public void descriptorRegistryUpdated () {
            reload ();
        }
    };

    /**
     * Returns a descriptor registry for specific project type and project id
     * @param projectType the project type
     * @param projectID the project id
     * @return the descriptor registry
     */
    public static DescriptorRegistry getDescriptorRegistry (String projectType, String projectID) {
        return GlobalDescriptorRegistry.getGlobalDescriptorRegistry (projectType).getProjectRegistry (projectID);
    }

    DescriptorRegistry (GlobalDescriptorRegistry globalDescriptorRegistry) { //, String projectType, String projectID) {
        assert Debug.isFriend (GlobalDescriptorRegistry.class, "getProjectRegistry"); // NOI18N

//        this.projectType = projectType;
//        this.projectID = projectID;
        this.globalDescriptorRegistry = globalDescriptorRegistry; // GlobalDescriptorRegistry.getGlobalDescriptorRegistry (projectType);
        globalDescriptorRegistry.addRegistryListener (listener);
        reload ();
    }

    private boolean isAccess () {
        return mutex.isReadAccess () || mutex.isWriteAccess ();
    }

    /**
     * Executes a Runnable.run method with read access.
     * @param runnable the runnable
     */
    public void readAccess (final Runnable runnable) {
        globalDescriptorRegistry.readAccess (new Runnable () {
            public void run () {
                mutex.readAccess (runnable);
            }
        });
    }

    private void writeAccess (final Runnable runnable) {
        globalDescriptorRegistry.readAccess (new Runnable() {
            public void run () {
                mutex.writeAccess (runnable);
            }
        });
    }

    private void reload () {
        writeAccess (new Runnable() {
            public void run () {
                reloadCore ();
            }
        });
    }

    private void reloadCore () {
        // TODO - reload from project descriptor registry

        HashMap<TypeID, ComponentDescriptor> tempDescriptors = new HashMap<TypeID, ComponentDescriptor> ();
        ArrayList<ComponentProducer> tempProducers = new ArrayList<ComponentProducer> ();

        Collection<ComponentDescriptor> _descriptors = globalDescriptorRegistry.getComponentDescriptors ();
        for (ComponentDescriptor descriptor : _descriptors)
            tempDescriptors.put (descriptor.getTypeDescriptor ().getThisType (), descriptor);

        tempProducers.addAll (globalDescriptorRegistry.getComponentProducers ());

        this.descriptors = tempDescriptors;
        this.producers = tempProducers;

        for (DescriptorRegistryListener _listener : listeners) {
            try {
                _listener.descriptorRegistryUpdated ();
            } catch (Exception e) {
                Exceptions.printStackTrace (e);
            }
        }
    }

//    /**
//     * Checks whether all specified component type ids are available in the descriptor registry. If some is missing there,
//     * it tries to resolve it from the sources.
//     * @param componentTypes the collection of component type ids that has to be checked
//     */
//    public void assertComponentDescriptors (Collection<TypeID> componentTypes) {
//        writeAccess (new Runnable () {
//            public void run () {
//                // TODO
//            }
//        });
//    }

    /**
     * Returns a component descriptor for a specified type id.
     * @param componentType the component type id
     * @return the component descriptor
     */
    public ComponentDescriptor getComponentDescriptor (TypeID componentType) {
        assert isAccess () : "No Mutex acess"; //NOI18N
        if (componentType == null)
            return null;
        return descriptors.get (componentType);
    }

    public void removeComponentDescriptor(final TypeID componentType) {
        if (componentType != null)
            globalDescriptorRegistry.writeAccess(new RemoveComponentDescriptorTask(componentType));
    }

    // TODO - proxy for GlobalDescriptorRegistry and ProjectRegistry
    // HINT - always up-to-date when any method is called (synchronize/refresh before working)

    /**
     * Returns a collection of all registered component descriptors.
     * @return the collection of all registered component descriptors
     */
    public Collection<ComponentDescriptor> getComponentDescriptors () {
        assert isAccess ();
        return Collections.unmodifiableCollection (descriptors.values ());
    }

    /**
     * Returns a list of all registered component producers.
     * @return the list of all registered component producers
     */
    public List<ComponentProducer> getComponentProducers () {
        assert isAccess ();
        return Collections.unmodifiableList (producers);
    }

    /**
     * Adds a registry listener.
     * The DescriptorRegistryListener.descriptorRegistryUpdated method is called everytime the content of registry is changed.
     * @param listener the listener
     */
    public void addRegistryListener (final DescriptorRegistryListener listener) {
        writeAccess (new Runnable() {
            public void run () {
                listeners.add (listener);
                listener.descriptorRegistryUpdated ();
            }
        });
    }

    /**
     * Removes a registry listener.
     * @param listener the listener
     */
    public void removeRegistryListener (final DescriptorRegistryListener listener) {
        writeAccess (new Runnable() {
            public void run () {
                listeners.remove (listener);
            }
        });
    }

    /**
     * Checks whether a component descriptor is compatible (descriptor analogy of instanceof operator) with a type id.
     * Means: Specified component descriptor or its super descriptor has the same type id as the specified one.
     * @param typeID the type id
     * @param componentDescriptor the component descriptor
     * @return true if compatible
     */
    private boolean isComponentDescriptorCompatibleWithTypeID (TypeID typeID, ComponentDescriptor componentDescriptor) {
        assert isAccess ();
        if (typeID == null)
            return false;
        for (;;) {
            if (componentDescriptor == null)
                return false;
            TypeDescriptor typeDescriptor = componentDescriptor.getTypeDescriptor ();
            TypeID checked = typeDescriptor.getThisType ();
            if (checked == null)
                return false;
            if (checked.equals (typeID))
                return true;
            componentDescriptor = getComponentDescriptor (typeDescriptor.getSuperType ());
        }
    }

    /**
     * Checks whether specified superTypeID is super type id of specified derivedTypeID
     * @param superTypeID the super type id
     * @param derivedTypeID the possible derived typeid
     * @return true if the superTypeID is one of super type ids of the derivedTypeID
     */
    public boolean isInHierarchy (TypeID superTypeID, TypeID derivedTypeID) {
        return isComponentDescriptorCompatibleWithTypeID (superTypeID, getComponentDescriptor (derivedTypeID));
    }
    
    final class RemoveComponentDescriptorTask implements Runnable {

        private TypeID componentType;

        public RemoveComponentDescriptorTask(TypeID componentType) {
            this.componentType = componentType;
        }

        public void run() {
            globalDescriptorRegistry.removeComponentDescriptor(componentType);
        }
        
    }

}
