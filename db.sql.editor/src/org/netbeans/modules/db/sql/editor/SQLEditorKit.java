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

package org.netbeans.modules.db.sql.editor;

import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.modules.editor.NbEditorKit;

/**
 * This class implements the editor kit for text/x-sql files
 * in the editor
 *
 * @author Jesse Beaumont
 * @author Andrei Badea
 */
public class SQLEditorKit extends NbEditorKit {

    public static final String MIME_TYPE = "text/x-sql"; // NOI18N

    /**
     * Creates a new instance of SQLEditorKit 
     */
    public SQLEditorKit() { 
    }
    
    /**
     * Create a syntax object suitable for highlighting SQL syntax
     */
    public Syntax createSyntax(Document doc) {
        return new SQLSyntax();
    }
    
    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new SQLSyntaxSupport(doc);
    }
    
    /**
     * Retrieves the content type for this editor kit
     */
    public String getContentType() {
        return MIME_TYPE;
    }
}
