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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.ProxyLookup;

/** Check problem with recursive loop.
 * @author Jaroslav Tulach
 */
public class CookieAction84636Test extends NbTestCase 
implements NodeListener, PropertyChangeListener {
    
    public CookieAction84636Test(String name) {
        super(name);
    }
    
    private SimpleCookieAction a1;
    private CookieNode n1;
    
    protected void setUp() throws Exception {
        a1 = SystemAction.get(SimpleCookieAction.class);
        n1 = new CookieNode();
        n1.setName("n1");
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testBasicUsage() throws Exception {
        a1.addPropertyChangeListener(this);
        
        FilterNode f1 = new FilterNode(n1);

        f1.addPropertyChangeListener(this);
        f1.addNodeListener(this);
        Action context = a1.createContextAwareInstance(f1.getLookup());
        
        
    }

    public void propertyChange(PropertyChangeEvent evt) {
    }

    public void childrenAdded(NodeMemberEvent ev) {
    }

    public void childrenRemoved(NodeMemberEvent ev) {
    }

    public void childrenReordered(NodeReorderEvent ev) {
    }

    public void nodeDestroyed(NodeEvent ev) {
    }

    
    public static class SimpleCookieAction extends CookieAction {
        protected int mode() {
            return MODE_EXACTLY_ONE;
        }
        protected Class[] cookieClasses() {
            return new Class[] {OpenCookie.class};
        }
        public static final List runOn = new ArrayList(); // List<List<Node>>
        protected void performAction(Node[] activatedNodes) {
            runOn.add(Arrays.asList(activatedNodes));
        }
        public String getName() {
            return "SimpleCookieAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }

        private boolean called = false;
        protected boolean enable(Node[] activatedNodes) {
            boolean retValue;
            if (called) {
                fail("Called already!");
            }
            called =true;
            
            retValue = super.enable(activatedNodes);
            return retValue;
        }
    }
    
    private static final class CookieNode extends AbstractNode {
        private static final class Open implements OpenCookie {
            public void open() {
                // do nothing
            }
        }
        public CookieNode() {
            super(Children.LEAF);
            getCookieSet().add(new Open());
        }
        public void setHasCookie(boolean b) {
            if (b && getCookie(OpenCookie.class) == null) {
                getCookieSet().add(new Open());
            } else if (!b) {
                OpenCookie o = getCookie(OpenCookie.class);
                if (o != null) {
                    getCookieSet().remove(o);
                }
            }
        }
    }
    
}

