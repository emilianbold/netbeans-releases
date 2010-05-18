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
