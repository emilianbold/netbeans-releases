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

package org.netbeans.modules.web.struts;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

/**
 *
 * @author Petr Pisl
 */

public class StrutsConfigNode extends DataNode {

    public static final String ICON_BASE =  "org/netbeans/modules/web/struts/resources/StrutsConfigIcon.png";

    /** Creates a new instance of StrutsConfigNode */
    public StrutsConfigNode (final StrutsConfigDataObject dataObject) {
	super(dataObject,Children.LEAF);
        setIconBaseWithExtension(ICON_BASE);
    }

    // test to see if we can use DeleteAction
    public boolean canDestroy() {
	return true;
    }
}
