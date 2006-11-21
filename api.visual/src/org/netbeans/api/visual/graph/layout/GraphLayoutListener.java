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
