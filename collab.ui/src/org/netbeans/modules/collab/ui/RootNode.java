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
package org.netbeans.modules.collab.ui;

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.collab.core.Debug;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class RootNode extends AbstractNode {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String ICON_BASE = "org/netbeans/modules/collab/ui/resources/collab"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            SystemAction.get(ToolsAction.class), SystemAction.get(PropertiesAction.class),
        };

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CollabExplorerPanel explorerPanel;

    /**
     *
     *
     */
    public RootNode(CollabExplorerPanel explorerPanel) {
        super(Children.LEAF);
        setChildren(new SessionsNodeChildren(this, explorerPanel));
        this.explorerPanel = explorerPanel;
        setName(NbBundle.getBundle(RootNode.class).getString("LBL_RootNode")); // NOI18N
        setIconBase(ICON_BASE);
        setActions(DEFAULT_ACTIONS);

        //		// Add a listener to automatically expand a mounted context's nodes
        //		addNodeListener(
        //			new NodeAdapter()
        //			{
        //				public void childrenAdded(NodeMemberEvent event)
        //				{
        //					Node[] nodes=event.getDelta();
        //					for (int i=0; i<nodes.length; i++)
        //					{
        //						final Node node=nodes[i];
        //						SwingUtilities.invokeLater(
        //							new Runnable()
        //							{
        //								public void run()
        //								{
        //									// Even though the expandNode() method
        //									// modifies Swing objects, it doesn't
        //									// take into account that it requires
        //									// the Swing thread...<grumble>...
        //									JatoExplorerPanel.getInstance()
        //										.getTreeView().expandNode(node);
        //								}
        //							});
        //					}
        //				}
        //			});
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RootNode.class);
    }

    /**
     *
     *
     */
    public void setActions(SystemAction[] actions) {
        systemActions = actions;
    }

    /**
     *
     *
     */
    public Node.Handle getHandle() {
        return new Handle();
    }

    /**
     *
     *
     */
    void bug_5071137_workaround() {
        Debug.out.println("ATTEMPTING WORKAROUND FOR BUG 5071137");
        ((SessionsNodeChildren) getChildren()).refreshCollabManagerListener();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Handle for this node, it is serialized instead of node
     *
     */

    /*pkg*/ static final class Handle extends Object implements Node.Handle {
        static final long serialVersionUID = 1L;

        public Node getNode() {
            return new RootNode(CollabExplorerPanel.getInstance());
        }
    }
}
