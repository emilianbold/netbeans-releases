/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
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
    public void bug_5071137_workaround() {
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
