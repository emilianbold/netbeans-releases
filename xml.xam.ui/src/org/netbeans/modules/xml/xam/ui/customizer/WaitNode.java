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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.customizer;

import java.util.Collection;
import java.util.Collections;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * A placeholder node that displays a "please wait" message while the
 * task to generate the final node is performed.
 *
 * @author  Nathan Fiedler
 */
public class WaitNode extends AbstractNode {
    /** A child key for this node, to be used with Children.Key.setKeys(). */
    public static final Object WAIT_KEY = new Object();

    /**
     * Creates a new instance of WaitNode.
     */
    public WaitNode() {
        super(Children.LEAF);
        setName(NbBundle.getMessage(WaitNode.class, "LBL_WaitNode_Wait"));
        setIconBaseWithExtension("org/openide/src/resources/wait.gif");
    }

    /**
     * Convenience method that creates an array with a single WaitNode.
     *
     * @return  array with a WaitNode.
     */
    public static Node[] createNode() {
        return new Node[] { new WaitNode() };
    }

    /**
     * Convenience method that creates a collection with a single child key
     * entry, that being the WAIT_KEY value.
     *
     * @return  collection with WAIT_KEY.
     */
    public static Collection getKeys() {
        return Collections.singletonList(WaitNode.WAIT_KEY);
    }
}
