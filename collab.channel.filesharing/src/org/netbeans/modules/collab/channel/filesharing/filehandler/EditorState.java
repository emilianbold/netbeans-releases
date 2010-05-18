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

import org.openide.text.*;

import java.awt.Color;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;


/**
 * Bean that holds editorPane of a document, as well as its state
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class EditorState extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private JEditorPane editorPane = null;
    private int refSave = 0;
    private Position reference = null;
    private int caretPosition = 0;
    private Color selectedTextColor = null;
    private int selectionStart = 0;
    private int selectionEnd = 0;
    private Color selectionColor = null;
    private Caret caret = null;

    /**
         * constructor
         *
         * @param editorPane
         */
    public EditorState(JEditorPane editorPane) {
        super();
        this.editorPane = editorPane;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
         *
         * @return editorPane
         */
    public JEditorPane getEditorPane() {
        return this.editorPane;
    }

    /**
         *
         * @return editorPane
         */
    public void setEditorPane(JEditorPane editorPane) {
        this.editorPane = editorPane;
    }

    /**
         *
         */
    public void save() {
        caret = editorPane.getCaret();
        caretPosition = caret.getDot(); //editorPane.getCaretPosition();

        selectedTextColor = editorPane.getSelectedTextColor();
        selectionStart = editorPane.getSelectionStart();
        selectionEnd = editorPane.getSelectionEnd();

        selectionColor = editorPane.getSelectionColor();

        refSave = caretPosition;

        if (selectionStart < refSave) {
            refSave = selectionStart;
        }

        if (selectionEnd < refSave) {
            refSave = selectionEnd;
        }

        try {
            reference = NbDocument.createPosition(editorPane.getDocument(), refSave, Position.Bias.Forward);
        } catch (javax.swing.text.BadLocationException ex) {
            reference = null;
        }
    }

    /**
         *
         */
    public void resume() {
        int adjust = 0;

        if (reference != null) {
            adjust = reference.getOffset() - refSave;
        }

        try {
            caret.setDot(caretPosition + adjust);

            //editorPane.setCaretPosition(caretPosition+adjust);
            editorPane.setSelectedTextColor(selectedTextColor);
            editorPane.setSelectionStart(selectionStart + adjust);
            editorPane.setSelectionEnd(selectionEnd + adjust);

            editorPane.setSelectionColor(selectionColor);
        } catch (java.lang.IllegalArgumentException iargs) {
            //ignore
        }

        //release position
        reference = null;
    }
}
