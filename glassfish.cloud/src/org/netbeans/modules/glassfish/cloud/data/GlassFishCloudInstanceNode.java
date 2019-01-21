/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.data;

import javax.swing.Action;
import org.netbeans.modules.glassfish.cloud.wizards.GlassFishCloudActionProperties;
import org.netbeans.modules.glassfish.cloud.wizards.GlassFishCloudActionRemoveInstance;
import org.openide.util.actions.SystemAction;

/**
 * GlassFish Cloud GUI Node.
 * <p>
 * this class represents cloud (CPAS) instance in GUI.
 * <p/>
 */
public class GlassFishCloudInstanceNode extends GlassFishInstanceNode {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Cloud instance object holding this node. */
    final GlassFishCloudInstance instance;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish cloud GUI node (Cloud item
     * in Services tab).
     * <p/>
     * @param instance GlassFish cloud instance holding this node.
     */
    public GlassFishCloudInstanceNode(GlassFishCloudInstance instance) {
        super(instance.getName(), instance);
        this.instance = instance;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get the set of actions that are associated with this node.
     * <p/>
     * @param context Whether to find actions for context meaning or for the
     *                node itself (ignored).
     * @return <code>List</code> of actions (you may include nulls
     *         for separators).
     */
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(GlassFishCloudActionProperties.class),
            SystemAction.get(GlassFishCloudActionRemoveInstance.class)
        };
    }

    /**
     * Get cloud instance object holding this node.
     * <p/>
     * @return Cloud instance object holding this node.
     */
    public GlassFishCloudInstance getInstance() {
        return instance;
    }

}
