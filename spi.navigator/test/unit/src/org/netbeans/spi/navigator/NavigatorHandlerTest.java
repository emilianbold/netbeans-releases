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

package org.netbeans.spi.navigator;

import java.io.File;
import java.net.URL;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import junit.framework.*;
import org.netbeans.modules.navigator.NavigatorTC;
import org.netbeans.modules.navigator.UnitTestUtils;
import org.netbeans.spi.navigator.NavigatorHandler;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Dafe Simonek
 */
public class NavigatorHandlerTest extends TestCase {
    
    public NavigatorHandlerTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(NavigatorHandlerTest.class);
        return suite;
    }
    
    public void testActivatePanel () throws Exception {
        System.out.println("Testing NavigatorHandlerTest.activatePanel");
        InstanceContent ic = new InstanceContent();
        GlobalLookup4TestImpl nodesLkp = new GlobalLookup4TestImpl(ic);
        UnitTestUtils.prepareTest(new String [] { 
            "/org/netbeans/modules/navigator/resources/NavigatorHandlerTestProvider.xml" }, 
            Lookups.singleton(nodesLkp)
        );

        TestLookupHint hint = new TestLookupHint("NavigatorHandlerTest/TestMimeType");
        ic.add(hint);
            
        NavigatorTC navTC = NavigatorTC.getInstance();
        navTC.componentOpened();

        NavigatorPanel selPanel = navTC.getSelectedPanel();
        assertNotNull("Selected panel is null", selPanel);
        
        List<NavigatorPanel> panels = navTC.getPanels();
        assertEquals(2, panels.size());
        
        int selIndex = panels.indexOf(selPanel);
        assertTrue(selIndex >= 0);
        
        System.out.println("selected panel before: " + navTC.getSelectedPanel().getDisplayName());
        
        if (selIndex == 0) {
            NavigatorHandler.activatePanel(panels.get(1));
        } else {
            NavigatorHandler.activatePanel(panels.get(0));
        }
        
        assertTrue(selPanel != navTC.getSelectedPanel());
        
        System.out.println("selected panel after: " + navTC.getSelectedPanel().getDisplayName());
        
    }

    /** Panel implementation 1
     */
    public static final class PanelImpl1 implements NavigatorPanel {
        
        public String getDisplayName () {
            return "Panel Impl 1";
        }
    
        public String getDisplayHint () {
            return null;
        }

        public JComponent getComponent () {
            return new JPanel();
        }

        public void panelActivated (Lookup context) {
        }

        public void panelDeactivated () {
        }
        
        public Lookup getLookup () {
            return null;
        }
    }
    
    /** Panel implementation 2
     */
    public static final class PanelImpl2 implements NavigatorPanel {
        
        public String getDisplayName () {
            return "Panel Impl 2";
        }
    
        public String getDisplayHint () {
            return null;
        }

        public JComponent getComponent () {
            return new JPanel();
        }

        public void panelActivated (Lookup context) {
        }

        public void panelDeactivated () {
        }
        
        public Lookup getLookup () {
            return null;
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
    }
            
    
}
