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
import java.io.IOException;

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

            //node detroy
//            node.destroy();
//        } catch (IOException iox) {
//            iox.printStackTrace(Debug.out);
        } catch (CollabException e) {
            e.printStackTrace(Debug.out);
        }
    }
}
