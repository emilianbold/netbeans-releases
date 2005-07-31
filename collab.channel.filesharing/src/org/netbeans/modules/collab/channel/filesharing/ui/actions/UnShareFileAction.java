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
package org.netbeans.modules.collab.channel.filesharing.ui.actions;

import com.sun.collablet.CollabException;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.event.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.FilesharingEventNotifierFactory;
import org.netbeans.modules.collab.channel.filesharing.FilesharingEventProcessorFactory;
import org.netbeans.modules.collab.channel.filesharing.event.DeleteFileEvent;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventProcessor;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 * UnShareFileAction
 *
 * @author  Ayub Khan
 * @version 1.0
 */
public class UnShareFileAction extends SystemAction {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////	
    public static final String UNSHARE_FILE_ACTION = NbBundle.getMessage(
            UnShareFileAction.class, "LBL_UnshareFileAction_ActionName"
        ); //"Unshare"	

    ////////////////////////////////////////////////////////////////////////////
    // Instant variables
    ////////////////////////////////////////////////////////////////////////////	
    FilesharingContext context = null;
    Node node = null;

    public UnShareFileAction(FilesharingContext context, Node node) {
        this.context = context;
        this.node = node;
    }

    public String getName() {
        return UNSHARE_FILE_ACTION; //NoI18n
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        Debug.out.println("Unshare File, actionPerformed"); //NoI18n

        FileObject fObj = null;

        try {
            DataShadow lnk = (DataShadow) node.getCookie(DataShadow.class);
            EditorCookie ec = null;

            if (lnk != null) {
                Debug.out.println("SPN Original node path: " + lnk.getOriginal().getPrimaryFile().getPath());
                ec = (EditorCookie) lnk.getCookie(EditorCookie.class);
                fObj = lnk.getOriginal().getPrimaryFile();
            } else {
                DataObject d = (DataObject) node.getCookie(DataObject.class);
                ec = (EditorCookie) d.getCookie(EditorCookie.class);
                fObj = d.getPrimaryFile();
            }

            if (ec != null) {
                ec.open();
                ec.close();
            }

            Debug.out.println("Unshare file: " + fObj.getPath()); //NoI18n
            node.getParentNode().getChildren().remove(new Node[] { node });

            if (!context.getSharedFileGroupManager().isShared(fObj)) {
                return;
            }

            CollabFileHandler fh = context.getSharedFileGroupManager().getFileHandler(fObj);
            FileshareUtil.deleteFileLink(fh.getName(), context);

            EventContext evContext = new EventContext(DeleteFileEvent.getEventID(), fh);
            CollabEvent ce = new DeleteFileEvent(evContext);

            //create a notifer 
            EventNotifier fileChangeNotifier = null;
            EventProcessor ep = FilesharingEventProcessorFactory.getDefault().createEventProcessor(
                    context.getProcessorConfig(), context
                );
            fileChangeNotifier = FilesharingEventNotifierFactory.getDefault().createEventNotifier(
                    context.getNotifierConfig(), ep
                );

            if (fileChangeNotifier != null) {
                fileChangeNotifier.notify(ce);
            }
        }
        /*catch(IOException iox){
                iox.printStackTrace(Debug.out);
        }*/
        catch (CollabException e) {
            e.printStackTrace(Debug.out);
        }
    }
}
