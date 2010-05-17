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

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.windows.TopComponent;

import java.beans.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;


/**
 */
public class DocumentTabMarker implements PropertyChangeListener {
    private Set components = new HashSet();
    private boolean inUpdate = false;

    public void DocumentTabMarker() {
    }

    public void installListeners() {
        TopComponent.Registry reg = TopComponent.getRegistry();
        reg.addPropertyChangeListener(this);
        updateListening(reg.getOpened());
    }

    private void updateListening(Set newSet) {
        Set added = new HashSet(newSet);
        added.removeAll(components);

        Set removed = components;
        components.removeAll(newSet);

        components = new HashSet(newSet);

        // stop listening on closed TCs
        for (Iterator it = removed.iterator(); it.hasNext();) {
            TopComponent tc = (TopComponent) it.next();
            tc.removePropertyChangeListener(this);
        }

        // start listening on
        for (Iterator it = added.iterator(); it.hasNext();) {
            TopComponent tc = (TopComponent) it.next();

            if (accept(tc)) {
                updateDisplayName(tc, null);
                tc.addPropertyChangeListener(this);
            }
        }
    }

    private boolean accept(TopComponent tc) {
        // filter only interresting TCs
        return true;
    }

    private void updateDisplayName(TopComponent tc, String propName) {
        if (inUpdate) {
            return;
        }

        inUpdate = true;

        try {
            String orig = tc.getHtmlDisplayName();
            Node[] nodes = tc.getActivatedNodes();
            boolean displayMark = false;

            if (nodes != null) {
                for (int i = 0; i < nodes.length; i++) {
                    DataObject dragDataObject = (DataObject) nodes[i].getCookie(DataObject.class);

                    if (dragDataObject != null) {
                        FileObject fileObject = dragDataObject.getPrimaryFile();

                        try {
                            if (FilesharingCollablet.isShared(fileObject)) {
                                displayMark = true;

                                FilesharingContext context = FilesharingCollablet.getContext(fileObject);

                                //also add document listener
                                if (context != null) {
                                    if (!context.isReadOnlyConversation()) {
                                        //register document Listener
                                        CollabFileHandler fh = context.getSharedFileGroupManager().getFileHandler(
                                                fileObject
                                            );

                                        if (fh != null) {
                                            fh.registerDocumentListener();
                                        }
                                    } else {
                                        EditorCookie cookie = (EditorCookie) dragDataObject.getCookie(
                                                EditorCookie.class
                                            );

                                        if ((cookie != null) && (cookie.getOpenedPanes() != null)) {
                                            cookie.getOpenedPanes()[0].setEditable(false);
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                }
            }

            if (orig == null) orig = tc.getDisplayName();
            if (orig == null) orig = tc.getName();

            if (orig == null) return; // through finally

            if (orig.startsWith("<html>")) {
                return;
            }


            if (displayMark) {
                tc.setHtmlDisplayName(NbBundle.getMessage(DocumentTabMarker.class, "FMT_Mark", orig));
            }
        } finally {
            inUpdate = false;
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        TopComponent.Registry reg = TopComponent.getRegistry();
        Object source = pce.getSource();
        String name = pce.getPropertyName();

        if (reg == source) {
            if (TopComponent.Registry.PROP_OPENED.equals(name)) {
                updateListening(reg.getOpened());
            }
        } else if (source instanceof TopComponent) {
            if ("displayName".equals(name) || "htmlDisplayName".equals(name)) {
                updateDisplayName((TopComponent) source, name);
            }
        }
    }
}
