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
