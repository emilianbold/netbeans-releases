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

import java.awt.Image;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.openide.nodes.Children;
import org.openide.util.Utilities;


class LoadingNode extends BaseNode {

    public LoadingNode() {
        super(Children.LEAF);
        setName("dummy"); // NOI18N
        setDisplayName(I18n.getMessage("Loading")); // NOI18N
    }
    
    /** Implements AbstractCsmNode.getData() */
    public CsmObject getCsmObject() {
	return null;
    }

    @Override
    public Image getIcon(int param) {
        return Utilities.loadImage("org/netbeans/modules/cnd/classview/resources/waitNode.gif"); // NOI18N
    }
}
