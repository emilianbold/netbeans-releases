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

package org.netbeans.modules.vmd.api.codegen;

import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.modules.vmd.api.model.Debug;
import org.openide.text.IndentEngine;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author David Kaspar
 */
public final class CodeWriter {

    private StyledDocument document;
    private String forceValue;
    private SimpleSection section;
    private int offset;
    private int endOffset;
    private Writer writer;
    private StringWriter memory;
    private boolean committed;

    public CodeWriter (StyledDocument document, SimpleSection section) {
        this (document, section.getStartPosition ().getOffset (), Integer.MIN_VALUE, null);
        this.section = section;
    }

    public CodeWriter (StyledDocument document, SimpleSection beforeSection, SimpleSection afterSection, String forceValue) {
        this (document, beforeSection.getEndPosition ().getOffset () + 1, afterSection.getStartPosition ().getOffset (), forceValue);
    }

    private CodeWriter (StyledDocument document, int beginOffset, int endOffset, String forceValue) {
        this.document = document;
        this.forceValue = forceValue;
        this.offset = beginOffset;
        this.endOffset = endOffset;
    }

    public CodeWriter write (String text) {
        assert ! committed;
        if (forceValue != null)
            return this;
        try {
            if (writer == null) {
                memory = new StringWriter (512);
                IndentEngine indentEngine = IndentEngine.find (document);
                if (indentEngine != null)
                    writer = indentEngine.createWriter (document, offset, memory);
                else
                    writer = memory;
            }
            writer.write (text);
            return this;
        } catch (IOException e) {
            throw Debug.error (e);
        }
    }

    public void commit () {
        assert ! committed;
        try {
            String text;
            if (forceValue != null) {
                text = forceValue;
            } else {
                if (writer != null) {
                    writer.flush ();
                    writer.close ();
                    text = memory.getBuffer ().toString ();
                } else
                    text = ""; // NOI18N
            }

            if (section != null)
                section.setText (text);
            else {
                if (endOffset != Integer.MIN_VALUE)
                    document.remove (offset, endOffset - offset);
                document.insertString (offset, text, null);
            }

            committed = true;
            writer = null;
            memory = null;
        } catch (IOException e) {
            throw Debug.error (e);
        } catch (BadLocationException e) {
            throw Debug.error (e);
        }
    }

    public boolean isCommitted () {
        return committed;
    }

}
