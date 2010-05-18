/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.spi.navigator;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.navigator.NavigatorTC;
import org.netbeans.modules.navigator.UnitTestUtils;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Dafe Simonek
 */
public class NavigatorHandlerTest extends NbTestCase {
    
    public NavigatorHandlerTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(NavigatorHandlerTest.class);
        return suite;
    }
    
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
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
        
        @Override
        public String getDisplayName () {
            return "Panel Impl 1";
        }
    
        @Override
        public String getDisplayHint () {
            return null;
        }

        @Override
        public JComponent getComponent () {
            return new JPanel();
        }

        @Override
        public void panelActivated (Lookup context) {
        }

        @Override
        public void panelDeactivated () {
        }
        
        @Override
        public Lookup getLookup () {
            return null;
        }
    }
    
    /** Panel implementation 2
     */
    public static final class PanelImpl2 implements NavigatorPanel {
        
        @Override
        public String getDisplayName () {
            return "Panel Impl 2";
        }
    
        @Override
        public String getDisplayHint () {
            return null;
        }

        @Override
        public JComponent getComponent () {
            return new JPanel();
        }

        @Override
        public void panelActivated (Lookup context) {
        }

        @Override
        public void panelDeactivated () {
        }
        
        @Override
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
        
        @Override
        public String getContentType () {
            return contentType;
        }

    }
    

    private static final class GlobalLookup4TestImpl extends AbstractLookup implements ContextGlobalProvider {
        
        public GlobalLookup4TestImpl (AbstractLookup.Content content) {
            super(content);
        }
        
        @Override
        public Lookup createGlobalContext() {
            return this;
        }
    }
            
    
}
