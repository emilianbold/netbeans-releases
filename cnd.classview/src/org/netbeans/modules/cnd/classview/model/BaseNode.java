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

package org.netbeans.modules.cnd.classview.model;

import java.util.Enumeration;
import org.openide.nodes.*;
import org.netbeans.modules.cnd.modelutil.*;

/**
 * @author Vladimir Kvasihn
 */
public abstract class BaseNode extends AbstractCsmNode {

    public BaseNode() {
        super(Children.LEAF);
    }

    public BaseNode(Children children) {
        super(children);
    }

    protected interface Callback {
        void call(BaseNode node);
    }
    
    /** 
     * Traversing nodes
     * for tracing/debugging purposes 
     */
    protected void traverse(Callback callback) {
        callback.call(this);
        for( Enumeration children = getChildren().nodes(); children.hasMoreElements(); ) {
            Node child = (Node) children.nextElement();
            if( child instanceof BaseNode ) {
                ((BaseNode) child).traverse(callback);
            }
        }
    }
}
