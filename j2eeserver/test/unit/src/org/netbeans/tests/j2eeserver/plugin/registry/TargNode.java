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
public class TargNode extends AbstractNode {
    static Node[] getNodes() {
        Node[] bogusNodes = new Node[] { Node.EMPTY.cloneNode(), Node.EMPTY.cloneNode() };
        bogusNodes[0].setName("Bogus1"); bogusNodes[0].setDisplayName("Bogus 1");
        bogusNodes[1].setName("Bogus2"); bogusNodes[1].setDisplayName("Bogus 2");
        return bogusNodes;
    }
    
    public TargNode(Targ targ) {
        super(new MyChildren(Arrays.asList(getNodes())));
        setDisplayName("Original:"+ targ.getName());
        setIconBase("org/netbeans/tests/j2eeserver/plugin/registry/target");
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        return new javax.swing.Action[] { 
            SystemAction.get(TargetAction.class) 
        };
    }
    
    public PropertySet[] getPropertySets() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.setName("TargetServer");
        ps.setDisplayName("Target Server");
        ps.put(new PropertySupport.ReadWrite(
            "DebugPort",  //NOI18N
            String.class,
            "Debug Name",   
            "Debug port number or share memory name") {
                public Object getValue() {
                    return "7485";
                }
                public void setValue(Object home) {
                }
        });
        return new PropertySet[] { ps };
    }

    public static class MyChildren extends Children.Array {
        public MyChildren(Collection nodes) {
            super(nodes);
        }
    }
    
    public static class TargetAction extends NodeAction {
        public String getName () { return "Target Action"; }
        
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        protected void performAction(Node[] activatedNodes) {
            System.out.println("Some one called target?");
        }
    }
}
