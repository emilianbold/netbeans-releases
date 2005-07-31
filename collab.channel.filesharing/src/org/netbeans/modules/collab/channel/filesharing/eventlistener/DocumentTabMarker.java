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
            String orig = tc.getDisplayName();
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

            if ((orig != null) && orig.startsWith("<html>")) {
                return;
            }

            if (orig == null) {
                orig = tc.getName();
            }

            if (displayMark) {
                tc.setDisplayName(NbBundle.getMessage(DocumentTabMarker.class, "FMT_Mark", orig));
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
            if ("displayName".equals(name)) {
                updateDisplayName((TopComponent) source, name);
            }
        }
    }
}
