/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport;

import org.openide.nodes.Node;

/**
 * This interface represents one layout constraints object describing position
 * of a component in visual container layout. This interface is the second part
 * of the layout support extensions - alongside LayoutSupportDelegate, which
 * takes care about container layout as a whole.
 *
 * @see LayoutSupportDelegate
 *
 * @author Tomas Pavek
 */

public interface LayoutConstraints {

    /** Gets the properties of these component layout constraints to be
     * presented in Component Inspector for the component.
     * @return properties of these constraints
     */
    Node.Property[] getProperties();

    /** Gets the real (reference) constraints object behind this metaobject.
     * This object is used as the constraints parameter when adding a component
     * to container.
     * @return the real constraints object
     */
    Object getConstraintsObject();

    /** Cloning method - creates a copy of the constraints. It should clone
     * the reference object inside.
     * @return cloned LayoutConstraints
     */
    LayoutConstraints cloneConstraints();
}
