/*
 *                 Sun Public License Notice
 *
 * The contents of thisfile are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.tests.j2eeserver.plugin.registry;

import org.openide.nodes.*;
import org.openide.util.actions.*;
import java.util.*;
import org.netbeans.tests.j2eeserver.plugin.jsr88.*;

/**
 *
 * @author  nn136682
 */
public class PluginNode extends AbstractNode {
    static java.util.Collection bogusNodes = java.util.Arrays.asList(new Node[] { Node.EMPTY, Node.EMPTY });
    
    // PENDING use Children.Keys()
    public PluginNode(DepFactory factory) {
        super(new MyChildren(bogusNodes));
        setDisplayName("Original:"+factory.getDisplayName());
        setIconBase("org/netbeans/tests/j2eeserver/plugin/registry/plugin");
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        return new javax.swing.Action[] { 
            SystemAction.get(BogusAction.class) 
        };
    }

    public static class MyChildren extends Children.Array {
        public MyChildren(Collection nodes) {
            super(nodes);
        }
    }
    
    public static class BogusAction extends NodeAction {
        public String getName () { return "Bogus"; }
        
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        protected void performAction(Node[] activatedNodes) {
            System.out.println("Some one called Bogus?");
        }
    }
}
