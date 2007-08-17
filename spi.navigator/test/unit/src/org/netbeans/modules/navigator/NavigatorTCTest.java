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

package org.netbeans.modules.navigator;

import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.navigator.NavigatorHandler;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.awt.UndoRedo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Dafe Simonek
 */
public class NavigatorTCTest extends NbTestCase {
    
    /** Creates a new instance of ProviderRegistryTest */
    public NavigatorTCTest() {
        super("");
    }
    
    public NavigatorTCTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(NavigatorTCTest.class);
        return suite;
    }
    
    
    public void testCorrectCallsOfNavigatorPanelMethods () throws Exception {
        System.out.println("Testing correct calls of NavigatorPanel methods...");
        InstanceContent ic = getInstanceContent();

        TestLookupHint ostravskiHint = new TestLookupHint("ostravski/gyzd");
        //nodesLkp.setNodes(new Node[]{ostravskiNode});
        ic.add(ostravskiHint);
            
        NavigatorTC navTC = NavigatorTC.getInstance();
        navTC.componentOpened();

        NavigatorPanel selPanel = navTC.getSelectedPanel();
        
        assertNotNull("Selected panel is null", selPanel);
        assertTrue("Panel class not expected", selPanel instanceof OstravskiGyzdProvider);
        OstravskiGyzdProvider ostravak = (OstravskiGyzdProvider)selPanel;
        assertEquals("panelActivated calls count invalid: " + ostravak.getPanelActivatedCallsCount(),
                        1, ostravak.getPanelActivatedCallsCount());
        assertEquals(0, ostravak.getPanelDeactivatedCallsCount());
        
        TestLookupHint prazskyHint = new TestLookupHint("prazsky/pepik");
        ic.add(prazskyHint);
        ic.remove(ostravskiHint);
        
        // wait for selected node change to be applied, because changes are
        // reflected with little delay
        waitForChange();
        
        selPanel = navTC.getSelectedPanel();
        assertNotNull(selPanel);
        assertTrue(selPanel instanceof PrazskyPepikProvider);
        PrazskyPepikProvider prazak = (PrazskyPepikProvider)selPanel;
        
        assertEquals(1, ostravak.getPanelDeactivatedCallsCount());
        assertTrue(ostravak.wasGetCompBetween());
        assertFalse(ostravak.wasActCalledOnActive());
        assertFalse(ostravak.wasDeactCalledOnInactive());
        
        assertEquals(1, prazak.getPanelActivatedCallsCount());
        assertEquals(0, prazak.getPanelDeactivatedCallsCount());

        ic.remove(prazskyHint);
        ic.add(ostravskiHint);
        // wait for selected node change to be applied, because changes are
        // reflected with little delay
        waitForChange();
        
        selPanel = navTC.getSelectedPanel();
        assertNotNull("Selected panel is null", selPanel);
        
        assertEquals(1, prazak.getPanelDeactivatedCallsCount());
        assertTrue(prazak.wasGetCompBetween());
        assertFalse(prazak.wasActCalledOnActive());
        assertFalse(prazak.wasDeactCalledOnInactive());
        
        navTC.componentClosed();

        selPanel = navTC.getSelectedPanel();
        assertNull("Selected panel should be null", selPanel);
        assertNull("Set of panels should be null", navTC.getPanels());
        
        // clean
        ic.remove(ostravskiHint);
    }
    
    public void testBugfix80155_NotEmptyOnProperties () throws Exception {
        System.out.println("Testing bugfix 80155, keeping content on Properties window and similar...");
        InstanceContent ic = getInstanceContent();

        TestLookupHint ostravskiHint = new TestLookupHint("ostravski/gyzd");
        ic.add(ostravskiHint);
            
        NavigatorTC navTC = NavigatorTC.getInstance();
        navTC.componentOpened();

        NavigatorPanel selPanel = navTC.getSelectedPanel();
        
        assertNotNull("Selected panel is null", selPanel);
        
        ic.remove(ostravskiHint);
        
        // wait for selected node change to be applied, because changes are
        // reflected with little delay
        waitForChange();

        // after 80155 fix, previous navigator should keep its content even when
        // new component was activated, but didn't contain any activated nodes or navigator lookup hint
        selPanel = navTC.getSelectedPanel();
        assertNotNull("Selected panel is null", selPanel);
        assertTrue("Panel class not expected", selPanel instanceof OstravskiGyzdProvider);

        // cleanup
        navTC.componentClosed();
    }
    
    public void testBugfix93123_RefreshCombo () throws Exception {
        System.out.println("Testing bugfix 93123, correct refreshing of combo box with providers list...");

        InstanceContent ic = getInstanceContent();
        
        TestLookupHint ostravskiHint = new TestLookupHint("ostravski/gyzd");
        ic.add(ostravskiHint);
        TestLookupHint prazskyHint = new TestLookupHint("prazsky/pepik");
        ic.add(prazskyHint);
            
        NavigatorTC navTC = NavigatorTC.getInstance();
        navTC.componentOpened();

        List<NavigatorPanel> panels = navTC.getPanels();
        
        assertNotNull("Selected panel should not be null", navTC.getSelectedPanel());
        assertTrue("Expected 2 provider panels, but got " + panels.size(), panels.size() == 2);
        
        NavigatorHandler.activatePanel(panels.get(1));
        
        NavigatorPanel selPanel = navTC.getSelectedPanel();
        int selIdx = panels.indexOf(selPanel);
        
        assertTrue("Expected selected provider #2, but got #1", selIdx == 1);
        
        TestLookupHint prazskyHint2 = new TestLookupHint("moravsky/honza");
        ic.add(prazskyHint2);
        
        // wait for selected node change to be applied, because changes are
        // reflected with little delay
        waitForChange();
        
        panels = navTC.getPanels();
        assertTrue("Expected 3 provider panels, but got " + panels.size(), panels.size() == 3);
        
        JComboBox combo = navTC.getPanelSelector();
        assertTrue("Expected 3 combo items, but got " + combo.getItemCount(), combo.getItemCount() == 3);
     
        assertTrue("Expected the same selection", selPanel.equals(navTC.getSelectedPanel()));
        
        selIdx = panels.indexOf(selPanel);
        assertTrue("Expected the same selection in combo, sel panel index: "
                + selIdx + ", sel in combo index: " + 
                combo.getSelectedIndex(), selIdx == combo.getSelectedIndex());

        // cleanup
        ic.remove(ostravskiHint);
        ic.remove(prazskyHint);
        ic.remove(prazskyHint2);
        navTC.componentClosed();
    }
    
    /** Test for IZ feature #93711. It tests ability of NavigatorPanel implementors
     * to provide activated nodes for whole navigator panel TopComponent.
     * 
     * See inner class ActNodeLookupProvider, especially getLookup method to get
     * inspiration how to write providers that provide also activated nodes.
     */
    public void testFeature93711_ActivatedNodes () throws Exception {
        System.out.println("Testing feature #93711, providing activated nodes...");

        InstanceContent ic = getInstanceContent();
        
        TestLookupHint actNodesHint = new TestLookupHint("actNodes/tester");
        ic.add(actNodesHint);
            
        NavigatorTC navTC = NavigatorTC.getInstance();
        navTC.componentOpened();

        List<NavigatorPanel> panels = navTC.getPanels();
        assertNotNull("Selected panel should not be null", navTC.getSelectedPanel());
        assertTrue("Expected 1 provider panel, but got " + panels.size(), panels != null && panels.size() == 1);
        assertTrue("Panel class not expected", panels.get(0) instanceof ActNodeLookupProvider);
        ActNodeLookupProvider provider = (ActNodeLookupProvider)panels.get(0);
                
        // wait for selected node change to be applied, because changes are
        // reflected with little delay
        waitForChange();

        // test if lookup content from provider propagated correctly to the
        // activated nodes of navigator TopComponent
        Node[] actNodes = navTC.getActivatedNodes();
        Node realContent = provider.getCurLookupContent();
        String tcDisplayName = navTC.getDisplayName();
        String providerDisplayName = provider.getDisplayName();
        
        assertNotNull("Activated nodes musn't be null", actNodes);
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertTrue("Incorrect instance of activated node " + actNodes[0].getName(), actNodes[0] == realContent);
        assertTrue("Expected display name starting with '" + providerDisplayName +
                    "', but got '" + tcDisplayName + "'",
                    (tcDisplayName != null) && tcDisplayName.startsWith(providerDisplayName));
        
        // change provider's lookup content and check again, to test infrastructure
        // ability to listen to client's lookup content change
        provider.changeLookup();
        actNodes = navTC.getActivatedNodes();
        realContent = provider.getCurLookupContent();
        tcDisplayName = navTC.getDisplayName();
        providerDisplayName = provider.getDisplayName();
        
        assertNotNull("Activated nodes musn't be null", actNodes);
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertTrue("Incorrect instance of activated node " + actNodes[0].getName(), actNodes[0] == realContent);
        assertTrue("Expected display name starting with '" + providerDisplayName +
                    "', but got '" + tcDisplayName + "'",
                    (tcDisplayName != null) && tcDisplayName.startsWith(providerDisplayName));
        
        // cleanup
        ic.remove(actNodesHint);
        navTC.componentClosed();
    }
    
    /** Test for IZ feature #98125. It tests ability of NavigatorPanelWithUndo
     * implementors to provide UndoRedo support for their view through
     * navigator TopComponent.
     */
    public void testFeature98125_UndoRedo () throws Exception {
        System.out.println("Testing feature #98125, providing UndoRedo...");

        InstanceContent ic = getInstanceContent();
        
        TestLookupHint undoHint = new TestLookupHint("undoRedo/tester");
        ic.add(undoHint);
            
        NavigatorTC navTC = NavigatorTC.getInstance();
        navTC.componentOpened();

        NavigatorPanel selPanel = navTC.getSelectedPanel();
        assertNotNull("Selected panel should not be null", navTC.getSelectedPanel());
        assertTrue("Panel class not expected", selPanel instanceof UndoRedoProvider);
        UndoRedoProvider provider = (UndoRedoProvider)selPanel;
        
        UndoRedo panelUndo = provider.getUndoRedo();
        UndoRedo tcUndo = navTC.getUndoRedo();
        
        assertTrue("Expected undo manager " + panelUndo + ", but got " + tcUndo, panelUndo == tcUndo);
        
        // cleanup
        ic.remove(undoHint);
        navTC.componentClosed();
        
    }
    
    
    /** Singleton global lookup. Lookup change notification won't come
     * if setting global lookup (UnitTestUtils.prepareTest) is called
     * multiple times.
     */ 
    private static InstanceContent getInstanceContent () throws Exception {
        if (instanceContent == null) {
            instanceContent = new InstanceContent();
            GlobalLookup4TestImpl nodesLkp = new GlobalLookup4TestImpl(instanceContent);
            UnitTestUtils.prepareTest(new String [] { 
                "/org/netbeans/modules/navigator/resources/testCorrectCallsOfNavigatorPanelMethodsLayer.xml" }, 
                Lookups.singleton(nodesLkp)
            );
        }
        return instanceContent;
    }
    
    private void waitForChange () {
        synchronized (this) {
            try {
                wait(NavigatorController.COALESCE_TIME + 500);
            } catch (InterruptedException exc) {
                System.out.println("waiting interrupted...");
            }
        }
    }
    

    /** Test provider base, to test that infrastucture calls correct
     * methods in correct order.
     */ 
    private static abstract class CorrectCallsProvider implements NavigatorPanel {
        
        private int panelActCalls = 0;
        private int panelDeactCalls = 0;
        
        private boolean wasGetCompBetween = true;
        
        private boolean wasActCalledOnActive = false;
        private boolean wasDeactCalledOnInactive = false;
        
        private boolean activated = false;
        
        public JComponent getComponent () {
            if (!activated) {
                wasGetCompBetween = false;
            }
            return null;
        }

        public void panelActivated (Lookup context) {
            if (activated) {
                wasActCalledOnActive = true;
            }
            panelActCalls++;
            activated = true;
        }

        public void panelDeactivated () {
            if (!activated) {
                wasDeactCalledOnInactive = true;
            }
            panelDeactCalls++;
            activated = false;
        }
        
        public Lookup getLookup () {
            return null;
        }
        
        public int getPanelActivatedCallsCount () {
            return panelActCalls;
        }
        
        public int getPanelDeactivatedCallsCount () {
            return panelDeactCalls;
        }
        
        public boolean wasGetCompBetween () {
            return wasGetCompBetween;
        } 
        
        public boolean wasActCalledOnActive () {
            return wasActCalledOnActive;
        }
        
        public boolean wasDeactCalledOnInactive () {
            return wasDeactCalledOnInactive;
        }
        
    }

    public static final class OstravskiGyzdProvider extends CorrectCallsProvider {
        
        public String getDisplayName () {
            return "Ostravski Gyzd";
        }
    
        public String getDisplayHint () {
            return null;
        }
        
        public JComponent getComponent () {
            // ensure call is counted by superclass
            super.getComponent();
            return new JLabel(getDisplayName());
        }

    }
    
    public static final class PrazskyPepikProvider extends CorrectCallsProvider {
        
        public String getDisplayName () {
            return "Prazsky Pepik";
        }
    
        public String getDisplayHint () {
            return null;
        }
        
        public JComponent getComponent () {
            // ensure call is counted by superclass
            super.getComponent();
            return new JLabel(getDisplayName());
        }
    
    }
    
    public static final class MoravskyHonzaProvider extends CorrectCallsProvider {
        
        public String getDisplayName () {
            return "Moravsky Honza";
        }
    
        public String getDisplayHint () {
            return null;
        }
        
        public JComponent getComponent () {
            // ensure call is counted by superclass
            super.getComponent();
            return new JLabel(getDisplayName());
        }
    
    }
    
    /** NavigatorPanel implementation that affects activated nodes of whole
     * navigator area TopComponent. See javadoc of NavigatorPanel.getLookup()
     * for details.
     * 
     * This test class defines method changeLookup to test infrastructure
     * ability to listen to lookup changes.
     */
    public static final class ActNodeLookupProvider implements NavigatorPanel {
        
        private Lookup lookup;
        private Node node1, node2;
        private boolean flag = false;
        private InstanceContent ic;
        
        private static final String FIRST_NAME = "first";
        private static final String SECOND_NAME = "second";
                
        public ActNodeLookupProvider () {
            this.node1 = new AbstractNode(Children.LEAF);
            this.node1.setDisplayName(FIRST_NAME);
            this.node2 = new AbstractNode(Children.LEAF);
            this.node2.setDisplayName(SECOND_NAME);
            
            ic = new InstanceContent();
            ic.add(node1);
            lookup = new AbstractLookup(ic);
        }
        
        public Lookup getLookup () {
            return lookup;
        }
        
        public Node getCurLookupContent () {
            return flag ? node2 : node1;
        }
        
        public void changeLookup () {
            flag = !flag;
            if (flag) {
                ic.remove(node1);
                ic.add(node2);
            } else {
                ic.remove(node2);
                ic.add(node1);
            }
        }
    
        public String getDisplayName() {
            return flag ? SECOND_NAME : FIRST_NAME;
        }

        public String getDisplayHint() {
            return null;
        }

        public JComponent getComponent() {
            return new JLabel(getDisplayName());
        }

        public void panelActivated(Lookup context) {
            // no operation
        }

        public void panelDeactivated() {
            // no operation
        }
    }

    /**
     * Test implementation of NavigatorPanelWithUndo which enables undo/redo support.
     */
    public static final class UndoRedoProvider implements NavigatorPanelWithUndo {

        private UndoRedo undo;
        
        public UndoRedo getUndoRedo() {
            if (undo == null) {
                undo = new UndoRedo.Manager();
            } 
            return undo;
        }

        public String getDisplayName() {
            return "UndoRedo provider";
        }

        public String getDisplayHint() {
            return null;
        }

        public JComponent getComponent() {
            return new JLabel("test");
        }

        public void panelActivated(Lookup context) {
            // no operation
        }

        public void panelDeactivated() {
            // no operation
        }

        public Lookup getLookup() {
            return null;
        }
        
    }

    /** Envelope for textual (mime-type like) content type to be used in 
     * global lookup
     */
    public static class TestLookupHint implements NavigatorLookupHint {
        
        private final String contentType; 
                
        public TestLookupHint (String contentType) {
            this.contentType = contentType;
        }
        
        public String getContentType () {
            return contentType;
        }

    }
            
    
    public static final class GlobalLookup4TestImpl extends AbstractLookup implements ContextGlobalProvider {
        
        public GlobalLookup4TestImpl (AbstractLookup.Content content) {
            super(content);
        }
        
        public Lookup createGlobalContext() {
            return this;
        }
        
        /*public GlobalLookup4Test() {
            super(new Lookup[0]);
        }
        
        public void setNodes(Node[] nodes) {
            setLookups(new Lookup[] {Lookups.fixed(nodes)});
        }*/
    }
    private static InstanceContent instanceContent;
    
}
