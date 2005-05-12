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
 *
 * @author Martin Adamek
 */
public class WLTargetNode extends AbstractNode {
    
    public WLTargetNode(Lookup lookup) {
        super(new Children.Array());
    }
    
    public int hashCode() {
        return super.hashCode();
    }
    
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
}
