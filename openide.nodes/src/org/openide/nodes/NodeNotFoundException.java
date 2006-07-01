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
package org.openide.nodes;

import java.io.IOException;


/** Exception indicating that a node could not be found while
* traversing a path from the root.
*
* @author Jaroslav Tulach
*/
public final class NodeNotFoundException extends IOException {
    static final long serialVersionUID = 1493446763320691906L;

    /** closest node */
    private Node node;

    /** name of child not found */
    private String name;

    /** depth of not founded node. */
    private int depth;

    /** Constructor.
    * @param node closest found node to the one being looked for
    * @param name name of child not found in that node
    * @param depth depth of the node that was found
    */
    NodeNotFoundException(Node node, String name, int depth) {
        this.node = node;
        this.name = name;
    }

    /** Get the closest node to the target that was able to be found.
     * @return the closest node
    */
    public Node getClosestNode() {
        return node;
    }

    /** Get the name of the missing child of the closest node.
     * @return the name of the missing child
    */
    public String getMissingChildName() {
        return name;
    }

    /** Getter for the depth of the closest node found.
    * @return the depth (0 for the start node, 1 for its child, etc.)
    */
    public int getClosestNodeDepth() {
        return depth;
    }
}
