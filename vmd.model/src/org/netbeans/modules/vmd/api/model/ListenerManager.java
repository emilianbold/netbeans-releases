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

import org.netbeans.modules.vmd.api.model.common.ValidatorPresenter;
import org.openide.ErrorManager;

import java.util.*;

/**
 * This class managers listeners and cares about firing an events.
 * <p>
 * The approach behind the listener manager are group listeners. It is not possible to add a listener directly to a component.
 * It is just possible to attach a listener that will be notified each time when a write transaction is finished and
 * there is a change in the document.
 * <p>
 * It is also possible to filter events that are not important for partical listener by specifying an event filter.
 * A event filter is mutable class, so the filter could be changed during the life-time of the listener. the listener
 * manager is working with the state that the filter was at the time of write transaction is finished.
 *
 * @author David Kaspar
 */
public final class ListenerManager {

    // WARNING - only one filter per a listener

    private static final boolean INVOKE_VALIDATORS = true;

    private volatile long eventID = 0;

    /**
     * Returns a document state. The state is a non-negative number that is increased each time a document is modified.
     * @return the state
     */
    public long getDocumentState () {
        return eventID;
    }

    // HINT - affectedComponentDescriptor event is not fired - replaced by Presenter.notifyAdded, Presenter.notifyRemoved
    // HINT - Does this cover all usages? - UPDATE - it is fired right now

    private static final class PresenterItem {

        private final DesignComponent component;
        private final Collection<? extends Presenter> presentersToRemove;

        PresenterItem (DesignComponent designComponent, Collection<? extends Presenter> presentersToRemove) {
            this.component = designComponent;
            this.presentersToRemove = presentersToRemove;
        }

    }

    private final DesignDocument document;

    private final WeakHashMap<DesignListener, DesignEventFilter> listeners = new WeakHashMap<DesignListener, DesignEventFilter> ();

    private HashSet<DesignComponent> descriptorChangedComponents;
    private ArrayList<PresenterItem> presenterItems;
    private HashSet<DesignComponent> fullyComponents;
    private HashSet<DesignComponent> fullyHierarchies;

    private HashSet<DesignComponent> partlyComponents;
    private HashSet<DesignComponent> partlyHieararchies;

    private boolean selectionChanged;

    private HashMap<DesignComponent, HashMap<String,PropertyValue>> oldPropertyValues;

    private Set<DesignComponent> createdComponents;

    private List<AccessController> controllers;

    private PresenterEventManager presenterEventManager;

    ListenerManager (DesignDocument document) {
        this.document = document;
        clearCaches ();
        controllers = AccessControllerFactoryRegistry.createAccessControllers (document);
        presenterEventManager = new PresenterEventManager ();
    }

    private void clearCaches () {
        // HINT - check capacity statistics
        descriptorChangedComponents = new HashSet<DesignComponent> ();
        presenterItems = new ArrayList<PresenterItem> (100);
        fullyComponents = new HashSet<DesignComponent> (100);
        fullyHierarchies = new HashSet<DesignComponent> (100);
        partlyComponents = new HashSet<DesignComponent> (100);
        partlyHieararchies = new HashSet<DesignComponent> (100);
        oldPropertyValues = new HashMap<DesignComponent, HashMap<String, PropertyValue>> (100);
        createdComponents = new HashSet<DesignComponent> (100);
        selectionChanged = false;
    }

    /**
     * Returns a access controller by a specific controller id
     * @param controllerClass the access controller class
     * @return the access controller
     */
    @SuppressWarnings ("unchecked") // NOI18N
    public <T extends AccessController> T getAccessController (Class<T> controllerClass) {
        if (controllerClass != null)
            for (AccessController controller : controllers)
                if (controllerClass.isInstance (controller))
                    return (T) controller;
        return null;
    }

    /**
     * Adds a design listener with a specified filter.
     * <p>
     * Note: Each listener could be registered only once, otherwise it just reassign its filter to new one.
     * @param listener the listener
     * @param filter the event filter
     */
    public void addDesignListener (DesignListener listener, DesignEventFilter filter) {
        assert listener != null  &&  filter != null;
        listeners.put (listener, filter);
    }

    /**
     * Removes a design listener.
     * @param listener the listener
     */
    public void removeDesignListener (DesignListener listener) {
        assert listener != null;
        listeners.remove (listener);
    }

    void addComponentDescriptorChanged (DesignComponent component, Collection<? extends Presenter> presentersToRemove, Collection<Presenter> presentersToAdd) {
        assert Debug.isFriend (TransactionManager.class, "componentDescriptorChangeHappened"); // NOI18N
        descriptorChangedComponents.add (component);
        if (presentersToAdd != null)
            for (Presenter presenter : presentersToAdd)
                presenter.setNotifyAttached (component);
        presenterItems.add (new PresenterItem (component, presentersToRemove/*, presentersToAdd*/));
    }

    void addAffectedDesignComponent (DesignComponent component, String propertyName, PropertyValue oldPropertyValue) {
        assert Debug.isFriend (TransactionManager.class, "writePropertyHappened"); // NOI18N
        fullyComponents.add (component);
        HashMap<String, PropertyValue> properties = oldPropertyValues.get (component);
        if (properties == null) {
            properties = new HashMap<String, PropertyValue> (100);
            oldPropertyValues.put (component, properties);
        }
        if (! properties.containsKey (propertyName))
            properties.put (propertyName, oldPropertyValue);
    }

    void addAffectedComponentHierarchy (DesignComponent component) {
        assert Debug.isFriend (TransactionManager.class, "rootChangeHappened")  ||  Debug.isFriend (TransactionManager.class, "parentChangeHappened"); // NOI18N
        fullyHierarchies.add (component);
    }

    void notifyComponentCreated (DesignComponent component) {
        assert Debug.isFriend (DesignDocument.class, "createRawComponent"); // NOI18N
        createdComponents.add (component);
    }

    void setSelectionChanged () {
        assert Debug.isFriend (TransactionManager.class, "selectComponentsHappened"); // NOI18N
        selectionChanged = true;
    }

    long getEventID () {
        // TODO - missing Debug.isFriend check
        return eventID;
    }

    DesignEvent fireEvent () {
        assert Debug.isFriend (TransactionManager.class, "writeAccessRootEnd"); // NOI18N

        if (! selectionChanged  &&  descriptorChangedComponents.isEmpty ()  &&  fullyComponents.isEmpty ()  &&  fullyHierarchies.isEmpty ())
            return null;

        for (DesignComponent component : fullyComponents) {
            while (component != null) {
                if (! partlyComponents.add (component))
                    break;
                component = component.getParentComponent ();
            }
        }
        for (DesignComponent component : fullyHierarchies) {
            while (component != null) {
                if (! partlyHieararchies.add (component))
                    break;
                component = component.getParentComponent ();
            }
        }

        Set<DesignComponent> fullyComponentsUm = Collections.unmodifiableSet (fullyComponents);
        Set<DesignComponent> partlyComponentsUm = Collections.unmodifiableSet (partlyComponents);
        Set<DesignComponent> fullyHierarchiesUm = Collections.unmodifiableSet (fullyHierarchies);
        Set<DesignComponent> partlyHierarchiesUm = Collections.unmodifiableSet (partlyHieararchies);
        Set<DesignComponent> descriptorChangedComponentsUm = Collections.unmodifiableSet (descriptorChangedComponents);
        Set<DesignComponent> createdComponentsUm = Collections. unmodifiableSet (createdComponents);

        final DesignEvent event = new DesignEvent (++ eventID, fullyComponentsUm, partlyComponentsUm, fullyHierarchiesUm, partlyHierarchiesUm, descriptorChangedComponentsUm, createdComponentsUm, oldPropertyValues, selectionChanged);

        fireEventInWriteAccess (event, new Runnable () {
            public void run () {
                fireEventCore (event);
            }
        });

        return event;
    }

    private void fireEventInWriteAccess (DesignEvent event, final Runnable runnable) {
        final boolean runStatus[] = new boolean[1];
        final int index[] = new int[1];
        final Runnable exec[] = new Runnable[1];

        runStatus[0] = false;
        index[0] = 0;
        exec[0] = new Runnable() {
            public void run () {
                if (runStatus[0]) {
                    Debug.warning ("AccessController.writeAccess must run the runnable no more than once", controllers.get (index[0] - 1)); // NOI18N
                    return;
                }
                if (index[0] >= controllers.size ()) {
                    runStatus[0] = true;
                    runnable.run ();
                    return;
                }
                AccessController current = controllers.get (index[0]);
                index[0] ++;
                try {
                    current.writeAccess (exec[0]);
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable th) {
                    ErrorManager.getDefault ().notify (th);
                }
                if (! runStatus[0]) {
                    Debug.warning ("AccessController.writeAccess must run the runnable once", current); // NOI18N
                    index[0] ++;
                    run ();
                }
            }
        };

        for (AccessController controller : controllers) {
            try {
                controller.notifyEventFiring (event);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable th) {
                ErrorManager.getDefault ().notify (th);
            }
        }

        exec[0].run ();

        for (AccessController controller : controllers) {
            try {
                controller.notifyEventFired (event);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable th) {
                ErrorManager.getDefault ().notify (th);
            }
        }
    }

    private void fireEventCore (DesignEvent designEvent) {
        for (PresenterItem item : presenterItems) {
            DesignComponent component = item.component;
            if (item.presentersToRemove != null)
                for (Presenter presenter : item.presentersToRemove) {
                    try {
                        presenter.setNotifyDetached (component);
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable th) {
                        ErrorManager.getDefault ().notify (th);
                    }
                }
        }

        Collection<DesignComponent> addedComponentsUm = Collections.unmodifiableCollection (createdComponents);
        for (AccessController controller : controllers) {
            try {
                controller.notifyComponentsCreated (addedComponentsUm);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable th) {
                ErrorManager.getDefault ().notify (th);
            }
        }

        ArrayList<DesignListener> affectedListeners = new ArrayList<DesignListener> ();
        for (Map.Entry<DesignListener, DesignEventFilter> entry : listeners.entrySet ()) {
            DesignListener listener = entry.getKey ();
            if (listener == null)
                continue;
            DesignEventFilter filter = entry.getValue ();
            if (filter.isAffected (document, designEvent))
                affectedListeners.add (listener);
        }

        boolean forcePresenterEventManagerUpdate = ! presenterItems.isEmpty ();
        clearCaches ();

        if (INVOKE_VALIDATORS)
            invokeValidators (designEvent);

        presenterEventManager.prepare (forcePresenterEventManagerUpdate);

        for (DesignListener designListener : affectedListeners) {
            try {
                designListener.designChanged (designEvent);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable th) {
                ErrorManager.getDefault ().notify (th);
            }
        }

        presenterEventManager.execute ();
    }

    private static void invokeValidators (DesignEvent event) {
        HashSet<DesignComponent> validated = new HashSet<DesignComponent> ();

        // TODO - "inside-main-tree" validator test is not covered completely

        invokeValidatorsCore (event.getPartlyAffectedComponents (), validated);
        invokeValidatorsCore (event.getPartlyAffectedHierarchies (), validated);
        invokeValidatorsCore (event.getDescriptorChangedComponents (), validated);
    }

    private static void invokeValidatorsCore (Set<DesignComponent> components, HashSet<DesignComponent> validated) {
        for (DesignComponent component : components) {
            if (validated.contains (component))
                continue;
            ValidatorPresenter presenter = component.getPresenter (ValidatorPresenter.class);
            try {
                if (presenter != null)
                    presenter.checkValidity ();
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
            }
            validated.add (component);
        }
    }

    /**
     * Adds a presenter listener on a presenter of a component.
     * @param component the component
     * @param presenterClass the presenter class
     * @param listener the listener
     */
    public void addPresenterListener (DesignComponent component, Class<? extends Presenter> presenterClass, PresenterListener listener) {
        presenterEventManager.addPresenterListener (component, presenterClass, listener);
    }

    /**
     * Removes a presenter listener on a presenter of a component.
     * @param component the component
     * @param presenterClass the presenter class
     * @param listener the listener
     */
    public void removePresenterListener (DesignComponent component, Class<? extends Presenter> presenterClass, PresenterListener listener) {
        presenterEventManager.removePresenterListener (component, presenterClass, listener);
    }

    void firePresenterChanged (DynamicPresenter presenter) {
        assert Debug.isFriend (DynamicPresenter.class, "firePresenterChanged"); // NOI18N
        presenterEventManager.firePresenterChanged (presenter.getPresenterListener ());
    }

}
