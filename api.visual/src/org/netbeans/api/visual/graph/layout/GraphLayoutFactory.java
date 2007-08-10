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
package org.netbeans.api.visual.graph.layout;

import org.netbeans.modules.visual.graph.layout.TreeGraphLayout;

/**
 * The factory class of all built-in GraphLayout based implementations.
 * 
 * @author David Kaspar
 * @since 2.4
 */
public class GraphLayoutFactory {

    /**
     * Creates a tree graph layout.
     * Use GraphLayoutSupport.setTreeGraphLayoutRootNode method to set the root node of the graph.
     * If not set/found, then layout is not executed.
     * Note: Use GraphLayoutSupport.setTreeGraphLayoutProperties method to set the parameters of the layout later.
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @return the tree graph layout
     * @since 2.4
     */
    public static <N,E> GraphLayout<N,E> createTreeGraphLayout (int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        return new TreeGraphLayout<N,E> (originX, originY, verticalGap, horizontalGap, vertical);
    }

}
