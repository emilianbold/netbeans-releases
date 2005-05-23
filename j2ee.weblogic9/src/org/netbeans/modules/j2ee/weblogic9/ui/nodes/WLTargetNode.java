/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * A node that represents a concrete target for a particuler server instance.
 * As it gets filtered and does not appear in the registry we do not implement
 * anything special.
 *
 * @author Kirill Sorokin
 */
public class WLTargetNode extends AbstractNode {
    
    /**
     * Creates a new instance of the WSTargetNode.
     * 
     * @param lookup a lookup object that contains the objects required for 
     *      node's customization, such as the deployment manager
     */
    public WLTargetNode(Lookup lookup) {
        super(new Children.Array());
    }
    
    /**
     * A fake implementation of the Object's hashCode() method, in order to 
     * avoid FindBugsTool's warnings
     */
    public int hashCode() {
        return super.hashCode();
    }
    
    /**
     * A fake implementation of the Object's hashCode() method, in order to 
     * avoid FindBugsTool's warnings
     */
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
}
