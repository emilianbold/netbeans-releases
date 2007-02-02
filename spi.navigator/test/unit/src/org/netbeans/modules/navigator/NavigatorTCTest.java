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
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorPanel;
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
    }
    
    public void testBugfix93123_RefreshCombo () throws Exception {
        System.out.println("Testing bugfix 93123, correct refreshing of combo box with providers list...");

        InstanceContent ic = getInstanceContent();
        
        TestLookupHint ostravskiHint = new TestLookupHint("ostravski/gyzd");
        ic.add(ostravskiHint);
            
        NavigatorTC navTC = NavigatorTC.getInstance();
        navTC.componentOpened();

        NavigatorPanel selPanel = navTC.getSelectedPanel();
        
        assertNotNull("Selected panel is null", selPanel);
        
        TestLookupHint prazskyHint = new TestLookupHint("prazsky/pepik");
        ic.add(prazskyHint);
        
        // wait for selected node change to be applied, because changes are
        // reflected with little delay
        waitForChange();

        List<NavigatorPanel> panels = navTC.getPanels();
        assertTrue("Expected 2 provider panels, but got " + panels.size(), panels.size() == 2);
        
        JComboBox combo = navTC.getPanelSelector();
        assertTrue("Expected 2 combo items, but got " + combo.getItemCount(), combo.getItemCount() == 2);
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
            
    
    private static final class GlobalLookup4TestImpl extends AbstractLookup implements ContextGlobalProvider {
        
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
