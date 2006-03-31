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

package org.openide.explorer.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.ExClipboard.Convertor;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach
 */
public class DragDropUtilitiesTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }
    
    
    private Lkp lookup;
    
    public DragDropUtilitiesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        Lookup l = Lookup.getDefault();
        assertEquals(Lkp.class, l.getClass());
        lookup = (Lkp)l;
        lookup.last = null;
    }
    
    public void testGetNodeTransferableForSingleNodeCopy() throws Exception {
        N node = new N();
        
        Transferable t = DragDropUtilities.getNodeTransferable(node, NodeTransfer.DND_COPY);
        
        assertEquals("One call to copy", 1, node.copy);
        assertEquals("Also one call to drag which delegates to copy", 1, node.drag);
        assertEquals("No call to cut", 0, node.cut);
        assertNotNull("Call to convertor", lookup.last);
        assertTrue("StringSelection got to ExClipboard convertor", lookup.last instanceof StringSelection);
    }
    
    public void testGetNodeTransferableForSingleNodeCut() throws Exception {
        N node = new N();
        
        Transferable t = DragDropUtilities.getNodeTransferable(node, NodeTransfer.DND_MOVE);
        
        assertEquals("One call to cut", 1, node.cut);
        assertEquals("No call to drag", 0, node.drag);
        assertEquals("No call to copy", 0, node.copy);
        assertNotNull("Call to convertor", lookup.last);
        assertTrue("StringSelection got to ExClipboard convertor", lookup.last instanceof StringSelection);
    }
    
    public void testMultiTransferableForCopy() throws Exception {
        N node = new N();
        N n2 = new N();
        N[] arr = { node, n2 };
        
        Transferable t = DragDropUtilities.getNodeTransferable(arr, NodeTransfer.DND_COPY);
        
        assertEquals("One call to copy", 1, node.copy);
        assertEquals("One call to copy on n2", 1, n2.copy);
        assertEquals("Also one call to drag which delegates to copy", 1, node.drag);
        assertEquals("Also one call to drag which delegates to copy on n2", 1, n2.drag);
        assertEquals("No call to cut", 0, node.cut);
        assertEquals("No call to cut", 0, n2.cut);
        
        assertNotNull("Call to convertor", lookup.last);
        assertTrue("multi flavor supported", lookup.last.isDataFlavorSupported(ExTransferable.multiFlavor));
        Object obj = lookup.last.getTransferData(ExTransferable.multiFlavor);
        if (!( obj instanceof MultiTransferObject)) {
            fail("It should be MultiTransferObject: " + obj);
        }
        MultiTransferObject m = (MultiTransferObject)obj;
        
        assertEquals("Two in multi", 2, m.getCount());
        assertTrue("Is string", m.getTransferData(0, DataFlavor.stringFlavor) instanceof String);
        assertTrue("Is string2", m.getTransferData(1, DataFlavor.stringFlavor) instanceof String);
    }
    
    public void testMultiTransferableForCut() throws Exception {
        N node = new N();
        N n2 = new N();
        N[] arr = { node, n2 };
        
        Transferable t = DragDropUtilities.getNodeTransferable(arr, NodeTransfer.DND_MOVE);
        
        assertEquals("One call to cut ", 1, node.cut);
        assertEquals("One call to cut on n2", 1, n2.cut);
        assertEquals("No to drag", 0, node.drag);
        assertEquals("No to drag on n2", 0, n2.drag);
        assertEquals("No call to copy", 0, node.copy);
        assertEquals("No call to copy on n2", 0, n2.copy);
        
        assertNotNull("Call to convertor", lookup.last);
        assertTrue("multi flavor supported", lookup.last.isDataFlavorSupported(ExTransferable.multiFlavor));
        Object obj = lookup.last.getTransferData(ExTransferable.multiFlavor);
        if (!( obj instanceof MultiTransferObject)) {
            fail("It should be MultiTransferObject: " + obj);
        }
        MultiTransferObject m = (MultiTransferObject)obj;
        
        assertEquals("Two in multi", 2, m.getCount());
        assertTrue("Is string", m.getTransferData(0, DataFlavor.stringFlavor) instanceof String);
        assertTrue("Is string2", m.getTransferData(1, DataFlavor.stringFlavor) instanceof String);
    }
    
    
    
    private static class N extends AbstractNode {
        public int copy;
        public int cut;
        public int drag;
        public Transferable ret = new StringSelection("A text");
        
        public N() {
            super(Children.LEAF);
        }
        
        public Transferable clipboardCut() throws IOException {
            cut++;
            return ret;
        }
        
        public Transferable clipboardCopy() throws IOException {
            copy++;
            return ret;
        }
        
        public Transferable drag() throws IOException {
            drag++;
            return super.drag();
        }
    }
    
    public static final class Lkp extends AbstractLookup
            implements ExClipboard.Convertor {
        public Transferable last;
        
        public Lkp() {
            this(new InstanceContent());
        }
        
        protected Lkp(InstanceContent ic) {
            super(ic);
            ic.add(new MyClipboard(this));
        }
        
        public Transferable convert(Transferable t) {
            last = t;
            return t;
        }
    }
    
    private static final class MyClipboard extends ExClipboard {
        private Lkp lookup;
        
        public MyClipboard(Lkp lookup) {
            super("Empty");
            this.lookup = lookup;
        }
        
        public Convertor[] getConvertors() {
            return new Convertor[] { lookup };
        }
    }
}
