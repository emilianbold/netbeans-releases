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
