/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        if (projectType == null || ProjectTypeInfo.getProjectTypeInfoFor (projectType) == null)
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
