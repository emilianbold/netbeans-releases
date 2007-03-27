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

package org.netbeans.modules.vmd.api.screen.display;

import javax.swing.*;

/**
 * @author David Kaspar
 */
public interface ScreenPropertyEditor {

    /**
     * It should create an editor component which will be placed into the screen designer.
     * Called in AWT with model read access.
     * @param controller the controller interface used e.g. for closing editor
     * @param descriptor the currently edited screen property descriptor
     * @return the editor component; if null, openNotify and closeNotify are not called
     */
    public JComponent createEditorComponent (Controller controller, ScreenPropertyDescriptor descriptor);

    /**
     * Loads the editor component with initial data.
     * Called in AWT with model read access.
     * @param controller the controller
     * @param descriptor the currently edited screen property descriptor
     * @param editorComponent the editor component created by createEditorComponent method of this instance
     */
    public void openNotify (Controller controller, ScreenPropertyDescriptor descriptor, JComponent editorComponent);

    /**
     * Commits the data from the editor component to the model.
     * Called in AWT with model write access.
     * @param controller the controller
     * @param descriptor the currently edited screen property descriptor
     * @param editorComponent the editor component created by createEditorComponent method of this instance
     * @param commit if true, then commit the data into the model; if false, do not commit anything
     */
    public void closeNotify (Controller controller, ScreenPropertyDescriptor descriptor, JComponent editorComponent, boolean commit);

    public interface Controller {

        public void closeEditor (boolean commitApproved);

    }

}
