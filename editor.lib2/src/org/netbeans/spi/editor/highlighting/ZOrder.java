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

package org.netbeans.spi.editor.highlighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 * This class determines a position of a <code>HighlightsLayer</code> in relation
 * to other layers. Instances of this class are immutable.
 *
 * <p>For the purpose of vertical ordering each <code>HighlightsLayer</code> is
 * identified by its <i>layer type id</i>. When creating a new
 * <code>ZOrder</code> you simply specify that the new <code>ZOrder</code> lies
 * above and/or below certain layers using their IDs.
 *
 * <p>Developers are encouraged to use the predefined z-order constants in this
 * class. Each constant refers to a rack in vertical ordering with a specific
 * purpose. The racks are ordered in the following way:
 *
 * <ul>
 * <li>TOP_RACK - the highest rack
 * <li>SHOW_OFF_RACK
 * <li>DEFAULT_RACK
 * <li>CARET_RACK
 * <li>SYNTAX_RACK
 * <li>BOTTOM_RACK - the lowest rack
 * </ul>
 *
 * <div class="nonnormative">
 * <p>When positioning your layer you should choose the rack that suites the purpose
 * of your layer. It is possible to further define more precise ordering
 * between layers within a rack. For example, if you have two layers - one providing
 * syntactical highlighting and the other one providing semantical highlighting -
 * they both belong to the <code>SYNTAX_RACK</code>, but the semantical layer should be placed
 * above the syntactical one, because it provides 'more accurate' highlights. You could
 * define <code>ZOrder</code> of your layers like this:
 *
 * <pre>
 * ZOrder syntaxLayerZOrder = ZOrder.SYNTAX;
 * ZOrder semanticLayerZOrder = ZOrder.SYNTAX.aboveLayers("your-syntax-layer-id");
 * </pre>
 * </div>
 *
 * @author Vita Stejskal
 */
public final class ZOrder {

    private static final Logger LOG = Logger.getLogger(ZOrder.class.getName());

    private static final Collection<String> EMPTY = new HashSet<String>();
    
    private static final String TOP_RACK_MARKER = "org-netbeans-spi-editor-highlighting-ZOrder-TOP-RACK"; //NOI18N
    private static final String SHOW_OFF_RACK_MARKER = "org-netbeans-spi-editor-highlighting-ZOrder-SHOW-OFF-RACK"; //NOI18N
    private static final String DEFAULT_RACK_MARKER = "org-netbeans-spi-editor-highlighting-ZOrder-DEFAULT-RACK"; //NOI18N
    private static final String CARET_RACK_MARKER = "org-netbeans-spi-editor-highlighting-ZOrder-CARET-RACK"; //NOI18N
    private static final String SYNTAX_RACK_MARKER = "org-netbeans-spi-editor-highlighting-ZOrder-SYNTAX-RACK"; //NOI18N
    
    /**
     * The highest rack of z-orders. Layers in this rack will be placed at
     * the top of the hierarchy.
     */
    public static final ZOrder TOP_RACK = ZOrder.above(TOP_RACK_MARKER);

    /**
     * The show off rack of z-orders. This rack is meant to be used by
     * layers with short-lived highlights that can temporarily override highlights
     * provided by other layers (eg. syntax coloring).
     */
    public static final ZOrder SHOW_OFF_RACK = new ZOrder(
        Collections.singleton(TOP_RACK_MARKER), Collections.singleton(SHOW_OFF_RACK_MARKER));
    
    /**
     * The default rack of z-orders. This rack should be used by most of the layers.
     */
    public static final ZOrder DEFAULT_RACK = new ZOrder(
        Collections.singleton(SHOW_OFF_RACK_MARKER), Collections.singleton(DEFAULT_RACK_MARKER));
    
    public static final ZOrder CARET_RACK = new ZOrder(
        Collections.singleton(DEFAULT_RACK_MARKER), Collections.singleton(CARET_RACK_MARKER));
    
    /**
     * The syntax highlighting rack of z-order. This rack is meant to be used by
     * layers that provide highlighting of a text according to its syntactical or
     * semantical rules.
     */
    public static final ZOrder SYNTAX_RACK = new ZOrder(
        Collections.singleton(CARET_RACK_MARKER), Collections.singleton(SYNTAX_RACK_MARKER));
    
    /**
     * The lowest rack of z-orders. Layers in this rack will be placed at the
     * bottom of the hierarchy.
     */
    public static final ZOrder BOTTOM_RACK = ZOrder.below(SYNTAX_RACK_MARKER);
    
    /**
     * Creates a z-order that determines a position above the layers passed in.
     *
     * @param layerIds    The IDs of layers which lay below the position that
     *                    will be refered to by a z-order created by this
     *                    method.
     *
     * @return The new z-order.
     */
    public static ZOrder above(String... layerIds) {
        return new ZOrder(null, Arrays.asList(layerIds));
    }
    
    /**
     * Creates a z-order that determines a position below the layers passed in.
     *
     * @param layerIds    The IDs of layers which lay above the position that
     *                    will be refered to by a z-order created by this
     *                    method.
     *
     * @return The new z-order.
     */
    public static ZOrder below(String... layerIds) {
        return new ZOrder(Arrays.asList(layerIds), null);
    }

    /**
     * Sorts an array of <code>HighlightLayer</code>s by their z-order. This is
     * a convenience method that delegates to the <code>sort(Collection)</code>
     * method.
     *
     * @param layers    The array to sort.
     * 
     * @return The sorted array where layers are sorted by their z-order starting
     * with the lowest z-order and going to the highest one.
     * @throws TopologicalSortException If the array contains cycles.
     */
    /* package */ static HighlightsLayer[] sort(HighlightsLayer[] layers) throws TopologicalSortException {
        List<? extends HighlightsLayer> list = sort(Arrays.asList(layers));
        return list.toArray(new HighlightsLayer [list.size()]);
    }
    
    /**
     * Sorts a collection of <code>HighlightLayer</code>s by their z-order. The layers
     * with <code>ZOrder.BOTTOM</code> will preceed all other layers in the resulting
     * list. Similarily the layers with <code>ZOrder.TOP</code> will be placed at the
     * end of the list. All the other layers will appear in between sorted by their
     * z-order.
     *
     * @param layers    The array to sort.
     *
     * @return The sorted array where layers are sorted by their z-order starting
     * with the lowest z-order and going to the highest one.
     * @throws TopologicalSortException If the collection contains cycles.
     */
    /* package */ static List<? extends HighlightsLayer> sort(Collection<? extends HighlightsLayer> layers) throws TopologicalSortException {
        HashMap<String, HighlightsLayer> id2layer = new HashMap<String, HighlightsLayer>();
        HashSet<String> vertices = new HashSet<String>();
        HashMap<String, List<String>> edges = new HashMap<String, List<String>>();
        
        vertices.add(TOP_RACK_MARKER);
        vertices.add(SHOW_OFF_RACK_MARKER);
        vertices.add(DEFAULT_RACK_MARKER);
        vertices.add(CARET_RACK_MARKER);
        vertices.add(SYNTAX_RACK_MARKER);

        edges.put(SYNTAX_RACK_MARKER, new ArrayList<String>(Collections.singleton(CARET_RACK_MARKER)));
        edges.put(CARET_RACK_MARKER, new ArrayList<String>(Collections.singleton(DEFAULT_RACK_MARKER)));
        edges.put(DEFAULT_RACK_MARKER, new ArrayList<String>(Collections.singleton(SHOW_OFF_RACK_MARKER)));
        edges.put(SHOW_OFF_RACK_MARKER, new ArrayList<String>(Collections.singleton(TOP_RACK_MARKER)));
        
        for (HighlightsLayer layer : layers) {
            id2layer.put(layer.getLayerTypeId(), layer);

            // process the layers below the current layer
            for (String belowLayerId : layer.getZOrder().layersBelow) {
                vertices.add(belowLayerId);
                List<String> verticeEdges = edges.get(belowLayerId);
                if (verticeEdges == null) {
                    verticeEdges = new ArrayList<String>();
                    edges.put(belowLayerId, verticeEdges);
                }
                verticeEdges.add(layer.getLayerTypeId());
                LOG.finest(belowLayerId + " < " + layer.getLayerTypeId());
            }

            // process the layers above the current layer
            vertices.add(layer.getLayerTypeId());
            List<String> verticeEdges = edges.get(layer.getLayerTypeId());
            if (verticeEdges == null) {
                verticeEdges = new ArrayList<String>();
                edges.put(layer.getLayerTypeId(), verticeEdges);
            }
            for (String aboveLayerId : layer.getZOrder().layersAbove) {
                verticeEdges.add(aboveLayerId);
                LOG.finest(layer.getLayerTypeId() + " < " + aboveLayerId);
            }
        }
        
        // Sort ordinary layers by their z-order
        List<String> sortedLayerIds = Utilities.topologicalSort(vertices, edges);
        List<HighlightsLayer> sortedLayers = new ArrayList<HighlightsLayer>();
        
        // Add all ordinary layers sorted by their z-order
        LOG.finest("Sorted layer Ids: ");
        for (String layerId : sortedLayerIds) {
            LOG.finest("    " + layerId);
            HighlightsLayer layer = id2layer.get(layerId);
            if (layer != null) {
                sortedLayers.add(layer);
            }
        }
        LOG.finest("End of Sorted layer Ids: -----------------------");
        
        return sortedLayers;
    }
    
    /* package */ final Collection<String> layersAbove;
    /* package */ final Collection<String> layersBelow;
    
    /** Creates a new instance of ZOrder */
    private ZOrder(Collection<String> aboveLayers, Collection<String> belowLayers) {
        this.layersAbove = aboveLayers == null ? EMPTY : aboveLayers;
        this.layersBelow = belowLayers == null ? EMPTY : belowLayers;
    }

    /**
     * Creates a copy of this ZOrder and modifies its position to lay above the
     * layers passed in.
     *
     * @param layerIds    The IDs of layers which lay below the position that
     *                    will be refered to by a z-order created by this
     *                    method.
     *
     * @return The new z-order.
     */
    public ZOrder aboveLayers(String... layerIds) {
        HashSet<String> newLayersAbove = new HashSet<String>(layersAbove);
        HashSet<String> newLayersBelow = new HashSet<String>(layersBelow.size() + layerIds.length);
        newLayersBelow.addAll(layersBelow);
        newLayersBelow.addAll(Arrays.asList(layerIds));
        return new ZOrder(newLayersAbove, newLayersBelow);
    }
    
    /**
     * Creates a copy of this ZOrder and modifies its position to lay below the
     * layers passed in.
     *
     * @param layerIds    The IDs of layers which lay above the position that
     *                    will be refered to by a z-order created by this
     *                    method.
     *
     * @return The new z-order.
     */
    public ZOrder belowLayers(String... layerIds) {
        HashSet<String> newLayersBelow = new HashSet<String>(layersBelow);
        HashSet<String> newLayersAbove = new HashSet<String>(layersAbove.size() + layerIds.length);
        newLayersAbove.addAll(layersAbove);
        newLayersAbove.addAll(Arrays.asList(layerIds));
        return new ZOrder(newLayersAbove, newLayersBelow);
    }
}
