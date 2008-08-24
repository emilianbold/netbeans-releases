/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
     * Returns an active document in whole environment. Insted of this method 
     * try to get real references of your document throgh DesignComponent.
     * 
     * @return the active document
     */
    @Deprecated
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
                    Debug.warning ("SelectionListener does not exist but it should"); // NOI18N
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
    
    private class DocumentObserver implements ActiveDocumentSupport.Listener {

        public void activeDocumentChanged(DesignDocument deactivatedDocument, DesignDocument activatedDocument) {
            
        }

        public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
            
        }
        
    }

}
