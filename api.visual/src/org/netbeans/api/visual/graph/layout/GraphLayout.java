package org.netbeans.api.visual.graph.layout;

import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.GraphScene;

import java.util.Collection;

/**
 * This class represents a graph-orinted layout.
 * The layout is invoked using layoutGraph methods.
 * The layoutNodes method could be called for resolving locations of a sub-set of all nodes.
 * <p>
 * Internally the invoked method creates an UniversalGraph from the scene in the arguments.
 * Then it calls the particular (performGraphLayout or performNodesLayout) methods with the UniversalGraph as a parameter.
 * These protected methods are implemented by the GraphLayout based class and performs particular layout using an UniversalGraph.
 *
 * @author David Kaspar
 */
public abstract class GraphLayout<N,E> {

    /**
     * Invokes graph-oriented layout on a GraphScene.
     * @param graphScene the graph scene
     */
    public final <N,E> void layoutGraph (GraphScene<N,E> graphScene) {
        performGraphLayout (UniversalGraph.createUniversalGraph (graphScene));
    }

    /**
     * Invokes graph-oriented layout on a GraphPinScene.
     * @param graphPinScene the graph pin scene
     */
    public final <N,E> void layoutGraph (GraphPinScene<N,E,?> graphPinScene) {
        performGraphLayout (UniversalGraph.createUniversalGraph (graphPinScene));
    }

    /**
     * Invokes resolving of locations for a collection of nodes in a GraphScene.
     * @param graphScene the graph scene
     * @param nodes the collection of nodes to resolve
     */
    public final <N,E> void layoutNodes (GraphScene<N,E> graphScene, Collection<N> nodes) {
        performNodesLayout (UniversalGraph.createUniversalGraph (graphScene), nodes);
    }

    /**
     * Invokes resolving of locations for a collection of nodes in a GraphPinScene.
     * @param graphPinScene the graph pin scene
     * @param nodes the collection of nodes to resolve
     */
    public final <N,E> void layoutNodes (GraphPinScene<N,?,E> graphPinScene, Collection<N> nodes) {
        performNodesLayout (UniversalGraph.createUniversalGraph (graphPinScene), nodes);
    }

    /**
     * Implements and performs particular graph-oriented algorithm of a UniversalGraph.
     * @param graph the universal graph on which the layout should be performed
     */
    protected abstract <N,E> void performGraphLayout (UniversalGraph<N,E> graph);

    /**
     * Implements and performs particular location resolution of a collection of nodes in a UniversalGraph.
     * @param graph the universal graph on which the nodes should be resolved
     * @param nodes the collection of nodes to be resolved
     */
    protected abstract <N,E> void performNodesLayout (UniversalGraph<N,E> graph, Collection<N> nodes);

}
