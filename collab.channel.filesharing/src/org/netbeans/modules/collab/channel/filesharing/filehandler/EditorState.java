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
