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

package org.netbeans.modules.vmd.api.inspector;

import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 *
 * @author Karol Harezlak
 */

/**
 * This class controls position of particular folder in the tree structure of
 * Mobility Visual Designer Navigator. It is possible to have more that one
 * InspectorPositionController for one InspectorFolder. If there is more
 * that one InspectorPositionControllers it simply means that the same folder will be
 * visible more that ones in the tree structure of the Visual Designer Navigator according to
 * position provided by the InspectorPositionControllers.
 */
public interface InspectorPositionController {

    /**
     * This method checks if InspectorFolder connected with this position controller
     * belongs to the particular place inside of the Mobility Visual Designer Navigator tree structure.
     * To check if this folder "isInside" particular place there is parameters like
     * current navigator path, folder and component.
     * @param path current navigator tree path
     * @param folder current inspector folder
     * @param component current component
     * @return Boolean.TRUE belongs to the particular place in the navigator structure,
     * Boolean.FALS doesn't  belong to the particular place in the navigator structure
     */
    boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component);
}