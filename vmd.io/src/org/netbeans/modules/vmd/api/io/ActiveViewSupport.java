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
package org.netbeans.modules.vmd.api.io;

import org.openide.util.*;
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
//            System.out.println ("!! Global Context Lookup Result = " + Utilities.actionsGlobalContext ().lookupAll (Object.class));
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
