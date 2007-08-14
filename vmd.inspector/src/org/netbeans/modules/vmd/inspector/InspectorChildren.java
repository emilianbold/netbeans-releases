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
package org.netbeans.modules.vmd.inspector;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Karol Harezlak
 */
final class InspectorChildren extends Children.Keys<AbstractNode> {

    private List<AbstractNode> keys = new ArrayList<AbstractNode>();

    protected Node[] createNodes (AbstractNode key) {
        return new Node[] { key };
    }

    void setKeys(List<AbstractNode> keys){
        if (keys == null)
            return;
        this.keys = keys;
        addNotify();
    }
    
    protected void addNotify() {
        if (keys == null)
            return;
        super.setKeys(keys);
        super.addNotify();
    }
    
    protected void removeNotify() {
        keys = Collections.emptyList ();
        super.removeNotify();
    }

}
