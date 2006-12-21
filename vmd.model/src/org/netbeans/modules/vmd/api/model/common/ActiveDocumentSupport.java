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
import org.openide.util.WeakSet;

import java.util.Collection;
import java.util.Collections;

/**
 * This class allows to register a listener on active document and components in whole environment.
 * Usually used by active-document-aware components like inspector, palette, ...
 *
 * @author David Kaspar
 */
public final class ActiveDocumentSupport {

    private static final DesignEventFilter FILTER_SELECTION = new DesignEventFilter ().setSelection (true);

    private static final ActiveDocumentSupport support = new ActiveDocumentSupport ();

    private final WeakSet<Listener> listeners = new WeakSet<Listener> ();

    private DesignDocument activeDocument = null;
    private DesignListener selectionListener = null;
    private Collection<DesignComponent> activeComponents = Collections.emptySet (); // WARNING - possible memory leak

    private ActiveDocumentSupport () {
    }

    /**
     * Returns a default instance of the ActiveViewSupport.
     * @return the default ActiveViewSupport
     */
    public static ActiveDocumentSupport getDefault () {
        return support;
    }

    /**
     * Registers an active-document listener.
     * @param listener the listener
     */
    public void addActiveDocumentListener (Listener listener) {
        synchronized (listeners) {
            listeners.add (listener);
        }
    }

    /**
     * Unregisters an active-document listener.
     * @param listener the listener
     */
    public void removeActiveDocumentListener (Listener listener) {
        synchronized (listeners) {
            listeners.remove (listener);
        }
    }

    /**
     * Returns an active document in whole environment.
     * @return the active document
     */
    public DesignDocument getActiveDocument () {
        return activeDocument;
    }

    /**
     * returns active components in whole environment.
     * @return the active components
     */
    public Collection<DesignComponent> getActiveComponents () {
        return activeComponents;
    }

    private void fireActiveDocumentChanged (DesignDocument deactivatedDocument, final DesignDocument activatedDocument) {
        Listener[] Listeners;
        synchronized (listeners) {
            Listeners = listeners.toArray (new Listener[listeners.size ()]);
        }
        for (Listener listener : Listeners) {
            if (listener != null)
                listener.activeDocumentChanged (deactivatedDocument, activatedDocument);
        }

        if (deactivatedDocument != activatedDocument) {
            if (deactivatedDocument != null) {
                if (selectionListener == null)
                    Debug.warning ("SelectionListener does not exist but it should");
                else
                    deactivatedDocument.getListenerManager ().removeDesignListener (selectionListener);
            }
            if (activatedDocument != null) {
                activatedDocument.getTransactionManager ().readAccess (new Runnable () {
                    public void run () {
                        activatedDocument.getListenerManager ().addDesignListener (selectionListener = new SelectionDesignListener (activatedDocument), FILTER_SELECTION);
                        fireActiveComponentsChanged (activatedDocument.getSelectedComponents ());
                    }
                });
            } else {
                fireActiveComponentsChanged (Collections.<DesignComponent>emptyList ());
            }
        }
    }

    private void fireActiveComponentsChanged (Collection<DesignComponent> activeComponents) {
        this.activeComponents = activeComponents;

        Listener[] Listeners;
        synchronized (listeners) {
            Listeners = listeners.toArray (new Listener[listeners.size ()]);
        }
        for (Listener listener : Listeners) {
            if (listener != null)
                listener.activeComponentsChanged (activeComponents);
        }
    }

    /**
     * Sets an active document for whole environment. This method is used from ActiveViewSupport.
     * Do not call it unless you know what it really does.
     * @param designDocument the newly active document
     */
    public void setActiveDocument (DesignDocument designDocument) {
        if (activeDocument == designDocument)
            return;
        DesignDocument lastDocument = activeDocument;
        activeDocument = designDocument;
        fireActiveDocumentChanged (lastDocument, activeDocument);
    }

    private class SelectionDesignListener implements DesignListener {

        private DesignDocument document;

        public SelectionDesignListener (DesignDocument document) {
            this.document = document;
        }

        public void designChanged (DesignEvent event) {
            fireActiveComponentsChanged (document.getSelectedComponents ());
        }
    }

    /**
     * Used for notification of an active document in the whole environment.
     * Usually used by active-document-aware components like inspector, palette, ...
     * The listener could be registered using ActiveDocumentSupport class.
     */
    public interface Listener {

        /**
         * Called when a reference to an active document is changed.
         * Called before activeComponentsChanged method.
         * @param deactivatedDocument the deactivated document; null if there was no active document before.
         * @param activatedDocument   the activated document; null if there is no active document now.
         */
        void activeDocumentChanged (DesignDocument deactivatedDocument, DesignDocument activatedDocument);

        /**
         * Called when a collection of active components is changed.
         * Called after activeDocumentChanged methods.
         * @param activeComponents the collection of active components
         */
        void activeComponentsChanged (Collection<DesignComponent> activeComponents);

    }

}
