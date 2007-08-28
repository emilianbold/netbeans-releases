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
package org.netbeans.modules.vmd.io.javame;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.vmd.api.io.ProjectTypeInfo;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.openide.actions.EditAction;
import org.openide.actions.OpenAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

import javax.swing.*;

/**
 * @author David Kaspar
 */
public final class MEDesignNode extends FilterNode {

    public MEDesignNode (Node originalNode) {
        super (originalNode);
    }

    public MEDesignNode (MEDesignDataObject dataObject) {
        super (JavaDataSupport.createJavaNode (dataObject.getPrimaryFile ()));
        String projectType = IOSupport.resolveProjectType (IOSupport.getDataObjectContext (dataObject));
        if (projectType == null)
            return;
        String iconResource = ProjectTypeInfo.getProjectTypeInfoFor (projectType).getIconResource ();
        ((AbstractNode) getOriginal ()).setIconBaseWithExtension (iconResource);
    }

    @Override
    public Action[] getActions (boolean context) {
        Action[] actions = super.getActions (context);
        if (actions == null  ||  actions.length <= 0)
            return actions;
        Action[] newActions = new Action[actions.length + 1];
        newActions[0] = actions[0];
        newActions[1] = SystemAction.get (EditAction.class);
        System.arraycopy (actions, 1, newActions, 2, actions.length - 1);
        return newActions;
    }

    public Action getPreferredAction () {
        return SystemAction.get (OpenAction.class);
    }

}
