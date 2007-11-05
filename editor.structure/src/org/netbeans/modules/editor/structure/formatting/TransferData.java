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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.structure.formatting;

import java.util.Arrays;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;

/**
 * This class is used to pass data to the formatters of embedded languages
 * @author tomslot
 */
public class TransferData {
    public static final String TRANSFER_DATA_DOC_PROPERTY = "TagBasedFormatter.TransferData"; //NOI18N
    
    public static final String ORG_CARET_OFFSET_DOCPROPERTY = "TagBasedFormatter.org_caret_offset";

    /**
     * Lines that must not be touched
     */
    private boolean[] formattableLines;
    /**
     * Indents before any formatter was called
     */
    private int[] originalIndents;
    /**
     * Indents after calling the current formatter.
     * It must be filled with valid data for at least
     * the current formatting range and the previous line
     */
    private int[] transformedOffsets;
    /**
     * Indents after calling the current formatter.
     * It must be filled with valid data for at least
     * the current formatting range and the previous line
     */
    private boolean[] alreadyProcessedByNativeFormatter;
    /**
     * Number of lines in the document
     */
    private int numberOfLines;

    public void init(BaseDocument doc) throws BadLocationException {
        numberOfLines = TagBasedLexerFormatter.getNumberOfLines(doc);
        formattableLines = new boolean[numberOfLines];
        alreadyProcessedByNativeFormatter = new boolean[numberOfLines];
        Arrays.fill(formattableLines, true);
        originalIndents = new int[numberOfLines];
        transformedOffsets = new int[numberOfLines];

        for (int i = 0; i < numberOfLines; i++) {
            originalIndents[i] = TagBasedLexerFormatter.getExistingIndent(doc, i);
        }

        doc.putProperty(TRANSFER_DATA_DOC_PROPERTY, this);
    }

    public static TransferData readFromDocument(BaseDocument doc) {
        return (TransferData) doc.getProperty(TRANSFER_DATA_DOC_PROPERTY);
    }

    public int getNumberOfLines() {
        return numberOfLines;
    }

    public boolean isFormattable(int line) {
        return formattableLines[line];
    }

    public void setNonFormattable(int line) {
        formattableLines[line] = false;
    }

    public int[] getTransformedOffsets() {
        return transformedOffsets;
    }

    public void setTransformedOffsets(int[] transformedOffsets) {
        this.transformedOffsets = transformedOffsets;
    }

    public int getOriginalIndent(int i) {
        return originalIndents[i];
    }

    public boolean wasProcessedByNativeFormatter(int line) {
        return alreadyProcessedByNativeFormatter[line];
    }

    public void setProcessedByNativeFormatter(int line) {
        alreadyProcessedByNativeFormatter[line] = true;
    }
}