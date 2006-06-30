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
