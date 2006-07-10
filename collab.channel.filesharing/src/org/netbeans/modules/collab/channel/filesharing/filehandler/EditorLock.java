/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import java.util.*;

import javax.swing.*;

import org.openide.util.Mutex;
import org.netbeans.modules.collab.core.Debug;


/**
 * Bean that holds editorStates of a document
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class EditorLock extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private EditorState[] editorStates = null;

    /**
         * constructor
         *
         * @param editorStates
         */
    public EditorLock(JEditorPane[] editorPanes) {
        super();
        setEditorPanes(editorPanes);
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
         *
         * @return editorPanes
         */
    public JEditorPane[] getEditorPanes() {
        List editorPaneList = new ArrayList();

        if (editorStates != null) {
            for (int i = 0; i < editorStates.length; i++) {
                EditorState editorState = editorStates[i];

                if (editorState != null) {
                    editorPaneList.add(editorState.getEditorPane());
                }
            }
        }

        return (JEditorPane[]) editorPaneList.toArray(new JEditorPane[0]);
    }

    /**
         *
         * @param editorPanes
         */
    public void setEditorPanes(JEditorPane[] editorPanes) {
        List editorStateList = new ArrayList();

        if (editorPanes != null) {
            for (int i = 0; i < editorPanes.length; i++) {
                if (editorPanes[i] != null) {
                    editorStateList.add(new EditorState(editorPanes[i]));
                }
            }
        }

        this.editorStates = (EditorState[]) editorStateList.toArray(new EditorState[0]);
    }

    /**
         *
         */
    public void lock() {
        Debug.log("CollabFileHandlerSupport", "EditorLock, lock");

        if (editorStates != null) {
            for (int i = 0; i < editorStates.length; i++) {
                final EditorState editorState = editorStates[i];
                final JEditorPane editorPane = editorState.getEditorPane();

                if (editorPane != null) {
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, editorPane: " + //NoI18n
                        editorPane.getName()
                    );
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
                            editorPane.setEditable(false);
                            editorState.save();
                        }
                    });
                }

            }
        }
    }

    /**
         *
         */
    public void releaseLock() {
        Debug.log("CollabFileHandlerSupport", "EditorLock, releaseLock");

        if (editorStates != null) {
            for (int i = 0; i < editorStates.length; i++) {
                final EditorState editorState = editorStates[i];

                if (editorState == null) {
                    continue;
                }


                final JEditorPane editorPane = editorState.getEditorPane();

                if (editorPane != null) {
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, editorPane: " + //NoI18n
                        editorPane.getName()
                    );
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
                            //editorState.resume();
                            editorPane.setEditable(true);
                            // editorPane.requestFocus();
                        }
                    });
                }
            }
        }
    }
}
