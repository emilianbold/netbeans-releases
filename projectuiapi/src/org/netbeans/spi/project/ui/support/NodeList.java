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

package org.netbeans.spi.project.ui.support;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;

/**
 * A <code>Children.Keys</code>-like abstration for use in 
 * {@link org.netbeans.spi.project.ui.support.NodeFactory}
 * instances. For utility methods of creating a <code>NodeList</code> instance, see
 * {@link org.netbeans.spi.project.ui.support.NodeFactorySupport}

 * @author mkleint
 * @since org.netbeans.modules.projectuiapi 1.17
 */
public interface NodeList {
    /**
     * child keys for which we later create a  {@link org.openide.nodes.Node}
     * in the node() method. If the change set of keys changes based on external
     *  events, fire a <code>ChangeEvent</code> to notify the parent Node.
     */
    List keys();
    /**
     * add a change listener, primarily to be used by the infrastructure
     */
    void addChangeListener(ChangeListener l); // change in keys()
    /**
     * remove a change listener, primarily to be used by the infrastructure
     */
    void removeChangeListener(ChangeListener l);
    /**
     * create Node for a given key, equal in semantics to <code>Children.Keys.createNode()</code>
     */
    Node node(Object key);
    /**
     * callback from Children instance of the parent node, equal in semantics to <code>Children.addNotify()</code>
     */
    void addNotify();
    /**
     * callback from Children instance of the parent node, equal in semantics to <code>Children.removeNotify()</code>
     */
    void removeNotify();
}
