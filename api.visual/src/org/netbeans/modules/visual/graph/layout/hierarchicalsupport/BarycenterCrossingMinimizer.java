/*
 * BarycenterCrossingMinimizer.java
 *
 * Created on November 4, 2005, 6:14 PM
 */

package org.netbeans.modules.visual.graph.layout.hierarchicalsupport;


import java.util.List;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.Vertex;

/**
 *
 * @author ptliu
 */
public class BarycenterCrossingMinimizer {
    
    /** Creates a new instance of BarycenterCrossingMinimizer */
    public BarycenterCrossingMinimizer() {
    }
    
    /**
     *
     *
     */
    public LayeredGraph minimizeCrossings(LayeredGraph graph) {
        List<List<Vertex>> layers = graph.getLayers();
        
        if (layers.size() > 1) {
            int maxIteration = 2;
            
            for (int i = 0; i < maxIteration; i++) {
                minimizeCrossingsPhaseI(graph);
            }
            
            minimizeCrossingsPhaseII(graph);
        }
        
        return graph;
    }
  
    /**
     *
     *
     */
    private void minimizeCrossingsPhaseI(LayeredGraph graph) {
        List<List<Vertex>> layers = graph.getLayers();
        int size = layers.size();
        
        // downward phase
        for (int i = 0; i < size-1; i++) {
            float lowerBarycenters[] = graph.computeLowerBarycenters(i);
            List<Vertex> lowerLayer = layers.get(i+1);
            sortVertices(lowerLayer, lowerBarycenters, false);
        }
        
        // upward phase
        for (int i = size-2; i >= 0; i--) {
            float upperBarycenters[] = graph.computeUpperBarycenters(i);
            List<Vertex> upperLayer = layers.get(i);
            sortVertices(upperLayer, upperBarycenters, false);
        }
    }
    
    
    /**
     *
     *
     */
    private void minimizeCrossingsPhaseII(LayeredGraph graph) {
        List<List<Vertex>> layers = graph.getLayers();
        int size = layers.size();
        
        // upward phase
        for (int i = size-2; i >= 0; i--) {
            float upperBarycenters[] = graph.computeUpperBarycenters(i);
            List<Vertex> upperLayer = layers.get(i);
            sortVertices(upperLayer, upperBarycenters, true);
            minimizeCrossingsPhaseI(graph);
        }
        
        // downward phase
        for (int i = 0; i < size-1; i++) {
            float lowerBarycenters[] = graph.computeLowerBarycenters(i);
            List<Vertex> lowerLayer = layers.get(i+1);
            sortVertices(lowerLayer, lowerBarycenters, true);
            minimizeCrossingsPhaseI(graph);
        }
    }
    

    /**
     *
     *
     */
    private boolean sortVertices(List<Vertex> vertices,
            float barycenters[], boolean reverseEqualBarycenters) {
        int size = vertices.size();
        boolean changed = false;
        
        for (int i = 0; i < size-1; i++) {
            for (int j = i+1; j < size; j++) {
                Vertex jv = vertices.get(j);
                Vertex iv = vertices.get(i);
                float jbc = barycenters[j]; 
                float ibc = barycenters[i];
                boolean swap = false;
                
                if (reverseEqualBarycenters) {
                    if (jbc <= ibc)
                        swap = true;
                } else {
                    if (jbc < ibc)
                        swap = true;
                }
                
                if (swap) {
                    vertices.set(j, iv);
                    vertices.set(i, jv);
                    barycenters[j] = ibc;
                    barycenters[i] = jbc;
                    iv.setX(j+1);
                    jv.setX(i+1);
                
                    changed = true;
                }
            }
        }
        
        return changed;
    }
}
