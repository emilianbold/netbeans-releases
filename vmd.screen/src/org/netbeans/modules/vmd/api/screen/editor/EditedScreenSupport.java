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
 *
 */

package org.netbeans.modules.vmd.api.screen.editor;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * @author David Kaspar
 */
public final class EditedScreenSupport {
    
    private static final WeakHashMap<DesignDocument,EditedScreenSupport> instances = new WeakHashMap<DesignDocument, EditedScreenSupport> ();
    
    private long editedScreenComponentID = -1;
    private final ArrayList<Listener> listeners = new ArrayList<EditedScreenSupport.Listener> ();
    
    private EditedScreenSupport () {
    }
    
    public static synchronized EditedScreenSupport getSupportForDocument (DesignDocument document) {
        EditedScreenSupport support = instances.get(document);
        if (support == null)
            instances.put(document, support = new EditedScreenSupport ());
        return support;
    }
    
    public void addListener (Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeListener (Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    public long getEditedScreenComponentID () {
        return editedScreenComponentID;
    }
    
    /**
     * This method has to be called from AWT thread and under read or write access on the document.
     * This method must not be called within EditedScreenSupport.Listener.editedScreenChanged method.
     * Also be aware of setting correct component id.
     * @param editedScreenComponentID the edited screen component id
     */
    public void setEditedScreenComponentID (long editedScreenComponentID) {
        this.editedScreenComponentID = editedScreenComponentID;
        Listener[] listenersArray;
        synchronized (listeners) {
            listenersArray = listeners.toArray(new Listener[listeners.size()]);
        }
        for (EditedScreenSupport.Listener listener : listenersArray)
            listener.editedScreenChanged(editedScreenComponentID);
    }

    /**
     * Return all editable screens in the document.
     * Call this method within the read or write access only.
     * @param document the design document
     * @return the list of all editable screens in the document.
     */
    public static List<DesignComponent> getAllEditableScreensInDocument(DesignDocument document) {
        assert document.getTransactionManager().isAccess();
        ArrayList<DesignComponent> screens = new ArrayList<DesignComponent> ();
        for (DesignComponent component : DocumentSupport.gatherAllComponentsContainingPresenterClass(document, ScreenDisplayPresenter.class)) {
            if (component.getPresenter(ScreenDisplayPresenter.class).isTopLevelDisplay())
                screens.add(component);
        }
        return screens;
    }

    public interface Listener {
        
        /**
         * This method is called within the AWT thread and under read or write access on the document.
         * The EditedScreenSupport.setEditedScreenComponentID method must not be called within this method.
         * @param editedScreenComponentID the edited screen component id
         */
        void editedScreenChanged (long editedScreenComponentID);
        
    }
    
}
