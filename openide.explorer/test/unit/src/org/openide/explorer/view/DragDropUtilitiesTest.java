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

package org.openide.explorer.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 *
 * @author Jaroslav Tulach
 */
public class DragDropUtilitiesTest extends NbTestCase {
    
    public DragDropUtilitiesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(new Class[] {MyClipboard.class});
        last = null;
    }
    
    public void testGetNodeTransferableForSingleNodeCopy() throws Exception {
        N node = new N();
        
        Transferable t = DragDropUtilities.getNodeTransferable(node, NodeTransfer.DND_COPY);
        
        assertEquals("One call to copy", 1, node.copy);
        assertEquals("Also one call to drag which delegates to copy", 1, node.drag);
        assertEquals("No call to cut", 0, node.cut);
        assertNotNull("Call to convertor", last);
        assertTrue("StringSelection got to ExClipboard convertor", last instanceof StringSelection);
    }
    
    public void testGetNodeTransferableForSingleNodeCut() throws Exception {
        N node = new N();
        
        Transferable t = DragDropUtilities.getNodeTransferable(node, NodeTransfer.DND_MOVE);
        
        assertEquals("One call to cut", 1, node.cut);
        assertEquals("No call to drag", 0, node.drag);
        assertEquals("No call to copy", 0, node.copy);
        assertNotNull("Call to convertor", last);
        assertTrue("StringSelection got to ExClipboard convertor", last instanceof StringSelection);
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
        
        assertNotNull("Call to convertor", last);
        assertTrue("multi flavor supported", last.isDataFlavorSupported(ExTransferable.multiFlavor));
        Object obj = last.getTransferData(ExTransferable.multiFlavor);
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
        
        assertNotNull("Call to convertor", last);
        assertTrue("multi flavor supported", last.isDataFlavorSupported(ExTransferable.multiFlavor));
        Object obj = last.getTransferData(ExTransferable.multiFlavor);
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
    
    public static Transferable last;
    
    public static final class MyClipboard extends ExClipboard {
        
        public MyClipboard() {
            super("Empty");
        }
        
        public ExClipboard.Convertor[] getConvertors() {
            return new ExClipboard.Convertor[] {new ExClipboard.Convertor() {
                public Transferable convert(Transferable t) {
                    last = t;
                    return t;
                }
            }};
        }
    }
}
