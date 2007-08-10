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
 * @author David Kaspar
 * @since 2.4
 */
public class GraphLayoutSupport {

    /**
     * Sets a root node to a tree graph layout.
     * @param graph the tree graph layout
     * @param rootNode the root node
     * @since 2.4
     */
    public static <N,E> void setTreeGraphLayoutRootNode (GraphLayout<N,E> graph, N rootNode) {
        if (graph instanceof TreeGraphLayout)
            ((TreeGraphLayout<N,E>) graph).setRootNode (rootNode);
    }

    /**
     * Sets properties to a tree graph layout.
     * @param graph the tree graph layout
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @since 2.7
     */
    public static <N,E> void setTreeGraphLayoutProperties (GraphLayout<N,E> graph, int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        if (graph instanceof TreeGraphLayout)
            ((TreeGraphLayout<N,E>) graph).setProperties (originX, originY, verticalGap, horizontalGap, vertical);
    }

}
