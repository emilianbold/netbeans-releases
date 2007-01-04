/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2001-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.shell;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.editor.*;
import org.netbeans.modules.editor.*;

import org.netbeans.modules.cnd.MIMENames;

/**
* Shell editor kit with appropriate document
*
*/

public class ShellKit extends NbEditorKit {

    public String getContentType() {
        return MIMENames.SHELL_MIME_TYPE;
    }

    public void install(JEditorPane c) {
        super.install(c);
    }

    /** Create new instance of syntax coloring scanner
    * @param doc document to operate on. It can be null in the cases the syntax
    *   creation is not related to the particular document
    */
    public Syntax createSyntax(Document doc) {
        return new ShellSyntax();
    }

    public Document createDefaultDocument() {
        Document doc = super.createDefaultDocument();
        doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, BaseDocument.LS_LF);
        return doc;
    }
}
