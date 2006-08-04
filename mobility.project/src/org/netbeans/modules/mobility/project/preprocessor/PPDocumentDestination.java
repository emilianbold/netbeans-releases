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

/*
 * PPDocumentDestination.java
 *
 * Created on February 6, 2004, 4:14 PM
 */
package org.netbeans.modules.mobility.project.preprocessor;

import java.io.IOException;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;

/**
 *
 * @author  Adam Sotona
 */
public final class PPDocumentDestination implements CommentingPreProcessor.Destination {
    
    private final BaseDocument document;
    
    
    /** Creates a new instance of PPDocumentDestination */
    public PPDocumentDestination(BaseDocument document) {
        this.document=document;
    }
    
    public void doInsert(final int line, final String s) throws IOException {
        try {
            document.insertString(Utilities.getRowStartFromLineOffset(document, line - 1), s, null);
        } catch (BadLocationException ble) {
            throw new IOException(ble.getLocalizedMessage());
        }
    }
    
    public void doRemove(final int line, final int column, final int length) throws IOException {
        try {
            document.remove(Utilities.getRowStartFromLineOffset(document, line - 1) + column, length);
        } catch (BadLocationException ble) {
            throw new IOException(ble.getLocalizedMessage());
        }
    }
    
    public Writer createWriter(@SuppressWarnings("unused")
	final boolean validOutput) {
        return null;
    }
    
    
    
}
