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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import java.util.*;

import javax.swing.*;

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
                EditorState editorState = editorStates[i];
                JEditorPane editorPane = editorState.getEditorPane();

                if (editorPane != null) {
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, editorPane: " + //NoI18n
                        editorPane.getName()
                    );
                    editorPane.setEnabled(false);
                }

                editorState.save();
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
                EditorState editorState = editorStates[i];

                if (editorState == null) {
                    continue;
                }

                editorState.resume();

                JEditorPane editorPane = editorState.getEditorPane();

                if (editorPane != null) {
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, editorPane: " + //NoI18n
                        editorPane.getName()
                    );
                    editorPane.setEnabled(true);
                    editorPane.requestFocus();
                }

                editorState = null;
            }
        }
    }
}
