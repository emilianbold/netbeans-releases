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
/*
 * Created on Apr 7, 2004
 */
package org.netbeans.modules.visualweb.insync.markup;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author cquinn
 */
public abstract class MarkupVisitor {

    public void apply(Node node) {
        visit(node);
        NamedNodeMap atts = node.getAttributes();
        if (atts != null) {
            int attc = atts.getLength();
            for (int i = 0; i < attc; i++)
                apply(atts.item(i));
        }
        NodeList kids = node.getChildNodes();
        int kidc = kids.getLength();
        for (int i = 0; i < kidc; i++)
            apply(kids.item(i));
    }

    public abstract void visit(Node node);
}
