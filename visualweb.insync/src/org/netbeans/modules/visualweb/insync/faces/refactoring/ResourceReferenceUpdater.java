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
package org.netbeans.modules.visualweb.insync.faces.refactoring;

import org.w3c.dom.Node;

import org.netbeans.modules.visualweb.insync.faces.ElAttrUpdater;


/**
 * A MarkupVisitor that updates EL reference attributes that match a pattern.
 * <code>
 *   #{<oldname><tail>} => #{<newname><tail>}
 * </code?
 *
 * @author cquinn
 */
public class ResourceReferenceUpdater extends ElAttrUpdater {

    public ResourceReferenceUpdater(String oldname, String newname) {
        super(oldname, newname);
    }

    public static String update(String attr, String oldname, String newname) {
        if (attr.equals(oldname))
            return newname;
        return null;
    }

    public void visit(Node node) {
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            String val = node.getNodeValue();
            String newval = update(val, oldname, newname);
            if (newval != null)
                node.setNodeValue(newval);
        }
    }
}
