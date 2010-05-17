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
package org.netbeans.modules.collab.channel.filesharing.eventlistener;

import org.openide.*;

import javax.swing.event.*;
import javax.swing.text.*;

import org.netbeans.modules.collab.channel.filesharing.event.DocumentChangeInsert;
import org.netbeans.modules.collab.channel.filesharing.event.DocumentChangeRemove;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.DocumentEventContext;
import org.netbeans.modules.collab.core.Debug;


/**
 * Document Listener attached to each shared file being handled
 *
 * @author  Todd Fast <todd.fast@sun.com>
 * @version 1.0
 */
public class FilesharingDocumentListener extends CollabDocumentListener {
    private boolean skipUpdate;

    /**
     * constructor for CollabDocumentListener
     *
     * @param document
     * @param fileHandler
     */
    public FilesharingDocumentListener(Document document, EventNotifier eventNotifier) {
        super(document, eventNotifier);
    }

    /**
     *
     * @param documentEvent
     */
    public void changedUpdate(DocumentEvent documentEvent) {
        // Ignore
    }

    /**
     * invoked during document insert
     *
     * @param event
     */
    public void insertUpdate(DocumentEvent event) {
        Debug.log("CollabFileHandlerSupport", "FilesharingDocumentListener, insertUpdate: ");

        if (isSkipUpdate()) {
            return;
        }

        int offset = event.getOffset();
        String text = null;

        try {
            text = document.getText(offset, event.getLength());
        } catch (Exception e) {
            remove(e);
        }

        EventContext evContext = new DocumentEventContext(
                DocumentChangeInsert.getEventID(), document, offset, text.length(), text
            );
        CollabEvent ce = new DocumentChangeInsert(evContext);

        try {
            eventNotifier.notify(ce);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * invoked during document remove
     *
     * @param event
     */
    public void removeUpdate(DocumentEvent event) {
        Debug.log("CollabFileHandlerSupport", "FilesharingDocumentListener, removeUpdate: ");

        if (isSkipUpdate()) {
            return;
        }

        EventContext evContext = new DocumentEventContext(
                DocumentChangeRemove.getEventID(), document, event.getOffset(), event.getLength(), null
            );
        CollabEvent ce = new DocumentChangeRemove(evContext);

        try {
            eventNotifier.notify(ce);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * invoked during listener removal
     * @param e
     */
    public void remove(Throwable e) {
        ErrorManager.getDefault().notify(
            ErrorManager.getDefault().annotate(e, "Removing document listener due to exception")
        ); // NOI18N
        remove();
    }

    /**
     *
     *
     */
    public void remove() {
        document.removeDocumentListener(this);
    }

    public void setSkipUpdate(boolean skip) {
        this.skipUpdate = skip;
    }

    public boolean isSkipUpdate() {
        return this.skipUpdate;
    }
}
