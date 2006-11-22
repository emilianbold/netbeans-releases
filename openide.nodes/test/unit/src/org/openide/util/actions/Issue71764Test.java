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

package org.openide.util.actions;

import java.io.IOException;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;

/** Tests if NodeAction initializes its listener list and resposes
 * to cookie changes. See http://www.netbeans.org/issues/show_bug.cgi?id=71764.
 */
public class Issue71764Test extends NbTestCase {
    
    public Issue71764Test(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(Issue71764Test.class);
        return suite;
    }
    
    public void test71764() {
        MockServices.setServices(ContextProvider.class);
        
        ContextProvider provider = Lookup.getDefault().lookup(ContextProvider.class);
        assertNotNull ("ContextProvider is not null.", provider);
        
        NodeAction action = (NodeAction) new TestAction ();
        Node node = new TestNode();

        assertFalse("Global Action should not be enabled yet", action.isEnabled());
        
        ContextProvider.current = node;
        provider.getLookup().lookup(Object.class);
        
        assertTrue("Global Action is enabled", action.isEnabled());
    }
    
    class TestNode extends AbstractNode {
        public TestNode() {
            super(Children.LEAF);
            getCookieSet().add(new SaveCookie() {
                public void save() throws IOException {
                    System.out.println("Save cookie called");
                }
            });
        }
    }
    
    public static class ContextProvider implements ContextGlobalProvider, Lookup.Provider {
        static Node current;
        Lookup lookup = Lookups.proxy (this );

        public Lookup createGlobalContext() {
            return lookup;
        }
    
        public Lookup getLookup() {
            return current == null ? Lookup.EMPTY : current.getLookup();
        }
    }
    
    public static class TestAction extends CookieAction {
        protected int mode() {
            return MODE_EXACTLY_ONE;
        }
        
        protected Class[] cookieClasses() {
            return new Class[] {SaveCookie.class};
        }
        
        protected void performAction(Node[] activatedNodes) {
            assert false;
        }
        public String getName() {
            return "TestAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
}
