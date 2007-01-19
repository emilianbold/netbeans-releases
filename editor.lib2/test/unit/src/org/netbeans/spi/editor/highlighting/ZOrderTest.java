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

import org.netbeans.junit.NbTestCase;
import org.openide.util.TopologicalSortException;

/**
 *
 * @author vita
 */
public class ZOrderTest extends NbTestCase {
    
    /** Creates a new instance of ZOrderTest */
    public ZOrderTest(String name) {
        super(name);
    }
    
    public void testOrder() throws Exception {
        ZOrder zOrderA = ZOrder.DEFAULT_RACK;
        ZOrder zOrderB = ZOrder.above("layerA");
        
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerB", zOrderB),
            simpleLayer("layerA", zOrderA),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);
        
        assertSame("Wrong order", layers[0], sortedLayers[1]);
        assertSame("Wrong order", layers[1], sortedLayers[0]);
    }

    public void testOrder2() throws Exception {
        ZOrder zOrderA = ZOrder.DEFAULT_RACK.belowLayers("layerB");
        ZOrder zOrderB = ZOrder.DEFAULT_RACK.aboveLayers("layerA").belowLayers("layerC");
        ZOrder zOrderC = ZOrder.DEFAULT_RACK.aboveLayers("layerB").belowLayers("layerD");
        ZOrder zOrderD = ZOrder.DEFAULT_RACK.aboveLayers("layerC").belowLayers("layerE");
        ZOrder zOrderE = ZOrder.DEFAULT_RACK.aboveLayers("layerD");
        
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerD", zOrderD),
            simpleLayer("layerC", zOrderC),
            simpleLayer("layerA", zOrderA),
            simpleLayer("layerE", zOrderE),
            simpleLayer("layerB", zOrderB),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);

        char ch = 'A';
        for (int i = 0; i < sortedLayers.length; i++) {
            String expectedLayerName = "layer" + ch++;
            assertEquals("Wrong order", expectedLayerName, sortedLayers[i].getLayerTypeId());
        }
    }

    public void testCreation() {
        ZOrder zOrder = ZOrder.DEFAULT_RACK;
        assertNotNull("ZOrder.DEFAULT", zOrder);
        assertEquals("DEFAULT.layersAbove", 1, zOrder.layersAbove.size());
        assertEquals("DEFAULT.layersBelow", 1, zOrder.layersBelow.size());
        
        zOrder = ZOrder.BOTTOM_RACK;
        assertNotNull("ZOrder.BOTTOM", zOrder);
        assertEquals("BOTTOM.layersAbove", 1, zOrder.layersAbove.size());
        assertEquals("BOTTOM.layersBelow", 0, zOrder.layersBelow.size());
        
        zOrder = ZOrder.TOP_RACK;
        assertNotNull("ZOrder.TOP", zOrder);
        assertEquals("TOP.layersAbove", 0, zOrder.layersAbove.size());
        assertEquals("TOP.layersBelow", 1, zOrder.layersBelow.size());
        
        zOrder = ZOrder.above("layerA");
        assertEquals("layersAbove should be empty", 0, zOrder.layersAbove.size());
        assertEquals("Wrong number of layersBelow", 1, zOrder.layersBelow.size());
        assertEquals("Wrong layersBelow", "layerA", zOrder.layersBelow.iterator().next());
        
        zOrder = ZOrder.below("layerA");
        assertEquals("layersBelow should be empty", 0, zOrder.layersBelow.size());
        assertEquals("Wrong number of layersAbove", 1, zOrder.layersAbove.size());
        assertEquals("Wrong layersAbove", "layerA", zOrder.layersAbove.iterator().next());
        
        zOrder = ZOrder.DEFAULT_RACK.aboveLayers("layerA");
        assertNotSame("ZOrder was not cloned", ZOrder.DEFAULT_RACK, zOrder);
        assertEquals("layersAbove should be empty", 1, zOrder.layersAbove.size());
        assertEquals("Wrong number of layersBelow", 2, zOrder.layersBelow.size());
        assertTrue("Wrong layersBelow", zOrder.layersBelow.contains("layerA"));
        
        zOrder = ZOrder.DEFAULT_RACK.belowLayers("layerA");
        assertNotSame("ZOrder was not cloned", ZOrder.DEFAULT_RACK, zOrder);
        assertEquals("layersBelow should be empty", 1, zOrder.layersBelow.size());
        assertEquals("Wrong number of layersAbove", 2, zOrder.layersAbove.size());
        assertTrue("Wrong layersAbove", zOrder.layersAbove.contains("layerA"));
        
        zOrder = ZOrder.DEFAULT_RACK.aboveLayers("layerA").belowLayers("layerB");
        assertEquals("Wrong number of layersAbove", 2, zOrder.layersAbove.size());
        assertTrue("Wrong layersAbove", zOrder.layersAbove.contains("layerB"));
        assertEquals("Wrong number of layersBelow", 2, zOrder.layersBelow.size());
        assertTrue("Wrong layersBelow", zOrder.layersBelow.contains("layerA"));
    }
    
    public void testTop() throws TopologicalSortException {
        ZOrder zOrderA = ZOrder.TOP_RACK;
        ZOrder zOrderB = ZOrder.above("layerA");
        
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerB", zOrderB),
            simpleLayer("layerA", zOrderA),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);
        
        assertSame("Wrong order", layers[0], sortedLayers[1]);
        assertSame("Wrong order", layers[1], sortedLayers[0]);
    }
    
    public void testBottom() throws TopologicalSortException {
        ZOrder zOrderA = ZOrder.BOTTOM_RACK;
        ZOrder zOrderB = ZOrder.below("layerA");
        
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerA", zOrderA),
            simpleLayer("layerB", zOrderB),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);
        
        assertSame("Wrong order", layers[0], sortedLayers[1]);
        assertSame("Wrong order", layers[1], sortedLayers[0]);
    }
    
    public void testRacks() throws TopologicalSortException {
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerE", ZOrder.SHOW_OFF_RACK),
            simpleLayer("layerC", ZOrder.CARET_RACK),
            simpleLayer("layerF", ZOrder.TOP_RACK),
            simpleLayer("layerA", ZOrder.BOTTOM_RACK),
            simpleLayer("layerD", ZOrder.DEFAULT_RACK),
            simpleLayer("layerB", ZOrder.SYNTAX_RACK),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);
        
        char ch = 'A';
        for(int i = 0; i < sortedLayers.length; i++) {
            assertEquals("Wrong order", "layer" + ch, sortedLayers[i].getLayerTypeId());
            ch++;
        }
    }
    
    public void testComplex() throws TopologicalSortException {
        ZOrder zOrderA = ZOrder.BOTTOM_RACK;
        ZOrder zOrderB = ZOrder.DEFAULT_RACK.aboveLayers("layerA").belowLayers("layerC");
        ZOrder zOrderC = ZOrder.DEFAULT_RACK.aboveLayers("layerB").belowLayers("layerD");
        ZOrder zOrderD = ZOrder.DEFAULT_RACK.aboveLayers("layerC").belowLayers("layerE");
        ZOrder zOrderE = ZOrder.TOP_RACK;
        
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerD", zOrderD),
            simpleLayer("layerC", zOrderC),
            simpleLayer("layerA", zOrderA),
            simpleLayer("layerE", zOrderE),
            simpleLayer("layerB", zOrderB),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);
        
        char ch = 'A';
        for (int i = 0; i < sortedLayers.length; i++) {
            String expectedLayerName = "layer" + ch++;
            assertEquals("Wrong order", expectedLayerName, sortedLayers[i].getLayerTypeId());
        }
    }

    private HighlightsLayer simpleLayer(String layerId, ZOrder zOrder) {
        return HighlightsLayer.create(layerId, zOrder, true, null);
    }
}
