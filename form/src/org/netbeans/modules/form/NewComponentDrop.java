/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import org.openide.nodes.Node;

import org.netbeans.modules.form.palette.PaletteItem;

/**
 * Cookie allowing drag and drop of nodes to form module.
 *
 * @author Jan Stola, Tomas Pavek
 */
public interface NewComponentDrop extends Node.Cookie {

    /**
     * Describes the primary component that should be added.
     */
    PaletteItem getPaletteItem();

    /**
     * Callback method that notifies about the added component. You should
     * set properties of the added component or add other beans to the model
     * in this method.
     *
     * @param model model of the form.
     * @param componentId ID of the newly added component.
     */
    void componentAdded(FormModel model, String componentId);

}
