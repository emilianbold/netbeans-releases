/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;

/**
 * This interface controls an in-place editor of an in-place editor action.
 *
 * @author David Kaspar
 */
public interface InplaceEditorProvider<C extends JComponent> {

    /**
     * This is an interface of editor action supplied to the methods in the provider.
     */
    interface EditorController {

        /**
         * Returns whether an in-place editor is visible.
         * @return true, if visible; false, if not visible
         */
        boolean isEditorVisible ();

        /**
         * Opens an in-place editor on a specified widget.
         * @param widget the widget
         * @return true, if the editor is really opened
         */
        boolean openEditor (Widget widget);

        /**
         * Closes the current in-place editor.
         * @param commit whether the current value in the in-place editor is approved by an user
         */
        void closeEditor (boolean commit);

    }

    /**
     * Called to notify about opening an in-place editor.
     * @param controller the editor controller
     * @param widget the widget where the editor is opened
     * @param editor the editor component
     */
    void notifyOpened (EditorController controller, Widget widget, C editor);

    /**
     * Called to notify about closing an in-place editor.
     * @param controller the editor controller
     * @param widget the widget where the editor is opened
     * @param editor the editor component
     * @param discarded true, if the current value is discarded by an user; false if the current value is approved and should be used
     */
    void notifyClosing (EditorController controller, Widget widget, C editor, boolean discarded);

    /**
     * Creates an in-place editor component for a specified widget. Called to acquire the component which should be added into the scene.
     * @param controller the editor controller
     * @param widget the widget where the editor is going to be opened
     * @return the editor component
     */
    C createEditorComponent (EditorController controller, Widget widget);

}
