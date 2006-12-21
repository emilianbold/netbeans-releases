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
package org.netbeans.modules.vmd.api.io;

import org.openide.util.WeakSet;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Mode;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.EventListener;
import java.util.Set;

/**
 * This class allows to register a Listener.
 *
 * @author David Kaspar
 */
public final class ActiveViewSupport {

    private static final String MODE_EDITOR = "editor"; // NOI18N

    private static final ActiveViewSupport support = new ActiveViewSupport ();

    private final WeakSet<Listener> listeners = new WeakSet<Listener> ();
    private ActiveViewObserver topComponentListener = null;

    private DataObjectContext context = null;
    private final ActiveDocumentObserver awereness = new ActiveDocumentObserver ();

    private ActiveViewSupport () {
        if (topComponentListener == null)
            TopComponent.getRegistry ().addPropertyChangeListener (topComponentListener = new ActiveViewObserver ());
        addActiveViewListener (new Listener() {
            public void activeViewChanged (DataEditorView deactivatedView, DataEditorView activatedView) {
                System.out.println ("!!!! CheckedActivatedView = " + activatedView);
            }
        });
    }

    /**
     * Returns a default instance of the ActiveViewSupport.
     * @return the default ActiveViewSupport
     */
    public static ActiveViewSupport getDefault () {
        return support;
    }

    /**
     * Registers an listener.
     * @param listener the listener
     */
    public void addActiveViewListener (Listener listener) {
        synchronized (listeners) {
            listeners.add (listener);
        }
    }

    /**
     * Unregisters an listener.
     * @param listener the listener
     */
    public void removeActiveViewListener (Listener listener) {
        synchronized (listeners) {
            listeners.remove (listener);
        }
    }

    /**
     * Returns an active view in whole environment.
     * @return the active view
     */
    public DataEditorView getActiveView () {
        synchronized (listeners) {
            return topComponentListener != null ? topComponentListener.activeView : null;
        }
    }

    private void fireActiveViewChanged (DataEditorView deactivatedView, DataEditorView activatedView) {
        Listener[] activeViewListeners;
        synchronized (listeners) {
            activeViewListeners = listeners.toArray (new Listener[listeners.size ()]);
        }
        for (Listener listener : activeViewListeners) {
            if (listener != null)
                listener.activeViewChanged (deactivatedView, activatedView);
        }

        DataObjectContext newContext = activatedView != null ? activatedView.getContext () : null;
        if (context != newContext) {
            if (context != null)
                context.removeDesignDocumentAwareness (awereness);
            context = newContext;
            if (context != null)
                context.addDesignDocumentAwareness (awereness);
            else
                awereness.setDesignDocument (null);
        }
    }

    private static Mode findEditorMode () {
        Set modes = WindowManager.getDefault ().getModes ();
        for (Object o : modes) {
            Mode m = (Mode) o;
            if (m != null  &&  MODE_EDITOR.equals (m.getName ()))
                return m;
        }
        return null;
    }

    private class ActiveViewObserver implements PropertyChangeListener, LookupListener {

        private Lookup.Result<DataEditorView> result;
        private DataEditorView activeView;

        public void propertyChange (PropertyChangeEvent evt) {
            if (! TopComponent.Registry.PROP_ACTIVATED.equals (evt.getPropertyName ()))
                return;
            Mode m = findEditorMode ();
            TopComponent component = m != null ? m.getSelectedTopComponent () : null;
            if (result != null)
                result.removeLookupListener (this);
            result = component != null ? component.getLookup ().lookupResult (DataEditorView.class) : null;
            if (result != null)
                result.addLookupListener (this);
            update ();
        }

        public void resultChanged (LookupEvent ev) {
            update ();
        }

        private void update () {
            DataEditorView lastView = activeView;
            if (result != null) {
                Collection<? extends DataEditorView> dataEditorViews = result.allInstances ();
                activeView = dataEditorViews.isEmpty () ? null : dataEditorViews.iterator ().next ();
            } else
                activeView = null;
            if (lastView != activeView)
                fireActiveViewChanged (lastView, activeView);

        }

    }

    private class ActiveDocumentObserver implements DesignDocumentAwareness {

        public void setDesignDocument (DesignDocument designDocument) {
            ActiveDocumentSupport.getDefault ().setActiveDocument (designDocument);
        }

    }

    /**
     * Used for notification of an active view in the whole environment.
     * Usually used by active-view-aware components like inspector, palette, ...
     * The listener could be registered using ActiveViewSupport class.
     * @author David Kaspar
     */
    public interface Listener extends EventListener {

        /**
         * Called when a reference to an active view is changed.
         * Called before activeDocumentChanged and activeComponentsChanged methods.
         * @param deactivatedView the deactivated view; null if there was no active view before.
         * @param activatedView   the activated view; null if there is no active view now.
         */
        void activeViewChanged (DataEditorView deactivatedView, DataEditorView activatedView);

    }

}
