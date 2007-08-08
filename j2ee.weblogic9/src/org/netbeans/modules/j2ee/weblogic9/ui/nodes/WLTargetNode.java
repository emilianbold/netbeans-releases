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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import javax.swing.Action;
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
    
    @Override
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }
}
