/**************************************************************************
 *
 *                          Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the HTTP Javadoc Filesystem.
 * The Initial Developer of the Original Code is Jeffrey A. Keyser.
 * Portions created by Jeffrey A. Keyser are Copyright (C) 2000-2002.
 * All Rights Reserved.
 *
 * Contributor(s): Jeffrey A. Keyser.
 *
 **************************************************************************/


package org.netbeans.modules.javadoc.httpfs;

import java.awt.event.ActionEvent;

import org.openide.actions.FileSystemAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 *  Performs the action of refreshing this file system from the web site.
 *
 *  @since 3.4
 */
class RefreshAction
    extends org.openide.util.actions.NodeAction {

    /**
     *  Constructs a <code>RefreshAction</code> used to refresh the file system.
     *
     *  @since 3.4
     */
    RefreshAction(
    ) {
    }


    /**
     *  Returns the name of this action to display in the context menu.
     *
     *  @return The name of the action.
     *
     *  @since 3.4
     */
    public String getName(
    ) {

        return NbBundle.getMessage(RefreshAction.class, "RefreshFS" );   // NOI18N

    }

    /**
     *  Tests the selected nodes if they are HTTPRootFileObject items, and if so,
     *  allows this action to be displayed.
     *
     *  @param activatedNodes Current activated nodes.
     *
     *  @return <code>true</code> to be enabled, <code>false</code> to be disabled
     *
     *  @since 3.4
     */
    protected boolean enable(
        Node[]  activatedNodes
    ) {

        // Flags whether all of the selected nodes represent HTTPRootFileObject nodes
        boolean     rootFolderSelected;
        // Index into the array of nodes
        int         nodeIndex;
        // Object behind the node
        DataObject  dataObject;
        // File object behind the node
        FileObject  fileObject;


        rootFolderSelected = true;
        nodeIndex = 0;

        // Loop through all the nodes passed to this routine
        while( nodeIndex < activatedNodes.length && rootFolderSelected ) {

            // Get the node object
            dataObject = (DataObject)activatedNodes[ nodeIndex ].getCookie( DataObject.class );
            if( dataObject != null ) {

                // Get the file
                fileObject = dataObject.getPrimaryFile( );

                // If this file is not an HTTPRootFileObject,
                if( !( fileObject instanceof HTTPRootFileObject ) ) {

                    // Flag that this action doesn't apply
                    rootFolderSelected = false;

                }

            } else {

                // Flag that this action doesn't apply
                rootFolderSelected = false;

            }
            nodeIndex++;

        }
        return rootFolderSelected;

    }


    /**
     *  Return the default help context.
     *
     *  @return The help context for this action
     *
     *  @since 3.4
     */
    public HelpCtx getHelpCtx(
    ) {

        return HelpCtx.DEFAULT_HELP;

    }


    /**
     *  Perform this action on the currently selected list of nodes.
     *
     *  @param activatedNodes Current activated nodes.
     *
     *  @since 3.4
     */
    protected void performAction(
        Node[]  activatedNodes
    ) {

        // Index into the list of nodes
        int                 nodeIndex;
        // Data object behind each node
        DataObject          dataObject;
        // File object behind each node
        HTTPRootFileObject  rootFileObject;


        // Loop through each of the nodes passed
        for( nodeIndex = 0; nodeIndex < activatedNodes.length; nodeIndex++ ) {

            // Refresh the contents of each file system selected
            dataObject = (DataObject)activatedNodes[ nodeIndex ].getCookie( DataObject.class );
            rootFileObject = (HTTPRootFileObject)dataObject.getPrimaryFile( );
            rootFileObject.triggerRefresh( );

        }

    }

}
