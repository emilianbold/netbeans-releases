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
package org.netbeans.api.visual.graph.layout;

import java.awt.*;

/**
 * @author David Kaspar
 */
public interface GraphLayoutListener<N,E> {

    /**
     * Called when a graph layout is started.
     * @param graph the universal graph
     */
    void graphLayoutStarted (UniversalGraph<N, E> graph);

    /**
     * Called when a graph layout is finished.
     * @param graph the universal graph
     */
    void graphLayoutFinished (UniversalGraph<N, E> graph);

    /**
     * Called when a graph layout resolves a new location for a node.
     * @param graph the universal graph
     * @param node the node with changed preferred location
     * @param previousPreferredLocation the previous preferred location
     * @param newPreferredLocation the new preferred location
     */
    void nodeLocationChanged (UniversalGraph<N,E> graph, N node, Point previousPreferredLocation, Point newPreferredLocation);

}
