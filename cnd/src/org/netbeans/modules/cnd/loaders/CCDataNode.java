/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.loaders;

import org.openide.nodes.Children;

/** A node to represent the C++ src object */
public class CCDataNode extends CndDataNode {

    /** The base name of the C++ source icon */
    private static final String CCSrcIcon =
		    "org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"; // NOI18N

    public CCDataNode(CndDataObject obj) {
	super(obj, Children.LEAF, CCSrcIcon);
    }

    public CCDataNode(CndDataObject obj, Children ch) {
	super(obj, ch, CCSrcIcon);
    }
}
