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
package org.netbeans.modules.vmd.api.model;

import org.openide.util.Utilities;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * This class represents a document holding all components.
 * <p>
 * For creating document it is required to define a relation to a project when the document will exists.
 * This is important resolving a repository of project-dependant component descriptors and classpath.
 * <p>
 * The document contains a transaction manager. It allows you to acquire a read or write access to the document.
 * It is because almost all method in the model API is protected and you have to have an appropriate access to call them.
 * The manager uses NetBeans MUTEX class that allows to multi-read or single-write access at the same time.
 * <p>
 * The document does not fire an event everytime you change something in the document and it does not allows you
 * to register a listener on each component directly. Instead there is a listener manager which groups all events
 * from the all parts of document and fires a single big event when a write transaction is finished.
 * The listener manager allows you to register document-related event listeners with a specific event filter.
 * <p>
 * The document contain a tree of component. The tree is specified by a root component. The root component could be set
 * only once.
 *
 * @author David Kaspar
 */
public final class DesignDocument {

    // TODO - how to define: a component is removing?

    // TODO - document reloading using the same instance (all components must be destroyed (how?) - what about a flag indicating that a component is unusable anymore?

    // TODO - selection model - right now a developer has to change the selection manually when a selected component is removed

    // HINT - deserializer must have possibility to specify uid (serialized uid vs. created uid) - could cause problem with code generation

    // TODO - setSelectedComponents - check whether the selection is really changed; if not, do not fire an event

    private final DocumentInterface documentInterface;
    private final DescriptorRegistry descriptorRegistry;
    private final ListenerManager listenerManager;
    private final TransactionManager transactionManager;

    private HashMap<Long, TimedWeakReference> uid2components;
    private DesignComponent rootComponent;
    private long componentIDCounter;

    private String selectionSourceID;
    private Collection<DesignComponent> selectedComponents;

    /**
     * Creates an instance of document.
     * @param documentInterface the documentInterface interface
     */
    public DesignDocument (DocumentInterface documentInterface) {
        this.documentInterface = documentInterface;
        descriptorRegistry = DescriptorRegistry.getDescriptorRegistry (documentInterface.getProjectType (), documentInterface.getProjectID ());
        listenerManager = new ListenerManager (this);
        transactionManager = new TransactionManager (this, descriptorRegistry, listenerManager);

        uid2components = new HashMap<Long, TimedWeakReference> (100);
        componentIDCounter = 0;
        selectedComponents = Collections.emptySet ();

        descriptorRegistry.addRegistryListener (new DescriptorRegistryListener() {
            public void descriptorRegistryUpdated () {
                updateDescriptorReferences ();
            }
        });
    }

    /**
     * Returns document interface of the document.
     * @return the document interface
     */
    // TODO - maybe it has to be replaced by another interface, since DocumentInterface contains more methods that required
    public DocumentInterface getDocumentInterface () {
//        assert Debug.isFriend (DescriptorRegistry.class, "getDescriptorRegistry")  ||  Debug.isFriend (TransactionManager.class, "writeAccessRootEnd"); // NOI18N
        return documentInterface;
    }

    /**
     * Returns a component descriptor registry related to the project when the document is placed.
     * @return the descriptor registry
     */
    public DescriptorRegistry getDescriptorRegistry () {
        return descriptorRegistry;
    }

    /**
     * Returns a listener manager of the document.
     * @return the listener manager
     */
    public ListenerManager getListenerManager () {
        return listenerManager;
    }

    /**
     * Returns a transaction manager of the document.
     * @return the transaction manager
     */
    public TransactionManager getTransactionManager () {
        return transactionManager;
    }

    /**
     * Returns a root component of components tree assigned to the document.
     * @return the root component.
     */
    public DesignComponent getRootComponent () {
        assert getTransactionManager ().isAccess ();
        return rootComponent;
    }

    /**
     * Sets a root component of the document. This method could be called only once.
     * <p>
     * Warning: This method should be used by IO module (where the document is created) only.
     * @param rootComponent the root component
     */
    public void setRootComponent (DesignComponent rootComponent) {
        assert getTransactionManager ().isWriteAccess ();
        assert this.rootComponent == null;
        assert rootComponent != null;
        this.rootComponent = rootComponent;
        getTransactionManager ().rootChangeHappened (rootComponent);
    }

    /**
     * Sets a preferred component ID.
     * <p>
     * Warning: This method is for deserialization purpose only (IO module). Do not use it directly.
     * @param preferredComponentID
     * @return true if preferred component id was used
     */
    public boolean setPreferredComponentID (long preferredComponentID) {
        assert transactionManager.isWriteAccess ();
        if (componentIDCounter > preferredComponentID)
            return false;
        componentIDCounter = preferredComponentID;
        return true;
    }

    /**
     * Creates a new component using a component descriptor with specified typeid.
     * <p>
     * Note: It does not add the component into a tree, you have to do it manually.
     * @param componentType the component typeid
     * @return the new component
     */
    public DesignComponent createComponent (TypeID componentType) {
        DesignComponent component = createRawComponent (componentType);
        assert component != null;
        performComponentPostInit (component, component.getComponentDescriptor ());
        return component;
    }

    private void performComponentPostInit (DesignComponent component, ComponentDescriptor descriptor) {
        if (descriptor == null)
            return;
        performComponentPostInit (component, descriptor.getSuperDescriptor ());
        descriptor.postInitialize (component);
    }

    /**
     * Creates a new raw component using a component descriptor with specified typeid.
     * <p>
     * Note: It does not add the component into a tree, you have to do it manually.
     * <p>
     * Note: it just allocates the component, it does not do any post-initializion.
     * @param componentType the component typeid
     * @return the new raw component
     */
    public DesignComponent createRawComponent (TypeID componentType) {
        assert Debug.isFriend (DesignDocument.class, "createComponent")  ||  Debug.isFriend ("org.netbeans.modules.vmd.io.DocumentLoad", "loadDocumentCore"); // NOI18N
        assert transactionManager.isWriteAccess ();

        ComponentDescriptor componentDescriptor = descriptorRegistry.getComponentDescriptor (componentType);
        assert componentDescriptor != null : "Missing component descriptor for " + componentType;
        assert componentDescriptor.getTypeDescriptor ().isCanInstantiate ();

        DesignComponent component = new DesignComponent (this, componentIDCounter ++, componentDescriptor);

        uid2components.put (component.getComponentID (), new TimedWeakReference (component));
        getListenerManager ().notifyComponentCreated (component);

        return component;
    }

    /**
     * Removes a component from the component tree and removes references to this component from all property values
     * in all components in the document.
     * <p>
     * Note: It does not allows to remove the root component.
     * @param component the component
     */
    public void deleteComponent (DesignComponent component) {
        assert transactionManager.isWriteAccess ();
        assert component != null  &&  component != rootComponent;
        assert component.getDocument () == this;

        Collection<DesignComponent> components = component.getComponents ();
        if (components.size () > 0)
            Debug.warning ("Children has to be deleted before deleting the component", component, components); // NOI18N

        component.removeFromParentComponent ();
        ComponentDescriptor descriptor = component.getComponentDescriptor ();
        if (descriptor != null)
            for (PropertyDescriptor property : descriptor.getPropertyDescriptors ())
                component.resetToDefault (property.getName ());

        if (selectedComponents.contains (component)) {
            HashSet<DesignComponent> selected = new HashSet<DesignComponent> (selectedComponents);
            selected.remove (component);
            setSelectedComponents ("deleteComponent", selected); // NOI18N
        }

        assert ! Debug.isComponentReferencedInRootTree (component) : "Component (" + component + ") is referenced still after deletion";
    }

    /**
     * Returns a component with specified component id.
     * @param componentID the component id
     * @return the component
     */
    public DesignComponent getComponentByUID (long componentID) {
        assert transactionManager.isAccess ();
        TimedWeakReference ref = uid2components.get (componentID);
        return ref != null ? ref.get () : null;
    }

    /**
     * Returns an id of the last source that set the current selection.
     * @return the source id
     */
    public String getSelectionSourceID () {
        assert transactionManager.isAccess ();
        return selectionSourceID;
    }

    /**
     * Returns a set of selected components.
     * @return the set of selected components.
     */
    public Collection<DesignComponent> getSelectedComponents () {
        assert transactionManager.isAccess ();
        return selectedComponents;
    }

    /**
     * Sets selected components. It requires to specify a source id for identifying the source of the selection.
     * <p>
     * Note: Undo/redo of setSelectedComponents changed will use null as a selectionSourceID
     * @param selectionSourceID the source id
     * @param components the set of selected components.
     */
    public void setSelectedComponents (String selectionSourceID, Collection<DesignComponent> components) {
        assert transactionManager.isWriteAccess ();
        assert components != null;
        for (DesignComponent component : components) {
            assert component != null;
            assert component.getDocument () == this;
        }

        if (this.selectedComponents.containsAll (components)  &&  components.containsAll (this.selectedComponents))
            return;

        Collection<DesignComponent> old = this.selectedComponents;
        this.selectionSourceID = selectionSourceID;
        this.selectedComponents = Collections.unmodifiableCollection (new ArrayList<DesignComponent> (components));

        transactionManager.selectComponentsHappened (old, this.selectedComponents);
    }

    private void updateDescriptorReferences () {
        transactionManager.writeAccess (new Runnable () {
            public void run () {
                for (TimedWeakReference reference : uid2components.values ()) {
                    DesignComponent component = reference.get ();
                    if (component == null)
                        continue;
                    ComponentDescriptor descriptor = descriptorRegistry.getComponentDescriptor (component.getType ());
                    component.setComponentDescriptor (descriptor, true);
                }
            }
        });
    }

    private final class TimedWeakReference extends WeakReference<DesignComponent> implements Runnable {

        private final long componentID;

        public TimedWeakReference (DesignComponent referent) {
            super (referent, Utilities.activeReferenceQueue ());
            this.componentID = referent.getComponentID ();
        }

        // HINT - optimalize
        public void run () {
            transactionManager.writeAccess (new Runnable() {
                public void run () {
                    uid2components.remove (componentID);
                }
            });
        }
    }

}
