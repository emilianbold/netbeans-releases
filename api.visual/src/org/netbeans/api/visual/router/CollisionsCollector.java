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
package org.netbeans.api.visual.router;

import java.awt.*;
import java.util.List;

/**
 * This class is used for collecting collision regions. There are two separate types of regions - vertical and horizontal.
 *
 * @author David Kaspar
 */
public interface CollisionsCollector {

    /**
     * Gathers collision collections and fill up the lists of vertical and horizontal collisions.
     * @param verticalCollisions the list of vertical collisions
     * @param horizontalCollisions the list of horizontal collisions
     */
    public void collectCollisions (List<Rectangle> verticalCollisions, List<Rectangle> horizontalCollisions);

}
