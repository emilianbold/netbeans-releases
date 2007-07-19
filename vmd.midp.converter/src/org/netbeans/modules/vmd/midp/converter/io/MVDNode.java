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
 *
 */
package org.netbeans.modules.vmd.midp.converter.io;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.vmd.midp.converter.wizard.ConvertAction;
import org.openide.actions.EditAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.util.actions.SystemAction;

import javax.swing.*;

/**
 * @author David Kaspar
 */
public final class MVDNode extends FilterNode {

    private static final String ICON_RESOURCE = "org/netbeans/modules/vmd/midp/converter/resources/design.png"; // NOI18N

    public MVDNode (MVDDataObject dataObject) {
        super (JavaDataSupport.createJavaNode (dataObject.getPrimaryFile ()));
        ((AbstractNode) getOriginal ()).setIconBaseWithExtension (ICON_RESOURCE);
    }

    @Override
    public Action[] getActions (boolean context) {
        Action[] actions = super.getActions (context);
        if (actions == null  ||  actions.length <= 0)
            return actions;
        Action[] newActions = new Action[actions.length + 3];
        newActions[0] = SystemAction.get (ConvertAction.class);
        newActions[1] = null;
        newActions[2] = actions[0];
        newActions[3] = SystemAction.get (EditAction.class);
        System.arraycopy (actions, 1, newActions, 4, actions.length - 1);
        return newActions;
    }

    public Action getPreferredAction () {
        return SystemAction.get (ConvertAction.class);
    }

}
