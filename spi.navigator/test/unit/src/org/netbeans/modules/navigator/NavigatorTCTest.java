/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.navigator;

import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


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
        InstanceContent ic = new InstanceContent();
        GlobalLookup4Test nodesLkp = new GlobalLookup4Test(ic);
        UnitTestUtils.prepareTest(new String [] { 
            "/org/netbeans/modules/navigator/resources/testCorrectCallsOfNavigatorPanelMethodsLayer.xml" }, 
            Lookups.singleton(nodesLkp)
        );

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

        // wait for selected node change to be applied, because changes are
        // reflected with little delay
        waitForChange();
        
        selPanel = navTC.getSelectedPanel();
        assertNull(selPanel);
        
        assertEquals(1, prazak.getPanelDeactivatedCallsCount());
        assertTrue(prazak.wasGetCompBetween());
        assertFalse(prazak.wasActCalledOnActive());
        assertFalse(prazak.wasDeactCalledOnInactive());
        
        navTC.componentClosed();
        selPanel = navTC.getSelectedPanel();
        assertNull(selPanel);
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

    /** Envelope for textual (mime-type like) content type to be used in 
     * global lookup
     */
    private static final class TestLookupHint implements NavigatorLookupHint {
        
        private final String contentType; 
                
        public TestLookupHint (String contentType) {
            this.contentType = contentType;
        }
        
        public String getContentType () {
            return contentType;
        }

    }
            
    
    private static final class GlobalLookup4Test extends AbstractLookup implements ContextGlobalProvider {
        
        public GlobalLookup4Test (AbstractLookup.Content content) {
            super(content);
        }
        
        public Lookup createGlobalContext() {
            return this;
        }
        
        /*public GlobalLookup4Test() {
            super(new Lookup[0]);
        }*/
        
        /*public void setNodes(Node[] nodes) {
            setLookups(new Lookup[] {Lookups.fixed(nodes)});
        }*/
    }
    
}
