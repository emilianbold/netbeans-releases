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
 * PPDocumentSource.java
 *
 * Created on February 6, 2004, 1:49 PM
 */
package org.netbeans.modules.mobility.project.preprocessor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;

/**
 *
 * @author  Adam Sotona
 */
public class PPDocumentSource implements CommentingPreProcessor.Source {
    
    private final Document document;
    
    /** Creates a new instance of PPDocumentSource */
    public PPDocumentSource(Document document) {
        this.document=document;
    }
    
    public Reader createReader() throws IOException {
        try {
            return new StringReader(document.getText(0, document.getLength()));
        } catch (BadLocationException ble) {
            throw new IOException(ble.getLocalizedMessage());
        }
    }
}
