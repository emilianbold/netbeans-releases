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
 * CssDataNode.java
 *
 * Created on December 8, 2004, 11:05 PM
 */

package org.netbeans.modules.css.loader;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

/**
 * Node that represents the CSS data object
 * @author Winston Prakash
 * @version 1.0
 */
public class CssDataNode extends DataNode{
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/css/resources/css.gif";
    /** Creates a new instance of CssDataNode */
    public CssDataNode(CssDataObject cssDataObject) {
        super(cssDataObject, Children.LEAF);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
    }
}
